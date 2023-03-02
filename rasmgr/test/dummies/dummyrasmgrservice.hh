#ifndef DUMMYRASMGRSERVICE_HH
#define DUMMYRASMGRSERVICE_HH

#include <cstdint>

#include <logging.hh>
#include "rasnet/messages/rasmgr_rasmgr_service.grpc.pb.h"

namespace rasmgr
{
namespace test
{

class DummyRasmgrService : public rasnet::service::RasmgrRasmgrService::Service
{
public:
    DummyRasmgrService()
        : clientId(1),
          dbSessionId(2),
          serverHost("serverHost"),
          serverPort(36000)
    {
    }

    grpc::Status TryGetRemoteServer(__attribute__((unused)) grpc::ServerContext *context,
                                    const rasnet::service::GetRemoteServerRequest *request,
                                    rasnet::service::GetRemoteServerReply *response)
    {
        LDEBUG << "Trying";
        if (request->user_name() == "gooduser")
        {
            response->set_client_session_id(clientId);
            response->set_db_session_id(dbSessionId);
            response->set_server_host_name(serverHost);
            response->set_server_port(serverPort);

            return grpc::Status::OK;
        }
        else
        {
            return grpc::Status(grpc::StatusCode::UNKNOWN, "failed");
        }
    }

    grpc::Status ReleaseServer(__attribute__((unused)) grpc::ServerContext *context,
                               __attribute__((unused)) const rasnet::service::ReleaseServerRequest *request,
                               __attribute__((unused)) rasnet::service::Void *response)
    {
        return grpc::Status::OK;
    }

    const std::uint32_t clientId;
    const std::uint32_t dbSessionId;
    const std::string serverHost;
    const std::uint32_t serverPort;
};
}  // namespace test
}  // namespace rasmgr

#endif  // DUMMYRASMGRSERVICE_HH
