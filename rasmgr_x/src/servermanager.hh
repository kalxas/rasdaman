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

#include "common/src/zeromq/zmq.hh"

#include "rasserver.hh"
#include "servergroupconfig.hh"
#include "servergroup.hh"
#include "iservercreator.hh"

#include "messages/rasmgrmess.pb.h"

namespace rasmgr
{
class ServerManager
{
public:
    /**
      * @brief ServerManager ServerManager Initialize a new instance of the ServerManager class.
      * @param dbhManager Database host manager used for initializing server groups.
      */
    ServerManager ( boost::shared_ptr<DatabaseHostManager> dbhManager );

    virtual ~ServerManager();

    /**
     * Method used to retrieve a free server. This method is NOT THREAD SAFE.
     * Example usage:
     * lock();
     * srv = getFreeServer(dbName);
     * srv->allocateClient(_,_,_);
     * unlock();
     * @return A shared pointer to a free server object.
     * @throws std::exception if after trying a number of times to obtain a server,
     * no free server can be found.
     */
    boost::shared_ptr<RasServer> getFreeServer ( const std::string& databaseName );

    /**
     * Registers a rasserver when the server starts and becomes available.
     * @param serverId - Server id of the server which became available.
     */
    void registerServer ( const std::string& serverId );

    /**
     * @brief defineServerGroup Define a server group that will be used to
     * automatically spawn servers.
     * @param serverGroupConfig Configuration used to initialize the server group
     */
    void defineServerGroup ( const ServerGroupConfig& serverGroupConfig );

    /**
     * @brief changeServerGroup Change the configuration of the server group with the given name.
     * This method will succeed only if the server group does not have any running servers.
     * @param oldServerGroupName The old name of the server group
     * @param newServerGroupConfig The new configuration that will be used by the server group
     */
    void changeServerGroup ( const std::string& oldServerGroupName, const ServerGroupConfig& newServerGroupConfig );

    /**
     * @brief removeServerGroup Remove a server group if it doesn;t have any running servers
     * @param serverGroupName
     */
    void removeServerGroup ( const std::string& serverGroupName );

    /**
     * @brief getServerGroupConfig Get the current configuration of the group with the given name
     * @param groupName Name of the group for which we are retrieving the configuration
     * @return The configuration of the group
     * @throws std::exception An exception is thrown if there is no server group with that name
     */
    ServerGroupConfig getServerGroupConfig ( const std::string& groupName );


    void startServerGroup ( const StartServerGroup& startGroup );

    /**
     * @brief stopServerGroup Mark the server group as stopped.
     * The server manager will not be able to spawn new servers
     * and running servers will be removed once they finish already running transactions.
     * @param serverGroupName
     */
    void stopServerGroup ( const StopServerGroup& stopGroup );


	/**
	 * @brief Check if there is at least one running server group.
	 *
	 * @return bool
	 */
	bool hasRunningServerGroup();

    //TODO-AT: Maybe,Refactor this so that instead of directly printing a string,
    // the data is serialized and sent to rascontrol and rascontrol decides what to print and how
    /**
     * @brief listServerGroupInfo List information about a given server group
     * @param details TRUE if we should list detailed information about the group
     * @return formatted string representing the information about thiis server group
     * @throws std::exception is thrown if there is no group with the given name
     */
    std::string getServerGroupInfo ( const std::string& serverGroupName, bool details=false );

    /**
     * @brief listServerGroupsInfo List information about all the server groups running on a host.
     * @param host The host on which the server groups are running. If this parameter is not passed in,
     * we list all the server groups.
     * @param details TRUE if we should list detailed information about the group
     * @return formatted string representing the infromation about a set of server groups
     * @throws std::exception if there is no server group running on the host
     */
    std::string getAllServerGroupsInfo ( bool details=false, const std::string& host="" );

private:
    zmq::context_t context; /*!< Context used for inter-thread communication */

    boost::scoped_ptr<zmq::socket_t> controlSocket; /*!< Socket used for sending messages between the main thread and the worker thread*/

    std::string controlEndpoint;/*!< Endpoint used for inter-thread communication */

    boost::shared_ptr<DatabaseHostManager> dbhManager;

    std::list<boost::shared_ptr<ServerGroup> > serverGroupList;/*!< Server group list */

    boost::shared_mutex serverGroupMutex;/*!< Mutex used to synchronize access to the list of server groups */

    boost::scoped_ptr<boost::thread> workerCleanup; /*!< Thread object running the @see workerCleanupRunner() function. */

    boost::shared_ptr<IServerCreator> serverCreator;
    /**
     * Function which cleans the servers which failed to start or were stopped.
     */
    void workerCleanupRunner();

    void evaluateServerGroups();
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_SERVERMANAGER_HH_ */
