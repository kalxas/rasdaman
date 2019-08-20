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

#include "qlparser/qtcommand.hh"
#include "mddmgr/mddcoll.hh"
#include "catalogmgr/typefactory.hh"
#include "reladminif/databaseif.hh"
#include "relcatalogif/settype.hh"
#include "relcatalogif/mdddomaintype.hh"
#include "servercomm/servercomm.hh"
#include "servercomm/cliententry.hh"
#include "qlparser/querytree.hh"
#include "qlparser/qtinsert.hh"
#include "relblobif/tilecache.hh"
#include <logging.hh>

#include <iostream>

using namespace std;

const QtNode::QtNodeType QtCommand::nodeType = QtNode::QT_COMMAND;
const size_t QtCommand::MAX_COLLECTION_NAME_LENGTH;

const string QtCommand::tmpMddTypePrefix = string("autoMdd-");
const string QtCommand::tmpSetTypePrefix = string("autoSet-");


QtCommand::QtCommand(QtCommandType initCommand, const QtCollection &initCollection, const std::string &initType)
    : QtExecute(),
      command(initCommand),
      collection(initCollection),
      typeName(initType),
      childNode(NULL)
{
    if (collection.getHostname() != "" && collection.getHostname() != "localhost")
    {
        LERROR << "Error: QtCommand::QtCommand(): Non-local collection is unsupported";
        parseInfo.setErrorNo(499);
        throw parseInfo;
    }
}



QtCommand::QtCommand(QtCommandType initCommand, const QtCollection &initCollection)
    : QtExecute(),
      command(initCommand),
      collection(initCollection),
      childNode(NULL)
{
    if (collection.getHostname() != "" && collection.getHostname() != "localhost")
    {
        LERROR << "Error: QtCommand::QtCommand(): Non-local collection is unsupported";
        parseInfo.setErrorNo(499);
        throw parseInfo;
    }
}



QtCommand::QtCommand(QtCommandType initCommand, const QtCollection &initCollection, QtOperationIterator *collectionitr)
    : QtExecute(),
      command(initCommand),
      collection(initCollection),
      childNode(collectionitr)
{
    if (collection.getHostname() != "" && collection.getHostname() != "localhost")
    {
        LERROR << "Error: QtCommand::QtCommand(): Non-local collection is unsupported";
        parseInfo.setErrorNo(499);
        throw parseInfo;
    }
}

void QtCommand::dropCollection(const QtCollection &collection2)
{
    // drop the actual collection
    if (!MDDColl::dropMDDCollection(collection2.getCollectionName().c_str()))
    {
        LERROR << "Error during query evaluation: collection name not found: " << collection.getCollectionName().c_str();
        parseInfo.setErrorNo(957);
        throw parseInfo;
    }

    // if this collection was created using a SELECT INTO statement, then delete the temporary datatypes as well
    string setName = tmpSetTypePrefix + collection2.getCollectionName();
    string mddName = tmpMddTypePrefix + collection2.getCollectionName();
    TypeFactory::deleteTmpSetType(setName.c_str());
    TypeFactory::deleteTmpMDDType(mddName.c_str());
}

OId QtCommand::createCollection(const QtCollection &collection2, string typeName2)
{
    // allocate a new oid within the current db
    OId oid = 0;

    if (collection2.getCollectionName().length() >= MAX_COLLECTION_NAME_LENGTH)
    {
        LERROR << "The collection name is longer than 200 characters.";
        parseInfo.setErrorNo(COLLECTION_NAME_LENGTH_EXCEEDED);
        throw parseInfo;
    }

    // get collection type
    CollectionType *collType = static_cast<CollectionType *>(const_cast<SetType *>(TypeFactory::mapSetType(typeName2.c_str())));

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
                MDDColl *coll = MDDColl::createMDDCollection(collection2.getCollectionName().c_str(), oid, collType);
                delete coll;
                coll = NULL;
            }
            catch (r_Error &obj)
            {
                parseInfo.setErrorNo(955);
                throw parseInfo;
            }
#ifdef BASEDB_O2
        }
        else
        {
            LERROR << "Error: QtCommand::evaluate() - oid allocation failed";
            parseInfo.setErrorNo(958);
            throw parseInfo;
        }
#endif
    }
    else
    {
        LERROR << "Error during query evaluation: collection type not found: " << typeName2.c_str();
        parseInfo.setErrorNo(956);
        throw parseInfo;
    }
    return oid;
}

void QtCommand::alterCollection(const QtCollection &collection2, string typeName2)
{
    // get new collection type
    unique_ptr<CollectionType> newCollType;
    newCollType.reset(static_cast<CollectionType *>(const_cast<SetType *>(
                          TypeFactory::mapSetType(typeName2.c_str()))));

    if (newCollType)
    {
        try
        {
            unique_ptr<MDDColl> coll;
            coll.reset(MDDColl::getMDDCollection(collection2.getCollectionName().c_str()));
            if (!coll)
            {
                LERROR << "Collection name not found: " << collection2.getCollectionName().c_str();
                parseInfo.setErrorNo(957);
                throw parseInfo;
            }

            const CollectionType *existingCollType = coll->getCollectionType();
            const MDDType *existingMDDType = existingCollType->getMDDType();
            if (!newCollType->compatibleWith(existingMDDType))
            {
                LERROR << "New collection type is incompatible with the existing collection type.";
                parseInfo.setErrorNo(959);
                throw parseInfo;
            }

            coll->setCollectionType(newCollType.get());
        }
        catch (r_Error &obj)
        {
            parseInfo.setErrorNo(955);
            throw parseInfo;
        }
    }
    else
    {
        LERROR << "Collection type not found: " << typeName2;
        parseInfo.setErrorNo(956);
        throw parseInfo;
    }
}

string QtCommand::getSelectedDataType(vector<QtData *> *data)
{
    char *typestr       = NULL;
    QtData *firstResult = NULL;
    vector<QtData *>::iterator dataIter = data->begin();

    if (data->size() > 0)
    {
        // take first element from the list of results
        firstResult = *dataIter;
        typestr = firstResult->getTypeStructure();
    }
    else
    {
        // empty results from SELECT
        LERROR << "Error: no results from the SELECT sub-query.";
        throw r_Error(r_Error::r_Error_QueryExecutionFailed, 243);
    }

    LTRACE << "getSelectedDataType() - type structure of the SELECT sub-query results: " << typestr;

    MDDType *mddType = NULL;
    SetType *setType = NULL;

    string setTypeName = tmpSetTypePrefix + collection.getCollectionName();
    string mddTypeName = tmpMddTypePrefix + collection.getCollectionName();

    if (firstResult->getDataType() == QT_MDD)
    {
        QtMDD *mddObj = static_cast<QtMDD *>(firstResult);
        const BaseType *baseType = mddObj->getCellType();
        const r_Minterval domain = mddObj->getLoadDomain();

        // create new domain; use *:* for all dimensions, to be general
        r_Dimension dimensions = domain.dimension();
        r_Minterval newDomain(dimensions);
        r_Sinterval interval;
        for (r_Dimension i = 0; i < dimensions; i++)
        {
            newDomain << interval;
        }

        mddType = new MDDDomainType(mddTypeName.c_str(), baseType, newDomain);
        LTRACE << "getSelectedDataType() - new mdd type: " << mddType->getTypeStructure();
        setType = new SetType(setTypeName.c_str(), mddType);
    }
    else
    {
        LERROR << "Error: the result from the SELECT sub-query is not an MDD, and can not be stored in a collection.";
        throw r_Error(r_Error::r_Error_QueryExecutionFailed, 243);
    }

    // this also creates the underlying mddType
    TypeFactory::addSetType(setType);

    return string(setTypeName);
}

void QtCommand::insertIntoCollection(vector<QtData *> *data, const QtCollection &collection2)
{
    vector<QtData *>::iterator it;
    for (it = data->begin(); it != data->end(); it++)
    {
        QtData *elemToInsert = *it;
        QtInsert *insertNode = new QtInsert(collection2.getCollectionName(), elemToInsert);

        QueryTree *query = new QueryTree(insertNode);
        vector<QtData *> *updateResult;
        try
        {
            LINFO << "inserting into new collection...";
            LINFO << "checking semantics...";
            query->checkSemantics();
            LINFO << "evaluating update...";
            updateResult = query->evaluateUpdate();
            LINFO << "done.";
            if (updateResult != NULL)
            {
                for (vector<QtData *>::iterator iter = updateResult->begin(); iter != updateResult->end(); iter++)
                {
                    delete *iter;
                    *iter = NULL;
                }
                delete updateResult;
            }
            updateResult = NULL;
            delete query;
            query = NULL;
        }
        catch (r_Error &myErr)
        {
            if (updateResult != NULL)
            {
                for (vector<QtData *>::iterator iter = updateResult->begin(); iter != updateResult->end(); iter++)
                {
                    delete *iter;
                    *iter = NULL;
                }
                delete updateResult;
            }
            updateResult = NULL;
            delete query;
            query = NULL;
            LERROR << "Error: bad exception while evaluating insert sub-query: " << myErr.what();
            throw;
        }
        catch (...)
        {
            if (updateResult != NULL)
            {
                for (vector<QtData *>::iterator iter = updateResult->begin(); iter != updateResult->end(); iter++)
                {
                    delete *iter;
                    *iter = NULL;
                }
                delete updateResult;
            }
            updateResult = NULL;
            delete query;
            query = NULL;
            LERROR << "Error: unknown exception while evaluating insert sub-query, re-throwing.";
            throw;
        }
    }
}

bool QtCommand::collectionExists(const QtCollection &collection2)
{
    try
    {
        MDDColl *coll = MDDColl::getMDDCollection(collection2.getCollectionName().c_str());
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
    catch (r_Error &e)
    {
        return false; // collection not found
    }
}

QtData *
QtCommand::evaluate()
{
    startTimer("QtCommand");

    switch (command)
    {
    case QT_DROP_COLLECTION:
        dropCollection(collection);
        break;
    case QT_CREATE_COLLECTION:
        createCollection(collection, typeName);
        break;
    case QT_ALTER_COLLECTION:
        alterCollection(collection, typeName);
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

            vector<QtData *> *data = NULL;
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
            catch (r_Error &myErr)
            {
                delete selectTree;
                LERROR << "Error: bad exception while evaluating insert sub-query: " << myErr.what();
                throw;
            }
            catch (...)
            {
                delete selectTree;
                LERROR << "Error: unknown exception while evaluating insert sub-query, re-throwing.";
                throw;
            }


            if (data == NULL)
            {
                LERROR << "Error: evaluating the SELECT sub-query failed.";
                throw r_Error(r_Error::r_Error_QueryExecutionFailed, 242);
            }
            else
            {
                LTRACE << "evaluate() - selected result size: " << data->size();
            }

            if (collectionExists(collection))
            {
                LWARNING << "Warning: inserting into an existing collection " << collection.getCollectionName();
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
                createCollection(collection, collectionType);
                LTRACE << "evaluate() - created collection " << collection.getCollectionName() << " with type " << collectionType;
            }

            /*
             * 4/4: Insert the data into the new collection
             */
            insertIntoCollection(data, collection);
            LTRACE << "evaluate() - data successfully inserted into collection " << collection.getCollectionName();
        }
        catch (r_Ebase_dbms &myErr)
        {
            LERROR << "Error: base DBMS exception: " << myErr.what();
            throw;
        }
        catch (r_Error &myErr)
        {
            LERROR << "Error: " << myErr.get_errorno() << " " << myErr.what();
            throw;
        }
        catch (bad_alloc &e)
        {
            LERROR << "Error: cannot allocate memory.";
            throw;
        }
        catch (ParseInfo &e)
        {
            LERROR << "Error: ";
            e.printStatus(RMInit::logOut);
            throw;
        }
        catch (...)
        {
            LERROR << "Error: caught unknown exception while evaluation SELECT INTO query, re-throwing.";
            throw;
        }
    default:
        break;
    }

    stopTimer();

    return 0;
}



void
QtCommand::printTree(int tab, std::ostream &s, __attribute__((unused)) QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCommand Object" << std::endl;

    switch (command)
    {
    case QT_DROP_COLLECTION:
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  drop collection("   << collection.getCollectionName().c_str() << ")";
        break;
    case QT_CREATE_COLLECTION:
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  create collection(" << collection.getCollectionName().c_str() << ", " << typeName.c_str() << ")";
        break;
    case QT_CREATE_COLLECTION_FROM_QUERY_RESULT:
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  select into(" << collection.getCollectionName().c_str() << ", " << typeName.c_str() << ")";
        break;
    default:
        s << "<command unknown>";
    }

    s << getEvaluationTime();

    s << std::endl;
}



void
QtCommand::printAlgebraicExpression(std::ostream &s)
{
    s << "command<";

    switch (command)
    {
    case QT_DROP_COLLECTION:
        s << "drop collection("   << collection.getCollectionName().c_str() << ")";
        break;
    case QT_CREATE_COLLECTION:
        s << "create collection(" << collection.getCollectionName().c_str() << ", " << typeName.c_str() << ")";
        break;
    case QT_CREATE_COLLECTION_FROM_QUERY_RESULT:
        s << "select ";
        childNode->printAlgebraicExpression(s);
        s << " into " << collection.getCollectionName().c_str() << ", " << typeName.c_str() << ")";
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

