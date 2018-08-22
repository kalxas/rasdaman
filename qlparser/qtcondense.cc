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

static const char rcsid[] = "@(#)qlparser, QtCondense: $Header: /home/rasdev/CVS-repository/rasdaman/qlparser/qtcondense.cc,v 1.47 2005/09/03 20:17:55 rasdev Exp $";

#include "config.h"
#include "debug.hh"

#include "qlparser/qtcondense.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtscalardata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtbinaryinduce.hh"
#include "qlparser/qtbinaryinduce2.hh"

#include <logging.hh>

#include "mddmgr/mddobj.hh"

#include "catalogmgr/typefactory.hh"
#include "catalogmgr/ops.hh"

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

const QtNode::QtNodeType QtCondense::nodeType = QtNode::QT_CONDENSE;

QtCondense::QtCondense(Ops::OpType newOpType)
    : QtUnaryOperation(), opType(newOpType)
{
}



QtCondense::QtCondense(Ops::OpType newOpType, QtOperation* initInput)
    : QtUnaryOperation(initInput), opType(newOpType)
{
}


QtNode::QtAreaType
QtCondense::getAreaType()
{
    return QT_AREA_SCALAR;
}



void
QtCondense::optimizeLoad(QtTrimList* trimList)
{
    // reset trimList because optimization enters a new MDD area

    // delete list
    // release( trimList->begin(), trimList->end() );
    vector<QtNode::QtTrimElement*>::iterator iter;
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



QtData*
QtCondense::computeFullCondense(QtDataList* inputList, r_Minterval& areaOp)
{
    QtScalarData* returnValue = NULL;

    // get the operand
    QtData* operand = input->evaluate(inputList);

    if (operand)
    {

#ifdef QT_RUNTIME_TYPE_CHECK
        if (operand->getDataType() != QT_MDD)
        {
            LERROR << "Internal error in QtCountCells::computeFullCondense() - "
                   << "runtime type checking failed (MDD).";

            // delete old operand
            if (operand)
            {
                operand->deleteRef();
            }

            return 0;
        }
#endif

        QtMDD* mdd = static_cast<QtMDD*>(operand);

#ifdef QT_RUNTIME_TYPE_CHECK
        if (opType == Ops::OP_SOME || opType == Ops::OP_ALL || opType == Ops::OP_COUNT)
        {
            if (mdd->getCellType()->getType() != BOOLTYPE)
            {
                LERROR << "Internal error in QtCondense::computeFullCondense() - "
                       << "runtime type checking failed (BOOL).";

                // delete old operand
                if (operand)
                {
                    operand->deleteRef();
                }

                return 0;
            }
        }
#endif

        // get result type
        const BaseType* resultType = Ops::getResultType(opType, mdd->getCellType());

        // get the MDD object
        MDDObj* op = (static_cast<QtMDD*>(operand))->getMDDObject();
        auto* nullValues = op->getNullValues();

        //  get the area, where the operation has to be applied
        areaOp = mdd->getLoadDomain();

        LDEBUG << "computeFullCondense-last-good\n";
        // get all tiles in relevant area
        vector<boost::shared_ptr<Tile>>* allTiles = op->intersect(areaOp);

        LDEBUG << "computeFullCondense-8\n";
        // get new operation object
        CondenseOp* condOp = Ops::getCondenseOp(opType, resultType, mdd->getCellType());
        condOp->setNullValues(nullValues);
        unsigned long totalValuesCount{0};

        LDEBUG << "computeFullCondense-9\n";
        // and iterate over them
        for (vector<boost::shared_ptr<Tile>>::iterator tileIt = allTiles->begin();
                tileIt != allTiles->end(); tileIt++)
        {
            // domain of the actual tile
            r_Minterval tileDom = (*tileIt)->getDomain();

            // domain of the relevant area of the actual tile
            r_Minterval intersectDom = tileDom.create_intersection(areaOp);
            totalValuesCount += intersectDom.cell_count();

            (*tileIt)->execCondenseOp(condOp, intersectDom);
        }

        // delete tile vector
        delete allTiles;
        allTiles = NULL;

        LDEBUG << "computeFullCondense-a\n";
        // create result object
        if (resultType->getType() == STRUCT)
        {
            returnValue = new QtComplexData();
        }
        else
        {
            returnValue = new QtAtomicData();
        }
        returnValue->setNullValuesCount(condOp->getNullValuesCount());
        returnValue->setTotalValuesCount(totalValuesCount);

        LDEBUG << "computeFullCondense-b\n";
        // allocate buffer for the result
        char* resultBuffer = new char[resultType->getSize()];
        memcpy(resultBuffer, condOp->getAccuVal(), resultType->getSize());

        LDEBUG << "computeFullCondense-c\n";
        returnValue->setValueType(resultType);
        returnValue->setValueBuffer(resultBuffer);

        LDEBUG << "computeFullCondense-d\n";
        // delete operation object
        delete condOp;
        condOp = NULL;

        LDEBUG << "computeFullCondense-e\n";
        // delete old operand
        operand->deleteRef();
#ifdef DEBUG
        LTRACE << "opType of QtCondense::computeFullCondense(): " << opType;
        LTRACE <<         "Result.....................................: ";

        returnValue->printStatus(RMInit::dbgOut);
#endif
    }

    return returnValue;
}



const QtTypeElement&
QtCondense::checkType(QtTypeTuple* typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {

        // get input types
        const QtTypeElement& inputType = input->checkType(typeTuple);
#ifdef DEBUG
        LTRACE << "Class..: " << getClassName();
        LTRACE << "Operand: ";

        inputType.printStatus(RMInit::dbgOut);
#endif

        if (inputType.getDataType() != QT_MDD)
        {
            LERROR << "Error: QtCondense::evaluate() - operand must be multidimensional.";
            parseInfo.setErrorNo(353);
            throw parseInfo;
        }

        const BaseType* baseType = (static_cast<const MDDBaseType*>(inputType.getType()))->getBaseType();

        if (opType == Ops::OP_SOME || opType == Ops::OP_ALL)
        {
            if (baseType->getType() != BOOLTYPE)
            {
                LERROR << "Error: QtCondense::evaluate() - operand of quantifiers must be of type r_Marray<d_Boolean>.";
                parseInfo.setErrorNo(354);
                throw parseInfo;
            }
        }

        if (opType == Ops::OP_COUNT)
        {
            if (baseType->getType() != BOOLTYPE)
            {
                LERROR << "Error: QtCondense::evaluate() - operand of count_cells must be of type r_Marray<d_Boolean>.";
                parseInfo.setErrorNo(415);
                throw parseInfo;
            }
        }

        const BaseType* resultType = Ops::getResultType(opType, baseType);

        if (getNodeType() == QT_AVGCELLS)
        {
            // consider division by the number of cells

            const BaseType* DoubleType = TypeFactory::mapType("Double");
            const BaseType* finalResultType = Ops::getResultType(Ops::OP_DIV, resultType, DoubleType);

            resultType = finalResultType;
        }

        if (getNodeType() >= QT_VARPOP && getNodeType() <= QT_STDDEVSAMP)
        {
            const BaseType* finalResultType = Ops::getResultType(Ops::OP_CAST_DOUBLE, resultType);

            resultType = finalResultType;
 
        }

        dataStreamType.setType(resultType);
    }
    else
    {
        LERROR << "Error: QtCondense::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}


void
QtCondense::printTree(int tab, ostream& s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << getClassName() << " object" << getEvaluationTime() << endl;

    QtUnaryOperation::printTree(tab, s, mode);
}



void
QtCondense::printAlgebraicExpression(ostream& s)
{
    s << getAlgebraicName() << "(";

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



const QtNode::QtNodeType QtSome::nodeType = QtNode::QT_SOME;


QtSome::QtSome()
    : QtCondense(Ops::OP_SOME)
{
}


QtSome::QtSome(QtOperation* inputNew)
    : QtCondense(Ops::OP_SOME, inputNew)
{
}


QtData*
QtSome::evaluate(QtDataList* inputList)
{
    startTimer("Qt");

    QtData* returnValue = NULL;
    r_ULong dummy = 0; // needed for conversion to and from CULong

    // get the operand
    QtData* operand = input->evaluate(inputList);

    if (operand)
    {
#ifdef QT_RUNTIME_TYPE_CHECK
        if (operand->getDataType() != QT_MDD)
        {
            LERROR << "Internal error in QtSome::evaluate() - "
                   << "runtime type checking failed (MDD).";

            // delete old operand
            if (operand)
            {
                operand->deleteRef();
            }

            return 0;
        }
#endif

        QtMDD* mdd = static_cast<QtMDD*>(operand);

        // get result type
        BaseType* resultType = static_cast<BaseType*>(const_cast<Type*>(dataStreamType.getType()));

#ifdef QT_RUNTIME_TYPE_CHECK
        if (mdd->getCellType()->getType() != BOOLTYPE)
            LERROR << "Internal error in QtSome::evaluate() - "
                   << "runtime type checking failed (BOOL).";

        // delete old operand
        if (operand)
        {
            operand->deleteRef();
        }

        return 0;
    }
#endif

    // get the MDD object
    MDDObj* op = mdd->getMDDObject();

    //  get the area, where the operation has to be applied
    r_Minterval areaOp = mdd->getLoadDomain();

    // get all tiles in relevant area
    vector<boost::shared_ptr<Tile>>* allTiles = op->intersect(areaOp);

    // allocate buffer for the result
    unsigned int typeSize = resultType->getSize();
    char* resultBuffer = new char[typeSize];

    // initialize result buffer with false
    dummy = 0;
    resultType->makeFromCULong(resultBuffer, &dummy);
    CondenseOp* condOp = Ops::getCondenseOp(Ops::OP_SOME, resultType, resultBuffer, resultType, 0, 0);

    // and iterate over them
    for (vector<boost::shared_ptr<Tile>>::iterator tileIt = allTiles->begin(); tileIt !=  allTiles->end() && !dummy ; tileIt++)
    {
        // domain of the actual tile
        r_Minterval tileDom = (*tileIt)->getDomain();

        // domain of the relevant area of the actual tile
        r_Minterval intersectDom = tileDom.create_intersection(areaOp);
        (*tileIt)->execCondenseOp(condOp, intersectDom);
        resultType->convertToCULong(condOp->getAccuVal(), &dummy);
    }

    delete condOp;
    condOp = NULL;
    // delete tile vector
    delete allTiles;
    allTiles = NULL;

    // create QtAtomicData object for the result
    returnValue = new QtAtomicData(static_cast<bool>(dummy));

    // delete result buffer
    delete[] resultBuffer;
    resultBuffer = NULL;

    // The following is now then when deleting the last reference to the operand.
    // delete the obsolete MDD object
    // delete op;

    // delete old operand
    if (operand)
    {
        operand->deleteRef();
    }
}

stopTimer();
return returnValue;
}



const QtAll::QtNodeType QtAll::nodeType = QtNode::QT_ALL;


QtAll::QtAll()
    : QtCondense(Ops::OP_ALL)
{
}


QtAll::QtAll(QtOperation* inputNew)
    : QtCondense(Ops::OP_ALL, inputNew)
{
}


QtData*
QtAll::evaluate(QtDataList* inputList)
{
    startTimer("QtAll");

    QtData* returnValue = NULL;
    r_ULong dummy = 0; // needed for conversion to and from CULong

    // get the operand
    QtData* operand = input->evaluate(inputList);

    if (operand)
    {

#ifdef QT_RUNTIME_TYPE_CHECK
        if (operand->getDataType() != QT_MDD)
        {
            LERROR << "Internal error in QtAll::evaluate() - "
                   << "runtime type checking failed (MDD).";

            // delete old operand
            if (operand)
            {
                operand->deleteRef();
            }

            return 0;
        }
#endif

        QtMDD* mdd = static_cast<QtMDD*>(operand);

        // get result type
        const BaseType* resultType = static_cast<BaseType*>(const_cast<Type*>(dataStreamType.getType()));

#ifdef QT_RUNTIME_TYPE_CHECK
        if (mdd->getCellType()->getType() != BOOLTYPE)
            LERROR << "Internal error in QtAll::evaluate() - "
                   << "runtime type checking failed (BOOL).";

        // delete old operand
        if (operand)
        {
            operand->deleteRef();
        }

        return 0;
    }
#endif

    // get the MDD object
    MDDObj* op = (static_cast<QtMDD*>(operand))->getMDDObject();

    //  get the area, where the operation has to be applied
    r_Minterval areaOp = mdd->getLoadDomain();

    // get all tiles in relevant area
    vector<boost::shared_ptr<Tile>>* allTiles = op->intersect(areaOp);

    // allocate buffer for the result
    unsigned int tempTypeSize = resultType->getSize();
    char* resultBuffer = new char[tempTypeSize];

    // initialize result buffer with true
    dummy = 1;
    resultType->makeFromCULong(resultBuffer, &dummy);
    CondenseOp* condOp = Ops::getCondenseOp(Ops::OP_ALL, resultType, resultBuffer, resultType, 0, 0);

    for (std::vector<boost::shared_ptr<Tile>>::iterator tileIt = allTiles->begin(); tileIt != allTiles->end() && dummy; tileIt++)
    {
        // domain of the actual tile
        r_Minterval tileDom = (*tileIt)->getDomain();

        // domain of the relevant area of the actual tile
        r_Minterval intersectDom = tileDom.create_intersection(areaOp);

        (*tileIt)->execCondenseOp(condOp, intersectDom);
        resultType->convertToCULong(condOp->getAccuVal(), &dummy);
    }
    delete condOp;
    condOp = NULL;
    // delete tile vector
    delete allTiles;
    allTiles = NULL;

    // create QtBoolData object for the result
    returnValue = new QtAtomicData(static_cast<bool>(dummy));

    // delete result buffer done in delete CondOp
    delete[] resultBuffer;
    resultBuffer = NULL;

    // delete old operand
    if (operand)
    {
        operand->deleteRef();
    }
}

stopTimer();

return returnValue;
}



const QtCountCells::QtNodeType QtCountCells::nodeType = QtNode::QT_COUNTCELLS;


QtCountCells::QtCountCells()
    : QtCondense(Ops::OP_COUNT)
{
}


QtCountCells::QtCountCells(QtOperation* inputNew)
    : QtCondense(Ops::OP_COUNT, inputNew)
{
}


QtData*
QtCountCells::evaluate(QtDataList* inputList)
{
    startTimer("QtCountCells");
    r_Minterval dummyint;
    QtData* returnValue = QtCondense::computeFullCondense(inputList, dummyint);
    stopTimer();

    return returnValue;
}



const QtAddCells::QtNodeType QtAddCells::nodeType = QtNode::QT_ADDCELLS;


QtAddCells::QtAddCells()
    : QtCondense(Ops::OP_SUM)
{
}


QtAddCells::QtAddCells(QtOperation* inputNew)
    : QtCondense(Ops::OP_SUM, inputNew)
{
}


QtData*
QtAddCells::evaluate(QtDataList* inputList)
{
    startTimer("QtAddCells");
    r_Minterval dummyint;

    QtData* returnValue = QtCondense::computeFullCondense(inputList, dummyint);
    stopTimer();

    return returnValue;
}



const QtAvgCells::QtNodeType QtAvgCells::nodeType = QtNode::QT_AVGCELLS;


QtAvgCells::QtAvgCells()
    : QtCondense(Ops::OP_SUM)
{
}


QtAvgCells::QtAvgCells(QtOperation* inputNew)
    : QtCondense(Ops::OP_SUM, inputNew)
{
}


QtData*
QtAvgCells::evaluate(QtDataList* inputList)
{
    startTimer("QtAvgCells");

    // domain for condensing operation
    r_Minterval areaOp;

    QtData* dataCond = QtCondense::computeFullCondense(inputList, areaOp);

    //
    // divide by the number of cells
    //

    QtScalarData* scalarDataResult = NULL;
    QtScalarData* scalarDataCond   = static_cast<QtScalarData*>(dataCond);
    BaseType*     resultType       = static_cast<BaseType*>(const_cast<Type*>(dataStreamType.getType()));


    // allocate memory for the result
    char* resultBuffer = new char[ resultType->getSize() ];

    // allocate ulong constant with number of cells
    r_ULong constValue = scalarDataCond->getTotalValuesCount() - dataCond->getNullValuesCount();
    if (constValue == 0)
    {
        constValue = 1;
    }
    const BaseType*     constType   = TypeFactory::mapType("ULong");
    char*         constBuffer = new char[ constType->getSize() ];

    constType->makeFromCULong(constBuffer, &constValue);

#ifdef DEBUG
    LTRACE << "Number of cells....: ";
    constType->printCell(RMInit::dbgOut, constBuffer);
#endif

    Ops::execBinaryConstOp(Ops::OP_DIV, resultType,
                           scalarDataCond->getValueType(),   constType,
                           resultBuffer,
                           scalarDataCond->getValueBuffer(), constBuffer);

    delete[] constBuffer;
    constBuffer = NULL;
    if (dataCond)
    {
        dataCond->deleteRef();
    }

    if (resultType->getType() == STRUCT)
    {
        scalarDataResult = new QtComplexData();
    }
    else
    {
        scalarDataResult = new QtAtomicData();
    }

    scalarDataResult->setValueType(resultType);
    scalarDataResult->setValueBuffer(resultBuffer);

#ifdef DEBUG
    LTRACE << "Result.............: ";
    scalarDataResult->printStatus(RMInit::dbgOut);
#endif

    stopTimer();
    return scalarDataResult;
}


const QtMinCells::QtNodeType QtMinCells::nodeType = QtNode::QT_MINCELLS;


QtMinCells::QtMinCells()
    : QtCondense(Ops::OP_MIN)
{
}


QtMinCells::QtMinCells(QtOperation* inputNew)
    : QtCondense(Ops::OP_MIN, inputNew)
{
}


QtData*
QtMinCells::evaluate(QtDataList* inputList)
{
    startTimer("QtMinCells");
    r_Minterval dummyint;

    QtData* returnValue = QtCondense::computeFullCondense(inputList, dummyint);

    stopTimer();

    return returnValue;
}



const QtMaxCells::QtNodeType QtMaxCells::nodeType = QtNode::QT_MAXCELLS;


QtMaxCells::QtMaxCells()
    : QtCondense(Ops::OP_MAX)
{
}


QtMaxCells::QtMaxCells(QtOperation* inputNew)
    : QtCondense(Ops::OP_MAX, inputNew)
{
}


QtData*
QtMaxCells::evaluate(QtDataList* inputList)
{
    startTimer("QtMaxCells");
    r_Minterval dummyint;

    QtData* returnValue = QtCondense::computeFullCondense(inputList, dummyint);

    stopTimer();

    return returnValue;
}


QtStdDevVar::QtStdDevVar(QtNodeType newNodeType)
    : QtCondense(Ops::OP_SQSUM), nodeType(newNodeType)
{
}


QtStdDevVar::QtStdDevVar(QtOperation* inputNew, QtNodeType newNodeType)
    : QtCondense(Ops::OP_SQSUM, inputNew), nodeType(newNodeType)
{
}


QtData*
QtStdDevVar::evaluate(QtDataList* inputList)
{
    startTimer("QtStdDevVar");
    r_Minterval dummyint1;
    r_Minterval dummyint2;

    QtScalarData* res = new QtAtomicData();

    // Computing \sum_i x_i^2 - (\sum_i x_i)^2 / n

    // sum of squares
    QtData* sqSum = QtCondense::computeFullCondense(inputList, dummyint1);
  
    // sum of elements
    QtCondense* tmp = new QtCondense(Ops::OP_SUM, input);
    QtData* sum = tmp->computeFullCondense(inputList, dummyint2);


    QtScalarData* scalarSum    = static_cast<QtScalarData*>(sum);
    BaseType*     resultType   = static_cast<BaseType*>(const_cast<Type*>(dataStreamType.getType()));
    char* resultBuffer = new char[ resultType->getSize() ];

    Ops::execUnaryConstOp(Ops::OP_CAST_DOUBLE, resultType, scalarSum->getValueType(),
                          resultBuffer, scalarSum->getValueBuffer()); 

    Ops::execBinaryConstOp(Ops::OP_MULT, resultType,
                           resultType,   resultType,
                           resultBuffer,
                           resultBuffer, resultBuffer);

  
    // allocate ulong constant with number of cells
    r_ULong constValue  = scalarSum->getTotalValuesCount() - sum->getNullValuesCount();
    if (constValue == 0)
    {
        constValue = 1;
    }
    const BaseType*     constType   = TypeFactory::mapType("ULong");
    char*               constBuffer = new char[ constType->getSize() ];

    constType->makeFromCULong(constBuffer, &constValue);

    Ops::execBinaryConstOp(Ops::OP_DIV, resultType,
                           resultType,   constType,
                           resultBuffer,
                           resultBuffer, constBuffer);


    QtScalarData* scalarSqSum = static_cast<QtScalarData*>(sqSum);

    Ops::execBinaryConstOp(Ops::OP_MINUS, resultType,
                           scalarSqSum->getValueType(),   resultType,
                           resultBuffer,
                           scalarSqSum->getValueBuffer(), resultBuffer);

    // end of computation

    if (nodeType == QT_VARSAMP || nodeType == QT_STDDEVSAMP)
    {
      if (constValue>1)
      {
        constValue --;
      }
      constType->makeFromCULong(constBuffer, &constValue);
    }

    Ops::execBinaryConstOp(Ops::OP_DIV, resultType,
                           resultType,   constType,
                           resultBuffer,
                           resultBuffer, constBuffer);

    if (nodeType == QT_STDDEVPOP || nodeType == QT_STDDEVSAMP)
    {
      Ops::execUnaryConstOp(Ops::OP_SQRT, resultType, resultType, 
                            resultBuffer, resultBuffer);
    }

    res->setValueType(resultType);
    res->setValueBuffer(resultBuffer);

    delete[] constBuffer;
    constBuffer = NULL;
    if (scalarSum)
    {
        scalarSum->deleteRef();
    }
    if (scalarSqSum)
    {
        scalarSqSum->deleteRef();
    }
    
    stopTimer();

    return res;
}
