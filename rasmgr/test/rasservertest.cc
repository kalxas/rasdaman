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
#include <memory>
#include <boost/cstdint.hpp>

#include <grpc++/grpc++.h>
#include <grpc++/security/credentials.h>

#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <logging.hh>
#include "common/grpc/grpcutils.hh"

#include "rasnet/messages/rassrvr_rasmgr_service.grpc.pb.h"

#include "rasmgr/src/serverrasnet.hh"
#pragma GCC diagnostic ignored "-Wunused-parameter"

namespace rasmgr
{
namespace test
{
using rasmgr::ServerRasNet;
using rasnet::service::RasServerService;

using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::Status;
//SynchronousService doesn't appear to exist. Did they mean AsynchronousService?
//using grpc::SynchronousService;

using ::testing::AtLeast;                     // #1
using ::testing::_;
using ::testing::Return;

class MockRasServerService: public ::rasnet::service::RasServerService::Service
{
    grpc::Status AllocateClient(grpc::ServerContext* context, const rasnet::service::AllocateClientReq* request, rasnet::service::Void* response) override
    {
        return Status::OK;
    }

    grpc::Status DeallocateClient(grpc::ServerContext* context, const rasnet::service::DeallocateClientReq* request, rasnet::service::Void* response) override
    {
        return Status::OK;
    }

    grpc::Status Close(grpc::ServerContext* context, const rasnet::service::CloseServerReq* request, rasnet::service::Void* response) override
    {
        return Status::OK;
    }

    grpc::Status GetClientStatus(grpc::ServerContext* context, const rasnet::service::ClientStatusReq* request, rasnet::service::ClientStatusRepl* response) override
    {
        response->set_status(rasnet::service::ClientStatusRepl_Status_ALIVE);

        return Status::OK;
    }

    grpc::Status GetServerStatus(grpc::ServerContext* context, const rasnet::service::ServerStatusReq* request, rasnet::service::ServerStatusRepl* response) override
    {
        response->set_clientqueuesize(1);
        return Status::OK;
    }
}
;

class FailingMockRasServerService: public rasnet::service::RasServerService::Service
{
public:
    grpc::Status AllocateClient(grpc::ServerContext* context, const rasnet::service::AllocateClientReq* request, rasnet::service::Void* response) override
    {
        return Status::CANCELLED;
    }

    grpc::Status DeallocateClient(grpc::ServerContext* context, const rasnet::service::DeallocateClientReq* request, rasnet::service::Void* response) override
    {
        return Status::CANCELLED;
    }

    grpc::Status Close(grpc::ServerContext* context, const rasnet::service::CloseServerReq* request, rasnet::service::Void* response) override
    {
        return Status::OK;
    }

    grpc::Status GetClientStatus(grpc::ServerContext* context, const rasnet::service::ClientStatusReq* request, rasnet::service::ClientStatusRepl* response) override
    {
        response->set_status(rasnet::service::ClientStatusRepl_Status_DEAD);
        
        return Status::CANCELLED;
    }

    grpc::Status GetServerStatus(grpc::ServerContext* context, const rasnet::service::ServerStatusReq* request, rasnet::service::ServerStatusRepl* response) override
    {
        return Status::CANCELLED;
    }
}
;

class RasServerTest: public ::testing::Test
{
protected:
    RasServerTest()
    {
        this->hostName = "localhost";
        this->goodPort = 50001;
        this->badPort = 50002;

        boost::shared_ptr<RasServerService::Service> service(new MockRasServerService());

        boost::shared_ptr<RasServerService::Service> failService(new FailingMockRasServerService());

        ServerBuilder goodServerBuilder;
        ServerBuilder failingServerBuilder;

        std::string serverBaseAddress = "0.0.0.0";

        goodServerBuilder.AddListeningPort(common::GrpcUtils::constructAddressString(serverBaseAddress, this->goodPort), grpc::InsecureServerCredentials());
        //was originally goodServerBuilder.RegisterService((SynchronousService*)service.get());
        //SynchronousService no longer exists?
        goodServerBuilder.RegisterService(service.get());
        goodService = goodServerBuilder.BuildAndStart();

        failingServerBuilder.AddListeningPort(common::GrpcUtils::constructAddressString(serverBaseAddress, this->badPort), grpc::InsecureServerCredentials());
        //was originally failingServerBuilder.RegisterService((SynchronousService*)failService.get());
        //SynchronousService does not appear to exist...
        failingServerBuilder.RegisterService(failService.get());
        failingService = failingServerBuilder.BuildAndStart();


        dbHost.reset(new rasmgr::DatabaseHost("hostName", "connectString", "user", "passwd"));

        ServerConfig goodServerConfig(hostName, goodPort, dbHost);
        ServerConfig failingServerConfig(hostName, badPort, dbHost);

        server.reset(new rasmgr::ServerRasNet(goodServerConfig));
        failingServer.reset(new rasmgr::ServerRasNet(failingServerConfig));
    }


    std::string hostName;
    boost::int32_t goodPort;
    boost::int32_t badPort;

    std::unique_ptr<Server>  goodService;
    std::unique_ptr<Server> failingService;
    boost::shared_ptr<rasmgr::DatabaseHost> dbHost;

    boost::shared_ptr<rasmgr::ServerRasNet> server;
    boost::shared_ptr<rasmgr::ServerRasNet> failingServer;
};

//TEST_F(RasServerTest, isAlive)
//{
//    ASSERT_TRUE(server->isAlive());
//    ASSERT_FALSE(failingServer->isAlive());
//}

}
}

//TEST_F(RasServerTest, DISABLED_isClientAlive)
//{
//    ASSERT_TRUE(server->isClientAlive("test"));
//    ASSERT_FALSE(failingServer->isClientAlive("test"));
//}

//TEST_F(RasServerTest, DISABLED_allocateClientSession)
//{
//    std::string clientId = "clientId";
//    std::string sessionId = "sessionId";
//    std::string dbName = "dbName";
//    rasmgr::UserDatabaseRights dbRights(false,true);
//    rasmgr::Database db(dbName);

//    ASSERT_NO_THROW(server->registerServer(server->getServerId()));

//    //This will fail because the database host does not have the database with the given name
//    ASSERT_ANY_THROW(server->allocateClientSession(clientId, sessionId, dbName, dbRights));

//    dbHost->addDbToHost(db);
//    //Everything will work.
//    ASSERT_NO_THROW(server->allocateClientSession(clientId, sessionId, dbName, dbRights));

//    //Adding the same session twice will throw an exception
//    ASSERT_ANY_THROW(server->allocateClientSession(clientId, sessionId, dbName, dbRights));

//    //This will fail due to the server
//    ASSERT_ANY_THROW(failingServer->allocateClientSession(clientId, sessionId, dbName, dbRights));

//}

////Test if the database host connection is freed
//TEST_F(RasServerTest, DISABLED_cleanupPerformed)
//{
//    std::string clientId = "clientId";
//    std::string sessionId = "sessionId";
//    std::string dbName = "dbName";
//    rasmgr::UserDatabaseRights dbRights(false,true);
//    rasmgr::Database db(dbName);
//    boost::shared_ptr<rasmgr::DatabaseHost> dbh( new rasmgr::DatabaseHost("hostName", "connectString", "user","passwd"));

//    dbh->addDbToHost(db);

//    rasmgr::ServerRasNet* srv = new ServerRasNet(hostName, port, dbh);

//    ASSERT_FALSE(dbh->isBusy());

//    ASSERT_NO_THROW(srv->registerServer(srv->getServerId()));

//    ASSERT_NO_THROW(srv->allocateClientSession(clientId, sessionId, dbName, dbRights));

//    ASSERT_TRUE(dbh->isBusy());

//    //Removing the server will also remove all its connections to the database host
//    delete srv;

//    ASSERT_FALSE(dbh->isBusy());

//}

//TEST_F(RasServerTest, DISABLED_deallocateClientSession)
//{
//    std::string clientId = "clientId";
//    std::string sessionId = "sessionId";
//    std::string dbName = "dbName";
//    rasmgr::UserDatabaseRights dbRights(false,true);
//    rasmgr::Database db(dbName);

//    dbHost->addDbToHost(db);

//    ASSERT_NO_THROW(server->registerServer(server->getServerId()));

//    //Everything will work.
//    ASSERT_NO_THROW(server->allocateClientSession(clientId, sessionId, dbName, dbRights));

//    ASSERT_NO_THROW(server->deallocateClientSession(clientId, sessionId));

//    ASSERT_ANY_THROW(failingServer->deallocateClientSession(clientId, sessionId));
//}

//TEST_F(RasServerTest, DISABLED_registerServer)
//{
//    ASSERT_ANY_THROW(server->registerServer("wrong"));

//    ASSERT_NO_THROW(server->registerServer(server->getServerId()));

//    ASSERT_NO_THROW(server->registerServer(server->getServerId()));
//}

////TODO
////TEST_F(RasServerTest, stop)
////{
////}

//TEST_F(RasServerTest, DISABLED_isStarting)
//{
//    ASSERT_TRUE(server->isStarting());

//    ASSERT_NO_THROW(server->registerServer(server->getServerId()));

//    //The server has already started
//    ASSERT_FALSE(server->isStarting());

//}


//TEST_F(RasServerTest, DISABLED_isFree)
//{
//    ASSERT_NO_THROW(server->registerServer(server->getServerId()));

//    ASSERT_TRUE(server->isFree());

//    std::string clientId = "clientId";
//    std::string sessionId = "sessionId";
//    std::string dbName = "dbName";
//    rasmgr::UserDatabaseRights dbRights(false,true);
//    rasmgr::Database db(dbName);

//    dbHost->addDbToHost(db);
//    //Everything will work.
//    ASSERT_NO_THROW(server->allocateClientSession(clientId, sessionId, dbName, dbRights));
//    //Allocate client

//    ASSERT_FALSE(server->isFree());

//    ASSERT_NO_THROW(server->deallocateClientSession(clientId, sessionId));

//    ASSERT_TRUE(server->isFree());
//}


//TEST_F(RasServerTest, DISABLED_isAvailable)
//{
//    ASSERT_ANY_THROW(server->isAvailable());
//    ASSERT_NO_THROW(server->registerServer(server->getServerId()));

//    ASSERT_TRUE(server->isAvailable());

//    std::string clientId = "clientId";
//    std::string sessionId = "sessionId";
//    std::string dbName = "dbName";
//    rasmgr::UserDatabaseRights dbRights(false,true);
//    rasmgr::Database db(dbName);

//    dbHost->addDbToHost(db);
//    //Everything will work.
//    ASSERT_NO_THROW(server->allocateClientSession(clientId, sessionId, dbName, dbRights));
//    //Allocate client

//    ASSERT_FALSE(server->isAvailable());
//}
