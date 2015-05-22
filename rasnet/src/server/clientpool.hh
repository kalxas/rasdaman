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

#ifndef RASNET_SRC_SERVER_CLIENTPOOL_HH_
#define RASNET_SRC_SERVER_CLIENTPOOL_HH_

#include <string>
#include <map>

#include <boost/cstdint.hpp>
#include <boost/thread.hpp>

#include "../../../common/src/zeromq/zmq.hh"
#include "../common/peerstatus.hh"

namespace rasnet
{
/**
 * @brief The ClientPool class Keeps track of the status of a list of clients.
 */
class ClientPool
{
public:
    static const boost::int32_t DEFAULT_MINIMUM_POLL_PERIOD;

    ClientPool();

    virtual ~ClientPool();

    /**
     * Add the client with the given ID and connection parameters to the pool.
     * @param clientId The ID uniquely represents the client with respect to the
     * server socket through which it sent the message.
     * @param period The number of milliseconds after which the client's liveliness decreases
     * @param retries The number of times the client's liveliness can decrease before declaring it dead
     * and removing it from the pool.
     */
    virtual void addClient(std::string clientId, boost::int32_t period, boost::int32_t retries);

    /**
     * @brief getMinimumPollPeriod Get the minimum number of milliseconds
     * between each check of the liveliness of the clients that would allow
     * for the status of each client to be up to date.
     * @return The minimum value of the lifetimes of all the clients in the pool.
     * If no client is present in the pool, -1 is returned
     */
    virtual boost::int32_t getMinimumPollPeriod() const;

    /**
     * Reset the status of the client with the given id. This method should be used
     * when the client has sent a message to the server
     * @param clientId The ID uniquely represents the client with respect to the
     * server socket through which it sent the message.
     */
    virtual void resetClientStatus(std::string clientId);

    /**
     * Send a ping to all the registered clients that have not had any activity.
     * @param socket Socket through which to send the message
     */
    virtual void pingAllClients(zmq::socket_t& socket);

    /**
     * Remove all the clients who are declared dead from the pool
     */
    virtual void removeDeadClients();

    /**
     * Check if the client with the given ID is alive
     * @param clientId
     * @return TRUE if the client is alive, FALSE otherwise
     */
    virtual bool isClientAlive(std::string clientId);

    /**
     * Remove all the clients in the pool.
     */
    virtual void removeAllClients();
private:
    std::map<std::string, PeerStatus> clients; /*!< Map between the client's ids and their peer status */

    std::map<boost::int32_t, boost::int32_t> periods; /*< Map between period values and the number of clients with that period */
};

} /* namespace rasnet */

#endif /* RASNET_SRC_SERVER_CLIENTPOOL_HH_ */
