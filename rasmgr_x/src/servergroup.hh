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

#include <string>

#include <boost/shared_ptr.hpp>

#include "messages/rasmgrmess.pb.h"

#include "server.hh"

namespace rasmgr
{
/**
 * @brief The ServerGroup class represents a group of servers with the same properties,
 * running on a set of predetermined ports. The same properties apply to all the servers.
 * The Group is responsible for managing its servers and maintaining the number of alive
 * and available servers.
 */
class ServerGroup
{
public:
    virtual ~ServerGroup();

    /**
     * @brief start Mark this server group as active and start the minimum number of
     * servers as specified in the server group config
     */
    virtual void start() = 0;

    /**
     * @brief isStopped Check if the server group has been stopped.
     * @return TRUE if the server group was stopped, FALSE otherwise
     */
    virtual bool isStopped() = 0;

    /**
     * @brief stop Stop the running servers of this group and
     * prevent any other servers from being started.
     * @param force TRUE if the running servers should be shutdown without waiting for running
     * transactions to finish
     */
    virtual void stop(KillLevel level) = 0;

    /**
     * @brief tryRegisterServer Register the server with the given ID as running.
     * @param serverId UUID that uniquely identifies the RasServer instance
     * @return TRUE if there was a server with the given serverId that was
     * starting and was successfully started, FALSE otherwise
     */
    virtual bool tryRegisterServer(const std::string& serverId) = 0;

    /**
     * @brief evaluateServerGroup Evaluate each server in this server group.
     * 1. Remove servers that are not responding to ping requests.
     * 2. Keep the configured minimum number of available servers
     * 3. Keep the configured minimum number of running servers
     * 4. Remove servers if there is extra, unused capacity
     */
    virtual void evaluateServerGroup() = 0;

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
    virtual bool tryGetAvailableServer(const std::string& dbName, boost::shared_ptr<Server>& out_server) = 0;

    /**
     * @brief getConfig Get a copy of the ServerGroupConfig
     * object used to create this ServerGroup.
     * @return
     */
    virtual ServerGroupConfigProto getConfig() const = 0;

    /**
     * @brief setConfig Set a new configuration for
     * this ServerGroup object.
     * @param value
     */
    virtual void changeGroupConfig(const ServerGroupConfigProto &value) = 0;

    /**
     * @brief getGroupName Get the name of this group.
     * @return
     */
    virtual std::string getGroupName() const = 0;

    virtual ServerGroupProto serializeToProto() = 0;
};
}
#endif // SERVERGROUP_HH
