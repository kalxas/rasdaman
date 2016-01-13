#ifndef DUMMYRASMGRSERVICE_HH
#define DUMMYRASMGRSERVICE_HH

#include <boost/smart_ptr.hpp>
#include <boost/cstdint.hpp>

#include "../../common/src/logging/easylogging++.hh"
#include "../../rasnet/messages/rasmgr_rasmgr_service.grpc.pb.h"

namespace rasmgr
{
namespace test
{

class DummyRasmgrService:public rasnet::service::RasmgrRasmgrService::Service
{
public:
    DummyRasmgrService():
        clientId("clientid"),
        dbSessionId("dbSessionId"),
        serverHost("serverHost"),
        serverPort(36000)
    {
    }

    grpc::Status TryGetRemoteServer(grpc::ServerContext *context, const rasnet::service::GetRemoteServerRequest *request, rasnet::service::GetRemoteServerReply *response)
    {
        LDEBUG<<"Trying";
        if(request->user_name()=="gooduser")
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

    grpc::Status ReleaseServer(grpc::ServerContext *context, const rasnet::service::ReleaseServerRequest *request, rasnet::service::Void *response)
    {
        return grpc::Status::OK;
    }

    const std::string clientId;
    const std::string dbSessionId;
    const std::string serverHost;
    const boost::uint32_t serverPort;
};
}
}

#endif // DUMMYRASMGRSERVICE_HH
