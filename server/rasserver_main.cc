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
#include "version.h"
#ifndef RMANVERSION
#error "Please specify RMANVERSION variable!"
#endif

#include "globals.hh"
#include "servercomm/httpserver.hh"
#include "storagemgr/sstoragelayout.hh"
#include "common/logging/signalhandler.hh"
#include "rasserver_entry.hh"
#include "rasserver/src/rasnetserver.hh"
#include "rasserver_config.hh"
#include "servercomm/accesscontrol.hh"

#include "rasserver_directql.hh"
#include "rasserver_rasdl.hh"

#include <logging.hh>
#include "loggingutils.hh"

#include <iostream>
#include <netdb.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string>
#include <signal.h>
#include <vector>

RMINITGLOBALS('S')
INITIALIZE_EASYLOGGINGPP

using namespace std;

#define RC_OK       (0)
#define RC_ERROR    (-1)

extern AccessControl accessControl;

// TODO remove these global variables at some point, used in servercomm.cc
unsigned long maxTransferBufferSize = 4000000;
int           noTimeOut = 0;

// here the id string for connecting to the RDBMS is stored (used by rel* modules).
// FIXME: bad hack -- PB 2003-oct-12
char globalConnectId[256];
char globalDbUser[255] = {0};
char globalDbPasswd[255] = {0};
// used in qtmddaccess.cc
bool isLockMgrOn = false;

bool initialize();

// -------------------------------------------------------------------------- //
//                           signal handlers                                  //
// -------------------------------------------------------------------------- //

/**
 * Invoked on SIGUSR1 signal, this handler prints the stack trace and then kills
 * the server process with SIGKILL. This is used in crash testing of rasserver.
 */
void testHandler(int sig, siginfo_t* info, void* ucontext);
void shutdownHandler(int sig, siginfo_t* info, void* ucontext);
void crashHandler(int sig, siginfo_t* info, void* ucontext);

void testHandler(int /*sig*/, siginfo_t* /*info*/, void* /*ucontext*/)
{
    LINFO << "test handler caught signal SIGUSR1, stacktrace: \n" 
          << common::SignalHandler::getStackTrace();
    LINFO << "killing rasserver with SIGKILL.";
    raise(SIGKILL);
}
void shutdownHandler(int /*sig*/, siginfo_t* info, void* /*ucontext*/)
{
    static bool alreadyExecuting{false};
    if (!alreadyExecuting)
    {
        alreadyExecuting = true;
        LINFO << "Interrupted by signal " << common::SignalHandler::toString(info);
        NNLINFO << "Shutting down... ";
        BLINFO << "rasserver terminated.";
        exit(EXIT_SUCCESS);
    }
}
void crashHandler(int sig, siginfo_t* info, void* /*ucontext*/)
{
    static bool alreadyExecuting{false};
    if (!alreadyExecuting)
    {
        alreadyExecuting = true;
        NNLERROR << "Interrupted by signal " << common::SignalHandler::toString(info);
        BLERROR << "... stacktrace:\n" << common::SignalHandler::getStackTrace() << "\n";
        BLFLUSH;
        NNLERROR << "Shutting down... ";
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

// -------------------------------------------------------------------------- //
//                                   main                                     //
// -------------------------------------------------------------------------- //

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

    if (!initialize())
    {
        LERROR << "Error during initialization, aborted.";
        return RC_ERROR;
    }

    //
    // run server
    //
    
    int returnCode = RC_OK;
    try
    {
        if (configuration.isRasserver())
        {
            LDEBUG << "starting daemon server...";
            rasserver::RasnetServer rasnetServer(
                        static_cast<std::uint32_t>(configuration.getListenPort()),
                        configuration.getRasmgrHost(),
                        static_cast<std::uint32_t>(configuration.getRasmgrPort()), 
                        configuration.getNewServerId());
            rasnetServer.startRasnetServer();
            LDEBUG << "daemon server started.";
        }
        else // client mode: directql or rasdl
        {
            LDEBUG << "run direct server...";
            common::LogConfiguration logConf(CONFDIR, CLIENT_LOG_CONF);
            logConf.configClientLogging(configuration.isQuietLogOn());

            if (configuration.hasQueryString())
            {
                rasserver::directql::openDatabase();
                rasserver::directql::doStuff();
                rasserver::directql::closeDatabase();
            }
            else if (configuration.usesRasdl())
            {
                rasserver::rasdl::runRasdl(argc, argv);
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
        LERROR << "rasserver: general exception.";
        returnCode = RC_ERROR;
    }

    LINFO << "rasserver terminated.";
    return returnCode;
}

bool initialize()
{
    accessControl.setServerName(configuration.getServerName());

    NNLINFO << "Server " << configuration.getServerName();
    if (configuration.isRasserver())
        BLINFO << " listening on port " << configuration.getListenPort();

    strcpy(globalConnectId, configuration.getDbConnectionID());
    BLINFO << ", connecting to " << BASEDBSTRING << " with '" << globalConnectId <<  "'";

    strcpy(globalDbUser, configuration.getDbUser());
    if (strlen(configuration.getDbUser()) > 0)
        BLINFO << ", user " << globalDbUser;
    
    strcpy(globalDbPasswd, configuration.getDbPasswd());
    BLINFO << "\n";

    NNLINFO << "Verifying rasmgr host name: " << configuration.getRasmgrHost() << "... ";
    if (!gethostbyname(configuration.getRasmgrHost()))
    {
        BLINFO << "failed\n";
        return false;
    }
    BLINFO << "ok\n";

    maxTransferBufferSize = static_cast<unsigned int>(configuration.getMaxTransferBufferSize());
    if (configuration.getTimeout() == 0)
        noTimeOut = 1;
    
    StorageLayout::DefaultTileSize = static_cast<r_Bytes>(configuration.getDefaultTileSize());
    LINFO << "Tile size set to : " << StorageLayout::DefaultTileSize;
    StorageLayout::DefaultMinimalTileSize = static_cast<r_Bytes>(configuration.getDefaultPCTMin());
    LINFO << "PCTMin set to    : " << StorageLayout::DefaultMinimalTileSize;
    StorageLayout::DefaultPCTMax = static_cast<r_Bytes>(configuration.getDefaultPCTMax());
    LINFO << "PCTMax set to    : " << StorageLayout::DefaultPCTMax;
    StorageLayout::DefaultIndexSize = static_cast<unsigned int>(configuration.getDefaultIndexSize());
    LINFO << "Index size set to: " << StorageLayout::DefaultIndexSize;

#ifdef RMANDEBUG
    RManDebug = configuration.getDebugLevel();
    LINFO << "Debug level      : " << RManDebug;
#endif

    try
    {
        StorageLayout::DefaultTileConfiguration = r_Minterval(configuration.getDefaultTileConfig());
    }
    catch (r_Error& err)
    {
        LERROR << "Failed converting " << configuration.getDefaultTileConfig() 
               << " to r_Minterval, error " << err.get_errorno() << " : " << err.what();
        return false;
    }
    LINFO << "Default tile conf: " << StorageLayout::DefaultTileConfiguration;

    // Tiling
    auto ts = get_tiling_scheme_from_name(configuration.getTilingScheme());
    if ((ts != r_NoTiling) && (ts != r_RegularTiling) && (ts != r_AlignedTiling))
    {
        LERROR << "Unsupported tiling strategy: " << configuration.getTilingScheme();
        return false;
    }
    if (ts != r_Tiling_Scheme_NUMBER)
        StorageLayout::DefaultTilingScheme = ts;
    // retiling enabled only if tiling scheme is regular tiling
    RMInit::tiling = (ts == r_RegularTiling);
    LINFO << "Default tiling   : " << StorageLayout::DefaultTilingScheme;

    // Index
    r_Index_Type tmpIT = get_index_type_from_name(configuration.getIndexType());
    if (tmpIT != r_Index_Type_NUMBER)
        StorageLayout::DefaultIndexType = tmpIT;
    LINFO << "Default Index    : " << StorageLayout::DefaultIndexType;

    //use tilecontainer
    RMInit::useTileContainer = configuration.useTileContainer();
    LINFO << "Tile container   : " << (RMInit::useTileContainer ? "yes" : "no");

    return true;
}
