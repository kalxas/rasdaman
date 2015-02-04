/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#include <limits.h>

#include "../../include/globals.hh"
#include "../../common/src/crypto/crypto.hh"

#include "messages/rasmgrmess.pb.h"

#include "userdatabaserights.hh"
#include "useradminrights.hh"
#include "usermanager.hh"
#include "databasehostmanager.hh"
#include "databasemanager.hh"
#include "servermanager.hh"
#include "servermanagementservice.hh"
#include "rascontrol.hh"
#include "controlservice.hh"
#include "clientmanager.hh"
#include "clientmanagementservice.hh"
#include "rasmanager.hh"
#include "controlcommandexecutor.hh"
#include "rasmgrconfig.hh"
#include "clientmanagerconfig.hh"

namespace rasmgr
{
using boost::shared_ptr;

RasManager::RasManager ( rasmgr::Configuration &config )
{
    this->port = config.getPort();
    RasMgrConfig::getInstance()->setRasMgrPort ( this->port );
    this->running = false;
}

RasManager::~RasManager()
{}

void RasManager::start()
{
    LINFO<<"Starting rasmanager.";


    boost::shared_ptr<rasmgr::UserManager> userManager ( new rasmgr::UserManager() );

	//This makes sure that we have an authentication file.
    //TODO:
    //userManager->saveUserInformation();

    shared_ptr<DatabaseHostManager> dbhManager ( new DatabaseHostManager() );
    shared_ptr<DatabaseManager> dbManager ( new DatabaseManager ( dbhManager ) );
    shared_ptr<ServerManager> serverManager ( new ServerManager ( dbhManager ) );

    ClientManagerConfig clientManagerConfig(3000,3000);
    shared_ptr<ClientManager> clientManager ( new ClientManager ( userManager,clientManagerConfig) );

    shared_ptr<RasControl> rascontrol ( new RasControl ( userManager, dbhManager, dbManager,serverManager , this) );

    shared_ptr<ControlCommandExecutor> commandExecutor ( new ControlCommandExecutor ( rascontrol ) );

    boost::shared_ptr<rasnet::service::RasMgrRasServerService> serverManagementService ( new rasmgr::ServerManagementService ( serverManager ) );
    boost::shared_ptr<rasnet::service::RasMgrRasCtrlService> rasctrlService ( new rasmgr::ControlService ( commandExecutor ) );
    boost::shared_ptr<rasnet::service::RasMgrClientService> clientService ( new rasmgr::ClientManagementService ( clientManager, serverManager ) );

    this->serviceManager.reset ( new rasnet::ServiceManager() );

    this->serviceManager->addService ( clientService );
    this->serviceManager->addService ( serverManagementService );
    this->serviceManager->addService ( rasctrlService );

    //Load the configuration from rasmgr.conf and initialize this rasmgr instance
    this->loadRasmgrConf ( commandExecutor );

    //TODO-AT: Factor out this tcp
    this->running=true;
    this->serviceManager->serve ( "tcp://*", this->port );


    while ( this->running )
    {
        boost::this_thread::sleep ( boost::posix_time::seconds ( 5 ) );
    }
}

void RasManager::stop()
{
    this->running = false;
    LINFO<<"Stopping rasmanager.";
}


void RasManager::loadRasmgrConf ( shared_ptr<ControlCommandExecutor> commandExecutor )
{
    char configFileName[PATH_MAX];
    char inBuffer[MAXMSG];
    configFileName[0]=0;

    if ( strlen ( CONFDIR ) +strlen ( RASMGR_CONF_FILE ) +2 > PATH_MAX )
    {
        LWARNING << "The path to the configuration file is longer than the maximum file system path. Initialization might fail";
    }

    //TODO: If the total path is longer than PATH_MAX, a segfault might be possible.
    //This is legacy code so I leave it unchanged for now.
    sprintf ( configFileName, "%s/%s", CONFDIR, RASMGR_CONF_FILE );

    LDEBUG<<"Opening rasmanager configuration file"<<std::string ( configFileName );

    std::ifstream ifs ( configFileName );   // open config file

    if ( !ifs )
    {
        LERROR<<"Could not open rasmanager initialization file:"<<std::string ( configFileName );
    }
    else
    {
        while ( !ifs.eof() )
        {
            ifs.getline ( inBuffer,MAXMSG );

            std::string result = commandExecutor->sudoExecuteCommand ( std::string ( inBuffer ) );

            //Only error messages are non-empty
            if ( !result.empty() )
            {
                LERROR<<result;
            }
        }
    }
}
}
