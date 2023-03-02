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

/**
 * @brief Manage clients of a rasserver. Only one client at a time is supported.
 */
class ClientManager
{
public:
    ClientManager();

    virtual ~ClientManager();

    /**
     * Allocate a slot for the client with the given clientUUID and sessionId
     * @return TRUE if the allocation was successful, FALSE otherwise.
     */
    bool allocateClient(std::uint32_t clientUUID, std::uint32_t sessionId);

    /**
     * Remove the client with the given id from the list.
     */
    void deallocateClient(std::uint32_t clientUUID, std::uint32_t sessionId);

    /**
     * Check if the client with the given ID is alive.
     * @return TRUE if the client id alive, false otherwise
     */
    bool isAlive(std::uint32_t clientUUID);

    /**
     * Reset the lifetime of a client.
     */
    void resetLiveliness(std::uint32_t clientUUID);

    /**
     * @return The number of alive clients.
     */
    bool hasClients();

    /**
     * Removes from the pool a result which must be streamed.
     * @param requestUUID An unique identifier for the streamed result.
     */
    void cleanQueryStreamedResult(std::uint32_t requestUUID);

    /**
     * @brief removeAllQueryStreamedResults Remove all results that must be streamed.
     * Meant to be called from commit/abort transaction, as in certain cases the
     * client may unexpectedly close the streaming (before all chunks are transferred)
     * and lead to a leak.
     */
    void removeAllQueryStreamedResults();

    /**
     * Retrieves the streamed result based on the request unique identifier.
     * @param requestUUID An unique identifier for the streamed result.
     * @return A ClientQueryStreamedResult providing methods for getting the next chunk of data to be streamed.
     */
    std::shared_ptr<ClientQueryStreamedResult> getQueryStreamedResult(std::uint32_t requestUUID);

    /**
     * Saves a resul which will be streamed to the client.
     * @param requestUUID An unique identifier for the streamed result.
     * @param streamedResult The result which will be streamed.
     */
    void addQueryStreamedResult(std::uint32_t requestUUID,
                                const std::shared_ptr<ClientQueryStreamedResult> &streamedResult);

private:
    /**
    * The maximum number of milliseconds between two consecutive KeepAlive messages
    * from the client. If a KeepAlive message is not received in this amount of time,
    * the client is removed from the server.
    */
    static const int CLIENT_ALIVE_PING_TIMEOUT_MS;

    boost::shared_mutex clientMutex;
    common::Timer timeSinceLastPing; /*! a Timer that counts down from the last ping*/
    std::uint32_t clientId;          /*! Current client connected to this server */
    bool clientConnected{false};

    boost::shared_mutex requestMutex;
    std::uint32_t requestId; /*! Request id for the query result */
    bool streamingRequest{false};
    std::shared_ptr<ClientQueryStreamedResult> requestResult;

    std::unique_ptr<std::thread> evaluateClientStatusThread;              /*! Thread running the evaluateClientStatus method */
    std::mutex evaluateClientStatusMutex;                                 /*! Mutex used to safely stop the worker thread */
    bool isEvaluateClientStatusThreadRunning;                             /*! Flag used to stop the worker thread */
    std::condition_variable isEvaluateClientStatusThreadRunningCondition; /*! Condition variable used to stop the worker thread */

    /**
     * Evaluate the list of clients and remove the ones who's lifetime has expired.
     * This method is run in a separate evaluateClientStatusThread thread.
     */
    void evaluateClientStatus();
};

}  // namespace rasserver

#endif  // CLIENTMANAGER_HH
