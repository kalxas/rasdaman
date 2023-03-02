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

#include "constants.hh"
#include "outpeer.hh"

#include "common/grpc/grpcutils.hh"
#include <logging.hh>

namespace rasmgr
{
using common::GrpcUtils;

using rasnet::service::GetRemoteServerReply;
using rasnet::service::GetRemoteServerRequest;

OutPeer::OutPeer(const std::string &h, const uint32_t p)
    : hostName(h), port(p)
{
    // Initialize the service used for communicating with the remote rasmgr
    std::string serverAddress = GrpcUtils::constructAddressString(this->hostName, std::uint32_t(this->port));
    auto channel = grpc::CreateChannel(serverAddress, grpc::InsecureChannelCredentials());

    LDEBUG << "Created channel to outpeer:" << serverAddress;

    this->healthService = std::make_shared<::common::HealthService::Stub>(channel);
    this->rasmgrService = std::make_shared<::rasnet::service::RasmgrRasmgrService::Stub>(channel);
}

const std::string &OutPeer::getHostName() const
{
    return hostName;
}

std::uint32_t OutPeer::getPort() const
{
    return port;
}

bool OutPeer::isBusy() const
{
    return !this->openSessions.empty();
}

bool rasmgr::OutPeer::tryGetRemoteServer(
    const ClientServerRequest &request, ClientServerSession &out_reply)
{
    // Before initiating a request check if the rasmgr is alive.
    if (!GrpcUtils::isServerAlive(this->healthService, SERVER_CALL_TIMEOUT))
    {
        LERROR << "Failed to contact remote rasmgr on " << this->hostName << ":" << this->port << ".";
        return false;
    }

    GetRemoteServerReply reply;
    GetRemoteServerRequest req;

    req.set_user_name(request.getUserName());
    req.set_password_hash(request.getPassword());
    req.set_database_name(request.getDatabaseName());

    grpc::ClientContext context;

    grpc::Status status = this->rasmgrService->TryGetRemoteServer(&context, req, &reply);
    if (status.ok())
    {
        out_reply.clientSessionId = reply.client_session_id();
        out_reply.dbSessionId = reply.db_session_id();
        out_reply.serverHostName = reply.server_host_name();
        out_reply.serverPort = reply.server_port();

        RemoteClientSession remoteSession(reply.client_session_id(), reply.db_session_id());
        this->openSessions.insert(this->createSessionId(remoteSession));
    }
    else
    {
        LDEBUG << "Failed to get remote server from " << this->hostName << ":" << this->port << ". "
               << "Error message: " << status.error_code() << "(" << status.error_message() << ") "
               << ", request: " << req.DebugString()
               << ", response: " << reply.DebugString();
    }

    return status.ok();
}

void OutPeer::releaseServer(const RemoteClientSession &clientSession)
{
    auto sessionKey = this->createSessionId(clientSession);
    if (this->openSessions.find(sessionKey) != this->openSessions.end())
    {
        // We need to remove the session from our local data even if the remote rasmgr has died.
        this->openSessions.erase(sessionKey);

        if (GrpcUtils::isServerAlive(this->healthService, SERVER_CALL_TIMEOUT))
        {
            // Set timeout for API
            grpc::ClientContext context;

            ::rasnet::service::Void reply;
            ::rasnet::service::ReleaseServerRequest request;

            request.set_client_session_id(clientSession.getClientSessionId());
            request.set_db_session_id(clientSession.getDbSessionId());

            grpc::Status status = this->rasmgrService->ReleaseServer(&context, request, &reply);
            if (!status.ok())
            {
                LERROR << "Failed to release remote server on rasmgr "
                       << this->hostName << ":" << this->port << ". Error message: "
                       << status.error_message();
            }
            else
            {
                LDEBUG << "Released remote server on rasmgr " << this->hostName << ":" << this->port;
            }
        }
        else
        {
            LERROR << "Failed to release remote server on rasmgr "
                   << this->hostName << ":" << this->port << "; "
                   << "remote rasmgr is not responding to health checks.";
        }
    }
}

std::string OutPeer::createSessionId(const RemoteClientSession &clientSession)
{
    return clientSession.getClientSessionId() + ":" + clientSession.getDbSessionId();
}

}  // namespace rasmgr
