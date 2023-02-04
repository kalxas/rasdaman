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
#include <cstdint>

#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <logging.hh>

#include "rasmgr/src/client.hh"
#include "rasmgr/src/user.hh"
#include "rasmgr/src/userdatabaserights.hh"
#include "rasmgr/src/useradminrights.hh"
#include "rasmgr/src/rasmgrconfig.hh"
#include "rasmgr/src/usermanager.hh"
#include "rasmgr/src/clientmanager.hh"
#include "rasmgr/src/clientcredentials.hh"
#include "rasmgr/src/clientmanagerconfig.hh"
#include "rasmgr/src/exceptions/rasmgrexceptions.hh"
#include "rasmgr/src/constants.hh"

#include "rasmgr/src/messages/rasmgrmess.pb.h"

#include "mocks/mockrasserver.hh"
#include "mocks/usermanagermock.hh"
#include "mocks/servermanagermock.hh"
#include "mocks/servergroupfactorymock.hh"
#include "mocks/peermanagermock.hh"

namespace rasmgr
{
namespace test
{
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
using ::testing::ReturnRef;
using ::testing::SetArgReferee;
using rasmgr::UserAdminRightsProto;
using rasmgr::UserDatabaseRightsProto;
using std::shared_ptr;

class ClientManagerTest: public ::testing::Test
{
protected:
    ClientManagerTest():
        clientId("clientId"), userName("userName"), userPassword("userPassword"),
        dbName("dbName"), sessionId("sessionId"),
        serverHost("localhost"), serverPort(7010)
    {
        config.setCleanupInterval(10);
        config.setClientLifeTime(10);

        rasmgr::UserAdminRights adminRights;
        adminRights.setAccessControlRights(true);
        adminRights.setInfoRights(true);
        adminRights.setServerAdminRights(true);
        adminRights.setSystemConfigRights(true);

        rasmgr::UserDatabaseRights dbRights(true, true);
        user = std::make_shared<User>(userName, userPassword, dbRights, adminRights);

        userManager = std::make_shared<UserManagerMock>();
        serverGroupFactory = std::make_shared<ServerGroupFactoryMock>();

        ServerManagerConfig serverManagerConfig;
        serverManager = std::make_shared<ServerManagerMock>(serverManagerConfig, serverGroupFactory);

        peerManager = std::make_shared<PeerManagerMock>();

        clientManager = std::make_shared<ClientManager>(config, userManager, serverManager, peerManager);
    }

    std::shared_ptr<rasmgr::User> user;

    std::string clientId;
    std::string userName;
    std::string userPassword;
    std::string dbName;
    std::string sessionId;
    ClientManagerConfig config;

    std::string serverHost;
    std::int32_t serverPort;
    std::shared_ptr<UserManager> userManager;
    std::shared_ptr<ServerGroupFactory> serverGroupFactory;
    std::shared_ptr<ClientManager> clientManager;
    std::shared_ptr<ServerManager> serverManager;
    std::shared_ptr<PeerManager> peerManager;
};

TEST_F(ClientManagerTest, connectClient_FailsInexistentUser)
{
    std::string badUser = "badUser";
    std::string badPassword = "badPassword";
    std::string out_clientId;
    rasmgr::ClientCredentials badCredentials(badUser, badPassword);
    UserManagerMock& userMgrMock = *std::dynamic_pointer_cast<UserManagerMock>(userManager);

    EXPECT_CALL(userMgrMock, tryGetUser(_, _, _)).WillOnce(Return(false));

    //Will fail because the user manager will say that there is no client with those credentials
    ASSERT_THROW(clientManager->connectClient(badCredentials, "", out_clientId), InexistentUserException);
}

// Fails because tryGetUser now does the password validation, instead of clientManager->connectClient
//TEST_F(ClientManagerTest, connectClientBadPassword)
//{
//    std::string badPassword = "badPassword";
//    std::string out_clientId;

//    // Create credentials with the same user name but with a different password
//    rasmgr::ClientCredentials badCredentials(user->getName(), badPassword);
//    UserManagerMock& userMgrMock = *std::dynamic_pointer_cast<UserManagerMock>(userManager);

//    EXPECT_CALL(userMgrMock, tryGetUser(_, _, _))
//            .WillOnce(DoAll(testing::SetArgReferee<2>(user), Return(true)));

//    ASSERT_THROW(clientManager->connectClient(badCredentials, out_clientId), InvalidClientCredentialsException);
//}

TEST_F(ClientManagerTest, connectClientSuccess)
{
    std::string out_clientId;
    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());

    UserManagerMock& userMgrMock = *std::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_, _, _))
            .WillOnce(DoAll(testing::SetArgReferee<2>(user), Return(true)));

    ASSERT_NO_THROW(clientManager->connectClient(credentials, "", out_clientId));
}

TEST_F(ClientManagerTest, disconnectClient_NoClientWithGivenId)
{
    //Nothing will happen because there is no client with that ID
    ASSERT_NO_THROW(clientManager->disconnectClient("out_clientId"));
}

TEST_F(ClientManagerTest, disconnectClient_Success)
{
    std::string out_clientId;
    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());

    UserManagerMock& userMgrMock = *std::dynamic_pointer_cast<UserManagerMock>(userManager);

    EXPECT_CALL(userMgrMock, tryGetUser(_, _, _))
    .WillOnce(DoAll(testing::SetArgReferee<2>(user), Return(true)));

    //Will succeed
    ASSERT_NO_THROW(clientManager->connectClient(credentials, "", out_clientId));

    //Will succeed and it will remove the client
    ASSERT_NO_THROW(clientManager->disconnectClient(out_clientId));
}

TEST_F(ClientManagerTest, openClientDbSession_FailBecauseThereIsNoClientWithTheGivenId)
{
    std::shared_ptr<Server> server(new MockRasServer());

    MockRasServer& serverMock = *std::dynamic_pointer_cast<MockRasServer>(server);
    //  EXPECT_CALL(serverMock, allocateClientSession(_, _ ,dbName, _)).Times(1);
    EXPECT_CALL(serverMock, isClientAlive(_)).WillRepeatedly(Return(false));

    std::string clientId("randomId");
    ClientServerSession out_serverSession;
    //Will fail because there is no client with this id
    ASSERT_THROW(clientManager->openClientDbSession(clientId, dbName, out_serverSession), InexistentClientException);
}

TEST_F(ClientManagerTest, openClientDbSession_SuccessLocalServerFirstAttempt)
{
    // Setup the user manager to successfully connect a client
    UserManagerMock& userMgrMock = *std::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_, _, _))
        .WillOnce(DoAll(testing::SetArgReferee<2>(user), Return(true)));

    // The ServerManager will return this server when asked for one
    std::shared_ptr<Server> freeServer(new MockRasServer());
    MockRasServer& mockServer = *std::dynamic_pointer_cast<MockRasServer>(freeServer);
    EXPECT_CALL(mockServer, getHostName()).WillOnce(ReturnRef(serverHost));
    EXPECT_CALL(mockServer, getPort()).WillOnce(Return(serverPort));
    EXPECT_CALL(mockServer, allocateClientSession(_, _, _, _)).Times(1);

    ServerManagerMock& serverMgrMock = *std::dynamic_pointer_cast<ServerManagerMock>(serverManager);
    EXPECT_CALL(serverMgrMock, tryGetAvailableServer(_, _)).WillOnce(DoAll(SetArgReferee<1>(freeServer), Return(true)));

    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());
    std::string clientId;
    clientManager->connectClient(credentials, "", clientId);

    ClientServerSession out_serverSession;
    ASSERT_NO_THROW(clientManager->openClientDbSession(clientId, dbName, out_serverSession));

    ASSERT_EQ(serverHost, out_serverSession.serverHostName);
    ASSERT_EQ(serverPort, out_serverSession.serverPort);
    ASSERT_EQ(clientId, out_serverSession.clientSessionId);
    //the database session id is automatically created
}

//DIsabled for speed. Enable when you refactor constants
TEST_F(ClientManagerTest, DISABLED_openClientDbSession_SuccessLocalServerLastAttempt)
{
    // Setup the user manager to successfully connect a client
    UserManagerMock& userMgrMock = *std::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_, _, _))
    .WillOnce(DoAll(testing::SetArgReferee<2>(user), Return(true)));

    // The ServerManager will return this server when asked for one
    std::shared_ptr<Server> freeServer(new MockRasServer());
    MockRasServer& mockServer = *std::dynamic_pointer_cast<MockRasServer>(freeServer);
    EXPECT_CALL(mockServer, getHostName()).WillOnce(ReturnRef(serverHost));
    EXPECT_CALL(mockServer, getPort()).WillOnce(Return(serverPort));
    EXPECT_CALL(mockServer, allocateClientSession(_, _, _, _)).Times(1);

    ServerManagerMock& serverMgrMock = *std::dynamic_pointer_cast<ServerManagerMock>(serverManager);
    EXPECT_CALL(serverMgrMock, tryGetAvailableServer(_, _))
    .Times(MAX_GET_SERVER_RETRIES - 1)
    .WillOnce(Return(false))
    .WillOnce(DoAll(SetArgReferee<1>(freeServer), Return(true)));

    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());
    std::string clientId;
    clientManager->connectClient(credentials, "", clientId);

    ClientServerSession out_serverSession;
    ASSERT_NO_THROW(clientManager->openClientDbSession(clientId, dbName, out_serverSession));

    ASSERT_EQ(serverHost, out_serverSession.serverHostName);
    ASSERT_EQ(serverPort, out_serverSession.serverPort);
    ASSERT_EQ(clientId, out_serverSession.clientSessionId);
    //the database session id is automatically created
}

//DIsabled for speed. Enable when you refactor constants into a configurable file
TEST_F(ClientManagerTest, DISABLED_openClientDbSession_SuccessRemoteServer)
{
    // Setup the user manager to successfully connect a client
    UserManagerMock& userMgrMock = *std::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_, _, _))
    .WillOnce(DoAll(testing::SetArgReferee<2>(user), Return(true)));

    ServerManagerMock& serverMgrMock = *std::dynamic_pointer_cast<ServerManagerMock>(serverManager);
    EXPECT_CALL(serverMgrMock, tryGetAvailableServer(_, _))
    .Times(MAX_GET_SERVER_RETRIES)
    .WillRepeatedly(Return(false));

    PeerManagerMock& peerMgrMock = *std::dynamic_pointer_cast<PeerManagerMock>(peerManager);
    std::string remoteClientId = "remoteClientId";
    std::string remoteDbId = "dbSessionId";
    std::string remoteServerHost = "remote";
    std::uint32_t remoteServerPort = 46000;
    ClientServerSession serverSession {remoteClientId, remoteDbId, remoteServerHost, remoteServerPort};

    EXPECT_CALL(peerMgrMock, tryGetRemoteServer(_, _))
    .Times(1)
    .WillOnce(DoAll(SetArgReferee<1>(serverSession), Return(true)));

    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());
    std::string clientId;
    clientManager->connectClient(credentials, "", clientId);

    ClientServerSession out_serverSession;
    ASSERT_NO_THROW(clientManager->openClientDbSession(clientId, dbName, out_serverSession));

    ASSERT_EQ(remoteServerHost, out_serverSession.serverHostName);
    ASSERT_EQ(remoteServerPort, out_serverSession.serverPort);
    ASSERT_EQ(remoteClientId, out_serverSession.clientSessionId);
    //the database session id is automatically created
}

//DIsabled for speed. Enable when you refactor constants into a configurable file
TEST_F(ClientManagerTest, DISABLED_openClientDbSession_FailBecauseNoAvailableServer)
{
    // Setup the user manager to successfully connect a client
    UserManagerMock& userMgrMock = *std::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_, _, _))
    .WillOnce(DoAll(testing::SetArgReferee<2>(user), Return(true)));

    ServerManagerMock& serverMgrMock = *std::dynamic_pointer_cast<ServerManagerMock>(serverManager);
    EXPECT_CALL(serverMgrMock, tryGetAvailableServer(_, _))
    .Times(MAX_GET_SERVER_RETRIES)
    .WillRepeatedly(Return(false));

    PeerManagerMock& peerMgrMock = *std::dynamic_pointer_cast<PeerManagerMock>(peerManager);

    EXPECT_CALL(peerMgrMock, tryGetRemoteServer(_, _))
    .Times(1)
    .WillOnce(Return(false));

    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());
    std::string clientId;
    clientManager->connectClient(credentials, "", clientId);

    ClientServerSession out_serverSession;
    ASSERT_THROW(clientManager->openClientDbSession(clientId, dbName, out_serverSession), NoAvailableServerException);
}


TEST_F(ClientManagerTest, closeClientDbSession_FailsBecauseThereIsNoClient)
{
    std::string out_clientId;
    std::string out_sessionId;

    PeerManagerMock& peerMgrMock = *std::dynamic_pointer_cast<PeerManagerMock>(peerManager);

    EXPECT_CALL(peerMgrMock, isRemoteClientSession(_))
    .Times(1)
    .WillOnce(Return(false));

    //There is no client connected.
    ASSERT_THROW(clientManager->closeClientDbSession(out_clientId, out_sessionId), InexistentClientException);
}

TEST_F(ClientManagerTest, closeClientDbSession_NoOpenSessions)
{
    std::string out_clientId;
    std::string out_sessionId;

    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());

    UserManagerMock& userMgrMock = *std::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_, _, _))
    .WillOnce(DoAll(testing::SetArgReferee<2>(user), Return(true)));

    clientManager->connectClient(credentials, "", out_clientId);

    //Will succeed
    ASSERT_NO_THROW(clientManager->closeClientDbSession(out_clientId, out_sessionId));
}

TEST_F(ClientManagerTest, closeClientDbSession_RemoteSession)
{
    std::string out_clientId;
    std::string out_sessionId;

    PeerManagerMock& peerMgrMock = *std::dynamic_pointer_cast<PeerManagerMock>(peerManager);

    EXPECT_CALL(peerMgrMock, isRemoteClientSession(_))
    .WillOnce(Return(true));

    EXPECT_CALL(peerMgrMock, releaseServer(_))
    .Times(1);

    //There is no client connected.
    ASSERT_NO_THROW(clientManager->closeClientDbSession(out_clientId, out_sessionId));
}

TEST_F(ClientManagerTest, keepClientAlive_InexistentClient)
{
    ASSERT_THROW(clientManager->keepClientAlive("testId"), InexistentClientException);
}

TEST_F(ClientManagerTest, keepClientAlive_ValidClient)
{
    std::string out_clientId;

    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());

    UserManagerMock& userMgrMock = *std::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_, _, _))
    .WillOnce(DoAll(testing::SetArgReferee<2>(user), Return(true)));

    clientManager->connectClient(credentials, "", out_clientId);

    ASSERT_NO_THROW(clientManager->keepClientAlive(out_clientId));
}

TEST_F(ClientManagerTest, keepClientAlive_ExpiredClient)
{
    std::string out_clientId;

    rasmgr::ClientCredentials credentials(user->getName(), user->getPassword());
    std::shared_ptr<Server> server(new MockRasServer());

    MockRasServer& serverMock = *std::dynamic_pointer_cast<MockRasServer>(server);
    EXPECT_CALL(serverMock, isClientAlive(_)).WillRepeatedly(Return(false));

    UserManagerMock& userMgrMock = *std::dynamic_pointer_cast<UserManagerMock>(userManager);
    EXPECT_CALL(userMgrMock, tryGetUser(_, _, _))
    .WillOnce(DoAll(testing::SetArgReferee<2>(user), Return(true)));

    clientManager->connectClient(credentials, "", out_clientId);

    // The client exists
    ASSERT_NO_THROW(clientManager->keepClientAlive(out_clientId));

    //Wait for the client to die
    usleep(std::uint32_t(config.getCleanupInterval() * 1000 * 4));

    ASSERT_THROW(clientManager->keepClientAlive(out_clientId), InexistentClientException);
}

}
}
