#ifndef _QTINFO_HH__
#define _QTINFO_HH___

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

#include "qlparser/qtunaryoperation.hh"

class QtVariable;


//@ManMemo: Module: {\bf qlparser}

/*@Doc:

  The class represents an oid node.

*/

class QtInfo : public QtUnaryOperation
{
public:
    /// constructor getting operand
    QtInfo(QtVariable *newInput);

    /// constructor getting operand and function params
    QtInfo(QtVariable *newInput, const char *params);

    /// evaluates the node
    QtData *evaluate(QtDataList *inputList);

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// type checking of the subtree
    virtual const QtTypeElement &checkType(QtTypeTuple *typeTuple = NULL);

private:
    /// attribute for identification of nodes
    static const QtNodeType nodeType;

    /// flag indicating whether to print tiles
    int printTiles;
};

#include "qlparser/qtinfo.icc"

#endif
