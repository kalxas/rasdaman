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
/*************************************************************
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#ifndef _QTMSHAPEDATA_
#define _QTMSHAPEDATA_

#include "qlparser/qtdata.hh"

#include "raslib/point.hh"
#include "qlparser/qtpointdata.hh"
#include "raslib/pointdouble.hh"

#include <iostream>
#include <string>
#include <vector>
#include <memory>

// removed deprecated ostrsteam -- PB 2005-jan-14
// #include <strstream.h>

//@ManMemo: Module: {\bf qlparser}

/*@Doc:
  The class encapsulates a MShape of type \Ref{r_MShape}.
*/


class QtMShapeData : public QtData
{
public:

    // constructor getting the vertices
    QtMShapeData(vector<r_Point> &pts);
    /// constructor getting a QtMShape
    QtMShapeData(vector<QtMShapeData*> &mshapes);

    /// virtual destructor
    virtual ~QtMShapeData();

    //@Man: Read/Write methods:
    //@{
    ///

    ///
    inline const std::vector<r_PointDouble>&  getMShapeData();
    ///
    inline void setMShapeData(std::vector<r_PointDouble> &pts);

    /// returns a null-terminated string describing the type structure
    virtual char* getTypeStructure() const;
    /**
      The string MShapeer has to be free using free() by the caller.
    */

    ///
    //@}

    /// returns {\tt QT_MShape}
    virtual QtDataType getDataType() const;

    /// compares data content
    virtual bool equal(const QtData* obj) const;

    /// returns content dependent string representation
    virtual std::string getSpelling() const;

    /// print status of the object to the specified stream
    virtual void printStatus(std::ostream& stream = std::cout) const;

    /// get the dimension of the space the object lies into (how many vectors define this space)
    r_Dimension getDimension();

    /// get the orthonormal vectors defining the space the object lies into.
    inline std::vector<r_PointDouble>* getDirectionVectors();

    /// compute the barycentre of the vertices that define the object.
    r_PointDouble* computeMidPoint();

    /// compute the directionVectors and the dimensionality of the space in which this mshape lives.
    void computeDimensionality();

    /// computes the hyperplaneEquations if they are not yet, and returns them otherwise
    std::vector<std::pair< r_PointDouble, double> > computeHyperplaneEquation();
private:
    /// attribute storing the poligon vertices coordinates
    vector<r_Point>  polytopePoints;
    
    /// barycentre of the polytope vertices.
    r_PointDouble* midPoint;
    
    /// contains data defining the n-1 dim facets
    vector<QtMShapeData*> edgePolytopes;
    
    /// polytope Vertex data; converted to double points to maintain precision.
    std::vector<r_PointDouble>   polytopePointsDouble;

    /// vectors representing the space in which the mshape lives.
    /// the first #dimensionality vectors form an orthonormal basis of the affine
    /// subspace while the latter ones form an orthonormal basis of the complement
    std::vector<r_PointDouble>  directionVectors;

    /// dimension of the affine subspace
    /// also represents the loop starting point of the basis of the orthogonal complement
    /// of the affine subspace in directionVectors.
    r_Dimension dimensionality;

    /// equations defining either an H-Polytope or a linear subspace given by 
    /// the intersection of codim-many hyperplanes
    std::vector<std::pair<r_PointDouble, double> > hyperplaneEquations; 
};

#include "qlparser/qtmshapedata.icc"

#endif