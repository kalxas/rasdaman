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
#include "version.h"
#ifndef RMANVERSION
#error "Please specify RMANVERSION variable!"
#endif

#ifdef EARLY_TEMPLATE
#define __EXECUTABLE__
#ifdef __GNUG__
#include "template_inst.hh"
#include "raslib/template_inst.hh"
#endif
#endif

#include <iostream>
#include <netdb.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string>
#include <signal.h>
#include <vector>
#include "storagemgr/sstoragelayout.hh"
#include "globals.hh"   // DEFAULT_PORT
#include "servercomm/httpserver.hh"
#include "common/logging/signalhandler.hh"
#include "relblobif/tilecache.hh"
#include "loggingutils.hh"

// from some unknown location the debug-srv.hh guard seems to be defined already, so get rid of it -- PB 2005-jan-10
#undef DEBUG_HH
#define DEBUG_MAIN debug_main

RMINITGLOBALS('S')

#include "server/rasserver_config.hh"
#include "rasserver_entry.hh"
#include "rasserver/src/rasnetserver.hh"

#include <logging.hh>

// -- directql section start

#include "rasodmg/ref.hh"
#include "rasodmg/set.hh"
#include "rasodmg/marray.hh"
#include "rasodmg/iterator.hh"
#include "rasodmg/oqlquery.hh"
#include "rasodmg/storagelayout.hh"
#include "rasodmg/alignedtiling.hh"

#include "raslib/type.hh"
#include "raslib/mddtypes.hh"
#include "raslib/primitive.hh"
#include "raslib/complex.hh"
#include "raslib/structure.hh"
#include "raslib/structuretype.hh"
#include "raslib/primitivetype.hh"

#include "relcatalogif/complextype.hh"
#include "servercomm/servercomm.hh"

#include "lockmgr/lockmanager.hh"
#include "mymalloc/mymalloc.h"

#include "servercomm/cliententry.hh"
#include "raslib/minterval.hh"
#include "raslib/marraytype.hh"
#include "rasserver_config.hh"
#include "rasserver_error.hh"

#include "rasserver_rasdl.hh"

using namespace std;

// directql macros, constants and function prototypes
#define SECURE_FREE_PTR(ptr) \
    if (ptr) { \
        free(ptr); \
        ptr = NULL; }
#define SECURE_DELETE_PTR(ptr) \
    if (ptr) { \
        delete ptr; \
        ptr = NULL; }

// TODO: remove this and use easylogging macros (LINFO)
// logging mechanism that respects 'quiet' flag:
#define INFO(a) { if (!configuration.isQuietLogOn()) std::cout << a; }

#define DQ_CLIENT_ID 1000000
#define DQ_TIMEOUT 1000000
#define DQ_MANAGEMENT_INTERVAL 1000000
#define DQ_LISTEN_PORT 8001
#define DQ_SERVER_NAME "NT1"
#define DQ_CAPABILITY "$I1$ERW$BRASBASE$T1:3:2008:23:39:24$NNT1$D3839d047344677ddb1ff1a24dada286e$K"

#define DEFAULT_DB  "RASBASE"


// rasdaman MDD type for byte strings (default type used for file format reading)
#define MDD_STRINGTYPE  "GreyString"

#define STATUS_MORE_ELEMS 0
#define STATUS_MDD 0
#define STATUS_SCALAR 1
#define STATUS_EMPTY 2

#define HELP_OUT    "<t> use display method t for cell values of result MDDs where t is one of none, file, formatted, string, hex. Implies --content"
#define DEFAULT_OUT OUT_NONE
#define PARAM_OUT_FILE  "file"
#define PARAM_OUT_STRING "string"
#define PARAM_OUT_HEX   "hex"
#define PARAM_OUT_FORMATTED "formatted"
#define PARAM_OUT_NONE  "none"
#define DEFAULT_OUT_STR PARAM_OUT_NONE

#define DEFAULT_OUTFILE "rasql_%d"


const int MAX_STR_LEN = 255;
const int MAX_QUERY_LEN = 10240;

const char* user = DEFAULT_USER;
const char* passwd = DEFAULT_PASSWD;

const char* fileName = NULL;
const char* queryString = NULL;

bool output = false;
bool displayType = false;

const char* outFileMask = DEFAULT_OUTFILE;
ClientTblElt* r;

r_Minterval mddDomain;
bool mddDomainDef = false;

const char* mddTypeName = NULL;
bool mddTypeNameDef = false;

bool dbIsOpen = false;
bool taIsOpen = false;


void doStuff();

std::string
getDefaultDb();

void
openTransaction(bool readwrite);

void
closeTransaction(bool doCommit);

void
printScalar(char* buffer, QtData* data, unsigned int resultIndex);


void
printResult(Tile* tile, int resultIndex);

void
printOutput(unsigned short status, ExecuteQueryRes* result);

r_Marray_Type*
getTypeFromDatabase(const char* mddTypeName);

void
freeResult(ExecuteQueryRes* result);

void
printError(unsigned short status, ExecuteQueryRes* result);

void
printError(unsigned short status, ExecuteUpdateRes* result);

void
openDatabase();

void
closeDatabase();

std::string baseName = getDefaultDb();

// -- end of directql code


// return codes
#define RC_OK       0
#define RC_ERROR    (-1)

bool initialization();

unsigned long maxTransferBufferSize = 4000000;
int           noTimeOut = 0;

// here the id string for connecting to the RDBMS is stored (used by rel* modules).
// FIXME: bad hack -- PB 2003-oct-12
char globalConnectId[256];
char globalDbUser[255] = {0};
char globalDbPasswd[255] = {0};

int  globalHTTPPort;
// do we allow for User-Defined Functions? (aka rasql routines)

// drop client after 5 minutes of no alive signal
// can be changed via cmd line parameter
unsigned long clientTimeOut      = CLIENT_TIMEOUT;

// server management every 2 minutes
unsigned long managementInterval = 120;

const char* rasmgrHost = 0;
int         rasmgrPort = DEFAULT_PORT;
const char* serverName  = 0;
int         serverListenPort = 0;
ServerComm* server = NULL;

/**
 * Invoked on SIGUSR1 signal, this handler prints the stack trace and then kills
 * the server process with SIGKILL. This is used in crash testing of rasserver.
 */
void
testHandler(int sig, siginfo_t* info, void* ucontext);

void
shutdownHandler(int sig, siginfo_t* info, void* ucontext);

void
crashHandler(int sig, siginfo_t* info, void* ucontext);



void testHandler(__attribute__((unused)) int sig, __attribute__((unused)) siginfo_t* info, void* ucontext)
{
    LINFO << "test handler caught signal SIGUSR1, stacktrace: \n" << common::SignalHandler::getStackTrace();
    LINFO << "killing rasserver with SIGKILL.";
    raise(SIGKILL);
}

void shutdownHandler(__attribute__ ((unused)) int sig, siginfo_t* info, void* ucontext)
{
    static bool alreadyExecuting{false};
    if (!alreadyExecuting)
    {
        alreadyExecuting = true;
        LINFO << "Interrupted by signal " << common::SignalHandler::toString(info);
        NNLINFO << "Shutting down... ";
        // {
        //     TileCache::clear();
        //     BLINFO << "aborting any open transactions... ";
        //     auto &rasserverEntry = RasServerEntry::getInstance();
        //     if (rasserverEntry.compat_isOpenTA())
        //         rasserverEntry.compat_abortTA();
        //     if (server)
        //     {
        //         server->abortEveryThingNow();
        //         delete server;
        //     }
        // }
        BLINFO << "rasserver terminated.";
        exit(EXIT_SUCCESS);
    }
}

void crashHandler(__attribute__ ((unused)) int sig, siginfo_t* info, void* ucontext)
{
    static bool alreadyExecuting{false};
    if (!alreadyExecuting)
    {
        alreadyExecuting = true;
        NNLERROR << "Interrupted by signal " << common::SignalHandler::toString(info);
        BLERROR << "... stacktrace:\n" << common::SignalHandler::getStackTrace() << "\n";
        BLFLUSH;
        NNLERROR << "Shutting down... ";
        // {
        //     TileCache::clear();
        //     BLERROR << "aborting any open transactions... ";
        //     auto &rasserverEntry = RasServerEntry::getInstance();
        //     if (rasserverEntry.compat_isOpenTA())
        //         rasserverEntry.compat_abortTA();
        //     if (server)
        //     {
        //         server->abortEveryThingNow();
        //         delete server;
        //     }
        // }
        BLERROR << "rasserver terminated." << endl;
    } else {
        // if a signal comes while the handler has already been invoked,
        // wait here for max 3 seconds, so that the handler above has some time
        // (hopefully) finish
        sleep(3);
    }
    exit(sig);
}

void installSignalHandlers()
{
    common::SignalHandler::handleAbortSignals(crashHandler);
    common::SignalHandler::handleShutdownSignals(shutdownHandler);
    common::SignalHandler::ignoreStandardSignals();
#ifdef RASDEBUG
    common::SignalHandler::handleSignals({SIGUSR1}, testHandler);
#endif
}


INITIALIZE_EASYLOGGINGPP

int main(int argc, char** argv)
{
    if (configuration.parseCommandLine(argc, argv) == false)
    {
        cerr << "Error: cannot parse command line." << endl;
        return RC_ERROR;
    }

    installSignalHandlers();

    if (configuration.isRasserver())
    {
        LINFO << "rasserver: rasdaman server " << RMANVERSION << " on base DBMS " << BASEDBSTRING << ".";
        LINFO << " Copyright 2003-2018 Peter Baumann / rasdaman GmbH. \n"
              << " Rasdaman community is free software: you can redistribute it and/or modify "
              << "it under the terms of the GNU General Public License as published by "
              << "the Free Software Foundation, either version 3 of the License, or "
              << "(at your option) any later version. \n"
              << " Rasdaman community is distributed in the hope that it will be useful, "
              << "but WITHOUT ANY WARRANTY; without even the implied warranty of "
              << "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
              << "GNU General Public License for more details.";
        LINFO << "To obtain a list of external packages used, please visit www.rasdaman.org.\n";
    }

    if (initialization() == false)
    {
        LERROR << "Error during initialization, aborted.";
        return RC_ERROR;
    }

    //
    // body rasserver
    //

    int returnCode = 0;
    try
    {
        LDEBUG << "selecting server type...";
        if (configuration.isHttpServer())
        {
            LDEBUG << "initializing http server...";
            server = new HttpServer(
                    static_cast<unsigned int>(serverListenPort), const_cast<char*>(rasmgrHost),
                    static_cast<unsigned int>(rasmgrPort), const_cast<char*>(serverName));
            LDEBUG << "http server initialized.";
        }
        else if (configuration.isRasnetServer())
        {
            LDEBUG << "initializing rasnet server...";
            //start rasnet server
            rasserver::RasnetServer rasnetServer(configuration);
            rasnetServer.startRasnetServer();
            LDEBUG << "rasnet server started.";
        }
        else
        {
            // client mode: directql or rasdl
            common::LogConfiguration logConf(string(CONFDIR), CLIENT_LOG_CONF);
            logConf.configClientLogging(configuration.isQuietLogOn());

            LDEBUG << "initializing direct server...";
            server = new ServerComm(
                    static_cast<unsigned int>(serverListenPort), const_cast<char*>(rasmgrHost),
                    static_cast<unsigned int>(rasmgrPort), const_cast<char*>(serverName));

            // -- directql
            if (configuration.hasQueryString())
            {
                openDatabase();
                doStuff();
                closeDatabase();
            }
            else if (configuration.usesRasdl())
            {
                runRasdl(argc, argv);
            }
        }
    }
    catch (r_Error& errorObj)
    {
        LERROR << "rasdaman server error " << errorObj.get_errorno() << ": " << errorObj.what();
        returnCode = RC_ERROR;
    }
    catch (std::exception& ex)
    {
        LERROR << "rasdaman server exception: " << ex.what();
        returnCode = RC_ERROR;
    }
    catch (...)
    {
        LERROR << "rasserver: general exception";
        returnCode = RC_ERROR;
    }

    if (server)
    {
        delete server;
        server = NULL;
    }

    LINFO << "rasserver terminated.";
    return returnCode;
}

bool initialization()
{
    serverName = configuration.getServerName();
    accessControl.setServerName(serverName);

    serverListenPort = globalHTTPPort = configuration.getListenPort();

    NNLINFO << "Server " << serverName << " of type ";

    if (configuration.isHttpServer())
    {
        BLINFO << "HTTP, listening on port " << serverListenPort;
    }
    else if (configuration.isRasnetServer())
    {
        BLINFO << "rasnet, listening on port " << serverListenPort;
    }
    else
    {
        BLINFO << "RPC, registered with prognum 0x" << hex << serverListenPort << dec;
    }

    //  globalConnectId         = configuration.getDbConnectionID();
    strcpy(globalConnectId, configuration.getDbConnectionID());
    BLINFO << ", connecting to " << BASEDBSTRING << " as '" << globalConnectId <<  "'";

    strcpy(globalDbUser, configuration.getDbUser());
    if (strlen(configuration.getDbUser()) > 0)
    {
        BLINFO << ", user " << globalDbUser;
    }
    strcpy(globalDbPasswd, configuration.getDbPasswd());
    BLINFO << "\n";

    rasmgrHost = configuration.getRasmgrHost();
    rasmgrPort = configuration.getRasmgrPort();

    NNLINFO << "Verifying rasmgr host name: " << rasmgrHost << "... ";
    if (!gethostbyname(rasmgrHost))
    {
        BLINFO << "failed\n";
        return false;
    }
    BLINFO << "ok\n";

    maxTransferBufferSize = static_cast<unsigned int>(configuration.getMaxTransferBufferSize());

    clientTimeOut = static_cast<unsigned int>(configuration.getTimeout());
    if (clientTimeOut == 0)
    {
        noTimeOut = 1;
    }

    managementInterval = clientTimeOut / 4;

    //tilesize
    StorageLayout::DefaultTileSize = static_cast<unsigned int>(configuration.getDefaultTileSize());
    LINFO << "Tile size set to : " << StorageLayout::DefaultTileSize;

    //pctmin
    StorageLayout::DefaultMinimalTileSize = static_cast<unsigned int>(configuration.getDefaultPCTMin());
    LINFO << "PCTMin set to    : " << StorageLayout::DefaultMinimalTileSize;

    //pctmax
    StorageLayout::DefaultPCTMax = static_cast<unsigned int>(configuration.getDefaultPCTMax());
    LINFO << "PCTMax set to    : " << StorageLayout::DefaultPCTMax;

    //indexsize
    StorageLayout::DefaultIndexSize = static_cast<unsigned int>(configuration.getDefaultIndexSize());
    LINFO << "IndexSize set to : " << StorageLayout::DefaultIndexSize;

#ifdef RMANDEBUG
    RManDebug = configuration.getDebugLevel();
    LINFO << "Debug level: " << RManDebug;
#endif

    try
    {
        StorageLayout::DefaultTileConfiguration = r_Minterval(configuration.getDefaultTileConfig());
    }
    catch (r_Error& err)
    {
        LERROR << "Failed converting " << configuration.getDefaultTileConfig() << " to r_Minterval, "
               "error " << err.get_errorno() << " : " << err.what();
        return false;
    }
    LINFO << "Default Tile Conf: " << StorageLayout::DefaultTileConfiguration;

    // Tiling
    r_Tiling_Scheme tmpTS = get_tiling_scheme_from_name(configuration.getTilingScheme());

    if (tmpTS != r_Tiling_Scheme_NUMBER)
    {
        StorageLayout::DefaultTilingScheme = tmpTS;
    }

    if ((tmpTS != r_NoTiling) && (tmpTS != r_RegularTiling) && (tmpTS != r_AlignedTiling))
    {
        LERROR << "Unsupported tiling strategy: " << configuration.getTilingScheme();
        return false;
    }

    //retiling enabled only if tiling scheme is regular tiling
    RMInit::tiling = (tmpTS == r_RegularTiling);
    LINFO << "Default Tiling   : " << StorageLayout::DefaultTilingScheme;

    // Index
    r_Index_Type tmpIT = get_index_type_from_name(configuration.getIndexType());
    if (tmpIT != r_Index_Type_NUMBER)
    {
        StorageLayout::DefaultIndexType = tmpIT;
    }
    LINFO << "Default Index    : " << StorageLayout::DefaultIndexType;

    //use tilecontainer
    RMInit::useTileContainer = configuration.useTileContainer();
    LINFO << "Tile Container   : " << RMInit::useTileContainer;

    //set cache size limit
    TileCache::cacheLimit = configuration.getCacheLimit();
    LINFO << "Cache size limit : " << TileCache::cacheLimit;

    return true;
}



// directql functions
std::string 
getDefaultDb()
{
    string rasmgrConfFilePath(string(CONFDIR) + "/" + string(RASMGR_CONF_FILE));
    if (rasmgrConfFilePath.length() >= PATH_MAX)
    {
        throw runtime_error("The path to the configuration file is longer than the maximum file system path.");
    }
    std::ifstream ifs(rasmgrConfFilePath);    // open config file

    if (!ifs)
    {
        throw runtime_error("Failed opening '" + rasmgrConfFilePath + "'.");
    }
    else
    {
        static const size_t bufferSize = 1024;
        char inBuffer[bufferSize];
        while (!ifs.eof())
        {
            ifs.getline(inBuffer, bufferSize);
            if (strlen(inBuffer) == 0)
                continue;

            char *token = strtok(inBuffer, " ");
            if (strcmp(token, "define") != 0)
                continue;
            token = strtok(NULL, " ");
            if (strcmp(token, "dbh") != 0)
                continue;
            token = strtok(NULL, " ");
            token = strtok(NULL, " ");
            if (strcmp(token, "-connect") != 0)
                continue;
            token = strtok(NULL, " ");
            return string(token);
        }
    }
    return string(DEFAULT_DB);
}

void
openDatabase()
{
    if (!dbIsOpen)
    {
        if (configuration.getBaseName())
            baseName = configuration.getBaseName();
        sprintf(globalConnectId, "%s", baseName.c_str());
        auto serverName = configuration.getServerName();
        auto serverPort = configuration.getListenPort();
        if (configuration.getUser())
            user = configuration.getUser();
        if (configuration.getPasswd())
            passwd = configuration.getPasswd();
        INFO("opening database " << baseName << " at " << serverName << ":" << serverPort << "..." << flush);
        r = new ClientTblElt(ClientType::Regular, DQ_CLIENT_ID);
        server->addClientTblEntry(r);
        accessControl.setServerName(DQ_SERVER_NAME);
        accessControl.crunchCapability(DQ_CAPABILITY);
        server->openDB(DQ_CLIENT_ID, baseName.c_str(), user);

        ObjectBroker::init();

        dbIsOpen = true;
        INFO(" ok" << endl << flush);
    }
} // openDatabase()

void
closeDatabase()
{
    if (dbIsOpen)
    {
        LDEBUG << "database was open, closing it";
        ObjectBroker::clearBroker();
        ObjectBroker::deinit();
        server->closeDB(DQ_CLIENT_ID);
        dbIsOpen = false;
    }

    return;
} // closeDatabase()

void
openTransaction(bool readwrite)
{
    if (!taIsOpen)
    {
        if (readwrite)
        {
            LDEBUG << "transaction was closed, opening rw...";
            server->beginTA(DQ_CLIENT_ID, false);
            LDEBUG << "ok";
        }
        else
        {
            LDEBUG << "transaction was closed, opening ro...";
            server->beginTA(DQ_CLIENT_ID, true);
            LDEBUG << "ok";
        }

        taIsOpen = true;
    }
} // openTransaction()

void
closeTransaction(bool doCommit)
{
    if (taIsOpen)
    {
        if (doCommit)
        {
            LDEBUG << "transaction was open, committing it...";
            server->commitTA(DQ_CLIENT_ID);
            LDEBUG << "ok";
        }
        else
        {
            LDEBUG << "transaction was open, aborting it...";
            server->abortTA(DQ_CLIENT_ID);
            LDEBUG << "ok";
        }
        taIsOpen = false;
    }
    if (configuration.isLockMgrOn())
    {
        LockManager* lockManager = LockManager::Instance();
        lockManager->clearLockTable();
    }
    return;
} // closeTransaction()

void printScalar(char* buffer, QtData* data, unsigned int resultIndex)
{
    INFO("  Result element " << resultIndex << ": ");
    switch (data->getDataType())
    {
    case QT_BOOL:
        INFO((*((bool*) buffer) ? "t" : "f") << flush);
        break;

    case QT_CHAR:
        INFO(static_cast<int>(*((r_Char*) buffer)) << flush);
        break;

    case QT_OCTET:
        INFO(static_cast<int>(*((r_Octet*) buffer)) << flush);
        break;

    case QT_SHORT:
        INFO(*((r_Short*) buffer) << flush);
        break;

    case QT_USHORT:
        INFO(*((r_UShort*) buffer) << flush);
        break;

    case QT_LONG:
        INFO(*((r_Long*) buffer) << flush);
        break;

    case QT_ULONG:
        INFO(*((r_ULong*) buffer) << flush);
        break;

    case QT_FLOAT:
        INFO(std::setprecision(std::numeric_limits<float>::digits10 + 1) << *((r_Float*) buffer) << flush);
        break;

    case QT_DOUBLE:
        INFO(std::setprecision(std::numeric_limits<double>::digits10 + 1) << *((r_Double*) buffer) << flush);
        break;

    case QT_COMPLEXTYPE1:
    {
        QtScalarData* scalarDataObj = static_cast<QtScalarData*>(data);
        ComplexType1* ct = static_cast<ComplexType1*>(const_cast<BaseType*>(scalarDataObj->getValueType()));
        auto re = *((r_Float*) (buffer + ct->getReOffset()));
        auto im = *((r_Float*) (buffer + ct->getImOffset()));
        INFO("(" << re << "," << im << ")" << flush)
        break;
    }
    case QT_COMPLEXTYPE2:
    {
        QtScalarData* scalarDataObj = static_cast<QtScalarData*>(data);
        ComplexType2* ct = static_cast<ComplexType2*>(const_cast<BaseType*>(scalarDataObj->getValueType()));
        auto re = *((r_Double*) (buffer + ct->getReOffset()));
        auto im = *((r_Double*) (buffer + ct->getImOffset()));
        INFO("(" << re << "," << im << ")" << flush)
        break;
    }

    case QT_CINT16:
    {
        QtScalarData* scalarDataObj = static_cast<QtScalarData*>(data);
        CInt16* ct = static_cast<CInt16*>(const_cast<BaseType*>(scalarDataObj->getValueType()));
        auto re = *((r_Short*) (buffer + ct->getReOffset()));
        auto im = *((r_Short*) (buffer + ct->getImOffset()));
        INFO("(" << re << "," << im << ")" << flush)
        break;
    }
    case QT_CINT32:
    {
        QtScalarData* scalarDataObj = static_cast<QtScalarData*>(data);
        CInt32* ct = static_cast<CInt32*>(const_cast<BaseType*>(scalarDataObj->getValueType()));
        auto re = *((r_Long*) (buffer + ct->getReOffset()));
        auto im = *((r_Long*) (buffer + ct->getImOffset()));
        INFO("(" << re << "," << im << ")" << flush)
        break;
    }

    case QT_COMPLEX:
    {
        QtScalarData* scalarDataObj = static_cast<QtScalarData*>(data);
        StructType* st = static_cast<StructType*>(const_cast<BaseType*>(scalarDataObj->getValueType()));
        INFO("{ ");
        for (unsigned int i = 0; i < st->getNumElems(); i++)
        {
            BaseType* bt = const_cast<BaseType*>(st->getElemType(i));
            if (i > 0)
            {
                INFO(", ");
            }
            bt->printCell(cout, buffer);

            buffer += bt->getSize();
        }
        INFO(" }" << flush);
    }
    break;

    case QT_STRING:
    case QT_INTERVAL:
    case QT_MINTERVAL:
    case QT_POINT:
        INFO(buffer << flush);
        break;
    default:
        INFO("scalar type not supported!" << endl);
        break;
    }
    INFO(endl << flush);
} // printScalar()


// result_set should be parameter, but is global -- see def for reason
void printResult(Tile* tile, int resultIndex)
{
    const char* theStuff = tile->getContents();
    r_Bytes numCells = tile->getSize();
    auto outputType = configuration.getOutputType();

    switch (outputType)
    {
    case OUT_NONE:
        break;
    case OUT_STRING:
    {
        INFO("  Result object " << resultIndex << ": ");
        for (r_Bytes cnt = 0; cnt < numCells; cnt++)
        {
            cout << theStuff[cnt];
        }
        cout << endl;
    }
    break;
    case OUT_HEX:
    {
        INFO("  Result object " << resultIndex << ": ");
        cout << hex;
        for (r_Bytes cnt = 0; cnt < numCells; cnt++)
        {
            cout << setw(2) << (unsigned short)(0xff & theStuff[cnt]) << " ";
        }
        cout << dec << endl;
    }
    break;
    case OUT_FILE:
    {
        char defFileName[FILENAME_MAX];
        (void) snprintf(defFileName, sizeof(defFileName) - 1, outFileMask, resultIndex);
        LDEBUG << "filename for #" << resultIndex << " is " << defFileName;

        // special treatment only for DEFs
        r_Data_Format mafmt = tile->getDataFormat();
        switch (mafmt)
        {
        case r_TIFF:
            strcat(defFileName, ".tif");
            break;
        case r_JP2:
            strcat(defFileName, ".jp2");
            break;
        case r_JPEG:
            strcat(defFileName, ".jpg");
            break;
        case r_HDF:
            strcat(defFileName, ".hdf");
            break;
        case r_PNG:
            strcat(defFileName, ".png");
            break;
        case r_BMP:
            strcat(defFileName, ".bmp");
            break;
        case r_NETCDF:
            strcat(defFileName, ".nc");
            break;
        case r_CSV:
            strcat(defFileName, ".csv");
            break;
        case r_JSON:
            strcat(defFileName, ".json");
            break;
        case r_DEM:
            strcat(defFileName, ".dem");
            break;
        default:
            strcat(defFileName, ".unknown");
            break;
        }

        INFO("  Result object " << resultIndex << ": going into file " << defFileName << "..." << flush);
        FILE* tfile = fopen(defFileName, "wb");
        if (tfile == NULL)
        {
            throw RasqlError(NOFILEWRITEPERMISSION);
        }
        if (fwrite(static_cast<void*>(const_cast<char*>(theStuff)), 1, numCells, tfile) != numCells)
        {
            fclose(tfile);
            throw RasqlError(UNABLETOWRITETOFILE);
        };
        fclose(tfile);
        INFO("ok." << endl);
    }
    break;
    default:
        cerr << "Internal error: unknown output type, ignoring action: " << static_cast<int>(outputType) << endl;
        break;
    } // switch(outputType)

} // printResult()

void printOutput(unsigned short status, ExecuteQueryRes* result)
{
    switch (status)
    {
    case STATUS_MDD:
        INFO("holds MDD elements" << endl);
        break;
    case STATUS_SCALAR:
        INFO("holds non-MDD elements" << endl);
        break;
    case STATUS_EMPTY:
        INFO("holds no elements" << endl);
        break;
    default:
        break;
    };

    if (result)
    {
        if (configuration.isOutputOn())
        {
            INFO("Getting result..." << flush);
            if (status == STATUS_MDD)
            {
                INFO("Getting MDD objects..." << endl << flush);

                char* typeName = NULL;
                char* typeStructure = NULL;
                r_OId oid;
                unsigned short currentFormat;

                int resultIndex = 0;
                while (server->getNextMDD(DQ_CLIENT_ID, mddDomain, typeName, 
                    typeStructure, oid, currentFormat) == STATUS_MORE_ELEMS)
                {
                    Tile* resultTile = new Tile(r->transTiles);
                    printResult(resultTile, ++resultIndex);
                    delete resultTile;

                    // cleanup
                    (*(r->transferDataIter))++;
                    if (*(r->transferDataIter) != r->transferData->end())
                    {
                        SECURE_DELETE_PTR(r->transTiles);
                        SECURE_DELETE_PTR(r->tileIter);
                    }
                    SECURE_FREE_PTR(typeStructure);
                    SECURE_FREE_PTR(typeName);
                }
            }
            else if (status == STATUS_SCALAR)
            {
                INFO("Getting scalars..." << endl << flush);

                unsigned int resultIndex = 0;
                char* buffer;
                unsigned int bufferSize;
                status = STATUS_MORE_ELEMS;
                while (status == STATUS_MORE_ELEMS)
                {
                    QtData* data = (**(r->transferDataIter));
                    status = server->getNextElement(DQ_CLIENT_ID, buffer, bufferSize);
                    printScalar(buffer, data, ++resultIndex);
                    if (buffer)
                    {
                        free(buffer);
                        buffer = NULL;
                    }
                }
            }
        }
        server->endTransfer(DQ_CLIENT_ID);
    }
}

/*
 * get database type structure from type name
 * returns ptr if an MDD type with the given name exists in the database, NULL otherwise
 * throws r_Error upon general database comm error
 * needs an open transaction
 */
r_Marray_Type* getTypeFromDatabase(const char* mddTypeName2)
{
    r_Marray_Type* retval = NULL;
    char* typeStructure = NULL;

    // first, try to get type structure from database using a separate r/o transaction
    try
    {
        server->getTypeStructure(DQ_CLIENT_ID, mddTypeName2, ClientComm::r_MDDType_Type, typeStructure);

        // above doesn't seem to work, so at least make it work with inv_* functions -- DM 2013-may-19
        if (!typeStructure)
        {
            typeStructure = strdup("marray<char>");
        }
        LDEBUG << "type structure is " << typeStructure;
    }
    catch (r_Error& err)
    {
        if (err.get_kind() == r_Error::r_Error_DatabaseClassUndefined)
        {
            LDEBUG << "Type is not a well known type: " << typeStructure;
            typeStructure = new char[strlen(mddTypeName2) + 1];
            // earlier code tried this one below, but I feel we better are strict -- PB 2003-jul-06
            // strcpy(typeStructure, mddTypeName2);
            // LDEBUG <<  "using instead: " << typeStructure;
            throw RasqlError(MDDTYPEINVALID);
        }
        else // unanticipated error
        {
            LDEBUG << "Error during type retrieval from database: " << err.get_errorno() << " " << err.what();
            throw;
        }
    }

    // next, find out whether it is an MDD type (and not a base or set type, eg)
    r_Type* tempType = NULL;
    try
    {
        tempType = r_Type::get_any_type(typeStructure);
        LDEBUG << "get_any_type() for this type returns: " << tempType;
        if (tempType->isMarrayType())
        {
            retval = (r_Marray_Type*) tempType;
            tempType = NULL;
            LDEBUG << "found MDD type: " << retval;
        }
        else
        {
            LDEBUG << "type is not an marray type: " << typeStructure;
            SECURE_DELETE_PTR(tempType);
            retval = NULL;
            throw RasqlError(MDDTYPEINVALID);
        }
    }
    catch (r_Error& err)
    {
        LDEBUG << "Error during retrieval of MDD type structure (" << typeStructure << "): " << err.get_errorno() << " " << err.what();
        SECURE_FREE_PTR(typeStructure);
        SECURE_DELETE_PTR(tempType);
        throw;
    }

    SECURE_FREE_PTR(typeStructure);

    return retval;
} // getTypeFromDatabase()

void freeResult(ExecuteQueryRes* result)
{
    SECURE_FREE_PTR(result->typeStructure);
    SECURE_FREE_PTR(result->token);
    SECURE_FREE_PTR(result->typeName);
}

void printError(unsigned short status, ExecuteQueryRes* result)
{
    cerr << endl << "Error number: " << result->errorNo << " Token: '" << result->token <<
         "' Line: " << result->lineNo << " Column: " << result->columnNo << " (status: " << status << ")" << endl << flush;
}

void printError(unsigned short status, ExecuteUpdateRes* result)
{
    cerr << endl << "Error number: " << result->errorNo << " Token: '" << result->token <<
         "' Line: " << result->lineNo << " Column: " << result->columnNo << " (status: " << status << ")" << endl << flush;
}

void doStuff()
{
    TileCache::cacheLimit = 0;

    char* fileContents = NULL; // contents of file satisfying "$1" parameter in query
    r_Marray_Type* mddType = NULL; // this MDD's type
    RPCMarray* marray = NULL;
    r_Bytes baseTypeSize = 0;

    queryString = configuration.getQueryString();
    mddTypeNameDef = configuration.isMddTypeNameDef();
    mddDomainDef = configuration.isMddDomainDef();
    mddDomain = configuration.getMddDomain();
    fileName = configuration.getFileName();
    if (configuration.getOutFileMask())
        outFileMask = configuration.getOutFileMask();

    r_OQL_Query query(queryString);
    LDEBUG << "query is: " << query.get_query();

    try
    {
        if (fileName != NULL)
        {
            openTransaction(false);

            // if no type name was specified then assume byte string (for encoded files)
            if (!mddTypeNameDef)
            {
                configuration.setMddTypeName(MDD_STRINGTYPE);
            }
            mddTypeName = configuration.getMddTypeName();

            INFO("fetching type information for " << mddTypeName << " from database, using readonly transaction..." << flush);
            mddType = getTypeFromDatabase(mddTypeName);
            closeTransaction(true);
            INFO("ok" << endl);

            INFO("reading file " << fileName << "..." << flush);
            FILE* fileD = fopen(fileName, "r");
            if (fileD == NULL)
            {
                throw RasqlError(FILEINACCESSIBLE);
            }

            fseek(fileD, 0, SEEK_END);
            long size = ftell(fileD);
            LDEBUG << "file size is " << size << " bytes";

            if (size == 0)
            {
                throw RasqlError(FILEEMPTY);
            }

            // if no domain specified (this is the case with encoded files), then set to byte stream
            if (!mddDomainDef)
            {
                mddDomain = r_Minterval(1) << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(size) - 1);
                LDEBUG << "domain set to " << mddDomain;
            }
            else if (size != static_cast<long>(mddDomain.cell_count() * mddType->base_type().size()))
            {
                throw RasqlError(FILESIZEMISMATCH);
            }

            try
            {
                fileContents = static_cast<char*>(mymalloc(static_cast<size_t>(size)));
                fseek(fileD, 0, SEEK_SET);
                size_t rsize = fread(fileContents, 1, static_cast<size_t>(size), fileD);

                baseTypeSize = mddType->base_type().size();
                r_GMarray* fileMDD = new r_GMarray(mddDomain, baseTypeSize, 0, NULL, false);
                fileMDD->set_type_schema(mddType);
                fileMDD->set_array_size(baseTypeSize);
                query << *fileMDD;
                SECURE_DELETE_PTR(fileMDD);
                SECURE_DELETE_PTR(mddType);

                marray = (RPCMarray*) mymalloc(sizeof(RPCMarray));
                marray->cellTypeLength = baseTypeSize;
                marray->domain = mddDomain.get_string_representation();
                marray->currentFormat = r_Array;
                marray->storageFormat = r_Array;
                marray->data.confarray_len = mddDomain.cell_count() * baseTypeSize;
                marray->data.confarray_val = fileContents;
            }
            catch (std::bad_alloc &e)
            {
                LDEBUG << "Unable to claim memory: " << size << " bytes";
                throw RasqlError(UNABLETOCLAIMRESOURCEFORFILE);
            }

            fclose(fileD);

            INFO("ok" << endl);
        }

        if (query.is_insert_query())
        {
            INFO("Executing insert query...\n" << flush);

            openTransaction(true);

            ExecuteQueryRes result;
            result.token = NULL;
            result.typeName = NULL;
            result.typeStructure = NULL;
            unsigned short status;

            if (fileContents != NULL)
            {
                server->initExecuteUpdate(DQ_CLIENT_ID);
                server->startInsertTransMDD(DQ_CLIENT_ID, mddDomain, baseTypeSize, mddTypeName);
                server->insertTile(DQ_CLIENT_ID, false, marray);
                server->endInsertMDD(DQ_CLIENT_ID, false);
                status = server->executeInsert(DQ_CLIENT_ID, query.get_query(), result);
                query.reset_query();

                server->endTransfer(DQ_CLIENT_ID);
            }
            else
            {
                status = server->executeInsert(DQ_CLIENT_ID, queryString, result);
            }

            if (status == 0 || status == 1 || status == 2)
            {
                printOutput(status, &result);
            }
            else
            {
                printError(status, &result);
            }
            freeResult(&result);
            closeTransaction(true);
        }
        else if (query.is_update_query())
        {
            INFO("Executing update query...\n" << flush);

            openTransaction(true);

            ExecuteUpdateRes result;
            unsigned short status;

            if (fileContents != NULL)
            {
                server->initExecuteUpdate(DQ_CLIENT_ID);
                server->startInsertTransMDD(DQ_CLIENT_ID, mddDomain, baseTypeSize, mddTypeName);
                server->insertTile(DQ_CLIENT_ID, false, marray);
                server->endInsertMDD(DQ_CLIENT_ID, false);
                status = server->executeUpdate(DQ_CLIENT_ID, query.get_query(), result);
                query.reset_query();

                server->endTransfer(DQ_CLIENT_ID);
            }
            else
            {
                status = server->executeUpdate(DQ_CLIENT_ID, queryString, result);
            }

            if (status != 0 && status != 1)
            {
                printError(status, &result);
            }
            SECURE_FREE_PTR(result.token);
            closeTransaction(true);
        }
        else // retrieval query
        {
            INFO("Executing retrieval query...\n" << flush);

            openTransaction(false);

            ExecuteQueryRes result;
            unsigned short status =
                server->executeQuery(DQ_CLIENT_ID, queryString, result);

            if (status <= 2) {
                printOutput(status, &result);
            } else if (result.token != NULL) {
                r_Equery_execution_failed e(
                    result.errorNo, result.lineNo, result.columnNo, result.token);
                LERROR << "rasdaman error " << e.get_errorno() << ": " << e.what();
            }
            
            freeResult(&result);

            closeTransaction(true);
        }
    }
    catch (r_Error& err)
    {
        if (marray)
        {
            SECURE_FREE_PTR(marray->domain);
            SECURE_FREE_PTR(marray);
        }
        throw err;
    }

    if (marray)
    {
        SECURE_FREE_PTR(marray->domain);
        SECURE_FREE_PTR(marray);
    }
}

// end of directql functions
