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

#include "client.hh"
#include "clientcredentials.hh"
#include "user.hh"
#include "usermanager.hh"
#include "servermanager.hh"
#include "server.hh"
#include "clientmanager.hh"
#include "peermanager.hh"

#include "exceptions/inexistentuserexception.hh"
#include "exceptions/inexistentclientexception.hh"
#include "exceptions/noavailableserverexception.hh"

#include "common/uuid/uuid.hh"
#include "common/string/stringutil.hh"

#include <logging.hh>

#include <iostream>
#include <stdexcept>
#include <string>
#include <chrono>

// clients will be put in a waiting queue first, from which a thread is taking
// one by one and assigning a server on OpenDb request
#define CLIENT_QUEUEING_ENABLED

namespace rasmgr
{

ClientManager::ClientManager(const ClientManagerConfig &c,
                             std::shared_ptr<UserManager> um,
                             std::shared_ptr<ServerManager> sm,
                             std::shared_ptr<PeerManager> pm):
    config(c), userManager(um), serverManager(sm), peerManager(pm)
{
    this->isCheckAssignedClientsThreadRunning = true;
    this->checkAssignedClientsThread.reset(new std::thread(&ClientManager::evaluateAssignedClients, this));
#ifdef CLIENT_QUEUEING_ENABLED
    this->isCheckWaitingClientsThreadRunning = true;
    this->checkWaitingClientsThread.reset(new std::thread(&ClientManager::evaluateWaitingClients, this));
#endif
}

ClientManager::~ClientManager()
{
    try
    {
        // exit checkAssignedClientsThread
        {
            std::lock_guard<std::mutex> lock(this->checkAssignedClientsMutex);
            this->isCheckAssignedClientsThreadRunning = false;
        }
        this->checkAssignedClientsCondition.notify_one();
        this->checkAssignedClientsThread->join();
    
#ifdef CLIENT_QUEUEING_ENABLED
        // exit checkWaitingClientsThread
        {
            LDEBUG << "~ClientManager() lock checkWaitingClientsMutex";
            std::lock_guard<std::mutex> lock(this->checkWaitingClientsMutex);
            this->isCheckWaitingClientsThreadRunning = false;
            LDEBUG << "~ClientManager() lock checkWaitingClientsMutex released";
        }
        this->checkWaitingClientsCondition.notify_one();
        LDEBUG << "~ClientManager() - wait for checkWaitingClientsThread to exit";
        this->checkWaitingClientsThread->join();
        LDEBUG << "~ClientManager() - wait for checkWaitingClientsThread to exit done";
    
        // notify all threads waiting in openClientDbSession
        while (!this->waitingClients.empty())
        {
            auto wc = this->waitingClients.front();
            {
                LDEBUG << "~ClientManager() - lock WaitingClient mutex";
                std::lock_guard<std::mutex> lock(wc->mut);
                wc->assigned = true;
                LDEBUG << "~ClientManager() - lock WaitingClient mutex done";
            }
            LDEBUG << "~ClientManager() - notify WaitingClient condition variable";
            wc->cv.notify_one();
            LDEBUG << "~ClientManager() - pop waitingClients";
            this->waitingClients.pop();
        }
#endif
    }
    catch (std::exception &ex)
    {
        LERROR << "Client manager destructor failed: " << ex.what();
    }
    catch (...)
    {
        LERROR << "Client manager destructor failed";
    }
}

void ClientManager::connectClient(const ClientCredentials &clientCredentials, 
                                  const std::string &rasmgrHost,
                                  std::string &out_clientUUID)
{
    /**
     * 1. Check if there is a user with the given credentials
     * 2. Generate a unique ID for the client
     * 3. Add the client to the list of managed clients
     */
    LDEBUG << "Connecting client username: " << clientCredentials.getUserName();

    std::shared_ptr<User> out_user;

    bool isUserValid = this->userManager->tryGetUser(clientCredentials.getUserName(), 
                                                  clientCredentials.getPasswordHash(),
                                                  out_user);

    if (isUserValid)
    {
        LDEBUG << "Successfully authenticated user " << out_user->getName();
        
        // Exclusive lock to make sure no conflicting UUID is generated
        // and to avoid concurrent changes to the clients list
        boost::upgrade_lock<boost::shared_mutex> sharedLock(this->clientsMutex);
        // Generate a UID for the client
        do
        {
            out_clientUUID = common::UUID::generateUUID();
        }
        while (this->clients.find(out_clientUUID) != this->clients.end());
        
        out_user->setPassword(clientCredentials.getPasswordHash());

        auto client = std::make_shared<Client>(out_clientUUID, out_user, 
                                               this->config.getClientLifeTime(),
                                               rasmgrHost);

        LDEBUG << "Inserting client object " << out_clientUUID << " into clients list...";
        boost::upgrade_to_unique_lock<boost::shared_mutex> exclusiveLock(sharedLock);
        LDEBUG << "Inserted client object " << out_clientUUID << " into clients list.";
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
        it->second->removeClientFromServer();

        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
        this->clients.erase(it);

        LDEBUG << "Client " << clientId << " has been removed from the active clients list";
    }
    else
    {
        LDEBUG << "Client " << clientId << " was not found in the active "
               << "clients list while trying to disconnect it, nothing to do.";
    }
}

void ClientManager::openClientDbSession(const std::string &clientId,
                                        const std::string &dbName,
                                        ClientServerSession &out_serverSession)
{
    std::shared_ptr<Client> client;
    {
        boost::shared_lock<boost::shared_mutex> lock(this->clientsMutex);
        auto it = this->clients.find(clientId);
        if (it != this->clients.end())
        {
            client = it->second;
        }
        else
        {
            throw InexistentClientException(
                clientId, "cannot assign a rasserver to it, possibly the client "
                          "failed to successfully connect first?");
        }
    }
    
#ifdef CLIENT_QUEUEING_ENABLED
    
    // check for queue overflow
    static const auto maxClientQueueSize = this->config.getMaxClientQueueSize();
    {
        boost::shared_lock<boost::shared_mutex> sharedLock(this->waitingClientsMutex);
        if (std::int32_t(this->waitingClients.size()) > maxClientQueueSize)
        {
            throw NoAvailableServerException();
        }
    }
    
    std::unique_ptr<WaitingClient> wc;
    wc.reset(new WaitingClient(client, dbName));
    {
        LDEBUG << "openClientDbSession - unique_lock on WaitingClient->mut " << clientId;
        std::unique_lock<std::mutex> lock(wc->mut);
        {
            LDEBUG << "openClientDbSession - lock_guard on waitingClientsMutex " << clientId;
            boost::lock_guard<boost::shared_mutex> exclusiveLock(this->waitingClientsMutex);
            LDEBUG << "openClientDbSession - add client to waitingClients " << clientId;
            this->waitingClients.push(wc.get());
            LDEBUG << "openClientDbSession - lock_guard on waitingClientsMutex released " << clientId;
        }
        // wake up waiting clients thread as there's a new client in the queue
        LDEBUG << "openClientDbSession - notifyWaitingClientsThread() " << clientId;
        this->notifyWaitingClientsThread();
        while (!wc->assigned)
        {
            LDEBUG << "openClientDbSession - WaitingClient->cv.wait() " << clientId;
            wc->cv.wait(lock);
        }
        if (this->isCheckAssignedClientsThreadRunning)
        {
            LDEBUG << "openClientDbSession - assigning client-server session to currentSession for client " << clientId;
            out_serverSession = wc->serverSession;
        }
        LDEBUG << "openClientDbSession - unique_lock on WaitingClient->mut released " << clientId;
    }
    
#else
    
    bool opened = this->tryGetFreeServer(client, dbName, out_serverSession);
    if (!opened)
    {
        throw NoAvailableServerException();
    }
    
#endif
}

void ClientManager::closeClientDbSession(const std::string &clientId,
                                         const std::string &sessionId)
{
    boost::shared_lock<boost::shared_mutex> lock(this->clientsMutex);

    auto it = this->clients.find(clientId);
    if (it != this->clients.end())
    {
        it->second->removeDbSession(sessionId);
    }
    else
    {
        RemoteClientSession clientSession(clientId, sessionId);
        if (this->peerManager->isRemoteClientSession(clientSession))
        {
            this->peerManager->releaseServer(clientSession);
        }
        else
        {
            throw InexistentClientException(
                clientId, "cannot close the client connection to rasserver.");
        }
    }
    
    // wake up waiting clients thread as one client has finished evaluation
    LDEBUG << "closeClientDbSession - notifyWaitingClientsThread() " << clientId;
    this->notifyWaitingClientsThread();
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

void ClientManager::evaluateAssignedClients()
{
    std::chrono::milliseconds timeToSleepFor(this->config.getCleanupInterval()); // 3s

    std::unique_lock<std::mutex> threadLock(this->checkAssignedClientsMutex);
    while (this->isCheckAssignedClientsThreadRunning)
    {
        // Wait on the condition variable to be notified from the
        // ~ClientManager() destructor when it is time to stop the worker thread,
        // or until the cleanup timeout is reached (3 seconds by default)
        if (this->checkAssignedClientsCondition.wait_for(threadLock, timeToSleepFor) ==
            std::cv_status::timeout)
        {
            boost::upgrade_lock<boost::shared_mutex> clientsLock(this->clientsMutex);
            LTRACE << "Evaluating assigned clients.";
            
            auto it = this->clients.begin();
            while (it != this->clients.end())
            {
                auto client = it->second;
                const auto &clientId = client->getClientId();
                try
                {
                    if (!client->isAlive())
                    {
                        LDEBUG << "Removing client from assigned client list as it seems to be dead: " << clientId;
                        {
                            boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(clientsLock);
                            it = this->clients.erase(it);
                        }
                        client->removeClientFromServer();

                        // wake up waiting clients thread as a client has been removed,
                        // potentially releasing space for a new client to be assigned a server
                        LDEBUG << "evaluateAssignedClients() - notifyWaitingClientsThread() " << clientId;
                        this->notifyWaitingClientsThread();
                    }
                    else
                    {
                        ++it;
                    }
                }
                catch (common::Exception &ex)
                {
                    LERROR << "Failed evaluating status of assigned client " << clientId << ": " << ex.what();
                }
                catch (std::exception &ex)
                {
                    LERROR << "Failed evaluating status of assigned client " << clientId << ": " << ex.what();
                }
                catch (...)
                {
                    LERROR << "Failed evaluating status of assigned client " << clientId;
                }
            }
        }
    }
}

void ClientManager::evaluateWaitingClients()
{
    std::chrono::milliseconds timeToSleepFor(this->config.getClientLifeTime() / 2); // 15s
  
    std::unique_lock<std::mutex> threadLock(this->checkWaitingClientsMutex);
    while (this->isCheckWaitingClientsThreadRunning)
    {
        // Wait on the condition variable to be notified when it is time to
        // check the queue of assigned clients, or until the cleanup timeout 
        // is reached (3 seconds by default)
        LDEBUG << "evaluateWaitingClients() - wait on checkWaitingClientsCondition or 15s timeout";
        this->isCheckWaitingClientsConditionWaiting = true;
        this->checkWaitingClientsCondition.wait_for(threadLock, timeToSleepFor);
        this->isCheckWaitingClientsConditionWaiting = false;
        LDEBUG << "evaluateWaitingClients() - wait on checkWaitingClientsCondition wakeup";
    
        //LDEBUG << "evaluateWaitingClients() - upgrade_lock on waitingClientsMutex";
        LDEBUG << "evaluateWaitingClients() - upgrade_lock on waitingClientsMutex";
        boost::upgrade_lock<boost::shared_mutex> sharedLock(this->waitingClientsMutex);
        LDEBUG << "evaluateWaitingClients() - upgrade_lock on waitingClientsMutex acquired " << this->waitingClients.size() << " waiting clients.";
        
        bool removed = true;
        while (removed && !this->waitingClients.empty())
        {
            auto wc = this->waitingClients.front();
            auto client = wc->client;
            const auto &clientId = client->getClientId();
            LDEBUG << "evaluateWaitingClients() - check if client is alive " << clientId;
            try
            {
                removed = false;
                ClientServerSession session;
                if (client->isAlive())
                {
                    // try to assign a server
                    LDEBUG << "evaluateWaitingClients() - client is alive, try to assign a server " << clientId;
                    {
                        removed = tryGetFreeServer(client, wc->dbName, wc->serverSession);
                        if (removed)
                        {
                            LDEBUG << "evaluateWaitingClients() - client was assigned a server, will remove it from waiting list " << clientId;
                        }
                        else
                        {
                            LDEBUG << "evaluateWaitingClients() - could not assign a server, keep waiting " << clientId;
                        }
                    }
                }
                else
                {
                    LDEBUG << "evaluateWaitingClients() - removing client from waiting client list as it seems to be dead " << clientId;
                    removed = true;
                }
                
                if (removed)
                {
                    // remove client from waiting queue
                    {
                        LDEBUG << "evaluateWaitingClients() - upgrade_to_unique_lock on waitingClientsMutex " << clientId;
                        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(sharedLock);
                        LDEBUG << "evaluateWaitingClients() - pop from waitingClients " << clientId;
                        this->waitingClients.pop();
                        LDEBUG << "evaluateWaitingClients() - upgrade_to_unique_lock on waitingClientsMutex released " << clientId;
                    }
                  
                    // notify the original thread that this has been done
                    wc->assigned = true;
                    LDEBUG << "evaluateWaitingClients() - WaitingClient->cv.notify_one() " << clientId;
                    wc->cv.notify_one();
                    LDEBUG << "evaluateWaitingClients() - WaitingClient->cv.notify_one() done: " << clientId;
                }
            }
            catch (common::Exception &ex)
            {
                LERROR << "Failed evaluating status of waiting client " << clientId << ": " << ex.what();
            }
            catch (std::exception &ex)
            {
                LERROR << "Failed evaluating status of waiting client " << clientId << ": " << ex.what();
            }
            catch (...)
            {
                LERROR << "Failed evaluating status of waiting client " << clientId;
            }
            
            LDEBUG << "evaluateWaitingClients() - upgrade_lock on waitingClientsMutex released: " << clientId;
        }
    }
}

void ClientManager::notifyWaitingClientsThread()
{
    std::lock_guard<std::mutex> methodLock(notifyWaitingClientsThreadMutex);
    if (this->isCheckWaitingClientsConditionWaiting)
    {
        LDEBUG << "notifyWaitingClientsThread() lock_guard on checkWaitingClientsMutex";
        std::lock_guard<std::mutex> lock(this->checkWaitingClientsMutex);
        LDEBUG << "notifyWaitingClientsThread() checkWaitingClientsCondition.notify_one()";
        this->checkWaitingClientsCondition.notify_one();
        LDEBUG << "notifyWaitingClientsThread() lock_guard on checkWaitingClientsMutex released";
    }
}

bool ClientManager::tryGetFreeServer(const std::shared_ptr<Client> &client,
                                     const std::string &dbName,
                                     ClientServerSession &out_serverSession)
{
    static const std::string localhost("127.0.0.1");
    static const std::string localhostName = "localhost";
    
#define NORMALIZE_LOCALHOST(h) \
    if (h.empty() || common::StringUtil::equalsCaseInsensitive(h, localhostName)) \
        h = localhost;
    
    auto clientHost = client->getRasmgrHost();
    NORMALIZE_LOCALHOST(clientHost)
    
    const char* serverType; // used only for debugging
    if (this->tryGetFreeLocalServer(client, dbName, out_serverSession))
    {
        auto serverHost = out_serverSession.serverHostName;
        NORMALIZE_LOCALHOST(serverHost)
        if (clientHost != localhost && clientHost != serverHost)
        {
            throw common::RuntimeException("No server is configured to listen on host '"
                + clientHost + "' in rasmgr.conf, the server -host is '" + serverHost + "'.");
        }
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
            auto serverHost = out_serverSession.serverHostName;
            NORMALIZE_LOCALHOST(serverHost)
            if (clientHost != localhost && clientHost != serverHost && serverHost == "127.0.0.1")
            {
                throw common::RuntimeException("No server is configured to listen on host '"
                    + clientHost + "' in rasmgr.conf, the server -host is '" + serverHost + "'.");
            }
            serverType = "remote";
        }
        else
        {
            return false;
        }
    }
    
    LDEBUG << "Allocated " << serverType << " server running on " 
           << out_serverSession.serverHostName << ":" << out_serverSession.serverPort
           << " to client " << out_serverSession.clientSessionId
           << " connected to rasmgr on " << client->getRasmgrHost();
    return true;
}

bool ClientManager::tryGetFreeLocalServer(std::shared_ptr<Client> client,
                                          const std::string &dbName,
                                          ClientServerSession &out_serverSession)
{
    /**
     * 1. Try to acquire a server.
     * 2. Add the session to the client manager.
     * 3. Fill the response to the client with the server's identity
     */
    std::unique_lock<std::mutex> lock(this->serverManagerMutex);

    bool foundServer = false;

    //Try to get a free server that contains the requested database
    std::shared_ptr<Server> assignedServer;
    if (this->serverManager->tryGetAvailableServer(dbName, assignedServer))
    {
        std::string dbSessionId;

        // A value will be assigned to dbSessionId by the ID
        client->addDbSession(dbName, assignedServer, dbSessionId);

        out_serverSession.clientSessionId = client->getClientId();
        out_serverSession.dbSessionId = dbSessionId;
        out_serverSession.serverHostName = assignedServer->getHostName();
        out_serverSession.serverPort = static_cast<std::uint32_t>(assignedServer->getPort());
        
        foundServer = true;
    }

    return foundServer;
}

bool ClientManager::tryGetFreeRemoteServer(const ClientServerRequest &request,
                                           ClientServerSession &out_reply)
{
    return this->peerManager->tryGetRemoteServer(request, out_reply);
}

} /* namespace rasmgr */
