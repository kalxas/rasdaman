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
#include <utility>
#include <set>

#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <logging.hh>

#include "mocks/serverfactorymock.hh"
#include "mocks/databasehostmgrmock.hh"
#include "mocks/mockrasserver.hh"
#include "util/testutil.hh"

#include "rasnet/messages/rassrvr_rasmgr_service.pb.h"
#include "common/exceptions/invalidargumentexception.hh"

#include "../src/server.hh"
#include "../src/servergroup.hh"
#include "../src/databasehostmanager.hh"
#include "../src/rasmgrconfig.hh"
#include "../src/constants.hh"

namespace rasmgr
{
namespace test
{
using ::testing::AtLeast;                     // #1
using ::testing::_;
using ::testing::Return;
using ::testing::ReturnRef;

using rasnet::service::RasServerService;
using rasmgr::ServerGroup;
using rasmgr::RasMgrConfig;
using rasmgr::test::TestUtil;
using rasmgr::ServerGroupConfigProto;
using rasmgr::ServerFactory;

class ServerGroupTest: public ::testing::Test
{
protected:
    ServerGroupTest()
    {
        this->dbHostManager.reset(new DatabaseHostManagerMock());
        this->serverFactory.reset(new ServerFactoryMock());
        this->dbHost.reset(new rasmgr::DatabaseHost("dbHost", "connect", "", ""));
        this->server.reset(new MockRasServer());
    }

    std::shared_ptr<rasmgr::DatabaseHost> dbHost;
    std::shared_ptr<rasmgr::ServerFactory> serverFactory;
    std::shared_ptr<rasmgr::DatabaseHostManager> dbHostManager;
    std::shared_ptr<rasmgr::Server> server;
};


TEST_F(ServerGroupTest, constructorValidation)
{
    // Use random values so that all the possible combinations
    // are tested throughout time
    bool hasName = TestUtil::randomBool();
    bool hasHost = TestUtil::randomBool();
    bool hasDbHost = TestUtil::randomBool();
    bool hasPorts = TestUtil::randomBool();
    bool hasAliveServers = TestUtil::randomBool();
    bool hasAvailableServers = TestUtil::randomBool();
    bool hasMaxServers = TestUtil::randomBool();
    bool hasAutoRestart = TestUtil::randomBool();
    bool hasSessionCount = TestUtil::randomBool();

    ServerGroupConfigProto groupConfig;

    if (hasName)
    {
        groupConfig.set_name("groupName");
    }

    if (hasHost)
    {
        groupConfig.set_host("host");
    }

    if (hasDbHost)
    {
        groupConfig.set_db_host("dbHost");
    }

    if (hasPorts)
    {
        groupConfig.add_ports(2034);
    }

    if (hasAliveServers || hasAvailableServers)
    {
        groupConfig.set_min_alive_server_no(20);
        for (size_t i = 0; i < 20; ++i) {
            groupConfig.add_ports(2035 + uint32_t(i));
        }
    }

    if (hasAvailableServers || hasAliveServers)
    {
        groupConfig.set_min_available_server_no(10);
    }

    if (hasMaxServers)
    {
        groupConfig.set_max_idle_server_no(21);
    }

    if (hasAutoRestart)
    {
        groupConfig.set_autorestart(TestUtil::randomBool());
    }

    if (hasSessionCount)
    {
        groupConfig.set_countdown(123);
    }


    ServerGroup* group = nullptr;

    // If one of these properties is not set, an exception shoulb be thrown
    if (!hasName || !hasHost || !hasDbHost || !hasPorts)
    {
        ASSERT_ANY_THROW(group = new ServerGroup(groupConfig, this->dbHostManager, this->serverFactory));
    }
    else
    {
        //The server count will be decreased in the destructor of the server group
        //so it is neccessary to increase it
        this->dbHost->increaseServerCount();
        DatabaseHostManagerMock& dbhManager = *std::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
        EXPECT_CALL(dbhManager, getAndLockDatabaseHost(_)).WillOnce(Return(this->dbHost));

        ASSERT_NO_THROW(group = new ServerGroup(groupConfig, this->dbHostManager, this->serverFactory));
//        try {
//            group = new ServerGroup(groupConfig, this->dbHostManager, this->serverFactory);
//        }
//        catch (common::InvalidArgumentException &ex) {
//            LERROR << "exception: " << ex.what();
//        }
        ASSERT_TRUE(group->isStopped());

        ASSERT_EQ(groupConfig.name(), group->getConfig().name());
        ASSERT_EQ(groupConfig.host(), group->getConfig().host());
        ASSERT_EQ(groupConfig.db_host(), group->getConfig().db_host());
        ASSERT_EQ(groupConfig.ports_size(), group->getConfig().ports_size());
        ASSERT_EQ(rasmgr::STARTING_SERVER_LIFETIME, group->getConfig().starting_server_lifetime());

        if (hasAliveServers || hasAvailableServers)
        {
            ASSERT_EQ(groupConfig.min_alive_server_no(), group->getConfig().min_alive_server_no());
        }
        else
        {
            ASSERT_EQ(rasmgr::MIN_ALIVE_SERVER_NO, group->getConfig().min_alive_server_no());
        }

        if (hasAvailableServers || hasAliveServers)
        {
            ASSERT_EQ(groupConfig.min_available_server_no(), group->getConfig().min_available_server_no());
        }
        else
        {
            ASSERT_EQ(rasmgr::MIN_AVAILABLE_SERVER_NO, group->getConfig().min_available_server_no());
        }

        if (hasMaxServers)
        {
            ASSERT_EQ(groupConfig.max_idle_server_no(), group->getConfig().max_idle_server_no());
        }
        else
        {
            ASSERT_EQ(rasmgr::MAX_IDLE_SERVER_NO, group->getConfig().max_idle_server_no());
        }

        if (hasAutoRestart)
        {
            ASSERT_EQ(groupConfig.autorestart(), group->getConfig().autorestart());
        }
        else
        {
            ASSERT_EQ(rasmgr::AUTORESTART_SERVER, group->getConfig().autorestart());
        }

        if (hasSessionCount)
        {
            ASSERT_EQ(groupConfig.countdown(), group->getConfig().countdown());
        }
        else
        {
            ASSERT_EQ(rasmgr::MAX_SERVER_SESSIONS, group->getConfig().countdown());
        }


        delete group;
    }
}


TEST_F(ServerGroupTest, start)
{
    std::int32_t runningPort = 2034;
    //The minimum number of alive servers will be created
    //The created servers will be started
    //The servers will be stopped in the destructor
    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *std::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDatabaseHost(_)).WillOnce(Return(this->dbHost));

    std::string serverId = "serverId";
    MockRasServer& serverRef = *std::dynamic_pointer_cast<MockRasServer>(this->server);
    EXPECT_CALL(serverRef, startProcess());
    EXPECT_CALL(serverRef, getServerId()).WillOnce(ReturnRef(serverId));
    EXPECT_CALL(serverRef, stop(_));

    ServerFactoryMock& factoryRef = *std::dynamic_pointer_cast<ServerFactoryMock>(this->serverFactory);
    EXPECT_CALL(factoryRef, createServer(_)).WillOnce(Return(this->server));

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name("name");
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(2034);
    groupConfig.set_starting_server_lifetime(1);

    ServerGroup group(groupConfig, this->dbHostManager, this->serverFactory);

    ASSERT_TRUE(group.isStopped());

    group.start();

    ASSERT_FALSE(group.isStopped());
}

TEST_F(ServerGroupTest, tryRegisterServer)
{
    std::uint32_t runningPort = 2034;
    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *std::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDatabaseHost(_)).WillOnce(Return(this->dbHost));

    std::string serverId = "serverId";
    MockRasServer& serverRef = *std::dynamic_pointer_cast<MockRasServer>(this->server);
    EXPECT_CALL(serverRef, startProcess());
    EXPECT_CALL(serverRef, getServerId()).WillRepeatedly(ReturnRef(serverId));
    EXPECT_CALL(serverRef, registerServer(serverId));


    ServerFactoryMock& factoryRef = *std::dynamic_pointer_cast<ServerFactoryMock>(this->serverFactory);
    EXPECT_CALL(factoryRef, createServer(_)).WillOnce(Return(this->server));

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name("name");
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(runningPort);
    groupConfig.set_starting_server_lifetime(1);

    ServerGroup group(groupConfig, this->dbHostManager, this->serverFactory);

    //The group is not started
    ASSERT_ANY_THROW(group.tryRegisterServer(serverId));

    group.start();
    //There is a server with this id and registerServer is called on the object.
    ASSERT_TRUE(group.tryRegisterServer(serverId));

    //No server with that ID
    ASSERT_FALSE(group.tryRegisterServer("randomId"));
}

//Try to stop a server group that is already stopped
TEST_F(ServerGroupTest, stopFailure)
{
    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *std::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDatabaseHost(_)).WillOnce(Return(this->dbHost));

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name("name");
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(2034);
    groupConfig.set_starting_server_lifetime(1);

    ServerGroup group(groupConfig, this->dbHostManager, this->serverFactory);

    //If the server group is not started, nothing bad should happen.
    KillLevel force = FORCE;
    ASSERT_ANY_THROW(group.stop(force));
}

TEST_F(ServerGroupTest, stop)
{
    KillLevel force = FORCE;
    std::uint32_t runningPort = 2035;

    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *std::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDatabaseHost(_)).WillOnce(Return(this->dbHost));

    std::string runningServerId = "runningServerId";
    MockRasServer& runningServerRef = *std::dynamic_pointer_cast<MockRasServer>(this->server);
    EXPECT_CALL(runningServerRef, startProcess());
    EXPECT_CALL(runningServerRef, registerServer(runningServerId));
    EXPECT_CALL(runningServerRef, getServerId()).WillRepeatedly(ReturnRef(runningServerId));
    //The server will be stopped
    //Because the server is stopped isAlive will return false.
    EXPECT_CALL(runningServerRef, isAlive()).WillOnce(Return(false));

    ServerFactoryMock& factoryRef = *std::dynamic_pointer_cast<ServerFactoryMock>(this->serverFactory);
    EXPECT_CALL(factoryRef, createServer(_)).WillOnce(Return(this->server));

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name("name");
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(runningPort);
    groupConfig.set_min_alive_server_no(1);
    groupConfig.set_min_available_server_no(1);
    groupConfig.set_starting_server_lifetime(1);

    ServerGroup group(groupConfig, this->dbHostManager, this->serverFactory);

    group.start();
    //Register the running server
    group.tryRegisterServer(runningServerId);

    group.stop(force);
}

TEST_F(ServerGroupTest, evaluateServerGroupNoStart)
{
    std::uint32_t startingPort = 2034;
    std::uint32_t runningPort = 2035;

    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *std::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDatabaseHost(_)).WillOnce(Return(this->dbHost));

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name("name");
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(startingPort);
    groupConfig.add_ports(runningPort);
    groupConfig.set_starting_server_lifetime(1);

    ServerGroup group(groupConfig, this->dbHostManager, this->serverFactory);

    ASSERT_NO_THROW(group.evaluateServerGroup());
}

// TODO

//TEST_F(ServerGroupTest, evaluateServerGroup)
//{
//    std::uint32_t runningServerPort = 20034;
//    std::uint32_t maxSessions = 5;

//    // The server count of the database host would be increased by the dbh manager
//    this->dbHost->increaseServerCount();
//    DatabaseHostManagerMock& dbhManager = *std::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
//    EXPECT_CALL(dbhManager, getAndLockDatabaseHost(_)).WillOnce(Return(this->dbHost));

//    // Setup a server that is running correctly
//    std::string runningServerId = "runningServerId";
//    MockRasServer& runningServerRef = *std::dynamic_pointer_cast<MockRasServer>(this->server);
//    EXPECT_CALL(runningServerRef, startProcess());
//    EXPECT_CALL(runningServerRef, registerServer(runningServerId));
//    EXPECT_CALL(runningServerRef, getTotalSessionNo()).WillOnce(Return(0));
//    EXPECT_CALL(runningServerRef, getServerId()).WillRepeatedly(ReturnRef(runningServerId));
//    EXPECT_CALL(runningServerRef, isAlive()).WillRepeatedly(Return(true));
//    EXPECT_CALL(runningServerRef, stop(KILL));

//    std::string deadServerId = "deadServerId";
//    std::shared_ptr<rasmgr::Server> deadServer(new MockRasServer());
//    MockRasServer& deadServerRef = *std::dynamic_pointer_cast<MockRasServer>(deadServer);
//    EXPECT_CALL(deadServerRef, startProcess());
//    EXPECT_CALL(deadServerRef, registerServer(deadServerId));
//    EXPECT_CALL(deadServerRef, getServerId()).WillRepeatedly(ReturnRef(deadServerId));
//    //Because the server is stopped isAlive will return false.
//    EXPECT_CALL(deadServerRef, isAlive()).WillOnce(Return(false));
//    EXPECT_CALL(deadServerRef, stop(KILL));

//    std::string fullServerId = "fullServerId";
//    std::shared_ptr<rasmgr::Server> fullServer(new MockRasServer());
//    MockRasServer& fullServerRef = *std::dynamic_pointer_cast<MockRasServer>(fullServer);
//    EXPECT_CALL(fullServerRef, startProcess());
//    EXPECT_CALL(fullServerRef, registerServer(fullServerId));
//    EXPECT_CALL(fullServerRef, getServerId()).WillRepeatedly(ReturnRef(fullServerId));
//    EXPECT_CALL(fullServerRef, isAlive()).WillOnce(Return(true));
//    EXPECT_CALL(fullServerRef, stop(KILL));

//    ServerFactoryMock& factoryRef = *std::dynamic_pointer_cast<ServerFactoryMock>(this->serverFactory);
//    EXPECT_CALL(factoryRef, createServer(_))
//    .WillOnce(Return(this->server))
//    .WillOnce(Return(deadServer))
//    .WillOnce(Return(fullServer));

//    ServerGroupConfigProto groupConfig;
//    groupConfig.set_name("name");
//    groupConfig.set_host("host");
//    groupConfig.set_db_host("dbHost");
//    groupConfig.add_ports(runningServerPort);
//    groupConfig.set_countdown(maxSessions);
//    groupConfig.set_min_alive_server_no(1);
//    groupConfig.set_min_available_server_no(1);
//    groupConfig.set_starting_server_lifetime(1);

//    ServerGroup group(groupConfig, this->dbHostManager, this->serverFactory);
    
//    {
//        group.start();
//        group.tryRegisterServer(runningServerId);
//        ASSERT_NO_THROW(group.evaluateServerGroup());
//        group.stop(KILL);
//    }
//    {
//        group.start();
//        group.tryRegisterServer(deadServerId);
//        ASSERT_NO_THROW(group.evaluateServerGroup());
//    }
//    {
//        group.start();
//        group.tryRegisterServer(fullServerId);
//        ASSERT_NO_THROW(group.evaluateServerGroup());
//        group.stop(KILL);
//    }
//}

}
}
