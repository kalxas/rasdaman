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

#include "common/time/timer.hh"
#include <boost/thread.hpp>
#include <string>
#include <memory>
#include <atomic>

namespace rasmgr
{

class User;
class Server;
class CpuScheduler;

/**
 * Represents a client that connects to rasmgr and requests a server onto which to
 * execute a query. The client has the following properties:
 * 
 * - unique client ID in UUID form assigned to the client by the ClientManager
 * - the User that initiated the client request
 * - the rasmgr hostname to which the client connected to
 * - a liveness timer which expires after a certain amount of time (meaning that
 *   the client will be considered dead), unless it's reset beforehand with a call
 *   to `resetLiveliness()`
 * - a database session ID and a server assigned for that session by the
 *   ClientManager with a call to `addDbSession(...)`; if the session ID is
 *   empty, no server has been assigned.
 * 
 * The ClientManager can request the client to remove itself from the
 * servers it's assigned to with a call to `removeClientFromServers()`, when the
 * client sends a disconnect request to rasmgr.
 */
class Client
{
public:
    /**
     * Initialize a new instance of the Client class.
     * @param clientId UUID assigned to the client by the client manager.
     * @param user user object with access rights this client has on the database
     * @param lifeTime The number of milliseconds for how long the client is alive between pings.
     * @param rasmgrHostArg rasmgr hostname
     * @param cpuSchedulerArg the CPU cores scheduler
     */
    Client(std::uint32_t clientId, std::shared_ptr<User> user,
           std::int32_t lifeTime, const std::string &rasmgrHostArg,
           const std::shared_ptr<CpuScheduler> &cpuSchedulerArg = nullptr);
    
    ~Client();

    /**
     *
     * @return The unique ID associated with this client
     */
    std::uint32_t getClientId() const;

    /**
     * Get the user information(user name, password, access rights) associated with this client.
     * The returned value is a constant to prevent accidental modification of the user information.
     * User data should be modified only through the UserManager.
     */
    const std::shared_ptr<const User> getUser() const;
    
    /**
     * @return the rasmgr hostname to which this client originally connected to.
     */
    const std::string &getRasmgrHost() const;

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
     * @param newServer Server assigned to the client for this session
     * @param out_sessionId Unique ID that is created for this session.
     * @throws An exception is thrown if the user does not have rights on the
     * database with the given name or if the server cannot allocate a client session.
     */
    void addDbSession(const std::string &dbName, std::shared_ptr<Server> newServer,
                      std::uint32_t &out_sessionId);

    /**
     * Remove the session with the given ID from the client's memory.
     * @param sessionId UUID that uniquely identifies the session on this client.
     */
    void removeDbSession(std::uint32_t sessionId);

    /**
     * Remove the client from all the servers it has been associated with.
     */
    void removeClientFromServer();

private:
    std::uint32_t clientId; /*! Unique client id.*/
    std::shared_ptr<User> user; /*! User represented by this client. */
    common::Timer timer;/*! Timer for keeping track of the life of the client */
    std::string rasmgrHost; /*! Hostname to which the client originally connected to. */

    boost::shared_mutex timerMutex; /*! Mutex used to synchronize access to the timer */

    static std::atomic<std::uint32_t> sessionIdCounter;
    std::uint32_t sessionId; /*! unique client session ID */
    bool sessionOpen{false}; /*! true if a session is currently open */
    std::shared_ptr<Server> assignedServer; /*! the server assigned for the session*/
    boost::shared_mutex assignedServerMutex; /*! Mutex used to synchronize access to the server*/
    
    std::shared_ptr<CpuScheduler> cpuScheduler;

    /**
     * @brief isClientAliveOnServers Go through the list of servers and check if the client
     * is alive.
     * @return TRUE if at least one server reports client activity
     */
    bool isClientAliveOnServer();

    /**
     * @brief removeDeadServers Remove dead assignedServer
     */
    void removeDeadServer();

};

} /* namespace rnp */

#endif /* RASMGR_X_SRC_CLIENT_HH_ */
