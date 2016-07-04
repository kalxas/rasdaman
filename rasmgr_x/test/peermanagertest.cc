#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include "common/src/grpc/healthserviceimpl.hh"
#include "common/src/grpc/grpcutils.hh"

#include "dummies/dummyrasmgrservice.hh"
#include "../src/peermanager.hh"

namespace rasmgr
{
namespace test
{
class PeerManagerTest: public ::testing::Test
{
protected:
    PeerManagerTest() :peerHost("127.0.0.1"), peerPort(35000)
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


    PeerManager peerManager;
    std::string peerHost;
    boost::uint32_t peerPort;

    boost::shared_ptr<DummyRasmgrService> service;
    boost::shared_ptr<common::HealthServiceImpl> healthService;
};

TEST_F(PeerManagerTest, defineInPeer_Success)
{
    ASSERT_NO_THROW(this->peerManager.defineInPeer(this->peerHost));
}

TEST_F(PeerManagerTest, defineInPeer_FailBecauseDuplicate)
{
    // Define the inpeer and then try to define it again
    this->peerManager.defineInPeer(this->peerHost);

    ASSERT_ANY_THROW(this->peerManager.defineInPeer(this->peerHost));
}

TEST_F(PeerManagerTest, removeInPeer_Success)
{
    //Define the inpeer that will be removed
    this->peerManager.defineInPeer(this->peerHost);

    // Remove the inpeer
    ASSERT_NO_THROW(this->peerManager.removeInPeer(this->peerHost));
}

TEST_F(PeerManagerTest, removeInPeer_FailBecauseMissing)
{
    // Fail because there is no inpeer
    ASSERT_ANY_THROW(this->peerManager.removeInPeer(this->peerHost));
}

TEST_F(PeerManagerTest, defineOutPeer_Success)
{
    ASSERT_NO_THROW(this->peerManager.defineOutPeer(this->peerHost, this->peerPort));
}

TEST_F(PeerManagerTest, defineOutPeer_FailBecauseDuplicate)
{
    // Define an outpeer
    this->peerManager.defineOutPeer(this->peerHost, this->peerPort);

    ASSERT_ANY_THROW(this->peerManager.defineOutPeer(this->peerHost, this->peerPort));
}


TEST_F(PeerManagerTest, removeOutPeer_Success)
{
    // Define an outpeer
    this->peerManager.defineOutPeer(this->peerHost, this->peerPort);

    ASSERT_NO_THROW(this->peerManager.removeOutPeer(this->peerHost));
}

TEST_F(PeerManagerTest, removeOutPeer_FailBecauseMissing)
{
    ASSERT_ANY_THROW(this->peerManager.removeOutPeer(this->peerHost));
}

TEST_F(PeerManagerTest, tryGetRemoteServer_FailBecauseNoOutPeers)
{
    ClientServerRequest request("gooduser", "password", "dbName");
    ClientServerSession reply;

    ASSERT_FALSE(this->peerManager.tryGetRemoteServer(request, reply));
}

TEST_F(PeerManagerTest, tryGetRemoteServer_FailBecausePeerIsNotResponding)
{
    //Define an outpeer that is not responding to requests
    this->peerManager.defineOutPeer(this->peerHost, this->peerPort);

    ClientServerRequest request("baduser", "password", "database");
    ClientServerSession serverSession;

    auto server = this->createAndInitServer();

    // Will fail because the peer is not responding
    ASSERT_FALSE(this->peerManager.tryGetRemoteServer(request, serverSession));

    server->Shutdown();
}

TEST_F(PeerManagerTest, tryGetRemoteServer_Success)
{
    //Define an outpeer that is not responding to requests
    this->peerManager.defineOutPeer(this->peerHost, this->peerPort);

    ClientServerRequest request("gooduser", "password", "database");
    ClientServerSession serverSession;

    auto server = this->createAndInitServer();

    ASSERT_TRUE(this->peerManager.tryGetRemoteServer(request, serverSession));

    RemoteClientSession remoteSession(serverSession.clientSessionId, serverSession.dbSessionId);
    ASSERT_TRUE(this->peerManager.isRemoteClientSession(remoteSession));

    server->Shutdown();
}

TEST_F(PeerManagerTest, releaseServer)
{
    //Define an outpeer that is not responding to requests
    this->peerManager.defineOutPeer(this->peerHost, this->peerPort);

    ClientServerRequest request("gooduser", "password", "database");
    ClientServerSession serverSession;

    auto server = this->createAndInitServer();

    this->peerManager.tryGetRemoteServer(request, serverSession);

    RemoteClientSession remoteSession(serverSession.clientSessionId, serverSession.dbSessionId);
    ASSERT_TRUE(this->peerManager.isRemoteClientSession(remoteSession));

    ASSERT_NO_THROW(this->peerManager.releaseServer(remoteSession));

    ASSERT_FALSE(this->peerManager.isRemoteClientSession(remoteSession));

    server->Shutdown();
}
}
}
