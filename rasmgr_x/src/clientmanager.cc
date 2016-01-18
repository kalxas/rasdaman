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
#include <string>

#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/thread.hpp>
#include <boost/bind.hpp>

#include <easylogging++.h>
#include "../../common/src/uuid/uuid.hh"

#include "exceptions/rasmgrexceptions.hh"

#include "constants.hh"
#include "client.hh"
#include "clientcredentials.hh"
#include "user.hh"
#include "usermanager.hh"
#include "servermanager.hh"
#include "server.hh"
#include "clientmanager.hh"
#include "peermanager.hh"

namespace rasmgr
{
using boost::bind;
using boost::scoped_ptr;
using boost::unique_lock;
using boost::shared_mutex;
using boost::shared_ptr;
using boost::shared_lock;
using boost::thread;
using boost::upgrade_lock;
using boost::unique_lock;
using boost::weak_ptr;
using boost::mutex;
using common::UUID;
using common::Timer;
using std::map;
using std::pair;
using std::runtime_error;
using std::string;

ClientManager::ClientManager(const ClientManagerConfig& config,
                             boost::shared_ptr<UserManager> userManager,
                             boost::shared_ptr<ServerManager> serverManager,
                             boost::shared_ptr<PeerManager> peerManager):
    config(config),
    userManager(userManager),
    serverManager(serverManager),
    peerManager(peerManager)
{
    this->isThreadRunning = true;
    this->managementThread.reset(
        new thread(&ClientManager::evaluateClientsStatus, this));
}

ClientManager::~ClientManager()
{
    try
    {
        {
            boost::lock_guard<boost::mutex> lock(this->threadMutex);
            this->isThreadRunning = false;
        }

        this->isThreadRunningCondition.notify_one();

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
            //       dbName Generate a UID for the client
            do
            {
                out_clientUUID = UUID::generateUUID();
            }
            while (this->clients.find(out_clientUUID) != this->clients.end());

            shared_ptr<Client> client = boost::make_shared<Client>(out_clientUUID, out_user, this->config.getClientLifeTime());

            this->clients.insert(std::make_pair(out_clientUUID, client));
        }
        else
        {
            throw InvalidClientCredentialsException();
        }
    }
    else
    {
        throw InexistentUserException(clientCredentials.getUserName());
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
    upgrade_lock<shared_mutex> lock(this->clientsMutex);

    it = this->clients.find(clientId);

    if (it != clients.end())
    {
        //Remove the client from all the servers where it had opened sessions
        it->second->removeClientFromServers();

        boost::upgrade_to_unique_lock<shared_mutex> uniqueLock(lock);
        this->clients.erase(it);

        LINFO<<"The client with ID:\""<<clientId<<"\" has been removed from the list";
    }
    else
    {
        LINFO<<"The client with ID:\""<<clientId<<"\" was not present in the active clients list";
    }
}

void ClientManager::openClientDbSession(std::string clientId, const std::string& dbName, ClientServerSession& out_serverSession)
{
    shared_lock<shared_mutex> lock(this->clientsMutex);

    auto clientsIter = this->clients.find(clientId);
    if(clientsIter!=this->clients.end())
    {
        auto client = clientsIter->second;

        if(tryGetFreeLocalServer(client, dbName, out_serverSession))
        {
            LDEBUG<<"Allocated local server running on "<<out_serverSession.serverHostName<<":"<<out_serverSession.serverPort
                  <<" to client with ID "<<out_serverSession.clientSessionId;
        }
        else
        {
            // Try to get a remote server for the client.

            ClientServerRequest request(client->getUser()->getName(), client->getUser()->getPassword(), dbName);

            if(this->tryGetFreeRemoteServer(request, out_serverSession))
            {
                LDEBUG<<"Allocated remote server running on "<<out_serverSession.serverHostName<<":"<<out_serverSession.serverPort
                      <<" to client with ID "<<out_serverSession.clientSessionId;
            }
            else
            {
                throw NoAvailableServerException();
            }
        }
    }
    else
    {
        throw InexistentClientException(clientId);
    }
}

void ClientManager::closeClientDbSession(const std::string& clientId, const std::string& sessionId)
{
    shared_lock<shared_mutex> lock(this->clientsMutex);

    RemoteClientSession clientSession(clientId, sessionId);

    map<string, shared_ptr<Client> >::iterator it = this->clients.find(clientId);
    if(it!=this->clients.end())
    {
        it->second->removeDbSession(sessionId);
    }
    else if(this->peerManager->isRemoteClientSession(clientSession))
    {
        this->peerManager->releaseServer(clientSession);
    }
    else
    {
        throw InexistentClientException(clientId);
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
        throw InexistentClientException(clientId);
    }
}

const ClientManagerConfig& ClientManager::getConfig()
{
    return this->config;
}


void ClientManager::evaluateClientsStatus()
{
    map<string, shared_ptr<Client> >::iterator it;
    map<string, shared_ptr<Client> >::iterator toErase;

    boost::posix_time::time_duration timeToSleepFor = boost::posix_time::milliseconds(this->config.getCleanupInterval());

    boost::unique_lock<boost::mutex> threadLock(this->threadMutex);
    while (this->isThreadRunning)
    {
        try
        {
            // Wait on the condition variable to be notified from the
            // destructor when it is time to stop the worker thread
            if(!this->isThreadRunningCondition.timed_wait(threadLock, timeToSleepFor))
            {

                boost::upgrade_lock<boost::shared_mutex> clientsLock(this->clientsMutex);
                it = this->clients.begin();

                while (it != this->clients.end())
                {
                    toErase=it;
                    ++it;
                    try
                    {
                        if(!toErase->second->isAlive())
                        {
                            toErase->second->removeClientFromServers();

                            boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueClientsLock(clientsLock);
                            this->clients.erase(toErase);
                        }
                    }
                    catch(...)
                    {
                        LERROR<<"Evaluating status of client failed. Client ID:"<<toErase->second->getClientId();
                    }
                }
            }
        }
        catch (std::exception& ex)
        {
            LERROR<<"Evaluating client status has failed";
            LERROR<<ex.what();
        }
        catch (...)
        {
            LERROR<<"Evaluating client status has failed for an unknown reason.";
        }
    }
}

bool ClientManager::tryGetFreeLocalServer(boost::shared_ptr<Client> client, const std::string& dbName, ClientServerSession& out_serverSession)
{
    boost::uint32_t attemptsLeft = MAX_GET_SERVER_RETRIES;
    boost::posix_time::time_duration intervalBetweenAttempts = boost::posix_time::milliseconds(INTERVAL_BETWEEN_GET_SERVER);
    bool foundServer = false;
    /**
     * 1. Try for a fixed number of times to acquire a server.
     *    If there is no server available, unlock access to this section and sleep for a given timeout.
     * 2. Add the session to the client manager.
     * 3. Fill the response to the client with the server's identity
     */
    while(attemptsLeft>=1)
    {
        unique_lock<mutex> lock(this->serverManagerMutex);

        boost::shared_ptr<Server> assignedServer;

        //Try to get a free server that contains the requested database
        if(this->serverManager->tryGetFreeServer(dbName, assignedServer))
        {
            std::string dbSessionId;
            \
            // A value will be assigned to dbSessionId by the ID
            client->addDbSession(dbName, assignedServer, dbSessionId);

            out_serverSession.clientSessionId = client->getClientId();
            out_serverSession.dbSessionId = dbSessionId;
            out_serverSession.serverHostName = assignedServer->getHostName();
            out_serverSession.serverPort = static_cast<google::protobuf::uint32_t>(assignedServer->getPort());

            foundServer = true;
            break;
        }
        else if(attemptsLeft>1)
        {
            // If there are still attempts left, unlock access to this region and sleep
            lock.unlock();
            boost::this_thread::sleep(intervalBetweenAttempts);
            lock.lock();
        }

        attemptsLeft--;
    }

    return foundServer;
}

bool ClientManager::tryGetFreeRemoteServer(const ClientServerRequest &request, ClientServerSession &out_reply)
{
    return this->peerManager->tryGetRemoteServer(request, out_reply);
}

} /* namespace rasmgr */
