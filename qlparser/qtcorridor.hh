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
 * File:   qtcorridor.hh
 * Author: bbell
 *
 * Created on May 22, 2018, 5:00 PM
 */

#ifndef QTCORRIDOR_HH
#define	QTCORRIDOR_HH

#include "config.h"

#include <string>

#include "qlparser/qtunaryoperation.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtmshapedata.hh"
#include "qlparser/qtclippingutil.hh"
#include "qlparser/qtpolygonclipping.hh"
#include "qlparser/qtpolygonutil.hh"
#include "qlparser/qtmulticlipping.hh"
#include "raslib/minterval.hh"
#include "catalogmgr/ops.hh"

#ifndef CPPSTDLIB
#else
#include <cmath>
#endif

#include <iostream>

class QtCorridor : public QtUnaryOperation
{
  public:     
  
    //constructor getting the mdd being operated on, the mask generator (QtMulticlipping object), and the projection dimensions containing the mask.
    QtCorridor(QtOperation* mddOp, std::shared_ptr<QtMulticlipping> clipArg,  QtMShapeData* lineString, QtMShapeData* projDims);
    
    QtData* computeOp(QtMDD* operand);

    MDDObj* extractCorridor(const MDDObj* op, const r_Minterval& areaOp);
    
    /// method for evaluating the node
    QtData* evaluate(QtDataList* inputList);

    /// method for identifying nodes
    inline virtual QtNodeType getNodeType() const;

    /// type checking of the subtree
    virtual const QtTypeElement& checkType(QtTypeTuple *typeTuple = NULL);
    
  protected: 
    /// takes a vector of vector of line segment end points, which has already been reduced to avoid redundancies, and an r_Minterval assumed to lie along the maskDims (private member)
    /// returns a vector of the embedded intervals (translated by the point differences).
    std::vector<r_Minterval> computeMaskEmbedding(const std::vector< std::vector<r_Point> >& pointListArg, const r_Minterval& convexHull, r_Range outputLength); 
      
  private:
    /// attribute for identifying nodes
    static const QtNodeType nodeType;
    
    std::pair< std::shared_ptr<char>, std::shared_ptr<r_Minterval> >  mask;
    
    std::shared_ptr<QtMulticlipping> clipping;
    
    // the linestring data
    QtMShapeData* lineStringData;
    
    // list of mask dimensions for corridors
    std::vector<r_Dimension> maskDims;
};


#include "qlparser/qtcorridor.icc"

#endif	/* QTCORRIDOR_HH */

