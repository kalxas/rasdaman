#ifndef RASMGR_X_TEST_MOCKS_USERMANAGERMOCK_HH
#define RASMGR_X_TEST_MOCKS_USERMANAGERMOCK_HH

#include <boost/shared_ptr.hpp>

#include "../../../common/src/mock/gmock.h"

#include "../../src/usermanager.hh"
#include "../../src/messages/rasmgrmess.pb.h"

namespace rasmgr
{
namespace test
{
class UserManagerMock:public rasmgr::UserManager
{
public:
    MOCK_METHOD1(defineUser, void(const rasmgr::UserProto&));
    MOCK_METHOD2(changeUser, void(const std::string&, const rasmgr::UserProto&));
    MOCK_METHOD1(removeUser, void(const std::string&));
    MOCK_METHOD2(tryGetUser, bool(const std::string&, boost::shared_ptr<rasmgr::User>&));
    MOCK_METHOD0(saveUserInformation, void(void));
    MOCK_METHOD0(loadUserInformation, void(void));
    MOCK_METHOD0(serializeToProto, rasmgr::UserMgrProto(void));
};
}
}

#endif // RASMGR_X_TEST_MOCKS_USERMANAGERMOCK_HH
