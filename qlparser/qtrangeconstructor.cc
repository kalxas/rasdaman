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

#include <logging.hh>

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

QtRangeConstructor::QtRangeConstructor(QtOperationList* opList)
    : QtNaryOperation(opList), complexLit(false)
{
    //setOperationObject();
}

bool
QtRangeConstructor::equalMeaning(QtNode* node)
{
    bool result = false;

    if (nodeType == node->getNodeType())
    {
        QtRangeConstructor* condNode;
        condNode = static_cast<QtRangeConstructor*>(node);  // by force

        // check domain and cell expression
        result = QtNaryOperation::equalMeaning(condNode);

    };

    return (result);
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
    LTRACE << "simplify() warning: QtRangeConstructor itself is not simplified yet";

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

QtData*
QtRangeConstructor::evaluate(QtDataList* inputList)
{
    QtData* returnValue = NULL;
    QtDataList* operandList = NULL;

    if (getOperands(inputList, operandList))
    {
        // check if we have a complex literal, e.g. {1c,0c,...} and handle separately
        if (complexLit)
        {
            QtComplexData::QtScalarDataList* scalarOperandList = new QtComplexData::QtScalarDataList();
            for (auto iter = operandList->begin(); iter != operandList->end(); iter++)
            {
                QtData* operand = *iter;
                QtDataType operandType = operand->getDataType();
                if (operandType != QT_TYPE_UNKNOWN && operandType <= QT_COMPLEXTYPE2)
                {
                    scalarOperandList->push_back(static_cast<QtScalarData*>(operand));
                }
                else
                {
                    LFATAL << "Error: QtRangeConstructor::evaluate() - invalid scalar type.";
                    if (scalarOperandList)
                    {
                        delete scalarOperandList;
                        scalarOperandList = NULL;
                    }
                    parseInfo.setErrorNo(404);
                    throw parseInfo;
                }
            }
            returnValue = new QtComplexData(scalarOperandList);
        }
        else
        {
            // create a transient MDD object for the query result
            MDDObj* resultMDD = getResultMDD(operandList);
            // size of each cell in the result tile
            unsigned int cellSize = resultMDD->getCellType()->getSize();
            // total # of cells in the result tile
            r_Area resultCellCount = resultMDD->getDefinitionDomain().cell_count();
            // offset used for copying the current operand: starts at 0 and
            // increments based on the size of the previous source cells.
            unsigned short structElemShift = 0;
            // create the result tile
            Tile* resultTile = new Tile(resultMDD->getDefinitionDomain(), resultMDD->getCellType());
            resultMDD->insertTile(resultTile);
            // iterate over the MDD objects from each band
            for (auto iter = operandList->begin(); iter != operandList->end(); iter++)
            {
                if (static_cast<QtScalarData*>(*iter)->isScalarData())
                {
                    // pointer to contents of target tile, shifted for this band
                    char* targetData = const_cast<char*>(resultTile->getContents()) + structElemShift;
                    // ptr to source data -- in this case, we do not shift as the data is constant
                    const char* scalarValuePtr = (static_cast<QtScalarData*>(*iter))->getValueBuffer();
                    // source data size for memcpy
                    size_t scalarDataSize = (static_cast<QtScalarData*>(*iter))->getValueType()->getSize();
                    // loop for copying the data
                    for(unsigned int cell = 0; cell < resultCellCount; ++cell)
                    {
                        memcpy(targetData, scalarValuePtr, scalarDataSize);
                        // shifted by total cell size
                        targetData += cellSize;
                    }
                    // for offset computations in the next pass
                    structElemShift += scalarDataSize;
                }
                else
                {
                    // ptr to this band's mdd & data container
                    QtMDD* qtMDDObj = static_cast<QtMDD*>(*iter);
                    MDDObj* currentMDDObj = qtMDDObj->getMDDObject();
                    // vector of tiles for the source mdd object
                    vector<boost::shared_ptr<Tile>>* tiles = currentMDDObj->intersect(qtMDDObj->getLoadDomain());
                    // cell size for this band
                    unsigned int bandCellSize = currentMDDObj->getCellType()->getSize();
                    // iterate over the source tiles of the current band
                    for (auto tileIter = tiles->begin(); tileIter != tiles->end(); tileIter++)
                    {
                        // use copyTile to move contents from source to target
                        resultTile->copyTile((*tileIter)->getDomain(), *tileIter, (*tileIter)->getDomain(), structElemShift, 0, bandCellSize);                           
                    }
                    // for offset computations in the next pass
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

MDDObj*
QtRangeConstructor::getResultMDD(QtDataList* operandList)
{
    size_t nBands = operandList->size();
    r_Minterval destinationDomain;
    //here, we build the destination domain
    for (auto iter = operandList->begin(); iter != operandList->end(); iter++)
    {
        bool destinationDomainSet = false;
        if (!(*iter)->isScalarData())
        {
            //get this operand's MDD object
            MDDObj* currMDD = (static_cast<QtMDD*>(*iter))->getMDDObject();
            // get this operand's domain
            r_Minterval currDomain = currMDD->getDefinitionDomain();
            //check that the domains match:
            if(destinationDomainSet && (currDomain != destinationDomain) )
            {
                LFATAL << "Error: QtRangeConstructor::evaluate( QtDataList* ) - the operands have different domains.";
                parseInfo.setErrorNo(351);
                throw parseInfo;
            }
            //define currDomain if not yet set
            else if(!destinationDomainSet)
            {
                destinationDomain = currDomain;
                destinationDomainSet = true;
            }
        }
    }
    
    if(dynamic_cast<MDDBaseType*>(const_cast<Type*>(dataStreamType.getType())))
    {
        MDDObj* resultMDD = new MDDObj(dynamic_cast<MDDBaseType*>(const_cast<Type*>(dataStreamType.getType())), destinationDomain);
        return resultMDD;
    }
    else
    {
        return NULL;
    }
}

void
QtRangeConstructor::printTree(int tab, ostream& s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtRangeConstructor Object " << static_cast<int>(getNodeType()) << endl;

    QtNaryOperation::printTree(tab, s, mode);
}

void
QtRangeConstructor::printAlgebraicExpression(ostream& s)
{
    s << "rangeCostructor(";

    if (operationList)
    {
        QtOperationList::iterator iter;

        for (iter = operationList->begin(); iter != operationList->end(); iter++)
        {
            if (iter != operationList->begin())
            {
                s << ",";
            }

            if (*iter)
            {
                (*iter)->printAlgebraicExpression(s);
            }
            else
            {
                s << "<nn>";
            }
        }
    }
    else
    {
        s << "<nn>";
    }

    s << "; ";

    s << ")";
}

const QtTypeElement&
QtRangeConstructor::checkType(QtTypeTuple* typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    complexLit = true;
    
    QtTypeElement inputType;

    // check operand branches
    if (operationList)
    {
        StructType* structType = new StructType("tmp_struct_name", operationList->size());
        for (QtOperationList::iterator iter = operationList->begin(); iter != operationList->end(); iter++)
        {

            //check if the current operand's subtree exists
            if (*iter)
            {
                inputType = (*iter)->checkType(typeTuple);
            }
            else
            {
                LERROR << "Error: QtRangeConstructor::checkType() - operand branch invalid.";
            }

            // add an element to the runtime Structure, which corresponds to the
            // result type of the current operand's subtree
            if (inputType.getDataType() == QT_MDD)
            {
                complexLit = false;
                //we need to identify the base type of the QT_MDD, and add it to our struct.
                char elementName[50];
                sprintf(elementName, "%d", structType->getNumElems());
                //we first need the MDDBaseType* from the child:
                MDDBaseType* dummyMDDBaseTypePtr = static_cast<MDDBaseType*>(const_cast<Type*>(inputType.getType()));
                //next, we reference the underlying BaseType:
                const BaseType* dummyBaseTypePtr = dummyMDDBaseTypePtr->getBaseType();
                //lastly, we pass the base type into the transient struct:
                structType->addElement(elementName, const_cast<BaseType*>(dummyBaseTypePtr));
            }
            else
            {
                char elementName[50];
                sprintf(elementName, "%d", structType->getNumElems());
                //in this case, the base type is much easier to pass into to the transient struct:
                structType->addElement(elementName, (static_cast<BaseType*>(const_cast<Type*>( inputType.getType() ))));
            }
        }

        // add the struct type to the list of temporary runtime struct types
        // if it is not scalar data, we must first create a resultMDDType to add
        if(complexLit)
        {
            TypeFactory::addTempType(structType);
            dataStreamType.setDataType(QT_COMPLEX);
            dataStreamType.setType(structType);
        }
        else
        {
            MDDBaseType* resultMDDType = new MDDBaseType("tmptype", static_cast<BaseType*>(structType));
            TypeFactory::addTempType(resultMDDType);
            dataStreamType.setDataType(QT_MDD);
            dataStreamType.setType(resultMDDType);
        }
    }
    else
    {
        LERROR << "Error: QtRangeConstructor::checkType() - operand branch invalid.";
    }
    
    return dataStreamType;
}

QtNode::QtAreaType
QtRangeConstructor::getAreaType()
{
    return QT_AREA_MDD;
}

const BaseType* QtRangeConstructor::getResultType(const BaseType* op1, const BaseType* op2)
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
        const BaseType* dummy;
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
        {
            return op2;
        }
        if (op1->getType() == USHORT)
        {
            return TypeFactory::mapType("Short");
        }
        if (op2->getType() == SHORT)
        {
            return op2;
        }
        return TypeFactory::mapType("Octet");
    }
    // return the stronger type
    if (op1->getType() == COMPLEXTYPE2 || op2->getType() == COMPLEXTYPE2)
    {
        return TypeFactory::mapType("Complex2");
    }
    if (op1->getType() == COMPLEXTYPE1 || op2->getType() == COMPLEXTYPE1)
    {
        return TypeFactory::mapType("Complex1");
    }
    if (op1->getType() == DOUBLE || op2->getType() == DOUBLE)
    {
        return TypeFactory::mapType("Double");
    }
    if (op1->getType() == FLOAT || op2->getType() == FLOAT)
    {
        return TypeFactory::mapType("Float");
    }
    if (op1->getType() <= op2->getType())
    {
        return op1;
    }
    else
    {
        return op2;
    }

    return NULL;
}

int QtRangeConstructor::isSignedType(const BaseType* type)
{
    return (type->getType() >= LONG && type->getType() <= COMPLEXTYPE2);
}
