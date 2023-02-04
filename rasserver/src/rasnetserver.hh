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

#ifndef RASSERVER_X_SRC_RASNETSERVER_HH
#define RASSERVER_X_SRC_RASNETSERVER_HH

#include "rasserverserviceimpl.hh"
#include "common/grpc/healthserviceimpl.hh"
#include "rasnet/messages/client_rassrvr_service.grpc.pb.h"

#include <grpc++/grpc++.h>

namespace grpc
{
class Server;
}

namespace rasserver
{
class RasmgrComm;

class RasnetServer
{
public:
    RasnetServer(std::uint32_t listenPort1, const char* rasmgrHost1, 
                 std::uint32_t rasmgrPort1, const char* serverId1);
    
    void startRasnetServer();

private:
    bool isRunning{false};
    std::uint32_t listenPort;
    std::uint32_t rasmgrPort;
    std::string rasmgrHost;
    std::string serverId;

    std::unique_ptr<grpc::Server> server;
    std::shared_ptr<rasnet::service::RasServerService::Service> rasserverService;
    std::shared_ptr<rasnet::service::ClientRassrvrService::Service> clientServerService;
    std::shared_ptr<RasmgrComm> rasmgrComm;
    std::shared_ptr<common::HealthServiceImpl> healthServiceImpl;
};
}
#endif // RASSERVER_X_SRC_RASNETSERVER_HH
