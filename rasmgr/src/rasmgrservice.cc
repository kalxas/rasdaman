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

#include "rasmgrservice.hh"
#include "clientcredentials.hh"
#include "clientmanager.hh"
#include "servermanager.hh"
#include "common/grpc/grpcutils.hh"
#include <logging.hh>

namespace rasmgr
{

RasmgrService::RasmgrService(std::shared_ptr<ClientManager> m)
    : clientManager(m)
{
}

grpc::Status RasmgrService::TryGetRemoteServer(
    __attribute__((unused)) grpc::ServerContext *context,
    const rasnet::service::GetRemoteServerRequest *request,
    rasnet::service::GetRemoteServerReply *response)
{
    grpc::Status status = grpc::Status::OK;

    try
    {
        ClientCredentials credentials(request->user_name(),
                                      request->password_hash());

        // The clientSessionId will be initialized by this method
        auto clientSessionId = this->clientManager->connectClient(credentials, request->hostname());
        LDEBUG << "Connected remote client with ID:" << clientSessionId;

        // The clientServerSession will be initialized by the following method
        ClientServerSession clientServerSession;
        this->clientManager->openClientDbSession(clientSessionId, request->database_name(), clientServerSession);

        response->set_client_session_id(clientServerSession.clientSessionId);
        response->set_db_session_id(clientServerSession.dbSessionId);
        response->set_server_host_name(clientServerSession.serverHostName);
        response->set_server_port(clientServerSession.serverPort);

        LDEBUG << "Opened DB session for remote client with ID:" << clientSessionId;
    }
    catch (std::exception &ex)
    {
        LERROR << "Connect request failed: " << ex.what();
        status = common::GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        std::string failureReason = "Connect request failed for unknown reason.";
        LERROR << failureReason;
        status = common::GrpcUtils::convertExceptionToStatus(failureReason);
    }

    return status;
}

grpc::Status RasmgrService::ReleaseServer(
    __attribute__((unused)) grpc::ServerContext *context,
    const rasnet::service::ReleaseServerRequest *request,
    __attribute__((unused)) rasnet::service::Void *response)
{
    grpc::Status status;

    try
    {
        this->clientManager->closeClientDbSession(request->client_session_id(), request->db_session_id());
        this->clientManager->disconnectClient(request->client_session_id());
    }
    catch (std::exception &ex)
    {
        LERROR << "Disconnect request failed: " << ex.what();
        status = common::GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        std::string failureReason = "Disconnect request failed with unknown exception";
        LERROR << failureReason;
        status = common::GrpcUtils::convertExceptionToStatus(failureReason);
    }

    return status;
}
}  // namespace rasmgr
