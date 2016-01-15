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

static const char rcsid[] = "@(#)qlparser, QtCommand: $Header: /home/rasdev/CVS-repository/rasdaman/qlparser/qtcommand.cc,v 1.21 2005/09/03 20:17:55 rasdev Exp $";

#include "config.h"
#include "qlparser/qtcommand.hh"
#include "mddmgr/mddcoll.hh"
#include "catalogmgr/typefactory.hh"
#include "reladminif/databaseif.hh"
#include "relcatalogif/settype.hh"
#include "relcatalogif/mdddomaintype.hh"
#include "servercomm/servercomm.hh"
#include "qlparser/querytree.hh"
#include "qlparser/qtinsert.hh"
#include "relblobif/tilecache.hh"
#include <easylogging++.h>

#include <iostream>

using namespace std;

extern ServerComm::ClientTblElt* currentClientTblElt;

const QtNode::QtNodeType QtCommand::nodeType = QtNode::QT_COMMAND;

const string QtCommand::tmpMddTypePrefix = string("autoMdd-");
const string QtCommand::tmpSetTypePrefix = string("autoSet-");


QtCommand::QtCommand( QtCommandType initCommand, const std::string& initCollection, const std::string& initType )
    : QtExecute(),
      command( initCommand ),
      collectionName( initCollection ),
      typeName( initType ),
      childNode( NULL )
{
}



QtCommand::QtCommand( QtCommandType initCommand, const std::string& initCollection )
    : QtExecute(),
      command( initCommand ),
      collectionName( initCollection ),
      childNode( NULL )
{
}



QtCommand::QtCommand( QtCommandType initCommand, const std::string& initCollection, QtOperationIterator* collection)
    : QtExecute(),
      command( initCommand ),
      collectionName( initCollection ),
      childNode( collection )
{
}

void QtCommand::dropCollection(string collectionName2)
{
    if (currentClientTblElt)
    {
        // drop the actual collection
        if (!MDDColl::dropMDDCollection(collectionName2.c_str()))
        {
            LFATAL << "Error during query evaluation: collection name not found: " << collectionName.c_str();
            parseInfo.setErrorNo(957);
            throw parseInfo;
        }
        
        // if this collection was created using a SELECT INTO statement, then delete the temporary datatypes as well
        string setName = tmpSetTypePrefix + collectionName2;
        string mddName = tmpMddTypePrefix + collectionName2;
        TypeFactory::deleteTmpSetType(setName.c_str());
        TypeFactory::deleteTmpMDDType(mddName.c_str());
    }
}

OId QtCommand::createCollection(string collectionName2, string typeName2)
{
    // allocate a new oid within the current db
    OId oid = 0;

    if (currentClientTblElt)
    {
        // get collection type
        CollectionType* collType = static_cast<CollectionType*>(const_cast<SetType*>(TypeFactory::mapSetType(typeName2.c_str())));

        if (collType)
        {
#ifdef BASEDB_O2
            if (!OId::allocateMDDCollOId(&oid))
            {
#else
            OId::allocateOId(oid, OId::MDDCOLLOID);
#endif
                try
                {
                    MDDColl* coll = MDDColl::createMDDCollection(collectionName2.c_str(), oid, collType);
                    delete coll;
                    coll = NULL;
                }
                catch (r_Error& obj)
                {
                    parseInfo.setErrorNo(955);
                    throw parseInfo;
                }
#ifdef BASEDB_O2
            }
            else
            {
                LFATAL << "Error: QtCommand::evaluate() - oid allocation failed";
                parseInfo.setErrorNo(958);
                throw parseInfo;
            }
#endif
        }
        else
        {
            LFATAL << "Error during query evaluation: collection type not found: " << typeName2.c_str();
            parseInfo.setErrorNo(956);
            throw parseInfo;
        }
    }
    return oid;
}

string QtCommand::getSelectedDataType(vector<QtData*>* data)
{
    char* typestr       = NULL;
    QtData *firstResult = NULL;
    vector<QtData*>::iterator dataIter = data->begin();
    
    if (data->size() > 0)
    {
        // take first element from the list of results
        firstResult = *dataIter;
        typestr = firstResult->getTypeStructure();
    }
    else
    {
        // empty results from SELECT
        LFATAL << "Error: no results from the SELECT sub-query.";
        throw r_Error(r_Error::r_Error_QueryExecutionFailed, 243);
    }

    LTRACE << "getSelectedDataType() - type structure of the SELECT sub-query results: " << typestr;

    MDDType *mddType = NULL;
    SetType *setType = NULL;

    string setTypeName = tmpSetTypePrefix + collectionName;
    string mddTypeName = tmpMddTypePrefix + collectionName;

    if (firstResult->getDataType() == QT_MDD)
    {
        QtMDD* mddObj = static_cast<QtMDD*>(firstResult);
        const BaseType* baseType = mddObj->getCellType();
        const r_Minterval domain = mddObj->getLoadDomain();

        // create new domain; use *:* for all dimensions, to be general
        r_Dimension dimensions = domain.dimension();
        r_Minterval newDomain(dimensions);
        r_Sinterval interval;
        for (r_Dimension i = 0; i < dimensions; i++)
            newDomain << interval;

        mddType = new MDDDomainType(mddTypeName.c_str(), baseType, newDomain);
        LTRACE << "getSelectedDataType() - new mdd type: " << mddType->getTypeStructure();
        setType = new SetType(setTypeName.c_str(), mddType);
    }
    else
    {
        LFATAL << "Error: the result from the SELECT sub-query is not an MDD, and can not be stored in a collection.";
        throw r_Error(r_Error::r_Error_QueryExecutionFailed, 243);
    }

    // this also creates the underlying mddType
    TypeFactory::addSetType(setType);

    return string(setTypeName);
}

void QtCommand::insertIntoCollection(vector<QtData*>* data, string collectionName2)
{
    vector<QtData*>::iterator it;
    for (it = data->begin(); it != data->end(); it++)
    {
        QtData *elemToInsert = *it;
        QtInsert *insertNode = new QtInsert(collectionName2, elemToInsert);
        
        QueryTree *query = new QueryTree(insertNode);
        try
        {
            LINFO << "inserting into new collection...";
            LINFO << "checking semantics...";
            query->checkSemantics();
            LINFO << "evaluating update...";
            query->evaluateUpdate();
            LINFO << "done.";
            delete query;
        }
        catch (r_Error& myErr)
        {
            delete query;
            LFATAL << "Error: bad exception while evaluating insert sub-query: " << myErr.what();
            throw;
        }
        catch (...)
        {
            delete query;
            LFATAL << "Error: unknown exception while evaluating insert sub-query, re-throwing.";
            throw;
        }
    }
}

bool QtCommand::collectionExists(string collectionName2)
{
    try
    {
        MDDColl *coll = MDDColl::getMDDCollection(collectionName2.c_str());
        if (coll)
        {
            delete coll;
            coll = NULL;
            return true;
        }
        else
        {
            return false;
        }
    }
    catch (r_Error& e)
    {
        return false; // collection not found
    }
}

QtData*
QtCommand::evaluate()
{
    startTimer("QtCommand");

    switch (command)
    {
    case QT_DROP_COLLECTION:
        dropCollection(collectionName);
        break;
    case QT_CREATE_COLLECTION:
        createCollection(collectionName, typeName);
        break;
    case QT_COMMIT:
        TileCache::clear();
        break;
    case QT_CREATE_COLLECTION_FROM_QUERY_RESULT:

        try
        {
            /* 
             * 1/4: Evaluate SELECT sub-query: construct a new query tree and execute it to get the results.
             */
            QueryTree *selectTree = new QueryTree(childNode);

            vector<QtData*>* data = NULL;
            try
            {
                LINFO << "evaluating select sub-query...";
                LINFO << "checking semantics...";
                selectTree->checkSemantics();
                LINFO << "evaluating retrieval...";
                data = selectTree->evaluateRetrieval();
                LINFO << "done.";
                delete selectTree;
            }
            catch (r_Error& myErr)
            {
                delete selectTree;
                LFATAL << "Error: bad exception while evaluating insert sub-query: " << myErr.what();
                throw;
            }
            catch (...)
            {
                delete selectTree;
                LFATAL << "Error: unknown exception while evaluating insert sub-query, re-throwing.";
                throw;
            }
            

            if (data == NULL)
            {
                LFATAL << "Error: evaluating the SELECT sub-query failed.";
                throw r_Error(r_Error::r_Error_QueryExecutionFailed, 242);
            }
            else
            {
                LTRACE << "evaluate() - selected result size: " << data->size();
            }

            if (collectionExists(collectionName))
            {
                LWARNING << "Warning: inserting into an existing collection " << collectionName;
            }
            else
            {
                /* 
                 * 2/4: Create a new datatypes for the collection, by looking at the first result
                 */
                string collectionType = getSelectedDataType(data);

                /* 
                 * 3/4: Create a new collection.
                 */
                createCollection(collectionName, collectionType);
                LTRACE << "evaluate() - created collection " << collectionName << " with type " << collectionType;
            }

            /* 
             * 4/4: Insert the data into the new collection
             */
            insertIntoCollection(data, collectionName);
            LTRACE << "evaluate() - data successfully inserted into collection " << collectionName;
        }
        catch (r_Ebase_dbms& myErr)
        {
            LFATAL << "Error: base DBMS exception: " << myErr.what();
            throw;
        }
        catch (r_Error& myErr)
        {
            LFATAL << "Error: " << myErr.get_errorno() << " " << myErr.what();
            throw;
        }
        catch (bad_alloc)
        {
            LFATAL << "Error: cannot allocate memory.";
            throw;
        }
        catch (ParseInfo &e)
        {
            LFATAL << "Error: ";
            e.printStatus(RMInit::logOut);
            throw;
        }
        catch (...)
        {
            LFATAL << "Error: caught unknown exception while evaluation SELECT INTO query, re-throwing.";
            throw;
        }
    default: break;
    }

    stopTimer();

    return 0;
}



void
QtCommand::printTree( int tab, std::ostream& s, __attribute__ ((unused)) QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCommand Object" << std::endl;

    switch( command )
    {
    case QT_DROP_COLLECTION:
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  drop collection("   << collectionName.c_str() << ")";
        break;
    case QT_CREATE_COLLECTION:
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  create collection(" << collectionName.c_str() << ", " << typeName.c_str() <<")";
        break;
    case QT_CREATE_COLLECTION_FROM_QUERY_RESULT:
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  select into(" << collectionName.c_str() << ", " << typeName.c_str() <<")";
        break;
    default:
        s << "<command unknown>";
    }
    
    s << getEvaluationTime();

    s << std::endl;
}



void
QtCommand::printAlgebraicExpression( std::ostream& s )
{
    s << "command<";

    switch( command )
    {
    case QT_DROP_COLLECTION:
        s << "drop collection("   << collectionName.c_str() << ")";
        break;
    case QT_CREATE_COLLECTION:
        s << "create collection(" << collectionName.c_str() << ", " << typeName.c_str() <<")";
        break;
    case QT_CREATE_COLLECTION_FROM_QUERY_RESULT:
        s << "select ";
        childNode->printAlgebraicExpression(s);
        s << " into " << collectionName.c_str() << ", " << typeName.c_str() <<")";
        break;
    default:
        s << "unknown";
    }

    s << ">";
}



void
QtCommand::checkType()
{
    // nothing to do here
}

