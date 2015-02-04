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

#include "../src/client.hh"
#include "../src/user.hh"
#include "../src/userdatabaserights.hh"
#include "../src/useradminrights.hh"
#include "../src/rasmgrconfig.hh"
#include "../src/usermanager.hh"
#include "../src/clientmanager.hh"
#include "../src/clientcredentials.hh"
#include "../src/clientmanagerconfig.hh"

#include "../src/messages/rasmgrmess.pb.h"

#include "mocks/mockrasserver.hh"
#include "mocks/usermanagermock.hh"

using rasmgr::Client;
using rasmgr::RasMgrConfig;
using rasmgr::Server;
using rasmgr::UserProto;
using rasmgr::UserManager;
using rasmgr::ClientManager;
using rasmgr::ClientManagerConfig;
using ::testing::AtLeast;                     // #1
using ::testing::_;
using ::testing::Return;
using rasmgr::UserAdminRightsProto;
using rasmgr::UserDatabaseRightsProto;
using boost::shared_ptr;

class ClientManagerTest:public ::testing::Test
{
protected:
    ClientManagerTest():
        clientId("clientId"),userName("userName"),userPassword("userPassword"),
        dbName("dbName"), sessionId("sessionId"),
        serverHost("tcp://localhost"),serverPort(7010), config(10,10)
    {
        rasmgr::UserAdminRights adminRights;
        adminRights.setAccessControlRights(true);
        adminRights.setInfoRights(true);
        adminRights.setServerAdminRights(true);
        adminRights.setSystemConfigRights(true);

        rasmgr::UserDatabaseRights dbRights(true, true);

        user.reset(new rasmgr::User(userName,userPassword,dbRights, adminRights));

        userManager.reset(new UserManagerMock());
        clientManager.reset(new ClientManager(userManager, config));

    }

    boost::shared_ptr<rasmgr::User> user;

    std::string userName;
    std::string userPassword;
    std::string clientId;
    std::string dbName;
    std::string sessionId;
    ClientManagerConfig config;

    std::string serverHost;
    boost::int32_t serverPort;
    boost::shared_ptr<UserManager> userManager;
    boost::shared_ptr<ClientManager> clientManager;
};

/**
 * Test what happens when a client with bad credentials connects
 */
TEST_F(ClientManagerTest, connectClientFail)
{
    std::string badUser="badUser";
    std::string badPassword = "badPassword";
    std::string out_clientId;
    rasmgr::ClientCredentials badCredentials(badUser, badPassword);
    UserManagerMock& userMgrMock = *boost::dynamic_pointer_cast<UserManagerMock>(userManager);

    EXPECT_CALL(userMgrMock, tryGetUser(_,_))
    .WillOnce(Return(false)).WillRepeatedly(DoAll(testing::SetArgReferee<1>(user), Return(true)));

    //Will fail because the user manager will say that there is no client with those credentials
    ASSERT_ANY_THROW(clientManager->connectClient(badCredentials, out_clientId));

    //Good user name, bad password
    badCredentials.setUserName(user->getName());
    ASSERT_ANY_THROW(clientManager->connectClient(badCredentials, out_clientId));


}

TEST_F(ClientManagerTest, connectClientSuccess)
{
    std::string out_clientId;
    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());

    UserManagerMock& userMgrMock = *boost::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_,_))
    .WillOnce(DoAll(testing::SetArgReferee<1>(user), Return(true)));

    ASSERT_NO_THROW(clientManager->connectClient(credentials, out_clientId));
}

TEST_F(ClientManagerTest, disconnectClient)
{
    std::string out_clientId;
    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());

    UserManagerMock& userMgrMock = *boost::dynamic_pointer_cast<UserManagerMock>(userManager);

    EXPECT_CALL(userMgrMock, tryGetUser(_,_))
    .WillOnce(DoAll(testing::SetArgReferee<1>(user), Return(true)));

    //Nothing will happen because there is no client with that ID
    ASSERT_NO_THROW(clientManager->disconnectClient("out_clientId"));

    //Will succeed
    ASSERT_NO_THROW(clientManager->connectClient(credentials, out_clientId));

    //Will succeed and it will remove the client
    ASSERT_NO_THROW(clientManager->disconnectClient(out_clientId));
}

TEST_F(ClientManagerTest, openClientDbSessionFail)
{
    std::string out_clientId;
    std::string out_sessionId;

    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());
    boost::shared_ptr<Server> server(new MockRasServer());

    MockRasServer& serverMock = *boost::dynamic_pointer_cast<MockRasServer>(server);
    //  EXPECT_CALL(serverMock, allocateClientSession(_, _ ,dbName, _)).Times(1);
    EXPECT_CALL(serverMock, isClientAlive(_)).WillRepeatedly(Return(false));

    UserManagerMock& userMgrMock = *boost::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_,_))
    .WillOnce(DoAll(testing::SetArgReferee<1>(user), Return(true)));

    //Will fail because there is no client with this id
    ASSERT_ANY_THROW(clientManager->openClientDbSession(out_clientId, dbName, server, out_sessionId));

    ASSERT_NO_THROW(clientManager->connectClient(credentials, out_clientId));

    //Wait for the client to die
    usleep(config.getCleanupInterval()*1000*2);

    ASSERT_ANY_THROW(clientManager->openClientDbSession(out_clientId, dbName, server, out_sessionId));
}

TEST_F(ClientManagerTest, openClientDbSession)
{
    std::string out_clientId;
    std::string out_sessionId;

    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());
    boost::shared_ptr<Server> server(new MockRasServer());

    MockRasServer& serverMock = *boost::dynamic_pointer_cast<MockRasServer>(server);
    EXPECT_CALL(serverMock, allocateClientSession(_, _ ,dbName, _)).Times(1);
    EXPECT_CALL(serverMock, isClientAlive(_)).WillRepeatedly(Return(false));

    UserManagerMock& userMgrMock = *boost::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_,_))
    .WillOnce(DoAll(testing::SetArgReferee<1>(user), Return(true)));

    ASSERT_NO_THROW(clientManager->connectClient(credentials, out_clientId));

    ASSERT_NO_THROW(clientManager->openClientDbSession(out_clientId, dbName, server, out_sessionId));
}


TEST_F(ClientManagerTest, closeClientDbSession)
{
    std::string out_clientId;
    std::string out_sessionId;

    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());
    boost::shared_ptr<Server> server(new MockRasServer());

    MockRasServer& serverMock = *boost::dynamic_pointer_cast<MockRasServer>(server);
    EXPECT_CALL(serverMock, allocateClientSession(_, _ ,dbName, _)).Times(1);
    EXPECT_CALL(serverMock, deallocateClientSession(_,_)).Times(1);
    EXPECT_CALL(serverMock, isClientAlive(_)).WillRepeatedly(Return(true));

    UserManagerMock& userMgrMock = *boost::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_,_))
    .WillOnce(DoAll(testing::SetArgReferee<1>(user), Return(true)));

    //There is no client connected.
    ASSERT_ANY_THROW(clientManager->closeClientDbSession(out_clientId, out_sessionId));

    ASSERT_NO_THROW(clientManager->connectClient(credentials, out_clientId));

    ASSERT_NO_THROW(clientManager->closeClientDbSession(out_clientId, out_sessionId));

    ASSERT_NO_THROW(clientManager->openClientDbSession(out_clientId, dbName, server, out_sessionId));

    ASSERT_NO_THROW(clientManager->closeClientDbSession(out_clientId, out_sessionId));
}

TEST_F(ClientManagerTest, keepClientAliveFail)
{
    std::string out_clientId;

    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());
    boost::shared_ptr<Server> server(new MockRasServer());

    MockRasServer& serverMock = *boost::dynamic_pointer_cast<MockRasServer>(server);
    EXPECT_CALL(serverMock, isClientAlive(_)).WillRepeatedly(Return(false));

    UserManagerMock& userMgrMock = *boost::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_,_))
    .WillOnce(DoAll(testing::SetArgReferee<1>(user), Return(true)));

    ASSERT_ANY_THROW(clientManager->keepClientAlive(out_clientId));

    ASSERT_NO_THROW(clientManager->connectClient(credentials, out_clientId));

    //Wait for the client to die
    usleep(config.getCleanupInterval()*1000*2);

    ASSERT_ANY_THROW(clientManager->keepClientAlive(out_clientId));
}
