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

#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <easylogging++.h>

#include "../../rasmgr_x/src/database.hh"
#include "../../rasmgr_x/src/databasehost.hh"
#include "../../rasmgr_x/src/databasehostmanager.hh"
#include "../../rasmgr_x/src/databasemanager.hh"
namespace rasmgr
{
namespace test
{
using rasmgr::Database;
using rasmgr::DatabaseHost;
using rasmgr::DatabaseHostManager;
using rasmgr::DatabaseManager;
using rasmgr::DatabaseHostPropertiesProto;
using rasmgr::DatabasePropertiesProto;
using rasmgr::DatabaseMgrProto;

class DatabaseManagerTest:public ::testing::Test
{
protected:
    DatabaseManagerTest():hostName("hostName"),connectString("connectString"),
        userName("userName"), passwdString("passwdString"),dbName("dbName"),
        db(new Database(dbName))
    {
        this->dbhManager.reset(new DatabaseHostManager());
        this->dbManager.reset(new DatabaseManager(this->dbhManager));
    }

    std::string hostName;
    std::string connectString ;
    std::string userName;
    std::string passwdString;
    std::string dbName;
    boost::shared_ptr<Database> db;
    boost::shared_ptr<DatabaseHostManager> dbhManager;
    boost::shared_ptr<DatabaseManager> dbManager;
};

TEST_F(DatabaseManagerTest, defineDatabaseFailBecauseThereIsNoHost)
{
    //Throw because there is no host
    ASSERT_ANY_THROW(dbManager->defineDatabase(dbName, hostName));
}

TEST_F(DatabaseManagerTest, defineDatabaseSuccess)
{
    DatabaseHostPropertiesProto proto;
    proto.set_host_name(hostName);
    proto.set_connect_string(connectString);
    proto.set_user_name(userName);
    proto.set_password(passwdString);
    dbhManager->defineDatabaseHost(proto);

    //Succeed
    ASSERT_NO_THROW(dbManager->defineDatabase(hostName, dbName));
}

TEST_F(DatabaseManagerTest, defineDatabaseFailsWhenDoneTwice)
{
    DatabaseHostPropertiesProto proto;
    proto.set_host_name(hostName);
    proto.set_connect_string(connectString);
    proto.set_user_name(userName);
    proto.set_password(passwdString);
    dbhManager->defineDatabaseHost(proto);

    //Succeed
    ASSERT_NO_THROW(dbManager->defineDatabase(hostName, dbName));
    //Fail because of duplication
    ASSERT_ANY_THROW(dbManager->defineDatabase(hostName, dbName));
}

TEST_F(DatabaseManagerTest, defineDatabaseWithSameNameOnTwoHosts)
{
    DatabaseHostPropertiesProto proto;
    proto.set_host_name(hostName);
    proto.set_connect_string(connectString);
    proto.set_user_name(userName);
    proto.set_password(passwdString);
    dbhManager->defineDatabaseHost(proto);

    std::string secondHost = "secondHost";
    proto.set_host_name(secondHost);
    dbhManager->defineDatabaseHost(proto);

    //Succeed
    ASSERT_NO_THROW(dbManager->defineDatabase(hostName, dbName));
    //succeed because defining the same database on multiple hosts works
    ASSERT_NO_THROW(dbManager->defineDatabase(secondHost, dbName));
}

TEST_F(DatabaseManagerTest, changeDatabaseNameWhenDatabaseDoesNotExist)
{
    std::string newName =  "newName";
    DatabasePropertiesProto dbProperties;
    dbProperties.set_n_name(newName);

    //Throw because there is no db
    ASSERT_ANY_THROW(dbManager->changeDatabase(dbName, dbProperties));

}

TEST_F(DatabaseManagerTest, changeDatabaseName)
{
    std::string newName =  "newName";
    DatabasePropertiesProto dbProperties;
    dbProperties.set_n_name(newName);

    DatabaseHostPropertiesProto proto;
    proto.set_host_name(hostName);
    proto.set_connect_string(connectString);
    proto.set_user_name(userName);
    proto.set_password(passwdString);

    //Define the database host
    dbhManager->defineDatabaseHost(proto);

    //Succeed
    ASSERT_NO_THROW(dbManager->defineDatabase(hostName, dbName));

    //Succeed
    ASSERT_NO_THROW(dbManager->changeDatabase(dbName, dbProperties));

    DatabaseMgrProto dbMgrData =  dbManager->serializeToProto();

    ASSERT_EQ(1, dbMgrData.databases_size());
    ASSERT_EQ(newName, dbMgrData.databases(0).database().name());
    ASSERT_EQ(hostName, dbMgrData.databases(0).database_host());
}

TEST_F(DatabaseManagerTest, removeDatabase)
{
    //Throw because there is no db
    ASSERT_ANY_THROW(dbManager->removeDatabase(hostName, dbName));

    DatabaseHostPropertiesProto proto;
    proto.set_host_name(hostName);
    proto.set_connect_string(connectString);
    proto.set_user_name(userName);
    proto.set_password(passwdString);

    //Define the database host
    dbhManager->defineDatabaseHost(proto);

    //Succeed
    ASSERT_NO_THROW(dbManager->defineDatabase(hostName, dbName));

    //Succeed
    ASSERT_NO_THROW(dbManager->removeDatabase(hostName, dbName));

    //Fail because the database ihas been removed.
    ASSERT_ANY_THROW(dbManager->removeDatabase(hostName, dbName));
}

TEST_F(DatabaseManagerTest, serializeToProto)
{
    DatabaseHostPropertiesProto proto;
    proto.set_host_name(hostName);
    proto.set_connect_string(connectString);
    proto.set_user_name(userName);
    proto.set_password(passwdString);

    //Define the database host
    dbhManager->defineDatabaseHost(proto);

    //Succeed
    ASSERT_NO_THROW(dbManager->defineDatabase(hostName, dbName));

    DatabaseMgrProto dbMgrData =  dbManager->serializeToProto();

    ASSERT_EQ(1, dbMgrData.databases_size());
    ASSERT_EQ(dbName, dbMgrData.databases(0).database().name());
    ASSERT_EQ(hostName, dbMgrData.databases(0).database_host());
}

}
}
