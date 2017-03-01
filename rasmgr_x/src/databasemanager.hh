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

#include <string>
#include <list>

#include <boost/shared_ptr.hpp>
#include <boost/thread/mutex.hpp>

#include "rasmgr_x/src/messages/rasmgrmess.pb.h"

namespace rasmgr
{

class DatabaseHostManager;
class Database;

/**
 * @brief The DatabaseManager class Keeps track of the databases available on
 * this rasmgr instance. There can be exactly one database with a given name at any moment.
 */
class DatabaseManager
{
public:
    /**
     * Initialize a new instance of the DatabaseManager class.
     * @param dbHostManager Reference to the database host manager with which this object is associated.
     */
    DatabaseManager(boost::shared_ptr<DatabaseHostManager> dbHostManager);

    virtual ~DatabaseManager();

    /**
     * Define a new database if there is no other database with the same name and
     * if there is a database host with the provided name
     * @param databaseName Name of the database that will be created
     * @param dbHostName Name of the database host on which this database will reside
     */
    void defineDatabase(const std::string& dbHostName, const std::string& databaseName);

    /**
     * Change the name of a database if there is no database with the same name.
     * @param oldDbName The old name of the database
     * @param newDbName The new name of the database
     */
    void changeDatabase(const std::string& oldDbName, const DatabasePropertiesProto& newDbProp);

    /**
     * Remove the database with the given name from the list.
     * If no database with the given name exists, nothing will happen.
     * @param databaseHostName name of the database host from which to remove this database
     * @param databaseName
     */
    void removeDatabase(const std::string& databaseHostName, const std::string& databaseName);

    /**
     * @brief serializeToProto Serialize the information this object holds in a snapshot.
     * @return
     */
    DatabaseMgrProto serializeToProto();
private:
    boost::shared_ptr<DatabaseHostManager> dbHostManager; /*!< Reference to the database host manager*/
    std::list<boost::shared_ptr<Database>> databases;

    boost::mutex mut;  /*!< Mutex used to synchronize access to this object.*/
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_DATABASEMANAGER_HH_ */
