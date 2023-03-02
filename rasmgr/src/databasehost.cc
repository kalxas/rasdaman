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

#include "database.hh"
#include "databasehost.hh"
#include "exceptions/inexistentdatabaseexception.hh"
#include "exceptions/databasealreadyexistsexception.hh"
#include "exceptions/dbbusyexception.hh"
#include "common/exceptions/logicexception.hh"

#include <boost/thread/shared_lock_guard.hpp>

namespace rasmgr
{

DatabaseHost::DatabaseHost(const std::string &hostName, const std::string &connectString)
    : hostName(hostName), connectString(connectString)
{
    this->sessionCount = 0;
    this->serverCount = 0;
}

void DatabaseHost::addClientSessionOnDB(const std::string &databaseName, std::uint32_t clientId, std::uint32_t sessionId)
{
    boost::upgrade_lock<boost::shared_mutex> lock(databaseListMutex);
    bool foundDb = false;
    for (auto it = this->databaseList.begin(); !foundDb && it != this->databaseList.end(); it++)
    {
        if ((*it)->getDbName() == databaseName)
        {
            foundDb = true;
            //If there already is a session with the given clientId and sessionId
            //on this db, the next line will throw an exception
            //and the counter will not be incremented
            (*it)->addClientSession(clientId, sessionId);

            boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
            this->sessionCount++;
        }
    }

    if (!foundDb)
    {
        throw InexistentDatabaseException(databaseName, "cannot add client session to database.");
    }
}

void DatabaseHost::removeClientSessionFromDB(std::uint32_t clientId, std::uint32_t sessionId)
{
    boost::lock_guard<boost::shared_mutex> lock(this->databaseListMutex);
    for (auto it = this->databaseList.begin(); it != this->databaseList.end(); it++)
    {
        this->sessionCount -= (*it)->removeClientSession(clientId, sessionId);
    }
}

void DatabaseHost::increaseServerCount()
{
    boost::lock_guard<boost::shared_mutex> lock(this->databaseListMutex);
    this->serverCount++;
}

void DatabaseHost::decreaseServerCount()
{
    boost::lock_guard<boost::shared_mutex> lock(this->databaseListMutex);
    if (this->serverCount == 0)
    {
        throw common::LogicException("Cannot decrease server count, as it is already 0.");
    }
    this->serverCount--;
}

bool DatabaseHost::isBusy() const
{
    boost::shared_lock<boost::shared_mutex> lock(this->databaseListMutex);
    return (this->sessionCount > 0 || this->serverCount > 0);
}

bool DatabaseHost::ownsDatabase(const std::string &databaseName)
{
    return this->containsDatabase(databaseName);
}

void DatabaseHost::addDbToHost(std::shared_ptr<Database> db)
{
    if (this->containsDatabase(db->getDbName()))
    {
        throw DatabaseAlreadyExistsException(db->getDbName(), this->getHostName());
    }
    else
    {
        boost::lock_guard<boost::shared_mutex> lock(databaseListMutex);
        this->databaseList.push_back(db);
    }
}

void DatabaseHost::removeDbFromHost(const std::string &dbName)
{
    bool removedDb = false;

    boost::upgrade_lock<boost::shared_mutex> lock(databaseListMutex);

    auto it = this->databaseList.begin();
    while (it != this->databaseList.end())
    {
        if ((*it)->getDbName() == dbName)
        {
            if (!(*it)->isBusy())
            {
                boost::upgrade_to_unique_lock<boost::shared_mutex> uniqueLock(lock);
                it = this->databaseList.erase(it);
                removedDb = true;
            }
            else
            {
                throw DbBusyException((*it)->getDbName(), "cannot remove database from database host " + hostName);
            }
            break;
        }
        else
        {
            ++it;
        }
    }

    if (!removedDb)
    {
        throw InexistentDatabaseException(dbName, "cannot remove it from database host " + hostName);
    }
}

DatabaseHostProto DatabaseHost::serializeToProto(const DatabaseHost &dbHost)
{
    DatabaseHostProto result;

    result.set_host_name(dbHost.hostName);
    result.set_connect_string(dbHost.connectString);

    result.set_session_count(dbHost.sessionCount);
    result.set_server_count(dbHost.serverCount);

    boost::shared_lock<boost::shared_mutex> lock(dbHost.databaseListMutex);
    for (auto it = dbHost.databaseList.begin(); it != dbHost.databaseList.end(); it++)
    {
        result.add_databases()->CopyFrom(Database::serializeToProto(**it));
    }

    return result;
}

const std::string &DatabaseHost::getHostName() const
{
    return this->hostName;
}

void DatabaseHost::setHostName(const std::string &hostName)
{
    if (hostName.empty())
    {
        throw common::LogicException("Cannot set empty hostname for database host.");
    }
    this->hostName = hostName;
}

const std::string &DatabaseHost::getConnectString() const
{
    return this->connectString;
}

void DatabaseHost::setConnectString(const std::string &connectString)
{
    this->connectString = connectString;
}

bool DatabaseHost::containsDatabase(const std::string &dbName)
{
    boost::shared_lock<boost::shared_mutex> lock(databaseListMutex);
    for (auto it = this->databaseList.begin(); it != this->databaseList.end(); it++)
    {
        if ((*it)->getDbName() == dbName)
        {
            return true;
        }
    }
    return false;
}
}  // namespace rasmgr
