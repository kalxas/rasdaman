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
 * File:   qtpolytopeclipping.hh
 * Author: bbell
 *
 * Created on September 11, 2017, 4:13 PM
 */

#ifndef QTPOLYTOPECLIPPING_HH
#define QTPOLYTOPECLIPPING_HH

#include "qlparser/qtpolygonclipping.hh"

class QtPolytope : public QtBinaryOperation
{
public:
    // constructor takes an mddObj and a matrix of features.
    //
    // features:    in 2-D, these are vertices
    //              in 3-D, these are faces (so, polygons embedded in $\mathbb{R}^3$)
    //              in 4-D, these are polyhedra
    //              ... and so on
    //
    // essentially, the goal of the constructor is to do nothing in 2-D
    // and to process the features into halfspace inequalities in higher dimensions
    QtPolytope(QtOperation *mddOp, QtOperation *pointOp);

    // a helper function for retrieving the halfspace inequalities for a polytope
    // not used in 2-D
    std::vector<double> getHalfspace(QtOperation *pointOp);

    // either a 2-D or an n-D clipping method is used based on the underlying MDDObj
    QtData *computeOp(QtMDD *operand, std::vector< std::vector<double>> halfspacesArg);

    // method for evaluating the node
    QtData *evaluate(QtDataList *inputList);

    // method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    // type checking of the subtree
    virtual const QtTypeElement &checkType(QtTypeTuple *typeTuple = NULL);

private:

    //attribute for identification of nodes
    static const QtNodeType nodeType;

    //this matrix stores the data corresponding to the affine hyperplanes.
    // our convention is that the last coefficients in each row correspond to
    // the constants, $b_i$,  in:
    // $Ax \leq b$
    //
    // i.e. "x" is suppressed while the coefficients of "A" and "b" are given
    // below as an augmented matrix.
    //
    // # rows = dimension of MDDObj
    // # cols = number of features
    std::vector< std::vector<double>> halfspaces;
};

#endif  /* QTPOLYTOPECLIPPING_HH */

