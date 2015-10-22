#ifndef RASMGR_X_TEST_MOCKS_DATABASEHOSTMGRMOCK
#define RASMGR_X_TEST_MOCKS_DATABASEHOSTMGRMOCK

#include "../../common/src/mock/gmock.h"

#include "../../src/databasehostmanager.hh"
namespace rasmgr
{
namespace test
{
class DatabaseHostManagerMock:public rasmgr::DatabaseHostManager
{
public:
    MOCK_METHOD1(defineDatabaseHost, void(const rasmgr::DatabaseHostPropertiesProto&));
    MOCK_METHOD2(changeDatabaseHost, void(std::string&,const rasmgr::DatabaseHostPropertiesProto&));
    MOCK_METHOD1(removeDatabaseHost,void(const std::string&));
    MOCK_METHOD1(getAndLockDatabaseHost, boost::shared_ptr<rasmgr::DatabaseHost>(const std::string&));
    MOCK_CONST_METHOD0(getDatabaseHostList, std::list<boost::shared_ptr<rasmgr::DatabaseHost> >(void));
    MOCK_CONST_METHOD0(serializeToProto,rasmgr::DatabaseHostMgrProto(void));
};
}
}
#endif // RASMGR_X_TEST_MOCKS_DATABASEHOSTMGRMOCK
