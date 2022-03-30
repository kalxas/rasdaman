/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#include <stdexcept>

#include "common/uuid/uuid.hh"
#include <logging.hh>

#include "exceptions/rasmgrexceptions.hh"
#include "rasmgr/src/messages/rasmgrmess.pb.h"
#include "rasmgrconfig.hh"
#include "server.hh"
#include "user.hh"

#include "client.hh"


namespace rasmgr
{
using std::string;
using std::runtime_error;
using std::map;
using common::UUID;
using boost::shared_lock;
using boost::shared_mutex;
using boost::unique_lock;
using boost::upgrade_lock;
using boost::upgrade_to_unique_lock;

Client::Client(const string &clientIdArg, std::shared_ptr<User> userArg, std::int32_t lifeTime)
    : clientId(clientIdArg), user(userArg), timer(lifeTime)
{
}

const string &Client::getClientId() const
{
    return this->clientId;
}

bool Client::isAlive()
{
    bool isAlive = false;

    boost::upgrade_lock<shared_mutex> timerLock(this->timerMutex);
    //If the timer has expired we ask the servers if they know anything about the client
    if (this->timer.hasExpired())
    {
        isAlive = this->isClientAliveOnServers();

        //If we received information from a server that the client is alive,
        //reset the time
        if (isAlive)
        {
            boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueTimerLock(timerLock);
            this->timer.reset();
        }
    }
    else
    {
        isAlive = true;
    }

    return isAlive;
}

void Client::resetLiveliness()
{
    unique_lock<shared_mutex> uniqueTimer(this->timerMutex);
    this->timer.reset();
}

void Client::addDbSession(const std::string &dbName,
                          std::shared_ptr<Server> assignedServer,
                          std::string &out_sessionId)
{
    /**
     * 1. Generate a DbSessionId
     * 2. Determine if the client is allowed to open the database he requested
     * 3. Determine the rights the client has on the database he requested
     * 4. a) Allocate the <client,DbSession> to the server
     *    b) send the AccessRights together with the clientId and DbSessionId to server
     */

    UserDatabaseRights out_dbRights(false, false);

    unique_lock<shared_mutex> lock(this->assignedServersMutex);

    //Generate a unique session id.
    do
    {
        out_sessionId = UUID::generateUUID();
    }
    while (this->assignedServers.find(out_sessionId) != this->assignedServers.end());

    //Check if the client is allowed to access the db
    out_dbRights = this->user->getDefaultDbRights();

    // TODO(DM) - remove this rights mgmt here
    if (out_dbRights.hasReadAccess()  || out_dbRights.hasWriteAccess())
    {
        this->assignedServers[out_sessionId] = assignedServer;
        assignedServer->allocateClientSession(this->clientId, out_sessionId, dbName, out_dbRights);
    }
    else
    {
        throw UserDbRightsException(this->user->getName(), dbName);
    }
}

void Client::removeDbSession(const string &sessionId)
{
    unique_lock<shared_mutex> lock(this->assignedServersMutex);

    auto assignedServerIt = this->assignedServers.find(sessionId);
    if (assignedServerIt != this->assignedServers.end())
    {
        if (auto server = assignedServerIt->second.lock())
        {
            server->deallocateClientSession(this->clientId, sessionId);
        }

        this->assignedServers.erase(sessionId);
    }
}

void Client::removeClientFromServers()
{
    upgrade_lock<shared_mutex> lock(this->assignedServersMutex);

    for (auto serverIt = this->assignedServers.begin(); serverIt != this->assignedServers.end(); ++serverIt)
    {
        if (auto server = serverIt->second.lock())
        {
            server->deallocateClientSession(this->clientId, serverIt->first);
        }
    }

    upgrade_to_unique_lock<shared_mutex> uniqueServerLock(lock);
    this->assignedServers.clear();
}

const std::shared_ptr<const User> Client::getUser() const
{
  return user;
}

bool Client::isClientAliveOnServers()
{
    bool isAlive = false;

    boost::upgrade_lock<shared_mutex> serversLock(this->assignedServersMutex);

    for (auto serverIt = this->assignedServers.begin(); !isAlive && serverIt != this->assignedServers.end(); ++serverIt)
    {
        if (auto server = serverIt->second.lock())
        {
            //The client must be alive on at least one server
            isAlive = isAlive || server->isClientAlive(this->clientId);
        }
    }

    //Remove the lock that we have on the list of servers
    serversLock.unlock();

    this->removeDeadServers();

    return isAlive;
}

void Client::removeDeadServers()
{
    unique_lock<shared_mutex> serversLock(this->assignedServersMutex);

    auto serverIt = this->assignedServers.begin();
    while (serverIt != this->assignedServers.end())
    {
        //Try to aquire a valid pointer to the assigned server
        auto server = serverIt->second.lock();

        //The server is dead,remove it
        auto serverToEraseIt = serverIt;
        ++serverIt;

        if (!server)
        {
            this->assignedServers.erase(serverToEraseIt);
        }
    }
}

}
