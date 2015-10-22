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

#ifndef RASMGR_X_SRC_DATABASEHOST_HH
#define RASMGR_X_SRC_DATABASEHOST_HH

#include <string>

#include <boost/thread.hpp>

#include "messages/rasmgrmess.pb.h"

namespace rasmgr
{

class Database;

/**
 * @brief The DatabaseHost class A database host manages multiple databases,
 * keeps track of servers using this database host
 */
class DatabaseHost
{
public:
    /**
     * @brief DatabaseHost Initialize a new instance of the DatabaseHost object.
     * @param hostName Name of the database host, the machine on which the database will run.
     * @param connectString The string that will be used to connect to the database
     * @param userName The user name used for secure connection to the database
     * @param passwdString The password string associated with the user name
     */
    DatabaseHost(std::string hostName, std::string connectString,
                 std::string userName, std::string passwdString);

    /**
     * @brief addClientSessionOnDB Increase the number of sessions running
     * on the given database
     * @throws An exception is thrown if this host does not contain a database
     */
    void addClientSessionOnDB(const std::string& databaseName, const std::string& clientId, const std::string& sessionId);

    /**
     * @brief removeClientSessionFromDB Decrease the number of sessions running
     * on the given database
     */
    void removeClientSessionFromDB(const std::string& clientId, const std::string& sessionId);

    /**
     * @brief increaseServerCount Increase the number of servers using this host.
     */
    void increaseServerCount();

    /**
     * @brief decreaseServerCount Decrease the number of servers using this host.
     * @throws An exception is thrown if the server count cannot be further decreased.
     */
    void decreaseServerCount();

    /**
     * @brief isBusy Check if the database host is busy with any servers
     * @return TRUE if there are servers assigned to this host
     * or if there are client sessions assigned to this DH, FALSE otherwise
     */
    bool isBusy() const;

    /**
     * Check if the database identified by databaseName is present on this host.
     * The database might be removed between this call and the moment an
     * operation is performed on the database if locking is not performed at
     * a higher level.
     * @param databaseName
     * @return TRUE if the database with the given name is on this host, FALSE otherwise
     */
    bool ownsDatabase(const std::string& databaseName);

    /**
     * Add the database to this host.
     * @param db
     */
    void addDbToHost(boost::shared_ptr<Database> db);

    /**
     * Remove the database with the given name from this host.
     * @param dbName
     */
    void removeDbFromHost(const std::string& dbName);

    /**
     * @brief serializeToProto
     * @param dbHost
     * @return Serialized representation of this DatabaseHost
     */
    static DatabaseHostProto serializeToProto(const DatabaseHost& dbHost);

    const std::string& getHostName() const;
    void setHostName(const std::string& hostName);

    const std::string& getConnectString() const;
    void setConnectString(const std::string& connectString);

    const std::string& getUserName() const;
    void setUserName(const std::string& userName);

    const std::string& getPasswdString() const;
    void setPasswdString(const std::string& passwdString);

private:
    std::string hostName; /*!< Name of this database host */
    std::string connectString; /*!< String used to connect to this database host*/
    std::string userName; /*!< User name used to connect to the database host*/
    std::string passwdString; /*!< Password string used to connect to the database host*/

    int sessionCount; /*!< Counter used to track the number of active sessions*/
    int serverCount;/*!< Counter used to track the number of server groups using this host*/
    std::list<boost::shared_ptr<Database> > databaseList;/*!< List of databases located on this host */
    mutable boost::mutex mut;/*!< Mutex used for syncrhonizing access to this object*/

    /**
     * Check if this host contains the database identified by the given name.
     * @param dbName
     * @return
     */
    bool containsDatabase(const std::string& dbName);
};

}

#endif // DATABASEHOST_HH
