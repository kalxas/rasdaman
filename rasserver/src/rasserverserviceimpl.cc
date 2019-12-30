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

#include "rasserverserviceimpl.hh"
#include "clientmanager.hh"
#include "server/rasserver_entry.hh"
#include <logging.hh>

namespace rasserver
{
RasServerServiceImpl::RasServerServiceImpl(std::shared_ptr<rasserver::ClientManager> clientManagerArg)
{
    this->clientManager = clientManagerArg;
}


grpc::Status rasserver::RasServerServiceImpl::AllocateClient(__attribute__ ((unused)) grpc::ServerContext* context, 
        const rasnet::service::AllocateClientReq* request, 
        __attribute__ ((unused)) rasnet::service::Void* response)
{
    grpc::Status result = grpc::Status::OK;

    LDEBUG << "Allocating client " << request->clientid();

    if (!clientManager->allocateClient(request->clientid(), request->sessionid()))
    {
        result = grpc::Status(grpc::StatusCode::ALREADY_EXISTS, "The client is already allocated to this server");
    }
    else
    {
        RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
        try
        {
            rasServerEntry.connectNewClient(request->capabilities().c_str());
        }
        catch (r_Error &)
        {
            result = grpc::Status(grpc::StatusCode::ALREADY_EXISTS, "The client is already allocated to this server");
        }
    }

    LDEBUG << "Allocated client " << request->clientid();

    return result;
}

grpc::Status rasserver::RasServerServiceImpl::DeallocateClient(__attribute__ ((unused)) grpc::ServerContext* context, 
        const rasnet::service::DeallocateClientReq* request, 
        __attribute__ ((unused)) rasnet::service::Void* response)
{
    this->clientManager->deallocateClient(request->clientid(), request->sessionid());
    RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
    rasServerEntry.disconnectClient();

    return grpc::Status::OK;
}

grpc::Status rasserver::RasServerServiceImpl::Close(__attribute__ ((unused)) grpc::ServerContext* context, 
        __attribute__ ((unused)) const rasnet::service::CloseServerReq* request, 
        __attribute__ ((unused)) rasnet::service::Void* response)
{
    //TODO: Implement a clean exit
    LDEBUG << "Closing server.";
    exit(EXIT_SUCCESS);
}

grpc::Status rasserver::RasServerServiceImpl::GetClientStatus(__attribute__ ((unused)) grpc::ServerContext* context, 
        const rasnet::service::ClientStatusReq* request, 
        rasnet::service::ClientStatusRepl* response)
{
    LDEBUG << "Starting GetClientStatus " << request->clientid();

    if (this->clientManager->isAlive(request->clientid()))
    {
        response->set_status(rasnet::service::ClientStatusRepl_Status_ALIVE);
    }
    else
    {
        response->set_status(rasnet::service::ClientStatusRepl_Status_DEAD);
    }

    LDEBUG << "Finish GetClientStatus of client " << request->clientid();

    return grpc::Status::OK;
}

grpc::Status rasserver::RasServerServiceImpl::GetServerStatus(__attribute__ ((unused)) grpc::ServerContext* context, 
        __attribute__ ((unused)) const rasnet::service::ServerStatusReq* request, 
        rasnet::service::ServerStatusRepl* response)
{
    response->set_clientqueuesize(this->clientManager->getClientQueueSize());
    return grpc::Status::OK;
}
}
