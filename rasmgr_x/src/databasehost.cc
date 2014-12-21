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

void DatabaseHost::increaseSessionCount(const std::string& databaseName, const std::string& clientId, const std::string& sessionId)
{
    unique_lock<mutex> lock(this->mut);

    std::list<Database>::iterator it;
    for(it=this->databaseList.begin(); it!=this->databaseList.end(); it++)
    {
        if(it->getDbName() == databaseName)
        {
            it->increaseSessionCount(clientId,sessionId);
            this->sessionCount++;
            return;
        }
    }

    throw runtime_error("The host does not contain database:\""+databaseName+"\"");
}

void DatabaseHost::decreaseSessionCount(const std::string& clientId, const std::string& sessionId)
{
    unique_lock<mutex> lock(this->mut);

    std::list<Database>::iterator it;
    for(it=this->databaseList.begin(); it!=this->databaseList.end(); it++)
    {
        this->sessionCount-=it->decreaseSessionCount(clientId, sessionId);
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
    this->serverCount--;
}

bool DatabaseHost::isBusy() const
{
    unique_lock<mutex> lock(this->mut);

    return (this->sessionCount>0 || this->serverCount>0);
}

bool DatabaseHost::ownsDatabase(const std::string& databaseName)
{
    unique_lock<mutex> lock(this->mut);

    return this->containsDatabase(databaseName);
}

void DatabaseHost::addDbToHost(const Database& db)
{
    std::list<Database>::iterator it;

    unique_lock<mutex> lock(this->mut);

    if(this->containsDatabase(db.getDbName()))
    {
        throw runtime_error("The database is already on this host.");
    }
    else
    {
        this->databaseList.push_back(db);
    }
}

void DatabaseHost::removeDbFromHost(const std::string& dbName)
{
    std::list<Database>::iterator it;

    unique_lock<mutex> lock(this->mut);

    for(it=this->databaseList.begin(); it!=this->databaseList.end(); it++)
    {
        if(it->getDbName() == dbName)
        {
            this->databaseList.erase(it);
            break;
        }
    }
}

const std::string& DatabaseHost::getHostName() const
{
    return this->hostName;
}

void DatabaseHost::setHostName(const std::string& hostName)
{
    this->hostName = hostName;
}

const std::string& DatabaseHost::getConnectString() const
{
    return this->connectString;
}

void DatabaseHost::setConnectString(const std::string& connectString)
{
    this->connectString=connectString;
}

const std::string& DatabaseHost::getUserName() const
{
    return this->userName;
}

void DatabaseHost::setUserName(const std::string& userName)
{
    this->userName = userName;
}

const std::string& DatabaseHost::getPasswdString() const
{
    return this->passwdString;
}

void DatabaseHost::setPasswdString(const std::string& passwdString)
{
    this->passwdString=passwdString;
}

Database DatabaseHost::getDatabase(const std::string& dbName)
{
    std::list<Database>::iterator it;

    unique_lock<mutex> lock(this->mut);
    for(it=this->databaseList.begin(); it!=this->databaseList.end(); it++)
    {
        if(it->getDbName() == dbName)
        {
            return (*it);
        }
    }

    throw runtime_error("There is no database named:"+dbName+" on this host.");
}

bool DatabaseHost::containsDatabase(const std::string& dbName)
{
    std::list<Database>::iterator it;

    for(it=this->databaseList.begin(); it!=this->databaseList.end(); it++)
    {
        if(it->getDbName() == dbName)
        {
            return true;
        }
    }

    return false;
}

std::list<Database> DatabaseHost::getDatabaseList()
{
    return this->databaseList;
}

}
