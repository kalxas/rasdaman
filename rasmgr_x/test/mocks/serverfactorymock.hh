#ifndef RASMGR_X_TEST_MOCKS_SERVERFACTORYMOCK_HH
#define RASMGR_X_TEST_MOCKS_SERVERFACTORYMOCK_HH

#include <boost/shared_ptr.hpp>

#include "../../../common/src/mock/gmock.h"

#include "../src/serverfactory.hh"

class ServerFactoryMock:public rasmgr::ServerFactory
{
public:

    MOCK_METHOD3(createServer, boost::shared_ptr<rasmgr::Server>(const std::string&, const boost::int32_t&, boost::shared_ptr<rasmgr::DatabaseHost>));
};

#endif // RASMGR_X_TEST_MOCKS_SERVERFACTORYMOCK_HH
