#ifndef RASMGR_X_TEST_SERVERGROUPMOCK_HH
#define RASMGR_X_TEST_SERVERGROUPMOCK_HH

#include <gmock/gmock.h>

#include "rasmgr/src/servergroup.hh"
namespace rasmgr
{
namespace test
{

class ServerGroupMock : public rasmgr::ServerGroup
{
public:
    MOCK_METHOD0(start, void(void));
    MOCK_METHOD0(isStopped, bool(void));
    MOCK_METHOD1(stop, void(KillLevel));
    MOCK_METHOD1(tryRegisterServer, bool(const std::string &));
    MOCK_METHOD0(evaluateServerGroup, void(void));
    MOCK_METHOD2(tryGetAvailableServer, bool(const std::string &, std::shared_ptr<rasmgr::Server> &));
    MOCK_CONST_METHOD0(getConfig, rasmgr::ServerGroupConfigProto(void));
    MOCK_METHOD1(changeGroupConfig, void(const rasmgr::ServerGroupConfigProto &));
    MOCK_CONST_METHOD0(getGroupName, std::string(void));
    MOCK_METHOD0(serializeToProto, rasmgr::ServerGroupProto(void));
};

}  // namespace test
}  // namespace rasmgr

#endif  // RASMGR_X_TEST_SERVERGROUPMOCK_HH
