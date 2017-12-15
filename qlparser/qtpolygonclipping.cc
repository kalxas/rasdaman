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
#else
#include <string>
using namespace std;
#endif

#include <iostream>
#include "rasodmg/polycutout.hh"

QtPolygonClipping::QtPolygonClipping(const r_Minterval& areaOp, const std::vector<r_Point>& vertices)
    :  domain(areaOp), polygonVertices(vertices)
{
    
}

MDDObj* 
QtPolygonClipping::compute2D_Bresenham(MDDObj* op, MDDObj* mddres, r_Dimension dim)
{
    pair<r_Point, r_Point> bBox = getBoundingBox( polygonVertices );

    // create MDDObj for result
    // this should rather be MDDDomainType? -- DM 2011-aug-12
    //Old implementation was :MDDBaseType* mddBaseType = new MDDBaseType( "tmp", resultBaseType );
    //Had type incompatibility issue because of missing dimension.
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), dim);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    r_Sinterval xAxis(bBox.first[0], bBox.second[0]);
    r_Sinterval yAxis(bBox.first[1], bBox.second[1]);

    r_Minterval convexHull(2);
    convexHull << xAxis;
    convexHull << yAxis;

    if( !domain.intersects_with(convexHull) ){
        return NULL;
    }

    domain = domain.create_intersection(convexHull);

    mddres = new MDDObj(mddBaseType, domain, op->getNullValues());

    //2-D array of type char "mask" for marking which cells are in the polygon and which are outside
    vector< vector<char> > mask(static_cast<long unsigned int>(bBox.second[0] - bBox.first[0] + 1), 
                                       vector<char>(static_cast<long unsigned int>(bBox.second[1] - bBox.first[1] + 1), 2));
    
    //translate polygon vertices into mask coordinates
    for( unsigned int i = 0; i < polygonVertices.size(); i++ )
    {
        polygonVertices[i][0] = polygonVertices[i][0]-bBox.first[0];
        polygonVertices[i][1] = polygonVertices[i][1]-bBox.first[1];
    }
    //fill the polygon edges in the mask
    rasterizePolygon(mask, polygonVertices);
    //fill the connected component of the complement of the polygon containing infinity
    //TODO (bbell): Consider replacing with floodfill for more-robust handling of edge-cases..
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
            if( tileDom.intersects_with( domain ) )
            {
                // domain of the relevant area of the actual tile
                r_Minterval intersectDom(tileDom.create_intersection(domain));

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
                            if( mask[static_cast<size_t>(i-bBox.first[0])][static_cast<size_t>(j-bBox.first[1])] != 0 )
                            {
                                memcpy(resultData, rowOfData, typeSize);
                            }
                            index++;
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
        throw err;
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
        throw r_Error(err);
    }
    delete allTiles;
    return mddres;
}

MDDObj* 
QtPolygonClipping::compute2D_Rays(MDDObj* op, MDDObj* mddres, r_Dimension dim)
{
   
    pair<r_Point, r_Point> bBox = getBoundingBox( polygonVertices );

    // create MDDObj for result
    // this should rather be MDDDomainType? -- DM 2011-aug-12
    //Old implementation was :MDDBaseType* mddBaseType = new MDDBaseType( "tmp", resultBaseType );
    //Had type incompatibility issue because of missing dimension.
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), dim);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    r_Sinterval xAxis(bBox.first[0], bBox.second[0]);
    r_Sinterval yAxis(bBox.first[1], bBox.second[1]);

    r_Minterval convexHull(2);
    convexHull << xAxis;
    convexHull << yAxis;

    if( !domain.intersects_with(convexHull) ){
        return NULL;
    }

    domain = domain.create_intersection(convexHull);

    mddres = new MDDObj(mddBaseType, domain, op->getNullValues());

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
            if( tileDom.intersects_with( domain ) )
            {
                // domain of the relevant area of the actual tile
                r_Minterval intersectDom(tileDom.create_intersection(domain));

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
                            if(isPointInsidePolygon(i, j, polygonVertices))
                            {
                                memcpy(resultData, rowOfData, typeSize);
                            }
                            index++;
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
        throw err;
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
        throw r_Error(err);
    }
    delete allTiles;
    return mddres;
}