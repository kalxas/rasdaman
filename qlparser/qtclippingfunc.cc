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

//includes qlparser
#include "qlparser/qtclippingfunc.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtnode.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtmshapedata.hh"
#include "qlparser/qtmshapeop.hh"
#include "qlparser/qtfindsection.hh"

//includes raslib
#include "raslib/miter.hh"
#include "raslib/mitera.hh"

//includes mddmgr
#include "mddmgr/mddobj.hh"

//includes tilemgr
#include "tilemgr/tile.hh"

//includes catalogmgr
#include "catalogmgr/typefactory.hh"

//includes relcatalogif
#include "relcatalogif/structtype.hh"
#include "relcatalogif/mdddimensiontype.hh"
#include "relcatalogif/mdddomaintype.hh"
#include "relcatalogif/syntaxtypes.hh"
#include "qtclippingfunc.hh"


//included libraries

#include <logging.hh>

#ifndef CPPSTDLIB
#else
#include <string>
#endif

#include <cmath>
#include <sstream>
#include <iostream>
#include <stack>
#include <vector>
#include <boost/make_shared.hpp>
#include <boost/shared_ptr.hpp>

struct mapCmpMinterval
{
    bool operator()(const r_Minterval &a, const r_Minterval &b) const
    {
        bool firstIsBigger = true;
        for (r_Dimension d = 0; d < a.dimension(); d++)
        {
            if ((a[d].high() - a[d].low()) < (b[d].high() - b[d].low()))
            {
                return true;
            }
            else if ((a[d].high() - a[d].low()) > (b[d].high() - b[d].low()))
            {
                return false;
            }
            else
            {
                continue;
            }
        }
        return false;
    }
};

const QtNode::QtNodeType QtClipping::nodeType = QtNode::QT_CLIPPING;

QtClipping::QtClipping(QtOperation *mddOp, QtOperation *geometryOp)
    : QtBinaryOperation(mddOp, geometryOp)
{
}

QtClipping::~QtClipping()
{
}

bool QtClipping::isCommutative() const
{
    return false; // NOT commutative
}

MDDObj *
QtClipping::extractBresenhamLine(const MDDObj *op,
                                 const r_Minterval &areaOp,
                                 QtMShapeData *mshape,
                                 const r_Dimension dim)
{
    // computing the bounding box of the multidimensional shape defined in QtMShapeData
    std::unique_ptr<BoundingBox> bBox;
    bBox.reset(computeBoundingBox(mshape));

    MDDDimensionType *mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), dim);
    MDDBaseType *mddBaseType = static_cast<MDDBaseType *>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    // the dataset dimension is the same as the dimension of the points defining the mshape, so
    // to extract the dataset dimension we use the first point.
    r_Dimension datasetDimension = (mshape->getMShapeData())[0].dimension();

    // directionVectors contains a set of n orthogonal vectors where n is the dimension of the dataset. The first
    // m vectors, where m is the dimension of the mshape define the space in which the m-dimensional shape lies into
    // The remaining vectors are vectors orthogonal to the mshape.
    std::vector<r_PointDouble> *directionVectors = mshape->getDirectionVectors();

    // Construct r_Minterval from the bounding box of the multidimensional shape
    r_Minterval mintervalBoundingBox = bBox->getHull();

    // check in case there are no points in common between the smallest r_Minterval containing the mshape and the dataset itself.
    // In that case there is no need for further computations and an exception is thrown
    if (!areaOp.intersects_with(mintervalBoundingBox))
    {
        LERROR << "Error: The subspace of the set of points provided does not pass through the stored data.";
        parseInfo.setErrorNo(SUBSPACENOINTERSECTION);
        throw parseInfo;
    }

    r_Minterval resAreaOp = areaOp.create_intersection(mintervalBoundingBox);

    // vector containing r_Points that define the line we want to extract.
    vector<r_Point> bresenhamLine = computeNDBresenhamLine(mshape);

    // domain of the relevant area of the actual dbobj corresponds to the bounding box of the start and end points.
    r_Minterval domainOfInterest = localHull(std::make_pair<int, int>(-1, -1), bresenhamLine);

    // resultDom's r_Sinterval corresponds to the longest extent of domainOfInterestGlobal
    std::vector<r_Range> bbExtents = (domainOfInterest.get_extent()).getVector();
    r_Range maxExtent = *(std::max_element(bbExtents.begin(), bbExtents.end()));
    r_Dimension index = std::distance(bbExtents.begin(), std::max_element(bbExtents.begin(), bbExtents.end()));

    // startEndIndicesGlobal keeps track of the segment of the line contained inside areaOp.
    pair<int, int> startEndIndicesGlobal = endpointsSearch(resAreaOp, bresenhamLine);

    // construct the result domain using the largest direction's index.
    // the extent here corresponds to the total number of result points
    r_Minterval resultDomainGlobal(1);
    resultDomainGlobal[0] = localHullByIndex(startEndIndicesGlobal, bresenhamLine, index);

    //result tile
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset(new MDDObj(mddBaseType, resultDomainGlobal));

    // get all tiles in relevant area
    std::unique_ptr<std::vector<boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->getTiles());

    // iterate over the tiles
    try
    {
        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++)
        {
            // domain of the actual tile
            const r_Minterval &tileDom = (*tileIt)->getDomain();
            if (tileDom.intersects_with(resAreaOp))
            {
                //we first determine which band of values from the BLA result apply to this tile
                std::pair<int, int> startEndIndices = endpointsSearch(tileDom, bresenhamLine);

                if (startEndIndices.first == -1)
                {
                    // continue to next tile since we have no points to read from this one.
                    continue;
                }

                // domain of the result data set.
                r_Minterval resultDomain(1);
                resultDomain[0] = localHullByIndex(startEndIndices, bresenhamLine, index);

                // data type size
                size_t typeSize = (*tileIt)->getType()->getSize();

                // result tile and contents
                boost::shared_ptr<Tile> resTile;
                resTile.reset(new Tile(resultDomain, (*tileIt).get()->getType()));
                char *resultData = resTile->getContents();

                // compute position in the source char* array for assigning the result tile content
                char *sourceData = (*tileIt)->getCell(bresenhamLine[static_cast<size_t>(startEndIndices.first)]);
                // we could insert this at the front of bresenhamLine, but this way is faster as it avoids an O(length of line) computation
                memcpy(resultData, sourceData, typeSize);

                // take care to avoid double-copying the point in case there is only a single point in this tile
                if (startEndIndices.second == startEndIndices.first)
                {
                    // insert Tile in result mdd
                    resultMDD->insertTile(resTile);
                    continue;
                }
                // loop over bresenhamLine points which are relevant for this tile
                // and transfer data from source to result tiles.
                for (size_t i = static_cast<size_t>(startEndIndices.first); i < static_cast<size_t>(startEndIndices.second); i++)
                {
                    // move to the next data point to be copied
                    // complexity O(dimensionality)
                    sourceData = (*tileIt)->getCell(bresenhamLine[i + 1]);
                    resultData += (typeSize);
                    memcpy(resultData, sourceData, typeSize);
                }

                // insert Tile in result mdd
                resultMDD->insertTile(resTile);
            }
        }
    }

    catch (r_Error &err)
    {
        LERROR << "Line clipping error: " << err.get_errorno() << " " << err.what();
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }

    catch (int err)
    {
        LERROR << "Line clipping error: " << err << ".";
        parseInfo.setErrorNo(err);
        throw parseInfo;
    }

    return resultMDD.release();
}

MDDObj *
QtClipping::extractSubspace(const MDDObj *op,
                            const r_Minterval &areaOp,
                            QtMShapeData *mshape)
{
    // dimension of the source space.
    r_Dimension datasetDimension = (mshape->getMShapeData())[0].dimension();

    // compute the bounding box of the mShape
    std::unique_ptr<BoundingBox> bBox;
    bBox.reset(computeBoundingBox(mshape));


    // construct an r_Minterval from the bounding box of mShape
    r_Minterval mintervalBoundingBox(datasetDimension);
    for (r_Dimension i = 0; i < datasetDimension; i++)
    {
        r_Sinterval rs(static_cast<r_Range>(bBox->minPoint[i]), static_cast<r_Range>(bBox->maxPoint[i]));
        mintervalBoundingBox << rs;
    }

    //throw an exception in case the bounding box does not lie in the collection.
    if (!areaOp.intersects_with(mintervalBoundingBox))
    {
        LERROR << "Error: The subspace of the set of points provided does not pass through the stored data.";
        parseInfo.setErrorNo(SUBSPACENOINTERSECTION);
        throw parseInfo;
    }

    //the area of interest of the intersection
    r_Minterval tranBox = areaOp.create_intersection(mintervalBoundingBox);

    // a set of dimensions where the subspace is going to be projected.
    // This set is used to create the tiles of the result mddobj.
    // They might not be in a 1:1 correspondence with the operand mddobj's tiles.
    std::set<r_Dimension, std::less<r_Dimension>> projectionDimensionSet;

    // bBoxRemoveDimension is a datastructure (r_PointDouble) that keeps track of the dimension we want to throw away.
    r_PointDouble bBoxRemoveDimension(datasetDimension);

    // compute the r_Minteral of the result object. Also fill in and construct the set defined above (projectionDimensionSet)
    r_Minterval subspaceAreaOp(mshape->getDimension());
    subspaceAreaOp = computeProjectedMinterval(mshape, bBox.get(), &bBoxRemoveDimension, projectionDimensionSet);

    // build a keptDimensions vector from the projectionDimensionSet;
    std::vector<r_Dimension> keptDimensions;
    keptDimensions.reserve(mshape->getDimension());

    //initializing keptDimensions
    for (size_t i = 0; i < tranBox.dimension(); i++)
    {
        if (projectionDimensionSet.find(i) == projectionDimensionSet.end())
        {
            keptDimensions.emplace_back(i);
        }
    }

    //projected domain -- domain of the result object
    const r_Minterval projectedDomain = computeProjectedDomain(tranBox, projectionDimensionSet, mshape->getDimension());

    MDDBaseType *mddBaseType = const_cast<MDDBaseType *>(op->getMDDBaseType());
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset(new MDDObj(mddBaseType, projectedDomain));

    // get all tiles in relevant area
    std::unique_ptr<std::vector<boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->intersect(tranBox));
    boost::shared_ptr<Tile> resTile;
    resTile.reset(new Tile(projectedDomain, (*(allTiles->begin()))->getType()));

    //data type size
    size_t typeSize = (*(allTiles->begin()))->getType()->getSize();


    // initialize the data in the resTile to null
    op->fillTileWithNullvalues(resTile->getContents(), projectedDomain.cell_count());

    //object for computing the point coordinates of the preimage of the projection in the subspace.
    FindSection resFinder(mshape->computeHyperplaneEquation(), keptDimensions);
    //prepare additional variables for later computation.
    resFinder.prepareSection();
    try
    {
        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++)
        {

            //domain of source tile
            const r_Minterval &tileDom = (*tileIt)->getDomain();

            //fill result tile contents...

            //dimensions of the area of interest intersected with the current source tile
            r_Minterval intersectDom(tileDom.create_intersection(tranBox));
            r_Point sourceExtents = intersectDom.get_extent();
            // dimensions of the result tile's area of interest for this pass
            r_Minterval resIntDom = computeProjectedDomain(intersectDom, projectionDimensionSet, mshape->getDimension());

            //solving the linear systems and placing the data in the cell if the sol'n lies in the AOI;

            // source data pointer
            char *sourceData = (*tileIt)->getContents();
            // result data pointer
            char *resultData = resTile->getContents();
            // starting point of iteration
            r_Point currentPoint = resIntDom.get_origin();

            for (r_Area localOffset = 0; localOffset < resIntDom.cell_count(); localOffset++)
            {
                //the point in the result domain
                if (localOffset > 0)
                {
                    currentPoint = resIntDom.cell_point(localOffset);
                }

                //method for finding the integer point in the tile from the projected domain's coordinates
                //utilizes an LU solver
                //set pointInTile to the nearest integer lattice point resulting from the LU solver
                auto pointInTile = resFinder.solveLU(currentPoint).toIntPoint();

                //it could be that the result is located in another tile
                if (tileDom.covers(pointInTile))
                {
                    memcpy(resultData + (projectedDomain.cell_offset(currentPoint))*typeSize,
                           sourceData + (tileDom.cell_offset(pointInTile))*typeSize,
                           typeSize);
                }
            }
        }

        //insert our tile into the resultMDD
        resultMDD->insertTile(resTile);
    }
    catch (r_Error &err)
    {
        LERROR << "Subspace clipping error: " << err.get_errorno() << " " << err.what();
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }

    catch (int err)
    {
        LERROR << "Subspace clipping error: " << err << ".";
        parseInfo.setErrorNo(err);
        throw parseInfo;
    }

    return resultMDD.release();
}

//functor for passing to the remove_if iterator in std::vector during extractLinestring and extractCurtainLinestring
bool isSingleton(const r_Minterval &interval)
{
    return interval.cell_count() == 1;
}

BaseType *QtClipping::getTypeWithCoordinates(const BaseType *valuesType, const r_Dimension dim) const
{
    bool isStructValuesType = valuesType->getType() == TypeEnum::STRUCT;
    const size_t bandNo = isStructValuesType ? static_cast<const StructType *>(valuesType)->getNumElems() : 1;

    auto *stype = new StructType("coordinates_values_base_type", dim + bandNo);
    // add coodinate types
    for (size_t i = 1; i <= dim; ++i)
    {
        string bandName = "d" + std::to_string(i);
        stype->addElement(bandName.c_str(), TypeFactory::mapType("Long"));
    }
    // add value types (flatten if type is composite)
    if (isStructValuesType)
    {
        const auto *structValuesType = static_cast<const StructType *>(valuesType);
        for (size_t i = 0; i < bandNo; ++i)
        {
            stype->addElement(structValuesType->getElemName(i), structValuesType->getElemType(i));
        }
    }
    else
    {
        stype->addElement("value", valuesType);
    }
    TypeFactory::addTempType(stype);
    return stype;
}

MDDObj *
QtClipping::extractLinestring(const MDDObj *op,
                              const QtMShapeData *mshape,
                              const r_Dimension dim)
{
    // create vector of bounding boxes (one for each line segment in the linestring)
    vector<r_Minterval> bBoxes = mshape->localConvexHulls();

    //only consider the segments which contribute new points to the result vector
    //as above, the start and end points must differ.
    bBoxes.erase(remove_if(bBoxes.begin(), bBoxes.end(), isRedundant), bBoxes.end());

    //for each one, we construct a vector (actually a pair) of r_Points representing the endpoints of the line segment being considered

    vector< vector< r_PointDouble >> vectorOfSegmentEndpointPairs = vectorOfPairsWithoutMultiplicity(mshape->getMShapeData(), bBoxes.size());

    // create vector of bresenham lines (one for each line segment passing through the domain of the MDDObject)
    vector< vector < r_Point >> vectorOfBresenhamLines;
    vectorOfBresenhamLines.reserve(vectorOfSegmentEndpointPairs.size());
    for (size_t i = 0; i < vectorOfSegmentEndpointPairs.size(); i++)
    {
        vectorOfBresenhamLines.emplace_back(computeNDBresenhamSegment(vectorOfSegmentEndpointPairs[i]));
    }

    // create vector of intervals for the result tiles
    //[0 : k_0-1], [k_0 : k_0 + k_1 - 1], [k_0 + k_1 : k_0 + k_1 + k_2 - 1], ...
    //for each segment, we need to find the intersection of its bounding box's longest extent's dimension with the domain of the MDDObject being considered

    //vector of dimension #'s corresponding to the longest extents in bBoxes
    vector<r_Dimension> longestExtentDims;
    longestExtentDims.reserve(bBoxes.size());
    vector< vector<r_Range>> bBoxesExtents;
    bBoxesExtents.reserve(bBoxes.size());
    for (size_t i = 0; i < bBoxes.size(); i ++)
    {
        bBoxesExtents.emplace_back(bBoxes[i].get_extent().getVector());
        longestExtentDims.emplace_back(std::distance(bBoxesExtents[i].begin(),
                                       std::max_element(bBoxesExtents[i].begin(),
                                               bBoxesExtents[i].end())));
    }

    //construct the resulting tile intervals
    vector<r_Minterval> resultTileMintervals = vectorOfResultTileDomains(bBoxes, longestExtentDims);

    //   loop over source tiles
    //  loop over bresenhamLines vector
    //first check the intersection of the source tile with the bounding box, then construct the startEndIndices pair for that line segment
    //process Bresenham into the respective output tile

    // get all tiles in relevant area
    std::unique_ptr<std::vector<boost::shared_ptr<Tile>>> srcTiles;
    srcTiles.reset(op->getTiles());

    //initialize the result tiles here
    const BaseType *resultType = srcTiles->front()->getType();

    // if coordinates should be included
    if (withCoordinates)
    {
        resultType = getTypeWithCoordinates(resultType, dim);
    }

    // prepare result tiles with null values
    vector< boost::shared_ptr<Tile>> resultTiles;
    resultTiles.reserve(resultTileMintervals.size());
    for (size_t i = 0; i < resultTileMintervals.size(); i++)
    {
        //build a new tile
        auto resultTile = boost::make_shared<Tile>(resultTileMintervals[i], resultType);
        //initialize contents to null values
        char *resData = resultTile->getContents();
        op->fillTileWithNullvalues(resData, resultTileMintervals[i].cell_count());
        //add tile to vector of result tiles
        resultTiles.emplace_back(resultTile);
    }

    //construct the result MMDDObj
    r_Minterval resultDomain(1);
    resultDomain << r_Sinterval(static_cast<r_Range>(0), resultTileMintervals.back()[0].high());

    MDDDimensionType *mddDimensionType = new MDDDimensionType("tmp", resultType, dim);
    TypeFactory::addTempType(mddDimensionType);
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset(new MDDObj(mddDimensionType, resultDomain, op->getNullValues()));

    try
    {
        const size_t typeSize = srcTiles->front()->getType()->getSize();
        //loop over source tiles
        for (const auto &srcTile : *srcTiles)
        {
            //loop over bresenham line segments (we do not use an iterator because we need the index for various vectors of the same length)
            for (size_t i = 0; i < bBoxes.size(); ++i)
            {
                //current result tile's data
                char *resData = (resultTiles[i])->getContents();

                auto ptIt = vectorOfBresenhamLines[i].cbegin();
                if (i > 0)
                {
                    ++ptIt;    // skip first coordinate for all intervals except the first one (i == 0)
                }

                //iterate over the points in the current bresenham line segment
                for (auto ptEnd = vectorOfBresenhamLines[i].cend(); ptIt != ptEnd; ++ptIt)
                {
                    const auto &pt = *ptIt;

                    // add coordinates first
                    if (withCoordinates)
                    {
                        for (size_t ii = 0; ii < dim; ++ii)
                        {
                            *((r_Long *) resData) = pt[ii];
                            resData += sizeof(r_Long);
                        }
                    }
                    // then add src value if the point is in the src domain
                    if (srcTile->getDomain().covers(pt))
                    {
                        //ptr to source data cell
                        char *srcData = srcTile->getCell(pt);
                        memcpy(resData, srcData, typeSize);
                    }
                    resData += typeSize;
                }
            }
        }
        //add result tiles to result MDDObj
        for (const auto resTile : resultTiles)
        {
            resultMDD->insertTile(resTile);
        }
    }
    catch (r_Error &err)
    {
        LERROR << "Linestring clipping error: " << err.get_errorno() << " " << err.what();
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }
    catch (int err)
    {
        LERROR << "Linestring clipping error: " << err << ".";
        parseInfo.setErrorNo(err);
        throw parseInfo;
    }

    return resultMDD.release();
}

MDDObj *
QtClipping::extractMultipolygon(const MDDObj *op,
                                const r_Minterval &areaOp,
                                std::vector<QtPositiveGenusClipping> &clipVector,
                                QtGeometryData::QtGeometryType geomType)
{
    //for each clipping in the vector, we generate a mask,
    //and we assemble them into a single result mask

    //constructing the result domain for the mask and the resultmdd
    std::shared_ptr<r_Minterval> resultMaskDom = buildResultDom(areaOp, clipVector);

    //in case nothing of interest was hit, we can simply return to the parent method, where we will throw an error.
    if (!resultMaskDom)
    {
        return NULL;
    }

    auto resultMask = buildResultMask(resultMaskDom, clipVector, geomType);

    //generate resultMDD
    MDDDimensionType *mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), 3);
    MDDBaseType *mddBaseType = static_cast<MDDBaseType *>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    std::shared_ptr<r_Minterval> resultDom;
    resultDom.reset(new r_Minterval(resultMaskDom->create_intersection(areaOp)));

    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset(new MDDObj(mddBaseType, *resultDom, op->getNullValues()));

    // here, we apply the resultMask to each tile to generate the output tiles.

    //iterate over the source tiles
    std::unique_ptr<std::vector<boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->intersect(*resultMaskDom));
    try
    {
        //data type size
        size_t typeSize = (*(allTiles->begin()))->getType()->getSize();

        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++)
        {
            //domain of source tile
            const r_Minterval &srcTileDom = (*tileIt)->getDomain();
            //data pointer of source tile
            const char *sourceDataPtr = (*tileIt)->getContents();

            //construct result tile
            boost::shared_ptr<Tile> resTile;
            r_Minterval intersectDom = resultDom->create_intersection(srcTileDom);
            resTile.reset(new Tile(intersectDom, op->getCellType()));

            char *resDataPtr = resTile->getContents();

            //initialize the tile to be filled with nullValues.
            op->fillTileWithNullvalues(resDataPtr, intersectDom.cell_count());

            //construct iterators for filling data in result tile
            r_Miter resTileMaskIterator(&intersectDom, resultMaskDom.get(), sizeof(char), resultMask.get());
            r_Miter sourceTileIterator(&intersectDom, &srcTileDom, typeSize, sourceDataPtr);
            r_Miter resTileIterator(&intersectDom, &intersectDom, typeSize, resDataPtr);

            while (!resTileMaskIterator.isDone())
            {
                //step to next cell for resTileMaskIterator
                if (*resTileMaskIterator.nextCell() < 2)
                {
                    //step to next cell for resTileIterator and sourceTileIterator, and copy data
                    memcpy(resTileIterator.nextCell(), sourceTileIterator.nextCell(), typeSize);
                }
                else
                {
                    //step to next cell for resTileIterator and sourceTileIterator
                    sourceTileIterator.nextCell();
                    resTileIterator.nextCell();
                }
            }

            // insert Tile in result mdd
            resultMDD->insertTile(resTile);
        }
    }
    catch (r_Error &err)
    {
        LERROR << "Multipolygon clipping error: " << err.get_errorno() << " " << err.what();
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }
    catch (int err)
    {
        LERROR << "Multipolygon clipping error: " << err << ".";
        parseInfo.setErrorNo(err);
        throw parseInfo;
    }


    return resultMDD.release();
}

MDDObj *
QtClipping::extractCurtain(const MDDObj *op, const r_Minterval &areaOp,
                           const std::vector<r_Dimension> &maskDims,
                           const std::pair< std::unique_ptr<char[]>, std::shared_ptr<r_Minterval>> &mask)
{
    // algo for extracting curtain from op using the stored mask

    // r_Minterval corresponding to the mask. used for iterating over the mask in r_Miter
    // our mask stores its data contiguously,
    // and so the mask can be used as the dataset for "convexHull" just as a tile would
    r_Minterval convexHull = *(mask.second);

    // result domain is canonically isomorphic to the product of the fiber of the projection map together with the aoi.
    // we reorder to match the order of the projection dimensions in the method below
    r_Minterval resultDomain = areaOp.trim_along_slice(convexHull, maskDims);

    // generate the result MDDObj with resultDomain as the domain.
    MDDDimensionType *mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), resultDomain.dimension());
    MDDBaseType *mddBaseType = static_cast<MDDBaseType *>(mddDimensionType);
    TypeFactory::addTempType(mddBaseType);
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset(new MDDObj(mddBaseType, resultDomain, op->getNullValues()));

    // pointer to all source tiles
    std::unique_ptr< std::vector< boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->intersect(resultDomain));

    // iterate over source tiles
    for (auto srcTileIter = allTiles->begin(); srcTileIter != allTiles->end(); srcTileIter++)
    {
        // source tile domain
        r_Minterval srcArea((*srcTileIter)->getDomain());

        // subset of the source tile's domain hitting the curtain
        // this is the result tile's domain as well!
        r_Minterval aoiSrc(srcArea.trim_along_slice(convexHull, maskDims));

        // get the type and type size of the data
        const BaseType *baseType = (*srcTileIter)->getType();
        r_Bytes typeSize = baseType->getSize();

        // generate the result tile and initialize its contents to 0
        boost::shared_ptr<Tile> resTile;
        resTile.reset(new Tile(aoiSrc, baseType));
        // result data pointer
        char *resDataPtr = resTile->getContents();
        op->fillTileWithNullvalues(resDataPtr, aoiSrc.cell_count());

        //memset(resDataPtr, 0, typeSize*aoiSrc.cell_count());

        // starting point for iterating through slices in the source tile
        r_Point aoiSrcOrigin = aoiSrc.get_origin();

        // initial slice to be iterated through in r_MiterArea
        r_Minterval aoiSrcSlice(aoiSrc.dimension());
        for (r_Dimension i = 0; i < aoiSrc.dimension(); i++)
        {
            auto it = std::find(maskDims.begin(), maskDims.end(), i);
            if (it == maskDims.end())
            {
                //set the area to be width 1 starting @ origin
                aoiSrcSlice << r_Sinterval(aoiSrcOrigin[i], aoiSrcOrigin[i]);
            }
            else
            {
                aoiSrcSlice << aoiSrc[i];
            }
        }

        r_Minterval aoiMask = aoiSrc.project_along_dims(maskDims);

        // for iterating through the slices in the result domain/source areas of interest slices
        r_MiterArea srcAoiIter(&aoiSrcSlice, &aoiSrc);

        while (!srcAoiIter.isDone())
        {
            r_Minterval aoiSlice = srcAoiIter.nextArea();

            //src tile data iterator -- iterates just over the relevant area in the source tile
            r_Miter srcIter(&aoiSlice, &srcArea, typeSize, (*srcTileIter)->getContents());
            //res tile data iterator -- iterates just over the relevant area in the result tile
            r_Miter resIter(&aoiSlice, &aoiSrc, typeSize, resTile->getContents());
            //mask arg data iterator -- iterates just over the relevant area in the mask
            r_Miter maskIter(&aoiMask, &convexHull, sizeof(char), mask.first.get());

            while (!maskIter.isDone())
            {
                if (*(maskIter.nextCell()) < 2)
                {
                    memcpy(resIter.nextCell(), srcIter.nextCell(), typeSize);
                }
                else
                {
                    srcIter.nextCell();
                    resIter.nextCell();
                }
            }
        }

        resultMDD->insertTile(resTile);
    }

    return resultMDD.release();
}

MDDObj *
QtClipping::extractCorridor(const MDDObj *op, const r_Minterval &areaOp,
                            QtMShapeData *lineStringData,
                            const std::vector<r_Dimension> &maskDims,
                            const std::pair< std::unique_ptr<char[]>, std::shared_ptr<r_Minterval>> &mask,
                            QtGeometryData::QtGeometryFlag geomFlagArg)
{
    // algo for extracting corridor from op using the stored mask
    // lsData.first is the path data and lsData.second is the associated domain for each path
    pair< vector< vector< r_Point >>, vector< r_Minterval >> lsData;

    // if the path is considered to be discrete, no extrapolation occurs
    if (geomFlagArg == QtGeometryData::QtGeometryFlag::DISCRETEPATH)
    {
        lsData = computeDiscreteLinestring(lineStringData);
    }
    else
    {
        lsData = computeLinestring(lineStringData);
    }
    // construct the result linestring interval
    r_Sinterval lineStringDomain(static_cast<r_Range>(0), lsData.second.back()[0].high());
    // the sdom of the mask
    r_Minterval convexHull = *(mask.second);

    // construct the convex hull of the embedding of the result in the source object, and the vector of mask domains (translated according to the linestring)
    // convention: the first mask is assumed to contain the first point of the linestring. We check that here.
    std::vector< r_Minterval > embeddedMaskDomains;
    //embeddedMaskDomains.reserve(static_cast<size_t>(lineStringDomain.get_extent()));

    embeddedMaskDomains = computeMaskEmbedding(lsData.first, convexHull, lineStringDomain.get_extent(), maskDims);

    //now, we build the convex hull of all these intervals, and call it the "outer hull"
    r_Minterval outerHull = embeddedMaskDomains[0];
    for (auto it = embeddedMaskDomains.begin(); it != embeddedMaskDomains.end(); it++)
    {
        outerHull.closure_with(*it);
    }


    // this section is specific to corridors, as we need a stack of intervals for each tile. We need to check each minterval associated with the result anyways, so we can process this "stack" while each individual tile is loaded into memory
    // 0. construct an "outer hull" for limiting the scope of which tiles get loaded into memory, and a vector of areas of interest from the linestring points.
    // 1. create the result domain and object
    // 2. loop through each point in the line string, searching for bands of intersections. Each time an intersection is found, the domain is computed and data is processed into the result
    // 3. loop through the source tiles for each slice --> for each intersection, process into the result domain
    // an optimization in the future might include searching tiles by offset region, but it is not obvious how to do this in a more-efficient way than simply checking each slice!

    // create the result domain -- dimension fixed at 1+maskdim in the current implementation, but our code is flexible.
    // Presently, we only iterate a 2-D mask over a linestring, but any dimension of mask should apply here

    // r_Minterval corresponding to the result is the length of the linestring * the convex hull of the polygon.



    r_Minterval resultDomain(1 + convexHull.dimension());
    //the first dimension corresponds to the length of the linestring
    resultDomain << lineStringDomain;
    // the last two dimensions correspond to the mask dimensions

    for (r_Dimension i = 0; i < convexHull.dimension(); i++)
    {
        resultDomain << convexHull[i];
    }

    // generate the result MDDObj with resultDomain as the domain.
    MDDDimensionType *mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), resultDomain.dimension());
    MDDBaseType *mddBaseType = static_cast<MDDBaseType *>(mddDimensionType);
    TypeFactory::addTempType(mddBaseType);
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset(new MDDObj(mddBaseType, resultDomain, op->getNullValues()));

    // unfortunately, the result consists of a single tile, since source tile data is sheared into place for the result.
    // REMARK (BBELL): I cannot think of a better way of doing this; it might be nice if we could predictively and meaningfully chunk this.

    // generate the unique result tile
    boost::shared_ptr<Tile> resTile;
    resTile.reset(new Tile(resultDomain, op->getCellType()));
    // result data pointer
    char *resDataPtr = resTile->getContents();
    // initialize the result with nullValues
    op->fillTileWithNullvalues(resDataPtr, resultDomain.cell_count());

    // pointer to all source tiles which might intersect our area of interest
    std::unique_ptr< std::vector< boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->intersect(outerHull));

    //if no intersections were found, we throw an error.
    if (allTiles->empty())
    {
        LERROR << "The entire selection exists outside the source MDD.";
        throw r_Error(CURTAINDOMAININTERSECTERROR);
    }

    // iterate over source tiles
    for (auto srcTileIter = allTiles->begin(); srcTileIter != allTiles->end(); srcTileIter++)
    {
        // source tile domain
        r_Minterval srcArea((*srcTileIter)->getDomain());


        // we first need to find which intervals coincide with the source tile, and create a stack for insertion
        // the stack consists of just the index of the interval in the embeddedMaskDomains vector, so r_Dimension
        std::stack< r_Dimension > indices;
        for (auto it = embeddedMaskDomains.begin(); it != embeddedMaskDomains.end(); it++)
        {
            if (it->intersects_with(srcArea))
            {
                r_Dimension index = std::distance(embeddedMaskDomains.begin(), it);
                indices.emplace(index);
            }
        }

        while (!indices.empty())
        {
            // get the type and type size of the data
            const BaseType *baseType = (*srcTileIter)->getType();
            r_Bytes typeSize = baseType->getSize();

            r_Minterval aoiSrc(srcArea.dimension());

            //src aoi
            if (srcArea.intersects_with(embeddedMaskDomains[ indices.top() ]))
            {
                aoiSrc = srcArea.create_intersection(embeddedMaskDomains[ indices.top() ]);
            }
            else
            {
                indices.pop();
                continue;
            }

            //res slice containing aoiRes
            r_Minterval resSlice(1 + convexHull.dimension());
            //first dim is linestring
            resSlice << r_Sinterval(static_cast<r_Range>(indices.top()), static_cast<r_Range>(indices.top()));

            for (r_Dimension i = 0; i < convexHull.dimension(); i++)
            {
                resSlice << convexHull[i];
            }

            //recover the translation, though this could be optimized out somehow... would be great if we could search tiles a bit more creatively!
            r_Point translationPt = embeddedMaskDomains[0].get_origin() - embeddedMaskDomains[ indices.top() ].get_origin();

            //mask aoi
            r_Minterval aoiMask = (aoiSrc.create_translation(translationPt)).project_along_dims(maskDims);

            //result aoi
            r_Minterval aoiRes(1 + aoiMask.dimension());

            aoiRes << r_Sinterval(static_cast<r_Range>(indices.top()), static_cast<r_Range>(indices.top()));

            for (r_Dimension i = 0; i < aoiMask.dimension(); i++)
            {
                aoiRes << aoiMask[i];
            }

            //src tile data iterator -- iterates just over the relevant area in the source tile
            r_Miter srcIter(&aoiSrc, &srcArea, typeSize, (*srcTileIter)->getContents());
            //res tile data iterator -- iterates just over the relevant area in the result tile
            r_Miter resIter(&aoiRes, &resultDomain, typeSize, resTile->getContents());
            //mask arg data iterator -- iterates just over the relevant area in the mask
            r_Miter maskIter(&aoiMask, &convexHull, sizeof(char), mask.first.get());

            while (!maskIter.isDone())
            {
                if (*(maskIter.nextCell()) < 2)
                {
                    memcpy(resIter.nextCell(), srcIter.nextCell(), typeSize);
                }
                else
                {
                    srcIter.nextCell();
                    resIter.nextCell();
                }
            }

            // pop!
            indices.pop();
        }
    }
    //add the result tile to the resultMDD
    resultMDD->insertTile(resTile);
    //return the resultMDD
    return resultMDD.release();
}

QtData *
QtClipping::computeOp(QtMDD *operand, QtGeometryData *geomData)
{
    // get the source MDD object
    MDDObj *op = operand->getMDDObject();

    //  get the area, where the operation has to be applied
    r_Minterval areaOp = operand->getLoadDomain();
    const r_Dimension opDim = areaOp.dimension();

    // the geometry data specific to the primary domain of interest. Initialized in the switch/case
    QtMShapeData *mshape = NULL;

    // the result MDD object
    std::unique_ptr<MDDObj> resultMDD;

    // the geometry type
    QtGeometryData::QtGeometryType geomType = geomData->getGeometryType();

    //next, we reference an mshape for error reporting, based on the clip type being applied
    switch (geomType)
    {
    case QtGeometryData::QtGeometryType::GEOM_SUBSPACE :
    {
        mshape = geomData->getProjections();
        break;
    }
    case QtGeometryData::QtGeometryType::GEOM_LINESTRING :
    {
        mshape = geomData->getLinestrings()[0];
        break;
    }
    case QtGeometryData::QtGeometryType::GEOM_MULTILINESTRING :
    {
        mshape = geomData->getLinestrings()[0];
        break;
    }
    default : //the majority of useful data is stored in the first entry of the polygon array
    {
        mshape = geomData->getPolygons()[0][0];
        break;
    }
    }

    computeOpErrorChecking(opDim, areaOp, mshape, geomType);

    switch (geomType)
    {
    case QtGeometryData::QtGeometryType::GEOM_SUBSPACE :
    {
        QtMShapeData *projectionData = geomData->getProjections();

        if (opDim != 1 && projectionData->getDimension() == 1) //are we really just interested in a line?
        {
            resultMDD.reset(extractBresenhamLine(op, areaOp, projectionData, opDim));
        }
        else //okay, then we want a subspace!
        {
            resultMDD.reset(extractSubspace(op, areaOp, projectionData));
        }
        break;
    }

    case QtGeometryData::QtGeometryType::GEOM_POLYGON :
    {
        vector< vector< QtMShapeData * >> polygonData = geomData->getPolygons();
        vector< QtPositiveGenusClipping > clipVector = buildMultipoly(polygonData, geomType);
        resultMDD.reset(extractMultipolygon(op, areaOp, clipVector, geomType));

        //in this case, the clipping is well-defined, but may result in the empty set
        if (!resultMDD)
        {
            throw r_Error(ALLPOLYGONSOUTSIDEMDDOBJ);
        }

        break;
    }
    case QtGeometryData::QtGeometryType::GEOM_LINESTRING :
    {
        QtMShapeData *linestringData = geomData->getLinestrings()[0];
        resultMDD.reset(extractLinestring(op, linestringData, opDim));
        break;
    }
    case QtGeometryData::QtGeometryType::GEOM_MULTIPOLYGON :
    {

        vector< vector< QtMShapeData * >> polygonData = geomData->getPolygons();
        vector< QtPositiveGenusClipping > clipVector = buildMultipoly(polygonData, geomType);

        resultMDD.reset(extractMultipolygon(op, areaOp, clipVector, geomType));

        //in this case, the clipping is well-defined, but may result in the empty set
        if (!resultMDD)
        {
            throw r_Error(ALLPOLYGONSOUTSIDEMDDOBJ);
        }

        break;
    }
    //case QtGeometryData::QtGeometryType::GEOM_MULTILINESTRING :
    //not supported, resultMDD remains null.

    case QtGeometryData::QtGeometryType::GEOM_CURTAIN_POLYGON :
    {
        vector< vector< QtMShapeData * >> polygonData = geomData->getPolygons();
        vector< QtPositiveGenusClipping > clipVector = buildMultipoly(polygonData, geomType);

        auto mask = buildAbstractMask(clipVector, geomType);

        vector< r_Dimension > maskDims = geomData->getProjections()->computeFirstProjection();

        checkProjDims(opDim, maskDims);
        checkMaskDim(mask.second->dimension(), maskDims);

        resultMDD.reset(extractCurtain(op, areaOp, maskDims, mask));

        break;
    }
    //case QtGeometryData::QtGeometryType::GEOM_CURTAIN_LINESTRING :
    //not supported, resultMDD remains null.
    case QtGeometryData::QtGeometryType::GEOM_CURTAIN_LINESTRING_EMBEDDED :
    {
        vector< vector< QtMShapeData * >> polygonData = geomData->getPolygons();
        vector< QtPositiveGenusClipping > clipVector = buildMultipoly(polygonData, geomType);

        auto mask = buildAbstractMask(clipVector, geomType);

        vector< r_Dimension > maskDims = geomData->getProjections()->computeFirstProjection();

        checkProjDims(opDim, maskDims);
        checkMaskDim(mask.second->dimension(), maskDims);

        resultMDD.reset(extractCurtain(op, areaOp, maskDims, mask));

        break;
    }
    case QtGeometryData::QtGeometryType::GEOM_CURTAIN_MULTIPOLYGON :
    {
        vector< vector< QtMShapeData * >> polygonData = geomData->getPolygons();
        vector< QtPositiveGenusClipping > clipVector = buildMultipoly(polygonData, geomType);

        auto mask = buildAbstractMask(clipVector, geomType);

        vector< r_Dimension > maskDims = geomData->getProjections()->computeFirstProjection();

        checkProjDims(opDim, maskDims);
        checkMaskDim(mask.second->dimension(), maskDims);

        resultMDD.reset(extractCurtain(op, areaOp, maskDims, mask));

        break;
    }
    //case QtGeometryData::QtGeometryType::GEOM_CURTAIN_MULTILINESTRING :
    //not supported, resultMDD remains null.
    //case QtGeometryData::QtGeometryType::GEOM_CURTAIN_MULTILINESTRING_EMBEDDED :
    //not supported, resultMDD remains null.

    case QtGeometryData::QtGeometryType::GEOM_CORRIDOR_POLYGON :
    {
        vector< vector< QtMShapeData * >> polygonData = geomData->getPolygons();
        vector< QtPositiveGenusClipping > clipVector = buildMultipoly(polygonData, geomType);

        auto mask = buildAbstractMask(clipVector, geomType);

        vector< r_Dimension > maskDims = geomData->getProjections()->computeFirstProjection();

        QtMShapeData *lineStringData = geomData->getLinestrings()[0];

        checkProjDims(opDim, maskDims);
        checkMaskDim(mask.second->dimension(), maskDims);

        resultMDD.reset(extractCorridor(op, areaOp, lineStringData, maskDims, mask, geomData->getGeomFlag()));

        break;
    }
    //case QtGeometryData::QtGeometryType::GEOM_CORRIDOR_LINESTRING :
    //not supported, resultMDD remains null.

    case QtGeometryData::QtGeometryType::GEOM_CORRIDOR_LINESTRING_EMBEDDED :
    {
        vector< vector< QtMShapeData * >> polygonData = geomData->getPolygons();
        vector< QtPositiveGenusClipping > clipVector = buildMultipoly(polygonData, geomType);

        auto mask = buildAbstractMask(clipVector, geomType);

        vector< r_Dimension > maskDims = geomData->getProjections()->computeFirstProjection();

        QtMShapeData *lineStringData = geomData->getLinestrings()[0];

        checkProjDims(opDim, maskDims);
        checkMaskDim(mask.second->dimension(), maskDims);

        resultMDD.reset(extractCorridor(op, areaOp, lineStringData, maskDims, mask, geomData->getGeomFlag()));

        break;
    }
    case QtGeometryData::QtGeometryType::GEOM_CORRIDOR_MULTIPOLYGON :
    {
        vector< vector< QtMShapeData * >> polygonData = geomData->getPolygons();
        vector< QtPositiveGenusClipping > clipVector = buildMultipoly(polygonData, geomType);

        auto mask = buildAbstractMask(clipVector, geomType);

        vector< r_Dimension > maskDims = geomData->getProjections()->computeFirstProjection();

        QtMShapeData *lineStringData = geomData->getLinestrings()[0];

        checkProjDims(opDim, maskDims);
        checkMaskDim(mask.second->dimension(), maskDims);

        resultMDD.reset(extractCorridor(op, areaOp, lineStringData, maskDims, mask, geomData->getGeomFlag()));

        break;
    }
    //case QtGeometryData::QtGeometryType::GEOM_CORRIDOR_MULTILINESTRING :
    //not supported, resultMDD remains null.
    //case QtGeometryData::QtGeometryType::GEOM_CORRIDOR_MULTILINESTRING_EMBEDDED :
    //not supported, resultMDD remains null.

    default :
        break;
    }

    //in this case, the clipping is not well-defined (but we need to protect against seg faults, so we throw a generic error).
    if (!resultMDD)
    {
        parseInfo.setErrorNo(CLIPERRORUNDEFINED);
        throw parseInfo;
    }

    // create a new QtMDD object as carrier object for the transient MDD object
    QtData *returnValue = new QtMDD(resultMDD.release());

    return returnValue;
}

void
QtClipping::computeOpErrorChecking(r_Dimension opDim,
                                   const r_Minterval &areaOp,
                                   QtMShapeData *shapeOp,
                                   QtGeometryData::QtGeometryType geomType)
{

    //mshape may define a subspace, linestring, or a polygon.
    //dimension of the smallest affine subspace containing the mshape vertices
    const r_Dimension mshapeDim = shapeOp->getDimension();

    //the dimensionality of the individual points
    //this is invariant for each mshape, and error checking for invariance is handled by QtMShapeData
    const r_Dimension mshapePtDim = shapeOp->getPointDimension();

    //number of points defining the mshape
    const size_t mshapePtCard = shapeOp->getPolytopePoints().size();

    // possible errors which apply to more than one clipType

    //need at least two points to get started here...
    if (mshapePtCard <= 1)
    {
        LERROR << "Error: All geometric clip types require at least two vertices to be well-defined.";
        parseInfo.setErrorNo(NEEDTWOORMOREVERTICES);
        throw parseInfo;
    }
    //point dimensionality must match that of the operand (< case may be acceptable for curtains)
    else if ((opDim != mshapePtDim && geomType < QtGeometryData::QtGeometryType::GEOM_CURTAIN_POLYGON)
             || opDim < mshapePtDim)
    {
        LERROR << "Error: No natural embedding from the space containing the mdd object's grid to the space containing the clip vertices exists.";

        if (geomType == QtGeometryData::QtGeometryType::GEOM_POLYGON
                || geomType == QtGeometryData::QtGeometryType::GEOM_MULTIPOLYGON)
        {
            parseInfo.setErrorNo(POINTDIMENSIONDIFFERS);
            throw parseInfo;
        }
        else
        {
            parseInfo.setErrorNo(NONATURALEMBEDDING);
            throw parseInfo;
        }
    }
    //linestrings can end up defining the entire space, as can polygons in 2D.
    else if (opDim <= mshapeDim
             && geomType < QtGeometryData::QtGeometryType::GEOM_POLYGON
             && geomType > QtGeometryData::QtGeometryType::GEOM_MULTILINESTRING)
    {
        LERROR << "Error: The smallest affine subspace containing the clip vertices must not be equal to the entire space containing the mdd object's grid.";
        parseInfo.setErrorNo(SUBSPACEDIMSAMEASMDDOBJ);
        throw parseInfo;
    }
}

void
QtClipping::checkProjDims(r_Dimension opDim, const vector<r_Dimension> &maskDims)
{
    for (auto iter = maskDims.begin(); iter != maskDims.end(); iter++)
    {
        // is the dimension value outside the expected range?
        if (*iter >= opDim)
        {
            // throw an error
            LERROR << "Error: The coordinate projections must correspond to existing axes.";
            parseInfo.setErrorNo(AXISNUMBERSMUSTEXIST);
            throw parseInfo;
        }
    }
}

void
QtClipping::checkMaskDim(r_Dimension maskDim,
                         const vector<r_Dimension> &maskDims)
{

    // check the mask dimension is the same as the size of projection result.
    if (maskDim != maskDims.size())
    {
        // throw an error
        LERROR << "Error: The number of coordinates projected to must match the dimension of the mask.";
        parseInfo.setErrorNo(PROJDIMNOTMATCHINGMASKDIM);
        throw parseInfo;
    }

}

QtData *
QtClipping::evaluate(QtDataList *inputlist)
{
    QtData *returnValue = NULL;
    QtData *operand1 = NULL;
    QtData *operand2 = NULL;

    // evaluate sub-nodes to obtain operand values
    if (getOperands(inputlist, operand1, operand2))
    {
        //valid due to checkType()
        QtMDD *qtMDDObj = static_cast< QtMDD * >(operand1);
        QtGeometryData *geomData = static_cast< QtGeometryData * >(operand2);

        //where the errors are thrown
        returnValue = computeOp(qtMDDObj, geomData);

        //delete the old operands
        if (operand1)
        {
            operand1->deleteRef();
        }
        if (operand2)
        {
            operand2->deleteRef();
        }
    }

    return returnValue;
}

const QtTypeElement &
QtClipping::checkType(QtTypeTuple *typeTuple)
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
            LERROR << "Error: First operand of the clipping operation must be of type MDD.";
            parseInfo.setErrorNo(MDDARGREQUIRED);
            throw parseInfo;
        }

        // operand two can be a single long number, the parser does [a] -> number a,
        // rather than [a] -> point (which is then used in marray/condense..),
        // so we need to take care manually here of this edge case -- DM 2015-aug-24
        if (inputType2.getDataType() != QT_GEOMETRY)
        {
            LERROR << "Error: Second operand must be of type QT_GEOMETRY.";
            parseInfo.setErrorNo(GEOMETRYARGREQUIRED);
            throw parseInfo;
        }

        if (!withCoordinates)
        {
            dataStreamType = inputType1;
        }
        else
        {
            r_Dimension dim{};
            const BaseType *baseType{nullptr};
            const auto *inputType = static_cast<const MDDType *>(inputType1.getType());
            switch (inputType->getSubtype())
            {
            case MDDType::MDDDOMAINTYPE:
            {
                dim = static_cast<const MDDDomainType *>(inputType)->getDomain()->dimension();
                baseType = static_cast<const MDDDomainType *>(inputType)->getBaseType();
                break;
            }
            case MDDType::MDDDIMENSIONTYPE:
            {
                dim = static_cast<const MDDDimensionType *>(inputType)->getDimension();
                baseType = static_cast<const MDDDimensionType *>(inputType)->getBaseType();
                break;
            }
            case MDDType::MDDBASETYPE:
            {
                LWARNING << "Cannot determine dimension from MDD base type.";
                dim = 2;
                baseType = static_cast<const MDDBaseType *>(inputType)->getBaseType();
                break;
            }
            default:
            {
                LWARNING << "Cannot determine dimension and base type from generic MDD type.";
                parseInfo.setErrorNo(MDDARGREQUIRED);
                throw parseInfo;
            }
            }
            auto *newBaseType = getTypeWithCoordinates(baseType, dim);
            auto *newMddType = new MDDDimensionType("tmp", newBaseType, dim);
            TypeFactory::addTempType(newMddType);
            dataStreamType = QtTypeElement{newMddType};
        }
    }
    else
    {
        LERROR << "Error: QtClipping::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}

vector<QtPositiveGenusClipping>
QtClipping::buildMultipoly(const vector< vector< QtMShapeData * >> &polygonData,
                           QtGeometryData::QtGeometryType geomType)
{
    vector< QtPositiveGenusClipping> clipVector;
    clipVector.reserve(polygonData.size());
    for (auto shapeIter = polygonData.begin(); shapeIter != polygonData.end(); shapeIter++)
    {
        std::vector<QtMShapeData *> thisPosGenPolygon;
        std::shared_ptr<r_Minterval> convexHull;
        thisPosGenPolygon.reserve(shapeIter->size());
        for (auto iter = shapeIter->begin(); iter != shapeIter->end(); iter++)
        {
            if (2 != (*iter)->getDimension() && 2 != (*iter)->getPointDimension())
            {
                LERROR << "Error: Dimension of the polygon vertices differs from the domain's dimension.";
                throw r_Error(POINTDIMENSIONDIFFERS);
            }
            else
            {
                if (!convexHull)
                {
                    convexHull.reset(new r_Minterval((*iter)->convexHull()));
                }


                //add the next polygon w/ interiors to the vector
                thisPosGenPolygon.emplace_back(*iter);
            }
        }
        clipVector.emplace_back(QtPositiveGenusClipping(*convexHull, thisPosGenPolygon));
    }

    return clipVector;
}

std::shared_ptr<r_Minterval>
QtClipping::buildResultDom(const r_Minterval &areaOp,
                           vector<QtPositiveGenusClipping> &mshapeList)
{
    //constructing the result domain for the mask and the resultmdd
    std::shared_ptr<r_Minterval> resultDom;
    //since we use this method both internally and externally, we don't always want to compute the result domain (it could be passed as areaOp)
    for (auto i = mshapeList.begin(); i != mshapeList.end(); i++)
    {
        if (i->getDomain().intersects_with(areaOp))
        {
            if (resultDom)
            {
                *resultDom = resultDom->closure_with(i->getDomain());
            }
            else
            {
                resultDom.reset(new r_Minterval(i->getDomain()));
            }
        }
    }

    return resultDom;
}

std::unique_ptr<char[]>
QtClipping::buildResultMask(
    std::shared_ptr<r_Minterval> resultDom,
    vector<QtPositiveGenusClipping> &mshapeList,
    QtGeometryData::QtGeometryType geomType)
{
    //result mask
    std::unique_ptr<char[]> resultMask;
    resultMask.reset(new char[resultDom->cell_count()]);
    memset(resultMask.get(), 2, resultDom->cell_count());

    //starting point of the mask, for iteration
    const char *resultMaskPtr = &resultMask.get()[0];

    for (auto iter = mshapeList.begin(); iter != mshapeList.end(); iter++)
    {
        if (iter->getDomain().intersects_with(*resultDom))
        {
            vector< vector<char>> polygonMask;

            if (geomType == QtGeometryData::QtGeometryType::GEOM_POLYGON
                    || geomType == QtGeometryData::QtGeometryType::GEOM_MULTIPOLYGON
                    || geomType == QtGeometryData::QtGeometryType::GEOM_CURTAIN_POLYGON
                    || geomType == QtGeometryData::QtGeometryType::GEOM_CURTAIN_MULTIPOLYGON
                    || geomType == QtGeometryData::QtGeometryType::GEOM_CORRIDOR_POLYGON
                    || geomType == QtGeometryData::QtGeometryType::GEOM_CORRIDOR_MULTIPOLYGON)
            {
                polygonMask = iter->generateMask(true);
            }
            else
            {
                polygonMask = iter->generateMask(false);
            }

            auto iterDomain = iter->getDomain();
            r_Miter resultMaskIter(&iterDomain, resultDom.get(), sizeof(char), resultMaskPtr);

            //this is the approach we take when combining positive genus polygonal masks
            for (size_t m = 0; m < polygonMask.size(); m++)
            {
                for (size_t n = 0; n < polygonMask[m].size(); n++)
                {
                    if (polygonMask[m][n] < 2) //copy the new mask's polygon into the overall mask
                    {
                        *(resultMaskIter.nextCell()) = polygonMask[m][n];
                    }
                    else
                    {
                        resultMaskIter.nextCell();
                    }
                }
            }
        }
    }

    return resultMask;
}

std::pair< std::unique_ptr<char[]>, std::shared_ptr<r_Minterval>>
        QtClipping::buildAbstractMask(vector<QtPositiveGenusClipping> &mshapeList,
                                      QtGeometryData::QtGeometryType geomType)
{

    //builds the result domain without worrying about an area of interest
    std::shared_ptr<r_Minterval> resultDom;
    for (auto i = mshapeList.begin(); i != mshapeList.end(); i++)
    {
        if (resultDom)
        {
            *resultDom = resultDom->closure_with(i->getDomain());
        }
        else
        {
            resultDom.reset(new r_Minterval(i->getDomain()));
        }
    }

    //result mask & domain

    std::pair< std::unique_ptr<char[]>, std::shared_ptr<r_Minterval>> retVal(buildResultMask(resultDom, mshapeList, geomType), resultDom);

    return retVal;
}




std::vector<r_Minterval>
QtClipping::computeMaskEmbedding(
    const std::vector< std::vector<r_Point>> &pointListArg,
    const r_Minterval &convexHullArg,
    r_Range outputLength,
    std::vector<r_Dimension> maskDims)
{
    std::vector<r_Minterval> result;
    result.reserve(static_cast<size_t>(outputLength));

    r_Point firstPoint = pointListArg[0][0];

    if (!convexHullArg.covers(pointListArg[0][0].indexedMap(maskDims)))
    {
        LERROR << "The coordinates of the starting point of the linestring do not sit inside the polygon's convex hull.";
        throw r_Error(MASKNOTALIGNEDWITHLINESTRING);
    }
    else
    {
        bool firstSeg = true;
        // iterate over the segments
        for (auto segIter = pointListArg.begin(); segIter != pointListArg.end(); ++segIter)
        {
            auto ptIter = segIter->begin();

            if (!firstSeg)
            {
                ptIter++;
            }
            else
            {
                firstSeg = false;
            }

            if (segIter->size() != 1 || firstSeg)
            {
                // iterate over the points in each segment
                for (; ptIter != segIter->end(); ++ptIter)
                {
                    //find the current slice by translating the convex hull
                    r_Minterval currentSlice(firstPoint.dimension());
                    r_Point translation = *ptIter - firstPoint;
                    for (r_Dimension i = 0; i < firstPoint.dimension(); i++)
                    {
                        //translate the convex hull argument along the dimensions it corresponds to (guaranteed to contain the translated point, since convexHullArg contains the first point)
                        auto it = std::find(maskDims.begin(), maskDims.end(), i);
                        auto index = std::distance(maskDims.begin(), it);
                        if (it == maskDims.end())
                        {
                            // the current point location
                            currentSlice << r_Sinterval((*ptIter)[i], (*ptIter)[i]);
                        }
                        else
                        {
                            // the current translated bounding box
                            currentSlice << r_Sinterval(convexHullArg[index].low() + translation[i], convexHullArg[index].high() + translation[i]);
                        }
                    }
                    result.emplace_back(currentSlice);
                }
            }
        }
    }

    return result;
}

void QtClipping::setWithCoordinates(bool withCoordinatesArg)
{
    withCoordinates = withCoordinatesArg;
}

void QtClipping::printTree(int tab, ostream &s, QtNode::QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtClipping Object:" << endl;
    QtBinaryOperation::printTree(tab, s, mode);
}
