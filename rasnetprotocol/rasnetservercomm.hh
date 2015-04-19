/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#ifndef RASNETSERVERCOMM_HH
#define RASNETSERVERCOMM_HH

#include "rasnet/src/messages/client_rassrvr_service.pb.h"
#include "../raslib/error.hh"
#include <boost/smart_ptr.hpp>
#include "rasserver_x/src/clientmanager.hh"

class RasnetServerComm : public rasnet::service::ClientRassrvrService
{
public:
    RasnetServerComm(::boost::shared_ptr<rasserver::ClientManager> clientManager);
    void OpenServerDatabase(::google::protobuf::RpcController* controller,
                      const ::rasnet::service::OpenServerDatabaseReq* request,
                      ::rasnet::service::OpenServerDatabaseRepl* response,
                      ::google::protobuf::Closure* done);
    void CloseServerDatabase(::google::protobuf::RpcController* controller,
                       const ::rasnet::service::CloseServerDatabaseReq* request,
                       ::rasnet::service::Void* response,
                       ::google::protobuf::Closure* done);
    void CreateDatabase(::google::protobuf::RpcController* controller,
                        const ::rasnet::service::CreateDatabaseReq* request,
                        ::rasnet::service::CreateDatabaseRepl* response,
                        ::google::protobuf::Closure* done);
    void DestroyDatabase(::google::protobuf::RpcController* controller,
                         const ::rasnet::service::DestroyDatabaseReq* request,
                         ::rasnet::service::DestroyDatabaseRepl* response,
                         ::google::protobuf::Closure* done);
    void BeginTransaction(::google::protobuf::RpcController* controller,
                          const ::rasnet::service::BeginTransactionReq* request,
                          ::rasnet::service::BeginTransactionRepl* response,
                          ::google::protobuf::Closure* done);
    void CommitTransaction(::google::protobuf::RpcController* controller,
                           const ::rasnet::service::CommitTransactionReq* request,
                           ::rasnet::service::CommitTransactionRepl* response,
                           ::google::protobuf::Closure* done);
    void AbortTransaction(::google::protobuf::RpcController* controller,
                          const ::rasnet::service::AbortTransactionReq* request,
                          ::rasnet::service::AbortTransactionRepl* response,
                          ::google::protobuf::Closure* done);
    void IsTransactionOpen(google::protobuf::RpcController *controller,
                           const rasnet::service::IsTransactionOpenReq *request,
                           rasnet::service::IsTransactionOpenRepl *response,
                           google::protobuf::Closure *done);
    void StartInsertMDD(::google::protobuf::RpcController* controller,
                        const ::rasnet::service::StartInsertMDDReq* request,
                        ::rasnet::service::StartInsertMDDRepl* response,
                        ::google::protobuf::Closure* done);
    void StartInsertTransMDD(::google::protobuf::RpcController* controller,
                             const ::rasnet::service::StartInsertTransMDDReq* request,
                             ::rasnet::service::StartInsertTransMDDRepl* response,
                             ::google::protobuf::Closure* done);
    void InsertTile(::google::protobuf::RpcController* controller,
                    const ::rasnet::service::InsertTileReq* request,
                    ::rasnet::service::InsertTileRepl* response,
                    ::google::protobuf::Closure* done);
    void EndInsertMDD(::google::protobuf::RpcController* controller,
                      const ::rasnet::service::EndInsertMDDReq* request,
                      ::rasnet::service::EndInsertMDDRepl* response,
                      ::google::protobuf::Closure* done);
    void InsertCollection(::google::protobuf::RpcController* controller,
                          const ::rasnet::service::InsertCollectionReq* request,
                          ::rasnet::service::InsertCollectionRepl* response,
                          ::google::protobuf::Closure* done);
    void DeleteCollectionByName(::google::protobuf::RpcController* controller,
                                const ::rasnet::service::DeleteCollectionByNameReq* request,
                                ::rasnet::service::DeleteCollectionByNameRepl* response,
                                ::google::protobuf::Closure* done);
    void DeleteCollectionByOid(::google::protobuf::RpcController* controller,
                               const ::rasnet::service::DeleteCollectionByOidReq* request,
                               ::rasnet::service::DeleteCollectionByOidRepl* response,
                               ::google::protobuf::Closure* done);
    void RemoveObjectFromCollection(::google::protobuf::RpcController* controller,
                                    const ::rasnet::service::RemoveObjectFromCollectionReq* request,
                                    ::rasnet::service::RemoveObjectFromCollectionRepl* response,
                                    ::google::protobuf::Closure* done);
    void GetCollectionByNameOrOid(::google::protobuf::RpcController* controller,
                                  const ::rasnet::service::GetCollectionByNameOrOidReq* request,
                                  ::rasnet::service::GetCollectionByNameOrOidRepl* response,
                                  ::google::protobuf::Closure* done);
    void GetCollOidsByNameOrOid(::google::protobuf::RpcController* controller,
                                const ::rasnet::service::GetCollOidsByNameOrOidReq* request,
                                ::rasnet::service::GetCollOidsByNameOrOidRepl* response,
                                ::google::protobuf::Closure* done);
    void GetNextMDD(::google::protobuf::RpcController* controller,
                    const ::rasnet::service::GetNextMDDReq* request,
                    ::rasnet::service::GetNextMDDRepl* response,
                    ::google::protobuf::Closure* done);
    void GetNextTile(::google::protobuf::RpcController* controller,
                     const ::rasnet::service::GetNextTileReq* request,
                     ::rasnet::service::GetNextTileRepl* response,
                     ::google::protobuf::Closure* done);
    void EndTransfer(::google::protobuf::RpcController* controller,
                     const ::rasnet::service::EndTransferReq* request,
                     ::rasnet::service::EndTransferRepl* response,
                     ::google::protobuf::Closure* done);
    void InitUpdate(::google::protobuf::RpcController* controller,
                    const ::rasnet::service::InitUpdateReq* request,
                    ::rasnet::service::InitUpdateRepl* response,
                    ::google::protobuf::Closure* done);
    void ExecuteQuery(::google::protobuf::RpcController* controller,
                      const ::rasnet::service::ExecuteQueryReq* request,
                      ::rasnet::service::ExecuteQueryRepl* response,
                      ::google::protobuf::Closure* done);
    void GetNextElement(::google::protobuf::RpcController* controller,
                        const ::rasnet::service::GetNextElementReq* request,
                        ::rasnet::service::GetNextElementRepl* response,
                        ::google::protobuf::Closure* done);
    void ExecuteUpdateQuery(::google::protobuf::RpcController* controller,
                            const ::rasnet::service::ExecuteUpdateQueryReq* request,
                            ::rasnet::service::ExecuteUpdateQueryRepl* response,
                            ::google::protobuf::Closure* done);
    void ExecuteInsertQuery(google::protobuf::RpcController *controller,
                            const ::rasnet::service::ExecuteInsertQueryReq *request,
                            ::rasnet::service::ExecuteInsertQueryRepl *response,
                            ::google::protobuf::Closure *done);
    void ExecuteHttpQuery(google::protobuf::RpcController *controller,
                          const rasnet::service::ExecuteHttpQueryReq *request,
                          rasnet::service::ExecuteHttpQueryRepl *response,
                          google::protobuf::Closure *done);
    void GetNewOid(::google::protobuf::RpcController* controller,
                   const ::rasnet::service::GetNewOidReq* request,
                   ::rasnet::service::GetNewOidRepl* response,
                   ::google::protobuf::Closure* done);
    void GetObjectType(::google::protobuf::RpcController* controller,
                       const ::rasnet::service::GetObjectTypeReq* request,
                       ::rasnet::service::GetObjectTypeRepl* response,
                       ::google::protobuf::Closure* done);
    void GetTypeStructure(::google::protobuf::RpcController* controller,
                          const ::rasnet::service::GetTypeStructureReq* request,
                          ::rasnet::service::GetTypeStructureRepl* response,
                          ::google::protobuf::Closure* done);
    void SetFormat(::google::protobuf::RpcController* controller,
                   const ::rasnet::service::SetFormatReq* request,
                   ::rasnet::service::SetFormatRepl* response,
                   ::google::protobuf::Closure* done);

    void KeepAlive(google::protobuf::RpcController *controller,
                   const rasnet::service::KeepAliveRequest *request,
                   rasnet::service::Void *response,
                   google::protobuf::Closure *done);

private:
    int clientId;
    static const int NoClient;
    ::boost::shared_ptr<rasserver::ClientManager> clientManager;
};

#endif // RASNETSERVERCOMM_HH
