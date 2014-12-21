#include "rasserverserviceimpl.hh"
#include "rasserver_entry.hh"

RasServerServiceImpl::RasServerServiceImpl()
{
}

void RasServerServiceImpl::AllocateClient(::google::protobuf::RpcController* controller,
                                          const ::rasnet::service::AllocateClientReq* request,
                                          ::rasnet::service::Void* response,
                                          ::google::protobuf::Closure* done)
{
    std::pair<std::set<std::pair<std::string, std::string> >::iterator, bool> result;
    std::pair<std::string, std::string> data(request->clientid(),request->sessionid());

    result = this->clientList.insert(data);
    if(!result.second)
    {
        controller->SetFailed("Client already in list");
    }else{
        RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
        rasServerEntry.compat_connectNewClient(request->capabilities().c_str());
    }
}

void RasServerServiceImpl::DeallocateClient(::google::protobuf::RpcController* controller,
                                            const ::rasnet::service::DeallocateClientReq* request,
                                            ::rasnet::service::Void* response,
                                            ::google::protobuf::Closure* done)
{
    this->clientList.erase(std::make_pair(request->clientid(), request->sessionid()));
    RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
    rasServerEntry.compat_disconnectClient();
}

void RasServerServiceImpl::Close(::google::protobuf::RpcController* controller,
                                 const ::rasnet::service::CloseServerReq* request,
                                 ::rasnet::service::Void* response,
                                 ::google::protobuf::Closure* done)
{
    exit(EXIT_SUCCESS);
}

void RasServerServiceImpl::GetClientStatus(::google::protobuf::RpcController* controller,
                                           const ::rasnet::service::ClientStatusReq* request,
                                           ::rasnet::service::ClientStatusRepl* response,
                                           ::google::protobuf::Closure* done)
{
    response->set_status(rasnet::service::ClientStatusRepl_Status_ALIVE);
}

void RasServerServiceImpl::GetServerStatus(::google::protobuf::RpcController* controller,
                                           const ::rasnet::service::ServerStatusReq* request,
                                           ::rasnet::service::ServerStatusRepl* response,
                                           ::google::protobuf::Closure* done)
{
    response->set_clientqueuesize(this->clientList.size());
}
