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

#ifndef RASMGR_X_SRC_SERVERGROUP_HH
#define RASMGR_X_SRC_SERVERGROUP_HH

#include "common/time/timer.hh"
#include "rasmgr/src/messages/rasmgrmess.pb.h"
#include <string>
#include <memory>
#include <cstdint>
#include <list>
#include <boost/thread/shared_mutex.hpp>


namespace rasmgr
{

class DatabaseHost;
class DatabaseHostManager;
class Server;
class ServerFactory;

enum class ServerStatus {
    INITIALIZING,
    RUNNING,
    STOPPING,
    STOPPED,
    STARTING,
    RESTARTING
};

/**
  Represents a group of servers with the same properties, running on a set of
  predetermined ports. The same properties apply to all the servers. The Group
  is responsible for managing its servers and maintaining the number of alive
  and available servers. The set of operations is similar to :ref:`Server`:
  
  - start a server group
  
    - if STOPPED -> start process, change to STARTING state
    - else, do nothing
  
  - stop a server group
  
    - if STOPPED -> throw error
    - if RUNNING -> send stop request to the server, change to STOPPING state
    - if STARTING -> kill the server, change to STOPPED state
    - evaluate the server group
  
  - restart a server group
  
    - if RUNNING -> stop gracefully, change to RESTARTING, evaluate server group
  
  - evaluate the server group to remove those not responding to pings
  
    - if STOPPED -> nothing is done
  
    - remove dead server:
  
      - if RUNNING and fails on ping -> KILL process, change to RESTARTING
      - if STARTING and timed out -> KILL process, change to RESTARTING
      - if STOPPING and not alive -> change to RESTARTING
      - if STOPPING and timed out -> KILL process, change to RESTARTING
  
    - evaluate servers to restart:
  
      - if total session no > config countdown -> change to RESTARTING
      - if running server and RESTARTING and server is free -> KILL process
  
    - if RESTARTING and no running server -> start a new server
  
  - check if the server group has been stopped
  
  - register a server that was previously started so it becomes available
  
    - if STOPPED -> throw error
    - if STARTING -> register server, change to RUNNING
    - if register server failed for more than 3 times in a row -> kill process
  
  - get an available server to be assigned to a client
  
    - if RUNNING and the server is available it is returned and true, otherwise false
 */
class ServerGroup
{
public:
    /**
      * Initialize a new instance of the ServerGroup class.
      * @param config ServerGroupConfigProto used to initialize this group.
      * Default values will be set and a validation will be performed on the configuration
      * @param dbhManager Database Host Manager used to retrieve the database host
      * used by servers of this server group.
      */
    ServerGroup(const ServerGroupConfigProto &config,
                std::shared_ptr<DatabaseHostManager> dbhManager,
                std::shared_ptr<ServerFactory> serverFactory);

    virtual ~ServerGroup();
    
    /**
     * Mark this server group as active and start the minimum number of servers
     * as specified in the server group config
     */
    virtual void start();
    
    /**
     * Stop the running servers of this group and prevent any other servers
     * from being started.
     */
    virtual void stop(KillLevel level);
    
    /**
     * Schedule the servers of this group for restart when they become free.
     */
    virtual void scheduleForRestart();

    /**
     * Check if the server group has been stopped.
     * @return TRUE if the server group was stopped, FALSE otherwise
     */
    virtual bool isStopped();

    /**
     * Register the server with the given ID as running.
     * @param serverId UUID that uniquely identifies the RasServer instance
     * @return TRUE if there was a server with the given serverId that was
     * starting and was successfully started, FALSE otherwise
     */
    virtual bool tryRegisterServer(const std::string &serverId);

    /**
     * Evaluate each server in this server group.
     * 1. Remove servers that are not responding to ping requests.
     * 2. Keep the configured minimum number of available servers
     * 3. Keep the configured minimum number of running servers
     * 4. Remove servers if there is extra, unused capacity
     */
    virtual void evaluateServerGroup();

    /**
     * If this server group has a server containing the database given by
     * dbName which has capacity for at least one more client, assign a
     * reference to RasServer to out_server. If the method returns TRUE,
     * out_server will contain a reference to the server, otherwise, the
     * contents of out_server are unknown.
     * 
     * @param dbName Name of the database that the client needs to run queries on
     * @param out_server shared_ptr to the RasServer instance
     * @return TRUE if there is a free server, false otherwise.
     */
    virtual bool tryGetAvailableServer(const std::string &dbName,
                                       std::shared_ptr<Server> &out_server);
    
    /**
     * @return the server with serverId if exists, nullptr otherwise.
     * Used only from the ServerManagementService to update the server cores.
     */
    virtual std::shared_ptr<Server> getServer(const std::string &serverId);
    
    /**
     * @return a copy of the ServerGroupConfig object used to create this
     * ServerGroup.
     */
    virtual ServerGroupConfigProto getConfig() const;

    /**
     * Set a new configuration for this ServerGroup object.
     */
    virtual void changeGroupConfig(const ServerGroupConfigProto &value);

    /**
     * @return the name of this group.
     */
    virtual std::string getGroupName() const;

    virtual ServerGroupProto serializeToProto();

protected:
    /// Only needed for creating a Mock object in the tests.
    ServerGroup() = default;
    
private:
    ServerGroupConfigProto config;/*!< Configuration for this group */

    std::shared_ptr<DatabaseHostManager> dbhManager; /*!< Reference to the DBH manager for retrieving dbh */
    std::shared_ptr<ServerFactory> serverFactory;

    std::shared_ptr<Server> runningServer;    /*!< Server that is currently running */
    std::shared_ptr<common::Timer> startingServerTimer;
    std::shared_ptr<common::Timer> stoppingServerTimer;

    std::shared_ptr<DatabaseHost> databaseHost;

    boost::shared_mutex groupMutex;

    ServerStatus serverStatus{ServerStatus::INITIALIZING};

    int failedRegistrations{};/*!< number of consecutive times a server failed to register */
    
    bool scheduledForRestart{false};

    /**
     * @return TRUE if there is a server with capacity for one more client, FALSE otherwise
     */
    bool hasAvailableServer();
    
    /**
     * Restart dead servers
    */
    void evaluateGroup();

    /**
     * Check if any servers need to be restarted.
     */
    void evaluateServersToRestart();

    /**
     * Remove dead servers and return the configuration data to the pool.
     */
    void removeDeadServers();

    /**
     * Start a new server if there are any servers that has not already been started.
     */
    void startServer();

    void stopActiveServer(KillLevel level);
    
    void restartServer();

    void validateAndInitConfig(ServerGroupConfigProto &config);
    
    /**
     * Return the server port configured for this server group in rasmgr.conf
     */
    std::int32_t getConfiguredPort() const;
};
}
#endif // SERVERGROUP_HH
