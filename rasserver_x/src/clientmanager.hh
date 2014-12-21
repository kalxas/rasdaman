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
#include "common/src/time/timer.hh"

namespace rasserver {

class ClientManager
{
public:
    ClientManager();
    bool allocateClient(std::string clientUUID, std::string sessionId, int32_t period);
    void deallocateClient(std::string clientUUID, std::string sessionId);
    bool isAlive(std::string clientUUID, std::string sessionId);
    void resetLiveliness(std::string clientUUID, std::string sessionId);
    size_t getClientQueueSize();

private:
    std::map<std::pair<std::string, std::string>, common::Timer> clientList;
};

}

#endif // CLIENTMANAGER_HH
