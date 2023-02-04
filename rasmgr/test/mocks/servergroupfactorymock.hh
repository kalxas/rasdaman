#ifndef RASMGR_X_TEST_SERVERGROUPFACTORYMOCK_HH
#define RASMGR_X_TEST_SERVERGROUPFACTORYMOCK_HH

#include <gmock/gmock.h>

#include "rasmgr/src/servergroupfactory.hh"

namespace rasmgr
{
namespace test
{
class ServerGroupFactoryMock: public rasmgr::ServerGroupFactory
{
public:
    ServerGroupFactoryMock(): ServerGroupFactory(nullptr, nullptr) {}
    MOCK_METHOD1(createServerGroup, std::shared_ptr<rasmgr::ServerGroup>(const rasmgr::ServerGroupConfigProto&));
};
}
}

#endif // RASMGR_X_TEST_SERVERGROUPFACTORYMOCK_HH
