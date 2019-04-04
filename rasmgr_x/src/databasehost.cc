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

#include "common/exceptions/logicexception.hh"

#include "exceptions/rasmgrexceptions.hh"
#include "database.hh"

#include "databasehost.hh"

namespace rasmgr
{
using std::runtime_error;
using boost::mutex;
using boost::unique_lock;

DatabaseHost::DatabaseHost(std::string hostName, std::string connectString,
                           std::string userName, std::string passwdString) :
    hostName(hostName), connectString(connectString),
    userName(userName), passwdString(passwdString)
{
    this->sessionCount = 0;
    this->serverCount = 0;
}

void DatabaseHost::addClientSessionOnDB(const std::string &databaseName, const std::string &clientId, const std::string &sessionId)
{
    unique_lock<mutex> lock(this->mut);

    bool foundDb = false;
    std::list<boost::shared_ptr<Database>>::iterator it;
    for (it = this->databaseList.begin(); !foundDb &&  it != this->databaseList.end(); it++)
    {
        if ((*it)->getDbName() == databaseName)
        {
            foundDb = true;
            //If there already is a session with the given clientId and sessionId
            //on this db, the next line will throw an exception
            //and the counter will not be incremented
            (*it)->addClientSession(clientId, sessionId);
            this->sessionCount++;
        }
    }

    if (!foundDb)
    {
        throw InexistentDatabaseException(databaseName);
    }
}

void DatabaseHost::removeClientSessionFromDB(const std::string &clientId, const std::string &sessionId)
{
    unique_lock<mutex> lock(this->mut);

    std::list<boost::shared_ptr<Database>>::iterator it;
    for (it = this->databaseList.begin(); it != this->databaseList.end(); it++)
    {
        this->sessionCount -= (*it)->removeClientSession(clientId, sessionId);
    }
}

void DatabaseHost::increaseServerCount()
{
    unique_lock<mutex> lock(this->mut);
    this->serverCount++;
}

void DatabaseHost::decreaseServerCount()
{
    unique_lock<mutex> lock(this->mut);
    if (this->serverCount == 0)
    {
        throw common::LogicException("serverCount==0");
    }

    this->serverCount--;
}

bool DatabaseHost::isBusy() const
{
    unique_lock<mutex> lock(this->mut);

    return (this->sessionCount > 0 || this->serverCount > 0);
}

bool DatabaseHost::ownsDatabase(const std::string &databaseName)
{
    unique_lock<mutex> lock(this->mut);

    return this->containsDatabase(databaseName);
}

void DatabaseHost::addDbToHost(boost::shared_ptr<Database> db)
{
    unique_lock<mutex> lock(this->mut);

    if (this->containsDatabase(db->getDbName()))
    {
        throw DatabaseAlreadyExistsException(db->getDbName(), this->getHostName());
    }
    else
    {
        this->databaseList.push_back(db);
    }
}

void DatabaseHost::removeDbFromHost(const std::string &dbName)
{
    std::list<boost::shared_ptr<Database>>::iterator it;
    bool removedDb = false;

    unique_lock<mutex> lock(this->mut);

    for (it = this->databaseList.begin(); !removedDb && it != this->databaseList.end(); it++)
    {
        if ((*it)->getDbName() == dbName)
        {
            if ((*it)->isBusy())
            {
                throw DbBusyException((*it)->getDbName());
            }
            else
            {
                this->databaseList.erase(it);
                removedDb = true;
            }

            break;
        }
    }

    if (!removedDb)
    {
        throw InexistentDatabaseException(dbName);
    }
}

DatabaseHostProto DatabaseHost::serializeToProto(const DatabaseHost &dbHost)
{
    DatabaseHostProto result;

    result.set_host_name(dbHost.hostName);
    result.set_connect_string(dbHost.connectString);
    result.set_user_name(dbHost.userName);
    result.set_password(dbHost.passwdString);

    result.set_session_count(dbHost.sessionCount);
    result.set_server_count(dbHost.serverCount);

    std::list<boost::shared_ptr<Database>>::const_iterator it;

    for (it = dbHost.databaseList.begin(); it != dbHost.databaseList.end(); it++)
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
        throw common::LogicException("hostName.empty()");
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

const std::string &DatabaseHost::getUserName() const
{
    return this->userName;
}

void DatabaseHost::setUserName(const std::string &userName)
{
    if (userName.empty())
    {
        throw common::LogicException("userName.empty()");
    }

    this->userName = userName;
}

const std::string &DatabaseHost::getPasswdString() const
{
    return this->passwdString;
}

void DatabaseHost::setPasswdString(const std::string &passwdString)
{
    this->passwdString = passwdString;
}

bool DatabaseHost::containsDatabase(const std::string &dbName)
{
    std::list<boost::shared_ptr<Database>>::iterator it;

    for (it = this->databaseList.begin(); it != this->databaseList.end(); it++)
    {
        if ((*it)->getDbName() == dbName)
        {
            return true;
        }
    }

    return false;
}
}
