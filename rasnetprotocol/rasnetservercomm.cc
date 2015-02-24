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
#include "../mymalloc/mymalloc.h"
#include "../server/rasserver_entry.hh"
#include "../debug/debug-srv.hh"

RasnetServerComm::RasnetServerComm(::boost::shared_ptr<rasserver::ClientManager> clientManager)
{
    this->clientManager = clientManager;
}


void RasnetServerComm::OpenServerDatabase(google::protobuf::RpcController *controller, const rasnet::service::OpenServerDatabaseReq *request, rasnet::service::OpenServerDatabaseRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
    rasServerEntry.compat_openDB(request->database_name().c_str());
}

void RasnetServerComm::CloseServerDatabase(google::protobuf::RpcController *controller, const rasnet::service::CloseServerDatabaseReq *request, rasnet::service::Void *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
    rasServerEntry.compat_closeDB();
}

void RasnetServerComm::CreateDatabase(google::protobuf::RpcController *controller, const rasnet::service::CreateDatabaseReq *request, rasnet::service::CreateDatabaseRepl *response, google::protobuf::Closure *done)
{

}

void RasnetServerComm::DestroyDatabase(google::protobuf::RpcController *controller, const rasnet::service::DestroyDatabaseReq *request, rasnet::service::DestroyDatabaseRepl *response, google::protobuf::Closure *done)
{

}

void RasnetServerComm::BeginTransaction(google::protobuf::RpcController *controller, const rasnet::service::BeginTransactionReq *request, rasnet::service::BeginTransactionRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
    rasServerEntry.compat_beginTA(request->rw());
}

void RasnetServerComm::IsTransactionOpen(google::protobuf::RpcController *controller, const rasnet::service::IsTransactionOpenReq *request, rasnet::service::IsTransactionOpenRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();
    bool isOpen = rasserver.compat_isOpenTA();
    response->set_isopen(isOpen);
}

void RasnetServerComm::CommitTransaction(google::protobuf::RpcController *controller, const rasnet::service::CommitTransactionReq *request, rasnet::service::CommitTransactionRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
    rasServerEntry.compat_commitTA();
}

void RasnetServerComm::AbortTransaction(google::protobuf::RpcController *controller, const rasnet::service::AbortTransactionReq *request, rasnet::service::AbortTransactionRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
    rasServerEntry.compat_abortTA();
}

void RasnetServerComm::StartInsertMDD(google::protobuf::RpcController *controller, const rasnet::service::StartInsertMDDReq *request, rasnet::service::StartInsertMDDRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    const char* collName = request->collname().c_str();
    r_Minterval mddDomain(request->domain().c_str());
    int typeLength = request->type_length();
    const char* typeName = request->type_name().c_str();
    r_OId oid(request->oid().c_str());

    int status = rasserver.compat_StartInsertPersMDD(collName, mddDomain, typeLength, typeName, oid);

    response->set_status(status);
}

void RasnetServerComm::StartInsertTransMDD(google::protobuf::RpcController *controller, const rasnet::service::StartInsertTransMDDReq *request, rasnet::service::StartInsertTransMDDRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    const char* domain = request->domain().c_str();
    int typeLength = request->type_length();
    const char* typeName = request->type_name().c_str();

    int status = rasserver.compat_StartInsertTransMDD(domain, typeLength, typeName);
    response->set_status(status);
}

void RasnetServerComm::InsertTile(google::protobuf::RpcController *controller, const rasnet::service::InsertTileReq *request, rasnet::service::InsertTileRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    RPCMarray *rpcMarray = new RPCMarray();

    int persistent = request->persistent();

    rpcMarray->domain = strdup(request->domain().c_str());
    rpcMarray->cellTypeLength = request->type_length();
    rpcMarray->currentFormat = request->current_format();
    rpcMarray->storageFormat = request->storage_format();
    rpcMarray->data.confarray_len = request->data_length();
    rpcMarray->data.confarray_val = (char*) mymalloc(request->data_length());
    memcpy(rpcMarray->data.confarray_val, request->data().c_str(), request->data_length());

    int status = rasserver.compat_InsertTile(persistent, rpcMarray);

    response->set_status(status);

    delete rpcMarray;
}

void RasnetServerComm::EndInsertMDD(google::protobuf::RpcController *controller, const rasnet::service::EndInsertMDDReq *request, rasnet::service::EndInsertMDDRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();
    int persistent = request->persistent();
    int status = rasserver.compat_EndInsertMDD(persistent);

    response->set_status(status);
}

void RasnetServerComm::InsertCollection(google::protobuf::RpcController *controller, const rasnet::service::InsertCollectionReq *request, rasnet::service::InsertCollectionRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    const char* collName = request->collection_name().c_str();
    const char* typeName = request->type_name().c_str();
    r_OId oid(request->oid().c_str());

    int status = rasserver.compat_InsertCollection(collName, typeName, oid);
    response->set_status(status);
}

void RasnetServerComm::DeleteCollectionByName(google::protobuf::RpcController *controller, const rasnet::service::DeleteCollectionByNameReq *request, rasnet::service::DeleteCollectionByNameRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    const char* collName = request->collection_name().c_str();
    int status = rasserver.compat_DeleteCollByName(collName);

    response->set_status(status);
}

void RasnetServerComm::DeleteCollectionByOid(google::protobuf::RpcController *controller, const rasnet::service::DeleteCollectionByOidReq *request, rasnet::service::DeleteCollectionByOidRepl *response, google::protobuf::Closure *done)
{
   RasServerEntry& rasserver = RasServerEntry::getInstance();

   r_OId oid(request->oid().c_str());

   int status = rasserver.compat_DeleteObjByOId(oid);
   response->set_status(status);
}

void RasnetServerComm::RemoveObjectFromCollection(google::protobuf::RpcController *controller, const rasnet::service::RemoveObjectFromCollectionReq *request, rasnet::service::RemoveObjectFromCollectionRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    const char* collName = request->collection_name().c_str();
    r_OId oid(request->oid().c_str());

    int status = rasserver.compat_RemoveObjFromColl(collName, oid);
    response->set_status(status);
}

void RasnetServerComm::GetCollectionByNameOrOid(google::protobuf::RpcController *controller, const rasnet::service::GetCollectionByNameOrOidReq *request, rasnet::service::GetCollectionByNameOrOidRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    char* typeName      = NULL;
    char* typeStructure = NULL;
    char* collName      = NULL;
    r_OId oid;
    int status = 0;

    if (request->is_name())
    {
        collName = strdup(request->collection_identifier().c_str());
        status = rasserver.compat_GetCollectionByName(collName, typeName, typeStructure, oid);
    }
    else
    {
        oid = r_OId(request->collection_identifier().c_str());
        status = rasserver.compat_GetCollectionByName(oid, typeName, typeStructure, collName);
    }

    response->set_status(status);
    response->set_type_name(typeName);
    response->set_type_structure(typeStructure);
    response->set_oid(oid.get_string_representation());
    response->set_collection_name(collName);

    free(typeName);
    free(typeStructure);
    free(collName);
}

void RasnetServerComm::GetCollOidsByNameOrOid(google::protobuf::RpcController *controller, const rasnet::service::GetCollOidsByNameOrOidReq *request, rasnet::service::GetCollOidsByNameOrOidRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    char* typeName      = NULL;
    char* typeStructure = NULL;
    char* collName      = NULL;
    r_OId oid;
    RPCOIdEntry* oidTable     = NULL;
    unsigned int oidTableSize = 0;
    int status = 0;

    if (request->is_name())
    {
        collName = strdup(request->collection_identifier().c_str());
        status = rasserver.compat_GetCollectionOidsByName(collName, typeName, typeStructure, oid, oidTable, oidTableSize);
    }
    else
    {
        oid = r_OId(request->collection_identifier().c_str());
        status = rasserver.compat_GetCollectionOidsByOId(oid, typeName, typeStructure, oidTable, oidTableSize, collName);
    }

    response->set_status(status);
    response->set_type_name(typeName != NULL ? typeName : "");
    response->set_type_structure(typeStructure != NULL ? typeStructure : "");
    response->set_collection_name(collName != NULL ? collName : "");

    if (oidTable != NULL)
    {
        for (int i = 0; i < oidTableSize; ++i)
        {
            response->add_oid_set(oidTable[i].oid);
            free(oidTable[i].oid);
        }
    }

    free(typeName);
    free(typeStructure);
    free(collName);
    free(oidTable);
}

void RasnetServerComm::GetNextMDD(google::protobuf::RpcController *controller, const rasnet::service::GetNextMDDReq *request, rasnet::service::GetNextMDDRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();
    r_Minterval  mddDomain;
    char*        typeName;
    char*        typeStructure;
    r_OId        oid;
    unsigned short  currentFormat;

    int status = rasserver.compat_getNextMDD(mddDomain, typeName, typeStructure, oid, currentFormat);

    response->set_status(status);
    response->set_domain(mddDomain.get_string_representation());
    response->set_type_name(typeName);
    response->set_type_structure(typeStructure);
    response->set_oid(oid.get_string_representation() ? oid.get_string_representation() : "");
    response->set_current_format(currentFormat);

    free(typeName);
    free(typeStructure);
}

void RasnetServerComm::GetNextTile(google::protobuf::RpcController *controller, const rasnet::service::GetNextTileReq *request, rasnet::service::GetNextTileRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    RPCMarray *tempRpcMarray;

    int status = rasserver.compat_getNextTile(&tempRpcMarray);

    response->set_status(status);

    if (tempRpcMarray != NULL)
    {
        response->set_domain(tempRpcMarray->domain);
        response->set_cell_type_length(tempRpcMarray->cellTypeLength);
        response->set_current_format(tempRpcMarray->currentFormat);
        response->set_storage_format(tempRpcMarray->storageFormat);
        response->set_data_length(tempRpcMarray->data.confarray_len);
        response->set_data(tempRpcMarray->data.confarray_val, tempRpcMarray->data.confarray_len);

        free(tempRpcMarray->domain);
        free(tempRpcMarray);
    }
}

void RasnetServerComm::EndTransfer(google::protobuf::RpcController *controller, const rasnet::service::EndTransferReq *request, rasnet::service::EndTransferRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
    int status = rasServerEntry.compat_endTransfer();

    response->set_status(status);
}

void RasnetServerComm::InitUpdate(google::protobuf::RpcController *controller, const rasnet::service::InitUpdateReq *request, rasnet::service::InitUpdateRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    int status = rasserver.compat_InitUpdate();
    response->set_status(status);
}

#define INITPTR(a) a = 0
#define SECUREPTR(a) if(a == 0) a = strdup("")
#define FREEPTR(a) free(a)

void RasnetServerComm::ExecuteQuery(google::protobuf::RpcController *controller, const rasnet::service::ExecuteQueryReq *request, rasnet::service::ExecuteQueryRepl *response, google::protobuf::Closure *done)
{
    const char* query = request->query().c_str();
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    ExecuteQueryRes queryResult;
    INITPTR(queryResult.token);
    INITPTR(queryResult.typeName);
    INITPTR(queryResult.typeStructure);

    int status = rasserver.compat_executeQueryRpc(query, queryResult);

    SECUREPTR(queryResult.token);
    SECUREPTR(queryResult.typeName);
    SECUREPTR(queryResult.typeStructure);

    response->set_status(status);
    response->set_err_no(queryResult.errorNo);
    response->set_line_no(queryResult.lineNo);
    response->set_col_no(queryResult.columnNo);
    response->set_token(queryResult.token);
    response->set_type_name(queryResult.typeName);
    response->set_type_structure(queryResult.typeStructure);

    FREEPTR(queryResult.token);
    FREEPTR(queryResult.typeName);
    FREEPTR(queryResult.typeStructure);
}

void RasnetServerComm::GetNextElement(google::protobuf::RpcController *controller, const rasnet::service::GetNextElementReq *request, rasnet::service::GetNextElementRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasServerEntry = RasServerEntry::getInstance();

    char* buffer = NULL;
    unsigned int bufferSize;

    int status = rasServerEntry.compat_getNextElement(buffer, bufferSize);
    response->set_data(buffer, bufferSize);
    response->set_data_length(bufferSize);
    response->set_status(status);
    free(buffer);
}

void RasnetServerComm::ExecuteUpdateQuery(google::protobuf::RpcController *controller, const rasnet::service::ExecuteUpdateQueryReq *request, rasnet::service::ExecuteUpdateQueryRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();
    const char* query = request->query().c_str();

    ExecuteUpdateRes returnStructure;
    returnStructure.token = NULL;

    int status = rasserver.compat_ExecuteUpdateQuery(query, returnStructure);

    const char *token = returnStructure.token != NULL ? returnStructure.token : "";

    response->set_status(status);
    response->set_errono(returnStructure.errorNo);
    response->set_lineno(returnStructure.lineNo);
    response->set_colno(returnStructure.columnNo);
    response->set_token(token);

    free(returnStructure.token);
}

void RasnetServerComm::ExecuteInsertQuery(google::protobuf::RpcController *controller, const rasnet::service::ExecuteInsertQueryReq *request, rasnet::service::ExecuteInsertQueryRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();
    const char* query = request->query().c_str();

    ExecuteQueryRes queryResult;
    INITPTR(queryResult.token);
    INITPTR(queryResult.typeName);
    INITPTR(queryResult.typeStructure);

    int status = rasserver.compat_ExecuteInsertQuery(query, queryResult);
    SECUREPTR(queryResult.token);
    SECUREPTR(queryResult.typeName);
    SECUREPTR(queryResult.typeStructure);

    response->set_status(status);
    response->set_errono(queryResult.errorNo);
    response->set_lineno(queryResult.lineNo);
    response->set_colno(queryResult.columnNo);
    response->set_token(queryResult.token);
    response->set_type_name(queryResult.typeName);
    response->set_type_structure(queryResult.typeStructure);

    FREEPTR(queryResult.token);
    FREEPTR(queryResult.typeName);
    FREEPTR(queryResult.typeStructure);
}

void RasnetServerComm::ExecuteHttpQuery(google::protobuf::RpcController *controller, const rasnet::service::ExecuteHttpQueryReq *request, rasnet::service::ExecuteHttpQueryRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();
    char *resultBuffer = 0;
    int resultLen = rasserver.compat_executeQueryHttp(request->data().c_str(), request->data_length(), resultBuffer);

    response->set_data(resultBuffer, resultLen);
    delete[] resultBuffer;
}

void RasnetServerComm::GetNewOid(google::protobuf::RpcController *controller, const rasnet::service::GetNewOidReq *request, rasnet::service::GetNewOidRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasServerEntry = RasServerEntry::getInstance();
    int objectType = request->object_type();
    r_OId oid = rasServerEntry.compat_getNewOId((unsigned short)objectType);

    response->set_oid(oid.get_string_representation());
}

void RasnetServerComm::GetObjectType(google::protobuf::RpcController *controller, const rasnet::service::GetObjectTypeReq *request, rasnet::service::GetObjectTypeRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    r_OId oid(request->oid().c_str());
    unsigned short objectType;

    int status = rasserver.compat_GetObjectType(oid, objectType);

    response->set_status(status);
    response->set_object_type(objectType);
}

void RasnetServerComm::GetTypeStructure(google::protobuf::RpcController *controller, const rasnet::service::GetTypeStructureReq *request, rasnet::service::GetTypeStructureRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    const char* typeName = request->type_name().c_str();
    int typeType = request->type_type();
    char* typeStructure = NULL;

    int status = rasserver.compat_GetTypeStructure(typeName, typeType, typeStructure);

    response->set_status(status);
    response->set_type_structure(typeStructure);

    free(typeStructure);
}

void RasnetServerComm::SetFormat(google::protobuf::RpcController *controller, const rasnet::service::SetFormatReq *request, rasnet::service::SetFormatRepl *response, google::protobuf::Closure *done)
{
    RasServerEntry& rasserver = RasServerEntry::getInstance();

    int whichFormat = request->transfer_format();
    int format = request->format();
    const char* params = request->format_params().c_str();

    int status = 0;

    if(whichFormat == 1)
    {
        status = rasserver.compat_SetTransferFormat(format, params);
    }
    else
    {
        status = rasserver.compat_SetStorageFormat(format, params);
    }

    response->set_status(status);
}

void RasnetServerComm::KeepAlive(google::protobuf::RpcController *controller, const rasnet::service::KeepAliveRequest *request, rasnet::service::Void *response, google::protobuf::Closure *done)
{
    this->clientManager->resetLiveliness(request->client_uuid());
}
