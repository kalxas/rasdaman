#ifndef RASMGR_X_TEST_MOCKS_SERVERFACTORYMOCK_HH
#define RASMGR_X_TEST_MOCKS_SERVERFACTORYMOCK_HH

#include <gmock/gmock.h>

#include "../../src/serverfactory.hh"
namespace rasmgr
{
namespace test
{
class ServerFactoryMock: public rasmgr::ServerFactory
{
public:

    MOCK_METHOD1(createServer, std::shared_ptr<rasmgr::Server>(const ServerConfig&));
};
}
}

#endif // RASMGR_X_TEST_MOCKS_SERVERFACTORYMOCK_HH
