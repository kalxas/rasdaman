#ifndef RASNET_TEST_MOCK_CLIENTPOOLMOCK_HH
#define RASNET_TEST_MOCK_CLIENTPOOLMOCK_HH

#include <string>
#include <boost/cstdint.hpp>

#include "../../../common/src/mock/gmock.h"
#include "../../src/server/clientpool.hh"
namespace rasnet
{
namespace test
{
class ClientPoolMock:public rasnet::ClientPool
{
public:
    MOCK_METHOD3(addClient, void(std::string, boost::int32_t period, boost::int32_t));
    MOCK_CONST_METHOD0(getMinimumPollPeriod, boost::int32_t(void));
    MOCK_METHOD1(resetClientStatus, void(std::string));
    MOCK_METHOD1(pingAllClients, void(zmq::socket_t&));
    MOCK_METHOD0(removeDeadClients, void(void));
    MOCK_METHOD1(isClientAlive, bool(std::string));
    MOCK_METHOD0(removeAllClients, void(void));
};

}
}


#endif // RASNET_TEST_MOCK_CLIENTPOOLMOCK_HH
