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

#include "qlparser/qtrangeconstructor.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtbinaryinduce.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtconst.hh"
#include "tilemgr/tile.hh"
#include "relcatalogif/mddbasetype.hh"
#include "mddmgr/mddobj.hh"
#include "mymalloc/mymalloc.h"
#include "tilemgr/tiler.hh"
#include "relcatalogif/structtype.hh"
#include "relcatalogif/typefactory.hh"

#include <logging.hh>

#include <iostream>
#include <string>

using namespace std;

const QtNode::QtNodeType QtRangeConstructor::nodeType = QT_RANGE_CONSTRUCTOR;

QtRangeConstructor::QtRangeConstructor(QtOperationList *opList)
    : QtNaryOperation(opList), complexLit(false)
{
    //setOperationObject();
}

bool QtRangeConstructor::equalMeaning(QtNode *node)
{
    bool result = false;

    if (nodeType == node->getNodeType())
    {
        QtRangeConstructor *condNode;
        condNode = static_cast<QtRangeConstructor *>(node);  // by force

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

void QtRangeConstructor::simplify()
{
    LTRACE << "simplify() warning: QtRangeConstructor itself is not simplified yet";

    // Default method for all classes that have no implementation.
    // Method is used bottom up.

    QtNodeList *resultList = NULL;
    QtNodeList::iterator iter;

    resultList = getChilds(QT_DIRECT_CHILDS);
    for (iter = resultList->begin(); iter != resultList->end(); iter++)
    {
        (*iter)->simplify();
    }

    delete resultList;
    resultList = NULL;
}

QtData *
QtRangeConstructor::evaluate(QtDataList *inputList)
{
    QtData *returnValue = NULL;
    QtDataList *operandList = NULL;

    if (getOperands(inputList, operandList))
    {
        // check if we have a complex literal, e.g. {1c,0c,...} and handle separately
        if (complexLit)
        {
            auto *scalarOperands = new QtComplexData::QtScalarDataList();
            for (auto iter = operandList->begin(); iter != operandList->end(); iter++)
            {
                if ((*iter)->getDataType() != QT_TYPE_UNKNOWN &&
                    (*iter)->getDataType() <= QT_COMPLEXTYPE2)
                {
                    scalarOperands->push_back(static_cast<QtScalarData *>(*iter));
                }
                else
                {
                    LERROR << "invalid scalar type.";
                    delete scalarOperands, scalarOperands = NULL;
                    parseInfo.setErrorNo(CELLEXP_WRONGOPERANDTYPE);
                    throw parseInfo;
                }
            }
            returnValue = new QtComplexData(scalarOperands);
        }
        else
        {
            // create a transient MDD object for the query result
            auto resultMDD = getResultMDD(operandList);
            auto resultDomain = resultMDD->getDefinitionDomain();
            // size of each cell in the result tile
            unsigned int cellSize = resultMDD->getCellType()->getSize();
            // create the result tile
            LDEBUG << "creating result tile with domain " << resultDomain;
            Tile *resultTile = new Tile(resultDomain, resultMDD->getCellType());
            resultMDD->insertTile(resultTile);

            // total # of cells in the result tile
            r_Area resultCellCount = resultDomain.cell_count();
            // offset used for copying the current operand: starts at 0 and
            // increments based on the size of the previous source cells.
            unsigned short structElemShift = 0;
            // iterate over the MDD objects from each band
            for (auto iter = operandList->begin(); iter != operandList->end(); iter++)
            {
                size_t bandCellSize{};
                if ((*iter)->isScalarData())
                {
                    // pointer to contents of target tile, shifted for this band
                    char *targetData = resultTile->getContents() + structElemShift;
                    // ptr to source data -- in this case, we do not shift as the data is constant
                    const char *scalarValuePtr = (static_cast<QtScalarData *>(*iter))->getValueBuffer();
                    // source data size for memcpy
                    bandCellSize = (static_cast<QtScalarData *>(*iter))->getValueType()->getSize();
                    // loop for copying the data
                    for (unsigned int cell = 0; cell < resultCellCount; ++cell)
                    {
                        memcpy(targetData, scalarValuePtr, bandCellSize);
                        // shifted by total cell size
                        targetData += cellSize;
                    }
                }
                else
                {
                    // ptr to this band's mdd & data container
                    QtMDD *qtMDDObj = static_cast<QtMDD *>(*iter);
                    MDDObj *currentMDDObj = qtMDDObj->getMDDObject();
                    // vector of tiles for the source mdd object
                    auto tiles = std::unique_ptr<vector<std::shared_ptr<Tile>>>(
                        currentMDDObj->intersect(qtMDDObj->getLoadDomain()));
                    // cell size for this band
                    bandCellSize = currentMDDObj->getCellType()->getSize();
                    LDEBUG << "copying source tiles with cell size: " << bandCellSize;
                    // iterate over the source tiles of the current band
                    for (auto tileIter = tiles->begin(); tileIter != tiles->end(); tileIter++)
                    {
                        // use copyTile to move contents from source to target
                        auto intersection = resultDomain.create_intersection((*tileIter)->getDomain());
                        resultTile->copyTile(intersection, *tileIter, intersection, structElemShift, 0, bandCellSize);
                    }
                    (*iter)->deleteRef();
                    *iter = NULL;
                }
                // for offset computations in the next pass
                structElemShift += bandCellSize;
            }
            returnValue = new QtMDD(resultMDD.release());
        }
        delete operandList, operandList = NULL;
    }
    return returnValue;
}

std::unique_ptr<MDDObj>
QtRangeConstructor::getResultMDD(QtDataList *operandList)
{
    size_t nBands = operandList->size();
    r_Minterval destinationDomain;
    //here, we build the destination domain
    bool destinationDomainSet = false;
    for (auto iter = operandList->begin(); iter != operandList->end(); iter++)
    {
        if (!(*iter)->isScalarData() && !complexLit)
        {
            //get this operand's MDD object
            MDDObj *currMDD = (static_cast<QtMDD *>(*iter))->getMDDObject();
            // get this operand's domain
            r_Minterval currDomain = currMDD->getDefinitionDomain();
            //check that the domains match:
            if (destinationDomainSet && currDomain != destinationDomain)
            {
                LERROR << "the operands have different domains.";
                parseInfo.setErrorNo(RANGE_DOMAINSINCOMPATIBLE);
                throw parseInfo;
            }
            //define currDomain if not yet set
            else if (!destinationDomainSet)
            {
                destinationDomain = currDomain;
                destinationDomainSet = true;
            }
        }
    }

    const auto *mddBaseType = dynamic_cast<const MDDBaseType *>(dataStreamType.getType());
    if (mddBaseType)
        return std::unique_ptr<MDDObj>(new MDDObj(mddBaseType, destinationDomain));
    else
        return nullptr;
}

void QtRangeConstructor::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtRangeConstructor Object " << static_cast<int>(getNodeType()) << endl;

    QtNaryOperation::printTree(tab, s, mode);
}

void QtRangeConstructor::printAlgebraicExpression(ostream &s)
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

const QtTypeElement &
QtRangeConstructor::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);
    complexLit = true;

    // check operand branches
    if (operationList)
    {
        StructType *structType = new StructType("tmp_struct_name", operationList->size());
        for (QtOperationList::iterator iter = operationList->begin(); iter != operationList->end(); iter++)
        {
            //check if the current operand's subtree exists
            if (!*iter)
                LERROR << "operand branch invalid.";
            auto inputType = (*iter)->checkType(typeTuple);

            // add an element to the runtime Structure, which corresponds to the
            // result type of the current operand's subtree
            auto elementName = std::to_string(structType->getNumElems());
            const BaseType *elementType;
            if (inputType.getDataType() == QT_MDD)
            {
                complexLit = false;
                elementType = static_cast<const MDDBaseType *>(inputType.getType())->getBaseType();
            }
            else
            {
                elementType = static_cast<const BaseType *>(inputType.getType());
            }
            structType->addElement(elementName.c_str(), elementType);
        }

        // add the struct type to the list of temporary runtime struct types
        // if it is not scalar data, we must first create a resultMDDType to add
        TypeFactory::addTempType(structType);
        if (complexLit)
        {
            dataStreamType.setType(structType);
        }
        else
        {
            auto *resultMDDType = new MDDBaseType("tmptype", structType);
            TypeFactory::addTempType(resultMDDType);
            dataStreamType.setType(resultMDDType);
        }
    }
    else
    {
        LERROR << "operand branch invalid.";
    }

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
            op2->getType() == FLOAT || op2->getType() == DOUBLE || op2->getType() == LONG ||
            op2->getType() == CINT32 || op2->getType() == CINT16)
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
        return TypeFactory::mapType("Complexd");
    }
    if (op1->getType() == COMPLEXTYPE1 || op2->getType() == COMPLEXTYPE1)
    {
        return TypeFactory::mapType("Complex");
    }
    if (op1->getType() == CINT32 || op2->getType() == CINT32)
    {
        return TypeFactory::mapType("CInt32");
    }
    if (op1->getType() == CINT16 || op2->getType() == CINT16)
    {
        return TypeFactory::mapType("CInt16");
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

int QtRangeConstructor::isSignedType(const BaseType *type)
{
    return (type->getType() >= LONG && type->getType() <= COMPLEXTYPE2);
}
