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

static const char rcsid[] = "@(#)qlparser, QtCurtain: $Id: qtcurtain.cc,v 1.47 2018/03/29 11:13:27 coman Exp $";

#include "qlparser/qtcurtain.hh"

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
#ifndef CPPSTDLIB
#else
#include <string>
#include <cmath>
#endif

#include <iostream>

// constructor for QtCurtain

const QtNode::QtNodeType QtCurtain::nodeType = QtNode::QT_CURTAIN;

QtCurtain::QtCurtain(QtOperation* mddOp, 
                     std::shared_ptr<QtMulticlipping> clipArg, 
                     QtMShapeData* projDims)
    : QtUnaryOperation(mddOp), clipping(clipArg)
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
            LFATAL << "Error: QtCurtain::QtCurtain() - The coordinate projections must be singular values separated by commas.";            
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


MDDObj*
QtCurtain::extractCurtain(const MDDObj* op, const r_Minterval& areaOp)
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
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), resultDomain.dimension());
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);
    TypeFactory::addTempType(mddBaseType);
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset( new MDDObj(mddBaseType, resultDomain, op->getNullValues()) );
    
    // pointer to all source tiles
    std::unique_ptr< std::vector< boost::shared_ptr<Tile> > > allTiles;
    allTiles.reset(op->intersect(resultDomain));

    // iterate over source tiles    
    for(auto srcTileIter = allTiles->begin(); srcTileIter != allTiles->end(); srcTileIter++)
    {
        // source tile domain
        r_Minterval srcArea((*srcTileIter)->getDomain());
        
        // subset of the source tile's domain hitting the curtain
        // this is the result tile's domain as well!
        r_Minterval aoiSrc(srcArea.trim_along_slice(convexHull, maskDims));
        
        // get the type and type size of the data
        const BaseType* baseType = (*srcTileIter)->getType();
        r_Bytes typeSize = baseType->getSize();
        
        // generate the result tile and initialize its contents to 0
        boost::shared_ptr<Tile> resTile;
        resTile.reset( new Tile(aoiSrc, baseType) );
        // result data pointer
        char* resDataPtr = resTile->getContents();
        op->fillTileWithNullvalues(resDataPtr, aoiSrc.cell_count());
        
        //memset(resDataPtr, 0, typeSize*aoiSrc.cell_count());
        
        // starting point for iterating through slices in the source tile
        r_Point aoiSrcOrigin = aoiSrc.get_origin();
        
        // initial slice to be iterated through in r_MiterArea
        r_Minterval aoiSrcSlice(aoiSrc.dimension());
        for(r_Dimension i = 0; i < aoiSrc.dimension(); i++)
        {
            auto it = std::find(maskDims.begin(), maskDims.end(), i);
            if(it == maskDims.end())
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
        
        while(!srcAoiIter.isDone())
        {
            r_Minterval aoiSlice = srcAoiIter.nextArea();
            
            //src tile data iterator -- iterates just over the relevant area in the source tile
            r_Miter srcIter( &aoiSlice, &srcArea, typeSize, (*srcTileIter)->getContents());
            //res tile data iterator -- iterates just over the relevant area in the result tile
            r_Miter resIter( &aoiSlice, &aoiSrc, typeSize, resTile->getContents());
            //mask arg data iterator -- iterates just over the relevant area in the mask
            r_Miter maskIter(&aoiMask, &convexHull, sizeof(char), mask.first.get());
            
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
        }

        resultMDD->insertTile(resTile);
    }

    return resultMDD.release();
}

QtData*
QtCurtain::evaluate(QtDataList* inputList)
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
QtCurtain::computeOp(QtMDD* operand)
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
            LFATAL << "Error: QtCurtain::computeOp() - The coordinate projections must correspond to existing axes.";                        
            parseInfo.setErrorNo(AXISNUMBERSMUSTEXIST);
            throw parseInfo;            
        }
    }
    
    // check the mask dimension is the same as the size of projection result.
    if(mask.second->dimension() != maskDims.size())
    {
        // throw an error
        LFATAL << "Error: QtCurtain::computeOp() - The number of coordinates projected to must match the dimension of the mask.";                        
        parseInfo.setErrorNo(PROJDIMNOTMATCHINGMASKDIM);
        throw parseInfo;         
    }
        
    //build the curtain for the result object.
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset( extractCurtain(op, areaOp) );
    
    if(!resultMDD)
    {
        LFATAL << "Error: QtCurtain::computeOp() - Failed for an unknown reason. Please contact the development team with the full query and the sdom of the MDD operand.";
        parseInfo.setErrorNo(CLIPERRORUNDEFINED);
        throw parseInfo;
    }
    
    QtData* returnValue = new QtMDD( resultMDD.release() );
    
    return returnValue;
}

const QtTypeElement& QtCurtain::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {

        // get input types
        const QtTypeElement &inputType = input->checkType(typeTuple);
        
        if (inputType.getDataType() != QT_MDD)
        {
            LFATAL << "Error: QtCurtain::checkType() - first operand must be of type MDD.";
            parseInfo.setErrorNo(MDDARGREQUIRED);
            throw parseInfo;
        }

        dataStreamType = inputType;
    }
    else
    {
        LERROR << "Error: QtCurtain::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}

