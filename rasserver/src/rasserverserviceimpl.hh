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
#include <memory>

#include "rasnet/messages/rassrvr_rasmgr_service.grpc.pb.h"

namespace rasserver
{
class ClientManager;

class RasServerServiceImpl : public rasnet::service::RasServerService::Service
{
public:
    RasServerServiceImpl(std::shared_ptr<rasserver::ClientManager> clientManagerArg);

    virtual grpc::Status AllocateClient(grpc::ServerContext* context, const rasnet::service::AllocateClientReq* request, rasnet::service::Void* response) override;

    virtual grpc::Status DeallocateClient(grpc::ServerContext* context, const rasnet::service::DeallocateClientReq* request, rasnet::service::Void* response) override;

    virtual grpc::Status Close(grpc::ServerContext* context, const rasnet::service::CloseServerReq* request, rasnet::service::Void* response) override;

    virtual grpc::Status GetClientStatus(grpc::ServerContext* context, const rasnet::service::ClientStatusReq* request, rasnet::service::ClientStatusRepl* response) override;

    virtual grpc::Status GetServerStatus(grpc::ServerContext* context, const rasnet::service::ServerStatusReq* request, rasnet::service::ServerStatusRepl* response) override;

private:
    std::shared_ptr<rasserver::ClientManager> clientManager;
};
}

#endif // RASSERVERSERVICE_HH
