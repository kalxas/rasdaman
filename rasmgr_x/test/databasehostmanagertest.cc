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

#include "../../rasmgr_x/src/database.hh"
#include "../../rasmgr_x/src/databasehost.hh"
#include "../../rasmgr_x/src/databasehostmanager.hh"

using rasmgr::Database;
using rasmgr::DatabaseHost;
using rasmgr::DatabaseHostManager;

class DatabaseHostManagerTest:public ::testing::Test
{
protected:
    DatabaseHostManagerTest():hostName("hostName"),connectString("connectString"),
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
    DatabaseHostManager dbhManager;
};

TEST_F(DatabaseHostManagerTest, constructor)
{
    ASSERT_EQ(0, dbhManager.getDatabaseHostList().size());
}

TEST_F(DatabaseHostManagerTest, addNewDatabaseHost)
{
    boost::shared_ptr<DatabaseHost> dbhResult;
    ASSERT_NO_THROW(dbhManager.addNewDatabaseHost(hostName, connectString, userName, passwdString));

    ASSERT_EQ(1, dbhManager.getDatabaseHostList().size());

    ASSERT_NO_THROW(dbhResult=dbhManager.getDatabaseHost(hostName));

    ASSERT_EQ(hostName, dbhResult->getHostName());
    ASSERT_EQ(connectString, dbhResult->getConnectString());
    ASSERT_EQ(passwdString, dbhResult->getPasswdString());
    ASSERT_EQ(userName, dbhResult->getUserName());

    ASSERT_ANY_THROW(dbhManager.addNewDatabaseHost(hostName, connectString, userName, passwdString));
}

TEST_F(DatabaseHostManagerTest, removeDatabaseHost)
{
    //no database with the given name
    ASSERT_ANY_THROW(dbhManager.removeDatabaseHost(hostName));

    //Add a dbh
    ASSERT_NO_THROW(dbhManager.addNewDatabaseHost(hostName, connectString, userName, passwdString));

    //remove it
    ASSERT_NO_THROW(dbhManager.removeDatabaseHost(hostName));

    //verify that removal was successful
    ASSERT_EQ(0, dbhManager.getDatabaseHostList().size());

    //try to remove it again and fail
    ASSERT_ANY_THROW(dbhManager.removeDatabaseHost(hostName));
}

TEST_F(DatabaseHostManagerTest, changeDHConnectString)
{
    std::string newConnectString = "newConnectString";
    //no database with the given name
    ASSERT_ANY_THROW(dbhManager.changeDHConnectString(hostName, newConnectString));

    //Add a dbh
    ASSERT_NO_THROW(dbhManager.addNewDatabaseHost(hostName, connectString, userName, passwdString));

    //remove it
    ASSERT_NO_THROW(dbhManager.changeDHConnectString(hostName, newConnectString));

    //Test that the change was successful
    ASSERT_EQ(newConnectString, dbhManager.getDatabaseHost(hostName)->getConnectString());
}

TEST_F(DatabaseHostManagerTest, changeDHPassword)
{
    std::string newsPassword = "newPassword";
    //no database with the given name
    ASSERT_ANY_THROW(dbhManager.changeDHPassword(hostName, newsPassword));

    //Add a dbh
    ASSERT_NO_THROW(dbhManager.addNewDatabaseHost(hostName, connectString, userName, passwdString));

    //remove it
    ASSERT_NO_THROW(dbhManager.changeDHPassword(hostName, newsPassword));

    //Test that the change was successful
    ASSERT_EQ(newsPassword, dbhManager.getDatabaseHost(hostName)->getPasswdString());
}

TEST_F(DatabaseHostManagerTest, changeDHUser)
{
    std::string newUser = "newUser";
    //no database with the given name
    ASSERT_ANY_THROW(dbhManager.changeDHUser(hostName, newUser));

    //Add a dbh
    ASSERT_NO_THROW(dbhManager.addNewDatabaseHost(hostName, connectString, userName, passwdString));

    //remove it
    ASSERT_NO_THROW(dbhManager.changeDHUser(hostName, newUser));

    //Test that the change was successful
    ASSERT_EQ(newUser, dbhManager.getDatabaseHost(hostName)->getUserName());
}

TEST_F(DatabaseHostManagerTest, getDatabaseHost)
{
    boost::shared_ptr<DatabaseHost> dbhResult;

    ASSERT_ANY_THROW(dbhManager.getDatabaseHost(hostName));

    //Add a dbh
    ASSERT_NO_THROW(dbhManager.addNewDatabaseHost(hostName, connectString, userName, passwdString));

    ASSERT_NO_THROW(dbhResult=dbhManager.getDatabaseHost(hostName));

    ASSERT_FALSE(dbhResult->isBusy());
}

TEST_F(DatabaseHostManagerTest, getAndLockDH)
{
    boost::shared_ptr<DatabaseHost> dbhResult;
    ASSERT_ANY_THROW(dbhManager.getAndLockDH(hostName));

    //Add a dbh
    ASSERT_NO_THROW(dbhManager.addNewDatabaseHost(hostName, connectString, userName, passwdString));

    ASSERT_NO_THROW(dbhResult=dbhManager.getAndLockDH(hostName));

    ASSERT_TRUE(dbhResult->isBusy());
}

TEST_F(DatabaseHostManagerTest, getDatabaseHostList)
{
    ASSERT_EQ(0, dbhManager.getDatabaseHostList().size());

    ASSERT_NO_THROW(dbhManager.addNewDatabaseHost(hostName, connectString, userName, passwdString));

    ASSERT_EQ(1, dbhManager.getDatabaseHostList().size());
}
