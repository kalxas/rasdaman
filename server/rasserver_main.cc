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
#include "raslib/rmdebug.hh"

#include "server/rasserver_config.hh"
#include "rnprotocol/rnpserver.hh"
#include "rasserver_entry.hh"

#ifdef RMANRASNET
#include "rasserver_x/src/rasnetserver.hh"
#include "common/src/logging/easylogging++.hh"
#endif

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



void
crash_handler(__attribute__ ((unused)) int sig, __attribute__ ((unused)) siginfo_t* info, void * ucontext) {
  ENTER( "crash_handler");

  print_stacktrace(ucontext);
  if (TileCache::cacheLimit > 0)
  {
    TileCache::clear();
  }
  if (server != NULL)
    delete server;
  server = NULL;
  RMInit::logOut << endl << "rasserver terminated." << endl;

  LEAVE("crash_handler");
  exit(SEGFAULT_EXIT_CODE);
}

#ifdef RMANRASNET
    _INITIALIZE_EASYLOGGINGPP
#endif

int main ( int argc, char** argv )
{

#ifdef RMANRASNET
    easyloggingpp::Configurations defaultConf;
    defaultConf.setToDefault();
    defaultConf.set(easyloggingpp::Level::Error,
                    easyloggingpp::ConfigurationType::Format,
                    "%datetime %level %loc %log %func ");
    easyloggingpp::Loggers::reconfigureAllLoggers(defaultConf);
#endif

    installSigSegvHandler(crash_handler);

    SET_OUTPUT( true );     // enable debug output, if compiled so
    ENTER( "rasserver.main()" );

    //print startup text (this line will still go into forking rasmgr's log!)
    cout << "Spawned rasserver " << RMANVERSION << " on base DBMS "  << BASEDBSTRING  << " -- generated on " << COMPDATE << "." << endl;

    if(configuration.parseCommandLine(argc, argv) == false)
    {
        RMInit::logOut << "Error: cannot parse command line." << endl;
        LEAVE( "rasserver.main(): Error parsing command line." );
        return RC_ERROR;
    }

    RMInit::logOut << "rasserver: rasdaman server " << RMANVERSION << " on base DBMS "  << BASEDBSTRING  << " -- generated on " << COMPDATE << "." << endl;
    RMInit::logOut << " Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann rasdaman GmbH." << std::endl
                   << "Rasdaman community is free software: you can redistribute it and/or modify "
                   << "it under the terms of the GNU General Public License as published by "
                   << "the Free Software Foundation, either version 3 of the License, or "
                   << "(at your option) any later version. \n"
                   << "Rasdaman community is distributed in the hope that it will be useful, "
                   << "but WITHOUT ANY WARRANTY; without even the implied warranty of "
                   << "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
                   << "GNU General Public License for more details. \n\n";

    RMInit::logOut << "To obtain a list of external packages used, please visit www.rasdaman.org." << endl;

    if(initialization() == false )
    {
        RMInit::logOut << "Error during initialization. aborted." << endl;
        LEAVE( "rasserver.main(): Error during initialization." );
        return RC_ERROR;
    }

    //
    // body rasserver
    //

    RMInit::logOut << "Installing signal handler for ignoring broken pipe signal..." << flush;
    signal( SIGPIPE, SIG_IGN );
    RMInit::logOut << "ok" << endl;

    int returnCode = 0;
    try
    {
        TALK( "selecting server type..." );

        if(configuration.isRnpServer())
        {
            TALK( "startRnpServer()..." );
            startRnpServer();
            TALK( "startRnpServer() done." );
        }
        else if(configuration.isHttpServer())
        {
            TALK( "initializing HttpServer()..." );
            server = new HttpServer( clientTimeOut, managementInterval, static_cast<unsigned int>(serverListenPort), const_cast<char*>(rasmgrHost), static_cast<unsigned int>(rasmgrPort), const_cast<char*>(serverName));
        }
#ifdef RMANRASNET
        else if(configuration.isRasnetServer())
        {
            //start rasnet server
            RasnetServer rasnetServer(configuration);
            rasnetServer.startRasnetServer();
        }
#endif
        else
        {
            TALK( "initializing ServerComm() (ie: RPC)..." );
            server = new ServerComm( clientTimeOut, managementInterval, static_cast<unsigned int>(serverListenPort), const_cast<char*>(rasmgrHost), static_cast<unsigned int>(rasmgrPort), const_cast<char*>(serverName));
        }

        // in case of HTTP or RPC server: launch previously generated object
        if(server)
        {
            TALK( "server->startRpcServer()..." );
            server->startRpcServer();
            TALK( "server->startRpcServer() done." );
        }
    }
    catch ( r_Error& errorObj )
    {
        TALK( "Error: encountered " << errorObj.get_errorno() << ": " << errorObj.what() );
        RMInit::logOut << "Error: encountered " << errorObj.get_errorno() << ": " << errorObj.what() << endl;
        returnCode = RC_ERROR;
    }
    catch(...)
    {
        TALK( "rasserver: general exception" );
        RMInit::logOut << "rasserver: general exception" << endl;
        returnCode = RC_ERROR;
    }

    if(server)
        delete server;
    server = NULL;

    if( dbSchema )
        free( dbSchema );
    dbSchema = NULL;

    LEAVE( "rasserver.main(): -> " << returnCode );
    RMInit::logOut << "rasserver terminated." << std::endl;
    return returnCode;
}

bool initialization()
{
    serverName = configuration.getServerName();
    accessControl.setServerName(serverName);

    serverListenPort = globalHTTPPort = configuration.getListenPort();

    RMInit::logOut<<"Server "<< serverName << " of type ";

    if(configuration.isRnpServer())
        RMInit::logOut << "RNP, listening on port " << serverListenPort << flush;
    else if(configuration.isHttpServer())
        RMInit::logOut << "HTTP, listening on port " << serverListenPort << flush;
    else
        RMInit::logOut << "RPC, registered with prognum 0x" << hex <<serverListenPort << dec << flush;

    //  globalConnectId         = configuration.getDbConnectionID();
    strcpy(globalConnectId,configuration.getDbConnectionID());
    RMInit::logOut << ", connecting to " << BASEDBSTRING << " as '" << globalConnectId <<  "'";

    strcpy(globalDbUser,configuration.getDbUser());
    if (strlen(configuration.getDbUser()) > 0)
    {
        RMInit::logOut << ", user " << globalDbUser;
    }
    RMInit::logOut << ".";
    strcpy(globalDbPasswd,configuration.getDbPasswd());

    rasmgrHost = configuration.getRasmgrHost();
    rasmgrPort = configuration.getRasmgrPort();

    RMInit::logOut << "Verifying rasmgr host name: " << rasmgrHost << "...";
    if(!gethostbyname(rasmgrHost))
    {
        RMInit::logOut << "failed" << endl;
        if(!configuration.isLogToStdOut())  RMInit::logOut << "failed" << endl;
        return false;
    }
    RMInit::logOut << "ok" << endl;

    maxTransferBufferSize = static_cast<unsigned int>(configuration.getMaxTransferBufferSize());

    clientTimeOut = static_cast<unsigned int>(configuration.getTimeout());
    if(clientTimeOut == 0)
        noTimeOut = 1;

    managementInterval = clientTimeOut/4;

    //tilesize
    StorageLayout::DefaultTileSize = static_cast<unsigned int>(configuration.getDefaultTileSize());
    RMInit::logOut << "Tile size set to : " << StorageLayout::DefaultTileSize << endl;

    //pctmin
    StorageLayout::DefaultMinimalTileSize = static_cast<unsigned int>(configuration.getDefaultPCTMin());
    RMInit::logOut << "PCTMin set to     : " << StorageLayout::DefaultMinimalTileSize << endl;

    //pctmax
    StorageLayout::DefaultPCTMax = static_cast<unsigned int>(configuration.getDefaultPCTMax());
    RMInit::logOut << "PCTMax set to     : " << StorageLayout::DefaultPCTMax << endl;

    //indexsize
    StorageLayout::DefaultIndexSize = static_cast<unsigned int>(configuration.getDefaultIndexSize());
    RMInit::logOut << "IndexSize set to : " << StorageLayout::DefaultIndexSize << endl;

#ifdef RMANDEBUG
    RManDebug = configuration.getDebugLevel();
    RMInit::logOut<<"Debug level: " << RManDebug << endl;
#endif

    try
    {
        StorageLayout::DefaultTileConfiguration = r_Minterval(configuration.getDefaultTileConfig());
    }
    catch(r_Error& err)
    {
        RMInit::logOut << "Error converting " << configuration.getDefaultTileConfig() << " to r_Minterval" << endl;
        RMInit::logOut << "Error " << err.get_errorno() << " : " << err.what() << endl;
        if(!configuration.isLogToStdOut())
        {
            RMInit::logOut << "Error converting " << configuration.getDefaultTileConfig() << " to r_Minterval" << endl;
            RMInit::logOut << "Error " << err.get_errorno() << " : " << err.what() << endl;
        }
        return false;
    }
    RMInit::logOut << "Default Tile Conf: " << StorageLayout::DefaultTileConfiguration << endl;

    // Tiling
    r_Tiling_Scheme tmpTS=get_tiling_scheme_from_name(configuration.getTilingScheme());

    if(tmpTS != r_Tiling_Scheme_NUMBER)
        StorageLayout::DefaultTilingScheme = tmpTS;

    if((tmpTS != r_NoTiling) && (tmpTS != r_RegularTiling) && (tmpTS != r_AlignedTiling))
    {
        RMInit::logOut << "Error: unsupported tiling strategy: " << configuration.getTilingScheme() << endl;
        if(configuration.isLogToStdOut())
        {
            RMInit::logOut << "Error: unsupported tiling strategy: " << configuration.getTilingScheme() << endl;
        }
        return false;
    }

    //retiling enabled only if tiling scheme is regular tiling
    RMInit::tiling = (tmpTS == r_RegularTiling);
    RMInit::logOut << "Default Tiling   : " << StorageLayout::DefaultTilingScheme << endl;

    // Index
    r_Index_Type tmpIT = get_index_type_from_name(configuration.getIndexType());
    if ( tmpIT != r_Index_Type_NUMBER)
        StorageLayout::DefaultIndexType = tmpIT;
    RMInit::logOut << "Default Index    : " << StorageLayout::DefaultIndexType << endl;

    //use tilecontainer
    RMInit::useTileContainer = configuration.useTileContainer();
    RMInit::logOut << "Use Tile Container: " << RMInit::useTileContainer << endl;
    
    //set cache size limit
    TileCache::cacheLimit = configuration.getCacheLimit();
    RMInit::logOut << "Cache size limit  : " << TileCache::cacheLimit << endl;

    return true;
}

