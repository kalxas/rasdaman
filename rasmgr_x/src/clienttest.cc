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

#include "mocks/mockrasserver.hh"

using rasmgr::Client;
using rasmgr::RasMgrConfig;
using rasmgr::RasServer;

using ::testing::AtLeast;                     // #1
using ::testing::_;
using ::testing::Return;

class ClientTest:public ::testing::Test
{
protected:
    ClientTest():clientId("clientId"),userName("userName"),userPassword("userPassword"),dbRights(true, true),
            dbName("dbName"), sessionId("sessionId"), serverHost("tcp://localhost"),serverPort(7010)
    {
        adminRights.setAccessControlRights(true);
        adminRights.setInfoRights(true);
        adminRights.setServerAdminRights(true);
        adminRights.setSystemConfigRights(true);

        user.reset(new rasmgr::User(userName, userPassword, dbRights, adminRights));

        rasmgr::RasMgrConfig::getInstance()->setClientLifeTime(100);

        client.reset(new Client(clientId,user, 100));
    }

    rasmgr::UserAdminRights adminRights;
    rasmgr::UserDatabaseRights dbRights;
    std::string userName;
    std::string userPassword;
    std::string clientId;
    std::string dbName;
    std::string sessionId;
    boost::shared_ptr<rasmgr::User> user;
    boost::shared_ptr<rasmgr::Client> client;

    std::string serverHost;
    boost::int32_t serverPort;
    boost::shared_ptr<rasmgr::DatabaseHost> dbHost;
};

TEST_F(ClientTest, isAliveNoSessions)
{

    ASSERT_TRUE(client->isAlive());

    usleep(RasMgrConfig::getInstance()->getClientLifeTime()*1000);

    ASSERT_FALSE(client->isAlive());
}

TEST_F(ClientTest, isAliveWSessions)
{
    boost::shared_ptr<RasServer> server(new MockRasServer(serverHost, serverPort, dbHost));
    std::string out_sessionId;

    EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(clientId, _ ,dbName, _)).Times(1);
    EXPECT_CALL(*((MockRasServer*)server.get()), isClientAlive(clientId)).WillOnce(Return(true));
    ASSERT_TRUE(client->isAlive());

    client->addDbSession(dbName,server, out_sessionId);

    usleep(RasMgrConfig::getInstance()->getClientLifeTime()*1000);

    //This will now return true because the server will confirm that the client is alive
    ASSERT_TRUE(client->isAlive());
}

TEST_F(ClientTest, resetLiveliness)
{
    ASSERT_TRUE(client->isAlive());

    usleep(RasMgrConfig::getInstance()->getClientLifeTime()*1000);

    ASSERT_FALSE(client->isAlive());

    client->resetLiveliness();

    ASSERT_TRUE(client->isAlive());
}


TEST_F(ClientTest, addDbSessionFail)
{
    //This method will throw an exception because the user does not have any rights
    // on the database.
    std::string out_sessionId;
    rasmgr::UserDatabaseRights noDbRights(false,false);
    boost::shared_ptr<RasServer> server(new MockRasServer(serverHost, serverPort, dbHost));

    user->setDefaultDbRights(noDbRights);
    EXPECT_ANY_THROW(client->addDbSession(dbName, server, out_sessionId));
}


TEST_F(ClientTest, addDbSessionSuccess)
{
    std::string out_sessionId;
    boost::shared_ptr<RasServer> server(new MockRasServer(serverHost, serverPort, dbHost));

    EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(clientId, _ ,dbName, _)).Times(1);

    EXPECT_NO_THROW(client->addDbSession(dbName, server, out_sessionId));
}

//
////Test removeDbSession when server is alive
TEST_F(ClientTest, removeDbSession)
{
    std::string out_sessionId;
    boost::shared_ptr<RasServer> server(new MockRasServer(serverHost, serverPort, dbHost));


    EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(clientId, _ ,dbName, _)).Times(1);
    EXPECT_CALL(*((MockRasServer*)server.get()), deallocateClientSession(clientId,_)).Times(1);

    EXPECT_NO_THROW(client->addDbSession(dbName,server, out_sessionId));
    EXPECT_NO_THROW(client->removeDbSession(out_sessionId));
}



TEST_F(ClientTest, removeClientFromServers)
{
  std::string out_sessionId;
  boost::shared_ptr<RasServer> server(new MockRasServer(serverHost, serverPort, dbHost));


  EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(clientId, _ ,dbName, _)).Times(1);
  EXPECT_CALL(*((MockRasServer*)server.get()), deallocateClientSession(clientId,_)).Times(1);

  EXPECT_NO_THROW(client->addDbSession(dbName,server, out_sessionId));
  EXPECT_NO_THROW(client->removeClientFromServers());
}
