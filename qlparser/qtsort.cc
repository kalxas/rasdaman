/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/


#include "config.h"

#include "qlparser/qtsort.hh"
#include "qlparser/qtnode.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtdomainoperation.hh"
#include "qlparser/qtoperation.hh"
#include "qlparser/qtconst.hh"

#include "relcatalogif/mdddimensiontype.hh"
#include "relcatalogif/mdddomaintype.hh"

#include "mddmgr/mddobj.hh"
#include "tilemgr/tile.hh"

#include <tuple>
#include <list>
#include <iterator>
#include <vector>
#include <set>
#include <memory>
#include <string>
#include <iostream>

#include <logging.hh>

const QtNode::QtNodeType QtSort::nodeType = QtNode::QT_SORT;

QtSort::QtSort(QtOperation *MDDtoSortInput, r_Dimension axis)
    : QtOperation(),
    MDDtoSort(MDDtoSortInput), sortAsc(false), sortAxis(axis), applyRankings(false)
{
    if (MDDtoSort)
    {
        MDDtoSort->setParent(this);
    }
}

QtSort::QtSort(QtOperation *MDDtoSortInput, const std::string &axis)
    : QtOperation(),
    MDDtoSort(MDDtoSortInput), sortAsc(false), namedAxisFlag(true), axisName(axis), applyRankings(false)
{
    if (MDDtoSort)
    {
        MDDtoSort->setParent(this);
    }
}

QtSort::QtSort(QtOperation *MDDtoSortInput, r_Dimension axis, bool order, QtOperation *ranksInput)
    : QtOperation(),
    MDDtoSort(MDDtoSortInput), ranks(ranksInput), sortAsc(order), sortAxis(axis), applyRankings(true)
{
    if (MDDtoSort)
    {
        MDDtoSort->setParent(this);
    }

    if (ranks)
    {
        ranks->setParent(this);
    }
}

QtSort::QtSort(QtOperation *MDDtoSortInput, const std::string &axis, bool order, QtOperation *ranksInput)
    : QtOperation(),
    MDDtoSort(MDDtoSortInput), ranks(ranksInput), sortAsc(order), namedAxisFlag(true), axisName(axis), applyRankings(true)
{
    if (MDDtoSort)
    {
        MDDtoSort->setParent(this);
    }

    if (ranks)
    {
        ranks->setParent(this);
    }
}

QtSort::~QtSort()
{
    if (namedAxisFlag)
    {
        if (axisNamesCorrect != NULL) delete axisNamesCorrect;
        axisNamesCorrect = NULL;
    }
    //do not delete MDDtoSort, since QtAxisSDom tries to delete its pointer later (shared).
    if (ranks)
    {
        delete ranks;
        ranks = NULL;
    }
}

void
QtSort::simplify()
{
    // In order to work bottom up, first inspect the descendants
    QtNode::simplify();

    // Test, if operand is available.
    if (MDDtoSort)
    {
        // Test, if operand is of const type.
        if (MDDtoSort->getNodeType() ==  QT_CONST)
        {
            // evaluate the self node with no input list
            QtData *newConst = this->evaluate(NULL);

            if (newConst)
            {
                // create a new constant node and fill it with newConst
                QtConst *newNode = new QtConst(newConst);

                // set its data stream type
                newNode->checkType(NULL);

                // link it to the parent
                getParent()->setInput(this, newNode);

                // delete the self node and its descendants
                delete this;
            }
        }
    }
}

QtNode::QtNodeList *
QtSort::getChilds(QtChildType flag)
{
    QtNodeList *resultList = NULL;
    resultList = QtOperation::getChilds(flag);

    if (MDDtoSort)
    {
        if (flag == QT_LEAF_NODES || flag == QT_ALL_NODES)
        {
            QtNodeList *subList;

            subList = MDDtoSort->getChilds(flag);

            // remove all elements in subList and insert them at the beginning in resultList
            resultList->splice(resultList->begin(), *subList);

            // delete temporary subList
            delete subList;
            subList = NULL;
        }

        // add the nodes of the current level
        if (flag == QT_DIRECT_CHILDS || flag == QT_ALL_NODES)
        {
            resultList->push_back(MDDtoSort);
        }
    }
    return resultList;
}

QtNode::QtAreaType
QtSort::getAreaType()
{
    return (MDDtoSort->getAreaType());
}

bool
QtSort::equalMeaning(QtNode *node)
{
    bool result = false;

    if (nodeType == node->getNodeType())
    {
        QtSort *sortNode;
        sortNode = static_cast<QtSort *>(node); // by force

        result = MDDtoSort->equalMeaning(sortNode->getMDDtoSort()) &&
                 ranks->equalMeaning(sortNode->getRanks());
    }
    return (result);
}

void
QtSort::sort()
{
    //as per std::list documentation slicesList is always initialized by the default empty constructor
    //this function returns nothing, it only reorders the elements of the list.
    if(this->sortAsc)
    {//sort ascending <
        slicesList.sort([this](sliceTuple& a, sliceTuple& b)
        {
            return accessSliceRank(a) < accessSliceRank(b);
        });
    }
    else
    {//sort descending >
        slicesList.sort([this](sliceTuple& a, sliceTuple& b)
        {
            return accessSliceRank(a) > accessSliceRank(b);
        });
    }
}

void
QtSort::appendSlice( sliceMDD *sliceInput, sliceRank rank)
{
    //add tuple to slice_List
    slicesList.push_back(std::make_tuple(sliceInput, rank));
}

sliceMDD*
QtSort::accessSliceMDD(sliceTuple &sT)
{
    //getter and setter
    return std::get<0>(sT);
}

sliceRank&
QtSort::accessSliceRank(sliceTuple &sT)
{
    //getter and setter
    return std::get<1>(sT);
}

std::string
QtSort::toString()
{
    std::string stateString = "";
    std::string str1, str2;
    auto it = slicesList.begin();
    for(it; it != slicesList.end(); ++it)
    {
        QtMDD *qtMDDObj = static_cast<QtMDD *>(accessSliceMDD(*it));
        MDDObj *temp = qtMDDObj->getMDDObject();

        //using separate variables to avoid manipulating the data since the access methods can read and write.
        str1 = temp->getArrayInfo(common::PrintTiles::EMBEDDED);
        str2 = std::to_string( accessSliceRank(*it) );

        stateString = stateString +
        "slice: "  + str1 +"\t" +
        "rank: "   + str2 +"\n" ;
    }
    return stateString;
}

void
QtSort::optimizeLoad(QtTrimList *trimList)
{
    QtNode::QtTrimList *list1 = NULL;

    if (MDDtoSort)
    {
        list1 = trimList;

        if (MDDtoSort)
        {
            MDDtoSort->optimizeLoad(list1);
        }
    }
    else
    {
        // release( trimList->begin(), trimList->end() );
        for (QtNode::QtTrimList::iterator iter = trimList->begin(); iter != trimList->end(); iter++)
        {
            delete *iter;
            *iter = NULL;
        }
        delete trimList;
        trimList = NULL;
    }
}

void
QtSort::getAxisFromName()
{
    bool found = false;
    std::vector<std::string> axisNamesVector = *axisNamesCorrect;
    std::vector<std::string>::iterator axisNamesVectorIt;

    r_Dimension count = 0;// loop through all axes names in the array.
    for(axisNamesVectorIt = axisNamesVector.begin(); axisNamesVectorIt != axisNamesVector.end(); axisNamesVectorIt++, count++)
    {
        if( (*axisNamesVectorIt).compare(axisName) == 0 )//if the name matches one of the names in the vector, take that axis
        {
            sortAxis = count;
            found = true;
            break;
        }
    }
    if(!found)// in case the name does not correspond to any axis
    {
        LERROR <<"Error: QtSort::getAxisFromName() - Name of the axis doesn't correspond with any defined axis name of the type.";
        ALONGClauseParseInfo.setErrorNo(347);
        throw ALONGClauseParseInfo;
    }
}

const QtTypeElement &
QtSort::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_MDD);

    // check operand branches
    if(MDDtoSort)
    {
        // get input type
        const QtTypeElement &inputType1 = MDDtoSort->checkType(typeTuple);

        if (inputType1.getDataType() != QT_MDD)
        {
            LERROR << "QtSort::checkType() - MDDtoSort operand is not of type MDD.";
            parseInfo.setErrorNo(MDDARGREQUIRED);
            throw parseInfo;
        }
        dataStreamType.setType(inputType1.getType());

        // if axis name given, get actual axes names of array to be sorted.
        if(namedAxisFlag)
        {
            r_Minterval domainDef = *((static_cast<MDDDomainType *>(const_cast<Type *>(inputType1.getType())))->getDomain());
            std::vector<std::string> axisDef = (&domainDef)->get_axis_names();
            axisNamesCorrect = new std::vector<std::string>(axisDef);

            // function to set sortAxis numbered value.
            this->getAxisFromName();
        }
    }
    else
    {
        LERROR << "QtSort::checkType() - MDDtoSort branch invalid.";
    }

    if(ranks)
    {
        const QtTypeElement &inputType2 = ranks->checkType(typeTuple);

        if (inputType2.getDataType() != QT_MDD)
        {
            LERROR << "QtSort::checkType() - Ranks operand is not of type MDD.";
            parseInfo.setErrorNo(SORT_RANKSOPINVALID);
            throw parseInfo;
        }
    }

    return dataStreamType;
}

QtData*
QtSort::slice(QtData *myMDD, r_Minterval myMinterval)
{
    bool dynamicMintervalExpression = true;//always true for sort
    QtData *returnValue = NULL;
    //
    // Trimming/Projection to an MDD object
    //

    QtMintervalData *newMint;
    newMint = new QtMintervalData(myMinterval);

    // get minterval data
    std::vector<bool>  *trimFlags = new std::vector<bool>(*((newMint)->getTrimFlags()));
    r_Minterval    domain    = myMinterval;
    LDEBUG << "Evaluate subset " << domain;

    //
    // In case of dynamic index expressions, load optimization has to
    // be performed for the current input expression.
    //
    std::vector<r_Minterval> *intervals;
    intervals = new std::vector<r_Minterval>();
    intervals->push_back(domain);

    if (dynamicMintervalExpression)
    {
        QtNode::QtTrimList *trimList = new QtNode::QtTrimList;

        for (unsigned int i = 0; i != domain.dimension(); i++)
        {
            // create a new element
            QtTrimElement *elem = new QtTrimElement;

            elem->interval     = domain[i];
            elem->intervalFlag = (*trimFlags)[i];
            elem->dimension    = i;

            // and add it to the list
            trimList->push_back(elem);
        }

        // pass optimization process to the input tree

        if (MDDtoSort->getNodeType() == QT_DOMAIN_OPERATION)
        {
            static_cast<QtDomainOperation*>(MDDtoSort)->optimizeLoad(trimList, intervals);
        }
        else
        {
            MDDtoSort->optimizeLoad(trimList);
        }
    }

    // resolve positionnaly independent axes by reording them according to the type definiton

    QtMDD  *qtMDD         = static_cast<QtMDD *>(myMDD);
    MDDObj *currentMDDObj = qtMDD->getMDDObject();
    r_Nullvalues *nullValues = NULL;

    if (currentMDDObj)
    {
        r_Minterval currentDomain = currentMDDObj->getCurrentDomain();
        for (std::vector<r_Minterval>::reverse_iterator it = intervals->rbegin();it != intervals->rend(); ++it)
        {
            if (it == intervals->rbegin())
            {
                if (!it->inside_of(currentDomain))
                {
                    if (!it->intersects_with(currentDomain))
                    {
                        LERROR << "Subset domain " << *it << " does not intersect with the spatial domain of MDD" << currentDomain;
                        parseInfo.setErrorNo(356);
                        throw parseInfo;
                    }
                    else
                    {
                        LERROR << "Subset domain " << *it << " extends outside of the spatial domain of MDD" << currentDomain;
                        parseInfo.setErrorNo(344);
                        throw parseInfo;
                    }
                }
            }
            else
            {
                if (!it->inside_of(*(it-1)))
                {
                    if (!it->intersects_with(*(it-1)))
                    {
                        LERROR << "Subset domain " << *it << " does not intersect with the previous subset of MDD" << *(it-1);
                        parseInfo.setErrorNo(356);
                        throw parseInfo;
                    }
                    else
                    {
                        LERROR << "Subset domain " << *it << " extends outside of the previous subset of MDD" << *(it-1);
                        parseInfo.setErrorNo(344);
                        throw parseInfo;
                    }
                }
            }
        }

        bool trimming   = false;
        bool projection = false;
        nullValues = currentMDDObj->getNullValues();
        // reset loadDomain to intersection of domain and loadDomain
        if (domain.intersection_with(qtMDD->getLoadDomain()) != qtMDD->getLoadDomain())
        {
            qtMDD->setLoadDomain(domain.intersection_with(qtMDD->getLoadDomain()));
        }

        // Test, if trimming has to be done; trimming = 1 if !(load domain is subset of spatial operation)
        trimming = (domain.intersection_with(qtMDD->getLoadDomain()) == qtMDD->getLoadDomain());

        // Test, if a projection has to be made and build the projSet in projection case
        std::set<r_Dimension, std::less<r_Dimension>> projSet;

        for (unsigned int i = 0; i < trimFlags->size(); i++)
            if (!(*trimFlags)[i])
            {
                projection = true;
                projSet.insert(i);
            }

        r_Minterval projectedDom(domain.dimension() - projSet.size());

        // build the projected domain
        for (unsigned int i = 0; i < domain.dimension(); i++)
            // do not include dimensions projected away
            //!!
            if (projSet.find(i) == projSet.end())
            {
                projectedDom << domain[i];
            }

        if (trimFlags)
        {
            delete trimFlags;
            trimFlags = NULL;
        }

        LTRACE << "  operation domain..: " << domain << " with projection " << projectedDom;
        LTRACE << "  mdd load    domain: " << qtMDD->getLoadDomain();
        LTRACE << "  mdd current domain: " << currentMDDObj->getCurrentDomain();

        if (trimming || projection)
        {
            // get relevant tiles
            auto *relevantTiles = currentMDDObj->intersect(domain);

            if (relevantTiles->size() > 0)
            {
                // create a transient MDD object for the query result
                MDDObj *resultMDD = new MDDObj(currentMDDObj->getMDDBaseType(), projectedDom, currentMDDObj->getNullValues());

                // and iterate over them
                for (auto tileIt = relevantTiles->begin(); tileIt !=  relevantTiles->end(); tileIt++)
                {
                    // domain of the actual tile
                    r_Minterval tileDom = (*tileIt)->getDomain();

                    // domain of the relevant area of the actual tile
                    r_Minterval intersectDom = tileDom.create_intersection(domain);

                    LDEBUG << "  trimming/projecting tile with domain " << tileDom << " to domain " << intersectDom;

                    // create projected tile
                    Tile *resTile = new Tile(tileIt->get(), intersectDom, &projSet);

                    // insert Tile in result mddObj
                    resultMDD->insertTile(resTile);
                }

                // create a new QtMDD object as carrier object for the transient MDD object
                returnValue = new QtMDD(static_cast<MDDObj *>(resultMDD));
                returnValue->setNullValues(nullValues);


                // delete the tile vector
                delete relevantTiles;
                relevantTiles = NULL;
            }
            else
            {
                // Instead of throwing an exception, return an MDD initialized
                // with null values when selecting an area that doesn't intersect
                // with any existing tiles in the database -- DM 2012-may-24
                const MDDBaseType *mddType = currentMDDObj->getMDDBaseType();

                // create a transient MDD object for the query result
                MDDObj *resultMDD = new MDDObj(mddType, projectedDom, nullValues);

                // create transient tile
                Tile *resTile = new Tile(projectedDom, mddType->getBaseType());
                resTile->setPersistent(false);
                resultMDD->fillTileWithNullvalues(resTile->getContents(), resTile->getDomain().cell_count());

                // insert Tile in result mddObj
                resultMDD->insertTile(resTile);
                returnValue = new QtMDD(static_cast<MDDObj *>(resultMDD));
            }

        } // if(trimming || projection)
        else
            // operand is passed through
        {
            returnValue = myMDD;
        }

    } // if( currentMDDObj )

    return returnValue;
}

void
QtSort::processOperand(unsigned int i, QtMDD *qtMDDObj, MDDObj *resultMDD,
                              const BaseType *baseType, const std::vector<r_Point> &tVector)
{
  MDDObj *mddOp = qtMDDObj->getMDDObject();
  const auto &mddOpDomain = qtMDDObj->getLoadDomain();

  // get intersecting tiles
  auto opTiles = std::unique_ptr<std::vector<std::shared_ptr<Tile>>>(mddOp->intersect(mddOpDomain));

  // iterate over source tiles
  for (const auto &opTile: *opTiles)
  {
      // get relevant area of source tile
      r_Minterval srcTileDomain = mddOpDomain.create_intersection(opTile->getDomain());
      // compute translated tile domain
      r_Minterval dstTileDomain = i == 0 ? srcTileDomain
                                         : srcTileDomain.create_translation(tVector[i]);
      // create a new transient tile, copy the transient data, and insert it into the mdd object
      Tile *newTransTile = new Tile(dstTileDomain, baseType);
      auto myOp = std::unique_ptr<UnaryOp>(Ops::getUnaryOp(
          Ops::OP_IDENTITY, baseType, mddOp->getCellType(), 0, 0));
      newTransTile->execUnaryOp(myOp.get(), dstTileDomain, opTile.get(), srcTileDomain);
      resultMDD->insertTile(newTransTile);
  }
}

QtData *
QtSort::concatenate(unsigned int dimension)
{
    QtData *returnValue = NULL;

    // check if type coercion is possible and compute the result type
    const auto *resultMDDType = static_cast<const MDDBaseType *>(dataStreamType.getType());
    const BaseType *baseType = resultMDDType->getBaseType();

    // compute the result domain
    std::vector<r_Point> tVector(slicesList.size()); // save the translating vectors for all arrays except the first
    r_Minterval destinationDomain;
    unsigned int i = 0;
    //iterate over all slices
    for (auto iter = slicesList.begin(); iter != slicesList.end(); iter++, i++)
    {
        QtMDD *qtMDDObj = static_cast<QtMDD *>( accessSliceMDD(*iter) );
        if (iter == slicesList.begin())
        {
            destinationDomain = qtMDDObj->getLoadDomain();
            if (destinationDomain.dimension() <= static_cast<r_Dimension>(dimension))
            {
                LERROR << "QtSort::concatenate() - the operands have less dimensions than the one specified";
                parseInfo.setErrorNo(424);
                throw parseInfo;
            }
        }
        else
        {
            // compute target position of the array in the result
            r_Point newPosB = destinationDomain.get_origin();
            newPosB[dimension] += destinationDomain.get_extent()[dimension];
            // translating vector as a difference between intial lower left
            // corner and target position in the new array
            const auto &opDom = qtMDDObj->getLoadDomain();
            tVector[i] = newPosB - opDom.get_origin();

            auto opDomTranslated = opDom.create_translation(tVector[i]);
            if (destinationDomain.is_mergeable(opDomTranslated))
            {
                destinationDomain = opDomTranslated.create_closure(destinationDomain);
            }
            else
            {
                LERROR << "QtSort::concatenate() - operands of concat have non-mergeable domains";
                parseInfo.setErrorNo(425);
                throw parseInfo;
            }
        }
    }

    // create a transient MDD object for the query result
    MDDObj *resultMDD = new MDDObj(resultMDDType, destinationDomain);
    std::vector<std::pair<r_Double, r_Double>> nullvalues;
    i = 0;
    for (auto iter = slicesList.begin(); iter != slicesList.end(); iter++, i++)
    {
        QtMDD *qtMDDObj = static_cast<QtMDD *>( accessSliceMDD(*iter) );
        MDDObj *mddOp = qtMDDObj->getMDDObject();

        processOperand(i, qtMDDObj, resultMDD, baseType, tVector);

        auto *tempValues = mddOp->getNullValues();
        if (tempValues != NULL)
        {
            for (const auto &p : tempValues->getNullvalues())
                nullvalues.push_back(p);
        }
    }

    if (!nullvalues.empty())
    {
        auto nullvaluesTmp = nullvalues;
        auto *tmp = new r_Nullvalues(std::move(nullvaluesTmp));
        resultMDD->setNullValues(tmp);
    }
    // create a new QtMDD object as carrier object for the transient MDD object
    returnValue = new QtMDD(resultMDD);
    if (!nullvalues.empty())
    {
        auto nullvaluesTmp = nullvalues;
        auto *tmp = new r_Nullvalues(std::move(nullvaluesTmp));
        returnValue->setNullValues(tmp);
    }

    return returnValue;
}

void
QtSort::extractRanks(QtData* ranksOperand)
{
    QtMDD  *qtMDD         = static_cast<QtMDD *>(ranksOperand);
    MDDObj *currentMDDObj = qtMDD->getMDDObject();

    r_Minterval currentDomain = currentMDDObj->getCurrentDomain();
    r_Sinterval::BoundType ranksExtent = (r_Sinterval::BoundType) currentDomain[0].get_extent();// number of ranks in a 1D ranksOperand

    // make sure the number of ranks == number of slices
    if(sortAxisExtent != ranksExtent)
    {
         LERROR << "Internal error in QtSort::extractRanks() - "
                   << "Number of ranks generated must match the number of slices at the sort axis.";

        BYClauseParseInfo.setErrorNo(SORT_NUMBEROFRANKSMISMATCH);
        throw BYClauseParseInfo;
    }

    // get relevant Tiles
    std::vector<std::shared_ptr<Tile>> *tiles = NULL;
    tiles = currentMDDObj->intersect(qtMDD->getLoadDomain());
    // create one single tile with the load domain (blocking operation -> merge the tiles into one)
    Tile *sourceTile = NULL;
    sourceTile = new Tile(tiles, qtMDD->getLoadDomain(), currentMDDObj);

    // get the ranksOperand values
    double *ranksArray = (double*) sourceTile->getContents();

    // iterate over all slices in list, update the Ranks
    auto it = slicesList.begin();
    int i=0;
    for(it; it != slicesList.end(); ++it)
    {
        // update rank in the relevant tuple for the slice
        accessSliceRank(*it) = (double)ranksArray[i];
        i++;
    }
}

QtData *
QtSort::evaluate(QtDataList *inputList)
{
    QtData *returnValue = NULL;      // output array
    QtData *MDDtoSortOperand = NULL; // array to be sorted
    QtData *ranksOperand = NULL;     // ranks array

    MDDtoSortOperand = MDDtoSort->evaluate(inputList);

    if(applyRankings)
        ranksOperand = ranks->evaluate(inputList);

    // get QtMDD and MDDObj
    QtMDD  *qtMDD         = static_cast<QtMDD *>(MDDtoSortOperand);
    MDDObj *currentMDDObj = qtMDD->getMDDObject();

    // get extent of the sortAxis dimension, this is the number of slices created
    r_Minterval currentDomain = currentMDDObj->getCurrentDomain();

    // check that sortAxis is within bounds - cannot ever be parsed as negative.
    if(sortAxis>= currentDomain.dimension())
    {
        LERROR << "Internal error in QtSort::evaluate() - "
                   << "The axis is outside the array's spatial domain.";

        ALONGClauseParseInfo.setErrorNo(AXIS_OUTOFBOUNDS);
        throw ALONGClauseParseInfo;
    }

    sortAxisExtent = (r_Sinterval::BoundType) currentDomain[sortAxis].get_extent();

    if(currentMDDObj)
    {
        r_Minterval sliceDom = currentDomain;// domain for each slicing iteration changes

        // slice array at sortAxis, and save slices into list.
        // this will slice the array, and save the slices in their original order into the list.
        for(r_Range i=0; i<sortAxisExtent; i++)
        {
            // set the spatial domain of the slice,
            // make interval = 'i' iterator at the sortAxis
            sliceDom[sortAxis] = r_Sinterval((r_Range) i);
            // reset MDD domain
            qtMDD->setLoadDomain(currentDomain);
            // do slicing and append slice to list (sliceMDD, placeholder rank)
            // the placeholder rank counts from zero up for each slice in original order,
            // which enables DESC to reverse the slices when no ranking function is given.
            //this->appendSlice(slice(MDDtoSortOperand, sliceDom), i);
            this->appendSlice(slice(MDDtoSortOperand, sliceDom), i);
        }

        // evaluate ranks array and extract the ranks
        if(applyRankings)
            this->extractRanks(ranksOperand);

        // sort the slices
        this->sort();

        // concatenate all slices into output array
        returnValue = this->concatenate((unsigned int) sortAxis);

        // clear slicesList, important when multiple objects are in 1 collection, which are sorted at the same time.
        auto it = slicesList.begin();
        for(it; it != slicesList.end(); ++it)
        {   //delete pointers
            delete accessSliceMDD(*it);
        }
        slicesList.clear();
    }
    return returnValue;// has to be a QtMDD
}

void
QtSort::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtSort Object: type " << std::flush;
    dataStreamType.printStatus(s);
    s << std::endl;

    if (mode != QtNode::QT_DIRECT_CHILDS)
    {
        if (MDDtoSort)
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "input: " << std::endl;
            MDDtoSort->printTree(tab+2, s, mode);
        }
        else
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "no input" << std::endl;
        }
    }

}

void
QtSort::printAlgebraicExpression(std::ostream &s)
{
    s << "MDD<";

    if(MDDtoSort)
    {
        MDDtoSort->printAlgebraicExpression(s);
    }
    else
    {
        s << "no MDD";
    }

    s << ",";

    if(ranks)
    {
        ranks->printAlgebraicExpression(s);
    }
    else
    {
        s <<"no ranks";
    }

    s << ">";
}

//EOF
