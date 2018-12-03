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

"""
This module contains the necessary classes and methods for establishing
connection with a remote / local rasdaman server for accessing the database
collections and performing remote operations using rasql. It also contains
the necessary methods for fetching the resulting arrays and converting them
to a format (i.e. NumPy) that can be used for performing scientific operations
on the resultant arrays efficiently on the local machine
"""

import os

import grpc.beta.implementations
from grpc._cython import cygrpc
from rasdapy.cores.utils import StoppableTimeoutThread, \
    get_spatial_domain_from_type_structure, get_type_structure_from_string, \
    convert_binary_data_stream

from exception_factories import ExceptionFactories
from rasdapy.cores.remote_procedures import rasmgr_close_db, rasmgr_connect, \
    rasmgr_disconnect, rasmgr_keep_alive, rasmgr_open_db, \
    rassrvr_abort_transaction, rassrvr_begin_streamed_http_query, \
    rassrvr_begin_transaction, rassrvr_close_db, rassrvr_commit_transaction, \
    rassrvr_delete_collection_by_id, \
    rassrvr_delete_collection_by_name, rassrvr_end_transfer, rassrvr_execute_query, \
    rassrvr_execute_update_query, rassrvr_get_collection_by_name, rassrvr_get_next_element, rassrvr_get_next_mdd, \
    rassrvr_get_next_tile, \
    rassrvr_init_update, \
    rassrvr_insert_collection, rassrvr_insert_tile, \
    rassrvr_keep_alive, rassrvr_open_db, \
    rassrvr_start_insert_trans_mdd
from rasdapy.models.minterval import MInterval
from rasdapy.models.ras_gmrray import RasGMArray
from rasdapy.models.ras_storage_layout import RasStorageLayOut
from rasdapy.models.result_array import ResultArray
from rasdapy.stubs import client_rassrvr_service_pb2 as rassrvr
from rasdapy.stubs import rasmgr_client_service_pb2 as rasmgr
from utils import int_to_bytes, str_to_encoded_bytes, get_tiling_domain, convert_data_from_bin



class Connection(object):
    """
    Class to represent the connection from the python client to the rasdaman
    server
    """

    def __init__(self, hostname="0.0.0.0", port=7001, username="rasguest",
                 password="rasguest"):
        """
        Constructor for the connection class
        :param str hostname: the hostname of the rasdaman server (default:
        localhost)
        :param int port: the port on which rasdaman listens on (default: 7001)
        :param str username: username for the database (default: rasguest)
        :param str password: password for the database (default: rasguest)
        """
        self.hostname = hostname
        self.port = port
        self.username = username
        self.password = password
        self.channel = insecure_channel(hostname, port)
        self.stub = rasmgr.beta_create_RasmgrClientService_stub(self.channel)
        self.session = None
        self._rasmgr_keep_alive_running = None
        self._keep_alive_thread = None

    def disconnect(self):
        """
        Method for disconnecting the connection to the RasManager. Stops
        sending keep alive messages to the RasManager, disconnects, and destroys
        the session.
        """
        self._stop_keep_alive()
        rasmgr_disconnect(self.stub, self.session.clientUUID,
                          self.session.clientId)
        self.session = None

    def connect(self):
        """
        Method for connecting to the RasManager. Sends a connection request
        and once connected starts sending keep alive messages to the
        RasManager
        """
        self.session = rasmgr_connect(self.stub, self.username, self.password)
        self._keep_alive()

    def _keep_alive(self):
        """
        Method for creating and spawning a separate thread for sending keep
        alive messages to the RasManager
        """
        if not self._rasmgr_keep_alive_running:
            self._rasmgr_keep_alive_running = True
            if not self._keep_alive_thread:
                self._keep_alive_thread = StoppableTimeoutThread(
                    rasmgr_keep_alive,
                    self.session.keepAliveTimeout / 2000,
                    self.stub, self.session.clientUUID)
                self._keep_alive_thread.daemon = True
                self._keep_alive_thread.start()
        else:
            raise Exception("RasMgrKeepAlive already running")

    def _stop_keep_alive(self):
        """
        Method for stopping the thread that is responsible for sending keep
        alive messages to the RasManager
        """
        if self._rasmgr_keep_alive_running is not None:
            self._rasmgr_keep_alive_running = None
            if self._keep_alive_thread is not None:
                self._keep_alive_thread.stop()
                self._keep_alive_thread.join()
                self._keep_alive_thread = None
            else:
                raise Exception("No thread named _keep_alive_thread to stop")
        else:
            raise Exception("rasmgr_keep_alive thread not running")

    def database(self, name):
        """
        Returns a database object initialized with this connection
        :param str name: name of the database collection to access
        :rtype: Database
        :return: a new database object
        """
        database = Database(self, name)
        return database


class Database(object):
    """
    Class to represent a database stored inside a rasdaman server
    """

    def __init__(self, connection, name):
        """
        Constructor for the Database class
        :param Connection connection: the connection object for the rasdaman
        server
        :param str name: the name of the database
        """
        self.connection = connection
        self.name = name
        self.rasmgr_db = None
        self.rassrvr_db = None
        self.channel = None
        self.stub = None
        self._rassrvr_keep_alive_running = None
        self._keep_alive_thread = None

    def open(self):
        """
        Opens a connection to the RasServer on which the database is stored
        and starts sending the keep alive messages to the RasServer. Also, stops
        sending keep alive messages to the RasManager in case they are on the
        same machine.
        """
        self.rasmgr_db = rasmgr_open_db(self.connection.stub,
                                        self.connection.session.clientUUID,
                                        self.connection.session.clientId,
                                        self.name)
        if self.rasmgr_db.dbSessionId == self.connection.session.clientUUID:
            self.connection._stop_keep_alive()
        self.channel = insecure_channel(
            self.rasmgr_db.serverHostName, self.rasmgr_db.port)
        self.stub = rassrvr.beta_create_ClientRassrvrService_stub(self.channel)
        self.rassrvr_db = rassrvr_open_db(self.stub,
                                          self.connection.session.clientId,
                                          self.name)

        # Open another thread to keep connection with rasserver
        self._keep_alive()

    def close(self):
        """
        Closes the connection to RasServer and RasManager. Also, stops
        sending the keep alive messages to the RasServer.
        """

        # Trying to stop keep alive message thread to rasserver
        self._stop_keep_alive()

        # Trying to close connection to rasserver
        rassrvr_close_db(self.stub, self.connection.session.clientId)

        # Trying to close connection to rasmgr
        rasmgr_close_db(self.connection.stub,
                        self.connection.session.clientUUID,
                        self.connection.session.clientId,
                        self.rasmgr_db.dbSessionId)

    def create(self):
        """
        Method for creating a collection
        """
        raise NotImplementedError("Sorry, not implemented yet")

    def destroy(self):
        """
        Method for destroying a collection
        """
        raise NotImplementedError("Sorry, not implemented yet")

    def transaction(self, rw=False):
        """
        Returns a new transaction object for this database
        :param bool rw: Boolean value for write access
        :rtype: Transaction
        :return: a new transaction object
        """
        transaction = Transaction(self, rw=rw)
        return transaction

    @property
    def collections(self):
        """
        Returns all the collections for this database
        :rtype: list[Collection]
        """
        transaction = self.transaction()
        query = transaction.query("select r from RAS_COLLECTIONNAMES as r")
        result = query._execute_read()
        return result

    def _keep_alive(self):
        """
        Method for creating and spawning a separate thread for sending keep
        alive messages to the RasServer
        """
        if not self._rassrvr_keep_alive_running:
            self._rassrvr_keep_alive_running = True
            if not self._keep_alive_thread:
                self._keep_alive_thread = StoppableTimeoutThread(
                    rassrvr_keep_alive,
                    self.connection.session.keepAliveTimeout / 2000,
                    self.stub, self.connection.session.clientUUID,
                    self.rasmgr_db.dbSessionId)

                self._keep_alive_thread.daemon = True
                self._keep_alive_thread.start()
        else:
            raise Exception("RasSrvrKeepAlive already running")

    def _stop_keep_alive(self):
        """
        Method for stopping the thread that is responsible for sending keep
        alive messages to the RasServer
        """
        if self._rassrvr_keep_alive_running is not None:
            self._rassrvr_keep_alive_running = None
            if self._keep_alive_thread is not None:
                self._keep_alive_thread.stop()
                self._keep_alive_thread.join()
                self._keep_alive_thread = None
            else:
                raise Exception("No thread named _keep_alive_thread to stop")
        else:
            raise Exception("rassrvr_keep_alive thread not running")


class Collection(object):
    def __init__(self, transaction, name=None, type=None, oid=None):
        """
        Constructor for the class
        :param Transaction transaction: the transaction for which the
        collections should be returned
        """
        self.transaction = transaction
        self.name = name
        self.type_name = type
        self.type_structure = None
        self.oid = oid

    @property
    def name(self):
        """
        Returns the name of the collection
        :rtype: str
        """
        return self.name

    def array(self):
        """
        Return the arrays in this collection
        :rtype: Array
        """
        resp = rassrvr_get_collection_by_name(self.transaction.database.stub,
                                              self.transaction.database.connection.session.clientId,
                                              self.name)
        return resp

    def create(self):
        resp = rassrvr_insert_collection(self.transaction.database.stub,
                                         self.transaction.database.connection.session.clientId,
                                         self.name,
                                         self.type_name, self.oid)
        if resp.status == 0:
            return resp.status
        elif resp.status == 1 or resp.status == 3:
            raise Exception(
                "Error: Unknown Client. Status: " + str(resp.status))
        elif resp.status == 2:
            raise Exception(
                "Error: Unknown Object. Status: " + str(resp.status))
        else:
            raise Exception("Error: Unknown Error. Status: " + str(resp.status))

    def delete_by_name(self):
        resp = rassrvr_delete_collection_by_name(self.transaction.database.stub,
                                                 self.transaction.database.connection.session.clientId,
                                                 self.name)
        if resp.status == 0:
            return resp.status
        elif resp.status == 1 or resp.status == 3:
            raise Exception(
                "Error: Unknown Client. Status: " + str(resp.status))
        elif resp.status == 2:
            raise Exception(
                "Error: Unknown Object. Status: " + str(resp.status))
        else:
            raise Exception("Error: Unknown Error. Status: " + str(resp.status))

    def delete_by_id(self):
        resp = rassrvr_delete_collection_by_id(self.transaction.database.stub,
                                               self.transaction.database.connection.session.clientId,
                                               self.oid)
        if resp.status == 0:
            return resp.status
        elif resp.status == 1 or resp.status == 3:
            raise Exception(
                "Error: Unknown Client. Status: " + str(resp.status))
        elif resp.status == 2:
            raise Exception(
                "Error: Unknown Object. Status: " + str(resp.status))
        else:
            raise Exception("Error: Unknown Error. Status: " + str(resp.status))

    def insert(self, array):
        """
        Inserts an array in the collection
        :param Array array: the array to be inserted
        """
        if self.transaction.rw is not True:
            raise Exception("Transaction is read only. Can't insert MDD")

    @name.setter
    def name(self, value):
        self._name = value


class Transaction(object):
    def __init__(self, database, rw=False):
        """
        Class to represent a transaction on the selected database
        :param Database database: the database to which the transaction is
        bound to
        """
        self.database = database
        self.rw = rw
        self.begin()

    def begin(self):
        rassrvr_begin_transaction(self.database.stub,
                                  self.database.connection.session.clientId,
                                  self.rw)

    def commit(self):
        rassrvr_commit_transaction(self.database.stub,
                                   self.database.connection.session.clientId)

    def abort(self):
        rassrvr_abort_transaction(self.database.stub,
                                  self.database.connection.session.clientId)

    def query(self, query_str):
        """
        Returns a new query object initialized with this transaction and the
        query string
        :param str query_str: the query to be executed
        :rtype: Query
        :return: a new query object
        """
        query = Query(self, query_str)
        return query

    def get_collection(self, name):
        """
        Returns a Collection object using this transaction
        :param name: name of the collection
        :return:
        """
        collection = Collection(self, name)
        if not collection:
            raise Exception("Can't get or create Collection from name")
        return collection


class Query(object):
    def __init__(self, transaction, query_str, file_path=None, mdddomain=None, mddtype=None):
        """
        Class to represent a rasql query that can be executed in a certain
        transaction
        :param transaction: the transaction to which the query is bound to
        :param query_str: the query as a string
        """
        self.transaction = transaction
        self.query_str = query_str.strip()
        self.mdd_constants = None
        self.exec_query_resp = None

    def execute_write_with_file(self):
        """
        Execute a full request query (not only the rasql query from user input) to rasserver which contains
        all the information about the MDD Array from the file to be used to update rasdaman collection.
        :param str query: a full request query to be sent to rasserver
        :return: response from server
        """
        # Send the request query and get the response from rasserver
        exec_update_query_from_file_resp = rassrvr_begin_streamed_http_query(
                                        self.transaction.database.stub,
                                        self.transaction.database.connection.session.clientUUID,
                                        self.query_str)

        return exec_update_query_from_file_resp

    def execute_update(self):
        """
        Executes the query with write permission and returns back a result
        :return: the resulting status returned by the query
        """
        if self.transaction.rw is False:
            raise Exception("Transaction does not have write access")

        if self.mdd_constants is not None:
            pass

        exec_update_query_resp = rassrvr_execute_update_query(
            self.transaction.database.stub,
            self.transaction.database.connection.session.clientId,
            self.query_str)
        if exec_update_query_resp.status == 2 or \
                        exec_update_query_resp.status == 3:
            error_message = ExceptionFactories.create_error_message(exec_update_query_resp.erroNo, exec_update_query_resp.lineNo,
                                                                    exec_update_query_resp.colNo, exec_update_query_resp.token)
            raise Exception("Error executing query '{}', error message '{}'".format(self.query_str, error_message))
        if exec_update_query_resp.status == 1:
            raise Exception("Error: Unknown Client")
        if exec_update_query_resp.status > 3:
            raise Exception("Error: Transfer failed")
        return exec_update_query_resp.status

    def execute_read(self):
        """
        Executes the query with read permission and returns back a result
        :return: the resulting array returned by the query
        :rtype: result object according to query
        """
        exec_query_resp = rassrvr_execute_query(self.transaction.database.stub,
                                                self.transaction.database.connection.session.clientId,
                                                self.query_str)
        self.exec_query_resp = exec_query_resp
        select_collection_names = "RAS_COLLECTIONNAMES" in self.query_str
        if exec_query_resp.status == 4 or exec_query_resp.status == 5:
            error_message = ExceptionFactories.create_error_message(exec_query_resp.err_no, exec_query_resp.line_no,
                                                                    exec_query_resp.col_no, exec_query_resp.token)
            raise Exception("Error executing query '{}', error message '{}'".format(self.query_str, error_message))
        elif exec_query_resp.status == 0 and select_collection_names:
            # e.g: query: select c from RAS_COLLECTIONNAMES as c
            return self._get_list_collection_names()
        elif exec_query_resp.status == 0:
            # e.g: query: select c + 1 from test_mr as c, select encode(c, "png") from test_mr as c
            return self._get_collection_result()
        elif exec_query_resp.status == 1:
            # e.g: query: select complex(35, 56), select sdom(c) from test_mr as c, select {1, 2, 3}
            return self._get_element_result()
        elif exec_query_resp.status == 2:
            # Query can return empty collection (e.g: select c from c where all_cells( c > 20 )) which returns empty
            pass
        else:
            raise Exception("Unknown status code: " + str(
                exec_query_resp.status) + " returned by ExecuteQuery")

    def __get_array_result_mdd(self):
        """
        With query to return mdds, use this method to collect all the binary data from tiles in an array
        :return: an array of binary string
        """
        result_data = []
        mddstatus = 0

        while mddstatus == 0:
            mddresp = rassrvr_get_next_mdd(self.transaction.database.stub,
                                           self.transaction.database.connection.session.clientId)
            mddstatus = mddresp.status
            if mddstatus == 2:
                raise Exception("getMDDCollection - no transfer or empty collection")

            tilestatus = 2
            while tilestatus == 2 or tilestatus == 3:
                tileresp = rassrvr_get_next_tile(self.transaction.database.stub,
                                                 self.transaction.database.connection.session.clientId)
                tilestatus = tileresp.status
                if tilestatus == 4:
                    raise Exception("rpcGetNextTile - no tile to transfer or empty collection")
                else:
                    data = tileresp.data
                    result_data.append(data)

            if tilestatus == 0:
                break

        # Close connection to server
        rassrvr_end_transfer(self.transaction.database.stub, self.transaction.database.connection.session.clientId)
        return result_data

    def _get_list_collection_names(self):
        """
        Return the list of collection names from RASBASE for query (select c from RAS_COLLECTIONNAMES as c)
        :return: list of string collection names
        """
        arr_temp = self.__get_array_result_mdd()
        for i, collection_name in enumerate(arr_temp):
            arr_temp[i] = str(collection_name)[:-1]
        return arr_temp

    def _get_collection_result(self):
        """
        Parse the string binary containing data (encoded, unencoded) from rasql to objects which later can be
        translated to numpy array.
        Or returns the list of rasdaman collections in RASBASE.
        :return: ResultArray object
        """
        spatial_domain = get_spatial_domain_from_type_structure(self.exec_query_resp.type_structure)
        band_types = get_type_structure_from_string(self.exec_query_resp.type_structure)
        metadata = ArrayMetadata(spatial_domain, band_types)

        # concatenate all the binary strings to one string to be decoded as Numpy ndarray buffer
        arr_temp = self.__get_array_result_mdd()
        result_data = "".join(arr_temp)

        data_type = metadata.band_types["type"]
        # NOTE: unencoded result array is nD+ numpy array. If number of bands > 1 then the number of dimensions + 1
        number_of_bands = 1
        if data_type == "struct":
            data_type = metadata.band_types["sub_type"]["types"][0]
            number_of_bands = len(metadata.band_types["sub_type"]["types"])

        result_array = ResultArray(result_data, metadata.spatial_domain, data_type, number_of_bands)
        return result_array

    def _get_element_result(self):
        """
        Get the response from rasserver for query which doesn't return array which can be converted to Numpy ndarray
        e.g: select 1 + 2, select {1, 2, 3}
        :return: array of results if server returns multiple elements or just 1 result if only 1 element
        """
        rpcstatus = 0
        result_arr = []

        spatial_domain = get_spatial_domain_from_type_structure(self.exec_query_resp.type_structure)
        band_types = get_type_structure_from_string(self.exec_query_resp.type_structure)
        metadata = ArrayMetadata(spatial_domain, band_types)

        while rpcstatus == 0:
            elemresp = rassrvr_get_next_element(self.transaction.database.stub,
                                                self.transaction.database.connection.session.clientId)
            rpcstatus = elemresp.status
            if rpcstatus == 2:
                raise Exception("getNextElement - no transfer or empty element")
            # e.g: select 2 + 3, select complex(3, 5)
            result = convert_binary_data_stream(metadata.band_types, elemresp.data)
            result_arr.append(result)
        if len(result_arr) > 1:
            # e.g: select sdom(c) from test_mr with test_mr has 2 collections, result is an array
            return result_arr
        else:
            # e.g: select 1 from test_mr, result is only a value
            return result_arr[0]

    def _send_mdd_constants(self):
        exec_init_update_resp = rassrvr_init_update(
            self.transaction.database.stub,
            self.transaction.database.connection.session.clientId)
        if exec_init_update_resp.status is not 1:
            raise Exception(
                "Error: Transfer Failed. ExecInitUpdate returned with a "
                "non-zero status: " + str(
                    exec_init_update_resp.status))

        for mdd in self.mdd_constants:
            insert_trans_mdd_resp = rassrvr_start_insert_trans_mdd(
                self.transaction.database.stub,
                self.transaction.database.connection.session.clientId,
                mdd.domain, mdd.type_length, mdd.type_name)
            if insert_trans_mdd_resp.status is 0:
                insert_tile_resp = rassrvr_insert_tile(
                    self.transaction.database.stub,
                    self.transaction.database.connection.session.clientId,
                    self.transaction.rw,
                    mdd.domain, mdd.type_length, mdd.current_format,
                    mdd.storage_format, mdd.data,
                    mdd.data_length)
                if insert_tile_resp.status > 0:
                    raise Exception("Error: Transfer failed")
            elif insert_trans_mdd_resp.status is 2:
                raise Exception("Error: Database Class Undefined")
            elif insert_trans_mdd_resp.status is 3:
                raise Exception("Error: Invalid Type")
            else:
                raise Exception("Error: Transfer failed")


class ArrayMetadata(object):
    def __init__(self, spatial_domain, band_types):
        """
        Class to represent the metadata associated to an array
        :param SpatialDomain spatial_domain: the spatial domain in which the
        array is represented
        :param dict[str, BandType] band_types: a dictionary containing the
        name of each band and its type
        """
        self.spatial_domain = spatial_domain
        self.band_types = band_types

def insecure_channel(host, port):
    """
    Override grpc.beta.implementations.insecure_channel function in 
    oder to to set max_message_length
    """
    channel = grpc.insecure_channel(
        target=host if port is None else '%s:%d' % (host, port),
        options=[(cygrpc.ChannelArgKey.max_send_message_length, -1),
                 (cygrpc.ChannelArgKey.max_receive_message_length, -1)])
    return grpc.beta.implementations.Channel(channel)
