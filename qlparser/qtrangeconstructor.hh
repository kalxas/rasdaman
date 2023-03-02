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

#ifndef _QtRangeConstructor__
#define _QtRangeConstructor__

#include <string>
#include "qlparser/qtnaryoperation.hh"
#include "mddmgr/mddobj.hh"

class BaseType;

class QtRangeConstructor : public QtNaryOperation
{
public:
    /// constructor getting operand list
    QtRangeConstructor(QtOperationList *opList);

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

    /// type coercion
    const BaseType *getResultType(const BaseType *op1, const BaseType *op2);

    /// checks whether a type is signed or not (for type coercion)
    int isSignedType(const BaseType *type);

private:
    std::unique_ptr<MDDObj> getResultMDD(QtDataList *operandList);

    /// attribute for identification of nodes
    static const QtNodeType nodeType;

    /// true if this is a struct literal, instead of a proper range constructor
    bool complexLit;
};

#include "qlparser/qtrangeconstructor.icc"

#endif
