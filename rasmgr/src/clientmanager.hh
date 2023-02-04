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


#ifndef RASMGR_X_SRC_CLIENTMANAGER_HH_
#define RASMGR_X_SRC_CLIENTMANAGER_HH_

#include "clientmanagerconfig.hh"
#include "clientserverrequest.hh"
#include "clientserversession.hh"

#include <string>
#include <memory>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <queue>
#include <atomic>
#include <boost/thread/shared_mutex.hpp>

namespace rasmgr
{

class Client;
class ClientCredentials;
class PeerManager;
class Server;
class ServerManager;
class UserManager;

/**
 * A struct allowing to communicate data (the session assigned to the client)
 * from the `evaluateWaitingClients()` thread back to the thread that created
 * this structure; that thread is waiting on the condition variable cv in the
 * `openClientDbSession(..)` method, and `evaluateWaitingClients()` does
 * `cv.notify()` when the client is assigned a session. The mutex mut is used
 * for synchronizing cv.
 */
struct WaitingClient {
  WaitingClient(const std::shared_ptr<Client> &c, const std::string &db)
    : client(c), serverSession(), dbName(db), assigned(false) {}
  ~WaitingClient() = default;
  /// openDB will wait on this to be notified when a server is assigned
  std::condition_variable cv;
  /// mutex for the condition variable
  std::mutex mut;
  /// the client waiting to be assigned a server
  std::shared_ptr<Client> client;
  /// the assigne server session
  ClientServerSession serverSession;
  /// database name to open
  std::string dbName;
  /// true if a server session was assigned
  bool assigned;
};

/**
  The ClientManager class maintains a registry of clientId -> active client, as
  well as a queue of `Client`s waiting to be assigned to an available server.
  It allows to
  
  - register and deregister clients into its registry on ``connectClient`` and 
    ``disconnectClient`` calls respectively (handled in the 
    `ClientManagementService`). In ``connectClient`` the provided credentials
    are authenticated either via username/password or token mechanism.
  
  - open a db session assigns the client to an available rasserver.
    
    1. The client is added to a queue
  
    2. A separate thread is responsible for taking clients from the queue and
       assigning them as servers become available. This thread is triggered after
       a client
  
       - is added to the queue (1. above)
       - properly closes a DB session
       - is determined to be dead
       - or when none of the above have happened within the last 15 seconds.
  
       When the client queue checking is triggered, the thread
  
       - checks if any client is in the queue
       - if yes, then check if there are available servers to assign
       - if yes, remove the client from the queue and assign to a server
       - otherwise wait until triggered again
  
    An error is thrown if the queue is filled above a fixed capacity of 1000
    clients.
  
  - close a db session deallocates the client on the assigned local or remote
    server.
  
  - extend the "life" of the `Client` on request from the connected client
    (with a KeepAlive call to `ClientManagementService`). When the client
    fails to issue a KeepAlive call within a certain time period, the 
    corresponding `Client` will be considered dead by the cleanup thread and
    removed from the client list.
  
  - runs a cleanup thread at a fixed interval that removes dead clients from its
    registry
  
  - runs a queue check thread at a fixed interval of 15 seconds that tries to
    assign clients in a waiting queue to any available servers
  
  Used by the `ClientManagementService` to handle network requests from clients.
 */
class ClientManager
{
public:
    /**
     * @param config client configuration
     * @param userManager Instance of the user manager that holds information
     * about registered users. It is needed to evaluate the access credentials
     * @param serverManager Instance of the server manager that is used to retrieve
     * servers for clients of each client
     * @param peerManager the peer manager
     */
    ClientManager(const ClientManagerConfig &config,
                  std::shared_ptr<UserManager> userManager,
                  std::shared_ptr<ServerManager> serverManager,
                  std::shared_ptr<PeerManager> peerManager);

    /**
     * Destruct the ClientManager class object.
     */
    virtual ~ClientManager();

    /**
     * Authenticate and connect the client to rasmgr. If the authentication
     * 
     * - succeeds, the UUID assigned to the client (out_clientUUID) will be returned
     * - fails, an exception is thrown
     * 
     * @param clientCredentials Credentials used to authenticate the client.
     * @param rasmgrHost The rasmgr hostname to which to connect
     * @param out_clientUUID The UUID assigned to the connected client
     * @throws InexistentUserException
     * @throws InvalidTokenException
     */
    virtual void connectClient(const ClientCredentials &clientCredentials,
                               const std::string &rasmgrHost, std::string &out_clientUUID);

    /**
     * Disconnect the client from rasmgr and remove it from its assigned server
     * if any. If the clientId is not found in the list of connected clients,
     * no error is thrown and only a message is logged in the rasmgr log.
     * @param clientId UUID of the client that will be disconnected.
     */
    virtual void disconnectClient(const std::string &clientId);

    /**
     * Open a DB session for the client with clientId and return a unique session id.
     * @param clientId UUID identifying the client
     * @param dbName Database the client wants to open
     * @param out_serverSession session ID identifying the client and assigned server.
     * @throws InexistentClientException
     * @throws NoAvailableServerException
     * @throws common::RuntimeException on invalid server hostname
     */
    virtual void openClientDbSession(const std::string &clientId,
                                     const std::string &dbName,
                                     ClientServerSession &out_serverSession);

    /**
     * Remove a client session from the client manager and assigned server.
     * @param clientId ID that uniquely identifies a client
     * @param sessionId ID that uniquely identifies a client session
     * @throws InexistentClientException
     */
    virtual void closeClientDbSession(const std::string &clientId,
                                      const std::string &sessionId);

    /**
     * Extend the liveliness of the client and prevent it from being removed
     * from rasmgr database of active clients.
     * @param clientId UUID of the client
     */
    virtual void keepClientAlive(const std::string &clientId);

    /**
     *  Get a copy of the configuration object used by the client manager.
     */
    const ClientManagerConfig &getConfig();

private:
    ClientManagerConfig config;
    std::shared_ptr<UserManager> userManager;
    std::shared_ptr<ServerManager> serverManager;
    std::mutex serverManagerMutex; /*! Mutex used to prevent a free server being assigned to two different clients when tryGetFreeLocalServer is called*/
    std::shared_ptr<PeerManager> peerManager;
    // -------------------------------------------------------------------------
    // manage all clients
    std::map<std::string, std::shared_ptr<Client>> clients; /*! Map of clientId -> active client */
    boost::shared_mutex clientsMutex; /*! Mutex used to synchronize access to the clients object*/

    std::unique_ptr<std::thread> checkAssignedClientsThread; /*! Thread used to manage the list of clients and remove dead ones */
    std::mutex checkAssignedClientsMutex;/*! Mutex used to safely stop the worker thread */
    std::condition_variable checkAssignedClientsCondition; /*! Condition variable used to stop the worker thread */
    bool isCheckAssignedClientsThreadRunning; /*! Flag used to stop the worker thread */
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // manage waiting clients
    std::queue<WaitingClient*> waitingClients;
    boost::shared_mutex waitingClientsMutex; /*! Mutex used to synchronize access to the waitingClients object*/
    
    std::unique_ptr<std::thread> checkWaitingClientsThread; /*! Thread used to check the queue of waiting clients */
    std::mutex checkWaitingClientsMutex;/*! Mutex used with the checkWaitingClientsThreadCondition */
    std::condition_variable checkWaitingClientsCondition; /*! Condition variable used to trigger waiting client checking thread */
    bool isCheckWaitingClientsThreadRunning; /*! Flag used to stop the waiting client checking thread */
    std::atomic<bool> isCheckWaitingClientsConditionWaiting{false};
    // -------------------------------------------------------------------------

    /// Evaluate the list of clients assigned to a server and remove the ones that have died.
    void evaluateAssignedClients();
    
    /// Evaluate the list of clients waiting to be assigned to a server.
    void evaluateWaitingClients();
    
    /// Notify the thread to check the queue of waiting clients
    void notifyWaitingClientsThread();
    std::mutex notifyWaitingClientsThreadMutex;
    
    /**
     * Open a DB session for the client and return a unique session id.
     * @param client the client to be assigned
     * @param dbName Database the client wants to open
     * @param out_serverSession session ID identifying the client and assigned server.
     * @return true if an available server was found, false otherwise.
     * @throws InexistentClientException
     * @throws NoAvailableServerException
     * @throws common::RuntimeException on invalid server hostname
     */
    virtual bool tryGetFreeServer(const std::shared_ptr<Client> &client,
                                  const std::string &dbName,
                                  ClientServerSession &out_serverSession);
    

    /// Try up to 3 times to acquire a free server for the client.
    bool tryGetFreeLocalServer(std::shared_ptr<Client> client,
                               const std::string &dbName,
                               ClientServerSession &out_serverSession);

    /// Try to get a free remote server for the client.
    bool tryGetFreeRemoteServer(const ClientServerRequest &request,
                                ClientServerSession &out_serverSession);
};

} /* namespace rasmgr */

#endif /* CLIENTMANAGER_HH_ */
