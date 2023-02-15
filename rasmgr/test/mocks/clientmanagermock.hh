#ifndef RASMGR_X_TEST_MOCKS_CLIENTMANAGERMOCK_HH
#define RASMGR_X_TEST_MOCKS_CLIENTMANAGERMOCK_HH

#include "../../src/clientmanager.hh"
#include "../../src/clientcredentials.hh"
#include "../../src/usermanager.hh"
#include "../../src/cpuscheduler.hh"
#include <gmock/gmock.h>

namespace rasmgr
{
namespace test
{
class ClientManagerMock: public rasmgr::ClientManager
{
public:
    ClientManagerMock(const ClientManagerConfig& config,
                      std::shared_ptr<UserManager> userManager,
                      std::shared_ptr<ServerManager> serverManager,
                      std::shared_ptr<PeerManager> peerManager):
        ClientManager(config, userManager, serverManager, peerManager, std::make_shared<CpuScheduler>(4)) {}
    MOCK_METHOD2(connectClient, std::uint32_t(const ClientCredentials&, const std::string&));
    MOCK_METHOD1(disconnectClient, void(std::uint32_t));
    MOCK_METHOD3(openClientDbSession, void(std::uint32_t, const std::string&, ClientServerSession&));
    MOCK_METHOD2(closeClientDbSession, void(std::uint32_t, std::uint32_t));
    MOCK_METHOD1(keepClientAlive, void(std::uint32_t));
};
}
}
#endif // CLIENTMANAGERMOCK_HH
