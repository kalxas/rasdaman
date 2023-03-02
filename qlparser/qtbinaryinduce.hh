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
#ifndef _QTBINARYINDUCE_
#define _QTBINARYINDUCE_

#include <string>

#include "qlparser/qtbinaryoperation.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtscalardata.hh"

#include "catalogmgr/ops.hh"
class Tile;

//@ManMemo: Module: {\bf qlparser}

/*@Doc:
The class hierarchy guarantees no type safety. Type checking is done at
execution time.

The operations greater and greater equal are mapped to QtLess and QtLessEqual.
*/

class QtBinaryInduce : public QtBinaryOperation
{
public:
    /// constructor getting the two operands
    QtBinaryInduce(QtOperation *input1, QtOperation *input2, Ops::OpType initOpType);

    /// method for evaluating the node
    QtData *evaluate(QtDataList *inputList);

    /// type checking of the subtree
    virtual const QtTypeElement &checkType(QtTypeTuple *typeTuple = NULL);

    /// computes a binary induce operation on two MDD objects
    static QtData *computeBinaryMDDOp(QtMDD *operand1, QtMDD *operand2, const BaseType *resultBaseType, BinaryOp *myOp);
    static std::vector<std::shared_ptr<Tile>> computeBinaryMDDOpOneTile(
        const std::shared_ptr<Tile> &tileOp1, const std::unique_ptr<std::vector<std::shared_ptr<Tile>>> &tileOp2,
        const r_Point &offset12, const r_Point &offset21, const r_Minterval &areaOp1,
        const BaseType *resultBaseType, BinaryOp *myOp, bool fullTiles, unsigned int &capturedErrCode);
    /**
      The method carries out the binary induce operation specified by <tt>operation</tt> on the two operands. For
      the result, a new transient MDD object is created and returned. In the end, the MDD objects of the operands
      are freed.
    */

protected:
    /// computes the binary operation
    QtData *computeOp(QtData *operand1, QtData *operand2);

    /// computes an unary induce operation with one MDD object and a scalar value either being atomic or complex constant
    QtData *computeUnaryMDDOp(QtMDD *operand1, QtScalarData *operand2, const BaseType *resultBaseType, int scalarPos = 1);
    /**
      The method carries out the unary induce operation specified by <tt>operation</tt> on the two operands. <tt>scalarPos </tt>
      determines if the scalar value is the first (=1, default) or the second (=2) operand in the operation. For
      the result, a new transient MDD object is created and returned. In the end, the MDD object of the first operand
      is freed.
    */

    /// computes a binary operation on two scalar objects
    QtData *computeBinaryOp(QtScalarData *operand1, QtScalarData *operand2, const BaseType *resultBaseType);
    /**
      The method carries out the binary operation specified by <tt>operation</tt> on the two operands.
    */

    //  private:
    // type of operation
    Ops::OpType opType;

private:
    /// atribute for identification of nodes
    static const QtNodeType nodeType;
};

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

*/

class QtPlus : public QtBinaryInduce
{
public:
    /// constructor getting the two operands
    QtPlus(QtOperation *input1, QtOperation *input2);

    ///for associative law
    virtual QtOperation *getUniqueOrder(const QtNode::QtNodeType ID);

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

private:
    /// attribute for identification of nodes
    static const QtNodeType nodeType;
};

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

*/

class QtMinus : public QtBinaryInduce
{
public:
    /// constructor getting the two operands
    QtMinus(QtOperation *input1, QtOperation *input2);

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// returns FALSE saying that the operation IS NOT commutative
    virtual bool isCommutative() const;

private:
    /// attribute for identification of nodes
    static const QtNodeType nodeType;
};

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

*/

class QtMax_binary : public QtBinaryInduce
{
public:
    /// constructor getting the two operands
    QtMax_binary(QtOperation *input1, QtOperation *input2);

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

private:
    /// attribute for identification of nodes
    static const QtNodeType nodeType;
};

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

*/

class QtMin_binary : public QtBinaryInduce
{
public:
    /// constructor getting the two operands
    QtMin_binary(QtOperation *input1, QtOperation *input2);

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

private:
    /// attribute for identification of nodes
    static const QtNodeType nodeType;
};

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

*/

class QtMult : public QtBinaryInduce
{
public:
    /// constructor getting the two operands
    QtMult(QtOperation *input1, QtOperation *input2);

    /// optimizes the tree
    virtual QtOperation *getUniqueOrder(const QtNode::QtNodeType ID);

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

private:
    /// attribute for identification of nodes
    static const QtNodeType nodeType;
};

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

*/

class QtDiv : public QtBinaryInduce
{
public:
    /// constructor getting the two operands
    QtDiv(QtOperation *input1, QtOperation *input2);

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// returns FALSE saying that the operation IS NOT commutative
    virtual bool isCommutative() const;

private:
    /// attribute for identification of nodes
    static const QtNodeType nodeType;
};

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

*/

class QtIntDiv : public QtBinaryInduce
{
public:
    /// constructor getting the two operands
    QtIntDiv(QtOperation *input1, QtOperation *input2);

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// returns FALSE saying that the operation IS NOT commutative
    virtual bool isCommutative() const;

private:
    /// attribute for identification of nodes
    static const QtNodeType nodeType;
};

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

*/

class QtMod : public QtBinaryInduce
{
public:
    /// constructor getting the two operands
    QtMod(QtOperation *input1, QtOperation *input2);

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// returns FALSE saying that the operation IS NOT commutative
    virtual bool isCommutative() const;

private:
    /// attribute for identification of nodes
    static const QtNodeType nodeType;
};

#include "qlparser/qtbinaryinduce.icc"

#endif
