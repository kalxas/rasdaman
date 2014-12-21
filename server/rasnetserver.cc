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

#include "rasnetserver.hh"

#include "rasserverserviceimpl.hh"
#include "../rasnetprotocol/rasnetservercomm.hh"

#include "server/rasserver_entry.hh"

#include <iostream>

using rasnet::ServiceManager;
using boost::shared_ptr;
using rasnet::service::RasServerService;
using rasnet::Channel;
using boost::scoped_ptr;
using google::protobuf::DoNothing;
using google::protobuf::NewPermanentCallback;
using rasnet::service::Void;
using rasnet::ClientController;

RasnetServer::RasnetServer(int port, const char* serverId)
{
    this->port = port;

    serviceManager.reset(new ServiceManager());

    shared_ptr<RasServerService> rasServerService(new RasServerServiceImpl());
    shared_ptr<RasnetServerComm> rasnetClinetServerComm(new RasnetServerComm());

    serviceManager->addService(rasServerService);
    serviceManager->addService(rasnetClinetServerComm);

    this->runMutex.lock();

    Channel channel("tcp://localhost" , 7001);

    scoped_ptr<rasnet::service::RasMgrRasServerService> rasmgrRasserverService(new ::rasnet::service::RasMgrRasServerService_Stub(&channel));

    ClientController controller;
    boost::scoped_ptr<google::protobuf::Closure> doNothing;
    doNothing.reset(NewPermanentCallback(&DoNothing));

    ::rasnet::service::RegisterServerReq request;
    Void response;

    request.set_serverid(serverId);
    rasmgrRasserverService->RegisterServer(&controller, &request, &response, doNothing.get());
}


void RasnetServer::startRasnetServer()
{
    std::cout<<"Serving on " << this->port;
    this->serviceManager->serve("tcp://*", this->port);
    RasServerEntry &rasserver = RasServerEntry::getInstance();
    rasserver.compat_connectToDBMS();
    while (true)
    {
        boost::this_thread::sleep(boost::posix_time::millisec(100000));
    }
}


void RasnetServer::stopRasnetServer()
{
    this->runMutex.unlock();
}
