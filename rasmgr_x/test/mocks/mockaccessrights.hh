#ifndef TEST_MOCKS_MOCKACCESSRIGHTS_HH_
#define TEST_MOCKS_MOCKACCESSRIGHTS_HH_

#include "../../common/src/mock/gmock.h"

#include "../../src/accessrights.hh"

class MockAccessRights : public rasmgr::AccessRights{
public:
  MOCK_CONST_METHOD1(canAccessDb, bool(const std::string& ));
};

#endif
