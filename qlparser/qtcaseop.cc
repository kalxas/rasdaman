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
#include "qlparser/qtvariable.hh"
#include "qlparser/qtconst.hh"

#include "catalogmgr/ops.hh"
#include "relcatalogif/type.hh"
#include "catalogmgr/typefactory.hh"
#include "relcatalogif/mdddomaintype.hh"

#include "../common/src/logging/easylogging++.hh"

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
#include <bits/stl_bvector.h>
using namespace std;
#endif
#include "raslib/miter.hh"
#include "qtvariable.hh"

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
: QtNaryOperation(opList), inducedCase(false), baseType(NULL) {
    conditionList = new QtOperationList();
}

QtCaseOp::~QtCaseOp()
{
    delete conditionList;
}

/**
 * Divides the operand list into several sublists for easier manipulation:
 * - conditions -> evaluated;
 * - results -> not evaluated; if the result is a base type, its evaluation is added to the 
 * scalarCacheList. If array operation (expression), the mdd objects are evaluated and 
 * added to the cache list. This is necessary to avoid multiple reads of the same 
 * mdd object from db. 
 * - a default result -> not evaluated.
 * 
 * @param inputList - the list of inputs to be passed to QtOperation evaluate()
 * @param cacheList - the list of cached array values
 * @param scalarCache - the list of cached scalar values
 * @param conditionList - the list where conditions will sit
 * @param resultList - the list where results will sit
 * @param defaultResult - the default result
 */
void
QtCaseOp::getCaseOperands(QtDataList* inputList, std::vector< std::pair <QtOperation*, QtDataList*> >* cacheList,
        std::vector<std::pair <QtOperation*, QtData* > >* scalarCache,
        QtDataList* conditionList2, QtOperationList* resultList, QtOperation* &defaultResult) {
    QtOperationList::iterator iter;
    r_Minterval definitionDomain;
    bool definitionDomainIsSet = false;
    unsigned int pos = 0;
    for (iter = operationList->begin(); (iter != (operationList->end())); iter++) {
        //else is specified
        if (pos == operationList->size() - 1) {
            //we are on the last element, so the default result
            if ((*iter)->getDataStreamType().getDataType() == QT_MDD) {
                addMddsToCache(inputList, (*iter), cacheList);
                defaultResult = *iter;
            } else {
                defaultResult = *iter;
                scalarCache->push_back(std::make_pair(*iter, (*iter)->evaluate(inputList)));
            }
        } else {
            if (pos % 2) {
                //we got results here  
                //if expression, evaluate the mdds, but not the entire expression
                if ((*iter)->getDataStreamType().getDataType() == QT_MDD) {
                    addMddsToCache(inputList, (*iter), cacheList);
                    resultList->push_back((*iter));
                }//if base type, evaluate it and add it to the cache list
                else {
                    resultList->push_back(*iter);
                    scalarCache->push_back(std::make_pair(*iter, (*iter)->evaluate(inputList)));
                }
            } else {
                //we got conditions here
                this->conditionList->push_back(*iter);
                QtData* cond = (*iter)->evaluate(inputList);
                conditionList2->push_back(cond);
                if (cond->getDataType() == QT_MDD) {
                    if (!definitionDomainIsSet) {
                        definitionDomain = (static_cast<QtMDD*>(cond))->getMDDObject()->getDefinitionDomain();
                        definitionDomainIsSet = true;
                    } else {
                        if (definitionDomain != (static_cast<QtMDD*>(cond))->getMDDObject()->getDefinitionDomain()) {
                            ;
                            //operand and condition mdds should have the same domain
                            LFATAL << "Error: QtCaseOp::inducedEvaluate() - The condition and result mdds don't have the same definition domain.";
                            parseInfo.setErrorNo(426);
                            throw parseInfo;
                        }
                    }
                }
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
QtCaseOp::evaluateInducedOp(QtDataList* inputList) {
    std::vector< std::pair <QtOperation*, QtDataList*> >* cacheList = new std::vector< std::pair <QtOperation*, QtDataList*> >();
    std::vector< std::pair <QtOperation*, QtData*> >* scalarCacheList = new std::vector< std::pair <QtOperation*, QtData*> >();
    QtDataList* conditionList2 = new QtDataList();
    QtOperationList* resultList = new QtOperationList();
    QtOperation* defaultResult = NULL;

    //get the case operators
    getCaseOperands(inputList, cacheList, scalarCacheList, conditionList2, resultList, defaultResult);
    //create a focus mdd object of the same dimension as the first condition
    MDDObj* focusCondMdd = (static_cast<QtMDD*>(*(conditionList2->begin())))->getMDDObject();
    MDDObj* focusMdd = new MDDObj((static_cast<MDDBaseType*>(const_cast<Type*>(dataStreamType.getType()))), focusCondMdd->getDefinitionDomain());
    //add tiles
    std::vector< boost::shared_ptr<Tile> >* tiles = new std::vector< boost::shared_ptr<Tile> >;
    std::vector< boost::shared_ptr<Tile> >* focusCondTiles = focusCondMdd->getTiles();
    if (focusCondTiles == NULL)
        focusCondTiles = new std::vector< boost::shared_ptr<Tile> >;
    std::vector< boost::shared_ptr<Tile> >::iterator tileIter;
    for (tileIter = focusCondTiles->begin(); tileIter != focusCondTiles->end(); tileIter++) {
        tiles->push_back(boost::shared_ptr<Tile>(new Tile((*tileIter)->getDomain(), this->baseType)));
    }
    //iterate through all the tiles of the focus mdd object
    vector<QtData*>::iterator condIter;
    QtOperationList::iterator resultIter;
    unsigned int tilePos = 0;
    for (tileIter = tiles->begin(); tileIter != tiles->end(); tileIter++) {
        //declare a watchdog for the changes
        std::vector<bool> changedCells((*tileIter)->getDomain().cell_count());
        //iterate through conditions and check whether the point needs to be changed
        unsigned int condPos = 0;
        for (condIter = conditionList2->begin(), resultIter = resultList->begin();
                condIter != conditionList2->end() && resultIter != resultList->end();
                condIter++, resultIter++) {
            MDDObj* condMdd = (static_cast<QtMDD*>(*condIter))->getMDDObject();
            boost::shared_ptr< std::vector< boost::shared_ptr<Tile> > > condTiles(condMdd->getTiles());
            boost::shared_ptr<Tile> condTile = condTiles->at(tilePos);
            std::vector<Tile*>* cachedTiles = new std::vector<Tile*>();
            //if the result is an mdd then fetch the cached tiles as well
            if ((*resultIter)->getDataStreamType().getDataType() == QT_MDD) {
                QtDataList* cachedData = getCachedData((*resultIter), cacheList);
                for (QtDataList::iterator i = cachedData->begin(); i != cachedData->end(); i++) {
                    boost::shared_ptr<Tile> aTile = getCorrespondingTile((static_cast<QtMDD*>(*i))->getMDDObject()->getTiles(), condTile->getDomain());
                    if (aTile == NULL) {
                        LFATAL << "Error: QtCaseOp::inducedEvaluate() - The condition and result mdds don't have the same tiling.";
                        parseInfo.setErrorNo(427);
                        throw parseInfo;
                    }
                    cachedTiles->push_back(new Tile(*aTile));
                }
            }
            //iterate through the points of each tile
            r_Miter condTileIter(&(condTile->getDomain()), &(condTile->getDomain()), condTile->getType()->getSize(), condTile->getContents());
            std::vector<r_Miter*>* defaultIter = new std::vector<r_Miter*>();
            //if on last conditions, use the same iteration for default results
            std::vector<Tile*>* cachedDefaultTiles = new std::vector<Tile*>();
            if (condPos == conditionList2->size() - 1) {
                if (defaultResult->getDataStreamType().getDataType() == QT_MDD) {
                    QtDataList* cachedData = getCachedData(defaultResult, cacheList);
                    for (QtDataList::iterator i = cachedData->begin(); i != cachedData->end(); i++) {
                        boost::shared_ptr<Tile> theTile = getCorrespondingTile((static_cast<QtMDD*>(*i))->getMDDObject()->getTiles(), condTile->getDomain());
                        Tile* aTile = new Tile(*theTile);
                        if (aTile == NULL) {
                            LFATAL << "Error: QtCaseOp::inducedEvaluate() - The condition and result mdds don't have the same tiling.";
                            parseInfo.setErrorNo(427);
                            throw parseInfo;
                        }
                        cachedDefaultTiles->push_back(aTile);
                        defaultIter->push_back(new r_Miter(&(condTile->getDomain()), &(aTile->getDomain()), aTile->getType()->getSize(), aTile->getContents()));
                    }
                }
            }
            std::vector<r_Miter*>* cacheIterators = new std::vector<r_Miter*>();
            //if there are cached tiles, iterate through them at the same time
            if (cachedTiles->size()) {
                for (std::vector<Tile*>::iterator i = cachedTiles->begin(); i != cachedTiles->end(); i++) {
                    cacheIterators->push_back(new r_Miter(&(condTile->getDomain()), &((*i)->getDomain()), (*i)->getType()->getSize(), (*i)->getContents()));
                    if (!(*i)->getDomain().covers(condTile->getDomain())) {
                        LFATAL << "Error: QtCaseOp::inducedEvaluate() - The condition and result mdds don't have the same definition domain.";
                        parseInfo.setErrorNo(426);
                        throw parseInfo;
                    }
                }
            }
            unsigned int cellCount = 0;
            while (!condTileIter.isDone()) {
                char* condPoint = condTileIter.nextCell();
                std::vector<char*>* cachedPoints = new std::vector<char*>();
                std::vector<char*>* cachedDefaultPoint = new std::vector<char*>();
                if (cachedTiles->size()) {
                    for (std::vector<r_Miter*>::iterator i = cacheIterators->begin(); i != cacheIterators->end(); i++) {
                        cachedPoints->push_back((*i)->nextCell());
                    }
                }
                if (defaultIter->size()) {
                    for (std::vector<r_Miter*>::iterator i = defaultIter->begin(); i != defaultIter->end(); i++) {
                        cachedDefaultPoint->push_back((*i)->nextCell());
                    }

                }
                if (static_cast<long>(*(condPoint)) == 1 && !changedCells.at(cellCount)) {
                    changedCells.at(cellCount) = true;
                    QtData* localResult = NULL;
                    //for MDDs, evaluate point by point
                    if ((*resultIter)->getDataStreamType().getDataType() == QT_MDD) {
                        localResult = evaluateCellByCell(inputList, *resultIter, cachedTiles, cachedPoints);
                    } else { //for base types
                        localResult = getCachedScalar((*resultIter), scalarCacheList);
                    }
                    if (localResult->getDataType() != QT_COMPLEX) {
                        (*tileIter)->setCell(cellCount, (static_cast<QtAtomicData*>(localResult))->getValueBuffer());
                    } else {
                        (*tileIter)->setCell(cellCount, (static_cast<QtComplexData*>(localResult))->getValueBuffer());
                    }
                }//on the last condition iteration, also plug in the default
                else if ((condPos == conditionList2->size() - 1) && !changedCells.at(cellCount)) {
                    //put default result
                    QtData* localResult = NULL;
                    if (defaultResult->getDataStreamType().getDataType() == QT_MDD) {
                        localResult = evaluateCellByCell(inputList, defaultResult, cachedDefaultTiles, cachedDefaultPoint);
                    } else {
                        //for base types
                        localResult = getCachedScalar(defaultResult, scalarCacheList);
                    }
                    if (localResult->getDataType() != QT_COMPLEX) {
                        (*tileIter)->setCell(cellCount, (static_cast<QtAtomicData*>(localResult))->getValueBuffer());
                    } else {
                        (*tileIter)->setCell(cellCount, (static_cast<QtComplexData*>(localResult))->getValueBuffer());
                    }
                }
                cellCount++;
                //if done cleanup
                if(cellCount == condTile->getDomain().cell_count()){
                    std::vector<char*>::iterator i;
                    delete cachedPoints;
                    delete cachedDefaultPoint;
                }
            }
            condPos++;
            //if done cleanup
            if (condPos == conditionList2->size()) {
                std::vector<Tile*>::iterator i;
                for (i = cachedTiles->begin(); i != cachedTiles->end(); i++) {
                    if ((*i)) {
                        delete (*i);
                    }
                }
                delete cachedTiles;
                for (i = cachedDefaultTiles->begin(); i != cachedDefaultTiles->end(); i++) {
                    if ((*i)) {
                        delete (*i);
                    }
                }
                delete cachedDefaultTiles;
                std::vector<r_Miter*>::iterator j;
                for (j = cacheIterators->begin(); j != cacheIterators->end(); j++) {
                    if ((*j)) {
                        delete (*j);
                    }
                }
                delete cacheIterators;
                for (j = defaultIter->begin(); j != defaultIter->end(); j++) {
                    if ((*j)) {
                        delete (*j);
                    }
                }
                delete defaultIter;
            }
        }
        tilePos++;
    }
    //cleanup
    for (condIter = conditionList2->begin(); condIter != conditionList2->end(); condIter++) {
        if (*condIter) {
            (*condIter)->deleteRef();
        }
    }

    delete conditionList2;
    delete resultList;

    //clear the cache
    //mdd
    std::vector< std::pair <QtOperation*, QtDataList*> >::iterator cacheIter;
    QtDataList::iterator dataCacheIter;
    for (cacheIter = cacheList->begin(); cacheIter != cacheList->end(); cacheIter++) {
        for (dataCacheIter = (*cacheIter).second->begin(); dataCacheIter != (*cacheIter).second->end(); dataCacheIter++) {
            if (*dataCacheIter) {
                (*dataCacheIter)->deleteRef();
            }
        }
    }
    delete cacheList;
    //scalar
    std::vector< std::pair <QtOperation*, QtData* > >::iterator scalarCacheIter;
    for (scalarCacheIter = scalarCacheList->begin(); scalarCacheIter != scalarCacheList->end();
            scalarCacheIter++) {
        if ((*scalarCacheIter).second) {
            (*scalarCacheIter).second->deleteRef();
        }
    }
    delete scalarCacheList;

    //add the tiles to the mddObj
    for (tileIter = tiles->begin(); tileIter != tiles->end(); tileIter++) {
        focusMdd->insertTile(*tileIter);
    }

    delete tiles;
    delete focusCondTiles;

    //restore the initial query tree
    restoreTree();
    //return the resulting MDD
    return new QtMDD(focusMdd);
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
	LTRACE << "qlparser";
    startTimer("QtCaseOp");
    if (this->inducedCase) {
        return this->evaluateInducedOp(inputList);
    }
    QtData* returnValue = NULL;
    bool foundClause = false;
    unsigned int pos = 0, foundAt;
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
            if (static_cast<bool>((static_cast<QtAtomicData*>(dataI))->getUnsignedValue())) {
                iter++;
                dataI = (*iter)->evaluate(inputList);
                returnValue = dataI;
                foundAt = pos;
                foundClause = true;
            }
        }//we arrived on the ELSE clause
        else if (pos == operationList->size() - 1) {
            dataI = (*iter)->evaluate(inputList);
            returnValue = dataI;
            foundAt = pos;
        }
        pos++;
    }

    //check if everything evaluated correctly
    if (!success) {
        LERROR << "Error: QtCaseOp::evaluate() - at least one operand branch is invalid.";
    }

    stopTimer();

    return returnValue;
}

/**
 * @override QtNarryOperation::printTree
 */
void
QtCaseOp::printTree(int tab, ostream& s, QtChildType mode) {

    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCaseOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;
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
 * - all the results must be of base types or mdds;
 * - type coercion is done between results, the types must be compatible.
 * 
 * @param typeTupe - the tuple containing the array types
 */
const QtTypeElement&
QtCaseOp::checkTypeInducedOp(QtTypeTuple* typeTuple) {
    QtOperationList::iterator iter;
    const BaseType* resultType = NULL;
    unsigned int pos = 0;
    for (iter = operationList->begin(); iter != operationList->end(); iter++) {
        (*iter)->checkType(typeTuple);
        if (!(pos % 2) && (pos != operationList->size() - 1)) {
            //conditions should be boolean mdds
            QtTypeElement condType = (*iter)->checkType(typeTuple);
            if (condType.getDataType() != QT_MDD) {
                LFATAL << "Error: QtCaseOp::checkInducedType() - At least one condition is not a boolean mdd.";
                parseInfo.setErrorNo(428);
                throw parseInfo;
            }
            if ((static_cast<MDDBaseType*>(const_cast<Type*>(condType.getType())))->getBaseType()->getType() != BOOLTYPE) {
                LFATAL << "Error: QtCaseOp::checkInducedType() - At least one condition is not a boolean mdd.";
                parseInfo.setErrorNo(428);
                throw parseInfo;
            }
        } else {
            QtTypeElement resType = (*iter)->checkType(typeTuple);
            //check if result types is base type
            if (!resType.isBaseType() && resType.getDataType() != QT_MDD) {
                LFATAL << "Error: QtCaseOp::checkInducedType() - At least one result type is not base type or mdd.";
                parseInfo.setErrorNo(429);
                throw parseInfo;
            }
            //make type coercion
            if (resultType) {
                //differentiate between base types and mdd base types
                if (resType.getDataType() != QT_MDD) {
                    resultType = getResultType(resultType, static_cast<BaseType*>(const_cast<Type*>(resType.getType())));
                } else {
                    resultType = getResultType(resultType, (static_cast<MDDBaseType*>(const_cast<Type*>(resType.getType())))->getBaseType());
                }
                if (!resultType) {
                    LFATAL << "Error: QtCaseOp::checkInducedType() - The results have incompatible types.";
                    parseInfo.setErrorNo(430);
                    throw parseInfo;
                }
            } else {
                //differentiate between base types and mdd base types
                if (resType.getDataType() != QT_MDD) {
                    resultType = static_cast<BaseType*>(const_cast<Type*>(resType.getType()));
                } else {
                    resultType = (static_cast<MDDBaseType*>(const_cast<Type*>(resType.getType())))->getBaseType();
                }
            }
        }
        pos++;
    }
    this->baseType = const_cast<BaseType*>(resultType);
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
	LTRACE << "qlparser";
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);
    if ((*(operationList->begin()))->checkType(typeTuple).getDataType() == QT_MDD) {
        this->inducedCase = true;
        return this->checkTypeInducedOp(typeTuple);
    }
    QtOperationList::iterator iter;
    bool whenTypesValid = true;
    bool resultTypesValid = true;
    unsigned int pos = 0;
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
                //differentiate between base types and mdd types
                if (type.getDataType() != QT_MDD) {
                    resultType = getResultType(resultType, static_cast<BaseType*>(const_cast<Type*>(type.getType())));
                } else {
                    resultType = getResultType(resultType, (static_cast<MDDBaseType*>(const_cast<Type*>(type.getType())))->getBaseType());
                }
                if (!resultType) {
                    //types are incompatible
                    resultTypesValid = false;
                }
            } else {
                //differentiate between base types and mdd types
                if (type.getDataType() != QT_MDD) {
                    resultType = static_cast<BaseType*>(const_cast<Type*>(type.getType()));
                } else {
                    resultType = (static_cast<MDDBaseType*>(const_cast<Type*>(type.getType())))->getBaseType();
                }
            }
        }
        pos++;
    }

    //some when clause is not boolean
    if (!whenTypesValid) {
        LFATAL << "Error: QtCaseOp::checkType() - At least one condition is not a boolean.";
        parseInfo.setErrorNo(431);
        throw parseInfo;
    }

    //result types are different
    if (!resultTypesValid) {
        LFATAL << "Error: QtCaseOp::checkType() - The results have incompatible types.";
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

/**
 * Checks if the given type is signed.
 * 
 * @param type - the type to be checked
 * @return 1 if true, 0 otherwise
 */
int QtCaseOp::isSignedType(const BaseType* type) {
    return ( type->getType() >= LONG && type->getType() <= COMPLEXTYPE2);
}

/**
 * The method evaluated an array returning operation cell by cell. This is
 * necessary since certain operations can't be applied to whole arrays, but only
 * to those cells which are in the definition domain.
 * 
 * @param inputList - the list to be passed to the QtOperation evaluate()
 * @param currentOperation - the operation to be evaluated cell by cell
 * @param currentTiles - the current tiles on which Case operates
 * @param cachedPoints - the values of the tile points from cache
 * @return the point value of the current operation
 */
QtData* QtCaseOp::evaluateCellByCell(QtDataList* inputList, QtOperation* currentOperation,
        std::vector<Tile*>* currentTiles, std::vector<char*>* cachedPoints) {
    QtData* localResult = NULL;
    std::vector<Tile*>::iterator tileIter;
    std::vector<char*>::iterator pointIter;
    QtNodeList::iterator mddVar;
    //if the current node is an mdd var itself then it has no op applied to, so 
    //return the cell
    if (currentOperation->getNodeType() == QT_MDD_VAR) {
        QtScalarData* point = new QtScalarData();
        point->setValueBuffer(cachedPoints->at(0));
        point->setValueType(currentTiles->at(0)->getType());
        localResult = static_cast<QtData*>(point);
    } else {
        //get the mdd vars
        QtNodeList* mddVars = currentOperation->getChild(QT_MDD_VAR, QT_ALL_NODES);
        QtNodeList* originalTree = new QtNodeList();
        QtNodeList* replacedTree = new QtNodeList();
        for (mddVar = mddVars->begin(), tileIter = currentTiles->begin(), pointIter = cachedPoints->begin();
                mddVar != mddVars->end() && tileIter != currentTiles->end() && pointIter != cachedPoints->end();
                mddVar++, tileIter++, pointIter++) {
            QtScalarData* point = new QtScalarData();
            point->setValueBuffer(*pointIter);
            point->setValueType((*tileIter)->getType());
            QtConst* newInput = new QtConst(point);
            //replace the mdd in the operation tree with the point
            (*mddVar)->getParent()->setInput(static_cast<QtOperation*>(*mddVar), newInput);
            originalTree->push_back((*mddVar));
            replacedTree->push_back(newInput);
        }
        //evaluate the newly formed operation
        //before, the operation was returning an array, set the dataStreamType to a baseType
        //this has to be done for every operation in the new tree
        QtNodeList* currentTree = currentOperation->getChilds(QT_ALL_NODES);
        currentTree->push_front(currentOperation);

        QtNodeList* streamsChanged = new QtNodeList();
        std::vector<QtTypeElement>* oldStreams = new std::vector<QtTypeElement>();
        for (QtNodeList::iterator i = currentTree->begin(); i != currentTree->end(); i++) {
            QtTypeElement currentDataStreamType = (static_cast<QtOperation*>(*i))->getDataStreamType();
            QtTypeElement oldDataStreamType;
            if (currentDataStreamType.getDataType() != QT_TYPE_UNKNOWN && !currentDataStreamType.isBaseType()) {
                oldDataStreamType = currentDataStreamType;
                const BaseType* newOpType = (static_cast<MDDBaseType*>(const_cast<Type*>(currentDataStreamType.getType())))->getBaseType();
                currentDataStreamType.setType(newOpType);
                (static_cast<QtOperation*>(*i))->setDataStreamType(currentDataStreamType);
                streamsChanged->push_back((*i));
                oldStreams->push_back(oldDataStreamType);
            }
        }
        localResult = currentOperation->evaluate(inputList);
        //restore the query tree
        QtNodeList::iterator orig, replaced;
        for (orig = originalTree->begin(), replaced = replacedTree->begin();
                orig != originalTree->end() && replaced != replacedTree->end(); orig++, replaced++) {
            (*replaced)->getParent()->setInput(static_cast<QtOperation*>(*replaced), static_cast<QtOperation*>(*orig));
        }
        //restore the dataStreamTypes
        QtNodeList::iterator streamChangedIter;
        std::vector<QtTypeElement>::iterator oldStreamIter;
        for (streamChangedIter = streamsChanged->begin(), oldStreamIter = oldStreams->begin();
                streamChangedIter != streamsChanged->end() && oldStreamIter != oldStreams->end();
                streamChangedIter++, oldStreamIter++) {
            (static_cast<QtOperation*>(*streamChangedIter))->setDataStreamType((*oldStreamIter));
        }
        //cleanup
        delete mddVars;
        delete originalTree;
        delete replacedTree;
        delete streamsChanged;
        delete oldStreams;
        delete currentTree;
    }
    return localResult;
}

/**
 * Returns the cached data list corresponding to the operation
 * @param op - the operation with cached data
 * @param cacheList - the list of cached values
 * @return a list of evaluated, cached data, corresponding to the op
 */
QtNode::QtDataList* QtCaseOp::getCachedData(QtOperation* op,
        std::vector< std::pair <QtOperation*, QtDataList* > >* cacheList) {
    QtDataList* returnValue = NULL;
    for (std::vector< std::pair <QtOperation*, QtDataList* > >::iterator i = cacheList->begin();
            i != cacheList->end(); i++) {
        if (op == (*i).first) {
            returnValue = (*i).second;
            break;
        }
    }
    return returnValue;
}

/**
 * Restores the tree to the original structure
 * The removedNodes has the following structure:
 * <parent < removedNode, currentNode> >
 * removedNode has to be re-introduced between parent and currentNode
 */
void QtCaseOp::restoreTree() {
    std::vector< std::pair < QtNode*, std::pair <QtNode*, QtNode*> > >::iterator i;
    for (i = this->removedNodes.begin(); i != this->removedNodes.end(); i++) {
        (static_cast<QtOperation*>((*i).first))->setInput(static_cast<QtOperation*>((*i).second.second), static_cast<QtOperation*>((*i).second.first));
        (static_cast<QtOperation*>((*i).second.second))->setParent((*i).second.first);
    }
}

/**
 * Gets the tile corresponding to the given domain from a tile vector. Necessary
 * since, for persistent objects, tiles are returned in the order in which they
 * are stored.
 * @param tiles - the vector of tiles
 * @param domain - the domain of the wanted tile
 * @return The tile corresponding to the given domain
 */
 boost::shared_ptr<Tile> QtCaseOp::getCorrespondingTile(std::vector< boost::shared_ptr<Tile> >* tiles, const r_Minterval& domain) {
    boost::shared_ptr<Tile> returnValue;
    for (std::vector< boost::shared_ptr<Tile> >::iterator i = tiles->begin(); i != tiles->end(); i++) {
        if ((*i)->getDomain().covers(domain)) {
            returnValue = (*i);
            break;
        }
    }
    delete tiles;
    return returnValue;
}

/**
 * Gets the cached scalar data corresponding to an operation.
 * @param op - the operation for which data is fetched
 * @param scalarCacheList - the list of cached data
 * @return - the data corresponding to the operation
 */
QtData* QtCaseOp::getCachedScalar(QtOperation* op, std::vector<std::pair<QtOperation*, QtData*> >* scalarCacheList) {
    QtData* returnValue = NULL;
    std::vector<std::pair<QtOperation*, QtData*> >::iterator i;
    for (i = scalarCacheList->begin(); i != scalarCacheList->end(); i++) {
        if (op == (*i).first) {
            returnValue = (*i).second;
            break;
        }
    }
    return returnValue;
}

/**
 * Adds the mdd found in the operation tree to cache
 * @param op - the operation to be searched for mdds
 * @param cacheList - the cache list
 * @param inputList - the input list to be passed to QtOperation evaluate()
 */
void QtCaseOp::addMddsToCache(QtDataList* inputList, QtOperation* &op, std::vector<std::pair<QtOperation*,
        QtDataList*> >* cacheList) {
    //get all the MDD variables
    QtNodeList* mddVars = op->getChild(QT_MDD_VAR, QT_ALL_NODES);
    //if the node itself is a mdd variable add it to the list
    if (op->getNodeType() == QT_MDD_VAR) {
        mddVars->push_back(op);
    }
    QtDataList* correspondingMdds = new QtDataList();
    for (QtNodeList::iterator i = mddVars->begin(); i != mddVars->end(); i++) {
        //if an mdd has a domain operation applied, consider them together and
        //remove the domain op from the tree
        QtOperation* trimmedArray = static_cast<QtOperation*>(*i);
        while (trimmedArray->getParent()->getNodeType() == QT_DOMAIN_OPERATION) {
            this->removedNodes.push_back(std::make_pair(trimmedArray->getParent()->getParent(),
                    std::make_pair(trimmedArray->getParent(), trimmedArray)));
            trimmedArray = static_cast<QtOperation*>((trimmedArray)->getParent());
        }
        QtData* evaluatedTrimmedArray = trimmedArray->evaluate(inputList);
        correspondingMdds->push_back(evaluatedTrimmedArray);
        //remove any eventual domain operation encountered from the tree
        trimmedArray->getParent()->setInput(trimmedArray, static_cast<QtOperation*>(*i));
    }
    cacheList->push_back(std::make_pair(op, correspondingMdds));
}
#include "qlparser/qtcaseop.icc"


