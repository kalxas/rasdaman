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

#include "qlparser/qtclippingfunc.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtnode.hh"
#include "qlparser/qtmshapedata.hh"

#include "mddmgr/mddobj.hh"
#include "tilemgr/tile.hh"

#include "catalogmgr/typefactory.hh"
#include "relcatalogif/structtype.hh"
#include "relcatalogif/mdddimensiontype.hh"
#include "relcatalogif/syntaxtypes.hh"
#include "qlparser/qtmshapeop.hh"
#include "qlparser/qtfindsection.hh"

#include <logging.hh>

#include "qlparser/qtpointdata.hh"

#include <sstream>
#ifndef CPPSTDLIB
#else
#include <string>
#include <cmath>
#endif

#include <iostream>
#include "rasodmg/polycutout.hh"

struct mapCmpMinterval
{
    bool operator()(const r_Minterval &a, const r_Minterval &b) const
    {
        bool firstIsBigger = true;
        for (r_Dimension d = 0; d < a.dimension(); d++)
        {
            if ((a[d].high() - a[d].low()) < (b[d].high() - b[d].low()))
                return true;
            else if ((a[d].high() - a[d].low()) > (b[d].high() - b[d].low()))
                return false;
            else
                continue;
        }
        return false;
    }
};

const QtNode::QtNodeType QtClipping::nodeType = QtNode::QT_CLIPPING;

QtClipping::QtClipping(QtOperation* mddOp, QtOperation* mshapePointList, QtClipType pt)
    : QtBinaryOperation(mddOp, mshapePointList), clipType(pt), range(NULL)
{
}

QtClipping::QtClipping(QtOperation* mddOp, QtOperation* mshapePointList, QtMShapeData* mshapeRange, QtClipType pt)
    : QtBinaryOperation(mddOp, mshapePointList), clipType(pt), range(mshapeRange)
{
    // for now, we assume the range is 1D in the first coordinate and mshapePointList is 2d in the last 2 coordinates. Will be expanded upon later.
}

QtClipping::~QtClipping()
{
    delete range;
}

bool QtClipping::isCommutative() const
{
    return false; // NOT commutative
}

MDDObj*
QtClipping::extractBresenhamLine(const MDDObj* op, r_Minterval areaOp, QtMShapeData* mshape, const r_Dimension dim)
{
    // computing the bounding box of the multidimensional shape defined in QtMShapeData
    std::unique_ptr<BoundingBox> bBox;
    bBox.reset( computeBoundingBox(mshape) );

    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), dim);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    // the dataset dimension is the same as the dimension of the points defining the mshape, so
    // to extract the dataset dimension we use the first point.
    r_Dimension datasetDimension = (mshape->getMShapeData())[0].dimension();

    // directionVectors contains a set of n orthogonal vectors where n is the dimension of the dataset. The first
    // m vectors, where m is the dimension of the mshape define the space in which the m-dimensional shape lies into
    // The remaining vectors are vectors orthogonal to the mshape.
    std::vector<r_PointDouble>* directionVectors = mshape->getDirectionVectors();

    // Construct r_Minterval from the bounding box of the multidimensional shape
    r_Minterval mintervalBoundingBox = bBox->getHull();

    // check in case there are no points in common between the smallest r_Minterval containing the mshape and the dataset itself.
    // In that case there is no need for further computations and an exception is thrown
    if (!areaOp.intersects_with(mintervalBoundingBox))
    {
        LFATAL << "The subspace of the set of points provided does not pass through the stored data.";
        parseInfo.setErrorNo(SUBSPACENOINTERSECTION);
        throw parseInfo;
    }

    areaOp = areaOp.create_intersection(mintervalBoundingBox);

    // vector containing r_Points that define the line we want to extract.
    vector<r_Point> bresenhamLine = computeNDBresenhamLine(mshape);
    
    // domain of the relevant area of the actual dbobj corresponds to the bounding box of the start and end points.
    r_Minterval domainOfInterest = localHull(std::make_pair<int, int>(-1, -1), bresenhamLine);
    
    // resultDom's r_Sinterval corresponds to the longest extent of domainOfInterestGlobal
    std::vector<r_Range> bbExtents = (domainOfInterest.get_extent()).getVector();
    r_Range maxExtent = *(std::max_element(bbExtents.begin(), bbExtents.end()));
    r_Dimension index = std::distance(bbExtents.begin(), std::max_element(bbExtents.begin(), bbExtents.end()));
    
    // startEndIndicesGlobal keeps track of the segment of the line contained inside areaOp.
    pair<int, int> startEndIndicesGlobal = endpointsSearch(areaOp, bresenhamLine);
    
    // construct the result domain using the largest direction's index.
    // the extent here corresponds to the total number of result points
    r_Minterval resultDomainGlobal(1);
    resultDomainGlobal[0] = localHullByIndex(startEndIndicesGlobal, bresenhamLine, index);
    
    //result tile
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset( new MDDObj(mddBaseType, resultDomainGlobal) );

    // get all tiles in relevant area
    std::unique_ptr<std::vector<boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->getTiles());
    
    // iterate over the tiles
    try
    {
        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++)
        {
            // domain of the actual tile
            const r_Minterval& tileDom = (*tileIt)->getDomain();
            if (tileDom.intersects_with(areaOp))
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
                resTile.reset( new Tile(resultDomain, (*tileIt).get()->getType()) );
                char* resultData = resTile->getContents();

                // compute position in the source char* array for assigning the result tile content
                char* sourceData = (*tileIt)->getCell( bresenhamLine[static_cast<size_t>(startEndIndices.first)] );
                // we could insert this at the front of bresenhamLine, but this way is faster as it avoids an O(length of line) computation
                memcpy(resultData, sourceData, typeSize);
                
                // take care to avoid double-copying the point in case there is only a single point in this tile
                if(startEndIndices.second == startEndIndices.first)
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
                    sourceData = (*tileIt)->getCell(bresenhamLine[i+1]);
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
        LFATAL << "QtClipping::compute2D caught error: " << err.get_errorno() << " " << err.what();
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }
    
    catch (int err)
    {
        LFATAL << "QtClipping::compute2D caught error: " << err << " in qtclipping";
        parseInfo.setErrorNo(err);
        throw parseInfo;
    }
    
    return resultMDD.release();
}

MDDObj*
QtClipping::extractSubspace(const MDDObj* op, const r_Minterval& areaOp, QtMShapeData* mshape)
{
    // dimension of the source space.
    r_Dimension datasetDimension = (mshape->getMShapeData())[0].dimension();

    // compute the bounding box of the mShape
    std::unique_ptr<BoundingBox> bBox;
    bBox.reset( computeBoundingBox(mshape) );
    
    
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
        LFATAL << "The subspace of the set of points provided does not pass through the stored data.";
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
    for(size_t i = 0; i < tranBox.dimension(); i++)
    {
        if( projectionDimensionSet.find(i) == projectionDimensionSet.end() )
        {
            keptDimensions.emplace_back(i);
        }
    }  
    
    //projected domain -- domain of the result object
    const r_Minterval projectedDomain = computeProjectedDomain(tranBox, projectionDimensionSet, mshape->getDimension());
    
    MDDBaseType* mddBaseType = const_cast<MDDBaseType*>(op->getMDDBaseType());
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset( new MDDObj(mddBaseType, projectedDomain) );
        
    // get all tiles in relevant area
    std::unique_ptr<std::vector<boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset( op->intersect(tranBox) );
    boost::shared_ptr<Tile> resTile;
    resTile.reset( new Tile(projectedDomain, (*(allTiles->begin()))->getType()) );
    
    //data type size
    size_t typeSize = (*(allTiles->begin()))->getType()->getSize();
    
    
    // initialize the data in the resTile to 0
    char* resInitializer = resTile->getContents();
    op->fillTileWithNullvalues(resInitializer, projectedDomain.cell_count());
    
    //object for computing the point coordinates of the preimage of the projection in the subspace.
    FindSection resFinder(mshape->computeHyperplaneEquation(), keptDimensions);
    //prepare additional variables for later computation.
    resFinder.prepareSection();
    try
    {
        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++)
        {

            //domain of source tile
            const r_Minterval& tileDom = (*tileIt)->getDomain();

            //fill result tile contents...    
                
            //dimensions of the area of interest intersected with the current source tile
            r_Minterval intersectDom(tileDom.create_intersection(tranBox));     
            r_Point sourceExtents = intersectDom.get_extent();
            // dimensions of the result tile's area of interest for this pass
            r_Minterval resIntDom = computeProjectedDomain(intersectDom, projectionDimensionSet, mshape->getDimension());
            
            //solving the linear systems and placing the data in the cell if the sol'n lies in the AOI;
                
            // source data pointer
            char* sourceData = (*tileIt)->getContents();
            // result data pointer
            char* resultData = resTile->getContents();
            // starting point of iteration
            r_Point currentPoint = resIntDom.get_origin();
               
            for(r_Area localOffset = 0; localOffset < resIntDom.cell_count(); localOffset++)
            {
                //the point in the result domain
                if(localOffset > 0)
                {
                    currentPoint = resIntDom.cell_point(localOffset);
                }
                
                //method for finding the integer point in the tile from the projected domain's coordinates
                    //utilizes an LU solver
                    //set pointInTile to the nearest integer lattice point resulting from the LU solver
                auto pointInTile = resFinder.solveLU(currentPoint).toIntPoint();
                 
                //it could be that the result is located in another tile
                if( tileDom.covers(pointInTile) )
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
        LFATAL << "QtClipping::extractSubspace caught " << err.get_errorno() << " " << err.what();
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }
    
    catch (int err)
    {
        LFATAL << "QtClipping::extractSubspace caught errno error (" << err << ") in qtclipping";
        parseInfo.setErrorNo(err);
        throw parseInfo;
    }
    
    return resultMDD.release();
}

//functor for passing to the remove_if iterator in std::vector during extractLinestring and extractCurtainLinestring
bool isSingleton(const r_Minterval& interval )
{
    return interval.cell_count() == 1;
}

MDDObj* 
QtClipping::extractLinestring(const MDDObj* op, const QtMShapeData* mshape, const r_Dimension dim)
{
    // create vector of bounding boxes (one for each line segment in the linestring)
    vector<r_Minterval> bBoxes = mshape->localConvexHulls();
    
    //only consider the segments which contribute new points to the result vector
    //as above, the start and end points must differ.
    bBoxes.erase(remove_if(bBoxes.begin(), bBoxes.end(), isSingleton), bBoxes.end());    
    
    //for each one, we construct a vector (actually a pair) of r_Points representing the endpoints of the line segment being considered
    
    vector< vector< r_PointDouble > > vectorOfSegmentEndpointPairs = vectorOfPairsWithoutMultiplicity( mshape->getMShapeData(), bBoxes.size() );
    
    // create vector of bresenham lines (one for each line segment passing through the domain of the MDDObject)
    vector< vector < r_Point > > vectorOfBresenhamLines;
    vectorOfBresenhamLines.reserve(vectorOfSegmentEndpointPairs.size());
    for(size_t i = 0; i < vectorOfSegmentEndpointPairs.size(); i++)
    {
        vectorOfBresenhamLines.emplace_back( computeNDBresenhamSegment(vectorOfSegmentEndpointPairs[i]) );
    }
    
    // create vector of intervals for the result tiles
    //[0 : k_0-1], [k_0 : k_0 + k_1 - 1], [k_0 + k_1 : k_0 + k_1 + k_2 - 1], ...
    //for each segment, we need to find the intersection of its bounding box's longest extent's dimension with the domain of the MDDObject being considered
    
    //vector of dimension #'s corresponding to the longest extents in bBoxes
    vector<r_Dimension> longestExtentDims;
    longestExtentDims.reserve(bBoxes.size());
    vector< vector<r_Range> > bBoxesExtents;
    bBoxesExtents.reserve(bBoxes.size());
    for(size_t i = 0; i < bBoxes.size(); i ++)
    {
        bBoxesExtents.emplace_back(bBoxes[i].get_extent().getVector());
        longestExtentDims.emplace_back( std::distance(bBoxesExtents[i].begin(), 
                                                      std::max_element(bBoxesExtents[i].begin(),
                                                                       bBoxesExtents[i].end())) );
    }
    
    //construct the resulting tile intervals
    vector<r_Minterval> resultTileMintervals = vectorOfResultTileDomains(bBoxes, longestExtentDims);
    
    //construct the result MMDDObj
    
    r_Minterval resultDomainGlobal(1);
    r_Sinterval totalDomain(static_cast<r_Range>(0), resultTileMintervals.back()[0].high());
    resultDomainGlobal[0] = totalDomain;

    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), dim);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);    

    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset(new MDDObj(mddBaseType, resultDomainGlobal, op->getNullValues()));
    
    //   loop over source tiles
    //  loop over bresenhamLines vector
    //first check the intersection of the source tile with the bounding box, then construct the startEndIndices pair for that line segment
    //process Bresenham into the respective output tile
    
    // get all tiles in relevant area
    std::unique_ptr<std::vector<boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->getTiles());
    
    //initialize the result tiles here
    boost::shared_ptr<Tile> tempTile;
    tempTile = *(allTiles->begin());
    size_t typeSize = tempTile->getType()->getSize();
    vector< boost::shared_ptr<Tile> > resultTiles;
//    vector< boost::shared_ptr<Tile> > resultTiles = initializeTileVector(resultTileMintervals, tempTile->getType());
    resultTiles.reserve(resultTileMintervals.size());
    for(size_t i = 0; i < resultTileMintervals.size(); i++)
    {
        //build a new tile        
        boost::shared_ptr<Tile> resTilePtr;
        resTilePtr.reset(new Tile(resultTileMintervals[i], tempTile->getType()));
        //Tile* resTilePtr = new Tile(resultTileMintervals[i], tempTile->getType());
        //initialize contents to 0
        char* resData = resTilePtr->getContents();
        op->fillTileWithNullvalues(resData, resultTileMintervals[i].cell_count());
        //add tile to vector of result tiles
        resultTiles.emplace_back(resTilePtr);
    }

    try
    {
        //loop over source tiles
        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++)
        {
            // domain of the current source tile
            r_Minterval tileDom = (*tileIt)->getDomain();

            //loop over bresenham line segments (we do not use an iterator because we need the index for various vectors of the same length)
            for(size_t i = 0; i < bBoxes.size(); i++)
            {

                //current result tile's data
                char* resData = ( resultTiles[i] )->getContents();
                
                bool firstElementSkipped = false;
                //iterate over the points in the current bresenham line segment
                for(auto ptIter = vectorOfBresenhamLines[i].begin(); ptIter != vectorOfBresenhamLines[i].end(); ptIter++)
                {
                    if(!firstElementSkipped && i != 0 )
                    {
                        //skip the first point for all but the first interval
                        ptIter++;
                        firstElementSkipped = true;
                    }
                    //if the current point is in the domain, we copy that point to the next point in resData
                    if ( tileDom.covers(*ptIter) )
                    {
                        //ptr to source data cell
                        char* srcData = (*tileIt)->getCell(*ptIter);
                        memcpy(resData, srcData, typeSize);
                        resData += typeSize;
                    }
                    else
                    {
                        resData += typeSize;
                    }
                }
            }
        }
        //add result tiles to result MDDObj
        for(auto resTileIt = resultTiles.begin(); resTileIt != resultTiles.end(); resTileIt++)
        {
            resultMDD->insertTile(*resTileIt);
        }
    }
    catch (r_Error &err)
    {
        LFATAL << "QtClipping::compute2D caught error: " << err.get_errorno() << " " << err.what();
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }
    catch (int err)
    {
        LFATAL << "QtClipping::compute2D caught error: " << err << " in qtclipping";
        parseInfo.setErrorNo(err);
        throw parseInfo;
    }

    return resultMDD.release();    
}

MDDObj* 
QtClipping::extractCurtainPolygon(const MDDObj* op, const r_Minterval& areaOp, QtMShapeData* polytope, const QtMShapeData* rangeArg)
{
    //currently takes the two points in the range to determine the extent in the first coordinate and uses the polygon for the last two coordinates to determine the cutout shape (two cases -- line & polygon)
    r_Sinterval rangeInterval;
    rangeInterval.set_low(rangeArg->getPolytopePoints()[0][0]);
    rangeInterval.set_high(rangeArg->getPolytopePoints()[1][0]);

    //project the minterval onto the last two coordinates.
    r_Minterval lastTwoDimsDomain(2);
    lastTwoDimsDomain << areaOp[1];
    lastTwoDimsDomain << areaOp[2];

    //build the result domain from the rangeInterval and the bounding box of the polygon vertices.
    pair<r_Point, r_Point> bBox = getBoundingBox( polytope->getPolytopePoints() );
    
    r_Sinterval xAxis(bBox.first[0], bBox.second[0]);
    r_Sinterval yAxis(bBox.first[1], bBox.second[1]);

    r_Minterval convexHull(2);
    convexHull << xAxis;
    convexHull << yAxis;

    if( !lastTwoDimsDomain.intersects_with(convexHull) || !areaOp[0].intersects_with(rangeInterval))
    {
        LFATAL << "QtClipping::extractCurtain - the domain of the curtain query does not intersect with the domain of the MDDObject";
        throw r_Error(CURTAINDOMAININTERSECTERROR);
    }

    r_Sinterval firstDimDomain = areaOp[0].create_intersection(rangeInterval);
    r_Minterval lastTwoResultDomain = lastTwoDimsDomain.create_intersection(convexHull);

    r_Minterval resultDomain(3);
    resultDomain << firstDimDomain;
    resultDomain << lastTwoResultDomain[0];
    resultDomain << lastTwoResultDomain[1];
    
    //create a new MDDObj to hold the result

    // this should rather be MDDDomainType? -- DM 2011-aug-12
    // Old implementation was :MDDBaseType* mddBaseType = new MDDBaseType( "tmp", resultBaseType );
    // Had type incompatibility issue because of missing dimension.
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), 3);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset( new MDDObj(mddBaseType, resultDomain, op->getNullValues()) );
    
    // build the polygon in the last two dimensions
    QtPolygonClipping polygonMethodsAccess(convexHull, polytope->getPolytopePoints() );   

    // using the Bresenham-style algorithm, we produce a mask applying to each 2D slice (translated to origin).
    vector<vector<char>> mask = polygonMethodsAccess.generateMask(true);
    
    // here, we apply the mask to each slice for the range and produce the output.
    boost::shared_ptr<Tile> resTile;
    std::unique_ptr<std::vector<boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->getTiles());

    try
    {
        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++)
        {
            //data type size
            size_t typeSize = (*tileIt)->getType()->getSize();
            //domain of source tile
            const r_Minterval& tileDom = (*tileIt)->getDomain();
            
            if (tileDom.intersects_with(resultDomain) )
            {
                // domain of the relevant area of the actual tile
                r_Minterval intersectDom(tileDom.create_intersection(resultDomain));

                // create tile for result
                resTile.reset( new Tile(intersectDom, op->getCellType()) );
                char* resultData = resTile->getContents();
                memset(resultData, 0, typeSize * intersectDom.cell_count());
                //various useful offsets for the algorithm
                size_t crossSectionSourceOffset = typeSize * static_cast<size_t>(tileDom[1].get_extent() * tileDom[2].get_extent());
                size_t crossSectionResultOffset = typeSize * static_cast<size_t>(intersectDom[1].get_extent() * intersectDom[2].get_extent());
                
                size_t rangeOffset = static_cast<size_t>(abs(intersectDom[0].low() - tileDom[0].low()));
                size_t topOffset = static_cast<size_t>(abs(intersectDom[1].low() - tileDom[1].low()));
                size_t botOffset = static_cast<size_t>(abs(tileDom[1].high() - intersectDom[1].high()));
                size_t leftOffset = static_cast<size_t>(abs(intersectDom[2].low() - tileDom[2].low()));
                size_t rightOffset = static_cast<size_t>(abs(tileDom[2].high() - intersectDom[2].high()));
                
                //iterate over the first coordinate (integration axis of curtain)
                for(size_t k = 0; k < static_cast<size_t>(intersectDom[0].get_extent()); k++)
                {
                    //result data pointer
                    resultData = resTile->getContents() + k*crossSectionResultOffset;
                    //source data pointer
                    char* sourceData = (*tileIt)->getContents() + (k + rangeOffset)*crossSectionSourceOffset;

                    //offset the source data pointer to the same row & column of the result data pointer
                    sourceData += (leftOffset + topOffset*static_cast<size_t>(tileDom[2].get_extent()))*typeSize;       
                    
                    //iterate over the 2nd coordinate (first axis of the polygon) (row)
                    for (auto i = intersectDom[1].low(); i <= intersectDom[1].high(); i++) 
                    {
                        //iterate over the 3rd coordinate (second axis of the polygon) (column))
                        for (auto j = intersectDom[2].low(); j <= intersectDom[2].high(); j++) 
                        {
                            //"< 2" -> fills boundary values(1) and interior (0); unchecked values (2) and exterior values (3) are not copied over in this case
                            if (mask[static_cast<size_t> (i - bBox.first[0])][static_cast<size_t> (j - bBox.first[1])] < 2 )
                            {
                                memcpy(resultData, sourceData, typeSize);
                            }
                            // move to the next cell of the op tile
                            sourceData += typeSize;
                            // move to the next cell of the result tile
                            resultData += typeSize;
                        }
                        //end of row in result tile. correct source data to the beginning of the next row
                        sourceData += (rightOffset + leftOffset)*typeSize;
                    }
                }
                
                // insert Tile in result mdd
                resultMDD->insertTile(resTile);
            }
        }
    }
    catch (r_Error &err)
    {
        LFATAL << "QtClipping::extractCurtain caught " << err.get_errorno() << " " << err.what();
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }
    catch (int err)
    {
        LFATAL << "QtClipping::extractCurtain caught errno error (" << err << ") in qtclipping";
        parseInfo.setErrorNo(err);
        throw parseInfo;
    }

    return resultMDD.release();    
}

MDDObj* 
QtClipping::extractCurtainLine(const MDDObj* op, const r_Minterval& areaOp, QtMShapeData* polytope, const QtMShapeData* rangeArg)
{
    //height of curtain
    //currently takes the two points in the range to determine the extent in the first coordinate and uses the polygon for the last two coordinates to determine the cutout shape (two cases -- line & polygon)
    r_Sinterval rangeInterval;
    rangeInterval.set_low(rangeArg->getPolytopePoints()[0][0]);
    rangeInterval.set_high(rangeArg->getPolytopePoints()[1][0]);
    
    //line algorithm
    
    // computing the bounding box of the multidimensional shape defined in QtMShapeData
    std::unique_ptr<BoundingBox> bBox;
    bBox.reset(computeBoundingBox(polytope));

    // the dataset dimension is the same as the dimension of the points defining the mshape, so
    // to extract the dataset dimension we use the first point.
    r_Dimension datasetDimension = (polytope->getMShapeData())[0].dimension();
    
    // directionVectors contains a set of n orthogonal vectors where n is the dimension of the dataset. The first
    // m vectors, where m is the dimension of the mshape define the space in which the m-dimensional shape lies into
    // The remaining vectors are vectors orthogonal to the mshape.
    std::vector<r_PointDouble>* directionVectors = polytope->getDirectionVectors();

    // Construct r_Minterval from the bounding box of the multidimensional shape
    r_Minterval mintervalBoundingBox(datasetDimension + 1);
    //initialize first coordinate to the given integration range
    mintervalBoundingBox << rangeInterval;
    //initialize the last two coordinates to those of the bounding box of the linestring
    for (r_Dimension i = 0; i < datasetDimension; i++)
    {
        r_Sinterval rs(static_cast<r_Range>(bBox->minPoint[i]), static_cast<r_Range>(bBox->maxPoint[i]));
        mintervalBoundingBox << rs;
    }
    
    // check in case there are no points in common between the r_Minterval where the mshape lies and the dataset itself.
    // In that case there is no need for further computations, and an exception is thrown
    if (!areaOp.intersects_with(mintervalBoundingBox))
    {
        LFATAL << "The subspace of the set of points provided does not pass through the stored data.";
        parseInfo.setErrorNo(SUBSPACENOINTERSECTION);
        throw parseInfo;
    }

    // vector containing r_Points that define the line we want to extract.
    vector<r_Point> bresenhamLine = computeNDBresenhamLine(polytope);

    //we first determine which band of values from the BLA result apply to this MDDObject
    r_Minterval newDomain = areaOp.create_intersection(mintervalBoundingBox);
    r_Point lowPointGlobal = newDomain.get_origin();
    r_Point highPointGlobal = newDomain.get_high();
    
    //now we project these to the dimensions of interest for the cross section containing the line segment
    r_Point lowPointLocal = (newDomain.get_origin()).indexedMap({1,2});
    r_Point highPointLocal = (newDomain.get_high()).indexedMap({1,2});

    // since for each tile the intersectionDomain is different, we need to know which points of the line
    // sit inside the overall dataset, so we can assign it a global domain. 
    // startEndIndices keeps track of the segment of the line contained inside areaOp.
    
    //we will use these for each cross-sectional slice
    pair<int, int> startEndIndicesGlobal = endpointsSearch(localHull(std::make_pair<int, int>(-1, -1), {lowPointLocal, highPointLocal}), bresenhamLine);

    // domain of the relevant area of the actual dbobj corresponds to the bounding box of the start and end points.
    r_Minterval domainOfInterest = localHull(std::make_pair<int, int>(-1, -1), bresenhamLine);
    
    std::vector<r_Range> bbExtents = (domainOfInterest.get_extent()).getVector();
    r_Range maxExtent = *(std::max_element(bbExtents.begin(), bbExtents.end()));
    r_Dimension index = std::distance(bbExtents.begin(), std::max_element(bbExtents.begin(), bbExtents.end()));
    
    // construct the result domain using the largest direction's index.
    // the 2nd dimension's extent corresponds to the total number of result points from the line in 2-D
    // the 1st dimension's extent corresponds to the intersection of the given range with the 1st dimension of the MDDObj.
    r_Minterval resultDomainGlobal(2);
    resultDomainGlobal[0] = newDomain[0];
    resultDomainGlobal[1] = localHullByIndex(startEndIndicesGlobal, bresenhamLine, index);
    
    //the result MDDObject
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), 2);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);
    
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset( new MDDObj(mddBaseType, resultDomainGlobal, op->getNullValues()) );

    // get all tiles in relevant area
    std::unique_ptr<std::vector<boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->intersect(newDomain));

    //pointer to the result tile
    boost::shared_ptr<Tile> resTile;
    // iterate over the tiles
    try
    {
        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++)
        {
            // domain of the actual tile
            r_Minterval tileDom = (*tileIt)->getDomain();
            if (tileDom.intersects_with(newDomain))
            {
                //interval for the curtain height on this tile
                r_Sinterval loopInterval = tileDom[0].create_intersection(newDomain[0]);
                
                 //we first determine which band of values from the BLA result apply to this tile
                r_Point lowPoint = tileDom.get_origin().indexedMap({1,2});
                r_Point highPoint = tileDom.get_high().indexedMap({1,2});
                
                // since for each tile the cross-sectional intersectionDomain is different, we need to know which points of the line
                // pass into this tile and only process those points. startEndIndices keeps track of the segment of the line
                // contained inside the intersectionDom.
                pair<int, int> startEndIndices = endpointsSearch(localHull(std::make_pair<int,int>(-1,-1), {lowPoint, highPoint}), bresenhamLine);

                if (startEndIndices.first == -1)
                {
                    // continue to next tile since we have no points to read from this one.
                    break;
                }                
                
                // domain of the result tile data set.
                r_Minterval resultDomain(2);
                resultDomain[0] = loopInterval;
                resultDomain[1] = localHullByIndex(startEndIndices, bresenhamLine, index);
                
                // data type size
                size_t typeSize = (*tileIt)->getType()->getSize();
                
                // source tile data pointer
                char* sourceData = NULL;

                // result tile and contents
                resTile.reset( new Tile(resultDomain, (*tileIt).get()->getType()) );
                char* resultData = resTile->getContents();
                
                //loop for performing the algorithm once for each value in the range interval
                for(auto x = loopInterval.low(); x <= loopInterval.high(); x++)
                {
                    sourceData = (*tileIt)->getCell(r_Point(x, bresenhamLine[startEndIndices.first][0], bresenhamLine[startEndIndices.first][1]));
                    memcpy(resultData, sourceData, typeSize);

                    // loop over bresenhamLine points which are relevant for this tile
                    // and transfer data from source to result tiles.
                    for (size_t i = static_cast<size_t>(startEndIndices.first); i < static_cast<size_t>(startEndIndices.second); i++)
                    {
                        // compute offset for the next data point to be copied
                        // complexity O(dimensionality)
                        sourceData = (*tileIt)->getCell(r_Point(x, bresenhamLine[i+1][0], bresenhamLine[i+1][1]));
                        resultData += typeSize;
                        memcpy(resultData, sourceData, typeSize);
                    }
                    //abuse the contiguousness of the result tile's data
                    resultData += typeSize;                    
                }
                
                // insert Tile in result mdd
                resultMDD->insertTile(resTile);
            }
        }
    }
    
    catch (r_Error &err)
    {
        LFATAL << "QtClipping::compute2D caught error: " << err.get_errorno() << " " << err.what();
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }
    
    catch (int err)
    {
        LFATAL << "QtClipping::compute2D caught error: " << err << " in qtclipping";
        parseInfo.setErrorNo(err);
        throw parseInfo;
    }
    
    return resultMDD.release();    
}

MDDObj* 
QtClipping::extractCurtainLinestring(const MDDObj* op, const r_Minterval& areaOp, const QtMShapeData* mshape, const QtMShapeData* rangeArg, r_Dimension dim)
{
    //height of curtain
    //currently takes the two points in the range to determine the extent in the first coordinate and uses the polygon for the last two coordinates to determine the cutout shape (two cases -- line & polygon)
    r_Sinterval rangeInterval;
    rangeInterval.set_low(rangeArg->getPolytopePoints()[0][0]);
    rangeInterval.set_high(rangeArg->getPolytopePoints()[1][0]);

    //restrict ourselves to the actual intersected range
    if(rangeInterval.intersects_with(areaOp[0]))
    {
        rangeInterval = rangeInterval.create_intersection(areaOp[0]);        
    }
    else
    {
        LFATAL << "QtClipping::extractCurtain - the curtain range does not intersect the first coordinate range of the MDDObj.";        
        throw r_Error(CURTAINRANGEINCORRECT);        
    }
    
    // create vector of bounding boxes (one for each line segment in the linestring)
    vector<r_Minterval> bBoxes = mshape->localConvexHulls();

    //for each one, we construct a vector (actually a pair) of r_Points representing the endpoints of the line segment being considered
    
    vector< vector< r_PointDouble > > vectorOfSegmentEndpointPairs;
    //max size
    vectorOfSegmentEndpointPairs.reserve(bBoxes.size());
    for(size_t i = 0; i < bBoxes.size(); i++)
    {
        size_t k = 1;
        
        while( i + k < mshape->getMShapeData().size() 
                && mshape->getMShapeData()[i] == mshape->getMShapeData()[i+k] )
        {
            k++;
        }
        
        vectorOfSegmentEndpointPairs.emplace_back(vector<r_PointDouble>({mshape->getMShapeData()[i], mshape->getMShapeData()[i+k]}));
        i += (k - 1);
    }
    
    //only consider the segments which contribute new points to the result vector
    //as above, the start and end points must differ.
    bBoxes.erase(remove_if(bBoxes.begin(), bBoxes.end(), isSingleton), bBoxes.end());
    
    vectorOfSegmentEndpointPairs.shrink_to_fit();
    
    // create vector of bresenham lines (one for each line segment passing through the domain of the MDDObject)
    vector< vector < r_Point > > vectorOfBresenhamLines;
    vectorOfBresenhamLines.reserve(vectorOfSegmentEndpointPairs.size());
    for(size_t i = 0; i < vectorOfSegmentEndpointPairs.size(); i++)
    {
        vectorOfBresenhamLines.emplace_back( computeNDBresenhamSegment(vectorOfSegmentEndpointPairs[i]) );
    }
    
    // create vector of intervals for the result tiles
    //[0 : k_0-1], [k_0 : k_0 + k_1 - 1], [k_0 + k_1 : k_0 + k_1 + k_2 - 1], ...
    //for each segment, we need to find the intersection of its bounding box's longest extent's dimension with the domain of the MDDObject being considered
    
    //vector of dimension #'s corresponding to the longest extents in bBoxes
    vector<r_Dimension> longestExtentDims;
    longestExtentDims.reserve(bBoxes.size());
    for(size_t i = 0; i < bBoxes.size(); i ++)
    {
        r_Range currentMaxExtent = 0;
        r_Dimension currentMaxDim = 0;
        for(r_Dimension j =0; j < dim; j++)
        {
            if ( bBoxes[i][j].get_extent() > currentMaxExtent )
            {
                currentMaxExtent = bBoxes[i][j].get_extent();
                currentMaxDim = j;
            }
        }
        
        longestExtentDims.emplace_back(currentMaxDim);
    }
    
    //construct the resulting tile intervals
    vector<r_Minterval> resultTileMintervals;
    resultTileMintervals.reserve(bBoxes.size());
    bool firstInterval = true;
    r_Range currentOffset = 0;
    for(size_t i = 0; i < bBoxes.size(); i++)
    {
            r_Minterval nextInterval(2);
            nextInterval[0] = rangeInterval;
            //translate to currentOffset
            if(!firstInterval)
            {
                nextInterval[1] = r_Sinterval(currentOffset, currentOffset + static_cast<r_Range>( bBoxes[i][longestExtentDims[i]].get_extent() ) - 2);                
            }
            else
            {
                nextInterval[1] = r_Sinterval(currentOffset, currentOffset + static_cast<r_Range>( bBoxes[i][longestExtentDims[i]].get_extent() ) - 1);
            }

            resultTileMintervals.emplace_back(nextInterval);
            currentOffset = nextInterval[1].high() + 1;
            firstInterval = false;
    }
    
    //construct the result MMDDObj
    
    r_Minterval resultDomainGlobal(2);
    r_Sinterval totalDomain(static_cast<r_Range>(0), static_cast<r_Range>(currentOffset) - 1);
    resultDomainGlobal[0] = rangeInterval.create_intersection(areaOp[0]);
    resultDomainGlobal[1] = totalDomain;
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), dim);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);    

    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset( new MDDObj(mddBaseType, resultDomainGlobal) );
    
    //   loop over source tiles
    //  loop over bresenhamLines vector
    //first check the intersection of the source tile with the bounding box, then construct the startEndIndices pair for that line segment
    //process Bresenham into the respective output tile
    
    // get all tiles in relevant area
    std::unique_ptr<std::vector<boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->getTiles());
   
    //initialize the result tiles here
    boost::shared_ptr<Tile> tempTile = *(allTiles->begin());
    size_t typeSize = tempTile->getType()->getSize();
    vector< boost::shared_ptr<Tile> > resultTiles;
    resultTiles.reserve(resultTileMintervals.size());
    for(size_t i = 0; i < resultTileMintervals.size(); i++)
    {
        //build a new tile
        boost::shared_ptr<Tile> resTilePtr;
        resTilePtr.reset( new Tile(resultTileMintervals[i], tempTile->getType()) );
        //initialize contents to 0
        char* resData = resTilePtr->getContents();
        memset(resData, 0, typeSize * ( resultTileMintervals[i] ).cell_count());
        //add tile to vector of result tiles
        resultTiles.emplace_back(resTilePtr);
    }

    try
    {
        //loop over source tiles
        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++)
        {
            // domain of the current source tile
            r_Minterval tileDom = (*tileIt)->getDomain();

            //loop over bresenham line segments (we do not use an iterator because we need the index for various vectors of the same length)
            for(size_t i = 0; i < bBoxes.size(); i++)
            {

                //current result tile's data
                char* resData = ( resultTiles[i] )->getContents();
                
                bool firstElementSkipped = false;
                                    //loop over the height of the curtain
                for(auto j = rangeInterval.low(); j <= rangeInterval.high(); j++)
                {
                    //iterate over the points in the current bresenham line segment
                    for(auto ptIter = vectorOfBresenhamLines[i].begin(); ptIter != vectorOfBresenhamLines[i].end(); ptIter++)
                    {
                        if(!firstElementSkipped && i != 0 )
                        {
                            //skip the first point for all but the first interval
                            ptIter++;
                            firstElementSkipped = true;
                        }

                        //r_Point to source data cell
                        //perhaps it would be better to simply construct these in a vector< vector< vector<r_Point> > > bresenhamCurtain?
                        r_Point srcPt(3);
                        srcPt[0] = j;
                        srcPt[1] = (*ptIter)[0];
                        srcPt[2] = (*ptIter)[1];
                        //if the current point is in the domain, we copy that point to the next point in resData
                        if ( tileDom.covers(srcPt) )
                        {
                            //pointer to source data cell
                            char* srcData = (*tileIt)->getCell(srcPt);
                            memcpy(resData, srcData, typeSize);
                            resData += typeSize;
                        }
                        else
                        {
                            resData += typeSize;
                        }
                    }
                    
                    firstElementSkipped = false;
                }
            }
        }
        //add result tiles to result MDDObj
        for(auto resTileIt = resultTiles.begin(); resTileIt != resultTiles.end(); resTileIt++)
        {
            resultMDD->insertTile(*resTileIt);
        }
    }
    catch (r_Error &err)
    {
        LFATAL << "QtClipping::compute2D caught error: " << err.get_errorno() << " " << err.what();
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }
    catch (int err)
    {
        LFATAL << "QtClipping::compute2D caught error: " << err << " in qtclipping";
        parseInfo.setErrorNo(err);
        throw parseInfo;
    }

    return resultMDD.release();    
}

QtData*
QtClipping::computeOp(QtMDD* operand, QtMShapeData* mshape)
{
    // get the MDD object
    MDDObj* op = operand->getMDDObject();

    //  get the area, where the operation has to be applied
    r_Minterval areaOp = operand->getLoadDomain();
    const r_Dimension opDim = areaOp.dimension();
    
    //mshape may define a subspace, linestring, or a polygon.
    //dimension of the smallest affine subspace containing the mshape vertices
    const r_Dimension mshapeDim = mshape->getDimension();
    
    //the dimensionality of the individual points
    //this is invariant for each mshape, and error checking for invariance is handled by QtMShapeData
    const r_Dimension mshapePtDim = mshape->getPointDimension();
    
    //number of points defining the mshape
    const size_t mshapePtCard = mshape->getPolytopePoints().size();

    // possible errors which apply to more than one clipType
    
    //need at least two points to get started here...
    if(mshapePtCard <= 1)
    {
        LFATAL << "Error: QtClipping::computeOp() - All geometric clip types require at least two vertices to be well-defined.";
        parseInfo.setErrorNo(NEEDTWOORMOREVERTICES);
        throw parseInfo;        
    }
    //point dimensionality must match that of the operand (< case may be acceptable for curtains)
    else if((opDim != mshapePtDim && clipType != CURTAIN_POLYGON && clipType != CURTAIN_LINESTRING) || opDim < mshapePtDim)
    {
        LFATAL << "Error: QtClipping::computeOp() - No natural embedding from the space containing the mdd object's grid to the space containing the clip vertices exists.";
        parseInfo.setErrorNo(NONATURALEMBEDDING);
        throw parseInfo;
    }
    //linestrings can end up defining the entire space, as can polygons in 2D.
    else if(opDim <= mshapeDim && clipType != CLIP_LINESTRING && clipType != CURTAIN_LINESTRING && clipType != CLIP_POLYGON)
    {
        LFATAL << "Error: QtClipping::computeOp() - The smallest affine subspace containing the clip vertices must not be equal to the entire space containing the mdd object's grid.";
        parseInfo.setErrorNo(SUBSPACEDIMSAMEASMDDOBJ);
        throw parseInfo;          
    }
    //curtain-specific errors
    else if(clipType == CURTAIN_POLYGON || clipType == CURTAIN_LINESTRING)
    {
        // throw an error since the dimension of the polygon/line vertices in the curtain cannot differ from 2.
        if(mshapePtDim != 2)
        {
            LFATAL << "Error: QtClipping::computeOp() - At present, curtain clipping can only occur on 3D datasets with polygons and linestrings confined to a plane.";
            parseInfo.setErrorNo(CURTAINLINESTRINGDIMENSIONMISMATCH);
            throw parseInfo;
        }
        // throw an error since the range is restricted to two values, the low and high point
        else if ( (range->getPointDimension() != 1) || (range->getPolytopePoints().size() != 2 ))
        {
            LFATAL << "Error: QtClipping::computeOp() - At present, polygon clipping can only occur on 3D datasets with the range defined in the first coordinate.";
            parseInfo.setErrorNo(CURTAINRANGEINCORRECT);
            throw parseInfo;                
        }
        // throw an error since the datacube operated on must be dimension 3, for now
        // todo (bbell): adapt curtains to be operable on arbitrary datasets of dimension >= 3.
        else if (opDim != 3)
        {
            LFATAL << "Error: QtClipping::computeOp() - At present, polygon clipping can only occur on 3D datasets.";
            parseInfo.setErrorNo(CURTAINDOMAINDIMENSIONERROR);
            throw parseInfo;                  
        }
    }
    
    std::unique_ptr<MDDObj> resultMDD;
    
    if (clipType == CLIP_POLYGON)
    {
        if(opDim == 2 && mshapeDim == 2)
        {
            //this is the usual case
            QtPolygonClipping polygonMethodsAccess(areaOp, mshape->getPolytopePoints());
            resultMDD.reset( polygonMethodsAccess.compute2DBresenham(op, opDim) );
        }
        //todo (bbell): else if(opDim == mshapePtDim > 2 && mshapeDim == 2)
        //      in this case, the polygon lies in an affine subspace, so we should perform a subspace query, project the polygon vertices to the result, and perform a polygon clipping there.
        else
        {
            // throw an error since the dimension of the polygon cannot differ from 2.
            LFATAL << "Error: QtClipping::computeOp() - At present, polygon clipping can only occur on 2D datasets with 2D polytopes such as triangles.";
            parseInfo.setErrorNo(INCORRECTPOLYGON);
            throw parseInfo;
        }
    }
    else if (clipType == CLIP_SUBSPACE)
    {
        if (opDim != 1 && mshapeDim == 1)
        {
           resultMDD.reset( extractBresenhamLine(op, areaOp, mshape, opDim) );
        }
        else //now, we are guaranteed that mshapeDim < opDim
        {
            resultMDD.reset( extractSubspace(op, areaOp, mshape) );
        }        
    }
    else if (clipType == CLIP_LINESTRING)
    {
        resultMDD.reset( extractLinestring(op, mshape, opDim) );
    }
    else if (clipType == CURTAIN_POLYGON)
    {
        //ensure that range has been initialized
        if(range)
        {
            resultMDD.reset( extractCurtainPolygon(op, areaOp, mshape, range) );
        }
    }
    else if (clipType == CURTAIN_LINESTRING)
    {
        //ensure that range has been initialized
        if(range)
        {
            //optimized version of linestring, for a single bresenham line segment.
            if (mshapePtCard == 2)
            {
                resultMDD.reset( extractCurtainLine(op, areaOp, mshape, range) ); 
            }
            else if (mshapePtCard > 2)
            {
                resultMDD.reset( extractCurtainLinestring(op, areaOp, mshape, range) );
            }
        }
    }
    else
    {        parseInfo.setErrorNo(CLIPERRORUNDEFINED);
        throw parseInfo;        
    }
    
    if(!resultMDD)
    {
        parseInfo.setErrorNo(CLIPERRORUNDEFINED);
        throw parseInfo;
    }
    
    // create a new QtMDD object as carrier object for the transient MDD object
    QtData* returnValue = new QtMDD(resultMDD.release());
    
    return returnValue;
}

QtData*
QtClipping::evaluate(QtDataList *inputlist)
{
    QtData *returnValue = NULL;
    QtData *operand1 = NULL;
    QtData *operand2 = NULL;

    // evaluate sub-nodes to obtain operand values
    if (getOperands(inputlist, operand1, operand2))
    {
        //
        // This implementation simply creates a new transient MDD object with the new
        // domain while copying the data. Optimization of this is left for future work.
        //

        QtMDD *qtMDDObj = static_cast<QtMDD *>(operand1);
        std::vector<r_PointDouble> const *polygon = NULL;

        // get multidimensional shape (already checked in checktype)
        if (operand2->getDataType() == QT_MSHAPE)
        {
            // get the vector from QT_MSHAPEOP

            polygon = &((static_cast<QtMShapeData *>(operand2))->getMShapeData());
        }

        MDDObj *currentMDDObj = qtMDDObj->getMDDObject();
        QtMShapeData *multidimShape = static_cast<QtMShapeData *>(operand2);
        if (qtMDDObj->getLoadDomain().dimension() < multidimShape->getDimension())
        {
            LFATAL << "Error: QtClipping::evaluate() - Dimension of the polygon vertices is bigger than the domain's dimension.";
            parseInfo.setErrorNo(407);
            throw parseInfo;
        }
        else
        {
            returnValue = computeOp(qtMDDObj, static_cast<QtMShapeData *>(operand2));
        }
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

const QtTypeElement& QtClipping::checkType(QtTypeTuple *typeTuple)
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
            LFATAL << "Error: QtClipping::checkType() - first operand must be of type MDD.";
            parseInfo.setErrorNo(MDDARGREQUIRED);
            throw parseInfo;
        }

        // operand two can be a single long number, the parser does [a] -> number a,
        // rather than [a] -> point (which is then used in marray/condense..),
        // so we need to take care manually here of this edge case -- DM 2015-aug-24
        if (inputType2.getDataType() != QT_MSHAPE)
        {
            LFATAL << "Error: QtClipping::checkType() - second operand must be of type QT_MSHAPE.";
            parseInfo.setErrorNo(MSHAPEARGREQUIRED);
            throw parseInfo;
        }

        dataStreamType = inputType1;
    }
    else
    {
        LERROR << "Error: QtClipping::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}
