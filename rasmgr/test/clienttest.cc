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

#include "rasmgr/src/messages/rasmgrmess.pb.h"
#include "rasmgr/src/client.hh"
#include "rasmgr/src/user.hh"
#include "rasmgr/src/userdatabaserights.hh"
#include "rasmgr/src/useradminrights.hh"
#include "rasmgr/src/rasmgrconfig.hh"
#include "rasmgr/src/server.hh"
#include "rasmgr/src/cpuscheduler.hh"

#include "mocks/mockrasserver.hh"
#pragma GCC diagnostic ignored "-Wreorder"

namespace rasmgr
{
namespace test
{
using rasmgr::Client;
using rasmgr::RasMgrConfig;
using rasmgr::Server;

using ::testing::AtLeast;                     // #1
using ::testing::_;
using ::testing::Return;


class ClientTest: public ::testing::Test
{
protected:
    ClientTest(): clientId(1), userName("userName"), userPassword("userPassword"), dbRights(true, true),
        dbName("dbName"), sessionId(2), clientLifeTime(10), serverHost("tcp://localhost"), serverPort(7010)
    {
        adminRights.setAccessControlRights(true);
        adminRights.setInfoRights(true);
        adminRights.setServerAdminRights(true);
        adminRights.setSystemConfigRights(true);

        user.reset(new rasmgr::User(userName, userPassword, dbRights, adminRights));
                
        client.reset(new Client(clientId, user, clientLifeTime, "", std::make_shared<CpuScheduler>(10)));
    }
    
    std::uint32_t clientId;
    std::string userName;
    std::string userPassword;
    rasmgr::UserDatabaseRights dbRights;
    rasmgr::UserAdminRights adminRights;
    std::string dbName;
    std::uint32_t sessionId;
    std::shared_ptr<rasmgr::User> user;
    std::shared_ptr<rasmgr::Client> client;

    std::int32_t clientLifeTime;
    std::string serverHost;
    std::int32_t serverPort;
    std::shared_ptr<rasmgr::DatabaseHost> dbHost;
};

TEST_F(ClientTest, isAliveSucceedsWhenTheClientTimerHasNotExpired)
{
    //Initially the client is alive
    ASSERT_TRUE(client->isAlive());
}

TEST_F(ClientTest, isAliveFailsWhenTheClientTimerHasExpiredNoServers)
{
    //sleep so that the client's time expires
    usleep(std::uint32_t(this->clientLifeTime) * 1000);

    //The client will now be dead
    ASSERT_FALSE(client->isAlive());
}

//Test when the client has active sessions with a server
TEST_F(ClientTest, isAliveSucceedsWhenClientAliveOnServers)
{
    std::shared_ptr<Server> server(new MockRasServer());
    std::uint32_t out_sessionId = 1;
    
    EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(clientId, _, _, _, out_sessionId, dbName, _)).Times(1);
    EXPECT_CALL(*((MockRasServer*)server.get()), isClientAlive(clientId)).WillOnce(Return(true));
    client->addDbSession(dbName, server, out_sessionId);

    usleep(std::uint32_t(this->clientLifeTime) * 1000);

    //This will now return true because the server will confirm that the client is alive
    ASSERT_TRUE(client->isAlive());
}

TEST_F(ClientTest, isAliveFailsWhenClientDeadOnServers)
{
    std::shared_ptr<Server> server(new MockRasServer());
    std::uint32_t out_sessionId = 2;

    EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(clientId, _, _, _, out_sessionId, dbName, _)).Times(1);
    EXPECT_CALL(*((MockRasServer*)server.get()), isClientAlive(clientId)).WillOnce(Return(false));
    ASSERT_TRUE(client->isAlive());

    client->addDbSession(dbName, server, out_sessionId);

    usleep(std::uint32_t(this->clientLifeTime) * 1000);

    //This will now return true because the server will confirm that the client is alive
    ASSERT_FALSE(client->isAlive());
}

TEST_F(ClientTest, resetLiveliness)
{
    ASSERT_TRUE(client->isAlive());

    usleep(std::uint32_t(this->clientLifeTime) * 1000);

    ASSERT_FALSE(client->isAlive());

    client->resetLiveliness();

    ASSERT_TRUE(client->isAlive());
}


TEST_F(ClientTest, addDbSessionFailWhenUserDoesNotHaveRights)
{
    //This method will throw an exception because the user does not have any rights
    // on the database.
    std::uint32_t out_sessionId = 0;
    rasmgr::UserDatabaseRights noDbRights(false, false);
    std::shared_ptr<Server> server(new MockRasServer());

    user->setDefaultDbRights(noDbRights);
    EXPECT_ANY_THROW(client->addDbSession(dbName, server, out_sessionId));
}


TEST_F(ClientTest, addDbSessionSuccess)
{
    std::uint32_t out_sessionId = 1;
    std::shared_ptr<Server> server(new MockRasServer());

    EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(clientId, _, _, _, _, dbName, _)).Times(1);

    EXPECT_NO_THROW(client->addDbSession(dbName, server, out_sessionId));
}

//
////Test removeDbSession when server is alive
TEST_F(ClientTest, removeDbSession)
{
    std::uint32_t out_sessionId = 0;
    std::shared_ptr<Server> server(new MockRasServer());

    EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(clientId, _, _, _, _,  dbName, _)).Times(1);
    EXPECT_CALL(*((MockRasServer*)server.get()), deallocateClientSession(clientId, _)).Times(1);

    EXPECT_NO_THROW(client->addDbSession(dbName, server, out_sessionId));
    EXPECT_NO_THROW(client->removeDbSession(out_sessionId));
}

TEST_F(ClientTest, removeClientFromServers)
{
    std::uint32_t out_sessionId = 0;
    std::shared_ptr<Server> server(new MockRasServer());


    EXPECT_CALL(*((MockRasServer*)server.get()), allocateClientSession(clientId, _, _, _, _, dbName, _)).Times(1);
    EXPECT_CALL(*((MockRasServer*)server.get()), deallocateClientSession(clientId, _)).Times(1);

    EXPECT_NO_THROW(client->addDbSession(dbName, server, out_sessionId));
    EXPECT_NO_THROW(client->removeClientFromServer());
}

}
}
