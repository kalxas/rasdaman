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


#include "qlparser/qtcondense.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtscalardata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtbinaryinduce.hh"
#include "qlparser/qtbinaryinduce2.hh"
#include "mddmgr/mddobj.hh"
#include "catalogmgr/ops.hh"
#include "tilemgr/tile.hh"
#include "relcatalogif/typefactory.hh"
#include "relcatalogif/mddbasetype.hh"
#include "relcatalogif/complextype.hh"

#include <logging.hh>

#include <iostream>
#include <string>
#include "config.h"

using namespace std;

const QtNode::QtNodeType QtCondense::nodeType = QtNode::QT_CONDENSE;

QtCondense::QtCondense(Ops::OpType newOpType)
    : QtUnaryOperation(), opType(newOpType)
{
}



QtCondense::QtCondense(Ops::OpType newOpType, QtOperation *initInput)
    : QtUnaryOperation(initInput), opType(newOpType)
{
}


QtNode::QtAreaType
QtCondense::getAreaType()
{
    return QT_AREA_SCALAR;
}



void
QtCondense::optimizeLoad(QtTrimList *trimList)
{
    // reset trimList because optimization enters a new MDD area

    // delete list
    // release( trimList->begin(), trimList->end() );
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



QtData *
QtCondense::computeFullCondense(QtDataList *inputList, r_Minterval &areaOp)
{
    QtScalarData *returnValue = NULL;

    // get the operand
    QtData *operand = input->evaluate(inputList);

    if (operand)
    {
        QtMDD *mdd = static_cast<QtMDD *>(operand);

        const BaseType *resultType;
        try {
            resultType = Ops::getResultType(opType, mdd->getCellType());
            if (!resultType) throw r_Error(OPERANDTYPENOTSUPPORTED);
        } catch (r_Error &e) {
            LERROR << "operation " << opType << " is not supported on the given operands.";
            operand->deleteRef();
            parseInfo.setErrorNo(static_cast<int>(e.get_errorno()));
            throw parseInfo;
        }

        // get the MDD object
        MDDObj *op = mdd->getMDDObject();
        auto *nullValues = op->getNullValues();

        //  get the area, where the operation has to be applied
        areaOp = mdd->getLoadDomain();

        // get all tiles in relevant area
        unique_ptr<vector<std::shared_ptr<Tile>>> allTiles;
        allTiles.reset(op->intersect(areaOp));

        // create result object
        if (resultType->getType() == STRUCT)
            returnValue = new QtComplexData();
        else
            returnValue = new QtAtomicData();

        // get new operation object
        unique_ptr<CondenseOp> condOp;
        condOp.reset(Ops::getCondenseOp(opType, resultType, mdd->getCellType()));
        if (!condOp)
        {
            LERROR << "condense not supported on operands of type " << mdd->getCellType()->getType();
            parseInfo.setErrorNo(OPERANDTYPENOTSUPPORTED);
            throw parseInfo;
        }
        condOp->setNullValues(nullValues);

        size_t resultTypeSize = resultType->getSize();
        size_t tileNo = allTiles->size();
        size_t partialResultsSize = tileNo * resultTypeSize;
        char *partialResults = new char[partialResultsSize];
        memset(partialResults, 0, partialResultsSize);
        unsigned long totalValuesCount = 0;
        size_t nullValuesCount = 0;
        unique_ptr<r_Area[]> tileCellCount(new r_Area[tileNo]);

        // iterate over all tiles
        for (size_t i = 0; i < tileNo; i++)
        {
            auto tile = allTiles->at(i);
            r_Minterval tileDom = tile->getDomain();

            // domain of the relevant area of the actual tile
            r_Minterval intersectDom = tileDom.create_intersection(areaOp);
            totalValuesCount += intersectDom.cell_count();

            tile->execCondenseOp(condOp.get(), intersectDom);
        }


        // create result object
        if (resultType->getType() == STRUCT)
            returnValue = new QtComplexData();
        else
            returnValue = new QtAtomicData();

        returnValue->setNullValuesCount(condOp->getNullValuesCount());
        returnValue->setTotalValuesCount(totalValuesCount);

        // allocate buffer for the result
        char *resultBuffer = new char[resultType->getSize()];
        memcpy(resultBuffer, condOp->getAccuVal(), resultType->getSize());

        returnValue->setValueType(resultType);
        returnValue->setValueBuffer(resultBuffer);

        // delete old operand
        operand->deleteRef();
    }

    return returnValue;
}



const QtTypeElement &
QtCondense::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {
        // get input types
        const QtTypeElement &inputType = input->checkType(typeTuple);
        if (inputType.getDataType() != QT_MDD)
        {
            LERROR << "operand of condenser must be an array, but got " << inputType.getDataType();
            parseInfo.setErrorNo(QUANTIFIEROPERANDNOTMULTIDIMENSIONAL);
            throw parseInfo;
        }

        const BaseType *baseType = getBaseType(inputType);
        const BaseType *resultBaseType;
        try {
            resultBaseType = Ops::getResultType(opType, baseType);
            if (!resultBaseType) throw r_Error(OPERANDTYPENOTSUPPORTED);
        } catch (r_Error &e) {
            LERROR << "condenser cannot be applied on operand of the given type.";
            parseInfo.setErrorNo(static_cast<int>(e.get_errorno()));
            throw parseInfo;
        }

        if (getNodeType() == QT_AVGCELLS)
        {
            try {
                resultBaseType = Ops::getResultType(Ops::OP_DIV, resultBaseType, TypeFactory::mapType("Double"));
                if (!resultBaseType) throw r_Error(BININDUCE_BASETYPESINCOMPATIBLE);
            } catch (r_Error &e) {
                LERROR << "division cannot be applied on operands of the given types.";
                parseInfo.setErrorNo(static_cast<int>(e.get_errorno()));
                throw parseInfo;
            }
        }
        else if (getNodeType() >= QT_VARPOP && getNodeType() <= QT_STDDEVSAMP)
        {
            try {
                resultBaseType = Ops::getResultType(Ops::OP_CAST_DOUBLE, resultBaseType);
                if (!resultBaseType) throw r_Error(OPERANDTYPENOTSUPPORTED);
            } catch (r_Error &e) {
                LERROR << "cast cannot be applied on operands of the given types.";
                parseInfo.setErrorNo(static_cast<int>(e.get_errorno()));
                throw parseInfo;
            }
        }

        dataStreamType.setType(resultBaseType);
    }
    else
    {
        LERROR << "operand branch invalid.";
    }

    return dataStreamType;
}


void
QtCondense::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << getClassName() << " object" << getEvaluationTime() << endl;

    QtUnaryOperation::printTree(tab, s, mode);
}



void
QtCondense::printAlgebraicExpression(ostream &s)
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


QtSome::QtSome(QtOperation *inputNew)
    : QtCondense(Ops::OP_SOME, inputNew)
{
}


QtData *
QtSome::evaluate(QtDataList *inputList)
{
    startTimer("Qt");

    QtData *returnValue = NULL;
    r_ULong dummy = 0; // needed for conversion to and from CULong

    // get the operand
    QtData *operand = input->evaluate(inputList);

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

        QtMDD *mdd = static_cast<QtMDD *>(operand);

        // get result type
        BaseType *resultType = static_cast<BaseType *>(const_cast<Type *>(dataStreamType.getType()));

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
    MDDObj *op = mdd->getMDDObject();

    //  get the area, where the operation has to be applied
    r_Minterval areaOp = mdd->getLoadDomain();

    // get all tiles in relevant area
    auto *allTiles = op->intersect(areaOp);

    // allocate buffer for the result
    unsigned int typeSize = resultType->getSize();
    char *resultBuffer = new char[typeSize];

    // initialize result buffer with false
    dummy = 0;
    resultType->makeFromCULong(resultBuffer, &dummy);
    CondenseOp *condOp = Ops::getCondenseOp(Ops::OP_SOME, resultType, resultBuffer, resultType, 0, 0);

    // and iterate over them
    for (auto tileIt = allTiles->begin(); tileIt !=  allTiles->end() && !dummy ; tileIt++)
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


QtAll::QtAll(QtOperation *inputNew)
    : QtCondense(Ops::OP_ALL, inputNew)
{
}


QtData *
QtAll::evaluate(QtDataList *inputList)
{
    startTimer("QtAll");

    QtData *returnValue = NULL;
    r_ULong dummy = 0; // needed for conversion to and from CULong

    // get the operand
    QtData *operand = input->evaluate(inputList);

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

        QtMDD *mdd = static_cast<QtMDD *>(operand);

        // get result type
        const BaseType *resultType = static_cast<BaseType *>(const_cast<Type *>(dataStreamType.getType()));

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
    MDDObj *op = (static_cast<QtMDD *>(operand))->getMDDObject();

    //  get the area, where the operation has to be applied
    r_Minterval areaOp = mdd->getLoadDomain();

    // get all tiles in relevant area
    auto *allTiles = op->intersect(areaOp);

    // allocate buffer for the result
    unsigned int tempTypeSize = resultType->getSize();
    char *resultBuffer = new char[tempTypeSize];

    // initialize result buffer with true
    dummy = 1;
    resultType->makeFromCULong(resultBuffer, &dummy);
    CondenseOp *condOp = Ops::getCondenseOp(Ops::OP_ALL, resultType, resultBuffer, resultType, 0, 0);

    for (auto tileIt = allTiles->begin(); tileIt != allTiles->end() && dummy; tileIt++)
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


QtCountCells::QtCountCells(QtOperation *inputNew)
    : QtCondense(Ops::OP_COUNT, inputNew)
{
}


QtData *
QtCountCells::evaluate(QtDataList *inputList)
{
    startTimer("QtCountCells");
    r_Minterval dummyint;
    QtData *returnValue = QtCondense::computeFullCondense(inputList, dummyint);
    stopTimer();

    return returnValue;
}



const QtAddCells::QtNodeType QtAddCells::nodeType = QtNode::QT_ADDCELLS;


QtAddCells::QtAddCells()
    : QtCondense(Ops::OP_SUM)
{
}


QtAddCells::QtAddCells(QtOperation *inputNew)
    : QtCondense(Ops::OP_SUM, inputNew)
{
}


QtData *
QtAddCells::evaluate(QtDataList *inputList)
{
    startTimer("QtAddCells");
    r_Minterval dummyint;

    QtData *returnValue = QtCondense::computeFullCondense(inputList, dummyint);
    stopTimer();

    return returnValue;
}



const QtAvgCells::QtNodeType QtAvgCells::nodeType = QtNode::QT_AVGCELLS;


QtAvgCells::QtAvgCells()
    : QtCondense(Ops::OP_SUM)
{
}


QtAvgCells::QtAvgCells(QtOperation *inputNew)
    : QtCondense(Ops::OP_SUM, inputNew)
{
}


QtData *
QtAvgCells::evaluate(QtDataList *inputList)
{
    startTimer("QtAvgCells");

    // domain for condensing operation
    r_Minterval areaOp;

    QtData *dataCond = QtCondense::computeFullCondense(inputList, areaOp);

    //
    // divide by the number of cells
    //

    QtScalarData *scalarDataResult = NULL;
    QtScalarData *scalarDataCond   = static_cast<QtScalarData *>(dataCond);
    BaseType     *resultType;
    BaseType     *inputValueType  = const_cast<BaseType *>(scalarDataCond->getValueType());
    char         *inputBuffer     = NULL;
    // allocate memory for the result
    char *resultBuffer = NULL;

    // allocate ulong constant with number of cells
    r_ULong constValue = scalarDataCond->getTotalValuesCount() - dataCond->getNullValuesCount();
    if (constValue == 0)
    {
        constValue = 1;
    }
    const auto inpType = inputValueType->getType();
    char *constBuffer;
    const BaseType     *constType;
    if (isComplexType(inpType))
    {
        double constValueD = 0;
        constValueD = static_cast<double>(constValue);
        if (inpType == COMPLEXTYPE1 || inpType == CINT16)
        {
            constType   = TypeFactory::mapType("Float");
            resultType = const_cast<BaseType *>(TypeFactory::mapType("Complex"));

        }
        else
        {
            constType   = TypeFactory::mapType("Double");
            resultType = const_cast<BaseType *>(TypeFactory::mapType("Complexd"));
        }
        
        if (inpType == CINT16 || inpType == CINT32)
        {
            size_t inReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(inputValueType)))->getReOffset();
            size_t inImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(inputValueType)))->getImOffset();
            r_Long inRe;
            r_Long inIm;
            inRe = *(inputValueType->convertToCLong(const_cast<char *>(scalarDataCond->getValueBuffer()) + inReOff, &inRe));
            inIm = *(inputValueType->convertToCLong(const_cast<char *>(scalarDataCond->getValueBuffer()) + inImOff, &inIm));
            
            if (inpType == CINT16)
                inputValueType = const_cast<BaseType *>(TypeFactory::mapType("Complex"));
            else
                inputValueType = const_cast<BaseType *>(TypeFactory::mapType("Complexd"));
            inputBuffer = new char[ constType->getSize() * 2 ];
            double resRe = inRe;
            double resIm = inIm;
            size_t inResReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(resultType)))->getReOffset();
            size_t inResImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(resultType)))->getImOffset();
            inputValueType->makeFromCDouble(inputBuffer + inResReOff, &resRe);
            inputValueType->makeFromCDouble(inputBuffer + inResImOff, &resIm);
    
        }
        else
        {
            inputBuffer     = const_cast<char *>(scalarDataCond->getValueBuffer());
        }
        
        constBuffer = new char[ constType->getSize() ];
        constType->makeFromCDouble(constBuffer, &constValueD);
    }
    else
    {
        inputBuffer     = const_cast<char *>(scalarDataCond->getValueBuffer());
        constType   = TypeFactory::mapType("ULong");
        constBuffer = new char[ constType->getSize() ];
        resultType       = static_cast<BaseType *>(const_cast<Type *>(dataStreamType.getType()));
        constType->makeFromCULong(constBuffer, &constValue);
       
    }
resultBuffer = new char[ resultType->getSize() ];

#ifdef DEBUG
    LTRACE << "Number of cells....: ";
    constType->printCell(RMInit::dbgOut, constBuffer);
#endif

    Ops::execBinaryConstOp(Ops::OP_DIV, resultType,
                           inputValueType,   constType,
                           resultBuffer,
                           inputBuffer, constBuffer);

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


QtMinCells::QtMinCells(QtOperation *inputNew)
    : QtCondense(Ops::OP_MIN, inputNew)
{
}


QtData *
QtMinCells::evaluate(QtDataList *inputList)
{
    startTimer("QtMinCells");
    r_Minterval dummyint;

    QtData *returnValue = QtCondense::computeFullCondense(inputList, dummyint);

    stopTimer();

    return returnValue;
}



const QtMaxCells::QtNodeType QtMaxCells::nodeType = QtNode::QT_MAXCELLS;


QtMaxCells::QtMaxCells()
    : QtCondense(Ops::OP_MAX)
{
}


QtMaxCells::QtMaxCells(QtOperation *inputNew)
    : QtCondense(Ops::OP_MAX, inputNew)
{
}


QtData *
QtMaxCells::evaluate(QtDataList *inputList)
{
    startTimer("QtMaxCells");
    r_Minterval dummyint;

    QtData *returnValue = QtCondense::computeFullCondense(inputList, dummyint);

    stopTimer();

    return returnValue;
}


QtStdDevVar::QtStdDevVar(QtNodeType newNodeType)
    : QtCondense(Ops::OP_SQSUM), nodeType(newNodeType)
{
}


QtStdDevVar::QtStdDevVar(QtOperation *inputNew, QtNodeType newNodeType)
    : QtCondense(Ops::OP_SQSUM, inputNew), nodeType(newNodeType)
{
}


QtData *
QtStdDevVar::evaluate(QtDataList *inputList)
{
    startTimer("QtStdDevVar");
    r_Minterval dummyint1;
    r_Minterval dummyint2;

    QtScalarData *res = new QtAtomicData();

    // Computing \sum_i x_i^2 - (\sum_i x_i)^2 / n

    // sum of squares
    QtData *sqSum = QtCondense::computeFullCondense(inputList, dummyint1);

    // sum of elements
    QtCondense *tmp = new QtCondense(Ops::OP_SUM, input);
    QtData *sum = tmp->computeFullCondense(inputList, dummyint2);


    QtScalarData *scalarSum    = static_cast<QtScalarData *>(sum);
    BaseType     *resultType   = static_cast<BaseType *>(const_cast<Type *>(dataStreamType.getType()));
    char *resultBuffer = new char[ resultType->getSize() ];

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
    const BaseType     *constType   = TypeFactory::mapType("ULong");
    char               *constBuffer = new char[ constType->getSize() ];

    constType->makeFromCULong(constBuffer, &constValue);

    Ops::execBinaryConstOp(Ops::OP_DIV, resultType,
                           resultType,   constType,
                           resultBuffer,
                           resultBuffer, constBuffer);


    QtScalarData *scalarSqSum = static_cast<QtScalarData *>(sqSum);

    Ops::execBinaryConstOp(Ops::OP_MINUS, resultType,
                           scalarSqSum->getValueType(),   resultType,
                           resultBuffer,
                           scalarSqSum->getValueBuffer(), resultBuffer);

    // end of computation

    if (nodeType == QT_VARSAMP || nodeType == QT_STDDEVSAMP)
    {
        if (constValue > 1)
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
