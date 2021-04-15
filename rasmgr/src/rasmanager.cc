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

#include "include/globals.hh"
#include "common/crypto/crypto.hh"
#include "common/grpc/grpcutils.hh"
#include "common/grpc/healthserviceimpl.hh"
#include <logging.hh>
#include "rasnet/messages/rasmgr_rasctrl_service.grpc.pb.h"
#include "rasnet/messages/rasmgr_rassrvr_service.grpc.pb.h"
#include "rasnet/messages/rasmgr_client_service.grpc.pb.h"
#include "rasmgr/src/messages/rasmgrmess.pb.h"

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
#include "common/grpc/grpcutils.hh"
#include "common/exceptions/resourcebusyexception.hh"

#include "rasmanager.hh"

namespace rasmgr
{
using std::shared_ptr;

RasManager::RasManager(rasmgr::Configuration &config
                       )
  : running{false}, port{config.getPort()}
{
    RasMgrConfig::getInstance()->setRasMgrPort(std::int32_t(this->port));
}

RasManager::~RasManager()
{}

void RasManager::start()
{
    LINFO << "Starting rasmanager.";

    if (common::GrpcUtils::isPortBusy(DEFAULT_HOSTNAME, this->port))
    {
        throw common::ResourceBusyException(
            "Failed to start rasmanager on port " + std::to_string(this->port) + ": address is already in use.");
    }

    std::shared_ptr<DatabaseHostManager> dbhManager(new DatabaseHostManager());
    std::shared_ptr<DatabaseManager> dbManager(new DatabaseManager(dbhManager));
    std::shared_ptr<rasmgr::UserManager> userManager(new rasmgr::UserManager());
    userManager->setDatabaseManager(dbManager);

    ServerManagerConfig serverMgrConfig;
    std::shared_ptr<ServerFactory> serverFactory(new ServerFactoryRasNet());
    std::shared_ptr<ServerGroupFactory> serverGroupFactory(new ServerGroupFactoryImpl(dbhManager, serverFactory));
    std::shared_ptr<ServerManager> serverManager(new ServerManager(serverMgrConfig,  serverGroupFactory));

    std::shared_ptr<PeerManager> peerManager(new PeerManager());

    ClientManagerConfig clientManagerConfig;
    std::shared_ptr<ClientManager> clientManager(new ClientManager(clientManagerConfig, userManager, serverManager, peerManager));

    std::shared_ptr<RasControl> rascontrol(new RasControl(userManager, dbhManager, dbManager, serverManager, peerManager, this));

    std::shared_ptr<ControlCommandExecutor> commandExecutor(new ControlCommandExecutor(rascontrol));

    this->configManager.reset(new ConfigurationManager(commandExecutor, dbhManager, dbManager, peerManager, serverManager, userManager));
    LINFO << "Loading rasmgr configuration.";
    this->configManager->loadConfiguration();
    LINFO << "Finished loading rasmgr configuration.";

    std::shared_ptr<rasnet::service::RasMgrRasServerService::Service> serverManagementService(new rasmgr::ServerManagementService(serverManager));
    std::shared_ptr<rasnet::service::RasMgrRasCtrlService::Service> rasctrlService(new rasmgr::ControlService(commandExecutor));
    std::shared_ptr<rasnet::service::RasmgrClientService::Service> clientService(new rasmgr::ClientManagementService(clientManager));
    std::shared_ptr<rasnet::service::RasmgrRasmgrService::Service> rasmgrService(new rasmgr::RasmgrService(clientManager));

    //The health service will only be used to report on the health of the server.
    std::shared_ptr<common::HealthServiceImpl> healthService(new common::HealthServiceImpl());

    std::string serverAddress = common::GrpcUtils::constructAddressString(ALL_IP_ADDRESSES,  this->port);
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

    if (this->server == nullptr)
    {
        throw common::Exception("Failed to start rasmanager on port " + std::to_string(this->port) + ".");
    }
    // Wait for the server to shutdown. Note that some other thread must be
    // responsible for shutting down the server for this call to ever return.
    server->Wait();
}

void RasManager::stop()
{
    if (this->running)
    {
        this->configManager->saveConfiguration(true);

        this->server->Shutdown();
        LINFO << "Stopping rasmanager.";
    }
}

void RasManager::saveConfiguration()
{
    this->configManager->saveConfiguration();
}

void RasManager::setIsConfigurationDirty(bool isDirty)
{
    this->configManager->setIsDirty(isDirty);
}
}
