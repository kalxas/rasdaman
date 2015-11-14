#ifndef RASMGR_X_TEST_MOCKS_CLIENTMANAGERMOCK_HH
#define RASMGR_X_TEST_MOCKS_CLIENTMANAGERMOCK_HH

#include "../../../common/src/mock/gmock.h"
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
class ClientManagerMock:public rasmgr::ClientManager
{
public:
    ClientManagerMock(const ClientManagerConfig& config, boost::shared_ptr<UserManager> userManager):
        ClientManager(config, userManager) {}
    MOCK_METHOD2(connectClient, void(const ClientCredentials &, std::string &));
    MOCK_METHOD1(disconnectClient, void(const std::string &));
    MOCK_METHOD4(openClientDbSession, void(std::string, const std::string &, boost::shared_ptr<Server>, std::string &));
    MOCK_METHOD2(closeClientDbSession, void(const std::string &, const std::string &));
    MOCK_METHOD1(keepClientAlive, void(const std::string &));
};
}
}
#endif // CLIENTMANAGERMOCK_HH
