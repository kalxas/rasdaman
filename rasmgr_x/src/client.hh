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

#include "common/src/time/timer.hh"

namespace rasmgr
{

class User;
class Server;

/**
 * @brief The Client class Represents a client that connects to rasmgr and requests
 * a server onto which to execute a query.
 * Stores information about the DB sessions allocated to the client
 */
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
     * @brief getUser Get the user information(user name, password, access rights) associated with this client.
     * The returned value is a constant to prevent accidental modification of the user information.
     * User data should be modified only through the UserManager.
     * @return
     */
    const boost::shared_ptr<const User> getUser() const;

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
     * @throws An exception is thrown if the user does not have rights on the database with the given name
     * or if the server cannot allocate a client session.
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

    /**
     * @brief isClientAliveOnServers Go through the list of servers and check if the client
     * is alive.
     * @return TRUE if at least one server reports client activity
     */
    bool isClientAliveOnServers();

    /**
     * @brief removeDeadServers Remove weak_ptr<Server> that are null from the least of
     * servers assigned to this client.
     * @return
     */
    void removeDeadServers();

};

} /* namespace rnp */

#endif /* RASMGR_X_SRC_CLIENT_HH_ */
