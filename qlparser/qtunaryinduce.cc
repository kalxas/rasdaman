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

#include "qlparser/qtunaryinduce.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtnode.hh"

#include "mddmgr/mddobj.hh"
#include "tilemgr/tile.hh"

#include "relcatalogif/typefactory.hh"
#include "relcatalogif/structtype.hh"
#include "relcatalogif/basetype.hh"
#include "relcatalogif/mdddimensiontype.hh"
#include "relcatalogif/syntaxtypes.hh"

#include <logging.hh>

#include <sstream>
#include <memory>
#include <string>

using namespace std;

const QtNode::QtNodeType QtUnaryInduce::nodeType = QtNode::QT_UNARY_INDUCE;

QtUnaryInduce::QtUnaryInduce(QtOperation *initInput)
    :  QtUnaryOperation(initInput)
{
}

const QtTypeElement &QtUnaryInduce::checkOperandType(Ops::OpType optype, QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {
        // get input types
        const QtTypeElement &inputType = input->checkType(typeTuple);
        const BaseType *baseType = getBaseType(inputType);
        if (baseType != nullptr)
        {
            const BaseType *resultBaseType;
            try {
                resultBaseType = Ops::getResultType(optype, baseType);
                if (!resultBaseType) throw r_Error(UNARY_INDUCE_BASETYPENOTSUPPORTED);
            } catch (r_Error &e) {
                LERROR << "operation not applicable on operand of the given type.";
                parseInfo.setErrorNo(static_cast<int>(e.get_errorno()));
                throw parseInfo;
            }
            if (inputType.getDataType() == QT_MDD)
            {
                MDDBaseType *resultMDDType = new MDDBaseType("tmp", resultBaseType);
                TypeFactory::addTempType(resultMDDType);
                dataStreamType.setType(resultMDDType);
            }
            else
            {
                dataStreamType.setType(resultBaseType);
            }
        }
        else
        {
            LERROR << "operation is not supported on the given operand type.";
            parseInfo.setErrorNo(STRINGSNOTSUPPORTED);
            throw parseInfo;
        }
    }
    else
    {
        LERROR << "operand branch invalid.";
    }
    return dataStreamType;
}



bool
QtUnaryInduce::getOperand(QtDataList *inputList, QtData *&operand)
{
    bool success = false;

    // get the operands
    operand = input->evaluate(inputList);

    // Test, if the operands are valid.
    if (operand)
        success = true;
    else
        LDEBUG << "operand is not provided.";

    return success;
}



QtData *
QtUnaryInduce::computeOp(QtData *operand, Ops::OpType operation, double param)
{
    const BaseType *operandBaseType;
    if (operand->getDataType() == QT_MDD)
        operandBaseType = static_cast<QtMDD *>(operand)->getCellType();
    else if (operand->isScalarData())
        operandBaseType = static_cast<QtScalarData *>(operand)->getValueType();
    else
        throw r_Error(OPERANDTYPENOTSUPPORTED);
    
    const BaseType *resultBaseType;
    try {
        resultBaseType = Ops::getResultType(operation, operandBaseType);
        if (!resultBaseType) throw r_Error(OPERANDTYPENOTSUPPORTED);
    } catch (r_Error &e) {
        LERROR << "Unary induced operation not applicable on operand of the given type.";
        parseInfo.setErrorNo(static_cast<int>(e.get_errorno()));
        throw parseInfo;
    }
    
    QtData *returnValue = NULL;
    if (operand->getDataType() == QT_MDD)
    {
        QtMDD *mdd = static_cast<QtMDD *>(operand);
        returnValue = computeUnaryMDDOp(mdd, resultBaseType, operation, 0, param);
        (static_cast<QtMDD *>(returnValue))->setFromConversion(mdd->isFromConversion());
    }
    else if (operand->isScalarData())
    {
        QtScalarData *scalar = static_cast<QtScalarData *>(operand);
        returnValue = computeUnaryOp(scalar, resultBaseType, operation, 0, param);
    }

    return returnValue;
}



QtData *
QtUnaryInduce::computeUnaryMDDOp(QtMDD *operand, const BaseType *resultBaseType,
                                 Ops::OpType operation, unsigned int operandOffset, double param)
{
    // get the MDD object
    MDDObj *op = (static_cast<QtMDD *>(operand))->getMDDObject();
    auto *nullValues = op->getNullValues();

    //  get the area, where the operation has to be applied
    const r_Minterval &areaOp = (static_cast<QtMDD *>(operand))->getLoadDomain();

    // get all tiles in relevant area
    unique_ptr<vector<std::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->intersect(areaOp));

    auto tileIt = allTiles->begin();

    unique_ptr<UnaryOp> myOp;
    if (operation == Ops::OP_IDENTITY)
    {
        myOp.reset(Ops::getUnaryOp(operation, resultBaseType, resultBaseType, 0, operandOffset));
    }
    else
    {
        myOp.reset(Ops::getUnaryOp(operation, resultBaseType, op->getCellType(), 0, 0));
    }
    if (!myOp)
    {
        LERROR << "could not get operation for result type " <<
               resultBaseType->getName() << " argument type " << (*tileIt)->getType() << " operation " << static_cast<int>(operation);
        parseInfo.setErrorNo(UNARY_INDUCE_BASETYPENOTSUPPORTED);
        throw parseInfo;
    }
    myOp->setNullValues(nullValues);

    // create MDDObj for result
    const r_Dimension dim = areaOp.dimension();
    MDDDimensionType *mddDimensionType = new MDDDimensionType("tmp", resultBaseType, dim);
    MDDBaseType *mddBaseType = static_cast<MDDBaseType *>(mddDimensionType);
    TypeFactory::addTempType(mddBaseType);
    unique_ptr<MDDObj> mddres;
    mddres.reset(new MDDObj(mddBaseType, areaOp, op->getNullValues()));

    if (tileIt != allTiles->end())
    {
        // set exponent for pow operations
        if (operation == Ops::OP_POW)
        {
            (static_cast<OpPOWCDouble *>(myOp.get()))->setExponent(param);
        }

        // and iterate over them
        try
        {
            for (; tileIt !=  allTiles->end(); tileIt++)
            {
                // domain of the actual tile
                const r_Minterval &tileDom = (*tileIt)->getDomain();

                // domain of the relevant area of the actual tile
                r_Minterval intersectDom(tileDom.create_intersection(areaOp));

                // create tile for result
                unique_ptr<Tile> resTile;
                resTile.reset(new Tile(intersectDom, resultBaseType));

                // carry out operation on the relevant area of the tiles
                resTile->execUnaryOp(myOp.get(), intersectDom, tileIt->get(), intersectDom);
                // insert Tile in result mdd
                mddres->insertTile(resTile.release());
            }
        }
        catch (r_Error &err)
        {
            LERROR << "QtUnaryInduce::computeUnaryMDDOp caught " << err.get_errorno() << " " << err.what();
            parseInfo.setErrorNo(err.get_errorno());
            throw parseInfo;
        }
        catch (int err)
        {
            LERROR << "QtUnaryInduce::computeUnaryMDDOp caught errno error (" << err << ") in unaryinduce";
            parseInfo.setErrorNo(err);
            throw parseInfo;
        }
    }

    // create a new QtMDD object as carrier object for the transient MDD object
    QtData *returnValue = new QtMDD(mddres.release());
    returnValue->cloneNullValues(myOp.get());

    return returnValue;
}


QtData *
QtUnaryInduce::computeUnaryOp(QtScalarData *operand, const BaseType *resultBaseType,
                              Ops::OpType operation, unsigned int operandOffset, double param)
{
    QtScalarData *scalarDataObj = NULL;
    auto *nullValues = operand->getNullValues();

    // allocate memory for the result
    char *resultBuffer = new char[ resultBaseType->getSize() ];

#ifdef DEBUG
    LTRACE << "Operand value ";
    operand->getValueType()->printCell(RMInit::dbgOut, operand->getValueBuffer());
#endif

    UnaryOp *myOp = NULL;

    try
    {
        if ((operation == Ops::OP_IDENTITY))   // || ( operation == Ops::OP_SQRT ))
        {
            // operand type is the same as result type
            myOp = Ops::getUnaryOp(operation, resultBaseType, resultBaseType, 0, operandOffset);
            myOp->setNullValues(nullValues);
            // set exponent for pow operations
            if (operation == Ops::OP_POW)
            {
                (static_cast<OpPOWCDouble *>(myOp))->setExponent(param);
            }

            (*myOp)(resultBuffer, operand->getValueBuffer());
        }
        else
            try
            {
                myOp = Ops::getUnaryOp(operation, resultBaseType, operand->getValueType(), 0, operandOffset);
                myOp->setNullValues(nullValues);
                // set exponent for pow operations
                if (operation == Ops::OP_POW)
                {
                    if (resultBaseType->getType() == STRUCT)
                    {
                        (static_cast<OpUnaryStruct *>(myOp))->setExponent(param);
                    }
                    else
                    {
                        (static_cast<OpPOWCDouble *>(myOp))->setExponent(param);
                    }
                }
                (*myOp)(resultBuffer, operand->getValueBuffer());
            }
            catch (int err)
            {
                delete[] resultBuffer;
                resultBuffer = NULL;
                parseInfo.setErrorNo(err);
                throw parseInfo;
            }
    }
    catch (...)
    {
        delete myOp;  // cleanup
        throw;
    }


#ifdef DEBUG
    LTRACE << "Result value ";
    resultBaseType->printCell(RMInit::dbgOut, resultBuffer);
#endif

    if (resultBaseType->getType() == STRUCT)
    {
        scalarDataObj = new QtComplexData();
    }
    else
    {
        scalarDataObj = new QtAtomicData();
    }

    scalarDataObj->setValueType(resultBaseType);
    scalarDataObj->setValueBuffer(resultBuffer);
    if (myOp != NULL)
    {
        scalarDataObj->cloneNullValues(myOp);
    }

    delete myOp;

    return scalarDataObj;
}


const QtNode::QtNodeType QtNot::nodeType = QtNode::QT_NOT;

QtNot::QtNot(QtOperation *initInput)
    :  QtUnaryInduce(initInput)
{
}


QtData *
QtNot::evaluate(QtDataList *inputList)
{
    startTimer("QtNot");
    QtData *returnValue = NULL;
    QtData *operand = NULL;

    if (getOperand(inputList, operand))
    {
        returnValue = computeOp(operand, Ops::OP_NOT);

        // delete old operand
        if (operand)
        {
            operand->deleteRef();
        }
    }

    stopTimer();

    return returnValue;
}



void
QtNot::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtNot Object" << getEvaluationTime() << endl;

    QtUnaryInduce::printTree(tab + 2, s, mode);
}



void
QtNot::printAlgebraicExpression(ostream &s)
{
    s << "not(";

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
QtNot::checkType(QtTypeTuple *typeTuple)
{
    return checkOperandType(Ops::OP_NOT, typeTuple);
}



const QtNode::QtNodeType QtIsNull::nodeType = QtNode::QT_IS_NULL;

QtIsNull::QtIsNull(QtOperation *initInput)
    :  QtUnaryInduce(initInput)
{
}


QtData *
QtIsNull::evaluate(QtDataList *inputList)
{
    startTimer("QtIsNull");
    QtData *returnValue = NULL;
    QtData *operand = NULL;

    if (getOperand(inputList, operand))
    {
        returnValue = computeOp(operand, Ops::OP_IS_NULL);

        // delete old operand
        if (operand)
        {
            operand->deleteRef();
        }
    }

    stopTimer();

    return returnValue;
}



void
QtIsNull::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtIsNull Object" << getEvaluationTime() << endl;

    QtUnaryInduce::printTree(tab + 2, s, mode);
}



void
QtIsNull::printAlgebraicExpression(ostream &s)
{
    s << "isnull(";

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
QtIsNull::checkType(QtTypeTuple *typeTuple)
{
    return checkOperandType(Ops::OP_IS_NULL, typeTuple);
}



const QtNode::QtNodeType QtDot::nodeType = QtNode::QT_DOT;



QtDot::QtDot(const string &initElementName)
    :  QtUnaryInduce(NULL),
       elementName(initElementName),
       elementNo(-1)
{
}



QtDot::QtDot(unsigned initElementNo)
    :  QtUnaryInduce(NULL),
       elementNo(static_cast<int>(initElementNo))
{
}



bool
QtDot::equalMeaning(QtNode *node)
{
    bool result = false;

    if (nodeType == node->getNodeType())
    {
        QtDot *dotNode = static_cast<QtDot *>(node); // by force

        // In future, elementName have to be converted to elementNo
        // and then just the numbers are compared.
        if ((elementNo != -1 && elementNo == dotNode->elementNo)     ||
                (elementNo == -1 && elementName == dotNode->elementName))
        {
            result = input->equalMeaning(dotNode->getInput());
        }
    };

    return result;
}


string
QtDot::getSpelling()
{
    char tempStr[20];
    sprintf(tempStr, "%lud", static_cast<unsigned long>(getNodeType()));
    string result  = string(tempStr);

    if (elementNo == -1)
    {
        result.append(elementName);
    }
    else
    {
        std::ostringstream bufferStream;
        bufferStream << elementNo << ends;

        result.append(bufferStream.str());
    }

    result.append("(");
    result.append(input->getSpelling());
    result.append(")");

    return result;
}


QtData *
QtDot::evaluate(QtDataList *inputList)
{
    startTimer("QtDot");

    QtData *returnValue = NULL;
    QtData *operand = NULL;

    if (getOperand(inputList, operand))
    {
        if (operand->getDataType() == QT_MDD)
        {
            QtMDD *mdd = static_cast<QtMDD *>(operand);

#ifdef QT_RUNTIME_TYPE_CHECK
            // test, if operand has complex base type
            if (mdd->getCellType()->getType() != STRUCT)
            {
                LERROR << "Internal error in QtDot::evaluate() - "
                       << "runtime type checking failed.";

                // delete old operand
                if (operand)
                {
                    operand->deleteRef();
                }

                return 0;
            }
#endif

            StructType  *operandType    = static_cast<StructType *>(mdd->getCellType());
            unsigned int operandOffset;
            const BaseType    *resultCellType = NULL;

            if (elementNo == -1)
            {
                resultCellType = operandType->getElemType(elementName.c_str());
            }
            else
            {
                resultCellType = operandType->getElemType(static_cast<unsigned int>(elementNo));
            }

            if (!resultCellType)
            {
                LERROR << "struct selector is not valid.";
                parseInfo.setErrorNo(UNARY_INDUCE_STRUCTSELECTORINVALID);
                throw parseInfo;
            }

            if (elementNo == -1)
            {
                operandOffset = operandType->getOffset(elementName.c_str());
            }
            else
            {
                operandOffset = operandType->getOffset(static_cast<unsigned int>(elementNo));
            }

#ifdef DEBUG
            char *typeStructure = operandType->getTypeStructure();
            LTRACE << "Operand base type   " << operandType->getTypeName() << ", structure " << typeStructure;
            free(typeStructure);
            typeStructure = NULL;
            LTRACE << "Operand base offset " << operandOffset;
            typeStructure = resultCellType->getTypeStructure();
            LTRACE << "Result base type    " << resultCellType->getTypeName() << ", structure " << typeStructure;
            free(typeStructure);
            typeStructure = NULL;
#endif

            returnValue = computeUnaryMDDOp(mdd, resultCellType, Ops::OP_IDENTITY, operandOffset);
        }
        else if (operand->isScalarData())
        {
            QtScalarData *scalar = static_cast<QtScalarData *>(operand);

#ifdef QT_RUNTIME_TYPE_CHECK
            // test, if operand has complex base type
            if (scalar->getValueType()->getType() != STRUCT)
            {
                LERROR << "Internal error in QtDot::evaluate() - "
                       << "runtime type checking failed.";

                // delete old operand
                if (operand)
                {
                    operand->deleteRef();
                }

                return 0;
            }
#endif

            StructType  *operandType    = static_cast<StructType *>(const_cast<BaseType *>(scalar->getValueType()));
            unsigned int operandOffset;
            const BaseType    *resultCellType = NULL;

            if (elementNo == -1)
            {
                resultCellType = operandType->getElemType(elementName.c_str());
            }
            else
            {
                resultCellType = operandType->getElemType(static_cast<unsigned int>(elementNo));
            }

            if (!resultCellType)
            {
                LERROR << "struct selector is not valid.";
                parseInfo.setErrorNo(UNARY_INDUCE_STRUCTSELECTORINVALID);
                throw parseInfo;
            }

            if (elementNo == -1)
            {
                operandOffset = operandType->getOffset(elementName.c_str());
            }
            else
            {
                operandOffset = operandType->getOffset(static_cast<unsigned int>(elementNo));
            }

#ifdef DEBUG
            char *typeStructure = operandType->getTypeStructure();
            LTRACE << "Operand scalar type   " << operandType->getTypeName() << ", structure " << typeStructure;
            free(typeStructure);
            typeStructure = NULL;
            LTRACE << "Operand scalar offset " << operandOffset;
            typeStructure = resultCellType->getTypeStructure();
            LTRACE << "Result scalar type    " << resultCellType->getTypeName() << ", structure " << typeStructure;
            free(typeStructure);
            typeStructure = NULL;
#endif
            returnValue = computeUnaryOp(scalar, resultCellType, Ops::OP_IDENTITY, operandOffset);
        }
        else
        {
            LERROR << "operation is not supported for strings.";
            parseInfo.setErrorNo(STRINGSNOTSUPPORTED);
            throw parseInfo;
        }

        // delete old operand
        if (operand)
        {
            operand->deleteRef();
        }
    }

    stopTimer();

    return returnValue;
}



void
QtDot::printTree(int tab, ostream &s, QtChildType mode)
{
    if (elementNo == -1)
    {
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtDot Object: access " << elementName.c_str() << getEvaluationTime() << endl;
    }
    else
    {
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtDot Object: access no " << elementNo << getEvaluationTime() << endl;
    }

    QtUnaryInduce::printTree(tab + 2, s, mode);
}

void
QtDot::printAlgebraicExpression(ostream &s)
{
    s << "(";

    if (input)
    {
        input->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ").";

    if (elementNo == -1)
    {
        s << elementName.c_str();
    }
    else
    {
        s << elementNo;
    }

    s << " ";
}

const QtTypeElement &
QtDot::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {
        // get input types
        const QtTypeElement &inputType = input->checkType(typeTuple);

#ifdef DEBUG
        LTRACE << "Operand: ";
        inputType.printStatus(RMInit::dbgOut);
#endif
        const BaseType *baseType = getBaseType(inputType);
        if (baseType == nullptr)
        {
            LERROR << "induced dot operation is not supported on operand of the given type.";
            parseInfo.setErrorNo(STRINGSNOTSUPPORTED);
            throw parseInfo;
        }
        if (baseType->getType() != STRUCT)
        {
            LERROR << "operand of induce dot operation must be of composite type.";
            parseInfo.setErrorNo(UNARY_INDUCE_BASETYPEMUSTBECOMPLEX);
            throw parseInfo;
        }
        const StructType *structType = static_cast<const StructType *>(baseType);
        const BaseType *resultBaseType = nullptr;
        if (elementNo == -1)
            resultBaseType = structType->getElemType(elementName.c_str());
        else
            resultBaseType = structType->getElemType(static_cast<unsigned int>(elementNo));
        
        if (resultBaseType == nullptr)
        {
            LERROR << "struct selector is not valid.";
            parseInfo.setErrorNo(UNARY_INDUCE_STRUCTSELECTORINVALID);
            throw parseInfo;
        }
        
        if (inputType.getDataType() == QT_MDD)
        {
            MDDBaseType *resultMDDType = new MDDBaseType("tmp", resultBaseType);
            TypeFactory::addTempType(resultMDDType);
            dataStreamType.setType(resultMDDType);
        }
        else if (inputType.isBaseType())
        {
            dataStreamType.setType(resultBaseType);
        }
    }
    else
    {
        LERROR << "operand branch invalid.";
    }
    return dataStreamType;
}

//--------------------------------------------
//  QtCast
//--------------------------------------------

const QtNode::QtNodeType QtCast::nodeType = QtNode::QT_CAST;

QtCast::QtCast(QtOperation *initInput, cast_types t):
    QtUnaryInduce(initInput), castType(t) {}

QtCast::QtCast(QtOperation *input2, const char *typeName2): QtUnaryInduce(input2)
{
    static std::map<string, cast_types> baseCastTypes;
    baseCastTypes.emplace(SyntaxType::BOOL_NAME, t_bool);
    baseCastTypes.emplace(SyntaxType::CHAR_NAME, t_char);
    baseCastTypes.emplace(SyntaxType::OCTET_NAME, t_octet);
    baseCastTypes.emplace(SyntaxType::SHORT_NAME, t_short);
    baseCastTypes.emplace(SyntaxType::USHORT_NAME, t_ushort);
    baseCastTypes.emplace(SyntaxType::UNSIGNED_SHORT_NAME, t_ushort);
    baseCastTypes.emplace(SyntaxType::LONG_NAME, t_long);
    baseCastTypes.emplace(SyntaxType::ULONG_NAME, t_ulong);
    baseCastTypes.emplace(SyntaxType::UNSIGNED_LONG_NAME, t_ulong);
    baseCastTypes.emplace(SyntaxType::FLOAT_NAME, t_float);
    baseCastTypes.emplace(SyntaxType::DOUBLE_NAME, t_double);

    string dstTypeName(typeName2);
    auto findIt = baseCastTypes.find(dstTypeName);
    if (findIt != baseCastTypes.end())
        this->castType = findIt->second;
    else
        this->typeName = std::move(dstTypeName);
}

QtData *QtCast::evaluate(QtDataList *inputList)
{
    startTimer("QtCast");

    QtData *returnValue = NULL;
    QtData *operand = NULL;

    if (getOperand(inputList, operand))
    {
        if (typeName.empty())
        {
            // old style cast operator
            returnValue = computeOp(operand, getOp(castType));
        }
        else
        {
            const BaseType *resultType = TypeFactory::mapType(typeName.c_str());
            if (operand->getDataType() == QT_MDD)
            {
                auto *op = static_cast<QtMDD *>(operand);
                returnValue = computeUnaryMDDOp(op, resultType, Ops::OP_CAST_GENERAL);
                (static_cast<QtMDD *>(returnValue))->setFromConversion(op->isFromConversion());
            }
            else if (operand->isScalarData())
            {
                auto *op = static_cast<QtScalarData *>(operand);
                returnValue = computeUnaryOp(op, resultType, Ops::OP_CAST_GENERAL);
            }
        }
    }
    if (operand)
        operand->deleteRef();

    stopTimer();
    return returnValue;
}

void QtCast::printTree(int tab, ostream &s, QtChildType mode)
{
    const char *type_name[] =
    {
        "bool", "octet", "char", "short", "ushort",
        "long", "ulong", "float", "double"
    };
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCastObject "
      << static_cast<int>(getNodeType())
      << "<" << (typeName.empty() ? type_name[castType] : typeName.c_str()) << ">" << getEvaluationTime()
      << endl;
    QtUnaryInduce::printTree(tab + 2, s, mode);
}

void QtCast::printAlgebraicExpression(ostream &s)
{
    const char *type_name[] =
    {
        "bool", "octet", "char", "short", "ushort",
        "long", "ulong", "float", "double"
    };
    s << "cast<" << (typeName.empty() ? type_name[castType] : typeName.c_str()) << ">(";
    if (input)
        input->printAlgebraicExpression(s);
    else
        s << "<nn>";
    s << ")";
}

const QtTypeElement &QtCast::checkType(QtTypeTuple *typeTuple)
{
    if (typeName.empty())
    {
        return checkOperandType(getOp(castType), typeTuple);
    }
    else
    {
        dataStreamType.setDataType(QT_TYPE_UNKNOWN);
   
        if (input)
        {
            const QtTypeElement &inputType = input->checkType(typeTuple);
            const BaseType *baseType = getBaseType(inputType);
            if (baseType != nullptr)
            {
                const BaseType *resultBaseType = TypeFactory::mapType(typeName.c_str());
                
                if (inputType.getDataType() == QT_MDD)
                {
                    MDDBaseType *resultMDDType = new MDDBaseType("tmp", resultBaseType);
                    TypeFactory::addTempType(resultMDDType);
                    dataStreamType.setType(resultMDDType);
                }
                else
                {
                    dataStreamType.setType(resultBaseType);
                }
            }
            else
            {
                LERROR << "operation is not supported on the given operand type.";
                parseInfo.setErrorNo(STRINGSNOTSUPPORTED);
                throw parseInfo;
            }
        }
        else
        {
            LERROR << "operand branch invalid.";
        }
        return dataStreamType;

    }
}


//--------------------------------------------
//  QtRealPartOp
//--------------------------------------------

const QtNode::QtNodeType QtRealPartOp::nodeType = QtNode::QT_REALPART;

QtRealPartOp::QtRealPartOp(QtOperation *initInput): QtUnaryInduce(initInput) {}

QtData *QtRealPartOp::evaluate(QtDataList *inputList)
{
    startTimer("QtRealPartOp");

    QtData *returnValue = NULL;
    QtData *operand = NULL;

    if (getOperand(inputList, operand))
        returnValue = computeOp(operand, Ops::OP_REALPART);

    if (operand)
        operand->deleteRef();

    stopTimer();

    return returnValue;
}

void QtRealPartOp::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtRealPartOpObject " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;
    QtUnaryInduce::printTree(tab + 2, s, mode);
}

void QtRealPartOp::printAlgebraicExpression(ostream &s)
{
    s << "Re(";
    if (input)
        input->printAlgebraicExpression(s);
    else
        s << "<nn>";
    s << ")";
}

const QtTypeElement &QtRealPartOp::checkType(QtTypeTuple *typeTuple)
{
    return checkOperandType(Ops::OP_REALPART, typeTuple);
}

//--------------------------------------------
//  QtImaginarPartOp
//--------------------------------------------

const QtNode::QtNodeType QtImaginarPartOp::nodeType = QtNode::QT_IMAGINARPART;

QtImaginarPartOp::QtImaginarPartOp(QtOperation *initInput): QtUnaryInduce(initInput) {}

QtData *QtImaginarPartOp::evaluate(QtDataList *inputList)
{
    startTimer("QtImaginaryPartOp");

    QtData *returnValue = NULL;
    QtData *operand = NULL;

    if (getOperand(inputList, operand))
        returnValue = computeOp(operand, Ops::OP_IMAGINARPART);
    // delete old operand
    if (operand)
        operand->deleteRef();

    stopTimer();

    return returnValue;
}

void QtImaginarPartOp::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtImaginarPartObject " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;
    QtUnaryInduce::printTree(tab + 2, s, mode);
}

void QtImaginarPartOp::printAlgebraicExpression(ostream &s)
{
    s << "Im(";
    if (input)
        input->printAlgebraicExpression(s);
    else
        s << "<nn>";
    s << ")";
}

const QtTypeElement &QtImaginarPartOp::checkType(QtTypeTuple *typeTuple)
{
    return checkOperandType(Ops::OP_REALPART, typeTuple);
}

#include "autogen_qtui.cc"
