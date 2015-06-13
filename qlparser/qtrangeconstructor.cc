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

#include "qlparser/qtrangeconstructor.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtbinaryinduce.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtconst.hh"
#include "tilemgr/tile.hh"
#include "catalogmgr/typefactory.hh"
#include "relcatalogif/structtype.hh"

#include "mddmgr/mddobj.hh"
#include "mymalloc/mymalloc.h"
#include "tilemgr/tiler.hh"

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif


const QtNode::QtNodeType QtRangeConstructor::nodeType = QT_RANGE_CONSTRUCTOR;

QtRangeConstructor::QtRangeConstructor(QtOperationList *opList)
: QtNaryOperation(opList), complexLit(false)
{
    //setOperationObject();
}

bool
QtRangeConstructor::equalMeaning(QtNode *node)
{
    RMDBCLASS("QtRangeConstructor", "equalMeaning( QtNode* )", "qlparser", __FILE__, __LINE__)

    bool result = false;

    if (nodeType == node->getNodeType())
    {
        QtRangeConstructor *condNode;
        condNode = static_cast<QtRangeConstructor *>(node); // by force

        // check domain and cell expression
        result = QtNaryOperation::equalMeaning(condNode);

    };

    return ( result);
}

string
QtRangeConstructor::getSpelling()
{

    char tempStr[20];
    sprintf(tempStr, "%lu", static_cast<unsigned long>(getNodeType()));
    string result = string(tempStr);
    result.append("{");
    result.append(QtNaryOperation::getSpelling());


    result.append("}");

    return result;
}

void
QtRangeConstructor::simplify()
{
    RMDBCLASS("QtRangeConstructor", "simplify()", "qlparser", __FILE__, __LINE__)

    RMDBGMIDDLE(1, RMDebug::module_qlparser, "QtRangeConstructor", "simplify() warning: QtRangeConstructor itself is not simplified yet")

    // Default method for all classes that have no implementation.
    // Method is used bottom up.

    QtNodeList *resultList = NULL;
    QtNodeList::iterator iter;

    resultList = getChilds(QT_DIRECT_CHILDS);
    for (iter = resultList->begin(); iter != resultList->end(); iter++)
        (*iter)->simplify();

    delete resultList;
    resultList = NULL;
}

QtData *
QtRangeConstructor::evaluate(QtDataList *inputList)
{
    RMDBCLASS("QtRangeConstructor", "evaluate( QtDataList* )", "qlparser", __FILE__, __LINE__)

    QtData *returnValue = NULL;
    QtDataList *operandList = NULL;

    if (getOperands(inputList, operandList))
    {
        // check if we have a complex literal, e.g. {1c,0c,...} and handle separately
        if (complexLit)
        {
            QtComplexData::QtScalarDataList *scalarOperandList = new QtComplexData::QtScalarDataList();
            for (QtDataList::iterator iter = operandList->begin(); iter != operandList->end(); iter++)
            {
                QtData *operand = *iter;
                QtDataType operandType = operand->getDataType();
                if (operandType != QT_TYPE_UNKNOWN && operandType <= QT_COMPLEXTYPE2)
                {
                    scalarOperandList->push_back(static_cast<QtScalarData *>(operand));
                }
                else
                {
                    RMInit::logOut << "Error: QtRangeConstructor::evaluate() - invalid scalar type." << endl;
                    parseInfo.setErrorNo(404);
                    throw parseInfo;
                }
            }
            returnValue = new QtComplexData(scalarOperandList);
        }
        else
        {

            // create a transient MDD object for the query result
            MDDObj *resultMDD = getResultMDD(operandList);


            unsigned int cellSize = resultMDD->getCellType()->getSize();
            vector<char *> targetTiles;

            unsigned short structElemShift = 0;
            // iterate over the MDD objects from each band
            for (QtDataList::iterator iter = operandList->begin(); iter != operandList->end(); iter++)
            {

                vector<char *>::iterator targetIt = targetTiles.begin();
                if (static_cast<QtScalarData *>(*iter)->isScalarData())
                {

                    char *ScalarTargetTile = NULL;
                    if (structElemShift == 0)
                    {
                        Tile *scalarTile = new Tile(resultMDD->getDefinitionDomain(), resultMDD->getCellType());
                        resultMDD->insertTile(scalarTile);
                        ScalarTargetTile = static_cast<char *>(scalarTile->getContents());
                        targetTiles.push_back(ScalarTargetTile);
                    }
                    else
                    {
                        ScalarTargetTile = *targetIt;
                        ++targetIt;
                    }
                    ScalarTargetTile += structElemShift;
                    r_Area scalarCellCount = resultMDD->getDefinitionDomain().cell_count();
                    unsigned int cell = 0;
                    while (cell < scalarCellCount)
                    {
                        memcpy(ScalarTargetTile, (static_cast<QtScalarData *>(*iter))->getValueBuffer(), (static_cast<QtScalarData *>(*iter))->getValueType()->getSize());
                        ++cell;
                    }
                    structElemShift += (static_cast<QtScalarData *>(*iter))->getValueType()->getSize();


                }
                else
                {

                    QtMDD *qtMDDObj = static_cast<QtMDD *>(*iter);
                    MDDObj *currentMDDObj = qtMDDObj->getMDDObject();
                    vector< boost::shared_ptr<Tile> > *tiles = currentMDDObj->intersect(qtMDDObj->getLoadDomain());
                    unsigned int bandCellSize = currentMDDObj->getCellType()->getSize();

                    // iterate over the source tiles of the curent band
                    targetIt = targetTiles.begin();
                    for (vector< boost::shared_ptr<Tile> >::iterator tileIter = tiles->begin(); tileIter != tiles->end(); tileIter++)
                    {
                        const char *sourceTile = (*tileIter)->getContents();
                        r_Minterval tileDomain = (*tileIter)->getDomain();

                        // get the target tile to which the input band will be copied
                        char *targetTile = NULL;
                        if (structElemShift == 0)
                        {
                            Tile *newTile = new Tile(tileDomain, resultMDD->getCellType());
                            resultMDD->insertTile(newTile);
                            targetTile = static_cast<char *>(newTile->getContents());
                            targetTiles.push_back(targetTile);
                        }
                        else
                        {
                            targetTile = *targetIt;
                            ++targetIt;
                        }
                        targetTile += structElemShift;

                        // copy all cells from input band to the output composite MDD,
                        // properly shifted to the right offset
                        r_Area cellCount = tileDomain.cell_count();
                        unsigned int cell = 0;
                        while (cell < cellCount)
                        {
                            memcpy(targetTile, sourceTile, bandCellSize);
                            targetTile += cellSize;
                            sourceTile += bandCellSize;
                            ++cell;
                        }
                    }
                    // increase element shift within the struct
                    structElemShift += bandCellSize;
                    delete tiles;
                }

            }

            returnValue = new QtMDD(resultMDD);
        }

        // delete old operands
        if (operandList)
        {
            delete operandList;
            operandList = NULL;
        }
    }

    return returnValue;
}

MDDObj *
QtRangeConstructor::getResultMDD(QtDataList *operandList)
{
    unsigned int nBands = operandList->size();
    StructType *destinationStructBaseType = new StructType("tmp_str_name", nBands);
    const BaseType *baseType = static_cast<const BaseType *>(destinationStructBaseType);
    r_Minterval destinationDomain;
    r_Minterval currDomain;
    BaseType *cellType = NULL;
    for (QtDataList::iterator iter = operandList->begin(); iter != operandList->end(); iter++)
    {
        if (!(*iter)->isScalarData())
        {
            destinationDomain = (static_cast<QtMDD *>(*iter))->getLoadDomain();
            cellType = (static_cast<QtMDD *>(*iter))->getCellType();
            currDomain = destinationDomain;
        }

    }

    // iterate over the MDDs in each band
    int band = 0;
    QtMDD *qtMDDObj = NULL;
    for (QtDataList::iterator iter = operandList->begin(); iter != operandList->end(); iter++, band++)
    {

        if (!(*iter)->isScalarData())
        {
            qtMDDObj = static_cast<QtMDD *>(*iter);
            currDomain = qtMDDObj->getLoadDomain();

        }

        if (destinationDomain != currDomain)
        {
            if (operandList)
            {
                delete operandList;
                operandList = NULL;
            }

            RMInit::logOut << "Error: QtRangeConstructor::evaluate( QtDataList* ) - the operands have different domains." << endl;
            parseInfo.setErrorNo(351);
            throw parseInfo;
        }

        char bandName[20];
        memset(bandName, '\0', 20);
        sprintf(bandName, "band%d", band);
        if ((*iter)->isScalarData())
        {

            if (strcmp(((static_cast<QtScalarData *>(*iter))->getValueType())->getTypeStructure(), cellType->getTypeStructure()) != 0)
            {
                RMInit::logOut << "Error: QtRangeConstructor::evaluate( QtDataList* ) - the operands should have the same type." << endl;
                parseInfo.setErrorNo(301);
                throw parseInfo;
            }

            destinationStructBaseType->addElement(bandName, (static_cast<QtScalarData *>(*iter))->getValueType());

        }
        else
        {

            destinationStructBaseType->addElement(bandName, qtMDDObj->getCellType());
        }

    }

    MDDBaseType *resultMDDType = new MDDBaseType("tmptype", baseType);
    TypeFactory::addTempType(resultMDDType);
    dataStreamType.setType(resultMDDType);
    dataStreamType.setDataType(QT_MDD);

    MDDObj *resultMDD = new MDDObj(resultMDDType, destinationDomain);
    return resultMDD;
}

void
QtRangeConstructor::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtRangeConstructor Object " << static_cast<int>(getNodeType()) << endl;

    QtNaryOperation::printTree(tab, s, mode);
}

void
QtRangeConstructor::printAlgebraicExpression(ostream &s)
{
    s << "rangeCostructor(";

    if (operationList)
    {
        QtOperationList::iterator iter;

        for (iter = operationList->begin(); iter != operationList->end(); iter++)
        {
            if (iter != operationList->begin()) s << ",";

            if (*iter)
                (*iter)->printAlgebraicExpression(s);
            else
                s << "<nn>";
        }
    }
    else
        s << "<nn>";

    s << "; ";

    s << ")";
}

const QtTypeElement &
QtRangeConstructor::checkType(QtTypeTuple *typeTuple)
{
    RMDBCLASS("QtRangeConstructor", "checkType( QtTypeTuple* )", "qlparser", __FILE__, __LINE__)

    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    complexLit = true;

    QtTypeElement inputType;

    StructType *structType;
    // check operand branches
    if (operationList)
    {
        QtOperationList::iterator iter;

        for (iter = operationList->begin(); iter != operationList->end(); iter++)
        {

            if (*iter)
                inputType = (*iter)->checkType(typeTuple);
            else
                RMInit::logOut << "Error: QtRangeConstructor::checkType() - operand branch invalid." << endl;

            if (inputType.getDataType() == QT_MDD)
            {
                complexLit = false;
            }
        }
        if (complexLit)
        {
            char elementName[50];
            int i = 0;

            std::vector<QtScalarData *> scalarOperandList;
            for (iter = operationList->begin(); iter != operationList->end(); iter++)
            {
                QtData *operand = (*iter)->evaluate(NULL);
                scalarOperandList.push_back(static_cast<QtScalarData *>(operand));
            }

            structType = new StructType("", scalarOperandList.size());

            // add type elements, the first element inserted has no 0, the second no 1, and so on
            for (std::vector<QtScalarData *>::iterator iterScalar = scalarOperandList.begin(); iterScalar != scalarOperandList.end(); iterScalar++, i++)
            {
                sprintf(elementName, "%d", i);
                structType->addElement(elementName, (*iterScalar)->getValueType());
            }
            TypeFactory::addTempType(structType);
            dataStreamType.setDataType(QT_COMPLEX);
            dataStreamType.setType(structType);
            for (std::vector<QtScalarData *>::iterator iterScalar = scalarOperandList.begin(); iterScalar != scalarOperandList.end(); iterScalar++)
            {
                (*iterScalar)->deleteRef();
                *iterScalar = NULL;
            }
        }
        else
        {
            dataStreamType.setDataType(QT_MDD);
        }

    }
    else
        RMInit::logOut << "Error: QtRangeConstructor::checkType() - operand branch invalid." << endl;

    return dataStreamType;
}

QtNode::QtAreaType
QtRangeConstructor::getAreaType()
{
    return QT_AREA_MDD;
}

const BaseType *QtRangeConstructor::getResultType(const BaseType *op1, const BaseType *op2)
{

    if ((op1->getType() == STRUCT) || (op2->getType() == STRUCT))
    {
        if (op1->compatibleWith(op2))
        {
            return op1;
        }
        else
        {
            return NULL;
        }
    }
    if (op1->getType() == op2->getType())
    {
        return op1;
    }


    // if only one of operand is signed, result also has to be signed.
    if (isSignedType(op1) && !isSignedType(op2))
    {
        // swap it, action is in next if clause
        const BaseType *dummy;
        dummy = op2;
        op2 = op1;
        op1 = dummy;
    }
    if (!isSignedType(op1) && isSignedType(op2))
    {
        // got to get the thing with the highest precision and make sure
        // it is signed.
        if (op2->getType() == COMPLEXTYPE1 || op2->getType() == COMPLEXTYPE2 ||
                op2->getType() == FLOAT || op2->getType() == DOUBLE || op2->getType() == LONG)
            return op2;
        if (op1->getType() == USHORT)
            return TypeFactory::mapType("Short");
        if (op2->getType() == SHORT)
            return op2;
        return TypeFactory::mapType("Octet");
    }
    // return the stronger type
    if (op1->getType() == COMPLEXTYPE2 || op2->getType() == COMPLEXTYPE2)
        return TypeFactory::mapType("Complex2");
    if (op1->getType() == COMPLEXTYPE1 || op2->getType() == COMPLEXTYPE1)
        return TypeFactory::mapType("Complex1");
    if (op1->getType() == DOUBLE || op2->getType() == DOUBLE)
        return TypeFactory::mapType("Double");
    if (op1->getType() == FLOAT || op2->getType() == FLOAT)
        return TypeFactory::mapType("Float");
    if (op1->getType() <= op2->getType())
        return op1;
    else
        return op2;

    return NULL;
}

int QtRangeConstructor::isSignedType(const BaseType *type)
{
    return ( type->getType() >= LONG && type->getType() <= COMPLEXTYPE2);
}
