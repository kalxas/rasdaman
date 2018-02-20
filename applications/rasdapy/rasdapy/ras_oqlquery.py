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
from rasdapy.models.ras_gmrray import RasGMArray
from rasdapy.cores.utils import int_to_bytes, str_to_encoded_bytes, get_tiling_domain, convert_data_from_bin


class RasOQLQuery(object):

    BIG_ENDIAN = 0
    LITTLE_ENDIAN = 1

    __COMMAND_QUERY_EXEC = 8
    __COMMAND_UPDATE_QUERY_EXEC = 9
    __COMMAND_INSERT_QUERY_EXEC = 11

    """
    The interface to an OQL query object.
    """

    def __init__(self, db_connector):
        """
        :param DBConnector db_connector: an opened connection to rasdaman server
        """
        self.db_connector = db_connector
        self.query = None
        self.params = []

    def __get_transfer_encoding(self, gmarray):
        """
        Returns a byte array representing the GMArray. This byte array is used for uploading insert into query from file
        to rasserver.
        e.g: "insert into test_grey3D values $1" -f "/home/rasdaman/tmp/50k.bin" --mdddomain [0:99,0:99,0:4] --mddtype GreyCube
        translated to:
        QueryString=insert into test_rasj values #MDD1#&Endianess=0
        &NumberOfQueryParameters=1&BinDataSize=414&BinData=GreyImage[0:18,0:18][0:10,0:10]BINARY_DATA_FROM_FILE
        :param RasGMArray gmarray: containing the information about the marray to be inserted to rasdaman collection
        :return str result: a binary string to be sent to rasserver for inserting marray from file to rasdaman collection
        """
        tmp_arr = [int_to_bytes(1), str_to_encoded_bytes(gmarray.type_name), str_to_encoded_bytes(""),
                   int_to_bytes(gmarray.type_length), str_to_encoded_bytes(gmarray.spatial_domain),
                   str_to_encoded_bytes(gmarray.storage_layout.spatial_domain), str_to_encoded_bytes("||0.0"),
                   int_to_bytes(len(gmarray.data))]
        result = "".join(tmp_arr)
        result = result + gmarray.data
        return result

    def create(self, query):
        """
        :param str query: rasql query to be initialized.
        """
        self.query = str(query.strip())

    def bind(self, param):
        """
        Add this input param to internal list of parameters
        :param param: object input parameter as a part of Rasql query (e.g: --mdddomain, --mddtype)
        """
        self.params.append(param)

    def execute(self):
        """
        Execute the OQL query
        :return: object depends on the type of query (insert/select/update...)
        """
        mdd_data = ""
        for i, param in enumerate(self.params):
            tmp = str(i + 1)
            if type(param) is RasGMArray:
                # This object contains the necessary informations for updating rasdaman collection from files
                mdd_data = self.__get_transfer_encoding(param)
                self.query = self.query.replace("$" + tmp, "#MDD" + tmp + "#")
                mdd_data += mdd_data
            else:
                # no MDD parameter => substitute each occurence of the
                # corresponding $ parameter in the query string with the
                # value of this parameter
                self.query = self.query.replace("$" + tmp, str(param))

        # Check what kind of internal helper methods should be used based on rasql query
        tmp_query = self.query.upper()

        if tmp_query.startswith("SELECT") and "INTO" not in tmp_query:
            # it is select query
            txn = self.db_connector.db.transaction(rw=False)
            query = txn.query(self.query)
            res = query.execute_read()
            txn.commit()

            return res
        elif mdd_data != "":
            # this one is important for rasserver to handle query properly by command id
            command_id = self.__COMMAND_INSERT_QUERY_EXEC
            if tmp_query.startswith("UPDATE"):
                command_id = self.__COMMAND_UPDATE_QUERY_EXEC

            # it is a write query to insert/update collection with MDD data from file
            # e.g: "insert into $TEST_SUBSETTING_1D values \$1" -f "$TESTDATA_PATH/101.bin"
            # --mdddomain "[0:100]" --mddtype GreyString
            # or:
            # "update $COLL_NAME as m set m[1,*:*,*:*] assign (double) \$1" -f "$QUERY_SCRIPT_DIR"/400k.bin
            # --mdddomain [0:499,0:99] --mddtype DoubleImage
            txn = self.db_connector.db.transaction(rw=True)
            # the full query to send to rasserver
            request_query = "Command={}&ClientID={}&QueryString={}&Endianess={}" \
                            "&NumberOfQueryParameters={}&BinDataSize={}&BinData={}".format(
                                command_id, txn.database.connection.session.clientId, self.query,
                                self.BIG_ENDIAN, len(self.params), len(mdd_data), mdd_data)
            query = txn.query(request_query)
            res = query.execute_write_with_file()
            txn.commit()

            return res
        else:
            # it is a write query  (e.g: create collection, drop collection,...)
            txn = self.db_connector.db.transaction(rw=True)
            query = txn.query(self.query)
            res = query.execute_update()
            txn.commit()

            return res

