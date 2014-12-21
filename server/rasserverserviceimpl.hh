#ifndef RASSERVERSERVICE_HH
#define RASSERVERSERVICE_HH

#include <set>
#include <utility>
#include <string>

#include "../../rasnet/src/messages/rassrvr_rasmgr_service.pb.h"

class RasServerServiceImpl : public rasnet::service::RasServerService
{
public:
    RasServerServiceImpl();

    void AllocateClient(::google::protobuf::RpcController* controller,
                         const ::rasnet::service::AllocateClientReq* request,
                         ::rasnet::service::Void* response,
                         ::google::protobuf::Closure* done);
    void DeallocateClient(::google::protobuf::RpcController* controller,
                         const ::rasnet::service::DeallocateClientReq* request,
                         ::rasnet::service::Void* response,
                         ::google::protobuf::Closure* done);
    void Close(::google::protobuf::RpcController* controller,
                         const ::rasnet::service::CloseServerReq* request,
                         ::rasnet::service::Void* response,
                         ::google::protobuf::Closure* done);
    void GetClientStatus(::google::protobuf::RpcController* controller,
                         const ::rasnet::service::ClientStatusReq* request,
                         ::rasnet::service::ClientStatusRepl* response,
                         ::google::protobuf::Closure* done);
    void GetServerStatus(::google::protobuf::RpcController* controller,
                         const ::rasnet::service::ServerStatusReq* request,
                         ::rasnet::service::ServerStatusRepl* response,
                         ::google::protobuf::Closure* done);
private:
    std::set<std::pair<std::string, std::string> > clientList;
};

#endif // RASSERVERSERVICE_HH
