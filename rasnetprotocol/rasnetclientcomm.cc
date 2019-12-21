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

#include <cstring>
#include <fstream>
#include <chrono>

#include <grpc++/grpc++.h>
#include <grpc++/security/credentials.h>

#include "../rasodmg/transaction.hh"
#include "../rasodmg/database.hh"
#include "../rasodmg/iterator.hh"
#include "../rasodmg/set.hh"
#include "../rasodmg/ref.hh"
#include "../rasodmg/storagelayout.hh"
#include "../rasodmg/tiling.hh"
#include "../rasodmg/gmarray.hh"

#include "../raslib/minterval.hh"
#include "../raslib/rminit.hh"
#include "../raslib/primitivetype.hh"
#include "../raslib/complextype.hh"
#include "../raslib/structuretype.hh"
#include "../raslib/primitive.hh"
#include "../raslib/complex.hh"
#include "../raslib/structure.hh"
#include "../raslib/parseparams.hh"
#include "../mymalloc/mymalloc.h"

#include "common/crypto/crypto.hh"
#include "common/uuid/uuid.hh"
#include "common/grpc/grpcutils.hh"
#include <logging.hh>

#include "common/grpc/messages/error.pb.h"

#include "globals.hh"
#include "rasnetclientcomm.hh"


using boost::scoped_ptr;
using boost::shared_ptr;
using boost::shared_mutex;
using boost::unique_lock;
using boost::thread;

using common::UUID;
using common::GrpcUtils;

using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;

using std::chrono::system_clock;
using std::chrono::milliseconds;

using rasnet::service::OpenServerDatabaseReq;
using rasnet::service::OpenServerDatabaseRepl;
using rasnet::service::CloseServerDatabaseReq;
using rasnet::service::AbortTransactionRepl;
using rasnet::service::AbortTransactionReq;
using rasnet::service::BeginTransactionRepl;
using rasnet::service::BeginTransactionReq;
using rasnet::service::CloseDbReq;
using rasnet::service::CloseDbReq;
using rasnet::service::ConnectReq;
using rasnet::service::ConnectRepl;
using rasnet::service::OpenDbReq;
using rasnet::service::OpenDbRepl;
using rasnet::service::DisconnectReq;
using rasnet::service::Void;
using rasnet::service::CommitTransactionRepl;
using rasnet::service::CommitTransactionReq;
using rasnet::service::DeleteCollectionByNameRepl;
using rasnet::service::DeleteCollectionByNameReq;
using rasnet::service::DeleteCollectionByOidRepl;
using rasnet::service::DeleteCollectionByOidReq;
using rasnet::service::EndInsertMDDRepl;
using rasnet::service::EndInsertMDDReq;
using rasnet::service::EndTransferRepl;
using rasnet::service::EndTransferReq;
using rasnet::service::ExecuteQueryRepl;
using rasnet::service::ExecuteQueryReq;
using rasnet::service::ExecuteUpdateQueryRepl;
using rasnet::service::ExecuteUpdateQueryReq;
using rasnet::service::ExecuteInsertQueryReq;
using rasnet::service::ExecuteInsertQueryRepl;
using rasnet::service::GetCollOidsByNameOrOidRepl;
using rasnet::service::GetCollOidsByNameOrOidReq;
using rasnet::service::GetCollectionByNameOrOidRepl;
using rasnet::service::GetCollectionByNameOrOidReq;
using rasnet::service::GetNewOidRepl;
using rasnet::service::GetNewOidReq;
using rasnet::service::GetNextElementRepl;
using rasnet::service::GetNextElementReq;
using rasnet::service::GetNextMDDRepl;
using rasnet::service::GetNextMDDReq;
using rasnet::service::GetNextTileRepl;
using rasnet::service::GetNextTileReq;
using rasnet::service::GetObjectTypeRepl;
using rasnet::service::GetObjectTypeReq;
using rasnet::service::GetTypeStructureRepl;
using rasnet::service::GetTypeStructureReq;
using rasnet::service::InitUpdateRepl;
using rasnet::service::InitUpdateReq;
using rasnet::service::InsertCollectionRepl;
using rasnet::service::InsertCollectionReq;
using rasnet::service::InsertTileRepl;
using rasnet::service::InsertTileReq;
using rasnet::service::RemoveObjectFromCollectionRepl;
using rasnet::service::RemoveObjectFromCollectionReq;
using rasnet::service::SetFormatRepl;
using rasnet::service::SetFormatReq;
using rasnet::service::StartInsertMDDRepl;
using rasnet::service::StartInsertMDDReq;
using rasnet::service::StartInsertTransMDDRepl;
using rasnet::service::StartInsertTransMDDReq;
using rasnet::service::KeepAliveReq;
using rasnet::service::KeepAliveRequest;
using common::ErrorMessage;

using std::string;

RasnetClientComm::RasnetClientComm(string rasmgrHost, int rasmgrPort):
    transferFormatParams(NULL),
    storageFormatParams(NULL)
{
    this->clientId = 0;

    clientParams = new r_Parse_Params();

    this->rasmgrHost = GrpcUtils::constructAddressString(rasmgrHost, static_cast<boost::uint32_t>(rasmgrPort));

    this->initializedRasMgrService = false;
    this->initializedRasServerService = false;
}

RasnetClientComm::~RasnetClientComm() noexcept
{
    this->stopRasMgrKeepAlive();
    this->stopRasServerKeepAlive();

    closeRasmgrService();
    closeRasserverService();

    if (clientParams != NULL)
    {
        delete clientParams;
        clientParams = NULL;
    }
}

int RasnetClientComm::connectClient(string userName, string passwordHash)
{
    int retval = 0; // ok
    ConnectReq connectReq;
    ConnectRepl connectRepl;

    connectReq.set_username(userName);
    connectReq.set_passwordhash(passwordHash);

    LDEBUG << "Connecting with rasmgr client with ID: " << clientId << ", UUID: " << clientUUID;
    ClientContext context;
    grpc::Status status = this->getRasMgrService()->Connect(&context, connectReq, &connectRepl);

    if (!status.ok())
    {
        handleError(status.error_message());
        retval = 1;
    }

    //Kept for backwards compatibility
    this->clientId = static_cast<long unsigned int>(common::UUID::generateIntId());
    this->clientUUID = connectRepl.clientuuid();

    // Send keep alive messages to rasmgr until openDB is called
    this->keepAliveTimeout = connectRepl.keepalivetimeout();
    this->startRasMgrKeepAlive();

    return retval;
}

int RasnetClientComm::disconnectClient()
{
    int retval = 0; // ok
    DisconnectReq disconnectReq;
    Void disconnectRepl;

    disconnectReq.set_clientuuid(this->clientUUID);

    LDEBUG << "Disconnecting from rasmgr client with ID: " << clientId << ", UUID: " << clientUUID;
    ClientContext context;
    grpc::Status status = this->getRasMgrService()->Disconnect(&context, disconnectReq, &disconnectRepl);

    this->stopRasMgrKeepAlive();
    this->closeRasmgrService();

    if (!status.ok())
    {
        handleError(status.error_message());
        retval = 1;
    }

    return retval;
}

int RasnetClientComm::openDB(const char *database)
{
    int retval = 0; // ok

    OpenDbReq openDatabaseReq;
    OpenDbRepl openDatabaseRepl;

    openDatabaseReq.set_clientid(this->clientId);
    openDatabaseReq.set_clientuuid(this->clientUUID.c_str());
    openDatabaseReq.set_databasename(database);

    LDEBUG << "Opening rasmgr database for client with ID: " << clientId << ", UUID: " << clientUUID;
    ClientContext openDbContext;
    grpc::Status openDbStatus = this->getRasMgrService()->OpenDb(&openDbContext, openDatabaseReq, &openDatabaseRepl);

    this->remoteClientUUID = openDatabaseRepl.clientsessionid();
    // If the allocated server belongs to the current rasmgr,
    // We can stop sending keep alive messages to the current rasmgr.

    if (this->remoteClientUUID == this->clientUUID)
    {
        LDEBUG << "Stopping rasmgr keep alive.";
        //stop sending keep alive messages to rasmgr
        this->stopRasMgrKeepAlive();
    }

    if (!openDbStatus.ok())
    {
        string errorText = openDbStatus.error_message();
        handleError(errorText);
        retval = 1;
    }

    this->rasServerHost = openDatabaseRepl.serverhostname();
    this->rasServerPort = static_cast<int>(openDatabaseRepl.port());
    this->sessionId = openDatabaseRepl.dbsessionid();

    OpenServerDatabaseReq openServerDatabaseReq;
    OpenServerDatabaseRepl openServerDatabaseRepl;

    openServerDatabaseReq.set_client_id(this->clientId);
    openServerDatabaseReq.set_database_name(database);

    LDEBUG << "Opening rasserver database for client with ID: " << clientId << ", UUID: " << clientUUID;
    grpc::ClientContext openServerDbContext;
    grpc::Status openServerDbStatus = this->getRasServerService()->OpenServerDatabase(&openServerDbContext, openServerDatabaseReq, &openServerDatabaseRepl);

    if (!openServerDbStatus.ok())
    {
        handleError(openServerDbStatus.error_message());
        retval = 1;
    }

    // Send keep alive messages to rasserver until openDB is called
    this->startRasServerKeepAlive();

    return retval;
}

int RasnetClientComm::closeDB()
{
    int retval = 0; // ok

    try
    {
        CloseServerDatabaseReq closeServerDatabaseReq;
        CloseDbReq closeDbReq;
        Void closeDatabaseRepl;

        closeServerDatabaseReq.set_client_id(this->clientId);
        closeDbReq.set_clientid(this->clientId);

        // The remoteClientUUID identifies local and remote sessions
        closeDbReq.set_clientuuid(this->remoteClientUUID);
        closeDbReq.set_dbsessionid(this->sessionId);

        LDEBUG << "Closing rasserver database for client with ID: " << clientId << ", UUID: " << clientUUID;
        grpc::ClientContext closeServerDbContext;
        grpc::Status closeServerDbStatus = this->getRasServerService()->CloseServerDatabase(&closeServerDbContext, closeServerDatabaseReq, &closeDatabaseRepl);
        if (!closeServerDbStatus.ok())
        {
            handleError(closeServerDbStatus.error_message());
            retval = 1;
        }

        LDEBUG << "Closing rasmgr database for client with ID: " << clientId << ", UUID: " << clientUUID;
        grpc::ClientContext closeDbContext;
        grpc::Status closeDbStatus = this->getRasMgrService()->CloseDb(&closeDbContext, closeDbReq, &closeDatabaseRepl);
        if (!closeDbStatus.ok())
        {
            handleError(closeDbStatus.error_message());
            retval = 1;
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

int RasnetClientComm::createDB(__attribute__((unused)) const char *name)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

int RasnetClientComm::destroyDB(__attribute__((unused)) const char *name)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

int RasnetClientComm::openTA(unsigned short readOnly)
{
    int retval = 0; // ok

    BeginTransactionReq beginTransactionReq;
    BeginTransactionRepl beginTransactionRepl;

    beginTransactionReq.set_rw(readOnly == 0);
    beginTransactionReq.set_client_id(this->clientId);

    LDEBUG << "Begin rasserver transaction (ro: " << readOnly << "), client with ID: "
           << clientId << ", UUID: " << clientUUID;
    grpc::ClientContext context;
    grpc::Status beginTransationStatus = this->getRasServerService()->BeginTransaction(&context, beginTransactionReq, &beginTransactionRepl);
    if (!beginTransationStatus.ok())
    {
        handleError(beginTransationStatus.error_message());
    }

    return retval;
}

int RasnetClientComm::commitTA()
{
    int retval = 0; // ok

    CommitTransactionReq commitTransactionReq;
    CommitTransactionRepl commitTransactionRepl;

    commitTransactionReq.set_client_id(this->clientId);

    LDEBUG << "Commit rasserver transaction, client with ID: "
           << clientId << ", UUID: " << clientUUID;
    grpc::ClientContext context;
    grpc::Status commitStatus = this->getRasServerService()->CommitTransaction(&context, commitTransactionReq, &commitTransactionRepl);

    if (!commitStatus.ok())
    {
        handleError(commitStatus.error_message());
        retval = 1;
    }

    return retval;
}

int RasnetClientComm::abortTA()
{
    int retval = 0; // ok
    try
    {
        AbortTransactionReq abortTransactionReq;
        AbortTransactionRepl AbortTransactionRepl;

        abortTransactionReq.set_client_id(this->clientId);

        LDEBUG << "Abort rasserver transaction, client with ID: "
               << clientId << ", UUID: " << clientUUID;
        grpc::ClientContext context;
        grpc::Status abortTransactionStatus = this->getRasServerService()->AbortTransaction(&context, abortTransactionReq, &AbortTransactionRepl);
        if (!abortTransactionStatus.ok())
        {
            handleError(abortTransactionStatus.error_message());
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
    checkForRwTransaction();

    r_Minterval     spatdom;
    r_Bytes         marBytes;
    RPCMarray      *rpcMarray;
    r_Bytes         tileSize = 0;

    // get the spatial domain of the r_GMarray
    spatdom = mar->spatial_domain();

    // determine the amount of data to be transferred
    marBytes = mar->get_array_size();

    const r_Base_Type *baseType = mar->get_base_type_schema();

    // if the MDD is too large for being transfered as one block, it has to be
    // divided in tiles
    const r_Tiling *til = mar->get_storage_layout()->get_tiling();
    r_Tiling_Scheme scheme = til->get_tiling_scheme();
    if (scheme == r_NoTiling)
    {
        tileSize = RMInit::RMInit::clientTileSize;
    }
    else
        //allowed because the only subclass of tiling without size is no tiling
    {
        tileSize = ((const r_Size_Tiling *)til)->get_tile_size();
    }


    // initiate composition of MDD at server side
    int status = executeStartInsertPersMDD(collName, mar);  //rpcStatusPtr = rpcstartinsertpersmdd_1( params, binding_h );

    switch (status)
    {
    case 0:
        break; // OK
    case 2:
        throw r_Error(r_Error::r_Error_DatabaseClassUndefined);
        break;
    case 3:
        throw r_Error(r_Error::r_Error_CollectionElementTypeMismatch);
        break;
    case 4:
        throw r_Error(r_Error::r_Error_TypeInvalid);
        break;
    default:
        throw r_Error(r_Error::r_Error_TransferFailed);
        break;
    }

    auto bagOfTiles = mar->get_storage_layout()->decomposeMDD(mar);

    LTRACE << "decomposing into " << bagOfTiles.cardinality() << " tiles";

    r_Iterator<r_GMarray *> iter = bagOfTiles.create_iterator();
    r_GMarray *origTile;

    for (iter.reset(); iter.not_done(); iter.advance())
    {
        origTile = *iter;

        LTRACE << "inserting Tile with domain " << origTile->spatial_domain()
               << ", " << origTile->spatial_domain().cell_count() * origTile->get_type_length() << " bytes";

        getMarRpcRepresentation(origTile, rpcMarray, mar->get_storage_layout()->get_storage_format(), baseType);

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

    executeEndInsertMDD(true); //rpcendinsertmdd_1( params3, binding_h );

    // delete transient data
    bagOfTiles.remove_all();
}

r_Ref_Any RasnetClientComm::getMDDByOId(__attribute__((unused)) const r_OId &oid)
{
    LDEBUG << "Internal error: RasnetClientComm::getMDDByOId() not implemented, returning empty r_Ref_Any().";
    return r_Ref_Any();
}

void RasnetClientComm::insertColl(const char *collName, const char *typeName, const r_OId &oid)
{
    checkForRwTransaction();

    InsertCollectionReq insertCollectionReq;
    InsertCollectionRepl  insertCollectionRepl;

    insertCollectionReq.set_client_id(this->clientId);
    insertCollectionReq.set_collection_name(collName);
    insertCollectionReq.set_type_name(typeName);
    insertCollectionReq.set_oid(oid.get_string_representation());

    grpc::ClientContext context;
    grpc::Status insertStatus = this->getRasServerService()->InsertCollection(&context, insertCollectionReq, &insertCollectionRepl);
    if (!insertStatus .ok())
    {
        handleError(insertStatus.error_message());
    }

    int status = insertCollectionRepl.status();

    handleStatusCode(status, "insertColl");
}

void RasnetClientComm::deleteCollByName(const char *collName)
{
    checkForRwTransaction();

    DeleteCollectionByNameReq deleteCollectionByNameReq;
    DeleteCollectionByNameRepl deleteCollectionByNameRepl;

    deleteCollectionByNameReq.set_client_id(this->clientId);
    deleteCollectionByNameReq.set_collection_name(collName);

    grpc::ClientContext context;
    grpc::Status deleteCollectionStatus = this->getRasServerService()->DeleteCollectionByName(&context, deleteCollectionByNameReq, &deleteCollectionByNameRepl);
    if (!deleteCollectionStatus.ok())
    {
        handleError(deleteCollectionStatus.error_message());
    }

    handleStatusCode(deleteCollectionByNameRepl.status(), "deleteCollByName");
}

void RasnetClientComm::deleteObjByOId(const r_OId &oid)
{
    checkForRwTransaction();

    DeleteCollectionByOidReq deleteCollectionByOidReq;
    DeleteCollectionByOidRepl deleteCollectionByOidRepl;

    deleteCollectionByOidReq.set_client_id(this->clientId);
    deleteCollectionByOidReq.set_oid(oid.get_string_representation());

    grpc::ClientContext context;
    grpc::Status deleteCollectionStatus = this->getRasServerService()->DeleteCollectionByOid(&context, deleteCollectionByOidReq, &deleteCollectionByOidRepl);
    if (!deleteCollectionStatus.ok())
    {
        handleError(deleteCollectionStatus.error_message());
    }

    handleStatusCode(deleteCollectionByOidRepl.status(), "deleteCollByName");
}

void RasnetClientComm::removeObjFromColl(const char *name, const r_OId &oid)
{
    checkForRwTransaction();

    RemoveObjectFromCollectionReq removeObjectFromCollectionReq;
    RemoveObjectFromCollectionRepl removeObjectFromCollectionRepl;

    removeObjectFromCollectionReq.set_client_id(this->clientId);
    removeObjectFromCollectionReq.set_collection_name(name);
    removeObjectFromCollectionReq.set_oid(oid.get_string_representation());

    grpc::ClientContext context;
    grpc::Status removeObjectStatus = this->getRasServerService()->RemoveObjectFromCollection(&context, removeObjectFromCollectionReq, &removeObjectFromCollectionRepl);
    if (!removeObjectStatus.ok())
    {
        handleError(removeObjectStatus.error_message());
    }

    int status = removeObjectFromCollectionRepl.status();
    handleStatusCode(status, "removeObjFromColl");
}

r_Ref_Any RasnetClientComm::getCollByName(const char *name)
{
    r_Ref_Any result = executeGetCollByNameOrOId(name, r_OId());
    return result;
}

r_Ref_Any RasnetClientComm::getCollByOId(const r_OId &oid)
{
    r_Ref_Any result = executeGetCollByNameOrOId(NULL, oid);

    return result;
}

r_Ref_Any RasnetClientComm::getCollOIdsByName(const char *name)
{
    r_Ref_Any result = executeGetCollOIdsByNameOrOId(name, r_OId());

    return result;
}

r_Ref_Any RasnetClientComm::getCollOIdsByOId(const r_OId &oid)
{
    r_Ref_Any result = executeGetCollOIdsByNameOrOId(NULL, oid);

    return result;
}

void RasnetClientComm::executeQuery(const r_OQL_Query &query, r_Set<r_Ref_Any> &result)
{
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
        LDEBUG << "Internal error: RasnetClientComm::executeQuery(): illegal status value " << status;
    }

}

void RasnetClientComm::executeQuery(const r_OQL_Query &query)
{
    checkForRwTransaction();

    sendMDDConstants(query);

    executeExecuteUpdateQuery(query.get_query());
}

void RasnetClientComm::executeQuery(const r_OQL_Query &query, r_Set<r_Ref_Any> &result, __attribute__((unused)) int dummy)
{
    checkForRwTransaction();

    sendMDDConstants(query);

    int status = executeExecuteUpdateQuery(query.get_query(), result);

    LDEBUG << "executeUpdateQuery (retrieval) returns " << status;

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
        LDEBUG << "Internal error: RasnetClientComm::executeQuery(): illegal status value " << status;
    }

}

r_OId RasnetClientComm::getNewOId(unsigned short objType)
{
    GetNewOidReq getNewOidReq;
    GetNewOidRepl getNewOidRepl;

    getNewOidReq.set_client_id(this->clientId);
    getNewOidReq.set_object_type(objType);

    grpc::ClientContext context;
    grpc::Status getOidStatus = this->getRasServerService()->GetNewOid(&context, getNewOidReq, &getNewOidRepl);
    if (!getOidStatus.ok())
    {
        handleError(getOidStatus.error_message());
    }

    r_OId oid(getNewOidRepl.oid().c_str());
    return oid;
}

unsigned short RasnetClientComm::getObjectType(const r_OId &oid)
{
    GetObjectTypeReq getObjectTypeReq;
    GetObjectTypeRepl getObjectTypeRepl;

    getObjectTypeReq.set_client_id(this->clientId);
    getObjectTypeReq.set_oid(oid.get_string_representation());

    grpc::ClientContext context;
    grpc::Status getObjectTypeStatus = this->getRasServerService()->GetObjectType(&context, getObjectTypeReq, &getObjectTypeRepl);
    if (!getObjectTypeStatus.ok())
    {
        handleError(getObjectTypeStatus.error_message());
    }

    int status = getObjectTypeRepl.status();
    handleStatusCode(status, "getObjectType");

    unsigned short objectType = getObjectTypeRepl.object_type();
    return objectType;
}

char *RasnetClientComm::getTypeStructure(const char *typeName, r_Type_Type typeType)
{
    GetTypeStructureReq getTypeStructureReq;
    GetTypeStructureRepl getTypeStructureRepl;

    getTypeStructureReq.set_client_id(this->clientId);
    getTypeStructureReq.set_type_name(typeName);
    getTypeStructureReq.set_type_type(typeType);

    grpc::ClientContext context;
    grpc::Status getTypesStructuresStatus = this
                                            ->getRasServerService()
                                            ->GetTypeStructure(&context, getTypeStructureReq, &getTypeStructureRepl);
    if (!getTypesStructuresStatus.ok())
    {
        handleError(getTypesStructuresStatus.error_message());
    }

    int status = getTypeStructureRepl.status();
    handleStatusCode(status, "getTypeStructure");

    const auto &src = getTypeStructureRepl.type_structure();
    char *typeStructure = new char[src.size() + 1];
    strcpy(typeStructure, src.c_str());
    return typeStructure;
}

int RasnetClientComm::setTransferFormat(r_Data_Format format, const char *formatParams)
{
    storageFormat = format;

    if (storageFormatParams != NULL)
    {
        free(storageFormatParams);
        storageFormatParams = NULL;
    }
    if (formatParams != NULL)
    {
        storageFormatParams = (char *)mymalloc(strlen(formatParams) + 1);
        strcpy(storageFormatParams, formatParams);
        // extract ``compserver'' if present
        clientParams->process(storageFormatParams);
    }

    int result = executeSetFormat(false, format, formatParams);

    return result;
}

int RasnetClientComm::setStorageFormat(r_Data_Format format, const char *formatParams)
{
    transferFormat = format;

    if (transferFormatParams != NULL)
    {
        free(transferFormatParams);
        transferFormatParams = NULL;
    }
    if (formatParams != NULL)
    {
        transferFormatParams = (char *)mymalloc(strlen(formatParams) + 1);
        strcpy(transferFormatParams, formatParams);
        // extract ``exactformat'' if present
        clientParams->process(transferFormatParams);
    }

    int result = executeSetFormat(true, format, formatParams);
    return result;
}


void RasnetClientComm::initRasserverService()
{
    boost::unique_lock<boost::shared_mutex> lock(this->rasServerServiceMtx);
    if (!this->initializedRasServerService)
    {
        try
        {
            std::string rasServerAddress = GrpcUtils::constructAddressString(rasServerHost, static_cast<boost::uint32_t>(rasServerPort));
            std::shared_ptr<grpc::Channel> channel(grpc::CreateCustomChannel(rasServerAddress, grpc::InsecureChannelCredentials(), GrpcUtils::getDefaultChannelArguments()));

            this->rasserverService.reset(new ::rasnet::service::ClientRassrvrService::Stub(channel));
            this->rasserverHealthService.reset(new common::HealthService::Stub(channel));
            this->initializedRasServerService = true;
        }
        catch (std::exception &ex)
        {
            LERROR << ex.what();
            handleError(ex.what());
        }
    }
}

boost::shared_ptr<rasnet::service::ClientRassrvrService::Stub> RasnetClientComm::getRasServerService(bool throwIfConnectionFailed)
{
    this->initRasserverService();

    // Check if the rasserver is serving
    if (!GrpcUtils::isServerAlive(this->rasserverHealthService, SERVICE_CALL_TIMEOUT) && throwIfConnectionFailed)
    {
        LDEBUG << "The client failed to connect to rasserver.";
        throw r_EGeneral("The client failed to connect to rasserver.");
    }

    return this->rasserverService;
}

void RasnetClientComm::closeRasserverService()
{
    boost::unique_lock<shared_mutex> lock(this->rasServerServiceMtx);
    if (this->initializedRasServerService)
    {
        this->initializedRasServerService = false;
        this->rasserverService.reset();
        this->rasserverHealthService.reset();
    }
}

boost::shared_ptr<rasnet::service::RasmgrClientService::Stub> RasnetClientComm::getRasMgrService(bool throwIfConnectionFailed)
{
    this->initRasmgrService();

    // Check if the rasmgr is serving
    if (!GrpcUtils::isServerAlive(this->rasmgrHealthService, SERVICE_CALL_TIMEOUT) && throwIfConnectionFailed)
    {
        LDEBUG << "The client failed to connect to rasmgr.";
        throw r_EGeneral("The client failed to connect to rasmgr.");
    }

    return this->rasmgrService;
}

void RasnetClientComm::initRasmgrService()
{
    boost::unique_lock<shared_mutex> lock(this->rasMgrServiceMtx);
    if (!this->initializedRasMgrService)
    {
        try
        {
            std::shared_ptr<Channel> channel(grpc::CreateCustomChannel(rasmgrHost, grpc::InsecureChannelCredentials(), GrpcUtils::getDefaultChannelArguments()));
            this->rasmgrService.reset(new ::rasnet::service::RasmgrClientService::Stub(channel));
            this->rasmgrHealthService.reset(new common::HealthService::Stub(channel));

            this->initializedRasMgrService = true;
        }
        catch (std::exception &ex)
        {
            LERROR << ex.what();
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


int RasnetClientComm::executeStartInsertPersMDD(const char *collName, r_GMarray *mar)
{
    StartInsertMDDReq startInsertMDDReq;
    StartInsertMDDRepl startInsertMDDRepl;

    startInsertMDDReq.set_client_id(this->clientId);
    startInsertMDDReq.set_collname(collName);
    startInsertMDDReq.set_domain(mar->spatial_domain().to_string());
    startInsertMDDReq.set_type_length(static_cast<int32_t>(mar->get_type_length()));
    startInsertMDDReq.set_type_name(mar->get_type_name());
    startInsertMDDReq.set_oid(mar->get_oid().get_string_representation());

    grpc::ClientContext context;
    grpc::Status startInsertStatus = this->getRasServerService()->StartInsertMDD(&context, startInsertMDDReq, &startInsertMDDRepl);
    if (!startInsertStatus.ok())
    {
        handleError(startInsertStatus.error_message());
    }

    return startInsertMDDRepl.status();
}

int RasnetClientComm::executeInsertTile(bool persistent, RPCMarray *tile)
{
    InsertTileReq insertTileReq;
    InsertTileRepl insertTileRepl;

    insertTileReq.set_client_id(this->clientId);
    insertTileReq.set_persistent(persistent);
    insertTileReq.set_domain(tile->domain);
    insertTileReq.set_type_length(tile->cellTypeLength);
    insertTileReq.set_current_format(tile->currentFormat);
    insertTileReq.set_storage_format(tile->storageFormat);
    insertTileReq.set_data(tile->data.confarray_val, tile->data.confarray_len);
    insertTileReq.set_data_length(static_cast<int32_t>(tile->data.confarray_len));

    grpc::ClientContext context;
    grpc::Status insertTileStatus = this->getRasServerService()->InsertTile(&context, insertTileReq, &insertTileRepl);
    if (!insertTileStatus.ok())
    {
        handleError(insertTileStatus.error_message());
    }

    return insertTileRepl.status();
}

void RasnetClientComm::executeEndInsertMDD(bool persistent)
{
    EndInsertMDDReq endInsertMDDReq;
    EndInsertMDDRepl endInsertMDDRepl;

    endInsertMDDReq.set_client_id(this->clientId);
    endInsertMDDReq.set_persistent(persistent);

    grpc::ClientContext context;
    grpc::Status endInsertMDDStatus = this->getRasServerService()->EndInsertMDD(&context, endInsertMDDReq, &endInsertMDDRepl);
    if (!endInsertMDDStatus.ok())
    {
        handleError(endInsertMDDStatus.error_message());
    }

    handleStatusCode(endInsertMDDRepl.status(), "executeEndInsertMDD");
}

void RasnetClientComm::getMDDCollection(r_Set<r_Ref_Any> &mddColl, unsigned int isQuery)
{
    unsigned short tileStatus = 0;
    unsigned short mddStatus = 0;

    while (mddStatus == 0)   // repeat until all MDDs are transferred
    {
        r_Ref<r_GMarray> mddResult;

        // Get spatial domain of next MDD
        GetMDDRes *thisResult = executeGetNextMDD();

        mddStatus = thisResult->status;

        if (mddStatus == 2)
        {
            LERROR << "Error: getMDDCollection(...) - no transfer collection or empty transfer collection";
            throw r_Error(r_Error::r_Error_TransferFailed);
        }

        tileStatus = getMDDCore(mddResult, thisResult, isQuery);

        // finally, insert the r_Marray into the set

        mddColl.insert_element(mddResult, 1);

        free(thisResult->domain);
        free(thisResult->typeName);
        free(thisResult->typeStructure);
        free(thisResult->oid);
        delete   thisResult;

        if (tileStatus == 0)   // if this is true, we're done with this collection
        {
            break;
        }

    } // end while( mddStatus == 0 )

    executeEndTransfer();
}

int RasnetClientComm::executeEndTransfer()
{
    EndTransferReq endTransferReq;
    EndTransferRepl endTransferRepl;

    endTransferReq.set_client_id(this->clientId);

    grpc::ClientContext context;
    grpc::Status endTransferStatus = this->getRasServerService()->EndTransfer(&context, endTransferReq, &endTransferRepl);
    if (!endTransferStatus.ok())
    {
        handleError(endTransferStatus.error_message());
    }

    return endTransferRepl.status();
}

GetMDDRes *RasnetClientComm::executeGetNextMDD()
{
    GetNextMDDReq getNextMDDReq;
    GetNextMDDRepl getNextMDDRepl;

    getNextMDDReq.set_client_id(this->clientId);

    grpc::ClientContext context;
    grpc::Status getNextMDD = this->getRasServerService()->GetNextMDD(&context, getNextMDDReq, &getNextMDDRepl);
    if (!getNextMDD.ok())
    {
        handleError(getNextMDD.error_message());
    }

    GetMDDRes *result = new GetMDDRes();
    result->status = getNextMDDRepl.status();
    result->domain = strdup(getNextMDDRepl.domain().c_str());
    result->typeName = strdup(getNextMDDRepl.type_name().c_str());
    result->typeStructure = strdup(getNextMDDRepl.type_structure().c_str());
    result->oid = strdup(getNextMDDRepl.oid().c_str());
    result->currentFormat = getNextMDDRepl.current_format();

    return result;
}

unsigned short RasnetClientComm::getMDDCore(r_Ref<r_GMarray> &mdd, GetMDDRes *thisResult, unsigned int isQuery)
{
    this->updateTransaction();

    //  create r_Minterval and oid
    r_Minterval mddDomain(thisResult->domain);
    r_OId       rOId(thisResult->oid);
    r_GMarray  *marray;

    if (isQuery)
    {
        marray = new (database, r_Object::transient, rOId) r_GMarray(transaction);
    }
    else
    {
        marray = new (database, r_Object::read, rOId) r_GMarray(transaction);
    }

    marray->set_spatial_domain(mddDomain);
    marray->set_type_by_name(thisResult->typeName);
    marray->set_type_structure(thisResult->typeStructure);

    r_Data_Format currentFormat = static_cast<r_Data_Format>(thisResult->currentFormat);
//    currentFormat = r_Array;
    marray->set_current_format(currentFormat);

    r_Data_Format decompFormat;

    const r_Base_Type *baseType = marray->get_base_type_schema();
    unsigned long marrayOffset = 0;

    // Variables needed for tile transfer
    GetTileRes *tileRes = 0;
    unsigned short  mddDim = mddDomain.dimension();  // we assume that each tile has the same dimensionality as the MDD
    r_Minterval     tileDomain;
    r_GMarray      *tile;  // for temporary tile
    char           *memCopy;
    unsigned long   memCopyLen;
    int             tileCntr = 0;
    unsigned short  tileStatus   = 0;

    tileStatus = 2; // call rpcgetnexttile_1 at least once

    while (tileStatus == 2 || tileStatus == 3)   // while( for all tiles of the current MDD )
    {
        tileRes = executeGetNextTile();

        tileStatus = tileRes->status;

        if (tileStatus == 4)
        {
            freeGetTileRes(tileRes);
            LERROR << "Error: rpcGetNextTile(...) - no tile to transfer or empty transfer collection";
            throw r_Error(r_Error::r_Error_TransferFailed);
        }

        // take cellTypeLength for current MDD of the first tile
        if (tileCntr == 0)
        {
            marray->set_type_length(tileRes->marray->cellTypeLength);
        }
        tileCntr++;

        tileDomain = r_Minterval(tileRes->marray->domain);

        if (currentFormat == r_Array)
        {
            memCopyLen = tileDomain.cell_count() * marray->get_type_length(); // cell type length of the tile must be the same
            if (memCopyLen < tileRes->marray->data.confarray_len)
            {
                memCopyLen = tileRes->marray->data.confarray_len;    // may happen when compression expands
            }
            memCopy    = new char[ memCopyLen ];

            // create temporary tile
            tile = new r_GMarray(transaction);
            tile->set_spatial_domain(tileDomain);
            tile->set_array(memCopy);
            tile->set_array_size(memCopyLen);
            tile->set_type_length(tileRes->marray->cellTypeLength);

            // Variables needed for block transfer of a tile
            unsigned long  blockOffset = 0;
            unsigned short subStatus  = 3;
            currentFormat = static_cast<r_Data_Format>(tileRes->marray->currentFormat);

            switch (tileStatus)
            {
            case 3: // at least one block of the tile is left

                // Tile arrives in several blocks -> put them together
                concatArrayData(tileRes->marray->data.confarray_val, tileRes->marray->data.confarray_len, memCopy, memCopyLen, blockOffset);
                freeGetTileRes(tileRes);

                tileRes = executeGetNextTile();//rpcgetnexttile_1( &clientID, binding_h );

                subStatus = tileRes->status;

                if (subStatus == 4)
                {
                    freeGetTileRes(tileRes);
                    throw r_Error(r_Error::r_Error_TransferFailed);
                }

                concatArrayData(tileRes->marray->data.confarray_val, tileRes->marray->data.confarray_len, memCopy, memCopyLen, blockOffset);
                freeGetTileRes(tileRes);

                tileStatus = subStatus;
                break;

            default: // tileStatus = 0,3 last block of the current tile

                // Tile arrives as one block.
                concatArrayData(tileRes->marray->data.confarray_val, tileRes->marray->data.confarray_len, memCopy, memCopyLen, blockOffset);
                freeGetTileRes(tileRes);

                break;
            }

            char *marrayData = NULL;
            // Now the tile is transferred completely, insert it into current MDD
            if (tileStatus < 2 && tileCntr == 1 && (tile->spatial_domain() == marray->spatial_domain()))
            {
                // MDD consists of just one tile that is the same size of the mdd

                // simply take the data memory of the tile
                marray->set_array(tile->get_array());
                marray->set_array_size(tile->get_array_size());
                tile->set_array(0);
            }
            else
            {
                // MDD consists of more than one tile or the tile does not cover the whole domain

                r_Bytes size = mddDomain.cell_count() * marray->get_type_length();

                if (tileCntr == 1)
                {
                    // allocate memory for the MDD
                    marrayData = new char[ size ];
                    memset(marrayData, 0, size);

                    marray->set_array(marrayData);
                }
                else
                {
                    marrayData = marray->get_array();
                }


                // copy tile data into MDD data space (optimized, relying on the internal representation of an MDD )
                char         *mddBlockPtr;
                char         *tileBlockPtr = tile->get_array();
                unsigned long blockCells   = static_cast<unsigned long>(tileDomain[tileDomain.dimension() - 1].high() - tileDomain[tileDomain.dimension() - 1].low() + 1);
                unsigned long blockSize    = blockCells * marray->get_type_length();
                unsigned long blockNo      = tileDomain.cell_count() / blockCells;

                for (unsigned long blockCtr = 0; blockCtr < blockNo; blockCtr++)
                {
                    mddBlockPtr = marrayData + marray->get_type_length() * mddDomain.cell_offset(tileDomain.cell_point(blockCtr * blockCells));
                    memcpy(mddBlockPtr, tileBlockPtr, blockSize);
                    tileBlockPtr += blockSize;
                }

                marray->set_array_size(size);
            }

            // delete temporary tile
            delete tile;
        }
        else
        {
            //
            // handle encoded data
            //
            char *marrayData = NULL;
            if (tileCntr == 1)
            {
                // allocate memory for the MDD
                r_Bytes size = mddDomain.cell_count() * marray->get_type_length();
                marrayData = new char[ size ];
                memset(marrayData, 0, size);
                marray->set_array(marrayData);
                marray->set_array_size(size);
            }
            else
            {
                marrayData = marray->get_array();
            }

            unsigned long blockSize = tileRes->marray->data.confarray_len;
            char *mddBlockPtr = marrayData + marrayOffset;
            char *tileBlockPtr = tileRes->marray->data.confarray_val;
            memcpy(mddBlockPtr, tileBlockPtr, blockSize);
            marrayOffset += blockSize;

            free(tileRes->marray->domain);
            free(tileRes->marray->data.confarray_val);
            delete tileRes->marray;
            delete tileRes;
            tileRes = NULL;
        }

    }  // end while( MDD is not transferred completely )


    mdd = r_Ref<r_GMarray>(marray->get_oid(), marray, transaction);

    return tileStatus;
}

GetTileRes *RasnetClientComm::executeGetNextTile()
{
    GetNextTileReq getNextTileReq;
    GetNextTileRepl getNextTileRepl;

    getNextTileReq.set_client_id(this->clientId);

    grpc::ClientContext context;
    grpc::Status getNextTileStatus = this->getRasServerService()->GetNextTile(&context, getNextTileReq, &getNextTileRepl);
    if (!getNextTileStatus.ok())
    {
        handleError(getNextTileStatus.error_message());
    }

    GetTileRes *result = new GetTileRes();
    result->marray = new RPCMarray();

    result->status = getNextTileRepl.status();
    result->marray->domain = strdup(getNextTileRepl.domain().c_str());
    result->marray->cellTypeLength = static_cast<u_long>(getNextTileRepl.cell_type_length());
    result->marray->currentFormat = getNextTileRepl.current_format();
    result->marray->storageFormat = getNextTileRepl.storage_format();

    int length = getNextTileRepl.data_length();
    result->marray->data.confarray_len = static_cast<u_int>(length);
    result->marray->data.confarray_val = (char *) mymalloc(static_cast<size_t>(length));
    memcpy(result->marray->data.confarray_val, getNextTileRepl.data().c_str(), static_cast<size_t>(length));

    return result;
}

void RasnetClientComm::getMarRpcRepresentation(const r_GMarray *mar, RPCMarray *&rpcMarray, r_Data_Format initStorageFormat, __attribute__((unused)) const r_Base_Type *baseType)
{
    // allocate memory for the RPCMarray data structure and assign its fields
    rpcMarray                 = (RPCMarray *)mymalloc(sizeof(RPCMarray));
    rpcMarray->domain         = mar->spatial_domain().get_string_representation();
    rpcMarray->cellTypeLength = mar->get_type_length();
    rpcMarray->currentFormat = initStorageFormat;
    rpcMarray->data.confarray_len = mar->get_array_size();
    rpcMarray->data.confarray_val = const_cast<char *>(mar->get_array());
    rpcMarray->storageFormat = initStorageFormat;
}


void RasnetClientComm::freeMarRpcRepresentation(const r_GMarray *mar, RPCMarray *rpcMarray)
{
    if (rpcMarray->data.confarray_val != mar->get_array())
    {
        delete[] rpcMarray->data.confarray_val;
    }

    free(rpcMarray->domain);
    free(rpcMarray);
}

int RasnetClientComm::concatArrayData(const char *source, unsigned long srcSize, char *&dest, unsigned long &destSize, unsigned long &blockOffset)
{
    if (blockOffset + srcSize > destSize)
    {
        // need to extend dest
        unsigned long newSize = blockOffset + srcSize;
        char *newArray;

        // allocate a little extra if we have to extend
        newSize = newSize + newSize / 16;

        //    LTRACE << need to extend from " << destSize << " to " << newSize;

        if ((newArray = new char[newSize]) == NULL)
        {
            return -1;
        }

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
    }
    if (ptr->marray->data.confarray_val)
    {
        free(ptr->marray->data.confarray_val);
    }
    delete ptr->marray;
    delete ptr;
}

r_Ref_Any RasnetClientComm::executeGetCollByNameOrOId(const char *collName, const r_OId &oid)
{
    GetCollectionByNameOrOidReq getCollectionByNameOrOidReq;
    GetCollectionByNameOrOidRepl getCollectionByNameOrOidRepl;


    getCollectionByNameOrOidReq.set_client_id(this->clientId);

    if (collName != NULL)
    {
        getCollectionByNameOrOidReq.set_collection_identifier(collName);
        getCollectionByNameOrOidReq.set_is_name(true);
    }
    else
    {
        getCollectionByNameOrOidReq.set_collection_identifier(oid.get_string_representation());
        getCollectionByNameOrOidReq.set_is_name(false);
    }

    grpc::ClientContext context;
    grpc::Status rasServerStatus = this->getRasServerService()->GetCollectionByNameOrOid(&context, getCollectionByNameOrOidReq, &getCollectionByNameOrOidRepl);
    if (!rasServerStatus.ok())
    {
        handleError(rasServerStatus.error_message());
    }

    int status = getCollectionByNameOrOidRepl.status();
    handleStatusCode(status, "getCollByName");

    this->updateTransaction();

    r_OId rOId(getCollectionByNameOrOidRepl.oid().c_str());
    r_Set<r_Ref_Any> *set  = new (database, r_Object::read, rOId)  r_Set<r_Ref_Any>;

    set->set_type_by_name(getCollectionByNameOrOidRepl.type_name().c_str());
    set->set_type_structure(getCollectionByNameOrOidRepl.type_structure().c_str());
    set->set_object_name(getCollectionByNameOrOidRepl.collection_name().c_str());

    if (status == 0)
    {
        getMDDCollection(*set, 0);
    }
    //  else rpcStatus == 1 -> Result collection is empty and nothing has to be got.

    r_Ref_Any result = r_Ref_Any(set->get_oid(), set, transaction);
    return result;
}

r_Ref_Any RasnetClientComm::executeGetCollOIdsByNameOrOId(const char *collName, const r_OId &oid)
{
    GetCollOidsByNameOrOidReq getCollOidsByNameOrOidReq;
    GetCollOidsByNameOrOidRepl getCollOidsByNameOrOidRepl;

    getCollOidsByNameOrOidReq.set_client_id(this->clientId);

    if (collName != NULL)
    {
        getCollOidsByNameOrOidReq.set_collection_identifier(collName);
        getCollOidsByNameOrOidReq.set_is_name(true);
    }
    else
    {
        getCollOidsByNameOrOidReq.set_collection_identifier(oid.get_string_representation());
        getCollOidsByNameOrOidReq.set_is_name(false);
    }

    grpc::ClientContext context;
    grpc::Status getCollOidsStatus = this->getRasServerService()->GetCollOidsByNameOrOid(&context, getCollOidsByNameOrOidReq, &getCollOidsByNameOrOidRepl);
    if (!getCollOidsStatus.ok())
    {
        handleError(getCollOidsStatus.error_message());
    }

    int status = getCollOidsByNameOrOidRepl.status();

    if (status != 0 && status != 1)
    {
        handleStatusCode(status, "executeGetCollOIdsByNameOrOId");
    }

    const char *typeName = getCollOidsByNameOrOidRepl.type_name().c_str();
    const char *typeStructure = getCollOidsByNameOrOidRepl.type_structure().c_str();
    const char *oidString = getCollOidsByNameOrOidRepl.oids_string().c_str();
    const char *collectionName = getCollOidsByNameOrOidRepl.collection_name().c_str();

    this->updateTransaction();

    r_OId rOId(oidString);
    r_Set<r_Ref<r_GMarray>> *set = new (database, r_Object::read, rOId)  r_Set<r_Ref<r_GMarray>>;

    set->set_type_by_name(typeName);
    set->set_type_structure(typeStructure);
    set->set_object_name(collName);

    for (int i = 0; i < getCollOidsByNameOrOidRepl.oid_set_size(); ++i)
    {
        r_OId roid(getCollOidsByNameOrOidRepl.oid_set(i).c_str());
        set->insert_element(r_Ref<r_GMarray>(roid, transaction), 1);
    }

    r_Ref_Any result = r_Ref_Any(set->get_oid(), set, transaction);
    return result;
}

void RasnetClientComm::sendMDDConstants(const r_OQL_Query &query)
{
    unsigned short status;

    if (query.get_constants())
    {
        r_Set<r_GMarray *> *mddConstants = const_cast<r_Set<r_GMarray *>*>(query.get_constants());

        // in fact executeInitUpdate prepares server structures for MDD transfer
        if (executeInitUpdate() != 0)
        {
            throw r_Error(r_Error::r_Error_TransferFailed);
        }

        r_Iterator<r_GMarray *> iter = mddConstants->create_iterator();

        for (iter.reset(); iter.not_done(); iter++)
        {
            r_GMarray *mdd = *iter;

            const r_Base_Type *baseType = mdd->get_base_type_schema();

            if (mdd)
            {
                status = executeStartInsertTransMDD(mdd);
                switch (status)
                {
                case 0:
                    break; // OK
                case 2:
                    throw r_Error(r_Error::r_Error_DatabaseClassUndefined);
                    break;
                case 3:
                    throw r_Error(r_Error::r_Error_TypeInvalid);
                    break;
                default:
                    throw r_Error(r_Error::r_Error_TransferFailed);
                    break;
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
                
                r_Iterator<r_GMarray *> iter2 = bagOfTiles->create_iterator();

                for (iter2.reset(); iter2.not_done(); iter2.advance())
                {
                    RPCMarray *rpcMarray;

                    r_GMarray *origTile = *iter2;

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
    StartInsertTransMDDReq startInsertTransMDDReq;
    StartInsertTransMDDRepl startInsertTransMDDRepl;

    startInsertTransMDDReq.set_client_id(this->clientId);
    startInsertTransMDDReq.set_domain(mdd->spatial_domain().to_string());
    startInsertTransMDDReq.set_type_length(static_cast<int32_t>(mdd->get_type_length()));
    startInsertTransMDDReq.set_type_name(mdd->get_type_name());

    grpc::ClientContext context;
    grpc::Status startInsertTransMDDStatus = this->getRasServerService()->StartInsertTransMDD(&context, startInsertTransMDDReq, &startInsertTransMDDRepl);
    if (!startInsertTransMDDStatus.ok())
    {
        handleError(startInsertTransMDDStatus.error_message());
    }

    return startInsertTransMDDRepl.status();
}

int RasnetClientComm::executeInitUpdate()
{
    InitUpdateReq initUpdateReq;
    InitUpdateRepl initUpdateRepl;

    initUpdateReq.set_client_id(this->clientId);

    grpc::ClientContext context;
    grpc::Status initUpdataStatus = this->getRasServerService()->InitUpdate(&context, initUpdateReq, &initUpdateRepl);
    if (!initUpdataStatus.ok())
    {
        handleError(initUpdataStatus.error_message());
    }

    return initUpdateRepl.status();
}


int RasnetClientComm::executeExecuteQuery(const char *query, r_Set<r_Ref_Any> &result)
{
    ExecuteQueryReq executeQueryReq;
    ExecuteQueryRepl executeQueryRepl;

    executeQueryReq.set_client_id(this->clientId);
    executeQueryReq.set_query(query);

    grpc::ClientContext context;
    grpc::Status executeQueryStatus = this->getRasServerService()->ExecuteQuery(&context, executeQueryReq, &executeQueryRepl);
    if (!executeQueryStatus.ok())
    {
        handleError(executeQueryStatus.error_message());
    }

    int status = executeQueryRepl.status();
    unsigned int errNo = static_cast<unsigned int>(executeQueryRepl.err_no());
    unsigned int lineNo = static_cast<unsigned int>(executeQueryRepl.line_no());
    unsigned int colNo = static_cast<unsigned int>(executeQueryRepl.col_no());
    const char *token = executeQueryRepl.token().c_str();
    const char *typeName = executeQueryRepl.type_name().c_str();
    const char *typeStructure = executeQueryRepl.type_structure().c_str();

    if (status == 0 || status == 1)
    {
        result.set_type_by_name(typeName);
        result.set_type_structure(typeStructure);
    }

    if (status == 4 || status == 5)
    {
        r_Equery_execution_failed err(errNo, lineNo, colNo, token);
        throw err;
    }

    return status;
}

GetElementRes *RasnetClientComm::executeGetNextElement()
{
    GetNextElementReq getNextElementReq;
    GetNextElementRepl getNextElementRepl;

    getNextElementReq.set_client_id(this->clientId);

    grpc::ClientContext context;
    grpc::Status getNextElementStatus = this->getRasServerService()->GetNextElement(&context, getNextElementReq, &getNextElementRepl);
    if (!getNextElementStatus.ok())
    {
        handleError(getNextElementStatus.error_message());
    }

    GetElementRes *result = new GetElementRes();

    result->data.confarray_len = static_cast<u_int>(getNextElementRepl.data_length());
    result->data.confarray_val = new char[getNextElementRepl.data_length()];
    memcpy(result->data.confarray_val, getNextElementRepl.data().c_str(), static_cast<size_t>(getNextElementRepl.data_length()));
    result->status = getNextElementRepl.status();

    return result;
}

void RasnetClientComm::getElementCollection(r_Set<r_Ref_Any> &resultColl)
{
    unsigned short rpcStatus = 0;
    this->updateTransaction();

    LDEBUG << "got set of type " << resultColl.get_type_structure();

    while (rpcStatus == 0)   // repeat until all elements are transferred
    {
        GetElementRes *thisResult = executeGetNextElement();

        rpcStatus = thisResult->status;

        if (rpcStatus == 2)
        {
            throw r_Error(r_Error::r_Error_TransferFailed);
        }
        // create new collection element, use type of collection resultColl
        r_Ref_Any     element;
        const r_Type *elementType = resultColl.get_element_type_schema();

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
            element = new r_Primitive(thisResult->data.confarray_val, static_cast<r_Primitive_Type *>(const_cast<r_Type *>(elementType)));
            transaction->add_object_list(r_Transaction::SCALAR, (void *) element);
            break;

        case r_Type::COMPLEXTYPE1:
        case r_Type::COMPLEXTYPE2:
        case r_Type::CINT16:
        case r_Type::CINT32:
            element = new r_Complex(thisResult->data.confarray_val, static_cast<r_Complex_Type *>(const_cast<r_Type *>(elementType)));
            transaction->add_object_list(r_Transaction::SCALAR, (void *)element);
            break;

        case r_Type::STRUCTURETYPE:
            element = new r_Structure(thisResult->data.confarray_val, static_cast<r_Structure_Type *>(const_cast<r_Type *>(elementType)));
            transaction->add_object_list(r_Transaction::SCALAR, (void *) element);
            break;

        case r_Type::POINTTYPE:
        {
            char *stringRep = new char[thisResult->data.confarray_len + 1];
            strncpy(stringRep, thisResult->data.confarray_val, thisResult->data.confarray_len);
            stringRep[thisResult->data.confarray_len] = '\0';

            r_Point *typedElement = new r_Point(stringRep);
            element               = typedElement;
            transaction->add_object_list(r_Transaction::POINT, (void *) typedElement);
            delete [] stringRep;
        }
        break;

        case r_Type::SINTERVALTYPE:
        {
            char *stringRep = new char[thisResult->data.confarray_len + 1];
            strncpy(stringRep, thisResult->data.confarray_val, thisResult->data.confarray_len);
            stringRep[thisResult->data.confarray_len] = '\0';

            r_Sinterval *typedElement = new r_Sinterval(stringRep);
            element                   = typedElement;
            transaction->add_object_list(r_Transaction::SINTERVAL, (void *) typedElement);
            delete [] stringRep;
        }
        break;

        case r_Type::MINTERVALTYPE:
        {
            char *stringRep = new char[thisResult->data.confarray_len + 1];
            strncpy(stringRep, thisResult->data.confarray_val, thisResult->data.confarray_len);
            stringRep[thisResult->data.confarray_len] = '\0';

            r_Minterval *typedElement = new r_Minterval(stringRep);
            element                   = typedElement;
            transaction->add_object_list(r_Transaction::MINTERVAL, (void *) typedElement);
            delete [] stringRep;
        }
        break;

        case r_Type::OIDTYPE:
        {
            char *stringRep = new char[thisResult->data.confarray_len + 1];
            strncpy(stringRep, thisResult->data.confarray_val, thisResult->data.confarray_len);
            stringRep[thisResult->data.confarray_len] = '\0';

            r_OId *typedElement = new r_OId(stringRep);
            element             = typedElement;
            transaction->add_object_list(r_Transaction::OID, (void *) typedElement);
            delete [] stringRep;
        }
        break;
        default:
            break;
        }

        LDEBUG << "got an element";

        // insert element into result set
        resultColl.insert_element(element, 1);

        delete[] thisResult->data.confarray_val;
        delete   thisResult;
    }

    executeEndTransfer();
}

int RasnetClientComm::executeExecuteUpdateQuery(const char *query)
{
    ExecuteUpdateQueryReq executeUpdateQueryReq;
    ExecuteUpdateQueryRepl executeUpdateQueryRepl;

    executeUpdateQueryReq.set_client_id(this->clientId);
    executeUpdateQueryReq.set_query(query);

    grpc::ClientContext context;
    grpc::Status executeUpdateQueryStatus = this->getRasServerService()->ExecuteUpdateQuery(&context, executeUpdateQueryReq, &executeUpdateQueryRepl);
    if (!executeUpdateQueryStatus.ok())
    {
        handleError(executeUpdateQueryStatus.error_message());
    }

    int status = executeUpdateQueryRepl.status();
    unsigned int errNo = static_cast<unsigned int>(executeUpdateQueryRepl.errono());
    unsigned int lineNo = static_cast<unsigned int>(executeUpdateQueryRepl.lineno());
    unsigned int colNo = static_cast<unsigned int>(executeUpdateQueryRepl.colno());

    string token = executeUpdateQueryRepl.token();

    if (status == 2 || status == 3)
    {
        throw r_Equery_execution_failed(errNo, lineNo, colNo, token.c_str());
    }

    if (status == 1)
    {
        throw r_Error(r_Error::r_Error_ClientUnknown);
    }

    if (status > 3)
    {
        throw r_Error(r_Error::r_Error_TransferFailed);
    }

    return status;
}

int  RasnetClientComm::executeExecuteUpdateQuery(const char *query, r_Set<r_Ref_Any> &result)
{
    ExecuteInsertQueryReq executeInsertQueryReq;
    ExecuteInsertQueryRepl executeInsertQueryRepl;

    executeInsertQueryReq.set_client_id(this->clientId);
    executeInsertQueryReq.set_query(query);

    grpc::ClientContext context;
    grpc::Status executeInsertStatus = this->getRasServerService()->ExecuteInsertQuery(&context, executeInsertQueryReq, &executeInsertQueryRepl);
    if (!executeInsertStatus.ok())
    {
        handleError(executeInsertStatus.error_message());
    }

    int status = executeInsertQueryRepl.status();
    unsigned int errNo = static_cast<unsigned int>(executeInsertQueryRepl.errono());
    unsigned int lineNo = static_cast<unsigned int>(executeInsertQueryRepl.lineno());
    unsigned int colNo = static_cast<unsigned int>(executeInsertQueryRepl.colno());
    string token = executeInsertQueryRepl.token();
    const char *typeName = executeInsertQueryRepl.type_name().c_str();
    const char *typeStructure = executeInsertQueryRepl.type_structure().c_str();

    if (status == 0 || status == 1 || status == 2)
    {
        result.set_type_by_name(typeName);
        result.set_type_structure(typeStructure);
    }

    // status == 2 - empty result

    if (status == 4 || status == 5)
    {
        throw r_Equery_execution_failed(errNo, lineNo, colNo, token.c_str());
    }

    return status;
}

int RasnetClientComm::executeSetFormat(bool lTransferFormat, r_Data_Format format, const char *formatParams)
{
    SetFormatReq setFormatReq;
    SetFormatRepl setFormatRepl;

    setFormatReq.set_client_id(this->clientId);
    setFormatReq.set_transfer_format((lTransferFormat ? 1 : 0));
    setFormatReq.set_format(format);
    setFormatReq.set_format_params(formatParams);

    grpc::ClientContext context;
    grpc::Status setFormatStatus = this->getRasServerService()->SetFormat(&context, setFormatReq, &setFormatRepl);
    if (!setFormatStatus.ok())
    {
        handleError(setFormatStatus.error_message());
    }

    return setFormatRepl.status();
}

void RasnetClientComm::checkForRwTransaction()
{
    this->updateTransaction();
    r_Transaction *trans = transaction;
    if (trans == 0 || trans->get_mode() == r_Transaction::read_only)
    {
        LDEBUG << "RasnetClientComm::checkForRwTransaction(): throwing exception from failed TA rw check.";
        throw r_Error(r_Error::r_Error_TransactionReadOnly);
    }
}

void RasnetClientComm::handleError(const string &error)
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
            if (!what.empty() && message.kind() == 1 && message.error_no() == 0)
            {
                throw r_Error(what.c_str());
            }
            else
            {
                throw r_Error(static_cast<r_Error::kind>(message.kind()), message.error_no());
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

void RasnetClientComm::handleConnectionFailure()
{
    throw r_EGeneral("The client failed to contact the server.");
}

void RasnetClientComm::handleStatusCode(int status, const string &method)
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

bool RasnetClientComm::effectivTypeIsRNP()
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

long unsigned int RasnetClientComm::getClientID() const
{
    return this->clientId;
}

void RasnetClientComm::triggerAliveSignal()
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

void RasnetClientComm::sendAliveSignal()
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

void RasnetClientComm::setUserIdentification(const char *userName, const char *plainTextPassword)
{

    connectClient(string(userName), common::Crypto::messageDigest(string(plainTextPassword), DEFAULT_DIGEST));
}

void RasnetClientComm::setMaxRetry(__attribute__((unused)) unsigned int newMaxRetry)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

unsigned int RasnetClientComm::getMaxRetry()
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

void RasnetClientComm::setTimeoutInterval(__attribute__((unused)) int seconds)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

int RasnetClientComm::getTimeoutInterval()
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
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
                KeepAliveReq keepAliveReq;
                Void keepAliveRepl;

                keepAliveReq.set_clientuuid(this->clientUUID);

                grpc::ClientContext context;

                // We do not want this thread to block forever
                system_clock::time_point deadline = system_clock::now() + milliseconds(SERVICE_CALL_TIMEOUT);
                context.set_deadline(deadline);

                grpc::Status keepAliveStatus = this->getRasMgrService(false)->KeepAlive(&context, keepAliveReq, &keepAliveRepl);

                if (!keepAliveStatus.ok())
                {
                    LERROR << "Failed to send keep alive message to rasmgr: " << keepAliveStatus.error_message();
                    LDEBUG << "Stopping client-rasmgr keep alive thread.";
                    this->isRasmgrKeepAliveRunning = false;
                }
            }
        }
        catch (std::exception &ex)
        {
            this->isRasmgrKeepAliveRunning = false;

            LERROR << "Rasmgr Keep Alive thread has failed";
            LERROR << ex.what();
        }
        catch (...)
        {
            this->isRasmgrKeepAliveRunning = false;

            LERROR << "Rasmgr Keep Alive thread failed for unknown reason.";
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
        LERROR << ex.what();
    }
    catch (...)
    {
        LERROR << "Stoping rasmgr keep alive has failed";
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
                ::rasnet::service::KeepAliveRequest keepAliveReq;
                Void keepAliveRepl;

                keepAliveReq.set_client_uuid(this->remoteClientUUID);
                keepAliveReq.set_session_id(this->sessionId);

                grpc::ClientContext context;

                // We do not want this thread to block forever
                system_clock::time_point deadline = system_clock::now() + milliseconds(SERVICE_CALL_TIMEOUT);
                context.set_deadline(deadline);

                grpc::Status keepAliveStatus = this->getRasServerService(false)->KeepAlive(&context, keepAliveReq, &keepAliveRepl);

                if (!keepAliveStatus.ok())
                {
                    LERROR << "Failed to send keep alive message to rasserver: " << keepAliveStatus.error_message();
                    LDEBUG << "Stopping client-rasserver keep alive thread.";
                    this->isRasserverKeepAliveRunning = false;
                }
            }
        }
        catch (std::exception &ex)
        {
            this->isRasserverKeepAliveRunning = false;

            LERROR << "RasServer Keep Alive thread has failed";
            LERROR << ex.what();
        }
        catch (...)
        {
            this->isRasserverKeepAliveRunning = false;
            LERROR << "RasServer Keep Alive thread failed for unknown reason.";
        }
    }

}
/* END: KEEP ALIVE */
