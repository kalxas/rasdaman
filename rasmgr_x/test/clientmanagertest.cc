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

#include "mocks/mockrasserver.hh"

using rasmgr::Client;
using rasmgr::RasMgrConfig;
using rasmgr::RasServer;
using rasmgr::UserManager;
using rasmgr::ClientManager;
using ::testing::AtLeast;                     // #1
using ::testing::_;
using ::testing::Return;

class ClientManagerTest:public ::testing::Test
{
protected:
    ClientManagerTest():clientId("clientId"),userName("userName"),userPassword("userPassword"),dbRights(true, true),
            dbName("dbName"), sessionId("sessionId"), serverHost("tcp://localhost"),serverPort(7010)
    {
        adminRights.setAccessControlRights(true);
        adminRights.setInfoRights(true);
        adminRights.setServerAdminRights(true);
        adminRights.setSystemConfigRights(true);

        rasmgr::RasMgrConfig::getInstance()->setClientLifeTime(10);
        rasmgr::RasMgrConfig::getInstance()->setClientManagementGarbageCollectionInterval(10);
        userManager.reset(new UserManager());
        clientManager.reset(new ClientManager(userManager));

    }

    rasmgr::UserAdminRights adminRights;
    rasmgr::UserDatabaseRights dbRights;
    std::string userName;
    std::string userPassword;
    std::string clientId;
    std::string dbName;
    std::string sessionId;

    std::string serverHost;
    boost::int32_t serverPort;
    boost::shared_ptr<rasmgr::DatabaseHost> dbHost;
    boost::shared_ptr<UserManager> userManager;
    boost::shared_ptr<ClientManager> clientManager;
};

/**
 * Test what happens when a client with bad credentials connects
 */
TEST_F(ClientManagerTest, connectClientFail)
{
    std::string badUser="badUser";
    std::string badPassword = "badUser";
    std::string out_clientId;
    rasmgr::ClientCredentials credentials(badUser, badPassword);

    ASSERT_ANY_THROW(clientManager->connectClient(credentials, out_clientId));

    //Good user name, bad password
    userManager->defineUser(userName, badPassword, dbRights, adminRights);

    ASSERT_ANY_THROW(clientManager->connectClient(credentials, out_clientId));

    rasmgr::UserDatabaseRights badRights(false,false);
    userManager->changeUserPassword(userName, userPassword);
    userManager->changeUserDatabaseRights(userName, badRights);
    ASSERT_ANY_THROW(clientManager->connectClient(credentials, out_clientId));
}

TEST_F(ClientManagerTest, connectClientSuccess)
{
    std::string out_clientId;
    rasmgr::ClientCredentials credentials(userName, userPassword);
    userManager->defineUser(userName, userPassword, dbRights, adminRights);

    ASSERT_NO_THROW(clientManager->connectClient(credentials, out_clientId));
}

TEST_F(ClientManagerTest, disconnectClient)
{
    std::string out_clientId;
    rasmgr::ClientCredentials credentials(userName, userPassword);
    userManager->defineUser(userName, userPassword, dbRights, adminRights);

    ASSERT_NO_THROW(clientManager->disconnectClient(out_clientId));

    ASSERT_NO_THROW(clientManager->connectClient(credentials, out_clientId));

    ASSERT_NO_THROW(clientManager->disconnectClient(out_clientId));
}

TEST_F(ClientManagerTest, openClientDbSessionFail)
{
    std::string out_clientId;
    std::string out_sessionId;

    rasmgr::RasMgrConfig::getInstance()->setClientLifeTime(1);
    rasmgr::RasMgrConfig::getInstance()->setClientManagementGarbageCollectionInterval(1);


    rasmgr::ClientCredentials credentials(userName, userPassword);
    boost::shared_ptr<RasServer> server(new MockRasServer(serverHost, serverPort, dbHost));
    userManager->defineUser(userName, userPassword, dbRights, adminRights);

    //  EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(_, _ ,dbName, _)).Times(1);
    EXPECT_CALL(*((MockRasServer*)server.get()), isClientAlive(_)).WillRepeatedly(Return(false));

    ASSERT_NO_THROW(clientManager->connectClient(credentials, out_clientId));

    //Wait for the client to die
    usleep(rasmgr::RasMgrConfig::getInstance()->getClientLifeTime()*1000*10);

    ASSERT_ANY_THROW(clientManager->openClientDbSession(out_clientId, dbName, server, out_sessionId));
}

TEST_F(ClientManagerTest, openClientDbSession)
{
    std::string out_clientId;
    std::string out_sessionId;

    rasmgr::ClientCredentials credentials(userName, userPassword);
    boost::shared_ptr<RasServer> server(new MockRasServer(serverHost, serverPort, dbHost));
    userManager->defineUser(userName, userPassword, dbRights, adminRights);

    EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(_, _ ,dbName, _)).Times(1);
    EXPECT_CALL(*((MockRasServer*)server.get()), isClientAlive(_)).WillRepeatedly(Return(true));

    ASSERT_NO_THROW(clientManager->connectClient(credentials, out_clientId));

    ASSERT_NO_THROW(clientManager->openClientDbSession(out_clientId, dbName, server, out_sessionId));
}


TEST_F(ClientManagerTest, closeClientDbSession)
{
    std::string out_clientId;
    std::string out_sessionId;

    rasmgr::ClientCredentials credentials(userName, userPassword);
    boost::shared_ptr<RasServer> server(new MockRasServer(serverHost, serverPort, dbHost));
    userManager->defineUser(userName, userPassword, dbRights, adminRights);

    EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(_, _ ,dbName, _)).Times(1);
    EXPECT_CALL(*((MockRasServer*)server.get()), deallocateClientSession(_,_)).Times(1);
    EXPECT_CALL(*((MockRasServer*)server.get()), isClientAlive(_)).WillRepeatedly(Return(true));

    ASSERT_NO_THROW(clientManager->connectClient(credentials, out_clientId));

    ASSERT_NO_THROW(clientManager->closeClientDbSession(out_clientId, out_sessionId));

    ASSERT_NO_THROW(clientManager->openClientDbSession(out_clientId, dbName, server, out_sessionId));

    ASSERT_NO_THROW(clientManager->closeClientDbSession(out_clientId, out_sessionId));
}

TEST_F(ClientManagerTest, keepClientAliveFail)
{
    std::string out_clientId;

    rasmgr::ClientCredentials credentials(userName, userPassword);

    ASSERT_ANY_THROW(clientManager->keepClientAlive(out_clientId));

    rasmgr::RasMgrConfig::getInstance()->setClientLifeTime(1);
    rasmgr::RasMgrConfig::getInstance()->setClientManagementGarbageCollectionInterval(1);

    boost::shared_ptr<RasServer> server(new MockRasServer(serverHost, serverPort, dbHost));
    userManager->defineUser(userName, userPassword, dbRights, adminRights);

    //  EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(_, _ ,dbName, _)).Times(1);
    EXPECT_CALL(*((MockRasServer*)server.get()), isClientAlive(_)).WillRepeatedly(Return(false));

    ASSERT_NO_THROW(clientManager->connectClient(credentials, out_clientId));

    //Wait for the client to die
    usleep(rasmgr::RasMgrConfig::getInstance()->getClientLifeTime()*1000*10);

    ASSERT_ANY_THROW(clientManager->keepClientAlive(out_clientId));

}
