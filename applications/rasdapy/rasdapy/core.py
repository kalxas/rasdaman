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

import numpy as np
from grpc.beta import implementations
from rasdapy.utils import StoppableTimeoutThread, \
    get_spatial_domain_from_type_structure, get_type_structure_from_string, \
    convert_data_stream_from_bin
from rasdapy.remote_procedures import rasmgr_close_db, rasmgr_connect, \
    rasmgr_disconnect, rasmgr_keep_alive, rasmgr_open_db, \
    rassrvr_abort_transaction, rassrvr_begin_streamed_http_query, \
    rassrvr_begin_transaction, rassrvr_close_db, rassrvr_commit_transaction, \
    rassrvr_create_db, rassrvr_delete_collection_by_id, \
    rassrvr_delete_collection_by_name, rassrvr_destroy_db, \
    rassrvr_end_insert_mdd, rassrvr_end_transfer, rassrvr_execute_http_query, \
    rassrvr_execute_insert_query, rassrvr_execute_query, \
    rassrvr_execute_update_query, rassrvr_get_collection_by_id, \
    rassrvr_get_collection_by_name, rassrvr_get_collection_oids_by_id, \
    rassrvr_get_collection_oids_by_name, rassrvr_get_new_oid, \
    rassrvr_get_next_element, rassrvr_get_next_mdd, \
    rassrvr_get_next_streamed_http_query, rassrvr_get_next_tile, \
    rassrvr_get_object_type, rassrvr_get_type_structure, rassrvr_init_update, \
    rassrvr_insert_collection, rassrvr_insert_tile, \
    rassrvr_is_transaction_open, \
    rassrvr_keep_alive, rassrvr_open_db, \
    rassrvr_remove_object_from_collection, \
    rassrvr_set_format, rassrvr_start_insert_mdd, rassrvr_start_insert_trans_mdd
from rasdapy.stubs import client_rassrvr_service_pb2 as rassrvr
from rasdapy.stubs import rasmgr_client_service_pb2 as rasmgr


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
        self.channel = implementations.insecure_channel(hostname, port)
        self.stub = rasmgr.beta_create_RasmgrClientService_stub(self.channel)
        self.session = None
        self._rasmgr_keep_alive_running = None
        self._keep_alive_thread = None
        self.connect()

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
        self.open()

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
        self.channel = implementations.insecure_channel(
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
    def __init__(self, transaction, query_str):
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

    def eval(self):
        tmp_query = self.query_str.upper()
        if tmp_query.startswith("SELECT ") and not " INTO " in tmp_query:
            # Only select, not select .... into collection
            return self._execute_read()
        else:
            # select ... into, create, update, delete, drop
            return self._execute_update()

    def _execute_update(self):
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
            raise Exception(
                "Error executing query: err_no = " + str(
                    exec_update_query_resp.erroNo) + ", line_no = " +
                str(
                    exec_update_query_resp.lineNo) + ", col_no = " +
                str(
                    exec_update_query_resp.colNo) + ", token = " +
                exec_update_query_resp.token)
        if exec_update_query_resp.status == 1:
            raise Exception("Error: Unknown Client")
        if exec_update_query_resp.status > 3:
            raise Exception("Error: Transfer failed")
        return exec_update_query_resp.status

    def _execute_read(self):
        """
        Executes the query with read permission and returns back a result
        :return: the resulting array returned by the query
        :rtype: Array
        """
        exec_query_resp = rassrvr_execute_query(self.transaction.database.stub,
                                                self.transaction.database.connection.session.clientId,
                                                self.query_str)
        self.exec_query_resp = exec_query_resp
        if exec_query_resp.status == 4 or exec_query_resp.status == 5:
            raise Exception("Error executing query: err_no = " + str(
                exec_query_resp.err_no) + ", line_no = " + str(
                exec_query_resp.line_no) + ", col_no = " + str(
                exec_query_resp.col_no) + ", token = " +
                            exec_query_resp.token)
        elif exec_query_resp.status == 0:
            return self._get_next_collection()
        elif exec_query_resp.status == 1:
            return self._get_next_element()
        elif exec_query_resp.status == 2:
            raise Exception("Query returned an empty collection")
        else:
            raise Exception("Unknown status code: " + str(
                exec_query_resp.status) + " returned by ExecuteQuery")

    def _get_next_collection(self):
        mddstatus = 0
        tilestatus = 0
        array = []
        metadata = ArrayMetadata(
            spatial_domain=SpatialDomain(
                get_spatial_domain_from_type_structure(
                    self.exec_query_resp.type_structure)),
            band_types=get_type_structure_from_string(
                self.exec_query_resp.type_structure))
        while mddstatus == 0:
            mddresp = rassrvr_get_next_mdd(self.transaction.database.stub,
                                           self.transaction.database.connection.session.clientId)
            mddstatus = mddresp.status
            if mddstatus == 2:
                raise Exception(
                    "getMDDCollection - no transfer or empty collection")
            tilestatus = 2
            while tilestatus == 2 or tilestatus == 3:
                tileresp = rassrvr_get_next_tile(self.transaction.database.stub,
                                                 self.transaction.database.connection.session.clientId)
                tilestatus = tileresp.status
                if tilestatus == 4:
                    raise Exception(
                        "rpcGetNextTile - no tile to transfer or empty "
                        "collection")
                else:
                    if self.query_str == "select r from RAS_COLLECTIONNAMES " \
                                         "as r":
                        array.append(tileresp.data[:-1])
                    else:
                        array.append(RPCMarray(domain=tileresp.domain,
                                               cell_type_length=tileresp.cell_type_length,
                                               current_format=tileresp.current_format,
                                               storage_format=tileresp.storage_format,
                                               data=convert_data_stream_from_bin(
                                                   metadata.band_types,
                                                   tileresp.data,
                                                   tileresp.data_length,
                                                   tileresp.cell_type_length,
                                                   metadata.spatial_domain)))

            if tilestatus == 0:
                break
        rassrvr_end_transfer(self.transaction.database.stub,
                             self.transaction.database.connection.session
                             .clientId)
        if self.query_str == "select r from RAS_COLLECTIONNAMES as r":
            return array
        else:
            return Array(values=array, metadata=metadata)

    def _get_next_element(self):
        rpcstatus = 0
        array = []
        metadata = ArrayMetadata(
            spatial_domain=SpatialDomain(
                get_spatial_domain_from_type_structure(
                    self.exec_query_resp.type_structure)),
            band_types=get_type_structure_from_string(
                self.exec_query_resp.type_structure))
        while rpcstatus == 0:
            elemresp = rassrvr_get_next_element(self.transaction.database.stub,
                                                self.transaction.database.connection.session.clientId)
            rpcstatus = elemresp.status
            if rpcstatus == 2:
                raise Exception("getNextElement - no transfer or empty element")
            array.append(
                convert_data_stream_from_bin(metadata.band_types,
                                             elemresp.data,
                                             elemresp.data_length,
                                             elemresp.data_length,
                                             metadata.spatial_domain))
        return array

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



class RPCMarray(object):
    """
    Class to represent RPC Array
    """

    def __init__(self, domain=None, cell_type_length=None, current_format=None,
                 storage_format=None, data=None):
        self.domain = domain
        self.cell_type_length = cell_type_length
        self.current_format = current_format
        self.storage_format = storage_format
        self.data = data

    def to_array(self):
        if type == "numpy":
            return np.frombuffer(self.data)
        elif type == "scipy":
            raise NotImplementedError("No support for SciPy yet")
        elif type == "pandas":
            raise NotImplementedError("No Support for Pandas yet")
        else:
            raise NotImplementedError(
                "Invalid type: only valid types are 'numpy' (default), "
                "'scipy', and 'pandas'")


class BandType(object):
    """
    Enum containing possible band types in rasdaman
    """
    INVALID = 0,
    CHAR = 1,
    USHORT = 2,
    SHORT = 3,
    ULONG = 4,
    LONG = 5,
    FLOAT = 6,
    DOUBLE = 7


class SpatialDomain(object):
    def __init__(self, interval_params):
        """
        Class to represent a spatial domain in rasdaman
        :param list[tuple] interval_parameters: a list of intervals
        represented as tuples
        """
        self.interval_params = interval_params


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


class Array(object):
    def __init__(self, metadata=None, values=None):
        """
        Class to represent an array produced by a rasdaman query
        :param ArrayMetadata metadata: the metadata of the array
        :param list[list[int | float]] values: the values of the array stored
        band-interleaved (we store a list of
        values for each band)
        """
        self.metadata = metadata
        self.values = values

    def to_image(self, filename, normalize=False):
        import matplotlib
        matplotlib.use('Agg')
        import matplotlib.pyplot as plt
        plt.imshow(self.to_array()[0], interpolation='nearest')
        plt.savefig(filename)
        plt.close()

    def to_array(self, type="numpy"):
        """
        Returns the serialized array as a numpy, scipy, or pandas data structure
        :param type: valid option - "numpy", "scipy", "pandas"
        :return:
        """
        if type == "numpy":
            nparr = np.array([val.data for val in self.values])
            return nparr
        elif type == "scipy":
            raise NotImplementedError("No support for SciPy yet")
        elif type == "pandas":
            raise NotImplementedError("No Support for Pandas yet")
        else:
            raise NotImplementedError(
                "Invalid type: only valid types are 'numpy' (default), "
                "'scipy', and 'pandas'")
