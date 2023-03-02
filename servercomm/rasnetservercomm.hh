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

#include "rasnet/messages/client_rassrvr_service.grpc.pb.h"
#include "raslib/error.hh"
#include "common/exceptions/exception.hh"

namespace rasserver
{
class ClientManager;
}

class RasnetServerComm : public rasnet::service::ClientRassrvrService::Service
{
public:
    RasnetServerComm(std::shared_ptr<rasserver::ClientManager> clientManager);
    ~RasnetServerComm() override = default;

    virtual grpc::Status OpenServerDatabase(grpc::ServerContext *context, const rasnet::service::OpenServerDatabaseReq *request, rasnet::service::OpenServerDatabaseRepl *response) override;
    virtual grpc::Status CloseServerDatabase(grpc::ServerContext *context, const rasnet::service::CloseServerDatabaseReq *request, rasnet::service::Void *response) override;
    virtual grpc::Status CreateDatabase(grpc::ServerContext *context, const rasnet::service::CreateDatabaseReq *request, rasnet::service::CreateDatabaseRepl *response) override;
    virtual grpc::Status DestroyDatabase(grpc::ServerContext *context, const rasnet::service::DestroyDatabaseReq *request, rasnet::service::DestroyDatabaseRepl *response) override;
    virtual grpc::Status BeginTransaction(grpc::ServerContext *context, const rasnet::service::BeginTransactionReq *request, rasnet::service::BeginTransactionRepl *response) override;
    virtual grpc::Status CommitTransaction(grpc::ServerContext *context, const rasnet::service::CommitTransactionReq *request, rasnet::service::CommitTransactionRepl *response) override;
    virtual grpc::Status AbortTransaction(grpc::ServerContext *context, const rasnet::service::AbortTransactionReq *request, rasnet::service::AbortTransactionRepl *response) override;
    virtual grpc::Status IsTransactionOpen(grpc::ServerContext *context, const rasnet::service::IsTransactionOpenReq *request, rasnet::service::IsTransactionOpenRepl *response) override;
    virtual grpc::Status StartInsertMDD(grpc::ServerContext *context, const rasnet::service::StartInsertMDDReq *request, rasnet::service::StartInsertMDDRepl *response) override;
    virtual grpc::Status StartInsertTransMDD(grpc::ServerContext *context, const rasnet::service::StartInsertTransMDDReq *request, rasnet::service::StartInsertTransMDDRepl *response) override;
    virtual grpc::Status InsertTile(grpc::ServerContext *context, const rasnet::service::InsertTileReq *request, rasnet::service::InsertTileRepl *response) override;
    virtual grpc::Status EndInsertMDD(grpc::ServerContext *context, const rasnet::service::EndInsertMDDReq *request, rasnet::service::EndInsertMDDRepl *response) override;
    virtual grpc::Status InsertCollection(grpc::ServerContext *context, const rasnet::service::InsertCollectionReq *request, rasnet::service::InsertCollectionRepl *response) override;
    virtual grpc::Status DeleteCollectionByName(grpc::ServerContext *context, const rasnet::service::DeleteCollectionByNameReq *request, rasnet::service::DeleteCollectionByNameRepl *response) override;
    virtual grpc::Status DeleteCollectionByOid(grpc::ServerContext *context, const rasnet::service::DeleteCollectionByOidReq *request, rasnet::service::DeleteCollectionByOidRepl *response) override;
    virtual grpc::Status RemoveObjectFromCollection(grpc::ServerContext *context, const rasnet::service::RemoveObjectFromCollectionReq *request, rasnet::service::RemoveObjectFromCollectionRepl *response) override;
    virtual grpc::Status GetCollectionByNameOrOid(grpc::ServerContext *context, const rasnet::service::GetCollectionByNameOrOidReq *request, rasnet::service::GetCollectionByNameOrOidRepl *response) override;
    virtual grpc::Status GetCollOidsByNameOrOid(grpc::ServerContext *context, const rasnet::service::GetCollOidsByNameOrOidReq *request, rasnet::service::GetCollOidsByNameOrOidRepl *response) override;
    virtual grpc::Status GetNextMDD(grpc::ServerContext *context, const rasnet::service::GetNextMDDReq *request, rasnet::service::GetNextMDDRepl *response) override;
    virtual grpc::Status GetNextTile(grpc::ServerContext *context, const rasnet::service::GetNextTileReq *request, rasnet::service::GetNextTileRepl *response) override;
    virtual grpc::Status EndTransfer(grpc::ServerContext *context, const rasnet::service::EndTransferReq *request, rasnet::service::EndTransferRepl *response) override;
    virtual grpc::Status InitUpdate(grpc::ServerContext *context, const rasnet::service::InitUpdateReq *request, rasnet::service::InitUpdateRepl *response) override;
    virtual grpc::Status ExecuteQuery(grpc::ServerContext *context, const rasnet::service::ExecuteQueryReq *request, rasnet::service::ExecuteQueryRepl *response) override;
    virtual grpc::Status ExecuteHttpQuery(grpc::ServerContext *context, const rasnet::service::ExecuteHttpQueryReq *request, rasnet::service::ExecuteHttpQueryRepl *response) override;
    virtual grpc::Status GetNextElement(grpc::ServerContext *context, const rasnet::service::GetNextElementReq *request, rasnet::service::GetNextElementRepl *response) override;
    virtual grpc::Status ExecuteUpdateQuery(grpc::ServerContext *context, const rasnet::service::ExecuteUpdateQueryReq *request, rasnet::service::ExecuteUpdateQueryRepl *response) override;
    virtual grpc::Status ExecuteInsertQuery(grpc::ServerContext *context, const rasnet::service::ExecuteInsertQueryReq *request, rasnet::service::ExecuteInsertQueryRepl *response) override;
    virtual grpc::Status GetNewOid(grpc::ServerContext *context, const rasnet::service::GetNewOidReq *request, rasnet::service::GetNewOidRepl *response) override;
    virtual grpc::Status GetObjectType(grpc::ServerContext *context, const rasnet::service::GetObjectTypeReq *request, rasnet::service::GetObjectTypeRepl *response) override;
    virtual grpc::Status GetTypeStructure(grpc::ServerContext *context, const rasnet::service::GetTypeStructureReq *request, rasnet::service::GetTypeStructureRepl *response) override;
    virtual grpc::Status SetFormat(grpc::ServerContext *context, const rasnet::service::SetFormatReq *request, rasnet::service::SetFormatRepl *response) override;
    virtual grpc::Status KeepAlive(grpc::ServerContext *context, const rasnet::service::KeepAliveRequest *request, rasnet::service::Void *response) override;
    virtual grpc::Status BeginStreamedHttpQuery(grpc::ServerContext *context, const rasnet::service::BeginStreamedHttpQueryReq *request, rasnet::service::StreamedHttpQueryRepl *response) override;
    virtual grpc::Status GetNextStreamedHttpQuery(grpc::ServerContext *context, const rasnet::service::GetNextStreamedHttpQueryReq *request, rasnet::service::StreamedHttpQueryRepl *response) override;

private:
    std::shared_ptr<rasserver::ClientManager> clientManager;

    static grpc::Status getRErrorStatus(r_Error &err);
    static grpc::Status getSTLExceptionStatus(std::exception &ex);
    static grpc::Status getCommonExceptionStatus(common::Exception &ex);
    static grpc::Status getUnknownExceptionStatus();
};

#endif  // RASNETSERVERCOMM_HH
