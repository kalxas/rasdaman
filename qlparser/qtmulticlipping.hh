
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
 * File:   qtmulticlipping.hh
 * Author: bbell
 *
 * Created on February 12, 2018, 18:03 PM
 */

#ifndef _QTMULTICLIPPING_
#define	_QTMULTICLIPPING_

#include "config.h"

#include <string>

#include "qlparser/qtunaryoperation.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtmshapedata.hh"
#include "qlparser/qtclippingutil.hh"
#include "qlparser/qtpolygonclipping.hh"
#include "qlparser/qtpolygonutil.hh"
#include "raslib/minterval.hh"
#include "catalogmgr/ops.hh"

#include <map>

#ifndef CPPSTDLIB
#else
#include <cmath>
#endif

#include <iostream>

/*
 * Class QtMulticlipping is similar to QtClipping; however, it is unary and takes a vector of QtMShapeData for variable initialization, parsed at query time.
 * 
 * 
 */

class QtMulticlipping : public QtUnaryOperation
{
  public:
      
    enum QtMulticlipType
    {
        CLIP_MULTIPOLYGON,
        CLIP_MULTILINESTRING
    };
    
    QtMulticlipping(QtOperation* mddOp, const std::vector<QtMShapeData*>& mshapeOp, QtMulticlipType ct);

    QtData* computeOp(QtMDD* operand);

    MDDObj* extractMultipolygon(const r_Minterval& areaOp, const MDDObj* op);
    
    /// method for evaluating the node
    QtData* evaluate(QtDataList* inputList);

    /// method for identifying nodes
    inline virtual QtNodeType getNodeType() const;

    /// type checking of the subtree
    virtual const QtTypeElement &checkType(QtTypeTuple *typeTuple = NULL);

  private:
    
    std::vector<QtMShapeData*> mshapeList;
      
    /// attribute for identifying the type of clipping to be performed
    QtMulticlipType clipType;
    
    /// attribute for identifying nodes
    static const QtNodeType nodeType;
};

#include "qlparser/qtmulticlipping.icc"

#endif	/* _QTMULTICLIPPING_ */

