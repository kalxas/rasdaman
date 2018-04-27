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
/* 
 * File:   qtmulticlipping.cc
 * Author: bbell
 *
 * Created on February 12, 2018, 18:03 PM
 */

static const char rcsid[] = "@(#)qlparser, QtMulticlipping: $Id: qtmulticlipping.cc,v 1.47 2002/08/19 11:13:27 coman Exp $";

#include "qlparser/qtmulticlipping.hh"
#include "raslib/miter.hh"

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

#include <logging.hh>

#include "qlparser/qtpointdata.hh"
#include "qlparser/qtoperation.hh"

#include <sstream>
#ifndef CPPSTDLIB
#else
#include <string>
#include <cmath>
#include <array>
#endif

#include <iostream>

// constructor for QtMulticlipping

const QtNode::QtNodeType QtMulticlipping::nodeType = QtNode::QT_MULTICLIPPING;

QtMulticlipping::QtMulticlipping(QtOperation* mddOp, const std::vector<QtMShapeData*>& mshapeListArg, QtMulticlipType ct)
    : QtUnaryOperation(mddOp), clipType(ct)
{
    //TODO (bbell): make this more flexible in the future, so that point dimensionality need not be enforced, 
    //              and the parser will pick up the exact line & column numbers.
    r_Dimension opDim = 2;
    
    for(auto iter = mshapeListArg.begin(); iter != mshapeListArg.end(); iter++)
    {
        if (opDim != (*iter)->getDimension() && opDim != (*iter)->getPointDimension() )
        {
            LFATAL << "Error: QtMulticlipping::QtMulticlipping() - Dimension of the polygon vertices differs from the domain's dimension.";
            throw r_Error(507);
        }
    } 

    mshapeList.reserve(1);
    //add the next polygon w/ interiors to the vector
    mshapeList.emplace_back( QtPositiveGenusClipping(mshapeListArg[0]->convexHull(), mshapeListArg) );
}

QtMulticlipping::QtMulticlipping(QtOperation* mddOp, const std::vector< std::vector<QtMShapeData*>* >& mshapeListArg, QtMulticlipType ct)
    : QtUnaryOperation(mddOp), clipType(ct)
{
    r_Dimension opDim = 2;
    
    for(auto shapeIter = mshapeListArg.begin(); shapeIter != mshapeListArg.end(); shapeIter++)
    {
        for(auto iter = (*shapeIter)->begin(); iter != (*shapeIter)->end(); iter++)
        {
            if (opDim != (*iter)->getDimension() && opDim != (*iter)->getPointDimension() )
            {
                LFATAL << "Error: QtMulticlipping::QtMulticlipping() - Dimension of the polygon vertices differs from the domain's dimension.";
                throw r_Error(507);
            }
        }
    }   
    
    mshapeList.reserve(mshapeListArg.size());
    for( auto iter = mshapeListArg.begin(); iter != mshapeListArg.end(); iter++)
    {
        //add the next polygon w/ interiors to the vector
        mshapeList.emplace_back( QtPositiveGenusClipping((*iter)->at(0)->convexHull(), **iter) );
    }
}

std::shared_ptr<r_Minterval> 
QtMulticlipping::buildResultDom(const r_Minterval& areaOp)
{
    //constructing the result domain for the mask and the resultmdd
    std::shared_ptr<r_Minterval> resultDom;
    //since we use this method both internall and externally, we don't always want to compute the result domain (it could be passed as areaOp)
    for(auto i = mshapeList.begin(); i != mshapeList.end(); i++)
    {
        if(i->getDomain().intersects_with(areaOp))
        {
            if(resultDom)
            {
                *resultDom = resultDom->closure_with(i->getDomain());
            }
            else
            {
                resultDom.reset( new r_Minterval(i->getDomain()) );
            }
        }
    }

    return resultDom;
}

std::shared_ptr<char>
QtMulticlipping::buildResultMask(std::shared_ptr<r_Minterval> resultDom)
{
    //result mask
    std::shared_ptr<char> resultMask;
    resultMask.reset(new char[resultDom->cell_count()]);
    memset(resultMask.get(), 2, resultDom->cell_count());
    
    //starting point of the mask, for iteration
    const char* resultMaskPtr = &resultMask.get()[0];
    
    for(auto iter = mshapeList.begin(); iter != mshapeList.end(); iter++)
    {
        if(iter->getDomain().intersects_with(*resultDom))
        {
            const r_Minterval* currentDomain = new r_Minterval(iter->getDomain());
            
            vector< vector<char> > polygonMask;
            
            if(clipType == CLIP_MULTIPOLYGON)
            {
                polygonMask = iter->generateMask(true);
            }
            else if(clipType == CLIP_MULTILINESTRING)
            {
                polygonMask = iter->generateMask(false);
            }
            
            r_Miter resultMaskIter(currentDomain, resultDom.get(), sizeof(char), resultMaskPtr);
            
            //this is the approach we take when combining positive genus polygonal masks
            for(size_t m = 0; m < polygonMask.size(); m++)
            {
                for(size_t n = 0; n < polygonMask[m].size(); n++)
                {
                    if(polygonMask[m][n] < 2) //copy the new mask's polygon into the overall mask
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

std::pair< std::shared_ptr<char>, std::shared_ptr<r_Minterval> >
QtMulticlipping::buildAbstractMask()
{
    
    //builds the result domain without worrying about an area of interest
    std::shared_ptr<r_Minterval> resultDom;
    for(auto i = mshapeList.begin(); i != mshapeList.end(); i++)
    {
        if(resultDom)
        {
            *resultDom = resultDom->closure_with(i->getDomain());
        }
        else
        {
            resultDom.reset( new r_Minterval(i->getDomain()) );
        }
    }     
     
    //result mask & domain
    
    std::pair< std::shared_ptr<char>, std::shared_ptr<r_Minterval> > retVal(buildResultMask(resultDom), resultDom);
    
    return retVal;
}

MDDObj*
QtMulticlipping::extractMultipolygon(const r_Minterval& areaOp, const MDDObj* op)
{   
    //for each clipping in the vector, we generate a mask, 
    //and we assemble them into a single result mask

    //constructing the result domain for the mask and the resultmdd
    std::shared_ptr<r_Minterval> resultDom = buildResultDom(areaOp);
    
    //in case nothing of interest was hit, we can simply return to the parent method, where we will throw an error.
    if(!resultDom)
    {
        return NULL;
    }
    
    std::shared_ptr<char> resultMask = buildResultMask(resultDom);
    
    //gennerate resultMDD
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), 3);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset( new MDDObj(mddBaseType, *resultDom, op->getNullValues()) );
    
    // here, we apply the resultMask to each tile to generate the output tiles.
    
    //iterate over the source tiles
    std::unique_ptr<std::vector<boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->intersect(*resultDom));
    try
    {
        //data type size
        size_t typeSize = (*(allTiles->begin()))->getType()->getSize();

        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++)
        {
            //domain of source tile
            const r_Minterval& srcTileDom = (*tileIt)->getDomain();
            //data pointer of source tile
            const char* sourceDataPtr = (*tileIt)->getContents();
            
            //construct result tile
            boost::shared_ptr<Tile> resTile;
            r_Minterval intersectDom = resultDom->create_intersection(srcTileDom);
            resTile.reset( new Tile(intersectDom, op->getCellType()) );
            
            const char* resDataPtr = resTile->getContents();
            
            //initialize the tile to be filled with nullValues.
            op->fillTileWithNullvalues(const_cast<char*>(resDataPtr), intersectDom.cell_count());
            
            //construct iterators for filling data in result tile
            r_Miter resTileMaskIterator(&intersectDom, resultDom.get(), sizeof(char), resultMask.get());
            r_Miter sourceTileIterator(&intersectDom, &srcTileDom, typeSize, sourceDataPtr);
            r_Miter resTileIterator(&intersectDom, &intersectDom, typeSize, resDataPtr);
            
            while(!resTileMaskIterator.isDone())
            {
                //step to next cell for resTileMaskIterator
                if(*resTileMaskIterator.nextCell() < 2)
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
        LFATAL << "QtClipping::extractMultipolygon caught " << err.get_errorno() << " " << err.what();
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }
    catch (int err)
    {
        LFATAL << "QtClipping::extractMultipolygon caught errno error (" << err << ") in qtmulticlipping";
        parseInfo.setErrorNo(err);
        throw parseInfo;
    }

    
    return resultMDD.release();
}

QtData*
QtMulticlipping::evaluate(QtDataList* inputList)
{
    QtData* returnValue = NULL;
    
    // get the operand
    QtData* operand = input->evaluate( inputList );
    
    // evaluate sub-nodes to obtain operand values
    if (operand)
    {
        // source mdd object in the 1st operand
        QtMDD* qtMDDObj = static_cast<QtMDD*>(operand);        
        MDDObj* currentMDDObj = qtMDDObj->getMDDObject();
        
        r_Dimension opDim = qtMDDObj->getLoadDomain().dimension();
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
QtMulticlipping::computeOp(QtMDD* operand)
{
    // get the MDD object
    MDDObj* op = operand->getMDDObject();
    //  get the source domain
    r_Minterval areaOp = operand->getLoadDomain();
         
    //extract the multipolygon data as a transient MDD object for the result.
    std::unique_ptr<MDDObj> resultMDD;
    
    if(clipType == CLIP_MULTIPOLYGON)
    {
    resultMDD.reset( extractMultipolygon(areaOp, op) );
    }
    else if(clipType == CLIP_POSITIVEGENUS)
    {
//        resultMDD.reset( extractPositiveGenus(areaOp, op) );
    }
    if(!resultMDD)
    {
        parseInfo.setErrorNo(ALLPOLYGONSOUTSIDEMDDOBJ);
        throw parseInfo;
    }
    
    QtData* returnValue = new QtMDD( resultMDD.release() );
    
    return returnValue;
}

const QtTypeElement&
QtMulticlipping::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {

        // get input types
        const QtTypeElement &inputType = input->checkType(typeTuple);

        if (inputType.getDataType() != QT_MDD)
        {
            LFATAL << "Error: QtMulticlipping::checkType() - first operand must be of type MDD.";
            parseInfo.setErrorNo(MDDARGREQUIRED);
            throw parseInfo;
        }

        dataStreamType = inputType;
    }
    else
    {
        LERROR << "Error: QtMulticlipping::checkType() - operand branch not a valid MDD.";
    }

    return dataStreamType;
}