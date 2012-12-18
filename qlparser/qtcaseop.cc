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
 *      Class implementation. For further information see qlparser/qtcaseop.hh     
 *  
 * COMMENTS:
 * 
 ************************************************************/

#include "config.h"
#include "qlparser/qtcaseop.hh"
#include "raslib/rmdebug.hh"

#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtintervaldata.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "mddmgr/mddobj.hh"
#include "qlparser/qtbinaryinduce.hh"
#include "qlparser/qtpointdata.hh"


#include "catalogmgr/ops.hh"
#include "relcatalogif/type.hh"
#include "catalogmgr/typefactory.hh"
#include "relcatalogif/mdddomaintype.hh"

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
#include <bits/stl_bvector.h>
using namespace std;
#endif
#include "raslib/miter.hh"

/**
 * Defines the node type, for further identification.
 */
const QtNode::QtNodeType QtCaseOp::nodeType = QT_CASEOP;

/**
 * Class constructor
 * 
 * @param opList - the arguments of the operation
 */
QtCaseOp::QtCaseOp(QtOperationList* opList)
: inducedCase(false), baseType(NULL), QtNaryOperation(opList) {
}

/**
 * Divides the operand list into several sublists for easier manipulation:
 * - conditions;
 * - results;
 * - a default result.
 * 
 * @param operandList - the initial operand list, to be divided
 * @param conditionList - the list where the conditions will sit
 * @param resultList - the list where the results will sit
 * @param defaultResult - the default result
 */
void
QtCaseOp::getCaseOperands(QtDataList* operandList, QtDataList* conditionList, QtDataList* resultList, QtData* &defaultResult) {
    vector<QtData*>::iterator dataIter;
    int pos = 0;
    for (dataIter = operandList->begin(); (dataIter != (operandList->end())); dataIter++) {
        //else is specified
        if (pos == operandList->size() - 1) {
            //we are on the last element, so the default result
            defaultResult = *dataIter;
        } else {
            if (pos % 2) {
                //we got results here
                resultList->push_back(*dataIter);
            } else {
                //we got conditions here
                conditionList->push_back(*dataIter);
            }
        }
        pos++;
    }
}

/**
 * Evaluation for the CASE operation of arrays.
 * The conditions are evaluated and, for each point which evaluates to true, the
 * result is copied in the resulting array. All the conditions must have the same
 * dimensionality and tiling.
 * 
 * @param inputList - the input list to be passed to further evaluations 
 * @return A raster array consisting of the results.
 */
QtData*
QtCaseOp::inducedEvaluate(QtDataList* inputList) {
    QtDataList* operandList = NULL;
    QtDataList* conditionList = new QtDataList();
    QtDataList* resultList = new QtDataList();
    QtData* defaultResult = NULL;
    if (getOperands(inputList, operandList)) {
        //get the case operators
        getCaseOperands(operandList, conditionList, resultList, defaultResult);
        //create a focus mdd object of the same dimension as the first condition
        MDDObj* focusCondMdd = ((QtMDD*)*(conditionList->begin()))->getMDDObject();
        MDDObj* focusMdd = new MDDObj(((MDDBaseType*) (dataStreamType.getType())), focusCondMdd->getDefinitionDomain());
        //add tiles
        std::vector<Tile*>* tiles = new std::vector<Tile*>();
        std::vector<Tile*>* focusCondTiles = focusCondMdd->getTiles();
        std::vector< Tile*>::iterator tileIter;
        for (tileIter = focusCondTiles->begin(); tileIter != focusCondTiles->end(); tileIter++) {
            tiles->push_back(new Tile((*tileIter)->getDomain(), this->baseType));
        }
        //iterate through all the tiles of the focus mdd object
        vector<QtData*>::iterator condIter;
        vector<QtData*>::iterator resultIter;
        int tilePos = 0;
        for (tileIter = tiles->begin(); tileIter != tiles->end(); tileIter++) {
            //declare a watchdog for the changes
            std::vector<int> changedCells((*tileIter)->getDomain().cell_count());
            //iterate through conditions and check whether the point needs to be changed
            for (condIter = conditionList->begin(), resultIter = resultList->begin();
                    condIter != conditionList->end() && resultIter != resultList->end();
                    condIter++, resultIter++) {
                MDDObj* condMdd = ((QtMDD*) (*condIter))->getMDDObject();
                //operand and condition mdds should have the same domain
                if (condMdd->getDefinitionDomain() != focusMdd->getDefinitionDomain()) {
                    RMInit::logOut << "Error: QtCaseOp::inducedEvaluate() - The condition mdds don't have the same domain." << endl;
                    parseInfo.setErrorNo(426);
                    throw parseInfo;
                }
                Tile* condTile = condMdd->getTiles()->at(tilePos);
                //operand tiles and condition tiles should have the same domain
                if ((*tileIter)->getDomain() != condTile->getDomain()) {
                    RMInit::logOut << "Error: QtCaseOp::inducedEvaluate() - The condition mdds don't have the same tiling." << endl;
                    parseInfo.setErrorNo(427);
                    throw parseInfo;
                }
                //iterate through the points of each tile
                r_Miter condTileIter(&(condTile->getDomain()), &(condTile->getDomain()), condTile->getType()->getSize(), condTile->getContents());
                int cellCount = 0;
                while (!condTileIter.isDone()) {
                    char* condPoint = condTileIter.nextCell();
                    if ((long) (*(condPoint)) == 1 && !changedCells.at(cellCount)) {
                        changedCells.at(cellCount) = 1;
                        if ((*resultIter)->getDataType() != QT_COMPLEX) {
                            (*tileIter)->setCell(cellCount, ((QtAtomicData*) (*resultIter))->getValueBuffer());
                        } else {
                            (*tileIter)->setCell(cellCount, ((QtComplexData*) (*resultIter))->getValueBuffer());
                        }
                    }
                    cellCount++;
                }
            }
            //check for default
            int cellCount = 0;
            for (std::vector<int>::iterator changesIter = changedCells.begin(); changesIter != changedCells.end(); changesIter++) {
                if (!(*changesIter)) {
                    if ((defaultResult)->getDataType() != QT_COMPLEX) {
                        (*tileIter)->setCell(cellCount, ((QtAtomicData*) (defaultResult))->getValueBuffer());
                    } else {
                        (*tileIter)->setCell(cellCount, ((QtComplexData*) (defaultResult))->getValueBuffer());
                    }
                }
                cellCount++;
            }
            tilePos++;
        }
        //cleanup
        for (condIter = conditionList->begin(), resultIter = resultList->begin();
                condIter != conditionList->end() && resultIter != resultList->end();
                condIter++, resultIter++) {
            if (*condIter) {
                (*condIter)->deleteRef();
            }
            if (*resultIter) {
                (*resultIter)->deleteRef();
            }
        }

        delete conditionList;
        delete resultList;

        if (defaultResult) {
            (*defaultResult).deleteRef();
        }

        //add the tiles to the mddObj
        for (tileIter = tiles->begin(); tileIter != tiles->end(); tileIter++) {
            focusMdd->insertTile(*tileIter);
        }
        //return the resulting MDD
        return new QtMDD(focusMdd);
    }
}

/**
 * Evaluation for the scalar CASE operation.
 * Conditions are evaluated until one is found to be true. When this happens the
 * corresponding result is evaluated and returned.
 * Unused results, as well as unnecessary conditions, are not evaluated.
 * 
 * @param inputList - the input list to be passed to further evaluations
 */
QtData*
QtCaseOp::evaluate(QtDataList* inputList) {
    RMDBCLASS("QtCaseOp", "evaluate( QtDataList* )", "qlparser", __FILE__, __LINE__)
    startTimer("QtCaseOp");
    if (this->inducedCase) {
        return this->inducedEvaluate(inputList);
    }
    QtData* returnValue = NULL;
    bool foundClause = false;
    int pos = 0, foundAt;
    QtOperationList::iterator iter;
    QtData* dataI;
    //everything is good for evaluation
    bool success = (operationList != 0);
    for (iter = operationList->begin(); iter != operationList->end() && !foundClause; iter++) {
        if ((*iter) == NULL) {
            success = false;
            break;
        }
        //check if WHEN clause
        if (pos % 2 == 0 && pos < operationList->size() - 1) {
            dataI = (*iter)->evaluate(inputList);
            if ((bool)((QtAtomicData*) (dataI))->getUnsignedValue()) {
                iter++;
                dataI = (*iter)->evaluate(inputList);
                returnValue = dataI;
                foundAt = pos;
                foundClause = true;
            }
        }//we arrived on the ELSE clause
        else if (pos == operationList->size() - 1) {
            QtData* dataI = (*iter)->evaluate(inputList);
            returnValue = dataI;
            foundAt = pos;
        }
        pos++;
    }

    //check if everything evaluated correctly
    if (!success) {
        RMInit::logOut << endl << "Error: QtCaseOp::evaluate() - at least one operand branch is invalid." << endl;
    }

    stopTimer();
    return returnValue;
}

/**
 * @override QtNarryOperation::printTree
 */
void
QtCaseOp::printTree(int tab, ostream& s, QtChildType mode) {
    s << SPACE_STR(tab).c_str() << "QtCaseOp Object " << getNodeType() << getEvaluationTime() << endl;
    QtNaryOperation::printTree(tab, s, mode);
}

/**
 * @override QtNarryOperation::printAlgebraicExpression
 */
void
QtCaseOp::printAlgebraicExpression(ostream& s) {
    s << "[";

    QtNaryOperation::printAlgebraicExpression(s);

    s << "]";
}

/**
 * Type checking for the CASE operation on arrays.
 * The following conditions have to be met by the operands:
 * - all the conditions must be arrays;
 * - all the conditions must have boolean base type;
 * - all the results must be of base types;
 * - type coercion is done between results, the types must be compatible.
 * 
 * @param typeTupe - the tuple containing the array types
 */
const QtTypeElement&
QtCaseOp::checkInducedType(QtTypeTuple* typeTuple) {
    QtOperationList::iterator iter;
    const BaseType* resultType = NULL;
    int pos = 0;
    for (iter = operationList->begin(); iter != operationList->end(); iter++) {
        (*iter)->checkType(typeTuple);
        if (!(pos % 2) && (pos != operationList->size() - 1)) {
            //conditions should be boolean mdds
            QtTypeElement condType = (*iter)->checkType(typeTuple);
            if (condType.getDataType() != QT_MDD) {
                RMInit::logOut << "Error: QtCaseOp::checkInducedType() - At least one condition is not a boolean mdd." << endl;
                parseInfo.setErrorNo(428);
                throw parseInfo;
            }
            if (((MDDBaseType*) condType.getType())->getBaseType()->getType() != BOOLTYPE) {
                RMInit::logOut << "Error: QtCaseOp::checkInducedType() - At least one condition is not a boolean mdd." << endl;
                parseInfo.setErrorNo(428);
                throw parseInfo;
            }
        } else {
            QtTypeElement resType = (*iter)->checkType(typeTuple);
            //check if result types is base type
            if (!resType.isBaseType()) {
                RMInit::logOut << "Error: QtCaseOp::checkInducedType() - At least one result type is not base type." << endl;
                parseInfo.setErrorNo(429);
                throw parseInfo;
            }
            //make type coercion
            if (resultType) {
                resultType = getResultType(resultType, (BaseType*) resType.getType());
                if (!resultType) {
                    RMInit::logOut << "Error: QtCaseOp::checkInducedType() - The results have incompatible types." << endl;
                    parseInfo.setErrorNo(430);
                    throw parseInfo;
                }
            }
            else{
                resultType = (BaseType*) resType.getType();
            }
        }
        pos++;
    }
    this->baseType = (BaseType*) resultType;
    MDDBaseType* resultMDDType = new MDDBaseType("tmp", this->baseType);
    TypeFactory::addTempType(resultMDDType);
    dataStreamType.setType(resultMDDType);
    return dataStreamType;
}

/**
 * Type checking for the CASE operation on scalar values.
 * The following conditions have to be met by the operands:
 * - all the conditions must be of type boolean;
 * - type coercion is done between results, the types must be compatible.
 * 
 * @param typeTuple - the tuple containing the types of the arrays 
 */
const QtTypeElement&
QtCaseOp::checkType(QtTypeTuple* typeTuple) {
    RMDBCLASS("QtCaseOp", "checkType( QtTypeTuple* )", "qlparser", __FILE__, __LINE__)
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);
    if ((*(operationList->begin()))->checkType(typeTuple).getDataType() == QT_MDD) {
        this->inducedCase = true;
        return this->checkInducedType(typeTuple);
    }
    QtOperationList::iterator iter;
    bool whenTypesValid = true;
    bool resultTypesValid = true;
    int pos = 0;
    const BaseType* resultType = NULL;

    //check if types are valid
    for (iter = operationList->begin(); (iter != operationList->end()) && whenTypesValid
            && resultTypesValid; iter++) {
        const QtTypeElement& type = (*iter)->checkType(typeTuple);
        //check if WHEN clauses are of type boolean
        if (pos % 2 == 0 && pos < operationList->size() - 1) {
            if (!(type.getDataType() == QT_BOOL)) {
                whenTypesValid = false;
            }
        } else {
            //type coercion between result clauses
            if (resultType) {
                resultType = getResultType(resultType, (BaseType*) type.getType());
                if (!resultType) {
                    //types are incompatible
                    resultTypesValid = false;
                }
            } else {
                resultType = (BaseType*) type.getType();
            }
        }
        pos++;
    }

    //some when clause is not boolean
    if (!whenTypesValid) {
        RMInit::logOut << "Error: QtCaseOp::checkType() - At least one condition is not a boolean." << endl;
        parseInfo.setErrorNo(431);
        throw parseInfo;
    }

    //result types are different
    if (!resultTypesValid) {
        RMInit::logOut << "Error: QtCaseOp::checkType() - The results have incompatible types." << endl;
        parseInfo.setErrorNo(430);
        throw parseInfo;
    }

    dataStreamType.setType(resultType);
    return dataStreamType;
}

/**
 * Type coercion for operands.
 * 
 * @param op1 - the base type of the first operand
 * @param op2 - the base type of the second operand
 * @return The resulting type if types are compatible, NULL otherwise
 */
const BaseType* QtCaseOp::getResultType(const BaseType* op1, const BaseType* op2) {

    if ((op1->getType() == STRUCT) || (op2->getType() == STRUCT)) {
        if (op1->compatibleWith(op2)) {
            return op1;
        } else {
            return NULL;
        }
    }
    if (op1->getType() == op2->getType()) {
        return op1;
    }


    // if only one of operand is signed, result also has to be signed.
    if (isSignedType(op1) && !isSignedType(op2)) {
        // swap it, action is in next if clause
        const BaseType* dummy;
        dummy = op2;
        op2 = op1;
        op1 = dummy;
    }
    if (!isSignedType(op1) && isSignedType(op2)) {
        // got to get the thing with the highest precision and make sure
        // it is signed.
        if (op2->getType() == COMPLEXTYPE1 || op2->getType() == COMPLEXTYPE2 ||
                op2->getType() == FLOAT || op2->getType() == DOUBLE || op2->getType() == LONG)
            return (BaseType*) op2;
        if (op1->getType() == USHORT)
            return TypeFactory::mapType("Short");
        if (op2->getType() == SHORT)
            return (BaseType*) op2;
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
        return (BaseType*) op1;
    else
        return (BaseType*) op2;

    return NULL;
}

/**
 * Checks if the given type is signed.
 * 
 * @param type - the type to be checked
 * @return 1 if true, 0 otherwise
 */
int QtCaseOp::isSignedType(const BaseType* type) {
    return ( type->getType() >= LONG && type->getType() <= COMPLEXTYPE2);
}

#include "qlparser/qtcaseop.icc"


