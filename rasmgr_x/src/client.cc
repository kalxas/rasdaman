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

#include "../../common/src/uuid/uuid.hh"
#include "../../common/src/logging/easylogging++.hh"

#include "client.hh"
#include "rasmgrconfig.hh"

namespace rasmgr
{
using std::string;
using std::runtime_error;
using std::map;
using common::UUID;
using boost::weak_ptr;
using boost::shared_lock;
using boost::shared_mutex;
using boost::shared_ptr;
using boost::unique_lock;

Client::Client(string clientId, boost::shared_ptr<User> user)
    : timer(RasMgrConfig::getInstance()->getClientLifeTime())
{
    this->clientId = clientId;
    this->user=user;
}

const string& Client::getClientId() const
{
    return this->clientId;
}

bool
Client::isAlive()
{
    bool isAlive = false;

    boost::upgrade_lock<shared_mutex> timerLock(this->timerMutex);
    //If the timer has expired we ask the servers if they know anything about the client
    if(this->timer.hasExpired())
    {
        map<string, weak_ptr<RasServer> >::iterator it;
        map<string, weak_ptr<RasServer> >::iterator toErase;

        boost::upgrade_lock<shared_mutex> serversLock(this->assignedServersMutex);

        it = this->assignedServers.begin();
        while(it != this->assignedServers.end())
        {
            if(shared_ptr<RasServer> r = it->
                                         second.lock())
            {
                //The client must be alive on at least one server
                isAlive = isAlive || r->isClientAlive(this->clientId);
                ++it;
            }
            else
            {
                //The server is dead,remove it
                toErase = it;
                ++it;

                boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueServerLock(serversLock);
                this->assignedServers.erase(toErase);
            }
        }
    }
    else
    {
        isAlive=true;
    }

    if(isAlive)
    {
        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueTimerLock(timerLock);
        this->timer.reset();
    }

    return isAlive;
}

void Client::resetLiveliness()
{
    unique_lock<shared_mutex> uniqueTimer(this->timerMutex);
    this->timer.reset();
}

void Client::addDbSession(const std::string& dbName,
                          boost::shared_ptr<RasServer> assignedServer,
                          std::string& out_sessionId)
{
    /**
     * 1. Generate a DbSessionId
     * 2. Determine if the client is allowed to open the database he requested
     * 3. Determine the rights the client has on the database he requested
     * 4. a) Allocate the <client,DbSession> to the server
     *    b) send the AccessRights together with the clientId and DbSessionId to server
     */

    UserDatabaseRights out_dbRights(false,false);

    unique_lock<shared_mutex> lock(this->assignedServersMutex);

    do
    {
        out_sessionId = UUID::generateUUID();
    }
    while(this->assignedServers.find(out_sessionId)!= this->assignedServers.end());

    out_dbRights = this->user->getDefaultDbRights();

    if(!out_dbRights.hasReadAccess() && !out_dbRights.hasWriteAccess())
    {
        throw runtime_error("User:"+this->user->getName()+" does not have access rights on database:"+dbName);
    }
    else
    {
        this->assignedServers[out_sessionId] = assignedServer;
        assignedServer->allocateClientSession(this->clientId, out_sessionId, dbName, out_dbRights);
    }
}

void Client::removeDbSession(const string& sessionId)
{
    map<string, weak_ptr<RasServer> >::iterator it;

    unique_lock<shared_mutex> lock(this->assignedServersMutex);

    it=this->assignedServers.find(sessionId);
    if(it!=this->assignedServers.end())
    {
        if(shared_ptr<RasServer> r = it->
                                     second.lock())
        {
            r->deallocateClientSession(this->clientId, sessionId);
        }
    }

    this->assignedServers.erase(sessionId);
}

void Client::removeClientFromServers()
{
    map<string, weak_ptr<RasServer> >::iterator it;
    map<string, weak_ptr<RasServer> >::iterator toErase;

    unique_lock<shared_mutex> lock(this->assignedServersMutex);

    it=this->assignedServers.begin();
    while(it!=this->assignedServers.end())
    {
        if(shared_ptr<RasServer>r=it->
                                  second.lock())
        {
            r->deallocateClientSession(this->clientId, it->first);
        }
        toErase=it;
        it++;

        this->assignedServers.erase(toErase);
    }
}

}
