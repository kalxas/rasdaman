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
#include "server/rasserver_entry.hh"

RasServerServiceImpl::RasServerServiceImpl(::boost::shared_ptr<rasserver::ClientManager> clientManager)
{
    this->clientManager = clientManager;
}

void RasServerServiceImpl::AllocateClient(::google::protobuf::RpcController* controller,
                                          const ::rasnet::service::AllocateClientReq* request,
                                          ::rasnet::service::Void* response,
                                          ::google::protobuf::Closure* done)
{
    if(!clientManager->allocateClient(request->clientid(), request->sessionid(), 3000))
    {
        controller->SetFailed("Client already in list");
    }else{
        RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
        rasServerEntry.compat_connectNewClient(request->capabilities().c_str());
    }
}

void RasServerServiceImpl::DeallocateClient(::google::protobuf::RpcController* controller,
                                            const ::rasnet::service::DeallocateClientReq* request,
                                            ::rasnet::service::Void* response,
                                            ::google::protobuf::Closure* done)
{
    this->clientManager->deallocateClient(request->clientid(), request->sessionid());
    RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
    rasServerEntry.compat_disconnectClient();
}

void RasServerServiceImpl::Close(::google::protobuf::RpcController* controller,
                                 const ::rasnet::service::CloseServerReq* request,
                                 ::rasnet::service::Void* response,
                                 ::google::protobuf::Closure* done)
{
    exit(EXIT_SUCCESS);
}

void RasServerServiceImpl::GetClientStatus(::google::protobuf::RpcController* controller,
                                           const ::rasnet::service::ClientStatusReq* request,
                                           ::rasnet::service::ClientStatusRepl* response,
                                           ::google::protobuf::Closure* done)
{
    if (this->clientManager->isAlive(request->clientid(), request->sessionid())){
         response->set_status(rasnet::service::ClientStatusRepl_Status_ALIVE);
    }
    else
    {
        response->set_status(rasnet::service::ClientStatusRepl_Status_DEAD);
    }
}

void RasServerServiceImpl::GetServerStatus(::google::protobuf::RpcController* controller,
                                           const ::rasnet::service::ServerStatusReq* request,
                                           ::rasnet::service::ServerStatusRepl* response,
                                           ::google::protobuf::Closure* done)
{
    response->set_clientqueuesize(this->clientManager->getClientQueueSize());
}
