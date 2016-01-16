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

using namespace std;

#include "config.h"
#include "version.h"
#ifndef RMANVERSION
#error "Please specify RMANVERSION variable!"
#endif

#ifndef COMPDATE
#error "Please specify the COMPDATE variable!"
/*
COMPDATE=`date +"%d.%m.%Y %H:%M:%S"`

and -DCOMPDATE="\"$(COMPDATE)\"" when compiling
*/
#endif


#ifdef EARLY_TEMPLATE
#define __EXECUTABLE__
#ifdef __GNUG__
#include "template_inst.hh"
#endif
#endif

#include <iostream>
#include <netdb.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string>

#include "config.h"
#include "globals.hh"   // DEFAULT_PORT
#include "servercomm/httpserver.hh"
#include "storagemgr/sstoragelayout.hh"
#include "relblobif/tilecache.hh"
#include "raslib/commonutil.hh"
#include <signal.h>

RMINITGLOBALS('C')

// from some unknown location the debug-srv.hh guard seems to be defined already, so get rid of it -- PB 2005-jan-10
#undef DEBUG_HH
#define DEBUG_MAIN debug_main


#include "debug-srv.hh"

#include "server/rasserver_config.hh"
#include "rnprotocol/rnpserver.hh"
#include "rasserver_entry.hh"

#ifdef RMANRASNET
#include "../rasserver_x/src/rasnetserver.hh"
#endif

#include <easylogging++.h>

// return codes
#define RC_OK       0
#define RC_ERROR    (-1)

bool initialization();

unsigned long maxTransferBufferSize = 4000000;
char*         dbSchema = NULL;
int           noTimeOut = 0;

// here the id string for connecting to the RDBMS is stored (used by rel* modules).
// FIXME: bad hack -- PB 2003-oct-12
char globalConnectId[256];
char globalDbUser[255] = {0};
char globalDbPasswd[255] = {0};

int  globalHTTPPort;
// do we allow for User-Defined Functions? (aka rasql routines)
bool udfEnabled = true;

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

void
crash_handler( int sig, siginfo_t* info, void * ucontext);

/**
 * Invoked on SIGUSR1 signal, this handler prints the stack trace and then kills
 * the server process with SIGKILL. This is used in crash testing of rasserver.
 */
void
test_handler( int sig, siginfo_t* info, void * ucontext);



void
crash_handler(__attribute__ ((unused)) int sig, __attribute__ ((unused)) siginfo_t* info, void * ucontext)
{
    print_stacktrace(ucontext);
    if (TileCache::cacheLimit > 0)
    {
        TileCache::clear();
    }
    if (server != NULL)
        delete server;
    server = NULL;
    LINFO << "rasserver terminated.";

    exit(SEGFAULT_EXIT_CODE);
}

void
test_handler(__attribute__ ((unused)) int sig, __attribute__ ((unused)) siginfo_t* info, void * ucontext)
{
    LINFO << "test handler caught signal SIGUSR1";
    print_stacktrace(ucontext);
    LINFO << "killing rasserver with SIGKILL.";
    raise(SIGKILL);
}

/**
  * This function is called when a SIGTERM signal is received by the process.
  * The function is placed here because it affects the global behavior of the process.
  */
void rasnetTerminationHandler(__attribute__ ((unused)) int sig, __attribute__ ((unused)) siginfo_t* info, void * ucontext)
{
    LINFO<<"Exiting server process.";

    exit(EXIT_SUCCESS);
}

_INITIALIZE_EASYLOGGINGPP

int main ( int argc, char** argv )
{
    installSigSegvHandler(crash_handler);
    installSigHandler(test_handler, SIGUSR1);

    SET_OUTPUT( true );     // enable debug output, if compiled so

    //print startup text (this line will still go into forking rasmgr's log!)
    cout << "Spawned rasserver " << RMANVERSION << " on base DBMS "  << BASEDBSTRING  << " -- generated on " << COMPDATE << "." << endl;

    if(configuration.parseCommandLine(argc, argv) == false)
    {
        LERROR << "Error: cannot parse command line.";
        return RC_ERROR;
    }

    LINFO << "rasserver: rasdaman server " << RMANVERSION << " on base DBMS "  << BASEDBSTRING  << " -- generated on " << COMPDATE << ".";
    LINFO << " Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann rasdaman GmbH. \n"
          << "Rasdaman community is free software: you can redistribute it and/or modify "
          << "it under the terms of the GNU General Public License as published by "
          << "the Free Software Foundation, either version 3 of the License, or "
          << "(at your option) any later version. \n"
          << "Rasdaman community is distributed in the hope that it will be useful, "
          << "but WITHOUT ANY WARRANTY; without even the implied warranty of "
          << "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
          << "GNU General Public License for more details. \n";

    LINFO << "To obtain a list of external packages used, please visit www.rasdaman.org.";

    if(initialization() == false )
    {
        LERROR << "Error during initialization. aborted.";
        return RC_ERROR;
    }

    //
    // body rasserver
    //

    LINFO << "Installing signal handler for ignoring broken pipe signal...";
    signal( SIGPIPE, SIG_IGN );
    LINFO << "ok";

    int returnCode = 0;
    try
    {
        LDEBUG << "selecting server type...";

        if(configuration.isRnpServer())
        {
            LDEBUG << "startRnpServer()...";
            startRnpServer();
            LDEBUG << "startRnpServer() done.";
        }
        else if(configuration.isHttpServer())
        {
            LDEBUG << "initializing HttpServer()...";
            server = new HttpServer( clientTimeOut, managementInterval, static_cast<unsigned int>(serverListenPort), const_cast<char*>(rasmgrHost), static_cast<unsigned int>(rasmgrPort), const_cast<char*>(serverName));
        }
#ifdef RMANRASNET
        else if(configuration.isRasnetServer())
        {
            installSigHandler(rasnetTerminationHandler, SIGTERM);

            //start rasnet server
            rasserver::RasnetServer rasnetServer(configuration);
            rasnetServer.startRasnetServer();
        }
#endif
        else
        {
            LDEBUG << "initializing ServerComm() (ie: RPC)...";
            server = new ServerComm( clientTimeOut, managementInterval, static_cast<unsigned int>(serverListenPort), const_cast<char*>(rasmgrHost), static_cast<unsigned int>(rasmgrPort), const_cast<char*>(serverName));
        }

        // in case of HTTP or RPC server: launch previously generated object
        if(server)
        {
            LDEBUG << "server->startRpcServer()...";
            server->startRpcServer();
            LDEBUG << "server->startRpcServer() done.";
        }
    }
    catch ( r_Error& errorObj )
    {
        LDEBUG << "Error: encountered " << errorObj.get_errorno() << ": " << errorObj.what();
        LERROR << "Error: encountered " << errorObj.get_errorno() << ": " << errorObj.what();
        returnCode = RC_ERROR;
    }
    catch (std::exception &ex)
    {
        LERROR << "Error encounter: " << ex.what();
        LDEBUG << "std::exception: " << ex.what();
        returnCode = RC_ERROR;
    }
    catch(...)
    {
        LDEBUG << "rasserver: general exception";
        LERROR << "rasserver: general exception";
        returnCode = RC_ERROR;
    }

    if(server)
        delete server;
    server = NULL;

    if( dbSchema )
        free( dbSchema );
    dbSchema = NULL;

    LINFO << "rasserver terminated.";
    return returnCode;
}

bool initialization()
{
    serverName = configuration.getServerName();
    accessControl.setServerName(serverName);

    serverListenPort = globalHTTPPort = configuration.getListenPort();

    LINFO << "Server " << serverName << " of type ";

    if(configuration.isRnpServer())
        LINFO << "RNP, listening on port " << serverListenPort;
    else if(configuration.isHttpServer())
        LINFO << "HTTP, listening on port " << serverListenPort;
    else
        LINFO << "RPC, registered with prognum 0x" << hex <<serverListenPort << dec;

    //  globalConnectId         = configuration.getDbConnectionID();
    strcpy(globalConnectId,configuration.getDbConnectionID());
    LINFO << ", connecting to " << BASEDBSTRING << " as '" << globalConnectId <<  "'";

    strcpy(globalDbUser,configuration.getDbUser());
    if (strlen(configuration.getDbUser()) > 0)
    {
        LINFO << ", user " << globalDbUser;
    }
    strcpy(globalDbPasswd,configuration.getDbPasswd());

    rasmgrHost = configuration.getRasmgrHost();
    rasmgrPort = configuration.getRasmgrPort();

    LINFO << "Verifying rasmgr host name: " << rasmgrHost << "...";
    if(!gethostbyname(rasmgrHost))
    {
        LINFO << "failed";
        if(!configuration.isLogToStdOut())  LINFO << "failed";
        return false;
    }
    LINFO << "ok";

    maxTransferBufferSize = static_cast<unsigned int>(configuration.getMaxTransferBufferSize());

    clientTimeOut = static_cast<unsigned int>(configuration.getTimeout());
    if(clientTimeOut == 0)
        noTimeOut = 1;

    managementInterval = clientTimeOut/4;

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
    LINFO<<"Debug level: " << RManDebug;
#endif

    try
    {
        StorageLayout::DefaultTileConfiguration = r_Minterval(configuration.getDefaultTileConfig());
    }
    catch(r_Error& err)
    {
        LERROR << "Error converting " << configuration.getDefaultTileConfig() << " to r_Minterval";
        LERROR << "Error " << err.get_errorno() << " : " << err.what();
        if(!configuration.isLogToStdOut())
        {
            LERROR << "Error converting " << configuration.getDefaultTileConfig() << " to r_Minterval";
            LERROR << "Error " << err.get_errorno() << " : " << err.what();
        }
        return false;
    }
    LINFO << "Default Tile Conf: " << StorageLayout::DefaultTileConfiguration;

    // Tiling
    r_Tiling_Scheme tmpTS=get_tiling_scheme_from_name(configuration.getTilingScheme());

    if(tmpTS != r_Tiling_Scheme_NUMBER)
        StorageLayout::DefaultTilingScheme = tmpTS;

    if((tmpTS != r_NoTiling) && (tmpTS != r_RegularTiling) && (tmpTS != r_AlignedTiling))
    {
        LERROR << "Error: unsupported tiling strategy: " << configuration.getTilingScheme();
        if(configuration.isLogToStdOut())
        {
            LERROR << "Error: unsupported tiling strategy: " << configuration.getTilingScheme();
        }
        return false;
    }

    //retiling enabled only if tiling scheme is regular tiling
    RMInit::tiling = (tmpTS == r_RegularTiling);
    LINFO << "Default Tiling   : " << StorageLayout::DefaultTilingScheme;

    // Index
    r_Index_Type tmpIT = get_index_type_from_name(configuration.getIndexType());
    if ( tmpIT != r_Index_Type_NUMBER)
        StorageLayout::DefaultIndexType = tmpIT;
    LINFO << "Default Index    : " << StorageLayout::DefaultIndexType;

    //use tilecontainer
    RMInit::useTileContainer = configuration.useTileContainer();
    LINFO << "Use Tile Container: " << RMInit::useTileContainer;

    //set cache size limit
    TileCache::cacheLimit = configuration.getCacheLimit();
    LINFO << "Cache size limit  : " << TileCache::cacheLimit;

    return true;
}

