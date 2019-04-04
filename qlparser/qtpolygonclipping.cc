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

//#include "qlparser/qtmdd.hh"
//#include "qlparser/qtatomicdata.hh"
//#include "qlparser/qtcomplexdata.hh"
//#include "qlparser/qtnode.hh"
//
//#include "mddmgr/mddobj.hh"
//#include "tilemgr/tile.hh"
//
//#include "catalogmgr/typefactory.hh"
//#include "relcatalogif/structtype.hh"
//#include "relcatalogif/mdddimensiontype.hh"
//#include "relcatalogif/syntaxtypes.hh"

#include <logging.hh>

//#include "qlparser/qtpointdata.hh"


//#include <sstream>
#ifndef CPPSTDLIB
#else
#include <string>
#endif

//#include <iostream>
#include "raslib/minterval.hh"
#include "raslib/point.hh"
#include "raslib/miter.hh"
#include "qlparser/qtmdd.hh"
#include "relcatalogif/mdddimensiontype.hh"

QtPolygonClipping::QtPolygonClipping(const r_Minterval &areaOp, const std::vector<r_Point> &vertices)
    :  domain(areaOp), polygonVertices(vertices)
{

}

QtPolygonClipping::QtPolygonClipping()
{
    //initialize domain & polygonVertices for use by QtPositiveGenusClipping
}

vector< vector<char>>
                   QtPolygonClipping::generateMask(bool toFill)
{
    //2-D array of type char "mask" for marking which cells are in the polygon and which are outside
    vector< vector<char>> mask(static_cast<long unsigned int>(domain[0].get_extent()),
                               vector<char>(static_cast<long unsigned int>(domain[1].get_extent()), 2));

    //translate polygon vertices into mask coordinates
    for (unsigned int i = 0; i < polygonVertices.size(); i++)
    {
        polygonVertices[i][0] = polygonVertices[i][0] - domain[0].low();
        polygonVertices[i][1] = polygonVertices[i][1] - domain[1].low();
    }

    //fill the polygon edges in the mask
    rasterizePolygon(mask, polygonVertices, toFill);

    //fill the connected components of the complement of the polygon
    if (toFill)
    {
        polygonInteriorFloodfill(mask, polygonVertices);
    }

    return mask;
}

const r_Minterval
QtPolygonClipping::getDomain() const
{
    return domain;
}

/*
 * Below, we define the mask generation methods for a polygon clipping containing interior regions.
 *
 * be mindful not to apply this to an empty vector!
 */

QtPositiveGenusClipping::QtPositiveGenusClipping(const r_Minterval &areaOp, const std::vector<QtMShapeData *> &polygonArgs)
{
    setDomain(areaOp);

    if (!polygonArgs.empty())
    {
        //the convention here is that the first MShape is the outer polygon, while the inner polygons are the subsequent MShapes
        interiorPolygons.reserve(polygonArgs.size() - 1);

        for (auto iter = polygonArgs.begin(); iter != polygonArgs.end(); iter++)
        {
            if (iter != polygonArgs.begin())
            {
                interiorPolygons.emplace_back(QtPolygonClipping((*iter)->convexHull(), (*iter)->getPolytopePoints()));
            }
            else
            {
                setPolygonVertices((*iter)->getPolytopePoints());
            }
        }
    }
}


vector< vector<char>>
                   QtPositiveGenusClipping::generateMask(bool toFill)
{
    //2-D array of type char "mask", initialized to the mask of the outer polygon
    vector< vector<char>> mask = QtPolygonClipping::generateMask(toFill);

    //make the data contiguous
    r_Minterval thisDomain = getDomain();
    std::unique_ptr<char[]> resultMask(new char[thisDomain.cell_count()]);
    char *resPtr = resultMask.get();
    const char *resultMaskPtr = &resultMask[0];

    for (size_t i = 0; i < static_cast<size_t>(thisDomain[0].get_extent()); i++)
    {
        for (size_t j = 0; j < static_cast<size_t>(thisDomain[1].get_extent()); j++)
        {
            *resPtr = mask[i][j];
            resPtr++;
        }
    }

    for (auto iter = interiorPolygons.begin(); iter != interiorPolygons.end(); iter++)
    {
        //then, we drill polygonally-shaped holes in the polygon
        if (thisDomain.covers(iter->getDomain()))
        {
            vector< vector<char>> polygonMask = iter->generateMask(toFill);
            r_Minterval interiorDomain = iter->getDomain();
            r_Miter resultMaskIter(&interiorDomain, &thisDomain, sizeof(char), resultMaskPtr);
            for (size_t m = 0; m < polygonMask.size(); m++)
            {
                for (size_t n = 0; n < polygonMask[m].size(); n++)
                {
                    char *thisCell = resultMaskIter.nextCell();

                    //case 1: we are interior of the outer polygon
                    if (*thisCell == 0)
                    {
                        //we are also in the interior of the inner polygon, so we remove this point from the mask
                        if (polygonMask[m][n] == 0)
                        {
                            *thisCell = 3;
                        }
                        //otherwise, we do nothing.
                    }
                    //case 2: we must either be on the boundary of the outer polygon, or in the exterior,
                    //        at which point we must not encounter any interior points of the interior polygon.
                    else if (polygonMask[m][n] == 0)
                    {
                        throw r_Error(POLYGONHOLEINEXTERIOR);
                    }
                }
            }
        }
        else
        {
            //throw an error, since an interior hole cannot leave the mask
            throw r_Error(POLYGONHOLEINEXTERIOR);
        }
    }

    //return to the noncontiguous state
    resPtr = &resultMask[0];
    for (size_t i = 0; i < static_cast<size_t>(thisDomain[0].get_extent()); i++)
    {
        for (size_t j = 0; j < static_cast<size_t>(thisDomain[1].get_extent()); j++)
        {
            mask[i][j] = *resPtr;
            resPtr++;
        }
    }

    return mask;
}