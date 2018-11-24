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

#include <fstream>

#include <boost/cstdint.hpp>
#include <boost/thread/locks.hpp>

#include <logging.hh>
#include "common/uuid/uuid.hh"
#include "common/grpc/grpcutils.hh"

#include "rasnet/messages/rasmgr_client_service.grpc.pb.h"
#include "rasnet/messages/rasmgr_client_service.pb.h"

#include "clientmanager.hh"
#include "constants.hh"

#include "clientcredentials.hh"
#include "clientmanagementservice.hh"
#include "rasmgrconfig.hh"
#include "server.hh"
#include "user.hh"

namespace rasmgr
{
using boost::mutex;
using boost::shared_ptr;
using boost::unique_lock;

using common::GrpcUtils;

using grpc::Status;

using std::string;

ClientManagementService::ClientManagementService(boost::shared_ptr<ClientManager> clientManager)
    : clientManager(clientManager)
{}

ClientManagementService::~ClientManagementService()
{}


grpc::Status ClientManagementService::Connect(__attribute__ ((unused)) grpc::ServerContext* context, const rasnet::service::ConnectReq* request, rasnet::service::ConnectRepl* response)
{
    grpc::Status status = Status::OK;
    /**
     * 1. Use the client manager to register the client and obtain a UUID for the client
     * 2. Set the UUID in the response so that it is sent back to the client.
     */
    try
    {
        LDEBUG << "Connecting client...";
        std::string out_clientUUID;

        //Create the ClientCredentials object used for authentication.
        ClientCredentials credentials(request->username(),
                                      request->passwordhash());

        //Try to authenticate the client and assign the client an ID.
        //If the authentication fails, an exception is thrown
        this->clientManager->connectClient(credentials, out_clientUUID);

        response->set_clientid(common::UUID::generateIntId());
        response->set_clientuuid(out_clientUUID);
        response->set_keepalivetimeout(this->clientManager->getConfig().getClientLifeTime());

        LDEBUG << "Client connected, assigned ID " << response->clientuuid();
    }
    catch (std::exception& ex)
    {
        LERROR << "Failed connecting client: " << ex.what();
        status  = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        LERROR << "Failed connecting client";
        status = GrpcUtils::convertExceptionToStatus("Failed connecting client");
    }

    return status;
}

grpc::Status ClientManagementService::Disconnect(__attribute__ ((unused)) grpc::ServerContext* context, 
        const rasnet::service::DisconnectReq* request, 
        __attribute__ ((unused)) rasnet::service::Void* response)
{
    grpc::Status status = Status::OK;
    try
    {
        LDEBUG << "Disconnect client " << request->clientuuid();
        this->clientManager->disconnectClient(request->clientuuid());
    }
    catch (std::exception& ex)
    {
        LERROR << "Failed disconnecting client: " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        LERROR << "Failed disconnecting client";
        status = GrpcUtils::convertExceptionToStatus("Failed disconnecting client");
    }
    return status;
}

grpc::Status ClientManagementService::OpenDb(__attribute__ ((unused)) grpc::ServerContext* context, const rasnet::service::OpenDbReq* request, rasnet::service::OpenDbRepl* response)
{
    string clientId = request->clientuuid();
    string dbName = request->databasename();

    grpc::Status status = Status::OK;

    try
    {
        ClientServerSession session;

        //The session is initialized by the call
        LDEBUG << "Open DB session for client " << clientId;
        this->clientManager->openClientDbSession(clientId, dbName, session);
        response->set_clientsessionid(session.clientSessionId);
        response->set_dbsessionid(session.dbSessionId);
        response->set_port(session.serverPort);
        response->set_serverhostname(session.serverHostName);
    }
    catch (std::exception& ex)
    {
        LERROR << "Failed opening DB session: " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        LERROR << "Failed opening DB session";
        status = GrpcUtils::convertExceptionToStatus("Failed opening DB session");
    }

    return status;
}

grpc::Status ClientManagementService::CloseDb(__attribute__ ((unused)) grpc::ServerContext* context, 
        const rasnet::service::CloseDbReq* request, 
        __attribute__ ((unused)) rasnet::service::Void* response)
{
    grpc::Status status;

    try
    {
        LDEBUG << "Close database session " << request->dbsessionid()
               << " by client " << request->clientid();
        this->clientManager->closeClientDbSession(request->clientuuid(), request->dbsessionid());
    }
    catch (std::exception& ex)
    {
        LERROR << "Failed closing client database session: " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        LERROR << "Failed closing client database session";
        status = GrpcUtils::convertExceptionToStatus("Failed closing client database session");
    }

    return status;
}

grpc::Status ClientManagementService::KeepAlive(__attribute__ ((unused)) grpc::ServerContext* context, 
        const rasnet::service::KeepAliveReq* request, 
        __attribute__ ((unused)) rasnet::service::Void* response)
{
    grpc::Status status = Status::OK;

    try
    {
        LDEBUG << "Process keep alive message from client " << request->clientuuid();
        this->clientManager->keepClientAlive(request->clientuuid());
    }
    catch (std::exception& ex)
    {
        LERROR << "Failed processing keep alive message from client " << request->clientuuid()
               << ": " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        LERROR << "Failed processing keep alive message from client " + request->clientuuid();
        status = GrpcUtils::convertExceptionToStatus("Failed processing keep alive message from client");
    }

    return status;
}

} /* namespace rasmgr */
