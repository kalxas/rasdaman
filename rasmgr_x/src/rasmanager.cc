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

#include <memory>

#include <grpc++/grpc++.h>
#include <grpc/support/time.h>

#include "../../include/globals.hh"
#include "../../common/src/crypto/crypto.hh"
#include "../../common/src/grpc/grpcutils.hh"
#include "../../common/src/grpc/healthserviceimpl.hh"
#include "../../common/src/logging/easylogging++.hh"
#include "../../rasnet/messages/rasmgr_rasctrl_service.grpc.pb.h"
#include "../../rasnet/messages/rasmgr_rassrvr_service.grpc.pb.h"
#include "../../rasnet/messages/rasmgr_client_service.grpc.pb.h"
#include "messages/rasmgrmess.pb.h"

#include "clientmanagementservice.hh"
#include "clientmanagerconfig.hh"
#include "clientmanager.hh"
#include "configuration.hh"
#include "configurationmanager.hh"
#include "controlcommandexecutor.hh"
#include "controlservice.hh"
#include "databasehostmanager.hh"
#include "databasemanager.hh"
#include "peermanager.hh"
#include "rascontrol.hh"
#include "rasmgrconfig.hh"
#include "rasmgrservice.hh"
#include "serverfactoryrasnet.hh"
#include "servergroupfactoryimpl.hh"
#include "servermanagementservice.hh"
#include "servermanagerconfig.hh"
#include "servermanager.hh"
#include "usermanager.hh"

#include "rasmanager.hh"

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

    shared_ptr<PeerManager> peerManager (new PeerManager());

    ClientManagerConfig clientManagerConfig;
    shared_ptr<ClientManager> clientManager ( new ClientManager ( clientManagerConfig, userManager, serverManager, peerManager));

    shared_ptr<RasControl> rascontrol ( new RasControl ( userManager, dbhManager, dbManager, serverManager, peerManager, this) );

    shared_ptr<ControlCommandExecutor> commandExecutor ( new ControlCommandExecutor ( rascontrol ) );

    this->configManager.reset(new ConfigurationManager(commandExecutor, userManager));
    LINFO<<"Loading rasmgr configuration.";
    this->configManager->loadConfiguration();
    LINFO<<"Finished loading rasmgr configuration.";


    boost::shared_ptr<rasnet::service::RasMgrRasServerService::Service> serverManagementService ( new rasmgr::ServerManagementService ( serverManager ) );
    boost::shared_ptr<rasnet::service::RasMgrRasCtrlService::Service> rasctrlService ( new rasmgr::ControlService ( commandExecutor ) );
    boost::shared_ptr<rasnet::service::RasMgrClientService::Service> clientService ( new rasmgr::ClientManagementService ( clientManager) );
    boost::shared_ptr<rasnet::service::RasmgrRasmgrService::Service> rasmgrService(new rasmgr::RasmgrService(clientManager));

    //The health service will only be used to report on the health of the server.
    boost::shared_ptr<common::HealthServiceImpl> healthService(new common::HealthServiceImpl());


    std::string serverAddress = common::GrpcUtils::constructAddressString(DEFAULT_HOSTNAME,  this->port);
    //GreeterServiceImpl service;

    grpc::ServerBuilder builder;
    // Listen on the given address without any authentication mechanism.
    builder.AddListeningPort(serverAddress, grpc::InsecureServerCredentials());

    // Register "service" as the instance through which we'll communicate with
    // clients. In this case it corresponds to an *synchronous* service.
    builder.RegisterService(clientService.get());
    builder.RegisterService(serverManagementService.get());
    builder.RegisterService(rasctrlService.get());
    builder.RegisterService(rasmgrService.get());

    builder.RegisterService(healthService.get());

    this->running = true;
    // Finally assemble the server.
    this->server = builder.BuildAndStart();

    // Wait for the server to shutdown. Note that some other thread must be
    // responsible for shutting down the server for this call to ever return.
    server->Wait();
}

void RasManager::stop()
{
    if(this->running)
    {
        this->configManager->saveConfiguration();
        //TODO: Check shutdown
        this->server->Shutdown();
        LINFO<<"Stopping rasmanager.";
    }
}

void RasManager::saveConfiguration()
{
    this->configManager->saveConfiguration();
}
}
