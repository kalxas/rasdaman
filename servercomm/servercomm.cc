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

#include "config.h"
#include "servercomm.hh"
#include "cliententry.hh"
#include "accesscontrol.hh"
#include "rpcif.h"

#include "raslib/error.hh"
#include "raslib/minterval.hh"
#include "raslib/parseparams.hh"
#include "raslib/mddtypes.hh"
#include "raslib/basetype.hh"
#include "raslib/endian.hh"
#include "raslib/odmgtypes.hh"
#include "raslib/parseparams.hh"

#include "mddmgr/mddcoll.hh"
#include "mddmgr/mddcolliter.hh"
#include "mddmgr/mddobj.hh"
#include "tilemgr/tile.hh"
#include "storagemgr/sstoragelayout.hh"
#include "lockmgr/lockmanager.hh"

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

#include "server/rasserver_config.hh"
#include "reladminif/adminif.hh"
#include "reladminif/eoid.hh"
#include "relcatalogif/typefactory.hh"
#include "relcatalogif/basetype.hh"
#include "relcatalogif/mddtype.hh"
#include "relcatalogif/mdddomaintype.hh"
#include "relcatalogif/mdddimensiontype.hh"
#include "relcatalogif/settype.hh"
#include "relcatalogif/structtype.hh"
#include "mymalloc/mymalloc.h"

#include "common/util/timer.hh"

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
#include <cassert>     // for assert()
#include <mutex>

#ifdef ENABLE_PROFILING
#include <google/profiler.h>
#include <gperftools/heap-profiler.h>
#include <string>
#endif

using namespace std;

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

// --------------------------------------------------------------------------------
//                          static variables
// --------------------------------------------------------------------------------

ServerComm *ServerComm::serverCommInstance = 0;
ClientTblElt *ServerComm::clientTbl = 0;
std::mutex ServerComm::clientTblMutex;

// --------------------------------------------------------------------------------
//                          global variables
// --------------------------------------------------------------------------------

// Defined here

MDDColl *mddConstants = 0;              // used in QtMDD
ClientTblElt *currentClientTblElt = 0;  // used in QtMDDAccess and oql.yy

// Defined elsewhere

// defined in oql.yy
extern int yyparse(void *);
extern void yyreset();
extern QueryTree *parseQueryTree;
extern ParseInfo *parseError;
extern char *beginParseString;
extern char *iterParseString;
extern AccessControl accessControl;

// -----------------------------------------------------------------------------------------
/// start the gperftools profilers
#ifdef ENABLE_PROFILING
void startProfiler(std::string fileNameTemplate, bool cpuProfiler)
{
    {
        char tmpFileName[fileNameTemplate.size() + 1];
        strcpy(tmpFileName, fileNameTemplate.c_str());

        int fd = mkstemps(tmpFileName, 6);
        string tmpFile(tmpFileName);
        if (fd != -1)
        {
            remove(tmpFileName);
            if (cpuProfiler)
            {
                ProfilerStart(tmpFileName);
                LINFO << "CPU profiler file: " << tmpFile;
            }
            else
            {
                HeapProfilerStart(tmpFileName);
                LINFO << "Heap profiler file: " << tmpFile << ".????.heap";
            }

        }
        else
        {
            LERROR << "failed creating a temporary profiler file '" << tmpFile << "':" << strerror(errno);
        }
    }
}
#endif

// -----------------------------------------------------------------------------------------
// handle logging requests

#ifdef RASDEBUG
#define DBGREQUEST(msg)   NNLINFO << "Request: " << msg << "... ";
#define DBGOK             BLINFO  << "ok\n";
#define DBGINFO(msg)      BLINFO  << msg << "\n";
#define DBGINFONNL(msg)   BLINFO  << msg;
#define DBGERROR(msg)     BLERROR << "Error: " << msg << "\n";
#define DBGWARN(msg)      BLWARNING << "Warning: " << msg << "\n";
#else
std::stringstream requestStream;
#define DBGREQUEST(msg) { requestStream.str(""); requestStream.clear(); requestStream << "Request: " << msg << "... "; }
#define DBGOK           ; // nothing to log if release mode and all is ok
#define DBGINFO(msg)    ; // nothing to log if release mode and all is ok
#define DBGINFONNL(msg) ; // nothing to log if release mode and all is ok
#define DBGERROR(msg)   { NNLINFO << requestStream.str(); BLERROR << "Error: " << msg << "\n"; BLFLUSH; requestStream.str(""); requestStream.clear(); }
#define DBGWARN(msg)    { NNLINFO << requestStream.str(); BLWARNING << "Warning: " << msg << "\n"; BLFLUSH; requestStream.str(""); requestStream.clear(); }
#endif

// -----------------------------------------------------------------------------------------

ServerComm::ServerComm()
{
    assert(!serverCommInstance);
    serverCommInstance = this;
}

ServerComm::~ServerComm()
{
    serverCommInstance = NULL;
    delete admin, admin = NULL;
}

// quick hack function used when stopping server to abort transaction and close db
void
ServerComm::abortEveryThingNow()
{
    if (serverCommInstance && serverCommInstance->clientTbl)
    {
        serverCommInstance->clientTbl->transaction.abort();
        serverCommInstance->clientTbl->database.close();
    }
}

ClientTblElt *
ServerComm::getClientContext(std::uint32_t clientId, bool printErrors)
{
    const std::lock_guard<std::mutex> lock(clientTblMutex);
    if (clientTbl)
    {
        if (clientId == clientTbl->clientId)
        {
            return clientTbl;
        }
        else
        {
            if (printErrors) {
                DBGERROR("the request client id " << clientId
                         << " does not match the session client id "
                         << clientTbl->clientId)
            }
            return NULL;
        }
    }
    else
    {
        if (printErrors) {
            DBGERROR("client table is not initialized, i.e. no session client "
                     "id has been registered.");
        }
        return NULL;
    }
}

void
ServerComm::addClientTblEntry(ClientTblElt *context)
{
    const std::lock_guard<std::mutex> lock(clientTblMutex);
    assert(context && "Cannot register client: client context is NULL.");
    DBGREQUEST("'register client' " << context->clientId << ", type = "
               << (context->clientType == ClientType::Http ? "http" : "non-http"))
    clientTbl = context;
    DBGOK
    ServerComm::printServerStatus();   // quite verbose
}

unsigned short
ServerComm::deleteClientTblEntry(std::uint32_t clientId)
{
    const std::lock_guard<std::mutex> lock(clientTblMutex);
  
    DBGREQUEST("unregister client " << clientId)

    unsigned short returnValue = RC_OK;
    
    if (clientTbl && clientId == clientTbl->clientId)
    {
        // The transaction contained in the client table element is aborted here.
        // This is reasonable because at this point, the transaction is either
        // already committed (This is the case if an closeDB call arrives.
        // In this case, abort doesn't do anything harmful.) or the communication
        // has broken down before a commitTA or a abortTA (In this case this
        // function is called by the garbage collection and aborting the transaction
        // is advisable.).
        clientTbl->releaseTransferStructures();
        if (transactionActive == clientId)
        {
            DBGINFONNL("abort transaction... ")
            try
            {
                clientTbl->transaction.abort();
            }
            catch (r_Error &err)
            {
                DBGERROR(err.what())
            }
            catch (...)
            {
                DBGERROR("unspecific exception.")
            }
            transactionActive = 0;
        }

        // close the database if it isn't already closed (e.g. after connection breakdowns)
        if (strcmp(clientTbl->baseName, "none") != 0)
        {
            DBGINFONNL("close database... ")
            try
            {
                clientTbl->database.close();
            }
            catch (r_Error &err)
            {
                if (err.get_kind() != r_Error::r_Error_DatabaseClosed)
                {
                    returnValue = RC_ERROR;
                    DBGERROR(err.what())
                }
            }
            catch (...)
            {
                DBGERROR("unspecific exception.")
            }
            // reset database name
            delete[] clientTbl->baseName;
            clientTbl->baseName = new char[5];
            strcpy(clientTbl->baseName, "none");
        }

        delete clientTbl;
        clientTbl = NULL;
        DBGOK
    }
    else
    {
        DBGERROR("client not found.")
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

void
ServerComm::printServerStatus()
{
#ifdef RASDEBUG
    stringstream ct;
    if (clientTbl)
    {
        ct << "\n  Client table dump:"
           << "\n    Client ID      : " << clientTbl->clientId
           << "\n    Client location: " << (clientTbl->clientType == ClientType::Http ? "http" : "non-http")
           << "\n    User name      : " << clientTbl->userName
           << "\n    Database in use: " << clientTbl->baseName
           << "\n    Creation time  : " << ctime((time_t *)&clientTbl->creationTime)
           <<   "    MDD collection : " << clientTbl->transferColl
           << "\n    MDD iterator   : " << clientTbl->transferCollIter
           << "\n    Current PersMDD: " << clientTbl->assembleMDD
           << "\n    Current MDD    : " << clientTbl->transferMDD
           << "\n    Tile vector    : " << clientTbl->transTiles
           << "\n    Tile iterator  : " << clientTbl->tileIter
           << "\n    Block byte cntr: " << clientTbl->bytesToTransfer;
    }
    auto currentTime = time(NULL);

    LDEBUG << "\n-----------------------------------------------------------------------------"
           << "\nServer state information at " << ctime(&currentTime)
           << "\n  Transaction active.............: " << (transactionActive ? "yes" : "no")
           << "\n  Max. transfer buffer size......: " << configuration.getMaxTransferBufferSize() << " bytes"
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

    DBGREQUEST("'open DB', name = " << dbName << ", client " << callingClientId);

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
                throw r_Error(r_Error::r_Error_DatabaseOpen);
            }
            else
            {
                returnValue = RC_ERROR;
                DBGERROR(err.what())
            }
        }
    }
    else
    {
        DBGWARN("client not registered " << callingClientId);
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::closeDB(unsigned long callingClientId)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'close DB', client " << callingClientId);

    ClientTblElt *context = getClientContext(callingClientId, false);
    if (context)
    {
        context->releaseTransferStructures();
        try
        {
            // If the current transaction belongs to this client, abort it.
            if (transactionActive == callingClientId)
            {
                DBGINFONNL("Warning: transaction is open, aborting... ");
                context->transaction.abort();
                transactionActive = 0;
            }
            context->database.close();
            DBGOK

            // reset database name
            delete[] context->baseName;
            context->baseName = new char[5];
            strcpy(context->baseName, "none");
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
        catch (...)
        {
            returnValue = RC_ERROR;
            DBGERROR("unspecific exception.")
        }
    }
    else
    {
        // Usually happens when a rasserver segfaults/dies: the client still sends
        // a final closeDB, but not to the original rasserver, so it is not registered.
        // So it's best to keep this error silent.
        //
        DBGWARN("client not registered " << callingClientId);
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

// no client id as this method is only ever called from rasserver --createdatabase
unsigned short
ServerComm::createDB(char *name)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'create DB', name = " << name);

    auto tempDbIf = std::unique_ptr<DatabaseIf>(new DatabaseIf());
    try
    {
        const char *dbSchema = NULL;
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
        DBGERROR("unspecific exception.");
        throw;
    }
    return returnValue;
}

// no client id as this method is only ever called from rasserver --deldatabase
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

    DBGREQUEST("'begin TA', mode = " << (readOnly ? "read" : "write") << ", client " << callingClientId)

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        if (transactionActive)
        {
            DBGERROR("transaction already active.")
            returnValue = RC_ERROR;
        }
        else
        {
            context->releaseTransferStructures();
            try
            {
                transactionActive = callingClientId;
                context->transaction.begin(&context->database, readOnly);
                DBGOK
            }
            catch (r_Error &err)
            {
                DBGERROR(err.what())
                transactionActive = 0;
                throw;
            }
            catch (...)
            {
                DBGERROR("unspecific exception.")
                transactionActive = 0;
                throw;
            }
        }
    }
    else
    {
        DBGWARN("client not found " << callingClientId)
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::commitTA(unsigned long callingClientId)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'commit TA', client " << callingClientId);

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();
        try
        {
            context->transaction.commit();
            transactionActive = 0;
            reportExecutionTimes(context);
            DBGOK
        }
        catch (r_Error &err)
        {
            DBGERROR(err.what());
            throw;
        }
        catch (...)
        {
            DBGERROR("unspecific exception.")
            throw;
        }
    }
    else
    {
        DBGWARN("client not found " << callingClientId)
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::abortTA(unsigned long callingClientId)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'abort TA', client " << callingClientId);

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();
        try
        {
            context->transaction.abort();
            transactionActive = 0;
            reportExecutionTimes(context);
            DBGOK
        }
        catch (r_Error &err)
        {
            DBGERROR(err.what());
            throw;
        }
        catch (...)
        {
            DBGERROR("unspecific exception.");
            throw;
        }
    }
    else
    {
        DBGWARN("client not found " << callingClientId)
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

// only one transaction can be active per server, so just need to check the transactionActive
bool
ServerComm::isTAOpen(unsigned long)
{
    DBGREQUEST("'is TA open'");
    bool returnValue = transactionActive;
    DBGINFO((transactionActive ? "yes." : "no."));
    return returnValue;
}

// -----------------------------------------------------------------------------------------
// Execute rasql queries (select, update, insert)
// -----------------------------------------------------------------------------------------

#define HANDLE_PARSE_INFO(info) \
    ostringstream os; (info).printStatus(os); \
    BLERROR << os.str(); \
    context->releaseTransferStructures(); \
    returnStructure.errorNo = (info).getErrorNo(); \
    returnStructure.lineNo = (info).getLineNo(); \
    returnStructure.columnNo = (info).getColumnNo(); \
    returnStructure.token = strdup((info).getToken().c_str());

#define HANDLE_PARSING_ERROR { \
        if (!parseError) { \
            parseError = new ParseInfo(); \
            parseError->setErrorNo(PARSER_UNKNOWNERROR); \
        } \
        HANDLE_PARSE_INFO(*parseError) \
        delete parseError, parseError = NULL; \
        yyreset(); \
        returnValue = RC_PARSING_ERROR; }

#define RELEASE_DATA { \
        mddConstants = NULL; \
        parseQueryTree = 0; currentClientTblElt = 0; delete qtree; qtree = NULL; }

#define RELEASE_ALL_DATA { \
        context->releaseTransferStructures(); \
        RELEASE_DATA }

std::pair<std::string, std::string> ServerComm::getTypeNameStructure(ClientTblElt *context) const
{
    assert(context && context->transferData && !context->transferData->empty());
    assert(context->transferDataIter);
    QtData *data = **context->transferDataIter;
    assert(data);
    
    if (data->getDataType() == QT_MDD)
    {
        QtMDD *mddObj = static_cast<QtMDD *>(data);
        MDDType *mddType = NULL;
        if (context->transferData->size() > 1)
        {
            // if there are more than one MDD object then they possibly have different domains,
            // so create a dimension result type in this case which will fit for all of them
            mddType = new MDDDimensionType("tmp", mddObj->getCellType(), mddObj->getLoadDomain().dimension());
        }
        else
        {
            mddType = new MDDDomainType("tmp", mddObj->getCellType(), mddObj->getLoadDomain());
        }
        TypeFactory::addTempType(mddType);
        SetType *setType = new SetType("tmp", mddType);
        TypeFactory::addTempType(setType);
        return {setType->getTypeName(), setType->getTypeStructure()};
    }
    else
    {
        char *dataTypeStructure = data->getTypeStructure();
        
        std::string retTypeStructure;
        retTypeStructure.reserve(strlen(dataTypeStructure) + 6);
        retTypeStructure += "set<";
        retTypeStructure += dataTypeStructure;
        retTypeStructure += ">";
        free(dataTypeStructure);
        
        return {"", retTypeStructure};
    }
}

unsigned short ServerComm::handleExecuteQueryResult(ClientTblElt *context, unsigned short returnValue,
                                                    ExecuteQueryRes &returnStructure) const
{
    assert(context != NULL);
    static constexpr unsigned short RC_OK_MDD_ELEMENTS = 0;
    static constexpr unsigned short RC_OK_SCALAR_ELEMENTS = 1;
    static constexpr unsigned short RC_OK_NO_ELEMENTS = 2;
    
    context->evaluationTime = context->timer.elapsedMs();
    if (returnValue == RC_OK)
    {
        if (context->transferData)
        {
            // create the transfer iterator
            context->timer.restart();
            context->transferDataIter = new vector<QtData *>::iterator;
            *context->transferDataIter = context->transferData->begin();

            // set typeName and typeStructure of returnStructure
            if (!context->transferData->empty())
            {
                // The type of first result object is used to determine the type of the result collection.
                QtData *firstElement = **context->transferDataIter;
                if (!firstElement)
                {
                    BLERROR << "Internal error: result object is null.\n";
                    throw r_Error(INTERNALSERVERERROR); // Unexpected internal server error.
                }

                try
                {
                    auto typeNameStructure = getTypeNameStructure(context);
                    returnStructure.typeName = strdup(typeNameStructure.first.c_str());
                    returnStructure.typeStructure = strdup(typeNameStructure.second.c_str());
                }
                catch (...)
                {
                    BLERROR << "Error: failed setting type name and structure of result.\n";
                    throw;
                }

                // print result feedback; note it's not finalized here, but in endTransfer()
                {
                  BLINFO << "result type '" << returnStructure.typeStructure << "', "
                         << context->transferData->size() << " element(s)... ";
#ifdef RASDEBUG
                  BLINFO << "\n"; // more requests will be logged in this case, so add a newline
#endif
                  // checked in endTransfer() to finalize the print stmt above with transfer size
                  context->reportTransferedSize = true;
                }
                returnValue = firstElement->getDataType() == QT_MDD
                              ? RC_OK_MDD_ELEMENTS : RC_OK_SCALAR_ELEMENTS;
            }
            else // context->transferData.empty()
            {
                BLINFO << "ok, result is empty.\n";
                returnValue = RC_OK_NO_ELEMENTS;
                returnStructure.typeName = strdup("");
                returnStructure.typeStructure = strdup("");
            }
        }
        else // context->transferData == NULL
        {
            BLINFO << "ok, result is empty.\n";
            returnValue = RC_OK_NO_ELEMENTS;
        }
    }
    return returnValue;
}

bool ServerComm::parseQuery(const char *query)
{
    beginParseString = const_cast<char *>(query);
    iterParseString = const_cast<char *>(query);

    BLINFO << "parsing... ";
    yyreset();
    int parserRet = yyparse(0);
    return parserRet == 0;
}

unsigned short
ServerComm::executeQuery(unsigned long callingClientId,
                         const char *query, ExecuteQueryRes &returnStructure, bool insert
                         )
{
#ifdef ENABLE_PROFILING
    startProfiler("/tmp/rasdaman_query_select.XXXXXX.pprof", true);
    startProfiler("/tmp/rasdaman_query_select.XXXXXX.pprof", false);
#endif
    static constexpr unsigned short RC_OK_MDD_ELEMENTS = 0;
    static constexpr unsigned short RC_OK_SCALAR_ELEMENTS = 1;
    static constexpr unsigned short RC_OK_NO_ELEMENTS = 2;
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 3;
    static constexpr unsigned short RC_PARSING_ERROR = 5;
    static constexpr unsigned short RC_EXECUTION_ERROR = 5;
    unsigned short returnValue = RC_OK;

    NNLINFO << "Request: '" << query << "'... ";

    resetExecuteQueryRes(returnStructure);
    ClientTblElt *context = getClientContext(callingClientId, false);
    if (context)
    {
        context->totalTransferedSize = 0;
        context->totalRawSize = 0;
        context->timer.restart();
        mddConstants = context->transferColl; // assign the mdd constants collection to the global pointer (temporary)
        context->transferColl = NULL;
        currentClientTblElt = context;        // assign current client table element (temporary)

        context->releaseTransferStructures();

        QueryTree *qtree = new QueryTree();   // create a query tree object...
        parseQueryTree = qtree;               // ...and assign it to the global parse query tree pointer;

        if (parseQuery(query))
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
                BLINFO << "evaluating... ";
                if (!insert)
                    context->transferData = qtree->evaluateRetrieval();
                else
                    context->transferData = qtree->evaluateUpdate();
            }
            catch (ParseInfo &info)
            {
                HANDLE_PARSE_INFO(info)
                returnValue = RC_EXECUTION_ERROR;
            }
            catch (r_Error &err)
            {
                BLERROR << err.what() << "\n";
                RELEASE_ALL_DATA
                throw;
            }
            catch (std::bad_alloc &)
            {
                BLERROR << "Error: memory allocation failed.\n";
                RELEASE_ALL_DATA
                throw;
            }
            catch (...)
            {
                BLERROR << "Error: unspecific exception.\n";
                RELEASE_ALL_DATA
                throw;
            }
            
            try
            {
                returnValue = handleExecuteQueryResult(context, returnValue, returnStructure);
            }
            catch (...)
            {
                RELEASE_ALL_DATA
                throw;
            }
        }
        else
        {
            HANDLE_PARSING_ERROR
        }

        RELEASE_DATA
        if (returnValue >= RC_OK_NO_ELEMENTS)
        {
            // release transfer structures on error, as the client will not call endTransfer()
            context->releaseTransferStructures();
        }
    }
    else
    {
        if (clientTbl)
        {
            BLERROR << "the request client id " << callingClientId
                    << " does not match the session client id "
                    << clientTbl->clientId << "\n";
        }
        else
        {
            BLERROR << "client table is not initialized, i.e. no session client "
                       "id has been registered.\n";
        }
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
            DBGOK
        }
        catch (r_Error &err)
        {
            DBGERROR(err.what());
            throw;
        }
        catch (...)
        {
            DBGERROR("unspecific exception while creating transient collection.");
            throw;
        }
    }
    else
    {
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}


unsigned short
ServerComm::executeUpdate(unsigned long callingClientId,
                          const char *query, ExecuteUpdateRes &returnStructure)
{
#ifdef ENABLE_PROFILING
    startProfiler("/tmp/rasdaman_query_update.XXXXXX.pprof", true);
    startProfiler("/tmp/rasdaman_query_update.XXXXXX.pprof", false);
#endif
    static constexpr unsigned short RC_OK_NO_ELEMENTS = RC_OK;
    static constexpr unsigned short RC_PARSING_ERROR = 2;
    static constexpr unsigned short RC_EXECUTION_ERROR = 3;
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 3;
    unsigned short returnValue = RC_OK;

    NNLINFO << "Request: '" << query << "'... ";

    resetExecuteUpdateRes(returnStructure);
    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->totalTransferedSize = 0;
        context->totalRawSize = 0;
        context->timer.restart();
        context->evaluationTime = 0;

        mddConstants = context->transferColl; // assign the mdd constants collection to the global pointer (temporary)
        currentClientTblElt = context;        // assign current client table element (temporary)

        QueryTree *qtree = new QueryTree();   // create a query tree object...
        parseQueryTree = qtree;               // ...and assign it to the global parse query tree pointer;

        if (parseQuery(query))
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
                BLINFO << "evaluating... ";
                vector<QtData *> *updateResult = qtree->evaluateUpdate();
                // release data
                for (auto *iter : *updateResult)
                {
                    delete iter, iter = NULL;
                }
                delete updateResult, updateResult = NULL;
                
                context->evaluationTime = context->timer.elapsedMs();
                context->timer.restart();
                context->reportTransferedSize = true;
#ifdef RASDEBUG
                BLINFO << "\n";
#endif
            }
            catch (ParseInfo &info)
            {
                HANDLE_PARSE_INFO(info)
                returnValue = RC_EXECUTION_ERROR;
            }
            catch (r_Error &err)
            {
                BLERROR << err.what() << "\n";
                RELEASE_ALL_DATA
                throw;
            }
            catch (std::bad_alloc &ex)
            {
                BLERROR << "Error: memory allocation failed.\n";
                RELEASE_ALL_DATA
                throw;
            }
            catch (...)
            {
                BLERROR << "Error: unspecific exception.\n";
                RELEASE_ALL_DATA
                throw;
            }
        }
        else
        {
            HANDLE_PARSING_ERROR
        }
        RELEASE_ALL_DATA
    }
    else
    {
        if (clientTbl)
        {
            BLERROR << "the request client id " << callingClientId
                    << " does not match the session client id "
                    << clientTbl->clientId << "\n";
        }
        else
        {
            BLERROR << "client table is not initialized, i.e. no session client "
                       "id has been registered.\n";
        }
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }

#ifdef ENABLE_PROFILING
    ProfilerStop();
    HeapProfilerStop();
#endif

    return returnValue;
}

unsigned short
ServerComm::executeInsert(unsigned long callingClientId,
                          const char *query, ExecuteQueryRes &returnStructure)
{
    return executeQuery(callingClientId, query, returnStructure, true);
}

// -----------------------------------------------------------------------------------------
// Insert MDD / tile
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
                    {
                        throw r_Error(SYSTEM_COLLECTION_NOT_WRITABLE);
                    }
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
                        throw;
                    }
                }
                catch (...)
                {
                    DBGERROR("unspecific exception while opening collection.");
                    throw;
                }

                // check MDD and collection type for compatibility

                if (!mddType->compatibleWithDomain(&domain))
                {
                    DBGERROR("MDD type not compatible wrt. its domain: " << domain);
                    context->transferColl->releaseAll();
                    delete context->transferColl;
                    context->transferColl = 0;
                    return RC_INVALID_MDDTYPE;
                }
                if (!context->transferColl->getCollectionType()->compatibleWith(mddType))
                {
                    DBGERROR("incompatible MDD and collection types.");
                    context->transferColl->releaseAll();
                    delete context->transferColl;
                    context->transferColl = 0;
                    return RC_INCOMPATIBLE_MDDTYPE;
                }

                // Create persistent MDD for further tile insertions

                StorageLayout ms;
                ms.setTileSize(StorageLayout::DefaultTileSize);
                ms.setIndexType(StorageLayout::DefaultIndexType);
                ms.setTilingScheme(StorageLayout::DefaultTilingScheme);
                if (domain.dimension() == StorageLayout::DefaultTileConfiguration.dimension())
                {
                    ms.setTileConfiguration(StorageLayout::DefaultTileConfiguration);
                }
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
                catch (std::bad_alloc &)
                {
                    DBGERROR("memory allocation failed.");
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
    }
    else
    {
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

    DBGREQUEST("'insert transient MDD', type '"
               << typeName << "', domain " << domain << ", cell length " << typeLength << ", "
               << domain.cell_count() * typeLength << " bytes")

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        DBGINFONNL("transferFormat = " << context->transferFormat << ", exactFormat = " << context->exactFormat << "... ");

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
    }
    else
    {
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
        try
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
            DBGOK
        }
        catch (...)
        {
            DBGERROR("unspecific exception.")
            throw;
        }
    }
    else
    {
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::insertTile(unsigned long callingClientId,
                       bool isPersistent, RPCMarray *rpcMarray, r_Minterval *tileSize)
{
    static constexpr unsigned short RC_TILE_FORMAT_ERROR = 1;
    static constexpr unsigned short RC_BASETYPE_MISMATCH = 3;
    assert(rpcMarray);

    unsigned short returnValue = RC_OK;

    DBGREQUEST("'insert tile', persistent = " << isPersistent);

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        const BaseType *baseType =
            isPersistent ? context->assembleMDD->getCellType() : context->transferMDD->getCellType();
        if (baseType)
        {
            r_Minterval domain(rpcMarray->domain);

            // reset data area from rpc structure so that it is not deleted by the Tile
            char *dataPtr = rpcMarray->data.confarray_val;
            rpcMarray->data.confarray_val = NULL;

            auto dataSize = static_cast<r_Bytes>(rpcMarray->data.confarray_len);
            rpcMarray->data.confarray_len = 0;

            auto dataFmt = static_cast<r_Data_Format>(rpcMarray->storageFormat);
            auto currFmt = static_cast<r_Data_Format>(rpcMarray->currentFormat);
            DBGINFONNL("storage format: " << dataFmt << ", transfer format: " << currFmt << "... ");

            // store in specified storage format; use (new) current format afterwards
            const int repack = 0;
            const int owner = 1;
            if (ensureTileFormat(currFmt, dataFmt, domain, baseType, dataPtr, dataSize, repack, owner,
                                 context->storageFormatParams) != RC_OK)
            {
                DBGERROR("invalid tile format.");
                return RC_TILE_FORMAT_ERROR;
            }
            // create tile to be inserted
            r_Bytes newDataSize = currFmt == r_Array ? 0 : dataSize;
            dataFmt = r_Array;
            try
            {
                std::unique_ptr<Tile> tile(new Tile(domain, baseType, true, dataPtr, newDataSize, dataFmt));

                // for java clients only: check endianness and swap bytes tile if necessary
                if (context->needEndianessSwap())
                {
                    DBGINFONNL("big-endian client so changing result endianness... ");
                    // we have to swap the endianess
                    auto tpstruct = baseType->getTypeStructure();
                    auto useType = std::unique_ptr<r_Base_Type>(
                                       static_cast<r_Base_Type *>(r_Type::get_any_type(tpstruct)));
                    char *newContents = static_cast<char *>(mymalloc(tile->getSize()));
                    // change the endianness of the entire tile for identical domains for src and dest
                    r_Endian::swap_array(useType.get(), domain, domain, tile->getContents(), newContents);
                    // set new swapped contents
                    free(tile->getContents());
                    tile->setContents(newContents);
                }

                // split the tile if necessary
                DBGINFONNL("inserting tile, " << dataSize << " bytes... ")
                std::unique_ptr<vector<Tile *>> tileSet;
                if (tileSize)
                {
                    tileSet.reset(tile->splitTile(*tileSize));
                }
                else
                {
                    tileSet.reset(new vector<Tile *> {tile.get()});
                    tile.release(); // don't destroy tile in this case
                }

                // insert tile
                for (auto it = tileSet->begin(); it != tileSet->end(); it++)
                {
                    if (isPersistent)
                    {
                        context->assembleMDD->insertTile(*it);
                    }
                    else
                    {
                        context->transferMDD->insertTile(*it);
                    }
                }
                DBGOK
            }
            catch (std::bad_alloc &ex)
            {
                DBGERROR("failed allocating " << dataSize << " bytes.")
                throw;
            }
            catch (...)
            {
                DBGERROR("unspecific exception.")
                throw;
            }
        }
        else
        {
            DBGERROR("tile and MDD base type do not match.");
            returnValue = RC_BASETYPE_MISMATCH;
        }
    }
    else
    {
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

// -----------------------------------------------------------------------------------------
// Fetch query results: next MDD, scalar, tile
// -----------------------------------------------------------------------------------------

unsigned short
ServerComm::getNextMDD(unsigned long callingClientId,
                       r_Minterval &mddDomain, std::string &typeName, std::string &typeStructure,
                       r_OId &oid, unsigned short &currentFormat)
{
    static constexpr unsigned short RC_COLL_EMPTY = 1;
    static constexpr unsigned short RC_MDD_EMPTY = 2;
    static constexpr unsigned short RC_NO_COLL = 2;
    // TODO: this should be 1 as in other methods
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 2;

    unsigned short returnValue = RC_OK;
    DBGREQUEST("'get next MDD'")

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        if (context->transferData && context->transferDataIter &&
           *context->transferDataIter != context->transferData->end())
        {
            try
            {
                // get the MDD object to be transferred
                QtMDD *mddData = static_cast<QtMDD *>(**context->transferDataIter);
                MDDObj *mddObj = mddData->getMDDObject();
                const BaseType *baseType = mddObj->getCellType();
                // set output parameter mddDomain
                mddDomain = mddData->getLoadDomain();
                DBGINFONNL("domain " << mddDomain << ", cell length " << baseType->getSize() << "... ");

                // initialize tiles to transfer
                context->transTiles = new vector<Tile *>();
                bool differentDomains = mddObj->getCurrentDomain() != mddDomain;

                // If the load domain is different from the current domain, we have a persistent MDD object.
                // The border tiles have to be cut (and therefore copied) in order to be ready for transferring them.
                // These temporary border tiles are added to the deletableTiles list which is deleted at the end.
                //
                // This is a hack. The mddObj is a part of context->transferDataIter and it will
                // not be deleted until the end of transaction, so storing raw pointers is safe.
                auto tiles = differentDomains ? mddObj->intersect(mddDomain) : mddObj->getTiles();
                // FIXME: change context->transTiles type to vector< shared_ptr<Tile> >
                for (size_t i = 0; i < tiles->size(); ++i)
                {
                    context->transTiles->push_back((*tiles)[i].get());
                }
                delete tiles, tiles = NULL;

                if (differentDomains)
                {
                    if (!(context->deletableTiles))
                    {
                        context->deletableTiles = new vector<Tile *>();
                    }
                    for (auto it = context->transTiles->begin(); it != context->transTiles->end(); it++)
                    {
                        // get relevant domain of source tile
                        r_Minterval relevantDomain(mddDomain.create_intersection((*it)->getDomain()));
                        if (relevantDomain != (*it)->getDomain())
                        {
                            // create a new transient tile and copy the transient data
                            Tile *newTransTile = new Tile(relevantDomain, baseType);
                            newTransTile->copyTile(relevantDomain, *it, relevantDomain);
                            // replace the tile in the list with the new one
                            *it = newTransTile;
                            // delete later by adding to this list of tiles
                            context->deletableTiles->push_back(newTransTile);
                        }
                    }
                }

                // initialize tile iterator
                context->tileIter = new vector<Tile *>::iterator;
                *(context->tileIter) = context->transTiles->begin();

                // set output parameters typeName and typeStructure
                typeName = ""; // no type name
                MDDType *mddType = new MDDDomainType("tmp", baseType, mddDomain);
                TypeFactory::addTempType(mddType);
                typeStructure = mddType->getTypeStructure();

                // set output parameter currentFormat
                if (!context->transTiles->empty())
                    currentFormat = (*(context->transTiles))[0]->getDataFormat();
                else
                    currentFormat = r_Array;

                // set output parameter oid in case of persistent MDD objects
                if (mddObj->isPersistent())
                {
                    EOId eOId;
                    if (mddObj->getEOId(&eOId) == 0)
                        oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                }
            }
            catch (r_Error &err)
            {
                DBGERROR(err.what());
                throw;
            }
            catch (std::bad_alloc &ex)
            {
                DBGERROR("memory allocation failed.");
                throw;
            }
            catch (...)
            {
                DBGERROR("unspecified exception.");
                throw;
            }

            if (context->transTiles->size() > 0)
            {
                DBGINFO("ok, " << context->transTiles->size() << " more tile(s)");
            }
            else // context->transTiles->size() == 0
            {
                returnValue = RC_MDD_EMPTY;
                DBGWARN("no tiles in MDD object.");
            }
        }
        else if (context->transferDataIter &&
                 *(context->transferDataIter) == context->transferData->end())
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
    else
    {
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }

    return returnValue;
}

void
ServerComm::swapScalarElement(char *buffer, const BaseType *baseType)
{
    assert(baseType);
    switch (baseType->getType())
    {
    case USHORT:
    {
        auto *buf = reinterpret_cast<r_UShort *>(buffer);
        *buf = r_Endian::swap(*buf);
        break;
    }
    case SHORT:
    {
        auto *buf = reinterpret_cast<r_Short *>(buffer);
        *buf = r_Endian::swap(*buf);
        break;
    }
    case LONG:
    {
        auto *buf = reinterpret_cast<r_Long *>(buffer);
        *buf = r_Endian::swap(*buf);
        break;
    }
    case ULONG:
    {
        auto *buf = reinterpret_cast<r_ULong *>(buffer);
        *buf = r_Endian::swap(*buf);
        break;
    }
    case FLOAT:
    {
        auto *buf = reinterpret_cast<r_Float *>(buffer);
        *buf = r_Endian::swap(*buf);
        break;
    }
    case DOUBLE:
    {
        auto *buf = reinterpret_cast<r_Double *>(buffer);
        *buf = r_Endian::swap(*buf);
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
            swapScalarElement(buffer, bt);
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

    DBGREQUEST("'get next element'");

    unsigned short returnValue = RC_OK;

    buffer = NULL;
    bufferSize = 0;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        if (!context->transferData || !context->transferDataIter)
        {
            returnValue = RC_NO_COLL;
            DBGWARN("no transfer collection.")
        }
        else if (*context->transferDataIter != context->transferData->end())
        {
            // Buffer is allocated and has to be freed by the caller using free().
            try
            {
                QtData *dataObj = **(context->transferDataIter);
                switch (dataObj->getDataType())
                {
                case QT_STRING:
                {
                    QtStringData *stringDataObj = static_cast<QtStringData *>(dataObj);
                    bufferSize = stringDataObj->getStringData().length() + 1;
                    buffer = new char [bufferSize];
                    memcpy(buffer, stringDataObj->getStringData().c_str(), bufferSize);
                    DBGINFONNL("string data of size " << bufferSize << "... ")
                    break;
                }
                case QT_INTERVAL:
                {
                    QtIntervalData *tmp = static_cast<QtIntervalData *>(dataObj);
                    auto s = tmp->getIntervalData().to_string();
                    bufferSize = s.size() + 1;
                    buffer = new char [bufferSize];
                    memcpy(buffer, s.c_str(), bufferSize);
                    DBGINFONNL("interval data of size " << bufferSize << "... ")
                    break;
                }
                case QT_MINTERVAL:
                {
                    QtMintervalData *tmp = static_cast<QtMintervalData *>(dataObj);
                    auto s = tmp->getMintervalData().to_string();
                    bufferSize = s.size() + 1;
                    buffer = new char [bufferSize];
                    memcpy(buffer, s.c_str(), bufferSize);
                    DBGINFONNL("minterval data of size " << bufferSize << "... ")
                    break;
                }
                case QT_POINT:
                {
                    QtPointData *tmp = static_cast<QtPointData *>(dataObj);
                    auto s = tmp->getPointData().to_string();
                    bufferSize = s.size() + 1;
                    buffer = new char [bufferSize];
                    memcpy(buffer, s.c_str(), bufferSize);
                    DBGINFONNL("point data of size " << bufferSize << "... ")
                    break;
                }
                default:
                {
                    if (dataObj->isScalarData())
                    {
                        QtScalarData *scalarDataObj = static_cast<QtScalarData *>(dataObj);
                        bufferSize = scalarDataObj->getValueType()->getSize();
                        buffer = new char [bufferSize];
                        memcpy(buffer, scalarDataObj->getValueBuffer(), bufferSize);
                        // change endianess if necessary
                        if (context->needEndianessSwap())
                        {
                            swapScalarElement(buffer, scalarDataObj->getValueType());
                        }
                        DBGINFONNL("scalar data of size " << bufferSize << "... ")
                    }
                    break;
                }
                }
                context->totalTransferedSize += bufferSize;
            }
            catch (r_Error &err)
            {
                DBGERROR(err.what());
                throw;
            }
            catch (...)
            {
                DBGERROR("unknown exception.")
                throw;
            }

            // increment list iterator
            (*(context->transferDataIter))++;
            if (*(context->transferDataIter) != context->transferData->end())
            {
                DBGINFO("ok, some more element(s) left.");
            }
            else
            {
                returnValue = RC_COLL_EMPTY;
                DBGINFO("ok, no more elements.");
            }
        }
        else
        {
            returnValue = RC_COLL_EMPTY;
            DBGINFO("nothing left... ok.");
            context->releaseTransferStructures();
        }
    }
    else
    {
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

    DBGREQUEST("'get next tile'")

    unsigned short returnValue = RC_OK;

    // initialize the output parameter for failure cases
    *rpcMarray = NULL;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        if (context->transTiles && context->tileIter)
        {
            unsigned short statusValue = 0;
            try
            {
                if (context->bytesToTransfer == 0 && context->encodedData != NULL)
                {
                    delete [] (char*)context->encodedData;
                    context->encodedData = NULL;
                    context->encodedSize = 0;
                }

                Tile *resultTile = **context->tileIter;

                // allocate memory for the output parameter rpcMarray and set fields
                *rpcMarray = new RPCMarray();
                (*rpcMarray)->currentFormat = resultTile->getDataFormat();
                (*rpcMarray)->cellTypeLength = resultTile->getType()->getSize();
                (*rpcMarray)->domain = resultTile->getDomain().get_string_representation();
                (*rpcMarray)->storageFormat = r_Array;

                unsigned long transferOffset = 0;
                char *useTransData = resultTile->getContents();
                unsigned long totalSize = resultTile->getSize();
                auto transferSize = totalSize;
                const auto maxTransferBufferSize = size_t(configuration.getMaxTransferBufferSize());
                if (totalSize > maxTransferBufferSize)
                {
                    // if there is the rest of a tile to transfer, do it!
                    if (context->bytesToTransfer > 0)
                    {
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

                (*rpcMarray)->data.confarray_len = static_cast<unsigned int>(transferSize);
                (*rpcMarray)->data.confarray_val = useTransData + transferOffset;

                DBGINFONNL("domain " << resultTile->getDomain() << ", " << transferSize << " bytes... ");

                // increment iterator only if tile is transferred completely
                if (statusValue > ST_MORE_BLOCKS)
                {
                    context->totalRawSize += resultTile->getSize();
                    (*context->tileIter)++;
                }
            }
            catch (r_Error &err)
            {
                DBGERROR(err.what());
                throw;
            }
            catch (std::bad_alloc &ex)
            {
                DBGERROR("failed allocating memory.")
                throw;
            }
            catch (...)
            {
                DBGERROR("unknown exception.")
                throw;
            }

            // delete tile vector and increment transfer collection iterator if tile iterator is exhausted
            if (*context->tileIter == context->transTiles->end())
            {
                // delete tile vector transTiles (tiles are deleted when the object is deleted)
                delete context->transTiles, context->transTiles = NULL;
                delete context->tileIter, context->tileIter = NULL;

                if (context->transferDataIter)
                {
                    (*context->transferDataIter)++;
                    if (*context->transferDataIter != context->transferData->end())
                    {
                        returnValue = RC_OK_MORE_MDDS;
                        DBGINFO("ok, some MDD(s) left.");
                    }
                    else
                    {
                        // Memory of last tile is still needed for the last byte transfer,
                        // therefore, do not release memory now but with endTransfer()
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
        else
        {
            returnValue = RC_NO_COLL;
            DBGERROR("no transfer collection or nothing left in collection.");
        }
    }
    else
    {
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::endTransfer(unsigned long client)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'endTransfer'")

    ClientTblElt *context = getClientContext(client);
    if (context)
    {
        context->releaseTransferStructures();
        reportExecutionTimes(context);
    }
    else
    {
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

void ServerComm::reportExecutionTimes(ClientTblElt *context)
{
    if (context->evaluationTime > 0)
    {
#ifdef RASDEBUG
        DBGINFO("ok, evaluation time " << context->evaluationTime
                << " ms, transfer time " << context->timer.elapsedMs()
                << " ms, transfer size " << context->totalTransferedSize << " bytes.\n");
        context->evaluationTime = 0;
        context->totalTransferedSize = 0;
#else
        if (context->reportTransferedSize)
        {
            BLINFO << "ok, evaluation time " << context->evaluationTime
                   <<" ms, transfer time " << context->timer.elapsedMs()
                   << " ms, transfer size " << context->totalTransferedSize << " bytes.\n";
            context->evaluationTime = 0;
            context->totalTransferedSize = 0;
        }
#endif
    }
}

// -----------------------------------------------------------------------------------------
// Collection mgmt, used by the rasodmg C++ API
// -----------------------------------------------------------------------------------------

unsigned short
ServerComm::insertColl(unsigned long callingClientId,
                       const char *collName, const char *typeName, r_OId &oid)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'insert collection', collection name = '" << collName << "', type = '" << typeName << "'");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();

        const SetType *collType = TypeFactory::mapSetType(typeName);
        if (collType)
        {
            try
            {
                MDDColl *coll = MDDColl::createMDDCollection(collName, OId(oid.get_local_oid()), collType);
                delete coll;
                BLINFO << "ok\n";
            }
            catch (r_Error &obj)
            {
                DBGERROR(obj.what())
                throw;
            }
            catch (...)
            {
                DBGERROR("unspecific exception while creating collection.")
                throw;
            }
        }
        else
        {
            DBGERROR("unknown collection type: '" << typeName << "'.");
            returnValue = RC_ERROR;
        }
    }
    else
    {
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
            throw;
        }
        catch (...)
        {
            DBGERROR("unspecific exception while dropping collection.")
            throw;
        }
    }
    else
    {
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::deleteObjByOId(unsigned long callingClientId,
                           r_OId &oid)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'delete object by OID', oid = '" << oid << "'");

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
            // There's no API in MDDObj to remove the object, it has to be removed
            // from the collection that contains it. The collection is not known though,
            // as only the OID is given.
            DBGINFO("found MDD object; not deleted yet... ok");
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
                throw;
            }
            catch (...)
            {
                DBGERROR("unspecific exception while dropping collection.")
                throw;
            }
            break;
        }
        default:
        {
            DBGERROR("object has unknown type '" << objType << "'.")
            returnValue = RC_ERROR;
        }
        }
    }
    else
    {
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::removeObjFromColl(unsigned long callingClientId,
                              const char *collName, r_OId &oid)
{
    unsigned short returnValue = RC_OK;

    DBGREQUEST("'remove MDD from collection', collection name = '" << collName << "', oid = '" << oid << "'");

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        context->releaseTransferStructures();

        std::unique_ptr<MDDColl> coll;
        try
        {
            coll.reset(MDDColl::getMDDCollection(collName));
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
                throw;
            }
        }
        catch (...)
        {
            DBGERROR("unspecified exception.");
            throw;
        }
        if (coll && coll->isPersistent())
        {
            OId mddId(oid.get_local_oid());
            OId collId;
            coll->getOId(collId);
            try
            {
                if (MDDColl::removeMDDObject(collId, mddId))
                {
                    DBGOK
                }
                else
                {
                    DBGERROR("object does not exist.")
                    returnValue = RC_ERROR;
                }
            }
            catch (r_Error &obj)
            {
                DBGERROR(obj.what());
                throw;
            }
            catch (...)
            {
                DBGERROR("unspecified exception.")
                throw;
            }
        }
    }
    else
    {
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

// -----------------------------------------------------------------------------------------
// Get collection/MDD by name or oid
// -----------------------------------------------------------------------------------------

unsigned short ServerComm::getTransferCollInfo(
    ClientTblElt *context, r_OId &oid, std::string &typeName, std::string &typeStructure, MDDColl *coll) const
{
    static constexpr unsigned short RC_OK_SOME_ELEMENTS = 0;
    static constexpr unsigned short RC_OK_NO_ELEMENTS = 1;
    assert(context && coll);

    // set typeName and typeStructure
    const CollectionType *collectionType = coll->getCollectionType();
    if (collectionType)
    {
        typeName = collectionType->getTypeName();
        typeStructure = collectionType->getTypeStructure();  // no copy !!!
        if (coll->isPersistent())
        {
            EOId eOId;
            if (coll->getEOId(eOId))
            {
                oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
            }
        }
        DBGINFO("ok, " << coll->getCardinality() << " result(s).");
    }
    else
    {
        DBGINFONNL("Warning: cannot obtain collection type information... ")
        typeName = strdup("");
        typeStructure = strdup("");
    }

    if (coll->getCardinality() > 0)
    {
        DBGINFO("ok, collection has " << coll->getCardinality() << " elements.")
        return RC_OK_SOME_ELEMENTS;
    }
    else
    {
        DBGINFO("ok, collection empty.");
        context->releaseTransferStructures();
        return RC_OK_NO_ELEMENTS;
    }
}

unsigned short
ServerComm::getCollByName(unsigned long callingClientId,
                          const char *collName, std::string &typeName, std::string &typeStructure, r_OId &oid)
{
    static constexpr unsigned short RC_OK_SOME_ELEMENTS = 0;
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

            // create the transfer iterator
            context->transferCollIter = context->transferColl->createIterator();
            context->transferCollIter->reset();
            returnValue = getTransferCollInfo(context, oid, typeName, typeStructure, context->transferColl);
        }
        catch (r_Error &obj)
        {
            if (obj.get_kind() == r_Error::r_Error_ObjectUnknown)
            {
                DBGERROR("collection not found.")
                returnValue = RC_COLL_NOT_FOUND;
            }
            else
            {
                DBGERROR(obj.what())
                throw;
            }
        }
        catch (std::bad_alloc &)
        {
            DBGERROR("memory allocation failed.");
            throw;
        }
        catch (...)
        {
            DBGERROR("unspecific exception while opening collection.");
            throw;
        }

    }
    else
    {
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }
    return returnValue;
}

// TODO: refactor, essentially same as getCollByName
unsigned short
ServerComm::getCollByOId(unsigned long callingClientId,
                         r_OId &oid, std::string &typeName, std::string &typeStructure, std::string &collName)
{
    static constexpr unsigned short RC_OK_SOME_ELEMENTS = 0;
    static constexpr unsigned short RC_COLL_NOT_FOUND = 2;
    // TODO: this should be 1 as in other methods
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 3;

    DBGREQUEST("'get collection by OID', oid = " << oid);

    unsigned short returnValue = RC_OK_SOME_ELEMENTS;

    collName = "";
    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        // delete old transfer collection/iterator
        context->releaseTransferStructures();

        // check type and existence of oid
        OId oidIf(oid.get_local_oid());
        OId::OIdType objType = oidIf.getType();
        if (objType != OId::MDDCOLLOID)
        {
            DBGERROR("not a collection oid: " << oid)
            return RC_COLL_NOT_FOUND;
        }

        try
        {
            context->transferColl = MDDColl::getMDDCollection(oidIf);

            collName = context->transferColl->getName();
            context->transferCollIter = context->transferColl->createIterator();
            context->transferCollIter->reset();
            returnValue = getTransferCollInfo(context, oid, typeName, typeStructure, context->transferColl);
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
                throw;
            }
        }
        catch (std::bad_alloc &ex)
        {
            DBGERROR("memory allocation failed.");
            throw;
        }
        catch (...)
        {
            DBGERROR("unspecific exception while opening collection.");
            throw;
        }
    }
    else
    {
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }
    return returnValue;
}


unsigned short
ServerComm::getCollOIdsByName(unsigned long callingClientId,
                              const char *collName, std::string &typeName, std::string &typeStructure,
                              r_OId &oid, RPCOIdEntry *&oidTable, unsigned int &oidTableSize)
{
    static constexpr unsigned short RC_OK_SOME_ELEMENTS = 0;
    static constexpr unsigned short RC_COLL_NOT_FOUND = 2;
    // TODO: this should be 1 as in other methods
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 3;

    DBGREQUEST("'get collection OIds by name', name = " << collName << "'");

    unsigned short returnValue = RC_OK_SOME_ELEMENTS;

    // init
    oidTable = NULL;
    oidTableSize = 0;

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

            returnValue = getTransferCollInfo(context, oid, typeName, typeStructure, coll.get());

            if (coll->getCardinality() > 0)
            {
                oidTableSize = coll->getCardinality();
                oidTable = new RPCOIdEntry[oidTableSize];

                auto collIter = std::unique_ptr<MDDCollIter>(coll->createIterator());
                collIter->reset();
                EOId eOId;
                for (size_t i = 0; collIter->notDone(); collIter->advance(), i++)
                {
                    MDDObj *mddObj = collIter->getElement();
                    oidTable[i].oid = NULL;
                    if (mddObj->isPersistent() && mddObj->getEOId(&eOId) == 0)
                    {
                        oidTable[i].oid = strdup(r_OId(eOId.getSystemName(), eOId.getBaseName(),
                                                       eOId.getOId()).get_string_representation());
                    }
                    if (!oidTable[i].oid)
                    {
                        oidTable[i].oid = strdup("");
                    }
                }
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
    }
    else
    {
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }

    return returnValue;
}


unsigned short
ServerComm::getCollOIdsByOId(unsigned long callingClientId,
                             r_OId &oid, std::string &typeName, std::string &typeStructure,
                             RPCOIdEntry *&oidTable, unsigned int &oidTableSize, std::string &)
{
    static constexpr unsigned short RC_OK_SOME_ELEMENTS = 0;
    static constexpr unsigned short RC_COLL_NOT_FOUND = 2;
    // TODO: this should be 1 as in other methods
    static constexpr unsigned short RC_CLIENT_CONTEXT_NOT_FOUND = 3;

    DBGREQUEST("'get collection OIDs by OId', oid = " << oid);

    unsigned short returnValue = RC_OK_SOME_ELEMENTS;

    // init
    oidTable = NULL;
    oidTableSize = 0;

    ClientTblElt *context = getClientContext(callingClientId);
    if (context)
    {
        // check type and existence of oid
        OId oidIf(oid.get_local_oid());
        OId::OIdType objType = oidIf.getType();
        if (objType != OId::MDDCOLLOID)
        {
            DBGERROR("not a collection oid: " << oid)
            return RC_COLL_NOT_FOUND;
        }

        std::unique_ptr<MDDColl> coll;
        try
        {
            coll.reset(MDDColl::getMDDCollection(oidIf));

            // TODO: refactor duplication with getCollOIdsByName
            if (!coll->isPersistent())
            {
                DBGERROR("inserting into system collection is illegal.");
                throw r_Error(SYSTEM_COLLECTION_NOT_WRITABLE);
            }

            returnValue = getTransferCollInfo(context, oid, typeName, typeStructure, coll.get());

            if (coll->getCardinality() > 0)
            {
                oidTableSize = coll->getCardinality();
                oidTable = new RPCOIdEntry[oidTableSize];

                auto collIter = std::unique_ptr<MDDCollIter>(coll->createIterator());
                collIter->reset();
                EOId eOId;
                for (size_t i = 0; collIter->notDone(); collIter->advance(), i++)
                {
                    MDDObj *mddObj = collIter->getElement();
                    oidTable[i].oid = NULL;
                    if (mddObj->isPersistent() && mddObj->getEOId(&eOId) == 0)
                    {
                        oidTable[i].oid = strdup(r_OId(eOId.getSystemName(), eOId.getBaseName(),
                                                       eOId.getOId()).get_string_representation());
                    }
                    if (!oidTable[i].oid)
                    {
                        oidTable[i].oid = strdup("");
                    }
                }
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
    }
    else
    {
        returnValue = RC_CLIENT_CONTEXT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::getMDDByOId(unsigned long callingClientId,
                        r_OId &oid, r_Minterval &mddDomain,
                        std::string &typeName, std::string &typeStructure, unsigned short &currentFormat)
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
                    throw;
                }
            }
            catch (std::bad_alloc &ex)
            {
                DBGERROR("memory allocation failed.");
                throw;
            }
            catch (...)
            {
                DBGERROR("unspecified exception.");
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
                    std::unique_ptr<vector<std::shared_ptr<Tile>>> tiles(context->transferMDD->getTiles());
                    context->transTiles = new vector<Tile *>;
                    for (size_t i = 0; i < tiles->size(); ++i)
                    {
                        context->transTiles->push_back((*tiles)[i].get());
                    }
                }
                context->tileIter = new vector<Tile *>::iterator;
                *(context->tileIter) = context->transTiles->begin();

                // set typeName and typeStructure

                // old: typeName = strdup( context->transferMDD->getCellTypeName() ); not known for the moment being
                typeName = "";
                // create a temporary mdd type for the moment being
                MDDType *mddType = new MDDDomainType(
                    "tmp", context->transferMDD->getCellType(), context->transferMDD->getCurrentDomain());
                TypeFactory::addTempType(mddType);

                typeStructure = mddType->getTypeStructure();  // no copy !!!
                if (context->transTiles->size())
                {
                    currentFormat = (*(context->transTiles))[0]->getDataFormat();
                }
                else
                {
                    currentFormat = r_Array;
                }

                // set oid in case of persistent MDD objects
                if (context->transferMDD->isPersistent())
                {
                    EOId eOId;
                    if (context->transferMDD->getEOId(&eOId) == 0)
                    {
                        oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                    }
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
    }
    else
    {
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
            return RC_ERROR;
        }
        oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
        DBGOK
    }
    else
    {
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
    }
    else
    {
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::getTypeStructure(unsigned long callingClientId,
                             const char *typeName, unsigned short typeType, std::string &typeStructure)
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
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    else if (!transactionActive)
    {
        DBGERROR("no transaction open.");
        returnValue = RC_NO_TA_OPEN;
    }
    else
    {
        const Type *mappedType = NULL;
        switch (typeType)
        {
        case TYPE_COLL:
            mappedType = TypeFactory::mapSetType(typeName);
            break;
        case TYPE_MDD:
            mappedType = TypeFactory::mapMDDType(typeName);
            break;
        default:
            returnValue = RC_INVALID_TYPE;
            break;
        }
        
        if (mappedType)
            typeStructure = mappedType->getTypeStructure();
        else
            returnValue = RC_INVALID_TYPE;

        if (returnValue == RC_INVALID_TYPE)
            DBGERROR("unknown type.")
        else
            DBGOK
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
        DBGOK
    }
    else
    {
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

unsigned short
ServerComm::setStorageMode(unsigned long callingClientId,
                           unsigned short format,
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
        DBGOK
    }
    else
    {
        returnValue = RC_CLIENT_NOT_FOUND;
    }
    return returnValue;
}

int
ServerComm::ensureTileFormat(r_Data_Format &,
                             r_Data_Format,
                             const r_Minterval &,
                             const BaseType *,
                             char *&,
                             r_Bytes &,
                             int,
                             int,
                             const char *)
{
    int status = RC_OK;
    return status;
}

void ServerComm::setAdmin(AdminIf *newAdmin)
{
    if (admin)
        delete admin;
    admin = newAdmin;
}

void ServerComm::resetExecuteQueryRes(ExecuteQueryRes &res)
{
    res.typeStructure = NULL;
    res.token = NULL;
    res.typeName = NULL;
    res.columnNo = 0;
    res.errorNo = 0;
    res.lineNo = 0;
    res.status = 0;
}

void ServerComm::resetExecuteUpdateRes(ExecuteUpdateRes &res)
{
    res.columnNo = 0;
    res.errorNo = 0;
    res.lineNo = 0;
    res.status = 0;
    res.token = NULL;
}

void ServerComm::cleanExecuteQueryRes(ExecuteQueryRes &res)
{
    if (res.typeStructure)
        free(res.typeStructure);
    if (res.token)
        free(res.token);
    if (res.typeName)
        free(res.typeName);
    resetExecuteQueryRes(res);
}
