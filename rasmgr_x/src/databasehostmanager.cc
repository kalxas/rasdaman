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

#include <stdexcept>
#include <boost/smart_ptr.hpp>

#include <logging.hh>
#include "common/src/exceptions/rasexceptions.hh"

#include "exceptions/rasmgrexceptions.hh"
#include "databasehost.hh"

#include "databasehostmanager.hh"

namespace rasmgr
{
using std::list;
using boost::shared_ptr;
using boost::unique_lock;
using boost::mutex;
using std::runtime_error;

DatabaseHostManager::~DatabaseHostManager()
{}

void DatabaseHostManager::defineDatabaseHost(const DatabaseHostPropertiesProto& newDbHost)
{
    if (!newDbHost.has_host_name() || newDbHost.host_name().empty())
    {
        throw common::InvalidArgumentException("Invalid database host configuration:\n" + newDbHost.DebugString());
    }

    list<shared_ptr<DatabaseHost>>::iterator it;
    bool duplicate = false;

    unique_lock<mutex> lock(this->mut);

    for (it = this->hostList.begin(); it != this->hostList.end(); ++it)
    {
        if ((*it)->getHostName() == newDbHost.host_name())
        {
            duplicate = true;
            break;
        }
    }

    if (duplicate)
    {
        throw DbHostAlreadyExistsException(newDbHost.host_name());
    }
    else
    {
        std::string empty = "";
        std::string connectStr = newDbHost.has_connect_string() ? newDbHost.connect_string() : empty;
        std::string userName = newDbHost.has_user_name() ? newDbHost.user_name() : empty;
        std::string password = newDbHost.has_password() ? newDbHost.password() : empty;
        auto dbHost = boost::make_shared<DatabaseHost>(newDbHost.host_name(), connectStr, userName, password);

        this->hostList.push_back(dbHost);
    }
}

void DatabaseHostManager::changeDatabaseHost(const std::string& oldName, const DatabaseHostPropertiesProto& newProperties)
{
    list<shared_ptr<DatabaseHost>>::iterator it;
    bool changed = false;

    unique_lock<mutex> lock(this->mut);

    for (it = this->hostList.begin(); it != this->hostList.end(); ++it)
    {
        if ((*it)->getHostName() == oldName)
        {
            if ((*it)->isBusy())
            {
                throw DbHostBusyException((*it)->getHostName());
            }
            else
            {
                if (newProperties.has_connect_string())
                {
                    (*it)->setConnectString(newProperties.connect_string());
                }

                if (newProperties.has_host_name() && !newProperties.host_name().empty())
                {
                    (*it)->setHostName(newProperties.host_name());
                }

                if (newProperties.has_password())
                {
                    (*it)->setPasswdString(newProperties.password());
                }

                if (newProperties.has_user_name())
                {
                    (*it)->setUserName(newProperties.user_name());
                }

                changed = true;
            }

            break;
        }
    }

    if (!changed)
    {
        throw InexistentDbHostException(oldName);
    }
}

void DatabaseHostManager::removeDatabaseHost(const std::string& dbHostName)
{
    bool erased = false;
    list<shared_ptr<DatabaseHost>>::iterator it;

    unique_lock<mutex> lock(this->mut);

    for (it = this->hostList.begin(); it != this->hostList.end(); ++it)
    {
        if ((*it)->getHostName() == dbHostName)
        {
            if ((*it)->isBusy())
            {
                throw DbHostBusyException((*it)->getHostName());
            }
            else
            {
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

boost::shared_ptr<DatabaseHost> DatabaseHostManager::getAndLockDatabaseHost(const std::string& dbHostName)
{
    list<shared_ptr<DatabaseHost>>::iterator it;

    unique_lock<mutex> lock(this->mut);

    for (it = this->hostList.begin(); it != this->hostList.end(); ++it)
    {
        if ((*it)->getHostName() == dbHostName)
        {
            (*it)->increaseServerCount();
            return (*it);
        }
    }

    throw InexistentDbHostException(dbHostName);
}

std::list<boost::shared_ptr<DatabaseHost>> DatabaseHostManager::getDatabaseHostList() const
{
    return this->hostList;
}

DatabaseHostMgrProto DatabaseHostManager::serializeToProto()
{
    DatabaseHostMgrProto result;

    list<shared_ptr<DatabaseHost>> dbhList = this->getDatabaseHostList();

    list<shared_ptr<DatabaseHost>>::iterator it;

    for (it = dbhList.begin(); it != dbhList.end(); ++it)
    {
        result.add_database_hosts()->CopyFrom(DatabaseHost::serializeToProto(*(*it)));
    }

    return result;
}


} /* namespace rasmgr */
