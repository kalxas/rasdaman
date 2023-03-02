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

#include "raslib/rmdebug.hh"

#include "qlparser/qtpointop.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtconst.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtatomicdata.hh"

#include "catalogmgr/ops.hh"
#include "relcatalogif/type.hh"

#include <logging.hh>

#include <iostream>
#include <string>
using namespace std;

const QtNode::QtNodeType QtPointOp::nodeType = QT_POINTOP;

QtPointOp::QtPointOp(QtOperationList *opList)
    : QtNaryOperation(opList)
{
    pt = NULL;
    bool areAllQtConst = true;
    for (auto iter = opList->begin(); iter != opList->end(); iter++)
    {
        areAllQtConst &= ((*iter)->getNodeType() == QT_CONST);
    }

    if (areAllQtConst)
    {
        pt = new r_Point{r_Dimension(opList->size())};
        size_t i = 0;
        for (auto iter = opList->begin(); iter != opList->end(); iter++, i++)
        {
            QtData *coordPtr = (dynamic_cast<QtConst *>(*iter))->getDataObj();

            (*pt)[i] = (static_cast<QtAtomicData *>(coordPtr))->getSignedValue();
        }
    }
}

QtPointOp::~QtPointOp()
{
    delete pt;
    pt = NULL;
}

QtData *
QtPointOp::evaluate(QtDataList *inputList)
{
    startTimer("QtPointOp");

    QtData *returnValue = NULL;
    QtDataList *operandList = NULL;

    if (!getOperands(inputList, operandList) || !operandList)
    {
        stopTimer();
        return returnValue;
    }

    // first check operand types
    bool allInt = std::all_of(operandList->begin(), operandList->end(), [](const QtData *val)
                              {
                                  return val->getDataType() == QT_SHORT || val->getDataType() == QT_USHORT ||
                                         val->getDataType() == QT_LONG || val->getDataType() == QT_ULONG ||
                                         val->getDataType() == QT_OCTET || val->getDataType() == QT_CHAR;
                              });
    if (!allInt)
    {
        LERROR << "Operands of point expression must be of type integer.";
        for (auto *data: *operandList)
            if (data)
                data->deleteRef();
        delete operandList;
        operandList = NULL;
        parseInfo.setErrorNo(POINTEXP_WRONGOPERANDTYPE);
        throw parseInfo;
    }

    //
    // create a QtPointData object and fill it
    //
    r_Point ptVar{r_Dimension(operandList->size())};
    r_Nullvalues *nullValues = NULL;
    for (auto *data: *operandList)
    {
        if (data->getDataType() == QT_SHORT || data->getDataType() == QT_LONG || data->getDataType() == QT_OCTET)
        {
            ptVar << (static_cast<QtAtomicData *>(data))->getSignedValue();
        }
        else
        {
            ptVar << (static_cast<QtAtomicData *>(data))->getUnsignedValue();
        }
        nullValues = (static_cast<QtAtomicData *>(data))->getNullValues();
    }
    returnValue = new QtPointData(ptVar);
    returnValue->setNullValues(nullValues);

    // delete the old operands
    for (auto *data: *operandList)
        if (data)
        {
            data->deleteRef();
        }
    delete operandList;
    operandList = NULL;

    stopTimer();

    return returnValue;
}

void QtPointOp::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtPointOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << std::endl;

    QtNaryOperation::printTree(tab, s, mode);
}

void QtPointOp::printAlgebraicExpression(std::ostream &s)
{
    s << "[";

    QtNaryOperation::printAlgebraicExpression(s);

    s << "]";
}

const QtTypeElement &
QtPointOp::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    QtOperationList::iterator iter;
    bool opTypesValid = true;

    for (iter = operationList->begin(); iter != operationList->end() && opTypesValid; iter++)
    {
        const QtTypeElement &type = (*iter)->checkType(typeTuple);

        // valid types: integers
        if (!(type.getDataType() == QT_SHORT ||
              type.getDataType() == QT_LONG ||
              type.getDataType() == QT_OCTET ||
              type.getDataType() == QT_USHORT ||
              type.getDataType() == QT_ULONG ||
              type.getDataType() == QT_CHAR))
        {
            opTypesValid = false;
            break;
        }
    }

    if (!opTypesValid)
    {
        LERROR << "Operand of point expression must be of type integer.";
        parseInfo.setErrorNo(POINTEXP_WRONGOPERANDTYPE);
        throw parseInfo;
    }

    dataStreamType.setDataType(QT_POINT);

    return dataStreamType;
}
