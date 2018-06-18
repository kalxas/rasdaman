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

static const char rcsid[] = "@(#)qlparser, QtCorridor: $Id: qtcorridor.cc,v 1.47 2018/05/22 10:36:15 coman Exp $";

#include "qlparser/qtcorridor.hh"

#include "raslib/miter.hh"
#include "raslib/mitera.hh"

#include "config.h"

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

#include <stack>    
#ifndef CPPSTDLIB
#else
#include <string>
#include <cmath>
#endif

#include <iostream>

// constructor for QtCorridor

const QtNode::QtNodeType QtCorridor::nodeType = QtNode::QT_CORRIDOR;

QtCorridor::QtCorridor(QtOperation* mddOp, 
                        std::shared_ptr<QtMulticlipping> clipArg,
                        QtMShapeData* lineString,
                        QtMShapeData* projDims)
    : QtUnaryOperation(mddOp), clipping(clipArg), lineStringData(lineString)
{
    //the assumption here is that the mshapeProjections form a pair of r_Dimension values
    //the parser should take something in the format of a 1-D linestring WKT point: (x, y)
    
    // check if projDims is the vector of projection dimension values we want.
    std::vector<r_Point> projections = projDims->getPolytopePoints();
    for(size_t i = 0; i < projections.size(); i++)
    {
        // is the dimension value complex? 
        if(projections[i].dimension() != 1)
        {
            // throw an error
            LERROR << "The coordinate projections must be singular values separated by commas.";            
            throw r_Error(SINGLETONPROJECTIONCOORDS);
        }
    }
    
    // a vector of dimensions containing the mask
    maskDims.reserve( projDims->getPolytopePoints().size() );
    for(size_t i = 0; i < projDims->getPolytopePoints().size(); i++)
    {
        maskDims.emplace_back(static_cast<r_Dimension>(projDims->getPolytopePoints()[i][0]));
    }    
}

//functor for passing to the remove_if iterator in std::vector in linestrings
bool isRedundant(const r_Minterval& interval )
{
    return interval.cell_count() == 1;
}

std::vector<r_Minterval>
QtCorridor::computeMaskEmbedding(const std::vector< std::vector<r_Point> >& pointListArg, const r_Minterval& convexHullArg, r_Range outputLength)
{   
    std::vector<r_Minterval> result;
    result.reserve(static_cast<size_t>(outputLength));
    
    r_Point firstPoint = pointListArg[0][0];    
     
    if(!convexHullArg.covers(pointListArg[0][0].indexedMap(maskDims)))
    {
        LERROR << "The coordinates of the starting point of the linestring do not sit inside the polygon's convex hull.";            
        throw r_Error(MASKNOTALIGNEDWITHLINESTRING);
    }
    else
    {
        // iterate over the segments
        for(auto segIter = pointListArg.begin(); segIter != pointListArg.end(); segIter++)
        {
            // iterate over the points in each segment
            for(auto ptIter = segIter->begin(); ptIter != segIter->end(); ptIter++)
            {
                //find the current slice by translating the convex hull of 
                r_Minterval currentSlice(firstPoint.dimension());
                r_Point translation = *ptIter - firstPoint;
                for(r_Dimension i = 0; i < firstPoint.dimension(); i++)
                {
                    //translate the convex hull argument along the dimensions it corresponds to (guaranteed to contain the translated point, since convexHullArg contains the first point)
                    auto it = std::find(maskDims.begin(), maskDims.end(), i);
                    auto index = std::distance(maskDims.begin(), it);                    
                    if(it == maskDims.end())
                    {
                        // the current point location
                        currentSlice << r_Sinterval((*ptIter)[i], (*ptIter)[i]);
                    }
                    else
                    {
                        // the current translated bounding box
                        currentSlice << r_Sinterval( convexHullArg[index].low() + translation[i], convexHullArg[index].high() + translation[i] );
                    }
                }
                result.emplace_back(currentSlice);
            }
        }
    }
    
    return result;
}

MDDObj*
QtCorridor::extractCorridor(const MDDObj* op, const r_Minterval& areaOp)
{   
    // algo for extracting corridor from op using the stored mask
    
    //first, we process the linestring as seen in QtClipping::extractLinestring
    
    // create vector of bounding boxes (one for each line segment in the linestring)
    vector<r_Minterval> bBoxes = lineStringData->localConvexHulls();
    
    //only consider the segments which contribute new points to the result vector
    //as above, the start and end points must differ.
    bBoxes.erase(remove_if(bBoxes.begin(), bBoxes.end(), isRedundant), bBoxes.end());    
    
    //for each one, we construct a vector (actually a pair) of r_Points representing the endpoints of the line segment being considered
    
    vector< vector< r_PointDouble > > vectorOfSegmentEndpointPairs = vectorOfPairsWithoutMultiplicity( lineStringData->getMShapeData(), bBoxes.size() );
    
    // create vector of bresenham lines (one for each line segment passing through the domain of the MDDObject)
    // optimization: we technically only need the offset vectors (points consisting of coordinate values -1, 0, +1), and the first point in the linestring.
    vector< vector < r_Point > > vectorOfBresenhamLines;
    vectorOfBresenhamLines.reserve(vectorOfSegmentEndpointPairs.size());
    for(size_t i = 0; i < vectorOfSegmentEndpointPairs.size(); i++)
    {
        vectorOfBresenhamLines.emplace_back( computeNDBresenhamSegment(vectorOfSegmentEndpointPairs[i]) );
    }

    // create vector of intervals for the result domain computations
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
       
    // construct the resulting tile intervals
    vector<r_Minterval> resultTileMintervals = vectorOfResultTileDomains(bBoxes, longestExtentDims);
    
    // construct the result linestring interval
    r_Sinterval lineStringDomain(static_cast<r_Range>(0), resultTileMintervals.back()[0].high());
    // the sdom of the mask
    r_Minterval convexHull = *(mask.second);
    
    // construct the convex hull of the embedding of the result in the source object, and the vector of mask domains (translated according to the linestring)
    // convention: the first mask is assumed to contain the first point of the linestring. We check that here.
    std::vector< r_Minterval > embeddedMaskDomains;
    //embeddedMaskDomains.reserve(static_cast<size_t>(lineStringDomain.get_extent()));
    
    embeddedMaskDomains = computeMaskEmbedding(vectorOfBresenhamLines, convexHull, lineStringDomain.get_extent());    
  
    //now, we build the convex hull of all these intervals, and call it the "outer hull"
    r_Minterval outerHull = embeddedMaskDomains[0];
    for(auto it = embeddedMaskDomains.begin(); it != embeddedMaskDomains.end(); it++)
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
    r_Minterval resultDomain( 1 + convexHull.dimension() );    
    //the first dimension corresponds to the length of the linestring
    resultDomain << lineStringDomain;
    // the last two dimensions correspond to the mask dimensions

    for(r_Dimension i = 0; i < convexHull.dimension(); i++)
    {
        resultDomain << convexHull[i];
    }
    
    // generate the result MDDObj with resultDomain as the domain.
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), resultDomain.dimension());
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);
    TypeFactory::addTempType(mddBaseType);
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset( new MDDObj(mddBaseType, resultDomain, op->getNullValues()) );
    
    // unfortunately, the result consists of a single tile, since source tile data is sheared into place for the result.
    // REMARK (BBELL): I cannot think of a better way of doing this; it might be nice if we could predictively and meaningfully chunk this.
    
    // generate the unique result tile
    boost::shared_ptr<Tile> resTile;
    resTile.reset( new Tile( resultDomain, op->getCellType() ) );
    // result data pointer
    char* resDataPtr = resTile->getContents();
    // initialize the result with nullValues
    op->fillTileWithNullvalues(resDataPtr, resultDomain.cell_count());
    
    // pointer to all source tiles which might intersect our area of interest
    std::unique_ptr< std::vector< boost::shared_ptr<Tile> > > allTiles;
    allTiles.reset(op->intersect(outerHull));

    //if no intersections were found, we throw an error.
    if(allTiles->empty())
    {
        LERROR << "The entire selection exists outside the source MDD.";            
        throw r_Error(CURTAINDOMAININTERSECTERROR);
    }
    
    // iterate over source tiles   
    for(auto srcTileIter = allTiles->begin(); srcTileIter != allTiles->end(); srcTileIter++)
    {    
        // source tile domain
        r_Minterval srcArea((*srcTileIter)->getDomain());
        
        
        // we first need to find which intervals coincide with the source tile, and create a stack for insertion
        // the stack consists of just the index of the interval in the embeddedMaskDomains vector, so r_Dimension
        std::stack< r_Dimension > indices;
        for(auto it = embeddedMaskDomains.begin(); it != embeddedMaskDomains.end(); it++)
        {
            if(it->intersects_with(srcArea));
            {
                r_Dimension index = std::distance(embeddedMaskDomains.begin(), it);
                indices.emplace(index);
            }
        }
        
        while(!indices.empty())
        {
            // get the type and type size of the data
            const BaseType* baseType = (*srcTileIter)->getType();
            r_Bytes typeSize = baseType->getSize();            
            
            r_Minterval aoiSrc(srcArea.dimension());
            
            //src aoi
            if(srcArea.intersects_with(embeddedMaskDomains[ indices.top() ] ))
            {
                aoiSrc = srcArea.create_intersection(embeddedMaskDomains[ indices.top() ] );
            }
            else
            {
                indices.pop();
                continue;
            }
            
            //res slice containing aoiRes
            r_Minterval resSlice(1 + convexHull.dimension());
            //first dim is linestring
            resSlice << r_Sinterval( static_cast<r_Range>(indices.top()), static_cast<r_Range>(indices.top()) );
            
            for(r_Dimension i = 0; i < convexHull.dimension(); i++)
            {
                resSlice << convexHull[i];
            }       

            //recover the translation, though this could be optimized out somehow... would be great if we could search tiles a bit more creatively!
            r_Point translationPt = embeddedMaskDomains[0].get_origin() - embeddedMaskDomains[ indices.top() ].get_origin();
            
            //mask aoi
            r_Minterval aoiMask = (aoiSrc.create_translation(translationPt)).project_along_dims(maskDims);            
                        
            //result aoi
            r_Minterval aoiRes(1 + aoiMask.dimension());
            
            aoiRes << r_Sinterval( static_cast<r_Range>(indices.top()), static_cast<r_Range>(indices.top()) );
            
            for(r_Dimension i = 0; i < aoiMask.dimension(); i++)
            {
                aoiRes << aoiMask[i];
            }             
            
            //src tile data iterator -- iterates just over the relevant area in the source tile
            r_Miter srcIter( &aoiSrc, &srcArea, typeSize, (*srcTileIter)->getContents());
            //res tile data iterator -- iterates just over the relevant area in the result tile
            r_Miter resIter( &aoiRes, &resultDomain, typeSize, resTile->getContents());
            //mask arg data iterator -- iterates just over the relevant area in the mask
            r_Miter maskIter( &aoiMask, &convexHull, sizeof(char), mask.first.get());
            
            while(!maskIter.isDone())
            {
                if( *(maskIter.nextCell()) < 2 )
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

QtData*
QtCorridor::evaluate(QtDataList* inputList)
{
    QtData* returnValue = NULL;
    
    // get the operand
    QtData* operand = input->evaluate( inputList );
    
    //now, we generate the mask
    mask = clipping->buildAbstractMask();
    
    // evaluate sub-nodes to obtain operand values
    if (operand)
    {
        // source mdd object in the 1st operand
        QtMDD* qtMDDObj = static_cast<QtMDD*>(operand);        
        MDDObj* currentMDDObj = qtMDDObj->getMDDObject();
        
        returnValue = computeOp(qtMDDObj);
        
        //delete the old operands
        if (operand)
        {
            operand->deleteRef();
        }
    }

    return returnValue;
}

QtData*
QtCorridor::computeOp(QtMDD* operand)
{
    // get the MDD object
    MDDObj* op = operand->getMDDObject();
    //  get the source domain
    r_Minterval areaOp = operand->getLoadDomain();
    // for error checking, we will need the dimension of the source domain.
    const r_Dimension opDim = areaOp.dimension();

    for(auto iter = maskDims.begin(); iter != maskDims.end(); iter++)
    {
        // is the dimension value outside the expected range?
        if(*iter >= opDim || *iter < 0)
        {
            // throw an error
            LFATAL << "Error: QtCorridor::computeOp() - The coordinate projections must correspond to existing axes.";                        
            parseInfo.setErrorNo(AXISNUMBERSMUSTEXIST);
            throw parseInfo;            
        }
    }
    
    // check the mask dimension is the same as the size of projection result.
    if(mask.second->dimension() != maskDims.size())
    {
        // throw an error
        LFATAL << "Error: QtCorridor::computeOp() - The number of coordinates projected to must match the dimension of the mask.";                        
        parseInfo.setErrorNo(PROJDIMNOTMATCHINGMASKDIM);
        throw parseInfo;         
    }
        
    //build the corridor for the result object.
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset( extractCorridor(op, areaOp) );
    
    if(!resultMDD)
    {
        LFATAL << "Error: QtCorridor::computeOp() - Failed for an unknown reason. Please contact the development team with the full query and the sdom of the MDD operand.";
        parseInfo.setErrorNo(CLIPERRORUNDEFINED);
        throw parseInfo;
    }
    
    QtData* returnValue = new QtMDD( resultMDD.release() );
    
    return returnValue;
}

const QtTypeElement& QtCorridor::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {

        // get input types
        const QtTypeElement &inputType = input->checkType(typeTuple);
        
        if (inputType.getDataType() != QT_MDD)
        {
            LFATAL << "Error: QtCorridor::checkType() - first operand must be of type MDD.";
            parseInfo.setErrorNo(MDDARGREQUIRED);
            throw parseInfo;
        }

        dataStreamType = inputType;
    }
    else
    {
        LERROR << "Error: QtCorridor::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}

