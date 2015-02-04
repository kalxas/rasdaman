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

#ifndef RASMGR_X_SRC_CLIENT_HH_
#define RASMGR_X_SRC_CLIENT_HH_

#include <string>

#include <boost/smart_ptr.hpp>
#include <boost/thread.hpp>

#include "../../common/src/time/timer.hh"

#include "server.hh"
#include "user.hh"
#include "userdatabaserights.hh"

namespace rasmgr
{

class Client
{

public:
    /**
     * Initialize a new instance of the Client class.
     * @param clientId UUID assigned to the client by the client manager.
     * @param accessRights access rights this client has on the database
     * @param lifeTime The number of milliseconds for how long the client is alive between pings.
     */
    Client(const std::string& clientId, boost::shared_ptr<User> user, boost::int32_t lifeTime);

    /**
     *
     * @return The unique ID associated with this client
     */
    const std::string& getClientId() const;

    /**
     * Check if the client is alive.
     * @return TRUE if the client is alive, false otherwise.
     */
    bool isAlive();

    /**
     * Reset the client's internal timer and prelong its life
     */
    void resetLiveliness();

    /**
     * Add a DB session to this client.
     * @param dbName Name of the database for this session
     * @param assignedServer Server assigned to the client for this session
     * @param out_sessionId Unique ID that is created for this session.
     */
    void addDbSession(const std::string& dbName, boost::shared_ptr<Server> assignedServer, std::string& out_sessionId);

    /**
     * Remove the session with the given ID from the client's memory.
     * @param sessionId UUID that uniquely identifies the session on this client.
     */
    void removeDbSession(const std::string& sessionId);

    /**
     * Remove the client from all the servers it has been associated with.
     */
    void removeClientFromServers();

private:
    std::string clientId; /*! Unique client id.*/
    boost::shared_ptr<User> user; /*! User represented by this client. */

    boost::shared_mutex timerMutex; /*! Mutex used to synchronize access to the timer */
    common::Timer timer;/*! Timer for keeping track of the life of the client */

    std::map<std::string, boost::weak_ptr<Server> > assignedServers; /*! Map between sessionIds and the server assigned for the session*/
    boost::shared_mutex assignedServersMutex; /*! Mutex used to synchronize access to the list of servers*/

    bool isClientAliveOnServers();

    bool removeDeadServers();

};

} /* namespace rnp */

#endif /* RASMGR_X_SRC_CLIENT_HH_ */
