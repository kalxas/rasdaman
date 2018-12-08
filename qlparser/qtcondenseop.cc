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

static const char rcsid[] = "@(#)qlparser, QtCondenseOp: $Header: /home/rasdev/CVS-repository/rasdaman/qlparser/qtcondenseop.cc,v 1.18 2003/12/27 20:51:28 rasdev Exp $";

#include "config.h"
#include "raslib/rmdebug.hh"

#include "qlparser/qtcondenseop.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "catalogmgr/algebraops.hh"
#include "mddmgr/mddobj.hh"

#include <logging.hh>

#include "catalogmgr/typefactory.hh"
#include "relcatalogif/doubletype.hh"
#include "relcatalogif/ulongtype.hh"
#include "relcatalogif/longtype.hh"

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif


const QtNode::QtNodeType QtCondenseOp::nodeType = QT_CONDENSEOP;

QtCondenseOp::QtCondenseOp(Ops::OpType  newOperation,
                           const string& initIteratorName,
                           QtOperation* mintervalExp,
                           QtOperation* cellExp,
                           QtOperation* condExp)
    :  QtBinaryOperation(mintervalExp, cellExp),
       iteratorName(initIteratorName),
       condOp(condExp),
       operation(newOperation)
{
}



QtCondenseOp::~QtCondenseOp()
{
    if (condOp)
    {
        delete condOp;
        condOp = NULL;
    }
}



QtNode::QtNodeList*
QtCondenseOp::getChilds(QtChildType flag)
{
    QtNodeList* resultList;
    resultList = QtBinaryOperation::getChilds(flag);
    if (condOp)
    {
        if (flag == QT_LEAF_NODES || flag == QT_ALL_NODES)
        {
            QtNodeList* subList = NULL;
            subList = condOp->getChilds(flag);
            // remove all elements in subList and insert them at the beginning in resultList
            resultList->splice(resultList->begin(), *subList);
            // delete temporary subList
            delete subList;
            subList = NULL;
        }

        // add the nodes of the current level
        if (flag == QT_DIRECT_CHILDS || flag == QT_ALL_NODES)
        {
            resultList->push_back(condOp);
        }
    }

    return resultList;
}



bool
QtCondenseOp::equalMeaning(QtNode* node)
{
    bool result = false;

    if (nodeType == node->getNodeType())
    {
        QtCondenseOp* condNode;
        condNode = static_cast<QtCondenseOp*>(node); // by force

        // check domain and cell expression
        result  = QtBinaryOperation::equalMeaning(condNode);

        // check condition expression
        result &= (!condOp && !condNode->getCondOp()) ||
                  condOp->equalMeaning(condNode->getCondOp());
    };

    return (result);
}



string
QtCondenseOp::getSpelling()
{
    char tempStr[20];
    sprintf(tempStr, "%lu", static_cast<unsigned long>(getNodeType()));
    string result  = string(tempStr);
    result.append("(");
    result.append(QtBinaryOperation::getSpelling());
    result.append(",");

    if (condOp)
    {
        result.append(condOp->getSpelling());
    }
    else
    {
        result.append("<nn>");
    }

    result.append(")");

    return result;
}


void
QtCondenseOp::setInput(QtOperation* inputOld, QtOperation* inputNew)
{
    QtBinaryOperation::setInput(inputOld, inputNew);

    if (condOp == inputOld)
    {
        condOp = inputNew;

        if (inputNew)
        {
            inputNew->setParent(this);
        }
    }
}



void
QtCondenseOp::optimizeLoad(QtTrimList* trimList)
{
    // delete the trimList and optimize subtrees

    // release( trimList->begin(), trimList->end() );
    for (QtNode::QtTrimList::iterator iter = trimList->begin(); iter != trimList->end(); iter++)
    {
        delete *iter;
        *iter = NULL;
    }
    delete trimList;
    trimList = NULL;

    QtBinaryOperation::optimizeLoad(new QtNode::QtTrimList());

    if (condOp)
    {
        condOp->optimizeLoad(new QtNode::QtTrimList());
    }
}


void
QtCondenseOp::simplify()
{
    LTRACE << "simplify() warning: QtCondenseOp itself is not simplified yet";

    // Default method for all classes that have no implementation.
    // Method is used bottom up.

    QtNodeList* resultList = NULL;
    QtNodeList::iterator iter;

    resultList = getChilds(QT_DIRECT_CHILDS);
    for (iter = resultList->begin(); iter != resultList->end(); iter++)
    {
        (*iter)->simplify();
    }

    delete resultList;
    resultList = NULL;
}


bool
QtCondenseOp::isCommutative() const
{
    return false; // NOT commutative
}



QtData*
QtCondenseOp::evaluate(QtDataList* inputList)
{
    startTimer("QtCondenseOp");

    QtData* returnValue = NULL;
    QtData* operand1 = NULL;

    if (getOperand(inputList, operand1, 1))
    {

#ifdef QT_RUNTIME_TYPE_CHECK
        if (operand1->getDataType() != QT_MINTERVAL)
        {
            LERROR << "Internal error in QtMarrayOp::evaluate() - "
                   << "runtime type checking failed (Minterval).";
            // delete old operand
            if (operand1)
                operand1->deleteRef();
            return 0;
        }
#endif

        r_Minterval domain = (static_cast<QtMintervalData*>(operand1))->getMintervalData();

        LTRACE << "Marray domain " << domain;

        // determine aggregation type
        const BaseType* cellBaseType;
        QtDataType cellType = input2->getDataStreamType().getDataType();
        if (cellType == QT_MDD)
            cellBaseType = (static_cast<const MDDBaseType*>(input2->getDataStreamType().getType()))->getBaseType();
        else
            cellBaseType = static_cast<const BaseType*>(input2->getDataStreamType().getType());

        const BaseType* resBaseType;
        switch (cellType)
        {
        case QT_FLOAT:
            resBaseType = TypeFactory::mapType("Double");
            break;
        case QT_CHAR:
        case QT_USHORT:
            resBaseType = TypeFactory::mapType("ULong");
            break;
        case QT_OCTET:
        case QT_SHORT:
            resBaseType = TypeFactory::mapType("Long");
            break;
        default:
            resBaseType = cellBaseType;
            break;
        }

        // get operation object
        auto cellBinOp = std::unique_ptr<BinaryOp>(Ops::getBinaryOp(operation, resBaseType, resBaseType, cellBaseType));
        try
        {
            if (cellType == QT_MDD)
                returnValue = evaluateInducedOp(inputList, cellBinOp.get(), domain);
            else
                returnValue = evaluateScalarOp(inputList, resBaseType, cellBinOp.get(), domain);
        }
        catch (...)
        {
            if (operand1)
                operand1->deleteRef();
            throw;
        }
        if (operand1)
            operand1->deleteRef();
    }

    stopTimer();
    return returnValue;
}

QtData*
QtCondenseOp::evaluateScalarOp(QtDataList* inputList, const BaseType* resType, BinaryOp* cellBinOp, r_Minterval domain)
{
    // create execution object QLCondenseOp
    auto qlCondenseOp = std::unique_ptr<QLCondenseOp>(new QLCondenseOp(input2, condOp, inputList, iteratorName,
            resType, 0, cellBinOp, NULL));

    // result buffer
    char* result = Tile::execGenCondenseOp(qlCondenseOp.get(), domain);
    // allocate cell buffer
    char* resultBuffer = new char[ resType->getSize() ];
    // copy cell content
    memcpy(resultBuffer, result, resType->getSize());

    // create data object for the cell
    QtScalarData* scalarDataObj = NULL;
    if (resType->getType() == STRUCT)
        scalarDataObj = new QtComplexData();
    else
        scalarDataObj = new QtAtomicData();

    scalarDataObj->setValueType(resType);
    scalarDataObj->setValueBuffer(resultBuffer);

    // set return data object
    return scalarDataObj;
}

QtData*
QtCondenseOp::evaluateInducedOp(QtDataList* inputList, BinaryOp* cellBinOp, r_Minterval domain)
{
    auto qlInducedCondenseOp = std::unique_ptr<QLInducedCondenseOp>(
        new QLInducedCondenseOp(input2, condOp, inputList, cellBinOp, iteratorName));
    return QLInducedCondenseOp::execGenCondenseInducedOp(qlInducedCondenseOp.get(), domain);
}

void
QtCondenseOp::printTree(int tab, ostream& s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCondenseOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "Iterator Name: " << iteratorName.c_str() << endl;

    QtBinaryOperation::printTree(tab, s, mode);
}



void
QtCondenseOp::printAlgebraicExpression(ostream& s)
{
    s << "(";

    s << iteratorName.c_str() << ",";

    if (input1)
    {
        input1->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ",";

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



const QtTypeElement&
QtCondenseOp::checkType(QtTypeTuple* typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input1 && input2)
    {

        // check domain expression
        const QtTypeElement& domainExp = input1->checkType(typeTuple);

        if (domainExp.getDataType() != QT_MINTERVAL)
        {
            LERROR << "Error: QtCondenseOp::checkType() - Can not evaluate domain expression to an minterval";
            parseInfo.setErrorNo(401);
            throw parseInfo;
        }

        // add domain iterator to the list of bounded variables
        bool newList = false;
        if (!typeTuple)
        {
            typeTuple = new QtTypeTuple();
            newList = true;
        }
        typeTuple->tuple.push_back(QtTypeElement(QT_POINT, iteratorName.c_str()));

        //
        // check value expression
        //

        // get value expression type
        const QtTypeElement& valueExp = input2->checkType(typeTuple);
        auto op2Type = valueExp.getDataType();

        // check type
        if (op2Type != QT_BOOL   && op2Type != QT_COMPLEX &&
            op2Type != QT_CHAR   && op2Type != QT_OCTET   &&
            op2Type != QT_USHORT && op2Type != QT_SHORT   &&
            op2Type != QT_ULONG  && op2Type != QT_LONG    &&
            op2Type != QT_FLOAT  && op2Type != QT_DOUBLE  &&
            op2Type != QT_COMPLEXTYPE1 && op2Type != QT_COMPLEXTYPE2 && op2Type != QT_MDD)
        {
            LERROR << "Error: QtCondenseOp::checkType() - Value expression must be either of type atomic, complex or MDD.";
            parseInfo.setErrorNo(412);
            throw parseInfo;
        }

        switch (op2Type)
        {
            case QT_FLOAT:
                dataStreamType.setDataType(QT_DOUBLE);
                break;
            case QT_CHAR:
            case QT_USHORT:
                dataStreamType.setDataType(QT_ULONG);
                break;
            case QT_OCTET:
            case QT_SHORT:
                dataStreamType.setDataType(QT_LONG);
                break;
            default:
                dataStreamType = valueExp;
                break;
        }

        //
        // check condition expression
        //

        if (condOp)
        {
            // get value expression type
            const QtTypeElement& condExp = condOp->checkType(typeTuple);

            // check type
            if (condExp.getDataType() != QT_BOOL)
            {
                LERROR << "Error: QtCondenseOp::checkType() - Condition expression must be of type boolean";
                parseInfo.setErrorNo(413);
                throw parseInfo;
            }
        }

        // remove iterator again
        typeTuple->tuple.pop_back();
        if (newList)
        {
            delete typeTuple;
            typeTuple = NULL;
        }
    }
    else
    {
        LERROR << "Error: QtCondenseOp::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}


