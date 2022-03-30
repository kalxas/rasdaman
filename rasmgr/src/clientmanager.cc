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
#include <chrono>

#include <logging.hh>
#include "common/uuid/uuid.hh"
#include "common/string/stringutil.hh"

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
using common::UUID;
using common::Timer;
using std::map;
using std::pair;
using std::runtime_error;
using std::string;

ClientManager::ClientManager(const ClientManagerConfig &config,
                             std::shared_ptr<UserManager> userManager,
                             std::shared_ptr<ServerManager> serverManager,
                             std::shared_ptr<PeerManager> peerManager):
    config(config),
    userManager(userManager),
    serverManager(serverManager),
    peerManager(peerManager)
{
    this->isThreadRunning = true;
    this->managementThread.reset(
        new std::thread(&ClientManager::evaluateClientsStatus, this));
}

ClientManager::~ClientManager()
{
    try
    {
        {
            std::lock_guard<std::mutex> lock(this->threadMutex);
            this->isThreadRunning = false;
        }
        this->isThreadRunningCondition.notify_one();
        this->managementThread->join();
    }
    catch (std::exception &ex)
    {
        LERROR << "ClientManager destructor has failed: " << ex.what();
    }
    catch (...)
    {
        LERROR << "ClientManager destructor has failed";
    }
}

void ClientManager::connectClient(const ClientCredentials &clientCredentials, string &out_clientUUID)
{
    /**
     * 1. Check if there is a user with the given credentials
     * 2. Generate a unique ID for the client
     * 3. Add the client to the list of managed clients
     */

    std::shared_ptr<User> out_user;

    bool isUserValid = this->userManager->tryGetUser(clientCredentials.getUserName(), 
                                                  clientCredentials.getPasswordHash(),
                                                  out_user);

    if (isUserValid)
    {
        LDEBUG << "Successfully authenticated user " << out_user->getName();
        
        //Lock access to this area.
        boost::unique_lock<boost::shared_mutex> lock(this->clientsMutex);
        // Generate a UID for the client
        do
        {
            out_clientUUID = UUID::generateUUID();
        }
        while (this->clients.find(out_clientUUID) != this->clients.end());
        
        out_user->setPassword(clientCredentials.getPasswordHash());

        auto client = std::make_shared<Client>(out_clientUUID, out_user, 
                                               this->config.getClientLifeTime());

        this->clients.insert(std::make_pair(out_clientUUID, client));
    }
    else
    {
        throw InexistentUserException(clientCredentials.getUserName());
    }
}

void ClientManager::disconnectClient(const std::string &clientId)
{
    /**
     * 1. Find the client with the given id. If the client is not in our list, just log a message.
     * 2. Remove the client from all the servers it might still be in. This ensures a clean exit
     * 3. Remove the client data from our registry.
     */

    boost::upgrade_lock<boost::shared_mutex> lock(this->clientsMutex);

    auto it = this->clients.find(clientId);
    if (it != clients.end())
    {
        //Remove the client from all the servers where it had opened sessions
        it->second->removeClientFromServers();

        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
        this->clients.erase(it);

        LDEBUG << "Client " << clientId << " has been removed from the active clients list";
    }
    else
    {
        LWARNING << "Client " << clientId << " was not found in the active clients list";
    }
}

void ClientManager::openClientDbSession(std::string clientId, const std::string &dbName,
                                        ClientServerSession &out_serverSession)
{
    boost::shared_lock<boost::shared_mutex> lock(this->clientsMutex);

    auto it = this->clients.find(clientId);
    if (it != this->clients.end())
    {
        auto client = it->second;
        
        const char* serverType; // used only for debugging
        if (tryGetFreeLocalServer(client, dbName, out_serverSession))
        {
            serverType = "local";
        }
        else
        {
            // Try to get a remote server for the client.
            ClientServerRequest request(client->getUser()->getName(),
                                        client->getUser()->getPassword(),
                                        dbName);

            if (this->tryGetFreeRemoteServer(request, out_serverSession))
            {
                serverType = "remote";
            }
            else
            {
                throw NoAvailableServerException();
            }
        }
        
        LDEBUG << "Allocated " << serverType << " server running on " 
               << out_serverSession.serverHostName << ":" << out_serverSession.serverPort
               << " to client " << out_serverSession.clientSessionId;
    }
    else
    {
        throw InexistentClientException(
            clientId, "cannot assign a rasserver to it, possibly the client "
                      "failed to successfully connect first?");
    }
}

void ClientManager::closeClientDbSession(const std::string &clientId, const std::string &sessionId)
{
    boost::shared_lock<boost::shared_mutex> lock(this->clientsMutex);

    RemoteClientSession clientSession(clientId, sessionId);

    auto it = this->clients.find(clientId);
    if (it != this->clients.end())
    {
        it->second->removeDbSession(sessionId);
    }
    else if (this->peerManager->isRemoteClientSession(clientSession))
    {
        this->peerManager->releaseServer(clientSession);
    }
    else
    {
        throw InexistentClientException(
            clientId, "cannot close the client connection to rasserver.");
    }
}

void ClientManager::keepClientAlive(const std::string &clientId)
{
    boost::shared_lock<boost::shared_mutex> lock(this->clientsMutex);

    auto it = this->clients.find(clientId);
    if (it != this->clients.end())
    {
        it->second->resetLiveliness();
    }
    else
    {
        throw InexistentClientException(
            clientId, "cannot reset client's liveliness.");
    }
}

const ClientManagerConfig &ClientManager::getConfig()
{
    return this->config;
}


void ClientManager::evaluateClientsStatus()
{
    std::chrono::milliseconds timeToSleepFor(this->config.getCleanupInterval());

    std::unique_lock<std::mutex> threadLock(this->threadMutex);
    while (this->isThreadRunning)
    {
        try
        {
            // Wait on the condition variable to be notified from the
            // destructor when it is time to stop the worker thread
            if (this->isThreadRunningCondition.wait_for(threadLock, timeToSleepFor) == std::cv_status::timeout)
            {
                boost::upgrade_lock<boost::shared_mutex> clientsLock(this->clientsMutex);
                auto it = this->clients.begin();
                while (it != this->clients.end())
                {
                    const auto &clientId = it->second->getClientId();
                    try
                    {
                        if (!it->second->isAlive())
                        {
                            LDEBUG << "Removing client from client list as it seems to be dead: " << clientId;
                            it->second->removeClientFromServers();
                            boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueClientsLock(clientsLock);
                            it = this->clients.erase(it);
                        }
                        else
                        {
                            ++it;
                        }
                    }
                    catch (std::exception &ex)
                    {
                        LERROR << "Failed evaluating status of client " << clientId
                               << ": " << ex.what();
                    }
                    catch (...)
                    {
                        LERROR << "Failed evaluating status of client " << clientId;
                    }
                }
            }
        }
        catch (std::exception &ex)
        {
            LERROR << "Failed evaluating status of clients: " << ex.what();
        }
        catch (...)
        {
            LERROR << "Failed evaluating status of clients.";
        }
    }
}

bool ClientManager::tryGetFreeLocalServer(std::shared_ptr<Client> client,
                                          const std::string &dbName,
                                          ClientServerSession &out_serverSession)
{
    std::uint32_t attemptsLeft = MAX_GET_SERVER_RETRIES;
    auto intervalBetweenAttempts = boost::posix_time::milliseconds(INTERVAL_BETWEEN_GET_SERVER);
    bool foundServer = false;
    /**
     * 1. Try for a fixed number of times to acquire a server.
     *    If there is no server available, unlock access to this section and
     *    sleep for a given timeout.
     * 2. Add the session to the client manager.
     * 3. Fill the response to the client with the server's identity
     */
    while (attemptsLeft >= 1)
    {
        std::unique_lock<std::mutex> lock(this->serverManagerMutex);

        std::shared_ptr<Server> assignedServer;

        //Try to get a free server that contains the requested database
        if (this->serverManager->tryGetFreeServer(dbName, assignedServer))
        {
            std::string dbSessionId;

            // A value will be assigned to dbSessionId by the ID
            client->addDbSession(dbName, assignedServer, dbSessionId);

            out_serverSession.clientSessionId = client->getClientId();
            out_serverSession.dbSessionId = dbSessionId;
            out_serverSession.serverHostName = assignedServer->getHostName();
            out_serverSession.serverPort = static_cast<std::uint32_t>(assignedServer->getPort());

            foundServer = true;
            break;
        }
        else if (attemptsLeft > 1)
        {
            // If there are still attempts left, unlock access to this region and sleep
            lock.unlock();
            boost::this_thread::sleep(intervalBetweenAttempts);
            lock.lock();
        }

        --attemptsLeft;
    }

    return foundServer;
}

bool ClientManager::tryGetFreeRemoteServer(const ClientServerRequest &request, ClientServerSession &out_reply)
{
    return this->peerManager->tryGetRemoteServer(request, out_reply);
}

} /* namespace rasmgr */
