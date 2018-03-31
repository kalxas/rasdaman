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

#include <logging.hh>

#include "exceptions/rasmgrexceptions.hh"
#include "database.hh"
#include "databasehost.hh"
#include "databasehostmanager.hh"

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

void DatabaseManager::defineDatabase(const std::string& dbHostName,
                                     const std::string& databaseName)
{
    unique_lock<mutex> lock(this->mut);

    //Get and lock access to the database host
    boost::shared_ptr<DatabaseHost> dbHost = this->dbHostManager->getAndLockDatabaseHost(dbHostName);
    //Release the lock as we do not care if the database host is removed in between
    dbHost->decreaseServerCount();

    boost::shared_ptr<Database> db;
    bool dbExists = false;

    //Check if there already is a database with this name in the list
    for (std::list<boost::shared_ptr<Database>>::iterator it = this->databases.begin();
            it != this->databases.end(); ++it)
    {
        if ((*it)->getDbName() == databaseName)
        {
            db = (*it);
            dbExists  = true;
            break;
        }
    }

    //Create new database if it does not exist
    if (!dbExists)
    {
        db.reset(new Database(databaseName));
    }

    //Try to add it to the host
    dbHost->addDbToHost(db);

    //If adding to the host was successful, add it to the list of active dbs
    databases.push_back(db);
}

void DatabaseManager::changeDatabase(const std::string& oldDbName, const DatabasePropertiesProto& newDbProp)
{
    unique_lock<mutex> lock(this->mut);
    bool changedDb = false;

    //Check if there already is a database with this name in the list
    for (std::list<boost::shared_ptr<Database>>::iterator it = this->databases.begin();
            it != this->databases.end(); ++it)
    {
        if ((*it)->getDbName() == oldDbName)
        {
            if ((*it)->isBusy())
            {
                throw DbBusyException((*it)->getDbName());
            }
            else
            {
                if (newDbProp.has_n_name())
                {
                    (*it)->setDbName(newDbProp.n_name());
                }

                changedDb = true;
                break;
            }
        }
    }

    if (!changedDb)
    {
        throw InexistentDatabaseException(oldDbName);
    }
}

void DatabaseManager::removeDatabase(const std::string& databaseHostName, const std::string& databaseName)
{
    unique_lock<mutex> lock(this->mut);

    //Get and lock access to the database host
    boost::shared_ptr<DatabaseHost> dbHost = this->dbHostManager->getAndLockDatabaseHost(databaseHostName);
    //Release the lock as we do not care if the database host is removed in between
    dbHost->decreaseServerCount();

    dbHost->removeDbFromHost(databaseName);

    LDEBUG << "Removed database \"" + databaseName + "\" from database host name \"" + databaseHostName + "\"";

    for (std::list<boost::shared_ptr<Database>>::iterator it = this->databases.begin();
            it != this->databases.end(); ++it)
    {
        if ((*it)->getDbName() == databaseName)
        {
            LDEBUG << "Removed database from list of active databases.";
            this->databases.remove(*it);

            break;
        }
    }
}

DatabaseMgrProto DatabaseManager::serializeToProto()
{
    DatabaseMgrProto result;

    unique_lock<mutex> lock(this->mut);

    list<shared_ptr<DatabaseHost>> dbhList = this->dbHostManager->getDatabaseHostList();

    for (list<shared_ptr<DatabaseHost>>::iterator it = dbhList.begin(); it != dbhList.end(); ++it)
    {
        DatabaseHostProto dbhProto = DatabaseHost::serializeToProto(*(*it));

        for (int i = 0; i < dbhProto.databases_size(); i++)
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
