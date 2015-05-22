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


#ifndef RASMGR_X_SRC_DATABASE_HH_
#define RASMGR_X_SRC_DATABASE_HH_

#include <string>
#include <set>
#include <utility>

#include "messages/rasmgrmess.pb.h"

namespace rasmgr
{
/**
 * @brief The Database class Represents a database hosted by this rasmgr.
 * A database can have multiple client sessions opened.
 */
class Database
{
public:
    /**
     * Initialize a new instance of the Database class with the given name.
     * This object is not thread safe.
     * @param dbName Name of the database that will uniquely identify it.
     */
    Database(const std::string& dbName);

    virtual ~Database();

    /**
     * When a server requests this database, the transaction count
     * MUST be increased.
     * This allows for preventing the removal of the database while it still has running
     * transactions.
     */
    void addClientSession(const std::string& clientId, const std::string& sessionId);

    /**
     * @brief removeClientSession
     * @param clientId
     * @param sessionId
     * @return The number of sessions removed.
     */
    int removeClientSession(const std::string& clientId, const std::string& sessionId);

    /**
     * Check if there are running transactions on this database
     * @return TRUE if there is at least one transaction running on this database,
     * FALSE otherwise
     */
    bool isBusy() const;

    /**
     * @brief serializeToProto Serialize the information related to the
     * given database so that the information can be saved to a file
     * or transfered to the user.
     * @param db
     * @return
     */
    static DatabaseProto serializeToProto(const Database& db);

    const std::string& getDbName() const;

    void setDbName(const std::string &value);

private:
    std::string dbName; /*!< Name of this database */
    std::set<std::pair<std::string, std::string> > sessionList; /* List of <clientId,sessionId> pairs representing open sessions on the db*/
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_DATABASE_HH_ */
