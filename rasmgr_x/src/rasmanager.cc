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
#include "servergroupfactoryimpl.hh"
#include "serverfactoryrasnet.hh"
#include "rasnet/src/util/proto/zmqutil.hh"

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

    shared_ptr<DatabaseHostManager> dbhManager ( new DatabaseHostManager() );
    shared_ptr<DatabaseManager> dbManager ( new DatabaseManager ( dbhManager ) );
    boost::shared_ptr<rasmgr::UserManager> userManager ( new rasmgr::UserManager() );

    ServerManagerConfig serverMgrConfig;
    boost::shared_ptr<ServerFactory> serverFactory(new ServerFactoryRasNet());
    boost::shared_ptr<ServerGroupFactory> serverGroupFactory(new ServerGroupFactoryImpl(dbhManager, serverFactory));
    shared_ptr<ServerManager> serverManager ( new ServerManager ( serverMgrConfig,  serverGroupFactory) );

    ClientManagerConfig clientManagerConfig;
    shared_ptr<ClientManager> clientManager ( new ClientManager ( clientManagerConfig, userManager));

    shared_ptr<RasControl> rascontrol ( new RasControl ( userManager, dbhManager, dbManager,serverManager , this) );

    shared_ptr<ControlCommandExecutor> commandExecutor ( new ControlCommandExecutor ( rascontrol ) );

    this->configManager.reset(new ConfigurationManager(commandExecutor, userManager));
    LINFO<<"Loading rasmgr configuration.";
    this->configManager->loadConfiguration();
    LINFO<<"Finished loading rasmgr configuration.";

    boost::shared_ptr<rasnet::service::RasMgrRasServerService> serverManagementService ( new rasmgr::ServerManagementService ( serverManager ) );
    boost::shared_ptr<rasnet::service::RasMgrRasCtrlService> rasctrlService ( new rasmgr::ControlService ( commandExecutor ) );
    boost::shared_ptr<rasnet::service::RasMgrClientService> clientService ( new rasmgr::ClientManagementService ( clientManager, serverManager ) );

    this->serviceManager.reset ( new rasnet::ServiceManager(1, 2) );

    this->serviceManager->addService ( clientService );
    this->serviceManager->addService ( serverManagementService );
    this->serviceManager->addService ( rasctrlService );

    this->running=true;
    this->serviceManager->serve (rasnet::ZmqUtil::ALL_LOCAL_INTERFACES, this->port );

    while ( this->running )
    {
        boost::this_thread::sleep ( boost::posix_time::seconds ( 5 ) );
    }
}

void RasManager::stop()
{
    if(this->running)
    {
        this->configManager->saveConfiguration();
        this->running = false;
        LINFO<<"Stopping rasmanager.";
    }
}

void RasManager::saveConfiguration()
{
    this->configManager->saveConfiguration();
}
}
