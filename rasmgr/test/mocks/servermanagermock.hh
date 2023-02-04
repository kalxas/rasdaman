#ifndef RASMGR_X_TEST_MOCKS_SERVERMANAGERMOCK_HH
#define RASMGR_X_TEST_MOCKS_SERVERMANAGERMOCK_HH

#include <gmock/gmock.h>
#include "../../src/servermanager.hh"

namespace rasmgr
{
namespace test
{
class ServerManagerMock: public rasmgr::ServerManager
{
public:
    ServerManagerMock(const ServerManagerConfig& config, std::shared_ptr<ServerGroupFactory> serverGroupFactory)
        : ServerManager(config, serverGroupFactory) {}

    MOCK_METHOD2(tryGetAvailableServer, bool(const std::string&, std::shared_ptr<Server>&));
    MOCK_METHOD1(registerServer, void(const std::string&));
    MOCK_METHOD1(defineServerGroup, void(const ServerGroupConfigProto&));
    MOCK_METHOD2(changeServerGroup, void(const std::string&, const ServerGroupConfigProto&));
    MOCK_METHOD1(removeServerGroup, void(const std::string&));
    MOCK_METHOD1(startServerGroup, void(const StartServerGroup&));
    MOCK_METHOD1(stopServerGroup, void(const StopServerGroup&));
    MOCK_METHOD0(hasRunningServers, bool());
    MOCK_METHOD0(serializeToProto, ServerMgrProto());
};
}
}
#endif // SERVERMANAGERMOCK_HH
