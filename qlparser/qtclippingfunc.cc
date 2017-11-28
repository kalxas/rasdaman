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

#include <easylogging++.h>

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

QtClipping::QtClipping(QtOperation *mddOp, QtOperation *mshapePointList, QtClipType pt)
    : QtBinaryOperation(mddOp, mshapePointList), clipType(pt)
{
}

bool QtClipping::isCommutative() const
{
    return false; // NOT commutative
}

MDDObj*
QtClipping::extractBresenhamLine(MDDObj* op, r_Minterval areaOp, QtMShapeData* mshape, MDDObj* resultMDD, r_Dimension dim)
{
    // computing the bounding box of the multidimensional shape defined in QtMShapeData
    boundingBox *bBox = computeBoundingBox(mshape);

    MDDDimensionType *mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), dim);
    MDDBaseType *mddBaseType = static_cast<MDDBaseType *>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    // the dataset dimension is the same as the dimension of the points defining the mshape, so
    // to extract the dataset dimension we use the first point.
    r_Dimension datasetDimension = (mshape->getMShapeData())[0].dimension();

    // directionVectors contains a set of n orthogonal vectors where n is the dimension of the dataset. The first
    // m vectors, where m is the dimension of the mshape define the space in which the m-dimensional shape lies into
    // The remaining vectors are vectors orthogonal to the mshape.
    std::vector<r_PointDouble>* directionVectors = mshape->getDirectionVectors();

    // Construct r_Minterval from the bounding box of the multidimensional shape
    r_Minterval mintervalBoundingBox(datasetDimension);
    for (r_Dimension i = 0; i < datasetDimension; i++)
    {
        r_Sinterval rs(static_cast<r_Range>(bBox->minPoint[i]), static_cast<r_Range>(bBox->maxPoint[i]));
        mintervalBoundingBox << rs;
    }

    //first, we check if perhaps the dimensionality of the bounding box and the dataset are different.
    if(areaOp.dimension() != mintervalBoundingBox.dimension())
    {
        LFATAL << "The dimension of the points is not the same as the dimension of the ambient space.";
        parseInfo.setErrorNo(SUBSPACENOINTERSECTION);
        throw parseInfo;
    }
    
    // check in case there are no points in common between the r_Minterval where the mshape lies into and the dataset itself.
    // In that case there is no need for further computations and an exception is thrown
    if (!areaOp.intersects_with(mintervalBoundingBox))
    {
        LFATAL << "The subspace of the set of points provided does not pass through the stored data.";
        parseInfo.setErrorNo(SUBSPACENOINTERSECTION);
        throw parseInfo;
    }

    areaOp = areaOp.create_intersection(mintervalBoundingBox);

    // a set of dimensions where the subspace is going to be projected. This set is used to create the tiles of the
    // result mddobj based on the original mddobj tiles.
    std::set<r_Dimension, std::less<r_Dimension>> projectionDimensionSet;

    // vector containing r_Points that define the line we want to extract.
    vector<r_Point> bresenhamLine;
    // this method initializes the vector with the values from a BLA
    compute_nD_Bresenham_Line(mshape, bresenhamLine);

    // initialize the MDDObj with the correct base type and an arbitrary result domain 
    // we do not know what the result tiles are yet, but we do know the dimension of the result
    r_Minterval resultDom(1);

    //we first determine which band of values from the BLA result apply to this tile
    r_Point lowPointGlobal = areaOp.get_origin();
    r_Point highPointGlobal = areaOp.get_high();

    // since for each tile the intersectionDomain is different, we need to know which points of the line
    // sit inside the overall dataset, so we can assign it a global domain. 
    // startEndIndices keeps track of the segment of the line contained inside areaOp.
    pair<int, int> startEndIndicesGlobal = make_pair(-1, -1);

    for (size_t i = 0; i < bresenhamLine.size(); i++)
    {
        if ((bresenhamLine[i] >= lowPointGlobal) && (bresenhamLine[i] <= highPointGlobal))
        {
            if (startEndIndicesGlobal.first == -1)
            {
                startEndIndicesGlobal.first = i;
                startEndIndicesGlobal.second = i;
            }
            else
            {
                startEndIndicesGlobal.second = i;
            }
        }
    }
    
    // domain of the relevant area of the actual dbobj corresponds to the bounding box of the start and end points.
    r_Minterval domainOfInterestGlobal(datasetDimension);
    for(size_t i = 0; i < datasetDimension; i++)
    {
        if(bresenhamLine[0][i] <= bresenhamLine[bresenhamLine.size()-1][i])
        {
            domainOfInterestGlobal[i].set_low(bresenhamLine[0][i]);
            domainOfInterestGlobal[i].set_high(bresenhamLine[bresenhamLine.size()-1][i]);
        }
        else
        {
            domainOfInterestGlobal[i].set_low(bresenhamLine[bresenhamLine.size()-1][i]);
            domainOfInterestGlobal[i].set_high(bresenhamLine[0][i]);
        }
    }
    
    // resultDom's r_Sinterval corresponds to the longest extent of domainOfInterestGlobal
    r_Point bbExtentsGlobal = domainOfInterestGlobal.get_extent();
    r_Range maxExtentGlobal = bbExtentsGlobal[0];
    size_t index = 0;
    for(size_t i = 1; i < datasetDimension; i++)
    {
        if(maxExtentGlobal < bbExtentsGlobal[i])
        {
            maxExtentGlobal = bbExtentsGlobal[i];
            index = i;
        }
    }
    
    // construct the result domain using the largest direction's index.
    // the extent here corresponds to the total number of result points
    r_Minterval resultDomainGlobal(1);
    if(startEndIndicesGlobal.first >= 0 && startEndIndicesGlobal.second >= 0)
    {
        if(bresenhamLine[static_cast<size_t>(startEndIndicesGlobal.first)][index] <= bresenhamLine[static_cast<size_t>(startEndIndicesGlobal.second)][index])
        {
            resultDomainGlobal[0].set_high(bresenhamLine[static_cast<size_t>(startEndIndicesGlobal.second)][index]);
            resultDomainGlobal[0].set_low(bresenhamLine[static_cast<size_t>(startEndIndicesGlobal.first)][index]);
        }
        else
        {
            resultDomainGlobal[0].set_high(bresenhamLine[static_cast<size_t>(startEndIndicesGlobal.first)][index]);
            resultDomainGlobal[0].set_low(bresenhamLine[static_cast<size_t>(startEndIndicesGlobal.second)][index]);
        }
    }
    resultMDD = new MDDObj(mddBaseType, resultDomainGlobal);
    
    // dimension associated with the iteration (projection operator is injective onto this subspace)
    r_Dimension maxSlopeDim = 0;
    for (r_Dimension d = 0; d < datasetDimension; d++)
    {
        maxSlopeDim = abs( directionVectors->at(0)[maxSlopeDim] ) > abs( directionVectors->at(0)[d] ) ? maxSlopeDim : d;
    }
    bool negativeDirection = directionVectors->at(0)[maxSlopeDim] < 0;

    // get all tiles in relevant area
    vector<boost::shared_ptr<Tile>> *allTiles = op->getTiles();

    Tile *resTile = NULL;
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
                r_Point lowPoint = tileIt->get()->getDomain().get_origin();
                r_Point highPoint = tileIt->get()->getDomain().get_high();

                // since for each tile the intersectionDomain is different we need to know which points of the line
                // pass into this tile and only process those points. startEndIndices keeps track of the segment of the line
                // contained inside the intersectionDom.
                pair<int, int> startEndIndices = make_pair(-1, -1);

                for (size_t i = 0; i < bresenhamLine.size(); i++)
                {
                    if ((bresenhamLine[i] >= lowPoint) && (bresenhamLine[i] <= highPoint))
                    {
                        if (startEndIndices.first == -1)
                        {
                            startEndIndices.first = i;
                            startEndIndices.second = i;
                        }
                        else
                        {
                            startEndIndices.second = i;
                        }
                    }
                }

                if (startEndIndices.first == -1)
                {
                    // continue to next tile since we have no points to read from this one.
                    continue;
                }

                // domain of the result data set.
                r_Minterval domainOfInterest(1);
                
                if(startEndIndices.first >= 0 && startEndIndices.second >= 0)
                {
                    if(bresenhamLine[static_cast<size_t>(startEndIndices.first)][index] <= bresenhamLine[static_cast<size_t>(startEndIndices.second)][index])
                    {
                        domainOfInterest[0].set_low(bresenhamLine[static_cast<size_t>(startEndIndices.first)][index]);
                        domainOfInterest[0].set_high(bresenhamLine[static_cast<size_t>(startEndIndices.second)][index]);
                    }
                    else
                    {
                        domainOfInterest[0].set_low(bresenhamLine[static_cast<size_t>(startEndIndices.second)][index]);
                        domainOfInterest[0].set_high(bresenhamLine[static_cast<size_t>(startEndIndices.first)][index]);                        
                    }
                }
                
                
                // data type size
                size_t typeSize = (*tileIt)->getType()->getSize();
                
                // result tile and contents
                resTile = new Tile(domainOfInterest, (*tileIt).get()->getType());
                char *resultData = resTile->getContents();

                // source tile extents
                r_Point extents = tileIt->get()->getDomain().get_extent();                
                // to simplify extent computations, we compute the products of the relevant extents first
                r_Point genExtents(extents.dimension());
                for(size_t i = 0; i < extents.dimension(); i++)
                {
                    genExtents[i] = static_cast<r_Range>(typeSize);
                    for(size_t j = i; j + 1 < extents.dimension(); j++)
                    {
                        genExtents[i] *= extents[j+1];
                    }
                }

                // compute position in the result char* array for assigning the result tile content
                char* sourceData = (*tileIt)->getCell(lowPoint);
                // we could insert this at the front of bresenhamLine, but this way is faster as it avoids an O(length of line) computation
                r_Point startingPoint = bresenhamLine[static_cast<size_t>(startEndIndices.first)];
                int offset = computeOffset(genExtents, lowPoint, startingPoint);
                sourceData += (offset);
                memcpy(resultData, sourceData, typeSize);
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
                    // compute offset for the next data point to be copied
                    // complexity O(dimensionality)
                    offset = computeOffset(genExtents, bresenhamLine[i], bresenhamLine[i+1]);                    
                    sourceData += (offset);
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
        delete allTiles;
        allTiles = NULL;
        delete bBox;
        bBox = NULL;
        //contents of allTiles are deleted when index is deleted
        delete resultMDD;
        resultMDD = NULL;
        delete resTile;
        resTile = NULL;
        delete resultMDD;
        resultMDD = NULL;
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }
    
    catch (int err)
    {
        LFATAL << "QtClipping::compute2D caught error: " << err << " in qtclipping";
        delete allTiles;
        allTiles = NULL;
        delete bBox;
        bBox = NULL;
        //contents of allTiles are deleted when index is deleted
        delete resultMDD;
        resultMDD = NULL;
        delete resTile;
        resTile = NULL;
        delete resultMDD;
        resultMDD = NULL;
        parseInfo.setErrorNo(static_cast<unsigned int>(err));
        throw parseInfo;
    }
    
    if (resultMDD->getTiles() == NULL)
    {
        LFATAL << "QtClipping::extract_nd_BresenhamLine() - The subspace defined by the set of points provided does not pass through the stored data.";
        parseInfo.setErrorNo(SUBSPACENOINTERSECTION);
        throw parseInfo;
    }
    
    return resultMDD;
}

MDDObj *
QtClipping::extractSubspace(MDDObj* op, const r_Minterval& areaOp, QtMShapeData* mshape, MDDObj* resultMDD)
{
    // dimension of the source space.
    r_Dimension datasetDimension = (mshape->getMShapeData())[0].dimension();

    // compute the bounding box of the mShape
    boundingBox* bBox = computeBoundingBox(mshape);
    
    
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
        throw r_Error(SUBSPACENOINTERSECTION);
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
    subspaceAreaOp = computeProjectedMinterval(mshape, bBox, &bBoxRemoveDimension, projectionDimensionSet);
    
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
    resultMDD = new MDDObj(mddBaseType, projectedDomain);
        
    // get all tiles in relevant area
    vector<boost::shared_ptr<Tile>>* allTiles = op->getTiles();
    Tile* resTile = NULL;
    
    //object for computing the point coordinates of the preimage of the projection in the subspace.
    FindSection resFinder(mshape->computeHyperplaneEquation(), keptDimensions);
    //prepare additional variables for later computation.
    resFinder.prepareSection();
    try
    {
        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++)
        {
            //data type size
            size_t typeSize = (*tileIt)->getType()->getSize();
            //domain of source tile
            const r_Minterval& tileDom = (*tileIt)->getDomain();
            
            if (tileDom.intersects_with(tranBox))
            {
                // create a new tile with the appropriate type and domain
                if (resTile == NULL)
                {
                    //the easiest solution is to have a single result tile:
                    resTile = new Tile(projectedDomain, (*tileIt)->getType());

                    // initialize the data in the tile to 0
                    char* resInitializer = resTile->getContents();
                    size_t totalTileSize = (resTile->getDomain().cell_count()) * typeSize;
                    memset(resInitializer, 0, totalTileSize);
                }

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
        }
        //insert our one tile, if it exists
        if(resTile)
        {
            resultMDD->insertTile(resTile);
        }
        else
        {
            throw r_Error(SUBSPACENOINTERSECTION);
        }
    }
    
    catch (r_Error &err)
    {
        LFATAL << "QtClipping::compute2D caught " << err.get_errorno() << " " << err.what();
        delete allTiles;
        allTiles = NULL;
        delete bBox;
        bBox = NULL;
        //contents of allTiles are deleted when index is deleted
        delete resultMDD;
        resultMDD = NULL;
        delete resTile;
        resTile = NULL;
        delete resultMDD;
        resultMDD = NULL;
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }
    
    catch (int err)
    {
        LFATAL << "QtClipping::compute2D caught errno error (" << err << ") in qtclipping";
        delete allTiles;
        allTiles = NULL;
        delete bBox;
        bBox = NULL;
        //contents of allTiles are deleted when index is deleted
        delete resultMDD;
        resultMDD = NULL;
        delete resTile;
        resTile = NULL;
        delete resultMDD;
        resultMDD = NULL;
        parseInfo.setErrorNo(static_cast<unsigned int>(err));
        throw parseInfo;
    }

    if (resultMDD->getTiles() == NULL)
    {
        LFATAL << "QtClipping::extractSubspace() - The subspace of the set of points provided does not pass through the stored data.";
        parseInfo.setErrorNo(SUBSPACENOINTERSECTION);
        throw parseInfo;
    }

    return resultMDD;
}

QtData*
QtClipping::computeOp(QtMDD* operand, QtMShapeData* mshape)
{
    QtData *returnValue = NULL;
    // get the MDD object
    MDDObj *op = (static_cast<QtMDD*>(operand))->getMDDObject();

    //  get the area, where the operation has to be applied
    r_Minterval areaOp = (static_cast<QtMDD*>(operand))->getLoadDomain();

    const r_Dimension dim = areaOp.dimension();

    MDDObj *resultMDD = NULL;

    if (clipType == CLIP_POLYGON)
    {
        if (operand->getLoadDomain().dimension() < mshape->getDimension())
        {
            // throw error since the dimension of the polytope cannot be larger than the dimension
            // of the space in which it lies.
            LFATAL << "Error: QtClipping::computeOp() - Dimension of the subspace containing the polygon is larger than the dimension of the MDD object.";
            parseInfo.setErrorNo(POLYTOPEDIMENSIONTOOLARGE);
            throw parseInfo;
        }
        else if (operand->getLoadDomain().dimension() == 2 && mshape->getDimension() == 2)
        {
            QtPolygonClipping polygonMethodsAccess(areaOp, mshape->getPolytopePoints());
            resultMDD = polygonMethodsAccess.compute2D_Bresenham(op, resultMDD, dim);
        }
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
        if (operand->getLoadDomain().dimension() < mshape->getDimension())
        {
            // throw error since the dimension of the polytope cannot be larger than the dimension
            // of the space in which it lies.
            LFATAL << "Error: QtClipping::computeOp() - Dimension of the subspace is larger than the dimension of the MDD object.";
            parseInfo.setErrorNo(POLYTOPEDIMENSIONTOOLARGE);
            throw parseInfo;
        }
        else if (operand->getLoadDomain().dimension() != 1 && mshape->getDimension() == 1)
        {
            resultMDD = extractBresenhamLine(op, areaOp, mshape, resultMDD, dim);
        }
        else
        {
            resultMDD = extractSubspace(op, areaOp, mshape, resultMDD);
        }        
    }
    // create a new QtMDD object as carrier object for the transient MDD object
    returnValue = new QtMDD(resultMDD);

    return returnValue;
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
            // TODO: (joana) - here apply the polytope clipping directly
            returnValue = computeOp(qtMDDObj, static_cast<QtMShapeData *>(operand2));
        }
        /*
        else if(qtMDDObj->getLoadDomain() != 1 && multidimShape->getDimension())
        {

        }
        else
        {
            // in case the dimension of the dataset is smaller than
            // we apply n-dimensional brensenham    
            returnValue = computeOp(qtMDDObj, static_cast<QtMShapeData *>(operand2));
        }
        */
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
            parseInfo.setErrorNo(405);
            throw parseInfo;
        }

        // operand two can be a single long number, the parser does [a] -> number a,
        // rather than [a] -> point (which is then used in marray/condense..),
        // so we need to take care manually here of this edge case -- DM 2015-aug-24
        if (inputType2.getDataType() != QT_MSHAPE)
        {
            LFATAL << "Error: QtClipping::checkType() - second operand must be of type QT_MSHAPE.";
            parseInfo.setErrorNo(MDDARGREQUIRED);
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
