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
 * File:   qtpolygonclipping.hh
 * Author: bbell
 *
 * Created on September 11, 2017, 4:39 PM
 */

//#include "catalogmgr/ops.hh"

//@ManMemo: Module: {\bf qlparser}

//The class represents a polygon clipping operation on MDD objects.

//this class summarizes and builds upon the work of Vlad Frasineanu from his Bachelor's thesis

#ifndef QTPOLYGONCLIPPING_HH
#define	QTPOLYGONCLIPPING_HH

#include "qlparser/qtbinaryoperation.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtpolygonutil.hh"


class QtPolygonClipping
{
public:
    /// constructor getting the mShape (collection of points) and a bounding box.
    QtPolygonClipping(const r_Minterval& areaOp, const std::vector<r_Point>& vertices);

    // computes the polygon clipping in 2D using Bresenham
    // fast and draws boundaries; currently used for lines, but works in general
    MDDObj* compute2D_Bresenham(MDDObj* op, MDDObj* mddres, r_Dimension dim);    
    
    // computes the polygon clipping in 2D using ray intersection counting 
    // also fast and can be adapted to float or double for vertices OR interior points
    // ref: https://wrf.ecse.rpi.edu//Research/Short_Notes/pnpoly.html
    MDDObj* compute2D_Rays(MDDObj* op, MDDObj* mddres, r_Dimension dim);
    
    // just returns the 2D mask on the full domain, computed using the Bresenham approach above.
    vector< vector<char> > generateMask();

private:
    /// the area of interest
    r_Minterval domain;    
    
    /// the vector of vertices
    vector<r_Point> polygonVertices;
};

#endif	/* QTPOLYGONCLIPPING_HH */

