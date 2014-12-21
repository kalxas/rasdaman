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
#include <boost/smart_ptr.hpp>

#include "../../common/src/unittest/gtest.h"
#include "../../common/src/mock/gmock.h"
#include "../../common/src/logging/easylogging++.hh"

#include "../../rasmgr_x/src/database.hh"
#include "../../rasmgr_x/src/databasehost.hh"
#include "../../rasmgr_x/src/databasehostmanager.hh"
#include "../../rasmgr_x/src/databasemanager.hh"

using rasmgr::Database;
using rasmgr::DatabaseHost;
using rasmgr::DatabaseHostManager;
using rasmgr::DatabaseManager;

class DatabaseManagerTest:public ::testing::Test
{
protected:
    DatabaseManagerTest():hostName("hostName"),connectString("connectString"),
            userName("userName"), passwdString("passwdString"),dbName("dbName"),
            db(dbName)
    {
        this->dbhManager.reset(new DatabaseHostManager());
        this->dbManager.reset(new DatabaseManager(this->dbhManager));
    }

    std::string hostName;
    std::string connectString ;
    std::string userName;
    std::string passwdString;
    std::string dbName;
    Database db;
    boost::shared_ptr<DatabaseHostManager> dbhManager;
    boost::shared_ptr<DatabaseManager> dbManager;
};

TEST_F(DatabaseManagerTest, defineDatabase)
{
    //Throw because there is no host
    ASSERT_ANY_THROW(dbManager->defineDatabase(dbName, hostName));

    dbhManager->addNewDatabaseHost(hostName, connectString,userName, passwdString);

    //Succeed
    ASSERT_NO_THROW(dbManager->defineDatabase(dbName, hostName));

    //Fail because of duplication
    ASSERT_ANY_THROW(dbManager->defineDatabase(dbName, hostName));
}

TEST_F(DatabaseManagerTest, changeDatabaseName)
{
    std::string newName =  "newName";
    //Throw because there is no db
    ASSERT_ANY_THROW(dbManager->changeDatabaseName(dbName, newName));

    dbhManager->addNewDatabaseHost(hostName, connectString,userName, passwdString);

    //Succeed
    ASSERT_NO_THROW(dbManager->defineDatabase(dbName, hostName));

    //Succeed
    ASSERT_NO_THROW(dbManager->changeDatabaseName(dbName, newName));

    dbhManager->getDatabaseHost(hostName)->increaseSessionCount(dbName, "test", "test");

    //Fail because the database is busy
    ASSERT_ANY_THROW(dbManager->changeDatabaseName(dbName, newName));

    dbhManager->getDatabaseHost(hostName)->decreaseSessionCount("test", "test");

    //Fail because the database is busy
    ASSERT_NO_THROW(dbManager->changeDatabaseName(dbName, newName));
}

TEST_F(DatabaseManagerTest, removeDatabase)
{
    //Throw because there is no db
    ASSERT_ANY_THROW(dbManager->removeDatabase(dbName));

    dbhManager->addNewDatabaseHost(hostName, connectString,userName, passwdString);

    //Succeed
    ASSERT_NO_THROW(dbManager->defineDatabase(dbName, hostName));

    dbhManager->getDatabaseHost(hostName)->increaseSessionCount(dbName, "test", "test");

    //Fail because the database is busy

    ASSERT_ANY_THROW(dbManager->removeDatabase(dbName));

    dbhManager->getDatabaseHost(hostName)->decreaseSessionCount("test", "test");

    //Succeed
    ASSERT_NO_THROW(dbManager->removeDatabase(dbName));

    //Fail because the database ihas been removed.
    ASSERT_ANY_THROW(dbManager->removeDatabase(dbName));

}
