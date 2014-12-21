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

#include <boost/thread/locks.hpp>

#include "../../common/src/logging/easylogging++.hh"

#include "databasemanager.hh"

namespace rasmgr
{
using boost::shared_ptr;
using std::runtime_error;
using std::list;
using boost::mutex;
using boost::unique_lock;

DatabaseManager::DatabaseManager(
    boost::shared_ptr<DatabaseHostManager> dbHostManager) :
        dbHostManager(dbHostManager)
{}

DatabaseManager::~DatabaseManager()
{}

void DatabaseManager::defineDatabase(const std::string& databaseName,
                                     const std::string& dbHostName)
{

    unique_lock<mutex> lock(this->mut);

    list<shared_ptr<DatabaseHost> > dbhList=this->dbHostManager->getDatabaseHostList();

    for(list<shared_ptr<DatabaseHost> >::iterator it=dbhList.begin(); it!=dbhList.end(); ++it)
    {
        if((*it)->ownsDatabase(databaseName))
        {
            throw runtime_error(
                "There already exists a database with the name: "
                + databaseName);
        }
    }

    //Get the host for this new database
    shared_ptr<DatabaseHost> dbHost = this->dbHostManager->getDatabaseHost(
                                          dbHostName);

    //Insert the new entry in the database
    dbHost->addDbToHost(Database(databaseName));
}

void DatabaseManager::changeDatabaseName(const std::string& oldDbName,
        const std::string& newDbName)
{
    bool changed=false;
    unique_lock<mutex> lock(this->mut);

    list<shared_ptr<DatabaseHost> > dbhList=this->dbHostManager->getDatabaseHostList();

    for(list<shared_ptr<DatabaseHost> >::iterator it=dbhList.begin(); it!=dbhList.end(); ++it)
    {
        if((*it)->ownsDatabase(oldDbName))
        {
            if((*it)->getDatabase(oldDbName).isBusy())
            {
                throw runtime_error("The database \""+oldDbName+"\" cannot be modified while it has open transactions.");
            }
            else
            {
                (*it)->getDatabase(oldDbName).setDbName(newDbName);
                changed=true;
            }
        }
    }

    if(!changed)
    {
        throw runtime_error("There is no database:\""+oldDbName+"\"");
    }
}

void DatabaseManager::removeDatabase(const std::string& databaseName)
{
    list<shared_ptr<Database> >::iterator it;
    bool removed=false;

    unique_lock<mutex> lock(this->mut);

    list<shared_ptr<DatabaseHost> > dbhList=this->dbHostManager->getDatabaseHostList();

    for(list<shared_ptr<DatabaseHost> >::iterator it=dbhList.begin(); it!=dbhList.end(); ++it)
    {
        if((*it)->ownsDatabase(databaseName))
        {
            if((*it)->getDatabase(databaseName).isBusy())
            {
                throw runtime_error("The database \""+databaseName+"\" could not be removed because it has open transactions.");
            }
            else
            {
                (*it)->removeDbFromHost(databaseName);
                removed=true;
                break;
            }
        }
    }

    if(!removed)
    {
        throw runtime_error("There is no database with the name:"+databaseName);
    }
}

std::list<Database> DatabaseManager::getDatabaseList()
{
    std::list<Database> result;

    unique_lock<mutex> lock(this->mut);

    list<shared_ptr<DatabaseHost> > dbhList=this->dbHostManager->getDatabaseHostList();

    for(list<shared_ptr<DatabaseHost> >::iterator it=dbhList.begin(); it!=dbhList.end(); ++it)
    {
        std::list<Database> aux=(*it)->getDatabaseList();
        result.insert(result.begin(), aux.begin(), aux.end());
    }

    return result;
}

Database DatabaseManager::getDatabase(const std::string& dbName)
{
    unique_lock<mutex> lock(this->mut);

    list<shared_ptr<DatabaseHost> > dbhList=this->dbHostManager->getDatabaseHostList();

    for(list<shared_ptr<DatabaseHost> >::iterator it=dbhList.begin(); it!=dbhList.end(); ++it)
    {
        if((*it)->ownsDatabase(dbName))
        {
            return (*it)->getDatabase(dbName);
        }
    }

    throw runtime_error("There is no database with the name:"+dbName);
}
} /* namespace rasmgr */
