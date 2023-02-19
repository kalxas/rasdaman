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

#include "server.hh"
#include "servergroup.hh"
#include "servergroupfactory.hh"
#include "servermanagerconfig.hh"
#include "servermanager.hh"
#include "exceptions/inexistentservergroupexception.hh"
#include "exceptions/servergroupduplicateexception.hh"
#include "exceptions/servergroupbusyexception.hh"
#include <logging.hh>

#include <cerrno>
#include <cstdio>
#include <unistd.h>

#include <map>
#include <set>
#include <stdexcept>
#include <sstream>
#include <unordered_set>

namespace rasmgr
{

ServerManager::ServerManager(const ServerManagerConfig &config1, std::shared_ptr<ServerGroupFactory> sgf)
  : serverGroupFactory(sgf), config(config1), isWorkerThreadRunning(true), isRestartServersThreadRunning(false)
{
    this->workerCleanup.reset(new std::thread(&ServerManager::workerCleanupRunner, this));
}

ServerManager::~ServerManager()
{
    try
    {
        {
            std::lock_guard<std::mutex> lock(this->threadMutex);
            this->isWorkerThreadRunning = false;
        }
        this->isThreadRunningCondition.notify_one();
        this->workerCleanup->join();
        if (this->restartServersThread)
        {
            this->restartServersThread->join();
        }
    }
    catch (std::exception &ex)
    {
        LERROR << "Server manager destructor failed: " << ex.what();
    }
    catch (...)
    {
        LERROR << "Server manager destructor failed";
    }
}

bool ServerManager::tryGetAvailableServer(const std::string &databaseName, std::shared_ptr<Server> &out_server)
{
    boost::shared_lock<boost::shared_mutex> lockMutexGroups(this->serverGroupMutex);
    for (const auto &sg: this->serverGroupList)
    {
        if (sg->tryGetAvailableServer(databaseName, out_server))
        {
            return true;
        }
    }
    return false;
}

void ServerManager::registerServer(const std::string &serverId)
{    
    bool registered = false;
    boost::shared_lock<boost::shared_mutex> lockMutexGroups(this->serverGroupMutex);
    
    for (const auto &sg: this->serverGroupList)
    {
        if (!sg->isStopped() && sg->tryRegisterServer(serverId))
        {
            registered = true;
            break;
        }
    }

    if (!registered)
    {
        throw InexistentServerGroupException(serverId, "failed registering server");
    }
}

void ServerManager::defineServerGroup(const ServerGroupConfigProto &serverGroupConfig)
{
    boost::upgrade_lock<boost::shared_mutex> lock(this->serverGroupMutex);
    for (const auto &sg: this->serverGroupList)
    {
        if (sg->getGroupName() == serverGroupConfig.name())
        {
            throw ServerGroupDuplicateException(sg->getGroupName());
        }
    }
    
    boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
    auto sg = this->serverGroupFactory->createServerGroup(serverGroupConfig);
    this->serverGroupList.push_back(sg);
}

void ServerManager::changeServerGroup(const std::string &oldServerGroupName,
                                      const ServerGroupConfigProto &newServerGroupConfig)
{
    bool found = false;

    boost::lock_guard<boost::shared_mutex> lock(this->serverGroupMutex);
    for (const auto &sg: this->serverGroupList)
    {
        if (sg->getGroupName() == oldServerGroupName)
        {
            found = true;
            if (sg->isStopped())
            {
                sg->changeGroupConfig(newServerGroupConfig);
            }
            else
            {
                throw common::InvalidStateException(
                    "Cannot change ServerGroup configuration while the ServerGroup is running.");
            }
            break;
        }
    }

    if (!found)
    {
        throw InexistentServerGroupException(oldServerGroupName, "cannot change server configuration");
    }
}

void ServerManager::removeServerGroup(const std::string &serverGroupName)
{
    bool found = false;

    boost::lock_guard<boost::shared_mutex> lock(this->serverGroupMutex);
    for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
    {
        if ((*it)->getGroupName() == serverGroupName)
        {
            found = true;
            if ((*it)->isStopped())
            {
                this->serverGroupList.erase(it);
                break;
            }
            else
            {
                throw ServerGroupBusyException(serverGroupName);
            }
        }
    }

    if (!found)
    {
        throw InexistentServerGroupException(serverGroupName, "cannot remove server");
    }
}

void ServerManager::startServerGroup(const StartServerGroup &startGroup)
{
    boost::shared_lock<boost::shared_mutex> lockMutexGroups(this->serverGroupMutex);
    
    if (startGroup.has_group_name())
    {
        // up srv N1
        bool found = false;
        const auto &group = startGroup.group_name();
        for (const auto &sg: this->serverGroupList)
        {
            if (sg->getGroupName() == group)
            {
                LDEBUG << "Starting server: " << group;
                sg->start();
                found = true;
                break;
            }
        }
        if (!found)
        {
            throw InexistentServerGroupException(group, "cannot start server");
        }
    }
    else if (startGroup.has_host_name())
    {
        // up srv -host rasdaman_host
        bool found = false;
        const auto &host = startGroup.host_name();
        for (const auto &sg: this->serverGroupList)
        {
            if (sg->getConfig().host() == host)
            {
                LDEBUG << "Starting server: " << sg->getGroupName();
                sg->start();
                found = true;
            }
        }
        if (!found)
        {
            throw common::MissingResourceException("No server groups are defined on host \"" + host + "\"");
        }
    }
    else if (startGroup.has_all())
    {
        // up srv -all
        for (const auto &sg: this->serverGroupList)
        {
            LDEBUG << "Starting server: " << sg->getGroupName();
            sg->start();
        }
    }
    else
    {
        throw common::InvalidArgumentException("Invalid configuration for starting server groups.");
    }
}

void ServerManager::stopServerGroup(const StopServerGroup &stopGroup)
{
    boost::shared_lock<boost::shared_mutex> lockMutexGroups(this->serverGroupMutex);
    if (stopGroup.has_group_name())
    {
        bool found = false;
        const auto &group = stopGroup.group_name();
        for (const auto &sg: this->serverGroupList)
        {
            if (sg->getGroupName() == group)
            {
                LDEBUG << "Stopping server " << group;
                sg->stop(stopGroup.kill_level());
                found = true;
                break;
            }
        }
        if (!found)
        {
            throw InexistentServerGroupException(group, "cannot stop server");
        }
    }
    else if (stopGroup.has_host_name())
    {
        bool found = false;
        const auto &host = stopGroup.host_name();
        for (const auto &sg: this->serverGroupList)
        {
            if (sg->getConfig().host() == host)
            {
                found = true;
                if (!sg->isStopped())
                {
                    LDEBUG << "Stopping server " << sg->getGroupName();
                    sg->stop(stopGroup.kill_level());
                }
            }
        }
        if (!found)
        {
            throw common::MissingResourceException("No server groups are defined on host \"" + host + "\"");
        }
    }
    else if (stopGroup.has_all())
    {
        for (const auto &sg: this->serverGroupList)
        {
            if (!sg->isStopped())
            {
                LDEBUG << "Stopping server " << sg->getGroupName();
                sg->stop(stopGroup.kill_level());
            }
        }
    }
    else
    {
        throw common::InvalidArgumentException("Invalid configuration for stopping server groups.");
    }
}

void ServerManager::restartAllServerGroups()
{
    boost::upgrade_lock<boost::shared_mutex> sharedLock(this->restartServersMutex);
    if (!this->isRestartServersThreadRunning && this->isWorkerThreadRunning)
    {
        boost::upgrade_to_unique_lock<boost::shared_mutex> exclusiveLock(sharedLock);
        if (this->restartServersThread)
        {
            this->restartServersThread->join();
        }
        this->isRestartServersThreadRunning = true;
        this->restartServersThread.reset(new std::thread(&ServerManager::restartServersRunner, this));
    }
}

void ServerManager::restartServersRunner()
{
    LDEBUG << "restarting servers, wait for " << this->config.getRestartDelay() << " seconds.";
    // wait before proceeding with restarting servers
//    sleep(this->config.getRestartDelay());
    LDEBUG << "restarting servers, waiting finished.";
    
    {
        boost::shared_lock<boost::shared_mutex> lock(this->serverGroupMutex);
        for (const auto &sg: this->serverGroupList)
        {
            sg->scheduleForRestart();
        }
        LDEBUG << "restarting servers, all groups restarted.";
    }
    {
        boost::unique_lock<boost::shared_mutex> exclusiveLock(this->restartServersMutex);
        this->isRestartServersThreadRunning = false;
        LDEBUG << "restarting servers, thread running flag set to false.";
    }
}

bool ServerManager::hasRunningServers()
{
    boost::lock_guard<boost::shared_mutex> lock(this->serverGroupMutex);
    for (const auto &sg: this->serverGroupList)
    {
        if (!sg->isStopped())
        {
            return true;
        }
    }
    return false;
}

std::shared_ptr<Server> ServerManager::getServer(const std::string &serverId)
{
    boost::shared_lock<boost::shared_mutex> lockMutexGroups(this->serverGroupMutex);
    
    for (const auto &sg: this->serverGroupList)
    {
        auto ret = sg->getServer(serverId);
        if (ret)
        {
            return ret;
        }
    }
  
    throw InexistentServerGroupException(serverId, "cannot get server");
}

ServerMgrProto ServerManager::serializeToProto()
{
    ServerMgrProto result;

    boost::shared_lock<boost::shared_mutex> lockMutexGroups(this->serverGroupMutex);
    for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
    {
        result.add_server_groups()->CopyFrom((*it)->serializeToProto());
    }

    return result;
}

void ServerManager::workerCleanupRunner()
{
    std::chrono::milliseconds timeToSleepFor(this->config.getCleanupInterval());

    std::unique_lock<std::mutex> threadLock(this->threadMutex);
    while (this->isWorkerThreadRunning)
    {
        // Wait on the condition variable to be notified from the
        // ~ServerManager() destructor when it is time to stop the worker thread,
        // or until the cleanup timeout is reached (3 seconds by default)
        if (this->isThreadRunningCondition.wait_for(threadLock, timeToSleepFor) ==
            std::cv_status::timeout)
        {
            this->evaluateServerGroups();
        }
    }
}

void ServerManager::evaluateServerGroups()
{
    /**
     * For each server group evaluate the group's status. This means that dead
     * server entries will be removed and new servers will be started.
     */
    boost::shared_lock<boost::shared_mutex> lock(this->serverGroupMutex);
    LTRACE << "Evaluating server groups.";
    for (const auto &sg: this->serverGroupList)
    {
        try
        {
            sg->evaluateServerGroup();
        }
        catch (common::Exception &e)
        {
            LERROR << "Failed evaluating status of server " << sg->getGroupName() << ": " << e.what();
        }
        catch (std::exception &e)
        {
            LERROR << "Failed evaluating status of server " << sg->getGroupName() << ": " << e.what();
        }
        catch (...)
        {
            LERROR << "Failed evaluating status of server " << sg->getGroupName();
        }
    }
}

} /* namespace rasmgr */
