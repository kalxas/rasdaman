#ifndef _QTUNARYFUNC_HH__
#define _QTUNARYFUNC_HH___

#include "qlparser/qtunaryoperation.hh"

// forward declarations
class QtOperation;

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


//@ManMemo: Module: {\bf qlparser}

/*@Doc:

  The class represenst a function returning the lower bound of an interval.

*/

class QtIntervalLoOp : public QtUnaryOperation
{
public:
    /// constructor getting operand
    QtIntervalLoOp(QtOperation *newInput);

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
};





//@ManMemo: Module: {\bf qlparser}

/*@Doc:

  The class represenst a function returning the higher bound of an interval.

*/

class QtIntervalHiOp : public QtUnaryOperation
{
public:
    /// constructor getting operand
    QtIntervalHiOp(QtOperation *newInput);

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
};




//@ManMemo: Module: {\bf qlparser}

/*@Doc:

  The class represenst a function returning the spatial domain of an mdd object.

*/

class QtSDom : public QtUnaryOperation
{
public:
    /// constructor getting operand
    QtSDom(QtOperation *newInput);

    /// evaluates the node
    QtData *evaluate(QtDataList *inputList);

    /// optimizing load access
    virtual void optimizeLoad(QtTrimList *trimList);
    /**
      The method deletes the given <tt>trimList</tt> and passes the <tt>optimizeLoad</tt>
      message with empty triming list to its operand tree.

      Optimization process enters a new MDD area.
    */

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// type checking
    virtual const QtTypeElement &checkType(QtTypeTuple *typeTuple = NULL);

private:
    /// attribute for identification of nodes
    static const QtNodeType nodeType;
};




//@ManMemo: Module: {\bf qlparser}

/*@Doc:

  The class represenst a function returning the spatial domain of
  an mdd object at a specified axis/dimension.

  The axis can be a number or a named axis.
  The result is always QtMintervalData.

*/

class QtAxisSDom : public QtUnaryOperation
{
public:
    /// constructor getting the two operands
    QtAxisSDom(QtOperation *mdd, r_Dimension axis);
    /// constructor getting the two operands, with a 'named' axis
    QtAxisSDom(QtOperation *mdd, const std::string &axis);

    /// method for evaluating the node
    QtData *evaluate(QtDataList *inputList);

    //@Man: Read/Write methods
    //@{
    ///

    /// set ParseInfo for ALONG clause
    inline void setAxisParseInfo(const ParseInfo &info);

    ///
    //@}

    /// optimizing load access
    virtual void optimizeLoad(QtTrimList *trimList);

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

    /// the axis
    r_Dimension axis;

    /// this flag determines whether the input axis is a name or number. true if name, false if number.
    bool namedAxisFlag  = false;
    /// user-provided axis name
    std::string axisName;
    /// actual names of the axes in the input array
    std::vector<std::string> *axisNamesCorrect;

    /// get the ParseInfo for the axis. In SORT op.: ALONG clause.
    ParseInfo AxisParseInfo;

    /// get the axis number from a given axis name
    void getAxisFromName();
};


#include "qlparser/qtunaryfunc.icc"

#endif

