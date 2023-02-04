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

#include "clientmanager.hh"

#include "common/uuid/uuid.hh"
#include "server/rasserver_entry.hh"
#include "common/exceptions/missingresourceexception.hh"
#include <logging.hh>
#include <boost/thread.hpp>

namespace rasserver
{

using std::string;
using std::pair;
using std::make_pair;
using std::map;
using std::set;
using std::unique_lock;
using std::shared_ptr;
using std::thread;
using boost::shared_mutex;
using boost::shared_lock;
using common::Timer;
using common::UUID;

const int ClientManager::CLIENT_ALIVE_PING_TIMEOUT_MS = 30000; // 30 seconds

ClientManager::ClientManager()
  : timeSinceLastPing(CLIENT_ALIVE_PING_TIMEOUT_MS), isEvaluateClientStatusThreadRunning{true}
{
    this->evaluateClientStatusThread.reset(new thread(&ClientManager::evaluateClientStatus, this));
}

ClientManager::~ClientManager()
{
    try
    {
        {
            std::lock_guard<std::mutex> lock(this->evaluateClientStatusMutex);
            this->isEvaluateClientStatusThreadRunning = false;
        }
        this->isEvaluateClientStatusThreadRunningCondition.notify_one(); // notify thread to stop
        this->evaluateClientStatusThread->join(); // wait for the thread to finish
    }
    catch (std::exception& ex)
    {
        LERROR << "Client manager destructor has failed: " << ex.what();
    }
    catch (...)
    {
        LERROR << "Client manager destructor has failed.";
    }
}

bool ClientManager::allocateClient(const std::string & clientUUID,
                                   __attribute__ ((unused)) const std::string &)
{
    boost::lock_guard<boost::shared_mutex> lock(clientMutex);
    if (!clientId.empty())
    {
        LWARNING << "Current client " << clientId << " has not been deallocated"
                 << ", but a new client is to be allocated: " << clientUUID;
    }
    timeSinceLastPing.reset();
    clientId = clientUUID;
    return true;
}

void ClientManager::deallocateClient(const std::string &clientUUID,
                                     __attribute__ ((unused)) const std::string &)
{
    boost::lock_guard<boost::shared_mutex> lock(clientMutex);
    if (clientId == clientUUID)
    {
        clientId = "";
    }
}

bool ClientManager::isAlive(const std::string &clientUUID)
{
    boost::shared_lock<boost::shared_mutex> lock(clientMutex);
    return clientId == clientUUID && !timeSinceLastPing.hasExpired();
}

void ClientManager::resetLiveliness(const std::string &clientUUID)
{
    boost::lock_guard<boost::shared_mutex> lock(clientMutex);
    if (clientId == clientUUID)
    {
        timeSinceLastPing.reset();
    }
}

void ClientManager::addQueryStreamedResult(const std::string& requestUUID,
                                           const shared_ptr<ClientQueryStreamedResult>& streamedResult)
{
    boost::lock_guard<boost::shared_mutex> lock(requestMutex);
    if (!requestId.empty())
    {
        LWARNING << "Previous request " << requestId << " has not been deallocated"
                 << ", but a new one is being added: " << requestUUID;
    }
    requestId = requestUUID;
    requestResult = streamedResult;
}

shared_ptr<ClientQueryStreamedResult>
ClientManager::getQueryStreamedResult(const std::string& requestUUID)
{
    boost::shared_lock<boost::shared_mutex> lock(requestMutex);
    if (requestId != requestUUID)
    {
        throw common::MissingResourceException(
            "No request result found for request uuid " + requestUUID);
    }
    return requestResult;
}

void ClientManager::removeAllQueryStreamedResults()
{
    boost::lock_guard<boost::shared_mutex> lock(requestMutex);
    requestId = "";
    requestResult.reset();
}

void ClientManager::cleanQueryStreamedResult(const std::string& requestUUID)
{
    boost::lock_guard<boost::shared_mutex> lock(requestMutex);
    if (requestId == requestUUID)
    {
        requestId = "";
        requestResult.reset();
    }
}

size_t ClientManager::getClientQueueSize()
{
    boost::shared_lock<boost::shared_mutex> lock(requestMutex);
    return clientId.empty() ? 0 : 1;
}

void ClientManager::evaluateClientStatus()
{
    static const std::chrono::milliseconds timeToSleepFor{CLIENT_ALIVE_PING_TIMEOUT_MS};

    std::unique_lock<std::mutex> threadLock(this->evaluateClientStatusMutex);
    while (this->isEvaluateClientStatusThreadRunning)
    {
        try
        {
            // Wait on the condition variable to be notified from the
            // destructor when it is time to stop the worker thread
            if (this->isEvaluateClientStatusThreadRunningCondition.wait_for(
                  threadLock, timeToSleepFor) == std::cv_status::timeout)
            {
                // will contain the clients from which no keep alive message
                // was received after ALIVE_PERIOD milliseconds
                boost::upgrade_lock<boost::shared_mutex> sharedLock(clientMutex);
                if (!clientId.empty() && timeSinceLastPing.hasExpired())
                {
                    // client Keep Alive timer has expired, so the client is considered dead

                    // clean up
                    RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
                    try {
                        rasServerEntry.abortTA();
                    } catch (...) {
                        LERROR << "Failed aborting transaction.";
                    }
                    try {
                        rasServerEntry.closeDB();
                    } catch (...) {
                        LERROR << "Failed closing database connection.";
                    }
                    try {
                        rasServerEntry.disconnectClient();
                    } catch (...) {
                        LERROR << "Failed disconnecting client.";
                    }
                    
                    // remove client results
                    removeAllQueryStreamedResults();
                    
                    // disconnect client here
                    boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(sharedLock);
                    clientId = "";
                }
            }
        }
        catch (std::exception& ex)
        {
            LERROR << "Evaluating client status failed, reason: " << ex.what();
        }
        catch (...)
        {
            LERROR << "Evaluating client status failed for unknown reason.";
        }
    }
}

}
