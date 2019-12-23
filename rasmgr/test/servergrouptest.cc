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

#include "../src/serverrasnet.hh"
#include "../src/servergroupimpl.hh"
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

using rasmgr::ServerRasNet;
using rasnet::service::RasServerService;
using rasmgr::ServerGroupImpl;
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
            groupConfig.add_ports(2035 + i);
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


    ServerGroupImpl* group;

    // If one of these properties is not set, an exception shoulb be thrown
    if (!hasName || !hasHost || !hasDbHost || !hasPorts)
    {
        ASSERT_ANY_THROW(group = new ServerGroupImpl(groupConfig, this->dbHostManager, this->serverFactory));
    }
    else
    {
        //The server count will be decreased in the destructor of the server group
        //so it is neccessary to increase it
        this->dbHost->increaseServerCount();
        DatabaseHostManagerMock& dbhManager = *std::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
        EXPECT_CALL(dbhManager, getAndLockDatabaseHost(_)).WillOnce(Return(this->dbHost));

        ASSERT_NO_THROW(group = new ServerGroupImpl(groupConfig, this->dbHostManager, this->serverFactory));
//        try {
//            group = new ServerGroupImpl(groupConfig, this->dbHostManager, this->serverFactory);
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
    EXPECT_CALL(serverRef, getServerId()).WillOnce(Return(serverId));
    EXPECT_CALL(serverRef, stop(_));
    EXPECT_CALL(serverRef, getPort()).WillOnce(Return(runningPort));

    ServerFactoryMock& factoryRef = *std::dynamic_pointer_cast<ServerFactoryMock>(this->serverFactory);
    EXPECT_CALL(factoryRef, createServer(_)).WillOnce(Return(this->server));

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name("name");
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(2034);
    groupConfig.set_starting_server_lifetime(1);

    ServerGroupImpl group(groupConfig, this->dbHostManager, this->serverFactory);

    ASSERT_TRUE(group.isStopped());

    group.start();

    ASSERT_FALSE(group.isStopped());
}

TEST_F(ServerGroupTest, tryRegisterServer)
{
    std::int32_t runningPort = 2034;
    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *std::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDatabaseHost(_)).WillOnce(Return(this->dbHost));

    std::string serverId = "serverId";
    MockRasServer& serverRef = *std::dynamic_pointer_cast<MockRasServer>(this->server);
    EXPECT_CALL(serverRef, startProcess());
    EXPECT_CALL(serverRef, getServerId()).WillOnce(Return(serverId));
    EXPECT_CALL(serverRef, stop(_));
    EXPECT_CALL(serverRef, getPort()).WillOnce(Return(runningPort));
    EXPECT_CALL(serverRef, registerServer(serverId));


    ServerFactoryMock& factoryRef = *std::dynamic_pointer_cast<ServerFactoryMock>(this->serverFactory);
    EXPECT_CALL(factoryRef, createServer(_)).WillOnce(Return(this->server));

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name("name");
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(runningPort);
    groupConfig.set_starting_server_lifetime(1);

    ServerGroupImpl group(groupConfig, this->dbHostManager, this->serverFactory);

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

    ServerGroupImpl group(groupConfig, this->dbHostManager, this->serverFactory);

    //If the server group is not started, nothing bad should happen.
    KillLevel force = FORCE;
    ASSERT_ANY_THROW(group.stop(force));
}

TEST_F(ServerGroupTest, stop)
{
    KillLevel force = FORCE;
    std::int32_t startingPort = 2034;
    std::int32_t runningPort = 2035;

    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *std::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDatabaseHost(_)).WillOnce(Return(this->dbHost));


    std::string runningServerId = "runningServerId";
    MockRasServer& runningServerRef = *std::dynamic_pointer_cast<MockRasServer>(this->server);
    EXPECT_CALL(runningServerRef, startProcess());
    EXPECT_CALL(runningServerRef, registerServer(runningServerId));
    EXPECT_CALL(runningServerRef, getServerId()).WillRepeatedly(Return(runningServerId));
    //The server will be stopped
    //Because the server is stopped isAlive will return false.
    EXPECT_CALL(runningServerRef, isAlive()).WillOnce(Return(false));
    //This will return the port used by the server
    EXPECT_CALL(runningServerRef, getPort()).WillOnce(Return(runningPort));

    std::string startingServerId = "startingServerId";
    std::shared_ptr<rasmgr::Server> startingServer(new MockRasServer());
    MockRasServer& startingServerRef = *std::dynamic_pointer_cast<MockRasServer>(startingServer);
    EXPECT_CALL(startingServerRef, startProcess());
    EXPECT_CALL(startingServerRef, getServerId()).WillOnce(Return(startingServerId));
    //A starting server will be forcibly closed
    EXPECT_CALL(startingServerRef, stop(KILL));
    EXPECT_CALL(startingServerRef, getPort()).WillOnce(Return(startingPort));

    ServerFactoryMock& factoryRef = *std::dynamic_pointer_cast<ServerFactoryMock>(this->serverFactory);
    EXPECT_CALL(factoryRef, createServer(_)).WillOnce(Return(this->server)).WillOnce(Return(startingServer));

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name("name");
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(startingPort);
    groupConfig.add_ports(runningPort);
    groupConfig.set_min_alive_server_no(2);
    groupConfig.set_min_available_server_no(2);
    groupConfig.set_starting_server_lifetime(1);

    ServerGroupImpl group(groupConfig, this->dbHostManager, this->serverFactory);

    group.start();
    //Register the running server
    group.tryRegisterServer(runningServerId);

    group.stop(force);
}

TEST_F(ServerGroupTest, evaluateServerGroupNoStart)
{
    std::int32_t startingPort = 2034;
    std::int32_t runningPort = 2035;

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

    ServerGroupImpl group(groupConfig, this->dbHostManager, this->serverFactory);

    ASSERT_NO_THROW(group.evaluateServerGroup());
}

TEST_F(ServerGroupTest, evaluateServerGroup)
{
    std::int32_t runningServerPort = 20034;
    std::int32_t startingServerPort = 20035;
    std::int32_t deadServerPort = 20036;
    std::int32_t fullServerPort = 20037;
    std::uint32_t maxSessions = 5;


    // The server count of the database host would be increased by the dbh manager
    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *std::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDatabaseHost(_)).WillOnce(Return(this->dbHost));

    std::string dummyServerId = "dummyServerId";
    std::shared_ptr<rasmgr::Server> dummyServer(new MockRasServer());
    MockRasServer& dummyServerRef = *std::dynamic_pointer_cast<MockRasServer>(dummyServer);
    EXPECT_CALL(dummyServerRef, startProcess()).Times(2);
    //Because the server is stopped isAlive will return false.
    EXPECT_CALL(dummyServerRef, getPort()).WillOnce(Return(deadServerPort));
    EXPECT_CALL(dummyServerRef, getServerId()).WillRepeatedly(Return(dummyServerId));
    //When the server group is destructed, it will kill the servers
    EXPECT_CALL(dummyServerRef, stop(KILL));

    // Setup a server that is running correctly
    std::string runningServerId = "runningServerId";
    MockRasServer& runningServerRef = *std::dynamic_pointer_cast<MockRasServer>(this->server);
    EXPECT_CALL(runningServerRef, startProcess());
    EXPECT_CALL(runningServerRef, registerServer(runningServerId));
    EXPECT_CALL(runningServerRef, getTotalSessionNo()).WillOnce(Return(0));
    EXPECT_CALL(runningServerRef, getServerId()).WillRepeatedly(Return(runningServerId));
    //Because the server is stopped isAlive will return false.
    EXPECT_CALL(runningServerRef, isAlive()).WillOnce(Return(true));
    EXPECT_CALL(runningServerRef, isFree()).WillOnce(Return(true));
    //This will return the port used by the server
    EXPECT_CALL(runningServerRef, getPort()).WillOnce(Return(runningServerPort));
    //When the server group is destructed, it will kill the servers
    EXPECT_CALL(runningServerRef, stop(KILL));

    std::string startingServerId = "startingServerId";
    std::shared_ptr<rasmgr::Server> startingServer(new MockRasServer());
    MockRasServer& startingServerRef = *std::dynamic_pointer_cast<MockRasServer>(startingServer);
    EXPECT_CALL(startingServerRef, startProcess());
    //Because the server is stopped isAlive will return false.
    EXPECT_CALL(startingServerRef, getPort()).WillOnce(Return(startingServerPort));
    EXPECT_CALL(startingServerRef, getServerId()).WillRepeatedly(Return(startingServerId));
    //When the server group is destructed, it will kill the servers
    EXPECT_CALL(startingServerRef, stop(KILL));

    std::string deadServerId = "deadServerId";
    std::shared_ptr<rasmgr::Server> deadServer(new MockRasServer());
    MockRasServer& deadServerRef = *std::dynamic_pointer_cast<MockRasServer>(deadServer);
    EXPECT_CALL(deadServerRef, startProcess());

    EXPECT_CALL(deadServerRef, registerServer(deadServerId));
    EXPECT_CALL(deadServerRef, getServerId()).WillRepeatedly(Return(deadServerId));
    //Because the server is stopped isAlive will return false.
    EXPECT_CALL(deadServerRef, isAlive()).WillOnce(Return(false));
    //This will return the port used by the server
    EXPECT_CALL(deadServerRef, getPort()).WillOnce(Return(deadServerPort));

    std::string fullServerId = "fullServerId";
    std::shared_ptr<rasmgr::Server> fullServer(new MockRasServer());
    MockRasServer& fullServerRef = *std::dynamic_pointer_cast<MockRasServer>(fullServer);
    EXPECT_CALL(fullServerRef, startProcess());
    EXPECT_CALL(fullServerRef, registerServer(fullServerId));
    EXPECT_CALL(fullServerRef, getTotalSessionNo()).WillOnce(Return(maxSessions));
    EXPECT_CALL(fullServerRef, getServerId()).WillRepeatedly(Return(fullServerId));
    //Because the server is stopped isAlive will return false.
    EXPECT_CALL(fullServerRef, isAlive()).WillOnce(Return(true));
    EXPECT_CALL(fullServerRef, isFree()).WillOnce(Return(true));
    //This will return the port used by the server
    EXPECT_CALL(fullServerRef, getPort()).WillOnce(Return(fullServerPort));
    //When the server group is destructed, it will kill the servers
    EXPECT_CALL(fullServerRef, stop(KILL));

    ServerFactoryMock& factoryRef = *std::dynamic_pointer_cast<ServerFactoryMock>(this->serverFactory);
    EXPECT_CALL(factoryRef, createServer(_))
    .WillOnce(Return(this->server))
    .WillOnce(Return(startingServer))
    .WillOnce(Return(deadServer))
    .WillOnce(Return(fullServer))
    .WillRepeatedly(Return(dummyServer));

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name("name");
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(runningServerPort);
    groupConfig.add_ports(startingServerPort);
    groupConfig.add_ports(deadServerPort);
    groupConfig.add_ports(fullServerPort);
    groupConfig.set_countdown(maxSessions);

    //This guarantees that both servers are started
    groupConfig.set_min_alive_server_no(4);
    groupConfig.set_min_available_server_no(4);
    groupConfig.set_starting_server_lifetime(1);

    ServerGroupImpl group(groupConfig, this->dbHostManager, this->serverFactory);
    group.start();

    //Register the running server
    group.tryRegisterServer(runningServerId);
    group.tryRegisterServer(deadServerId);
    group.tryRegisterServer(fullServerId);

    ASSERT_NO_THROW(group.evaluateServerGroup());

}

}
}
