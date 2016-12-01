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

#include <gtest/gtest.h>

#include "mocks/servergroupfactorymock.hh"
#include "mocks/servergroupmock.hh"
#include "util/testutil.hh"

#include "../src/servermanagerconfig.hh"
#include "../src/servermanager.hh"

namespace rasmgr
{
namespace test
{
using rasmgr::ServerGroupFactory;
using rasmgr::ServerManagerConfig;
using rasmgr::ServerManager;
using rasmgr::ServerGroupConfigProto;
using rasmgr::ServerGroup;
using rasmgr::StartServerGroup;
using rasmgr::StopServerGroup;
using rasmgr::Server;
using rasmgr::ServerMgrProto;

using ::testing::AtLeast;                     // #1
using ::testing::_;
using ::testing::Return;

class ServerManagerTest: public ::testing::Test
{
protected:
    ServerManagerTest()
    {
        serverGroupFactory.reset(new ServerGroupFactoryMock());
    }

    boost::shared_ptr<ServerGroupFactory> serverGroupFactory;

    ServerManagerConfig config;
};

TEST_F(ServerManagerTest, preconditions)
{
    ServerManager* serverManager;

    ASSERT_NO_THROW(serverManager = new ServerManager(this->config, this->serverGroupFactory));

    ASSERT_NO_THROW(delete serverManager);
}

TEST_F(ServerManagerTest, defineServerGroup)
{
    ServerGroupConfigProto groupConfig;
    groupConfig.set_name("name");
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(2000);
    groupConfig.set_starting_server_lifetime(1);

    boost::shared_ptr<ServerGroup> serverGroup(new ServerGroupMock());
    ServerGroupMock& serverGroupMockRef = *boost::dynamic_pointer_cast<ServerGroupMock>(serverGroup);
    EXPECT_CALL(serverGroupMockRef, getGroupName()).Times(3).WillRepeatedly(Return(groupConfig.name()));

    boost::shared_ptr<ServerGroup> serverGroup2(new ServerGroupMock());

    ServerGroupFactoryMock& serverGroupFactoryRef  = *boost::dynamic_pointer_cast<ServerGroupFactoryMock>(serverGroupFactory);
    EXPECT_CALL(serverGroupFactoryRef, createServerGroup(_)).WillOnce(Return(serverGroup)).WillOnce(Return(serverGroup2));

    ServerManager serverManager(this->config, this->serverGroupFactory);

    //There are no server groups defined, this will work
    ASSERT_NO_THROW(serverManager.defineServerGroup(groupConfig));

    //There already is a server group defined with this name
    ASSERT_ANY_THROW(serverManager.defineServerGroup(groupConfig));

    ServerGroupConfigProto secGroupConfig;
    secGroupConfig.set_name("name2");
    secGroupConfig.set_host("host");
    secGroupConfig.set_db_host("dbHost");
    secGroupConfig.add_ports(2000);
    secGroupConfig.set_starting_server_lifetime(1);

    serverManager.defineServerGroup(secGroupConfig);
}

TEST_F(ServerManagerTest, changeServerGroup)
{
    std::string name = "name";
    std::string newName = "newName";

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name(name);
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(2000);
    groupConfig.set_starting_server_lifetime(1);

    ServerGroupConfigProto newGroupConfig;
    groupConfig.set_name(newName);

    ServerManager serverManager(this->config, this->serverGroupFactory);
    //Will throw because there is no group with this name
    ASSERT_ANY_THROW(serverManager.changeServerGroup(name, newGroupConfig));

    boost::shared_ptr<ServerGroup> serverGroup(new ServerGroupMock());
    ServerGroupMock& serverGroupMockRef = *boost::dynamic_pointer_cast<ServerGroupMock>(serverGroup);
    EXPECT_CALL(serverGroupMockRef, getGroupName())
    .WillOnce(Return(name))
    .WillOnce(Return(name))
    .WillOnce(Return(newName));
    EXPECT_CALL(serverGroupMockRef, isStopped())
    .WillOnce(Return(false))
    .WillOnce(Return(true));
    EXPECT_CALL(serverGroupMockRef, changeGroupConfig(_));

    ServerGroupFactoryMock& serverGroupFactoryRef  = *boost::dynamic_pointer_cast<ServerGroupFactoryMock>(serverGroupFactory);
    EXPECT_CALL(serverGroupFactoryRef, createServerGroup(_)).WillOnce(Return(serverGroup));

    //There are no server groups defined, this will work
    ASSERT_NO_THROW(serverManager.defineServerGroup(groupConfig));

    //Will throw an exception because the server group is not stopped
    ASSERT_ANY_THROW(serverManager.changeServerGroup(name, newGroupConfig));

    ASSERT_NO_THROW(serverManager.changeServerGroup(name, newGroupConfig));

    //Will throw an exception because the server group's name was changed
    ASSERT_ANY_THROW(serverManager.changeServerGroup(name, newGroupConfig));
}

TEST_F(ServerManagerTest, removeServerGroup)
{
    std::string name = "name";
    std::string newName = "newName";

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name(name);
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(2000);
    groupConfig.set_starting_server_lifetime(1);

    ServerManager serverManager(this->config, this->serverGroupFactory);
    //Will throw because there is no group with this name
    ASSERT_ANY_THROW(serverManager.removeServerGroup(name));

    boost::shared_ptr<ServerGroup> serverGroup(new ServerGroupMock());
    ServerGroupMock& serverGroupMockRef = *boost::dynamic_pointer_cast<ServerGroupMock>(serverGroup);
    EXPECT_CALL(serverGroupMockRef, getGroupName())
    .WillOnce(Return(name))
    .WillOnce(Return(name));
    EXPECT_CALL(serverGroupMockRef, isStopped())
    .WillOnce(Return(false))
    .WillOnce(Return(true));

    ServerGroupFactoryMock& serverGroupFactoryRef  = *boost::dynamic_pointer_cast<ServerGroupFactoryMock>(serverGroupFactory);
    EXPECT_CALL(serverGroupFactoryRef, createServerGroup(_)).WillOnce(Return(serverGroup));

    //There are no server groups defined, this will work
    ASSERT_NO_THROW(serverManager.defineServerGroup(groupConfig));

    //Will throw an exception because the server group is not stopped
    ASSERT_ANY_THROW(serverManager.removeServerGroup(name));

    ASSERT_NO_THROW(serverManager.removeServerGroup(name));

    //Will throw an exception because the server group was removed
    ASSERT_ANY_THROW(serverManager.removeServerGroup(name));
}

TEST_F(ServerManagerTest, registerServer)
{
    std::string name = "name";
    std::string serverId = "serverId";
    ServerGroupConfigProto groupConfig;

    boost::shared_ptr<ServerGroup> serverGroup(new ServerGroupMock());
    ServerGroupMock& serverGroupMockRef = *boost::dynamic_pointer_cast<ServerGroupMock>(serverGroup);
    EXPECT_CALL(serverGroupMockRef, tryRegisterServer(serverId)).WillOnce(Return(false)).WillOnce(Return(true));

    ServerGroupFactoryMock& serverGroupFactoryRef  = *boost::dynamic_pointer_cast<ServerGroupFactoryMock>(serverGroupFactory);
    EXPECT_CALL(serverGroupFactoryRef, createServerGroup(_)).WillOnce(Return(serverGroup));

    ServerManager serverManager(this->config, this->serverGroupFactory);

    //Will throw because there is no server with this ID
    ASSERT_ANY_THROW(serverManager.registerServer(serverId));

    //There are no server groups defined, this will work
    ASSERT_NO_THROW(serverManager.defineServerGroup(groupConfig));

    //Will throw because the registration failed. tryregister returned false
    ASSERT_ANY_THROW(serverManager.registerServer(serverId));

    ASSERT_NO_THROW(serverManager.registerServer(serverId));
}

TEST_F(ServerManagerTest, tryGetFreeServer)
{
    std::string databaseName = "dbName";
    boost::shared_ptr<Server> out_server;

    ServerManager serverManager(this->config, this->serverGroupFactory);

    //Will fail because there is no server group defined
    ASSERT_FALSE(serverManager.tryGetFreeServer(databaseName, out_server));

    std::string name = "name";
    std::string serverId = "serverId";
    ServerGroupConfigProto groupConfig;

    boost::shared_ptr<ServerGroup> serverGroup(new ServerGroupMock());
    ServerGroupMock& serverGroupMockRef = *boost::dynamic_pointer_cast<ServerGroupMock>(serverGroup);
    EXPECT_CALL(serverGroupMockRef, tryGetAvailableServer(_, _)).WillOnce(Return(false)).WillOnce(Return(true));

    ServerGroupFactoryMock& serverGroupFactoryRef  = *boost::dynamic_pointer_cast<ServerGroupFactoryMock>(serverGroupFactory);
    EXPECT_CALL(serverGroupFactoryRef, createServerGroup(_)).WillOnce(Return(serverGroup));

    //There are no server groups defined, this will work
    ASSERT_NO_THROW(serverManager.defineServerGroup(groupConfig));

    ASSERT_FALSE(serverManager.tryGetFreeServer(databaseName, out_server));

    ASSERT_TRUE(serverManager.tryGetFreeServer(databaseName, out_server));
}

TEST_F(ServerManagerTest, startServerGroupNothing)
{
    ServerManager serverManager(this->config, this->serverGroupFactory);

    StartServerGroup startGroup;

    //Will fail because the start group is not configured
    ASSERT_ANY_THROW(serverManager.startServerGroup(startGroup));
}

TEST_F(ServerManagerTest, startServerGroupAll)
{
    ServerManager serverManager(this->config, this->serverGroupFactory);

    StartServerGroup startGroup;
    startGroup.set_all(true);

    ServerGroupConfigProto groupConfig;

    boost::shared_ptr<ServerGroup> serverGroup(new ServerGroupMock());
    ServerGroupMock& serverGroupMockRef = *boost::dynamic_pointer_cast<ServerGroupMock>(serverGroup);
    EXPECT_CALL(serverGroupMockRef, isStopped()).WillOnce(Return(false)).WillOnce(Return(true));
    EXPECT_CALL(serverGroupMockRef, start());

    ServerGroupFactoryMock& serverGroupFactoryRef  = *boost::dynamic_pointer_cast<ServerGroupFactoryMock>(serverGroupFactory);
    EXPECT_CALL(serverGroupFactoryRef, createServerGroup(_)).WillOnce(Return(serverGroup));

    ASSERT_NO_THROW(serverManager.defineServerGroup(groupConfig));

    ASSERT_NO_THROW(serverManager.startServerGroup(startGroup));
    ASSERT_NO_THROW(serverManager.startServerGroup(startGroup));
}

TEST_F(ServerManagerTest, startServerGroupHost)
{
    std::string hostName = "hostName";
    ServerGroupConfigProto groupConfig;
    groupConfig.set_host(hostName);

    ServerManager serverManager(this->config, this->serverGroupFactory);

    StartServerGroup startGroup;
    startGroup.set_host_name(hostName);

    //Will fail because the start group is not configured
    ASSERT_ANY_THROW(serverManager.startServerGroup(startGroup));

    boost::shared_ptr<ServerGroup> serverGroup(new ServerGroupMock());
    ServerGroupMock& serverGroupMockRef = *boost::dynamic_pointer_cast<ServerGroupMock>(serverGroup);
    EXPECT_CALL(serverGroupMockRef, isStopped()).WillOnce(Return(false)).WillOnce(Return(true));
    EXPECT_CALL(serverGroupMockRef, start());
    EXPECT_CALL(serverGroupMockRef, getConfig()).WillRepeatedly(Return(groupConfig));

    ServerGroupFactoryMock& serverGroupFactoryRef  = *boost::dynamic_pointer_cast<ServerGroupFactoryMock>(serverGroupFactory);
    EXPECT_CALL(serverGroupFactoryRef, createServerGroup(_)).WillOnce(Return(serverGroup));

    serverManager.defineServerGroup(groupConfig);

    ASSERT_NO_THROW(serverManager.startServerGroup(startGroup));
    ASSERT_NO_THROW(serverManager.startServerGroup(startGroup));
}

TEST_F(ServerManagerTest, startServerGroupByName)
{
    std::string groupName = "groupName";
    ServerGroupConfigProto groupConfig;
    groupConfig.set_name(groupName);

    ServerManager serverManager(this->config, this->serverGroupFactory);

    StartServerGroup startGroup;
    startGroup.set_group_name(groupName);

    //Will fail because the start group is not configured
    ASSERT_ANY_THROW(serverManager.startServerGroup(startGroup));

    boost::shared_ptr<ServerGroup> serverGroup(new ServerGroupMock());
    ServerGroupMock& serverGroupMockRef = *boost::dynamic_pointer_cast<ServerGroupMock>(serverGroup);
    EXPECT_CALL(serverGroupMockRef, start());
    EXPECT_CALL(serverGroupMockRef, getGroupName())
    .WillOnce(Return(""))
    .WillOnce(Return(groupName));

    ServerGroupFactoryMock& serverGroupFactoryRef  = *boost::dynamic_pointer_cast<ServerGroupFactoryMock>(serverGroupFactory);
    EXPECT_CALL(serverGroupFactoryRef, createServerGroup(_)).WillOnce(Return(serverGroup));

    serverManager.defineServerGroup(groupConfig);

    //Will throw because the group is running
    ASSERT_ANY_THROW(serverManager.startServerGroup(startGroup));
    ASSERT_NO_THROW(serverManager.startServerGroup(startGroup));
}

TEST_F(ServerManagerTest, stopServerGroupAll)
{
    ServerManager serverManager(this->config, this->serverGroupFactory);

    bool force = rasmgr::test::TestUtil::randomBool();
    StopServerGroup stopGroup;
    stopGroup.set_all(true);
    stopGroup.set_kill_level(FORCE);

    ServerGroupConfigProto groupConfig;

    boost::shared_ptr<ServerGroup> serverGroup(new ServerGroupMock());
    ServerGroupMock& serverGroupMockRef = *boost::dynamic_pointer_cast<ServerGroupMock>(serverGroup);
    EXPECT_CALL(serverGroupMockRef, isStopped()).WillOnce(Return(false)).WillOnce(Return(true));
    EXPECT_CALL(serverGroupMockRef, stop(FORCE));

    ServerGroupFactoryMock& serverGroupFactoryRef  = *boost::dynamic_pointer_cast<ServerGroupFactoryMock>(serverGroupFactory);
    EXPECT_CALL(serverGroupFactoryRef, createServerGroup(_)).WillOnce(Return(serverGroup));

    ASSERT_NO_THROW(serverManager.defineServerGroup(groupConfig));

    ASSERT_NO_THROW(serverManager.stopServerGroup(stopGroup));
    ASSERT_NO_THROW(serverManager.stopServerGroup(stopGroup));
}

TEST_F(ServerManagerTest, stopServerGroupHost)
{
    std::string hostName = "hostName";
    ServerGroupConfigProto groupConfig;
    groupConfig.set_host(hostName);

    ServerManager serverManager(this->config, this->serverGroupFactory);

    StopServerGroup stopGroup;
    stopGroup.set_host_name(hostName);
    stopGroup.set_kill_level(FORCE);

    //Will fail because the start group is not configured
    ASSERT_ANY_THROW(serverManager.stopServerGroup(stopGroup));

    boost::shared_ptr<ServerGroup> serverGroup(new ServerGroupMock());
    ServerGroupMock& serverGroupMockRef = *boost::dynamic_pointer_cast<ServerGroupMock>(serverGroup);
    EXPECT_CALL(serverGroupMockRef, isStopped()).WillOnce(Return(false)).WillOnce(Return(true));
    EXPECT_CALL(serverGroupMockRef, stop(FORCE));
    EXPECT_CALL(serverGroupMockRef, getConfig()).WillRepeatedly(Return(groupConfig));

    ServerGroupFactoryMock& serverGroupFactoryRef  = *boost::dynamic_pointer_cast<ServerGroupFactoryMock>(serverGroupFactory);
    EXPECT_CALL(serverGroupFactoryRef, createServerGroup(_)).WillOnce(Return(serverGroup));

    serverManager.defineServerGroup(groupConfig);

    ASSERT_NO_THROW(serverManager.stopServerGroup(stopGroup));
    ASSERT_NO_THROW(serverManager.stopServerGroup(stopGroup));
}

TEST_F(ServerManagerTest, stopServerGroupByName)
{
    std::string groupName = "groupName";
    ServerGroupConfigProto groupConfig;
    groupConfig.set_name(groupName);

    ServerManager serverManager(this->config, this->serverGroupFactory);

    StopServerGroup stopGroup;
    stopGroup.set_group_name(groupName);
    stopGroup.set_kill_level(FORCE);

    //Will fail because the start group is not configured
    ASSERT_ANY_THROW(serverManager.stopServerGroup(stopGroup));

    boost::shared_ptr<ServerGroup> serverGroup(new ServerGroupMock());
    ServerGroupMock& serverGroupMockRef = *boost::dynamic_pointer_cast<ServerGroupMock>(serverGroup);
    EXPECT_CALL(serverGroupMockRef, stop(FORCE)).Times(1);
    EXPECT_CALL(serverGroupMockRef, getGroupName())
    .WillOnce(Return(""))
    .WillOnce(Return(groupName));

    ServerGroupFactoryMock& serverGroupFactoryRef  = *boost::dynamic_pointer_cast<ServerGroupFactoryMock>(serverGroupFactory);
    EXPECT_CALL(serverGroupFactoryRef, createServerGroup(_)).WillOnce(Return(serverGroup));

    serverManager.defineServerGroup(groupConfig);

    //Will throw because the group is running
    ASSERT_ANY_THROW(serverManager.stopServerGroup(stopGroup));
    ASSERT_NO_THROW(serverManager.stopServerGroup(stopGroup));
}

TEST_F(ServerManagerTest, serializeToProto)
{
    ServerMgrProto proto;

    ServerManager serverManager(this->config, this->serverGroupFactory);

    proto =  serverManager.serializeToProto();

    ASSERT_EQ(0, proto.server_groups_size());

    rasmgr::ServerGroupProto serverGroupProto;
    boost::shared_ptr<ServerGroup> serverGroup(new ServerGroupMock());
    ServerGroupMock& serverGroupMockRef = *boost::dynamic_pointer_cast<ServerGroupMock>(serverGroup);
    EXPECT_CALL(serverGroupMockRef, serializeToProto()).WillOnce(Return(serverGroupProto));

    ServerGroupFactoryMock& serverGroupFactoryRef  = *boost::dynamic_pointer_cast<ServerGroupFactoryMock>(serverGroupFactory);
    EXPECT_CALL(serverGroupFactoryRef, createServerGroup(_)).WillOnce(Return(serverGroup));

    ServerGroupConfigProto groupConfig;
    groupConfig.set_name("name");
    groupConfig.set_host("host");
    groupConfig.set_db_host("dbHost");
    groupConfig.add_ports(2000);
    groupConfig.set_starting_server_lifetime(1);
    //There are no server groups defined, this will work
    ASSERT_NO_THROW(serverManager.defineServerGroup(groupConfig));

    proto =  serverManager.serializeToProto();

    ASSERT_EQ(1, proto.server_groups_size());
    ASSERT_EQ(serverGroupProto.DebugString(), proto.server_groups(0).DebugString());

}

}
}
