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
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#include "config.h"
#include "raslib/rmdebug.hh"

#include "qlparser/qtbinaryinduce2.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtconst.hh"

#include "mddmgr/mddobj.hh"

#include "catalogmgr/typefactory.hh"

#include <logging.hh>

#include <iostream>
#include <string>
#include <vector>
using namespace std;

const QtNode::QtNodeType QtIs::nodeType = QT_IS;

QtIs::QtIs(QtOperation *initInput1, QtOperation *initInput2)
    :  QtBinaryInduce(initInput1, initInput2, Ops::OP_EQUAL)
{
}


void
QtIs::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtIs Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree(tab, s, mode);
}


void
QtIs::printAlgebraicExpression(ostream &s)
{
    s << "(";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << " is ";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}


const QtNode::QtNodeType QtAnd::nodeType = QT_AND;

QtAnd::QtAnd(QtOperation *initInput1, QtOperation *initInput2)
    :  QtBinaryInduce(initInput1, initInput2, Ops::OP_AND)
{
}


QtData *
QtAnd::evaluate(QtDataList *inputList)
{
    /*
    // RUNTIME OPTIMIZATION: FALSE AND A     -> MDD(FALSE)
    //                       A     AND FALSE -> MDD(FALSE)
    //                       TRUE  AND A     -> A
    //                       A     AND TRUE  -> A
    */
    startTimer("QtAnd");

    QtData *returnValue = NULL;

    if (input1->getDataStreamType().getDataType() == QT_BOOL &&
            input2->getDataStreamType().getDataType() == QT_BOOL)
    {
        // RUNTIME OPTIMIZATION: FALSE AND A -> FALSE
        //                       TRUE  AND A -> A

        QtData *operand1 = NULL;

        if (getOperand(inputList, operand1, 1))
        {
            bool op1 = (static_cast<QtAtomicData *>(operand1))->getUnsignedValue();

            if (op1)
            {
                // first operand is obsolete
                if (operand1)
                {
                    operand1->deleteRef();
                }

                QtData *operand2 = NULL;

                if (getOperand(inputList, operand2, 2))
                {
                    returnValue = operand2;
                }
            }
            else
            {
                returnValue = operand1;
                LTRACE <<  "   -> FALSE AND A evaluates FALSE";
            }
        }
    }
    else
    {
        returnValue = QtBinaryInduce::evaluate(inputList);
    }

    stopTimer();

    return returnValue;
}


void
QtAnd::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtAnd Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree(tab, s, mode);
}


void
QtAnd::printAlgebraicExpression(ostream &s)
{
    s << "(";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << " and ";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}



const QtNode::QtNodeType QtOr::nodeType = QT_OR;

QtOr::QtOr(QtOperation *initInput1, QtOperation *initInput2)
    :  QtBinaryInduce(initInput1, initInput2, Ops::OP_OR)
{
}


QtData *
QtOr::evaluate(QtDataList *inputList)
{
    /*
    // RUNTIME OPTIMIZATION: TRUE  OR  A     -> MDD(TRUE)
    //                       A     OR  TRUE  -> MDD(TRUE)
    //                       FALSE OR  A     -> A
    //                       A     OR  FALSE -> A
    */
    startTimer("QtOr");

    QtData *returnValue = NULL;

    if (input1->getDataStreamType().getDataType() == QT_BOOL &&
            input2->getDataStreamType().getDataType() == QT_BOOL)
    {
        // RUNTIME OPTIMIZATION: FALSE OR A -> A
        //                       TRUE  OR A -> TRUE

        QtData *operand1 = NULL;

        if (getOperand(inputList, operand1, 1))
        {
            bool op1 = (static_cast<QtAtomicData *>(operand1))->getUnsignedValue();

            if (!op1)
            {
                // first operand is obsolete
                if (operand1)
                {
                    operand1->deleteRef();
                }

                QtData *operand2 = NULL;

                if (getOperand(inputList, operand2, 2))
                {
                    returnValue = operand2;
                }
            }
            else
            {
                returnValue = operand1;
                LTRACE <<  "   -> TRUE OR A evaluates TRUE";
            }
        }
    }
    else
    {
        returnValue = QtBinaryInduce::evaluate(inputList);
    }

    stopTimer();

    return returnValue;
}


void
QtOr::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtOr Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree(tab, s, mode);
}


void
QtOr::printAlgebraicExpression(ostream &s)
{
    s << "(";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << " or ";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}



const QtNode::QtNodeType QtXor::nodeType = QT_XOR;


QtXor::QtXor(QtOperation *initInput1, QtOperation *initInput2)
    :  QtBinaryInduce(initInput1, initInput2, Ops::OP_XOR)
{
}


void
QtXor::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtXor Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree(tab, s, mode);
}


void
QtXor::printAlgebraicExpression(ostream &s)
{
    s << "(";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << " xor ";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}



const QtNode::QtNodeType QtEqual::nodeType = QT_EQUAL;

QtEqual::QtEqual(QtOperation *initInput1, QtOperation *initInput2)
    :  QtBinaryInduce(initInput1, initInput2, Ops::OP_EQUAL)
{
}



void
QtEqual::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtEqual Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree(tab, s, mode);
}



void
QtEqual::printAlgebraicExpression(ostream &s)
{
    s << "(";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << " = ";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}



const QtNode::QtNodeType QtLess::nodeType = QT_LESS;


QtLess::QtLess(QtOperation *initInput1, QtOperation *initInput2)
    :  QtBinaryInduce(initInput1, initInput2, Ops::OP_LESS)
{
}


bool
QtLess::isCommutative() const
{
    return false; // NOT commutative
}



void
QtLess::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtLess Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree(tab, s, mode);
}



void
QtLess::printAlgebraicExpression(ostream &s)
{
    s << "(";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << " < ";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}



const QtNode::QtNodeType QtLessEqual::nodeType = QT_LESS_EQUAL;


QtLessEqual::QtLessEqual(QtOperation *initInput1, QtOperation *initInput2)
    :  QtBinaryInduce(initInput1, initInput2, Ops::OP_LESSEQUAL)
{
}


bool
QtLessEqual::isCommutative() const
{
    return false; // NOT commutative
}



void
QtLessEqual::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtLessEqual Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree(tab, s, mode);
}


void
QtLessEqual::printAlgebraicExpression(ostream &s)
{
    s << "(";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << " <= ";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}



const QtNode::QtNodeType QtNotEqual::nodeType = QT_NOT_EQUAL;

QtNotEqual::QtNotEqual(QtOperation *initInput1, QtOperation *initInput2)
    :  QtBinaryInduce(initInput1, initInput2, Ops::OP_NOTEQUAL)
{
}



void
QtNotEqual::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtNotEqual Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree(tab, s, mode);
}



void
QtNotEqual::printAlgebraicExpression(ostream &s)
{
    s << "(";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << " != ";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}





const QtNode::QtNodeType QtOverlay::nodeType = QT_OVERLAY;

QtOverlay::QtOverlay(QtOperation *initInput1, QtOperation *initInput2)
    :  QtBinaryInduce(initInput1, initInput2, Ops::OP_OVERLAY)
{
}



bool QtOverlay::isCommutative() const
{
    return false; // NOT commutative
}



void
QtOverlay::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtOverlay Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree(tab, s, mode);
}



void
QtOverlay::printAlgebraicExpression(ostream &s)
{
    s << "(";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << " overlay ";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}

/********************************************************
 *  QtBit
 ********************************************************
 */


const QtNode::QtNodeType QtBit::nodeType = QT_BIT;

QtBit::QtBit(QtOperation *initInput1, QtOperation *initInput2)
    :  QtBinaryInduce(initInput1, initInput2, Ops::OP_BIT) {}


bool QtBit::isCommutative() const
{
    return false;
}

void QtBit::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtBit Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;
    QtBinaryInduce::printTree(tab, s, mode);
}

void QtBit::printAlgebraicExpression(ostream &s)
{
    s << "(";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << " bit ";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}

const QtTypeElement &QtBit::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input1 && input2)
    {

        // get input types
        const QtTypeElement &inputType1 = input1->checkType(typeTuple);
        const QtTypeElement &inputType2 = input2->checkType(typeTuple);

        if (RManDebug >= 4)
        {
            LTRACE << "Operand 1: ";
            inputType1.printStatus(RMInit::dbgOut);

            LTRACE << "Operand 2: ";
            inputType2.printStatus(RMInit::dbgOut);

            LTRACE << "Operation            " << opType;
        }

        if (inputType2.getDataType() < QT_BOOL || inputType2.getDataType() > QT_LONG)
        {
            LERROR << "Error: QtBit::checkType() - second operand must be of integral type.";
            parseInfo.setErrorNo(418);
            throw parseInfo;
        }

        if (inputType1.getDataType() == QT_MDD)
        {
            const BaseType *baseType1 = (static_cast<MDDBaseType *>(const_cast<Type *>(inputType1.getType())))->getBaseType();
            BaseType *baseType2 = static_cast<BaseType *>(const_cast<Type *>(inputType2.getType()));

            const BaseType *resultBaseType = Ops::getResultType(opType, baseType1, baseType2);

            if (!resultBaseType)
            {
                LERROR << "Error: QtBit::checkType() - unary induce: operand types are incompatible.";
                parseInfo.setErrorNo(364);
                throw parseInfo;
            }

            MDDBaseType *resultMDDType = new MDDBaseType("tmp", resultBaseType);
            TypeFactory::addTempType(resultMDDType);

            dataStreamType.setType(resultMDDType);
        }

        else if (inputType1.isBaseType())
        {
            BaseType *baseType1 = static_cast<BaseType *>(const_cast<Type *>(inputType1.getType()));
            BaseType *baseType2 = static_cast<BaseType *>(const_cast<Type *>(inputType2.getType()));

            const BaseType *resultBaseType = Ops::getResultType(opType, baseType1, baseType2);

            if (!resultBaseType)
            {
                LERROR << "Error: QtBit::computeOp() - operand types are incompatible.";
                parseInfo.setErrorNo(365);
                throw parseInfo;
            }

            dataStreamType.setType(resultBaseType);
        }
        else
        {
            LERROR << "Error: QtBit::checkType() - operation is not supported on these data types.";
            parseInfo.setErrorNo(403);
            throw parseInfo;
        }
    }
    else
    {
        LERROR << "Error: QtBit::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}

/********************************************************
 *  QtConstructComplex
 ********************************************************
 */


const QtNode::QtNodeType QtConstructComplex::nodeType = QT_CONSTRUCT_COMPLEX;

QtConstructComplex::QtConstructComplex(QtOperation *initInput1, QtOperation *initInput2)
    :  QtBinaryInduce(initInput1, initInput2, Ops::OP_CONSTRUCT_COMPLEX) {}


bool QtConstructComplex::isCommutative() const
{
    return false;
}

void QtConstructComplex::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtConstructComplex Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;
    QtBinaryInduce::printTree(tab, s, mode);
}

void QtConstructComplex::printAlgebraicExpression(ostream &s)
{
    s << "complex (";

    if (input2)
    {
        input2->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ", ";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}
