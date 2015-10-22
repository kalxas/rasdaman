/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#ifndef RASSERVER_CLIENTMANAGER_HH
#define RASSERVER_CLIENTMANAGER_HH

#include <map>
#include <string>

#include <boost/scoped_ptr.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>

#include "../../common/src/time/timer.hh"

namespace rasserver
{

class ClientManager
{
public:
    ClientManager();

    virtual ~ClientManager();

    /**
     * @brief allocateClient Allocate a slot for the client with the given clientUUID and sessionId
     * @param clientUUID
     * @param sessionId
     * @return TRUE if the allocation was successful, FALSE otherwise.
     */
    bool allocateClient(std::string clientUUID, std::string sessionId);

    /**
     * @brief deallocateClient Remove the client with the given id from the list.
     * @param clientUUID
     * @param sessionId
     */
    void deallocateClient(std::string clientUUID, std::string sessionId);

    /**
     * @brief isAlive Check if the client with the given ID is alive.
     * @param clientUUID
     * @return TRUE if the client id alive, false otherwise
     */
    bool isAlive(std::string clientUUID);

    /**
     * @brief resetLiveliness Reset the lifetime of a client.
     * @param clientUUID
     */
    void resetLiveliness(std::string clientUUID);

    /**
     * @brief getClientQueueSize
     * @return The number of alive clients.
     */
    size_t getClientQueueSize();

private:
    static const int ALIVE_PERIOD = 3000; /* milliseconds */

    boost::scoped_ptr<boost::thread> managementThread;

    boost::shared_mutex clientMutex;/*! Mutex used to synchronize access to the clientList */
    std::map<std::string, common::Timer> clientList;/*! Map between a clientId and a Timer that counts down from the last ping*/

    boost::mutex threadMutex;/*! Mutex used to safely stop the worker thread */
    bool isThreadRunning; /*! Flag used to stop the worker thread */
    boost::condition_variable isThreadRunningCondition; /*! Condition variable used to stop the worker thread */

    /**
     * @brief evaluateClientStatus Evaluate the list of clients and remove the ones who's
     * lifetime has expired.
     * This method will be ran in another thread.
     */
    void evaluateClientStatus();
};

}

#endif // CLIENTMANAGER_HH
