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
from rasdapy.models.ras_gmarray import RasGMArray
from rasdapy.cores.utils import int_to_bytes, str_to_encoded_bytes, get_tiling_domain, convert_data_from_bin
from enum import Enum


class QueryType(Enum):
    SELECT = 1,
    INSERT = 2,
    UPDATE = 3


class RasOQLQuery(object):

    BIG_ENDIAN = 0
    LITTLE_ENDIAN = 1


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

    def reset(self):
        self.params = []
        self.query = None

    def execute(self):
        """
        Execute the OQL query
        :return: object depends on the type of query (insert/select/update...)
        """

        for i, param in enumerate(self.params):
            tmp = str(i + 1)
            if isinstance(param, RasGMArray):
                self.query = self.query.replace("$" + tmp, "#MDD" + tmp + "#")
            else:
                # no MDD parameter => substitute each occurence of the
                # corresponding $ parameter in the query string with the
                # value of this parameter
                self.query = self.query.replace("$" + tmp, str(param))

        # Check what kind of internal helper methods should be used based on rasql query
        tmp_query = self.query.upper()

        if tmp_query.startswith("SELECT ") and "INTO " not in tmp_query:
            return self.execute_query(QueryType.SELECT)
        elif tmp_query.startswith("INSERT "):
            return self.execute_query(QueryType.INSERT)
        else:
            return self.execute_query(QueryType.UPDATE)

    # oqlquery.cc
    def execute_query(self, query_type: QueryType):

        txn = self.db_connector.db.transaction(rw=True if query_type != QueryType.SELECT else False)

        query = txn.query(self.query)  # core.Query

        mdd_list = [ras_array for ras_array in self.params if isinstance(ras_array, RasGMArray)]
        if len(mdd_list) > 0:
            query.mdd_constants = mdd_list

        res = {
            QueryType.SELECT: query.execute_read,
            QueryType.INSERT: query.execute_insert,
            QueryType.UPDATE: query.execute_update,

        }[query_type]()

        txn.commit()

        return res

