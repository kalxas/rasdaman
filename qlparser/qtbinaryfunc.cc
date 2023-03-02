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
/*************************************************************
 *
 *
 * PURPOSE:
 *
 *
 * COMMENTS:
 * - QtScale is expected to have rounding errors with >1 tiles
 * - shift, extend, scale release input tiles only at end; this
 *   shouldbe optimized (release immediately after evaluating)
 * - why is this file called "binary"? all ops have just one MDD!
 * - QtShift(), QtExtend() deliver for >1 tiles under PG an error
 *   "libpq 'select' did not yield 1 result but 0"
 *   which however does not seem to affect the result.
 *
 ************************************************************/

#include "config.h"
#include "mymalloc/mymalloc.h"
#include "common/util/scopeguard.hh"

#include "qlparser/qtbinaryfunc.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtintervaldata.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtconst.hh"

#include "mddmgr/mddobj.hh"
#include "tilemgr/tile.hh"
#include "relcatalogif/basetype.hh"

#include "raslib/rmdebug.hh"
#include <logging.hh>

#include <iostream>
#include <string>
#include <functional>  // for equal_to
#include <vector>

using namespace std;

// --- QtShift --------------------------------------------------

const QtNode::QtNodeType QtShift::nodeType = QT_SHIFT;

QtShift::QtShift(QtOperation *mddOp, QtOperation *pointOp)
    : QtBinaryOperation(mddOp, pointOp)
{
    skipCopy = mddOp->getNodeType() == QtNode::QT_CONVERSION ||
               mddOp->getNodeType() == QtNode::QT_DECODE;
}

bool QtShift::isCommutative() const
{
    return false;  // NOT commutative
}

QtData *
QtShift::evaluate(QtDataList *inputList)
{
    startTimer("QtShift");

    QtData *returnValue = NULL;
    QtData *operand1 = NULL;
    QtData *operand2 = NULL;

    // evaluate sub-nodes to obtain operand values
    if (getOperands(inputList, operand1, operand2))
    {
        const auto cleanupOnMethodExit = common::make_scope_guard(
            [operand1, operand2]() noexcept
            {
                if (operand1) operand1->deleteRef();
                if (operand2) operand2->deleteRef();
            });
        //
        // This implementation simply creates a new transient MDD object with the new
        // domain while copying the data. Optimization of this is left for future work.
        //

        QtMDD *qtMDDObj = static_cast<QtMDD *>(operand1);
        r_Point transPoint(1u);

        // get transPoint
        if (operand2->getDataType() == QT_POINT)
        {
            transPoint = (static_cast<QtPointData *>(operand2))->getPointData();
        }
        else
        {
            const BaseType *baseType = ((QtScalarData *)operand2)->getValueType();
            const char *data = ((QtScalarData *)operand2)->getValueBuffer();
            r_Long dataScalar = 0;
            transPoint << *baseType->convertToCLong(data, &dataScalar);
        }

        MDDObj *currentMDDObj = qtMDDObj->getMDDObject();

        if (transPoint.dimension() != qtMDDObj->getLoadDomain().dimension())
        {
            LERROR << "Error: QtShift::evaluate( QtDataList* ) - dimensionality of MDD and point expression do not match.";
            parseInfo.setErrorNo(SHIFT_DIMENSIONALITYMISMATCH);
            throw parseInfo;
        }

        // compute new domain
        r_Minterval destinationDomain(qtMDDObj->getLoadDomain().create_translation(transPoint));

        MDDObj *resultMDD = new MDDObj(currentMDDObj->getMDDBaseType(), destinationDomain, currentMDDObj->getNullValues());

        // get all tiles
        auto *tiles = currentMDDObj->intersect(qtMDDObj->getLoadDomain());

        // iterate over source tiles
        for (auto tileIter = tiles->begin(); tileIter != tiles->end(); tileIter++)
        {
            auto srcTile = tileIter->get();

            // get relevant area of source tile
            r_Minterval sourceTileDomain = qtMDDObj->getLoadDomain().create_intersection(srcTile->getDomain());

            // compute translated tile domain
            r_Minterval destinationTileDomain = sourceTileDomain.create_translation(transPoint);

            // create a new transient tile, copy the transient data, and insert it into the mdd object
            // FIXME: how can this work without tile area allocation??? -- PB 2005-jun-19
            Tile *newTransTile = nullptr;

            if (skipCopy)
            {
                // const r_Minterval& newDom, const BaseType* newType, bool takeOwnershipOfNewCells, char* newCells, r_Bytes newSize, r_Data_Format newFormat
                newTransTile = new Tile(destinationTileDomain, currentMDDObj->getCellType(), true,
                                        srcTile->getContents(), 0, r_Array);
                tileIter->get()->setContents(NULL);
            }
            else
            {
                newTransTile = new Tile(destinationTileDomain, currentMDDObj->getCellType());
                newTransTile->copyTile(destinationTileDomain, srcTile, sourceTileDomain);
            }
            resultMDD->insertTile(newTransTile);
        }

        // create a new QtMDD object as carrier object for the transient MDD object
        returnValue = new QtMDD(static_cast<MDDObj *>(resultMDD));

        // delete the tile vector, the tiles itself are deleted when the destructor
        // of the MDD object is called
        delete tiles;
        tiles = NULL;
    }

    stopTimer();

    return returnValue;
}

void QtShift::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtShift Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryOperation::printTree(tab, s, mode);
}

void QtShift::printAlgebraicExpression(ostream &s)
{
    s << "shift(";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ",";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}

void QtShift::optimizeLoad(QtTrimList *trimList)
{
    QtNode::QtTrimList *list1 = NULL, *list2 = NULL;

    if (input1 && input2)
    {
        QtNode::QtTrimList::iterator iter;

        //
        // The result of input2 has to be a constant expression.
        //

        // shift of trimList is just possible, if no open bounds are available
        bool openBounds = false;
        for (iter = trimList->begin(); iter != trimList->end() && !openBounds; iter++)
        {
            openBounds = !((*iter)->interval.is_low_fixed()) || !((*iter)->interval.is_high_fixed());
        }

        if (openBounds)
        {
            // release( trimList->begin(), trimList->end() );
            for (QtNode::QtTrimList::iterator iter2 = trimList->begin(); iter2 != trimList->end(); iter2++)
            {
                delete *iter2;
                *iter2 = NULL;
            }
            delete trimList;
            trimList = NULL;

            LERROR << "Error: QtShift::optimizeLoad() - spatial domain shift of open bounds is not supported";
            parseInfo.setErrorNo(SHIFT_OPENBOUNDSINCOMPATIBLE);
            throw parseInfo;
        }

        QtData *operand = input2->evaluate(NULL);

        if (!operand)
        {
            // release( trimList->begin(), trimList->end() );
            for (QtNode::QtTrimList::iterator iter2 = trimList->begin(); iter2 != trimList->end(); iter2++)
            {
                delete *iter2;
                *iter2 = NULL;
            }
            delete trimList;
            trimList = NULL;

            LERROR << "Error: QtShift::optimizeLoad() - second operand of shift function must be a constant expression.";
            parseInfo.setErrorNo(SHIFT_CONSTEXPREQUIRED);
            throw parseInfo;
        }

        if (operand->getDataType() != QT_POINT && operand->getDataType() != QT_LONG)
        {
            // release( trimList->begin(), trimList->end() );
            for (QtNode::QtTrimList::iterator iter2 = trimList->begin(); iter2 != trimList->end(); iter2++)
            {
                delete *iter2;
                *iter2 = NULL;
            }
            delete trimList;
            trimList = NULL;

            operand->deleteRef();

            LERROR << "Error: QtShift::optimizeLoad() - second operand must be of type Point.";
            parseInfo.setErrorNo(SHIFT_POINTREQUIRED);
            throw parseInfo;
        }

        // get transPoint
        if (operand->getDataType() == QT_POINT)
        {
            const r_Point &transPoint = (static_cast<QtPointData *>(operand))->getPointData();

            // shift trim elements by -transPoint
            for (iter = trimList->begin(); iter != trimList->end(); iter++)
            {
                QtTrimElement *elem = *iter;

                if (elem->dimension <= transPoint.dimension())
                {
                    elem->interval.set_interval(elem->interval.low() - transPoint[elem->dimension], elem->interval.high() - transPoint[elem->dimension]);
                }
            }
        }
        else
        {
            QtDataType dt = operand->getDataType();
            const r_Long transPoint = 0;

            // shift trim elements by -transPoint
            for (iter = trimList->begin(); iter != trimList->end(); iter++)
            {
                QtTrimElement *elem = *iter;
                elem->interval.set_interval(elem->interval.low() - transPoint, elem->interval.high() - transPoint);
            }
        }

        // point is not needed anymore
        operand->deleteRef();

        input1->optimizeLoad(trimList);
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

const QtTypeElement &
QtShift::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input1 && input2)
    {
        // get input types
        const QtTypeElement &inputType1 = input1->checkType(typeTuple);
        const QtTypeElement &inputType2 = input2->checkType(typeTuple);

        if (inputType1.getDataType() != QT_MDD)
        {
            LERROR << "Error: QtShift::checkType() - first operand must be of type MDD.";
            parseInfo.setErrorNo(MDDARGREQUIRED);
            throw parseInfo;
        }

        // operand two can be a single long number, the parser does [a] -> number a,
        // rather than [a] -> point (which is then used in marray/condense..),
        // so we need to take care manually here of this edge case -- DM 2015-aug-24
        if (inputType2.getDataType() != QT_POINT && inputType2.getDataType() != QT_LONG)
        {
            LERROR << "Error: QtShift::checkType() - second operand must be of type Point.";
            parseInfo.setErrorNo(SHIFT_POINTREQUIRED);
            throw parseInfo;
        }

        // pass MDD type
        dataStreamType = inputType1;
    }
    else
    {
        LERROR << "Error: QtShift::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}

// --- QtExtend --------------------------------------------------

const QtNode::QtNodeType QtExtend::nodeType = QT_EXTEND;

QtExtend::QtExtend(QtOperation *mddOp, QtOperation *mintervalOp)
    : QtBinaryOperation(mddOp, mintervalOp)
{
}

bool QtExtend::isCommutative() const
{
    return false;  // NOT commutative
}

QtData *
QtExtend::evaluate(QtDataList *inputList)
{
    startTimer("QtExtend");

    QtData *returnValue = NULL;       // operation result
    QtData *operand1 = NULL;          // 1st operand: MDD expression
    QtData *operand2 = NULL;          // 2nd operand: Minterval expression
    vector<Tile *> completeAreaList;  // list of tiles comprising the whole area (possibly with holes); needed for 1-code below

    if (getOperands(inputList, operand1, operand2))
    {
        const auto cleanupOnMethodExit = common::make_scope_guard(
            [operand1, operand2]() noexcept
            {
                if (operand1) operand1->deleteRef();
                if (operand2) operand2->deleteRef();
            });

        //
        // This implementation simply creates a single new transient MDD object with the new
        // domain while copying the data.
        // FIXME: create a tiled object
        //

        QtMDD *qtMDDObj = static_cast<QtMDD *>(operand1);                                           // object to be extended
        r_Minterval targetDomain = (static_cast<QtMintervalData *>(operand2))->getMintervalData();  // new domain of extended object
        MDDObj *currentMDDObj = qtMDDObj->getMDDObject();

        // precondition checks (we call the MDD C and the Minterval M):
        // - dim(C) == dim(M)
        if (targetDomain.dimension() != qtMDDObj->getLoadDomain().dimension())
        {
            LERROR << "Error: QtExtend::evaluate( QtDataList* ) - dimensionality of MDD and point expression do not match.";
            parseInfo.setErrorNo(SHIFT_DIMENSIONALITYMISMATCH);
            throw parseInfo;
        }

        // - M does not contain open bounds (i.e., "*")
        if (!targetDomain.is_origin_fixed() || !targetDomain.is_high_fixed())
        {
            LERROR << "Error: QtExtend::evaluate( QtDataList* ) - target domain must not have open bounds.";
            parseInfo.setErrorNo(EXTEND_OPENBOUNDSNOTSUPPORTED);
            throw parseInfo;
        }
        // - M.subset( sdom(C) ); can we relieve this?
        // yes, commented out the below, all seems to work perfectly fine and
        // I can't think of a good reason for this limitation -- DM 2013-feb-27
        //        if( ! targetDomain.covers( qtMDDObj->getLoadDomain() ) )
        //        {
        //            LERROR << "Error: QtExtend::evaluate( QtDataList* ) - new interval does not cover MDD to be extended.";
        //            parseInfo.setErrorNo(EXTEND_TARGETINTERVALINVALID);
        //            throw parseInfo;
        //        }

        // LINFO << "QtExtend::evaluate( QtDataList* ) - extending MDD with basetype " << currentMDDObj->getMDDBaseType() << " and load domain " << qtMDDObj->getLoadDomain() << " to domain " << targetDomain;

        // create a transient MDD object for the query result
        MDDObj *resultMDD = new MDDObj(currentMDDObj->getMDDBaseType(), targetDomain, currentMDDObj->getNullValues());

        // --- 1: put all existing tiles into their place ------------------------

        // get all tiles
        auto *tiles = currentMDDObj->intersect(qtMDDObj->getLoadDomain());

        // iterate over source tiles
        // Note that source and target MDD have the same coordinate basis
        for (auto tileIter = tiles->begin(); tileIter != tiles->end(); tileIter++)
        {
            // LINFO << "QtExtend::evaluate( QtDataList* ) - load domain is " << qtMDDObj->getLoadDomain();
            // get relevant area of source tile
            r_Minterval sourceTileDomain = qtMDDObj->getLoadDomain().create_intersection((*tileIter)->getDomain());

            Tile *newTransTile = new Tile(sourceTileDomain, currentMDDObj->getCellType());
            // LINFO << "QtExtend::evaluate( QtDataList* ) - adding source part " << sourceTileDomain << " of tile " << (*tileIter)->getDomain();
            newTransTile->copyTile(sourceTileDomain, tileIter->get(), sourceTileDomain);

            resultMDD->insertTile(newTransTile);  // needed for 2-code below
            // completeAreaList.push_back( newTransTile ); // needed for 1-code below
        }

        // --- 2: fill up new space with null values -----------------------------

// this 1-code does the same thing as the 2-code, but easier & more efficiently -- PB 2005-jun-24
#if 0  
       // INCOMPLETE!
       // create minimal (1x1) tiles at origin and high end, but only if the source domain isn't there
        if (targetDomain.get_origin() != qtMDDObj->getLoadDomain().get_origin())
        {
            LINFO << "QtExtend::evaluate( QtDataList* ) - adding aux tile at origin.";
            ->          Tile *originTile = new Tile(origin..origin + 1, currentMDDObj->getCellType());
            extendDomainList.push_back(originTile);
        }
        if (targetDomain.get_high() != qtMDDObj->getLoadDomain().get_high())
        {
            LINFO << "QtExtend::evaluate( QtDataList* ) - adding aux tile at high.";
            ->          Tile *highTile = new Tile(high - 1..high, currentMDDObj->getCellType());
            extendDomainList.push_back(highTile);
        }

        // merge all tiles into one & free not-used-any-longer stuff
        Tile *completeTile = new Tile(extendDomainList);
        delete[] extendDomainList;
        resultMDD->insertTile(completeTile);
        delete completeTile;
#else
        // 2-code; unused -- PB 2005-jun-24
        // the part below does the trick explicitly, leading to a larger number of result tiles.
        // establish list of domains
        vector<r_Minterval> extendDomainList;

        // inspect 2*d lower/upper neighbours
        // LINFO << "QtExtend::evaluate( QtDataList* ): - inspect 2*d lower/upper neighbours, dimension is " << targetDomain.dimension();
        for (r_Dimension d = 0; d < targetDomain.dimension(); d++)
        {
            // is there any space left of original MDD; ie, has MDD been extended left?
            if (targetDomain.get_origin()[d] < qtMDDObj->getLoadDomain().get_origin()[d])
            {
                // this domain is identical to original MDD except for dim d where it is left of original
                r_Minterval lowerNeighbour = qtMDDObj->getLoadDomain();
                lowerNeighbour[d] = r_Sinterval(targetDomain.get_origin()[d], qtMDDObj->getLoadDomain().get_origin()[d] - 1);
                // LINFO << "QtExtend::evaluate( QtDataList* ):   adding lower neighbour domain " << lowerNeighbour;
                extendDomainList.push_back(lowerNeighbour);
            }
            // is there any space right of original MDD; ie, has MDD been extended right?
            if (targetDomain.get_high()[d] > qtMDDObj->getLoadDomain().get_high()[d])
            {
                // this domain is identical to original MDD except for dim d where it is right of original
                r_Minterval upperNeighbour = qtMDDObj->getLoadDomain();
                upperNeighbour[d] = r_Sinterval(qtMDDObj->getLoadDomain().get_high()[d] + 1, targetDomain.get_high()[d]);
                // LINFO << "QtExtend::evaluate( QtDataList* ):   adding upper neighbour domain " << upperNeighbour;
                extendDomainList.push_back(upperNeighbour);
            }
        }

        // inspect 2^d corner points

        // LINFO << "QtExtend::evaluate( QtDataList* ): - inspect 2^d corner neighbours, dimension is " << targetDomain.dimension();
        r_Minterval cornerBoxDomain = r_Minterval(targetDomain.dimension());
        QtExtend::extendGetCornerTiles(targetDomain, qtMDDObj->getLoadDomain(), 0, targetDomain.dimension(), cornerBoxDomain, &extendDomainList);

        // merge where possible to minimize tile number
        // ...just an optimization, tbd later

        // create tiles for all domains found
        // LINFO << "QtExtend::evaluate( QtDataList* ): - creating " << extendDomainList.size() << " tiles...";

        const auto *nullValues = currentMDDObj->getNullValues();
        bool hasNullValues = nullValues != NULL && !nullValues->getNullvalues().empty();

        for (auto domainIter = extendDomainList.begin(); domainIter != extendDomainList.end(); domainIter++)
        {
            // LINFO << "QtExtend::evaluate( QtDataList* ): creating tile for domain " << (*domainIter);
            Tile *newTransTile = new Tile(*domainIter, currentMDDObj->getCellType());
            resultMDD->insertTile(newTransTile);
            if (hasNullValues)
            {
                // initialize tile with null values
                resultMDD->fillTileWithNullvalues(newTransTile->getContents(), domainIter->cell_count());
            }
        }
#endif  // 0

        // --- 3: package into MDD object & finalize -----------------------------

        // create a new QtMDD object as carrier object for the transient MDD object
        returnValue = new QtMDD(static_cast<MDDObj *>(resultMDD));

        // delete the tile vector, the tiles itself are deleted when the destructor
        // of the MDD object is called

        delete tiles;
        tiles = NULL;

        // temporary: dump result tile
        // LINFO << "QtExtend::evaluate( QtDataList* ) - result tile = " << newTransTile->printStatus();
        //  newTransTile->printStatus(99,RMInit::logOut);
    }

    stopTimer();
    // LINFO << "QtExtend::evaluate( QtDataList* ) - done.";
    return returnValue;
}

#if 1  // needed for 1-code above -- PB 2005-jun-24
/**
aux function for QtExtend::evaluate(): build up (recursing the dimension) a list of all spatial domains that sit in the corners between outerDomain and innerDomain; at the recursion bottom the resulting domain is added to the cornerList.
**/

void QtExtend::extendGetCornerTiles(r_Minterval outerDomain, r_Minterval innerDomain, const r_Dimension currentDim, const r_Dimension maxDim, r_Minterval currentInterval, vector<r_Minterval> *cornerList)
{
    // LINFO << "QtExtend::extendGetCornerTiles( " << outerDomain << ", " << innerDomain << ", " << currentDim << ", " << maxDim << ", " << currentInterval << ", _  ) start";

    // not yet addressed all dimensions in the current coordinate?
    // note: what about 1D? 0D?
    if (currentDim < maxDim)
    {
        // add domain's lower end, continue building up the minterval
        // ...but only if the area is nonempty
        if (outerDomain.get_origin()[currentDim] < innerDomain.get_origin()[currentDim])
        {
            // make local working copy
            r_Minterval extendedInterval(currentInterval);
            // add i-th coordinate to domain, up to (but excluding) innerDomain
            extendedInterval[currentDim] = r_Sinterval(outerDomain.get_origin()[currentDim], innerDomain.get_origin()[currentDim] - 1);
            // inspect next dimension
            // LINFO << "QtExtend::extendGetCornerTiles(): recursing for lower end box in next dimension " << currentDim+1 << " using extendedInterval " << extendedInterval;
            extendGetCornerTiles(outerDomain, innerDomain, currentDim + 1, maxDim, extendedInterval, cornerList);
        }
        // add domain's upper end, continue building up the minterval
        if (innerDomain.get_high()[currentDim] < outerDomain.get_high()[currentDim])
        {
            // make local working copy
            r_Minterval extendedInterval(currentInterval);
            // add i-th coordinate to domain, starting from (but excluding) innerDomain
            extendedInterval[currentDim] = r_Sinterval(innerDomain.get_high()[currentDim] + 1, outerDomain.get_high()[currentDim]);
            // inspect next dimension
            // LINFO << "QtExtend::extendGetCornerTiles(): recursing for upper end box in next dimension " << currentDim+1 << " using extendedInterval " << extendedInterval;
            extendGetCornerTiles(outerDomain, innerDomain, currentDim + 1, maxDim, extendedInterval, cornerList);
        }
    }
    else if (currentDim > maxDim)
    {
        // this is an error, see preconditions
        LERROR << "QtExtend::extendGetCornerTiles(): error: dimension overflow.";
    }
    else  // then we've reached currentDim==maxDim
    {
        // add this minterval to the tile domain list
        cornerList->push_back(currentInterval);
        // LINFO << "QtExtend::extendGetCornerTiles(): added " << currentInterval << " to tile domain list.";
    }

    // LINFO << "QtExtend::extendGetCornerTiles() done.";
}
#endif  // 1

void QtExtend::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtExtend Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryOperation::printTree(tab, s, mode);
}

void QtExtend::printAlgebraicExpression(ostream &s)
{
    s << "extend(";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ",";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}

void QtExtend::optimizeLoad(QtTrimList *trimList)
{
    QtNode::QtTrimList *list1 = NULL, *list2 = NULL;

    if (input1 && input2)
    {
#if 0
// not yet sure what to do -- PB 2005-06-18
        QtNode::QtTrimList::iterator iter;

        //
        // The result of input2 has to be a constant expression.
        //

        // shift of trimList is just possible, if no open bounds are available
        bool openBounds = false;
        for (iter = trimList->begin(); iter != trimList->end() && !openBounds; iter++)
        {
            openBounds = !((*iter)->interval.is_low_fixed()) || !((*iter)->interval.is_high_fixed());
        }

        if (openBounds)
        {
            // release( trimList->begin(), trimList->end() );
            for (QtNode::QtTrimList::iterator iter = trimList->begin(); iter != trimList->end(); iter++)
            {
                delete *iter;
                *iter = NULL;
            }
            delete trimList;
            trimList = NULL;

            LERROR << "Error: QtExtend::optimizeLoad() - spatial domain shift of open bounds is not supported";
// XXX need new error code
            parseInfo.setErrorNo(SHIFT_OPENBOUNDSINCOMPATIBLE);
            throw parseInfo;
        }

        QtData *operand = input2->evaluate(NULL);

        if (!operand)
        {
            // release( trimList->begin(), trimList->end() );
            for (QtNode::QtTrimList::iterator iter = trimList->begin(); iter != trimList->end(); iter++)
            {
                delete *iter;
                *iter = NULL;
            }
            delete trimList;
            trimList = NULL;

            LERROR <<  "Error: QtExtend::optimizeLoad() - second operand of extend function must be a constant expression.";
// XXX correct new error code
            parseInfo.setErrorNo(SHIFT_CONSTEXPREQUIRED);
            throw parseInfo;
        }

        if (operand->getDataType() != QT_MINTERVAL)
        {
            // release( trimList->begin(), trimList->end() );
            for (QtNode::QtTrimList::iterator iter = trimList->begin(); iter != trimList->end(); iter++)
            {
                delete *iter;
                *iter = NULL;
            }
            delete trimList;
            trimList = NULL;

            operand->deleteRef();

            LERROR << "Error: QtExtend::optimizeLoad() - second operand must be of type Minterval.";
// XXX correct new error code
            parseInfo.setErrorNo(SHIFT_POINTREQUIRED);
            throw parseInfo;
        }

        // get extend target domain
        const r_Minterval &targetDomain = ((QtPointData *)operand)->getMintervalData();

        // shift trim elements by -transPoint
        // XXX replace with extend() code
        for (iter = trimList->begin(); iter != trimList->end(); iter++)
        {
            QtTrimElement *elem = *iter;

            if (elem->dimension <= transPoint.dimension())
            {
                elem->interval.set_interval(elem->interval.low()  - transPoint[elem->dimension], elem->interval.high() - transPoint[elem->dimension]);
            }
        }

        // point is not needed anymore
        operand->deleteRef();
#endif  // 0 not yet sure what to do -- PB 2005-06-18

        input1->optimizeLoad(trimList);
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

const QtTypeElement &
QtExtend::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input1 && input2)
    {
        // get input types
        const QtTypeElement &inputType1 = input1->checkType(typeTuple);
        const QtTypeElement &inputType2 = input2->checkType(typeTuple);

        if (inputType1.getDataType() != QT_MDD)
        {
            LERROR << "Error: QtExtend::checkType() - first operand must be of type MDD.";
            parseInfo.setErrorNo(MDDARGREQUIRED);
            throw parseInfo;
        }

        if (inputType2.getDataType() != QT_MINTERVAL)
        {
            LERROR << "Error: QtExtend::checkType() - second operand must be of type Minterval.";
            parseInfo.setErrorNo(EXTEND_MINTERVALREQUIRED);
            throw parseInfo;
        }

        // pass MDD type
        dataStreamType = inputType1;
    }
    else
    {
        LERROR << "Error: QtExtend::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}

// --- QtScale --------------------------------------------------

const QtNode::QtNodeType QtScale::nodeType = QT_SCALE;

QtScale::QtScale(QtOperation *mddOp, QtOperation *pointOp)
    : QtBinaryOperation(mddOp, pointOp)
{
}

bool QtScale::isCommutative() const
{
    return false;  // NOT commutative
}

#include <iomanip>
#include <math.h>

// this define was used during testing, we had a problem
inline double FLOOR(double a)
{
    return floor(a);
}

QtData *
QtScale::evaluate(QtDataList *inputList)
{
    startTimer("QtScale");

    QtData *returnValue = NULL;
    QtData *operand1 = NULL;
    QtData *operand2 = NULL;

    if (!getOperands(inputList, operand1, operand2))
        return returnValue;

    const auto cleanupOnMethodExit = common::make_scope_guard(
        [operand1, operand2]() noexcept
        {
            if (operand1) operand1->deleteRef();
            if (operand2) operand2->deleteRef();
        });

    QtMDD *qtMDDObj = static_cast<QtMDD *>(operand1);
    vector<r_Double> scaleVector(qtMDDObj->getLoadDomain().dimension());

    r_Minterval sourceDomain = qtMDDObj->getLoadDomain();
    r_Minterval wishedTargetDomain;
    bool isWishedTargetSet = false;

    switch (operand2->getDataType())
    {
    case QT_POINT:
    {
        const r_Point &transPoint = (static_cast<QtPointData *>(operand2))->getPointData();

        if (transPoint.dimension() == qtMDDObj->getLoadDomain().dimension())
        {
            for (size_t i = 0; i < scaleVector.size(); i++)
                scaleVector[i] = transPoint[i];
        }
        else
        {
            LERROR << "dimensionalities of MDD and scale expression are not matching.";
            parseInfo.setErrorNo(BIT_WRONGOPERANDTYPE);
            throw parseInfo;
        }
    }
    break;

    case QT_CHAR:
    case QT_USHORT:
    case QT_ULONG:
    {
        for (unsigned int i = 0; i < scaleVector.size(); i++)
            scaleVector[i] = (static_cast<QtAtomicData *>(operand2))->getUnsignedValue();
    }
    break;

    case QT_OCTET:
    case QT_SHORT:
    case QT_LONG:
    {
        for (unsigned int i = 0; i < scaleVector.size(); i++)
            scaleVector[i] = (static_cast<QtAtomicData *>(operand2))->getSignedValue();
    }
    break;

    case QT_DOUBLE:
    case QT_FLOAT:
    {
        for (unsigned int i = 0; i < scaleVector.size(); i++)
            scaleVector[i] = (static_cast<QtAtomicData *>(operand2))->getDoubleValue();
    }
    break;

    case QT_MINTERVAL:
    {
        wishedTargetDomain = (static_cast<QtMintervalData *>(operand2))->getMintervalData();
        isWishedTargetSet = true;

        if (wishedTargetDomain.dimension() != sourceDomain.dimension())
        {
            LERROR << "dimensionalities of MDD and scale expression are not matching.";
            parseInfo.setErrorNo(BIT_WRONGOPERANDTYPE);
            throw parseInfo;
        }

        for (r_Dimension i = 0; i < scaleVector.size(); i++)
        {
            const auto sourceRange = static_cast<r_Double>(sourceDomain[i].get_extent());
            const auto targetRange = static_cast<r_Double>(wishedTargetDomain[i].get_extent());

            if (sourceRange != 0.)
            {
                scaleVector[i] = targetRange / sourceRange;
                auto f = scaleVector[i];

                const auto slow = sourceDomain[i].low();
                const auto shigh = sourceDomain[i].high();
                auto low = FLOOR(f * slow);
                //correction by 1e-6 to avoid the strange bug when high was a
                //integer value and floor return value-1(e.g. query 47.ql)
                auto high = FLOOR(f * (shigh + 1) + 0.000001) - 1;
                // apparently the above correction doesn't work for certain big numbers,
                // e.g. 148290:148290 is scaled to 74145:74144 (invalid) by factor 0.5 -- DM 2012-may-25
                if (high < low)
                {
                    if (high > 0)
                        high = low;
                    else
                        low = high;
                }

                LTRACE << "Scale dimension " << i << ":"
                       << "\nbefore f=" << setprecision(12) << f
                       << "\nprecalculated low/high: " << low << ':' << high << " <--> wished low/high: "
                       << wishedTargetDomain[i].low() << ':' << wishedTargetDomain[i].high()
                       << "\npro memoria: " << static_cast<r_Range>(f * (shigh + 1)) << ", raw: " << (f * (shigh + 1))
                       << ", floor: " << floor(f * (shigh + 1)) << ", ceil: " << ceil(f * (shigh + 1));

                if (!std::equal_to<r_Double>()((high - low + 1), targetRange))
                {
                    f = f + (targetRange - (high - low + 1)) / sourceRange;
                    scaleVector[i] = f;

                    low = FLOOR(f * slow);
                    //correction by 1e-6 to avoid the strange bug when high was a
                    //integer value and floor return value-1(e.g. query 47.ql)
                    high = FLOOR(f * (shigh + 1) + 0.000001) - 1;
                    if (high < low)
                    {
                        if (high > 0)
                            high = low;
                        else
                            low = high;
                    }

                    LTRACE << "low/high correction necessary, new values: " << low << ':' << high << ", f=" << setprecision(12) << f;
                }
            }
            else
            {
                scaleVector[i] = 0;  //exception? it can't happen, this error is filtered long before reaching this point
            }
        }
    }
    break;
    default:
        LDEBUG << "bad type for operand2 in scale " << operand2->getDataType();
        break;
    }

    // -----------------------------------------------------------------------------

    // scale domain
    r_Minterval targetDomain;
    if (!scaleDomain(sourceDomain, scaleVector, targetDomain))
    {
        LERROR << "empty result after scaling.";
        parseInfo.setErrorNo(DOMAINSCALEFAILED);
        throw parseInfo;
    }

    r_Point translation;
    if (isWishedTargetSet)
    {
        translation = wishedTargetDomain.get_origin() - targetDomain.get_origin();
        translateTargetDomain(targetDomain, wishedTargetDomain, translation);
    }
    LTRACE << "Target domain: " << targetDomain;

    // create a transient MDD object for the query result
    MDDObj *currentMDDObj = qtMDDObj->getMDDObject();
    MDDObj *resultMDD = new MDDObj(currentMDDObj->getMDDBaseType(), targetDomain, currentMDDObj->getNullValues());

    auto sourceDomainOrigin = r_Point{r_Dimension(scaleVector.size())};  // all zero!!

    // get all tiles
    auto *tiles = currentMDDObj->intersect(qtMDDObj->getLoadDomain());
    vector<Tile *> *tmpTiles = new vector<Tile *>();
    for (auto tileIter = tiles->begin(); tileIter != tiles->end(); tileIter++)
    {
        Tile *t = const_cast<Tile *>(tileIter->get());
        tmpTiles->push_back(t);
    }
    Tile *sourceTile = new Tile(tmpTiles);
    delete tmpTiles;
    tmpTiles = NULL;

    // get relevant area of source tile
    auto sourceTileDomain = qtMDDObj->getLoadDomain().create_intersection(sourceTile->getDomain());
    LTRACE << "Source tile domain: " << sourceTileDomain;

    // compute scaled  tile domain and check if it exists
    r_Minterval targetTileDomain;
    if (sourceTile->scaleGetDomain(sourceTileDomain, scaleVector, targetTileDomain))
    {
        LTRACE << "Target tile domain: " << targetTileDomain;
        // create a new transient tile
        Tile *targetTile = new Tile(targetTileDomain, currentMDDObj->getCellType());
        targetTile->execScaleOp(sourceTile, sourceTileDomain);

        if (isWishedTargetSet)
        {
            r_Minterval &scaledTileDomain = const_cast<r_Minterval &>(targetTile->getDomain());
            translateTargetDomain(scaledTileDomain, wishedTargetDomain, translation);
            LTRACE << "Translated target tile domain: " << scaledTileDomain;
        }

        resultMDD->insertTile(targetTile);
    }

    // create a new QtMDD object as carrier object for the transient MDD object
    returnValue = new QtMDD(static_cast<MDDObj *>(resultMDD));

    // delete the tile vector, the tiles itself are deleted when the destructor
    // of the MDD object is called
    delete tiles;
    tiles = NULL;
    delete sourceTile;
    sourceTile = NULL;

    stopTimer();

    return returnValue;
}

void QtScale::translateTargetDomain(r_Minterval &dst, const r_Minterval &src, r_Point &translation)
{
    dst.translate(translation);
    // adjust target domain to fit in the wished target domain, in case it was badly calculated
    r_Point correctedTranslation(translation.dimension());
    for (r_Dimension i = 0; i < dst.dimension(); i++)
    {
        const auto &s = src[i];
        auto &d = dst[i];
        if (d.low() < s.low())
            correctedTranslation[i] = s.low() - d.low();
        else if (d.low() > s.high())
            correctedTranslation[i] = s.high() - d.low();

        if (d.high() > s.high())
            correctedTranslation[i] = s.high() - d.high();
        else if (d.high() < s.low())
            correctedTranslation[i] = s.low() - d.high();
    }
    dst.translate(correctedTranslation);
    translation = translation + correctedTranslation;
}

void QtScale::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtScale Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryOperation::printTree(tab, s, mode);
}

void QtScale::printAlgebraicExpression(ostream &s)
{
    s << "scale(";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ",";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}

void QtScale::optimizeLoad(QtTrimList *trimList)
{
    // Don't forward the load domain to the underlying node,
    // as the domain of the input is not known at this step, and thus
    // we can't calculate which part of it will be actually used.
    if (trimList)
    {
        for (QtNode::QtTrimList::iterator iter = trimList->begin(); iter != trimList->end(); iter++)
        {
            delete *iter;
            *iter = NULL;
        }
        delete trimList;
        trimList = NULL;
    }
    if (input1)
    {
        input1->optimizeLoad(new QtNode::QtTrimList);
    }
}

const QtTypeElement &
QtScale::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input1 && input2)
    {
        // get input types
        const QtTypeElement &inputType1 = input1->checkType(typeTuple);
        const QtTypeElement &inputType2 = input2->checkType(typeTuple);

        if (inputType1.getDataType() != QT_MDD)
        {
            LERROR << "Error: QtScale::checkType() - first operand must be of type MDD.";
            parseInfo.setErrorNo(SCALE_MDDARGREQUIRED);
            throw parseInfo;
        }

        if (inputType2.getDataType() != QT_POINT && inputType2.getDataType() != QT_MINTERVAL &&
            inputType2.getDataType() != QT_FLOAT && inputType2.getDataType() != QT_DOUBLE &&
            !inputType2.isInteger())
        {
            LERROR << "Error: QtScale::checkType() - second operand must be either of type Point, Integer or Float.";
            parseInfo.setErrorNo(SCALE_INDICATORINVALID);
            throw parseInfo;
        }

        // pass MDD type
        dataStreamType = inputType1;
    }
    else
    {
        LERROR << "Error: QtScale::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}

int QtScale::scaleDomain(const r_Minterval &areaOp,
                         const vector<double> &scaleFactors,
                         r_Minterval &areaScaled)
{
    try
    {
        areaScaled = areaOp.create_scale(scaleFactors);
    }
    catch (r_Error &ex)
    {
        //error scaling
        std::string scaleStr{};
        for (const auto &f: scaleFactors)
        {
            if (!scaleStr.empty())
            {
                scaleStr += ",";
            }
            scaleStr += std::to_string(f);
        }
        LERROR << "Error: QtScale::scaleDomain() - exception while determining scale target interval for " << areaOp << " and " << scaleStr;
        return 0;
    }

    return 1;
}

// origin1 von getLoadDomain
// origin2 von getCurrentDomain

int QtScale::scaleDomain(const r_Minterval &areaOp, const r_Point &origin1, const r_Point &origin2,
                         const vector<double> &scaleFactors, r_Minterval &areaScaled)
{
    r_Minterval tempIv = areaOp;

    //reverse_translated with origin1
    tempIv.reverse_translate(origin1);

    //scale it normaly
    if (!scaleDomain(tempIv, scaleFactors, areaScaled))
    {
        return 0;
    }

    //translate areaScaled to origin2
    areaScaled.translate(origin2);

    return 1;
}
