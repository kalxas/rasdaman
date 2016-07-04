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

#include <easylogging++.h>
#include "common/src/uuid/uuid.hh"
#include "common/src/grpc/grpcutils.hh"

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
    :clientManager(clientManager)
{}

ClientManagementService::~ClientManagementService()
{}


grpc::Status ClientManagementService::Connect(grpc::ServerContext *context, const rasnet::service::ConnectReq *request, rasnet::service::ConnectRepl *response)
{
    grpc::Status status = Status::OK;
    /**
     * 1. Use the client manager to register the client and obtain a UUID for the client
     * 2. Set the UUID in the response so that it is sent back to the client.
     */
    try
    {
        LDEBUG<<"Started connecting client";
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

        LDEBUG<<"Finished connecting client with ID:"
              <<response->clientuuid();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        status  = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch(...)
    {
        string failureReason="Connect request failed for unknown reason.";
        LERROR<<failureReason;

        status = GrpcUtils::convertExceptionToStatus(failureReason);
    }

    return status;
}

grpc::Status ClientManagementService::Disconnect(grpc::ServerContext *context, const rasnet::service::DisconnectReq *request, rasnet::service::Void *response)
{
    grpc::Status status = Status::OK;

    try
    {
        LDEBUG<<"Started disconnecting client with ID:"
              <<request->clientuuid();

        this->clientManager->disconnectClient(request->clientuuid());

        LDEBUG<<"Finished disconnecting client with ID:"
              <<request->clientuuid();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();

        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch(...)
    {
        string failureReason="Disconnect request failed for unknown reason";
        LERROR<<failureReason;

        status = GrpcUtils::convertExceptionToStatus(failureReason);
    }

    return status;
}

grpc::Status ClientManagementService::OpenDb(grpc::ServerContext *context, const rasnet::service::OpenDbReq *request, rasnet::service::OpenDbRepl *response)
{
    string clientId = request->clientuuid();
    string dbName = request->databasename();

    grpc::Status status = Status::OK;

    try
    {
        ClientServerSession session;

        //The session is initialized by the call
        this->clientManager->openClientDbSession(clientId, dbName, session);

        LDEBUG_IF(clientId!=session.clientSessionId)<<"Opened remote database session for client with ID:"<<clientId;

        response->set_clientsessionid(session.clientSessionId);
        response->set_dbsessionid(session.dbSessionId);
        response->set_port(session.serverPort);
        response->set_serverhostname(session.serverHostName);
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();

        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch(...)
    {
        string failureReason="Open Database request failed for unknown reason.";
        LERROR<<failureReason;

        status = GrpcUtils::convertExceptionToStatus(failureReason);
    }

    return status;
}

grpc::Status ClientManagementService::CloseDb(grpc::ServerContext *context, const rasnet::service::CloseDbReq *request, rasnet::service::Void *response)
{
    grpc::Status status;

    try
    {
        LDEBUG<<"Started closing database session: "
              <<request->dbsessionid()
              <<"by client with ID"
              <<request->clientid();

        this->clientManager->closeClientDbSession(request->clientuuid(), request->dbsessionid());

        LDEBUG<<"Finished closing database session: "
              <<request->dbsessionid()
              <<"by client with ID"
              <<request->clientid();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();

        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch(...)
    {
        string failureReason="Close Database request failed with unknown exception";
        LERROR<<failureReason;

        status = GrpcUtils::convertExceptionToStatus(failureReason);
    }

    return status;
}

grpc::Status ClientManagementService::KeepAlive(grpc::ServerContext *context, const rasnet::service::KeepAliveReq *request, rasnet::service::Void *response)
{
    grpc::Status status = Status::OK;

    try
    {
        LDEBUG<<"Start processing Keep Alive message from client with ID:"<<request->clientuuid();

        this->clientManager->keepClientAlive(request->clientuuid());

        LDEBUG<<"Finished processing Keep Alive message from client with ID:"<<request->clientuuid();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();

        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch(...)
    {
        string failureReason="KeepAlive request failed with unknown exception";
        LERROR<<failureReason;

        status = GrpcUtils::convertExceptionToStatus(failureReason);
    }

    return status;
}

} /* namespace rasmgr */
