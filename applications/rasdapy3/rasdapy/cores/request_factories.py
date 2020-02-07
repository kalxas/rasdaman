"""
 *
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""

from rasdapy.cores.utils import get_md5_string
from rasdapy.stubs import client_rassrvr_service_pb2 as rassrvr
from rasdapy.stubs import rasmgr_client_service_pb2 as rasmgr


def make_rasmgr_connect_req(username, password):
    passwordHash = get_md5_string(password)
    con_req = rasmgr.ConnectReq(userName=username, passwordHash=passwordHash)
    if not con_req:
        raise Exception("Can't create Connect request")
    return con_req


def make_rasmgr_disconnect_req(cuiid, cid):
    discon_req = rasmgr.DisconnectReq(clientUUID=cuiid, clientId=cid)
    if not discon_req:
        raise Exception("Can't create Disconnect request")
    return discon_req


def make_rasmgr_keep_alive_req(cuiid):
    keep_alive_req = rasmgr.KeepAliveReq(clientUUID=cuiid)
    if not keep_alive_req:
        raise Exception("Can't create KeepAlive request")
    return keep_alive_req


def make_rasmgr_open_db_req(cuiid, cid, dbname):
    open_db_req = rasmgr.OpenDbReq(clientUUID=cuiid, clientId=cid,
                                   databaseName=dbname)
    if not open_db_req:
        raise Exception("Can't create OpenDb request")
    return open_db_req


def make_rasmgr_close_db_req(cuuid, cid, dbsid):
    close_db_req = rasmgr.CloseDbReq(clientUUID=cuuid, clientId=cid,
                                     dbSessionId=dbsid)
    if not close_db_req:
        raise Exception("Can't create CloseDb request")
    return close_db_req


# Start RasServer request creation


def make_rassrvr_open_db_req(cid, dbname):
    open_db_req = rassrvr.OpenServerDatabaseReq(client_id=cid,
                                                database_name=dbname)
    if not open_db_req:
        raise Exception("Can't create OpenServerDatabase request for Rassrvr")
    return open_db_req


def make_rassrvr_close_db_req(cid):
    close_db_req = rassrvr.CloseServerDatabaseReq(client_id=cid)
    if not close_db_req:
        raise Exception("Can't create CloseServerDatabase request for Rassrvr")
    return close_db_req


def make_rassrvr_create_db_req(cid, dbname):
    create_db_req = rassrvr.CreateDatabaseReq(client_id=cid,
                                              database_name=dbname)
    if not create_db_req:
        raise Exception("Can't create CreateDatabase request for Rassrvr")
    return create_db_req


def make_rassrvr_destroy_db_req(cid, dbname):
    destroy_db_req = rassrvr.DestroyDatabaseReq(client_id=cid,
                                                database_name=dbname)
    if not destroy_db_req:
        raise Exception("Can't destroy CreateDatabase request for Rassrvr")
    return destroy_db_req


def make_rassrvr_begin_transaction_req(cid, rw):
    begin_transaction_req = rassrvr.BeginTransactionReq(client_id=cid, rw=rw)
    if not begin_transaction_req:
        raise Exception("Can't create BeginTransaction request")
    return begin_transaction_req


def make_rassrvr_commit_transaction_req(cid):
    commit_transaction_req = rassrvr.CommitTransactionReq(client_id=cid)
    if not commit_transaction_req:
        raise Exception("Can't create CommitTransaction request")
    return commit_transaction_req


def make_rassrvr_abort_transaction_req(cid):
    abort_transaction_req = rassrvr.AbortTransactionReq(client_id=cid)
    if not abort_transaction_req:
        raise Exception("Can't create AbortTransaction request")
    return abort_transaction_req


def make_rassrvr_is_transaction_open_req(cid):
    is_transaction_open_req = rassrvr.IsTransactionOpenReq(client_id=cid)
    if not is_transaction_open_req:
        raise Exception("Can't create IsTransactionOpen request")
    return is_transaction_open_req


def make_rassrvr_start_insert_mdd(cid, collName, domain, type_len, type_name,
                                  oid):
    start_insert_mdd_req = rassrvr.StartInsertMDDReq(client_id=cid,
                                                     collName=collName,
                                                     domain=domain,
                                                     type_length=type_len,
                                                     type_name=type_name,
                                                     oid=oid)
    if not start_insert_mdd_req:
        raise Exception("Can't create StartInsertMDD request")
    return start_insert_mdd_req


def make_rassrvr_start_insert_trans_mdd(cid, domain, type_len, type_name):
    start_insert_trans_mdd_req = rassrvr.StartInsertTransMDDReq(client_id=cid,
                                                                domain=domain,
                                                                type_length=type_len,
                                                                type_name=type_name)
    if not start_insert_trans_mdd_req:
        raise Exception("Can't create StartInsertTransMDD request")
    return start_insert_trans_mdd_req


def make_rassrvr_insert_tile_req(cid, persistence, domain, type_len,
                                 current_format, storage_format, data,
                                 data_len):
    insert_tile_req = rassrvr.InsertTileReq(client_id=cid,
                                            persistent=persistence,
                                            domain=domain, type_length=type_len,
                                            current_format=current_format,
                                            storage_format=storage_format,
                                            data=data,
                                            data_length=data_len)
    if not insert_tile_req:
        raise Exception("Can't create InsertTile request")
    return insert_tile_req


def make_rassrvr_end_insert_mdd_req(cid, persistence):
    end_insert_mdd_req = rassrvr.EndInsertMDDReq(client_id=cid,
                                                 persistent=persistence)
    if not end_insert_mdd_req:
        raise Exception("Can't create EndInsertMDD request")
    return end_insert_mdd_req


def make_rassrvr_insert_collection_req(cid, coll_name, type_name, oid):
    insert_collection_req = rassrvr.InsertCollectionReq(client_id=cid,
                                                        collection_name=coll_name,
                                                        type_name=type_name,
                                                        oid=oid)
    if not insert_collection_req:
        raise Exception("Can't create InsertCollection request")
    return insert_collection_req


def make_rassrvr_delete_collection_by_name_req(cid, coll_name):
    delete_collection_by_name_req = rassrvr.DeleteCollectionByNameReq(
            client_id=cid, collection_name=coll_name)
    if not delete_collection_by_name_req:
        raise Exception("Can't create DeleteCollectionByName request")
    return delete_collection_by_name_req


def make_rassrvr_delete_collection_by_id_req(cid, coll_id):
    delete_collection_by_id_req = rassrvr.DeleteCollectionByOidReq(
            client_id=cid, oid=coll_id)
    if not delete_collection_by_id_req:
        raise Exception("Can't create DeleteCollectionById request")
    return delete_collection_by_id_req


def make_rassrvr_remove_object_from_collection_req(cid, coll_name, oid):
    remove_object_from_collection_req = rassrvr.RemoveObjectFromCollectionReq(
            client_id=cid, collection_name=coll_name,
            oid=oid)
    if not remove_object_from_collection_req:
        raise Exception("Can't create RemoveObjectFromCollection request")
    return remove_object_from_collection_req


def make_rassrvr_get_collection_by_name_or_id_req(cid, colid, is_name):
    get_collection_req = rassrvr.GetCollectionByNameOrOidReq(client_id=cid,
                                                             collection_identifier=colid,
                                                             is_name=is_name)
    if not get_collection_req:
        raise Exception("Can't create GetCollectionByNameOrOid request")
    return get_collection_req


def make_rassrvr_get_collection_oids_by_name_or_id(cid, colid, is_name):
    get_collection_req = rassrvr.GetCollOidsByNameOrOidReq(client_id=cid,
                                                           collection_identifier=colid,
                                                           is_name=is_name)
    if not get_collection_req:
        raise Exception("Can't create GetCollOidsByNameOrOid request")
    return get_collection_req


def make_rassrvr_get_next_mdd_req(cid):
    get_next_mdd_req = rassrvr.GetNextMDDReq(client_id=cid)
    if not get_next_mdd_req:
        raise Exception("Can't create GetNextMDD request")
    return get_next_mdd_req


def make_rassrvr_get_next_tile_req(cid):
    get_next_tile_req = rassrvr.GetNextTileReq(client_id=cid)
    if not get_next_tile_req:
        raise Exception("Can't create GetNextTile request")
    return get_next_tile_req


def make_rassrvr_end_transfer_req(cid):
    end_transfer_req = rassrvr.EndTransferReq(client_id=cid)
    if not end_transfer_req:
        raise Exception("Can't create EndTransfer request")
    return end_transfer_req


def make_rassrvr_init_update_req(cid):
    init_update_req = rassrvr.InitUpdateReq(client_id=cid)
    if not init_update_req:
        raise Exception("Can't create InitUpdate request")
    return init_update_req


def make_rassrvr_execute_query_req(cid, query):
    execute_query_req = rassrvr.ExecuteQueryReq(client_id=cid, query=query)
    if not execute_query_req:
        raise Exception("Can't create ExecuteQuery request")
    return execute_query_req


def make_rassrvr_execute_http_query_req(cid, data):
    execute_http_query_req = rassrvr.ExecuteHttpQueryReq(client_id=cid,
                                                         data=data)
    if not execute_http_query_req:
        raise Exception("Can't create ExecuteHttpQuery request")
    return execute_http_query_req


def make_rassrvr_get_next_element_req(cid):
    get_next_element_req = rassrvr.GetNextElementReq(client_id=cid)
    if not get_next_element_req:
        raise Exception("Can't create GetNextElement request")
    return get_next_element_req


def make_rassrvr_execute_update_query_req(cid, query):
    execute_update_query_req = rassrvr.ExecuteUpdateQueryReq(client_id=cid,
                                                             query=query)
    if not execute_update_query_req:
        raise Exception("Can't create ExecuteUpdateQuery request")
    return execute_update_query_req


def make_rassrvr_execute_insert_query_req(cid, query):
    execute_insert_query_req = rassrvr.ExecuteInsertQueryReq(client_id=cid,
                                                             query=query)
    if not execute_insert_query_req:
        raise Exception("Can't create ExecuteInsertQuery request")
    return execute_insert_query_req


def make_rassrvr_get_new_oid_req(cid, object_type):
    get_new_oid_req = rassrvr.GetNewOidReq(client_id=cid,
                                           object_type=object_type)
    if not get_new_oid_req:
        raise Exception("Can't create GetNewOid request")
    return get_new_oid_req


def make_rassrvr_get_object_type_req(cid, oid):
    get_obj_type_req = rassrvr.GetObjectTypeReq(client_id=cid, oid=oid)
    if not get_obj_type_req:
        raise Exception("Can't create GetObjectType request")
    return get_obj_type_req


def make_rassrvr_get_type_structure_req(cid, type_name, type_type):
    get_type_structure_req = rassrvr.GetTypeStructureReq(client_id=cid,
                                                         type_name=type_name,
                                                         type_type=type_type)
    if not get_type_structure_req:
        raise Exception("Can't create GetTypeStructure request")
    return get_type_structure_req


def make_rassrvr_set_format_req(cid, transfer_format, format, format_params):
    set_format_req = rassrvr.SetFormatReq(client_id=cid,
                                          transfer_format=transfer_format,
                                          format=format,
                                          format_params=format_params)
    if not set_format_req:
        raise Exception("Can't create SetFormat request")
    return set_format_req


def make_rassrvr_keep_alive_req(cuuid, dbsid):
    keep_alive_req = rassrvr.KeepAliveRequest(client_uuid=cuuid,
                                              session_id=dbsid)
    if not keep_alive_req:
        raise Exception("Can't create KeepAlive request for Rassrvr")
    return keep_alive_req


def make_rassrvr_begin_streamed_http_query_req(cuuid, data):
    begin_streamed_http_query_req = rassrvr.BeginStreamedHttpQueryReq(
        client_uuid=cuuid,
        data=data)
    if not begin_streamed_http_query_req:
        raise Exception("Can't create BeginStreamedHttpQuery request")
    return begin_streamed_http_query_req


def make_rassrvr_get_next_streamed_http_query_req(cuuid):
    get_next_streamed_http_query_req = rassrvr.GetNextStreamedHttpQueryReq(
            uuid=cuuid)
    if not get_next_streamed_http_query_req:
        raise Exception("Can't create GetNextStreamedHttpQuery request")
    return get_next_streamed_http_query_req
