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

#include "databasehostmanager.hh"
#include "databasehost.hh"
#include "common/exceptions/invalidargumentexception.hh"
#include "exceptions/dbhostalreadyexistsexception.hh"
#include "exceptions/dbhostbusyexception.hh"
#include "exceptions/inexistentdbhostexception.hh"
#include <logging.hh>

#include <boost/thread/shared_lock_guard.hpp>

namespace rasmgr
{
using std::list;
using std::shared_ptr;
using std::runtime_error;

void DatabaseHostManager::defineDatabaseHost(const DatabaseHostPropertiesProto &newDbHost)
{
    if (!newDbHost.has_host_name() || newDbHost.host_name().empty())
    {
        throw common::InvalidArgumentException("Invalid database host configuration:\n" + newDbHost.DebugString());
    }

    // make sure it's not a duplicate database host
    boost::upgrade_lock<boost::shared_mutex> lock(this->hostListMutex);
    for (auto it = this->hostList.begin(); it != this->hostList.end(); ++it)
    {
        if ((*it)->getHostName() == newDbHost.host_name())
        {
            throw DbHostAlreadyExistsException(newDbHost.host_name());
        }
    }

    std::string empty = "";
    std::string connectStr = newDbHost.has_connect_string() ? newDbHost.connect_string() : empty;
    std::string userName = newDbHost.has_user_name() ? newDbHost.user_name() : empty;
    std::string password = newDbHost.has_password() ? newDbHost.password() : empty;
    auto dbHost = std::make_shared<DatabaseHost>(newDbHost.host_name(), connectStr, userName, password);

    boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
    this->hostList.push_back(dbHost);
}

void DatabaseHostManager::changeDatabaseHost(const std::string &oldName, const DatabaseHostPropertiesProto &newProperties)
{
    bool changed = false;

    boost::lock_guard<boost::shared_mutex> lock(this->hostListMutex);
    for (auto it = this->hostList.begin(); it != this->hostList.end(); ++it)
    {
        if ((*it)->getHostName() == oldName)
        {
            if (!(*it)->isBusy())
            {
              if (newProperties.has_connect_string())
                  (*it)->setConnectString(newProperties.connect_string());
              if (newProperties.has_host_name() && !newProperties.host_name().empty())
                  (*it)->setHostName(newProperties.host_name());
              if (newProperties.has_password())
                  (*it)->setPasswdString(newProperties.password());
              if (newProperties.has_user_name())
                  (*it)->setUserName(newProperties.user_name());
              changed = true;
              break;
            }
            else
            {
                throw DbHostBusyException((*it)->getHostName());
            }
        }
    }

    if (!changed)
    {
        throw InexistentDbHostException(oldName);
    }
}

void DatabaseHostManager::removeDatabaseHost(const std::string &dbHostName)
{
    bool erased = false;

    boost::upgrade_lock<boost::shared_mutex> lock(this->hostListMutex);

    for (auto it = this->hostList.begin(); it != this->hostList.end(); ++it)
    {
        if ((*it)->getHostName() == dbHostName)
        {
            if ((*it)->isBusy())
            {
                throw DbHostBusyException((*it)->getHostName());
            }
            else
            {
                boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
                this->hostList.erase(it);
                erased = true;
                break;
            }
        }
    }

    if (!erased)
    {
        throw InexistentDbHostException(dbHostName);
    }
}

std::shared_ptr<DatabaseHost> DatabaseHostManager::getAndLockDatabaseHost(const std::string &dbHostName)
{
    boost::shared_lock<boost::shared_mutex> lock(this->hostListMutex);
    for (auto it = this->hostList.begin(); it != this->hostList.end(); ++it)
    {
        if ((*it)->getHostName() == dbHostName)
        {
            (*it)->increaseServerCount();
            return (*it);
        }
    }
    throw InexistentDbHostException(dbHostName);
}

std::shared_ptr<DatabaseHost> DatabaseHostManager::getDatabaseHost(const std::string &dbName)
{
    boost::shared_lock<boost::shared_mutex> lock(this->hostListMutex);
    for (auto it = this->hostList.begin(); it != this->hostList.end(); ++it)
    {
        if ((*it)->ownsDatabase(dbName))
        {
            return (*it);
        }
    }
    throw common::RuntimeException("Host containing database " + dbName + " not found.");
}

std::list<std::shared_ptr<DatabaseHost> > DatabaseHostManager::getDatabaseHostList() const
{
    return this->hostList;
}

DatabaseHostMgrProto DatabaseHostManager::serializeToProto()
{
    boost::shared_lock<boost::shared_mutex> lock(this->hostListMutex);
    DatabaseHostMgrProto result;
    for (auto it = this->hostList.begin(); it != this->hostList.end(); ++it)
    {
        result.add_database_hosts()->CopyFrom(DatabaseHost::serializeToProto(*(*it)));
    }
    return result;
}


} /* namespace rasmgr */
