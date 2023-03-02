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
 * MERCHANTrABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

#ifndef _QTCONCAT__
#define _QTCONCAT__

#include <string>

#include "qlparser/qtnaryoperation.hh"
#include "raslib/point.hh"

class BaseType;
class MDDObj;
class QtMDD;

class QtConcat : public QtNaryOperation
{
public:
    /// constructor getting operand list and dimension
    QtConcat(QtOperationList *opList, unsigned int dim);

    /// test if the two nodes have an equal meaning in a subtree
    virtual bool equalMeaning(QtNode *node);

    /// creates a unique name for a common subexpression
    virtual std::string getSpelling();

    /// simplifies the tree
    virtual void simplify();

    /// method for evaluating the node
    QtData *evaluate(QtDataList *inputList);
    /**
     */

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// type checking of the subtree
    virtual const QtTypeElement &checkType(QtTypeTuple *typeTuple = NULL);

    /// test if the edge to the parent node is of type mdd or atomic
    virtual QtAreaType getAreaType();

    /// getter for the dimension along which the concatenation is performed
    inline unsigned int getDimension();

private:
    // process the i-th operand and insert the new tiles into resultMDD
    void processOperand(unsigned int i, QtMDD *qtMDDObj, MDDObj *resultMDD,
                        const BaseType *baseType, const std::vector<r_Point> &tVector);

    /// attribute for identification of nodes
    static const QtNodeType nodeType;

    /// the dimension along which the concatenation is performed
    unsigned int dimension;
};

#include "qlparser/qtconcat.icc"

#endif
