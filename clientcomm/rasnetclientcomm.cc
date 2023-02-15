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

#include "rasnetclientcomm.hh"
#include "globals.hh"
#include "rasodmg/transaction.hh"
#include "rasodmg/database.hh"
#include "rasodmg/iterator.hh"
#include "rasodmg/set.hh"
#include "rasodmg/ref.hh"
#include "rasodmg/storagelayout.hh"
#include "rasodmg/gmarray.hh"
#include "rasodmg/oqlquery.hh"
#include "rasodmg/genreftype.hh"

#include "raslib/minterval.hh"
#include "raslib/primitivetype.hh"
#include "raslib/complextype.hh"
#include "raslib/structuretype.hh"
#include "raslib/primitive.hh"
#include "raslib/complex.hh"
#include "raslib/structure.hh"
#include "raslib/turboqueryresult.hh"
#include "raslib/miterd.hh"
#include "mymalloc/mymalloc.h"
#include "servercomm/rpcif.h"

#include "common/crypto/crypto.hh"
#include "common/uuid/uuid.hh"
#include "common/string/stringutil.hh"
#include "common/grpc/grpcutils.hh"
#include "common/exceptions/invalidargumentexception.hh"
#include "common/pragmas/pragmas.hh"
#include "common/util/scopeguard.hh"
#include "common/grpc/messages/error.pb.h"
#include <logging.hh>

#include <cstring>
#include <fstream>
#include <chrono>
#include <grpc++/grpc++.h>
#include <grpc++/security/credentials.h>

using common::GrpcUtils;
using common::ErrorMessage;
using common::StringUtil;

using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;

using namespace rasnet::service;

RasnetClientComm::RasnetClientComm(const std::string &rasmgrHost1, int rasmgrPort1)
  : rasmgrHostname{rasmgrHost1}
{
    this->rasmgrHost = GrpcUtils::constructAddressString(rasmgrHost1, boost::uint32_t(rasmgrPort1));
}

RasnetClientComm::~RasnetClientComm() noexcept
{
    this->stopRasMgrKeepAlive();
    this->stopRasServerKeepAlive();
    this->closeRasmgrService();
    this->closeRasserverService();
}

int RasnetClientComm::connectClient(const std::string &userName, const std::string &passwordHash)
{
    LDEBUG << "Connecting with rasmgr client with ID: " << clientId << " with rasmgr on " << this->rasmgrHostname;
    
    int retval = 0; // ok
    
    ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    ConnectReq req;
    req.set_username(userName);
    req.set_passwordhash(passwordHash);
    req.set_hostname(this->rasmgrHostname);
    ConnectRepl repl;
    auto status = this->getRasMgrService()->Connect(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
        retval = 1;
    }
    
    this->clientId = repl.clientid();

    // Send keep alive messages to rasmgr until openDB is called
    this->keepAliveTimeout = repl.keepalivetimeout();
    this->startRasMgrKeepAlive();

    return retval;
}

int RasnetClientComm::disconnectClient()
{
    LDEBUG << "Disconnecting from rasmgr client with ID: " << clientId;
    
    int retval = 0; // ok
    ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    DisconnectReq req;
    req.set_clientid(this->clientId);
    Void repl;
    grpc::Status status = this->getRasMgrService()->Disconnect(&context, req, &repl);
    this->stopRasMgrKeepAlive();
    this->closeRasmgrService();
    if (!status.ok())
    {
        handleError(status.error_message());
        retval = 1;
    }
    return retval;
}

int RasnetClientComm::openDB(const char *database1)
{
    int retval = 0; // ok
    {
        LDEBUG << "Opening rasmgr database for client with ID: " << clientId;
        ClientContext context;
        GrpcUtils::setDeadline(context, timeoutMs);
        OpenDbReq req;
        req.set_clientid(this->clientId);
        req.set_databasename(database1);
        OpenDbRepl repl;
        grpc::Status openDbStatus = this->getRasMgrService()->OpenDb(&context, req, &repl);
        this->remoteClientId = repl.clientsessionid();
        if (this->remoteClientId == this->clientId)
        {
            // The allocated server belongs to the current rasmgr,
            // we can stop sending keep alive messages to the current rasmgr.
            LDEBUG << "Stopping rasmgr keep alive.";
            this->stopRasMgrKeepAlive();
        }
        if (!openDbStatus.ok())
        {
            handleError(openDbStatus.error_message());
            retval = 1;
        }
        this->rasServerHost = repl.serverhostname();
        this->rasServerPort = int(repl.port());
        this->sessionId = repl.dbsessionid();
        LDEBUG << "OpenDb assigned server: " << rasServerHost << ":" << rasServerPort << ", sessionId: " << sessionId;
    }
    {
        LDEBUG << "Opening rasserver database for client with ID: " << clientId;
        grpc::ClientContext context;
        GrpcUtils::setDeadline(context, timeoutMs);
        OpenServerDatabaseReq req;
        req.set_client_id(this->clientId);
        req.set_database_name(database1);
        OpenServerDatabaseRepl repl;
        grpc::Status openServerDbStatus = this->getRasServerService()->OpenServerDatabase(&context, req, &repl);
        LDEBUG << "Opened rasserver database for client with ID: " << clientId;
        if (!openServerDbStatus.ok())
        {
            handleError(openServerDbStatus.error_message());
            retval = 1;
        }
    }
    // Send keep alive messages to rasserver until closeDB is called
    this->startRasServerKeepAlive();
    return retval;
}

int RasnetClientComm::closeDB()
{
    int retval = 0; // ok

    try
    {
        {
            LDEBUG << "Closing rasserver database for client with ID: " << clientId;
            grpc::ClientContext context;
            GrpcUtils::setDeadline(context, timeoutMs);
            CloseServerDatabaseReq req;
            req.set_client_id(this->clientId);
            Void repl;
            grpc::Status status = this->getRasServerService()->CloseServerDatabase(&context, req, &repl);
            if (!status.ok())
            {
                handleError(status.error_message());
                retval = 1;
            }
        }
        {
            LDEBUG << "Closing rasmgr database for client with ID: " << clientId;
            grpc::ClientContext context;
            GrpcUtils::setDeadline(context, timeoutMs);
            CloseDbReq req;
            req.set_clientid(this->clientId);
            // The remoteClientId identifies local and remote sessions
            req.set_clientid(this->remoteClientId);
            req.set_dbsessionid(this->sessionId);
            Void repl;
            grpc::Status status = this->getRasMgrService()->CloseDb(&context, req, &repl);
            if (!status.ok())
            {
                handleError(status.error_message());
                retval = 1;
            }
        }

        this->stopRasServerKeepAlive();
        this->disconnectClient();
        this->closeRasserverService();
    }
    catch (...)
    {
        LDEBUG << "Closing database failed.";
        retval = 1;
    }

    return retval;
}

int RasnetClientComm::createDB(UNUSED const char *name)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

int RasnetClientComm::destroyDB(UNUSED const char *name)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

int RasnetClientComm::openTA(unsigned short readOnly)
{
    int retval = 0; // ok
    
    LDEBUG << "Begin rasserver transaction (ro: " << readOnly << "), client with ID: " << clientId;
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    BeginTransactionReq req;
    req.set_rw(readOnly == 0);
    req.set_client_id(this->clientId);
    BeginTransactionRepl repl;
    grpc::Status status = this->getRasServerService()->BeginTransaction(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }

    return retval;
}

int RasnetClientComm::commitTA()
{
    int retval = 0; // ok

    LDEBUG << "Commit rasserver transaction, client with ID: " << clientId;
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    CommitTransactionReq req;
    req.set_client_id(this->clientId);
    CommitTransactionRepl repl;
    grpc::Status status = this->getRasServerService()->CommitTransaction(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
        retval = 1;
    }

    return retval;
}

int RasnetClientComm::abortTA()
{
    int retval = 0; // ok
    try
    {
        LDEBUG << "Abort rasserver transaction, client with ID: " << clientId;
        grpc::ClientContext context;
        GrpcUtils::setDeadline(context, timeoutMs);
        AbortTransactionReq req;
        req.set_client_id(this->clientId);
        AbortTransactionRepl repl;
        grpc::Status status = this->getRasServerService()->AbortTransaction(&context, req, &repl);
        if (!status.ok())
        {
            handleError(status.error_message());
            retval = 1;
        }
    }
    catch (...)
    {
        LDEBUG << "Aborting transaction failed.";
        retval = 1;
    }

    return retval;
}

void RasnetClientComm::insertMDD(const char *collName, r_GMarray *mar)
{
    LDEBUG << "insertMDD into collection " << collName << ", client with ID: " << clientId;
    
    checkForRwTransaction();

    // initiate composition of MDD at server side
    int status = executeStartInsertPersMDD(collName, mar);
    switch (status)
    {
    case 0:
        break; // OK
    case 2:
        throw r_Error(r_Error::r_Error_DatabaseClassUndefined);
    case 3:
        throw r_Error(r_Error::r_Error_CollectionElementTypeMismatch);
    case 4:
        throw r_Error(r_Error::r_Error_TypeInvalid);
    default:
        throw r_Error(r_Error::r_Error_TransferFailed);
    }
    
    auto bagOfTiles = mar->get_storage_layout()->decomposeMDD(mar);
    LDEBUG << "inserting " << bagOfTiles.cardinality() << " tiles";
    auto iter = bagOfTiles.create_iterator();
    for (iter.reset(); iter.not_done(); iter.advance())
    {
        auto origTile = *iter;

        LTRACE << "inserting tile with domain " << origTile->spatial_domain()
               << " (" << (origTile->spatial_domain().cell_count() * origTile->get_type_length()) << " bytes), "
               << "band linearization: " << int(origTile->get_band_linearization());
        
        RPCMarray *rpcMarray{nullptr};
        getMarRpcRepresentation(origTile, rpcMarray, mar->get_storage_layout()->get_storage_format(), mar->get_base_type_schema());
        status = executeInsertTile(true, rpcMarray);
        // free rpcMarray structure (rpcMarray->data.confarray_val is freed somewhere else)
        freeMarRpcRepresentation(origTile, rpcMarray);
        // delete current tile (including data block)
        delete origTile;
        if (status > 0)
        {
            throw r_Error(r_Error::r_Error_TransferFailed);
        }
    }

    executeEndInsertMDD(true);

    // delete transient data
    bagOfTiles.remove_all();
}

r_Ref_Any RasnetClientComm::getMDDByOId(UNUSED const r_OId &oid)
{
    LDEBUG << "Internal error: RasnetClientComm::getMDDByOId() not implemented, returning empty r_Ref_Any().";
    return r_Ref_Any();
}

void RasnetClientComm::insertColl(const char *collName, const char *typeName, const r_OId &oid)
{
    LDEBUG << "insertColl " << collName << " " << typeName << ", oid " << oid.get_string_representation();
    checkForRwTransaction();
    
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    InsertCollectionReq req;
    req.set_client_id(this->clientId);
    req.set_collection_name(collName);
    req.set_type_name(typeName);
    req.set_oid(oid.get_string_representation());
    InsertCollectionRepl repl;
    grpc::Status status = this->getRasServerService()->InsertCollection(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    handleStatusCode(repl.status(), "insertColl");
}

void RasnetClientComm::deleteCollByName(const char *collName)
{
    LDEBUG << "deleteCollByName " << collName;
    checkForRwTransaction();
    
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    DeleteCollectionByNameReq req;
    req.set_client_id(this->clientId);
    req.set_collection_name(collName);
    DeleteCollectionByNameRepl repl;
    grpc::Status status = this->getRasServerService()->DeleteCollectionByName(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    handleStatusCode(repl.status(), "deleteCollByName");
}

void RasnetClientComm::deleteObjByOId(const r_OId &oid)
{
    LDEBUG << "deleteObjByOId " << oid.get_string_representation();
    checkForRwTransaction();
    
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    DeleteCollectionByOidReq req;
    req.set_client_id(this->clientId);
    req.set_oid(oid.get_string_representation());
    DeleteCollectionByOidRepl repl;
    grpc::Status status = this->getRasServerService()->DeleteCollectionByOid(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    handleStatusCode(repl.status(), "deleteCollByName");
}

void RasnetClientComm::removeObjFromColl(const char *name, const r_OId &oid)
{
    LDEBUG << "removeObjFromColl " << name << ", oid " << oid.get_string_representation();
    checkForRwTransaction();
    
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    RemoveObjectFromCollectionReq req;
    req.set_client_id(this->clientId);
    req.set_collection_name(name);
    req.set_oid(oid.get_string_representation());
    RemoveObjectFromCollectionRepl repl;
    grpc::Status status = this->getRasServerService()->RemoveObjectFromCollection(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    handleStatusCode(repl.status(), "removeObjFromColl");
}

r_Ref_Any RasnetClientComm::getCollByName(const char *name)
{
    LDEBUG << "getCollByName " << name;
    r_Ref_Any result = executeGetCollByNameOrOId(name, r_OId());
    return result;
}

r_Ref_Any RasnetClientComm::getCollByOId(const r_OId &oid)
{
    LDEBUG << "getCollByOId " << oid.get_string_representation();
    r_Ref_Any result = executeGetCollByNameOrOId(NULL, oid);
    return result;
}

r_Ref_Any RasnetClientComm::getCollOIdsByName(const char *name)
{
    LDEBUG << "getCollOIdsByName " << name;
    r_Ref_Any result = executeGetCollOIdsByNameOrOId(name, r_OId());
    return result;
}

r_Ref_Any RasnetClientComm::getCollOIdsByOId(const r_OId &oid)
{
    LDEBUG << "getCollOIdsByOId " << oid.get_string_representation();
    r_Ref_Any result = executeGetCollOIdsByNameOrOId(NULL, oid);
    return result;
}

void RasnetClientComm::executeQuery(const r_OQL_Query &query, r_Set<r_Ref_Any> &result)
{
    LDEBUG << "executeQuery retrieval";
    sendMDDConstants(query);
    int status = executeExecuteQuery(query.get_query(), result);
    switch (status)
    {
    case 0:
        getMDDCollection(result, 1);
        break; // 1== isQuery
    case 1:
        getElementCollection(result);
        break;
    case 2:
        // Status 2 - empty result. Should not be treated as default.
        break;
    default:
        LDEBUG << "Illegal status value " << status;
    }
}

void RasnetClientComm::executeQuery(const r_OQL_Query &query)
{
    LDEBUG << "executeQuery update";
    checkForRwTransaction();
    sendMDDConstants(query);
    executeExecuteUpdateQuery(query.get_query());
}

void RasnetClientComm::executeQuery(const r_OQL_Query &query, r_Set<r_Ref_Any> &result, UNUSED int dummy)
{
    LDEBUG << "executeQuery insert";
    checkForRwTransaction();
    sendMDDConstants(query);
    int status = executeExecuteUpdateQuery(query.get_query(), result);
    switch (status)
    {
    case 0:
        getMDDCollection(result, 1);
        break; // 1== isQuery
    case 1:
        getElementCollection(result);
        break;
    case 2:
        // empty result, should not be treated as default case
        break;
    default:
        LDEBUG << "Illegal status value " << status;
    }
}

r_OId RasnetClientComm::getNewOId(unsigned short objType)
{
    LDEBUG << "getNewOId objType = " << objType;
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    GetNewOidReq req;
    req.set_client_id(this->clientId);
    req.set_object_type(objType);
    GetNewOidRepl repl;
    grpc::Status status = this->getRasServerService()->GetNewOid(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    return r_OId(repl.oid().c_str());
}

unsigned short RasnetClientComm::getObjectType(const r_OId &oid)
{
    LDEBUG << "getObjectType oid = " << oid.get_string_representation();
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    GetObjectTypeReq req;
    req.set_client_id(this->clientId);
    req.set_oid(oid.get_string_representation());
    GetObjectTypeRepl repl;
    grpc::Status getObjectTypeStatus = this->getRasServerService()->GetObjectType(&context, req, &repl);
    if (!getObjectTypeStatus.ok())
    {
        handleError(getObjectTypeStatus.error_message());
    }
    handleStatusCode(repl.status(), "getObjectType");

    unsigned short objectType = repl.object_type();
    return objectType;
}

char *RasnetClientComm::getTypeStructure(const char *typeName, r_Type_Type typeType)
{
    LDEBUG << "getTypeStructure typeName = " << typeName << ", type type = " << int(typeType);
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    GetTypeStructureReq req;
    req.set_client_id(this->clientId);
    req.set_type_name(typeName);
    req.set_type_type(typeType);
    GetTypeStructureRepl repl;
    grpc::Status getTypesStructuresStatus = this->getRasServerService()->GetTypeStructure(&context, req, &repl);
    if (!getTypesStructuresStatus.ok())
    {
        handleError(getTypesStructuresStatus.error_message());
    }
    handleStatusCode(repl.status(), "getTypeStructure");

    const auto &src = repl.type_structure();
    char *typeStructure = new char[src.size() + 1];
    strcpy(typeStructure, src.c_str());
    return typeStructure;
}

int RasnetClientComm::setStorageFormat(r_Data_Format format, const char *formatParams)
{
    LDEBUG << "setTransferFormat format = " << format;
    storageFormat = format;
    if (storageFormatParams != NULL)
    {
        free(storageFormatParams);
        storageFormatParams = NULL;
    }
    if (formatParams != NULL)
    {
        storageFormatParams = strdup(formatParams);
        // extract ``compserver'' if present
        clientParams.process(storageFormatParams);
    }
    return executeSetFormat(false, format, formatParams);
}

int RasnetClientComm::setTransferFormat(r_Data_Format format, const char *formatParams)
{
    LDEBUG << "setStorageFormat format = " << format;
    transferFormat = format;
    if (transferFormatParams != NULL)
    {
        free(transferFormatParams);
        transferFormatParams = NULL;
    }
    if (formatParams != NULL)
    {
        transferFormatParams = strdup(formatParams);
        // extract ``exactformat'' if present
        clientParams.process(transferFormatParams);
    }
    return executeSetFormat(true, format, formatParams);
}

int RasnetClientComm::executeStartInsertPersMDD(const char *collName, r_GMarray *mar)
{
    LDEBUG << "executeStartInsertPersMDD collName = " << collName;
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    StartInsertMDDReq req;
    req.set_client_id(this->clientId);
    req.set_collname(collName);
    req.set_domain(mar->spatial_domain().to_string());
    req.set_type_length(static_cast<int32_t>(mar->get_type_length()));
    req.set_type_name(mar->get_type_name());
    req.set_oid(mar->get_oid().get_string_representation());
    StartInsertMDDRepl repl;
    grpc::Status status = this->getRasServerService()->StartInsertMDD(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    return repl.status();
}

int RasnetClientComm::executeInsertTile(bool persistent, RPCMarray *tile)
{
    LDEBUG << "executeInsertTile persistent = " << persistent;
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    InsertTileReq req;
    req.set_client_id(this->clientId);
    req.set_persistent(persistent);
    req.set_domain(tile->domain);
    req.set_type_length(tile->cellTypeLength);
    req.set_current_format(tile->currentFormat);
    req.set_storage_format(tile->storageFormat);
    req.set_data(tile->data.confarray_val, tile->data.confarray_len);
    req.set_data_length(static_cast<int32_t>(tile->data.confarray_len));
    req.set_band_linearization(static_cast<int32_t>(tile->bandLinearization));
    req.set_cell_linearization(static_cast<int32_t>(tile->cellLinearization));
    InsertTileRepl repl;
    grpc::Status status = this->getRasServerService()->InsertTile(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    return repl.status();
}

void RasnetClientComm::executeEndInsertMDD(bool persistent)
{
    LDEBUG << "executeEndInsertMDD persistent = " << persistent;
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    EndInsertMDDReq req;
    req.set_client_id(this->clientId);
    req.set_persistent(persistent);
    EndInsertMDDRepl repl;
    grpc::Status status = this->getRasServerService()->EndInsertMDD(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    handleStatusCode(repl.status(), "executeEndInsertMDD");
}

void RasnetClientComm::getMDDCollection(r_Set<r_Ref_Any> &mddColl, unsigned int isQuery)
{
    // repeat until all MDDs are transferred
    unsigned short moreMddToTransfer = 0;
    while (moreMddToTransfer == 0)
    {
        GetMDDRes *thisResult = executeGetNextMDD();
        moreMddToTransfer = thisResult->status;
        if (moreMddToTransfer == 2)
        {
            LERROR << "no transfer collection or empty transfer collection";
            throw r_Error(r_Error::r_Error_TransferFailed);
        }
        
        r_Ref<r_GMarray> mddResult;
        unsigned short tileStatus = getMDDCore(mddResult, thisResult, isQuery);

        // finally, insert the r_Marray into the set
        mddColl.insert_element(mddResult, 1);
        free(thisResult->domain);
        free(thisResult->typeName);
        free(thisResult->typeStructure);
        free(thisResult->oid);
        delete thisResult;

        if (tileStatus == 0)
            break; // no more tiles, done
    }
    executeEndTransfer();
}

int RasnetClientComm::executeEndTransfer()
{
    LDEBUG << "executeEndTransfer";
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    EndTransferReq req;
    req.set_client_id(this->clientId);
    EndTransferRepl repl;
    grpc::Status status = this->getRasServerService()->EndTransfer(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    return repl.status();
}

GetMDDRes *RasnetClientComm::executeGetNextMDD()
{
    LDEBUG << "executeGetNextMDD";
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    GetNextMDDReq req;
    req.set_client_id(this->clientId);
    GetNextMDDRepl repl;
    grpc::Status status = this->getRasServerService()->GetNextMDD(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    GetMDDRes *result = new GetMDDRes();
    result->status = repl.status();
    result->domain = strdup(repl.domain().c_str());
    result->typeName = strdup(repl.type_name().c_str());
    result->typeStructure = strdup(repl.type_structure().c_str());
    result->oid = strdup(repl.oid().c_str());
    result->currentFormat = repl.current_format();
    return result;
}

unsigned short RasnetClientComm::getMDDCore(r_Ref<r_GMarray> &mdd, GetMDDRes *thisResult, unsigned int isQuery)
{
    LDEBUG << "getMDDCore, isQuery = " << isQuery;
    this->updateTransaction();

    r_OId rOId(thisResult->oid);
    auto objectStatus = isQuery ? r_Object::transient : r_Object::read;
    auto *marray = new (database, objectStatus, rOId) r_GMarray(transaction);
    r_Minterval mddDomain(thisResult->domain);
    marray->set_spatial_domain(mddDomain);
    marray->set_type_by_name(thisResult->typeName);
    marray->set_type_structure(thisResult->typeStructure);
    r_Data_Format currentFormat = static_cast<r_Data_Format>(thisResult->currentFormat);
    marray->set_current_format(currentFormat);
    marray->get_base_type_schema(); // just to make sure the type is correct?
    
    // get tiles
    size_t marrayOffset = 0;
    bool firstTile = true;
    unsigned short tileStatus = 2; // 2: has more tiles
    while (tileStatus == 2 || tileStatus == 3)
    {
        GetTileRes *tileRes = executeGetNextTile();
        tileStatus = tileRes->status;
        if (tileStatus == 4)
        {
            freeGetTileRes(tileRes);
            LERROR << "no tile to transfer or empty transfer collection";
            throw r_Error(r_Error::r_Error_TransferFailed);
        }

        if (firstTile)
        {
            marray->set_type_length(tileRes->marray->cellTypeLength);
            marray->set_band_linearization(r_Band_Linearization(tileRes->marray->bandLinearization));
            marray->set_cell_linearization(r_Cell_Linearization(tileRes->marray->cellLinearization));
        }

        if (currentFormat == r_Array)
        {
            currentFormat = r_Data_Format(tileRes->marray->currentFormat);
            r_Minterval tileDomain(tileRes->marray->domain);
            auto memCopyLen = tileDomain.cell_count() * marray->get_type_length(); // cell type length of the tile must be the same
            if (memCopyLen < tileRes->marray->data.confarray_len)
                memCopyLen = tileRes->marray->data.confarray_len;    // may happen when compression expands
            char *memCopy = new char[memCopyLen];

            // create temporary tile
            std::unique_ptr<r_GMarray> tile(new r_GMarray(transaction));
            tile->set_spatial_domain(tileDomain);
            tile->set_array(memCopy);
            tile->set_array_size(memCopyLen);
            tile->set_type_length(tileRes->marray->cellTypeLength);

            unsigned long blockOffset = 0;
            if (tileStatus == 3)
            {
                // at least one block of the tile is left
                // Tile arrives in several blocks -> put them together
                concatArrayData(tileRes->marray->data.confarray_val, tileRes->marray->data.confarray_len, memCopy, memCopyLen, blockOffset);
                freeGetTileRes(tileRes);
                tileRes = executeGetNextTile();
                auto subStatus = tileRes->status;
                if (subStatus == 4)
                {
                    freeGetTileRes(tileRes);
                    throw r_Error(r_Error::r_Error_TransferFailed);
                }
                concatArrayData(tileRes->marray->data.confarray_val, tileRes->marray->data.confarray_len, memCopy, memCopyLen, blockOffset);
                freeGetTileRes(tileRes);
                tileStatus = subStatus;
            }
            else
            {
                // tileStatus = 0,3 last block of the current tile
                // Tile arrives as one block.
                concatArrayData(tileRes->marray->data.confarray_val, tileRes->marray->data.confarray_len, memCopy, memCopyLen, blockOffset);
                freeGetTileRes(tileRes);
            }

            // Now the tile is transferred completely, insert it into current MDD
            if (tileStatus < 2 && firstTile && (tile->spatial_domain() == marray->spatial_domain()))
            {
                // MDD consists of just one tile that is the same size of the mdd
                // simply take the data memory of the tile
                marray->set_array(tile->get_array());
                marray->set_array_size(tile->get_array_size());
                tile->set_array(nullptr);
            }
            else
            {
                // MDD consists of more than one tile or the tile does not cover the whole domain
                const auto cellTypeSize = marray->get_type_length();
                if (firstTile)
                {
                    // allocate memory for the MDD
                    r_Bytes size = mddDomain.cell_count() * cellTypeSize;
                    auto initData = new char[size];
                    memset(initData, 0, size);
                    marray->set_array(initData);
                    marray->set_array_size(size);
                }

                // copy tile data into MDD data space (optimized, relying on the internal representation of an MDD )
                char *marrayData = marray->get_array();
                char *tileData = tile->get_array();
                auto blockCells = tileDomain[tileDomain.dimension() - 1].get_extent();
                auto blockSize = blockCells * cellTypeSize;
                
                // these iterators iterate last dimension first, i.e. minimal step size
                r_Dimension dimRes = mddDomain.dimension();
                r_Dimension dimOp = tileDomain.dimension();
                r_MiterDirect resTileIter(static_cast<void *>(marrayData), mddDomain, tileDomain, marray->get_type_length());
                r_MiterDirect opTileIter(static_cast<void *>(tileData), tileDomain, tileDomain, marray->get_type_length());
            #ifdef RMANBENCHMARK
                opTimer.resume();
            #endif
                while (!resTileIter.isDone())
                {
                    // copy entire line (continuous chunk in last dimension) in one go
                    memcpy(resTileIter.getData(), opTileIter.getData(), blockSize);
                    // force overflow of last dimension
                    resTileIter.id[dimRes - 1].pos += r_Range(blockCells);
                    opTileIter.id[dimOp - 1].pos += r_Range(blockCells);
                    // iterate; the last dimension will always overflow now
                    ++resTileIter;
                    ++opTileIter;
                }
            }
        }
        else
        {
            // handle encoded data
            char *marrayData;
            if (firstTile)
            {
                // first tile, allocate memory for the MDD
                r_Bytes size = mddDomain.cell_count() * marray->get_type_length();
                marrayData = new char[size];
                memset(marrayData, 0, size);
                marray->set_array(marrayData);
                marray->set_array_size(size);
            }
            else
            {
                // next tiles, the data for the whole marray was already allocated
                marrayData = marray->get_array();
            }

            size_t blockSize = tileRes->marray->data.confarray_len;
            char *mddBlockPtr = marrayData + marrayOffset;
            char *tileBlockPtr = tileRes->marray->data.confarray_val;
            memcpy(mddBlockPtr, tileBlockPtr, blockSize);
            marrayOffset += blockSize;
            freeGetTileRes(tileRes);
        }
        
        firstTile = false;
    }  // end while( MDD is not transferred completely )

    mdd = r_Ref<r_GMarray>(marray->get_oid(), marray, transaction);
    return tileStatus;
}

GetTileRes *RasnetClientComm::executeGetNextTile()
{
    LDEBUG << "executeGetNextTile";
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    GetNextTileReq req;
    req.set_client_id(this->clientId);
    GetNextTileRepl repl;
    grpc::Status getNextTileStatus = this->getRasServerService()->GetNextTile(&context, req, &repl);
    if (!getNextTileStatus.ok())
    {
        handleError(getNextTileStatus.error_message());
    }
    
    GetTileRes *result = new GetTileRes();
    result->marray = new RPCMarray();
    result->status = repl.status();
    result->marray->domain = strdup(repl.domain().c_str());
    result->marray->cellTypeLength = static_cast<u_long>(repl.cell_type_length());
    result->marray->currentFormat = repl.current_format();
    result->marray->storageFormat = repl.storage_format();
    result->marray->bandLinearization = static_cast<u_short>(repl.band_linearization());
    result->marray->cellLinearization = static_cast<u_short>(repl.cell_linearization());
    u_int length = u_int(repl.data_length());
    result->marray->data.confarray_len = length;
    result->marray->data.confarray_val = (char *) mymalloc(length);
    memcpy(result->marray->data.confarray_val, repl.data().c_str(), length);
    
    return result;
}

void RasnetClientComm::getMarRpcRepresentation(
    const r_GMarray *mar, RPCMarray *&rpcMarray, r_Data_Format initStorageFormat, UNUSED const r_Base_Type *baseType)
{
    // allocate memory for the RPCMarray data structure and assign its fields
    rpcMarray                 = new RPCMarray;
    rpcMarray->domain         = mar->spatial_domain().get_string_representation();
    rpcMarray->cellTypeLength = mar->get_type_length();
    rpcMarray->currentFormat = initStorageFormat;
    rpcMarray->data.confarray_len = mar->get_array_size();
    rpcMarray->data.confarray_val = const_cast<char *>(mar->get_array());
    rpcMarray->storageFormat = initStorageFormat;
    rpcMarray->bandLinearization = static_cast<u_short>(mar->get_band_linearization());
    rpcMarray->cellLinearization = static_cast<u_short>(mar->get_cell_linearization());
}


void RasnetClientComm::freeMarRpcRepresentation(const r_GMarray *mar, RPCMarray *rpcMarray)
{
    if (rpcMarray->data.confarray_val != mar->get_array())
    {
        delete[] rpcMarray->data.confarray_val;
        rpcMarray->data.confarray_val = nullptr;
    }
    free(rpcMarray->domain);
    rpcMarray->domain = nullptr;
    delete rpcMarray;
}

int RasnetClientComm::concatArrayData(const char *source, unsigned long srcSize, char *&dest, unsigned long &destSize, unsigned long &blockOffset)
{
    if (blockOffset + srcSize > destSize)
    {
        // need to extend dest
        unsigned long newSize = blockOffset + srcSize;
        // allocate a little extra if we have to extend
        newSize = newSize + newSize / 16;
        char *newArray = new char[newSize];
        memcpy(newArray, dest, blockOffset);
        delete [] dest;
        dest = newArray;
        destSize = newSize;
    }
    memcpy(dest + blockOffset, source, srcSize);
    blockOffset += srcSize;
    return 0;
}

void RasnetClientComm::freeGetTileRes(GetTileRes *ptr)
{
    if (ptr->marray->domain)
    {
        free(ptr->marray->domain);
        ptr->marray->domain = nullptr;
    }
    if (ptr->marray->data.confarray_val)
    {
        free(ptr->marray->data.confarray_val);
        ptr->marray->data.confarray_val = nullptr;
    }
    delete ptr->marray;
    delete ptr;
}

r_Ref_Any RasnetClientComm::executeGetCollByNameOrOId(const char *collName, const r_OId &oid)
{
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    GetCollectionByNameOrOidReq req;
    req.set_client_id(this->clientId);
    if (collName != NULL)
    {
        LDEBUG << "executeGetCollByNameOrOId " << collName;
        req.set_collection_identifier(collName);
        req.set_is_name(true);
    }
    else
    {
        LDEBUG << "executeGetCollByNameOrOId " << oid.get_string_representation();
        req.set_collection_identifier(oid.get_string_representation());
        req.set_is_name(false);
    }
    GetCollectionByNameOrOidRepl repl;
    grpc::Status st = this->getRasServerService()->GetCollectionByNameOrOid(&context, req, &repl);
    if (!st.ok())
    {
        handleError(st.error_message());
    }

    int status = repl.status();
    handleStatusCode(status, "getCollByName");

    this->updateTransaction();

    r_OId rOId(repl.oid().c_str());
    r_Set<r_Ref_Any> *set = new (database, r_Object::read, rOId) r_Set<r_Ref_Any>;
    set->set_type_by_name(repl.type_name().c_str());
    set->set_type_structure(repl.type_structure().c_str());
    set->set_object_name(repl.collection_name().c_str());
    if (status == 0)
        getMDDCollection(*set, 0);
    //  else rpcStatus == 1 -> Result collection is empty and nothing has to be got.
    
    r_Ref_Any result = r_Ref_Any(set->get_oid(), set, transaction);
    return result;
}

r_Ref_Any RasnetClientComm::executeGetCollOIdsByNameOrOId(const char *collName, const r_OId &oid)
{
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    GetCollOidsByNameOrOidReq req;
    req.set_client_id(this->clientId);
    if (collName != NULL)
    {
        LDEBUG << "executeGetCollOIdsByNameOrOId " << collName;
        req.set_collection_identifier(collName);
        req.set_is_name(true);
    }
    else
    {
        LDEBUG << "executeGetCollOIdsByNameOrOId " << oid.get_string_representation();
        req.set_collection_identifier(oid.get_string_representation());
        req.set_is_name(false);
    }
    GetCollOidsByNameOrOidRepl repl;
    grpc::Status st = this->getRasServerService()->GetCollOidsByNameOrOid(&context, req, &repl);
    if (!st.ok())
    {
        handleError(st.error_message());
    }

    int status = repl.status();
    if (status != 0 && status != 1)
    {
        handleStatusCode(status, "executeGetCollOIdsByNameOrOId");
    }

    this->updateTransaction();
    
    r_OId rOId(repl.oids_string().c_str());
    r_Set<r_Ref<r_GMarray>> *set = new (database, r_Object::read, rOId)  r_Set<r_Ref<r_GMarray>>;
    set->set_type_by_name(repl.type_name().c_str());
    set->set_type_structure(repl.type_structure().c_str());
    set->set_object_name(repl.collection_name().c_str());
    for (int i = 0; i < repl.oid_set_size(); ++i)
    {
        r_OId roid(repl.oid_set(i).c_str());
        set->insert_element(r_Ref<r_GMarray>(roid, transaction), 1);
    }
    r_Ref_Any result = r_Ref_Any(set->get_oid(), set, transaction);
    return result;
}

void RasnetClientComm::sendMDDConstants(const r_OQL_Query &query)
{
    LDEBUG << "sendMDDConstants";
    if (query.get_constants())
    {
        // executeInitUpdate prepares server structures for MDD transfer
        if (executeInitUpdate() != 0)
        {
            throw r_Error(r_Error::r_Error_TransferFailed);
        }
        
        r_Set<r_GMarray *> *mddConstants = const_cast<r_Set<r_GMarray *>*>(query.get_constants());
        r_Iterator<r_GMarray *> iter = mddConstants->create_iterator();
        for (iter.reset(); iter.not_done(); iter++)
        {
            r_GMarray *mdd = *iter;
            if (mdd)
            {
                auto status = executeStartInsertTransMDD(mdd);
                switch (status)
                {
                case 0:  break; // OK
                case 2:  throw r_Error(r_Error::r_Error_DatabaseClassUndefined); break;
                case 3:  throw r_Error(r_Error::r_Error_TypeInvalid);  break;
                default: throw r_Error(r_Error::r_Error_TransferFailed); break;
                }

                r_Set<r_GMarray *> *bagOfTiles = NULL;
                r_Set<r_GMarray *> decompTiles;
                if (mdd->get_array())
                {
                    decompTiles = mdd->get_storage_layout()->decomposeMDD(mdd);
                    bagOfTiles = &decompTiles;
                }
                else
                {
                    bagOfTiles = mdd->get_tiled_array();
                }
                
                const r_Base_Type *baseType = mdd->get_base_type_schema();
                r_Iterator<r_GMarray *> tileIter = bagOfTiles->create_iterator();
                for (tileIter.reset(); tileIter.not_done(); tileIter.advance())
                {
                    RPCMarray *rpcMarray;
                    r_GMarray *origTile = *tileIter;
                    getMarRpcRepresentation(origTile, rpcMarray, mdd->get_storage_layout()->get_storage_format(), baseType);
                    status = executeInsertTile(false, rpcMarray);
                    // free rpcMarray structure (rpcMarray->data.confarray_val is freed somewhere else)
                    freeMarRpcRepresentation(origTile, rpcMarray);
                    // delete current tile (including data block)
                    delete origTile;
                    origTile = NULL;
                    if (status > 0)
                    {
                        throw r_Error(r_Error::r_Error_TransferFailed);
                    }
                }
                
                bagOfTiles->remove_all();
                bagOfTiles = NULL;
                executeEndInsertMDD(false);
            }
        }
    }
}

int RasnetClientComm::executeStartInsertTransMDD(r_GMarray *mdd)
{
    LDEBUG << "executeStartInsertTransMDD";
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    StartInsertTransMDDReq req;
    StartInsertTransMDDRepl repl;
    req.set_client_id(this->clientId);
    req.set_domain(mdd->spatial_domain().to_string());
    req.set_type_length(static_cast<int32_t>(mdd->get_type_length()));
    req.set_type_name(mdd->get_type_name());
    grpc::Status status = this->getRasServerService()->StartInsertTransMDD(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    return repl.status();
}

int RasnetClientComm::executeInitUpdate()
{
    LDEBUG << "executeInitUpdate";
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    InitUpdateReq req;
    req.set_client_id(this->clientId);
    InitUpdateRepl repl;
    grpc::Status status = this->getRasServerService()->InitUpdate(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    return repl.status();
}

int RasnetClientComm::executeExecuteQuery(const char *query, r_Set<r_Ref_Any> &result)
{
    LDEBUG << "executeExecuteQuery";
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    ExecuteQueryReq req;
    ExecuteQueryRepl repl;
    req.set_client_id(this->clientId);
    req.set_query(query);
    grpc::Status st = this->getRasServerService()->ExecuteQuery(&context, req, &repl);
    if (!st.ok())
    {
        handleError(st.error_message());
    }

    int status = repl.status();
    switch (status)
    {
      case 4:
      case 5:
        throw r_Equery_execution_failed(unsigned(repl.err_no()), unsigned(repl.line_no()),
                                        unsigned(repl.col_no()), repl.token().c_str());
      case 0:
      case 1:
        result.set_type_by_name(repl.type_name().c_str());
        result.set_type_structure(repl.type_structure().c_str());
      default: break;
    }
    return status;
}

GetElementRes *RasnetClientComm::executeGetNextElement()
{
    LDEBUG << "executeGetNextElement";
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    GetNextElementReq req;
    req.set_client_id(this->clientId);
    GetNextElementRepl repl;
    grpc::Status status = this->getRasServerService()->GetNextElement(&context, req, &repl);
    LDEBUG << "getting next element from server: got response from server";
    if (!status.ok())
    {
        handleError(status.error_message());
    }

    GetElementRes *result = new GetElementRes();
    result->data.confarray_len = u_int(repl.data_length());
    result->data.confarray_val = new char[repl.data_length()];
    memcpy(result->data.confarray_val, repl.data().c_str(), size_t(repl.data_length()));
    result->status = repl.status();
    return result;
}

void RasnetClientComm::getElementCollection(r_Set<r_Ref_Any> &resultColl)
{
    LDEBUG << "getElementCollection of type " << resultColl.get_type_structure();
    
    this->updateTransaction();
    
    unsigned short status = 0;
    while (status == 0)   // repeat until all elements are transferred
    {
        GetElementRes *thisResult = executeGetNextElement();
        status = thisResult->status;
        if (status == 2)
        {
            throw r_Error(r_Error::r_Error_TransferFailed);
        }
        
        // create new collection element, use type of collection resultColl
        r_Ref_Any element;
        const r_Type *elementType = resultColl.get_element_type_schema();
        auto dataLen = thisResult->data.confarray_len;
        auto *data = thisResult->data.confarray_val;
        switch (elementType->type_id())
        {
        case r_Type::BOOL:
        case r_Type::CHAR:
        case r_Type::OCTET:
        case r_Type::SHORT:
        case r_Type::USHORT:
        case r_Type::LONG:
        case r_Type::ULONG:
        case r_Type::FLOAT:
        case r_Type::DOUBLE:
        {
            element = new r_Primitive(data, static_cast<const r_Primitive_Type *>(elementType));
            transaction->add_object_list(GenRefType::SCALAR, (void *) element);
            break;
        }
        case r_Type::COMPLEXTYPE1:
        case r_Type::COMPLEXTYPE2:
        case r_Type::CINT16:
        case r_Type::CINT32:
        {
            element = new r_Complex(data, static_cast<const r_Complex_Type *>(elementType));
            transaction->add_object_list(GenRefType::SCALAR, (void *)element);
            break;
        }
        case r_Type::STRUCTURETYPE:
        {
            element = new r_Structure(data, static_cast<const r_Structure_Type *>(elementType));
            transaction->add_object_list(GenRefType::SCALAR, (void *) element);
            break;
        }
        case r_Type::POINTTYPE:
        {
            char *stringRep = new char[dataLen + 1];
            strncpy(stringRep, data, dataLen);
            stringRep[dataLen] = '\0';
            element = new r_Point(stringRep);
            transaction->add_object_list(GenRefType::POINT, (void *) element);
            delete [] stringRep;
            break;
        }
        case r_Type::SINTERVALTYPE:
        {
            char *stringRep = new char[dataLen + 1];
            strncpy(stringRep, data, dataLen);
            stringRep[dataLen] = '\0';
            element = new r_Sinterval(stringRep);
            transaction->add_object_list(GenRefType::SINTERVAL, (void *) element);
            delete [] stringRep;
            break;
        }
        case r_Type::MINTERVALTYPE:
        {
            char *stringRep = new char[dataLen + 1];
            strncpy(stringRep, data, dataLen);
            stringRep[dataLen] = '\0';
            element = new r_Minterval(stringRep);
            transaction->add_object_list(GenRefType::MINTERVAL, (void *) element);
            delete [] stringRep;
            break;
        }
        case r_Type::OIDTYPE:
        {
            char *stringRep = new char[dataLen + 1];
            strncpy(stringRep, data, dataLen);
            stringRep[dataLen] = '\0';
            element = new r_OId(stringRep);
            transaction->add_object_list(GenRefType::OID, (void *) element);
            delete [] stringRep;
            break;
        }
        default: break;
        }

        // insert element into result set
        resultColl.insert_element(element, 1);
        delete[] thisResult->data.confarray_val;
        thisResult->data.confarray_val = nullptr;
        delete thisResult;
    }

    executeEndTransfer();
}

int RasnetClientComm::executeExecuteUpdateQuery(const char *query)
{
    LDEBUG << "executeExecuteUpdateQuery";
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    ExecuteUpdateQueryReq req;
    req.set_client_id(this->clientId);
    req.set_query(query);
    ExecuteUpdateQueryRepl repl;
    grpc::Status st = this->getRasServerService()->ExecuteUpdateQuery(&context, req, &repl);
    if (!st.ok())
    {
        handleError(st.error_message());
    }

    int status = repl.status();
    switch (status)
    {
      case 0:
        return status;
      case 1:
        throw r_Error(r_Error::r_Error_ClientUnknown);
      case 2:
      case 3:
        throw r_Equery_execution_failed(unsigned(repl.errono()), unsigned(repl.lineno()),
                                        unsigned(repl.colno()), repl.token().c_str());
      default:
        throw r_Error(r_Error::r_Error_TransferFailed);
    }
}

int  RasnetClientComm::executeExecuteUpdateQuery(const char *query, r_Set<r_Ref_Any> &result)
{
    LDEBUG << "executeExecuteUpdateQuery";
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    ExecuteInsertQueryReq req;
    req.set_client_id(this->clientId);
    req.set_query(query);
    ExecuteInsertQueryRepl repl;
    grpc::Status st = this->getRasServerService()->ExecuteInsertQuery(&context, req, &repl);
    if (!st.ok())
    {
        handleError(st.error_message());
    }

    int status = repl.status();
    switch (status)
    {
      case 4:
      case 5:
        throw r_Equery_execution_failed(unsigned(repl.errono()), unsigned(repl.lineno()),
                                        unsigned(repl.colno()), repl.token().c_str());
      case 0:
      case 1:
      case 2:
        result.set_type_by_name(repl.type_name().c_str());
        result.set_type_structure(repl.type_structure().c_str());
      default: break;
    }
    return status;
}

int RasnetClientComm::executeSetFormat(bool lTransferFormat, r_Data_Format format, const char *formatParams)
{
    LDEBUG << "executeSetFormat";
    grpc::ClientContext context;
    GrpcUtils::setDeadline(context, timeoutMs);
    SetFormatReq req;
    req.set_client_id(this->clientId);
    req.set_transfer_format((lTransferFormat ? 1 : 0));
    req.set_format(format);
    req.set_format_params(formatParams);
    SetFormatRepl repl;
    grpc::Status status = this->getRasServerService()->SetFormat(&context, req, &repl);
    if (!status.ok())
    {
        handleError(status.error_message());
    }
    return repl.status();
}

void RasnetClientComm::checkForRwTransaction()
{
    this->updateTransaction();
    r_Transaction *trans = transaction;
    if (trans == 0 || trans->get_mode() == r_Transaction::read_only)
    {
        LDEBUG << "failed TA read-write check: transaction is not set or is read-only";
        throw r_Error(r_Error::r_Error_TransactionReadOnly);
    }
}

void RasnetClientComm::handleError(const std::string &error)
{
    ErrorMessage message;
    if (error.empty())
    {
        LDEBUG << "Internal server error.";
        throw r_EGeneral("Internal server error.");
    }
    else if (message.ParseFromString(error))
    {
        auto msg = message.DebugString();
        if (message.type() == ErrorMessage::RERROR)
        {
            LDEBUG << "Throwing error received from the server: " << msg;
            const auto &what = message.error_text();
            if (!what.empty() && message.error_no() == 0 &&
                (message.kind() == 1 || message.kind() == 0))
            {
                throw r_Error(what.c_str());
            }
            else
            {
                r_Error err(static_cast<r_Error::kind>(message.kind()), message.error_no());
                if (!what.empty() && what != err.what())
                {
                    err.set_what(what.c_str());
                }
                throw err;
            }
        }
        else if (message.type() == ErrorMessage::STL)
        {
            LDEBUG << "Throwing error received from the server: " << msg;
            throw r_EGeneral(message.error_text());
        }
        else if (message.type() == ErrorMessage::UNKNOWN)
        {
            LDEBUG << "Throwing error received from the server: " << msg;
            throw r_EGeneral(message.error_text());
        }
        else
        {
            LDEBUG << "Throwing error received from the server: " << msg;
            throw r_EGeneral("General error received from the server.");
        }
    }
    else
    {
        auto msg = "The client failed to contact the server: " + error;
        LDEBUG << msg;
        throw r_EGeneral(msg.c_str());
    }
}

void RasnetClientComm::handleStatusCode(int status, const std::string &method)
{
    switch (status)
    {
    case 0:
        break;
    case 1:
        LDEBUG << "RasnetClientComm::" << method << ": error: status = " << status;
        throw r_Error(r_Error::r_Error_ClientUnknown);
        break;
    case 2:
        LDEBUG << "RasnetClientComm::" << method << ": error: status = " << status;
        throw r_Error(r_Error::r_Error_ObjectUnknown);
        break;
    case 3:
        LDEBUG << "RasnetClientComm::" << method << ": error: status = " << status;
        throw r_Error(r_Error::r_Error_ClientUnknown);
        break;
    default:
        LDEBUG << "RasnetClientComm::" << method << ": error: status = " << status;
        throw r_Error(r_Error::r_Error_General);
        break;
    }
}

void RasnetClientComm::setUserIdentification(const char *userName, const char *plainTextPassword)
{
    connectClient(userName, common::Crypto::messageDigest(plainTextPassword, DEFAULT_DIGEST));
}

void RasnetClientComm::setMaxRetry(UNUSED unsigned int newMaxRetry)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

unsigned int RasnetClientComm::getMaxRetry()
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

void RasnetClientComm::setTimeoutInterval(int seconds)
{
    if (seconds < 0)
    {
        throw common::InvalidArgumentException{"The client connection timeout must be a value >= 0."};
    }
    timeoutMs = size_t(seconds) * 1000;
}

int RasnetClientComm::getTimeoutInterval()
{
    return int(timeoutMs / 1000);
}

std::shared_ptr<ClientRassrvrService::Stub> RasnetClientComm::getRasServerService(bool throwIfConnectionFailed)
{
    this->initRasserverService();
    // Check if the rasserver is serving
    if (!GrpcUtils::isServerAlive(this->rasserverHealthService, SERVICE_CALL_TIMEOUT) && throwIfConnectionFailed)
    {
        LDEBUG << "The client failed to connect to rasserver at " << rasServerHost << ":" << rasServerPort;
        throw r_EGeneral("The client failed to connect to rasserver at " + rasServerHost + ":" + std::to_string(rasServerPort));
    }
    return this->rasserverService;
}

void RasnetClientComm::initRasserverService()
{
    boost::unique_lock<boost::shared_mutex> lock(this->rasServerServiceMtx);
    if (!this->initializedRasServerService)
    {
        LDEBUG << "initializing rasserver service to " << rasServerHost << ":" << rasServerPort;
        try
        {
            auto address = GrpcUtils::constructAddressString(rasServerHost, boost::uint32_t(rasServerPort));
            auto channel = grpc::CreateCustomChannel(address, grpc::InsecureChannelCredentials(),
                                                     GrpcUtils::getDefaultChannelArguments());

            this->rasserverService.reset(new ClientRassrvrService::Stub(channel));
            this->rasserverHealthService.reset(new common::HealthService::Stub(channel));
            this->initializedRasServerService = true;
        }
        catch (std::exception &ex)
        {
            LERROR << "Failed initializing rasserver service to host "
                   << rasServerHost << ":" << rasServerPort << ": " << ex.what();
            handleError(ex.what());
        }
    }
}

void RasnetClientComm::closeRasserverService()
{
    boost::unique_lock<boost::shared_mutex> lock(this->rasServerServiceMtx);
    if (this->initializedRasServerService)
    {
        this->initializedRasServerService = false;
        this->rasserverService.reset();
        this->rasserverHealthService.reset();
    }
}

std::shared_ptr<rasnet::service::RasmgrClientService::Stub> RasnetClientComm::getRasMgrService(bool throwIfConnectionFailed)
{
    this->initRasmgrService();
    // Check if the rasmgr is serving
    if (!GrpcUtils::isServerAlive(this->rasmgrHealthService, SERVICE_CALL_TIMEOUT) && throwIfConnectionFailed)
    {
        LDEBUG << "The client failed to connect to rasmgr at " << rasmgrHost;
        throw r_EGeneral("The client failed to connect to rasmgr at " + rasmgrHost);
    }
    return this->rasmgrService;
}

void RasnetClientComm::initRasmgrService()
{
    boost::unique_lock<boost::shared_mutex> lock(this->rasMgrServiceMtx);
    if (!this->initializedRasMgrService)
    {
        try
        {
            auto channel = grpc::CreateCustomChannel(rasmgrHost, grpc::InsecureChannelCredentials(),
                                                     GrpcUtils::getDefaultChannelArguments());
            this->rasmgrService.reset(new RasmgrClientService::Stub(channel));
            this->rasmgrHealthService.reset(new common::HealthService::Stub(channel));
            this->initializedRasMgrService = true;
        }
        catch (std::exception &ex)
        {
            LERROR << "Failed initializing rasmgr service to host " << rasmgrHost << ": " << ex.what();
            handleError(ex.what());
        }
    }
}

void RasnetClientComm::closeRasmgrService()
{
    boost::unique_lock<boost::shared_mutex> lock(this->rasMgrServiceMtx);
    if (this->initializedRasMgrService)
    {
        this->initializedRasMgrService = false;
        this->rasmgrService.reset();
        this->rasmgrHealthService.reset();
    }
}

/* START: KEEP ALIVE */

/* RASMGR */
void RasnetClientComm::startRasMgrKeepAlive()
{
    boost::lock_guard<boost::mutex> lock(this->rasmgrKeepAliveMutex);

    //TODO-GM
    this->isRasmgrKeepAliveRunning = true;
    this->rasMgrKeepAliveManagementThread.reset(new boost::thread(&RasnetClientComm::clientRasMgrKeepAliveRunner, this));
}

void RasnetClientComm::stopRasMgrKeepAlive()
{
    try
    {
        {
            boost::unique_lock<boost::mutex> lock(this->rasmgrKeepAliveMutex);
            this->isRasmgrKeepAliveRunning = false;
        }

        if (!rasMgrKeepAliveManagementThread)
        {
            LDEBUG << "Thread that sends messages from client to rasmgr is not running.";
        }
        else
        {

            this->isRasmgrKeepAliveRunningCondition.notify_one();

            if (this->rasMgrKeepAliveManagementThread->joinable())
            {
                LDEBUG << "Joining rasmgr keep alive management thread.";
                this->rasMgrKeepAliveManagementThread->join();
                LDEBUG << "Joined rasmgr keep alive management thread.";
            }
            else
            {
                LDEBUG << "Interrupting rasmgr keep alive management thread.";
                this->rasMgrKeepAliveManagementThread->interrupt();
                LDEBUG << "Interrupted rasmgr keep alive management thread.";
            }

        }
    }
    catch (std::exception &ex)
    {
        LERROR << ex.what();
    }
    catch (...)
    {
        LERROR << "Stoping rasmgr keep alive has failed";
    }
}

void RasnetClientComm::clientRasMgrKeepAliveRunner()
{
    boost::posix_time::time_duration timeToSleepFor = boost::posix_time::milliseconds(this->keepAliveTimeout);

    boost::unique_lock<boost::mutex> threadLock(this->rasmgrKeepAliveMutex);
    while (this->isRasmgrKeepAliveRunning)
    {
        try
        {
            // Wait on the condition variable to be notified from the
            // destructor when it is time to stop the worker thread
            if (!this->isRasmgrKeepAliveRunningCondition.timed_wait(threadLock, timeToSleepFor))
            {
                LDEBUG << "clientRasMgrKeepAliveRunner - sending KeepAlive request to rasmgr";
                grpc::ClientContext context;
                GrpcUtils::setDeadline(context, SERVICE_CALL_TIMEOUT);
                KeepAliveReq req;
                req.set_clientid(this->clientId);
                Void repl;
                grpc::Status status = this->getRasMgrService(false)->KeepAlive(&context, req, &repl);
                if (!status.ok())
                {
                    LERROR << "Client " << this->clientId << " failed to send keep alive message to rasmgr: "
                           << status.error_message();
                    LDEBUG << "Stopping client-rasmgr keep alive thread.";
                    this->isRasmgrKeepAliveRunning = false;
                }
            }
        }
        catch (std::exception &ex)
        {
            this->isRasmgrKeepAliveRunning = false;
            LERROR << "rasmgr keep alive thread failed: " << ex.what();
        }
        catch (...)
        {
            this->isRasmgrKeepAliveRunning = false;
            LERROR << "rasmgr keep alive thread failed.";
        }
    }
}

/* RASSERVER */
void RasnetClientComm::startRasServerKeepAlive()
{
    boost::lock_guard<boost::mutex> lock(this->rasserverKeepAliveMutex);

    this->isRasserverKeepAliveRunning = true;
    this->rasServerKeepAliveManagementThread.reset(
        new boost::thread(&RasnetClientComm::clientRasServerKeepAliveRunner, this));
}

void RasnetClientComm::stopRasServerKeepAlive()
{
    try
    {
        {
            boost::unique_lock<boost::mutex> lock(this->rasserverKeepAliveMutex);
            // Change the condition and notify the variable
            this->isRasserverKeepAliveRunning = false;
        }

        if (!rasServerKeepAliveManagementThread)
        {
            LDEBUG << "Thread that sends messages from client to rasserver is not running.";
        }
        else
        {
            this->isRasserverKeepAliveRunningCondition.notify_one();

            if (this->rasServerKeepAliveManagementThread->joinable())
            {
                LDEBUG << "Joining rasserver keep alive management thread.";
                this->rasServerKeepAliveManagementThread->join();
                LDEBUG << "Joined rasserver keep alive management thread.";
            }
            else
            {
                LDEBUG << "Interrupting rasserver keep alive management thread.";
                this->rasServerKeepAliveManagementThread->interrupt();
                LDEBUG << "Interrupted rasserver keep alive management thread.";
            }

        }
    }
    catch (std::exception &ex)
    {
        LERROR << "Stoping rasmgr keep alive failed: " << ex.what();
    }
    catch (...)
    {
        LERROR << "Stoping rasmgr keep alive failed.";
    }
}

void RasnetClientComm::clientRasServerKeepAliveRunner()
{
    boost::posix_time::time_duration timeToSleepFor = boost::posix_time::milliseconds(this->keepAliveTimeout);

    boost::unique_lock<boost::mutex> threadLock(this->rasserverKeepAliveMutex);
    while (this->isRasserverKeepAliveRunning)
    {
        try
        {
            // Wait on the condition variable to be notified from the
            // destructor when it is time to stop the worker thread
            if (!this->isRasserverKeepAliveRunningCondition.timed_wait(threadLock, timeToSleepFor))
            {
                grpc::ClientContext context;
                GrpcUtils::setDeadline(context, SERVICE_CALL_TIMEOUT);
                KeepAliveRequest req;
                req.set_client_uuid(this->remoteClientId);
                req.set_session_id(this->sessionId);
                Void repl;
                grpc::Status status = this->getRasServerService(false)->KeepAlive(&context, req, &repl);
                if (!status.ok())
                {
                    LERROR << "Failed to send keep alive message to rasserver: " << status.error_message();
                    LDEBUG << "Stopping client-rasserver keep alive thread.";
                    this->isRasserverKeepAliveRunning = false;
                }
            }
        }
        catch (std::exception &ex)
        {
            this->isRasserverKeepAliveRunning = false;
            LERROR << "rasserver keep alive thread failed: " << ex.what();
        }
        catch (...)
        {
            this->isRasserverKeepAliveRunning = false;
            LERROR << "rasserver keep alive thread failed.";
        }
    }

}
/* END: KEEP ALIVE */
