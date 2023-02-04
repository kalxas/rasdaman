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

#ifndef RASMGR_X_SRC_DATABASEMANAGER_HH_
#define RASMGR_X_SRC_DATABASEMANAGER_HH_

#include "rasmgr/src/messages/rasmgrmess.pb.h"

#include <string>
#include <list>
#include <memory>
#include <mutex>

namespace rasmgr
{

class DatabaseHostManager;
class Database;

/**
 * Keeps track of the databases available on this rasmgr instance. There can be
 * exactly one database with a given name at any moment.
 */
class DatabaseManager
{
public:
    /**
     * @param dbHostManager Reference to the database host manager with which
     * this object is associated.
     */
    explicit DatabaseManager(std::shared_ptr<DatabaseHostManager> dbHostManager);

    virtual ~DatabaseManager() = default;

    /**
     * Define a new database if there is no other database with the same name and
     * if there is a database host with the provided name
     * @param databaseName Name of the database that will be created
     * @param dbHostName Name of the database host on which this database will reside
     */
    void defineDatabase(const std::string &dbHostName, const std::string &databaseName);

    /**
     * Change the name of a database if there is no database with the same name.
     * @param oldDbName The old name of the database
     * @param newDbProp new database properties
     * @throws InexistentDatabaseException
     */
    void changeDatabase(const std::string &oldDbName, const DatabasePropertiesProto &newDbProp);

    /**
     * Remove the database with the given name from the list.
     * If no database with the given name exists, nothing will happen.
     * @param databaseHostName name of the database host from which to remove this database
     * @param databaseName name of the database to be removed
     * @throws InexistentDatabaseException
     */
    void removeDatabase(const std::string &databaseHostName, const std::string &databaseName);
    
    const std::list<std::shared_ptr<Database>> &getDatabases() const;
    
    const std::shared_ptr<DatabaseHostManager> &getDbHostManager() const;

    /**
     * Serialize the information this object holds in a snapshot.
     */
    DatabaseMgrProto serializeToProto();
private:
    std::shared_ptr<DatabaseHostManager> dbHostManager; /*!< Reference to the database host manager*/
    std::list<std::shared_ptr<Database>> databases;

    std::mutex mut;  /*!< Mutex used to synchronize access to this object.*/
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_DATABASEMANAGER_HH_ */
