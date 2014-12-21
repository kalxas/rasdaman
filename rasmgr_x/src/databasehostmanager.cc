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

#include "../../common/src/logging/easylogging++.hh"
#include "databasehostmanager.hh"

namespace rasmgr
{
using std::list;
using boost::shared_ptr;
using boost::unique_lock;
using boost::mutex;
using std::runtime_error;

DatabaseHostManager::DatabaseHostManager()
{}

DatabaseHostManager::~DatabaseHostManager()
{}

void DatabaseHostManager::addNewDatabaseHost(const std::string& dbHostName, const std::string& connectString, const std::string& userName, const std::string& password)
{
    list<shared_ptr<DatabaseHost> >::iterator it;
    bool duplicate=false;

    unique_lock<mutex> lock(this->mut);

    for(it=this->hostList.begin(); it!=this->hostList.end(); ++it)
    {
        if((*it)->getHostName() == dbHostName)
        {
            duplicate=true;
            break;
        }
    }

    if(duplicate)
    {
        throw runtime_error("There already is a database host named:"+dbHostName);
    }
    else
    {
        shared_ptr<DatabaseHost> dbHost(new DatabaseHost(dbHostName, connectString, userName, password));
        this->hostList.push_back(dbHost);
    }
}

void DatabaseHostManager::changeDatabaseHost(const std::string &oldName, const std::string &newName, const std::string &newConnect, const std::string newUserName, const std::string newPassword)
{
    list<shared_ptr<DatabaseHost> >::iterator it;
    bool changed=false;

    unique_lock<mutex> lock(this->mut);

    for(it=this->hostList.begin(); it!=this->hostList.end(); ++it)
    {
        if((*it)->getHostName() == oldName)
        {
            (*it)->setConnectString(newConnect);
            (*it)->setUserName(newUserName);
            (*it)->setPasswdString(newPassword);
            (*it)->setHostName(newName);
            changed=true;
            break;
        }
    }

    if(!changed)
    {
        throw runtime_error("There exist no database host named:"+oldName);
    }
}

void DatabaseHostManager::removeDatabaseHost(const std::string& dbHostName)
{
    bool erased=false;
    list<shared_ptr<DatabaseHost> >::iterator it;

    unique_lock<mutex> lock(this->mut);

    for(it=this->hostList.begin(); it!=this->hostList.end(); ++it)
    {
        if( (*it)->getHostName() == dbHostName)
        {
            if(!(*it)->isBusy())
            {
                this->hostList.erase(it);
                erased=true;
                break;

            }
            else
            {
                throw runtime_error("The database host:"+dbHostName+" is busy.");
            }
        }
    }

    if(!erased)
    {
        throw runtime_error("There exist no database host named:"+dbHostName);
    }
}

boost::shared_ptr<DatabaseHost> DatabaseHostManager::getDatabaseHost(const std::string& dbHostName)
{
    list<shared_ptr<DatabaseHost> >::iterator it;

    unique_lock<mutex> lock(this->mut);

    for(it=this->hostList.begin(); it!=this->hostList.end(); ++it)
    {
        if((*it)->getHostName() == dbHostName)
        {
            return (*it);
        }
    }

    throw runtime_error("There is no database host with the name:"+dbHostName);
}

boost::shared_ptr<DatabaseHost> DatabaseHostManager::getAndLockDH(const std::string &dbHostName)
{
    list<shared_ptr<DatabaseHost> >::iterator it;

    unique_lock<mutex> lock(this->mut);

    for(it=this->hostList.begin(); it!=this->hostList.end(); ++it)
    {
        if((*it)->getHostName() == dbHostName)
        {
            (*it)->increaseServerCount();
            return (*it);
        }
    }

    throw runtime_error("There is no database host with the name:"+dbHostName);
}

std::list<boost::shared_ptr<DatabaseHost> > DatabaseHostManager::getDatabaseHostList() const
{
    return this->hostList;
}


} /* namespace rasmgr */
