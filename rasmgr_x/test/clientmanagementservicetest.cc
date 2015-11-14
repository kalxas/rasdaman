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

#include "../../common/src/unittest/gtest.h"
#include "../../common/src/mock/gmock.h"
#include "../../common/src/grpc/grpcutils.hh"

#include "../src/clientmanager.hh"
#include "../src/servermanager.hh"
#include "../src/constants.hh"
#include "../src/clientmanagementservice.hh"

#include "mocks/servermanagermock.hh"
#include "mocks/clientmanagermock.hh"
#include "mocks/usermanagermock.hh"
#include "mocks/servergroupfactorymock.hh"
#include "mocks/mockrasserver.hh"

namespace rasmgr
{
namespace test
{

using ::testing::AtLeast;                     // #1
using ::testing::_;
using ::testing::Return;
using ::testing::DoAll;
using ::testing::SetArgReferee;
using ::testing::Throw;

using ::grpc::Status;

class ClientManagementServiceTest:public ::testing::Test
{
protected:
    ClientManagementServiceTest()
    {
        this->userManager.reset(new UserManagerMock());

        ClientManagerConfig config;
        this->clientManager.reset(new ClientManagerMock(config,userManager));

        ServerManagerConfig serverManagerConfig;
        this->serverGroupFactory.reset(new ServerGroupFactoryMock());

        this->serverManager.reset(new ServerManagerMock(serverManagerConfig, serverGroupFactory));

        this->service.reset(new ClientManagementService(this->clientManager, this->serverManager));
    }

    boost::shared_ptr<ServerManager> serverManager;
    boost::shared_ptr<ClientManager> clientManager;
    boost::shared_ptr<UserManager> userManager;
    boost::shared_ptr<ServerGroupFactory> serverGroupFactory;

    boost::shared_ptr<ClientManagementService> service;
};


TEST_F(ClientManagementServiceTest, ConnectSuccess)
{
    rasnet::service::ConnectReq request;
    request.set_username("user");
    request.set_passwordhash("password");

    std::string clientId = "clientId";

    rasnet::service::ConnectRepl reply;

    ClientManagerMock& clientMgrMock = *boost::dynamic_pointer_cast<ClientManagerMock>(clientManager);

    EXPECT_CALL(clientMgrMock, connectClient(_, _))
    .WillOnce(SetArgReferee<1>(clientId));

    Status status = this->service->Connect(NULL, &request, &reply);

    ASSERT_TRUE(status.ok());
    ASSERT_EQ(clientId, reply.clientuuid());
    ASSERT_EQ(this->clientManager->getConfig().getClientLifeTime(), reply.keepalivetimeout());
}

TEST_F(ClientManagementServiceTest, ConnectFailure)
{
    rasnet::service::ConnectRepl reply;
    rasnet::service::ConnectReq request;
    request.set_username("user");
    request.set_passwordhash("password");

    std::runtime_error exception("errorMessage");
    grpc::Status expectedStatus = common::GrpcUtils::convertExceptionToStatus(exception);

    ClientManagerMock& clientMgrMock = *boost::dynamic_pointer_cast<ClientManagerMock>(clientManager);

    EXPECT_CALL(clientMgrMock, connectClient(_, _))
    .WillOnce(Throw(exception));

    Status status = this->service->Connect(NULL, &request, &reply);

    ASSERT_FALSE(status.ok());
    ASSERT_EQ(status.error_code(), expectedStatus.error_code());
    ASSERT_EQ(status.error_message(), expectedStatus.error_message());
}

TEST_F(ClientManagementServiceTest, DisconnectFailure)
{
    std::runtime_error exception("errorMessage");
    grpc::Status expectedStatus = common::GrpcUtils::convertExceptionToStatus(exception);

    ClientManagerMock& clientMgrMock = *boost::dynamic_pointer_cast<ClientManagerMock>(clientManager);
    EXPECT_CALL(clientMgrMock, disconnectClient(_)).WillOnce(Throw(exception));

    rasnet::service::DisconnectReq request;
    rasnet::service::Void reply;


    Status status = this->service->Disconnect(NULL, &request, &reply);
    ASSERT_FALSE(status.ok());
    ASSERT_EQ(status.error_code(), expectedStatus.error_code());
    ASSERT_EQ(status.error_message(), expectedStatus.error_message());
}

TEST_F(ClientManagementServiceTest, DisconnectSuccess)
{
    ClientManagerMock& clientMgrMock = *boost::dynamic_pointer_cast<ClientManagerMock>(clientManager);
    EXPECT_CALL(clientMgrMock, disconnectClient(_));

    rasnet::service::DisconnectReq request;
    rasnet::service::Void reply;


    Status status = this->service->Disconnect(NULL, &request, &reply);
    ASSERT_TRUE(status.ok());
}

TEST_F(ClientManagementServiceTest, OpenDbSuccessInOneAttempt)
{
    std::string clientUUID = "clientUUID";
    std::string hostName = "hostName";
    boost::int32_t port = 7001;

    ClientManagerMock& clientMgrMock = *boost::dynamic_pointer_cast<ClientManagerMock>(clientManager);
    EXPECT_CALL(clientMgrMock, openClientDbSession(_, _, _, _)).WillOnce(SetArgReferee<3>(clientUUID));

    boost::shared_ptr<Server> freeServer(new MockRasServer());
    MockRasServer& mockServer = *boost::dynamic_pointer_cast<MockRasServer>(freeServer);
    EXPECT_CALL(mockServer, getHostName()).WillOnce(Return(hostName));
    EXPECT_CALL(mockServer, getPort()).WillOnce(Return(port));

    ServerManagerMock& serverMgrMock = *boost::dynamic_pointer_cast<ServerManagerMock>(serverManager);
    EXPECT_CALL(serverMgrMock, tryGetFreeServer(_,_)).WillOnce(DoAll(SetArgReferee<1>(freeServer), Return(true)));

    rasnet::service::OpenDbReq request;
    rasnet::service::OpenDbRepl response;

    Status status = this->service->OpenDb(NULL, &request, &response);
    ASSERT_TRUE(status.ok());
}

TEST_F(ClientManagementServiceTest, OpenDbSuccessInLastAttempt)
{
    std::string clientUUID = "clientUUID";
    std::string hostName = "hostName";
    boost::int32_t port = 7001;

    ClientManagerMock& clientMgrMock = *boost::dynamic_pointer_cast<ClientManagerMock>(clientManager);
    EXPECT_CALL(clientMgrMock, openClientDbSession(_, _, _, _)).WillOnce(SetArgReferee<3>(clientUUID));

    boost::shared_ptr<Server> freeServer(new MockRasServer());
    MockRasServer& mockServer = *boost::dynamic_pointer_cast<MockRasServer>(freeServer);
    EXPECT_CALL(mockServer, getHostName()).WillOnce(Return(hostName));
    EXPECT_CALL(mockServer, getPort()).WillOnce(Return(port));

    ServerManagerMock& serverMgrMock = *boost::dynamic_pointer_cast<ServerManagerMock>(serverManager);
    EXPECT_CALL(serverMgrMock, tryGetFreeServer(_,_))
    .Times(MAX_GET_SERVER_RETRIES - 1)
    .WillOnce(Return(false))
    .WillOnce(DoAll(SetArgReferee<1>(freeServer), Return(true)));

    rasnet::service::OpenDbReq request;
    rasnet::service::OpenDbRepl response;

    Status status = this->service->OpenDb(NULL, &request, &response);
    ASSERT_TRUE(status.ok());
}

TEST_F(ClientManagementServiceTest, OpenDbFailure)
{
    ServerManagerMock& serverMgrMock = *boost::dynamic_pointer_cast<ServerManagerMock>(serverManager);
    EXPECT_CALL(serverMgrMock, tryGetFreeServer(_,_)).WillRepeatedly(Return(false));

    rasnet::service::OpenDbReq request;
    rasnet::service::OpenDbRepl response;

    Status status = this->service->OpenDb(NULL, &request, &response);
    ASSERT_FALSE(status.ok());
}

TEST_F(ClientManagementServiceTest, CloseDbFailure)
{
    std::runtime_error exception("errorMessage");
    grpc::Status expectedStatus = common::GrpcUtils::convertExceptionToStatus(exception);

    ClientManagerMock& clientMgrMock = *boost::dynamic_pointer_cast<ClientManagerMock>(clientManager);
    EXPECT_CALL(clientMgrMock, closeClientDbSession(_, _)).WillOnce(Throw(exception));

    rasnet::service::CloseDbReq request;
    rasnet::service::Void reply;

    Status status = this->service->CloseDb(NULL, &request, &reply);
    ASSERT_FALSE(status.ok());
    ASSERT_EQ(status.error_code(), expectedStatus.error_code());
    ASSERT_EQ(status.error_message(), expectedStatus.error_message());
}

TEST_F(ClientManagementServiceTest, CloseDbSuccess)
{
    ClientManagerMock& clientMgrMock = *boost::dynamic_pointer_cast<ClientManagerMock>(clientManager);
    EXPECT_CALL(clientMgrMock, closeClientDbSession(_, _));

    rasnet::service::CloseDbReq request;
    rasnet::service::Void reply;

    Status status = this->service->CloseDb(NULL, &request, &reply);
    ASSERT_TRUE(status.ok());
}


TEST_F(ClientManagementServiceTest, KeepAliveFailure)
{
    std::runtime_error exception("errorMessage");
    grpc::Status expectedStatus = common::GrpcUtils::convertExceptionToStatus(exception);

    ClientManagerMock& clientMgrMock = *boost::dynamic_pointer_cast<ClientManagerMock>(clientManager);
    EXPECT_CALL(clientMgrMock, keepClientAlive(_)).WillOnce(Throw(exception));

    rasnet::service::KeepAliveReq request;
    rasnet::service::Void reply;

    Status status = this->service->KeepAlive(NULL, &request, &reply);
    ASSERT_FALSE(status.ok());
    ASSERT_EQ(status.error_code(), expectedStatus.error_code());
    ASSERT_EQ(status.error_message(), expectedStatus.error_message());
}

TEST_F(ClientManagementServiceTest, KeepAliveSuccess)
{
    ClientManagerMock& clientMgrMock = *boost::dynamic_pointer_cast<ClientManagerMock>(clientManager);
    EXPECT_CALL(clientMgrMock, keepClientAlive(_));

    rasnet::service::KeepAliveReq request;
    rasnet::service::Void reply;

    Status status = this->service->KeepAlive(NULL, &request, &reply);
    ASSERT_TRUE(status.ok());
}

}
}
