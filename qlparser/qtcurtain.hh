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
 * File:   qtcurtain.hh
 * Author: bbell
 *
 * Created on February 1, 2018, 5:00 PM
 */

#ifndef QTCURTAIN_HH
#define	QTCURTAIN_HH

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

class QtCurtain : public QtUnaryOperation
{
  public:     
  
    //constructor getting the mdd being operated on, the mask generator (QtMulticlipping object), and the projection dimensions containing the mask.
    QtCurtain(QtOperation* mddOp, std::shared_ptr<QtMulticlipping> clipArg, QtMShapeData* projDims);
    
    QtData* computeOp(QtMDD* operand);

    MDDObj* extractCurtain(const MDDObj* op, const r_Minterval& areaOp);
    
    /// method for evaluating the node
    QtData* evaluate(QtDataList* inputList);

    /// method for identifying nodes
    inline virtual QtNodeType getNodeType() const;

    /// type checking of the subtree
    virtual const QtTypeElement& checkType(QtTypeTuple *typeTuple = NULL);
    
    void fillTileWithNullvalues( char* resDataPtr, const MDDObj* op, size_t cellCount);

  private:
    /// attribute for identifying nodes
    static const QtNodeType nodeType;
    
    std::pair< std::shared_ptr<char>, std::shared_ptr<r_Minterval> >  mask;
    
    std::shared_ptr<QtMulticlipping> clipping;
    // list of mask dimensions for curtains
    std::vector<r_Dimension> maskDims;
};


#include "qlparser/qtcurtain.icc"

#endif	/* QTCURTAIN_HH */

