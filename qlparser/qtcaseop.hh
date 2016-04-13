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
 * PURPOSE: 
 *      Defines the rasql CASE statement for scalar and induces cases.
 *      The following syntax applies:
 *              SELECT CASE WHEN condition1 THEN result1
 *                          WHEN condition2 THEN result2
 *                          ...
 *                          WHEN conditionN THEN resultN
 *                          ELSE resultDefault
 *                     END
 *              FROM collectionName
 * 
 * COMMENTS:
 *      The ELSE clause is mandatory.
 * 
 ************************************************************/

#ifndef QTCASEOP_HH
#define	QTCASEOP_HH

#include <boost/shared_ptr.hpp>

#include "config.h"
#include "qlparser/qtnaryoperation.hh"
#include "relcatalogif/basetype.hh"
#include "qtvariable.hh"
#include "qtmdd.hh"

class QtCaseOp : public QtNaryOperation {
public:
    /// constructor getting the operand list
    QtCaseOp(QtOperationList* opList);

    virtual ~QtCaseOp();

    /// method for evaluating the node
    QtData* evaluate(QtDataList* inputList);

    /// method for evaluating the induced node
    QtData* evaluateInducedOp(QtDataList* inputList);

    /// prints the tree
    virtual void printTree(int tab, std::ostream& s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream& s = std::cout);

    /// method for identification of nodes
    virtual QtNodeType getNodeType() const;

    /// type checking of the subtree
    virtual const QtTypeElement& checkType(QtTypeTuple* typeTuple = NULL);

    /// type checking for the induced method
    virtual const QtTypeElement& checkTypeInducedOp(QtTypeTuple* typeTuple = NULL);

    /// breaks down the operand list into conditions and results
    void getCaseOperands(QtDataList* inputList, std::vector< std::pair <QtOperation*, QtDataList* > >* cacheList,
            std::vector<std::pair <QtOperation*, QtData* > >* scalarCache,
            QtDataList* conditionList, QtOperationList* resultList, QtOperation* &defaultResult);
    
    /// type coercion: given 2 types, computes the resulting type 
    const BaseType* getResultType(const BaseType* op1, const BaseType* op2);
    
    /// indicates if a type is signed or not
    int isSignedType(const BaseType* type);
    
    /// method for cell-by-cell evaluation
    QtData* evaluateCellByCell(QtDataList* inputList, QtOperation* currentOperation, 
            std::vector<Tile*>* currentTiles, std::vector<char*>* cachedPoints);
    
    /// method for retrieving the cached data list corresponding to an operation
    QtDataList* getCachedData(QtOperation* op,  std::vector< std::pair <QtOperation*, QtDataList* > >* cacheList);
    
    /// method for retrieving cached scalar values
    QtData* getCachedScalar(QtOperation* op, std::vector< std::pair<QtOperation*, QtData*> >* scalarCacheList);
    
    /// method for restoring the tree to the original structure
    void restoreTree();
    
    /// method for retrieving a tile from an unsorted vector
    /// deletes the passed tiles pointer
    boost::shared_ptr<Tile> getCorrespondingTile(std::vector< boost::shared_ptr<Tile> >* tiles, const r_Minterval& domain);
    
    /// method for adding the list of corresponding mdd objects of an operation to cache
    void addMddsToCache(QtDataList* inputList, QtOperation* &op, 
            std::vector< std::pair <QtOperation*, QtDataList* > >* cacheList);
    
private:
    /// attribute for identification of nodes
    static const QtNodeType nodeType;

    /// attribute for indicating whether it's an induced case over arrays
    bool inducedCase;
    
    /// attribute for indicating the base type of the result
    BaseType* baseType; 
    
    /// attribute for storing the nodes removed from the tree
    std::vector< std::pair< QtNode*, std::pair<QtNode*, QtNode*> > > removedNodes;

protected:
    /// attribute storing the condition list
    QtOperationList* conditionList;
};

#endif	/* QTCASEOP_HH */

