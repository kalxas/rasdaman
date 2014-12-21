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

/* SOURCE: ClientPool.cc
 *
 * MODULE:  communication/socket/server
 * CLASS: ClientPool
 *
 * COMMENTS: ClientPool is a container for information
 * regarding the liveliness of a set of clients
 *
 */
#include <stdexcept>
#include <algorithm>

#include <boost/thread.hpp>

#include "../../../src/util/proto/protozmq.hh"

#include "clientpool.hh"

namespace rasnet
{

using std::runtime_error;
using std::map;
using std::string;
using std::pair;
using boost::unique_lock;
using boost::shared_lock;
using boost::shared_mutex;

ClientPool::ClientPool()
{
    this->pollPeriod = -1;
}

ClientPool::~ClientPool()
{}

void ClientPool::addClient(std::string clientId, boost::int32_t period, boost::int32_t retries)
{
    unique_lock<shared_mutex> lock(this->clientsMutex);
    PeerStatus status(retries, period);
    pair<map<string, PeerStatus>::iterator, bool> insertRes = this->clients.insert(
                pair<string, PeerStatus>(clientId, status));
    if(this->pollPeriod == -1)
    {
        this->pollPeriod = period;
    }
    else
    {
        this->pollPeriod = std::min(this->pollPeriod,period);
    }
    //There is already a client with that id so just reset its status
    if (insertRes.second == false)
    {
        insertRes.first->second.reset();
    }
}

boost::int32_t ClientPool::getMinimumPollPeriod() const
{
    return this->pollPeriod;
}

void ClientPool::resetClientStatus(std::string clientId)
{
    shared_lock<shared_mutex> lock(this->clientsMutex);
    map<string, PeerStatus>::iterator it = this->clients.find(clientId);
    if (it != this->clients.end())
    {
        it->second.reset();
    }
}

void ClientPool::pingAllClients(zmq::socket_t& socket)
{
    AlivePing ping;
    map<string, PeerStatus>::iterator it;

    shared_lock<shared_mutex> lock(this->clientsMutex);

    for (it = this->clients.begin(); it != this->clients.end(); it++)
    {
        //Ping only clients that have not sent a message in some time
        if(it->second.decreaseLiveliness())
        {
            ProtoZmq::zmqSendToPeer(socket, ping, it->first);
        }
    }
}

void ClientPool::removeClient(std::string clientId)
{
    unique_lock<shared_mutex> lock(this->clientsMutex);
    this->clients.erase(clientId);
}

void ClientPool::removeDeadClients()
{
    AlivePing ping;
    map<string, PeerStatus>::iterator it;
    map<string, PeerStatus>::iterator toErase;

    unique_lock<shared_mutex> lock(this->clientsMutex);

    it = this->clients.begin();
    while (it != this->clients.end())
    {
        it->second.decreaseLiveliness();
        if (!it->second.isAlive())
        {
            toErase=it;
            ++it;
            this->clients.erase(toErase);
        }
        else
        {
            ++it;
        }
    }
}

bool ClientPool::isClientAlive(std::string clientId)
{
    map<string, PeerStatus>::iterator it;
    bool result = false;

    shared_lock<shared_mutex> lock(this->clientsMutex);
    it= this->clients.find(clientId);

    if (it != this->clients.end())
    {
        result = it->second.isAlive();
    }

    return result;
}

void ClientPool::removeAllClients()
{
    unique_lock<shared_mutex> lock(this->clientsMutex);
    this->clients.clear();
}
}
/* namespace rasnet */
