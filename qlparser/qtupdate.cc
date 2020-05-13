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


#include "qtupdate.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtmintervaldata.hh"

#include "tilemgr/tile.hh"
#include "tilemgr/tiler.hh"
#include "storagemgr/sstoragelayout.hh"
#include "relcatalogif/basetype.hh"
#include "relcatalogif/mddbasetype.hh"

#include "mddmgr/mddobj.hh"
#include "qtnullvaluesdata.hh"

#include <iostream>
#include <memory>

#include <logging.hh>

const QtNode::QtNodeType QtUpdate::nodeType = QtNode::QT_UPDATE;



QtUpdate::QtUpdate(QtOperation *initUpdateTarget, QtOperation *initUpdateDomain, QtOperation *initUpdateSource)
    : QtExecute(), input(NULL),
      updateTarget(initUpdateTarget),
      updateDomain(initUpdateDomain),
      updateSource(initUpdateSource)
{
    if (updateTarget)
    {
        updateTarget->setParent(this);
    }
    if (updateDomain)
    {
        updateDomain->setParent(this);
    }
    if (updateSource)
    {
        updateSource->setParent(this);
    }
}



QtUpdate::~QtUpdate()
{
    delete updateTarget;
    delete updateDomain;
    delete updateSource;
    delete input;
}



QtData *
QtUpdate::evaluate()
{
    startTimer("QtUpdate");

    // Test, if all necessary operands are available.
    if (updateTarget && input)
    {
        try
        {
            input->open(); // open input stream

            QtNode::QtDataList *nextTuple;
            while ((nextTuple = input->next()))
            {
                if (updateSource)
                {
                    evaluateTuple(nextTuple);
                }

                // delete tuple vector received by next()
                for (auto it = nextTuple->begin(); it != nextTuple->end(); it++)
                    if (*it)
                    {
                        (*it)->deleteRef();
                    }
                delete nextTuple, nextTuple = NULL;
            } // while
        }
        catch (...)
        {
            input->close();
            throw;
        }

        input->close();
    }
    else
    {
        LERROR << "at least one operand branch is invalid.";
    }

    stopTimer();

    return 0;
}


void
QtUpdate::evaluateTuple(QtNode::QtDataList *nextTuple)
{
    LDEBUG << "Evaluating MDD update...";

    // mdd object to be updated
    LDEBUG << "  evaluating target object expression...";
    QtData *target = updateTarget->evaluate(nextTuple);
    QtDataDeleter targetWrapper{target};

    // mdd object that is the source of the update
    LDEBUG << "  evaluating source object expression...";
    QtData *source = updateSource->evaluate(nextTuple);
    QtDataDeleter sourceWrapper{source};

    // check if target and source are valid
    if (!checkOperands(nextTuple, target, source))
    {
        return;
    }

    QtMDD *targetMDD = static_cast<QtMDD *>(target);
    QtMDD *sourceMDD = static_cast<QtMDD *>(source);

    MDDObj *targetObj = targetMDD->getMDDObject();
    MDDObj *sourceObj = sourceMDD->getMDDObject();
    r_Nullvalues *sourceNullValues = sourceObj->getNullValues();
    // the null values of source object have precedence; if they are not specified, only then take
    // the null values of the target object.
    if (!sourceNullValues)
    {
        sourceNullValues = targetObj->getNullValues();
    }

    // this is necessary so that later on when generated tiles are filled with null values,
    // the source null values will be considered if the target object has no null values itself
    if (!targetObj->getNullValues() && sourceNullValues)
    {
        targetObj->setNullValues(sourceNullValues);
    }

    // test, if target is a persistent object
    if (!targetObj->isPersistent())
    {
        LERROR << "result of target expression must be an assignable value (l-value).";
        throwError(nextTuple, target, source, 954);
    }

    // check that source base type matches the target base type
    const BaseType *srcBaseType = sourceObj->getCellType();
    const BaseType *dstBaseType = targetObj->getCellType();
    if (!dstBaseType->compatibleWith(srcBaseType))
    {
        const char *srcTypeStructure = srcBaseType->getTypeStructure();
        const char *dstTypeStructure = dstBaseType->getTypeStructure();
        LERROR << "Base type of source object (" << srcTypeStructure
               << ") does not match the base type of the target object (" << dstTypeStructure << ")";
        throwError(nextTuple, target, source, 434);
    }

    // get optional domain
    QtData *targetDomainData = NULL;
    r_Minterval targetDomain;
    r_Minterval sourceMDDDomain(sourceMDD->getLoadDomain());

    if (targetObj->getStorageLayout())
    {
        targetObj->getStorageLayout()->setCellSize(static_cast<int>(targetObj->getCellType()->getSize()));
    }

    if (updateDomain)
    {
        targetDomainData = updateDomain->evaluate(nextTuple);
        if (targetDomainData)
        {
            targetDomain = (static_cast<QtMintervalData *>(targetDomainData))->getMintervalData();
        }
    }
    QtDataDeleter targetDomainDataWrapper{targetDomainData};

#ifdef DEBUG
    if (targetDomainData)
    {
        LINFO << "  target MDD, domain " << targetDomain;
    }
    else
    {
        LINFO  << "  target MDD";
    }
    targetMDD->printStatus(RMInit::logOut);
    LINFO  << "  source MDD, domain " << sourceMDDDomain;
    sourceMDD->printStatus(RMInit::logOut);
#endif

    // 1st update strategy:
    //
    // 1. All cell values of the source domain which are already defined are
    //    updated with the corresponding new values.
    // 2. If a source tile does not intersect with any tile of the target
    //    object, it is inserted.

    // In case of update domain existence, test for compatibility.
    checkDomainCompatibility(nextTuple, target, source, targetDomainData, targetMDD, sourceMDD);

    // compute source MDD domain taking into account update domain
    if (targetDomainData)
    {
        const vector<bool> *trimFlags = (static_cast<QtMintervalData *>(targetDomainData))->getTrimFlags();
        r_Minterval newSourceMDDDomain(targetMDD->getLoadDomain().dimension());
        for (unsigned int i = 0, j = 0; i < trimFlags->size(); i++)
        {
            if ((*trimFlags)[i])
            {
                newSourceMDDDomain << sourceMDDDomain[j++];
            }
            else
            {
                newSourceMDDDomain << targetDomain[i];
            }
        }
        sourceMDDDomain = newSourceMDDDomain;
        LDEBUG << "  new source update domain " << sourceMDDDomain;
    }
    // check if you may update that are ( you should not go out of bounds for mdddomaintype )
    if (!targetObj->getMDDBaseType()->compatibleWithDomain(&sourceMDDDomain))
    {
        LERROR << "The update domain is outside the allowed domain of the target mdd.";
        throwError(nextTuple, target, source, 953, targetDomainData);
    }

    // get all source tiles
    std::shared_ptr<vector<std::shared_ptr<Tile>>> tmpTilePtrs(sourceObj->getTiles());
    vector<Tile *> sourceTiles;
    for (auto it = tmpTilePtrs->begin(); it != tmpTilePtrs->end(); ++it)
    {
        sourceTiles.push_back(it->get());
    }

#ifdef DEBUG
    if (sourceTiles)
    {
        LDEBUG << "  there are " << sourceTiles.size() << " source tiles";
        for (auto sourceTilesIterator = sourceTiles.begin(); sourceTilesIterator != sourceTiles.end(); sourceTilesIterator++)
        {
            LDEBUG << "    tile domain: " << (*sourceTilesIterator)->getDomain();
        }
    }
    else
    {
        LDEBUG << "  there are no source tiles";
    }
#endif

    // get all target tiles in the domain of interest
    vector<r_Minterval> targetDomains;
    vector<Tile *> targetTiles;
    tmpTilePtrs.reset(targetObj->intersect(sourceMDDDomain));
    for (auto it = tmpTilePtrs->begin(); it != tmpTilePtrs->end(); ++it)
    {
        LDEBUG << "existing target tile with domain: " << (*it)->getDomain();
        targetTiles.push_back(it->get());
        targetDomains.push_back((*it)->getDomain());
    }

    // split target tiles to match the source tiles, and generate result (retval) tiles
    vector<r_Minterval> sourceDomains{sourceMDDDomain};
    r_Tiler t(sourceDomains, targetDomains);
    t.split();
    t.removeDoubleDomains();
    t.removeCoveredDomains();
    t.mergeDomains();
    vector<Tile *> retval = t.generateTiles(sourceTiles);
    for (auto retvalIt = retval.begin(); retvalIt != retval.end(); retvalIt++)
    {
        const auto &retvalTmp = *retvalIt;
        LDEBUG << "generated target tile with domain: " << retvalTmp->getDomain();
        if (targetObj->getNullValues())
        {
            targetObj->fillTileWithNullvalues(retvalTmp->getContents(), retvalTmp->getDomain().cell_count());
        }
        targetTiles.push_back(*retvalIt);
    }

    // get tile copy operation
    std::unique_ptr<UnaryOp> updateOp;
    if (!sourceTiles.empty())
    {
        // if there are null values in the source, they need to be considered to
        // not overwrite the target (which OP_UPDATE does), otherwise the faster
        // OP_IDENTITY that copies everything can be used.
        auto op = sourceNullValues ? Ops::OP_UPDATE : Ops::OP_IDENTITY;

        updateOp.reset(Ops::getUnaryOp(op, targetObj->getCellType(), (*sourceTiles.begin())->getType()));
        if (!updateOp)
        {
            LERROR << "source MDD base type does not match target MDD base type.";
            parseInfo.setErrorNo(952);
            throw parseInfo;
        }
        updateOp->setNullValues(sourceNullValues);
    }

    //
    // iterate over source tiles
    //
    LDEBUG << "";
    for (auto sourceIt = sourceTiles.begin(); sourceIt != sourceTiles.end(); sourceIt++)
    {
        // calculate relevant area of source tile
        r_Minterval sourceTileDomain = (*sourceIt)->getDomain().create_intersection(sourceMDD->getLoadDomain());
        LDEBUG << "original source tile domain " << sourceTileDomain;

        // compute update source tile domain taking into account update domain
        r_Minterval updateSourceTileDomain;
        if (targetDomainData)
        {
            updateSourceTileDomain = r_Minterval(targetMDD->getLoadDomain().dimension());
            const vector<bool> *trimFlags = (static_cast<QtMintervalData *>(targetDomainData))->getTrimFlags();
            for (unsigned int i = 0, j = 0; i < trimFlags->size(); i++)
            {
                if ((*trimFlags)[i])
                {
                    updateSourceTileDomain << sourceTileDomain[j++];
                }
                else
                {
                    updateSourceTileDomain << targetDomain[i];
                }
            }
        }
        else
        {
            updateSourceTileDomain = sourceTileDomain;
        }
        LDEBUG << "update source tile domain " << updateSourceTileDomain;

        // find intersecting target tiles that need to be updated
        bool intersection = false;
        for (auto targetIt = targetTiles.begin(); targetIt != targetTiles.end(); targetIt++)
        {
            r_Minterval targetTileDomain = (*targetIt)->getDomain();

            // if tiles are intersecting
            if (updateSourceTileDomain.intersects_with(targetTileDomain))
            {
                LDEBUG << "  target tile domain " << targetTileDomain;
                intersection = true;

                // get intersecting updateSourceTileDomain
                r_Minterval intersectUpdateSourceTileDomain = updateSourceTileDomain.create_intersection(targetTileDomain);
                LDEBUG << "    source and target tiles intersect: " << intersectUpdateSourceTileDomain;

                // compute corresponding sourceTileDomain
                r_Minterval intersectSourceTileDomain = intersectUpdateSourceTileDomain;
                if (targetDomainData)
                {
                    const vector<bool> *trimFlags = (static_cast<QtMintervalData *>(targetDomainData))->getTrimFlags();
                    for (unsigned int i = 0, j = 0; i < trimFlags->size(); i++)
                    {
                        if (!((*trimFlags)[i]))
                        {
                            intersectSourceTileDomain.delete_dimension(j);
                        }
                        else
                        {
                            j++;
                        }
                    }
                }
                LDEBUG << "    intersection after removing slices: " << intersectSourceTileDomain;
                LDEBUG << "    updating target tile with source tile data...";
                if (intersectUpdateSourceTileDomain.dimension() == intersectSourceTileDomain.dimension() && !sourceNullValues)
                {
                    (*targetIt)->copyTile(intersectUpdateSourceTileDomain, *sourceIt, intersectSourceTileDomain);
                }
                else
                {
                    (*targetIt)->execUnaryOp(&(*updateOp), intersectUpdateSourceTileDomain, *sourceIt, intersectSourceTileDomain);
                }
            }
        }

        // insert the tile
        if (!intersection)
        {
            // Create a new persistent tile, copy the transient data,
            // and insert it into the target mdd object.
            LDEBUG << "  no intersection with target tiles found, inserting new tile";
            Tile *newPersTile = new Tile(updateSourceTileDomain, targetObj->getCellType(), (*sourceIt)->getDataFormat());
            if (updateSourceTileDomain.dimension() == sourceTileDomain.dimension() && !sourceNullValues)
            {
                newPersTile->copyTile(updateSourceTileDomain, *sourceIt, sourceTileDomain);
            }
            else
            {
                newPersTile->execUnaryOp(&(*updateOp), updateSourceTileDomain, *sourceIt, sourceTileDomain);
            }

            LDEBUG << "  update domains: target tile " << newPersTile->getDomain()
                   << " update target at " << updateSourceTileDomain <<
                   ", source tile " << (*sourceIt)->getDomain()
                   << " update with data at " << sourceTileDomain;

            targetObj->insertTile(newPersTile);
        }
    }//for is done

    for (auto retvalIt = retval.begin(); retvalIt != retval.end(); retvalIt++)
    {
        targetObj->insertTile(static_cast<Tile *>(*retvalIt));
    }
    //update the db domain to the one computed from the index
    targetObj->setDbDomain(targetObj->getCurrentDomain());
}

bool
QtUpdate::checkOperands(QtNode::QtDataList *nextTuple, QtData *target, QtData *source)
{
    // Test, if the operands are valid.
    if (target && source)
    {
        // check update target
        if (target->getDataType() != QT_MDD)
        {
            LERROR << "update target must be an iterator variable.";
            throwError(nextTuple, target, source, 950);
        }

        // check update source
        if (source->getDataType() != QT_MDD)
        {
            LERROR << "update source must be an expression resulting in an MDD";
            throwError(nextTuple, target, source, 951);
        }
    }
    else
    {
        LERROR << "target or source is not provided.";

        // delete the operands
        if (target)
        {
            target->deleteRef();
        }
        if (source)
        {
            source->deleteRef();
        }

        return false;
    }
    return true;
}


void
QtUpdate::throwError(QtNode::QtDataList *nextTuple, QtData *target, QtData *source, int errorNumber, QtData *domainData)
{
    parseInfo.setErrorNo(static_cast<unsigned long>(errorNumber));
    throw parseInfo;
}


void
QtUpdate::checkDomainCompatibility(QtNode::QtDataList *nextTuple, QtData *target,
                                   QtData *source, QtData *domainData,
                                   QtMDD *targetMDD, QtMDD *sourceMDD)
{
    // In case of update domain existence, test for compatibility.
    if (domainData)
    {
        r_Minterval domain = (static_cast<QtMintervalData *>(domainData))->getMintervalData();
        // Dimensionality of the udate domain specification has to be equal to
        // the target MDD dimensionality.
        if (domain.dimension() != targetMDD->getLoadDomain().dimension())
        {
            LERROR << "Update domain dimensionality must match target MDD dimensionaltiy.";
            throwError(nextTuple, target, source, 963, domainData);
        }

        // The number of interval dimension of the update domain has to be
        // equal to the number of dimensions of the source domain.
        unsigned int updateIntervals = 0;
        const vector<bool> *trimFlags = (static_cast<QtMintervalData *>(domainData))->getTrimFlags();
        for (unsigned int i = 0; i < trimFlags->size(); i++)
            if ((*trimFlags)[i])
            {
                updateIntervals++;
            }

        if (updateIntervals != sourceMDD->getLoadDomain().dimension())
        {
            LERROR << "Number of update intervals must match source dimensionality.";
            throwError(nextTuple, target, source, 962, domainData);
        }

        // Before: Warning: Fixed bounds in update domain specifications are ignored.

        // After: Actually we should be stricter here and respect the documentation:
        // throw an exception if the source domain isn't within the target domain -- DM 2012-mar-12

        // copied from r_Minterval::covers(..), we have to rule out slices here
        const r_Minterval &sourceDomain = sourceMDD->getLoadDomain();
        unsigned int j = 0;
        for (r_Dimension i = 0; i < domain.dimension(); i++)
        {
            if ((*trimFlags)[i]) // consider only trims
            {
                if ((domain[i].is_low_fixed() && (!(sourceDomain[j].is_low_fixed()) || domain[i].low() > sourceDomain[j].low())) ||
                        (domain[i].is_high_fixed() && (!(sourceDomain[j].is_high_fixed()) || domain[i].high() < sourceDomain[j].high())))
                {
                    LERROR << "source domain " <<
                           sourceDomain << " isn't within the target domain " << domain;

                    // delete tuple vector received by next()
                    for (auto dataIter = nextTuple->begin(); dataIter != nextTuple->end(); dataIter++)
                        if (*dataIter)
                        {
                            (*dataIter)->deleteRef();
                        }
                    delete nextTuple;
                    nextTuple = NULL;

                    parseInfo.setErrorNo(967);
                    throw parseInfo;
                }
                ++j;
            }
        }

    }
}

#define GET_CHILDREN(n) \
    if (n) { \
        QtNodeList* subList = (n)->getChilds(flag); \
        resultList->splice(resultList->begin(), *subList); \
        delete subList; subList = NULL; \
    }

QtNode::QtNodeList *
QtUpdate::getChilds(QtChildType flag)
{
    QtNodeList *resultList = NULL;

    // allocate resultList
    resultList = new QtNodeList();

    if (flag == QT_LEAF_NODES || flag == QT_ALL_NODES)
    {
        GET_CHILDREN(input);
        GET_CHILDREN(updateTarget);
        GET_CHILDREN(updateDomain);
        GET_CHILDREN(updateSource);
    }

    // add the nodes of the current level
    if (flag == QT_DIRECT_CHILDS || flag == QT_ALL_NODES)
    {
        resultList->push_back(input);
        resultList->push_back(updateTarget);
        resultList->push_back(updateDomain);
        resultList->push_back(updateSource);
    }

    return resultList;
}



void
QtUpdate::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtUpdate Object" << getEvaluationTime() << endl;

    if (mode != QtNode::QT_DIRECT_CHILDS)
    {
        if (updateTarget)
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "target: " << endl;
            updateTarget->printTree(tab + 2, s);

            if (updateDomain)
            {
                s << SPACE_STR(static_cast<size_t>(tab) + 2).c_str() << "domain: " << endl;
                updateDomain->printTree(tab + 4, s);
            }
            else
            {
                s << SPACE_STR(static_cast<size_t>(tab) + 2).c_str() << "no domain" << endl;
            }
        }
        else
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "no target" << endl;
        }

        if (updateSource)
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "source: " << endl;
            updateSource->printTree(tab + 2, s);
        }
        else
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "no source" << endl;
        }

        if (input)
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "input: " << endl;
            input->printTree(tab + 2, s, mode);
        }
        else
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "no input" << endl;
        }

        s << endl;
    }
}



void
QtUpdate::printAlgebraicExpression(std::ostream &s)
{
    s << "update<" << std::flush;
    if (updateTarget)
    {
        updateTarget->printAlgebraicExpression(s);
    }
    else
    {
        s << "<no target>";
    }

    if (updateDomain)
    {
        updateDomain->printAlgebraicExpression(s);
    }

    s << "," << std::flush;
    if (updateSource)
    {
        updateSource->printAlgebraicExpression(s);
    }
    else
    {
        s << "<no source>";
    }

    s << ">" << std::flush;
    if (input)
    {
        s << "( ", input->printAlgebraicExpression(s), s << " )";
    }
    else
    {
        s << "(<no input>)" << std::flush;
    }
}



void
QtUpdate::setStreamInput(QtONCStream *newInput)
{
    input = newInput;
    input->setParent(this);
}

QtOperation *
QtUpdate::getUpdateTarget()
{
    return updateTarget;
}

QtOperation *
QtUpdate::getUpdateDomain()
{
    return updateDomain;
}

QtOperation *
QtUpdate::getUpdateSource()
{
    return updateSource;
}

QtONCStream *
QtUpdate::getInput()
{
    return input;
}



void
QtUpdate::checkType()
{
    // check operand branches
    if (updateTarget && input)
    {

        // get input type
        QtTypeTuple inputType  = input->checkType();

        // check target
        const QtTypeElement &targetType = updateTarget->checkType(&inputType);
        if (targetType.getDataType() != QT_MDD)
        {
            LERROR << "update target must be an iterator variable.";
            parseInfo.setErrorNo(950);
            throw parseInfo;
        }

        // check domain
        if (updateDomain)
        {
            const QtTypeElement &domainType = updateDomain->checkType(&inputType);
            if (domainType.getDataType() != QT_MINTERVAL)
            {
                LERROR << "update domain must be of type Minterval.";
                parseInfo.setErrorNo(961);
                throw parseInfo;
            }
        }

        // check source
        const QtTypeElement &sourceType = updateSource->checkType(&inputType);
        if (sourceType.getDataType() != QT_MDD)
        {
            LERROR << "update source must be an expression resulting in an MDD.";
            parseInfo.setErrorNo(951);
            throw parseInfo;
        }

        // test for compatible base types
        bool compatible = false;
        const BaseType *type1 = (static_cast<MDDBaseType *>(const_cast<Type *>(targetType.getType())))->getBaseType();
        const BaseType *type2 = (static_cast<MDDBaseType *>(const_cast<Type *>(sourceType.getType())))->getBaseType();

        // substituted the string comparison as it fails for composite types:
        // the update base type is usually "struct { char 0, char 1,...}"
        // while the target type is "struct { char red, char green, ...}"
        // Now we rather use the compatibleWith method which handles such cases -- DM 2012-mar-07

        // If the source MDD comes from an inv_* function we consider it's compatible
        // at this point, as we can't determine the type until the data is decoded -- DM 2012-mar-12
        // The same applies to QT_DECODE functions

        QtNodeList *convChildren = updateSource->getChild(QT_CONVERSION, QT_ALL_NODES);
        QtNodeList *decodeChildren = updateSource->getChild(QT_DECODE, QT_ALL_NODES);
        compatible = updateSource->getNodeType() == QT_CONVERSION ||
                     updateSource->getNodeType() == QT_DECODE ||
                     (convChildren != NULL && !convChildren->empty()) ||
                     (decodeChildren != NULL && !decodeChildren->empty()) ||
                     type1->compatibleWith(type2); //(strcmp(type1, type2) == 0);
        delete convChildren, convChildren = NULL;
        delete decodeChildren, decodeChildren = NULL;

        if (!compatible)
        {
            LERROR << "source MDD base type does not match target MDD base type.";
            parseInfo.setErrorNo(952);
            throw parseInfo;
        }
    }
    else
    {
        LERROR << "operand branch invalid.";
    }
}
