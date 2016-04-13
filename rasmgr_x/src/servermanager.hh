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

#include <map>
#include <list>
#include <string>

#include <boost/cstdint.hpp>
#include <boost/thread.hpp>
#include <boost/thread/mutex.hpp>
#include <boost/smart_ptr.hpp>

#include "servermanagerconfig.hh"

#include "messages/rasmgrmess.pb.h"

namespace rasmgr
{

class Server;
class ServerGroup;
class ServerGroupFactory;

/**
 * @brief The ServerManager class Responsible for the management of server groups (creation, destruction, change),
 * and registering new server processes with the parent group.
 * The ServerManager has a thread that performs management tasks on the owned servergroups
 * at a certain interval.
 */
class ServerManager
{
public:
    /**
      * @brief ServerManager ServerManager Initialize a new instance of the ServerManager class.
      */
    ServerManager (const ServerManagerConfig& config, boost::shared_ptr<ServerGroupFactory> serverGroupFactory );

    virtual ~ServerManager();

    /**
     * Method used to retrieve a free server. This method is NOT THREAD SAFE.
     */
    virtual bool tryGetFreeServer(const std::string& databaseName, boost::shared_ptr<Server>& out_server );

    /**
     * Registers a rasserver when the server starts and becomes available.
     * @param serverId - Server id of the server which became available.
     */
    virtual void registerServer ( const std::string& serverId );

    /**
     * @brief defineServerGroup Define a server group that will be used to
     * automatically spawn servers.
     * @param serverGroupConfig Configuration used to initialize the server group
     */
    virtual void defineServerGroup ( const ServerGroupConfigProto& serverGroupConfig );

    /**
     * @brief changeServerGroup Change the configuration of the server group with the given name.
     * This method will succeed only if the server group does not have any running servers.
     * @param oldServerGroupName The old name of the server group
     * @param newServerGroupConfig The new configuration that will be used by the server group
     */
    virtual void changeServerGroup ( const std::string& oldServerGroupName, const ServerGroupConfigProto& newServerGroupConfig );

    /**
     * @brief removeServerGroup Remove a server group if it doesn;t have any running servers
     * @param serverGroupName
     */
    virtual void removeServerGroup ( const std::string& serverGroupName );

    /**
     * @brief startServerGroup
     * @param startGroup
     */
    virtual void startServerGroup ( const StartServerGroup& startGroup );

    /**
     * @brief stopServerGroup Mark the server group as stopped.
     * The server manager will not be able to spawn new servers
     * and running servers will be removed once they finish already running transactions.
     * @param serverGroupName
     */
    virtual void stopServerGroup ( const StopServerGroup& stopGroup );

    /**
     * @brief hasRunningServers Check if there are running server groups
     * @return
     */
    virtual bool hasRunningServers();

    /**
     * @brief serializeToProto Serialize the data contained by this object
     * into a format which can be later used for presenting information to the user
     * @return
     */
    virtual ServerMgrProto serializeToProto();

private:
    std::list<boost::shared_ptr<ServerGroup> > serverGroupList;/*!< Server group list */

    boost::shared_mutex serverGroupMutex;/*!< Mutex used to synchronize access to the list of server groups */

    boost::scoped_ptr<boost::thread> workerCleanup; /*!< Thread object running the @see workerCleanupRunner() function. */

    boost::shared_ptr<ServerGroupFactory> serverGroupFactory;

    ServerManagerConfig config;

    bool isWorkerThreadRunning; /*! Flag used to stop the worker thread */
    boost::mutex threadMutex;/*! Mutex used to safely stop the worker thread */
    boost::condition_variable isThreadRunningCondition; /*! Condition variable used to stop the worker thread */

    /**
     * Function which cleans the servers which failed to start or were stopped.
     */
    void workerCleanupRunner();

    void evaluateServerGroups();
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_SERVERMANAGER_HH_ */
