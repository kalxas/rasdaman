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
#include <thread>

#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <logging.hh>

#include "common/grpc/grpcutils.hh"
#include "common/grpc/healthserviceimpl.hh"

#include "dummies/dummyrasmgrservice.hh"

#include "../src/outpeer.hh"

namespace rasmgr
{
namespace test
{

class OutPeerTest: public ::testing::Test
{
protected:
    OutPeerTest()
        : peerHost("127.0.0.1"), peerPort(35000), peer(peerHost, peerPort)
    {
        this->service = boost::make_shared<DummyRasmgrService>();
        this->healthService = boost::make_shared<common::HealthServiceImpl>();
    }

    std::unique_ptr<grpc::Server> createAndInitServer()
    {
        std::string serverAddress = common::GrpcUtils::constructAddressString("127.0.0.1",  this->peerPort);

        grpc::ServerBuilder builder;
        builder.RegisterService(healthService.get());
        builder.RegisterService(service.get());

        // Listen on the given address without any authentication mechanism.
        builder.AddListeningPort(serverAddress, grpc::InsecureServerCredentials());

        // Finally assemble the server.
        return builder.BuildAndStart();
    }

    std::string peerHost;
    boost::uint32_t peerPort;
    OutPeer peer;
    boost::shared_ptr<DummyRasmgrService> service;
    boost::shared_ptr<common::HealthServiceImpl> healthService;
};

TEST_F(OutPeerTest, isBusyWithNoActiveSessions)
{
    ASSERT_FALSE(peer.isBusy());
}

TEST_F(OutPeerTest, DISABLED_tryGetRemoteServer_RemoteNotResponding)
{
    ClientServerRequest request("gooduser", "password", "database");
    ClientServerSession serverSession;

    ASSERT_FALSE(this->peer.tryGetRemoteServer(request, serverSession));

    // Failing to add a server should keep the peer not busy
    ASSERT_FALSE(peer.isBusy());
}


TEST_F(OutPeerTest, tryGetRemoteServer_RemoteHasNoServers)
{
    ClientServerRequest request("baduser", "password", "database");
    ClientServerSession serverSession;

    auto server = this->createAndInitServer();

    ASSERT_FALSE(this->peer.tryGetRemoteServer(request, serverSession));

    // Failing to add a server should keep the peer not busy
    ASSERT_FALSE(peer.isBusy());

    server->Shutdown();
}

TEST_F(OutPeerTest, tryGetRemoteServer_Success)
{
    ClientServerRequest request("gooduser", "password", "database");
    ClientServerSession serverSession;

    auto server = this->createAndInitServer();

    ASSERT_TRUE(this->peer.tryGetRemoteServer(request, serverSession));

    ASSERT_EQ(this->service->clientId, serverSession.clientSessionId);
    ASSERT_EQ(this->service->dbSessionId, serverSession.dbSessionId);
    ASSERT_EQ(this->service->serverHost, serverSession.serverHostName);
    ASSERT_EQ(this->service->serverPort, serverSession.serverPort);

    server->Shutdown();

    ASSERT_TRUE(peer.isBusy());
}

TEST_F(OutPeerTest, releaseServer_NoSessions)
{
    RemoteClientSession clientSession("testClientId", "testDbId");

    // Even if the session does not exist, releasing it will not throw an exception
    ASSERT_NO_THROW(this->peer.releaseServer(clientSession));
}

TEST_F(OutPeerTest, releaseServer_ValidSession)
{
    ClientServerRequest request("gooduser", "password", "database");
    ClientServerSession serverSession;

    auto server = this->createAndInitServer();
    // Get a remote server
    this->peer.tryGetRemoteServer(request, serverSession);

    // The outpeer is now busy with a session
    ASSERT_TRUE(peer.isBusy());

    //Releasing the acquired server will not throw an exception
    RemoteClientSession clientSession(serverSession.clientSessionId, serverSession.dbSessionId);
    ASSERT_NO_THROW(this->peer.releaseServer(clientSession));

    ASSERT_FALSE(peer.isBusy());

    server->Shutdown();
}

}
}
