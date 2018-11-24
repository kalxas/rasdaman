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

#include <unistd.h>

#include <iostream>
#include <memory>

#include <boost/thread.hpp>

#include <grpc++/grpc++.h>


#include "common/grpc/healthserviceimpl.hh"
#include "server/rasserver_config.hh"
#include "rasnetprotocol/rasnetservercomm.hh"

#include "rasserverserviceimpl.hh"

namespace grpc
{
class Server;
}

namespace rasserver
{
class RasnetServer
{
public:
    RasnetServer(const Configuration& configuration);
    void startRasnetServer();

private:
    bool isRunning;
    Configuration configuration;

    std::unique_ptr<grpc::Server> server;
    boost::shared_ptr<rasnet::service::RasServerService::Service> rasserverService;
    boost::shared_ptr<rasnet::service::ClientRassrvrService::Service> clientServerService;
    boost::shared_ptr<common::HealthServiceImpl> healthServiceImpl;

    void registerServerWithRasmgr();
};
}
#endif // RASSERVER_X_SRC_RASNETSERVER_HH
