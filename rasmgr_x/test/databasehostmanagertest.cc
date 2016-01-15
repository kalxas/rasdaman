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
#include <easylogging++.h>
#include "util/testutil.hh"

#include "../../rasmgr_x/src/database.hh"
#include "../../rasmgr_x/src/databasehost.hh"
#include "../../rasmgr_x/src/databasehostmanager.hh"
#include "../src/messages/rasmgrmess.pb.h"

namespace rasmgr
{
namespace test
{
using rasmgr::Database;
using rasmgr::DatabaseHost;
using rasmgr::DatabaseHostManager;
using rasmgr::DatabaseHostPropertiesProto;
using rasmgr::DatabaseHostMgrProto;

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

TEST_F(DatabaseHostManagerTest, preconditions)
{
    ASSERT_EQ(0, dbhManager.getDatabaseHostList().size());
}

TEST_F(DatabaseHostManagerTest, defineDatabaseHost)
{
    DatabaseHostPropertiesProto properties;

    //The operation will fail because the host_name of properties is not set.
    ASSERT_ANY_THROW(dbhManager.defineDatabaseHost(properties));

    properties.set_host_name(hostName);
    properties.set_user_name(userName);
    properties.set_password(passwdString);
    properties.set_connect_string(connectString);

    ASSERT_NO_THROW(dbhManager.defineDatabaseHost(properties));

    ASSERT_EQ(1, dbhManager.getDatabaseHostList().size());

    //Get the first database and see if the result was valid.
    boost::shared_ptr<DatabaseHost> dbhResult = dbhManager.getDatabaseHostList().front();

    ASSERT_EQ(hostName, dbhResult->getHostName());
    ASSERT_EQ(connectString, dbhResult->getConnectString());
    ASSERT_EQ(passwdString, dbhResult->getPasswdString());
    ASSERT_EQ(userName, dbhResult->getUserName());

    //Will fail because there already is a database host with the given host name
    ASSERT_ANY_THROW(dbhManager.defineDatabaseHost(properties));
}

TEST_F(DatabaseHostManagerTest, changeDbHostProperties)
{
    DatabaseHostPropertiesProto properties;

    if(rasmgr::test::TestUtil::randomBool())
    {
        properties.set_connect_string("newConnectString");
    }

    if(rasmgr::test::TestUtil::randomBool())
    {
        properties.set_host_name("newHostName");
    }

    if(rasmgr::test::TestUtil::randomBool())
    {
        properties.set_password("newPass");
    }

    if(rasmgr::test::TestUtil::randomBool())
    {
        properties.set_user_name("newUserName");
    }

    //no database with the given name
    ASSERT_ANY_THROW(dbhManager.changeDatabaseHost(hostName, properties));

    DatabaseHostPropertiesProto originalDBH;
    originalDBH.set_host_name(hostName);
    originalDBH.set_user_name(userName);
    originalDBH.set_password(passwdString);
    originalDBH.set_connect_string(connectString);

    //Add a dbh
    ASSERT_NO_THROW(dbhManager.defineDatabaseHost(originalDBH));

    ASSERT_NO_THROW(dbhManager.changeDatabaseHost(hostName, properties));

    boost::shared_ptr<DatabaseHost> dbhResult = dbhManager.getDatabaseHostList().front();

    if(properties.has_connect_string())
    {
        ASSERT_EQ(properties.connect_string(),dbhResult->getConnectString());
    }
    else
    {
        ASSERT_EQ(originalDBH.connect_string(),dbhResult->getConnectString());
    }

    if(properties.has_host_name())
    {
        ASSERT_EQ(properties.host_name(), dbhResult->getHostName());
    }
    else
    {
        ASSERT_EQ(originalDBH.host_name(), dbhResult->getHostName());
    }

    if(properties.has_password())
    {
        ASSERT_EQ(properties.password(), dbhResult->getPasswdString());
    }
    else
    {
        ASSERT_EQ(originalDBH.password(), dbhResult->getPasswdString());
    }

    if(properties.has_user_name())
    {
        ASSERT_EQ(properties.user_name(), dbhResult->getUserName());
    }
    else
    {
        ASSERT_EQ(originalDBH.user_name(), dbhResult->getUserName());
    }
}

TEST_F(DatabaseHostManagerTest, removeDatabaseHostFailWhenInexistendDbHost)
{
    //no database with the given name
    ASSERT_ANY_THROW(dbhManager.removeDatabaseHost(hostName));
}

TEST_F(DatabaseHostManagerTest, removeDatabaseHost)
{
    DatabaseHostPropertiesProto properties;
    properties.set_host_name(hostName);
    properties.set_user_name(userName);
    properties.set_password(passwdString);
    properties.set_connect_string(connectString);
    //Add a dbh
    ASSERT_NO_THROW(dbhManager.defineDatabaseHost(properties));

    //remove it
    ASSERT_NO_THROW(dbhManager.removeDatabaseHost(hostName));

    //verify that removal was successful
    ASSERT_EQ(0, dbhManager.getDatabaseHostList().size());

    //try to remove it again and fail
    ASSERT_ANY_THROW(dbhManager.removeDatabaseHost(hostName));
}

TEST_F(DatabaseHostManagerTest, getAndLockDHFailsWhenNoDbHost)
{
    boost::shared_ptr<DatabaseHost> dbhResult;
    ASSERT_ANY_THROW(dbhManager.getAndLockDatabaseHost(hostName));
}

TEST_F(DatabaseHostManagerTest, getAndLockDH)
{
    boost::shared_ptr<DatabaseHost> dbhResult;

    DatabaseHostPropertiesProto originalDBH;
    originalDBH.set_host_name(hostName);
    originalDBH.set_user_name(userName);
    originalDBH.set_password(passwdString);
    originalDBH.set_connect_string(connectString);

    //Setup
    ASSERT_NO_THROW(dbhManager.defineDatabaseHost(originalDBH));
    ASSERT_FALSE(dbhManager.getDatabaseHostList().front()->isBusy());

    // will succeed
    ASSERT_NO_THROW(dbhResult=dbhManager.getAndLockDatabaseHost(hostName));

    // because the dbh is blocked
    ASSERT_TRUE(dbhResult->isBusy());
}

TEST_F(DatabaseHostManagerTest, getDatabaseHostList)
{
    ASSERT_EQ(0, dbhManager.getDatabaseHostList().size());

    DatabaseHostPropertiesProto originalDBH;
    originalDBH.set_host_name(hostName);
    originalDBH.set_user_name(userName);
    originalDBH.set_password(passwdString);
    originalDBH.set_connect_string(connectString);

    //Add a dbh
    ASSERT_NO_THROW(dbhManager.defineDatabaseHost(originalDBH));

    ASSERT_EQ(1, dbhManager.getDatabaseHostList().size());
}

TEST_F(DatabaseHostManagerTest, serializeToProto)
{
    DatabaseHostMgrProto result = dbhManager.serializeToProto();
    ASSERT_EQ(0, result.database_hosts_size());

    DatabaseHostPropertiesProto originalDBH;
    originalDBH.set_host_name(hostName);
    originalDBH.set_user_name(userName);
    originalDBH.set_password(passwdString);
    originalDBH.set_connect_string(connectString);

    dbhManager.defineDatabaseHost(originalDBH);

    result =  dbhManager.serializeToProto();
    ASSERT_EQ(1, result.database_hosts_size());

    ASSERT_EQ(hostName, result.database_hosts(0).host_name());
    ASSERT_EQ(userName, result.database_hosts(0).user_name());
    ASSERT_EQ(passwdString, result.database_hosts(0).password());
    ASSERT_EQ(connectString, result.database_hosts(0).connect_string());
}

}
}
