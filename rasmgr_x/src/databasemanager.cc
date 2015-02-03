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

    shared_ptr<DatabaseHost> dbHost;
    bool foundHost = false;

    list<shared_ptr<DatabaseHost> > dbhList = this->dbHostManager->getDatabaseHostList();
    list<shared_ptr<DatabaseHost> >::iterator it=dbhList.begin();

    while(it!=dbhList.end())
    {
        if((*it)->ownsDatabase(databaseName))
        {
            throw runtime_error("There already exists a database named: \""
                                + databaseName+"\"");
        }

        if((*it)->getHostName() == dbHostName)
        {
            foundHost = true;
            dbHost = (*it);
        }

        it++;
    }

    if(foundHost)
    {
        //Insert the new entry in the database
        dbHost->addDbToHost(Database(databaseName));
    }
    else
    {
        throw runtime_error("There is no database host named \""
                            + databaseName+"\" defined.");
    }
}

void DatabaseManager::changeDatabase(const std::string &oldDbName, const DatabasePropertiesProto &newDbProp)
{
    unique_lock<mutex> lock(this->mut);

    list<shared_ptr<DatabaseHost> > dbhList=this->dbHostManager->getDatabaseHostList();
    bool changed=false;

    for(list<shared_ptr<DatabaseHost> >::iterator it=dbhList.begin(); it!=dbhList.end(); ++it)
    {
        if((*it)->ownsDatabase(oldDbName))
        {
            (*it)->changeDbProperties(oldDbName, newDbProp);
            changed=true;

            break;
        }
    }

    if(!changed)
    {
        throw runtime_error("There is no database named:\""+oldDbName+"\"");
    }
}

void DatabaseManager::removeDatabase(const std::string& databaseName)
{
    unique_lock<mutex> lock(this->mut);

    list<shared_ptr<DatabaseHost> > dbhList=this->dbHostManager->getDatabaseHostList();
    bool removed=false;

    for(list<shared_ptr<DatabaseHost> >::iterator it=dbhList.begin(); it!=dbhList.end(); ++it)
    {
        if((*it)->ownsDatabase(databaseName))
        {
            (*it)->removeDbFromHost(databaseName);
            removed=true;

            break;
        }
    }

    if(!removed)
    {
        throw runtime_error("There is no database named: \""+databaseName+"\" on this manager.");
    }
}

DatabaseMgrProto DatabaseManager::serializeToProto()
{
    DatabaseMgrProto result;

    unique_lock<mutex> lock(this->mut);

    list<shared_ptr<DatabaseHost> > dbhList=this->dbHostManager->getDatabaseHostList();

    for(list<shared_ptr<DatabaseHost> >::iterator it=dbhList.begin(); it!=dbhList.end(); ++it)
    {
        DatabaseHostProto dbhProto = DatabaseHost::serializeToProto( *(*it));

        for(int i=0; i<dbhProto.databases_size(); i++)
        {
            DatabaseMgrProto::DbAndDbHostPair* p =  result.add_databases();

            DatabaseProto* dbProto = new DatabaseProto();
            dbProto->CopyFrom(dbhProto.databases(i));

            p->set_database_host(dbhProto.host_name());
            p->set_allocated_database(dbProto);
        }
    }

    return result;
}

} /* namespace rasmgr */
