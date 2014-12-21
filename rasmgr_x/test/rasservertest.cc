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

#include "../../rasnet/src/service/server/servicemanager.hh"
#include "../../rasnet/src/messages/rassrvr_rasmgr_service.pb.h"

#include "../src/rasserver.hh"

using rasmgr::RasServer;
using rasnet::ServiceManager;
using rasnet::service::RasServerService;

using ::testing::AtLeast;                     // #1
using ::testing::_;
using ::testing::Return;

class MockRasServerService:public rasnet::service::RasServerService
{
    virtual void AllocateClient(::google::protobuf::RpcController* controller,
                                const ::rasnet::service::AllocateClientReq* request,
                                ::rasnet::service::Void* response,
                                ::google::protobuf::Closure* done)
    {
    }

    virtual void DeallocateClient(::google::protobuf::RpcController* controller,
                                  const ::rasnet::service::DeallocateClientReq* request,
                                  ::rasnet::service::Void* response,
                                  ::google::protobuf::Closure* done)
    {}

    virtual void Close(::google::protobuf::RpcController* controller,
                       const ::rasnet::service::CloseServerReq* request,
                       ::rasnet::service::Void* response,
                       ::google::protobuf::Closure* done)
    {}

    virtual void GetClientStatus(::google::protobuf::RpcController* controller,
                                 const ::rasnet::service::ClientStatusReq* request,
                                 ::rasnet::service::ClientStatusRepl* response,
                                 ::google::protobuf::Closure* done)
    {
        response->set_status(rasnet::service::ClientStatusRepl_Status_ALIVE);
    }

    virtual void GetServerStatus(::google::protobuf::RpcController* controller,
                                 const ::rasnet::service::ServerStatusReq* request,
                                 ::rasnet::service::ServerStatusRepl* response,
                                 ::google::protobuf::Closure* done)
    {
        response->set_clientqueuesize(1);
    }
}
;

class FailingMockRasServerService:public rasnet::service::RasServerService
{
    virtual void AllocateClient(::google::protobuf::RpcController* controller,
                                const ::rasnet::service::AllocateClientReq* request,
                                ::rasnet::service::Void* response,
                                ::google::protobuf::Closure* done)
    {
        controller->SetFailed("Failed");
    }

    virtual void DeallocateClient(::google::protobuf::RpcController* controller,
                                  const ::rasnet::service::DeallocateClientReq* request,
                                  ::rasnet::service::Void* response,
                                  ::google::protobuf::Closure* done)
    {
        controller->SetFailed("Failed");
    }

    virtual void Close(::google::protobuf::RpcController* controller,
                       const ::rasnet::service::CloseServerReq* request,
                       ::rasnet::service::Void* response,
                       ::google::protobuf::Closure* done)
    {}

    virtual void GetClientStatus(::google::protobuf::RpcController* controller,
                                 const ::rasnet::service::ClientStatusReq* request,
                                 ::rasnet::service::ClientStatusRepl* response,
                                 ::google::protobuf::Closure* done)
    {

        response->set_status(rasnet::service::ClientStatusRepl_Status_DEAD);
    }

    virtual void GetServerStatus(::google::protobuf::RpcController* controller,
                                 const ::rasnet::service::ServerStatusReq* request,
                                 ::rasnet::service::ServerStatusRepl* response,
                                 ::google::protobuf::Closure* done)
    {
        controller->SetFailed("Failed");
    }
}
;

class RasServerTest:public ::testing::Test
{
protected:
    RasServerTest()
    {
        hostName = "tcp://localhost";
        port = 7001;
        failPort = 7002;

        boost::shared_ptr<RasServerService> service (new MockRasServerService());

        boost::shared_ptr<RasServerService> failService (new FailingMockRasServerService());

        manager.reset((new ServiceManager()));
        manager->addService(service);
        manager->serve("tcp://*", port);

        managerFail.reset((new ServiceManager()));
        managerFail->addService(failService);
        managerFail->serve("tcp://*", failPort);

        dbHost.reset(new rasmgr::DatabaseHost("hostName", "connectString", "user","passwd"));

        server.reset(new rasmgr::RasServer(hostName, port, dbHost));
        failingServer.reset(new rasmgr::RasServer(hostName, failPort, dbHost));
    }


    std::string hostName;
    boost::int32_t port;
    boost::int32_t failPort;

    boost::scoped_ptr<ServiceManager> manager;
    boost::scoped_ptr<ServiceManager> managerFail;
    boost::shared_ptr<rasmgr::DatabaseHost> dbHost;
    boost::shared_ptr<rasmgr::RasServer> server;
    boost::shared_ptr<rasmgr::RasServer> failingServer;
};

TEST_F(RasServerTest, DISABLED_isAlive)
{
    ASSERT_TRUE(server->isAlive());
    ASSERT_FALSE(failingServer->isAlive());
}

TEST_F(RasServerTest, DISABLED_isClientAlive)
{
    ASSERT_TRUE(server->isClientAlive("test"));
    ASSERT_FALSE(failingServer->isClientAlive("test"));
}

TEST_F(RasServerTest, DISABLED_allocateClientSession)
{
    std::string clientId = "clientId";
    std::string sessionId = "sessionId";
    std::string dbName = "dbName";
    rasmgr::UserDatabaseRights dbRights(false,true);
    rasmgr::Database db(dbName);

    ASSERT_NO_THROW(server->registerServer(server->getServerId()));

    //This will fail because the database host does not have the database with the given name
    ASSERT_ANY_THROW(server->allocateClientSession(clientId, sessionId, dbName, dbRights));

    dbHost->addDbToHost(db);
    //Everything will work.
    ASSERT_NO_THROW(server->allocateClientSession(clientId, sessionId, dbName, dbRights));

    //Adding the same session twice will throw an exception
    ASSERT_ANY_THROW(server->allocateClientSession(clientId, sessionId, dbName, dbRights));

    //This will fail due to the server
    ASSERT_ANY_THROW(failingServer->allocateClientSession(clientId, sessionId, dbName, dbRights));

}

//Test if the database host connection is freed
TEST_F(RasServerTest, DISABLED_cleanupPerformed)
{
    std::string clientId = "clientId";
    std::string sessionId = "sessionId";
    std::string dbName = "dbName";
    rasmgr::UserDatabaseRights dbRights(false,true);
    rasmgr::Database db(dbName);
    boost::shared_ptr<rasmgr::DatabaseHost> dbh( new rasmgr::DatabaseHost("hostName", "connectString", "user","passwd"));

    dbh->addDbToHost(db);

    rasmgr::RasServer* srv = new RasServer(hostName, port, dbh);

    ASSERT_FALSE(dbh->isBusy());

    ASSERT_NO_THROW(srv->registerServer(srv->getServerId()));

    ASSERT_NO_THROW(srv->allocateClientSession(clientId, sessionId, dbName, dbRights));

    ASSERT_TRUE(dbh->isBusy());

    //Removing the server will also remove all its connections to the database host
    delete srv;

    ASSERT_FALSE(dbh->isBusy());

}

TEST_F(RasServerTest, DISABLED_deallocateClientSession)
{
    std::string clientId = "clientId";
    std::string sessionId = "sessionId";
    std::string dbName = "dbName";
    rasmgr::UserDatabaseRights dbRights(false,true);
    rasmgr::Database db(dbName);

    dbHost->addDbToHost(db);

    ASSERT_NO_THROW(server->registerServer(server->getServerId()));

    //Everything will work.
    ASSERT_NO_THROW(server->allocateClientSession(clientId, sessionId, dbName, dbRights));

    ASSERT_NO_THROW(server->deallocateClientSession(clientId, sessionId));

    ASSERT_ANY_THROW(failingServer->deallocateClientSession(clientId, sessionId));
}

TEST_F(RasServerTest, DISABLED_registerServer)
{
    ASSERT_ANY_THROW(server->registerServer("wrong"));

    ASSERT_NO_THROW(server->registerServer(server->getServerId()));

    ASSERT_NO_THROW(server->registerServer(server->getServerId()));
}

//TODO
//TEST_F(RasServerTest, stop)
//{
//}

TEST_F(RasServerTest, DISABLED_isStarting)
{
    ASSERT_TRUE(server->isStarting());

    ASSERT_NO_THROW(server->registerServer(server->getServerId()));

    //The server has already started
    ASSERT_FALSE(server->isStarting());

}


TEST_F(RasServerTest, DISABLED_isFree)
{
    ASSERT_NO_THROW(server->registerServer(server->getServerId()));

    ASSERT_TRUE(server->isFree());

    std::string clientId = "clientId";
    std::string sessionId = "sessionId";
    std::string dbName = "dbName";
    rasmgr::UserDatabaseRights dbRights(false,true);
    rasmgr::Database db(dbName);

    dbHost->addDbToHost(db);
    //Everything will work.
    ASSERT_NO_THROW(server->allocateClientSession(clientId, sessionId, dbName, dbRights));
    //Allocate client

    ASSERT_FALSE(server->isFree());

    ASSERT_NO_THROW(server->deallocateClientSession(clientId, sessionId));

    ASSERT_TRUE(server->isFree());
}


TEST_F(RasServerTest, DISABLED_isAvailable)
{
    ASSERT_ANY_THROW(server->isAvailable());
    ASSERT_NO_THROW(server->registerServer(server->getServerId()));

    ASSERT_TRUE(server->isAvailable());

    std::string clientId = "clientId";
    std::string sessionId = "sessionId";
    std::string dbName = "dbName";
    rasmgr::UserDatabaseRights dbRights(false,true);
    rasmgr::Database db(dbName);

    dbHost->addDbToHost(db);
    //Everything will work.
    ASSERT_NO_THROW(server->allocateClientSession(clientId, sessionId, dbName, dbRights));
    //Allocate client

    ASSERT_FALSE(server->isAvailable());
}
