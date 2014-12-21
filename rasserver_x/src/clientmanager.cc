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

#include "clientmanager.hh"

namespace rasserver
{

using std::string;
using std::pair;
using std::make_pair;
using std::map;
using common::Timer;

ClientManager::ClientManager()
{
}

bool ClientManager::allocateClient(std::string clientUUID, std::string sessionId, int32_t period)
{
    pair<string, string> key = make_pair(clientUUID, sessionId);
    Timer timer(period);

    pair<map<pair<string, string>, Timer>::iterator, bool> result = this->clientList.insert(make_pair(key, period));
    return result.second;
}

void ClientManager::deallocateClient(std::string clientUUID, std::string sessionId)
{
    this->clientList.erase(make_pair(clientUUID, sessionId));
}

bool ClientManager::isAlive(std::string clientUUID, std::string sessionId)
{
   map<pair<string, string>, Timer>::iterator clientIt = this->clientList.find(make_pair(clientUUID, sessionId));
   if (clientIt == this->clientList.end())
   {
       return false;
   }
   return !clientIt->second.hasExpired();
}

void ClientManager::resetLiveliness(std::string clientUUID, std::string sessionId)
{
    map<pair<string, string>, Timer>::iterator clientIt = this->clientList.find(make_pair(clientUUID, sessionId));
    if (clientIt != this->clientList.end())
    {
        clientIt->second.reset();
    }
}

size_t ClientManager::getClientQueueSize()
{
    return this->clientList.size();
}

}
