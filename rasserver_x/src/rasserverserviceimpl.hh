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

#ifndef RASSERVERSERVICE_HH
#define RASSERVERSERVICE_HH

#include <set>
#include <utility>
#include <string>
#include <boost/smart_ptr.hpp>

#include "clientmanager.hh"
#include "../../rasnet/src/messages/rassrvr_rasmgr_service.pb.h"

class RasServerServiceImpl : public rasnet::service::RasServerService
{
public:
    RasServerServiceImpl(::boost::shared_ptr<rasserver::ClientManager> clientManager);

    void AllocateClient(::google::protobuf::RpcController* controller,
                         const ::rasnet::service::AllocateClientReq* request,
                         ::rasnet::service::Void* response,
                         ::google::protobuf::Closure* done);
    void DeallocateClient(::google::protobuf::RpcController* controller,
                         const ::rasnet::service::DeallocateClientReq* request,
                         ::rasnet::service::Void* response,
                         ::google::protobuf::Closure* done);
    void Close(::google::protobuf::RpcController* controller,
                         const ::rasnet::service::CloseServerReq* request,
                         ::rasnet::service::Void* response,
                         ::google::protobuf::Closure* done);
    void GetClientStatus(::google::protobuf::RpcController* controller,
                         const ::rasnet::service::ClientStatusReq* request,
                         ::rasnet::service::ClientStatusRepl* response,
                         ::google::protobuf::Closure* done);
    void GetServerStatus(::google::protobuf::RpcController* controller,
                         const ::rasnet::service::ServerStatusReq* request,
                         ::rasnet::service::ServerStatusRepl* response,
                         ::google::protobuf::Closure* done);
private:
    ::boost::shared_ptr<rasserver::ClientManager> clientManager;
};

#endif // RASSERVERSERVICE_HH
