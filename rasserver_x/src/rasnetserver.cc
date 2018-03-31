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


#include <iostream>

#include <grpc++/grpc++.h>
#include <chrono>

#include "rasnet/messages/rasmgr_rassrvr_service.grpc.pb.h"

#include <logging.hh>
#include "common/src/grpc/grpcutils.hh"
#include "common/src/exceptions/rasexceptions.hh"
#include "rasnetprotocol/rasnetservercomm.hh"

#include "server/rasserver_entry.hh"
#include "include/globals.hh"

#include "clientmanager.hh"

#include "rasnetserver.hh"

namespace rasserver
{

using boost::shared_ptr;
using boost::scoped_ptr;
using common::ConnectionFailedException;
using common::GrpcUtils;
using common::HealthServiceImpl;


RasnetServer::RasnetServer(const Configuration& configuration):
    isRunning(false),
    configuration(configuration)
{
    shared_ptr<ClientManager> clientManager(new ClientManager());

    rasserverService.reset(new RasServerServiceImpl(clientManager));
    clientServerService.reset(new RasnetServerComm(clientManager));
    healthServiceImpl.reset(new HealthServiceImpl());
}


void RasnetServer::startRasnetServer()
{
    std::string serverAddress = GrpcUtils::constructAddressString("0.0.0.0",  boost::uint32_t(configuration.getListenPort()));

    grpc::ServerBuilder builder;
    // Listen on the given address without any authentication mechanism.
    builder.AddListeningPort(serverAddress, grpc::InsecureServerCredentials());
    builder.RegisterService(rasserverService.get());
    builder.RegisterService(clientServerService.get());
    builder.RegisterService(healthServiceImpl.get());

    RasServerEntry& rasserver = RasServerEntry::getInstance();
    rasserver.compat_connectToDBMS();

    this->isRunning = true;
    // Finally assemble the server.

    LDEBUG << "Starting server on:" << serverAddress;
    this->server = builder.BuildAndStart();

    // Register the server
    this->registerServerWithRasmgr();

    // Wait for the server to shutdown. Note that some other thread must be
    // responsible for shutting down the server for this call to ever return.
    this->server->Wait();
}

void RasnetServer::registerServerWithRasmgr()
{
    std::string rasmgrAddress = GrpcUtils::constructAddressString(configuration.getRasmgrHost(), boost::uint32_t(configuration.getRasmgrPort()));;
    std::shared_ptr<grpc::Channel> channel(grpc::CreateCustomChannel(rasmgrAddress, grpc::InsecureChannelCredentials(), GrpcUtils::getDefaultChannelArguments()));

    ::rasnet::service::RasMgrRasServerService::Stub rasmgrRasserverService(channel);
    boost::shared_ptr<common::HealthService::Stub> healthService(new common::HealthService::Stub(channel));

    ::rasnet::service::Void response;
    ::rasnet::service::RegisterServerReq request;
    request.set_serverid(configuration.getNewServerId());

    if (!GrpcUtils::isServerAlive(healthService, SERVICE_CALL_TIMEOUT))
    {
        throw ConnectionFailedException("rasserver failed to connect to rasmgr.");
    }

    grpc::ClientContext context;
    grpc::Status status = rasmgrRasserverService.RegisterServer(&context, request, &response);

    if (!status.ok())
    {
        //TODO-GM: Throw the appropriate exception
        LERROR << status.error_message();
        throw std::runtime_error("Could not register server with rasmgr.");
    }
}

}
