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

import grpc


from rasdapy.cores.utils import StoppableTimeoutThread, \
    get_spatial_domain_from_type_structure, get_type_structure_from_string, \
    convert_binary_data_stream
from rasdapy.cores.remote_procedures import *
from rasdapy.stubs.rasmgr_client_service_pb2_grpc import RasmgrClientServiceStub
from rasdapy.stubs.client_rassrvr_service_pb2_grpc import ClientRassrvrServiceStub
from rasdapy.models.result_array import ResultArray
from rasdapy.cores.utils import convert_data_from_bin, encoded_bytes_to_str
from rasdapy.query_result import QueryResult
from rasdapy.models.ras_gmarray import RasGMArray
from rasdapy.models.minterval import MInterval
from rasdapy.models.mdd_types import rDataFormat
from rasdapy.models.tile_assigner import TileAssigner



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
        options = [
            ('grpc.max_send_message_length', 100 * 1024 * 1024),
            ('grpc.max_receive_message_length', 100 * 1024 * 1024)
        ]
        self.channel = grpc.insecure_channel(f"{hostname}:{port}", options=options)
        self.stub = RasmgrClientServiceStub(self.channel)
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
                #self._keep_alive_thread.join()
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
        options = [
            ('grpc.max_send_message_length', 100 * 1024 * 1024),
            ('grpc.max_receive_message_length', 100 * 1024 * 1024)
        ]
        self.channel = grpc.insecure_channel(f"{self.rasmgr_db.serverHostName}:{self.rasmgr_db.port}", options=options)
        self.stub = ClientRassrvrServiceStub(self.channel)
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
                #self._keep_alive_thread.join()
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
        self.query_str = query_str
        self.mdd_constants = None

    def execute_streamed_http(self):
        """
        Execute a full request query (not only the rasql query from user input) to rasserver which contains
        all the information about the MDD Array from the file to be used to update rasdaman collection.
        :param str query: a full request query to be sent to rasserver
        :return: response from server
        """
        qr = QueryResult()
        # Send the request query and get the response from rasserver
        exec_update_query_from_file_resp = rassrvr_begin_streamed_http_query(
                                        self.transaction.database.stub,
                                        self.transaction.database.connection.session.clientUUID,
                                        self.query_str)

        qr.from_streamed_response(exec_update_query_from_file_resp)
        return qr

    def execute_update(self):
        """
        Executes the query with write permission and returns back a result
        :return: the resulting status returned by the query
        """

        qr = QueryResult()
        if self.transaction.rw is False:
            raise Exception("Transaction does not have write access")

        if self.mdd_constants is not None:
            self._send_mdd_constants()

        exec_update_query_resp = rassrvr_execute_update_query(
            self.transaction.database.stub,
            self.transaction.database.connection.session.clientId,
            self.query_str)
        if exec_update_query_resp.status == 2 or \
                        exec_update_query_resp.status == 3:
            qr.with_error = True
            qr.err_no = exec_update_query_resp.erroNo
            qr.line_no = exec_update_query_resp.lineNo
            qr.col_no = exec_update_query_resp.colNo
            qr.token = exec_update_query_resp.token
        if exec_update_query_resp.status == 1:
            raise Exception("Error: Unknown Client")
        if exec_update_query_resp.status > 3:
            raise Exception("Error: Transfer failed")
        return qr

    def execute_insert(self):

        qr = QueryResult()

        if self.transaction.rw is False:
            raise Exception("Transaction does not have write access")

        if self.mdd_constants is not None:
            self._send_mdd_constants()

        exec_insert_query_resp = rassrvr_execute_insert_query(
            self.transaction.database.stub,
            self.transaction.database.connection.session.clientId,
            self.query_str
        )

        if exec_insert_query_resp.status == 0:
            qr.elements = self._get_mdd_collection()
        elif exec_insert_query_resp.status == 1:
            type_struct = get_type_structure_from_string(exec_insert_query_resp.type_structure)
            qr.elements = self._get_element_collection(type_struct)
        elif exec_insert_query_resp.status == 2:
            # empty result, should not be treated as default case
            pass
        else:
            print(
                f"Internal error: RasnetClientComm::executeQuery(): illegal status value {exec_insert_query_resp.status}")
            qr.with_error = True
            qr.err_no = exec_insert_query_resp.erroNo
            qr.line_no = exec_insert_query_resp.lineNo
            qr.col_no = exec_insert_query_resp.colNo
            qr.token = exec_insert_query_resp.token

        return qr

    def execute_read(self):
        """
        Executes the query with read permission and returns back a result
        :return: the resulting array returned by the query
        :rtype: result object according to query
        """
        exec_query_resp = rassrvr_execute_query(self.transaction.database.stub,
                                                self.transaction.database.connection.session.clientId,
                                                self.query_str)

        if exec_query_resp.status == 4 or exec_query_resp.status == 5:
            error_message = ExceptionFactories.create_error_message(exec_query_resp.err_no, exec_query_resp.line_no,
                                                                    exec_query_resp.col_no, exec_query_resp.token)
            raise Exception("Error executing query '{}', error message '{}'".format(self.query_str, error_message))
        elif exec_query_resp.status == 0:

            tmp_query = self.query_str.upper()
            # e.g: query: select c from RAS_COLLECTIONNAMES as c
            if "RAS_COLLECTIONNAMES" in tmp_query or \
                    "RAS_STRUCT_TYPES" in tmp_query or \
                    "RAS_MARRAY_TYPES" in tmp_query or \
                    "RAS_SET_TYPES" in tmp_query or \
                    "DBINFO" in tmp_query:
                return self._get_list_collection()

            # e.g: query: select c + 1 from test_mr as c, select encode(c, "png") from test_mr as c
            return self._get_collection_result(exec_query_resp)
        elif exec_query_resp.status == 1:
            # e.g: query: select complex(35, 56), select sdom(c) from test_mr as c, select {1, 2, 3}
            return self._get_element_result(exec_query_resp)
        elif exec_query_resp.status == 2:
            # Query can return empty collection (e.g: select c from c where all_cells( c > 20 )) which returns empty
            return ResultArray("string") # empty array
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

            tilestatus, array = self._get_mdd_core(mddresp)
            result_data.append(array)

            if tilestatus == 0:
                break

        rassrvr_end_transfer(self.transaction.database.stub, self.transaction.database.connection.session.clientId)

        return result_data

    def _get_mdd_core(self, mdd_resp):

        if mdd_resp.current_format != rDataFormat.r_Array.value:
            # So far, for other format than binary array, only one tile is used.
            tileresp = rassrvr_get_next_tile(self.transaction.database.stub,
                                             self.transaction.database.connection.session.clientId)
            tile_status = tileresp.status
            if tile_status == 3:
                raise Exception(f"multipart format {mdd_resp.current_format} is not handled")

            if tile_status == 4:
                raise Exception("rpcGetNextTile - no tile to transfer or empty collection")
            return tile_status, tileresp.data

        # At that point, we try to read a binary array.

        mdd_result = None
        tile_idx = 0
        tile_assigner = None

        read_size = 0

        tile_status = 2  # call get_next_tile at least once
        while tile_status == 2 or tile_status == 3:
            tileresp = rassrvr_get_next_tile(self.transaction.database.stub,
                                             self.transaction.database.connection.session.clientId)
            tile_status = tileresp.status
            if tile_status == 4:
                raise Exception("rpcGetNextTile - no tile to transfer or empty collection")

            if mdd_result is None:
                mdd_result = RasGMArray(
                    spatial_domain=MInterval.from_str(mdd_resp.domain),
                    type_length=tileresp.cell_type_length)
                #print(f"full domain {mdd_result.spatial_domain} with format {mdd_result.format}")
                #print(f"need to read {mdd_result.byte_size} bytes")

            tile_idx += 1
            #print(f"tile {tile_idx}")

            # Creates current tile array
            mdd_tile = RasGMArray(
                spatial_domain=MInterval.from_str(tileresp.domain),
                type_length=mdd_result.type_length
            )

            if tile_status == 3:
                tile_status = self.read_tile_by_parts(mdd_tile, tileresp.data)
            else:
                mdd_tile.data = tileresp.data

            # At that point, data array of the mdd_tile should be complete for its spatial_domain

            read_size += len(mdd_tile.data)
            #print(f"read {read_size} bytes so far")
            #print(f"tile domain {mdd_tile.spatial_domain}")

            if tile_status < 2 and tile_idx == 1 and str(mdd_tile.spatial_domain) == str(mdd_result.spatial_domain):
                # MDD consists of just one tile that is the same size of the mdd
                mdd_result.data = mdd_tile.data
            else:
                # MDD consists of more than one tile or the tile does not cover the whole domain
                if tile_idx == 1:
                    mdd_result.data = bytearray(mdd_result.byte_size)
                    tile_assigner = TileAssigner(mdd_result)
                    tile_assigner.start()

                # copy tile data into global MDD data space
                # optimized, relying on the internal representation of an MDD
                tile_assigner.add_tile(mdd_tile)

        if tile_assigner is not None:
            tile_assigner.go_on = False
            tile_assigner.join()

        return tile_status, mdd_result.data


    def read_tile_by_parts(self, mdd_tile, data):
        """
        When the tile itself is send by parts, direct assembly of all parts in
        the mdd_tile
        :param mdd_tile: The mdd_tile to put parts into
        :param data: the first data array from the first part
        :return:
        """
        mdd_tile.data = bytearray(mdd_tile.byte_size)

        # first part
        bloc_size = len(data)
        mdd_tile.data[0:bloc_size] = data
        offset = bloc_size

        # read remaining parts
        tilestatus = 3
        while tilestatus == 3:
            tileresp = rassrvr_get_next_tile(self.transaction.database.stub,
                                             self.transaction.database.connection.session.clientId)
            tilestatus = tileresp.status
            if tilestatus == 4:
                raise Exception("rpcGetNextTile - no tile to transfer or empty collection")
            bloc_size = len(tileresp.data)
            mdd_tile.data[offset:offset+bloc_size] = tileresp.data
            offset += bloc_size

        return tilestatus


    def _get_list_collection(self):
        """
        Return the list of collection names from RASBASE for query (select c from RAS_COLLECTIONNAMES as c)
        :return: list of string collection names
        """
        result_array = ResultArray("string")
        for r in self.__get_array_result_mdd():
            result_array.add_data(encoded_bytes_to_str(r))
        return result_array

    def _get_collection_result(self, exec_query_resp):
        """
        Parse the string binary containing data (encoded, unencoded) from rasql to objects which later can be
        translated to numpy array.
        Or returns the list of rasdaman collections in RASBASE.
        :return: ResultArray object
        """
        spatial_domain = get_spatial_domain_from_type_structure(exec_query_resp.type_structure)
        band_types = get_type_structure_from_string(exec_query_resp.type_structure)

        # concatenate all the binary strings to one string to be decoded as Numpy ndarray buffer
        data_list = self.__get_array_result_mdd()
        data = b''.join(data_list)

        data_type = band_types["type"]
        # NOTE: unencoded result array is nD+ numpy array. If number of bands > 1 then the number of dimensions + 1
        number_of_bands = 1
        if data_type == "struct":
            data_type = band_types["sub_type"]["types"][0]
            number_of_bands = len(band_types["sub_type"]["types"])

        res_array = ResultArray(data_type, spatial_domain, number_of_bands)
        res_array.add_data(data)

        return res_array

    def _get_element_result(self, exec_query_resp):
        """
        Get the response from rasserver for query which doesn't return array which can be converted to Numpy ndarray
        e.g: select 1 + 2, select {1, 2, 3}
        :return: array of results if server returns multiple elements or just 1 result if only 1 element
        """
        rpcstatus = 0

        band_types = get_type_structure_from_string(exec_query_resp.type_structure)
        result_arr = ResultArray(band_types['type'], is_object=False)

        while rpcstatus == 0:
            elemresp = rassrvr_get_next_element(self.transaction.database.stub,
                                                self.transaction.database.connection.session.clientId)
            rpcstatus = elemresp.status
            if rpcstatus == 2:
                raise Exception("getNextElement - no transfer or empty element")
            # e.g: select 2 + 3, select complex(3, 5)
            result = convert_binary_data_stream(band_types, elemresp.data)
            result_arr.add_data(result)

        return result_arr


    def _get_element_collection(self, type_struct):
        rpc_status = 0

        elements = []
        while rpc_status == 0:
            resp = rassrvr_get_next_element(
                self.transaction.database.stub,
                self.transaction.database.connection.session.clientId
            )

            rpc_status = resp.status;

            if rpc_status == 2:
                raise Exception("Error: Transfer failed")

            elements.append(convert_data_from_bin(type_struct['type'], resp.data))
        rassrvr_end_transfer(
            self.transaction.database.stub,
            self.transaction.database.connection.session.clientId
        )

        return elements

    def _get_mdd_collection(self):
        mdd_status = 0
        while  mdd_status == 0:
            result = rassrvr_get_next_mdd(
                    self.transaction.database.stub,
                    self.transaction.database.connection.session.clientId
            )

            mdd_status == result.status

            if mdd_status == 2:
                raise Exception("Error: _get_mdd_collection(...) - no transfer collection or empty transfer collection")

            tile_status = 2
            while tile_status == 2 or tile_status == 3:
                repl = rassrvr_get_next_tile(
                    self.transaction.database.stub,
                    self.transaction.database.connection.session.clientId
                )

                tile_status == repl.status

                if repl.status == 4:
                    raise Exception("Error: rassrvr_get_next_tile(...) - no tile to transfer or empty transfer collection")

        rassrvr_end_transfer(
            self.transaction.database.stub,
            self.transaction.database.connection.session.clientId
        )

    def _send_mdd_constants(self):

        for ras_array in self.mdd_constants:

            exec_init_update_resp = rassrvr_init_update(
                self.transaction.database.stub,
                self.transaction.database.connection.session.clientId)
            if exec_init_update_resp.status > 0:
                raise Exception(
                    "Error: Transfer Failed. ExecInitUpdate returned with a "
                    "non-zero status: " + str(
                        exec_init_update_resp.status))

            insert_trans_mdd_resp = rassrvr_start_insert_trans_mdd(
                self.transaction.database.stub,
                self.transaction.database.connection.session.clientId,
                str(ras_array.spatial_domain),
                ras_array.type_length,
                ras_array.type_name)

            if insert_trans_mdd_resp.status > 0:
                raise Exception("Error: Transfer failed")

            mdd_itr = ras_array.decompose_mdd()
            for mdd in mdd_itr:
                insert_tile_resp = rassrvr_insert_tile(
                    self.transaction.database.stub,
                    self.transaction.database.connection.session.clientId,
                    0,
                    str(mdd.spatial_domain),
                    mdd.type_length,
                    mdd.format.value,
                    mdd.storage_layout.format.value,
                    mdd.data,
                    mdd.data_length)
                if insert_tile_resp.status > 0:
                    raise Exception("Error: Transfer failed")

            end_insert_resp = rassrvr_end_insert_mdd(
                self.transaction.database.stub,
                self.transaction.database.connection.session.clientId,
                False
            )
            if end_insert_resp.status > 0:
                raise Exception("Error: Transfer failed")
