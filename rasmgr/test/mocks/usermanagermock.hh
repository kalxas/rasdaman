#ifndef RASMGR_X_TEST_MOCKS_USERMANAGERMOCK_HH
#define RASMGR_X_TEST_MOCKS_USERMANAGERMOCK_HH

#include <gmock/gmock.h>

#include "rasmgr/src/usermanager.hh"
#include "rasmgr/src/messages/rasmgrmess.pb.h"

namespace rasmgr
{
namespace test
{
class UserManagerMock: public rasmgr::UserManager
{
public:
    MOCK_METHOD1(defineUser, void(const rasmgr::UserProto&));
    MOCK_METHOD2(changeUser, void(const std::string&, const rasmgr::UserProto&));
    MOCK_METHOD1(removeUser, void(const std::string&));
    MOCK_METHOD2(tryGetUser, bool(const std::string&, std::shared_ptr<rasmgr::User>&));
    MOCK_METHOD1(saveUserInformation, void(bool));
    MOCK_METHOD0(loadUserInformation, void(void));
    MOCK_METHOD0(serializeToProto, rasmgr::UserMgrProto(void));
};
}
}

#endif // RASMGR_X_TEST_MOCKS_USERMANAGERMOCK_HH
