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

#include "servergroup.hh"
#include "constants.hh"
#include "databasehost.hh"
#include "databasehostmanager.hh"
#include "server.hh"
#include "serverconfig.hh"
#include "serverfactory.hh"
#include "common/exceptions/exception.hh"
#include "common/exceptions/invalidstateexception.hh"
#include "common/exceptions/invalidargumentexception.hh"

#include <logging.hh>

#include <unistd.h>
#include <stdexcept>
#include <algorithm>
#include <cstdint>

// wrap the tryBody in a TRY_CATCH with this macro
#define CODE(...) __VA_ARGS__

#define TRY_CATCH(tryBody, logError) \
  try { tryBody } \
  catch (common::Exception &ex) { logError << ", reason: " << ex.what(); } \
  catch (std::exception &ex)    { logError << ", reason: " << ex.what(); } \
  catch (...)                   { logError; }

namespace rasmgr
{

ServerGroup::ServerGroup(const ServerGroupConfigProto &c,
                         std::shared_ptr<DatabaseHostManager> m,
                         std::shared_ptr<ServerFactory> f):
    config(c), dbhManager(m), serverFactory(f)
{
    if (dbhManager)
    {
        //Validate the configuration and initialize defaults
        this->validateAndInitConfig(this->config);
        //Make sure to decrease the server count in the destructor
        //or when the database host is changed.
        this->databaseHost = dbhManager->getAndLockDatabaseHost(this->config.db_host());
        this->serverStatus = ServerStatus::STOPPED;
    }
}

ServerGroup::~ServerGroup()
{
    if (this->databaseHost)
    {
        TRY_CATCH(
          CODE(
            this->databaseHost->decreaseServerCount();
            this->stopActiveServer(KILL);
          ),
          CODE(LERROR << "Server group destructor failed " << getGroupName())
        )
    }
}

void ServerGroup::start()
{
    boost::upgrade_lock<boost::shared_mutex> groupLock(this->groupMutex);
    // Start is idempotent
    if (this->serverStatus == ServerStatus::STOPPED)
    {
        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(groupLock);
        LDEBUG << "Server group " << getGroupName() << " is stopped, starting it";
        this->startServer();
    }
    else
    {
        LDEBUG << "Server group " << getGroupName() << " is not stopped, will not be started";
    }
}

bool ServerGroup::isStopped()
{
    boost::lock_guard<boost::shared_mutex> groupLock(this->groupMutex);
    return this->serverStatus == ServerStatus::STOPPED;
}

void ServerGroup::stop(KillLevel level)
{
    boost::upgrade_lock<boost::shared_mutex> groupLock(this->groupMutex);
    LDEBUG << "Stopping server group " << getGroupName()
           << " with kill level " << KillLevel_Name(level);
    if (this->serverStatus == ServerStatus::STOPPED)
    {
        throw common::InvalidStateException("Server group is already stopped.");
    }
    else
    {
        this->failedRegistrations = 0;
        boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(groupLock);
        this->stopActiveServer(level);
        this->evaluateGroup();
        this->scheduledForRestart = false;
    }
}

void ServerGroup::scheduleForRestart()
{
    boost::lock_guard<boost::shared_mutex> groupLock(this->groupMutex);
    if (serverStatus == ServerStatus::RUNNING)
    {
        this->scheduledForRestart = true;
        this->restartServer();
    }
    else
    {
        LDEBUG << "Server " << getGroupName() << " not running, will not be restarted";
    }
}

void ServerGroup::restartServer()
{
    LDEBUG << "restarting server, check if available";
    if (this->hasAvailableServer())
    {
        // Send a Close request to all active rasservers
        const auto &serverId = runningServer->getServerId();
        LDEBUG << "\n\nRestarting server " << serverId;
        TRY_CATCH(
          CODE(
            LDEBUG << "Stopping server " << serverId << " gracefully.";
            runningServer->stop(NONE);
            serverStatus = ServerStatus::RESTARTING;
            this->scheduledForRestart = false;
            this->evaluateGroup();
          ),
          CODE(LWARNING << "Failed to stop server " << serverId)
        )
              
        LDEBUG << "Server " << serverId << " restarted: " << !this->scheduledForRestart;
    }
    else
    {
        LDEBUG << "Server " << getGroupName() << " busy, will restart it later";
    }
}

bool ServerGroup::tryRegisterServer(const std::string &serverId)
{
    bool registered = false;
    
    boost::upgrade_lock<boost::shared_mutex> groupLock(this->groupMutex);
    LDEBUG << "Try register server " << serverId;

    //If the server group is stopped, we cannot register new servers.
    if (this->serverStatus == ServerStatus::STOPPED)
    {
        throw common::InvalidStateException(
            "Server group is already stopped, server " + serverId + " cannot be registered.");
    }
    
    if (this->serverStatus == ServerStatus::STARTING && runningServer->getServerId() == serverId)
    {
        TRY_CATCH(
          CODE(
            boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(groupLock);
            runningServer->registerServer(serverId);
            registered = true;
            serverStatus = ServerStatus::RUNNING;
          ),
          CODE(LWARNING << "Failed registering server " << serverId)
        )

        // record failed registrations
        if (registered)
        {
            failedRegistrations = 0; // all good, reset
        }
        else if (static_cast<uint32_t>(failedRegistrations++) >= MAX_GET_SERVER_RETRIES)
        {
            LERROR << "Server registration failed too many times, stopping " << runningServer->getServerId();
            TRY_CATCH(
              CODE(
                boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(groupLock);
                runningServer->stop(KILL);
                runningServer.reset();
                this->serverStatus = ServerStatus::RESTARTING;
                failedRegistrations = 0;
              ),
              CODE(LERROR << "Server is not responding to pings " << runningServer->getServerId())
            )
        }
    }
    // else: the serverId is not found in this server group, nothing to do
    
    LDEBUG << "Try register server " << serverId << ": " << registered;
    return registered;
}

void ServerGroup::evaluateServerGroup()
{
    boost::unique_lock<boost::shared_mutex> groupLock(this->groupMutex);
    this->evaluateGroup();
}

bool ServerGroup::tryGetAvailableServer(const std::string &dbName,
                                        std::shared_ptr<Server> &out_server)
{
    boost::lock_guard<boost::shared_mutex> groupLock(this->groupMutex);
    LDEBUG << getGroupName() << " try to get available server on db " << dbName;
    if (this->scheduledForRestart)
    {
        LDEBUG << "server scheduled for restart, will restart it";
        this->restartServer();
    }
    if (this->databaseHost->ownsDatabase(dbName))
    {
        if (this->serverStatus == ServerStatus::RUNNING && runningServer->isAvailable())
        {
            out_server = runningServer;
            return true;
        }
    }
    return false;
}

std::shared_ptr<Server> ServerGroup::getServer(const std::string &serverId)
{
    boost::shared_lock<boost::shared_mutex> groupLock(this->groupMutex);
    return this->runningServer && this->runningServer->getServerId() == serverId
           ? this->runningServer
           : nullptr;
}

ServerGroupConfigProto ServerGroup::getConfig() const
{
    return config;
}

void ServerGroup::changeGroupConfig(const ServerGroupConfigProto &value)
{
    boost::lock_guard<boost::shared_mutex> groupLock(this->groupMutex);

    if (this->serverStatus != ServerStatus::STOPPED)
        throw common::InvalidStateException(
            "Cannot change ServerGroup configuration while the ServerGroup is running.");

    if (value.has_name())
        this->config.set_name(value.name());
    if (value.has_host())
        this->config.set_host(value.host());
    if (value.has_db_host())
        this->config.set_db_host(value.db_host());
    if (value.ports_size() > 0)
    {
        this->config.clear_ports();
        for (int i = 0; i < value.ports_size(); ++i)
            this->config.add_ports(value.ports(i));
    }

    if (value.has_min_alive_server_no())
        this->config.set_min_alive_server_no(value.min_alive_server_no());
    if (value.has_min_available_server_no())
        this->config.set_min_available_server_no(value.min_available_server_no());
    if (value.has_max_idle_server_no())
        this->config.set_max_idle_server_no(value.max_idle_server_no());
    if (value.has_autorestart())
        this->config.set_autorestart(value.autorestart());
    if (value.has_countdown())
        this->config.set_countdown(value.countdown());
    if (value.has_server_options())
        this->config.set_server_options(value.server_options());

    this->validateAndInitConfig(config);

    if (value.has_db_host())
    {
        //Release the current database host
        this->databaseHost->decreaseServerCount();
        //Retrieve and lock the new database host.
        this->databaseHost = dbhManager->getAndLockDatabaseHost(this->config.db_host());
    }
}

std::string ServerGroup::getGroupName() const
{
    return this->config.name();
}

ServerGroupProto ServerGroup::serializeToProto()
{
    ServerGroupProto result;

    boost::lock_guard<boost::shared_mutex> groupLock(this->groupMutex);

    result.set_name(this->config.name());
    result.set_host(this->config.host());
    result.set_db_host(this->config.db_host());

    for (int i = 0; i < this->config.ports_size(); ++i)
        result.add_ports(static_cast<std::int32_t>(this->config.ports(i)));

    result.set_min_alive_server_no(this->config.min_alive_server_no());
    result.set_min_available_server_no(this->config.min_available_server_no());
    result.set_max_idle_server_no(this->config.max_idle_server_no());
    result.set_autorestart(this->config.autorestart());
    result.set_countdown(this->config.countdown());
    result.set_server_options(this->config.server_options());

    result.set_running(this->serverStatus == ServerStatus::RUNNING);
    result.set_available(this->hasAvailableServer());

    return result;
}

bool ServerGroup::hasAvailableServer()
{    
    if (this->serverStatus == ServerStatus::RUNNING)
    {
        TRY_CATCH(
          CODE(
            return runningServer->isAvailable();
          ),
          CODE(LWARNING << "Failed to check if server is available " << runningServer->getServerId())
        )
    }
    return false;
}

void ServerGroup::evaluateGroup()
{
    // Remove dead servers
    LTRACE << "Evaluating server group " << getGroupName();
    
    if (this->serverStatus == ServerStatus::STOPPED)
    {
        // nothing to do as the server was explicitly stopped e.g. by stop_rasdaman.sh
        return;
    }
    
    this->removeDeadServers();
    this->evaluateServersToRestart();
    
    if (this->serverStatus == ServerStatus::RESTARTING)
    {
        if (runningServer && !runningServer->isAlive())
        {
            // can happen on restart() which just marks as RESTARTING but does
            // not clear the runningServer because it's stopped gracefully
            runningServer->setStarted(false);
            runningServer.reset();
        }
        if (!runningServer)
        {
            LDEBUG << "Start a new server in server group " << getGroupName();
            this->startServer();
        }
        else
        {
            LTRACE << "Server " << runningServer->getServerId() 
                   << " still running, cannot start a new server";
        }
    }
}

// called only from evaluateGroup()
void ServerGroup::removeDeadServers()
{
    LTRACE << "Remove any dead servers in group " << getGroupName();
    
    try
    {
        bool killServer = false;
        
        if (this->serverStatus == ServerStatus::RUNNING)
        {        
            // check for dead process
            TRY_CATCH(
              CODE(
                killServer = !runningServer->isAlive();
              ),
              CODE(
                killServer = true;
                LWARNING << "Server is not responding to pings " << runningServer->getServerId()
              )
            )
        }
        else if (this->serverStatus == ServerStatus::STARTING)
        {
            // check for starting rasserver that hasn't registered with rasmgr within a timeout
            killServer = startingServerTimer->hasExpired();
        }
        else if (this->serverStatus == ServerStatus::STOPPING)
        {
            // check for stopping rasserver that hasn't exited within a timeout
            TRY_CATCH(
              CODE(
                if (!runningServer->isAlive())
                {
                    serverStatus = ServerStatus::STOPPED;
                    runningServer->setStarted(false);
                    runningServer.reset();
                }
                else
                {
                    killServer = stoppingServerTimer->hasExpired();
                }
              ),
              CODE(
                killServer = true;
                LWARNING << "Server is not responding to pings " << runningServer->getServerId()
              )
            )
        }
        
        if (killServer)
        {
            TRY_CATCH(
              CODE(
                LDEBUG << "Killing server and marking for restart " << runningServer->getServerId();
                runningServer->stop(KILL);
                this->scheduledForRestart = false;
                this->serverStatus = ServerStatus::RESTARTING;
                runningServer.reset();
              ),
              CODE(LDEBUG << "Server in is not responding to pings " << runningServer->getServerId())
            )
        }
    }
    catch (common::Exception &ex)
    {
        LWARNING << "Failed removing dead server, reason: " << ex.what();
    }
    catch (std::exception &ex)
    {
        LWARNING << "Failed removing dead server, reason: " << ex.what();
    }
    catch (...)
    {
        LWARNING << "Failed removing dead server";
    }

    LTRACE << "Finished removing dead servers in group " << this->getGroupName();
}

// called only from evaluateGroup()
void ServerGroup::evaluateServersToRestart()
{
    LTRACE << "Evaluate which servers to be restarted in group " << this->getGroupName();
    
    if (!runningServer)
        return;

    if (this->config.countdown() > 0)
    {
        const auto &serverId = runningServer->getServerId();
        const auto totalSessionNo = runningServer->getTotalSessionNo();
        if (totalSessionNo >= this->config.countdown())
        {
            LDEBUG << "Server " << serverId << " has had " << totalSessionNo 
                   << " sessions, more than the configured session countdown of "
                   << this->config.countdown() << ", so it will be restarted.";
            this->serverStatus = ServerStatus::RESTARTING;
        }
    }
    // else the countdown is set to 0: servers will not be restarted
    
    // Stop the server that has to be restarted; it will be started 
    // afterwards in evaluateGroup()
    if (runningServer && this->serverStatus == ServerStatus::RESTARTING)
    {
        bool serverIsFree = true;
        TRY_CATCH(
          CODE(
            serverIsFree = runningServer->isFree();
          ),
          CODE(LWARNING << "Failed to check if server is free " << runningServer->getServerId())
        )
        if (serverIsFree)
        {
            LDEBUG << "Killing free server " << runningServer->getServerId() << " so it can be restarted.";
            runningServer->stop(KILL);
            runningServer.reset();
            this->scheduledForRestart = false;
        }
    }

    LTRACE << "Finished evaluating which servers have to be restarted in group " << this->getGroupName();
}

void ServerGroup::startServer()
{
    int32_t port = getConfiguredPort();
    const auto &host = this->config.host();
    
    LDEBUG << "Starting server " << this->getGroupName() << " on " << host << ":" << port;
    
    ServerConfig serverConfig(host, static_cast<uint32_t>(port), this->databaseHost);
    serverConfig.setOptions(this->config.server_options());
    
    runningServer = this->serverFactory->createServer(serverConfig);
    auto startingServerLifetime = this->config.starting_server_lifetime();
    startingServerTimer = std::make_shared<common::Timer>(startingServerLifetime);
    
    runningServer->startProcess();
    LDEBUG << "Server " << this->getGroupName() << " started.";
    
    serverStatus = ServerStatus::STARTING;
    this->scheduledForRestart = false;
}

void ServerGroup::stopActiveServer(KillLevel level)
{
    LDEBUG << "Stopping servers in group " << this->getGroupName();

    //Stop the running server
    if (serverStatus == ServerStatus::RUNNING)
    {
        TRY_CATCH(
          CODE(
            runningServer->stop(level);
            serverStatus = ServerStatus::STOPPING;
            this->scheduledForRestart = false;
            stoppingServerTimer = std::make_shared<common::Timer>(10*1000);
          ),
          CODE(LERROR << "Failed to stop running server " << runningServer->getServerId())
        )
    }

    //The servers that are starting but have not yet registered
    //will be stoped forcibly and the ports they used will be returned
    //to the pool of available ports
    if (serverStatus == ServerStatus::STARTING)
    {
        TRY_CATCH(
          CODE(
            LDEBUG << "killing server " << runningServer->getServerId();
            runningServer->stop(KILL);
            serverStatus = ServerStatus::STOPPED;
            this->scheduledForRestart = false;
            runningServer.reset();
          ),
          CODE(LERROR << "Failed to stop starting server " << runningServer->getServerId())
        )
    }
}

void ServerGroup::validateAndInitConfig(ServerGroupConfigProto &cfg)
{
    if (!cfg.has_name())
        throw common::InvalidArgumentException("Missing configuration parameter: \"name\"");
    if (!cfg.has_host())
        throw common::InvalidArgumentException("Missing configuration parameter: \"host\"");
    if (!cfg.has_db_host())
        throw common::InvalidArgumentException("Missing configuration parameter: \"db_host\"");
    if (!cfg.ports_size())
        throw common::InvalidArgumentException("Missing configuration parameter: \"port\"");

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

std::int32_t ServerGroup::getConfiguredPort() const
{
    return std::int32_t(config.ports(0));
}

}
