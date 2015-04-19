#ifndef RASMGR_X_TEST_MOCKS_USERMANAGERMOCK_HH
#define RASMGR_X_TEST_MOCKS_USERMANAGERMOCK_HH

#include <boost/shared_ptr.hpp>

#include "../../../common/src/mock/gmock.h"
#include "../../src/common/peerstatus.hh"
namespace rasnet
{
namespace test
{
class PeerStatusMock:public rasnet::PeerStatus
{
public:

    PeerStatusMock():rasnet::PeerStatus(1,1)
    {}

    MOCK_METHOD0(isAlive, bool());
    MOCK_METHOD0(decreaseLiveliness, bool());
    MOCK_METHOD0(reset, void());
};
}
}
#endif // RASMGR_X_TEST_MOCKS_USERMANAGERMOCK_HH
