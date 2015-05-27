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
 * SOURCE: querytree.cc
 *
 * MODULE: qlparser
 * CLASS:  QueryTree
 *
 * PURPOSE:
 *
 * COMMENTS:
 *
 ************************************************************/

static const char rcsid[] = "@(#)qlparser, QueryTree: $Id: querytree.cc,v 1.52 2005/06/28 08:42:13 rasdev Exp $";


#include "config.h"
#include "version.h"
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif
#include <iostream>
#include <dlfcn.h>

#include "raslib/rmdebug.hh"
#include "globals.hh"

#include "qlparser/querytree.hh"
#include "qlparser/qtnode.hh"
#include "qlparser/qtoperationiterator.hh"
#include "qlparser/qtselectioniterator.hh"
#include "qlparser/qtjoiniterator.hh"
#include "qlparser/qtvariable.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtdomainoperation.hh"
#include "qlparser/qtexecute.hh"

#include "raslib/type.hh"
#include "catalogmgr/typefactory.hh"
#include "relcatalogif/mdddomaintype.hh"
#include "relcatalogif/settype.hh"

unsigned int QueryTree::nextCSENo = 0;

SymbolTable<int> QueryTree::symtab;

QueryTree::QueryTree()
    : rootNode(NULL), infoType(QT_INFO_UNKNOWN)
{
}

QueryTree::QueryTree(QtNode* root)
    : rootNode(root), infoType(QT_INFO_UNKNOWN)
{
}


QueryTree::~QueryTree()
{
    if( rootNode )
    {
        delete rootNode;
        rootNode=NULL;
    }
    releaseDynamicObjects();
}


void
QueryTree::checkSemantics()
{
    RMDBCLASS( "QueryTree", "checkSemantics()", "qlparser", __FILE__, __LINE__ )
    if (!rootNode)
        return;

    switch( rootNode->getNodeType() )
    {
    case QtNode::QT_MDD_ACCESS:
    case QtNode::QT_OPERATION_ITERATOR:
    case QtNode::QT_JOIN_ITERATOR:
    case QtNode::QT_SELECTION_ITERATOR:
    case QtNode::QT_EMPTY_STREAM:
    {
        const QtTypeTuple& resultType = ((QtONCStream*)rootNode)->checkType();
    }
    break;

    case QtNode::QT_UPDATE:
    case QtNode::QT_INSERT:
    case QtNode::QT_DELETE:
    case QtNode::QT_COMMAND:
    case QtNode::QT_PYRAMID:
    case QtNode::QT_CREATE_CELL_TYPE:
    case QtNode::QT_CREATE_MDD_TYPE:
    case QtNode::QT_CREATE_SET_TYPE:
    case QtNode::QT_DROP_TYPE:
        ((QtExecute*)rootNode)->checkType();
        break;

    default:
    {
        const QtTypeElement& resultType = ((QtOperation*)rootNode)->checkType();
    }
    break;
    }
}

vector<QtData*>*
QueryTree::evaluateRetrieval() throw (r_Error, ParseInfo)
{
    vector<QtData*>* returnValue=NULL;

    if( rootNode )
    {
        if( rootNode->getNodeType() != QtNode::QT_MDD_ACCESS &&
                rootNode->getNodeType() != QtNode::QT_OPERATION_ITERATOR &&
                rootNode->getNodeType() != QtNode::QT_JOIN_ITERATOR &&
                rootNode->getNodeType() != QtNode::QT_SELECTION_ITERATOR    )
        {
            RMInit::logOut << "QueryTree::evaluateRetrieval() - Retrieval query must start with an ONC node." << endl;
            ParseInfo errorInfo = rootNode->getParseInfo();
            errorInfo.setErrorNo(371);
            throw errorInfo;
        }

        QtNode::QtDataList*          dataList=NULL;
        QtNode::QtDataList::iterator dataIter;
        QtONCStream*                 oncRootNode = (QtONCStream*)rootNode;

        try
        {
            oncRootNode->open();
        }
        catch( ... )
        {
            oncRootNode->close();
            RMInit::logOut << "QueryTree::evaluateRetrieval() - rethrow exception from oncRootNode->open()." << endl;
            throw;
        }

        // create result collection
        vector<QtData*>* resultData = new vector<QtData*>();

        try
        {
            while( (dataList = oncRootNode->next()) )
            {
                if( dataList->size() > 1 || (*dataList)[0] == NULL )
                {
                    // Delete the tupel vector received by next(). Just tupel elements which are not
                    // further referenced are deleted.
                    for( dataIter=dataList->begin(); dataIter!=dataList->end(); dataIter++ )
                        if( *dataIter ) (*dataIter)->deleteRef();
                    delete dataList;
                    dataList=NULL;

                    if( resultData )
                    {
                        // Delete the result vector
                        for( dataIter=resultData->begin(); dataIter!=resultData->end(); dataIter++ )
                            if( *dataIter ) (*dataIter)->deleteRef();
                        delete resultData;
                        resultData = NULL;
                    }

                    RMInit::logOut << "QueryTree::evaluateTree() - multiple query targets are not supported." << endl;
                    ParseInfo errorInfo = oncRootNode->getParseInfo();
                    errorInfo.setErrorNo(361);
                    throw errorInfo;
                }

                QtData* resultElement = (*dataList)[0];

                // take the data element as result data and reset it in the tupel vector
                resultData->push_back( resultElement );
                (*dataList)[0] = NULL;

                RMDBGMIDDLE( 2, RMDebug::module_qlparser, "QueryTree", endl << endl << "NEXT RESULT ITEM OF THE QUERY INSERTED" << endl << endl )

                // Delete the tupel vector received by next(). Just tupel elements which are not
                // set to zero and which are not further referenced are deleted.
                for( dataIter=dataList->begin(); dataIter!=dataList->end(); dataIter++ )
                    if( *dataIter ) (*dataIter)->deleteRef();

                // delete the tuple vector
                delete dataList;
                dataList=NULL;
            }
        }
        catch(r_Error& myErr)
        {
            if( resultData )
            {
                // Delete the result vector
                for( dataIter=resultData->begin(); dataIter!=resultData->end(); dataIter++ )
                    if( *dataIter ) (*dataIter)->deleteRef();
                delete resultData;
                resultData = NULL;
            }

            oncRootNode->close();
            throw;
        }
        catch( ... )
        {
            if( resultData )
            {
                // Delete the result vector
                for( dataIter=resultData->begin(); dataIter!=resultData->end(); dataIter++ )
                    if( *dataIter ) (*dataIter)->deleteRef();
                delete resultData;
                resultData = NULL;
            }

            oncRootNode->close();
            throw;
        }

        oncRootNode->close();

        returnValue = resultData;

#ifdef RMANBENCHMARK
        RMInit::logOut << "Evaluated query tree:" << endl;
        rootNode->printTree(2, RMInit::logOut);
#endif
    } else if (infoType == QT_INFO_VERSION) {
        ostringstream version("");

#ifdef RMANVERSION
        version << "rasdaman " << RMANVERSION;
#else
        version << "Unknown version";
#endif

#ifdef GCCTARGET
        version << " on " << GCCTARGET;
#endif

#ifdef GCCVERSION
        version << ", compiled by " << GCCVERSION;
#endif

        // result domain: it is now format encoded so we just consider it as a char array
        r_Type* type = r_Type::get_any_type("char");
        const BaseType* baseType = TypeFactory::mapType(type->name());

        string infoString = version.str();
        int contentLength = infoString.length();
        char* contents = strdup(infoString.c_str());

        r_Minterval mddDomain = r_Minterval(1) << r_Sinterval((r_Range) 0, (r_Range) contentLength - 1);
        Tile *resultTile = new Tile(mddDomain, baseType, contents, contentLength, r_Array);

        // create a transient MDD object for the query result
        MDDBaseType* mddBaseType = new MDDBaseType("tmp", baseType);
        TypeFactory::addTempType(mddBaseType);
        MDDObj* resultMDD = new MDDObj(mddBaseType, resultTile->getDomain());
        resultMDD->insertTile(resultTile);

        // create a new QtMDD object as carrier object for the transient MDD object
        returnValue = new vector<QtData*>();
        returnValue->push_back(new QtMDD((MDDObj*) resultMDD));
    }
    
    return returnValue;
}



vector<QtData*>*
QueryTree::evaluateUpdate() throw (r_Error,ParseInfo)
{
    QtData* resultElement = NULL;
    // create result collection
    vector<QtData*>* resultData = new vector<QtData*>();

    if( rootNode )
    {
        if( rootNode->getNodeType() != QtNode::QT_UPDATE  &&
                rootNode->getNodeType() != QtNode::QT_INSERT  &&
                rootNode->getNodeType() != QtNode::QT_DELETE  &&
                rootNode->getNodeType() != QtNode::QT_COMMAND &&
                rootNode->getNodeType() != QtNode::QT_PYRAMID &&
                rootNode->getNodeType() != QtNode::QT_CREATE_CELL_TYPE &&
                rootNode->getNodeType() != QtNode::QT_CREATE_MDD_TYPE &&
                rootNode->getNodeType() != QtNode::QT_CREATE_SET_TYPE &&
                rootNode->getNodeType() != QtNode::QT_DROP_TYPE
          )
        {
            RMInit::logOut << "QueryTree::evaluateUpdate() - update query must start with an INSERT, UPDATE, DELETE, DROP or CREATE statement." << endl;
            ParseInfo errorInfo = rootNode->getParseInfo();
            errorInfo.setErrorNo(372);
            delete resultData;
            throw errorInfo;
        }

        QtExecute* executeNode = (QtExecute*) rootNode;

        try
        {
            // evaluate the update query
            resultElement = executeNode->evaluate();
        }
        catch (...)
        {
            delete resultData;
            throw;
        }

#ifdef RMANBENCHMARK
        RMInit::logOut << "Evaluated query tree:" << endl;
        rootNode->printTree(2, RMInit::logOut);
#endif
    }
    resultData->push_back( resultElement );
    return resultData;
}



void QueryTree::printTree( int tab, ostream& s )
{
    if ( rootNode )
    {
        s <<  SPACE_STR(tab).c_str() << "QueryTree:" << endl;
        rootNode->printTree( tab + 2, s );
    }
    else
        s << SPACE_STR(tab).c_str() << "QueryTree: Qt has no root node." << endl;
}

void QueryTree::addDynamicObject( QtNode *node )
{
    qtNodeList.push_back( node );
}

void QueryTree::removeDynamicObject( QtNode *node )
{
    qtNodeList.remove( node );
}

void QueryTree::addDynamicObject( QtData *node )
{
    qtDataList.push_back( node );
}

void QueryTree::removeDynamicObject( QtData *node )
{
    qtDataList.remove( node );
}

void QueryTree::addDynamicObject( ParseInfo *node )
{
    parseInfoList.push_back( node );
}

void QueryTree::removeDynamicObject( ParseInfo *node )
{
    parseInfoList.remove( node );
}

void QueryTree::addDynamicObject( vector<QtONCStream *> *node )
{
    vectorList.push_back( node );
}

void QueryTree::removeDynamicObject( vector<QtONCStream*> *node )
{
    vectorList.remove( node );
}

void QueryTree::releaseDynamicObjects()
{
    for( list<QtNode*>::iterator iter = qtNodeList.begin(); iter != qtNodeList.end(); iter++ )
    {
        if (*iter != NULL)
        {
            delete *iter;
            *iter=NULL;
        }
    }

    for( list<QtData*>::iterator iter = qtDataList.begin(); iter != qtDataList.end(); iter++ )
    {
        if (*iter != NULL)
        {
            delete *iter;
            *iter=NULL;
        }
    }

    for( list<ParseInfo*>::iterator iter = parseInfoList.begin(); iter != parseInfoList.end(); iter++ )
    {
        if (*iter != NULL)
        {
            delete *iter;
            *iter=NULL;
        }
    }

    for( list<vector<QtONCStream*>*>::iterator iter = vectorList.begin(); iter != vectorList.end(); iter++ )
    {
        if (*iter != NULL)
        {
            delete *iter;
            *iter=NULL;
        }
    }

    for( list<char *>::iterator iter = lexedCStringList.begin(); iter != lexedCStringList.end(); iter++ )
    {
        if (*iter != NULL)
        {
            free(*iter);
            *iter=NULL;
        }
    }
}

void QueryTree::addDomainObject( QtDomainOperation *dop )
{
    dopList.push_back( dop );
}

void QueryTree::removeDomainObject( QtDomainOperation *dop )
{
    dopList.remove( dop );
}

void QueryTree::printDomainObjects()
{
    list<QtDomainOperation*>::iterator iter;
    for( iter = dopList.begin(); iter != dopList.end(); iter++ )
    {
        cout << endl;
        (*iter)->printTree( 2 );
    }
}

void QueryTree::releaseDomainObjects()
{
    list<QtDomainOperation*>::iterator iter;
    for( iter = dopList.begin(); iter != dopList.end(); iter++ )
    {
        delete *iter;
        *iter=NULL;
    }
}

void QueryTree::rewriteDomainObjects(r_Minterval *greatDomain, string *greatIterator, QtMarrayOp2::mddIntervalListType *greatList)
{

    RMDBGENTER( 2, RMDebug::module_qlparser, "QueryTree", endl << "QueryTree: Iterator: <" << *greatIterator << "> Domain: " << *greatDomain << endl )
    list<QtDomainOperation*>::iterator iter;

    for( iter = dopList.begin(); iter != dopList.end(); iter++ )
    {

        // 1. get var name from iter
        QtVariable *qtVar = ((QtVariable *)((*iter)->getInput()));
        string stVar = qtVar->getIteratorName();
        const char *varname = stVar.c_str();

        // 2. get position of varname in varList
        bool bcond = false;
        QtMarrayOp2::mddIntervalListType *varList = greatList;
        QtMarrayOp2::mddIntervalListType::iterator varIter;
        r_Long varpos = 0;
        for (varIter = varList->begin(); varIter != varList->end(); varIter++)
        {

            if (!strcmp(varname, varIter->variable.c_str()))
            {
                bcond = true;
                break;
            };
            QtData *data = varIter->tree->evaluate(0);
            r_Dimension dimension = ((QtMintervalData*)data)->getMintervalData().dimension();
            varpos = varpos+dimension;
        };
        // postcond: bcond == false: varname not found in list. else varpos gives the position.

        if (bcond)
        {

            // 3. set domain expression to old one incremented by varpos
            QtNode::QtOperationList *lop = new QtNode::QtOperationList(1);
            (*lop)[0] =
                new QtPlus(
                (*iter)->getMintervalOp(),
                new QtConst(
                    new QtAtomicData(
                        varpos, sizeof(varpos)
                    )
                )
            );

            // set properly parent to new op -- DM 2013-jun-26
            QtOperation* mintervalOp = (*iter)->getMintervalOp();
            QtOperation* newMintervalOp = new QtPointOp( lop );
            if (mintervalOp)
            {
                newMintervalOp->setParent(*iter);
            }
            (*iter)->setMintervalOp( newMintervalOp );

            // 4. set varname to greatIterator
            QtVariable *var1 = new QtVariable( string(*greatIterator) );
            (*iter)->setInput(var1);

        }
        else
        {
            // TODO: insert some error notify code here!
            RMDBGMIDDLE( 1, RMDebug::module_qlparser, "QueryTree", " variable name not found in list " )
        }
    }
}

void QueryTree::addCString( char *str )
{
    lexedCStringList.push_back( str );
}


void QueryTree::setInfoType(QtInfoType newInfoType)
{
    infoType = newInfoType;
}
