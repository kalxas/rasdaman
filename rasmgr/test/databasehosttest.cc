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

#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <logging.hh>

#include "../src/databasehost.hh"
#include "../src/database.hh"

namespace rasmgr
{
namespace test
{
using rasmgr::Database;
using rasmgr::DatabaseHost;

class DatabaseHostTest : public ::testing::Test
{
protected:
    DatabaseHostTest()
        : hostName("hostName"), connectString("connectString"), dbName("dbName"),
          dbh(hostName, connectString), db(new Database(dbName))
    {
    }

    std::string hostName;
    std::string connectString;
    std::string dbName;
    DatabaseHost dbh;
    std::shared_ptr<Database> db;
};

TEST_F(DatabaseHostTest, preconditions)
{
    ASSERT_EQ(hostName, dbh.getHostName());
    ASSERT_EQ(connectString, dbh.getConnectString());

    ASSERT_FALSE(dbh.isBusy());
}

TEST_F(DatabaseHostTest, addClientSessionOnDbNoDatabase)
{
    std::uint32_t clientId = 1;
    std::uint32_t sessionId = 2;

    ASSERT_FALSE(dbh.isBusy());

    //Will fail because there is no db on this host
    ASSERT_ANY_THROW(dbh.addClientSessionOnDB(db->getDbName(), clientId, sessionId));

    ASSERT_FALSE(dbh.isBusy());
}

TEST_F(DatabaseHostTest, addClientSessionOnDbSuccess)
{
    std::uint32_t clientId = 1;
    std::uint32_t sessionId = 2;

    ASSERT_FALSE(dbh.isBusy());

    //Add the db
    ASSERT_NO_THROW(dbh.addDbToHost(db));
    ASSERT_NO_THROW(dbh.addClientSessionOnDB(db->getDbName(), clientId, sessionId));

    ASSERT_TRUE(dbh.isBusy());
}

TEST_F(DatabaseHostTest, addClientSessionOnDbFailBecauseOfDuplicateSession)
{
    std::uint32_t clientId = 1;
    std::uint32_t sessionId = 2;

    //Add the db
    ASSERT_NO_THROW(dbh.addDbToHost(db));
    ASSERT_NO_THROW(dbh.addClientSessionOnDB(db->getDbName(), clientId, sessionId));

    //Will fail because there is already a session with this id
    ASSERT_ANY_THROW(dbh.addClientSessionOnDB(db->getDbName(), clientId, sessionId));
}

TEST_F(DatabaseHostTest, removeClientSessionFromDB)
{
    std::uint32_t clientId = 1;
    std::uint32_t sessionId = 2;

    //Setup
    ASSERT_NO_THROW(dbh.addDbToHost(db));
    ASSERT_NO_THROW(dbh.addClientSessionOnDB(db->getDbName(), clientId, sessionId));

    //Test
    ASSERT_NO_THROW(dbh.removeClientSessionFromDB(clientId, sessionId));

    //Post conditions
    ASSERT_FALSE(dbh.isBusy());
}

TEST_F(DatabaseHostTest, increaseServerCount)
{
    // Preconditions
    ASSERT_FALSE(dbh.isBusy());

    // Increasing the server count should not throw an exception
    ASSERT_NO_THROW(dbh.increaseServerCount());

    // Postconditions
    ASSERT_TRUE(dbh.isBusy());
}

TEST_F(DatabaseHostTest, decreaseServerCount)
{
    // Preconditions
    ASSERT_FALSE(dbh.isBusy());

    // Setup
    ASSERT_NO_THROW(dbh.increaseServerCount());

    //Must succeed while the server count is positive
    ASSERT_NO_THROW(dbh.decreaseServerCount());

    // Postconditions
    ASSERT_FALSE(dbh.isBusy());

    // Reducing the server count udner zero will throw an exception.
    ASSERT_ANY_THROW(dbh.decreaseServerCount());
}

TEST_F(DatabaseHostTest, ownsDatabase)
{
    //Before adding it, the host does not own the db
    ASSERT_FALSE(dbh.ownsDatabase(db->getDbName()));

    ASSERT_NO_THROW(dbh.addDbToHost(db));

    //After adding it, the host does own the db
    ASSERT_TRUE(dbh.ownsDatabase(db->getDbName()));
}

TEST_F(DatabaseHostTest, addDbToHost)
{
    //Before adding it, the host does not own the db
    ASSERT_FALSE(dbh.ownsDatabase(db->getDbName()));

    ASSERT_NO_THROW(dbh.addDbToHost(db));

    ASSERT_TRUE(dbh.ownsDatabase(db->getDbName()));

    //Addding the same database twice or a database
    //with the same name will cause an exception to be thrown.
    ASSERT_ANY_THROW(dbh.addDbToHost(db));
}

TEST_F(DatabaseHostTest, removeDbFromHostInexistentDatabase)
{
    //Removing a database that is not present on the host will not
    //throw an exception
    ASSERT_ANY_THROW(dbh.removeDbFromHost(db->getDbName()));
}

TEST_F(DatabaseHostTest, removeDbFromHost)
{
    std::uint32_t clientId = 1;
    std::uint32_t sessionId = 2;

    // Setup
    ASSERT_NO_THROW(dbh.addDbToHost(db));
    ASSERT_TRUE(dbh.ownsDatabase(db->getDbName()));

    ASSERT_NO_THROW(dbh.addClientSessionOnDB(db->getDbName(), clientId, sessionId));

    //fail because the database is busy
    ASSERT_ANY_THROW(dbh.removeDbFromHost(db->getDbName()));

    ASSERT_NO_THROW(dbh.removeClientSessionFromDB(clientId, sessionId));

    ASSERT_NO_THROW(dbh.removeDbFromHost(db->getDbName()));

    //The database will now be removed
    ASSERT_FALSE(dbh.ownsDatabase(db->getDbName()));
}
}  // namespace test
}  // namespace rasmgr
