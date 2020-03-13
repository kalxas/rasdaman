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

#include "clientquerystreamedresult.hh"
#include "common/time/timer.hh"

#include <map>
#include <string>
#include <memory>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <boost/thread/shared_mutex.hpp>

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

    /**
     * @brief cleanQueryStreamedResult Removes from the pool a result which must be streamed.
     * @param requestUUID An unique identifier for the streamed result.
     */
    void cleanQueryStreamedResult(const std::string& requestUUID);
    
    /**
     * @brief removeAllQueryStreamedResults Remove all results that must be streamed.
     * Meant to be called from commit/abort transaction, as in certain cases the
     * client may unexpectedly close the streaming (before all chunks are transferred)
     * and lead to a leak.
     */
    void removeAllQueryStreamedResults();

    /**
     * @brief getQueryStreamedResult Retrieves the streamed result based on the request unique identifier.
     * @param requestUUID An unique identifier for the streamed result.
     * @return A ClientQueryStreamedResult providing methods for getting the next chunk of data to be streamed.
     */
    std::shared_ptr<ClientQueryStreamedResult> getQueryStreamedResult(const std::string& requestUUID);

    /**
     * @brief addQueryStreamedResult Saves a resul which will be streamed to the client.
     * @param requestUUID An unique identifier for the streamed result.
     * @param streamedResult The result which will be streamed.
     */
    void addQueryStreamedResult(const std::string& requestUUID, const std::shared_ptr<ClientQueryStreamedResult>& streamedResult);


private:
    /**
    * The maximum number of milliseconds between two consecutive KeepAlive messages
    * from the client. If a KeepAlive message is not received in this amount of time,
    * the client is removed from the server.
    */
    static const int ALIVE_PERIOD; /* milliseconds */

    std::unique_ptr<std::thread> managementThread;

    boost::shared_mutex clientMutex;/*! Mutex used to synchronize access to the clientList */
    std::map<std::string, common::Timer> clientList;/*! Map between a clientId and a Timer that counts down from the last ping*/
    std::map<std::string, std::shared_ptr<ClientQueryStreamedResult>> queryStreamedResultList; /*! Map between request id and the request result.*/

    std::mutex threadMutex;/*! Mutex used to safely stop the worker thread */
    bool isThreadRunning; /*! Flag used to stop the worker thread */
    std::condition_variable isThreadRunningCondition; /*! Condition variable used to stop the worker thread */

    /**
     * @brief evaluateClientStatus Evaluate the list of clients and remove the ones who's
     * lifetime has expired.
     * This method will be ran in another thread.
     */
    void evaluateClientStatus();
};

}

#endif // CLIENTMANAGER_HH
