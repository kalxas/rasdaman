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

#include <iostream>
#include <stdexcept>

#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/thread.hpp>
#include <boost/bind.hpp>

#include "../../common/src/logging/easylogging++.hh"
#include "../../common/src/uuid/uuid.hh"
#include "../../rasnet/src/util/proto/protozmq.hh"
#include "../../rasnet/src/messages/communication.pb.h"
#include "../../rasnet/src/messages/base.pb.h"

#include "clientmanager.hh"
#include "rasmgrconfig.hh"
#include "rasserver.hh"


namespace rasmgr
{
using boost::bind;
using boost::scoped_ptr;
using boost::unique_lock;
using boost::shared_mutex;
using boost::shared_ptr;
using boost::shared_lock;
using boost::thread;
using boost::unique_lock;
using boost::weak_ptr;
using common::UUID;
using common::Timer;
using std::map;
using std::pair;
using std::runtime_error;
using std::string;
using zmq::socket_t;
using rasnet::ProtoZmq;
using rasnet::InternalDisconnectReply;
using rasnet::InternalDisconnectRequest;

ClientManager::ClientManager(boost::shared_ptr<rasmgr::UserManager> userManager, const ClientManagerConfig& config):config(config)
{
    this->userManager = userManager;
    this->controlEndpoint = "inproc://"+UUID::generateUUID();

    this->managementThread.reset(
        new thread(&ClientManager::evaluateClientsStatus, this));

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

void ClientManager::connectClient(const ClientCredentials& clientCredentials, string& out_clientUUID)
{
    /**
     * 1. Check if there is a user with the given credentials
     * 2. Generate a unique ID for the client
     * 3. Add the client to the list of managed clients
     */

    boost::shared_ptr<User> out_user;

    if(this->userManager->tryGetUser(clientCredentials.getUserName(),out_user))
    {
        if(out_user->getPassword()==clientCredentials.getPasswordHash())
        {
            //Lock access to this area.
            unique_lock<shared_mutex> lock(this->clientsMutex);
            //        Generate a UID for the client
            do
            {
                out_clientUUID = UUID::generateUUID();
            }
            while (this->clients.find(out_clientUUID) != this->clients.end());

            shared_ptr<Client> client(new Client(out_clientUUID, out_user, this->config.getClientLifeTime()));

            this->clients.insert(std::make_pair(out_clientUUID, client));
        }
        else
        {
            throw runtime_error("Invalid client credentials for client :\""+clientCredentials.getUserName()+"\"");
        }
    }
    else
    {
        throw runtime_error("There is no client named \""+clientCredentials.getUserName()+"\"");
    }
}

void ClientManager::disconnectClient(const std::string& clientId)
{
    /**
     * 1. Find the client with the given id. If the client is not in our list, just log a message.
     * 2. Remove the client from all the servers it might still be in. This ensures a clean exit
     * 3. Remove the client data from our registry.
     */

    map<string, shared_ptr<Client> >::iterator it;
    boost::upgrade_lock<boost::shared_mutex> lock(this->clientsMutex);

    it = this->clients.find(clientId);

    if (it != clients.end())
    {
        //Remove the client from all the servers where it had opened sessions
        it->second->removeClientFromServers();

        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
        this->clients.erase(it);

        LINFO<<"The client with ID:\""<<clientId<<"\" has been removed from the list";
    }
    else
    {
        LINFO<<"The client with ID:\""<<clientId<<"\" was not present in the active clients list";
    }
}

void ClientManager::openClientDbSession(std::string clientId, const std::string& dbName,boost::shared_ptr<RasServer> assignedServer, std::string& out_sessionId)
{

    /**
     * 1. Determine if the client is in the list of active clients.
     * 2. Create a DBSession for the client or throw an exception.
     */

    map<string, shared_ptr<Client> >::iterator it;

    shared_lock<shared_mutex> lock(this->clientsMutex);

    it = this->clients.find(clientId);
    if(it!=this->clients.end())
    {
        it->second->addDbSession(dbName, assignedServer, out_sessionId);
    }
    else
    {
        throw runtime_error("Client with id:\""+clientId+"\" is not part of the list of active clients.");
    }
}

void ClientManager::closeClientDbSession(const std::string& clientId, const std::string& sessionId)
{
    map<string, shared_ptr<Client> >::iterator it;

    shared_lock<shared_mutex> lock(this->clientsMutex);

    it = this->clients.find(clientId);
    if(it!=this->clients.end())
    {
        it->second->removeDbSession(sessionId);
    }
    else
    {
        throw runtime_error("Client with id:\""+clientId+"\" is not part of the list of active clients.");
    }
}

void ClientManager::keepClientAlive(const std::string& clientId)
{
    map<string, shared_ptr<Client> >::iterator it;

    shared_lock<shared_mutex> lock(this->clientsMutex);

    it = this->clients.find(clientId);
    if (it != this->clients.end())
    {
        it->second->resetLiveliness();
    }
    else
    {
        throw runtime_error(
            "Client with id:" + clientId
            + " is not present in the list of active client");
    }
}


void ClientManager::evaluateClientsStatus()
{
    map<string, shared_ptr<Client> >::iterator it;
    map<string, shared_ptr<Client> >::iterator toErase;
    base::BaseMessage controlMessage;
    bool keepRunning=true;

    try
    {
        zmq::socket_t control(this->context, ZMQ_PAIR);
        control.bind(this->controlEndpoint.c_str());
        zmq::pollitem_t items[] = {{control,0,ZMQ_POLLIN,0}};

        while (keepRunning)
        {
            zmq::poll(items, 1, this->config.getCleanupInterval());
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
                boost::upgrade_lock<boost::shared_mutex> lock(this->clientsMutex);
                it = this->clients.begin();

                while (it != this->clients.end())
                {
                    toErase=it;
                    ++it;
                    if(!toErase->second->isAlive())
                    {
                        toErase->second->removeClientFromServers();

                        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
                        this->clients.erase(toErase);
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

} /* namespace rasmgr */
