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

#include "clientmanager.hh"
#include "clientcredentials.hh"
#include "clientmanagementservice.hh"

#include "common/grpc/grpcutils.hh"
#include "common/exceptions/exception.hh"

#include "rasnet/messages/rasmgr_client_service.grpc.pb.h"
#include "rasnet/messages/rasmgr_client_service.pb.h"

#include <logging.hh>

#include <fstream>
#include <cstdint>

namespace rasmgr
{

using common::GrpcUtils;

ClientManagementService::ClientManagementService(std::shared_ptr<ClientManager> cm)
    : clientManager(cm)
{
}

grpc::Status ClientManagementService::Connect(
    grpc::ServerContext *, const rasnet::service::ConnectReq *request, rasnet::service::ConnectRepl *response)
{
    BLDEBUG << "\n";
    LDEBUG << "Connecting client...";
    grpc::Status status = grpc::Status::OK;
    /**
     * 1. Use the client manager to register the client and obtain a UUID for the client
     * 2. Set the UUID in the response so that it is sent back to the client.
     */
    try
    {
        std::uint32_t out_clientUUID;

        //Create the ClientCredentials object used for authentication.
        ClientCredentials credentials(request->username(),
                                      request->passwordhash(),
                                      request->token());

        //Try to authenticate the client and assign the client an ID.
        //If the authentication fails, an exception is thrown
        out_clientUUID = this->clientManager->connectClient(credentials, request->hostname());

        response->set_clientid(out_clientUUID);
        response->set_keepalivetimeout(this->clientManager->getConfig().getClientLifeTime());

        LDEBUG << "Client connected, assigned ID " << response->clientid();
    }
    catch (common::Exception &ex)
    {
        LERROR << "Failed connecting client: " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (std::exception &ex)
    {
        LERROR << "Failed connecting client: " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        LERROR << "Failed connecting client";
        status = GrpcUtils::convertExceptionToStatus("Failed connecting client");
    }

    return status;
}

grpc::Status ClientManagementService::Disconnect(
    grpc::ServerContext *, const rasnet::service::DisconnectReq *request, rasnet::service::Void *)
{
    const auto clientId = request->clientid();
    BLDEBUG << "\n";
    LDEBUG << "Disconnect client " << clientId;
    grpc::Status status = grpc::Status::OK;
    try
    {
        this->clientManager->disconnectClient(clientId);
    }
    catch (common::Exception &ex)
    {
        LERROR << "Failed disconnecting client " << clientId << ": " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (std::exception &ex)
    {
        LERROR << "Failed disconnecting client " << clientId << ": " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        LERROR << "Failed disconnecting client " << clientId;
        status = GrpcUtils::convertExceptionToStatus("Failed disconnecting client " + std::to_string(clientId));
    }
    return status;
}

grpc::Status ClientManagementService::OpenDb(
    grpc::ServerContext *, const rasnet::service::OpenDbReq *request, rasnet::service::OpenDbRepl *response)
{
    const auto clientId = request->clientid();
    const auto &dbName = request->databasename();
    BLDEBUG << "\n";
    LDEBUG << "Open DB session for client " << clientId;
    grpc::Status status = grpc::Status::OK;
    try
    {
        ClientServerSession session;
        //The session is initialized by the call
        this->clientManager->openClientDbSession(clientId, dbName, session);
        response->set_clientsessionid(session.clientSessionId);
        response->set_dbsessionid(session.dbSessionId);
        response->set_port(session.serverPort);
        response->set_serverhostname(session.serverHostName);
    }
    catch (common::Exception &ex)
    {
        LERROR << "Failed opening DB session for client " << clientId << ": " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (std::exception &ex)
    {
        LERROR << "Failed opening DB session for client " << clientId << ": " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        LERROR << "Failed opening DB session for client " << clientId;
        status = GrpcUtils::convertExceptionToStatus("Failed opening DB session for client " + std::to_string(clientId));
    }

    return status;
}

grpc::Status ClientManagementService::CloseDb(
    grpc::ServerContext *, const rasnet::service::CloseDbReq *request, rasnet::service::Void *)
{
    const auto clientId = request->clientid();
    const auto &sessionId = request->dbsessionid();
    BLDEBUG << "\n";
    LDEBUG << "Close database session " << sessionId << " by client " << clientId;
    grpc::Status status;
    try
    {
        this->clientManager->closeClientDbSession(clientId, sessionId);
    }
    catch (common::Exception &ex)
    {
        LERROR << "Failed closing client database session for client " << clientId << ": " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (std::exception &ex)
    {
        LERROR << "Failed closing client database session for client " << clientId << ": " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        LERROR << "Failed closing client database session for client " << clientId << "";
        status = GrpcUtils::convertExceptionToStatus("Failed closing client database session for client " + std::to_string(clientId));
    }

    return status;
}

grpc::Status ClientManagementService::KeepAlive(
    grpc::ServerContext *, const rasnet::service::KeepAliveReq *request, rasnet::service::Void *)
{
    const auto clientId = request->clientid();
    LDEBUG << "Process keep alive message from client " << clientId;
    grpc::Status status = grpc::Status::OK;
    try
    {
        this->clientManager->keepClientAlive(clientId);
    }
    catch (common::Exception &ex)
    {
        LERROR << "Failed processing keep alive message from client " << clientId << ": " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (std::exception &ex)
    {
        LERROR << "Failed processing keep alive message from client " << clientId << ": " << ex.what();
        status = GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        LERROR << "Failed processing keep alive message from client " << clientId;
        status = GrpcUtils::convertExceptionToStatus("Failed processing keep alive message from client " + std::to_string(clientId));
    }

    return status;
}

} /* namespace rasmgr */
