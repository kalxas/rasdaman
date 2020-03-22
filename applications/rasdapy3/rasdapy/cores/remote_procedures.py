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
import struct

from rasdapy.cores.request_factories import make_rasmgr_close_db_req, \
    make_rasmgr_connect_req, make_rasmgr_disconnect_req, \
    make_rasmgr_keep_alive_req, make_rasmgr_open_db_req, \
    make_rassrvr_abort_transaction_req, \
    make_rassrvr_begin_streamed_http_query_req, \
    make_rassrvr_begin_transaction_req, make_rassrvr_close_db_req, \
    make_rassrvr_commit_transaction_req, make_rassrvr_create_db_req, \
    make_rassrvr_delete_collection_by_id_req, \
    make_rassrvr_delete_collection_by_name_req, make_rassrvr_destroy_db_req, \
    make_rassrvr_end_insert_mdd_req, make_rassrvr_end_transfer_req, \
    make_rassrvr_create_db_req, make_rassrvr_execute_http_query_req, \
    make_rassrvr_execute_insert_query_req, make_rassrvr_execute_query_req, \
    make_rassrvr_execute_update_query_req, \
    make_rassrvr_get_collection_by_name_or_id_req, \
    make_rassrvr_get_collection_oids_by_name_or_id, \
    make_rassrvr_get_new_oid_req, make_rassrvr_get_next_element_req, \
    make_rassrvr_get_next_mdd_req, \
    make_rassrvr_get_next_streamed_http_query_req, \
    make_rassrvr_get_next_tile_req, make_rassrvr_get_object_type_req, \
    make_rassrvr_get_type_structure_req, make_rassrvr_init_update_req, \
    make_rassrvr_insert_collection_req, make_rassrvr_insert_tile_req, \
    make_rassrvr_is_transaction_open_req, make_rassrvr_keep_alive_req, \
    make_rassrvr_open_db_req, make_rassrvr_remove_object_from_collection_req, \
    make_rassrvr_set_format_req, make_rassrvr_start_insert_mdd, \
    make_rassrvr_start_insert_trans_mdd

from rasdapy.cores.utils import ubytes_to_int, byte_to_char_value
from rasdapy.cores.exception_factories import ExceptionFactories

_TIMEOUT_SECONDS = 60
_QUERY_TIMEOUT_SECONDS = 3600


def rasmgr_connect(stub, username, password):
    connection = stub.Connect(make_rasmgr_connect_req(username, password),
                              _TIMEOUT_SECONDS)
    if not connection:
        raise Exception("Remote function 'Connect' did not return anything")
    return connection


def rasmgr_disconnect(stub, cuuid, cid):
    return stub.Disconnect(make_rasmgr_disconnect_req(cuuid, cid),
                           _TIMEOUT_SECONDS)


def rasmgr_keep_alive(stub, cuuid):
    return stub.KeepAlive(make_rasmgr_keep_alive_req(cuuid), _TIMEOUT_SECONDS)


def rasmgr_open_db(stub, cuuid, cid, dbname):
    resp = stub.OpenDb(make_rasmgr_open_db_req(cuuid, cid, dbname),
                       _TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'OpenDb' did not return anything")
    return resp


def rasmgr_close_db(stub, cuuid, cid, dbid):
    return stub.CloseDb(make_rasmgr_close_db_req(cuuid, cid, dbid),
                        _TIMEOUT_SECONDS)


# Start RasServer RPCs


def rassrvr_open_db(stub, cid, dbname):
    resp = stub.OpenServerDatabase(make_rassrvr_open_db_req(cid, dbname),
                                   _TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'OpenDB' did not return anything")
    return resp


def rassrvr_close_db(stub, cid):
    return stub.CloseServerDatabase(make_rassrvr_close_db_req(cid),
                                    _TIMEOUT_SECONDS)


def rassrvr_create_db(stub, cid, dbname):
    return stub.CreateDatabase(make_rassrvr_create_db_req(cid, dbname),
                               _TIMEOUT_SECONDS)


def rassrvr_destroy_db(stub, cid, dbname):
    return stub.DestroyDatabase(make_rassrvr_destroy_db_req(cid, dbname),
                                _TIMEOUT_SECONDS)


def rassrvr_begin_transaction(stub, cid, rw):
    return stub.BeginTransaction(make_rassrvr_begin_transaction_req(cid, rw),
                                 _TIMEOUT_SECONDS)


def rassrvr_commit_transaction(stub, cid):
    return stub.CommitTransaction(make_rassrvr_commit_transaction_req(cid),
                                  _TIMEOUT_SECONDS)


def rassrvr_abort_transaction(stub, cid):
    return stub.AbortTransaction(make_rassrvr_abort_transaction_req(cid),
                                 _TIMEOUT_SECONDS)


def rassrvr_is_transaction_open(stub, cid):
    resp = stub.IsTransactionOpen(make_rassrvr_is_transaction_open_req(cid),
                                  _TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'IsTransactionOpen' did not return anything")
    return resp


def rassrvr_start_insert_mdd(stub, cid, coll_name, domain, type_len, type_name,
                             oid):
    resp = stub.StartInsertMDD(
            make_rassrvr_start_insert_mdd(cid, coll_name, domain, type_len,
                                          type_name, oid),
            _QUERY_TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'StartInsertMDD' did not return anything")
    return resp


def rassrvr_start_insert_trans_mdd(stub, cid, domain, type_len, type_name):
    resp = stub.StartInsertTransMDD(
            make_rassrvr_start_insert_trans_mdd(cid, domain, type_len,
                                                type_name),
            _QUERY_TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'StartInsertTransMDD' did not return anything")
    return resp


def rassrvr_insert_tile(stub, cid, persistent, domain, type_len, current_format,
                        storage_format, data, data_len):
    resp = stub.InsertTile(
            make_rassrvr_insert_tile_req(cid, persistent, domain, type_len,
                                         current_format, storage_format, data,
                                         data_len),
            _QUERY_TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'InsertTile' did not return anything")
    return resp


def rassrvr_end_insert_mdd(stub, cid, persistence):
    resp = stub.EndInsertMDD(make_rassrvr_end_insert_mdd_req(cid, persistence),
                          _TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'EndInsertMDD' failed")
    return resp


def rassrvr_insert_collection(stub, cid, coll_name, type_name, oid):
    resp = stub.InsertCollection(
            make_rassrvr_insert_collection_req(cid, coll_name, type_name, oid),
            _TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'InsertCollection' failed")
    return resp


def rassrvr_delete_collection_by_name(stub, cid, coll_name):
    resp = stub.DeleteCollectionByName(
            make_rassrvr_delete_collection_by_name_req(cid, coll_name),
            _TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'DeleteCollectionByName' failed")
    return resp


def rassrvr_delete_collection_by_id(stub, cid, oid):
    resp = stub.DeleteCollectionByOid(
            make_rassrvr_delete_collection_by_name_req(cid, oid),
            _TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'DeleteCollectionByOid' failed")
    return resp


def rassrvr_remove_object_from_collection(stub, cid, coll_name, oid):
    resp = stub.RemoveObjectFromCollection(
            make_rassrvr_remove_object_from_collection_req(cid, coll_name, oid),
            _TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'RemoveObjectFromCollection' failed")
    return resp


def rassrvr_get_collection_by_name(stub, cid, name):
    resp = stub.GetCollectionByNameOrOid(
            make_rassrvr_get_collection_by_name_or_id_req(cid, name, True),
            _QUERY_TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'GetCollectionByNameOrOid' did not return "
                "anything")
    return resp


def rassrvr_get_collection_by_id(stub, cid, oid):
    resp = stub.GetCollectionByNameOrOid(
            make_rassrvr_get_collection_by_name_or_id_req(cid, oid, False),
            _QUERY_TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'GetCollectionByNameOrOid' did not return "
                "anything")
    return resp


def rassrvr_get_collection_oids_by_id(stub, cid, coll_id):
    resp = stub.GetCollOidsByNameOrOid(
            make_rassrvr_get_collection_oids_by_name_or_id(cid, coll_id, False),
            _TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'GetCollOidsByNameOrOid' did not return "
                "anything")
    return resp


def rassrvr_get_collection_oids_by_name(stub, cid, coll_name):
    resp = stub.GetCollOidsByNameOrOid(
            make_rassrvr_get_collection_oids_by_name_or_id(cid, coll_name,
                                                           True),
            _TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'GetCollOidsByNameOrOid' did not return "
                "anything")
    return resp


def rassrvr_get_next_mdd(stub, cid):
    resp = stub.GetNextMDD(make_rassrvr_get_next_mdd_req(cid),
                           _QUERY_TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'GetNextMDD' did not return anything")
    return resp


def rassrvr_get_next_tile(stub, cid):
    resp = stub.GetNextTile(make_rassrvr_get_next_tile_req(cid),
                            _QUERY_TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'GetNextTile' did not return anything")
    return resp


def rassrvr_end_transfer(stub, cid):
    resp = stub.EndTransfer(make_rassrvr_end_transfer_req(cid),
                            _TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'EndTransfer' did not return anything")
    return resp


def rassrvr_init_update(stub, cid):
    resp = stub.InitUpdate(make_rassrvr_init_update_req(cid), _TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'InitUpdate' did not return anything")
    return resp


def rassrvr_execute_query(stub, cid, query):
    resp = stub.ExecuteQuery(make_rassrvr_execute_query_req(cid, query),
                             _QUERY_TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'ExecuteQuery' did not return anything")
    return resp


def rassrvr_execute_http_query(stub, cid, data):
    resp = stub.ExecuteHttpQuery(make_rassrvr_execute_http_query_req(cid, data),
                                 _QUERY_TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'ExecuteHttpQuery' did not return anything")
    return resp


def rassrvr_get_next_element(stub, cid):
    resp = stub.GetNextElement(make_rassrvr_get_next_element_req(cid),
                               _QUERY_TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'GetNextElement' did not return anything")
    return resp


def rassrvr_execute_update_query(stub, cid, query):
    resp = stub.ExecuteUpdateQuery(
            make_rassrvr_execute_update_query_req(cid, query),
            _QUERY_TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'ExecuteUpdateQuery' did not return anything")
    return resp


def rassrvr_execute_insert_query(stub, cid, query):
    resp = stub.ExecuteInsertQuery(
            make_rassrvr_execute_insert_query_req(cid, query),
            _QUERY_TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'ExecuteInsertQuery' did not return anything")
    return resp


def rassrvr_get_new_oid(stub, cid, obj_type):
    resp = stub.GetNewOid(make_rassrvr_get_new_oid_req(cid, obj_type),
                          _TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'GetNewOid' did not return anything")
    return resp


def rassrvr_get_object_type(stub, cid, oid):
    resp = stub.GetObjectType(make_rassrvr_get_object_type_req(cid, oid),
                              _TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'GetObjectType' did not return anything")
    return resp


def rassrvr_get_type_structure(stub, cid, type_name, type_type):
    resp = stub.GetTypeStructure(
            make_rassrvr_get_type_structure_req(cid, type_name, type_type),
            _TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'GetTypeStructure' did not return anything")
    return resp


def rassrvr_set_format(stub, cid, transfer_format, format, format_params):
    resp = stub.SetFormat(
            make_rassrvr_set_format_req(cid, transfer_format, format,
                                        format_params), _TIMEOUT_SECONDS)
    if not resp:
        raise Exception("Remote function 'SetFormat' did not return anything")
    return resp


def rassrvr_keep_alive(stub, client_uuid, session_id):
    resp = stub.KeepAlive(make_rassrvr_keep_alive_req(client_uuid, session_id),
                          _TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'KeepAlive' from RasServer did not return "
                "anything")
    return resp


def rassrvr_begin_streamed_http_query(stub, cuuid, data):
    request = make_rassrvr_begin_streamed_http_query_req(cuuid, data)
    resp = stub.BeginStreamedHttpQuery(request)
    if not resp:
        raise Exception(
                "Remote function 'BeginStreamedHttpQuery' from RasServer did "
                "not return anything")
                
    # check first byte to know it is error or not
    resp_data_conv = [resp.data[i:i+1] for i in range(len(resp.data))]
    error = byte_to_char_value(resp_data_conv[0])
    if error == 0:
        # Unpack the response for error code
        bytes_arr = []
        for i in range(0, len(resp_data_conv)):
            tmp = byte_to_char_value(resp_data_conv[i])
            bytes_arr.append(tmp)

        edianness = bytes_arr[1]
        err_no = ubytes_to_int(bytes_arr[2:6], edianness)
        line_no = ubytes_to_int(bytes_arr[6:10], edianness)
        col_no = ubytes_to_int(bytes_arr[10:14], edianness)
        token = ""
        for i in range(14, len(resp_data_conv) - 1):
            token += chr(bytes_arr[i])

        error_message = ExceptionFactories.create_error_message(err_no, line_no, col_no, token)
        raise Exception("Error executing query. Reason: '{}'".format(error_message))

    return resp


def rassrvr_get_next_streamed_http_query(stub, uuid):
    resp = stub.GetNextStreamedHttpQuery(
            make_rassrvr_get_next_streamed_http_query_req(uuid),
            _TIMEOUT_SECONDS)
    if not resp:
        raise Exception(
                "Remote function 'GetNextStreamedHttpQuery' from RasServer "
                "did not return anything")
    return resp
