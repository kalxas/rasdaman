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
#include "raslib/error.hh"
#include "common/pragmas/pragmas.hh"
#include <logging.hh>

using grpc::ServerContext;
using namespace rasnet::service;

namespace rasserver
{
RasServerServiceImpl::RasServerServiceImpl(std::shared_ptr<rasserver::ClientManager> clientManagerArg)
  : clientManager{clientManagerArg}
{
}

grpc::Status rasserver::RasServerServiceImpl::AllocateClient(
    UNUSED ServerContext* context, const AllocateClientReq* req, UNUSED Void* resp)
{
    grpc::Status result = grpc::Status::OK;
    LDEBUG << "Allocating client " << req->clientid();
    
    if (!clientManager->allocateClient(req->clientid(), req->sessionid()))
    {
        result = grpc::Status(grpc::StatusCode::ALREADY_EXISTS,
                              "The client is already allocated to this server");
    }
    else
    {
        try
        {
            RasServerEntry::getInstance().connectNewClient(req->clientid(), req->capabilities().c_str());
        }
        catch (r_Error &err)
        {
            result = grpc::Status(grpc::StatusCode::ALREADY_EXISTS,
                                  std::string("The client is already connected to this server: ") + err.what());
        }
    }

    LDEBUG << "Allocated client " << req->clientid();
    return result;
}

grpc::Status rasserver::RasServerServiceImpl::DeallocateClient(
    UNUSED ServerContext* context, const DeallocateClientReq* request, UNUSED Void* response)
{
    this->clientManager->deallocateClient(request->clientid(), request->sessionid());
    RasServerEntry::getInstance().disconnectClient();
    return grpc::Status::OK;
}

grpc::Status rasserver::RasServerServiceImpl::Close(
    UNUSED ServerContext* context, UNUSED const CloseServerReq* request, UNUSED Void* response)
{
    // We need to do the exit in a thread a bit after the response has been returned
    // back to rasmgr, otherwise rasmgr will think there's an error
    LDEBUG << "Received Close request...";
    shutdownThread.reset(new std::thread(&RasServerServiceImpl::shutdownRunner, this));
    return grpc::Status::OK;
}

void RasServerServiceImpl::shutdownRunner()
{
    // wait 10ms for the response to network request Close to be returned, for
    // upto 10s in total
    LDEBUG << "starting shutdown process...";
    RasServerEntry& rasserver = RasServerEntry::getInstance();
    size_t waited = 0;
    static const size_t maxWait = 10*1000; // 10 s
    
    do
    {
      usleep(10*1000);
      waited += 10;
    }
    while (rasserver.isOpenTA() && waited < maxWait);
    
    if (waited >= maxWait)
      LINFO << "shutting down rasserver with a transaction still in progress after waiting for 10 seconds.";
    else
      LINFO << "shutting down rasserver.";
  
    exit(EXIT_SUCCESS);
}

grpc::Status rasserver::RasServerServiceImpl::GetClientStatus(
    UNUSED ServerContext* context, const ClientStatusReq* request, ClientStatusRepl* response)
{
    LTRACE << "Starting GetClientStatus " << request->clientid();

    if (this->clientManager->isAlive(request->clientid()))
        response->set_status(ClientStatusRepl_Status_ALIVE);
    else
        response->set_status(ClientStatusRepl_Status_DEAD);

    LTRACE << "Finish GetClientStatus of client " << request->clientid();

    return grpc::Status::OK;
}

grpc::Status rasserver::RasServerServiceImpl::GetServerStatus(
    UNUSED ServerContext* context, UNUSED const ServerStatusReq* request, ServerStatusRepl* response)
{
    response->set_hasclients(this->clientManager->hasClients());
    return grpc::Status::OK;
}
}
