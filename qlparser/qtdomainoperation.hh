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
#ifndef _QTDOMAINOPERATION_
#define _QTDOMAINOPERATION_

#include <string>

#include "qlparser/qtunaryoperation.hh"
#include "qlparser/qtmintervaldata.hh"
#include "raslib/sinterval.hh"
#include "raslib/minterval.hh"

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

The class represents a spatial domain operation, e.g. trimming or projections.

*/

class QtDomainOperation : public QtUnaryOperation
{
public:
    /// constructor getting an minterval expression
    QtDomainOperation(QtOperation *mintOp);
    ///Constructor in case of positionally-independent subsetting in rasql
    QtDomainOperation(QtOperation *mintOp, std::vector<std::string> *axisNames2);
    /// constructor
    QtDomainOperation(r_Minterval domainNew, const std::vector<bool> *newTrimFlags);

    /// destructor
    virtual ~QtDomainOperation();

    /// return childs of the node
    virtual QtNodeList *getChilds(QtChildType flag);

    /// test if the two nodes have an equal meaning in a subtree
    virtual bool equalMeaning(QtNode *node);

    /// creates a unique name for a common subexpression
    virtual std::string getSpelling();

    /// method for query rewrite
    virtual void setInput(QtOperation *inputOld, QtOperation *inputNew);

    /// optimizing load access
    using QtUnaryOperation::optimizeLoad;
    void optimizeLoad(QtTrimList *trimList, std::vector<r_Minterval> *trimIntervals);

    /// evaluates the node
    QtData *evaluate(QtDataList *inputList);

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    //@Man: Read/Write methods:
    //@{
    ///

    ///
    inline virtual void setInput(QtOperation *newInput);
    ///
    inline virtual void setMintervalOp(QtOperation *miop);
    ///
    inline QtOperation *getMintervalOp();

    ///
    //@}

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// type checking of the subtree
    virtual const QtTypeElement &checkType(QtTypeTuple *typeTuple = NULL);

private:
    /// pointer to an minterval expression
    QtOperation *mintervalOp{NULL};

    /// the flag determines if the minterval expression has to be calculated at runtime or not
    bool dynamicMintervalExpression;
    /// The flag determines whether the subbsetting is positionally dependent or not(if not, axes names are provided)
    bool namedAxisFlag;
    std::vector<std::string> *axisNames;
    std::vector<std::string> *axisNamesCorrect;

    /// attribute for identification of nodes
    static const QtNodeType nodeType;
};

#include "qlparser/qtdomainoperation.icc"

#endif
