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
#ifndef _QTBINARYOPERATION_
#define _QTBINARYOPERATION_

#include <string>
#include <stdio.h>
#include "qlparser/qtoperation.hh"

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

The class serves as superclass for all operation classes taking two
arguments.

*/

class QtBinaryOperation : public QtOperation
{
public:
    /// default constructor
    QtBinaryOperation();

    /// constructor getting the node to the parent
    QtBinaryOperation(QtNode *node);

    /// constructor getting pointers to its operands
    QtBinaryOperation(QtOperation *input1, QtOperation *input2);

    /// virtual destructor
    virtual ~QtBinaryOperation();

    /// simplifies the tree
    virtual void simplify();

    /// test if the two nodes have an equal meaning in the query tree
    virtual bool equalMeaning(QtNode *node);
    /**
      The meaning of a binary operation is equal, iff both operands have
      the same meaning. In case of a commutative operation, the operands
      can be switched.
    */

    /// return childs of the node
    virtual QtNodeList *getChilds(QtChildType flag);

    /// creates a unique name for a subexpression
    virtual std::string getSpelling();

    /// test if the edge to the parent node is of type mdd or atomic
    virtual QtAreaType getAreaType();

    /// method for query rewrite
    inline virtual void setInput(QtOperation *inputOld, QtOperation *inputNew);

    /// optimizing load access
    virtual void optimizeLoad(QtTrimList *trimList);
    /**
      The method deletes the given <tt> trimList</tt> and passes the <tt> optimizeLoad</tt>
      message with empty triming lists to its input trees. The method is rewritten
      by some subclasses.
    */

    /// debugging method
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    //@Man: read/write methods for the operands
    //@{
    ///
    ///
    inline void setInput1(QtOperation *input);
    ///
    inline void setInput2(QtOperation *input);
    ///
    inline QtOperation *getInput1();
    ///
    inline QtOperation *getInput2();
    ///
    //@}

    /// returns commutativity information (by default, an operation IS commutative)
    virtual bool isCommutative() const;

protected:
    /// method for testing and evaluating the input branches
    bool getOperands(QtDataList *inputList, QtData *&operand1, QtData *&operand2);
    /**
      The method checks if the input branches are valid. Then it passes the evaluate message to its two
      operands with the <tt> inputList</tt> as argument. The returned results are provided through the arguments
      <tt> operand1</tt> and <tt> operand2</tt> called by reference. The method returns <tt> true</tt> it the operands are
      valid, otherwise <tt> false</tt>.
    */

    /// method for testing and evaluating the input branches
    bool getOperand(QtDataList *inputList, QtData *&operand1, int number);
    /**
      The method checks if the by number specified input branch si valid. Then it passes the evaluate message to the
      operand with the <tt> inputList</tt> as argument. The returned result are provided through the argument
      <tt> operand</tt> called by reference. The method returns <tt> true</tt> it the operand is
      valid, otherwise <tt> false</tt>.
    */

    /// first operation operand
    QtOperation *input1;
    /// second operation operand
    QtOperation *input2;
};

#include "qlparser/qtbinaryoperation.icc"

#endif
