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

#include <logging.hh>

#include "exceptions/rasmgrexceptions.hh"
#include "database.hh"
#include "databasehost.hh"
#include "databasehostmanager.hh"

#include "databasemanager.hh"

namespace rasmgr
{
using std::shared_ptr;
using std::runtime_error;
using std::list;
using std::mutex;
using std::lock_guard;

DatabaseManager::DatabaseManager(std::shared_ptr<DatabaseHostManager> m) : dbHostManager(m)
{}

void DatabaseManager::defineDatabase(const std::string &dbHostName,
                                     const std::string &databaseName)
{
    lock_guard<mutex> lock(this->mut);

    //Get and lock access to the database host
    auto dbHost = this->dbHostManager->getAndLockDatabaseHost(dbHostName);
    //Release the lock as we do not care if the database host is removed in between
    dbHost->decreaseServerCount();

    //Check if there already is a database with this name in the list
    std::shared_ptr<Database> db{nullptr};
    for (auto it = this->databases.begin(); it != this->databases.end(); ++it)
    {
        if ((*it)->getDbName() == databaseName)
        {
            db = (*it);
            break;
        }
    }

    //Create new database if it does not exist
    if (!db)
    {
        db.reset(new Database(databaseName));
    }

    //Try to add it to the host
    dbHost->addDbToHost(db);

    //If adding to the host was successful, add it to the list of active dbs
    databases.push_back(db);
    
    LDEBUG << "Added database \"" + databaseName + "\" to database host name \"" + dbHostName + "\"";
}

void DatabaseManager::changeDatabase(const std::string &oldDbName, const DatabasePropertiesProto &newDbProp)
{
    lock_guard<mutex> lock(this->mut);
    bool changedDb = false;

    //Check if there already is a database with this name in the list
    for (auto it = this->databases.begin(); it != this->databases.end(); ++it)
    {
        if ((*it)->getDbName() == oldDbName)
        {
            if ((*it)->isBusy())
            {
                throw DbBusyException((*it)->getDbName(), "cannot change database.");
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
        throw InexistentDatabaseException(oldDbName, "cannot change database.");
    }
}

void DatabaseManager::removeDatabase(const std::string &dbHostName, const std::string &databaseName)
{
    lock_guard<mutex> lock(this->mut);

    //Get and lock access to the database host
    auto dbHost = this->dbHostManager->getAndLockDatabaseHost(dbHostName);
    //Release the lock as we do not care if the database host is removed in between
    dbHost->decreaseServerCount();
    dbHost->removeDbFromHost(databaseName);

    databases.remove_if([&databaseName](const std::shared_ptr<Database> &db) {
      return db->getDbName() == databaseName;
    });
    
    LDEBUG << "Removed database \"" + databaseName + "\" from database host name \"" + dbHostName + "\"";
}

const std::list<std::shared_ptr<Database> > &DatabaseManager::getDatabases() const
{
    return databases;
}

const std::shared_ptr<DatabaseHostManager> &DatabaseManager::getDbHostManager() const
{
    return dbHostManager;
}

DatabaseMgrProto DatabaseManager::serializeToProto()
{
    lock_guard<mutex> lock(this->mut);

    auto dbhList = this->dbHostManager->getDatabaseHostList();
    
    DatabaseMgrProto result;
    for (auto it = dbhList.begin(); it != dbhList.end(); ++it)
    {
        DatabaseHostProto dbhProto = DatabaseHost::serializeToProto(*(*it));
        for (int i = 0; i < dbhProto.databases_size(); i++)
        {
            DatabaseMgrProto::DbAndDbHostPair *p =  result.add_databases();
            DatabaseProto *dbProto = new DatabaseProto();
            dbProto->CopyFrom(dbhProto.databases(i));
            p->set_database_host(dbhProto.host_name());
            p->set_allocated_database(dbProto);
        }
    }

    return result;
}

} /* namespace rasmgr */
