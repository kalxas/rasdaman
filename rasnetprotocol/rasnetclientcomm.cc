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

#include "../rasodmg/transaction.hh"
#include "../rasodmg/database.hh"
#include "../rasodmg/iterator.hh"
#include "../rasodmg/set.hh"
#include "../rasodmg/ref.hh"
#include "../rasodmg/storagelayout.hh"
#include "../rasodmg/tiling.hh"

#include "../raslib/minterval.hh"
#include "../raslib/rmdebug.hh"
#include "../raslib/rminit.hh"
#include "../raslib/primitivetype.hh"
#include "../raslib/complextype.hh"
#include "../raslib/structuretype.hh"
#include "../raslib/primitive.hh"
#include "../raslib/complex.hh"
#include "../raslib/structure.hh"
#include "../raslib/parseparams.hh"
#include "../mymalloc/mymalloc.h"

#include "../debug/debug.hh"

#include "common/src/crypto/crypto.hh"
#include "common/src/uuid/uuid.hh"
#include "rasnet/src/util/proto/protozmq.hh"
#include "common/src/logging/easylogging++.hh"
#include "globals.hh"
#include "rasnet/src/util/proto/zmqutil.hh"

#include <cstring>
#include <fstream>

using boost::scoped_ptr;
using boost::shared_mutex;
using boost::unique_lock;
using google::protobuf::DoNothing;
using google::protobuf::NewPermanentCallback;
using rasnet::Channel;
using rasnet::service::OpenServerDatabaseReq;
using rasnet::service::OpenServerDatabaseRepl;
using rasnet::service::CloseServerDatabaseReq;
using rasnet::service::AbortTransactionRepl;
using rasnet::service::ClientIdentity;
using rasnet::service::AbortTransactionReq;
using rasnet::service::BeginTransactionRepl;
using rasnet::service::BeginTransactionReq;
using rasnet::service::ClientRassrvrService_Stub;
using rasnet::service::RasMgrClientService_Stub;
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
using rasnet::service::ClientIdentity;
using rasnet::service::KeepAliveReq;
using rasnet::service::KeepAliveRequest;
using std::string;
using boost::thread;
using rasnet::ProtoZmq;
using rasnet::InternalDisconnectReply;
using rasnet::InternalDisconnectRequest;
using common::UUID;
using rasnet::ZmqUtil;

RasnetClientComm::RasnetClientComm(string rasmgrHost, int rasmgrPort)
{
    this->clientId = -1;

    clientParams = new r_Parse_Params();

    this->rasmgrHost = ZmqUtil::toTcpAddress(rasmgrHost);
    this->rasmgrPort = rasmgrPort;

    doNothing.reset(NewPermanentCallback(&DoNothing));

    this->initializedRasMgrService = false;
    this->initializedRasServerService = false;
}

RasnetClientComm::~RasnetClientComm() throw()
{
    closeRasmgrService();
    closeRasserverService();
}

int RasnetClientComm::connectClient(string userName, string passwordHash)
{
    ConnectReq connectReq;
    ConnectRepl connectRepl;

    connectReq.set_username(userName);
    connectReq.set_passwordhash(passwordHash);

    this->getRasMgrService()->Connect(&clientController, &connectReq, &connectRepl, doNothing.get());

    if (clientController.Failed())
    {
        throw r_Error(r_Error::r_Error_AccesDenied, INCORRECT_USER_PASSWORD);
    }

    this->clientId = connectRepl.clientid();
    this->clientUUID = connectRepl.clientuuid();

    // Send keep alive messages to rasmgr until openDB is called
    this->keepAliveTimeout = connectRepl.keepalivetimeout();
    this->startRasMgrKeepAlive();

    return 0;
}

int RasnetClientComm::disconnectClient()
{
    DisconnectReq disconnectReq;
    Void disconnectRepl;

    disconnectReq.set_clientuuid(this->clientUUID);

    this->getRasMgrService()->Disconnect(&clientController, &disconnectReq, &disconnectRepl, doNothing.get());
    this->closeRasmgrService();

    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    return 0;
}

int RasnetClientComm::openDB(const char *database)
{
    int retval = 0;

    OpenDbReq openDatabaseReq;
    OpenDbRepl openDatabaseRepl;

    openDatabaseReq.set_clientid(this->clientId);
    openDatabaseReq.set_clientuuid(this->clientUUID.c_str());
    openDatabaseReq.set_databasename(database);

    this->getRasMgrService()->OpenDb(&clientController, &openDatabaseReq, &openDatabaseRepl, doNothing.get());

    if (clientController.Failed())
    {
        throw r_Error(r_Error::r_Error_DatabaseOpen);
    }

    this->rasServerHost = openDatabaseRepl.serverhostname();
    this->rasServerPort = openDatabaseRepl.port();
    this->sessionId = openDatabaseRepl.dbsessionid();

    OpenServerDatabaseReq openServerDatabaseReq;
    OpenServerDatabaseRepl openServerDatabaseRepl;

    openServerDatabaseReq.set_client_id(this->clientId);
    openServerDatabaseReq.set_database_name(database);

    this->getRasServerService()->OpenServerDatabase(&clientController, &openServerDatabaseReq, &openServerDatabaseRepl, doNothing.get());
    //stop sending keep alive messages to rasmgr
    this->stopRasMgrKeepAlive();

    if (clientController.Failed())
    {
        throw r_Error(r_Error::r_Error_DatabaseOpen);
    }
    // Send keep alive messages to rasserver until openDB is called
    this->startRasServerKeepAlive();

    return retval;
}

int RasnetClientComm::closeDB()
{
    int retval = 0;

    CloseServerDatabaseReq closeServerDatabaseReq;
    CloseDbReq closeDbReq;
    Void closeDatabaseRepl;

    closeServerDatabaseReq.set_client_id(this->clientId);
    closeDbReq.set_clientid(this->clientId);
    closeDbReq.set_clientuuid(this->clientUUID);
    closeDbReq.set_dbsessionid(this->sessionId);

    this->getRasServerService()->CloseServerDatabase(&clientController, &closeServerDatabaseReq, &closeDatabaseRepl, doNothing.get());
    this->getRasMgrService()->CloseDb(&clientController, &closeDbReq, &closeDatabaseRepl, doNothing.get());

    this->stopRasServerKeepAlive();

    disconnectClient();

    this->closeRasserverService();

    if (clientController.Failed())
    {
        throw r_Error(r_Error::r_Error_DatabaseClosed);
    }

    return retval;
}

int RasnetClientComm::createDB(const char *name) throw (r_Error)
{

}

int RasnetClientComm::destroyDB(const char *name) throw (r_Error)
{

}

int RasnetClientComm::openTA(unsigned short readOnly) throw (r_Error)
{
    int retval = 1;

    BeginTransactionReq beginTransactionReq;
    BeginTransactionRepl beginTransactionRepl;

    beginTransactionReq.set_rw(readOnly == 0);
    beginTransactionReq.set_client_id(this->clientId);

    this->getRasServerService()->BeginTransaction(&clientController, &beginTransactionReq, &beginTransactionRepl, doNothing.get());
    if (clientController.Failed())
    {
        throw r_Error(r_Error::r_Error_TransactionOpen);
    }

    return retval;
}

int RasnetClientComm::commitTA() throw (r_Error)
{
    int retval = 1;

    CommitTransactionReq commitTransactionReq;
    CommitTransactionRepl commitTransactionRepl;

    commitTransactionReq.set_client_id(this->clientId);

    this->getRasServerService()->CommitTransaction(&clientController, &commitTransactionReq, &commitTransactionRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    return retval;
}

int RasnetClientComm::abortTA()
{
    AbortTransactionReq abortTransactionReq;
    AbortTransactionRepl AbortTransactionRepl;

    abortTransactionReq.set_client_id(this->clientId);

    this->getRasServerService()->AbortTransaction(&clientController, &abortTransactionReq, &AbortTransactionRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }
}

void RasnetClientComm::insertMDD(const char *collName, r_GMarray *mar) throw (r_Error)
{
    ENTER( "RnpClientComm::insertMDD(" << (collName?collName:"(null)") << "," << (long) mar << ")"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "insertMDD(" << collName << "," << (long) mar << ")"  );

    checkForRwTransaction();

    r_Minterval     spatdom;
    r_Bytes         marBytes;
    RPCMarray*      rpcMarray;
    r_Bytes         tileSize = 0;

    // get the spatial domain of the r_GMarray
    spatdom = mar->spatial_domain();

    // determine the amount of data to be transferred
    marBytes = mar->get_array_size();

    const r_Base_Type* baseType = mar->get_base_type_schema();

    // if the MDD is too large for being transfered as one block, it has to be
    // divided in tiles
    const r_Tiling* til = mar->get_storage_layout()->get_tiling();
    r_Tiling_Scheme scheme = til->get_tiling_scheme();
    if (scheme == r_NoTiling)
        tileSize = RMInit::RMInit::clientTileSize;
    else
        //allowed because the only subclass of tiling without size is no tiling
        tileSize = ((const r_Size_Tiling*)til)->get_tile_size();


    // initiate composition of MDD at server side
    int status = executeStartInsertPersMDD(collName, mar);  //rpcStatusPtr = rpcstartinsertpersmdd_1( params, binding_h );

    switch( status )
    {
    case 0:
        break; // OK
    case 2:
        LEAVE( "RnpClientComm::insertMDD() Error: database class undefined." );
        throw r_Error( r_Error::r_Error_DatabaseClassUndefined );
        break;
    case 3:
        LEAVE( "RnpClientComm::insertMDD() Error: collection element type mismatch." );
        throw r_Error( r_Error::r_Error_CollectionElementTypeMismatch );
        break;
    case 4:
        LEAVE( "RnpClientComm::insertMDD() Error: type invalid." );
        throw r_Error( r_Error::r_Error_TypeInvalid );
        break;
    default:
        LEAVE( "RnpClientComm::insertMDD() Error: transfer invalid." );
        throw r_Error( r_Error::r_Error_TransferFailed );
        break;
    }

    r_Set< r_GMarray* >* bagOfTiles;

    bagOfTiles = mar->get_storage_layout()->decomposeMDD( mar );

    RMDBGMIDDLE(2, RMDebug::module_clientcomm, "RnpClientComm", "decomposing into " << bagOfTiles->cardinality() << " tiles")

            r_Iterator< r_GMarray* > iter = bagOfTiles->create_iterator();
    r_GMarray *origTile;

    for(iter.reset(); iter.not_done(); iter.advance() )
    {
        origTile = *iter;

        RMDBGMIDDLE(2, RMDebug::module_clientcomm, "RnpClientComm", "inserting Tile with domain " << origTile->spatial_domain() << ", " << origTile->spatial_domain().cell_count() * origTile->get_type_length() << " bytes")

                getMarRpcRepresentation( origTile, rpcMarray, mar->get_storage_layout()->get_storage_format(), baseType );

        status = executeInsertTile(true, rpcMarray);

        // free rpcMarray structure (rpcMarray->data.confarray_val is freed somewhere else)
        freeMarRpcRepresentation( origTile, rpcMarray );

        // delete current tile (including data block)
        delete origTile;

        if( status > 0 )
        {
            throw r_Error( r_Error::r_Error_TransferFailed );
        }
    }

    executeEndInsertMDD(true); //rpcendinsertmdd_1( params3, binding_h );

    // delete transient data
    bagOfTiles->remove_all();
    delete bagOfTiles;



    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "insertMDD()" );
    LEAVE( "RnpClientComm::insertMDD()"  );

}

r_Ref_Any RasnetClientComm::getMDDByOId(const r_OId &oid) throw (r_Error)
{
    ENTER( "RnpClientComm::getMDDByOId(" << oid << ")"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "getMDDByOId(" << oid << ")"  );

    RMInit::logOut << "Internal error: RnpClientComm::getMDDByOId() not implemented, returning empty r_Ref_Any()." << endl;

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "getMDDByOId()" );
    LEAVE( "RnpClientComm::getMDDByOId()"  );
    return r_Ref_Any();
}

void RasnetClientComm::insertColl(const char *collName, const char *typeName, const r_OId &oid) throw (r_Error)
{
    ENTER( "RnpClientComm::insertColl(" << collName << "," << typeName << "," << oid << ")"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "insertColl(" << collName << "," << typeName << "," << oid << ")"  );

    checkForRwTransaction();

    InsertCollectionReq insertCollectionReq;
    InsertCollectionRepl  insertCollectionRepl;

    insertCollectionReq.set_client_id(this->clientId);
    insertCollectionReq.set_collection_name(collName);
    insertCollectionReq.set_type_name(typeName);
    insertCollectionReq.set_oid(oid.get_string_representation());

    this->getRasServerService()->InsertCollection(&clientController, &insertCollectionReq, &insertCollectionRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    int status = insertCollectionRepl.status();

    handleStatusCode(status, "insertColl");

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "insertColl()" );
    LEAVE( "RnpClientComm::insertColl()"  );
}

void RasnetClientComm::deleteCollByName(const char *collName) throw (r_Error)
{
    ENTER( "RnpClientComm::deleteCollByName(" << (collName?collName:"(null)") << ")"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "deleteCollByName(" << collName << ")"  );

    checkForRwTransaction();

    DeleteCollectionByNameReq deleteCollectionByNameReq;
    DeleteCollectionByNameRepl deleteCollectionByNameRepl;

    deleteCollectionByNameReq.set_client_id(this->clientId);
    deleteCollectionByNameReq.set_collection_name(collName);

    this->getRasServerService()->DeleteCollectionByName(&clientController, &deleteCollectionByNameReq, &deleteCollectionByNameRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    handleStatusCode(deleteCollectionByNameRepl.status(), "deleteCollByName");
    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "deleteCollByName()" );
    LEAVE( "RnpClientComm::deleteCollByName()"  );
}

void RasnetClientComm::deleteObjByOId(const r_OId &oid) throw (r_Error)
{
    ENTER( "RnpClientComm::deleteCollByName(" << (collName?collName:"(null)") << ")"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "deleteCollByName(" << collName << ")"  );

    checkForRwTransaction();

    DeleteCollectionByOidReq deleteCollectionByOidReq;
    DeleteCollectionByOidRepl deleteCollectionByOidRepl;

    deleteCollectionByOidReq.set_client_id(this->clientId);
    deleteCollectionByOidReq.set_oid(oid.get_string_representation());

    this->getRasServerService()->DeleteCollectionByOid(&clientController, &deleteCollectionByOidReq, &deleteCollectionByOidRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    handleStatusCode(deleteCollectionByOidRepl.status(), "deleteCollByName");
    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "deleteCollByName()" );
    LEAVE( "RnpClientComm::deleteCollByName()"  );
}

void RasnetClientComm::removeObjFromColl(const char *name, const r_OId &oid) throw (r_Error)
{
    ENTER( "RnpClientComm::removeObjFromColl(" << (collName?collName:"(null)") << "," << oid << ")"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "removeObjFromColl(" << collName << "," << oid << ")"  );

    checkForRwTransaction();

    RemoveObjectFromCollectionReq removeObjectFromCollectionReq;
    RemoveObjectFromCollectionRepl removeObjectFromCollectionRepl;

    removeObjectFromCollectionReq.set_client_id(this->clientId);
    removeObjectFromCollectionReq.set_collection_name(name);
    removeObjectFromCollectionReq.set_oid(oid.get_string_representation());

    this->getRasServerService()->RemoveObjectFromCollection(&clientController, &removeObjectFromCollectionReq, &removeObjectFromCollectionRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    int status = removeObjectFromCollectionRepl.status();
    handleStatusCode(status, "removeObjFromColl");

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "removeObjFromColl()" );
    LEAVE( "RnpClientComm::removeObjFromColl()"  );
}

r_Ref_Any RasnetClientComm::getCollByName(const char *name) throw (r_Error)
{
    ENTER( "RnpClientComm::getCollByName(" << (collName?collName:"(null)") << ")"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "getCollByName(" << collName << ")"  );

    r_Ref_Any result = executeGetCollByNameOrOId ( name, r_OId() );

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "getCollByName()" );
    LEAVE( "RnpClientComm::getCollByName()"  );
    return result;
}

r_Ref_Any RasnetClientComm::getCollByOId(const r_OId &oid) throw (r_Error)
{
    ENTER( "RnpClientComm::getCollByOId(" << oid << ")"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "getCollByOId(" << oid << ")"  );

    r_Ref_Any result = executeGetCollByNameOrOId ( NULL, oid );

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "getCollByOId()" );
    LEAVE( "RnpClientComm::getCollByOId()"  );
    return result;
}

r_Ref_Any RasnetClientComm::getCollOIdsByName(const char *name) throw (r_Error)
{
    ENTER( "RnpClientComm::getCollOIdsByName(" << (name?name:"(null)") << ")"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "getCollOIdsByName(" << name << ")"  );

    r_Ref_Any result = executeGetCollOIdsByNameOrOId ( name, r_OId() );

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "getCollOIdsByName()" );
    LEAVE( "RnpClientComm::getCollOIdsByName()"  );
    return result;
}

r_Ref_Any RasnetClientComm::getCollOIdsByOId(const r_OId &oid) throw (r_Error)
{
    ENTER( "RnpClientComm::getCollOIdsByOId(" << oid << ")"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "getCollOIdsByOId(" << oid << ")"  );

    r_Ref_Any result = executeGetCollOIdsByNameOrOId ( NULL, oid );

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "getCollOIdsByOId()" );
    LEAVE( "RnpClientComm::getCollOIdsByOId()"  );
    return result;
}

void RasnetClientComm::executeQuery(const r_OQL_Query &query, r_Set<r_Ref_Any> &result) throw (r_Error)
{
    ENTER( "RnpClientComm::executeQuery(_,_)"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "executeQuery(_,_)"  );

    sendMDDConstants(query);
    int status = executeExecuteQuery( query.get_query(), result );

    switch(status)
    {
    case 0:
        getMDDCollection( result, 1 );
        break; // 1== isQuery
    case 1:
        getElementCollection( result );
        break;
        //case 2:  nothing
    default:
        RMInit::logOut << "Internal error: RnpClientComm::executeQuery(): illegal status value " << status << endl;
    }

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "executeQuery()" );
    LEAVE( "RnpClientComm::executeQuery()"  );
}

void RasnetClientComm::executeQuery(const r_OQL_Query &query) throw (r_Error)
{
    ENTER( "RnpClientComm::executeQuery(_)" );

    checkForRwTransaction();

    sendMDDConstants(query);

    executeExecuteUpdateQuery(query.get_query());
    LEAVE( "RnpClientComm::executeQuery(_)" );
}

void RasnetClientComm::executeQuery(const r_OQL_Query &query, r_Set<r_Ref_Any> &result, int dummy) throw (r_Error)
{
    ENTER( "RnpClientComm::executeQuery(_,_,_)" );

    checkForRwTransaction();

    sendMDDConstants(query);

    int status = executeExecuteUpdateQuery(query.get_query(), result);

    TALK("executeUpdateQuery (retrieval) returns " << status );

    switch(status)
    {
    case 0:
        getMDDCollection( result, 1 );
        break; // 1== isQuery
    case 1:
        getElementCollection( result );
        break;
        // case 2:  nothing, should not be error?
    default:
        RMInit::logOut << "Internal error: RnpClientComm::executeQuery(): illegal status value " << status << endl;
    }

    LEAVE( "RnpClientComm::executeQuery(_)" );

}

r_OId RasnetClientComm::getNewOId(unsigned short objType) throw (r_Error)
{
    GetNewOidReq getNewOidReq;
    GetNewOidRepl getNewOidRepl;

    getNewOidReq.set_client_id(this->clientId);
    getNewOidReq.set_object_type(objType);

    this->getRasServerService()->GetNewOid(&clientController, &getNewOidReq, &getNewOidRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    r_OId oid(getNewOidRepl.oid().c_str());
    return oid;
}

unsigned short RasnetClientComm::getObjectType(const r_OId &oid) throw (r_Error)
{
    GetObjectTypeReq getObjectTypeReq;
    GetObjectTypeRepl getObjectTypeRepl;

    getObjectTypeReq.set_client_id(this->clientId);
    getObjectTypeReq.set_oid(oid.get_string_representation());

    this->getRasServerService()->GetObjectType(&clientController, &getObjectTypeReq, &getObjectTypeRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    int status = getObjectTypeRepl.status();
    handleStatusCode(status, "getObjectType");

    unsigned short objectType = getObjectTypeRepl.object_type();
    return objectType;
}

char* RasnetClientComm::getTypeStructure(const char *typeName, r_Type_Type typeType) throw (r_Error)
{
    GetTypeStructureReq getTypeStructureReq;
    GetTypeStructureRepl getTypeStructureRepl;

    getTypeStructureReq.set_client_id(this->clientId);
    getTypeStructureReq.set_type_name(typeName);
    getTypeStructureReq.set_type_type(typeType);

    this->getRasServerService()->GetTypeStructure(&clientController, &getTypeStructureReq, &getTypeStructureRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    int status = getTypeStructureRepl.status();
    handleStatusCode(status, "getTypeStructure");

    char* typeStructure = strdup(getTypeStructureRepl.type_structure().c_str());
    return typeStructure;
}

int RasnetClientComm::setTransferFormat(r_Data_Format format, const char *formatParams)
{
    ENTER( "RnpClientComm::setStorageFormat( format=" << format << ", formatParams=" << (formatParams?formatParams:"(null)") << " )" );

    storageFormat = format;

    if (storageFormatParams != NULL)
    {
        free(storageFormatParams);
        storageFormatParams = NULL;
    }
    if (formatParams != NULL)
    {
        storageFormatParams = (char*)mymalloc(strlen(formatParams) + 1);
        strcpy(storageFormatParams, formatParams);
        // extract ``compserver'' if present
        clientParams->process(storageFormatParams);
    }

    int result = executeSetFormat( false, format, formatParams);

    LEAVE( "RnpClientComm::setStorageFormat() -> " << result );
    return result;
}

int RasnetClientComm::setStorageFormat(r_Data_Format format, const char *formatParams)
{
    ENTER( "RnpClientComm::setTransferFormat( format=" << format << ", formatParams=" << (formatParams?formatParams:"(null)") << " )" );

    transferFormat = format;

    if (transferFormatParams != NULL)
    {
        free(transferFormatParams);
        transferFormatParams = NULL;
    }
    if (formatParams != NULL)
    {
        transferFormatParams = (char*)mymalloc(strlen(formatParams)+1);
        strcpy(transferFormatParams, formatParams);
        // extract ``exactformat'' if present
        clientParams->process(transferFormatParams);
    }

    int result = executeSetFormat( true, format, formatParams);
    LEAVE( "RnpClientComm::setTransferFormat() -> " << result );
    return result;
}

boost::shared_ptr<rasnet::service::ClientRassrvrService> RasnetClientComm::getRasServerService()
{
    unique_lock<shared_mutex> lock(this->rasServerServiceMtx);
    if (!this->initializedRasServerService)
    {
        this->rasserverChannel.reset(new Channel(rasServerHost, rasServerPort));
        this->rasserverService.reset(new ClientRassrvrService_Stub(rasserverChannel.get()));
        this->initializedRasServerService = true;
    }

    return this->rasserverService;
}

void RasnetClientComm::closeRasserverService()
{
    unique_lock<shared_mutex> lock(this->rasServerServiceMtx);
    if (this->initializedRasServerService)
    {
        this->rasserverService.reset();
        this->rasserverChannel.reset();
        this->initializedRasServerService = false;
    }
}

boost::shared_ptr<rasnet::service::RasMgrClientService> RasnetClientComm::getRasMgrService()
{
    unique_lock<shared_mutex> lock(this->rasMgrServiceMtx);
    if (!this->initializedRasMgrService)
    {
        this->rasmgrChannel.reset(new Channel(rasmgrHost, rasmgrPort));
        this->rasmgrService.reset(new RasMgrClientService_Stub(rasmgrChannel.get()));
        this->initializedRasMgrService = true;
    }

    return this->rasmgrService;
}

void RasnetClientComm::closeRasmgrService()
{
    unique_lock<shared_mutex> lock(this->rasMgrServiceMtx);
    if (this->initializedRasMgrService)
    {
        this->rasmgrService.reset();
        this->rasmgrChannel.reset();
        this->initializedRasMgrService = false;
    }
}


int RasnetClientComm::executeStartInsertPersMDD(const char *collName, r_GMarray *mar)
{
    StartInsertMDDReq startInsertMDDReq;
    StartInsertMDDRepl startInsertMDDRepl;

    startInsertMDDReq.set_client_id(this->clientId);
    startInsertMDDReq.set_collname(collName);
    startInsertMDDReq.set_domain(mar->spatial_domain().get_string_representation());
    startInsertMDDReq.set_type_length(mar->get_type_length());
    startInsertMDDReq.set_type_name(mar->get_type_name());
    startInsertMDDReq.set_oid(mar->get_oid().get_string_representation());

    this->getRasServerService()->StartInsertMDD(&clientController, &startInsertMDDReq, &startInsertMDDRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
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
    insertTileReq.set_data_length(tile->data.confarray_len);

    this->getRasServerService()->InsertTile(&clientController, &insertTileReq, &insertTileRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    return insertTileRepl.status();
}

void RasnetClientComm::executeEndInsertMDD(bool persistent)
{
    EndInsertMDDReq endInsertMDDReq;
    EndInsertMDDRepl endInsertMDDRepl;

    endInsertMDDReq.set_client_id(this->clientId);
    endInsertMDDReq.set_persistent(persistent);

    this->getRasServerService()->EndInsertMDD(&clientController, &endInsertMDDReq, &endInsertMDDRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    handleStatusCode(endInsertMDDRepl.status(), "executeEndInsertMDD");
}

void RasnetClientComm::getMDDCollection(r_Set<r_Ref_Any> &mddColl, unsigned int isQuery) throw (r_Error)
{
    ENTER( "RnpClientComm::getMDDCollection(_," << isQuery << ")"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "getMDDCollection(_," << isQuery << ")"  );

    unsigned short tileStatus=0;
    unsigned short mddStatus = 0;

    while( mddStatus == 0 ) // repeat until all MDDs are transferred
    {
        r_Ref<r_GMarray> mddResult;

        // Get spatial domain of next MDD
        GetMDDRes* thisResult = executeGetNextMDD();

        mddStatus = thisResult->status;

        if( mddStatus == 2 )
        {
            RMInit::logOut << "Error: getMDDCollection(...) - no transfer collection or empty transfer collection" << endl;
            LEAVE( "RnpClientComm::getMDDCollection(): exception, status = " << mddStatus );
            throw r_Error( r_Error::r_Error_TransferFailed );
        }

        tileStatus = getMDDCore(mddResult, thisResult, isQuery);

        // finally, insert the r_Marray into the set

        mddColl.insert_element( mddResult, 1 );

        free(thisResult->domain);
        free(thisResult->typeName);
        free(thisResult->typeStructure);
        free(thisResult->oid);
        delete   thisResult;

        if( tileStatus == 0 ) // if this is true, we're done with this collection
            break;

    } // end while( mddStatus == 0 )

    executeEndTransfer();

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "getMDDCollection()" );
    LEAVE( "RnpClientComm::getMDDCollection()"  );
}

int RasnetClientComm::executeEndTransfer()
{
    EndTransferReq endTransferReq;
    EndTransferRepl endTransferRepl;

    endTransferReq.set_client_id(this->clientId);

    this->getRasServerService()->EndTransfer(&clientController, &endTransferReq, &endTransferRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    return endTransferRepl.status();
}

GetMDDRes* RasnetClientComm::executeGetNextMDD()
{
    GetNextMDDReq getNextMDDReq;
    GetNextMDDRepl getNextMDDRepl;

    getNextMDDReq.set_client_id(this->clientId);

    this->getRasServerService()->GetNextMDD(&clientController, &getNextMDDReq, &getNextMDDRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    GetMDDRes* result = new GetMDDRes();
    result->status = getNextMDDRepl.status();
    result->domain = strdup(getNextMDDRepl.domain().c_str());
    result->typeName = strdup(getNextMDDRepl.type_name().c_str());
    result->typeStructure = strdup(getNextMDDRepl.type_structure().c_str());
    result->oid = strdup(getNextMDDRepl.oid().c_str());
    result->currentFormat = getNextMDDRepl.current_format();

    return result;
}

unsigned short RasnetClientComm::getMDDCore(r_Ref<r_GMarray> &mdd, GetMDDRes *thisResult, unsigned int isQuery) throw (r_Error)
{
    ENTER( "RnpClientComm::getMDDCore(_,_," << isQuery << ")"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "getMDDCore(_,_," << isQuery << ")"  );

    //  create r_Minterval and oid
    r_Minterval mddDomain( thisResult->domain );
    r_OId       rOId     ( thisResult->oid );
    r_GMarray  *marray;

    if( isQuery )
        marray = new( r_Database::actual_database, r_Object::transient, rOId ) r_GMarray();
    else
        marray = new( r_Database::actual_database, r_Object::read     , rOId ) r_GMarray();

    marray->set_spatial_domain( mddDomain );
    marray->set_type_by_name  ( thisResult->typeName );
    marray->set_type_structure( thisResult->typeStructure );

    r_Data_Format currentFormat = (r_Data_Format)(thisResult->currentFormat);
    //    currentFormat = r_Array;
    marray->set_current_format( currentFormat );

    r_Data_Format decompFormat;

    const r_Base_Type *baseType = marray->get_base_type_schema();

    // Variables needed for tile transfer
    GetTileRes* tileRes=0;
    unsigned short  mddDim = mddDomain.dimension();  // we assume that each tile has the same dimensionality as the MDD
    r_Minterval     tileDomain;
    r_GMarray*      tile;  // for temporary tile
    char*           memCopy;
    unsigned long   memCopyLen;
    int             tileCntr = 0;
    unsigned short  tileStatus   = 0;

    tileStatus = 2; // call rpcgetnexttile_1 at least once

    while( tileStatus == 2 || tileStatus == 3 )  // while( for all tiles of the current MDD )
    {
        tileRes = executeGetNextTile();

        tileStatus = tileRes->status;

        if( tileStatus == 4 )
        {
            freeGetTileRes(tileRes);
            RMInit::logOut << "Error: rpcGetNextTile(...) - no tile to transfer or empty transfer collection" << endl;
            LEAVE( "RnpClientComm::getMDDCore(): exception, status = " << tileStatus );
            throw r_Error( r_Error::r_Error_TransferFailed );
        }

        // take cellTypeLength for current MDD of the first tile
        if( tileCntr == 0 )
            marray->set_type_length( tileRes->marray->cellTypeLength );

        tileDomain = r_Minterval( tileRes->marray->domain );
        memCopyLen = tileDomain.cell_count() * marray->get_type_length(); // cell type length of the tile must be the same
        if (memCopyLen < tileRes->marray->data.confarray_len)
            memCopyLen = tileRes->marray->data.confarray_len;   // may happen when compression expands
        memCopy    = new char[ memCopyLen ];

        // create temporary tile
        tile = new r_GMarray();
        tile->set_spatial_domain( tileDomain );
        tile->set_array( memCopy );
        tile->set_array_size( memCopyLen );
        tile->set_type_length( tileRes->marray->cellTypeLength );
        tileCntr++;

        // Variables needed for block transfer of a tile
        unsigned long  blockOffset = 0;
        unsigned short subStatus  = 3;
        currentFormat = (r_Data_Format)(tileRes->marray->currentFormat);

        switch( tileStatus )
        {
        case 3: // at least one block of the tile is left

            // Tile arrives in several blocks -> put them together
            concatArrayData(tileRes->marray->data.confarray_val, tileRes->marray->data.confarray_len, memCopy, memCopyLen, blockOffset);
            freeGetTileRes(tileRes);

            tileRes = executeGetNextTile();//rpcgetnexttile_1( &clientID, binding_h );

            subStatus = tileRes->status;

            if( subStatus == 4 )
            {
                freeGetTileRes(tileRes);
                LEAVE( "RnpClientComm::getMDDCore(): exception, status = " << tileStatus << ", subStatus = " << subStatus );
                throw r_Error( r_Error::r_Error_TransferFailed );
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

        char* marrayData = NULL;
        // Now the tile is transferred completely, insert it into current MDD
        if( tileStatus < 2 && tileCntr == 1 && (tile->spatial_domain() == marray->spatial_domain()))
        {
            // MDD consists of just one tile that is the same size of the mdd

            // simply take the data memory of the tile
            marray->set_array( tile->get_array() );
            marray->set_array_size( tile->get_array_size() );
            tile->set_array( 0 );
        }
        else
        {
            // MDD consists of more than one tile or the tile does not cover the whole domain

            r_Bytes size = mddDomain.cell_count() * marray->get_type_length();

            if( tileCntr == 1 )
            {
                // allocate memory for the MDD
                marrayData = new char[ size ];
                memset(marrayData, 0, size);

                marray->set_array( marrayData );
            }
            else
                marrayData = marray->get_array();


            // copy tile data into MDD data space (optimized, relying on the internal representation of an MDD )
            char*         mddBlockPtr;
            char*         tileBlockPtr = tile->get_array();
            unsigned long blockCells   = tileDomain[tileDomain.dimension()-1].high()-tileDomain[tileDomain.dimension()-1].low()+1;
            unsigned long blockSize    = blockCells * marray->get_type_length();
            unsigned long blockNo      = tileDomain.cell_count() / blockCells;

            for( unsigned long blockCtr = 0; blockCtr < blockNo; blockCtr++ )
            {
                mddBlockPtr = marrayData + marray->get_type_length()*mddDomain.cell_offset( tileDomain.cell_point( blockCtr * blockCells ) );
                memcpy( (void*)mddBlockPtr, (void*)tileBlockPtr, (size_t)blockSize );
                tileBlockPtr += blockSize;
            }

            // former non-optimized version
            // for( i=0; i<tileDomain->cell_count(); i++ )
            //   (*marray)[tileDomain->cell_point( i )] = (*tile)[tileDomain->cell_point( i )];

            marray->set_array_size( size );
        }

        // delete temporary tile
        delete tile;

    }  // end while( MDD is not transferred completely )


    mdd = r_Ref<r_GMarray>( marray->get_oid(), marray );

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "getMDDCore() -> " << tileStatus );
    LEAVE( "RnpClientComm::getMDDCore() -> " << tileStatus );
    return tileStatus;
}

GetTileRes* RasnetClientComm::executeGetNextTile()
{
    GetNextTileReq getNextTileReq;
    GetNextTileRepl getNextTileRepl;

    getNextTileReq.set_client_id(this->clientId);

    this->getRasServerService()->GetNextTile(&clientController, &getNextTileReq, &getNextTileRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    GetTileRes* result = new GetTileRes();
    result->marray = new RPCMarray();

    result->status = getNextTileRepl.status();
    result->marray->domain = strdup(getNextTileRepl.domain().c_str());
    result->marray->cellTypeLength = getNextTileRepl.cell_type_length();
    result->marray->currentFormat = getNextTileRepl.current_format();
    result->marray->storageFormat = getNextTileRepl.storage_format();

    int length = getNextTileRepl.data_length();
    result->marray->data.confarray_len = length;
    result->marray->data.confarray_val = (char*) mymalloc(length);
    memcpy(result->marray->data.confarray_val, getNextTileRepl.data().c_str(), length);

    return result;
}

void RasnetClientComm::getMarRpcRepresentation(const r_GMarray *mar, RPCMarray *&rpcMarray, r_Data_Format initStorageFormat, const r_Base_Type *baseType)
{
    ENTER( "RnpClientComm::getMarRpcRepresentation(...)");
    RMDBGENTER(2, RMDebug::module_clientcomm, "RnpClientComm", "getMarRpcRepresentation(...)");

    // allocate memory for the RPCMarray data structure and assign its fields
    rpcMarray                 = (RPCMarray*)mymalloc( sizeof(RPCMarray) );
    rpcMarray->domain         = mar->spatial_domain().get_string_representation();
    rpcMarray->cellTypeLength = mar->get_type_length();
    rpcMarray->currentFormat = initStorageFormat;
    rpcMarray->data.confarray_len = mar->get_array_size();
    rpcMarray->data.confarray_val = (char*)(mar->get_array());
    rpcMarray->storageFormat = initStorageFormat;

    RMDBGEXIT(2, RMDebug::module_clientcomm, "RnpClientComm", "getMarRpcRepresentation(...)");
    LEAVE( "RnpClientComm::getMarRpcRepresentation()");
}


void RasnetClientComm::freeMarRpcRepresentation(const r_GMarray *mar, RPCMarray *rpcMarray)
{
    ENTER( "RnpClientComm::freeMarRpcRepresentation(_,_)" );

    if (rpcMarray->data.confarray_val != ((r_GMarray*)mar)->get_array())
    {
        delete[] rpcMarray->data.confarray_val;
    }
    free( rpcMarray->domain );
    free( rpcMarray );

    LEAVE( "RnpClientComm::freeMarRpcRepresentation()" );
}

int RasnetClientComm::concatArrayData( const char *source, unsigned long srcSize, char *&dest, unsigned long &destSize, unsigned long &destLevel )
{
    ENTER( "RnpClientComm::concatArrayData( 0x" << hex << (unsigned long) source << dec << "," << srcSize << ",_,_,_ )" );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "concatArrayData(" << source << "," << srcSize << ",_,_,_)" );

    if (destLevel + srcSize > destSize)
    {
        // need to extend dest
        unsigned long newSize = destLevel + srcSize;
        char *newArray;

        // allocate a little extra if we have to extend
        newSize = newSize + newSize / 16;

        //    RMDBGOUT( 1, "RnpClientComm::concatArrayData(): need to extend from " << destSize << " to " << newSize );

        if ((newArray = new char[newSize]) == NULL)
        {
            RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "concatArrayData() -> " << -1 );
            LEAVE( "RnpClientComm::concatArrayData() -> -1" );
            return -1;
        }

        memcpy(newArray, dest, destLevel);
        delete [] dest;
        dest = newArray;
        destSize = newSize;
    }

    memcpy(dest + destLevel, source, srcSize);
    destLevel += srcSize;

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "concatArrayData() -> " << 0 );
    LEAVE( "RnpClientComm::concatArrayData() -> 0" );
    return 0;
}

void RasnetClientComm::freeGetTileRes(GetTileRes *ptr)
{
    ENTER( "RnpClientComm::freeGetTileRes(_)"  );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "freeGetTileRes(_)"  );

    if(ptr->marray->domain)
        free(ptr->marray->domain);
    if(ptr->marray->data.confarray_val)
        free(ptr->marray->data.confarray_val);
    delete ptr->marray;
    delete ptr;

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "freeGetTileRes()" );
    LEAVE( "RnpClientComm::freeGetTileRes(_)"  );
}

r_Ref_Any RasnetClientComm::executeGetCollByNameOrOId(const char *collName, const r_OId &oid) throw( r_Error )
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

    this->getRasServerService()->GetCollectionByNameOrOid(&clientController, &getCollectionByNameOrOidReq, &getCollectionByNameOrOidRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    int status = getCollectionByNameOrOidRepl.status();
    handleStatusCode(status, "getCollByName");

    r_OId rOId(getCollectionByNameOrOidRepl.oid().c_str());
    r_Set< r_Ref_Any >* set  = new ( r_Database::actual_database, r_Object::read, rOId )  r_Set< r_Ref_Any >;

    set->set_type_by_name(getCollectionByNameOrOidRepl.type_name().c_str());
    set->set_type_structure(getCollectionByNameOrOidRepl.type_structure().c_str());
    set->set_object_name(getCollectionByNameOrOidRepl.collection_name().c_str());

    if( status == 0 )
        getMDDCollection( *set, 0 );
    //  else rpcStatus == 1 -> Result collection is empty and nothing has to be got.

    r_Ref_Any result = r_Ref_Any( set->get_oid(), set );
    LEAVE( "RnpClientComm::executeGetCollByNameOrOId() -> (result set not displayed)" );
    return result;
}

r_Ref_Any RasnetClientComm::executeGetCollOIdsByNameOrOId(const char *collName, const r_OId &oid) throw( r_Error )
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

    this->getRasServerService()->GetCollOidsByNameOrOid(&clientController, &getCollOidsByNameOrOidReq, &getCollOidsByNameOrOidRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    int status = getCollOidsByNameOrOidRepl.status();

    if (status != 0 && status != 1)
    {
        handleStatusCode(status, "executeGetCollOIdsByNameOrOId");
    }

    const char* typeName = getCollOidsByNameOrOidRepl.type_name().c_str();
    const char* typeStructure = getCollOidsByNameOrOidRepl.type_structure().c_str();
    const char* oidString = getCollOidsByNameOrOidRepl.oids_string().c_str();
    const char* collectionName = getCollOidsByNameOrOidRepl.collection_name().c_str();

    r_OId rOId(oidString);
    r_Set< r_Ref<r_GMarray> >* set = new ( r_Database::actual_database, r_Object::read, rOId )  r_Set< r_Ref< r_GMarray > >;

    set->set_type_by_name  ( typeName );
    set->set_type_structure( typeStructure );
    set->set_object_name   ( collectionName );

    for (int i = 0; i < getCollOidsByNameOrOidRepl.oid_set_size(); ++i)
    {
        r_OId roid(getCollOidsByNameOrOidRepl.oid_set(i).c_str());
        set->insert_element(r_Ref<r_GMarray>(roid), 1);
    }

    r_Ref_Any result = r_Ref_Any( set->get_oid(), set );
    return result;
}

void RasnetClientComm::sendMDDConstants( const r_OQL_Query& query ) throw( r_Error )
{
    unsigned short status;

    if( query.get_constants() )
    {
        r_Set< r_GMarray* >* mddConstants = (r_Set< r_GMarray* >*)query.get_constants();

        // in fact executeInitUpdate prepares server structures for MDD transfer
        if(executeInitUpdate() != 0)
        {
            LEAVE( "Error: RnpClientComm::sendMDDConstants(): MDD transaction initialization failed." );
            throw r_Error( r_Error::r_Error_TransferFailed );
        }

        r_Iterator<r_GMarray*> iter = mddConstants->create_iterator();

        for( iter.reset(); iter.not_done(); iter++ )
        {
            r_GMarray* mdd = *iter;

            const r_Base_Type* baseType = mdd->get_base_type_schema();

            if( mdd )
            {
                status = executeStartInsertTransMDD(mdd);
                switch( status )
                {
                case 0:
                    break; // OK
                case 2:
                    LEAVE( "RnpClientComm::sendMDDConstants(): exception, status = " << status );
                    throw r_Error( r_Error::r_Error_DatabaseClassUndefined );
                    break;
                case 3:
                    LEAVE( "RnpClientComm::sendMDDConstants(): exception, status = " << status );
                    throw r_Error( r_Error::r_Error_TypeInvalid );
                    break;
                default:
                    LEAVE( "RnpClientComm::sendMDDConstants(): exception, status = " << status );
                    throw r_Error( r_Error::r_Error_TransferFailed );
                    break;
                }


                r_Set< r_GMarray* >* bagOfTiles = NULL;

                if (mdd->get_array())
                {
                    bagOfTiles = mdd->get_storage_layout()->decomposeMDD( mdd );
                }
                else
                {
                    bagOfTiles = mdd->get_tiled_array();
                }

                r_Iterator< r_GMarray* > iter2 = bagOfTiles->create_iterator();

                for(iter2.reset(); iter2.not_done(); iter2.advance())
                {
                    RPCMarray* rpcMarray;

                    r_GMarray *origTile = *iter2;

                    getMarRpcRepresentation( origTile, rpcMarray, mdd->get_storage_layout()->get_storage_format(), baseType );

                    status = executeInsertTile(false, rpcMarray);

                    // free rpcMarray structure (rpcMarray->data.confarray_val is freed somewhere else)
                    freeMarRpcRepresentation( origTile, rpcMarray );

                    // delete current tile (including data block)
                    delete origTile;
                    origTile = NULL;

                    if( status > 0 )
                    {
                        LEAVE( "RnpClientComm::sendMDDConstants(): exception, status = " << status );
                        throw r_Error( r_Error::r_Error_TransferFailed );
                    }
                }

                bagOfTiles->remove_all();
                delete bagOfTiles;
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
    startInsertTransMDDReq.set_domain(mdd->spatial_domain().get_string_representation());
    startInsertTransMDDReq.set_type_length(mdd->get_type_length());
    startInsertTransMDDReq.set_type_name(mdd->get_type_name());

    this->getRasServerService()->StartInsertTransMDD(&clientController, &startInsertTransMDDReq, &startInsertTransMDDRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    return startInsertTransMDDRepl.status();
}

int RasnetClientComm::executeInitUpdate()
{
    InitUpdateReq initUpdateReq;
    InitUpdateRepl initUpdateRepl;

    initUpdateReq.set_client_id(this->clientId);
    this->getRasServerService()->InitUpdate(&clientController, &initUpdateReq, &initUpdateRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    return initUpdateRepl.status();
}


int RasnetClientComm::executeExecuteQuery(const char *query, r_Set<r_Ref_Any> &result) throw( r_Error )
{
    ExecuteQueryReq executeQueryReq;
    ExecuteQueryRepl executeQueryRepl;

    executeQueryReq.set_client_id(this->clientId);
    executeQueryReq.set_query(query);

    this->getRasServerService()->ExecuteQuery(&clientController, &executeQueryReq, &executeQueryRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    int status = executeQueryRepl.status();
    int errNo = executeQueryRepl.err_no();
    int lineNo = executeQueryRepl.line_no();
    int colNo = executeQueryRepl.col_no();
    const char* token = executeQueryRepl.token().c_str();
    const char* typeName = executeQueryRepl.type_name().c_str();
    const char* typeStructure = executeQueryRepl.type_structure().c_str();

    if(status == 0 || status == 1)
    {
        result.set_type_by_name( typeName );
        result.set_type_structure( typeStructure );
    }

    if( status == 4 || status == 5 )
    {
        r_Equery_execution_failed err( errNo, lineNo, colNo, token );
        LEAVE( "RnpClientComm::executeExecuteQuery() exception: status=" << status );
        throw err;
    }

    return status;
}

GetElementRes* RasnetClientComm::executeGetNextElement()
{
    GetNextElementReq getNextElementReq;
    GetNextElementRepl getNextElementRepl;

    getNextElementReq.set_client_id(this->clientId);

    this->getRasServerService()->GetNextElement(&clientController, &getNextElementReq, &getNextElementRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    GetElementRes* result = new GetElementRes();

    result->data.confarray_len = getNextElementRepl.data_length();
    result->data.confarray_val = new char[getNextElementRepl.data_length()];
    memcpy(result->data.confarray_val, getNextElementRepl.data().c_str(), getNextElementRepl.data_length());
    result->status = getNextElementRepl.status();

    return result;
}

void RasnetClientComm::getElementCollection( r_Set< r_Ref_Any >& resultColl ) throw(r_Error)
{
    ENTER( "RnpClientComm::getElementCollection()" );
    RMDBGENTER( 2, RMDebug::module_clientcomm, "RnpClientComm", "getElementCollection(_)" );

    unsigned short rpcStatus = 0;

    TALK( "got set of type " << resultColl.get_type_structure() );

    while( rpcStatus == 0 ) // repeat until all elements are transferred
    {
        GetElementRes* thisResult = executeGetNextElement();

        rpcStatus = thisResult->status;

        if( rpcStatus == 2 )
        {
            RMInit::logOut << "Error: getElementCollection(...) - no transfer collection or empty transfer collection" << endl;
            LEAVE( "RnpClientComm::getElementCollection(): exception: rpcStatus = " << rpcStatus );
            throw r_Error( r_Error::r_Error_TransferFailed );
        }
        // create new collection element, use type of collection resultColl
        r_Ref_Any     element;
        const r_Type* elementType = resultColl.get_element_type_schema();

        switch( elementType->type_id() )
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
            element = new r_Primitive( thisResult->data.confarray_val, (r_Primitive_Type*) elementType );
            r_Transaction::actual_transaction->add_object_list( r_Transaction::SCALAR, (void*) element );
            break;

        case r_Type::COMPLEXTYPE1:
        case r_Type::COMPLEXTYPE2:
            element = new r_Complex(thisResult->data.confarray_val, (r_Complex_Type *)elementType);
            r_Transaction::actual_transaction->add_object_list(r_Transaction::SCALAR, (void *)element);
            break;

        case r_Type::STRUCTURETYPE:
            element = new r_Structure( thisResult->data.confarray_val, (r_Structure_Type*) elementType );
            r_Transaction::actual_transaction->add_object_list( r_Transaction::SCALAR, (void*) element );
            break;

        case r_Type::POINTTYPE:
        {
            char* stringRep = new char[thisResult->data.confarray_len+1];
            strncpy( stringRep, thisResult->data.confarray_val, thisResult->data.confarray_len );
            stringRep[thisResult->data.confarray_len] = '\0';

            r_Point* typedElement = new r_Point( stringRep );
            element               = typedElement;
            r_Transaction::actual_transaction->add_object_list( r_Transaction::POINT, (void*) typedElement );
            delete [] stringRep;
        }
            break;

        case r_Type::SINTERVALTYPE:
        {
            char* stringRep = new char[thisResult->data.confarray_len+1];
            strncpy( stringRep, thisResult->data.confarray_val, thisResult->data.confarray_len );
            stringRep[thisResult->data.confarray_len] = '\0';

            r_Sinterval* typedElement = new r_Sinterval( stringRep );
            element                   = typedElement;
            r_Transaction::actual_transaction->add_object_list( r_Transaction::SINTERVAL, (void*) typedElement );
            delete [] stringRep;
        }
            break;

        case r_Type::MINTERVALTYPE:
        {
            char* stringRep = new char[thisResult->data.confarray_len+1];
            strncpy( stringRep, thisResult->data.confarray_val, thisResult->data.confarray_len );
            stringRep[thisResult->data.confarray_len] = '\0';

            r_Minterval* typedElement = new r_Minterval( stringRep );
            element                   = typedElement;
            r_Transaction::actual_transaction->add_object_list( r_Transaction::MINTERVAL, (void*) typedElement );
            delete [] stringRep;
        }
            break;

        case r_Type::OIDTYPE:
        {
            char* stringRep = new char[thisResult->data.confarray_len+1];
            strncpy( stringRep, thisResult->data.confarray_val, thisResult->data.confarray_len );
            stringRep[thisResult->data.confarray_len] = '\0';

            r_OId* typedElement = new r_OId( stringRep );
            element             = typedElement;
            r_Transaction::actual_transaction->add_object_list( r_Transaction::OID, (void*) typedElement );
            delete [] stringRep;
        }
            break;
        default:
            RMDBGENTER(2, RMDebug::module_clientcomm, "RnpClientComm", "getElementCollection(...) bad element typeId" << elementType->type_id())
                    break;
        }

        TALK( "got an element" );

        // insert element into result set
        resultColl.insert_element( element, 1 );

        delete[] thisResult->data.confarray_val;
        delete   thisResult;
    }

    executeEndTransfer();

    RMDBGEXIT( 2, RMDebug::module_clientcomm, "RnpClientComm", "getElementCollection()" );
    LEAVE( "RnpClientComm::getElementCollection()" );
}

int RasnetClientComm::executeExecuteUpdateQuery(const char *query) throw( r_Error )
{
    ExecuteUpdateQueryReq executeUpdateQueryReq;
    ExecuteUpdateQueryRepl executeUpdateQueryRepl;

    executeUpdateQueryReq.set_client_id(this->clientId);
    executeUpdateQueryReq.set_query(query);

    this->getRasServerService()->ExecuteUpdateQuery(&clientController, &executeUpdateQueryReq, &executeUpdateQueryRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    int status = executeUpdateQueryRepl.status();
    int errNo = executeUpdateQueryRepl.errono();
    int lineNo = executeUpdateQueryRepl.lineno();
    int colNo = executeUpdateQueryRepl.colno();

    string token = executeUpdateQueryRepl.token();

    if( status == 2 || status == 3 )
    {
        LEAVE( "RnpClientComm::executeExecuteUpdateQuery(): exception, status = " << status );
        throw r_Equery_execution_failed( errNo, lineNo, colNo, token.c_str() );
    }

    if( status == 1 )
    {
        LEAVE( "RnpClientComm::executeExecuteUpdateQuery(): exception, status = " << status );
        throw r_Error( r_Error::r_Error_ClientUnknown );
    }

    if( status > 3 )
    {
        LEAVE( "RnpClientComm::executeExecuteUpdateQuery(): exception, status = " << status );
        throw r_Error( r_Error::r_Error_TransferFailed );
    }

    LEAVE( "RnpClientComm::executeExecuteUpdateQuery()" );
    return status;
}

int  RasnetClientComm::executeExecuteUpdateQuery(const char *query, r_Set< r_Ref_Any >& result) throw(r_Error)
{
    ExecuteInsertQueryReq executeInsertQueryReq;
    ExecuteInsertQueryRepl executeInsertQueryRepl;

    executeInsertQueryReq.set_client_id(this->clientId);
    executeInsertQueryReq.set_query(query);

    this->getRasServerService()->ExecuteInsertQuery(&clientController, &executeInsertQueryReq, &executeInsertQueryRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    int status = executeInsertQueryRepl.status();
    int errNo = executeInsertQueryRepl.errono();
    int lineNo = executeInsertQueryRepl.lineno();
    int colNo = executeInsertQueryRepl.colno();
    string token = executeInsertQueryRepl.token();
    const char* typeName = executeInsertQueryRepl.type_name().c_str();
    const char* typeStructure = executeInsertQueryRepl.type_structure().c_str();

    if(status == 0 || status == 1 || status == 2)
    {
        result.set_type_by_name( typeName );
        result.set_type_structure( typeStructure );
    }

    // status == 2 - empty result

    if( status == 4 || status == 5 )
    {
        LEAVE( "RnpClientComm::executeExecuteUpdateQuery(_,_): exception, status = " << status );
        throw r_Equery_execution_failed( errNo, lineNo, colNo, token.c_str() );
    }

    LEAVE( "RnpClientComm::executeExecuteUpdateQuery()" );
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

    this->getRasServerService()->SetFormat(&clientController, &setFormatReq, &setFormatRepl, doNothing.get());
    if (clientController.Failed())
    {
        handleError(clientController.ErrorText());
    }

    return setFormatRepl.status();
}

void RasnetClientComm::checkForRwTransaction() throw( r_Error )
{
    r_Transaction *trans = r_Transaction::actual_transaction;
    if(  trans == 0 || trans->get_mode() == r_Transaction::read_only )
    {
        TALK( "RnpClientComm::checkForRwTransaction(): throwing exception from failed TA rw check." );
        throw r_Error( r_Error::r_Error_TransactionReadOnly );
    }
}

void RasnetClientComm::handleError(string error) throw( r_Error )
{
    char* errorText = strdup(error.c_str());
    r_Error *temp = r_Error::getAnyError(errorText);
    r_Error err = *temp;
    delete temp;
    free(errorText);

    throw err;
}

void RasnetClientComm::handleStatusCode(int status, string method) throw( r_Error )
{
    switch( status )
    {
    case 0:
        break;
    case 1:
        TALK( "RasnetClientComm::" << method << ": error: status = " << status );
        throw r_Error( r_Error::r_Error_ClientUnknown );
        break;
    case 2:
        TALK( "RasnetClientComm::" << method << ": error: status = " << status );
        throw r_Error( r_Error::r_Error_ObjectUnknown );
        break;
    default:
        TALK( "RasnetClientComm::" << method << ": error: status = " << status );
        throw r_Error( r_Error::r_Error_General );
        break;
    }
}

bool RasnetClientComm::effectivTypeIsRNP() throw()
{

}

long unsigned int RasnetClientComm::getClientID() const
{

}

void RasnetClientComm::triggerAliveSignal()
{

}

void RasnetClientComm::sendAliveSignal()
{

}

const char* RasnetClientComm::getExtendedErrorInfo() throw (r_Error)
{

}

void RasnetClientComm::setUserIdentification(const char *userName, const char *plainTextPassword)
{

    connectClient(string(userName), common::Crypto::messageDigest(string(plainTextPassword), DEFAULT_DIGEST));
}

void RasnetClientComm::setMaxRetry(unsigned int newMaxRetry)
{

}

unsigned int RasnetClientComm::getMaxRetry()
{

}

void RasnetClientComm::setTimeoutInterval(int seconds)
{

}

int RasnetClientComm::getTimeoutInterval()
{

}

/* START: KEEP ALIVE */

/* RASMGR */
void RasnetClientComm::startRasMgrKeepAlive()
{
    this->rasmgrKeepAliveControlEndpoint = ZmqUtil::toInprocAddress(UUID::generateUUID());
    this->rasMgrKeepAliveManagementThread.reset(
                new thread(&RasnetClientComm::clientRasMgrKeepAliveRunner, this));

    this->rasmgrKeepAliveControlSocket.reset(new zmq::socket_t(this->rasmgrKeepAliveContext, ZMQ_PAIR));
    this->rasmgrKeepAliveControlSocket->connect(this->rasmgrKeepAliveControlEndpoint.c_str());
}

void RasnetClientComm::stopRasMgrKeepAlive()
{
    try
    {
        // Kill the thread in a clean way
        rasnet::InternalDisconnectRequest request = rasnet::InternalDisconnectRequest::default_instance();
        base::BaseMessage reply;

        ProtoZmq::zmqSend(*(this->rasmgrKeepAliveControlSocket.get()), request);
        ProtoZmq::zmqReceive(*(this->rasmgrKeepAliveControlSocket.get()), reply);

        if(reply.type()!=rasnet::InternalDisconnectReply::default_instance().GetTypeName())
        {
            LERROR<<"Unexpected message received from control socket."<<reply.DebugString();
        }

        this->rasMgrKeepAliveManagementThread->join();
    }
    catch (std::exception& ex)
    {
        LERROR<<ex.what();
    }
    catch (...)
    {
        LERROR<<"Rasmgr Keep Alive stop has failed";
    }
}

void RasnetClientComm::clientRasMgrKeepAliveRunner(){

    base::BaseMessage controlMessage;
    bool keepRunning = true;

    try
    {
        zmq::socket_t control(this->rasmgrKeepAliveContext, ZMQ_PAIR);
        control.bind(this->rasmgrKeepAliveControlEndpoint.c_str());
        zmq::pollitem_t items[] = {
            {control, 0, ZMQ_POLLIN}
        };

        while (keepRunning){
            zmq::poll(items, 1, this->keepAliveTimeout);
            if (items[0].revents & ZMQ_POLLIN)
            {
                ProtoZmq::zmqReceive(control, controlMessage);
                if (controlMessage.type() == InternalDisconnectRequest::default_instance().GetTypeName())
                {
                    keepRunning = false;
                    InternalDisconnectReply disconnectReply;
                    ProtoZmq::zmqSend(control, disconnectReply);
                }
            }
            else
            {
                KeepAliveReq keepAliveReq;
                Void keepAliveRepl;
                keepAliveReq.set_clientuuid(this->clientUUID);
                this->getRasMgrService()->KeepAlive(&clientController, &keepAliveReq, &keepAliveRepl, doNothing.get());
            }
        }
    }
    catch (std::exception& ex)
    {
        LERROR<<"Rasmgr Keep Alive thread has failed";
        LERROR<<ex.what();
    }
    catch ( ... )
    {
        LERROR<<"Rasmgr Keep Alive thread failed for unknown reason.";
    }
}

/* RASSERVER */
void RasnetClientComm::startRasServerKeepAlive()
{
    this->rasserverKeepAliveControlEndpoint = ZmqUtil::toInprocAddress(UUID::generateUUID());
    this->rasServerKeepAliveManagementThread.reset(
                new thread(&RasnetClientComm::clientRasServerKeepAliveRunner, this));

    this->rasserverKeepAliveSocket.reset(new zmq::socket_t(this->rasserverKeepAliveContext, ZMQ_PAIR));
    this->rasserverKeepAliveSocket->connect(this->rasserverKeepAliveControlEndpoint.c_str());
}

void RasnetClientComm::stopRasServerKeepAlive()
{
    try
    {
        // Kill the thread in a clean way
        rasnet::InternalDisconnectRequest request = rasnet::InternalDisconnectRequest::default_instance();
        base::BaseMessage reply;

        ProtoZmq::zmqSend(*(this->rasserverKeepAliveSocket.get()), request);
        ProtoZmq::zmqReceive(*(this->rasserverKeepAliveSocket.get()), reply);

        if(reply.type()!=rasnet::InternalDisconnectReply::default_instance().GetTypeName())
        {
            LERROR<<"Unexpected message received from control socket."<<reply.DebugString();
        }

        this->rasServerKeepAliveManagementThread->join();
    }
    catch (std::exception& ex)
    {
        LERROR<<ex.what();
    }
    catch (...)
    {
        LERROR<<"RasServer Keep Alive stop has failed";
    }
}

void RasnetClientComm::clientRasServerKeepAliveRunner(){

    base::BaseMessage controlMessage;
    bool keepRunning = true;

    try
    {
        zmq::socket_t control(this->rasserverKeepAliveContext, ZMQ_PAIR);
        control.bind(this->rasserverKeepAliveControlEndpoint.c_str());
        zmq::pollitem_t items[] = {
            {control, 0, ZMQ_POLLIN}
        };

        while (keepRunning){
            zmq::poll(items, 1, this->keepAliveTimeout);
            if (items[0].revents & ZMQ_POLLIN)
            {
                ProtoZmq::zmqReceive(control, controlMessage);
                if (controlMessage.type() == InternalDisconnectRequest::default_instance().GetTypeName())
                {
                    keepRunning = false;
                    InternalDisconnectReply disconnectReply;
                    ProtoZmq::zmqSend(control, disconnectReply);
                }
            }
            else
            {
                KeepAliveRequest keepAliveReq;
                Void keepAliveRepl;

                keepAliveReq.set_client_uuid(this->clientUUID);
                keepAliveReq.set_session_id(this->sessionId);
                this->getRasServerService()->KeepAlive(&clientController, &keepAliveReq, &keepAliveRepl, doNothing.get());
            }
        }
    }
    catch (std::exception& ex)
    {
        LERROR<<"RasServer Keep Alive thread has failed";
        LERROR<<ex.what();
    }
    catch ( ... )
    {
        LERROR<<"RasServer Keep Alive thread failed for unknown reason.";
    }
}
/* END: KEEP ALIVE */
