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

#ifndef RASMGR_X_SRC_SERVERMANAGER_HH_
#define RASMGR_X_SRC_SERVERMANAGER_HH_

#include "servermanagerconfig.hh"
#include "rasmgr/src/messages/rasmgrmess.pb.h"

#include <map>
#include <list>
#include <string>
#include <thread>
#include <cstdint>
#include <condition_variable>
#include <boost/thread/shared_mutex.hpp>

namespace rasmgr
{

class Server;
class ServerGroup;
class ServerGroupFactory;

/**
  Responsible for the management of server groups (creation, destruction,
  change), and registering new server processes with the parent group. The
  ServerManager has a thread that performs management tasks on the owned
  server groups at a certain interval. Functionality provided includes:
  
  - Try to get a server with available capacity for clients. Called by
    the :ref:`ClientManager` when a new client connects intending to send a query
    for evaluation. Internally the method goes throught the list of server groups
    and returns the first available server. If no such server is found, false is
    returned, otherwise true.
  
  - Register a rasserver in the correct server group that started it. The method
    is called when the rasserver sends a call to rasmgr to register itself.
  
  - Server group management functions, corresponding to `define srv`, `change srv`
    and `remove srv` rascontrol commands:
  
    - Define a new server group to be added to the list of server groups.
    - Change an existing group with a new configuration.
    - Remove an existing server group
  
  - Start a server group, corresponding to `up srv` rascontrol command
  
  - Stop a server group, corresponding to `down srv` rascontrol command
  
  - Restart all server groups, used from the :ref:`UdfMonitor` when a UDF library
    is updated
  
  - Check if there are any running servers, used from :ref:`RasControl` when
    rasmgr is stopped to make sure that all servers have been stopped beforehand
  
  - Server cleanup thread runs every 3 seconds to evaluate the server groups
    (`evaluateServerGroup()` in each :ref:`ServerGroup`) for dead servers and
    servers that may need to be restarted if they've had too many sessions.
 */
class ServerManager
{
public:
    /**
     * Create a server manager with the given configuration and factory for
     * creating server groups.
     */
    ServerManager(const ServerManagerConfig &config,
                  std::shared_ptr<ServerGroupFactory> serverGroupFactory);

    virtual ~ServerManager();

    /**
     * Method used to retrieve a free server. Called by the ClientManager when
     * a new client connects intending to send a query for evaluation.
     * Internally the method goes throught the list of server groups and
     * returns true while setting out_server to the the first free server. If
     * no free server is found, false is returned.
     * 
     * This method is NOT THREAD SAFE.
     */
    virtual bool tryGetAvailableServer(const std::string &databaseName,
                                       std::shared_ptr<Server> &out_server);

    /**
     * Registers a rasserver when the server starts and becomes available.
     * @param serverId - Server id of the server which became available.
     */
    virtual void registerServer(const std::string &serverId);

    /**
     * Define a server group that will be used to automatically spawn servers.
     * @param serverGroupConfig Configuration used to initialize the server group
     */
    virtual void defineServerGroup(const ServerGroupConfigProto &serverGroupConfig);

    /**
     * Change the configuration of the server group with the given name. This
     * method will succeed only if the server group does not have any running
     * servers.
     * 
     * @param oldServerGroupName The old name of the server group
     * @param newServerGroupConfig The new configuration that will be used by
     * the server group
     */
    virtual void changeServerGroup(const std::string &oldServerGroupName,
                                   const ServerGroupConfigProto &newServerGroupConfig);

    /**
     * Remove a server group if it doesn't have any running servers.
     */
    virtual void removeServerGroup(const std::string &serverGroupName);

    /**
     * Start a server group.
     */
    virtual void startServerGroup(const StartServerGroup &startGroup);

    /**
     * Mark the server group as stopped. The server manager will not be able to
     * spawn new servers and running servers will be removed once they finish
     * already running transactions.
     */
    virtual void stopServerGroup(const StopServerGroup &stopGroup);

    /**
     * Restart gracefully all server groups.
     */
    virtual void restartAllServerGroups();

    /**
     * Check if there are running server groups
     */
    virtual bool hasRunningServers();

    /**
     * @return the server with serverId if found, nullptr otherwise.
     */
    virtual std::shared_ptr<Server> getServer(const std::string &serverId);

    /**
     * Serialize the data contained by this object into a format which can be
     * later used for presenting information to the user.
     */
    virtual ServerMgrProto serializeToProto();

private:
    std::list<std::shared_ptr<ServerGroup>> serverGroupList; /*!< Server group list */
    boost::shared_mutex serverGroupMutex;                    /*!< Mutex used to synchronize access to the list of server groups */
    std::shared_ptr<ServerGroupFactory> serverGroupFactory;
    ServerManagerConfig config;

    // -------------------------------------------------------------------------
    // cleanup thread
    std::unique_ptr<std::thread> workerCleanup;       /*!< Thread object running the @see workerCleanupRunner() function. */
    bool isWorkerThreadRunning;                       /*! Flag used to stop the worker thread */
    std::mutex threadMutex;                           /*! Mutex used to safely stop the worker thread */
    std::condition_variable isThreadRunningCondition; /*! Condition variable used to stop the worker thread */

    /// Function which cleans the servers which failed to start or were stopped.
    void workerCleanupRunner();
    /// For each server group evaluate the group's status. This means that dead
    /// server entries will be removed and new servers will be started.
    void evaluateServerGroups();
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // restart servers thread
    std::shared_ptr<std::thread> restartServersThread; /*!< Restarts the servers after 1 second delay upon startup */
    boost::shared_mutex restartServersMutex;           /*!< Mutex used to synchronize access to the restartServersThreads */
    bool isRestartServersThreadRunning;
    /// Wait for 1 second, then restart all server groups. Executed in the
    /// restartServersThread from restartAllServerGroups()
    void restartServersRunner();
    // -------------------------------------------------------------------------
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_SERVERMANAGER_HH_ */
