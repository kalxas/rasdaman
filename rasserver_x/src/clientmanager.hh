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

#ifndef RASSERVER_CLIENTMANAGER_HH
#define RASSERVER_CLIENTMANAGER_HH

#include <map>
#include <string>

#include <boost/scoped_ptr.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>

#include "common/src/time/timer.hh"
#include "common/src/zeromq/zmq.hh"
#include "common/src/logging/easylogging++.hh"

namespace rasserver {

class ClientManager
{
public:
    ClientManager();
    virtual ~ClientManager();

    bool allocateClient(std::string clientUUID, std::string sessionId);
    void deallocateClient(std::string clientUUID, std::string sessionId);
    bool isAlive(std::string clientUUID);
    void resetLiveliness(std::string clientUUID);
    size_t getClientQueueSize();

private:
    static const int ALIVE_PERIOD = 3000; /* milliseconds */

    zmq::context_t context;
    boost::scoped_ptr<zmq::socket_t> controlSocket;
    std::string controlEndpoint;

    boost::scoped_ptr<boost::thread> managementThread;

    boost::shared_mutex clientMutex;
    std::map<std::string, common::Timer> clientList;

    void evaluateClientStatus();
};

}

#endif // CLIENTMANAGER_HH
