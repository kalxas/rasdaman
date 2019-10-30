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

#include "qlparser/qtcaseop.hh"
#include "raslib/rmdebug.hh"

#include "mymalloc/mymalloc.h"
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
#include "relcatalogif/structtype.hh"
#include "relcatalogif/mdddomaintype.hh"

#include <logging.hh>

#include <iostream>
#include <complex>
#include <string>
#include <bits/stl_bvector.h>
#include "raslib/miter.hh"
#include "qtvariable.hh"
#include "qtcaseequality.hh"

using namespace std;

/**
 * Defines the node type, for further identification.
 */
const QtNode::QtNodeType QtCaseOp::nodeType = QT_CASEOP;

/**
 * Class constructor
 *
 * @param opList - the arguments of the operation
 */
QtCaseOp::QtCaseOp(QtOperationList *opList)
    : QtNaryOperation(opList), inducedCase(false), baseType(NULL)
{
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
QtCaseOp::getCaseOperands(QtDataList *inputList, std::vector<std::pair <QtOperation *, QtDataList *>> *cacheList,
                          std::vector<std::pair <QtOperation *, QtData *>> *scalarCache,
                          QtDataList *conditionList2, QtOperationList *resultList, QtOperation *&defaultResult)
{
    QtOperationList::iterator iter;
    r_Minterval definitionDomain;
    bool definitionDomainIsSet = false;
    unsigned int pos = 0;
    for (iter = operationList->begin(); (iter != (operationList->end())); iter++)
    {
        //else is specified
        if (pos == operationList->size() - 1)
        {
            //we are on the last element, so the default result
            if ((*iter)->getDataStreamType().getDataType() == QT_MDD)
            {
                addMddsToCache(inputList, (*iter), cacheList);
                defaultResult = *iter;
            }
            else
            {
                defaultResult = *iter;
                scalarCache->push_back(std::make_pair(*iter, (*iter)->evaluate(inputList)));
            }
        }
        else
        {
            if (pos % 2)
            {
                //we got results here
                //if expression, evaluate the mdds, but not the entire expression
                if ((*iter)->getDataStreamType().getDataType() == QT_MDD)
                {
                    addMddsToCache(inputList, (*iter), cacheList);
                    resultList->push_back((*iter));
                }//if base type, evaluate it and add it to the cache list
                else
                {
                    resultList->push_back(*iter);
                    scalarCache->push_back(std::make_pair(*iter, (*iter)->evaluate(inputList)));
                }
            }
            else
            {
                //we got conditions here
                this->conditionList->push_back(*iter);
                QtData *cond = (*iter)->evaluate(inputList);
                conditionList2->push_back(cond);
                if (cond->getDataType() == QT_MDD)
                {
                    if (!definitionDomainIsSet)
                    {
                        definitionDomain = (static_cast<QtMDD *>(cond))->getMDDObject()->getDefinitionDomain();
                        definitionDomainIsSet = true;
                    }
                    else
                    {
                        if (definitionDomain != (static_cast<QtMDD *>(cond))->getMDDObject()->getDefinitionDomain())
                        {
                            ;
                            //operand and condition mdds should have the same domain
                            LERROR << "Error: QtCaseOp::inducedEvaluate() - The condition and result mdds don't have the same definition domain.";
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
QtData *
QtCaseOp::evaluateInducedOp(QtDataList *inputList)
{
    std::vector<std::pair <QtOperation *, QtDataList *>> *cacheList = new std::vector<std::pair <QtOperation *, QtDataList *>>();
    std::vector<std::pair <QtOperation *, QtData *>> *scalarCacheList = new std::vector<std::pair <QtOperation *, QtData *>>();
    QtDataList *conditionList2 = new QtDataList();
    QtOperationList *resultList = new QtOperationList();
    QtOperation *defaultResult = NULL;
    MDDObj *finalResObj = NULL;
    //get the case operators
    getCaseOperands(inputList, cacheList, scalarCacheList, conditionList2, resultList, defaultResult);
    //create a focus mdd object of the same dimension as the first condition
    QtMDD *focusCondMdd = static_cast<QtMDD *>(*(conditionList2->begin()));
    MDDObj *focusCondMddObj = (focusCondMdd)->getMDDObject();   

    MDDDomainType *mddCondMaskType = new MDDDomainType("tmp0", focusCondMddObj->getCellType(), focusCondMddObj->getDefinitionDomain());
    TypeFactory::addTempType(mddCondMaskType);
    MDDObj *conditionMask = new MDDObj(mddCondMaskType, focusCondMddObj->getDefinitionDomain(), focusCondMddObj->getNullValues()); 
    Tile *condMaskTile = NULL;
    Tile *finResTile = NULL;
    auto *focuCondTiles = focusCondMddObj->getTiles();
    vector<boost::shared_ptr<Tile>>::iterator tileFocusCondIt;
    for (tileFocusCondIt = focuCondTiles->begin();tileFocusCondIt != focuCondTiles->end(); tileFocusCondIt++){
        condMaskTile = new Tile(tileFocusCondIt->get()->getDomain(), focusCondMddObj->getCellType());
        conditionMask->insertTile(condMaskTile);
    }

    QtData *defResultData = ((defaultResult)->evaluate(inputList));

    MDDObj *defResultMddObj = NULL;
    if (defResultData->isScalarData())
    {
        QtScalarData *defResultScData = static_cast<QtScalarData *>(defResultData);
        auto baseType = defResultScData->getValueType();
        MDDDomainType *defResMddBaseType = new MDDDomainType("tmp1",baseType, focusCondMddObj->getDefinitionDomain());
        defResultMddObj = new MDDObj(defResMddBaseType, focusCondMddObj->getDefinitionDomain(), focusCondMddObj->getNullValues());
        Tile *defResultMddObjTile = NULL;

        for (tileFocusCondIt = focuCondTiles->begin();tileFocusCondIt != focuCondTiles->end(); tileFocusCondIt++){
            defResultMddObjTile = new Tile(tileFocusCondIt->get()->getDomain(), baseType);
            UnaryOp *myOp = NULL;
            myOp = Ops::getUnaryOp(Ops::OP_IDENTITY, baseType, defResultMddObj->getCellType(), 0, 0); 

            char *cellRes = NULL;
            char *dummy1 = defResultMddObjTile->getContents();
            assert(dummy1);
            r_Minterval tileDomain = focusCondMdd->getLoadDomain();
            r_Miter defResTileIter(&defResultMddObjTile->getDomain(), &defResultMddObjTile->getDomain(), defResultMddObjTile->getType()->getSize(), dummy1);
            while (!defResTileIter.isDone())
            {
                cellRes = defResTileIter.nextCell();
                (*myOp)(cellRes, defResultScData->getValueBuffer());
            }
            
            defResultMddObj->insertTile(defResultMddObjTile);
        }   

       
    }
    else {
        defResultMddObj = (static_cast<QtMDD*>(defResultData))->getMDDObject();
    }
                    
    vector<QtData *>::iterator condIter;
    QtOperationList::iterator resultIter;

    for (condIter = conditionList2->begin(), resultIter = resultList->begin();
            condIter != conditionList2->end() && resultIter != resultList->end();
            condIter++, resultIter++)
    {
    bool finalBranch = false;
    QtOperationList::iterator lastIter = resultList->end();
    --lastIter;
    if (lastIter == resultIter){
        finalBranch = true;
    }

    QtMDD *condMdd = static_cast<QtMDD *>(*condIter);
    QtData *resultData = (*resultIter)->evaluate(inputList);
    MDDObj *condMddObj = (condMdd)->getMDDObject();
    const r_Minterval &areaOp1 = condMdd->getLoadDomain();
    MDDObj *resultMddObj = NULL;
    if (resultData->isScalarData())
    {
        QtScalarData *resultScData = static_cast<QtScalarData *>(resultData);
        auto baseType = resultScData->getValueType();
        MDDDomainType *resMddBaseType = new MDDDomainType("tmp1",baseType, areaOp1);
        resultMddObj = new MDDObj(resMddBaseType, condMddObj->getDefinitionDomain(), condMddObj->getNullValues());
        Tile *resultMddObjTile = NULL;
        
        resultMddObjTile = new Tile(resultMddObj->getDefinitionDomain(), resultMddObj->getCellType());

        UnaryOp *myOp = NULL;
        myOp = Ops::getUnaryOp(Ops::OP_IDENTITY, baseType, resultMddObj->getCellType(), 0, 0); 

        char *cellRes = NULL;
        char *dummy1 = resultMddObjTile->getContents();
        assert(dummy1);
        r_Minterval tileDomain = condMdd->getLoadDomain();
        r_Miter resTileIter(&tileDomain, &resultMddObjTile->getDomain(), resultMddObjTile->getType()->getSize(), dummy1);
        while (!resTileIter.isDone())
        {
            cellRes = resTileIter.nextCell();
            // execute operation on cell
            (*myOp)(cellRes, resultScData->getValueBuffer());
        }
        resultMddObj->insertTile(resultMddObjTile);
    }
    else {
        resultMddObj = (static_cast<QtMDD*>(resultData))->getMDDObject();
    }
    const r_Minterval &areaOp2 = resultMddObj->getDefinitionDomain();
    if (areaOp1.get_extent() == areaOp2.get_extent())
        {
                vector<boost::shared_ptr<Tile>> *allTilesCond;
            vector<boost::shared_ptr<Tile>> *allTilesCondMask;
         // contains all tiles of op2 which intersect a given op1 Tile in the relevant area.
            vector<boost::shared_ptr<Tile>> *allTilesResult = NULL;
            vector<boost::shared_ptr<Tile>> *allTilesFinalResult = NULL;
         // iterators for tiles of the MDDs
            vector<boost::shared_ptr<Tile>>::iterator tileCondIt;
            vector<boost::shared_ptr<Tile>>::iterator tileCondMaskIt;
            vector<boost::shared_ptr<Tile>>::iterator tileResIt;
            vector<boost::shared_ptr<Tile>>::iterator tileFinResIt;
         
            r_Minterval intersectDom;
            r_Minterval intersectDom2;
            // pointer to generated result tile
            Tile *resTile = NULL;

         // create MDDObj for result
            MDDDomainType *mddBaseType = new MDDDomainType("tmp2", resultMddObj->getCellType(), areaOp1);
            TypeFactory::addTempType(mddBaseType);

            if (finalResObj == NULL){
                finalResObj = new MDDObj(mddBaseType, areaOp1, resultMddObj->getNullValues());   // FIXME consider op2 too
                finResTile = new Tile(finalResObj->getDefinitionDomain(), finalResObj->getCellType());
                finalResObj->insertTile(finResTile);
            } 

            allTilesCond = condMddObj->getTiles();
            allTilesCondMask = conditionMask->getTiles();

            for (tileCondIt = allTilesCond->begin(),tileCondMaskIt = allTilesCondMask->begin(); tileCondIt !=  allTilesCond->end(), tileCondMaskIt !=  allTilesCondMask->end(); tileCondIt++,tileCondMaskIt++)
            {
                // domain of the op1 tile
                const r_Minterval &tileOp1Dom = (*tileCondIt)->getDomain();
                
                allTilesResult = resultMddObj->intersect(tileOp1Dom);
                allTilesFinalResult = finalResObj->intersect(tileOp1Dom);
                r_Minterval intersectionTileOp1Dom(tileOp1Dom.create_intersection(areaOp1));
             // iterate over intersecting tiles
                tileFinResIt  = allTilesFinalResult->begin();
                for (tileResIt  = allTilesResult->begin();
                        tileResIt != allTilesResult->end();
                        tileResIt++)
                {
                    const char *cellOpRes = NULL;
                    const char *cellOpFinalRes = NULL;
                    const char *cellOpCond = NULL;
                    const char *cellOpCondMask = NULL;
                    auto *opResTile = tileResIt->get();
                    auto *opFinResTile = tileFinResIt->get();
                    auto *opCondTile = tileCondIt->get(); 
                    auto *opMaskTile = tileCondMaskIt->get();
                    
                    const char *dummy2 = opResTile->getContents();
                    const char *dummy3 = opCondTile->getContents();
                    const char *dummy4 = opFinResTile->getContents();
                    const char *dummyMask = opMaskTile->getContents();
                    
                    assert(dummy2 && dummy3 && dummy4 &&dummyMask);
                    r_Miter opResTileIter(&intersectionTileOp1Dom, &opResTile->getDomain(), opResTile->getType()->getSize(), dummy2);
                    r_Miter opCondTileIter(&intersectionTileOp1Dom, &opCondTile->getDomain(), opCondTile->getType()->getSize(), dummy3);
                    r_Miter finResTileIter(&intersectionTileOp1Dom, &opFinResTile->getDomain(), opFinResTile->getType()->getSize(), dummy4);
                    r_Miter opCondMaskTileIter(&intersectionTileOp1Dom, &opMaskTile->getDomain(), opMaskTile->getType()->getSize(), dummyMask);
                 while (!finResTileIter.isDone())
                    {
                        cellOpFinalRes = finResTileIter.nextCell();
                        cellOpRes = opResTileIter.nextCell();
                        cellOpCond = opCondTileIter.nextCell();
                        cellOpCondMask = opCondMaskTileIter.nextCell();
                        if  (*(unsigned char *)(cellOpCond)!=0 && *(unsigned char *)(cellOpCondMask)==0){ 
                            
                            *(unsigned char *)(cellOpCondMask) = (unsigned char )(1);
                            auto op1Type = opResTile->getType()->getType();
                            if (op1Type == CHAR){
                                *(unsigned char *)(cellOpFinalRes) = *(unsigned char *)(const_cast<char *>(cellOpRes));
                            }
                            else if (op1Type == BOOLTYPE){
                                *(unsigned char *)(cellOpFinalRes) = *(unsigned char *)(const_cast<char *>(cellOpRes));
                            }
                            else if (op1Type >= ULONG &&op1Type<=BOOLTYPE){
                                *(r_ULong *)(cellOpFinalRes) = *(r_ULong *)(const_cast<char *>(cellOpRes));
                            }
                            else if (op1Type >= LONG &&op1Type<=OCTET){
                                *(r_Long *)(cellOpFinalRes) = *(r_Long *)(const_cast<char *>(cellOpRes));
                            }
                            else if (op1Type == FLOAT || op1Type == DOUBLE){
                                *(r_Double *)(cellOpFinalRes) = *(r_Double *)(const_cast<char *>(cellOpRes));
                            }
                            else if (op1Type == COMPLEXTYPE1){
                                
                                *const_cast<std::complex<float>*>(reinterpret_cast<const std::complex<float>*>(cellOpFinalRes)) = *reinterpret_cast<const std::complex<float>*>(cellOpRes);
                            }
                            else if (op1Type == COMPLEXTYPE2){
                                
                                *const_cast<std::complex<double>*>(reinterpret_cast<const std::complex<double>*>(cellOpFinalRes)) = *reinterpret_cast<const std::complex<double>*>(cellOpRes);
                            }
                            else if (op1Type == CINT16){
                                *const_cast<std::complex<int16_t>*>(reinterpret_cast<const std::complex<int16_t>*>(cellOpFinalRes)) = *reinterpret_cast<const std::complex<int16_t>*>(cellOpRes);

                            }
                            else if (op1Type == CINT32){
                                *const_cast<std::complex<int32_t>*>(reinterpret_cast<const std::complex<int32_t>*>(cellOpFinalRes)) = *reinterpret_cast<const std::complex<int32_t>*>(cellOpRes);
                            }
                            else if (op1Type == STRUCT){
                                int numElems = dynamic_cast<StructType *>(const_cast<BaseType *>(opResTile->getType()))->getNumElems();
                                    auto *type1 = opResTile->getType();
                                    size_t offset1 = 0;
                                    type1 = (dynamic_cast<StructType *>(const_cast<BaseType *>(type1)))->getElemType(1);
                                    //char dummy = 1;
                                    for (size_t i = 0; i < numElems; i++)
                                    {    
                                        offset1 = (dynamic_cast<StructType *>(const_cast<BaseType *>(opResTile->getType())))->getOffset(i);
                                        if (type1->getType() == CHAR){
                                            *(unsigned char *)(cellOpFinalRes+offset1) =  *(unsigned char *)(const_cast<char *>(cellOpRes+offset1));
                                        }
                                        else if (type1->getType() == BOOLTYPE){
                                            *(unsigned char *)(cellOpFinalRes+offset1) = *(unsigned char *)(const_cast<char *>(cellOpRes+offset1));
                                        }
                                        else if (type1->getType() >= ULONG &&type1->getType()<=BOOLTYPE){
                                            *(r_ULong *)(cellOpFinalRes+offset1) = *(r_ULong *)(const_cast<char *>(cellOpRes+offset1));
                                        }
                                        else if (type1->getType() >= LONG &&type1->getType()<=OCTET){
                                            *(r_Long *)(cellOpFinalRes+offset1) = *(r_Long *)(const_cast<char *>(cellOpRes+offset1));
                                        }
                                        else if (type1->getType() == FLOAT || type1->getType() == DOUBLE){
                                            *(r_Double *)(cellOpFinalRes+offset1) = *(r_Double *)(const_cast<char *>(cellOpRes+offset1));
                                        }
                                        
                                    }
                            }
                        }
                        
                         
                    }
                  
                }
                delete allTilesResult;
                allTilesResult = NULL;
                
                if (finalBranch){
                    
                    vector<boost::shared_ptr<Tile>> *allTilesDefResult = NULL;
                    vector<boost::shared_ptr<Tile>>::iterator tileDefResIt;
                    allTilesDefResult = defResultMddObj->intersect(tileOp1Dom);
                    
                    allTilesFinalResult = finalResObj->intersect(tileOp1Dom);
                    tileFinResIt  = allTilesFinalResult->begin();
                
                    for (tileDefResIt  = allTilesDefResult->begin(); tileDefResIt != allTilesDefResult->end(); tileDefResIt++)
                    {
                        const char *cellOpFinalRes = NULL;
                        const char *cellOpCondMask = NULL;
                        const char *cellDefRes = NULL;
                        auto *opDefResTile = tileDefResIt->get();
                        auto *opFinResTile = tileFinResIt->get();
                        auto *opMaskTile = tileCondMaskIt->get();

                        const char *dummy2 = opDefResTile->getContents();
                        const char *dummy3 = opFinResTile->getContents();
                        const char *dummyMask = opMaskTile->getContents();
                        assert(dummy2 && dummy3 &&dummyMask);

                        r_Miter opDefResTileIter(&intersectionTileOp1Dom, &opDefResTile->getDomain(), opDefResTile->getType()->getSize(), dummy2);
                        r_Miter finResTileIter(&intersectionTileOp1Dom, &opFinResTile->getDomain(), opFinResTile->getType()->getSize(), dummy3);
                        r_Miter opCondMaskTileIter(&intersectionTileOp1Dom, &opMaskTile->getDomain(), opMaskTile->getType()->getSize(), dummyMask);
                        while (!finResTileIter.isDone()){
                            cellOpFinalRes = finResTileIter.nextCell();
                            cellDefRes = opDefResTileIter.nextCell();
                            cellOpCondMask = opCondMaskTileIter.nextCell();
                            if  (*(unsigned char *)(cellOpCondMask)==0){ 

                                *(unsigned char *)(cellOpCondMask) = (unsigned char )(1);
                                auto op1Type = opDefResTile->getType()->getType();
                                if (op1Type == CHAR){
                                    *(unsigned char *)(cellOpFinalRes) =  *(unsigned char *)(const_cast<char *>(cellDefRes));
                                }
                                else if (op1Type == BOOLTYPE){
                                    *(unsigned char *)(cellOpFinalRes) = *(unsigned char *)(const_cast<char *>(cellDefRes));
                                }
                                else if (op1Type >= ULONG &&op1Type<=BOOLTYPE){
                                    *(r_ULong *)(cellOpFinalRes) = *(r_ULong *)(const_cast<char *>(cellDefRes));
                                }
                                else if (op1Type >= LONG &&op1Type<=OCTET){
                                    *(r_Long *)(cellOpFinalRes) = *(r_Long *)(const_cast<char *>(cellDefRes));
                                }
                                else if (op1Type == FLOAT || op1Type == DOUBLE){
                                    *(r_Double *)(cellOpFinalRes) = *(r_Double *)(const_cast<char *>(cellDefRes));
                                }
                                else if (op1Type == COMPLEXTYPE1){
                                
                                *const_cast<std::complex<float>*>(reinterpret_cast<const std::complex<float>*>(cellOpFinalRes)) = *reinterpret_cast<const std::complex<float>*>(cellDefRes);
                                }
                                else if (op1Type == COMPLEXTYPE2){

                                    *const_cast<std::complex<double>*>(reinterpret_cast<const std::complex<double>*>(cellOpFinalRes)) = *reinterpret_cast<const std::complex<double>*>(cellDefRes);
                                }
                                else if (op1Type == CINT16){
                                    *const_cast<std::complex<int16_t>*>(reinterpret_cast<const std::complex<int16_t>*>(cellOpFinalRes)) = *reinterpret_cast<const std::complex<int16_t>*>(cellDefRes);

                                }
                                else if (op1Type == CINT32){
                                    *const_cast<std::complex<int32_t>*>(reinterpret_cast<const std::complex<int32_t>*>(cellOpFinalRes)) = *reinterpret_cast<const std::complex<int32_t>*>(cellDefRes);
                                }
                                else if (op1Type == STRUCT){
                                    int numElems = dynamic_cast<StructType *>(const_cast<BaseType *>(opDefResTile->getType()))->getNumElems();
                                    auto *type1 = opDefResTile->getType();
                                    size_t offset1 = 0;
                                    type1 = (dynamic_cast<StructType *>(const_cast<BaseType *>(type1)))->getElemType(1);
                                    
                                    for (size_t i = 0; i < numElems; i++)
                                    {    
                                        offset1 = (dynamic_cast<StructType *>(const_cast<BaseType *>(opDefResTile->getType())))->getOffset(i);
                                        if (type1->getType() == CHAR){
                                            *(unsigned char *)(cellOpFinalRes+offset1) =  *(unsigned char *)(const_cast<char *>(cellDefRes+offset1));
                                        }
                                        else if (type1->getType() == BOOLTYPE){
                                            *(unsigned char *)(cellOpFinalRes+offset1) = *(unsigned char *)(const_cast<char *>(cellDefRes+offset1));
                                        }
                                        else if (type1->getType() >= ULONG &&type1->getType()<=BOOLTYPE){
                                            *(r_ULong *)(cellOpFinalRes+offset1) = *(r_ULong *)(const_cast<char *>(cellDefRes+offset1));
                                        }
                                        else if (type1->getType() >= LONG &&type1->getType()<=OCTET){
                                            *(r_Long *)(cellOpFinalRes+offset1) = *(r_Long *)(const_cast<char *>(cellDefRes+offset1));
                                        }
                                        else if (type1->getType() == FLOAT || type1->getType() == DOUBLE){
                                            *(r_Double *)(cellOpFinalRes+offset1) = *(r_Double *)(const_cast<char *>(cellDefRes+offset1));
                                        }
                                        
                                    }
                                }
                            }

                        }
                    }
                    delete allTilesDefResult;
                    allTilesDefResult = NULL;

                    delete allTilesFinalResult;
                    allTilesFinalResult = NULL;
                }
            }
            
            delete allTilesCond;
            allTilesCond = NULL;
                    
        }
        else
        {
            LERROR << "Domains of the operands are incompatible.";
            LERROR << "areaOp1 " << areaOp1 << " with extent " << areaOp1.get_extent();
            LERROR << "areaOp2 " << areaOp2 << " with extent " << areaOp2.get_extent();
            throw r_Error(351);
        }
            
        }
    for (condIter = conditionList2->begin(); condIter != conditionList2->end(); condIter++)
    {
        if (*condIter)
        {
            (*condIter)->deleteRef();
        }
    }

    delete conditionList2;
    delete resultList;

    //clear the cache
    //mdd
    std::vector<std::pair <QtOperation *, QtDataList *>>::iterator cacheIter;
    QtDataList::iterator dataCacheIter;
    for (cacheIter = cacheList->begin(); cacheIter != cacheList->end(); cacheIter++)
    {
        for (dataCacheIter = (*cacheIter).second->begin(); dataCacheIter != (*cacheIter).second->end(); dataCacheIter++)
        {
            if (*dataCacheIter)
            {
                int x = (*dataCacheIter)->deleteRef();
            }
        }
    }
    delete cacheList;
    //scalar
    std::vector<std::pair <QtOperation *, QtData *>>::iterator scalarCacheIter;
    for (scalarCacheIter = scalarCacheList->begin(); scalarCacheIter != scalarCacheList->end();
            scalarCacheIter++)
    {
        if ((*scalarCacheIter).second)
        {
            (*scalarCacheIter).second->deleteRef();
        }
    }
    delete scalarCacheList;

    
    delete conditionMask;

    restoreTree();
    //return the resulting MDD
    return new QtMDD(finalResObj);
}
QtData *QtCaseOp::safeEvaluateInducedOp(QtDataList *inputList)
	{
	    std::vector<std::pair <QtOperation *, QtDataList *>> *cacheList = new std::vector<std::pair <QtOperation *, QtDataList *>>();
	    std::vector<std::pair <QtOperation *, QtData *>> *scalarCacheList = new std::vector<std::pair <QtOperation *, QtData *>>();
	    QtDataList *conditionList2 = new QtDataList();
	    QtOperationList *resultList = new QtOperationList();
	    QtOperation *defaultResult = NULL;
	
	    //get the case operators
	    getCaseOperands(inputList, cacheList, scalarCacheList, conditionList2, resultList, defaultResult);
	    //create a focus mdd object of the same dimension as the first condition
	    MDDObj *focusCondMdd = (static_cast<QtMDD *>(*(conditionList2->begin())))->getMDDObject();
	    MDDObj *focusMdd = new MDDObj((static_cast<MDDBaseType *>(const_cast<Type *>(dataStreamType.getType()))), focusCondMdd->getDefinitionDomain());
	    //add tiles
	    std::vector<boost::shared_ptr<Tile>> *tiles = new std::vector<boost::shared_ptr<Tile>>;
	    std::vector<boost::shared_ptr<Tile>> *focusCondTiles = focusCondMdd->getTiles();
	    if (focusCondTiles == NULL)
	    {
	        focusCondTiles = new std::vector<boost::shared_ptr<Tile>>;
	    }
	    std::vector<boost::shared_ptr<Tile>>::iterator tileIter;
	    for (tileIter = focusCondTiles->begin(); tileIter != focusCondTiles->end(); tileIter++)
	    {
	        tiles->push_back(boost::shared_ptr<Tile>(new Tile((*tileIter)->getDomain(), this->baseType)));
	    }
	    //iterate through all the tiles of the focus mdd object
	    vector<QtData *>::iterator condIter;
	    QtOperationList::iterator resultIter;
	    unsigned int tilePos = 0;
	    for (tileIter = tiles->begin(); tileIter != tiles->end(); tileIter++)
	    {
	        //declare a watchdog for the changes
	        std::vector<bool> changedCells((*tileIter)->getDomain().cell_count());
	        //iterate through conditions and check whether the point needs to be changed
	        unsigned int condPos = 0;
	        for (condIter = conditionList2->begin(), resultIter = resultList->begin();
	                condIter != conditionList2->end() && resultIter != resultList->end();
	                condIter++, resultIter++)
	        {
	            MDDObj *condMdd = (static_cast<QtMDD *>(*condIter))->getMDDObject();
	            boost::shared_ptr<std::vector<boost::shared_ptr<Tile>>> condTiles(condMdd->getTiles());
	            boost::shared_ptr<Tile> condTile = condTiles->at(tilePos);
	            std::vector<Tile *> *cachedTiles = new std::vector<Tile *>();
	            //if the result is an mdd then fetch the cached tiles as well
	            if ((*resultIter)->getDataStreamType().getDataType() == QT_MDD)
	            {
	                QtDataList *cachedData = getCachedData((*resultIter), cacheList);
	                for (QtDataList::iterator i = cachedData->begin(); i != cachedData->end(); i++)
	                {
	                    boost::shared_ptr<Tile> aTile = getCorrespondingTile((static_cast<QtMDD *>(*i))->getMDDObject()->getTiles(), condTile->getDomain());
	                    if (aTile == NULL)
	                    {
	                        LERROR << "Error: QtCaseOp::inducedEvaluate() - The condition and result mdds don't have the same tiling.";
	                        parseInfo.setErrorNo(427);
	                        throw parseInfo;
	                    }
	                    cachedTiles->push_back(new Tile(*aTile));
	                }
	            }
	            //iterate through the points of each tile
	            r_Miter condTileIter(&(condTile->getDomain()), &(condTile->getDomain()), condTile->getType()->getSize(), condTile->getContents());
	            std::vector<r_Miter *> *defaultIter = new std::vector<r_Miter *>();
	            //if on last conditions, use the same iteration for default results
	            std::vector<Tile *> *cachedDefaultTiles = new std::vector<Tile *>();
	            if (condPos == conditionList2->size() - 1)
	            {
	                if (defaultResult->getDataStreamType().getDataType() == QT_MDD)
	                {
	                    QtDataList *cachedData = getCachedData(defaultResult, cacheList);
	                    for (QtDataList::iterator i = cachedData->begin(); i != cachedData->end(); i++)
	                    {
	                        boost::shared_ptr<Tile> theTile = getCorrespondingTile((static_cast<QtMDD *>(*i))->getMDDObject()->getTiles(), condTile->getDomain());
	                        Tile *aTile = new Tile(*theTile);
	                        if (aTile == NULL)
	                        {
	                            LERROR << "Error: QtCaseOp::inducedEvaluate() - The condition and result mdds don't have the same tiling.";
	                            parseInfo.setErrorNo(427);
	                            throw parseInfo;
	                        }
	                        cachedDefaultTiles->push_back(aTile);
	                        defaultIter->push_back(new r_Miter(&(condTile->getDomain()), &(aTile->getDomain()), aTile->getType()->getSize(), aTile->getContents()));
	                    }
	                }
	            }
	            std::vector<r_Miter *> *cacheIterators = new std::vector<r_Miter *>();
	            //if there are cached tiles, iterate through them at the same time
	            if (cachedTiles->size())
	            {
	                for (std::vector<Tile *>::iterator i = cachedTiles->begin(); i != cachedTiles->end(); i++)
	                {
	                    cacheIterators->push_back(new r_Miter(&(condTile->getDomain()), &((*i)->getDomain()), (*i)->getType()->getSize(), (*i)->getContents()));
	                    if (!(*i)->getDomain().covers(condTile->getDomain()))
	                    {
	                        LERROR << "Error: QtCaseOp::inducedEvaluate() - The condition and result mdds don't have the same definition domain.";
	                        delete cacheIterators;
	                        parseInfo.setErrorNo(426);
	                        throw parseInfo;
	                    }
	                }
	            }
	
	            unsigned int cellCount = 0;
	            while (!condTileIter.isDone())
	            {
	                std::vector<char *> cachedPoints;
	                std::vector<char *> cachedDefaultPoint;
	                char *condPoint = condTileIter.nextCell();
	                if (cachedTiles->size())
	                {
	                    for (std::vector<r_Miter *>::iterator i = cacheIterators->begin(); i != cacheIterators->end(); i++)
	                    {
	                        cachedPoints.push_back((*i)->nextCell());
	                    }
	                }
	                if (defaultIter->size())
	                {
	                    for (std::vector<r_Miter *>::iterator i = defaultIter->begin(); i != defaultIter->end(); i++)
	                    {
	                        cachedDefaultPoint.push_back((*i)->nextCell());
	                    }
	                }
	                if (static_cast<long>(*(condPoint)) == 1 && !changedCells.at(cellCount))
	                {
	                    changedCells.at(cellCount) = true;
	                    QtData *localResult = NULL;
	                    //for MDDs, evaluate point by point
	                    if ((*resultIter)->getDataStreamType().getDataType() == QT_MDD)
	                    {
	                        localResult = evaluateCellByCell(inputList, *resultIter, cachedTiles, &cachedPoints);
	                        (*tileIter)->setCell(cellCount, (dynamic_cast<QtScalarData *>(localResult))->getValueBuffer());
	                        delete localResult;
	                    }
	                    //for base types
	                    else
	                    {
	                        localResult = getCachedScalar((*resultIter), scalarCacheList);
	                        (*tileIter)->setCell(cellCount, (dynamic_cast<QtScalarData *>(localResult))->getValueBuffer());
	                    }
	
	                }
	                //on the last condition iteration, also plug in the default
	                else if ((condPos == conditionList2->size() - 1) && !changedCells.at(cellCount))
	                {
	                    //put default result
	                    QtData *localResult = NULL;
	                    if (defaultResult->getDataStreamType().getDataType() == QT_MDD)
	                    {
	                        localResult = evaluateCellByCell(inputList, defaultResult, cachedDefaultTiles, &cachedDefaultPoint);
	                        (*tileIter)->setCell(cellCount, (dynamic_cast<QtScalarData *>(localResult))->getValueBuffer());
	                        delete localResult;
	                    }
	                    //for base types
	                    else
	                    {
	                        localResult = getCachedScalar(defaultResult, scalarCacheList);
	                        (*tileIter)->setCell(cellCount, (dynamic_cast<QtScalarData *>(localResult))->getValueBuffer());
	                    }
	
	                }
	                cellCount++;
	            }
	
	            condPos++;
	            //if done cleanup
	            if (condPos == conditionList2->size())
	            {
	                std::vector<Tile *>::iterator i;
	                for (i = cachedTiles->begin(); i != cachedTiles->end(); i++)
	                {
	                    if ((*i))
	                    {
	                        delete (*i);
	                    }
	                }
	                delete cachedTiles;
	                for (i = cachedDefaultTiles->begin(); i != cachedDefaultTiles->end(); i++)
	                {
	                    if ((*i))
	                    {
	                        delete (*i);
	                    }
	                }
	                delete cachedDefaultTiles;
	                std::vector<r_Miter *>::iterator j;
	                for (j = cacheIterators->begin(); j != cacheIterators->end(); j++)
	                {
	                    if ((*j))
	                    {
	                        delete (*j);
	                    }
	                }
	                delete cacheIterators;
	                for (j = defaultIter->begin(); j != defaultIter->end(); j++)
	                {
	                    if ((*j))
	                    {
	                        delete (*j);
	                    }
	                }
	                delete defaultIter;
	            }
	        }
	        tilePos++;
	    }
	    //cleanup
	    for (condIter = conditionList2->begin(); condIter != conditionList2->end(); condIter++)
	    {
	        if (*condIter)
	        {
	            (*condIter)->deleteRef();
	        }
	    }
	
	    delete conditionList2;
	    delete resultList;
	
	    //clear the cache
	    //mdd
	    std::vector<std::pair <QtOperation *, QtDataList *>>::iterator cacheIter;
	    QtDataList::iterator dataCacheIter;
	    for (cacheIter = cacheList->begin(); cacheIter != cacheList->end(); cacheIter++)
	    {
	        for (dataCacheIter = (*cacheIter).second->begin(); dataCacheIter != (*cacheIter).second->end(); dataCacheIter++)
	        {
	            if (*dataCacheIter)
	            {
	                int x = (*dataCacheIter)->deleteRef();
	            }
	        }
	    }
	    delete cacheList;
	    //scalar
	    std::vector<std::pair <QtOperation *, QtData *>>::iterator scalarCacheIter;
	    for (scalarCacheIter = scalarCacheList->begin(); scalarCacheIter != scalarCacheList->end();
	            scalarCacheIter++)
	    {
	        if ((*scalarCacheIter).second)
	        {
	            (*scalarCacheIter).second->deleteRef();
	        }
	    }
	    delete scalarCacheList;
	
	    //add the tiles to the mddObj
	    for (tileIter = tiles->begin(); tileIter != tiles->end(); tileIter++)
	    {
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
QtData *
QtCaseOp::evaluate(QtDataList *inputList)
{
    startTimer("QtCaseOp");
    if (this->inducedCase)
    {
        QtData* res;
        try{
            res = this->evaluateInducedOp(inputList);
        }
        catch(...){
            res = this->safeEvaluateInducedOp(inputList);
        }
        return res;
        
    }
    QtData *returnValue = NULL;
    bool foundClause = false;
    unsigned int pos = 0, foundAt;
    QtOperationList::iterator iter;
    QtData *dataI;
    //everything is good for evaluation
    bool success = (operationList != 0);
    for (iter = operationList->begin(); iter != operationList->end() && !foundClause; iter++)
    {
        if ((*iter) == NULL)
        {
            success = false;
            break;
        }
        //check if WHEN clause
        if (pos % 2 == 0 && pos < operationList->size() - 1)
        {
            dataI = (*iter)->evaluate(inputList);
            if (static_cast<bool>((static_cast<QtAtomicData *>(dataI))->getUnsignedValue()))
            {
                iter++;
                dataI = (*iter)->evaluate(inputList);
                returnValue = dataI;
                foundAt = pos;
                foundClause = true;
            }
        }//we arrived on the ELSE clause
        else if (pos == operationList->size() - 1)
        {
            dataI = (*iter)->evaluate(inputList);
            returnValue = dataI;
            foundAt = pos;
        }
        pos++;
    }

    //check if everything evaluated correctly
    if (!success)
    {
        LERROR << "Error: QtCaseOp::evaluate() - at least one operand branch is invalid.";
    }

    stopTimer();

    return returnValue;
}

/**
 * @override QtNarryOperation::printTree
 */
void
QtCaseOp::printTree(int tab, ostream &s, QtChildType mode)
{

    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCaseOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;
    QtNaryOperation::printTree(tab, s, mode);
}

/**
 * @override QtNarryOperation::printAlgebraicExpression
 */
void
QtCaseOp::printAlgebraicExpression(ostream &s)
{

    s << "[";

    QtNaryOperation::printAlgebraicExpression(s);

    s << "]";
}

/**
 * Type checking for the CASE operation.
 * The following conditions have to be met by the operands:
 * - all the conditions must be of type boolean;
 * - type coercion is done between results, the types must be compatible.
 *
 * @param typeTuple - the tuple containing the types of the operands
 */
const QtTypeElement &
QtCaseOp::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);
    inducedCase = ((*(operationList->begin()))->checkType(typeTuple).getDataType() == QT_MDD);
    const BaseType *resultType = NULL;

    //check if types are valid
    unsigned int pos = 0;
    for (auto iter = operationList->begin(); iter != operationList->end(); iter++, pos++)
    {
        const QtTypeElement &opType = (*iter)->checkType(typeTuple);
        if (pos % 2 == 0 && pos < (operationList->size() - 1))
        {
            if ((!inducedCase && opType.getDataType() != QT_BOOL) ||
                    (inducedCase && (opType.getDataType() != QT_MDD ||
                                     static_cast<const MDDBaseType *>(opType.getType())->getBaseType()->getType() != BOOLTYPE)))
            {
                LERROR << "A WHEN condition (operand " << (pos + 1)
                       << ") in CASE expression is not a boolean value.";
                parseInfo.setErrorNo(this->inducedCase ? 428 : 431);
                throw parseInfo;
            }
        }
        else
        {
            const BaseType *opBaseType = NULL;
            if (opType.isBaseType())
            {
                opBaseType = static_cast<const BaseType *>(opType.getType());
            }
            else if (opType.getDataType() == QT_MDD)
            {
                opBaseType = (static_cast<const MDDBaseType *>(opType.getType()))->getBaseType();
            }
            else
            {
                LERROR << "A THEN or ELSE result (operand " << (pos + 1)
                       << ") in CASE expression is not a scalar or mdd.";
                parseInfo.setErrorNo(429);
                throw parseInfo;
            }

            if (resultType)
            {
                if (!resultType->compatibleWith(opBaseType))
                {
                    LERROR << "THEN / ELSE results in CASE statement have different base types (at operand " << (pos + 1) << "). "
                           << "Please add casts as necessary to make sure all result expressions are of the same base type.";
                    parseInfo.setErrorNo(430);
                    throw parseInfo;
                }
                resultType = getResultType(resultType, opBaseType);
            }
            else
            {
                resultType = opBaseType;
            }
        }
    }

    if (inducedCase)
    {
        this->baseType = const_cast<BaseType *>(resultType);
        MDDBaseType *resultMDDType = new MDDBaseType("tmp", this->baseType);
        TypeFactory::addTempType(resultMDDType);
        dataStreamType.setType(resultMDDType);
    }
    else
    {
        dataStreamType.setType(const_cast<BaseType *>(resultType));
    }
    return dataStreamType;
}

/**
 * Type coercion for operands.
 *
 * @param op1 - the base type of the first operand
 * @param op2 - the base type of the second operand
 * @return The resulting type if types are compatible, NULL otherwise
 */
const BaseType *QtCaseOp::getResultType(const BaseType *op1, const BaseType *op2)
{
    //shortcut in case the types are actually the same
    if (op1->getType() == op2->getType())
    {
        return op1;
    }

    //in case both are structs
    // for proper type coercion, do we not need to ensure the best type from each pair of bands is selected?
    // -BB
    if (op1->getType() == STRUCT)
    {
        if (((StructType *)const_cast<BaseType *>(op1))->compatibleWith(op2))
        {
            return op1;
        }
        else
        {
            return NULL;
        }
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

/**
 * Checks if the given type is signed.
 *
 * @param type - the type to be checked
 * @return 1 if true, 0 otherwise
 */
int QtCaseOp::isSignedType(const BaseType *type)
{
    return (type->getType() >= LONG && type->getType() <= COMPLEXTYPE2);
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
QtData *QtCaseOp::evaluateCellByCell(QtDataList *inputList, QtOperation *currentOperation,
                                     std::vector<Tile *> *currentTiles, std::vector<char *> *cachedPoints)
{
    // return value
    QtData *localResult = NULL;
    // if the current node is an mdd var itself then it has no op applied to, so
    // return the cell
    if (currentOperation->getNodeType() == QT_MDD_VAR)
    {
        // storage container for the data of the cell
        QtScalarData *point = new QtScalarData();
        point->setValueBuffer(cachedPoints->at(0));
        point->setValueType(currentTiles->at(0)->getType());
        //do not delete data as cell data belongs to the cached point
        //and QtScalarData only stores a reference to the data
        point->disownCells();
        // assigns return value
        localResult = dynamic_cast<QtData *>(point);
    }
    else
    {
        // get the mdd vars
        QtNodeList *mddVars = currentOperation->getChild(QT_MDD_VAR, QT_ALL_NODES);
        QtNodeList originalTree;
        QtNodeList replacedTree;
        QtNodeList::iterator mddVar = mddVars->begin();
        auto tileIter = currentTiles->begin();
        auto pointIter = cachedPoints->begin();
        for (; mddVar != mddVars->end() && tileIter != currentTiles->end() && pointIter != cachedPoints->end();
                mddVar++, tileIter++, pointIter++)
        {
            // storage container for the data of the cell
            QtScalarData *point = new QtScalarData();
            point->setValueBuffer(*pointIter);
            point->setValueType((*tileIter)->getType());
            //do not delete data as cell data belongs to the cached point
            //and QtScalarData only stores a reference to the data
            point->disownCells();

            QtConst *newInput = new QtConst(point);
            // replace the mdd in the operation tree with the point
            (*mddVar)->getParent()->setInput(static_cast<QtOperation *>(*mddVar), newInput);
            originalTree.push_back((*mddVar));
            replacedTree.push_back(newInput);
        }
        delete mddVars;
        // evaluate the newly formed operation
        // before, the operation was returning an array, set the dataStreamType to a baseType
        // this has to be done for every operation in the new tree
        std::unique_ptr<QtNodeList> currentTree(currentOperation->getChilds(QT_ALL_NODES));
        currentTree->push_front(currentOperation);

        QtNodeList streamsChanged;
        std::vector<QtTypeElement> oldStreams;
        for (auto i = currentTree->begin(); i != currentTree->end(); i++)
        {
            QtTypeElement currentDataStreamType = (static_cast<QtOperation *>(*i))->getDataStreamType();
            QtTypeElement oldDataStreamType;
            if (currentDataStreamType.getDataType() != QT_TYPE_UNKNOWN && !currentDataStreamType.isBaseType())
            {
                oldDataStreamType = currentDataStreamType;
                const BaseType *newOpType = (static_cast<MDDBaseType *>(const_cast<Type *>(currentDataStreamType.getType())))->getBaseType();
                currentDataStreamType.setType(newOpType);
                (static_cast<QtOperation *>(*i))->setDataStreamType(currentDataStreamType);
                streamsChanged.push_back((*i));
                oldStreams.push_back(oldDataStreamType);
            }
        }
        //assigns return value
        localResult = currentOperation->evaluate(inputList);
        // restore the query tree
        QtNodeList::iterator orig, replaced;
        for (orig = originalTree.begin(), replaced = replacedTree.begin();
                orig != originalTree.end() && replaced != replacedTree.end(); orig++, replaced++)
        {
            (*replaced)->getParent()->setInput(static_cast<QtOperation *>(*replaced), static_cast<QtOperation *>(*orig));
        }
        // restore the dataStreamTypes
        auto streamChangedIter = streamsChanged.begin();
        auto oldStreamIter = oldStreams.begin();
        for (; streamChangedIter != streamsChanged.end() && oldStreamIter != oldStreams.end();
                streamChangedIter++, oldStreamIter++)
        {
            (static_cast<QtOperation *>(*streamChangedIter))->setDataStreamType((*oldStreamIter));
        }
        for (replaced = replacedTree.begin(); replaced != replacedTree.end(); ++replaced)
        {
            delete *replaced;
            *replaced = NULL;
        }
    }
    return localResult;
}

/**
 * Returns the cached data list corresponding to the operation
 * @param op - the operation with cached data
 * @param cacheList - the list of cached values
 * @return a list of evaluated, cached data, corresponding to the op
 */
QtNode::QtDataList *QtCaseOp::getCachedData(QtOperation *op,
        std::vector<std::pair <QtOperation *, QtDataList *>> *cacheList)
{
    QtDataList *returnValue = NULL;
    for (std::vector<std::pair <QtOperation *, QtDataList *>>::iterator i = cacheList->begin();
            i != cacheList->end(); i++)
    {
        if (op == (*i).first)
        {
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
void QtCaseOp::restoreTree()
{
    std::vector<std::pair <QtNode *, std::pair <QtNode *, QtNode *>>>::iterator i;
    for (i = this->removedNodes.begin(); i != this->removedNodes.end(); i++)
    {
        (static_cast<QtOperation *>((*i).first))->setInput(static_cast<QtOperation *>((*i).second.second), static_cast<QtOperation *>((*i).second.first));
        (static_cast<QtOperation *>((*i).second.second))->setParent((*i).second.first);
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
boost::shared_ptr<Tile> QtCaseOp::getCorrespondingTile(std::vector<boost::shared_ptr<Tile>> *tiles, const r_Minterval &domain)
{
    boost::shared_ptr<Tile> returnValue;
    for (std::vector<boost::shared_ptr<Tile>>::iterator i = tiles->begin(); i != tiles->end(); i++)
    {
        if ((*i)->getDomain().covers(domain))
        {
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
QtData *QtCaseOp::getCachedScalar(QtOperation *op, std::vector<std::pair<QtOperation *, QtData *>> *scalarCacheList)
{
    QtData *returnValue = NULL;
    std::vector<std::pair<QtOperation *, QtData *>>::iterator i;
    for (i = scalarCacheList->begin(); i != scalarCacheList->end(); i++)
    {
        if (op == (*i).first)
        {
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
void QtCaseOp::addMddsToCache(QtDataList *inputList, QtOperation *&op, std::vector<std::pair<QtOperation *,
                              QtDataList *>> *cacheList)
{
    //get all the MDD variables
    std::unique_ptr<QtNodeList> mddVars(op->getChild(QT_MDD_VAR, QT_ALL_NODES));
    //if the node itself is a mdd variable add it to the list
    if (op->getNodeType() == QT_MDD_VAR)
    {
        mddVars->push_back(op);
    }
    QtDataList *correspondingMdds = new QtDataList();
    for (QtNodeList::iterator i = mddVars->begin(); i != mddVars->end(); i++)
    {
        //if an mdd has a domain operation applied, consider them together and
        //remove the domain op from the tree
        QtOperation *trimmedArray = static_cast<QtOperation *>(*i);
        while (trimmedArray->getParent()->getNodeType() == QT_DOMAIN_OPERATION)
        {
            this->removedNodes.push_back(std::make_pair(trimmedArray->getParent()->getParent(),
                                         std::make_pair(trimmedArray->getParent(), trimmedArray)));
            trimmedArray = static_cast<QtOperation *>((trimmedArray)->getParent());
        }
        QtData *evaluatedTrimmedArray = trimmedArray->evaluate(inputList);
        correspondingMdds->push_back(evaluatedTrimmedArray);
        //remove any eventual domain operation encountered from the tree
        trimmedArray->getParent()->setInput(trimmedArray, static_cast<QtOperation *>(*i));
    }
    cacheList->emplace_back(std::make_pair(op, correspondingMdds));
}
#include "qlparser/qtcaseop.icc"


