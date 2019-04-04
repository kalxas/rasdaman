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

static const char rcsid[] = "@(#)qlparser, QtIntervalLoOp, QtIntervalHiOp, QtSDom: $Header: /home/rasdev/CVS-repository/rasdaman/qlparser/qtunaryfunc.cc,v 1.11 2003/12/27 20:51:28 rasdev Exp $";

#include "config.h"
#include "qlparser/qtunaryfunc.hh"
#include "qlparser/qtintervaldata.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtmdd.hh"
#include "mddmgr/mddobj.hh"

#include <logging.hh>

const QtNode::QtNodeType QtIntervalLoOp::nodeType = QtNode::QT_LO;


QtIntervalLoOp::QtIntervalLoOp(QtOperation *newInput)
    : QtUnaryOperation(newInput)
{
}



QtData *
QtIntervalLoOp::evaluate(QtDataList *inputList)
{
    startTimer("QtIntervalLoOp");

    QtData *returnValue = NULL;
    QtData *operand = NULL;

    operand = input->evaluate(inputList);

    if (operand)
    {
#ifdef QT_RUNTIME_TYPE_CHECK
        if (operand->getDataType() != QT_INTERVAL)
        {
            LERROR << "Internal error in QtIntervalLoOp::evaluate() - "
                   << "runtime type checking failed (INTERVAL).";

            // delete old operand
            if (operand)
            {
                operand->deleteRef();
            }
            return 0;
        }
#endif

        QtIntervalData *intervalData = static_cast<QtIntervalData *>(operand);

        r_Sinterval sinterval = intervalData->getIntervalData();

        if (sinterval.is_low_fixed())
        {
            returnValue = new QtAtomicData(static_cast<r_Long>(sinterval.low()), 4);
        }
        else
        {
            LERROR << "Error: QtIntervalLoOp::evaluate() - operation lo() can not be used for an open bound.";

            // delete old operand
            if (operand)
            {
                operand->deleteRef();
            }

            parseInfo.setErrorNo(394);
            throw parseInfo;
        }

        // delete old operand
        if (operand)
        {
            operand->deleteRef();
        }

    }
    else
    {
        LTRACE << "Information: QtIntervalLoOp::evaluate() - operand is not provided.";
    }

    stopTimer();

    return returnValue;
}



void
QtIntervalLoOp::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtIntervalLoOp Object: " << getEvaluationTime() << std::endl;

    QtUnaryOperation::printTree(tab, s, mode);
}



void
QtIntervalLoOp::printAlgebraicExpression(std::ostream &s)
{
    s << "(" << std::flush;

    if (input)
    {
        input->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ").lo ";
}



const QtTypeElement &
QtIntervalLoOp::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {

        // get input type
        const QtTypeElement &inputType = input->checkType(typeTuple);

        if (inputType.getDataType() != QT_INTERVAL)
        {
            LERROR << "Error: QtIntervalLoOp::checkType() - operation lo() must be of type interval.";
            parseInfo.setErrorNo(393);
            throw parseInfo;
        }

        dataStreamType.setDataType(QT_LONG);
    }
    else
    {
        LERROR << "Error: QtIntervalLoOp::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}



const QtNode::QtNodeType QtIntervalHiOp::nodeType = QtNode::QT_HI;


QtIntervalHiOp::QtIntervalHiOp(QtOperation *newInput)
    : QtUnaryOperation(newInput)
{
}



QtData *
QtIntervalHiOp::evaluate(QtDataList *inputList)
{
    startTimer("QtIntervalHiOp");

    QtData *returnValue = NULL;
    QtData *operand = NULL;

    operand = input->evaluate(inputList);

    if (operand)
    {
#ifdef QT_RUNTIME_TYPE_CHECK
        if (operand->getDataType() != QT_INTERVAL)
        {
            LERROR << "Internal error in QtIntervalHiOp::evaluate() - "
                   << "runtime type checking failed (INTERVAL).";

            // delete old operand
            if (operand)
            {
                operand->deleteRef();
            }
            return 0;
        }
#endif

        QtIntervalData *intervalData = static_cast<QtIntervalData *>(operand);

        r_Sinterval sinterval = intervalData->getIntervalData();

        if (sinterval.is_high_fixed())
        {
            returnValue = new QtAtomicData(static_cast<r_Long>(sinterval.high()), 4);
        }
        else
        {
            LERROR << "Error: QtIntervalHiOp::evaluate() - operation lo() can not be used for an open bound.";

            // delete old operand
            if (operand)
            {
                operand->deleteRef();
            }

            parseInfo.setErrorNo(394);
            throw parseInfo;
        }

        // delete old operand
        if (operand)
        {
            operand->deleteRef();
        }
    }
    else
    {
        LTRACE << "Information: QtIntervalHiOp::evaluate() - operand is not provided.";
    }

    stopTimer();

    return returnValue;
}



void
QtIntervalHiOp::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtIntervalHiOp Object: " << getEvaluationTime() << std::endl;

    QtUnaryOperation::printTree(tab, s, mode);
}



void
QtIntervalHiOp::printAlgebraicExpression(std::ostream &s)
{
    s << "(" << std::flush;

    if (input)
    {
        input->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ").hi ";
}



const QtTypeElement &
QtIntervalHiOp::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {

        // get input type
        const QtTypeElement &inputType = input->checkType(typeTuple);

        if (inputType.getDataType() != QT_INTERVAL)
        {
            LERROR << "Error: QtIntervalHiOp::checkType() - operation lo() must be of type interval.";
            parseInfo.setErrorNo(393);
            throw parseInfo;
        }

        dataStreamType.setDataType(QT_LONG);
    }
    else
    {
        LERROR << "Error: QtIntervalHiOp::checkType() - operand branch invalid.";
    }
    return dataStreamType;
}




const QtNode::QtNodeType QtSDom::nodeType = QtNode::QT_SDOM;


QtSDom::QtSDom(QtOperation *newInput)
    : QtUnaryOperation(newInput)
{
}



QtData *
QtSDom::evaluate(QtDataList *inputList)
{
    startTimer("QtSDom");

    QtData *returnValue = NULL;
    QtData *operand = NULL;

    operand = input->evaluate(inputList);

    if (operand)
    {
#ifdef QT_RUNTIME_TYPE_CHECK
        if (operand->getDataType() != QT_MDD)
        {
            LERROR << "Internal error in QtSDom::evaluate() - "
                   << "runtime type checking failed (MDD).";

            // delete old operand
            if (operand)
            {
                operand->deleteRef();
            }
            return 0;
        }
#endif

        QtMDD  *qtMDD  = static_cast<QtMDD *>(operand);
        MDDObj *currentMDDObj = qtMDD->getMDDObject();
        auto *nullValues = currentMDDObj->getNullValues();

        returnValue = new QtMintervalData(qtMDD->getLoadDomain());
        returnValue->setNullValues(nullValues);

        // delete old operand
        if (operand)
        {
            operand->deleteRef();
        }
    }
    else
    {
        LTRACE << "Information: QtSDom::evaluate() - operand is not provided.";
    }

    stopTimer();

    return returnValue;
}



void
QtSDom::optimizeLoad(QtTrimList *trimList)
{
    // reset trimList because optimization enters a new MDD area

    // delete list
    vector<QtNode::QtTrimElement *>::iterator iter;
    for (iter = trimList->begin(); iter != trimList->end(); iter++)
    {
        delete *iter;
        *iter = NULL;
    }
    delete trimList;
    trimList = NULL;

    if (input)
    {
        input->optimizeLoad(new QtNode::QtTrimList);
    }
}



void
QtSDom::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtSDom Object: " << getEvaluationTime() << std::endl;

    QtUnaryOperation::printTree(tab, s, mode);
}



void
QtSDom::printAlgebraicExpression(std::ostream &s)
{
    s << "sdom(" << std::flush;

    if (input)
    {
        input->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}



const QtTypeElement &
QtSDom::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {

        // get input type
        const QtTypeElement &inputType = input->checkType(typeTuple);

        if (inputType.getDataType() != QT_MDD)
        {
            LERROR << "Error: QtSDom::checkType() - operand must be of type MDD.";
            parseInfo.setErrorNo(395);
            throw parseInfo;
        }

        dataStreamType.setDataType(QT_MINTERVAL);
    }
    else
    {
        LERROR << "Error: QtSDom::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}


