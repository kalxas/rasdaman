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

#include <boost/cstdint.hpp>
#include <boost/shared_ptr.hpp>

#include "../../common/src/unittest/gtest.h"
#include "../../common/src/mock/gmock.h"
#include "../../common/src/logging/easylogging++.hh"

#include "mocks/serverfactorymock.hh"
#include "mocks/databasehostmgrmock.hh"
#include "mocks/mockrasserver.hh"
#include "util/testutil.hh"

#include "rasnet/src/server/servicemanager.hh"
#include "rasnet/src/messages/rassrvr_rasmgr_service.pb.h"

#include "../src/serverrasnet.hh"
#include "../src/servergroupconfig.hh"
#include "../src/servergroupimpl.hh"
#include "../src/databasehostmanager.hh"
#include "../src/rasmgrconfig.hh"
#include "../src/constants.hh"

using ::testing::AtLeast;                     // #1
using ::testing::_;
using ::testing::Return;

using rasmgr::ServerRasNet;
using rasnet::ServiceManager;
using rasnet::service::RasServerService;
using rasmgr::ServerGroupImpl;
using rasmgr::ServerGroupConfig;
using rasmgr::RasMgrConfig;
using rasmgr::test::TestUtil;
using rasmgr::ServerGroupConfigProto;
using rasmgr::ServerFactory;



class ServerGroupTest:public ::testing::Test
{
protected:
    ServerGroupTest()
    {
        this->dbHostManager.reset(new DatabaseHostManagerMock());
        this->serverFactory.reset(new ServerFactoryMock());
        this->dbHost.reset(new rasmgr::DatabaseHost("dbHost","connect","",""));
        this->server.reset(new MockRasServer());
    }

    boost::shared_ptr<rasmgr::DatabaseHost> dbHost;
    boost::shared_ptr<rasmgr::ServerFactory> serverFactory;
    boost::shared_ptr<rasmgr::DatabaseHostManager> dbHostManager;
    boost::shared_ptr<rasmgr::Server> server;
};

TEST_F(ServerGroupTest, constructorValidation)
{
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

    if(hasName)
    {
        groupConfig.set_name("groupName");
    }

    if(hasHost)
    {
        groupConfig.set_host("host");
    }

    if(hasDbHost)
    {
        groupConfig.set_db_host("dbHost");
    }

    if(hasPorts)
    {
        groupConfig.add_ports(2034);
    }

    if(hasAliveServers)
    {
        groupConfig.set_min_alive_server_no(20);
    }

    if(hasAvailableServers)
    {
        groupConfig.set_min_available_server_no(102);
    }

    if(hasMaxServers)
    {
        groupConfig.set_max_idle_server_no(21);
    }

    if(hasAutoRestart)
    {
        groupConfig.set_autorestart(TestUtil::randomBool());
    }

    if(hasSessionCount)
    {
        groupConfig.set_countdown(123);
    }


    ServerGroupImpl* group;

    if(!hasName || !hasHost || !hasDbHost || !hasPorts)
    {
        ASSERT_ANY_THROW(group=new ServerGroupImpl(groupConfig, this->dbHostManager, this->serverFactory));
    }
    else
    {
        //The server count will be decreased in the destructor of the server group
        //so it is neccessary to increase it
        this->dbHost->increaseServerCount();
        DatabaseHostManagerMock& dbhManager = *boost::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
        EXPECT_CALL(dbhManager, getAndLockDH(_)).WillOnce(Return(this->dbHost));

        ASSERT_NO_THROW(group=new ServerGroupImpl(groupConfig, this->dbHostManager, this->serverFactory));
        ASSERT_TRUE(group->isStopped());

        ASSERT_EQ(groupConfig.name(), group->getConfig().name());
        ASSERT_EQ(groupConfig.host(), group->getConfig().host());
        ASSERT_EQ(groupConfig.db_host(), group->getConfig().db_host());
        ASSERT_EQ(groupConfig.ports_size(), group->getConfig().ports_size());
        ASSERT_EQ(rasmgr::STARTING_SERVER_LIFETIME, group->getConfig().starting_server_lifetime());

        if(hasAliveServers)
        {
            ASSERT_EQ(groupConfig.min_alive_server_no(), group->getConfig().min_alive_server_no());
        }
        else
        {
            ASSERT_EQ(rasmgr::MIN_ALIVE_SERVER_NO, group->getConfig().min_alive_server_no());
        }

        if(hasAvailableServers)
        {
            ASSERT_EQ(groupConfig.min_available_server_no(), group->getConfig().min_available_server_no());
        }
        else
        {
            ASSERT_EQ(rasmgr::MIN_AVAILABLE_SERVER_NO, group->getConfig().min_available_server_no());
        }

        if(hasMaxServers)
        {
            ASSERT_EQ(groupConfig.max_idle_server_no(), group->getConfig().max_idle_server_no());
        }
        else
        {
            ASSERT_EQ(rasmgr::MAX_IDLE_SERVER_NO, group->getConfig().max_idle_server_no());
        }

        if(hasAutoRestart)
        {
            ASSERT_EQ(groupConfig.autorestart(), group->getConfig().autorestart());
        }
        else
        {
            ASSERT_EQ(rasmgr::AUTORESTART_SERVER, group->getConfig().autorestart());
        }

        if(hasSessionCount)
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
    boost::int32_t runningPort = 2034;
    //The minimum number of alive servers will be created
    //The created servers will be started
    //The servers will be stopped in the destructor
    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *boost::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDH(_)).WillOnce(Return(this->dbHost));

    std::string serverId = "serverId";
    MockRasServer& serverRef = *boost::dynamic_pointer_cast<MockRasServer>(this->server);
    EXPECT_CALL(serverRef, startProcess());
    EXPECT_CALL(serverRef, getServerId()).WillOnce(Return(serverId));
    EXPECT_CALL(serverRef, stop(_));
    EXPECT_CALL(serverRef, getPort()).WillOnce(Return(runningPort));

    ServerFactoryMock& factoryRef = *boost::dynamic_pointer_cast<ServerFactoryMock>(this->serverFactory);
    EXPECT_CALL(factoryRef, createServer(_,_,_)).WillOnce(Return(this->server));

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
    boost::int32_t runningPort = 2034;
    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *boost::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDH(_)).WillOnce(Return(this->dbHost));

    std::string serverId = "serverId";
    MockRasServer& serverRef = *boost::dynamic_pointer_cast<MockRasServer>(this->server);
    EXPECT_CALL(serverRef, startProcess());
    EXPECT_CALL(serverRef, getServerId()).WillOnce(Return(serverId));
    EXPECT_CALL(serverRef, stop(_));
    EXPECT_CALL(serverRef, registerServer(serverId));


    ServerFactoryMock& factoryRef = *boost::dynamic_pointer_cast<ServerFactoryMock>(this->serverFactory);
    EXPECT_CALL(factoryRef, createServer(_,_,_)).WillOnce(Return(this->server));

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
    bool force = TestUtil::randomBool();

    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *boost::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDH(_)).WillOnce(Return(this->dbHost));

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name("name");
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(2034);
    groupConfig.set_starting_server_lifetime(1);

    ServerGroupImpl group(groupConfig, this->dbHostManager, this->serverFactory);

    //If the server group is not started, nothing bad should happen.
    ASSERT_ANY_THROW(group.stop(force));
}

TEST_F(ServerGroupTest, stop)
{
    bool force = TestUtil::randomBool();
    boost::int32_t startingPort = 2034;
    boost::int32_t runningPort = 2035;

    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *boost::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDH(_)).WillOnce(Return(this->dbHost));


    std::string runningServerId = "runningServerId";
    MockRasServer& runningServerRef = *boost::dynamic_pointer_cast<MockRasServer>(this->server);
    EXPECT_CALL(runningServerRef, startProcess());
    EXPECT_CALL(runningServerRef, registerServer(runningServerId));
    EXPECT_CALL(runningServerRef, getServerId()).WillRepeatedly(Return(runningServerId));
    //The server will be stopped
    EXPECT_CALL(runningServerRef, stop(force));
    //Because the server is stopped isAlive will return false.
    EXPECT_CALL(runningServerRef, isAlive()).WillOnce(Return(false));
    //This will return the port used by the server
    EXPECT_CALL(runningServerRef, getPort()).WillOnce(Return(runningPort));

    std::string startingServerId = "startingServerId";
    boost::shared_ptr<rasmgr::Server> startingServer(new MockRasServer());
    MockRasServer& startingServerRef = *boost::dynamic_pointer_cast<MockRasServer>(startingServer);
    EXPECT_CALL(startingServerRef, startProcess());
    EXPECT_CALL(startingServerRef, getServerId()).WillOnce(Return(startingServerId));
    //A starting server will be forcibly closed
    EXPECT_CALL(startingServerRef, stop(true));
    EXPECT_CALL(startingServerRef, getPort()).WillOnce(Return(startingPort));

    ServerFactoryMock& factoryRef = *boost::dynamic_pointer_cast<ServerFactoryMock>(this->serverFactory);
    EXPECT_CALL(factoryRef, createServer(_,_,_)).WillOnce(Return(this->server)).WillOnce(Return(startingServer));

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
    boost::int32_t startingPort = 2034;
    boost::int32_t runningPort = 2035;

    this->dbHost->increaseServerCount();
    DatabaseHostManagerMock& dbhManager = *boost::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
    EXPECT_CALL(dbhManager, getAndLockDH(_)).WillOnce(Return(this->dbHost));

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
//    boost::int32_t startingPort = 2034;
//    boost::int32_t runningPort = 2035;

//    this->dbHost->increaseServerCount();
//    DatabaseHostManagerMock& dbhManager = *boost::dynamic_pointer_cast<DatabaseHostManagerMock>(this->dbHostManager);
//    EXPECT_CALL(dbhManager, getAndLockDH(_)).WillOnce(Return(this->dbHost));


//    std::string runningServerId = "runningServerId";
//    MockRasServer& runningServerRef = *boost::dynamic_pointer_cast<MockRasServer>(this->server);
//    EXPECT_CALL(runningServerRef, startProcess());
//    EXPECT_CALL(runningServerRef, registerServer(runningServerId));
//    EXPECT_CALL(runningServerRef, getServerId()).WillRepeatedly(Return(runningServerId));

//    //Because the server is stopped isAlive will return false.
//    EXPECT_CALL(runningServerRef, isAlive()).WillOnce(Return(false));
//    //This will return the port used by the server
//    EXPECT_CALL(runningServerRef, getPort()).WillOnce(Return(runningPort));

//    //A server that failed to start
//    std::string startingServerId = "startingServerId";
//    boost::shared_ptr<rasmgr::Server> startingServer(new MockRasServer());
//    MockRasServer& startingServerRef = *boost::dynamic_pointer_cast<MockRasServer>(startingServer);
//    EXPECT_CALL(startingServerRef, startProcess());
//    //The server will be stopped
//    EXPECT_CALL(startingServerRef, stop(true));
//    EXPECT_CALL(startingServerRef, getServerId()).WillOnce(Return(startingServerId));
//    //A starting server will be forcibly closed
//    EXPECT_CALL(startingServerRef, getPort()).WillOnce(Return(startingPort));

//    std::string runningServerId1 = "runningServerId1";
//    boost::shared_ptr<rasmgr::Server> runningServer1(new MockRasServer());
//    MockRasServer& runningServerRef1 = *boost::dynamic_pointer_cast<MockRasServer>(runningServer1);
//    EXPECT_CALL(runningServerRef1, startProcess());
//    EXPECT_CALL(runningServerRef, getServerId()).WillRepeatedly(Return(runningServerId1));

//    //This will return the port used by the server
//    EXPECT_CALL(runningServerRef, getPort()).WillOnce(Return(runningPort));

//    ServerFactoryMock& factoryRef = *boost::dynamic_pointer_cast<ServerFactoryMock>(this->serverFactory);
//    EXPECT_CALL(factoryRef, createServer(_,_,_))
//    .WillOnce(Return(this->server))
//    .WillOnce(Return(startingServer))
//    .WillOnce(Return(runningServer1));

//    ServerGroupConfigProto groupConfig;
//    groupConfig.set_name("name");
//    groupConfig.set_host("host");
//    groupConfig.set_db_host("dbHost");
//    groupConfig.add_ports(startingPort);
//    groupConfig.add_ports(runningPort);
//    //This guarantees that both servers are started
//    groupConfig.set_min_alive_server_no(2);
//    groupConfig.set_min_available_server_no(2);
//    groupConfig.set_starting_server_lifetime(1);

//    ServerGroup group(groupConfig, this->dbHostManager, this->serverFactory);
//    group.start();
//    //Register the running server
//    group.tryRegisterServer(runningServerId);

//    ASSERT_NO_THROW(group.evaluateServerGroup());
}
