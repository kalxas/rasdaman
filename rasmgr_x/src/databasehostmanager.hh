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

#include "rasmgr_x/src/messages/rasmgrmess.pb.h"
#pragma GCC diagnostic ignored "-Woverloaded-virtual"

namespace rasmgr
{

class DatabaseHost;

/**
 * @brief The DatabaseHostManager class Keeps track of a list of database hosts,
 * allows for their properties to be changed and provides thread-safe access to
 * database host instances
 */
class DatabaseHostManager
{
public:
    virtual ~DatabaseHostManager();

    /**
     * @brief addNewDatabaseHost
     * @param newDbHost Configuration information required to define a new database host
     * @throws If the host name is not specified, it throws an exception.
     */
    virtual void defineDatabaseHost(const DatabaseHostPropertiesProto &newDbHost);

    /**
     * @brief changeDatabaseHost Change the properties of the database host identified by
     * oldName with the new values.
     * @param oldName
     * @param newProperties
     */
    virtual void changeDatabaseHost(const std::string &oldName, const DatabaseHostPropertiesProto &newProperties);

    /**
     * @brief removeDatabaseHost Remove the database host identified by the dbHostName from
     * the registry.
     * @param dbHostName
     */
    virtual void removeDatabaseHost(const std::string &dbHostName);

    /**
     * @brief getAndLockDH Get a reference to the database host with the given name
     * if it exists and increase the server count once.
     * This means that the server count MUST be decreased before releasing the reference.
     * This method is used to retrieve a dbhost and make certain that it will not be removed
     * by another thread(e.g. rascontrol)
     * @param dbHostName
     * @return
     */
    virtual boost::shared_ptr<DatabaseHost> getAndLockDatabaseHost(const std::string &dbHostName);

    /**
     * @brief getDatabaseHostList Retrieve a list containing the list of database hosts
     * currently registered with this rasmgr.
     * @return
     */
    virtual std::list<boost::shared_ptr<DatabaseHost>> getDatabaseHostList() const;


    virtual DatabaseHostMgrProto serializeToProto();

private:
    std::list<boost::shared_ptr<DatabaseHost>> hostList;
    boost::mutex mut;
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_DATABASEHOSTMANAGER_HH_ */
