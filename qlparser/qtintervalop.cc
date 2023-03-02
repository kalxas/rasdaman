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

#include "qlparser/qtintervalop.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtintervaldata.hh"
#include "qlparser/qtstringdata.hh"
#include "qlparser/qtatomicdata.hh"
#include <logging.hh>

#include <iostream>
#include <string>
using namespace std;

const QtNode::QtNodeType QtIntervalOp::nodeType = QT_INTERVALOP;

QtIntervalOp::QtIntervalOp(QtOperation *initInput1, QtOperation *initInput2)
    : QtBinaryOperation(initInput1, initInput2)
{
}

bool QtIntervalOp::isCommutative() const
{
    return false;  // NOT commutative
}

QtData *
QtIntervalOp::evaluate(QtDataList *inputList)
{
    startTimer("QtIntervalOp");
    QtData *returnValue = NULL;
    QtData *operand1 = NULL;
    QtData *operand2 = NULL;

    if (getOperands(inputList, operand1, operand2))
    {
        r_Sinterval sinterval;

        switch (operand1->getDataType())
        {
        case QT_LONG:
        case QT_SHORT:
        case QT_OCTET:
            try
            {
                sinterval.set_low(static_cast<r_Range>((static_cast<QtAtomicData *>(operand1))->getSignedValue()));
            }
            catch (...)
            {
                LERROR << "Error: QtIntervalOp::evaluate() - interval bound must be of type integer or '*'.";
                parseInfo.setErrorNo(INTERVAL_INVALID);

                // delete the old operands
                if (operand1)
                {
                    operand1->deleteRef();
                }
                if (operand2)
                {
                    operand2->deleteRef();
                }

                throw parseInfo;
            }
            break;

        case QT_ULONG:
        case QT_USHORT:
        case QT_CHAR:
            try
            {
                sinterval.set_low(static_cast<r_Range>(static_cast<QtAtomicData *>(operand1)->getUnsignedValue()));
            }
            catch (...)
            {
                LERROR << "Error: QtIntervalOp::evaluate() - interval bound must be of type integer or '*'.";
                parseInfo.setErrorNo(INTERVAL_INVALID);

                // delete the old operands
                if (operand1)
                {
                    operand1->deleteRef();
                }
                if (operand2)
                {
                    operand2->deleteRef();
                }

                throw parseInfo;
            }
            break;

        case QT_STRING:
            QtStringData *p;
            p = dynamic_cast<QtStringData *>(operand1);

            if (p && (p->getStringData() == string("*")))
            {
                sinterval.set_low('*');
            }
            else
            {
                LERROR << "Error: QtIntervalOp::evaluate() - interval bound must be '*'.";
                parseInfo.setErrorNo(INTERVAL_INVALID);

                // delete the old operands
                if (operand1)
                {
                    operand1->deleteRef();
                }
                if (operand2)
                {
                    operand2->deleteRef();
                }

                throw parseInfo;
            }
            break;

        default:
            LERROR << "Error: QtIntervalOp::evaluate() - interval bound must be of type integer or '*'.";
            parseInfo.setErrorNo(INTERVAL_BOUNDINVALID);

            // delete the old operands
            if (operand1)
            {
                operand1->deleteRef();
            }
            if (operand2)
            {
                operand2->deleteRef();
            }

            throw parseInfo;
        }

        switch (operand2->getDataType())
        {
        case QT_LONG:
        case QT_SHORT:
        case QT_OCTET:
            try
            {
                sinterval.set_high(static_cast<r_Range>((static_cast<QtAtomicData *>(operand2))->getSignedValue()));
            }
            catch (...)
            {
                LERROR << "Error: QtIntervalOp::evaluate() - interval bound must be of type integer or '*'.";
                parseInfo.setErrorNo(INTERVAL_INVALID);

                // delete the old operands
                if (operand1)
                {
                    operand1->deleteRef();
                }
                if (operand2)
                {
                    operand2->deleteRef();
                }

                throw parseInfo;
            }
            break;

        case QT_ULONG:
        case QT_USHORT:
        case QT_CHAR:
            try
            {
                sinterval.set_high(static_cast<r_Range>((static_cast<QtAtomicData *>(operand2))->getUnsignedValue()));
            }
            catch (...)
            {
                LERROR << "Error: QtIntervalOp::evaluate() - interval bound must be of type integer or '*'.";
                parseInfo.setErrorNo(INTERVAL_INVALID);
                // delete the old operands
                if (operand1)
                {
                    operand1->deleteRef();
                }
                if (operand2)
                {
                    operand2->deleteRef();
                }

                throw parseInfo;
            }
            break;

        case QT_STRING:
            QtStringData *p;
            p = dynamic_cast<QtStringData *>(operand2);

            if (p && (p->getStringData() == string("*")))
            {
                sinterval.set_high('*');
            }
            else
            {
                LERROR << "Error: QtIntervalOp::evaluate() - interval bound must be '*'.";
                parseInfo.setErrorNo(INTERVAL_INVALID);

                // delete the old operands
                if (operand1)
                {
                    operand1->deleteRef();
                }
                if (operand2)
                {
                    operand2->deleteRef();
                }

                throw parseInfo;
            }
            break;

        default:
            LERROR << "Error: QtIntervalOp::evaluate() - interval bound must be of type integer or '*'.";
            parseInfo.setErrorNo(INTERVAL_BOUNDINVALID);

            // delete the old operands
            if (operand1)
            {
                operand1->deleteRef();
            }
            if (operand2)
            {
                operand2->deleteRef();
            }

            throw parseInfo;
        }

        returnValue = new QtIntervalData(sinterval);

        // delete the old operands
        if (operand1)
        {
            operand1->deleteRef();
        }
        if (operand2)
        {
            operand2->deleteRef();
        }
    }

    stopTimer();

    return returnValue;
}

void QtIntervalOp::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtIntervalOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryOperation::printTree(tab, s, mode);
}

void QtIntervalOp::printAlgebraicExpression(ostream &s)
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

    s << ":";

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
QtIntervalOp::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input1 && input2)
    {
        const QtTypeElement &input1Type = input1->checkType(typeTuple);
        const QtTypeElement &input2Type = input2->checkType(typeTuple);

        bool opTypesValid = true;

        // check operand1
        opTypesValid &= input1Type.getDataType() == QT_STRING ||
                        input1Type.getDataType() == QT_LONG ||
                        input1Type.getDataType() == QT_SHORT ||
                        input1Type.getDataType() == QT_OCTET ||
                        input1Type.getDataType() == QT_ULONG ||
                        input1Type.getDataType() == QT_USHORT ||
                        input1Type.getDataType() == QT_CHAR;

        // check operand2
        opTypesValid &= input2Type.getDataType() == QT_STRING ||
                        input2Type.getDataType() == QT_LONG ||
                        input2Type.getDataType() == QT_SHORT ||
                        input2Type.getDataType() == QT_OCTET ||
                        input2Type.getDataType() == QT_ULONG ||
                        input2Type.getDataType() == QT_USHORT ||
                        input2Type.getDataType() == QT_CHAR;

        if (!opTypesValid)
        {
            LERROR << "Error: QtIntervalOp::evaluate() - interval bound must be of type integer or '*'.";
            parseInfo.setErrorNo(INTERVAL_INVALID);
            throw parseInfo;
        }

        dataStreamType.setDataType(QT_INTERVAL);
    }
    else
    {
        LERROR << "Error: QtIntervalOp::checkType() - input branch invalid.";
    }

    return dataStreamType;
}
