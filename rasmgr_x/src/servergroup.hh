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

#include <boost/cstdint.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>

#include "../../common/src/time/timer.hh"

#include "servergroupconfig.hh"
#include "databasehostmanager.hh"
#include "databasehost.hh"
#include "serverfactory.hh"

namespace rasmgr
{
class ServerGroup
{
public:
    /**
      * @brief ServerGroup Initialize a new instance of the ServerGroup class.
      * @param config ServerGroupConfig used to initialize this group.
      * @param dbhManager Database Host Manager used to retrieve the database host
      * used by servers of this server group.
      */
    ServerGroup(const ServerGroupConfig& config, boost::shared_ptr<DatabaseHostManager> dbhManager, boost::shared_ptr<ServerFactory> serverFactory);

    virtual ~ServerGroup();

    /**
     * @brief isBusy Check if this server group has running servers.
     * @return TRUE if there are servers running, FALSE otherwise
     */
    bool isBusy();

    /**
     * @brief start Mark this server group as active and start the minimum number of alive
     * servers as specified in the server group config
     */
    void start();

    /**
     * @brief isStopped Check if the server group has been stopped.
     * @return TRUE if the server group was stopped, FALSE otherwise
     */
    bool isStopped();

    /**
     * @brief stop Stop the running servers of this group and
     * prevent any other servers from being started.
     * @param force TRUE if the running servers should be shutdown without waiting for running
     * transactions to finish
     */
    void stop(bool force=false);

    /**
     * @brief registerServer Register the server with the given ID as running.
     * @param serverId UUID that uniquely identifies the RasServer instance
     * @return TRUE if there was a server with the given serverId that was
     * starting and was successfully started, FALSE otherwise
     */
    bool registerServer(const std::string& serverId);

    /**
     * @brief evaluateServerGroup Evaluate each server in this server group.
     * 1.Remove servers that are not responding to ping requests.
     * 2. Keep the configured minimum number of available servers
     * 3. Keep the configured minimum number of running servers
     * 4. Remove servers if there is extra, unused capacity
     */
    void evaluateServerGroup();

    /**
     * @brief getAvailableServer If this server group has a server containing the
     * database given by dbName which has capacity for at least one more client,
     * assign a reference to RasServer to out_server. If the method returns TRUE,
     * out_server will contain a reference to the server, otherwise, the contents of
     * out_server are unknown.
     * @param dbName Name of the database that the client needs to run queries on
     * @param out_server shared_ptr to the RasServer instance
     * @return TRUE if there is a free server, false otherwise.
     */
    bool getAvailableServer(const std::string& dbName, boost::shared_ptr<Server>& out_server);

    /**
     * @brief getConfig Get a copy of the ServerGroupConfig
     * object used to create this ServerGroup.
     * @return
     */
    ServerGroupConfig getConfig() const;

    /**
     * @brief setConfig Set a new configuration for
     * this ServerGroup object.
     * @param value
     */
    void setConfig(const ServerGroupConfig &value);

    /**
     * @brief getGroupName Get the name of this group.
     * @return
     */
    std::string getGroupName() const;

private:
    ServerGroupConfig config;

    std::list<boost::shared_ptr<Server> > runningServers;

    boost::shared_ptr<ServerFactory> serverFactory;

    boost::shared_ptr<DatabaseHost> databaseHost;

    std::map<std::string, std::pair< boost::shared_ptr<Server>, common::Timer > > startingServers;

    boost::shared_mutex groupMutex;

    bool stopped;

    std::set<boost::int32_t> availablePorts;

    void evaluateGroup();

    /**
     * @brief cleanupServerList Remove dead servers and return the configuration
     * data to the pool.
     */
    void removeDeadServers();

    /**
     * @brief startServer Start a new server if there are any servers
     * that has not already been started.
     * @return Reference to the RasServer object that represents the
     * RasServer process.
     */
    void startServer();
};
}
#endif // SERVERGROUP_HH
