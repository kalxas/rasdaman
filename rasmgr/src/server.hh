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

#ifndef RASMGR_X_SRC_RASSERVER_HH_
#define RASMGR_X_SRC_RASSERVER_HH_

#include "databasehost.hh"
#include "serverconfig.hh"
#include "rasnet/messages/rassrvr_rasmgr_service.grpc.pb.h"
#include "common/grpc/messages/health_service.grpc.pb.h"
#include "rasmgr/src/messages/rasmgrmess.pb.h"

#include <string>
#include <cstdint>
#include <vector>
#include <utility>
#include <memory>
#include <sys/types.h>

#include <boost/thread/shared_mutex.hpp>
#include <grpc++/grpc++.h>

namespace rasmgr
{

class UserDatabaseRights;

/**
  Represents an interface to a server. The following operations can be performed:
  
  - start/stop a server process
  - register a server so that it becomes available
  - check if the server is running, is free, is available, is alive
  - check if a client is alive
  
    - true if server is started and client responds to GetClientStatus request
  
  - allocate a client session on the server
  
    - throws error if server has no available capacity
    - sends an AllocateClient request to the rasserver with access control
      capabilities string, client id and session id
    - add client to session list, increase the number of allocated clients,
      and increase the number of total sessions
  
  - deallocate a client session on the server
    
    - remove client from current session list, decrease number of allocated clients
  
  - get the total number of client sessions so far
  
  States:
  
  +--------------+--------------------------------------------------------------+
  | State        | Condition                                                    |
  +==============+==============================================================+
  | started      | started on startProcess                                      |
  +--------------+--------------------------------------------------------------+
  | starting     | started and not registered                                   |
  +--------------+--------------------------------------------------------------+
  | not started  | killed with KILL or FORCE level                              |
  +--------------+--------------------------------------------------------------+
  | alive        | started, process exists, and responds to GetServerStatus     |
  +--------------+--------------------------------------------------------------+
  | registered   | started, and rasserver responds to GetServerStatus           |
  +--------------+--------------------------------------------------------------+
  | available    | started, registered, assigned clients < max (1)              |
  +--------------+--------------------------------------------------------------+
  | free         | started, registered, no assigned clients                     |
  +--------------+--------------------------------------------------------------+
  
 */
class Server
{
public:
    /**
     * Initialize a new instance of RasServer which will run on the given host and port
     * @param config contains the following parameters:
     *
     *  * hostName: The name of the host on which the server will run
     *  * port: The port on which the server will run. The port is int32_t to allow 
     *    for compatibility with protobuf and java.
     *  * dbHost: Reference to the database host to which this server will connect.
     */
    explicit Server(const ServerConfig &config);

    virtual ~Server();

    /**
     * Start the RasServer Process.
     */
    virtual void startProcess();
    
    /**
     * Stop the RasServer process.
     */
    virtual void stop(KillLevel level);
    
    /**
     * Register this server and transfer it to the FREE state. This is called
     * when the server is initalized. The serverId must be the same as the one
     * used to initialize the object.
     * @param serverId UUID used to identify the server
     */
    virtual void registerServer(const std::string &serverId);
  
    /**
     * @return True if the server process was started but the server has not registered with RasMgr
     */
    virtual bool isStarting();
    /**
     * @return True if the server does not have any clients assigned, false otherwise.
     */
    virtual bool isFree();
  
    /**
     * @return True if the server has available capacity, false otherwise
     */
    virtual bool isAvailable();

    /**
     * @return TRUE if the server replies to pings, false otherwise.
     */
    virtual bool isAlive();

    /**
     * Check if the client with the given ID is alive.
     * @param clientId UUID of the client.
     * @return true if the client is alive, false otherwise
     * @throws runtime_error if the server cannot be contacted.
     */
    virtual bool isClientAlive(const std::string &clientId);

    /**
     * Allocate the client with the given ID and session ID to the server and respective database.
     * @param clientId UUID of the client
     * @param sessionId UUID of the session.
     * @param dbName name of the database which will be opened.
     * @param dbRights rights the client has on this database
     */
    virtual void allocateClientSession(const std::string &clientId,
                                       const std::string &sessionId,
                                       const std::string &dbName,
                                       const UserDatabaseRights &dbRights);


    /**
     * Remove the client with the given ID and session ID from the server.
     * @param clientId UUID of the client
     * @param sessionId UUID of the session.
     */
    virtual void deallocateClientSession(const std::string &clientId,
                                         const std::string &sessionId);

    /**
     * @return the number of client sessions processed by this server throughout
     * its lifetime. This method is used by the ServerGroup to restart a server
     * once it has reached a number of sessions (to prevent memory leaks from
     * getting out of control)
     */
    virtual std::uint32_t getTotalSessionNo();

    /**
     * @return the port on which the server is running.
     */
    virtual std::int32_t getPort() const;

    /**
     * @return the name of the host on which the server is running
     */
    virtual const std::string &getHostName() const;

    /**
     * @return the UUID of the server
     */
    virtual const std::string &getServerId() const;
    
    virtual bool isRegistered();
    virtual bool isStarted();
    virtual void setStarted(bool value);
  
    /**
     * @return the current client session if one exists as a pair of
     * (clientId, sessionId), or a pair of empty strings otherwise.
     */
    virtual std::pair<std::string, std::string> getCurrentClientSession();

protected:
    /// Only needed for creating a Mock object in the tests.
    Server() = default;

private:

    /**
     * Send a signal sig to this server with PID processId; will log an error
     * in case it fails to send the signal.
     */
    void sendSignal(int sig) const;

    /**
     * The number of microseconds between a SIGTERM signal and a SIGKILL signal sent to the server.
     * This timeout allows the server enough time to cleanup after itself.
    */
    static const std::int32_t SERVER_CLEANUP_TIMEOUT;
    /**
     * Microseconds to wait between checks on whether the server is still alive
     */
    static const std::int32_t SERVER_CHECK_INTERVAL;

    std::string hostName;/*! Hostname of the RasServer process */
    std::int32_t port;/*! Port of the RasServer process */
    std::shared_ptr<DatabaseHost> dbHost;/*! Database host to which this server has access */
    std::string options;

    pid_t processId; /*! Id of the server process*/
    std::string serverId; /*! UUID that uniquely identifies the server */

    boost::shared_mutex stateMutex;
    bool registered;/*! Flag to indicate if the server is starting but has not yet registered */
    std::uint32_t allocatedClientsNo; /*! The number of allocated clients */
    bool started; /*! True after the process is started*/

    std::uint32_t sessionNo;

    std::shared_ptr<::rasnet::service::RasServerService::Stub> service; /*! Service stub used to communicate with the RasServer process */

    boost::shared_mutex sessionListMutex; /*!Mutex used for making the object thread safe */
    std::set<std::pair<std::string, std::string>> sessionList;

    /**
     * @return the number of clients currently allocated to the the RasServer process.
     * @throws common::Exception if the GRPC GetServerStatus fails or timeouts.
     */
    uint32_t getClientQueueSize();

    /**
     * @return the command to start a server process as a list of arguments.
     */
    std::vector<std::string> getStartProcessCommand();

    /**
     * Configure the client context for calls to the server. It's important to
     * do this in order to set a timeout of SERVER_CALL_TIMEOUT ms for calls.
     */
    void configureClientContext(grpc::ClientContext &context);
    
    /**
     * Allocate the client with the given ID and session ID to the server and respective database.
     * @param clientId UUID of the client
     * @param sessionId UUID of the session.
     * @param dbName name of the database which will be opened.
     * @param capabilities access rights capability string
     */
    void allocateClientSession(const std::string &clientId,
                               const std::string &sessionId,
                               const std::string &dbName,
                               const std::string &capabilities);


    std::string getCapability(const char *serverName, const char *databaseName,
                              const UserDatabaseRights &rights);

    std::string convertDatabRights(const UserDatabaseRights &dbRights);
    
    /**
     * Wait until the server is not alive anymore or a timeout is reached.
     * @return true if the server is still alive, false otherwise.
     */
    bool waitUntilServerExits();
};

}

#endif /* RASMGR_X_SRC_RASSERVER_HH_ */
