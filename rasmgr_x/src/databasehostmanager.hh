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

#ifndef RASMGR_X_SRC_DATABASEHOSTMANAGER_HH_
#define RASMGR_X_SRC_DATABASEHOSTMANAGER_HH_

#include <list>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>

#include "databasehost.hh"

namespace rasmgr
{

class DatabaseHostManager
{
public:
    DatabaseHostManager();

    virtual ~DatabaseHostManager();

    /**
     * Insert a new database host into the list.
     * @param hostName Name of the database host
     * @param connectString String used to connect to the host
     * @param userName User name
     * @param password Password associated with the user
     */
    void addNewDatabaseHost(const std::string& dbHostName,
                            const std::string& connectString, const std::string& userName,
                            const std::string& password);

    /**
     * @brief changeDatabaseHost Change the properties of the database host identified by
     * oldName with the new values. If you do not want to change a value, assign it the old value i.e.
     * retrieve a reference to the DatabaseHost object, get its configuration parameters,
     * change the ones you desire and apply the changes.
     * @param oldName
     * @param newName
     * @param newConnect
     * @param newUserName
     * @param newPassword
     */
    void changeDatabaseHost(const std::string& oldName, const std::string& newName,
                            const std::string& newConnect, const std::string newUserName,
                            const std::string newPassword);


    void removeDatabaseHost(const std::string& dbHostName);

    /**
     * @brief getDatabaseHost Get a shared reference to the database host identified
     * by dbHostName
     * @param dbHostName
     * @return
     * @throws std::exception An exception is thrown if there is no database host with this name
     */
    boost::shared_ptr<DatabaseHost> getDatabaseHost(const std::string& dbHostName);

    /**
     * @brief getAndLockDH Get a reference to the database host with the given name
     * if it exists and increase the server count once.
     * This means that the server count MUST be decreased before releasing the reference.
     * This method is used to retrieve a dbhost and make certain that it will not be removed
     * by another thread.
     * @param dbHostName
     * @return
     */
    boost::shared_ptr<DatabaseHost> getAndLockDH(const std::string& dbHostName);

    std::list<boost::shared_ptr<DatabaseHost> > getDatabaseHostList() const;

private:
    std::list< boost::shared_ptr<DatabaseHost> > hostList;
    boost::mutex mut;

};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_DATABASEHOSTMANAGER_HH_ */
