#ifndef RASMGR_X_TEST_MOCKS_PEERMANAGERMOCK_HH
#define RASMGR_X_TEST_MOCKS_PEERMANAGERMOCK_HH

#include <gmock/gmock.h>
#include "../../src/peermanager.hh"


namespace rasmgr
{
namespace test
{
class PeerManagerMock: public PeerManager
{
public:
    MOCK_METHOD1(defineInPeer, void(const std::string&));
    MOCK_METHOD1(removeInPeer, void(const std::string&));
    MOCK_METHOD2(defineOutPeer, void(const std::string&, const uint32_t port));
    MOCK_METHOD1(removeOutPeer, void(const std::string&));

    MOCK_METHOD2(tryGetRemoteServer, bool(const ClientServerRequest&, ClientServerSession&));
    MOCK_METHOD1(isRemoteClientSession, bool(const RemoteClientSession&));
    MOCK_METHOD1(releaseServer, void(const RemoteClientSession&));

    MOCK_METHOD0(serializeToProto, PeerMgrProto());
};
}
}

#endif // PEERMANAGERMOCK_HH
