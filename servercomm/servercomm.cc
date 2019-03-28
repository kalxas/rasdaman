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
/**
 * SOURCE: servercomm.cc
 *
 * MODULE: servercomm
 * CLASS:  ServerComm
 *
 * COMMENTS:
 *          None
*/

#include "config.h"
#include "servercomm.hh"
#include "cliententry.hh"

#include "raslib/rmdebug.hh"
#include "raslib/rminit.hh"
#include "raslib/error.hh"
#include "raslib/minterval.hh"
#include "raslib/parseparams.hh"
#include "raslib/mddtypes.hh"
#include "raslib/basetype.hh"
#include "raslib/endian.hh"
#include "raslib/odmgtypes.hh"
#include "raslib/parseparams.hh"

#include "catalogmgr/typefactory.hh"
#include "mddmgr/mddcoll.hh"
#include "mddmgr/mddcolliter.hh"
#include "mddmgr/mddobj.hh"
#include "tilemgr/tile.hh"
#include "lockmgr/lockmanager.hh"
#include "mymalloc/mymalloc.h"

#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtintervaldata.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtstringdata.hh"
#include "qlparser/qtnode.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/querytree.hh"

#include "reladminif/eoid.hh"
#include "relcatalogif/mddtype.hh"
#include "relcatalogif/mdddomaintype.hh"
#include "relcatalogif/settype.hh"
#include "relcatalogif/structtype.hh"

#include "debug.hh"
#include <logging.hh>

#include <iostream>
#include <iomanip>
#include <cmath>
#include <cstring>
#include <cstdio>
#include <cstdlib>
#include <ctime>      // for time()
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <errno.h>
#include <signal.h>    // for sigaction()
#include <unistd.h>    // for alarm(), gethostname()
#include <byteswap.h>

#ifdef ENABLE_PROFILING
#include <google/profiler.h>
#include <gperftools/heap-profiler.h>
#include <string>
#endif

using namespace std;

// init globals for server initialization
// RMINITGLOBALS('S')


// --------------------------------------------------------------------------------
//                          constants
// --------------------------------------------------------------------------------

const int ServerComm::RESPONSE_ERROR = 0;
const int ServerComm::RESPONSE_MDDS = 1;
const int ServerComm::RESPONSE_SCALARS = 2;
const int ServerComm::RESPONSE_INT = 3;
const int ServerComm::RESPONSE_OID = 4;
const int ServerComm::RESPONSE_OK_NEGATIVE = 98;
const int ServerComm::RESPONSE_OK = 99;

const int ServerComm::ENDIAN_BIG = 0;
const int ServerComm::ENDIAN_LITTLE = 1;

/// ensureTileFormat returns the following:
const int ServerComm::ENSURE_TILE_FORMAT_OK = 0;
const int ServerComm::ENSURE_TILE_FORMAT_BAD = -1;

const char *ServerComm::HTTPCLIENT = "HTTPClient";

// --- these defs should go into a central constant definition section,
// as they define externally observable behavior -- PB 2003-nov-15

// waiting period until client is considered dead [secs]
#define CLIENT_TIMEOUT  3600

// timeout for select() call at server startup [secs]
#define TIMEOUT_SELECT  30

// period after which the next garbage collection is scheduled [secs]
#define GARBCOLL_INTERVAL 600

// console output describing successful/unsuccessful actions
#define MSG_OK      "ok"
#define MSG_FAILED  "failed"

// rasserver exit codes (selection of value sometimes unclear :-(
#define EXITCODE_ZERO       0
#define EXITCODE_ONE        1
#define EXITCODE_RASMGR_FAILED  10 // Why 10 ?

// --------------------------------------------------------------------------------

// static variables
ServerComm *ServerComm::serverCommInstance = 0;
std::vector<ClientTblElt *> ServerComm::clientTbl;
unsigned long ServerComm::clientCount = 0;

// --------------------------------------------------------------------------------

// global variables
MDDColl *mddConstants = 0;
ClientTblElt *currentClientTblElt = 0;
// This is needed in httpserver.cc
char globalHTTPSetTypeStructure[4096];

// defined elsewhere
extern int yyparse(void *);
extern void yyreset();

extern unsigned long maxTransferBufferSize;
extern QueryTree *parseQueryTree;
extern ParseInfo *parseError;
extern char *beginParseString;
extern char *iterParseString;
extern unsigned long maxTransferBufferSize;
extern char *dbSchema;

// -----------------------------------------------------------------------------------------
/// start the gperftools profilers
#ifdef ENABLE_PROFILING
void startProfiler(std::string fileNameTemplate, bool cpuProfiler)
{
    {
        char tmpFileName[fileNameTemplate.size() + 1];
        strcpy(tmpFileName, fileNameTemplate.c_str());

        int fd = mkstemps(tmpFileName, 6);
        if (fd != -1)
        {
            remove(tmpFileName);
            if (cpuProfiler)
            {
                ProfilerStart(tmpFileName);
                LINFO << "CPU profiler file: " << tmpFileName;
            }
            else
            {
                HeapProfilerStart(tmpFileName);
                LINFO << "Heap profiler file: " << tmpFileName << ".????.heap";
            }

        }
        else
        {
            LERROR << "failed creating a temporary profiler file: " << tmpFileName;
            LERROR << "reason: " << strerror(errno);
        }
    }
}
#endif

// -----------------------------------------------------------------------------------------
// handle logging requests

#ifdef RASDEBUG
#define DBGREQUEST(msg)   NNLINFO << "Request: " << msg << "... ";
#define DBGOK             BLINFO  << MSG_OK << "\n";
#define DBGINFO(msg)      BLINFO  << msg << "\n";
#define DBGINFONNL(msg)   BLINFO  << msg;
#define DBGERROR(msg)     BLERROR << "Error: " << msg << "\n";
#define DBGWARN(msg)      BLWARNING << "Warning: " << msg << "\n";
#else
std::stringstream requestStream;
#define DBGREQUEST(msg) { requestStream.clear(); requestStream << "Request: " << msg << "... "; }
#define DBGOK           ; // nothing to log if release mode if all is ok
#define DBGINFO(msg)    ; // nothing to log if release mode if all is ok
#define DBGINFONNL(msg) ; // nothing to log if release mode if all is ok
#define DBGERROR(msg)   { NNLINFO << requestStream.str(); BLERROR << "Error: " << msg << "\n"; requestStream.clear(); }
#define DBGWARN(msg)    { NNLINFO << requestStream.str(); BLWARNING << "Warning: " << msg << "\n"; requestStream.clear(); }
#endif

// -----------------------------------------------------------------------------------------

ServerComm::ServerComm(): ServerComm(CLIENT_TIMEOUT, GARBCOLL_INTERVAL, 0, NULL, 0, NULL) {}

ServerComm::ServerComm(unsigned long timeOut, unsigned long managementInterval, unsigned long newListenPort,
                       char *newRasmgrHost, unsigned int newRasmgrPort, char *newServerName)
        : clientTimeout(timeOut), garbageCollectionInterval(managementInterval), listenPort{newListenPort},
          rasmgrHost{newRasmgrHost}, rasmgrPort{newRasmgrPort}, serverName{newServerName}
{
    assert(!serverCommInstance);
    serverCommInstance = this;
    clientTbl.reserve(2); // usually there's only one client
}

ServerComm::~ServerComm()
{
    delete admin;
    serverCommInstance = NULL;
}

// quick hack function used when stopping server to abort transaction and close db
void
ServerComm::abortEveryThingNow()
{
    if (serverCommInstance)
    {
        for (auto *clnt: serverCommInstance->clientTbl)
        {
            clnt->transaction.abort();
            clnt->database.close();
        }
    }
}

ClientTblElt *
ServerComm::getClientContext(unsigned long clientId)
{
    for (auto *clientEntry: clientTbl)
    {
        if (clientEntry && clientId == clientEntry->clientId)
        {
            // Valid entry was found, so increase the number of current users and
            // reset the client's lastActionTime to now.
            // TODO: time() is not a cheap call, should be removed if not necessary
            clientEntry->currentUsers++;
            clientEntry->lastActionTime = static_cast<long unsigned int>(time(NULL));
            return clientEntry;
        }
    }
    return NULL;
}

void
ServerComm::addClientTblEntry(ClientTblElt *context)
{
    if (context == NULL)
    {
        LERROR << "Cannot register client in the client table: client context is NULL.";
        throw r_Error(r_Error::r_Error_RefNull);
    }
    clientTbl.push_back(context);
#ifdef RASDEBUG
    ServerComm::printServerStatus();   // quite verbose
#endif
}

unsigned short
ServerComm::deleteClientTblEntry(unsigned long clientId)
{
    static constexpr unsigned short RC_CLIENT_MULTIPLE_USERS = 2;

    ClientTblElt *context = getClientContext(clientId);
    if (!context)
    {
        LDEBUG << "Warning: null context, client " << clientId << " not found.";
        return RC_CLIENT_NOT_FOUND;  // desired client id was not found in the client table
    }
    if (context->currentUsers > 1)
    {
        // In this case, the client table entry was under use before our getClientContext() call.
        context->release();
        LDEBUG << "Client context of user " << clientId << " has current users = " << context->currentUsers;
        return RC_CLIENT_MULTIPLE_USERS;
    }

    // The transaction contained in the client table element is aborted here.
    // This is reasonable because at this point, the transaction is either
    // already committed (This is the case if an rpcCloseDB call arrives.
    // In this case, abort doesn't do anything harmful.) or the communication
    // has broken down before a rpcCommitTA or a rpcAbortTA (In this case this
    // function is called by the garbage collection and aborting the transaction
    // is advisable.).

    context->releaseTransferStructures();

    // If the current transaction belongs to this client, abort it.
    if (transactionActive == clientId)
    {
        LDEBUG << "aborting transaction...";
        context->transaction.abort();
        transactionActive = 0;
    }

    // close the database if it isn't already closed
    // (e.g. after connection breakdowns)
    if (strcmp(context->baseName, "none") != 0)
    {
        LDEBUG << "closing database...";
#ifndef BASEDB_SQLITE
        context->database.close();
#endif
        // reset database name
        delete[] context->baseName;
        context->baseName = new char[5];
        strcpy(context->baseName, "none");
    }
#ifdef RASDEBUG
    ServerComm::printServerStatus();      // can be pretty verbose
#endif

    // remove the entry from the client table
    for (auto it = clientTbl.begin(); it != clientTbl.end(); ++it)
    {
        if (*it == context)
        {
            clientTbl.erase(it);
            break;
        }
    }
    // delete the client table entry data itself
    delete context;
    context = NULL;

    LDEBUG << "client table now has " << clientTbl.size() << " entries.";
    return RC_OK;
}

void
ServerComm::printServerStatus()
{
#ifdef RASDEBUG
    stringstream ct;
    if (!clientTbl.empty())
    {
        ct << "\n  Client table dump";
        for (auto iter = clientTbl.begin(); iter != clientTbl.end(); iter++)
        {
            if (*iter == NULL)
            {
                LERROR << "null context found.";
                continue;
            }
            ct << "\n  Client ID        : " << (*iter)->clientId
               << "\n    Current Users  : " << (*iter)->currentUsers
               << "\n    Client location: " << (*iter)->clientIdText
               << "\n    User name      : " << (*iter)->userName
               << "\n    Database in use: " << (*iter)->baseName
               << "\n    Creation time  : " << ctime((time_t*)&(*iter)->creationTime)
               <<   "    Last action at : " << ctime((time_t*)&(*iter)->lastActionTime)
               <<   "    MDD collection : " << (*iter)->transferColl
               << "\n    MDD iterator   : " << (*iter)->transferCollIter
               << "\n    Current PersMDD: " << (*iter)->assembleMDD
               << "\n    Current MDD    : " << (*iter)->transferMDD
               << "\n    Tile vector    : " << (*iter)->transTiles
               << "\n    Tile iterator  : " << (*iter)->tileIter
               << "\n    Block byte cntr: " << (*iter)->bytesToTransfer;
        }
    }
    auto currentTime = time(NULL);

    LDEBUG << "\n-----------------------------------------------------------------------------"
           << "\nServer state information at " << ctime(&currentTime)
           <<   "  Inactivity time out of clients.: " << clientTimeout << " sec"
           << "\n  Server management interval.....: " << garbageCollectionInterval << " sec"
           << "\n  Transaction active.............: " << (transactionActive ? "yes" : "no")
           << "\n  Max. transfer buffer size......: " << maxTransferBufferSize << " bytes"
           << "\n  Next available client id.......: " << clientCount + 1
           << "\n  No. of client table entries....: " << clientTbl.size()
           << ct.str()
           << "\n-----------------------------------------------------------------------------";
#else
    return;
#endif
}

// -----------------------------------------------------------------------------------------
// DB methods: open, close, create, destroy
// -----------------------------------------------------------------------------------------

unsigned short
ServerComm::openDB(unsigned long callingClientId, const char *dbName, const char *userName)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'open DB', name = " << dbName);

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();
        try
        {
            context->database.open(dbName);
            // database was successfully opened, so assign db and user name
            delete[] context->baseName;
            context->baseName = new char[strlen(dbName) + 1];
            strcpy(context->baseName, dbName);
            delete[] context->userName;
            context->userName = new char[strlen(userName) + 1];
            strcpy(context->userName, userName);
            DBGOK;
        }
        catch (r_Error &err)
        {
            if (err.get_kind() == r_Error::r_Error_DatabaseOpen)
            {
                DBGWARN("database already open for user '" << userName << "', ignoring command.");
            }
            else
            {
                returnValue = RC_ERROR;
                DBGERROR(err.what())
            }
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::closeDB(unsigned long callingClientId)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'close DB'");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();
        try
        {
            // If the current transaction belongs to this client, abort it.
            if (transactionActive == callingClientId)
            {
                DBGWARN("transaction is open; aborting this transaction...");
                context->transaction.abort();
                transactionActive = 0;
            }
            context->database.close();
            DBGOK
        }
        catch (r_Error &err)
        {
            if (err.get_kind() == r_Error::r_Error_DatabaseClosed)
            {
                DBGWARN("database already closed, ignoring command.");
            }
            else
            {
                returnValue = RC_ERROR;
                DBGERROR(err.what())
            }
        }

        // reset database name
        delete[] context->baseName;
        context->baseName = new char[5];
        strcpy(context->baseName, "none");

        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

// no client id as this method is only ever called from rasdl
unsigned short
ServerComm::createDB(char *name)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'create DB', name = " << name);

    auto tempDbIf = std::unique_ptr<DatabaseIf>(new DatabaseIf());
    try
    {
        tempDbIf->createDB(name, dbSchema);
        DBGOK
    }
    catch (r_Error &myErr)
    {
        DBGERROR(myErr.what());
        throw;
    }
    catch (...)
    {
        DBGERROR("Unspecified exception.");
        throw;
    }
    return returnValue;
}

// no client id as this method is only ever called from rasdl
unsigned short
ServerComm::destroyDB(char *name)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'destroy DB', name = " << name);
    auto tempDbIf = std::unique_ptr<DatabaseIf>(new DatabaseIf());
    try
    {
        tempDbIf->destroyDB(name);
        DBGOK
    }
    catch (r_Error &myErr)
    {
        DBGERROR(myErr.what());
        throw;
    }
    return returnValue;
}

// -----------------------------------------------------------------------------------------
// Transaction (TA) methods: begin, commit, abort, isTAOpen
// -----------------------------------------------------------------------------------------

unsigned short
ServerComm::beginTA(unsigned long callingClientId, unsigned short readOnly)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'begin TA', mode = " << (readOnly ? "read" : "write"));

    ClientTblElt *context = getClientContext(callingClientId);
    if (!context)
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    else
    {
        if (transactionActive)
        {
            DBGERROR("transaction already active.");
            returnValue = RC_ERROR;
        }
        else
        {
            context->releaseTransferStructures();
            try
            {
                context->transaction.begin(&context->database, readOnly);
                transactionActive = callingClientId;
                DBGOK
            }
            catch (r_Error &err)
            {
                DBGERROR(err.what());
                context->release();
                throw;
            }
        }
        context->release();
    }
    return returnValue;
}

unsigned short
ServerComm::commitTA(unsigned long callingClientId)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'commit TA'");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();
        if (configuration.isLockMgrOn())
            LockManager::Instance()->unlockAllTiles();
        try
        {
            context->transaction.commit();
            transactionActive = 0;
            DBGOK
        }
        catch (r_Error &err)
        {
            DBGERROR(err.what());
            context->release();
            throw;
        }
        catch (...)
        {
            DBGERROR("unspecific exception.");
            context->release();
            throw;
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::abortTA(unsigned long callingClientId)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'abort TA'");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();

        try
        {
            context->transaction.abort();
            if (configuration.isLockMgrOn())
                LockManager::Instance()->unlockAllTiles();
            transactionActive = 0;
            DBGOK
        }
        catch (r_Error &err)
        {
            DBGERROR(err.what());
            context->release();
            throw;
        }
        catch (...)
        {
            DBGERROR("unspecific exception.");
            context->release();
            throw;
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

// only one transaction can be active per server, so just need to check the transactionActive
bool
ServerComm::isTAOpen(__attribute__((unused)) unsigned long callingClientId)
{
    DBGREQUEST("'is TA open'");
    bool returnValue = transactionActive;
    DBGINFO((transactionActive ? "yes." : "no."));
    return returnValue;
}

// -----------------------------------------------------------------------------------------
// Execute rasql queries (select, update, insert)
// -----------------------------------------------------------------------------------------

unsigned short
ServerComm::executeQuery(unsigned long callingClientId,
                         const char *query,
                         ExecuteQueryRes &returnStructure)
{
    static constexpr unsigned short RC_OK_SCALAR_ELEMENTS = 1;
    static constexpr unsigned short RC_OK_NO_ELEMENTS = 2;
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 3;
    static constexpr unsigned short RC_PARSING_ERROR = 4;
    static constexpr unsigned short RC_EXECUTION_ERROR = 5;

    unsigned short returnValue = RC_OK;
#ifdef ENABLE_PROFILING
    startProfiler("/tmp/rasdaman_query_select.XXXXXX.pprof", true);
    startProfiler("/tmp/rasdaman_query_select.XXXXXX.pprof", false);
#endif

    // set all to zero as default. They are not really applicable here.
    returnStructure.errorNo = 0;
    returnStructure.lineNo = 0;
    returnStructure.columnNo = 0;

    NNLINFO << "Request: '" << query << "'... ";

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
#ifdef RMANBENCHMARK
        Tile::relTimer.start();
        Tile::relTimer.pause();
        Tile::opTimer.start();
        Tile::opTimer.pause();
#endif
        mddConstants = context->transferColl; // assign the mdd constants collection to the global pointer (temporary)
        context->transferColl = NULL;

        context->releaseTransferStructures();
        currentClientTblElt = context;       // assign current client table element (temporary)

        QueryTree *qtree = new QueryTree();   // create a query tree object...
        parseQueryTree = qtree;               // ...and assign it to the global parse query tree pointer;

        beginParseString = const_cast<char *>(query);
        iterParseString = const_cast<char *>(query);

        BLINFO << "parsing... ";
        yyreset();
        int parserRet = yyparse(0);
        if (parserRet == 0)
        {
            // parsing was successful
            try
            {
#ifdef RASDEBUG
                LDEBUG << "\n" << *qtree;
#endif
                BLINFO << "checking semantics... ";
                qtree->checkSemantics();
#ifdef RASDEBUG
                BLDEBUG << "query tree after semantic check:\n" << *qtree;
#endif
#ifdef RMANBENCHMARK
                if (RManBenchmark > 0)
                    context->evaluationTimer = new RMTimer("ServerComm", "evaluation");
#endif
                BLINFO << "evaluating... ";
                context->transferData = qtree->evaluateRetrieval();
            }
            catch (ParseInfo &info)
            {
                ostringstream os;
                info.printStatus(os);
                BLERROR << "Error: " << os.str() << "\n";

                context->releaseTransferStructures();
                // set the error values of the return structure
                returnStructure.errorNo = info.getErrorNo();
                returnStructure.lineNo = info.getLineNo();
                returnStructure.columnNo = info.getColumnNo();
                returnStructure.token = strdup(info.getToken().c_str());
                returnValue = RC_EXECUTION_ERROR;
            }
            catch (r_Ebase_dbms &myErr)
            {
                BLERROR << "Base DBMS exception: " << myErr.what() << "\n";
                // release data
                context->releaseTransferStructures();
                context->release();
                if (mddConstants)
                {
                    mddConstants->releaseAll();
                    delete mddConstants;
                    mddConstants = NULL;
                }
                parseQueryTree = 0;
                currentClientTblElt = 0;
                delete qtree;
                throw;
            }
            catch (r_Error &myErr)
            {
                BLERROR << "Error: " << myErr.get_errorno() << " " << myErr.what() << "\n";
                // release data
                context->releaseTransferStructures();
                context->release();
                if (mddConstants)
                {
                    mddConstants->releaseAll();
                    delete mddConstants;
                    mddConstants = NULL;
                }
                parseQueryTree = 0;
                currentClientTblElt = 0;
                delete qtree;
                throw;
            }
            catch (std::bad_alloc)
            {
                BLERROR << "Error: cannot allocate memory.\n";
                // release data
                context->releaseTransferStructures();
                context->release();
                if (mddConstants)
                {
                    mddConstants->releaseAll();
                    delete mddConstants;
                    mddConstants = NULL;
                }
                parseQueryTree = 0;
                currentClientTblElt = 0;
                delete qtree;
                throw;
            }
            catch (...)
            {
                BLERROR << "Error: unspecific exception.\n";
                // release data
                context->releaseTransferStructures();
                context->release();
                if (mddConstants)
                {
                    mddConstants->releaseAll();
                    delete mddConstants;
                    mddConstants = NULL;
                }
                parseQueryTree = 0;
                currentClientTblElt = 0;
                delete qtree;
                throw;
            }

            if (returnValue == RC_OK)
            {
                if (context->transferData != 0)
                {
                    // create the transfer iterator
                    context->transferDataIter = new vector<QtData *>::iterator;
                    *(context->transferDataIter) = context->transferData->begin();

                    //
                    // set typeName and typeStructure
                    //

                    // The type of first result object is used to determine the type of the result
                    // collection.
                    if (*(context->transferDataIter) != context->transferData->end())
                    {
                        QtData *firstElement = (**(context->transferDataIter));

                        if (firstElement->getDataType() == QT_MDD)
                        {
                            QtMDD *mddObj = static_cast<QtMDD *>(firstElement);
                            const BaseType *baseType = mddObj->getMDDObject()->getCellType();
                            r_Minterval domain = mddObj->getLoadDomain();

                            MDDType *mddType = new MDDDomainType("tmp", const_cast<BaseType *>(baseType), domain);
                            TypeFactory::addTempType(mddType);
                            SetType *setType = new SetType("tmp", mddType);
                            TypeFactory::addTempType(setType);

                            returnStructure.typeName = strdup(setType->getTypeName());
                            returnStructure.typeStructure = setType->getTypeStructure();  // no copy

                            // print total data size
                            unsigned long totalReturnedSize = 0;
                            for (auto it = context->transferData->begin(); it != context->transferData->end(); it++)
                            {
                                auto baseTypeSize = mddObj->getMDDObject()->getCellType()->getSize();
                                r_Minterval tmpDomain = mddObj->getLoadDomain();
                                totalReturnedSize += (tmpDomain.cell_count() * baseTypeSize);
                            }
                            BLINFO << MSG_OK << ", result type '" << returnStructure.typeStructure << "', "
                                   << context->transferData->size() << " element(s)"
                                   << ", total size " << totalReturnedSize << " bytes.\n";
                        }
                        else
                        {
                            returnValue = RC_OK_SCALAR_ELEMENTS;       // evaluation ok, non-MDD elements
                            returnStructure.typeName = strdup("");
                            // hack set type
                            char *elementType = firstElement->getTypeStructure();
                            returnStructure.typeStructure = static_cast<char *>(mymalloc(strlen(elementType) + 6));
                            sprintf(returnStructure.typeStructure, "set<%s>", elementType);
                            free(elementType);
                            BLINFO << MSG_OK << ", result type '" << returnStructure.typeStructure << "', "
                                   << context->transferData->size() << " element(s).\n";
                        }

                        strcpy(globalHTTPSetTypeStructure, returnStructure.typeStructure);
                    }
                    else
                    {
                        BLINFO << "ok, result is empty.\n";
                        returnValue = RC_OK_NO_ELEMENTS;
                        returnStructure.typeName = strdup("");
                        returnStructure.typeStructure = strdup("");
                    }
                }
                else
                {
                    BLINFO << "ok, result is empty.\n";
                    returnValue = RC_OK_NO_ELEMENTS;
                }
            }
        }
        else
        {
            // parse error
            if (parseError)
            {
                ostringstream os;
                parseError->printStatus(os);
                BLERROR << "Error: " << os.str() << "\n";

                returnStructure.errorNo = parseError->getErrorNo();
                returnStructure.lineNo = parseError->getLineNo();
                returnStructure.columnNo = parseError->getColumnNo();
                returnStructure.token = strdup(parseError->getToken().c_str());
                delete parseError, parseError = NULL;
            }
            else
            {
                returnStructure.errorNo = 309;
                BLERROR << "Error: Unknown parsing error.\n";
            }
            yyreset(); // reset the input buffer of the scanner
            returnValue = RC_PARSING_ERROR;
        }

        parseQueryTree = 0;
        currentClientTblElt = 0;
        delete qtree, qtree = NULL;
        if (mddConstants)
        {
            mddConstants->releaseAll();
            delete mddConstants;
            mddConstants = NULL;
        }

#ifdef RMANBENCHMARK
        // Evaluation timer can not be stopped because some time spent in the transfer
        // module is added to this phase.
        if (context->evaluationTimer)
            context->evaluationTimer->pause();
        if (RManBenchmark > 0)
            context->transferTimer = new RMTimer("ServerComm", "transfer");
#endif

        // In case of an error or the result set is empty,
        // no endTransfer() is called by the client.
        // Therefore, some things have to be release here.
        if (returnValue >= RC_OK_NO_ELEMENTS)
        {
#ifdef RMANBENCHMARK
            Tile::opTimer.stop();
            Tile::relTimer.stop();
            delete context->evaluationTimer;
            context->evaluationTimer = 0;
            delete context->transferTimer;
            context->transferTimer = 0;

            RMTimer* releaseTimer = 0;
            if (RManBenchmark > 0)
                releaseTimer = new RMTimer("ServerComm", "release");
#endif
            context->releaseTransferStructures();
#ifdef RMANBENCHMARK
            delete releaseTimer;
            releaseTime = NULL;
#endif
        }
        context->release();
    }
    else
    {
        BLERROR << "Error: client not registered.\n";
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }

#ifdef ENABLE_PROFILING
    ProfilerStop();
    HeapProfilerStop();
#endif

    return returnValue;
}


unsigned short
ServerComm::initExecuteUpdate(unsigned long callingClientId)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'initialize update'... ");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();

        MDDType *mddType = new MDDType("tmp");
        TypeFactory::addTempType(mddType);

        SetType *setType = new SetType("tmp", mddType);
        TypeFactory::addTempType(setType);
        try
        {
            // create a transient collection for storing MDD constants
            context->transferColl = new MDDColl(setType);
            context->release();
            DBGOK
        }
        catch (r_Error &err)
        {
            DBGERROR(err.what());
            context->release();
            throw;
        }
        catch (...)
        {
            DBGERROR("unspecific exception while creating transient collection.");
            context->release();
            throw;
        }
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}


unsigned short
ServerComm::executeUpdate(unsigned long callingClientId,
                          const char *query,
                          ExecuteUpdateRes &returnStructure)
{
#ifdef ENABLE_PROFILING
    startProfiler("/tmp/rasdaman_query_update.XXXXXX.pprof", true);
    startProfiler("/tmp/rasdaman_query_update.XXXXXX.pprof", false);
#endif

#ifdef RMANBENCHMARK
    Tile::relTimer.start();
    Tile::relTimer.pause();
    Tile::opTimer.start();
    Tile::opTimer.pause();
#endif
    static constexpr unsigned short RC_PARSING_ERROR = 2;
    static constexpr unsigned short RC_EXECUTION_ERROR = 3;

    unsigned short returnValue = RC_OK;

    NNLINFO << "Request: '" << query << "'... ";

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        QueryTree *qtree = new QueryTree();   // create a query tree object...
        parseQueryTree = qtree;               // ...and assign it to the global parse query tree pointer;

        mddConstants = context->transferColl; // assign the mdd constants collection to the global pointer (temporary)
        currentClientTblElt = context;        // assign current client table element (temporary)

        beginParseString = const_cast<char *>(query);
        iterParseString = const_cast<char *>(query);

        BLINFO << "parsing... ";
        yyreset();
        if (yyparse(0) == 0)
        {
            try
            {
#ifdef RASDEBUG
                LDEBUG << "\n" << *qtree;
#endif
                BLINFO << "checking semantics... ";
                qtree->checkSemantics();
#ifdef RASDEBUG
                BLDEBUG << "query tree after semantic check:\n" << *qtree;
#endif

#ifdef RMANBENCHMARK
                if (RManBenchmark > 0)
                    context->evaluationTimer = new RMTimer("ServerComm", "evaluation");
#endif
                BLINFO << "evaluating... ";

                vector<QtData *> *updateResult = qtree->evaluateUpdate();
                // release data
                for (auto *iter: *updateResult)
                    delete iter, iter = NULL;
                delete updateResult, updateResult = NULL;

                context->releaseTransferStructures();
                BLINFO << MSG_OK << "\n";
            }
            catch (ParseInfo &info)
            {
                ostringstream os;
                info.printStatus(os);
                BLERROR << "Error: " << os.str() << "\n";

                context->releaseTransferStructures();
                // set the error values of the return structure
                returnStructure.errorNo = info.getErrorNo();
                returnStructure.lineNo = info.getLineNo();
                returnStructure.columnNo = info.getColumnNo();
                returnStructure.token = strdup(info.getToken().c_str());
                returnValue = RC_EXECUTION_ERROR;
            }
            catch (r_Ebase_dbms &myErr)
            {
                BLERROR << "Base DBMS exception: " << myErr.what() << "\n";
                // release data
                context->releaseTransferStructures();
                context->release();
                mddConstants = NULL;
                parseQueryTree = 0;
                currentClientTblElt = 0;
                delete qtree;
                throw;
            }
            catch (r_Error &err)
            {
                BLERROR << "Error: " << err.get_errorno() << " " << err.what() << "\n";
                context->releaseTransferStructures();
                context->release();
                mddConstants = NULL;
                parseQueryTree = 0;
                currentClientTblElt = 0;
                delete qtree;
                throw;
            }
            catch (std::bad_alloc)
            {
                BLERROR << "Error: cannot allocate memory.\n";
                // release data
                context->releaseTransferStructures();
                context->release();
                mddConstants = NULL;
                parseQueryTree = 0;
                currentClientTblElt = 0;
                delete qtree;
                throw;
            }
            catch (...)
            {
                BLERROR << "Error: unspecific exception.\n";
                // release data
                context->releaseTransferStructures();
                context->release();
                mddConstants = NULL;
                parseQueryTree = 0;
                currentClientTblElt = 0;
                delete qtree;
                throw;
            }
        }
        else
        {
            if (parseError)
            {
                ostringstream os;
                parseError->printStatus(os);
                BLERROR << "Error: " << os.str() << "\n";

                returnStructure.errorNo = parseError->getErrorNo();
                returnStructure.lineNo = parseError->getLineNo();
                returnStructure.columnNo = parseError->getColumnNo();
                returnStructure.token = strdup(parseError->getToken().c_str());
                delete parseError, parseError = NULL;
            }
            else
            {
                returnStructure.errorNo = 309;
                BLERROR << "Error: Unknown parsing error.\n";
            }
            yyreset(); // reset the input buffer of the scanner
            returnValue = RC_PARSING_ERROR;
        }

        parseQueryTree = 0;
        currentClientTblElt = 0;
        delete qtree, qtree = NULL;
        // delete set of mdd constants
        context->releaseTransferStructures();
        context->release();
        mddConstants = NULL;
    }
    else
    {
        BLERROR << "Error: client not registered.\n";
        returnValue = RC_CLIENT_NOT_FOUND;
    }
#ifdef RMANBENCHMARK
    delete context->evaluationTimer;
    context->evaluationTimer = 0;
    Tile::opTimer.stop();
    Tile::relTimer.stop();
#endif
#ifdef ENABLE_PROFILING
    ProfilerStop();
    HeapProfilerStop();
#endif
    return returnValue;
}

unsigned short
ServerComm::executeInsert(unsigned long callingClientId,
                          const char *query,
                          ExecuteQueryRes &returnStructure)
{

#ifdef ENABLE_PROFILING
    startProfiler("/tmp/rasdaman_query_insert.XXXXXX.pprof", true);
    startProfiler("/tmp/rasdaman_query_insert.XXXXXX.pprof", false);
#endif

#ifdef RMANBENCHMARK
    Tile::relTimer.start();
    Tile::relTimer.pause();
    Tile::opTimer.start();
    Tile::opTimer.pause();
#endif
    static constexpr unsigned short RC_OK_SCALAR_ELEMENTS = 1;
    static constexpr unsigned short RC_OK_NO_ELEMENTS = 2;
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 3;
    static constexpr unsigned short RC_PARSING_ERROR = 4;
    static constexpr unsigned short RC_EXECUTION_ERROR = 5;

    unsigned short returnValue = RC_OK;

    NNLINFO << "Request: '" << query << "'... ";

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        QueryTree *qtree = new QueryTree();   // create a query tree object...
        parseQueryTree = qtree;               // ...and assign it to the global parse query tree pointer;

        mddConstants = context->transferColl; // assign the mdd constants collection to the global pointer (temporary)
        currentClientTblElt = context;        // assign current client table element (temporary)

        beginParseString = const_cast<char *>(query);
        iterParseString = const_cast<char *>(query);

        BLINFO << "parsing... ";
        yyreset();
        if (yyparse(0) == 0)
        {
            try
            {
#ifdef RASDEBUG
                LDEBUG << "\n" << *qtree;
#endif
                BLINFO << "checking semantics... ";
                qtree->checkSemantics();
#ifdef RASDEBUG
                BLDEBUG << "query tree after semantic check:\n" << *qtree;
#endif

#ifdef RMANBENCHMARK
                if (RManBenchmark > 0)
                    context->evaluationTimer = new RMTimer("ServerComm", "evaluation");
#endif
                BLINFO << "evaluating... ";

                context->transferData = qtree->evaluateUpdate();
            }
            catch (ParseInfo &info)
            {
                ostringstream os;
                info.printStatus(os);
                BLERROR << "Error: " << os.str() << "\n";

                context->releaseTransferStructures();
                // set the error values of the return structure
                returnStructure.errorNo = info.getErrorNo();
                returnStructure.lineNo = info.getLineNo();
                returnStructure.columnNo = info.getColumnNo();
                returnStructure.token = strdup(info.getToken().c_str());
                returnValue = RC_EXECUTION_ERROR;
            }
            catch (r_Ebase_dbms &myErr)
            {
                BLERROR << "Base DBMS exception: " << myErr.what() << "\n";
                // release data
                context->releaseTransferStructures();
                context->release();
                mddConstants = NULL;
                parseQueryTree = 0;
                currentClientTblElt = 0;
                delete qtree;
                throw;
            }
            catch (r_Error &err)
            {
                BLERROR << "Error: " << err.get_errorno() << " " << err.what() << "\n";
                context->releaseTransferStructures();
                context->release();
                mddConstants = NULL;
                parseQueryTree = 0;
                currentClientTblElt = 0;
                delete qtree;
                throw;
            }
            catch (std::bad_alloc)
            {
                BLERROR << "Error: cannot allocate memory.\n";
                // release data
                context->releaseTransferStructures();
                context->release();
                mddConstants = NULL;
                parseQueryTree = 0;
                currentClientTblElt = 0;
                delete qtree;
                throw;
            }
            catch (...)
            {
                BLERROR << "Error: unspecific exception.\n";
                // release data
                context->releaseTransferStructures();
                context->release();
                mddConstants = NULL;
                parseQueryTree = 0;
                currentClientTblElt = 0;
                delete qtree;
                throw;
            }

            if (returnValue == RC_OK)
            {
                if (context->transferData != 0)
                {
                    // create the transfer iterator
                    context->transferDataIter = new vector<QtData *>::iterator;
                    *(context->transferDataIter) = context->transferData->begin();

                    //
                    // set typeName and typeStructure
                    //

                    // The type of first result object is used to determine the type of the result
                    // collection.
                    if (*(context->transferDataIter) != context->transferData->end())
                    {
                        QtData *firstElement = (**(context->transferDataIter));

                        if (firstElement->getDataType() == QT_MDD)
                        {
                            QtMDD *mddObj = static_cast<QtMDD *>(firstElement);
                            const BaseType *baseType = mddObj->getMDDObject()->getCellType();
                            r_Minterval domain = mddObj->getLoadDomain();

                            MDDType *mddType = new MDDDomainType("tmp", const_cast<BaseType *>(baseType), domain);
                            TypeFactory::addTempType(mddType);
                            SetType *setType = new SetType("tmp", mddType);
                            TypeFactory::addTempType(setType);

                            returnStructure.typeName = strdup(setType->getTypeName());
                            returnStructure.typeStructure = setType->getTypeStructure();  // no copy
                        }
                        else
                        {
                            returnValue = RC_OK_SCALAR_ELEMENTS;
                            returnStructure.typeName = strdup("");
                            // hack set type
                            char *elementType = firstElement->getTypeStructure();
                            returnStructure.typeStructure = static_cast<char *>(mymalloc(strlen(elementType) + 6));
                            sprintf(returnStructure.typeStructure, "set<%s>", elementType);
                            free(elementType);
                        }

                        strcpy(globalHTTPSetTypeStructure, returnStructure.typeStructure);

                        BLINFO << "ok, result type '" << returnStructure.typeStructure << "', "
                               << context->transferData->size() << " element(s).\n";
                    }
                    else
                    {
                        BLINFO << "ok, result is empty.\n";
                        returnValue = RC_OK_NO_ELEMENTS;
                        returnStructure.typeName = strdup("");
                        returnStructure.typeStructure = strdup("");
                    }
                }
                else
                {
                    BLINFO << "ok, result is empty.\n";
                    returnValue = RC_OK_NO_ELEMENTS;
                }
            }
        }
        else
        {
            if (parseError)
            {
                ostringstream os;
                parseError->printStatus(os);
                BLERROR << "Error: " << os.str() << "\n";

                returnStructure.errorNo = parseError->getErrorNo();
                returnStructure.lineNo = parseError->getLineNo();
                returnStructure.columnNo = parseError->getColumnNo();
                returnStructure.token = strdup(parseError->getToken().c_str());
                delete parseError, parseError = NULL;
            }
            else
            {
                returnStructure.errorNo = 309;
                BLERROR << "Error: Unknown parsing error.\n";
            }
            yyreset(); // reset the input buffer of the scanner
            returnValue = RC_PARSING_ERROR;
        }

        parseQueryTree = 0;
        mddConstants = 0;
        currentClientTblElt = 0;
        delete qtree, qtree = NULL;

        // In case of an error or the result set is empty, no endTransfer()
        // is called by the client. Therefore, some things have to be release here.
        if (returnValue >= RC_OK_NO_ELEMENTS)
        {
            context->releaseTransferStructures();
        }
        context->release();
    }
    else
    {
        BLERROR << "Error: client not registered.\n";
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }

#ifdef RMANBENCHMARK
    delete context->evaluationTimer;
    context->evaluationTimer = 0;
    Tile::opTimer.stop();
    Tile::relTimer.stop();
#endif

#ifdef ENABLE_PROFILING
    ProfilerStop();
    HeapProfilerStop();
#endif

    return returnValue;
}

// -----------------------------------------------------------------------------------------
// Insert MDD
// -----------------------------------------------------------------------------------------

unsigned short
ServerComm::startInsertPersMDD(unsigned long callingClientId,
                               const char *collName, r_Minterval &domain,
                               unsigned long typeLength, const char *typeName, r_OId &oid)
{
    static constexpr unsigned short RC_MDDTYPE_NOT_FOUND = 2;
    static constexpr unsigned short RC_INCOMPATIBLE_MDDTYPE = 3;
    static constexpr unsigned short RC_INVALID_MDDTYPE = 4;
    static constexpr unsigned short RC_COLL_NOT_FOUND = 5;
    static constexpr unsigned short RC_GENERAL_ERROR = 6;
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'start inserting persistent MDD type', type = '" << typeName
                                                                 << "', collection = '" << collName << "', domain = " << domain
                                                                 << ", cell size = " << typeLength  << ", size = " << (domain.cell_count() * typeLength));

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();

        const MDDType *mddType = TypeFactory::mapMDDType(typeName);
        if (mddType)
        {
            if (mddType->getSubtype() != MDDType::MDDONLYTYPE)
            {
                try
                {
                    // store PersMDDColl for insert operation at the end of the transfer
                    context->transferColl = MDDColl::getMDDCollection(collName);
                    if (!context->transferColl->isPersistent())
                        throw r_Error(SYSTEM_COLLECTION_NOT_WRITABLE);
                }
                catch (r_Error &obj)
                {
                    if (obj.get_kind() == r_Error::r_Error_ObjectUnknown)
                    {
                        DBGERROR("collection not found.");
                        returnValue = RC_COLL_NOT_FOUND;
                    }
                    else
                    {
                        DBGERROR(obj.what());
                        context->release();
                        throw;
                    }
                }
                catch (...)
                {
                    DBGERROR("unspecific exception while opening collection.");
                    context->release();
                    throw;
                }

                // check MDD and collection type for compatibility

                if (!mddType->compatibleWithDomain(&domain))
                {
                    DBGERROR("MDD type not compatible wrt. its domain: " << domain);
                    context->transferColl->releaseAll();
                    delete context->transferColl;
                    context->transferColl = 0;
                    context->release();
                    return RC_INVALID_MDDTYPE;
                }
                if (!context->transferColl->getCollectionType()->compatibleWith(mddType))
                {
                    DBGERROR("incompatible MDD and collection types.");
                    context->transferColl->releaseAll();
                    delete context->transferColl;
                    context->transferColl = 0;
                    context->release();
                    return RC_INCOMPATIBLE_MDDTYPE;
                }

                // Create persistent MDD for further tile insertions

                StorageLayout ms;
                ms.setTileSize(StorageLayout::DefaultTileSize);
                ms.setIndexType(StorageLayout::DefaultIndexType);
                ms.setTilingScheme(StorageLayout::DefaultTilingScheme);
                if (domain.dimension() == StorageLayout::DefaultTileConfiguration.dimension())
                    ms.setTileConfiguration(StorageLayout::DefaultTileConfiguration);
                try
                {
                    context->assembleMDD = new MDDObj(
                            static_cast<const MDDBaseType *>(mddType), domain, OId(oid.get_local_oid()), ms);
                    DBGOK
                }
                catch (r_Error &err)
                {
                    DBGERROR("while creating persistent tile: " << err.what());
                    returnValue = RC_GENERAL_ERROR;
                }
                catch (std::bad_alloc)
                {
                    DBGERROR("cannot allocate memory.");
                    returnValue = RC_GENERAL_ERROR;
                }
                catch (...)
                {
                    DBGERROR("unspecific exception during creation of persistent object.");
                    returnValue = RC_GENERAL_ERROR;
                }
            }
            else
            {
                DBGERROR("MDD type '" << typeName << "' has no base type.");
                returnValue = RC_MDDTYPE_NOT_FOUND;
            }
        }
        else
        {
            DBGERROR("MDD type name '" << typeName << "' not found.");
            returnValue = RC_MDDTYPE_NOT_FOUND;
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::startInsertTransMDD(unsigned long callingClientId,
                                r_Minterval &domain, unsigned long typeLength, const char *typeName)
{
    static constexpr unsigned short RC_MDDTYPE_NOT_FOUND = 2;
    static constexpr unsigned short RC_INCOMPATIBLE_MDDTYPE = 3;
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'insert MDD', type '"
                       << typeName << "', domain " << domain << ", cell length " << typeLength << ", "
                       << domain.cell_count() * typeLength << " bytes... ")

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        DBGINFO("transferFormat = " << context->transferFormat << ", exactFormat = " << (bool) context->exactFormat);

        // Determine the type of the MDD to be inserted.
        const MDDType *mddType = TypeFactory::mapMDDType(typeName);
        if (mddType)
        {
            if (mddType->getSubtype() != MDDType::MDDONLYTYPE)
            {
                if (mddType->compatibleWithDomain(&domain))
                {
                    context->transferMDD = new MDDObj(static_cast<const MDDBaseType *>(mddType), domain);
                    DBGOK
                }
                else
                {
                    DBGERROR("MDD type not compatible wrt. its domain: " << domain);
                    returnValue = RC_INCOMPATIBLE_MDDTYPE;
                }
            }
            else
            {
                DBGERROR("MDD type '" << typeName << "' has no base type.");
                returnValue = RC_MDDTYPE_NOT_FOUND;
            }
        }
        else
        {
            DBGERROR("MDD type name '" << typeName << "' not found.");
            returnValue = RC_MDDTYPE_NOT_FOUND;
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::insertMDD(unsigned long callingClientId,
                      const char *collName, RPCMarray *rpcMarray, const char *typeName, r_OId &oid)
{
    static constexpr unsigned short RC_MDD_TYPE_NOT_FOUND = 2;
    static constexpr unsigned short RC_INCOMPATIBLE_MDD_TYPE = 3;
    static constexpr unsigned short RC_INVALID_MDD_TYPE = 4;
    static constexpr unsigned short RC_COLL_NOT_FOUND = 5;
    static constexpr unsigned short RC_COLL_CREATE_ERROR = 6;
    unsigned short returnValue = RC_OK;

    DBGERROR("'insert MDD type', type = '" << typeName << "', collection = '" << collName << "'");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();

        const MDDType *mddType = TypeFactory::mapMDDType(typeName);
        if (mddType)
        {
            if (mddType->getSubtype() != MDDType::MDDONLYTYPE)
            {
                // 1. get collection

                std::unique_ptr<MDDColl, std::function<void(MDDColl*)>>
                        collection(nullptr, [context](MDDColl* op) { op->releaseAll(); context->release(); });
                try
                {
                    collection.reset(MDDColl::getMDDCollection(collName));
                    if (!collection->isPersistent())
                    {
                        DBGERROR("inserting into system collection is illegal.");
                        throw r_Error(SYSTEM_COLLECTION_NOT_WRITABLE);
                    }
                }
                catch (r_Error &err)
                {
                    if (err.get_kind() == r_Error::r_Error_ObjectUnknown)
                    {
                        DBGERROR("collection not found.");
                        returnValue = RC_COLL_NOT_FOUND;
                    }
                    else
                    {
                        DBGERROR(err.what());
                        throw;
                    }
                }
                catch (...)
                {
                    DBGERROR("unspecific exception while opening collection.");
                    throw;
                }

                // 2. check MDD and collection type for compatibility

                r_Minterval domain(rpcMarray->domain);
                if (!mddType->compatibleWithDomain(&domain))
                {
                    DBGERROR("MDD type is not compatible wrt. its domain: " << domain);
                    returnValue = RC_INVALID_MDD_TYPE;
                    return returnValue;
                }
                if (!collection->getCollectionType()->compatibleWith(mddType))
                {
                    DBGERROR("MDD and collection types are incompatible.");
                    returnValue = RC_INCOMPATIBLE_MDD_TYPE;
                    return returnValue;
                }

                // 3. create persistent MDD object

                const MDDBaseType *mddBaseType = static_cast<const MDDBaseType *>(mddType);
                MDDObj *mddObj = NULL;
                try
                {
                    StorageLayout ms;
                    ms.setTileSize(StorageLayout::DefaultTileSize);
                    ms.setIndexType(StorageLayout::DefaultIndexType);
                    ms.setTilingScheme(StorageLayout::DefaultTilingScheme);
                    if (domain.dimension() == StorageLayout::DefaultTileConfiguration.dimension())
                    {
                        ms.setTileConfiguration(StorageLayout::DefaultTileConfiguration);
                    }
                    mddObj = new MDDObj(mddBaseType, domain, OId(oid.get_local_oid()), ms);
                }
                catch (r_Error &obj)
                {
                    DBGERROR(obj.what());
                    return RC_COLL_CREATE_ERROR;
                }
                catch (...)
                {
                    DBGERROR("unspecific exception during creation of persistent object.");
                    return RC_COLL_CREATE_ERROR;
                }

                const BaseType *baseType = mddBaseType->getBaseType();
                char *dataPtr = rpcMarray->data.confarray_val;
                r_Bytes dataSize = static_cast<r_Bytes>(rpcMarray->data.confarray_len);
                // reset data area from rpc structure so that it is not deleted
                // deletion is done by TransTile resp. Tile
                rpcMarray->data.confarray_len = 0;
                rpcMarray->data.confarray_val = NULL;
                auto myDataFmt = static_cast<r_Data_Format>(rpcMarray->storageFormat);
                auto myCurrentFmt = static_cast<r_Data_Format>(rpcMarray->currentFormat);
                LTRACE << "oid " << oid
                       << ", domain " << domain
                       << ", cell length " << rpcMarray->cellTypeLength
                       << ", data size " << dataSize
                       << ", rpc storage " << myDataFmt
                       << ", rpc transfer " << myCurrentFmt << " ";

                // store in the specified storage format; the current tile format afterwards will be the
                // requested format if all went well, but use the (new) current format to be sure.
                // Don't repack here, however, because it might be retiled before storage.
                if (ensureTileFormat(myCurrentFmt, myDataFmt, domain, baseType, dataPtr, dataSize, 0, 1,
                                     context->storageFormatParams) != ENSURE_TILE_FORMAT_OK)
                {
                    DBGERROR("illegal tile format for creating object.");
                    return RC_COLL_CREATE_ERROR;
                }
                // if compressed, getMDDData is != 0
                r_Bytes getMDDData = myCurrentFmt == r_Array ? 0 : dataSize;

                // This should check the compressed size rather than the raw data size
                if (RMInit::tiling && dataSize > StorageLayout::DefaultTileSize)
                {
                    r_Range edgeLength = static_cast<r_Range>(floor(exp((1 / static_cast<r_Double>(domain.dimension()))
                                                                        * log(static_cast<r_Double>(StorageLayout::DefaultTileSize) / rpcMarray->cellTypeLength))));
                    if (edgeLength < 1) edgeLength = 1;

                    r_Minterval tileDom(domain.dimension());
                    for (unsigned int i = 0; i < tileDom.dimension(); i++)
                        tileDom << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(edgeLength - 1));

                    Tile *entireTile = new Tile(domain, baseType, true, dataPtr, getMDDData, myDataFmt);
                    vector<Tile *> *tileSet = entireTile->splitTile(tileDom);
                    if (entireTile->isPersistent())
                        entireTile->setPersistent(0);
                    delete entireTile;

                    DBGINFO("creating " << tileSet->size() << " tile(s)... ");
                    for (auto iter = tileSet->begin(); iter != tileSet->end(); iter++)
                        mddObj->insertTile(*iter);
                    delete tileSet;
                }
                else
                {
                    Tile* tile = new Tile(domain, baseType, true, dataPtr, getMDDData, myDataFmt);
                    DBGINFO("creating one tile... ");
                    mddObj->insertTile(tile);
                }
                collection->insert(mddObj);
                DBGOK;
            }
            else
            {
                DBGERROR("MDD type name '" << typeName << "' has no base type.");
                returnValue = RC_MDD_TYPE_NOT_FOUND; // TODO should be different code
            }
        }
        else
        {
            DBGERROR("MDD type name '" << typeName << "' not found.");
            returnValue = RC_MDD_TYPE_NOT_FOUND;
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::endInsertMDD(unsigned long callingClientId,
                         int isPersistent)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'end insert MDD'")

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        if (isPersistent)
        {
            // we are finished with this MDD Object, so insert it into the collection
            context->transferColl->insert(context->assembleMDD);
            // reset assembleMDD, because otherwise it is tried to be freed
            context->assembleMDD = 0;
            // free transfer structure
            context->releaseTransferStructures();
        }
        else
        {
            // we are finished with this MDD Object, so insert it into the collection
            context->transferColl->insert(context->transferMDD);
            // reset transferMDD
            context->transferMDD = 0;
            // Do not delete the transfer structure because the transient set
            // of MDD objects will be used as constants for executeUpdate().
        }
        context->release();
        DBGOK
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

// -----------------------------------------------------------------------------------------
// Insert tile
// -----------------------------------------------------------------------------------------

unsigned short
ServerComm::insertTile(unsigned long callingClientId,
                       bool isPersistent,
                       RPCMarray *rpcMarray)
{
    return insertTileSplitted(callingClientId, isPersistent, rpcMarray, NULL);
}

unsigned short
ServerComm::insertTileSplitted(unsigned long callingClientId,
                               bool isPersistent, RPCMarray *rpcMarray, r_Minterval *tileSize)
{
    static constexpr unsigned short RC_TILE_FORMAT_ERROR = 1;
    static constexpr unsigned short RC_BASETYPE_NOT_SUPPORTED = 2;
    static constexpr unsigned short RC_BASETYPE_MISMATCH = 3;
    static constexpr unsigned short RC_GENERAL_ERROR = 4;
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'insert tile splitted'");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        const BaseType *baseType = isPersistent
                                   ? context->assembleMDD->getCellType() : context->transferMDD->getCellType();
        if (baseType)
        {
            r_Minterval domain(rpcMarray->domain);
            char *dataPtr = rpcMarray->data.confarray_val;
            r_Bytes dataSize = static_cast<r_Bytes>(rpcMarray->data.confarray_len);
            // reset data area from rpc structure so that it is not deleted
            // deletion is done by TransTile resp. Tile
            rpcMarray->data.confarray_len = 0;
            rpcMarray->data.confarray_val = 0;
            r_Data_Format myDataFmt = static_cast<r_Data_Format>(rpcMarray->storageFormat);
            r_Data_Format myCurrentFmt = static_cast<r_Data_Format>(rpcMarray->currentFormat);
            DBGINFO("rpc storage  format : " << myDataFmt << ", rpc transfer format : " << myCurrentFmt);
            // store in specified storage format; use (new) current format afterwards
            // Don't repack here because of possible retiling.
            if (ensureTileFormat(myCurrentFmt, myDataFmt, domain, baseType, dataPtr, dataSize, 0, 1,
                                 context->storageFormatParams) != ENSURE_TILE_FORMAT_OK)
            {
                returnValue = RC_TILE_FORMAT_ERROR;
                context->release();
                return returnValue;
            }
            r_Bytes getMDDData = myCurrentFmt == r_Array ? 0 : dataSize;
            Tile *tile = new Tile(domain, baseType, true, dataPtr, getMDDData, r_Array);
            // for java clients only: check endianness and split tile if necessary
            if (strcmp(context->clientIdText, ServerComm::HTTPCLIENT) == 0)
            {
                // check endianess
                if (r_Endian::get_endianness() != r_Endian::r_Endian_Big)
                {
                    DBGINFO(", changing endianness...");
                    // we have to swap the endianess
                    char *tpstruct = baseType->getTypeStructure();
                    r_Base_Type *useType = static_cast<r_Base_Type *>(r_Type::get_any_type(tpstruct));
                    free(tpstruct);
                    char *newContents = static_cast<char *>(mymalloc(tile->getSize()));
                    // change the endianness of the entire tile for identical domains for src and dest
                    r_Endian::swap_array(useType, domain, domain, tile->getContents(), newContents);
                    delete useType;
                    // set new swapped contents
                    free(tile->getContents());
                    tile->setContents(newContents);
                }
                // split the tile
                vector<Tile *> *tileSet = tile->splitTile(*tileSize);
                LTRACE << "inserting split tile...";
                for (auto iter = tileSet->begin(); iter != tileSet->end(); iter++)
                {
                    if (isPersistent)
                        context->assembleMDD->insertTile(*iter);
                    else
                        context->transferMDD->insertTile(*iter);
                }
                // delete the vector again
                delete tile;
                delete tileSet;
            }
            else
            {
                // insert one single tile; later, we should take into consideration the default server tile-size!
                LTRACE << "inserting single tile...";
                if (isPersistent)
                    context->assembleMDD->insertTile(tile);
                else
                    context->transferMDD->insertTile(tile);
            }
            DBGOK;
        }
        else
        {
            DBGERROR("tile and MDD base type do not match.");
            returnValue = RC_BASETYPE_MISMATCH;
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

// -----------------------------------------------------------------------------------------
// Fetch query results: next MDD, scalar, tile
// -----------------------------------------------------------------------------------------

unsigned short
ServerComm::getNextMDD(unsigned long callingClientId,
                       r_Minterval &mddDomain, char *&typeName, char *&typeStructure,
                       r_OId &oid, unsigned short &currentFormat)
{
    static constexpr unsigned short RC_COLL_EMPTY = 1;
    static constexpr unsigned short RC_MDD_EMPTY = 2;
    static constexpr unsigned short RC_NO_COLL = 2;
    // TODO: this should be 1 as in other methods
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 2;

    DBGREQUEST("(continuing): 'get next MDD'");

    unsigned short returnValue = RC_OK;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        try
        {
            if (context->transferData && context->transferDataIter &&
                *(context->transferDataIter) != context->transferData->end())
            {
                //
                // convert the mdd to transfer to rpc data structures
                //

                // get the MDD object to be transfered
                QtMDD *mddData = static_cast<QtMDD *>(**(context->transferDataIter));
                MDDObj *mddObj = mddData->getMDDObject();
                // initialize mddDomain to give it back
                mddDomain = mddData->getLoadDomain();

                //
                // initialize tiles to transfer
                //

#ifdef RMANBENCHMARK
                // pause transfer timer and resume evaluation timer
                if (context->transferTimer)
                    context->transferTimer->pause();
                if (context->evaluationTimer)
                    context->evaluationTimer->resume();
#endif
                context->transTiles = new vector<Tile *>();
                bool differentDomains = mddObj->getCurrentDomain() != mddData->getLoadDomain();
                // If the load domain is different from the current domain, we have
                // a persitent MDD object. The border tiles have to be cut (and
                // therefore copied) in order to be ready for transferring them.
                // These temporary border tiles are added to the deletableTiles list
                // which is deleted at the end.
                //
                // This is a hack. The mddObj is a part of context->transferDataIter and it will
                // not be deleted until the end of transaction, so storing raw pointers is safe.
                auto tiles = differentDomains ? mddObj->intersect(mddData->getLoadDomain()) : mddObj->getTiles();
                // FIXME: change context->transTiles type to vector< shared_ptr<Tile> >
                for (size_t i = 0; i < tiles->size(); ++i)
                    context->transTiles->push_back((*tiles)[i].get());

                if (differentDomains)
                {
                    for (auto it = context->transTiles->begin(); it != context->transTiles->end(); it++)
                    {
                        // get relevant area of source tile
                        r_Minterval sourceTileDomain(mddData->getLoadDomain().create_intersection((*it)->getDomain()));
                        if (sourceTileDomain != (*it)->getDomain())
                        {
                            // create a new transient tile and copy the transient data
                            Tile *newTransTile = new Tile(sourceTileDomain, mddObj->getCellType());
                            newTransTile->copyTile(sourceTileDomain, *it, sourceTileDomain);
                            // replace the tile in the list with the new one
                            *it = newTransTile;
                            // add the new tile to deleteableTiles
                            if (!(context->deletableTiles))
                                context->deletableTiles = new vector<Tile *>();
                            context->deletableTiles->push_back(newTransTile);
                        }
                    }
                }

#ifdef RMANBENCHMARK
                // In order to be sure that reading tiles from disk is done
                // in the evaluation phase, the contents pointers of each tile are got.
                char* benchmarkPointer = NULL;
                for (auto it = context->transTiles->begin(); it != context->transTiles->end(); it++)
                    benchmarkPointer = (*it)->getContents();
                // pause evaluation timer and resume transfer timer
                if (context->evaluationTimer)
                    context->evaluationTimer->pause();
                if (context->transferTimer)
                    context->transferTimer->resume();
#endif
                // initialize tile iterator
                context->tileIter = new vector<Tile *>::iterator;
                *(context->tileIter) = context->transTiles->begin();

                const BaseType *baseType = mddObj->getCellType();

                DBGINFONNL("domain " << mddDomain << ", cell length " << baseType->getSize() << "... ");

                // set typeName and typeStructure

                // old: typeName = strdup( mddObj->getCellTypeName() ); not known for the moment being
                typeName = strdup("");
                // create a temporary mdd type for the moment being
                MDDType *mddType = new MDDDomainType("tmp", baseType, mddData->getLoadDomain());
                TypeFactory::addTempType(mddType);
                typeStructure = mddType->getTypeStructure();

                if (context->transTiles->size())
                    currentFormat = (*(context->transTiles))[0]->getDataFormat();
                else
                    currentFormat = r_Array;

                // set oid in case of persistent MDD objects
                if (mddObj->isPersistent())
                {
                    EOId eOId;
                    if (mddObj->getEOId(&eOId) == 0)
                        oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                }

                if (context->transTiles->size() > 0)
                {
                    DBGINFO("ok, " << context->transTiles->size() << " more tile(s)");
                }
                else   // context->transTiles->size() == 0
                {
                    returnValue = RC_MDD_EMPTY;
                    DBGWARN("no tiles in MDD object.");
                }
                context->totalTransferedSize = 0;
                context->totalRawSize = 0;
            }
            else
            {
                if (context->transferDataIter && *(context->transferDataIter) == context->transferData->end())
                {
                    returnValue = RC_COLL_EMPTY;
                    DBGINFO("ok, no more tiles.");
                    context->releaseTransferStructures();
                }
                else
                {
                    returnValue = RC_NO_COLL;
                    DBGWARN("no transfer collection.");
                }
            }
            context->release();
        }
        catch (r_Ebase_dbms &myErr)
        {
            DBGERROR("base DBMS exception (kind " << static_cast<unsigned int>(myErr.get_kind())
                                                  << ", errno " << myErr.get_errorno() << ") " << myErr.what());
            throw;
        }
        catch (r_Error &myErr)
        {
            DBGERROR("kind " << myErr.get_kind() << ", errno " << myErr.get_errorno() << " - " << myErr.what());
            throw;
        }
        catch (std::bad_alloc)
        {
            DBGERROR("cannot allocate memory.");
            throw;
        }
        catch (...)
        {
            DBGERROR("unspecified exception.");
            throw;
        }
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }

    return returnValue;
}

void
ServerComm::getNextStructElement(char *buffer, const BaseType *baseType)
{
    switch (baseType->getType())
    {
        case USHORT:
        {
            r_UShort tmp = *(r_UShort *) buffer;
            *(r_UShort *) buffer = r_Endian::swap(tmp);
            break;
        }
        case SHORT:
        {
            r_Short tmp = *(r_Short *) buffer;
            *(r_Short *) buffer = r_Endian::swap(tmp);
            break;
        }
        case LONG:
        {
            r_Long tmp = *(r_Long *) buffer;
            *(r_Long *) buffer = r_Endian::swap(tmp);
            break;
        }
        case ULONG:
        {
            r_ULong tmp = *(r_ULong *) buffer;
            *(r_ULong *) buffer = r_Endian::swap(tmp);
            break;
        }
        case FLOAT:
        {
            uint32_t value = bswap_32(*(uint32_t *) buffer);
            // use memcpy because older (<4.5?) gcc versions
            // choke if we assign to buffer directly
            memcpy(buffer, &value, sizeof(uint32_t));
            break;
        }
        case DOUBLE:
        {
            uint64_t value = bswap_64(*(uint64_t *) buffer);
            // use memcpy because older (<4.5?) gcc versions
            // choke if we assign to buffer directly
            memcpy(buffer, &value, sizeof(uint64_t));
            break;
        }
        case STRUCT:
        {
            const StructType *st = static_cast<const StructType *>(baseType);
            unsigned int numElems = st->getNumElems();
            for (unsigned int i = 0; i < numElems; i++)
            {
                const BaseType *bt = st->getElemType(i);
                unsigned int elemTypeSize = bt->getSize();
                getNextStructElement(buffer, bt);
                buffer += elemTypeSize;
            }
            break;
        }
        default:
            break;
    }

}

unsigned short
ServerComm::getNextElement(unsigned long callingClientId,
                           char *&buffer, unsigned int &bufferSize)
{
    static constexpr unsigned short RC_COLL_EMPTY = 1;
    static constexpr unsigned short RC_MDD_EMPTY = 2;
    static constexpr unsigned short RC_NO_COLL = 2;
    // TODO: this should be 1 as in other methods
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 2;

    DBGREQUEST("(continuing): 'get next element'");

    unsigned short returnValue = RC_OK;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        if (context->transferData && context->transferDataIter &&
            *(context->transferDataIter) != context->transferData->end())
        {
            // convert data element to rpc data structures
            // Buffer is allocated and has to be freed by the caller using free().

            // get next object to be transfered
            try
            {
                QtData *dataObj = **(context->transferDataIter);
                DBGINFONNL("of type " << dataObj->getDataType() << "... ");
                switch (dataObj->getDataType())
                {
                    case QT_STRING:
                    {
                        QtStringData *stringDataObj = static_cast<QtStringData *>(dataObj);
                        bufferSize = stringDataObj->getStringData().length() + 1;
                        buffer = static_cast<char *>(mymalloc(bufferSize));
                        memcpy(buffer, stringDataObj->getStringData().c_str(), bufferSize);
                        break;
                    }
                    case QT_INTERVAL:
                    {
                        QtIntervalData *tmp = static_cast<QtIntervalData *>(dataObj);
                        buffer = tmp->getIntervalData().get_string_representation();
                        bufferSize = strlen(buffer) + 1;
                        break;
                    }
                    case QT_MINTERVAL:
                    {
                        QtMintervalData *tmp = static_cast<QtMintervalData *>(dataObj);
                        buffer = tmp->getMintervalData().get_string_representation();
                        bufferSize = strlen(buffer) + 1;
                        break;
                    }
                    case QT_POINT:
                    {
                        QtPointData *tmp = static_cast<QtPointData *>(dataObj);
                        buffer = tmp->getPointData().get_string_representation();
                        bufferSize = strlen(buffer) + 1;
                        break;
                    }
                    default:
                    {
                        if (dataObj->isScalarData())
                        {
                            QtScalarData *scalarDataObj = static_cast<QtScalarData *>(dataObj);
                            bufferSize = scalarDataObj->getValueType()->getSize();
                            DBGINFONNL("scalar data of size " << bufferSize << "... ");
                            buffer = static_cast<char *>(mymalloc(bufferSize));
                            memcpy(buffer, scalarDataObj->getValueBuffer(), bufferSize);
                            // change endianess if necessary
                            if ((strcmp(context->clientIdText, ServerComm::HTTPCLIENT) == 0) &&
                                (r_Endian::get_endianness() != r_Endian::r_Endian_Big))
                            {
                                // client is a http-client (java -> always BigEndian) and server has LittleEndian
                                getNextStructElement(buffer, scalarDataObj->getValueType());
                            }
                        }
                        break;
                    }
                }
            }
            catch (r_Ebase_dbms &myErr)
            {
                DBGERROR("base BMS exception (kind " << static_cast<unsigned int>(myErr.get_kind())
                                                     << ", errno " << myErr.get_errorno() << ") " << myErr.what());
                throw;
            }
            catch (r_Error &err)
            {
                DBGERROR("kind " << err.get_kind() << ", errno " << err.get_errorno() << " - " << err.what());
                throw;
            }

            // increment list iterator
            (*(context->transferDataIter))++;
            if (*(context->transferDataIter) != context->transferData->end())
            {
                DBGINFO("ok, some more tile(s) left.");
            }
            else
            {
                returnValue = RC_COLL_EMPTY;
                DBGINFO("ok, no more tiles.");
            }
        }
        else
        {
            if (context->transferDataIter && *(context->transferDataIter) == context->transferData->end())
            {
                returnValue = RC_COLL_EMPTY;  // nothing left in the collection
                DBGINFO("nothing left... " << MSG_OK);
                context->releaseTransferStructures();
            }
            else
            {
                returnValue = RC_NO_COLL;  // no actual transfer collection
                DBGWARN("no transfer collection.");
            }
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::getNextTile(unsigned long callingClientId,
                        RPCMarray **rpcMarray)
{

    static constexpr unsigned short RC_MDD_TRANSFERRED = 0;
    static constexpr unsigned short RC_MDD_EMPTY = 0;
    static constexpr unsigned short RC_OK_MORE_MDDS = 1;
    static constexpr unsigned short RC_OK_MORE_TILES = 2;
    static constexpr unsigned short RC_OK_MORE_BLOCKS = 3;
    static constexpr unsigned short RC_NO_COLL = 4;
    // TODO: this should be 1 as in other methods
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 4;

    static constexpr unsigned short ST_MORE_BLOCKS = 1;
    static constexpr unsigned short ST_FINAL_BLOCK = 2;
    static constexpr unsigned short ST_SMALL_TILE = 3;

    DBGREQUEST("(continuing): 'get next tile'");

    unsigned short returnValue = RC_OK;

    // initialize the result parameter for failure cases
    *rpcMarray = NULL;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        if (context->transTiles && context->tileIter)
        {
            Tile *resultTile = **(context->tileIter);

            // allocate memory for the output parameter rpcMarray
            *rpcMarray = static_cast<RPCMarray *>(mymalloc(sizeof(RPCMarray)));

            if (context->bytesToTransfer == 0)
            {
                // free old data
                if (context->encodedData != NULL)
                {
                    free(context->encodedData);
                    context->encodedData = NULL;
                    context->encodedSize = 0;
                }
            }

            unsigned long totalSize = 0;
            char *useTransData = NULL;
            useTransData = resultTile->getContents();
            totalSize = resultTile->getSize();
            (*rpcMarray)->currentFormat = resultTile->getDataFormat();
            (*rpcMarray)->storageFormat = r_Array;
            LTRACE << "using storage format " << (r_Data_Format) (*rpcMarray)->storageFormat;

            unsigned long transferOffset = 0;
            unsigned short statusValue = 0;
            auto transferSize = totalSize;
            if (totalSize > maxTransferBufferSize)
            {
                // if there is the rest of a tile to transfer, do it!
                if (context->bytesToTransfer)
                {
                    LTRACE << " resuming block transfer...";
                    transferOffset = totalSize - context->bytesToTransfer;
                    if (context->bytesToTransfer > maxTransferBufferSize)
                    {
                        transferSize = maxTransferBufferSize;
                        statusValue = ST_MORE_BLOCKS;
                    }
                    else
                    {
                        transferSize = context->bytesToTransfer;
                        statusValue = ST_FINAL_BLOCK;
                    }
                    context->bytesToTransfer -= transferSize;
                }
                else // transfer first block of too large tile
                {
                    LTRACE << " has to be split...";
                    transferSize = maxTransferBufferSize;
                    context->bytesToTransfer = totalSize - transferSize;
                    statusValue = ST_MORE_BLOCKS;
                }
            }
            else    // resultTile->getSize() <= maxTransferBufferSize
            {
                statusValue = ST_SMALL_TILE;
            }

            context->totalTransferedSize += transferSize;

            // 1. convert domain
            (*rpcMarray)->domain = resultTile->getDomain().get_string_representation();
            LTRACE << " domain " << resultTile->getDomain() << ", " << transferSize << " bytes";

            // 2. copy data pointers
            // allocate memory for the output parameter data and assign its fields
            (*rpcMarray)->data.confarray_len = static_cast<unsigned int>(transferSize);
            (*rpcMarray)->data.confarray_val = useTransData + transferOffset;

            // 3. store cell type length
            (*rpcMarray)->cellTypeLength = resultTile->getType()->getSize();

            // increment iterator only if tile is transferred completely
            if (statusValue > ST_MORE_BLOCKS)
            {
                context->totalRawSize += resultTile->getSize();
                (*context->tileIter)++;
            }

            // delete tile vector and increment transfer collection iterator if tile iterator is exhausted
            if ((*context->tileIter) == context->transTiles->end())
            {
                // delete tile vector transTiles (tiles are deleted when the object is deleted)
                delete context->transTiles;
                context->transTiles = NULL;
                delete context->tileIter;
                context->tileIter = NULL;

                if (context->transferDataIter)
                {
                    (*(context->transferDataIter))++;
                    if (*(context->transferDataIter) != context->transferData->end())
                    {
                        returnValue = RC_OK_MORE_MDDS;
                        DBGINFO("ok, some MDD(s) left.");
                    }
                    else
                    {
                        // no elements left -> delete collection and iterator

                        // Memory of last tile is still needed for the last byte transfer,
                        // therefore, do not release memory now, but with any next RPC call.
                        // context->releaseTransferStructures();
                        returnValue = RC_MDD_EMPTY;
                        DBGINFO("ok, all MDDs fetched.");
                    }
                }
                else
                {
                    returnValue = RC_MDD_TRANSFERRED;
                    DBGINFO("ok, MDD transfer complete.");
                }
            }
            else
            {
                if (statusValue == ST_MORE_BLOCKS)
                {
                    DBGINFO("ok, some block(s) left.");
                    returnValue = RC_OK_MORE_BLOCKS;
                }
                else
                {
                    DBGINFO("ok, some tile(s) left.");
                    returnValue = RC_OK_MORE_TILES;
                }
            }
        }
        else    // no actual transfer collection or nothing left in the collection
        {
            returnValue = RC_NO_COLL;
            DBGERROR("no transfer collection or nothing left in collection.");
        }
        context->release();
    }
    else
    {
        // client context not found
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::endTransfer(unsigned long client)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'endTransfer'");

    ClientTblElt *context = getClientContext(client);
    if (context)
    {
#ifdef RMANBENCHMARK
        Tile::relTimer.stop();
        Tile::opTimer.stop();
        delete context->evaluationTimer; context->evaluationTimer = NULL;
        delete context->transferTimer;   context->transferTimer = NULL;
#endif
        context->releaseTransferStructures();
        context->release();
        DBGOK
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

// -----------------------------------------------------------------------------------------
// Collection mgmt, used by the rasodmg C++ API
// -----------------------------------------------------------------------------------------

unsigned short
ServerComm::insertColl(unsigned long callingClientId,
                       const char *collName, const char *typeName, r_OId &oid)
{
    static constexpr unsigned short RC_COLL_EXISTS = 3;
    static constexpr unsigned short RC_GENERAL_ERROR = 4;

    unsigned short returnValue = RC_OK;

    DBGREQUEST("'insert collection', collection name = '" << collName << "', type = '" << typeName << "'");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();

        const CollectionType *collType = static_cast<const CollectionType *>(TypeFactory::mapSetType(typeName));
        if (collType)
        {
            try
            {
                MDDColl *coll = MDDColl::createMDDCollection(collName, OId(oid.get_local_oid()), collType);
                delete coll;
                BLINFO << MSG_OK << "\n";
            }
            catch (r_Error &obj)
            {
                if (obj.get_kind() == r_Error::r_Error_NameNotUnique)
                {
                    DBGERROR("collection exists already.");
                    returnValue = RC_COLL_EXISTS;
                }
                else
                {
                    DBGERROR("cannot create collection, reason: " << obj.what());
                    returnValue = RC_GENERAL_ERROR;
                }
            }
        }
        else
        {
            DBGERROR("unknown collection type: '" << typeName << "'.");
            returnValue = RC_ERROR;
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::deleteCollByName(unsigned long callingClientId,
                             const char *collName)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'delete collection by name', name = '" << collName << "'");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();
        try
        {
            if (MDDColl::dropMDDCollection(collName))
            {
                DBGOK
            }
            else
            {
                DBGERROR("collection does not exist.")
                returnValue = RC_ERROR;
            }
        }
        catch (r_Error &err)
        {
            DBGERROR(err.what())
            context->release();
            throw;
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::deleteObjByOId(unsigned long callingClientId,
                           r_OId &oid)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'delete MDD by OID', oid = '" << oid << "'");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();

        OId oidIf(oid.get_local_oid());
        OId::OIdType objType = oidIf.getType();
        switch (objType)
        {
            case OId::MDDOID:
            {
                // FIXME: why not deleted?? -- PB 2005-aug-27
                DBGINFO("found MDD object; NOT deleted yet... " << MSG_OK);
                break;
            }
            case OId::MDDCOLLOID:
            {
                try
                {
                    if (MDDColl::dropMDDCollection(oidIf))
                    {
                        DBGOK
                    }
                    else
                    {
                        DBGERROR("collection does not exist.")
                        returnValue = RC_ERROR;
                    }
                }
                catch (r_Error &err)
                {
                    DBGERROR(err.what())
                    context->release();
                    throw;
                }
                break;
            }
            default:
            {
                DBGERROR("object has unknown type: " << objType);
                returnValue = RC_ERROR;
            }
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::removeObjFromColl(unsigned long callingClientId,
                              const char *collName, r_OId &oid)
{
    static constexpr unsigned short RC_OBJ_NOT_FOUND = 3;
    static constexpr unsigned short RC_GENERAL_ERROR = 4;
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'remove MDD from collection', collection name = '" << collName << "', oid = '" << oid << "'");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();

        MDDColl *coll = NULL;
        try
        {
            coll = MDDColl::getMDDCollection(collName);
        }
        catch (r_Error &obj)
        {
            if (obj.get_kind() == r_Error::r_Error_ObjectUnknown)
            {
                DBGERROR("collection not found.");
                returnValue = RC_ERROR;
            }
            else
            {
                DBGERROR(obj.what());
                returnValue = RC_GENERAL_ERROR;
            }
        }
        catch (...)
        {
            DBGERROR("unspecified exception.");
            returnValue = RC_GENERAL_ERROR;
        }
        if (coll)
        {
            if (coll->isPersistent())
            {
                OId mddId(oid.get_local_oid());
                OId collId; coll->getOId(collId);
                try
                {
                    if (MDDColl::removeMDDObject(collId, mddId))
                    {
                        DBGOK
                    }
                    else
                    {
                        DBGERROR("object does not exist.")
                        returnValue = RC_OBJ_NOT_FOUND;
                    }
                }
                catch (r_Error &obj)
                {
                    DBGERROR(obj.what());
                    returnValue = RC_GENERAL_ERROR;
                }
                catch (...)
                {
                    DBGERROR("unspecified exception.");
                    returnValue = RC_GENERAL_ERROR;
                }
            }
            delete coll;
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

// -----------------------------------------------------------------------------------------
// Get collection/MDD by name or oid
// -----------------------------------------------------------------------------------------

unsigned short
ServerComm::getCollByName(unsigned long callingClientId,
                          const char *collName, char *&typeName, char *&typeStructure, r_OId &oid)
{
    static constexpr unsigned short RC_OK_SOME_ELEMENTS = 0;
    static constexpr unsigned short RC_OK_NO_ELEMENTS = 1;
    static constexpr unsigned short RC_COLL_NOT_FOUND = 2;
    // TODO: this should be 1 as in other methods
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 3;
    DBGREQUEST("'get collection by name', name = " << collName << "'");

    unsigned short returnValue = RC_OK_SOME_ELEMENTS;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();
        try
        {
            // create the transfer collection
            context->transferColl = MDDColl::getMDDCollection(collName);
        }
        catch (r_Error &obj)
        {
            if (obj.get_kind() == r_Error::r_Error_ObjectUnknown)
            {
                DBGERROR("collection not found.");
                returnValue = RC_COLL_NOT_FOUND;
            }
            else
            {
                DBGERROR(obj.what());
                context->release();
                throw;
            }
        }
        catch (std::bad_alloc)
        {
            DBGERROR("cannot allocate memory.");
            context->release();
            throw;
        }
        catch (...)
        {
            DBGERROR("unspecific exception while opening collection.");
            context->release();
            throw;
        }

        if (returnValue == RC_OK_SOME_ELEMENTS)
        {
            // create the transfer iterator
            context->transferCollIter = context->transferColl->createIterator();
            context->transferCollIter->reset();

            // set typeName and typeStructure
            const CollectionType *collectionType = context->transferColl->getCollectionType();
            if (collectionType)
            {
                typeName = strdup(collectionType->getTypeName());
                typeStructure = collectionType->getTypeStructure();  // no copy !!!
                // set oid in case of a persistent collection
                if (context->transferColl->isPersistent())
                {
                    EOId eOId;
                    if (context->transferColl->getEOId(eOId))
                    {
                        oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                    }
                }
                DBGOK
            }
            else
            {
                DBGWARN("cannot obtain collection type information");
                typeName = strdup("");
                typeStructure = strdup("");
            }

            if (!context->transferCollIter->notDone())
            {
                DBGINFO("ok, result empty.");
                returnValue = RC_OK_NO_ELEMENTS;
                context->releaseTransferStructures();
            }
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }
    return returnValue;
}

// TODO: refactor, essentially same as getCollByName
unsigned short
ServerComm::getCollByOId(unsigned long callingClientId,
                         r_OId &oid, char *&typeName, char *&typeStructure, char *&collName)
{
    static constexpr unsigned short RC_OK_SOME_ELEMENTS = 0;
    static constexpr unsigned short RC_OK_NO_ELEMENTS = 1;
    static constexpr unsigned short RC_COLL_NOT_FOUND = 2;
    // TODO: this should be 1 as in other methods
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 3;

    DBGREQUEST("'get collection by OID', oid = " << oid);

    unsigned short returnValue = RC_OK_SOME_ELEMENTS;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        // delete old transfer collection/iterator
        context->releaseTransferStructures();

        // check type and existence of oid
        OId oidIf(oid.get_local_oid());
        OId::OIdType objType = oidIf.getType();
        if (objType == OId::MDDCOLLOID)
        {
            try
            {
                context->transferColl = MDDColl::getMDDCollection(oidIf);
            }
            catch (r_Error &obj)
            {
                if (obj.get_kind() == r_Error::r_Error_ObjectUnknown)
                {
                    DBGERROR("collection not found.");
                    returnValue = RC_COLL_NOT_FOUND;
                }
                else
                {
                    DBGERROR(obj.what());
                    context->release();
                    throw;
                }
            }
            catch (std::bad_alloc)
            {
                DBGERROR("cannot allocate memory.");
                context->release();
                throw;
            }
            catch (...)
            {
                DBGERROR("unspecific exception while opening collection.");
                context->release();
                throw;
            }

            if (returnValue == RC_OK_SOME_ELEMENTS)
            {
                collName = strdup(context->transferColl->getName());
                // create the transfer iterator
                context->transferCollIter = context->transferColl->createIterator();
                context->transferCollIter->reset();

                // set typeName and typeStructure
                const CollectionType *collectionType = context->transferColl->getCollectionType();
                if (collectionType)
                {
                    typeName = strdup(collectionType->getTypeName());
                    typeStructure = collectionType->getTypeStructure();  // no copy !!!
                    // set oid in case of a persistent collection
                    if (context->transferColl->isPersistent())
                    {
                        EOId eOId;
                        if (context->transferColl->getEOId(eOId))
                            oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                    }
                    DBGINFO("ok, " << context->transferColl->getCardinality() << " result(s).");
                }
                else
                {
                    DBGWARN("cannot obtain collection type information");
                    typeName = strdup("");
                    typeStructure = strdup("");
                }

                if (!context->transferCollIter->notDone())
                {
                    DBGINFO("ok, result empty.");
                    returnValue = RC_OK_NO_ELEMENTS;
                    context->releaseTransferStructures();
                }
            }
        }
        else
        {
            DBGERROR("oid does not belong to a collection object.");
            returnValue = RC_COLL_NOT_FOUND;
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }
    return returnValue;
}


unsigned short
ServerComm::getCollOIdsByName(unsigned long callingClientId,
                              const char *collName, char *&typeName, char *&typeStructure,
                              r_OId &oid, RPCOIdEntry *&oidTable, unsigned int &oidTableSize)
{
    static constexpr unsigned short RC_OK_SOME_ELEMENTS = 0;
    static constexpr unsigned short RC_OK_NO_ELEMENTS = 1;
    static constexpr unsigned short RC_COLL_NOT_FOUND = 2;
    // TODO: this should be 1 as in other methods
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 3;

    DBGREQUEST("'get collection OIds by name', name = " << collName << "'");

    unsigned short returnValue = RC_OK_SOME_ELEMENTS;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        std::unique_ptr<MDDColl> coll;
        try
        {
            coll.reset(MDDColl::getMDDCollection(collName));
            if (!coll->isPersistent())
            {
                DBGERROR("inserting into system collection is illegal.");
                throw r_Error(SYSTEM_COLLECTION_NOT_WRITABLE);
            }
        }
        catch (r_Error &err)
        {
            if (err.get_kind() == r_Error::r_Error_ObjectUnknown)
            {
                DBGERROR("collection not found.");
                returnValue = RC_COLL_NOT_FOUND;
            }
            else
            {
                DBGERROR(err.what());
                context->release();
                throw;
            }
        }
        catch (...)
        {
            DBGERROR("unspecific exception while opening collection.");
            context->release();
            throw;
        }

        if (returnValue == RC_OK_SOME_ELEMENTS)
        {
            // set typeName and typeStructure
            const CollectionType *collectionType = coll->getCollectionType();
            if (collectionType)
            {
                typeName = strdup(collectionType->getTypeName());
                typeStructure = collectionType->getTypeStructure();  // no copy !!!
                EOId eOId;
                if (coll->getEOId(eOId) == true)
                    oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
            }
            else
            {
                DBGWARN("cannot obtain collection type information");
                typeName = strdup("");
                typeStructure = strdup("");
            }

            if (coll->getCardinality())
            {
                oidTableSize = coll->getCardinality();
                oidTable = static_cast<RPCOIdEntry *>(mymalloc(sizeof(RPCOIdEntry) * oidTableSize));

                auto collIter = std::unique_ptr<MDDCollIter>(coll->createIterator());
                int i = 0;
                for (collIter->reset(); collIter->notDone(); collIter->advance(), i++)
                {
                    MDDObj *mddObj = collIter->getElement();
                    if (mddObj->isPersistent())
                    {
                        EOId eOId;
                        if (mddObj->getEOId(&eOId) == 0)
                            oidTable[i].oid = strdup(r_OId(eOId.getSystemName(), eOId.getBaseName(),
                                                           eOId.getOId()).get_string_representation());
                        else
                            oidTable[i].oid = strdup("");
                    }
                    else
                    {
                        oidTable[i].oid = strdup("");
                    }
                }
                DBGINFO("ok, " << oidTableSize << " result(s).");
            }
            else
            {
                DBGINFO("ok, result empty.");
                returnValue = RC_OK_NO_ELEMENTS;
            }
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }

    return returnValue;
}


unsigned short
ServerComm::getCollOIdsByOId(unsigned long callingClientId,
                             r_OId &oid, char *&typeName, char *&typeStructure,
                             RPCOIdEntry *&oidTable, unsigned int &oidTableSize, char *&collName)
{
    static constexpr unsigned short RC_OK_SOME_ELEMENTS = 0;
    static constexpr unsigned short RC_OK_NO_ELEMENTS = 1;
    static constexpr unsigned short RC_COLL_NOT_FOUND = 2;
    // TODO: this should be 1 as in other methods
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 3;

    DBGREQUEST("'get collection OIDs by OId', oid = " << oid);

    unsigned short returnValue = RC_OK_SOME_ELEMENTS;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        // check type and existence of oid
        OId oidIf(oid.get_local_oid());
        OId::OIdType objType = oidIf.getType();
        if (objType == OId::MDDCOLLOID)
        {
            std::unique_ptr<MDDColl> coll;
            try
            {
                coll.reset(MDDColl::getMDDCollection(oidIf));
                if (!coll->isPersistent())
                {
                    DBGERROR("inserting into system collection is illegal.");
                    throw r_Error(SYSTEM_COLLECTION_NOT_WRITABLE);
                }
            }
            catch (r_Error &err)
            {
                if (err.get_kind() == r_Error::r_Error_ObjectUnknown)
                {
                    DBGERROR("collection not found.");
                    returnValue = RC_COLL_NOT_FOUND;
                }
                else
                {
                    DBGERROR(err.what());
                    context->release();
                    throw;
                }
            }
            catch (...)
            {
                DBGERROR("unspecific exception while opening collection.");
                context->release();
                throw;
            }

            if (returnValue == RC_OK_SOME_ELEMENTS)
            {
                collName = strdup(coll->getName());
                const CollectionType *collectionType = coll->getCollectionType();
                if (collectionType)
                {
                    typeName = strdup(collectionType->getTypeName());
                    typeStructure = collectionType->getTypeStructure();  // no copy !!!
                    EOId eOId;
                    if (coll->getEOId(eOId) == true)
                        oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                }
                else
                {
                    DBGWARN("cannot obtain collection type information");
                    typeName = strdup("");
                    typeStructure = strdup("");
                }

                if (coll->getCardinality())
                {
                    oidTableSize = coll->getCardinality();
                    oidTable = static_cast<RPCOIdEntry *>(mymalloc(sizeof(RPCOIdEntry) * oidTableSize));

                    auto collIter = std::unique_ptr<MDDCollIter>(coll->createIterator());
                    int i = 0;
                    for (collIter->reset(); collIter->notDone(); collIter->advance(), i++)
                    {
                        MDDObj *mddObj = collIter->getElement();
                        if (mddObj->isPersistent())
                        {
                            EOId eOId;
                            if ((static_cast<MDDObj *>(mddObj))->getEOId(&eOId) == 0)
                                oidTable[i].oid = strdup(r_OId(eOId.getSystemName(), eOId.getBaseName(),
                                                               eOId.getOId()).get_string_representation());
                            else
                                oidTable[i].oid = strdup("");
                        }
                        else
                        {
                            oidTable[i].oid = strdup("");
                        }
                    }
                    DBGINFO("ok, " << oidTableSize << " result(s).");
                }
                else
                {
                    DBGINFO("ok, result empty.");
                    returnValue = RC_OK_NO_ELEMENTS;
                }
            }
        }
        else
        {
            returnValue = RC_COLL_NOT_FOUND; // oid does not belong to a collection object
            DBGERROR("not a collection oid: " << oid);
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::getMDDByOId(unsigned long callingClientId,
                        r_OId &oid, r_Minterval &mddDomain,
                        char *&typeName, char *&typeStructure, unsigned short &currentFormat)
{
    static constexpr unsigned short RC_MDD_NOT_FOUND = 2;
    static constexpr unsigned short RC_MDD_HAS_NO_TILES = 3;
    DBGREQUEST("'get MDD by OId', oid = " << oid);

    unsigned short returnValue = RC_OK;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();

        OId oidIf(oid.get_local_oid());
        OId::OIdType objType = oidIf.getType();
        if (objType == OId::MDDOID)
        {
            try
            {
                context->transferMDD = new MDDObj(oidIf);
            }
            catch (r_Error &obj)
            {
                if (obj.get_kind() == r_Error::r_Error_ObjectUnknown)
                {
                    DBGERROR("collection not found.");
                    returnValue = RC_MDD_NOT_FOUND;
                }
                else
                {
                    DBGERROR(obj.what());
                    context->release();
                    throw;
                }
            }
            catch (std::bad_alloc)
            {
                DBGERROR("cannot allocate memory.");
                context->release();
                throw;
            }
            catch (...)
            {
                DBGERROR("unspecified exception.");
                context->release();
                throw;
            }

            if (returnValue == RC_OK)
            {
                // convert the mdd to transfer to rpc data structures

                // initialize mddDomain to give it back
                mddDomain = context->transferMDD->getCurrentDomain();

                // initialize context fields
                // This is a hack. The mddObj is a part of context->transferDataIter and it will
                // not be deleted until the end of transaction, so storing raw pointers is safe.
                // FIXME: change context->transTiles type to vector< shared_ptr<Tile> >
                {
                    std::unique_ptr<vector<boost::shared_ptr<Tile>>> tiles(context->transferMDD->getTiles());
                    context->transTiles = new vector<Tile *>;
                    for (size_t i = 0; i < tiles->size(); ++i)
                        context->transTiles->push_back((*tiles)[i].get());
                }
                context->tileIter = new vector<Tile *>::iterator;
                *(context->tileIter) = context->transTiles->begin();

                // set typeName and typeStructure

                // old: typeName = strdup( context->transferMDD->getCellTypeName() ); not known for the moment being
                typeName = strdup("");
                // create a temporary mdd type for the moment being
                MDDType *mddType = new MDDDomainType(
                        "tmp", context->transferMDD->getCellType(), context->transferMDD->getCurrentDomain());
                TypeFactory::addTempType(mddType);

                typeStructure = mddType->getTypeStructure();  // no copy !!!
                if (context->transTiles->size())
                    currentFormat = (*(context->transTiles))[0]->getDataFormat();
                else
                    currentFormat = r_Array;

                // set oid in case of persistent MDD objects
                if (context->transferMDD->isPersistent())
                {
                    EOId eOId;
                    if (context->transferMDD->getEOId(&eOId) == 0)
                        oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                }

                if (context->transTiles->size() > 0)
                {
                    DBGINFO("ok, got " << context->transTiles->size() << " tile(s).");
                }
                else   // context->transTiles->size() == 0
                {
                    returnValue = RC_MDD_HAS_NO_TILES;
                    DBGERROR("no tiles in MDD object.");
                }
            }
        }
        else
        {
            returnValue = RC_MDD_NOT_FOUND; // oid does not belong to an MDD object
            DBGERROR("oid does not belong to an MDD object.");
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    context->totalRawSize = 0;
    context->totalTransferedSize = 0;
    return returnValue;
}

// -----------------------------------------------------------------------------------------
// Utility methods
// -----------------------------------------------------------------------------------------

unsigned short
ServerComm::aliveSignal(unsigned long client)
{
    static constexpr unsigned short RC_UPDATED = 1;
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'aliveSignal'");

    ClientTblElt *context = getClientContext(client);
    if (context)
    {
        // set the time of the client's last action to now
        context->lastActionTime = static_cast<unsigned long>(time(NULL));
        returnValue = RC_UPDATED;
        context->release();
        DBGOK
    }
    else
    {
        DBGERROR("client not registered.");
    }
    return returnValue;
}

unsigned short
ServerComm::getNewOId(unsigned long callingClientId,
                      unsigned short objType, r_OId &oid)
{
    static constexpr unsigned short OBJTYPE_MDD = 1;
    static constexpr unsigned short OBJTYPE_COLL = 2;
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'get new OId for " << (objType == 1 ? "MDD" : "collection") << " type'");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        EOId eOId;
        if (objType == OBJTYPE_MDD)
        {
            EOId::allocateEOId(eOId, OId::MDDOID);
        }
        else if (objType == OBJTYPE_COLL)
        {
            EOId::allocateEOId(eOId, OId::MDDCOLLOID);
        }
        else
        {
            DBGERROR("Invalid object type: " << objType)
            context->release();
            return RC_ERROR;
        }
        oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
        context->release();
        DBGOK
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::getObjectType(unsigned long callingClientId,
                          r_OId &oid, unsigned short &objType)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'get object type by OID', oid = " << oid);

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        OId oidIf(oid.get_local_oid());
        objType = oidIf.getType();
        if (objType == OId::INVALID)
        {
            DBGERROR("no type for this oid.");
            returnValue = RC_ERROR;
        }
        else
        {
            DBGINFO("type is " << (objType == 1 ? "MDD" : "collection") << "... ok.");
        }
        context->release();
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::getTypeStructure(unsigned long callingClientId,
                             const char *typeName, unsigned short typeType, char *&typeStructure)
{
    static constexpr unsigned short RC_NO_TA_OPEN = 1;
    static constexpr unsigned short RC_INVALID_TYPE = 2;

    // typeType argument can be one of these..
    static constexpr unsigned short TYPE_COLL = 1;
    static constexpr unsigned short TYPE_MDD  = 2;

    unsigned short returnValue = RC_OK;

    DBGREQUEST("'get type structure', type = '" << typeName);

    ClientTblElt *context = getClientContext(callingClientId);
    if (!context)
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    else if (!transactionActive)
    {
        DBGERROR("no transaction open.");
        returnValue = RC_NO_TA_OPEN;
    }
    else
    {
        const Type* mappedType = NULL;
        switch (typeType)
        {
            case TYPE_COLL: mappedType = TypeFactory::mapSetType(typeName); break;
            case TYPE_MDD:  mappedType = TypeFactory::mapMDDType(typeName); break;
            default:        returnValue = RC_INVALID_TYPE;                  break;
        }
        if (mappedType)
            typeStructure = mappedType->getTypeStructure(); // no copy
        else
            returnValue = RC_INVALID_TYPE;

        if (returnValue == RC_INVALID_TYPE)
            DBGERROR("unknown type.")
        else
            DBGOK
        context->release();
    }
    return returnValue;
}

unsigned short
ServerComm::setTransferMode(unsigned long callingClientId,
                            unsigned short format, const char *formatParams)
{
    DBGREQUEST("'set transfer mode', format = '" << format << "', params = '" << formatParams << "'");

    unsigned short returnValue = RC_OK;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        r_Data_Format fmt = static_cast<r_Data_Format>(format);
        if (context->transferFormatParams != NULL)
        {
            delete[] context->transferFormatParams;
            context->transferFormatParams = NULL;
            // revert the transfer format strictness
            context->exactFormat = 0;
        }
        if (formatParams != NULL)
        {
            context->transferFormatParams = new char[strlen(formatParams) + 1];
            strcpy(context->transferFormatParams, formatParams);
            // extract any occurrences of ``exactformat''
            context->clientParams->process(context->transferFormatParams);
        }
        context->transferFormat = fmt;
        context->release();
        DBGOK
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::setStorageMode(unsigned long callingClientId,
                           __attribute__((unused)) unsigned short format,
                           const char *formatParams)
{
    DBGREQUEST("'set storage mode', format = " << format << ", params = " << formatParams);

    unsigned short returnValue = RC_OK;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        r_Data_Format fmt = r_Array;
        if (context->storageFormatParams != NULL)
        {
            delete[] context->storageFormatParams;
            context->storageFormatParams = NULL;
        }
        if (formatParams != NULL)
        {
            context->storageFormatParams = new char[strlen(formatParams) + 1];
            strcpy(context->storageFormatParams, formatParams);
        }
        context->storageFormat = fmt;
        context->release();
        DBGOK
    }
    else
    {
        DBGERROR("client not registered.");
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

int
ServerComm::ensureTileFormat(__attribute__((unused)) r_Data_Format &hasFmt,
                             __attribute__((unused)) r_Data_Format needFmt,
                             __attribute__((unused)) const r_Minterval &dom,
                             __attribute__((unused)) const BaseType *type,
                             __attribute__((unused)) char *&data,
                             __attribute__((unused)) r_Bytes &size,
                             __attribute__((unused)) int repack,
                             __attribute__((unused)) int owner,
                             __attribute__((unused)) const char *params)
{
    int returnValue = RC_OK;
    return returnValue;
}
