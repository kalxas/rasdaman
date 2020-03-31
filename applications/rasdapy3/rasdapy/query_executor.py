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
import os
from rasdapy.models.ras_gmarray import RasGMArray
from rasdapy.models.ras_storage_layout import RasStorageLayOut
from rasdapy.models.ras_gmarray_builder import RasGMArrayBuilder
from rasdapy.ras_oqlquery import RasOQLQuery


class QueryExecutor(object):
    """
    Interface to user to run Rasql queries (create, insert, update, delete,...) and return result to user.
    """
    def __init__(self, db_connector):
        """
        :param DBConnector db_connector: an opened connection to rasdaman server
        """
        self.ras_oqlquery = RasOQLQuery(db_connector)

    def execute_read(self, query):
        """
        Execture rasql query which only needs read permission.
        :param str query: rasql query which only needs read permission (e.g: select c from test_rgb)
        :return: depends on the rasql query, it can return different objects (e.g: number if query returns scalar,
                mintervals if rasql query is sdom() or Numpy ndarray if it is encode or binary result from collections)
        """
        self.ras_oqlquery.create(query)
        res = self.ras_oqlquery.execute()

        return res

    def execute_write(self, query):
        """
        Execute rasql query which needs write permission (but not be used with
        ***insert into collection values from file($1) -f --mdddomain --mddtype***)
        :param str query: rasql query which needs the write permission , e.g: create collection, drop collection,
        update collection with MDArray from file.
        :return: int status: return the status of the query from rasserver
        """
        self.ras_oqlquery.create(query)
        res = self.ras_oqlquery.execute()
        self.ras_oqlquery.reset()
        return res

    def execute_query(self, query, gmarray: RasGMArray):
        # Then, it can run the query to update collection with MDArray from input file
        self.ras_oqlquery.create(query)
        self.ras_oqlquery.bind(gmarray)

        query_result = self.ras_oqlquery.execute()
        self.ras_oqlquery.reset()
        return query_result

    def execute_update_from_file(self, query, file_path,
                                 mdd_domain=None,
                                 mdd_type=RasGMArray.DEFAULT_MDD_TYPE,
                                 mdd_type_length=RasGMArray.DEFAULT_TYPE_LENGTH,
                                 tile_domain=None,
                                 tile_size=RasStorageLayOut.DEFAULT_TILE_SIZE):
        """
        Execute rasql query which needs write permission to insert MDArray from file into collection
        (only used with ***insert into collection values from file($1) -f --mdddomain --mddtype***)
        :param str query: insert into collection rasql query
        :param str file_path: path to the decodable file in user system to be read as binary to send POST request to rasserver.
        :param str mdd_domain: domain of the input MDArray (e.g: --mddomain [0:100,0:100])
        :param str mdd_type: type of input MDArray (e.g: GreyImage is 2D char type)
        :param int mdd_type_length: base type length (e.g: char:1 byte, short: 2 bytes) of the input MDArray
               if not specified, it is default char length.
        :param str tile_domain: domain of tile (e.g: [0:10,0:10]). This is optional as rasql doesn't have parameter for it.
        :param int tile_size: size of the tile (e.g: 128KB). This is optional as rasql doesn't have parameter for it.
        :return: int oid: newly inserted OID of MDArray in collection
        """
        gmarray = RasGMArrayBuilder.from_file(file_path, mdd_domain, mdd_type, mdd_type_length, tile_domain, tile_size)

        return self.execute_query(query, gmarray)



