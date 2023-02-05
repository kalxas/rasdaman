#ifndef RASMGR_X_TEST_MOCKS_CLIENTMANAGERMOCK_HH
#define RASMGR_X_TEST_MOCKS_CLIENTMANAGERMOCK_HH

#include <gmock/gmock.h>
#include "../../src/clientmanager.hh"
#include "../../src/client.hh"

#include "../../src/client.hh"
#include "../../src/clientcredentials.hh"
#include "../../src/server.hh"
#include "../../src/usermanager.hh"

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
        ClientManager(config, userManager, serverManager, peerManager) {}
    MOCK_METHOD3(connectClient, void(const ClientCredentials&, const std::string&, std::string&));
    MOCK_METHOD1(disconnectClient, void(const std::string&));
    MOCK_METHOD3(openClientDbSession, void(const std::string&, const std::string&, ClientServerSession&));
    MOCK_METHOD2(closeClientDbSession, void(const std::string&, const std::string&));
    MOCK_METHOD1(keepClientAlive, void(const std::string&));
};
}
}
#endif // CLIENTMANAGERMOCK_HH
