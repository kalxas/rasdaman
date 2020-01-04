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

#include <unistd.h>

#include <stdexcept>
#include <algorithm>
#include <cstdint>

#include <logging.hh>
#include "common/exceptions/rasexceptions.hh"

#include "constants.hh"
#include "databasehost.hh"
#include "databasehostmanager.hh"
#include "rasmgrconfig.hh"
#include "server.hh"
#include "serverconfig.hh"
#include "serverfactory.hh"
#include "servergroupimpl.hh"

namespace rasmgr
{
using std::map;
using std::list;
using std::string;
using std::pair;
using std::runtime_error;

using boost::unique_lock;
using boost::shared_lock;
using boost::shared_mutex;

using common::Timer;

ServerGroupImpl::ServerGroupImpl(const ServerGroupConfigProto &c,
                                 std::shared_ptr<DatabaseHostManager> m,
                                 std::shared_ptr<ServerFactory> f):
    config(c), dbhManager(m), serverFactory(f)
{
    //Validate the configuration and initialize defaults
    this->validateAndInitConfig(this->config);

    //Make sure to decrease the server count in the destructor
    //or when the database host is changed.
    this->databaseHost = dbhManager->getAndLockDatabaseHost(this->config.db_host());
    this->stopped = true;

    for (int i = 0; i < this->config.ports_size(); ++i)
    {
        this->availablePorts.insert(static_cast<int>(this->config.ports(i)));
    }
}

ServerGroupImpl::~ServerGroupImpl()
{
    try
    {
        this->databaseHost->decreaseServerCount();
        this->stopActiveServers(KILL);
    }
    catch (std::exception &ex)
    {
        LERROR << "ServerGroup destructor failed: " << ex.what();
    }
    catch (...)
    {
        LERROR << "ServerGroup destructor failed for unknown reason";
    }
}

void ServerGroupImpl::start()
{
    /**
      1. Get exclusive access and start the minimum number of alive servers
      */
    boost::unique_lock<shared_mutex> groupLock(this->groupMutex);
    // Start is idempotent
    if (this->stopped)
    {
        this->stopped = false;

        for (std::uint32_t i = 0; i < this->config.min_alive_server_no(); i++)
        {
            this->startServer();
        }
    }
}

bool ServerGroupImpl::isStopped()
{
    unique_lock<shared_mutex> groupLock(this->groupMutex);
    return this->stopped;
}

void ServerGroupImpl::stop(KillLevel level)
{
    unique_lock<shared_mutex> groupLock(this->groupMutex);

    if (this->stopped)
    {
        throw common::InvalidStateException("ServerGroup is already stopped");
    }
    else
    {
        this->stopped = true;
        this->failedRegistrations = 0;

        this->stopActiveServers(level);
        this->evaluateGroup();
    }
}

bool ServerGroupImpl::tryRegisterServer(const std::string &serverId)
{
    /**
      * If the server group was not shutdown, add the server to the list of running
      * servers and remove it from the list of starting servers.
      */
    bool registered = false;

    unique_lock<shared_mutex> groupLock(this->groupMutex);
    LDEBUG << "Try register server " << serverId;

    //If the server group is stopped, we cannot register new servers.
    if (this->stopped)
    {
        throw common::InvalidStateException(
            "Server group is already stopped, no new servers can register.");
    }

    auto it = this->startingServers.find(serverId);
    if (it != this->startingServers.end())
    {
        try
        {
            it->second.first->registerServer(serverId);
            this->runningServers.push_back(it->second.first);
            this->startingServers.erase(it);
            registered = true;
        }
        catch (std::exception &ex)
        {
            LWARNING << "Failed registering server " << serverId << ": " << ex.what();
        }
        catch (...)
        {
            LWARNING << "Failed registering server " << serverId;
        }

        // record failed registrations
        if (registered)
        {
            failedRegistrations = 0; // all good, reset
        }
        else if (static_cast<uint32_t>(failedRegistrations++) >= MAX_GET_SERVER_RETRIES)
        {
            LERROR << "Server registration in group " << getGroupName()
                   << " failed too many times; stopping group.";
            this->stop(KillLevel::KILL);
        }
    }

    return registered;
}

void ServerGroupImpl::evaluateServerGroup()
{
    unique_lock<shared_mutex> groupLock(this->groupMutex);
    this->evaluateGroup();
}

bool ServerGroupImpl::tryGetAvailableServer(const std::string &dbName, std::shared_ptr<Server> &out_server)
{
    unique_lock<shared_mutex> groupLock(this->groupMutex);

    std::shared_ptr<Server> result;
    if (this->databaseHost->ownsDatabase(dbName))
    {
        for (auto it = this->runningServers.begin(); it != this->runningServers.end(); ++it)
        {
            if ((*it)->isAvailable())
            {
                out_server = (*it);
                //Round-robin distribution of load
                this->runningServers.push_back(out_server);
                this->runningServers.erase(it);
                return true;
            }
        }
    }

    return false;
}

ServerGroupConfigProto ServerGroupImpl::getConfig() const
{
    return config;
}

void ServerGroupImpl::changeGroupConfig(const ServerGroupConfigProto &value)
{
    unique_lock<shared_mutex> groupLock(this->groupMutex);

    if (!this->stopped)
    {
        throw common::InvalidStateException(
            "Cannot change ServerGroup configuration while the ServerGroup is running.");
    }

    if (value.has_name())
    {
        this->config.set_name(value.name());
    }

    if (value.has_host())
    {
        this->config.set_host(value.host());
    }

    if (value.has_db_host())
    {
        this->config.set_db_host(value.db_host());
    }

    if (value.ports_size() > 0)
    {
        this->config.clear_ports();

        for (int i = 0; i < value.ports_size(); ++i)
        {
            this->config.add_ports(value.ports(i));
        }
    }

    if (value.has_min_alive_server_no())
    {
        this->config.set_min_alive_server_no(value.min_alive_server_no());
    }

    if (value.has_min_available_server_no())
    {
        this->config.set_min_available_server_no(value.min_available_server_no());
    }

    if (value.has_max_idle_server_no())
    {
        this->config.set_max_idle_server_no(value.max_idle_server_no());
    }

    if (value.has_autorestart())
    {
        this->config.set_autorestart(value.autorestart());
    }

    if (value.has_countdown())
    {
        this->config.set_countdown(value.countdown());
    }

    if (value.has_server_options())
    {
        this->config.set_server_options(value.server_options());
    }

    this->validateAndInitConfig(config);

    if (value.has_db_host())
    {
        //Release the current database host
        this->databaseHost->decreaseServerCount();

        //Retrieve and lock the new database host.
        this->databaseHost = dbhManager->getAndLockDatabaseHost(this->config.db_host());
    }

    if (value.ports_size() > 0)
    {
        this->availablePorts.clear();
        for (int i = 0; i < this->config.ports_size(); ++i)
        {
            this->availablePorts.insert(static_cast<std::int32_t>(this->config.ports(i)));
        }
    }
}


std::string ServerGroupImpl::getGroupName() const
{
    return this->config.name();
}

ServerGroupProto ServerGroupImpl::serializeToProto()
{
    ServerGroupProto result;

    unique_lock<shared_mutex> groupLock(this->groupMutex);

    result.set_name(this->config.name());
    result.set_host(this->config.host());
    result.set_db_host(this->config.db_host());

    for (int i = 0; i < this->config.ports_size(); ++i)
    {
        result.add_ports(static_cast<std::int32_t>(this->config.ports(i)));
    }

    result.set_min_alive_server_no(this->config.min_alive_server_no());
    result.set_min_available_server_no(this->config.min_available_server_no());
    result.set_max_idle_server_no(this->config.max_idle_server_no());
    result.set_autorestart(this->config.autorestart());
    result.set_countdown(this->config.countdown());
    result.set_server_options(this->config.server_options());

    result.set_running(!this->stopped);
    result.set_available(this->hasAvailableServers());

    return result;
}

bool ServerGroupImpl::hasAvailableServers()
{
    bool result = false;

    for (auto runningServer = this->runningServers.begin(); runningServer != this->runningServers.end(); ++runningServer)
    {
        try
        {
            if ((*runningServer)->isAvailable())
            {
                result = true;
                break;
            }
        }
        catch (std::exception &ex)
        {
            LERROR << "Failed to check if running server is available: " << ex.what();
        }
        catch (...)
        {
            LERROR << "Failed to check if running server is available for an unkown reason";
        }
    }

    return result;
}

void ServerGroupImpl::evaluateGroup()
{
    /**
    1. Remove dead servers
    2. Try to keep the minimum number of available servers
    3. Try to keep the minimum number of alive servers
    */
    LTRACE << "Evaluating server group " << getGroupName();
    uint32_t availableServerNo = 0;
    uint32_t idleServerNo = 0;

    this->removeDeadServers();
    this->evaluateRestartingServers();

    if (!this->stopped)
    {
        for (auto it = this->runningServers.begin(); it != this->runningServers.end(); ++it)
        {
            //If the server is free, it is also available
            //If it is not free, there is still a chance that it is available
            //depeding on the capacity
            if ((*it)->isFree())
            {
                idleServerNo++;
                availableServerNo++;
            }
            else if ((*it)->isAvailable())
            {
                availableServerNo++;
            }
        }

        LTRACE << "Total servers in group: " << this->runningServers.size()
               << ", of which free: " << idleServerNo
               << " and available: " << availableServerNo;

        if (availableServerNo < this->config.min_available_server_no())
        {
            uint32_t maxNoServersToStart = this->config.min_available_server_no() - availableServerNo;
            uint32_t notStartedServerNo = uint32_t(this->config.ports_size())
                                          - (this->startingServers.size() + this->runningServers.size() + this->restartingServers.size());

            uint32_t serversToStartNo = std::min(maxNoServersToStart, notStartedServerNo);
            LTRACE << "Server group " << this->getGroupName() << " will start " << serversToStartNo << " new servers";

            for (uint32_t i = 0; i < serversToStartNo; i++)
            {
                this->startServer();
            }
        }
        else if (availableServerNo > this->config.min_available_server_no() && idleServerNo > this->config.max_idle_server_no())
        {
            //Stopping this many servers will bring us to the maximum number of idle servers
            uint32_t maxServersToStop = idleServerNo - this->config.max_idle_server_no();
            //Stopping this many servers will keep the minimum number of available servers
            uint32_t availableServersToStop =  availableServerNo - this->config.min_available_server_no();

            uint32_t serversToStop = std::min(availableServersToStop, maxServersToStop);

            LTRACE << "Server group " << this->getGroupName() << " will stop " << serversToStop << " free servers";

            uint32_t stoppedCount{0};
            for (auto it = this->runningServers.begin(); stoppedCount != serversToStop && it != this->runningServers.end(); ++it)
            {
                if ((*it)->isFree())
                {
                    (*it)->stop(NONE);
                    ++stoppedCount;
                }
            }
            LOG_IF(stoppedCount != serversToStop, TRACE) << "Stopped only " << stoppedCount << " servers";
        }
    }
}

void ServerGroupImpl::evaluateRestartingServers()
{
    LTRACE << "Evaluate which servers have to be restarted in group " << this->getGroupName();

    if (this->config.countdown() > 0)
    {
        //Go through all the running servers, find the ones that have to
        //be restarted, and add them to the list
        auto runningServerIt = this->runningServers.begin();
        while (runningServerIt != this->runningServers.end())
        {
            auto runningServerToEraseIt = runningServerIt;
            ++runningServerIt;

            const auto totalSessionNo = (*runningServerToEraseIt)->getTotalSessionNo();
            if (totalSessionNo >= this->config.countdown())
            {
                LTRACE << "Server " << (*runningServerToEraseIt)->getServerId()
                       << " has had " << totalSessionNo << " sessions; this is "
                       << " greater than the configured session countdown of "
                       << this->config.countdown() << ", so it will be restarted.";
                this->restartingServers.push_back((*runningServerToEraseIt));
                this->runningServers.erase(runningServerToEraseIt);
            }
        }

        //Go through the list of restarting servers
        //Close the ones that have to be restarted and add the port
        //back to the pool of available servers
        auto restartingServerIt = this->restartingServers.begin();
        while (restartingServerIt != this->restartingServers.end())
        {
            auto restartingServerToEraseIt = restartingServerIt;
            ++restartingServerIt;
            try
            {
                if ((*restartingServerToEraseIt)->isFree())
                {
                    LTRACE << "Stopping server with ID " << (*restartingServerToEraseIt)->getServerId()
                           << " because it has to be restarted.";
                    (*restartingServerToEraseIt)->stop(KILL);

                    //add the port back to the pool
                    this->availablePorts.insert((*restartingServerToEraseIt)->getPort());
                    //restart the server
                    this->restartingServers.erase(restartingServerToEraseIt);
                }
            }
            catch (std::exception &ex)
            {
                LWARNING << "Failed to stop server with ID "
                         << (*restartingServerToEraseIt)->getServerId()
                         << ", reason: " << ex.what();
            }
            catch (...)
            {
                LWARNING << "Failed to stop server with ID "
                         << (*restartingServerToEraseIt)->getServerId();
            }
        }
    }
    else
    {
        LTRACE << "The countdown is set to 0. Servers will never be restarted.";
    }

    LTRACE << "Finished evaluating which servers have to be restarted in group " << this->getGroupName();
}

void ServerGroupImpl::removeDeadServers()
{
    LTRACE << "Remove any dead servers in group " << this->getGroupName();

    /**
      1.Remove servers that have failed to register in the allocated time.
      2.Remove servers that are no longer responding to pings
      */
    try
    {
        auto runningIt = this->runningServers.begin();
        while (runningIt != this->runningServers.end())
        {
            auto runningToErase = runningIt;
            ++runningIt;

            bool processIsDead{};
            try
            {
                processIsDead = !(*runningToErase)->isAlive();
            }
            catch (std::exception &ex)
            {
                processIsDead = true;
                LWARNING << "Server " << (*runningToErase)->getServerId()
                         << " is not responding to pings: " << ex.what();
            }
            catch (...)
            {
                processIsDead = true;
                LWARNING << "Server " << (*runningToErase)->getServerId()
                         << " is not responding to pings.";
            }
            if (processIsDead)
            {
                try
                {
                    LTRACE << "Stopping dead server " << (*runningToErase)->getServerId();
                    (*runningToErase)->stop(KILL);
                    this->availablePorts.insert((*runningToErase)->getPort());
                    this->runningServers.erase(runningToErase);
                }
                catch (std::exception &ex)
                {
                    LWARNING << "Failed to stop server " << (*runningToErase)->getServerId()
                             << ": " << ex.what();
                }
                catch (...)
                {
                    LWARNING << "Failed to stop server " << (*runningToErase)->getServerId();
                }
            }
        }

        auto startingIt = this->startingServers.begin();
        while (startingIt != this->startingServers.end())
        {
            auto startingToEraseIt = startingIt;
            ++startingIt;

            const auto &serverId = startingToEraseIt->second.first->getServerId();
            try
            {
                if (startingToEraseIt->second.second.hasExpired())
                {
                    LDEBUG << "Removing server that failed to start: " << serverId;
                    startingToEraseIt->second.first->stop(KILL);
                    this->availablePorts.insert(startingToEraseIt->second.first->getPort());
                    this->startingServers.erase(startingToEraseIt);
                }
            }
            catch (std::exception &ex)
            {
                LTRACE << "Server " << serverId << " is not responding to pings: "
                       << ex.what();
            }
            catch (...)
            {
                LTRACE << "Server " << serverId << " is not responding to pings.";
            }
        }
    }
    catch (std::exception &ex)
    {
        LWARNING << "Failed removing dead servers: " << ex.what();
    }
    catch (...)
    {
        LWARNING << "Failed removing dead servers";
    }

    LTRACE << "Finished removing dead servers in group " << this->getGroupName();
}

void ServerGroupImpl::startServer()
{
    //This should never happen
    if (this->availablePorts.empty())
    {
        throw common::InvalidStateException(
            "The ServerGroup has reached capacity, no more servers can be started.");
    }
    else
    {
        auto it = this->availablePorts.begin();
        int32_t port = *it;
        this->availablePorts.erase(port);

        std::string host = this->config.host();
        ServerConfig serverConfig(host, static_cast<uint32_t>(port), this->databaseHost);
        LTRACE << "Starting server in group " << this->getGroupName() << " on " << host << ":" << port;

        serverConfig.setOptions(this->config.server_options());

        auto server = this->serverFactory->createServer(serverConfig);
        pair<std::shared_ptr<Server>, Timer> startingServerEntry(server, Timer(this->config.starting_server_lifetime()));

        this->startingServers.emplace(server->getServerId(), startingServerEntry);

        server->startProcess();
        LTRACE << "Server in group " << this->getGroupName() << " started.";
    }

}

void ServerGroupImpl::stopActiveServers(KillLevel level)
{
    LTRACE << "Stopping running servers in group " << this->getGroupName();

    //Stop the running servers
    for (auto runningServer = this->runningServers.begin(); runningServer != this->runningServers.end(); ++runningServer)
    {
        try
        {
            (*runningServer)->stop(level);

            if (level == KILL)
            {
                this->availablePorts.insert((*runningServer)->getPort());
            }
        }
        catch (std::exception &ex)
        {
            LERROR << "Failed to stop running server: " << ex.what();
        }
        catch (...)
        {
            LERROR << "Failed to stop running server for an unkown reason";
        }
    }

    //If the servers were killed
    if (level == KILL)
    {
        this->runningServers.clear();
    }

    //The servers that are starting but have not yet registered
    //will be stoped forcibly and the ports they used will be returned
    //to the pool of available ports
    for (auto startingServerEntry = this->startingServers.begin(); startingServerEntry != this->startingServers.end(); ++startingServerEntry)
    {
        try
        {
            startingServerEntry->second.first->stop(KILL);
            this->availablePorts.insert(startingServerEntry->second.first->getPort());
        }
        catch (std::exception &ex)
        {
            LERROR << "Failed to stop starting server: " << ex.what();
        }
        catch (...)
        {
            LERROR << "Failed to stop starting server";
        }
    }

    //The list of starting servers will be cleared.
    this->startingServers.clear();
}

void ServerGroupImpl::validateAndInitConfig(ServerGroupConfigProto &cfg)
{
    if (!cfg.has_name())
        throw common::InvalidArgumentException("Missing configuration parameter: \"name\"");
    if (!cfg.has_host())
        throw common::InvalidArgumentException("Missing configuration parameter: \"host\"");
    if (!cfg.has_db_host())
        throw common::InvalidArgumentException("Missing configuration parameter: \"db_host\"");
    if (!cfg.ports_size())
        throw common::InvalidArgumentException("Missing configuration parameter: \"ports\"");

    //Make the list of ports contain unique entries
    std::set<std::int32_t> ports;
    for (int i = 0; i < cfg.ports_size(); ++i)
        ports.insert(static_cast<std::int32_t>(cfg.ports(i)));
    cfg.clear_ports();
    for (std::set<std::int32_t>::iterator it = ports.begin(); it != ports.end(); ++it)
        cfg.add_ports(static_cast<std::uint32_t>(*it));

    if (!cfg.has_min_alive_server_no())
        cfg.set_min_alive_server_no(MIN_ALIVE_SERVER_NO);
    if (!cfg.has_min_available_server_no())
        cfg.set_min_available_server_no(MIN_AVAILABLE_SERVER_NO);
    if (!cfg.has_max_idle_server_no())
        cfg.set_max_idle_server_no(MAX_IDLE_SERVER_NO);
    if (!cfg.has_autorestart())
        cfg.set_autorestart(AUTORESTART_SERVER);
    if (!cfg.has_countdown())
        cfg.set_countdown(MAX_SERVER_SESSIONS);
    if (!cfg.has_starting_server_lifetime())
        cfg.set_starting_server_lifetime(STARTING_SERVER_LIFETIME);
    if (cfg.min_available_server_no() > cfg.min_alive_server_no())
        throw common::InvalidArgumentException(
                "The minimum number of available servers must be less or equal to the minimum number of alive servers");
    if ((std::uint32_t)cfg.ports_size() < cfg.min_alive_server_no())
        throw common::InvalidArgumentException(
                "The number of allocated ports must be greater than the minimum number of alive servers.");
}

}
