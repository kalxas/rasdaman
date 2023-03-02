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

#include "rasnetservercomm.hh"
#include "rpcif.h"
#include "rasserver/src/clientmanager.hh"
#include "rasserver/src/clientquerystreamedresult.hh"
#include "server/rasserver_entry.hh"
#include "common/grpc/messages/error.pb.h"
#include "common/uuid/uuid.hh"
#include "common/exceptions/exception.hh"
#include "mymalloc/mymalloc.h"
#include "common/pragmas/pragmas.hh"
#include <logging.hh>

using namespace rasnet::service;
using common::ErrorMessage;
using grpc::ServerContext;

// clang-format off

// Executes the try_block in a try/catch that automatically handles several exception types.
// The finally block is executed then after the try/catch block.
// Note: this could be done more elegantly with a proper helper method where the try_block
// and finally_block are passed as lambda functions, but this does the job just fine as well.
#define HANDLE_REQUEST(request_name, try_block, finally_block) \
  grpc::Status status = grpc::Status::OK; \
  try { \
      LTRACE << "executing request " << request_name; \
      try_block \
  } catch (r_Ebase_dbms &ex) { \
      LERROR << "request " << request_name << " failed with base DBMS error: " << ex.what(); \
      status = RasnetServerComm::getRErrorStatus(ex); \
  } catch (r_Error &ex) { \
      LDEBUG << "request " << request_name << " failed with rasdaman error: " << ex.what(); \
      status = RasnetServerComm::getRErrorStatus(ex); \
  } catch (common::Exception &ex) { \
      LDEBUG << "request " << request_name << " failed with rasdaman exception: " << ex.what(); \
      status = RasnetServerComm::getCommonExceptionStatus(ex); \
  } catch (std::exception &ex) { \
      LERROR << "request " << request_name << " failed with general exception: " << ex.what(); \
      status = RasnetServerComm::getSTLExceptionStatus(ex); \
  } catch (...) { \
      LERROR << "request " << request_name << " failed with unknown exception."; \
      status = RasnetServerComm::getUnknownExceptionStatus(); \
  } \
  { finally_block } \
  return status;

// Helper for specifying well the try/finally blocks above.
#define CODE(...) __VA_ARGS__

RasnetServerComm::RasnetServerComm(std::shared_ptr<rasserver::ClientManager> cm)
    : clientManager{cm}
{
}

grpc::Status RasnetServerComm::OpenServerDatabase(
    UNUSED ServerContext *context, const OpenServerDatabaseReq *request, UNUSED OpenServerDatabaseRepl *response)
{
    HANDLE_REQUEST("OpenServerDatabase", CODE(
        RasServerEntry::getInstance().openDB(request->database_name().c_str());
    ), CODE())
}

grpc::Status RasnetServerComm::CloseServerDatabase(
    UNUSED ServerContext *context, UNUSED const CloseServerDatabaseReq *request, UNUSED Void *response)
{
    HANDLE_REQUEST("CloseServerDatabase", CODE(
        RasServerEntry::getInstance().closeDB();
    ), CODE())
}

grpc::Status RasnetServerComm::CreateDatabase(
    UNUSED ServerContext *context, UNUSED const CreateDatabaseReq *request, UNUSED CreateDatabaseRepl *response)
{
    return grpc::Status::OK;
}

grpc::Status RasnetServerComm::DestroyDatabase(
    UNUSED ServerContext *context, UNUSED const DestroyDatabaseReq *request, UNUSED DestroyDatabaseRepl *response)
{
    return grpc::Status::OK;
}

grpc::Status RasnetServerComm::BeginTransaction(
    UNUSED ServerContext *context, const BeginTransactionReq *request, UNUSED BeginTransactionRepl *response)
{
    HANDLE_REQUEST("BeginTransaction", CODE(
        RasServerEntry::getInstance().beginTA(request->rw());
    ), CODE())
}

grpc::Status RasnetServerComm::CommitTransaction(
    UNUSED ServerContext *context, UNUSED const CommitTransactionReq *request, UNUSED CommitTransactionRepl *response)
{
    HANDLE_REQUEST("CommitTransaction",
    CODE(
        RasServerEntry::getInstance().commitTA();
    ), CODE(    
       this->clientManager->removeAllQueryStreamedResults();
    ))
}

grpc::Status RasnetServerComm::AbortTransaction(
    UNUSED ServerContext *context, UNUSED const AbortTransactionReq *request, UNUSED AbortTransactionRepl *response)
{
    HANDLE_REQUEST("AbortTransaction",
    CODE(
        RasServerEntry::getInstance().abortTA();
    ), CODE(    
       this->clientManager->removeAllQueryStreamedResults();
    ))
}

grpc::Status RasnetServerComm::IsTransactionOpen(
    UNUSED ServerContext *context, UNUSED const IsTransactionOpenReq *request, IsTransactionOpenRepl *response)
{
    HANDLE_REQUEST("IsTransactionOpen", CODE(
        response->set_isopen(RasServerEntry::getInstance().isOpenTA());
    ), CODE())
}

grpc::Status RasnetServerComm::StartInsertMDD(
    UNUSED ServerContext *context, const StartInsertMDDReq *request, StartInsertMDDRepl *response)
{
    HANDLE_REQUEST("StartInsertMDD", CODE(
        const char *collName = request->collname().c_str();
        r_Minterval mddDomain(request->domain().c_str());
        int typeLength = request->type_length();
        const char *typeName = request->type_name().c_str();
        r_OId oid(request->oid().c_str());
 
        int res = RasServerEntry::getInstance().compat_StartInsertPersMDD(
              collName, mddDomain, typeLength, typeName, oid);
        response->set_status(res);
    ), CODE())
}

grpc::Status RasnetServerComm::StartInsertTransMDD(
    UNUSED ServerContext *context, const StartInsertTransMDDReq *request, StartInsertTransMDDRepl *response)
{
    HANDLE_REQUEST("StartInsertTransMDD", CODE(
        const char *domain = request->domain().c_str();
        int typeLength = request->type_length();
        const char *typeName = request->type_name().c_str();
  
        int res =RasServerEntry::getInstance().compat_StartInsertTransMDD(
              domain, typeLength, typeName);
        response->set_status(res);
    ), CODE())
}

grpc::Status RasnetServerComm::InsertTile(
    UNUSED ServerContext *context, const InsertTileReq *request, InsertTileRepl *response)
{
    HANDLE_REQUEST("StartInsertTransMDD", CODE(
        std::unique_ptr<RPCMarray> rpcMarray;
        rpcMarray.reset(new RPCMarray());
        
        std::unique_ptr<char[]> domain;
        domain.reset(new char[request->domain().size() + 1]);
        std::strcpy(domain.get(), request->domain().c_str());
        rpcMarray->domain = domain.get();
        rpcMarray->cellTypeLength = static_cast<u_long>(request->type_length());
        rpcMarray->currentFormat = request->current_format();
        rpcMarray->storageFormat = request->storage_format();
        rpcMarray->data.confarray_len = static_cast<u_int>(request->data_length());
        rpcMarray->data.confarray_val = (char *) mymalloc(static_cast<size_t>(request->data_length()));
        memcpy(rpcMarray->data.confarray_val, request->data().c_str(), static_cast<size_t>(request->data_length()));
        
        int res = RasServerEntry::getInstance().compat_InsertTile(
              request->persistent(), rpcMarray.get());
        
        response->set_status(res);
    ), CODE())
}

grpc::Status RasnetServerComm::EndInsertMDD(
    UNUSED ServerContext *context, const EndInsertMDDReq *request, EndInsertMDDRepl *response)
{
    HANDLE_REQUEST("EndInsertMDD", CODE(
        int res = RasServerEntry::getInstance().compat_EndInsertMDD(request->persistent());
        response->set_status(res);
    ), CODE())
}

grpc::Status RasnetServerComm::InsertCollection(
    UNUSED ServerContext *context, const InsertCollectionReq *request, InsertCollectionRepl *response)
{
    HANDLE_REQUEST("InsertCollection", CODE(
        const char *collName = request->collection_name().c_str();
        const char *typeName = request->type_name().c_str();
        r_OId oid(request->oid().c_str());
        int statusCode = RasServerEntry::getInstance().compat_InsertCollection(collName, typeName, oid);
        response->set_status(statusCode);
    ), CODE())
}

grpc::Status RasnetServerComm::DeleteCollectionByName(
    UNUSED ServerContext *context, const DeleteCollectionByNameReq *request, DeleteCollectionByNameRepl *response)
{
    HANDLE_REQUEST("DeleteCollectionByName", CODE(
        const char *collName = request->collection_name().c_str();
        int res = RasServerEntry::getInstance().compat_DeleteCollByName(collName);
        response->set_status(res);
    ), CODE())
}

grpc::Status RasnetServerComm::DeleteCollectionByOid(
    UNUSED ServerContext *context, const DeleteCollectionByOidReq *request, DeleteCollectionByOidRepl *response)
{
    HANDLE_REQUEST("DeleteCollectionByOid", CODE(
        r_OId oid(request->oid().c_str());
        int res = RasServerEntry::getInstance().compat_DeleteObjByOId(oid);
        response->set_status(res);
    ), CODE())
}

grpc::Status RasnetServerComm::RemoveObjectFromCollection(
    UNUSED ServerContext *context, const RemoveObjectFromCollectionReq *request, RemoveObjectFromCollectionRepl *response)
{
    HANDLE_REQUEST("RemoveObjectFromCollection", CODE(
        const char *collName = request->collection_name().c_str();
        r_OId oid(request->oid().c_str());
        int res = RasServerEntry::getInstance().compat_RemoveObjFromColl(collName, oid);
        response->set_status(res);
    ), CODE())
}

grpc::Status RasnetServerComm::GetCollectionByNameOrOid(
    UNUSED ServerContext *context, const GetCollectionByNameOrOidReq *request, GetCollectionByNameOrOidRepl *response)
{
    HANDLE_REQUEST("GetCollectionByNameOrOid", CODE(
        std::string typeName;
        std::string typeStructure;
        std::string collName;
        r_OId oid;
        int res = 0;
        if (request->is_name())
        {
             collName = request->collection_identifier();
             res = RasServerEntry::getInstance().compat_GetCollectionByName(
                   collName.c_str(), typeName, typeStructure, oid);
        }
        else
        {
             oid = r_OId(request->collection_identifier().c_str());
             res = RasServerEntry::getInstance().compat_GetCollectionByName(
                   oid, typeName, typeStructure, collName);
        }
        response->set_status(res);
        response->set_type_name(typeName);
        response->set_type_structure(typeStructure);
        response->set_oid(oid.get_string_representation());
        response->set_collection_name(collName);
    ), CODE())
}

grpc::Status RasnetServerComm::GetCollOidsByNameOrOid(
    UNUSED ServerContext *context, const GetCollOidsByNameOrOidReq *request, GetCollOidsByNameOrOidRepl *response)
{
    HANDLE_REQUEST("GetCollOidsByNameOrOid", CODE(
        std::string typeName;
        std::string typeStructure;
        std::string collName;
        r_OId oid;
        RPCOIdEntry *oidTable     = NULL;
        unsigned int oidTableSize = 0;
        int res = 0;
        if (request->is_name())
        {
            collName = request->collection_identifier();
            res = RasServerEntry::getInstance().compat_GetCollectionOidsByName(
                  collName.c_str(), typeName, typeStructure, oid, oidTable, oidTableSize);
        }
        else
        {
            oid = r_OId(request->collection_identifier().c_str());
            res = RasServerEntry::getInstance().compat_GetCollectionOidsByOId(
                  oid, typeName, typeStructure, oidTable, oidTableSize, collName);
        }
        
        // fill response
        response->set_status(res);
        response->set_type_name(typeName);
        response->set_type_structure(typeStructure);
        response->set_collection_name(collName);
        if (oid.is_valid())
            response->set_oids_string(oid.get_string_representation());

        // cleanup
        if (oidTable != NULL)
        {
            for (uint i = 0; i < oidTableSize; ++i)
            {
                response->add_oid_set(oidTable[i].oid);
                free(oidTable[i].oid);
            }
        }
        delete [] oidTable;
    ), CODE())
}

grpc::Status RasnetServerComm::GetNextMDD(
    UNUSED ServerContext *context, UNUSED const GetNextMDDReq *request, GetNextMDDRepl *response)
{
    HANDLE_REQUEST("GetNextMDD", CODE(
        r_Minterval mddDomain;
        std::string typeName;
        std::string typeStructure;
        r_OId oid;
        unsigned short currentFormat;
        
        int statusCode = RasServerEntry::getInstance().compat_getNextMDD(
             mddDomain, typeName, typeStructure, oid, currentFormat);
        
        response->set_status(statusCode);
        response->set_domain(mddDomain.to_string());
        response->set_type_name(typeName);
        response->set_type_structure(typeStructure);
        response->set_oid(oid.get_string_representation() ? oid.get_string_representation() : "");
        response->set_current_format(currentFormat);
    ), CODE())
}

grpc::Status RasnetServerComm::GetNextTile(
    UNUSED ServerContext *context, UNUSED const GetNextTileReq *request, GetNextTileRepl *response)
{
    HANDLE_REQUEST("GetNextTile", CODE(
        RPCMarray *rpcMarray;
        int res = RasServerEntry::getInstance().compat_getNextTile(&rpcMarray);
        response->set_status(res);
        if (rpcMarray != NULL)
        {
            response->set_domain(rpcMarray->domain);
            response->set_cell_type_length(rpcMarray->cellTypeLength);
            response->set_current_format(rpcMarray->currentFormat);
            response->set_storage_format(rpcMarray->storageFormat);
            response->set_data_length(static_cast<int>(rpcMarray->data.confarray_len));
            response->set_data(rpcMarray->data.confarray_val, rpcMarray->data.confarray_len);
        
            free(rpcMarray->domain);
            delete rpcMarray;
        }
    ), CODE())
}

grpc::Status RasnetServerComm::EndTransfer(
    UNUSED ServerContext *context, UNUSED const EndTransferReq *request, EndTransferRepl *response)
{
    HANDLE_REQUEST("EndTransfer", CODE(
        int res = RasServerEntry::getInstance().compat_endTransfer();
        response->set_status(res);
    ), CODE())
}

grpc::Status RasnetServerComm::InitUpdate(
    UNUSED ServerContext *context, UNUSED const InitUpdateReq *request, InitUpdateRepl *response)
{
    HANDLE_REQUEST("InitUpdate", CODE(
        int res = RasServerEntry::getInstance().compat_InitUpdate();
        response->set_status(res);
    ), CODE())
}


#define INITPTR(a) a = 0
#define SECUREPTR(a) if(a == 0) a = strdup("")
#define FREEPTR(a) free(a)

grpc::Status RasnetServerComm::ExecuteQuery(
    UNUSED ServerContext *context, const ExecuteQueryReq *request, ExecuteQueryRepl *response)
{
    HANDLE_REQUEST("ExecuteQuery", CODE(
        const char *query = request->query().c_str();
        ExecuteQueryRes queryResult;
        int statusCode = RasServerEntry::getInstance().compat_executeQueryRpc(query, queryResult);
        
        SECUREPTR(queryResult.token);
        SECUREPTR(queryResult.typeName);
        SECUREPTR(queryResult.typeStructure);
        
        response->set_status(statusCode);
        response->set_err_no(queryResult.errorNo);
        response->set_line_no(queryResult.lineNo);
        response->set_col_no(queryResult.columnNo);
        response->set_token(queryResult.token);
        response->set_type_name(queryResult.typeName);
        response->set_type_structure(queryResult.typeStructure);
        
        FREEPTR(queryResult.token);
        FREEPTR(queryResult.typeName);
        FREEPTR(queryResult.typeStructure);
    ), CODE())
}

grpc::Status RasnetServerComm::ExecuteHttpQuery(
    UNUSED ServerContext *context, const ExecuteHttpQueryReq *request, ExecuteHttpQueryRepl *response)
{
    HANDLE_REQUEST("ExecuteHttpQuery", CODE(
        char *resultBuffer = 0;
        long resultLen = RasServerEntry::getInstance().compat_executeQueryHttp(
            request->data().c_str(), request->data().length(), resultBuffer);
        response->set_data(resultBuffer, static_cast<size_t>(resultLen));
        delete [] resultBuffer;
    ), CODE())
}

grpc::Status RasnetServerComm::BeginStreamedHttpQuery(
    UNUSED ServerContext *context, const BeginStreamedHttpQueryReq *request, StreamedHttpQueryRepl *response)
{
    HANDLE_REQUEST("BeginStreamedHttpQuery", CODE(
        char *resultBuffer = 0;
        long resultLen = RasServerEntry::getInstance().compat_executeQueryHttp(
            request->data().c_str(), request->data().length(), resultBuffer);
        
        auto result = std::make_shared<rasserver::ClientQueryStreamedResult>(
            resultBuffer, static_cast<uint64_t>(resultLen), request->client_uuid());
        
        auto nextChunk = result->getNextChunk();
        response->set_data(nextChunk.bytes, nextChunk.length);
        response->set_bytes_left(result->getRemainingBytesLength());
        response->set_data_length(std::uint64_t(resultLen));
        
        static std::atomic<std::uint32_t> requestIdCounter{};
        auto requestUUID = ++requestIdCounter;
        response->set_uuid(requestUUID);
        
        this->clientManager->addQueryStreamedResult(requestUUID, result);
    ), CODE())
}

grpc::Status RasnetServerComm::GetNextStreamedHttpQuery(
    UNUSED ServerContext *context, const GetNextStreamedHttpQueryReq *request, StreamedHttpQueryRepl *response)
{
    HANDLE_REQUEST("GetNextStreamedHttpQuery", CODE(
        auto result = this->clientManager->getQueryStreamedResult(request->uuid());
        auto nextChunk = result->getNextChunk();
        response->set_data(nextChunk.bytes, nextChunk.length);
        response->set_bytes_left(result->getRemainingBytesLength());
        if (result->getRemainingBytesLength() == 0)
            this->clientManager->cleanQueryStreamedResult(request->uuid());
    ), CODE())
}

grpc::Status RasnetServerComm::GetNextElement(
    UNUSED ServerContext *context, UNUSED const GetNextElementReq *request, GetNextElementRepl *response)
{
    HANDLE_REQUEST("GetNextElement", CODE(
        char *buffer = NULL;
        unsigned int bufferSize;
        int statusCode = RasServerEntry::getInstance().compat_getNextElement(buffer, bufferSize);
        response->set_data(buffer, static_cast<size_t>(bufferSize));
        response->set_data_length(static_cast<int32_t>(bufferSize));
        response->set_status(statusCode);
        delete [] buffer;
    ), CODE())
}

grpc::Status RasnetServerComm::ExecuteUpdateQuery(
    UNUSED ServerContext *context, const ExecuteUpdateQueryReq *request, ExecuteUpdateQueryRepl *response)
{
    HANDLE_REQUEST("ExecuteUpdateQuery", CODE(
        const char *query = request->query().c_str();
        ExecuteUpdateRes returnStructure;
        int statusCode = RasServerEntry::getInstance().compat_ExecuteUpdateQuery(query, returnStructure);
        const char *token = returnStructure.token != NULL ? returnStructure.token : "";
        response->set_status(statusCode);
        response->set_errono(returnStructure.errorNo);
        response->set_lineno(returnStructure.lineNo);
        response->set_colno(returnStructure.columnNo);
        response->set_token(token);
        free(returnStructure.token);
    ), CODE())
}

grpc::Status RasnetServerComm::ExecuteInsertQuery(UNUSED ServerContext *context,
        const ExecuteInsertQueryReq *request,
        ExecuteInsertQueryRepl *response)
{
    HANDLE_REQUEST("ExecuteInsertQuery", CODE(
        const char *query = request->query().c_str();
        
        ExecuteQueryRes queryRes;
        INITPTR(queryRes.token);
        INITPTR(queryRes.typeName);
        INITPTR(queryRes.typeStructure);
        
        int res = RasServerEntry::getInstance().compat_ExecuteInsertQuery(query, queryRes);
        SECUREPTR(queryRes.token);
        SECUREPTR(queryRes.typeName);
        SECUREPTR(queryRes.typeStructure);
        
        response->set_status(res);
        response->set_errono(queryRes.errorNo);
        response->set_lineno(queryRes.lineNo);
        response->set_colno(queryRes.columnNo);
        response->set_token(queryRes.token);
        response->set_type_name(queryRes.typeName);
        response->set_type_structure(queryRes.typeStructure);
        
        FREEPTR(queryRes.token);
        FREEPTR(queryRes.typeName);
        FREEPTR(queryRes.typeStructure);
    ), CODE())
}

grpc::Status RasnetServerComm::GetNewOid(
    UNUSED ServerContext *context, const GetNewOidReq *request, GetNewOidRepl *response)
{
    HANDLE_REQUEST("GetNewOid", CODE(
        int objectType = request->object_type();
        r_OId oid = RasServerEntry::getInstance().compat_getNewOId((unsigned short)objectType);
        response->set_oid(oid.get_string_representation());
    ), CODE())
}

grpc::Status RasnetServerComm::GetObjectType(
    UNUSED ServerContext *context, const GetObjectTypeReq *request, GetObjectTypeRepl *response)
{
    HANDLE_REQUEST("GetObjectType", CODE(
        r_OId oid(request->oid().c_str());
        unsigned short objectType;
        int statusCode = RasServerEntry::getInstance().compat_GetObjectType(oid, objectType);
        response->set_status(statusCode);
        response->set_object_type(objectType);
    ), CODE())
}

grpc::Status RasnetServerComm::GetTypeStructure(
    UNUSED ServerContext *context, const GetTypeStructureReq *request, GetTypeStructureRepl *response)
{
    HANDLE_REQUEST("GetTypeStructure", CODE(
        const char *typeName = request->type_name().c_str();
        int typeType = request->type_type();
        std::string typeStructure;
        int res = RasServerEntry::getInstance().compat_GetTypeStructure(
             typeName, typeType, typeStructure);
        response->set_status(res);
        response->set_type_structure(typeStructure);
    ), CODE())
}

grpc::Status RasnetServerComm::SetFormat(
    UNUSED ServerContext *context, const SetFormatReq *request, SetFormatRepl *response)
{
    HANDLE_REQUEST("SetFormat", CODE(
        int format = request->format();
        const char *params = request->format_params().c_str();
        int res = request->transfer_format() == 1
           ? RasServerEntry::getInstance().compat_SetTransferFormat(format, params)
           : RasServerEntry::getInstance().compat_SetStorageFormat(format, params);
        response->set_status(res);
    ), CODE())
}

grpc::Status RasnetServerComm::KeepAlive(
    UNUSED ServerContext *context, const KeepAliveRequest *request, UNUSED Void *response)
{
    HANDLE_REQUEST("KeepAlive", CODE(
        this->clientManager->resetLiveliness(request->client_uuid());
    ), CODE())
}

grpc::Status RasnetServerComm::getRErrorStatus(r_Error &err)
{
    common::ErrorMessage message;
    message.set_error_no(err.get_errorno());
    message.set_kind(err.get_kind());
    message.set_error_text(err.what());
    message.set_type(ErrorMessage::RERROR);
    return grpc::Status(grpc::StatusCode::UNKNOWN, message.SerializeAsString());
}

grpc::Status RasnetServerComm::getSTLExceptionStatus(std::exception &ex)
{
    common::ErrorMessage message;
    message.set_error_text(ex.what());
    message.set_type(ErrorMessage::STL);
    return grpc::Status(grpc::StatusCode::UNKNOWN, message.SerializeAsString());
}

grpc::Status RasnetServerComm::getCommonExceptionStatus(common::Exception &ex)
{
    common::ErrorMessage message;
    message.set_error_text(ex.what());
    message.set_type(ErrorMessage::STL);
    return grpc::Status(grpc::StatusCode::UNKNOWN, message.SerializeAsString());  
}

grpc::Status RasnetServerComm::getUnknownExceptionStatus()
{
    common::ErrorMessage message;
    message.set_type(ErrorMessage::UNKNOWN);
    return grpc::Status(grpc::StatusCode::UNKNOWN, message.SerializeAsString());
}

// clang-format on
