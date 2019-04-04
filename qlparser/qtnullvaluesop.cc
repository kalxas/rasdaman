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

static const char rcsid[] = "@(#)qlparser, QtNullvaluesOp: $Header: /home/rasdev/CVS-repository/rasdaman/qlparser/qtmintervalop.cc,v 1.12 2003/12/27 20:51:28 rasdev Exp $";

#include "config.h"
#include "raslib/rmdebug.hh"

#include "qlparser/qtnullvaluesop.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "mddmgr/mddobj.hh"
#include "qlparser/qtscalardata.hh"
#include "qlparser/qtnullvaluesdata.hh"
#include "qtatomicdata.hh"
#include "raslib/nullvalues.hh"

#include <easylogging++.h>

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif


const QtNode::QtNodeType QtNullvaluesOp::nodeType = QT_NULLVALUESOP;

QtNullvaluesOp::QtNullvaluesOp(QtNullvaluesList *opList)
    :  QtNaryOperation(), nullvalueIntervals{opList}
{
}

QtNullvaluesOp::~QtNullvaluesOp()
{
    if (nullvalueIntervals)
    {
        delete nullvalueIntervals;
        nullvalueIntervals = NULL;
    }
}

r_Double
QtNullvaluesOp::getDoubleValue(const QtScalarData *data)
{
    switch (data->getDataType())
    {
    case QT_OCTET:
    case QT_SHORT:
    case QT_LONG:
        return static_cast<r_Double>((static_cast<const QtAtomicData *>(data))->getSignedValue());
    case QT_BOOL:
    case QT_CHAR:
    case QT_USHORT:
    case QT_ULONG:
        return static_cast<r_Double>((static_cast<const QtAtomicData *>(data))->getUnsignedValue());
    case QT_FLOAT:
    case QT_DOUBLE:
        return (static_cast<const QtAtomicData *>(data))->getDoubleValue();
    default:
    {
        LERROR << "Unsupported null value data type '" << data->getDataType() << "'.";
        parseInfo.setErrorNo(499);
        throw parseInfo;
    }
    }
}


QtData *
QtNullvaluesOp::evaluate(QtDataList *inputList)
{
    startTimer("QtNullvaluesOp");

    std::vector<std::pair<r_Double, r_Double>> nullvaluePairs;
    for (auto it = nullvalueIntervals->begin(); it != nullvalueIntervals->end(); it++)
    {
        auto nullvalueInterval = *it;
        auto low = getDoubleValue(nullvalueInterval.first);
        auto high = getDoubleValue(nullvalueInterval.second);
        nullvaluePairs.emplace_back(low, high);
    }

    r_Nullvalues nullvalues{std::move(nullvaluePairs)};
    QtData *returnValue = new QtNullvaluesData(nullvalues);

    stopTimer();

    return returnValue;
}


void
QtNullvaluesOp::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtNullvaluesOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtNaryOperation::printTree(tab, s, mode);
}



void
QtNullvaluesOp::printAlgebraicExpression(ostream &s)
{
    s << "NULL VALUES [";
    QtNaryOperation::printAlgebraicExpression(s);
    s << "]";
}



const QtTypeElement &
QtNullvaluesOp::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_MINTERVAL);
    return dataStreamType;
}


// -----------------------------------------------------------------------------
// QtAddNullvalues
// -----------------------------------------------------------------------------


const QtNode::QtNodeType QtAddNullvalues::nodeType = QT_NULLVALUESOP;

QtAddNullvalues::QtAddNullvalues(QtOperation *input, QtNullvaluesOp *nullvaluesOp)
    :  QtBinaryOperation(input, nullvaluesOp)
{
}

QtData *
QtAddNullvalues::evaluate(QtDataList *inputList)
{
    startTimer("QtAddNullvalues");
    QtData *returnValue = NULL;
    QtData *operand1 = NULL;
    QtData *operand2 = NULL;

    if (getOperands(inputList, operand1, operand2))
    {
        QtNullvaluesData *nullvaluesData = static_cast<QtNullvaluesData *>(operand2);
        auto *newNullvalues = new r_Nullvalues(nullvaluesData->getNullvaluesData());
        (static_cast<QtMDD *>(operand1))->getMDDObject()->setNullValues(newNullvalues);
        static_cast<QtMDD *>(operand1)->setNullValues(newNullvalues);
        returnValue = operand1;

        // delete operand2 (operand1 is returned as is, so not deleted)
        operand2->deleteRef();
    }
    stopTimer();
    return returnValue;
}


void
QtAddNullvalues::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtAddNullvalues Object "
      << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryOperation::printTree(tab, s, mode);
}



void
QtAddNullvalues::printAlgebraicExpression(ostream &s)
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
    s << " ";
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



const QtTypeElement &
QtAddNullvalues::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);
    if (input1)
    {
        const QtTypeElement &inputType1 = input1->checkType(typeTuple);
        if (inputType1.getDataType() != QT_MDD)
        {
            LERROR << "Cannot add null values to a non-MDD value.";
            parseInfo.setErrorNo(405);
            throw parseInfo;
        }
        dataStreamType.setDataType(inputType1.getDataType());
        dataStreamType.setType(inputType1.getType());
    }
    else
    {
        LERROR << "QtAddNullvalues::checkType() - operand branch invalid.";
    }
    return dataStreamType;
}
