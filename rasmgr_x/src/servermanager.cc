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

#include <cerrno>
#include <cstdio>
#include <unistd.h>

#include <map>
#include <set>
#include <stdexcept>
#include <sstream>

#include <boost/lexical_cast.hpp>
#include <boost/thread.hpp>
#include <boost/unordered_set.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/thread/thread.hpp>
#include <boost/format.hpp>

#include "common/src/exceptions/rasexceptions.hh"
#include <logging.hh>
#include "common/src/uuid/uuid.hh"

#include "exceptions/rasmgrexceptions.hh"

#include "server.hh"
#include "servergroup.hh"
#include "servergroupfactory.hh"
#include "servermanagerconfig.hh"

#include "servermanager.hh"

namespace rasmgr
{

using boost::format;
using boost::lexical_cast;
using boost::mutex;
using boost::posix_time::microsec_clock;
using boost::posix_time::milliseconds;
using boost::scoped_ptr;
using boost::shared_lock;
using boost::shared_mutex;
using boost::shared_ptr;
using boost::thread;
using boost::unique_lock;
using boost::unordered_set;

using std::map;
using std::runtime_error;
using std::set;
using std::string;
using std::list;

using common::UUID;


ServerManager::ServerManager(const ServerManagerConfig& config, boost::shared_ptr<ServerGroupFactory> serverGroupFactory)
    : serverGroupFactory(serverGroupFactory), config(config)
{
    this->isWorkerThreadRunning = true;
    this->workerCleanup.reset(new thread(&ServerManager::workerCleanupRunner, this));
}

ServerManager::~ServerManager()
{
    try
    {
        {
            boost::lock_guard<boost::mutex> lock(this->threadMutex);
            this->isWorkerThreadRunning = false;
        }

        this->isThreadRunningCondition.notify_one();

        this->workerCleanup->join();
    }
    catch (std::exception& ex)
    {
        LERROR << ex.what();
    }
    catch (...)
    {
        LERROR << "ServerManager destructor has failed";
    }
}

bool ServerManager::tryGetFreeServer(const std::string& databaseName, boost::shared_ptr<Server>& out_server)
{
    bool success = false;
    list<shared_ptr<ServerGroup>>::iterator it;

    shared_lock<shared_mutex> lockMutexGroups(this->serverGroupMutex);

    for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
    {
        if ((*it)->tryGetAvailableServer(databaseName, out_server))
        {
            success = true;
            break;
        }
    }

    return success;
}


void ServerManager::registerServer(const string& serverId)
{
    bool registered = false;
    list<shared_ptr<ServerGroup>>::iterator it;

    shared_lock<shared_mutex> lockMutexGroups(this->serverGroupMutex);

    for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
    {
        if (((*it)->tryRegisterServer(serverId)))
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

void ServerManager::defineServerGroup(const ServerGroupConfigProto& serverGroupConfig)
{
    list<shared_ptr<ServerGroup>>::iterator it;

    unique_lock<shared_mutex> lock(this->serverGroupMutex);

    for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
    {
        if ((*it)->getGroupName() == serverGroupConfig.name())
        {
            throw ServerGroupDuplicateException((*it)->getGroupName());
        }
    }

    this->serverGroupList.push_back(this->serverGroupFactory->createServerGroup(serverGroupConfig));
}

void ServerManager::changeServerGroup(const std::string& oldServerGroupName, const ServerGroupConfigProto& newServerGroupConfig)
{
    list<shared_ptr<ServerGroup>>::iterator it;
    bool changed = false;

    unique_lock<shared_mutex> lock(this->serverGroupMutex);
    for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
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

void ServerManager::removeServerGroup(const std::string& serverGroupName)
{
    list<shared_ptr<ServerGroup>>::iterator it;
    bool removed = false;

    unique_lock<shared_mutex> lock(this->serverGroupMutex);
    for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
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

void ServerManager::startServerGroup(const StartServerGroup& startGroup)
{
    list<shared_ptr<ServerGroup>>::iterator it;
    shared_lock<shared_mutex> lockMutexGroups(this->serverGroupMutex);

    if (startGroup.has_group_name())
    {
        bool found = false;
        for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
        {
            shared_ptr<ServerGroup>  srv = (*it);
            if (srv->getGroupName() == startGroup.group_name())
            {
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

        for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
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
        for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
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

void ServerManager::stopServerGroup(const StopServerGroup& stopGroup)
{
    list<shared_ptr<ServerGroup>>::iterator it;
    shared_lock<shared_mutex> lockMutexGroups(this->serverGroupMutex);

    if (stopGroup.has_group_name())
    {
        bool stopped = false;

        for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
        {
            shared_ptr<ServerGroup>  srv = (*it);
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

        for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
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
        for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
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

    unique_lock<shared_mutex> lock(this->serverGroupMutex);

    list<shared_ptr<ServerGroup>>::iterator it;
    for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
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
    list<shared_ptr<ServerGroup>>::iterator it;

    for (it = this->serverGroupList.begin(); it != this->serverGroupList.end(); ++it)
    {
        result.add_server_groups()->CopyFrom((*it)->serializeToProto());
    }

    return result;
}

void ServerManager::workerCleanupRunner()
{
    boost::posix_time::time_duration timeToSleepFor = boost::posix_time::milliseconds(this->config.getCleanupInterval());

    boost::unique_lock<boost::mutex> threadLock(this->threadMutex);
    while (this->isWorkerThreadRunning)
    {
        try
        {
            // Wait on the condition variable to be notified from the
            // destructor when it is time to stop the worker thread
            if (!this->isThreadRunningCondition.timed_wait(threadLock, timeToSleepFor))
            {

                shared_lock<shared_mutex> lock(this->serverGroupMutex);

                this->evaluateServerGroups();
            }
        }
        catch (std::exception& ex)
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
    LTRACE << "Evaluating server groups.";
    for (list<shared_ptr<ServerGroup>>::iterator it = this->
            serverGroupList.begin();
            it != this->serverGroupList.end();
            ++it)
    {
        try
        {
            (*it)->evaluateServerGroup();
        }
        catch (std::exception& e)
        {
            LERROR << "Could not evaluate server in group.Reason:" << e.what();
        }
        catch (...)
        {
            LERROR << "Unexpected exception when starting server";
        }
    }
}


} /* namespace rasmgr */
