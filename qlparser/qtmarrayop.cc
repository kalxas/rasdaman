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

#include "config.h"
#include "raslib/rmdebug.hh"

#include "qlparser/qtmarrayop.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtintervaldata.hh"
#include "qlparser/algebraops.hh"

#include "mddmgr/mddobj.hh"
#include "tilemgr/tile.hh"
#include "relcatalogif/typefactory.hh"
#include "relcatalogif/basetype.hh"
#include "relcatalogif/mdddimensiontype.hh"
#include "common/util/scopeguard.hh"

#include <logging.hh>

#include <iostream>
#include <string>

using namespace std;

const QtNode::QtNodeType QtMarrayOp::nodeType = QT_MARRAYOP;

QtMarrayOp::QtMarrayOp(const string &initIteratorName, QtOperation *mintervalExp, QtOperation *cellExp)
    : QtBinaryOperation(mintervalExp, cellExp), iteratorName(initIteratorName)
{
}

void QtMarrayOp::optimizeLoad(QtTrimList *trimList)
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
}

bool QtMarrayOp::isCommutative() const
{
    return false;  // NOT commutative
}

QtData *
QtMarrayOp::evaluate(QtDataList *inputList)
{
    startTimer("QtMarrayOp");

    QtData *returnValue = NULL;
    QtData *operand1 = NULL;

    if (getOperand(inputList, operand1, 1))
    {
        const auto deleteOperand1 = common::make_scope_guard(
            [&operand1]() noexcept
            {
                if (operand1) operand1->deleteRef();
            });

        // if operand1 is a QT_INTERVAL convert it to QT_MINTERVAL
        if (operand1->getDataType() == QT_INTERVAL)
        {
            //do conversion to QT_MINTERVAL
            //create one-dimensional minterval from operand1 (operand1 is a sinterval)
            r_Minterval tmpMinterval(1u);
            tmpMinterval << (static_cast<QtIntervalData *>(operand1))->getIntervalData();
            if (operand1)
                operand1->deleteRef();
            operand1 = new QtMintervalData(tmpMinterval);
        }

#ifdef QT_RUNTIME_TYPE_CHECK
        if (operand1->getDataType() != QT_MINTERVAL)
            LERROR << "Internal error in QtMarrayOp::evaluate() - "
                   << "runtime type checking failed (Minterval).";
        return 0;
#endif

        const auto &domain = (static_cast<QtMintervalData *>(operand1))->getMintervalData();

        LTRACE << "Marray domain " << domain;

        // add point data with its iterator name to the input list

        // create a QtPointData object with corner point
        QtPointData *point = new QtPointData(domain.get_origin());
        // set its iterator name
        point->setIteratorName(iteratorName);
        // if the list of binding variables is empty, create a new one and delete it afterwards
        bool newInputList = false;
        if (!inputList)
        {
            inputList = new QtDataList();
            newInputList = true;
        }
        inputList->push_back(point);
        // automatically cleanup the input list on exit
        const auto deleteInputList = common::make_scope_guard(
            [&inputList, newInputList]() noexcept
            {
                inputList->back()->deleteRef();
                inputList->pop_back();
                if (newInputList)
                {
                    delete inputList;
                    inputList = NULL;
                }
            });

        // determine types
        const BaseType *cellType = static_cast<const BaseType *>(input2->getDataStreamType().getType());
        MDDDimensionType *mddBaseType = new MDDDimensionType("tmp", cellType, domain.dimension());
        TypeFactory::addTempType(mddBaseType);

        // create tile for result
        auto resTile = std::unique_ptr<Tile>(new Tile(domain, cellType));

        // create execution object QLArrayOp
        auto qlMarrayOp = std::unique_ptr<QLMarrayOp>(new QLMarrayOp(input2, inputList, iteratorName, cellType));

        // execute query engine marray operation
        resTile->execMarrayOp(qlMarrayOp.get(), domain, domain);

        // create MDDObj for result
        MDDObj *mddres = new MDDObj(mddBaseType, domain);
        // insert Tile in result mdd
        mddres->insertTile(resTile.release());
        // create a new QtMDD object as carrier object for the transient MDD object
        returnValue = new QtMDD(mddres);
    }

    stopTimer();
    return returnValue;
}

void QtMarrayOp::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtMarrayOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "Iterator Name: " << iteratorName.c_str() << endl;
    QtBinaryOperation::printTree(tab, s, mode);
}

void QtMarrayOp::printAlgebraicExpression(ostream &s)
{
    s << "(";
    s << iteratorName.c_str() << ",";
    if (input1)
        input1->printAlgebraicExpression(s);
    else
        s << "<nn>";

    s << ",";

    if (input2)
        input2->printAlgebraicExpression(s);
    else
        s << "<nn>";

    s << ")";
}

const QtTypeElement &
QtMarrayOp::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input1 && input2)
    {
        // check domain expression
        const QtTypeElement &domainExp = input1->checkType(typeTuple);
        if (domainExp.getDataType() != QT_MINTERVAL && domainExp.getDataType() != QT_INTERVAL)
        {
            LERROR << "Can not evaluate domain expression to an minterval.";
            parseInfo.setErrorNo(DOMAINEVALUATIONERROR);
            throw parseInfo;
        }

        // check value expression

        // add domain iterator to the list of bounded variables
        std::unique_ptr<QtTypeTuple> typeTuplePtr;
        if (!typeTuple)
        {
            typeTuple = new QtTypeTuple();
            typeTuplePtr.reset(typeTuple);
        }
        typeTuple->tuple.push_back(QtTypeElement(QT_POINT, iteratorName.c_str()));
        // get type of value expression
        const QtTypeElement &valueExp = input2->checkType(typeTuple);
        // remove iterator again
        typeTuple->tuple.pop_back();

        // check type
        const auto valueExpType = valueExp.getDataType();
        const auto isAtomic = valueExpType >= QT_BOOL && valueExpType <= QT_COMPLEXTYPE2;
        if (!isAtomic && valueExpType != QT_COMPLEX)
        {
            LERROR << "Value expression must be scalar, but was " << valueExpType << ".";
            parseInfo.setErrorNo(VALUEEXP_WRONGOPERANDTYPE);
            throw parseInfo;
        }

        // create MDD type
        const BaseType *cellType = static_cast<const BaseType *>(valueExp.getType());
        MDDBaseType *mddBaseType = new MDDBaseType("tmp", cellType);
        TypeFactory::addTempType(mddBaseType);

        dataStreamType.setType(mddBaseType);
    }
    else
    {
        LERROR << "operand branch invalid.";
    }

    return dataStreamType;
}
