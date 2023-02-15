#ifndef TEST_MOCKS_MOCKRASSERVER_HH_
#define TEST_MOCKS_MOCKRASSERVER_HH_

#include <gmock/gmock.h>

#include "rasmgr/src/server.hh"
#include "rasmgr/src/databasehost.hh"
#include "rasmgr/src/userdatabaserights.hh"
#include "rasmgr/src/messages/rasmgrmess.pb.h"

namespace rasmgr
{
namespace test
{

class MockRasServer: public Server
{
public:
    MOCK_METHOD0(startProcess, void(void));
    MOCK_METHOD0(isAlive, bool(void));
    MOCK_METHOD1(isClientAlive, bool(std::uint32_t));
    
    MOCK_METHOD4(allocateClientSession, void(std::uint32_t,
                                             std::uint32_t,
                                             const std::string&,
                                             const rasmgr::UserDatabaseRights& dbRights));
    
    MOCK_METHOD7(allocateClientSession, void(std::uint32_t,
                                             const std::string&,
                                             const std::string&,
                                             const std::string&,
                                             std::uint32_t,
                                             const std::string&,
                                             const rasmgr::UserDatabaseRights& dbRights));
    
    MOCK_METHOD4(allocateClientSession, void(std::uint32_t,
                                             std::uint32_t,
                                             const std::string&,
                                             const std::string& capabilities));
    
    MOCK_METHOD2(deallocateClientSession, void(std::uint32_t, std::uint32_t));
    MOCK_METHOD1(registerServer, void(const std::string&));
    MOCK_METHOD0(getTotalSessionNo, std::uint32_t(void));
    MOCK_METHOD1(stop, void(KillLevel));
    MOCK_METHOD0(isStarting, bool(void));
    MOCK_METHOD0(isFree, bool(void));
    MOCK_METHOD0(isAvailable, bool(void));
    MOCK_CONST_METHOD0(getPort, std::int32_t(void));
    MOCK_CONST_METHOD0(getHostName, const std::string&(void));
    MOCK_CONST_METHOD0(getServerId, const std::string&(void));
};

}
}


#endif
