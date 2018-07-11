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

#ifndef _QTCLIPPINGUTIL_
#define _QTCLIPPINGUTIL_

//#ifndef CPPSTDLIB
//#include <ospace/string.h> // STL<ToolKit>
//#else
#include <string>
//#endif

#include "raslib/point.hh"
#include "raslib/pointdouble.hh"
#include "raslib/mddtypes.hh"
#include "raslib/error.hh"
#include "qlparser/qtmshapedata.hh"

#include <vector>
#include <set>

using namespace std;

struct classcomp
{
    bool operator()(const r_Point &x, const r_Point &y) const;
};

typedef struct BoundingBox
{
    // properties of the struct
    r_PointDouble minPoint;
    r_PointDouble maxPoint;
    r_PointDouble bBoxSizes;

    // constructor
    BoundingBox( r_PointDouble minP, r_PointDouble maxP, r_PointDouble bSize) : minPoint(minP), maxPoint(maxP), bBoxSizes(bSize)
    {    };
    
    // return the respective r_Minterval
    r_Minterval getHull()
    {
        r_Minterval retVal(minPoint.dimension());
        for (r_Dimension i = 0; i < minPoint.dimension(); i++)
        {
            r_Sinterval rs(static_cast<r_Range>(minPoint[i]), static_cast<r_Range>(maxPoint[i]));
            retVal << rs;
        }
        return retVal;
    };
} BoundingBox;

/* Generates the vertice coordinates relevant to the new basis. This is later to be used to generate
   the bounding box 


  Get the bounding box of the given polygon i.e. the point with min coordinates on all axes and the point
  with max coordinates on all axes
 */

/// compute the box in which the polytope lies
BoundingBox* computeBoundingBox(const QtMShapeData* mshape);


/// compute the box in which the polytope lies
BoundingBox* computeBoundingBoxFromList(vector<r_Point>& vertices);

/// check if a given point is in the given affine subspace defined by the vertices of mshape
pair<double, bool> isInNSubspace(const r_Point& position, QtMShapeData* mshape);

/// given a point in the bounding box, the function computes the position of that point in 
/// accordance with the bounding box size.
int computePosition(const r_Point& bBoxSize, const r_Point& current);

int computeOffset(const r_Point& genExtents, const r_Point& pointOne, const r_Point& pointTwo);

/// given a position, computes the distance to the linear subspace in the last dimension
/// this is done by finding the value of t for A(x + t*e_n) = b, where A & b are given by
/// QtMShape's hyperplaneEquations and x is the "currentPosition". The convention is to search
/// for solutions by scan rays starting at x and going in the positive e_n direction

/// first = initial offset; second = total # cells to produce
/// convention: first = -1          --> no solutions (0 = 1)
///             second = std::max   --> infinitely many solutions (0 = 0)
std::pair<int, int> computeStepsToSkip(const r_Point& currentPosition, const r_Point& boundingPosition, QtMShapeData* mshape, r_Dimension boxDim);

// appends index vectors to nSubspace corresponding to a BLA performed on the two vertices in mshape
// the BLA gives indices, not offsets. So the offsets are computed int he output method.
vector<r_Point> computeNDBresenhamLine(QtMShapeData* mshape);

// performs the same BLA as above, but with a pair of points as an argument, and without order swapping, for more flexible usage.
vector<r_Point> computeNDBresenhamSegment(const std::vector<r_PointDouble>& polytopeVertices);

//functor for determining redundancy of r_Mintervals in a vector of r_Mintervals.
bool isRedundant(const r_Minterval& interval);

// returns a pair consisting of the vector of extrapolated linestring data and a vector consisting of the domains of each segment.
std::pair< std::vector< std::vector< r_Point > >, std::vector< r_Minterval> > computeLinestring(QtMShapeData* linestringData);
// returns a pair consisting of teh vector of non-extrapolated linestring data and a vector consisting of the domain of the full linestring.
// meant to be used in place of the above in discrete corridors.
pair< vector< vector< r_Point > >, vector< r_Minterval > > computeDiscreteLinestring(QtMShapeData* lineStringData);

// builds a vector of pairs of r_PointDouble to be passed sequentially into computeNDBresenhamSegments, using computeNDBresenhamSegment
vector< vector< r_PointDouble > > vectorOfPairsWithoutMultiplicity(const std::vector<r_PointDouble>& polytopeVertices, size_t numSteps);

// constructs a vector of 1-D r_Mintervals given a vector of bounding boxes and their corresponding longest dimensions
// this method removes point duplicity on the edges. Primarily used for linestrings
vector< r_Minterval > vectorOfResultTileDomains(const std::vector<r_Minterval>& bBoxes, const std::vector<r_Dimension>& projectionDims );

//generate a vector of tiles with the given domains and base type, initialized to 0.
//std::vector<boost::shared_ptr<Tile>> initializeTileVector(const std::vector<r_Minterval>& resTileDomains, const BaseType* resTileBasetype);

// takes the result of computeNDBresenhamLine or computeNDBresenhamSegment and an r_Minterval to find the index of the positions of the first and last points in a segment contained within the r_Minterval
// note: this method assumes that lineSegmentArg exhibits monotonicity
std::pair<int, int> endpointsSearch(const r_Minterval& domainArg, const std::vector<r_Point>& lineSegmentArg);

// takes a pair and returns the smallest  r_Minterval containing the end points given by lineSegmentArg[first] and lineSegmentArg[second]
r_Minterval localHull(const std::pair<int, int>& indices, const std::vector<r_Point>& lineSegmentArg);
r_Sinterval localHullByIndex(const std::pair<int, int>& indices, const std::vector<r_Point>& lineSegmentArg, size_t index);

/// computes the r_Minterval of the projected subspace.
r_Minterval computeProjectedMinterval(QtMShapeData* mshape, BoundingBox* bBox, r_PointDouble* indexToRemove, std::set<r_Dimension, std::less<r_Dimension>>& projectionDimensionSet);

/// given an r_Minterval, computes the new r_Minterval i.e. the projection of the first according to the projectionDimensionSet.
/// dim is the number of dimensions of the first input r_Minterval
r_Minterval computeProjectedDomain(r_Minterval intersectDom, std::set<r_Dimension, std::less<r_Dimension>> projectionDimensionSet, r_Dimension dim);

/// given an r_Point in the parent set, a projectionDimensionSet, and the number of dimensions of the input, we find the projected range r_Point
/// 
r_Point computeProjectedPoint(const r_Point& pointOp, const std::vector<r_Dimension>& keptDims);

#endif
