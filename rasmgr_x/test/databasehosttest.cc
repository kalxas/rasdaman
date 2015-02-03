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

#include <string>
#include <boost/cstdint.hpp>

#include "../../common/src/unittest/gtest.h"
#include "../../common/src/mock/gmock.h"
#include "../../common/src/logging/easylogging++.hh"

#include "../../rasmgr_x/src/databasehost.hh"
#include "../../rasmgr_x/src/database.hh"

using rasmgr::DatabaseHost;
using rasmgr::Database;

class DatabaseHostTest:public ::testing::Test
{
protected:
    DatabaseHostTest():hostName("hostName"),connectString("connectString"),
            userName("userName"), passwdString("passwdString"),dbName("dbName"),
            dbh(hostName,connectString, userName, passwdString),db(dbName)
    {}

    std::string hostName;
    std::string connectString ;
    std::string userName;
    std::string passwdString;
    std::string dbName;
    DatabaseHost dbh;
    Database db;
};

TEST_F(DatabaseHostTest, preconditions)
{
    ASSERT_EQ(hostName, dbh.getHostName());
    ASSERT_EQ(connectString, dbh.getConnectString());
    ASSERT_EQ(userName, dbh.getUserName());
    ASSERT_EQ(passwdString, dbh.getPasswdString());

    ASSERT_FALSE(dbh.isBusy());
}

TEST_F(DatabaseHostTest, addClientSessionOnDB)
{
    std::string clientId = "clientId";
    std::string sessionId = "sessionId";

    ASSERT_FALSE(dbh.isBusy());

    //Will fail because there is no db on this host
    ASSERT_ANY_THROW(dbh.addClientSessionOnDB(db.getDbName(), clientId, sessionId));

    //Add the db
    ASSERT_NO_THROW(dbh.addDbToHost(db));

    ASSERT_NO_THROW(dbh.addClientSessionOnDB(db.getDbName(), clientId, sessionId));

    //Will fail because there is already a session with this id
    ASSERT_ANY_THROW(dbh.addClientSessionOnDB(db.getDbName(), clientId, sessionId));

    ASSERT_TRUE(dbh.isBusy());
}

TEST_F(DatabaseHostTest, removeClientSessionFromDB)
{
    std::string clientId = "clientId";
    std::string sessionId = "sessionId";

    ASSERT_FALSE(dbh.isBusy());

    ASSERT_NO_THROW(dbh.addDbToHost(db));

    ASSERT_NO_THROW(dbh.addClientSessionOnDB(db.getDbName(), clientId, sessionId));

    ASSERT_NO_THROW(dbh.removeClientSessionFromDB(clientId, sessionId));

    ASSERT_FALSE(dbh.isBusy());
}

TEST_F(DatabaseHostTest, increaseServerCount)
{
    ASSERT_FALSE(dbh.isBusy());

    ASSERT_NO_THROW(dbh.increaseServerCount());

    ASSERT_TRUE(dbh.isBusy());
}

TEST_F(DatabaseHostTest, decreaseServerCount)
{
    ASSERT_FALSE(dbh.isBusy());

    ASSERT_NO_THROW(dbh.increaseServerCount());

    ASSERT_TRUE(dbh.isBusy());

    ASSERT_NO_THROW(dbh.decreaseServerCount());

    ASSERT_FALSE(dbh.isBusy());

    ASSERT_ANY_THROW(dbh.decreaseServerCount());
}

TEST_F(DatabaseHostTest, ownsDatabase)
{
    //Before adding it, the host does not own the db
    ASSERT_FALSE(dbh.ownsDatabase(db.getDbName()));

    ASSERT_NO_THROW(dbh.addDbToHost(db));

    ASSERT_TRUE(dbh.ownsDatabase(db.getDbName()));
}

TEST_F(DatabaseHostTest, addDbToHost)
{
    //Before adding it, the host does not own the db
    ASSERT_FALSE(dbh.ownsDatabase(db.getDbName()));

    ASSERT_NO_THROW(dbh.addDbToHost(db));

    ASSERT_TRUE(dbh.ownsDatabase(db.getDbName()));

    //Addding the same database twice or a database
    //with the same name will cause an exception to be thrown.
    ASSERT_ANY_THROW(dbh.addDbToHost(db));
}

TEST_F(DatabaseHostTest, removeDbFromHost)
{
    //Before adding it, the host does not own the db
    ASSERT_FALSE(dbh.ownsDatabase(db.getDbName()));

    //Removing a database that is not present on the host will not
    //throw an exception
    ASSERT_ANY_THROW(dbh.removeDbFromHost(db.getDbName()));

    ASSERT_NO_THROW(dbh.addDbToHost(db));

    ASSERT_TRUE(dbh.ownsDatabase(db.getDbName()));

    std::string clientId = "clientId";
    std::string sessionId = "sessionId";

    ASSERT_NO_THROW(dbh.addClientSessionOnDB(db.getDbName(), clientId, sessionId));

    //fail because the database is busy
    ASSERT_ANY_THROW(dbh.removeDbFromHost(db.getDbName()));

    ASSERT_NO_THROW(dbh.removeClientSessionFromDB(clientId, sessionId));

    ASSERT_NO_THROW(dbh.removeDbFromHost(db.getDbName()));
    //The database will now be removed
    ASSERT_FALSE(dbh.ownsDatabase(db.getDbName()));
}


TEST_F(DatabaseHostTest, changeDbProperties)
{
    rasmgr::DatabasePropertiesProto properties;
    properties.set_n_name("test");

    ASSERT_ANY_THROW(dbh.changeDbProperties(db.getDbName(), properties));

    dbh.addDbToHost(db);

    std::string clientId = "clientId";
    std::string sessionId = "sessionId";

    ASSERT_NO_THROW(dbh.addClientSessionOnDB(db.getDbName(), clientId, sessionId));

    ASSERT_ANY_THROW(dbh.changeDbProperties(db.getDbName(), properties));

    ASSERT_NO_THROW(dbh.removeClientSessionFromDB(clientId, sessionId));

    ASSERT_NO_THROW(dbh.changeDbProperties(db.getDbName(), properties));
}
