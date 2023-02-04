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

Client::Client(const string &clientIdArg, std::shared_ptr<User> userArg, 
               std::int32_t lifeTime, const std::string &rasmgrHostArg)
    : clientId(clientIdArg), user(userArg), timer(lifeTime), rasmgrHost(rasmgrHostArg)
{
}

const string &Client::getClientId() const
{
    return this->clientId;
}

bool Client::isAlive()
{
    boost::upgrade_lock<shared_mutex> timerLock(this->timerMutex);
    //If the timer has expired we ask the servers if they know anything about the client
    if (this->timer.hasExpired())
    {
        LDEBUG << "Client::isAlive() - timer expired " << this->clientId;
        if (this->isClientAliveOnServer())
        {
            LDEBUG << "Client::isAlive() - client alive on server " << this->clientId;
            //If we received information from a server that the client is alive, reset the time
            boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueTimerLock(timerLock);
            LDEBUG << "Client " << clientId << " is alive on servers, resetting keep alive timer";
            this->timer.reset();
        }
        else
        {
            LDEBUG << "Client::isAlive() - client not alive on server " << this->clientId;
            this->removeDeadServer();
            return false;
        }
    }
    return true;
}

void Client::resetLiveliness()
{
    boost::lock_guard<shared_mutex> uniqueTimer(this->timerMutex);
//    LDEBUG << "resetting liveliness of client " << clientId;
    this->timer.reset();
}

void Client::addDbSession(const std::string &dbName,
                          std::shared_ptr<Server> newServer,
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

    boost::lock_guard<shared_mutex> lock(this->assignedServerMutex);
    
    if (!sessionId.empty())
    {
        throw DuplicateDbSessionException(dbName, sessionId);
    }

    //Generate a unique session id.
    out_sessionId = UUID::generateUUID();
    sessionId = out_sessionId;
    
    //Check if the client is allowed to access the db
    out_dbRights = this->user->getDefaultDbRights();

    // TODO(DM) - remove this rights mgmt here
    if (out_dbRights.hasReadAccess()  || out_dbRights.hasWriteAccess())
    {
        this->assignedServer = newServer;
        newServer->allocateClientSession(
              this->clientId, out_sessionId, dbName, out_dbRights);
    }
    else
    {
        throw UserDbRightsException(this->user->getName(), dbName);
    }
}

void Client::removeDbSession(const string &sessionIdToRemove)
{
    boost::lock_guard<shared_mutex> lock(this->assignedServerMutex);

    if (!sessionId.empty())
    {
        if (sessionIdToRemove == sessionId)
        {
            assignedServer->deallocateClientSession(this->clientId, sessionId);
            assignedServer.reset();
            sessionId = "";
        }
        else
        {
            throw InexistentDbSessionException(sessionIdToRemove);
        }
    }
}

void Client::removeClientFromServer()
{
    upgrade_lock<shared_mutex> lock(this->assignedServerMutex);
    if (assignedServer && !sessionId.empty())
    {
        assignedServer->deallocateClientSession(this->clientId, sessionId);
        upgrade_to_unique_lock<shared_mutex> uniqueServerLock(lock);
        assignedServer.reset();
        sessionId = "";
    }
}

const std::shared_ptr<const User> Client::getUser() const
{
    return user;
}

const string &Client::getRasmgrHost() const
{
    return rasmgrHost;
}

bool Client::isClientAliveOnServer()
{
    {
        boost::shared_lock<shared_mutex> serversLock(this->assignedServerMutex);
        if (!sessionId.empty())
        {
            return assignedServer->isClientAlive(this->clientId);
        }
    }
    return false;
}

void Client::removeDeadServer()
{
    boost::upgrade_lock<shared_mutex> serversLock(this->assignedServerMutex);

    //Try to acquire a valid pointer to the assigned server
    if (assignedServer)
    {
        LDEBUG << "removing dead server assigned to client";
        boost::upgrade_to_unique_lock<shared_mutex> uniqueLock(serversLock);
        assignedServer.reset();
        sessionId = "";
    }
}

}
