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

#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <logging.hh>

#include "../../rasmgr_x/src/database.hh"

namespace rasmgr
{
namespace test
{
TEST(DatabaseTest, addClientSessionSuccess)
{
    using rasmgr::Database;

    Database db("dbName");
    std::string clientId = "client";
    std::string sessionId = "session";
    std::string sessionId2 = "session2";

    // Test preconditions
    ASSERT_FALSE(db.isBusy());

    ASSERT_NO_THROW(db.addClientSession(clientId, sessionId));

    ASSERT_TRUE(db.isBusy());

    //Adding another session will not cause failure
    ASSERT_NO_THROW(db.addClientSession(clientId, sessionId2));
}

TEST(DatabaseTest, addClientSessionFailWhenDuplicateIsAdded)
{
    using rasmgr::Database;

    Database db("dbName");
    std::string clientId = "client";
    std::string sessionId = "session";

    ASSERT_FALSE(db.isBusy());

    ASSERT_NO_THROW(db.addClientSession(clientId, sessionId));

    //Will throw because there already is a session with these IDs
    ASSERT_ANY_THROW(db.addClientSession(clientId, sessionId));
}

TEST(DatabaseTest, isBusyReturnsFalseWhenThereAreNoSessions)
{
    using rasmgr::Database;

    Database db("dbName");

    ASSERT_FALSE(db.isBusy());
}

TEST(DatabaseTest, isBusyReturnsTrueWhenThereIsAtLeastOneSessions)
{
    using rasmgr::Database;

    Database db("dbName");
    std::string clientId = "client";
    std::string sessionId = "session";

    //Add a client sessions and the state of the database should become busy
    ASSERT_NO_THROW(db.addClientSession(clientId, sessionId));


    ASSERT_TRUE(db.isBusy());
}

TEST(DatabaseTest, removeClientSession)
{
    using rasmgr::Database;

    Database db("dbName");
    std::string clientId = "client";
    std::string sessionId = "session";

    //Initial state
    ASSERT_FALSE(db.isBusy());

    //Increase the session count and then decrease it
    ASSERT_NO_THROW(db.addClientSession(clientId, sessionId));

    ASSERT_TRUE(db.isBusy());

    int deletedSessionsCount;
    ASSERT_NO_THROW(deletedSessionsCount = db.removeClientSession(clientId, sessionId));

    ASSERT_EQ(1, deletedSessionsCount);

    ASSERT_FALSE(db.isBusy());
}

TEST(DatabaseTest, serializeToProto)
{
    std::string dbName = "dbName";
    std::string clientId = "client";
    std::string sessionId = "session";

    Database db(dbName);
    db.addClientSession(clientId, sessionId);

    DatabaseProto proto = Database::serializeToProto(db);

    ASSERT_EQ(dbName, proto.name());
    ASSERT_EQ(1, proto.sessions_size());
    ASSERT_EQ(clientId, proto.sessions(0).first());
    ASSERT_EQ(sessionId, proto.sessions(0).second());
}

TEST(DatabaseTest, setDbNameDbIsNotBusy)
{
    std::string dbName = "dbName";
    std::string newName = "newName";

    Database db(dbName);

    // Will succeed when the db is not busy
    ASSERT_FALSE(db.isBusy());
    ASSERT_NO_THROW(db.setDbName(newName));
    ASSERT_EQ(newName, db.getDbName());
}

TEST(DatabaseTest, setDbNameDbIsBusy)
{
    std::string dbName = "dbName";
    std::string newName = "newName";
    std::string clientId = "client";
    std::string sessionId = "session";

    Database db(dbName);
    db.addClientSession(clientId, sessionId);

    // Will succeed when the db is not busy
    ASSERT_TRUE(db.isBusy());
    ASSERT_ANY_THROW(db.setDbName(newName));
}
}
}
