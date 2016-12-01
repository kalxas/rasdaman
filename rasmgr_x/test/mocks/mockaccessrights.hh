#ifndef TEST_MOCKS_MOCKACCESSRIGHTS_HH_
#define TEST_MOCKS_MOCKACCESSRIGHTS_HH_

#include <gmock/gmock.h>

#include "../../src/accessrights.hh"

namespace rasmgr
{
namespace test
{
class MockAccessRights : public rasmgr::AccessRights
{
public:
    MOCK_CONST_METHOD1(canAccessDb, bool(const std::string&));
};
}
}

#endif
