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

#include "rasnet/src/messages/base.pb.h"
#include "rasnet/src/messages/communication.pb.h"
#include "rasnet/src/util/proto/protozmq.hh"
#include "rasnet/src/util/proto/zmqutil.hh"
#include "common/src/uuid/uuid.hh"
#include "server/rasserver_entry.hh"

namespace rasserver
{

using std::string;
using std::pair;
using std::make_pair;
using std::map;
using common::Timer;
using zmq::socket_t;
using rasnet::ProtoZmq;
using rasnet::InternalDisconnectReply;
using rasnet::InternalDisconnectRequest;
using boost::scoped_ptr;
using boost::unique_lock;
using boost::shared_mutex;
using boost::shared_ptr;
using boost::shared_lock;
using boost::thread;
using boost::unique_lock;
using common::UUID;
using rasnet::ZmqUtil;

ClientManager::ClientManager()
{
    this->controlEndpoint = ZmqUtil::toInprocAddress(UUID::generateUUID());
    this->managementThread.reset(
                new thread(&ClientManager::evaluateClientStatus, this));

    this->controlSocket.reset(new socket_t(this->context, ZMQ_PAIR));
    this->controlSocket->connect(this->controlEndpoint.c_str());
}

ClientManager::~ClientManager()
{
    try
    {
        // Kill the thread in a clean way
        rasnet::InternalDisconnectRequest request = rasnet::InternalDisconnectRequest::default_instance();
        base::BaseMessage reply;

        ProtoZmq::zmqSend(*(this->controlSocket.get()), request);
        ProtoZmq::zmqReceive(*(this->controlSocket.get()), reply);

        if(reply.type()!=rasnet::InternalDisconnectReply::default_instance().GetTypeName())
        {
            LERROR<<"Unexpected message received from control socket."<<reply.DebugString();
        }

        this->managementThread->join();
    }
    catch (std::exception& ex)
    {
        LERROR<<ex.what();
    }
    catch (...)
    {
        LERROR<<"ClientManager destructor has failed";
    }
}

bool ClientManager::allocateClient(std::string clientUUID, std::string sessionId)
{
    Timer timer(ALIVE_PERIOD);

    pair<map<string, Timer>::iterator, bool> result = this->clientList.insert(make_pair(clientUUID, timer));
    return result.second;
}

void ClientManager::deallocateClient(std::string clientUUID, std::string sessionId)
{
    this->clientList.erase(clientUUID);
}

bool ClientManager::isAlive(std::string clientUUID)
{
   map<string, Timer>::iterator clientIt = this->clientList.find(clientUUID);
   if (clientIt == this->clientList.end())
   {
       return false;
   }
   return !clientIt->second.hasExpired();
}

void ClientManager::resetLiveliness(std::string clientUUID)
{
    map<string, Timer>::iterator clientIt = this->clientList.find(clientUUID);
    if (clientIt != this->clientList.end())
    {
        clientIt->second.reset();
    }
}

size_t ClientManager::getClientQueueSize()
{
    return this->clientList.size();
}

void ClientManager::evaluateClientStatus()
{
    map<string, Timer>::iterator it;
    map<string, Timer>::iterator toErase;
    base::BaseMessage controlMessage;
    bool keepRunning=true;

    try
    {
        zmq::socket_t control(this->context, ZMQ_PAIR);
        control.bind(this->controlEndpoint.c_str());
        zmq::pollitem_t items[] = {{control,0,ZMQ_POLLIN,0}};

        while (keepRunning)
        {
            zmq::poll(items, 1, ALIVE_PERIOD);
            if (items[0].revents & ZMQ_POLLIN)
            {
                ProtoZmq::zmqReceive(control, controlMessage);
                if(controlMessage.type()==InternalDisconnectRequest::default_instance().GetTypeName())
                {
                    keepRunning=false;
                    InternalDisconnectReply disconnectReply;
                    ProtoZmq::zmqSend(control, disconnectReply);
                }
            }
            else
            {
                boost::upgrade_lock<boost::shared_mutex> lock(this->clientMutex);
                it = this->clientList.begin();

                while (it != this->clientList.end())
                {
                    toErase=it;
                    ++it;
                    if(toErase->second.hasExpired())
                    {
                        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
                        this->clientList.erase(toErase);

                        // If the client dies, clean up
                        RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
                        rasServerEntry.compat_abortTA();
                        rasServerEntry.compat_closeDB();
                        rasServerEntry.compat_disconnectClient();
                    }
                }
            }
        }
    }
    catch (std::exception& ex)
    {
        LERROR<<"Client management thread has failed";
        LERROR<<ex.what();
    }
    catch (...)
    {
        LERROR<<"Client management thread failed for unknown reason.";
    }
}

}
