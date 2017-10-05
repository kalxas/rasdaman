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

#include "qlparser/qtpolygonclipping.hh"

#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtnode.hh"

#include "mddmgr/mddobj.hh"
#include "tilemgr/tile.hh"

#include "catalogmgr/typefactory.hh"
#include "relcatalogif/structtype.hh"
#include "relcatalogif/mdddimensiontype.hh"
#include "relcatalogif/syntaxtypes.hh"

#include <easylogging++.h>

#include "qlparser/qtpointdata.hh"

#include <sstream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

#include <iostream>
#include "rasodmg/polycutout.hh"

#include "qlparser/qtpolygonutil.hh"


const QtNode::QtNodeType QtPolygonClipping::nodeType = QtNode::QT_CLIPPING;

QtPolygonClipping::QtPolygonClipping(QtOperation* mddOp, QtOperation* pointOp)
    :  QtBinaryOperation(mddOp, pointOp)
{
}


bool
QtPolygonClipping::isCommutative() const
{
    return false; // NOT commutative
}

MDDObj* 
QtPolygonClipping::compute2D_Bresenham(MDDObj* op, r_Minterval areaOp, r_Point vertices, MDDObj* mddres, r_Dimension dim)
{
    vector<r_Point> polygon;
    polygon.reserve(vertices.dimension()/2);

    for( size_t i = 0; i < vertices.dimension(); i++ )
    {
        if( i%2 == 0 )
        {
            r_Point newPoint(vertices[i], vertices[i+1]);
            polygon.emplace_back(std::move(newPoint));
        }
    }

    pair<r_Point, r_Point> bBox = getBoundingBox( polygon );

    // create MDDObj for result
    // this should rather be MDDDomainType? -- DM 2011-aug-12
    //Old implementation was :MDDBaseType* mddBaseType = new MDDBaseType( "tmp", resultBaseType );
    //Had type incompatibility issue because of missing dimension.
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), dim);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    r_Sinterval xAxis(bBox.first[0], bBox.second[0]);
    r_Sinterval yAxis(bBox.first[1], bBox.second[1]);

    r_Minterval boundingBox(2);
    boundingBox << xAxis;
    boundingBox << yAxis;

    if( !areaOp.intersects_with(boundingBox) ){
        return NULL;
    }

    areaOp = areaOp.create_intersection(boundingBox);

    mddres = new MDDObj(mddBaseType, areaOp, op->getNullValues());

    //2-D array of type char "mask" for marking which cells are in the polygon and which are outside
    vector< vector<char> > mask(static_cast<long unsigned int>(bBox.second[0] - bBox.first[0] + 1), 
                                       vector<char>(static_cast<long unsigned int>(bBox.second[1] - bBox.first[1] + 1), 2));
    
    for( unsigned int i = 0; i < polygon.size(); i++ )
    {
        polygon[i][0] = polygon[i][0]-bBox.first[0];
        polygon[i][1] = polygon[i][1]-bBox.first[1];
    }
    //fill the polygon edges in the mask
    rasterizePolygon(mask, polygon);
    //fill the connected component of the complement of the polygon containing infinity
    fillOutsideOfPolygon(mask);
        
    // get all tiles in relevant area
    vector<boost::shared_ptr<Tile>>* allTiles = op->getTiles();

    Tile* resTile = NULL;
    // iterate over the tiles
    try 
    {
        for (auto tileIt = allTiles->begin(); tileIt !=  allTiles->end(); tileIt++)
        {
            resTile = NULL;
            // domain of the actual tile
            const r_Minterval& tileDom = (*tileIt)->getDomain();
            if( tileDom.intersects_with( areaOp ) )
            {
                // domain of the relevant area of the actual tile
                r_Minterval intersectDom(tileDom.create_intersection(areaOp));

                // create tile for result
                resTile = new Tile(intersectDom, op->getCellType());
                size_t typeSize = (*tileIt)->getType()->getSize();
                char* resultData = resTile->getContents();
                long unsigned int index = 0;
                
                for( auto i = intersectDom[0].low(); i <= intersectDom[0].high(); i++ )
                {
                    // locate the data pointed to in the i-th row and j-th column of the op tile
                    // as there may be a nontrivial intersection
                    char* rowOfData = (*tileIt)->getCell(r_Point(i, intersectDom[1].low()));
                    
                    for( auto j = intersectDom[1].low(); j <= intersectDom[1].high(); j++ )
                    {
                        if( i >= bBox.first[0] && i <= bBox.second[0]
                            && j >= bBox.first[1] && j <= bBox.second[1] )
                        {
                            if( mask[static_cast<size_t>(i-bBox.first[0])][static_cast<size_t>(j-bBox.first[1])] != 0 )
                            {
                                memcpy(resultData, rowOfData, typeSize);
                            }
                            index++;
                        }
                        // move to the next cell of the op tile
                        rowOfData += typeSize;
                        // move to the next cell of the result tile
                        resultData += typeSize;
                    }
                }
                // insert Tile in result mdd
                mddres->insertTile(resTile);
            }
        }
    } 
    catch (r_Error& err) 
    {
        LFATAL << "QtCLipping::compute2D caught " << err.get_errorno() << " " << err.what();
        delete allTiles;
        allTiles = NULL;
        //contents of allTiles are deleted when index is deleted
        delete mddres;
        mddres = NULL;
        delete resTile;
        resTile = NULL;
        delete mddres;
        mddres = NULL;
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    } 
    catch (int err) 
    {
        LFATAL << "QtPolygonClipping::compute2D caught errno error (" << err << ") in qtclipping";
        delete allTiles;
        allTiles = NULL;
        //contents of allTiles are deleted when index is deleted
        delete mddres;
        mddres = NULL;
        delete resTile;
        resTile = NULL;
        delete mddres;
        mddres = NULL;
        parseInfo.setErrorNo(static_cast<unsigned int>(err));
        throw parseInfo;
    }
    return mddres;
}

MDDObj* 
QtPolygonClipping::compute2D_Rays(MDDObj* op, r_Minterval areaOp, r_Point vertices, MDDObj* mddres, r_Dimension dim)
{
    vector< std::pair<int, int> > polygon1;
    polygon1.reserve(vertices.dimension()/2);

    for( size_t i = 0; i < vertices.dimension(); i++ )
    {
        if( i%2 == 0 )
        {
            std::pair<int, int> newPoint(vertices[i], vertices[i+1]);
            polygon1.emplace_back(std::move(newPoint));
        }
    }
    
    
    vector<r_Point> polygon;
    polygon.reserve(vertices.dimension()/2);

    for( size_t i = 0; i < vertices.dimension(); i++ )
    {
        if( i%2 == 0 )
        {
            r_Point newPoint(vertices[i], vertices[i+1]);
            polygon.emplace_back(std::move(newPoint));
        }
    }
    
    pair<r_Point, r_Point> bBox = getBoundingBox( polygon );

    // create MDDObj for result
    // this should rather be MDDDomainType? -- DM 2011-aug-12
    //Old implementation was :MDDBaseType* mddBaseType = new MDDBaseType( "tmp", resultBaseType );
    //Had type incompatibility issue because of missing dimension.
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), dim);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    r_Sinterval xAxis(bBox.first[0], bBox.second[0]);
    r_Sinterval yAxis(bBox.first[1], bBox.second[1]);

    r_Minterval boundingBox(2);
    boundingBox << xAxis;
    boundingBox << yAxis;

    if( !areaOp.intersects_with(boundingBox) ){
        return NULL;
    }

    areaOp = areaOp.create_intersection(boundingBox);

    mddres = new MDDObj(mddBaseType, areaOp, op->getNullValues());

    // get all tiles in relevant area
    vector<boost::shared_ptr<Tile>>* allTiles = op->getTiles();

    Tile* resTile = NULL;
    // iterate over the tiles
    try 
    {
        for (auto tileIt = allTiles->begin(); tileIt !=  allTiles->end(); tileIt++)
        {
            resTile = NULL;
            // domain of the actual tile
            const r_Minterval& tileDom = (*tileIt)->getDomain();
            if( tileDom.intersects_with( areaOp ) )
            {
                // domain of the relevant area of the actual tile
                r_Minterval intersectDom(tileDom.create_intersection(areaOp));

                // create tile for result
                resTile = new Tile(intersectDom, op->getCellType());
                size_t typeSize = (*tileIt)->getType()->getSize();
                char* resultData = resTile->getContents();
                long unsigned int index = 0;
                
                for( auto i = intersectDom[0].low(); i <= intersectDom[0].high(); i++ )
                {
                    // locate the data pointed to in the i-th row and j-th column of the op tile
                    // as there may be a nontrivial intersection
                    char* rowOfData = (*tileIt)->getCell(r_Point(i, intersectDom[1].low()));
                    
                    for( auto j = intersectDom[1].low(); j <= intersectDom[1].high(); j++ )
                    {
                        if( i >= bBox.first[0] && i <= bBox.second[0]
                            && j >= bBox.first[1] && j <= bBox.second[1] )
                        {
                            if(pnpoly(i, j, polygon1))
                            {
                                memcpy(resultData, rowOfData, typeSize);
                            }
                            index++;
                        }
                        // move to the next cell of the op tile
                        rowOfData += typeSize;
                        // move to the next cell of the result tile
                        resultData += typeSize;
                    }
                }
                // insert Tile in result mdd
                mddres->insertTile(resTile);
            }
        }
    } 
    catch (r_Error& err) 
    {
        LFATAL << "QtCLipping::compute2D caught " << err.get_errorno() << " " << err.what();
        delete allTiles;
        allTiles = NULL;
        //contents of allTiles are deleted when index is deleted
        delete mddres;
        mddres = NULL;
        delete resTile;
        resTile = NULL;
        delete mddres;
        mddres = NULL;
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    } 
    catch (int err) 
    {
        LFATAL << "QtPolygonClipping::compute2D caught errno error (" << err << ") in qtclipping";
        delete allTiles;
        allTiles = NULL;
        //contents of allTiles are deleted when index is deleted
        delete mddres;
        mddres = NULL;
        delete resTile;
        resTile = NULL;
        delete mddres;
        mddres = NULL;
        parseInfo.setErrorNo(static_cast<unsigned int>(err));
        throw parseInfo;
    }
    return mddres;
}

MDDObj* 
QtPolygonClipping::compute2D_Divide(MDDObj* op, r_Minterval areaOp, r_Point vertices, MDDObj* mddres, r_Dimension dim)
{
    vector<r_Point> polygon;
    polygon.reserve(vertices.dimension()/2);

    for( unsigned int i = 0; i < vertices.dimension(); i++ )
    {
        if( i%2 == 0 )
        {
            r_Point newPoint(vertices[i], vertices[i+1]);
            polygon.emplace_back(std::move(newPoint));
        }
    }

    pair<r_Point, r_Point> bBox = getBoundingBox( polygon );

    // create MDDObj for result
    // this should rather be MDDDomainType? -- DM 2011-aug-12
    //Old implementation was :MDDBaseType* mddBaseType = new MDDBaseType( "tmp", resultBaseType );
    //Had type incompatibility issue because of missing dimension.
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), dim);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    r_Sinterval xAxis(bBox.first[0], bBox.second[0]);
    r_Sinterval yAxis(bBox.first[1], bBox.second[1]);

    r_Minterval boundingBox(2);
    boundingBox << xAxis;
    boundingBox << yAxis;

    if( !areaOp.intersects_with(boundingBox) )
    {
        return NULL;
    }

    areaOp = areaOp.create_intersection(boundingBox);

    mddres = new MDDObj(mddBaseType, areaOp, op->getNullValues());

    //2-D array of type char "mask" for marking which cells are in the polygon and which are outside
    vector< vector<char> > mask(static_cast<long unsigned int>(bBox.second[0] - bBox.first[0] + 1), 
                                       vector<char>(static_cast<long unsigned int>(bBox.second[1] - bBox.first[1] + 1), 2));

    for( size_t i = 0; i < polygon.size(); i++ )
    {
        polygon[i][0] = polygon[i][0]-bBox.first[0];
        polygon[i][1] = polygon[i][1]-bBox.first[1];
    }

    checkSquare( r_Point(0,0), r_Point(bBox.second[0]-bBox.first[0], bBox.second[1]-bBox.first[1]), polygon, mask );


    // get all tiles in relevant area
    vector<boost::shared_ptr<Tile>>* allTiles = op->getTiles();
    
    Tile* resTile;
    // iterate over the tiles
    try {
        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++) {
            resTile = NULL;
            // domain of the actual tile
            const r_Minterval& tileDom = (*tileIt)->getDomain();
            if (tileDom.intersects_with(areaOp)) {
                // domain of the relevant area of the actual tile
                r_Minterval intersectDom(tileDom.create_intersection(areaOp));

                // create tile for result
                resTile = new Tile(intersectDom, op->getCellType());
                size_t typeSize = (*tileIt)->getType()->getSize();
                char* resultData = resTile->getContents();

                long unsigned int index = 0;
                for (auto i = intersectDom[0].low(); i <= intersectDom[0].high(); i++) 
                {
                    char* rowOfData = (*tileIt)->getCell(r_Point(i, intersectDom[1].low()));
                    for (auto j = intersectDom[1].low(); j <= intersectDom[1].high(); j++) 
                    {
                        if (i >= bBox.first[0] && i <= bBox.second[0] && j >= bBox.first[1] && j <= bBox.second[1]) 
                        {
                            if (mask[static_cast<size_t> (i - bBox.first[0])][static_cast<size_t> (j - bBox.first[1])] == 1) 
                            {
                                 memcpy(resultData, rowOfData, typeSize);
                            }
                            index++;
                        }
                        // move to the next cell of the op tile
                        rowOfData += typeSize;
                        // move to the next cell of the result tile
                        resultData += typeSize;
                    }
                }
                // insert Tile in result mdd
                mddres->insertTile(resTile);
            }
        }
    } 
    catch (r_Error& err) 
    {
        LFATAL << "QtCLipping::compute2D caught " << err.get_errorno() << " " << err.what();
        delete allTiles;
        allTiles = NULL;
        //contents of allTiles are deleted when index is deleted
        delete mddres;
        mddres = NULL;
        delete resTile;
        resTile = NULL;
        delete mddres;
        mddres = NULL;
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    } 
    catch (int err) 
    {
        LFATAL << "QtPolygonClipping::compute2D caught errno error (" << err << ") in qtclipping";
        delete allTiles;
        allTiles = NULL;
        //contents of allTiles are deleted when index is deleted
        delete mddres;
        mddres = NULL;
        delete resTile;
        resTile = NULL;
        delete mddres;
        mddres = NULL;
        parseInfo.setErrorNo(static_cast<unsigned int> (err));
        throw parseInfo;
    }
    return mddres;
}

MDDObj* 
QtPolygonClipping::compute3D_Divide(MDDObj* op, r_Minterval areaOp, r_Point vertices, MDDObj* mddres, r_Dimension dim)
{
    vector<r_Point> polygon;
    polygon.reserve(vertices.dimension()/3);

    for( unsigned int i = 0; i < vertices.dimension(); i++ )
    {
        if( i%3 == 0 )
        {
            r_Point newPoint(vertices[i], vertices[i+1], vertices[i+2]);
            polygon.emplace_back(std::move(newPoint));
        }
    }

    pair<r_Point, r_Point> bBox = getBoundingBox( polygon );

    // create MDDObj for result
    // this should rather be MDDDomainType? -- DM 2011-aug-12
    //Old implementation was :MDDBaseType* mddBaseType = new MDDBaseType( "tmp", resultBaseType );
    //Had type incompatibility issue because of missing dimension.
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), dim);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    r_Sinterval xAxis(bBox.first[0], bBox.second[0]);
    r_Sinterval yAxis(bBox.first[1], bBox.second[1]);
    r_Sinterval zAxis(bBox.first[2], bBox.second[2]);

    r_Minterval boundingBox(3);
    boundingBox << xAxis;
    boundingBox << yAxis;
    boundingBox << zAxis;

    if( !areaOp.intersects_with(boundingBox) )
        return NULL;

    areaOp = areaOp.create_intersection(boundingBox);

    mddres = new MDDObj(mddBaseType, areaOp, op->getNullValues());

    map<r_Point,bool,classcomp> result;

    checkCube( bBox.first, bBox.second, polygon, result );

    map<r_Point,bool,classcomp>::iterator it;

    // get all tiles in relevant area
    vector<boost::shared_ptr<Tile>>* allTiles = op->getTiles();

    Tile* resTile;
    // iterate over them
    try 
    {
        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++) 
        {
            resTile = NULL;
            // domain of the actual tile
            const r_Minterval& tileDom = (*tileIt)->getDomain();

            if (tileDom.intersects_with(areaOp)) 
            {
                // domain of the relevant area of the actual tile
                r_Minterval intersectDom(tileDom.create_intersection(areaOp));

                // create tile for result
                resTile = new Tile(intersectDom, op->getCellType());
                size_t typeSize = (*tileIt)->getType()->getSize();
                char* resultData = resTile->getContents();
                long unsigned int index = 0;
                
                for (auto i = intersectDom[0].low(); i <= intersectDom[0].high(); i++) 
                {
                    for (auto j = intersectDom[1].low(); j <= intersectDom[1].high(); j++) 
                    {
                        char* rowOfData = (*tileIt)->getCell(r_Point(i, j, intersectDom[2].low()));
                        for (auto k = intersectDom[2].low(); k <= intersectDom[2].high(); k++) 
                        {
                            if (i >= bBox.first[0] && i <= bBox.second[0]
                                    && j >= bBox.first[1] && j <= bBox.second[1]
                                    && k >= bBox.first[2] && k <= bBox.second[2]) 
                            {
                                if (result.find(r_Point(i, j, k)) != result.end())
                                {
                                    //resTile->setCell(index, (*tileIt)->getCell(r_Point(i, j, k)));
                                    memcpy(resultData, rowOfData, typeSize);
                                }
                                index++;
                            }
                            // move to the next cell of the op tile
                            rowOfData += typeSize;
                            // move to the next cell of the result tile
                            resultData += typeSize;
                        }
                    }
                }
                // insert Tile in result mdd
                mddres->insertTile(resTile);
            }
        }
    } 
    catch (r_Error& err) 
    {
        LFATAL << "QtCLipping::computeOp3D caught " << err.get_errorno() << " " << err.what();
        delete allTiles;
        allTiles = NULL;
        //contents of allTiles are deleted when index is deleted
        delete mddres;
        mddres = NULL;
        delete resTile;
        resTile = NULL;
        delete mddres;
        mddres = NULL;
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    } 
    catch (int err) 
    {
        LFATAL << "QtPolygonClipping::computeOp3D caught errno error (" << err << ") in qtclipping";
        delete allTiles;
        allTiles = NULL;
        //contents of allTiles are deleted when index is deleted
        delete mddres;
        mddres = NULL;
        delete resTile;
        resTile = NULL;
        delete mddres;
        mddres = NULL;
        parseInfo.setErrorNo(static_cast<unsigned int> (err));
        throw parseInfo;
    }
    return mddres;
}

QtData* 
QtPolygonClipping::computeOp(QtMDD* operand, r_Point vertices)
{
    QtData* returnValue = NULL;
    // get the MDD object
    MDDObj* op = (static_cast<QtMDD*>(operand))->getMDDObject();
    
    //  get the area, where the operation has to be applied
    r_Minterval areaOp = (static_cast<QtMDD*>(operand))->getLoadDomain();
    
    const r_Dimension dim = areaOp.dimension();
    
    MDDObj* mddres = NULL;
    
    if (dim == 2)
    {
        //plots the connected component of the complement of the polygon boundary
        //not containing infinity
        if(vertices.dimension() > 4)
        {
            mddres = compute2D_Rays(op, areaOp, vertices, mddres, dim);
        }
        //at the moment, if we have only 2 vertices, we can plot the line segment
        else if(vertices.dimension() == 4)
        {
            mddres = compute2D_Bresenham(op, areaOp, vertices, mddres, dim);
        }
        else
        {
            throw r_Error(NOTENOUGHVERTICES);
        }
    }
    else if (dim == 3)
    {
        mddres = compute3D_Divide(op, areaOp, vertices, mddres, dim);
    }
    else
    {
        throw r_Error(CLIPNOTPOSSIBLEOVER3D);
    }
    
    // create a new QtMDD object as carrier object for the transient MDD object
    returnValue = new QtMDD(mddres);
    
    return returnValue;
}

QtData*
QtPolygonClipping::evaluate(QtDataList* inputlist)
{
    QtData* returnValue = NULL;
    QtData* operand1 = NULL;
    QtData* operand2 = NULL;
    
    // evaluate sub-nodes to obtain operand values
    if (getOperands(inputlist, operand1, operand2))
    {
        //
        // This implementation simply creates a new transient MDD object with the new
        // domain while copying the data. Optimization of this is left for future work.
        //

        QtMDD* qtMDDObj = static_cast<QtMDD*>(operand1);
        r_Point transPoint(1);

        // get transPoint
        if ( operand2->getDataType() == QT_POINT )
        {
            transPoint = (static_cast<QtPointData*>(operand2))->getPointData();
        }
        else
        {
            const BaseType* baseType = ((QtScalarData*) operand2)->getValueType();
            const char* data = ((QtScalarData*) operand2)->getValueBuffer();
            r_Long dataScalar = 0;
            transPoint << *baseType->convertToCLong(data, &dataScalar);
        }

        MDDObj* currentMDDObj = qtMDDObj->getMDDObject();

        r_Dimension points = transPoint.dimension();
        points = points / qtMDDObj->getLoadDomain().dimension();
        if ( points * qtMDDObj->getLoadDomain().dimension() != transPoint.dimension() )
        {
            // delete the old operands
            if (operand1)
            {
                operand1->deleteRef();
            }
            if (operand2)
            {
                operand2->deleteRef();
            }
            LFATAL << "Error: QtPolygonClipping::evaluate( QtDataList* ) - dimensionality of MDD and polygon vertices do not match.";
            parseInfo.setErrorNo(407);
            throw parseInfo;
        }
        returnValue = computeOp( qtMDDObj, transPoint);

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

const QtTypeElement& QtPolygonClipping::checkType(QtTypeTuple* typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input1 && input2)
    {

        // get input types
        const QtTypeElement& inputType1 = input1->checkType(typeTuple);
        const QtTypeElement& inputType2 = input2->checkType(typeTuple);

        if (inputType1.getDataType() != QT_MDD)
        {
            LFATAL << "Error: QtPolygonClipping::checkType() - first operand must be of type MDD.";
            parseInfo.setErrorNo(405);
            throw parseInfo;
        }

        // operand two can be a single long number, the parser does [a] -> number a,
        // rather than [a] -> point (which is then used in marray/condense..),
        // so we need to take care manually here of this edge case -- DM 2015-aug-24
        if (inputType2.getDataType() != QT_POINT && inputType2.getDataType() != QT_LONG)
        {
            LFATAL << "Error: QtPolygonClipping::checkType() - second operand must be of type Point.";
            parseInfo.setErrorNo(406);
            throw parseInfo;
        }

        // pass MDD type
        dataStreamType = inputType1;
    }
    else
    {
        LERROR << "Error: QtPolygonClipping::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}

QtNode::QtNodeType
QtPolygonClipping::getNodeType() const
{
  return nodeType;
}
