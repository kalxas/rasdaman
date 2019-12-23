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

#include "exceptions/rasmgrexceptions.hh"
#include "common/exceptions/rasexceptions.hh"
#include "common/uuid/uuid.hh"
#include <logging.hh>

#include <cerrno>
#include <cstdio>
#include <unistd.h>

#include <map>
#include <set>
#include <stdexcept>
#include <sstream>
#include <unordered_set>

#include <boost/lexical_cast.hpp>
#include <boost/format.hpp>

namespace rasmgr
{

using boost::format;
using boost::lexical_cast;
using boost::shared_lock;
using boost::shared_mutex;

using std::map;
using std::runtime_error;
using std::set;
using std::string;
using std::list;

using common::UUID;


ServerManager::ServerManager(const ServerManagerConfig &config1, std::shared_ptr<ServerGroupFactory> sgf)
    : serverGroupFactory(sgf), config(config1)
{
    this->isWorkerThreadRunning = true;
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
    }
    catch (std::exception &ex)
    {
        LERROR << "ServerManager destructor failed: " << ex.what();
    }
    catch (...)
    {
        LERROR << "ServerManager destructor failed";
    }
}

bool ServerManager::tryGetFreeServer(const std::string &databaseName, std::shared_ptr<Server> &out_server)
{
    bool success = false;

    shared_lock<shared_mutex> lockMutexGroups(this->serverGroupMutex);

    for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
    {
        if ((*it)->tryGetAvailableServer(databaseName, out_server))
        {
            success = true;
            break;
        }
    }

    return success;
}


void ServerManager::registerServer(const string &serverId)
{
    bool registered = false;

    shared_lock<shared_mutex> lockMutexGroups(this->serverGroupMutex);

    for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
    {
        if (!(*it)->isStopped() && ((*it)->tryRegisterServer(serverId)))
        {
            registered = true;
            break;
        }
    }

    if (!registered)
    {
        throw InexistentServerGroupException(serverId);
    }
}

void ServerManager::defineServerGroup(const ServerGroupConfigProto &serverGroupConfig)
{
    boost::unique_lock<shared_mutex> lock(this->serverGroupMutex);

    for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
    {
        if ((*it)->getGroupName() == serverGroupConfig.name())
        {
            throw ServerGroupDuplicateException((*it)->getGroupName());
        }
    }

    this->serverGroupList.push_back(this->serverGroupFactory->createServerGroup(serverGroupConfig));
}

void ServerManager::changeServerGroup(const std::string &oldServerGroupName, const ServerGroupConfigProto &newServerGroupConfig)
{
    bool changed = false;

    boost::unique_lock<shared_mutex> lock(this->serverGroupMutex);
    for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
    {
        if ((*it)->getGroupName() == oldServerGroupName)
        {
            if ((*it)->isStopped())
            {
                (*it)->changeGroupConfig(newServerGroupConfig);
                changed = true;
            }
            else
            {
                std::string errorMessage("Cannot change ServerGroup configuration while the ServerGroup is running.");
                throw common::InvalidStateException(errorMessage);
            }

            break;
        }
    }

    if (!changed)
    {
        throw InexistentServerGroupException(oldServerGroupName);
    }
}

void ServerManager::removeServerGroup(const std::string &serverGroupName)
{
    bool removed = false;

    boost::unique_lock<shared_mutex> lock(this->serverGroupMutex);
    for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
    {
        if ((*it)->getGroupName() == serverGroupName)
        {
            if ((*it)->isStopped())
            {
                this->serverGroupList.erase(it);
                removed = true;

                break;
            }
            else
            {
                throw ServerGroupBusyException(serverGroupName);
            }
        }
    }

    if (!removed)
    {
        throw InexistentServerGroupException(serverGroupName);
    }
}

void ServerManager::startServerGroup(const StartServerGroup &startGroup)
{
    shared_lock<shared_mutex> lockMutexGroups(this->serverGroupMutex);
    
    if (startGroup.has_group_name())
    {
        bool found = false;
        for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
        {
            auto srv = (*it);

            if (srv->getGroupName() == startGroup.group_name())
            {
                LDEBUG << "Starting server: " << srv->getGroupName();
                srv->start();
                found = true;
                break;
            }
        }

        if (!found)
        {
            throw InexistentServerGroupException(startGroup.group_name());
        }
    }
    else if (startGroup.has_host_name())
    {
        bool hostExists = false;
        std::string onHost = startGroup.host_name();

        for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
        {
            if ((*it)->getConfig().host() == onHost)
            {
                hostExists = true;
                if ((*it)->isStopped())
                {
                    (*it)->start();
                }
            }
        }

        if (!hostExists)
        {
            throw common::MissingResourceException("There are no server groups defined on host \"" + onHost + "\"");
        }
    }
    else if (startGroup.has_all())
    {
        for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
        {
            if ((*it)->isStopped())
            {
                (*it)->start();
            }
        }
    }
    else
    {
        throw common::InvalidArgumentException("startGroup");
    }
}

void ServerManager::stopServerGroup(const StopServerGroup &stopGroup)
{
    shared_lock<shared_mutex> lockMutexGroups(this->serverGroupMutex);

    if (stopGroup.has_group_name())
    {
        bool stopped = false;

        for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
        {
            auto srv = (*it);
            if (srv->getGroupName() == stopGroup.group_name())
            {
                srv->stop(stopGroup.kill_level());
                stopped = true;
                break;
            }
        }

        if (!stopped)
        {
            throw InexistentServerGroupException(stopGroup.group_name());
        }
    }
    else if (stopGroup.has_host_name())
    {
        bool hostExists = false;
        std::string onHost = stopGroup.host_name();

        for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
        {
            if ((*it)->getConfig().host() == onHost)
            {
                hostExists = true;
                if (!(*it)->isStopped())
                {
                    (*it)->stop(stopGroup.kill_level());
                }
            }
        }

        if (!hostExists)
        {
            throw common::MissingResourceException("There are no server groups defined on host \"" + onHost + "\"");
        }
    }
    else if (stopGroup.has_all())
    {
        for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
        {
            if (!(*it)->isStopped())
            {
                (*it)->stop(stopGroup.kill_level());
            }
        }
    }
    else
    {
        throw common::InvalidArgumentException("stopGroup");
    }
}

bool ServerManager::hasRunningServers()
{
    bool found = false;

    boost::unique_lock<shared_mutex> lock(this->serverGroupMutex);

    for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
    {
        if (!(*it)->isStopped())
        {
            found = true;
            break;
        }
    }

    return found;
}

ServerMgrProto ServerManager::serializeToProto()
{
    ServerMgrProto result;

    shared_lock<shared_mutex> lockMutexGroups(this->serverGroupMutex);
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
        try
        {
            // Wait on the condition variable to be notified from the
            // destructor when it is time to stop the worker thread
            if (this->isThreadRunningCondition.wait_for(threadLock, timeToSleepFor) == std::cv_status::timeout)
            {
                this->evaluateServerGroups();
            }
        }
        catch (std::exception &ex)
        {
            LERROR << ex.what();
        }
        catch (...)
        {
            LERROR << "Evaluating server groups has failed";
        }
    }
}

void ServerManager::evaluateServerGroups()
{
    /**
     * For each server group evaluate the group's status.
     * This means that dead server entries will be removed
     * and new servers will be started.
     */
    list<std::string> removeGroups;
    {
        shared_lock<shared_mutex> lock(this->serverGroupMutex);
        LTRACE << "Evaluating server groups.";
        for (auto it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
        {
            try
            {
                (*it)->evaluateServerGroup();
            }
            catch (std::exception &e)
            {
                LERROR << "Could not evaluate server in group: " << e.what();
            }
            catch (...)
            {
                LERROR << "Unexpected exception when starting server";
            }
            if ((*it)->isStopped())
            {
                removeGroups.push_back((*it)->getGroupName());
            }
        }
    }
}


} /* namespace rasmgr */
