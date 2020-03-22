#!/usr/bin/env python3
#
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# Test RasdaPy API script
import argparse
import sys
import re
import os
from rasdapy.db_connector import DBConnector
from rasdapy.query_executor import QueryExecutor
from rasdapy.models.result_array import ResultArray
from rasdapy.query_result import QueryResult
from rasdapy.cores.utils import encoded_bytes_to_str

SUCCESS = 0
ERROR = 1

LOCALHOST = "localhost"
RASMG_PORT = 7001
RASBASE = "RASBASE"
RASGUEST = "rasguest"


OUTPUT_STRING = "string"
OUTPUT_FILE = "file"

# https://github.com/grpc/grpc/issues/17631
# if 'http_proxy' in os.environ and os.environ['http_proxy'] == '':
#     del os.environ['http_proxy']


class Validator:
    """
    Validate the input parameters from argv which reduced the first parameter (script name)
    """
    def __init__(self):
        parser = argparse.ArgumentParser(description="", usage="rasql [--query querystring|-q querystring] [options]")
        parser.add_argument('-q', '--query', type=str,
                            help="query string to be sent to the rasdaman server for execution", dest="query",
                            metavar='')
        parser.add_argument('-f', '--file', type=str,
                            help="file name for upload through $i parameters within queries; each $i needs its own"
                                 " file parameter, in proper sequence. Requires --mdddomain and --mddtype",
                            dest="file", metavar='')
        parser.add_argument('--out', type=str,
                            help="use display method t for cell values of result MDDs as file or string.",
                            dest="out", metavar='')
        parser.add_argument('--outfile', type=str,
                            help="file name template for storing result images (ignored for scalar results)."
                                 " Use '%%d' to indicate auto numbering position, like with printf(1). "
                                 "For well-known file types, a proper suffix is appended to the resulting file name "
                                 "Implies --out file. (default: rasql_%%d)",
                            dest="outfile", metavar='')
        parser.add_argument('--mdddomain', type=str,
                            help="domain of marray, format: '[x0:x1,y0:y1]' "
                                 "(required only if --file specified and file is in data format r_Array)",
                            dest="mdddomain", metavar='')
        parser.add_argument('--mddtype', type=str,
                            help="type of marray (required only if --file specified and file is in data format r_Array)",
                            dest="mddtype", metavar='')
        parser.add_argument('-s', "--server", type=str,
                            help="rasdaman server (default: localhost)", default=LOCALHOST,
                            dest="server", metavar='')
        parser.add_argument('-p', "--port", type=int,
                            help="rasmgr port number (default: 7001)", default=RASMG_PORT,
                            dest="port", metavar='')
        parser.add_argument('-d', "--database", type=str,
                            help="name of database (default: RASBASE)", default=RASBASE,
                            dest="database", metavar='')
        parser.add_argument('--user', type=str,
                            help="name of user (default: rasguest)", default=RASGUEST,
                            dest="user", metavar='')
        parser.add_argument('--passwd', type=str,
                            help="password of user (default: rasguest)", default=RASGUEST,
                            dest="passwd", metavar='')

        # If no input arguments, just print the help
        if len(sys.argv) == 1:
            parser.print_help()
            sys.exit(SUCCESS)

        args = parser.parse_args()
        self.query = args.query
        self.file = args.file
        self.out = args.out
        self.outfile = args.outfile
        self.mdddomain = args.mdddomain
        self.mddtype = args.mddtype
        self.server = args.server
        self.port = args.port
        self.database = args.database
        self.user = args.user
        self.passwd = args.passwd



class Main:
    """
    Connect to rasserver and run query then return the result to client
    """

    def __init__(self):
        self.validator = Validator()
        self.db_connector = DBConnector(self.validator.server, self.validator.port, self.validator.user, self.validator.passwd)
        self.query_executor = QueryExecutor(self.db_connector)

    def execute(self):
        """
        Execute the query and get the response to client
        """
        # self.query = 'select {33.0, 33.5}'
        # open connection to rasserver
        self.db_connector.open()

        try:
            tmp = self.validator.query.lower().strip()
            if tmp.startswith("select") and "into" not in tmp:
                """ e.g: select c from RAS_COLLECTIONNAMES as c"""
                res = self.query_executor.execute_read(self.validator.query)
            elif self.validator.file:
                # insert or update with file
                """e.g:  rasql -q 'insert into test2 values $1' 
                -f '/home/rasdaman/101.bin' --mdddomain [0:100] --mddtype 'GreyString' --user rasadmin --passwd rasadmin"""
                res = self.query_executor.execute_update_from_file(self.validator.query, self.validator.file,
                                                                   self.validator.mdddomain, self.validator.mddtype)
            else:
                # just normal update query
                """e.g:  rasql -q 'create collection test2 GreySet1' --user rasadmin --passwd rasadmin
                """
                res = self.query_executor.execute_write(self.validator.query)

            res_arr = res

            # Depend on the output (string, file) to write the result from rasserver
            if res_arr is not None:
                self.__handle_result(res_arr)
        except Exception as e:
            if "error message" in str(e):
                """ e.g: Error executing query 'select stddev_samp(1f)',
                error message 'rasdaman error 353: Execution error 353 in line 1, column 8, near token stddev_samp: Operand.'
                """
                error_message = re.findall(r"([^']*)'?", str(e), re.M)[-2]
                # Write error to stderr
                sys.stderr.write(error_message)
            else:
                raise
        finally:
            #  Close the connection to rasserver to release rasserver from this client
            self.db_connector.close()
            print("rasql done.")

    def __handle_result(self, res_arr):
        """
        Handle the result of query from rasserver
        :param list res_arr: list of result which can be MDDArray or scalar values
        :return: it will print output as string if --out string or write to file if --out file
        """

        if isinstance(res_arr, QueryResult):
            if res_arr.with_error:
                sys.stderr.write(res_arr.error_message())
                return

        if self.validator.out == OUTPUT_STRING:
            # Output list of results to console
            self.__handle_result_as_string(res_arr)
        elif self.validator.out == OUTPUT_FILE:
            # Output list of results as files
            self.__handle_result_as_file(res_arr)

    def __handle_result_as_string(self, res_arr):
        """
        When query outputs to string, then create the output for it.
        If query returns scalar, it will look like:
            'select 1' --out string
            Query result collection has 1 element(s):
                Result element 1: 1
        If query returns MDD, it will look like:
            'select encode(c[0:1, 0:2], "csv") from test_mr as c'
            Query result collection has 1 element(s):
                Result object 1: {0,0,0},{0,0,0}
        :param list res_arr: output from rasserver
        """

        output = "Query result collection has {} element(s): \n".format(res_arr.size)
        if res_arr.size > 0:
            for index, res in enumerate(res_arr):
                msg = encoded_bytes_to_str(res) if res_arr.is_object else res
                output += "  Result {} {}: {}\n".format(res_arr.nature, index + 1, msg)
        print(output.strip())

    def __handle_result_as_file(self, res_arr):
        """
        When query outputs to file, then write MDDArray to files
        e.g: rasql -q 'select c[0:1] from test2 as c' --out file with test2 has 5 MDDArray, then it writes to:
        rasql_1.unknown, rasql_2.unknown,...

        NOTE: if it has --outfile PATH_TO_OUTPUT_FILE parameter, then it will write to PATH_TO_OUTPUT_FILE instead
        e.g:  rasql -q 'select c[0:1] from test2 as c'  --out file --outfile /tmp/test1, then it writes to:
        /tmp/test1_1, /tmp/test1_2,...

        :param list res_arr: output from rasserver
        """
        cur_data = self.validator.query.lower()
        if '"png"' in cur_data:
            file_ext = "png"
        elif '"jp2"' in cur_data:
            file_ext = "jp2"
        elif '"jpg"' in cur_data:
            file_ext = "jpg"
        elif '"bmp"' in cur_data:
            file_ext = "bmp"
        elif '"netcdf"' in cur_data:
            file_ext = "nc"
        elif '"json"' in cur_data:
            file_ext = "json"
        elif '"csv"' in cur_data:
            file_ext = "csv"
        elif '"tiff"' in cur_data:
            file_ext = "tif"
        elif '"gtiff"' in cur_data:
            file_ext = "tif"
        else:
            file_ext = "unknown"

        # Only has paramter --out file
        file_name_prefix = "rasql"
        # It has --outfile PATH_TO_FILE parameter
        if self.validator.outfile is not None:
            file_name_prefix = self.validator.outfile

        # Write output results to files
        for i, cur_data in enumerate(res_arr):
            if res_arr.size > 1:
                file_name = file_name_prefix + "_{}.{}".format(i + 1, file_ext)
            else:
                file_name = file_name_prefix + ".{}".format(file_ext)
            if res_arr.is_object:
                with open(file_name, "wb") as binary_file:
                    # If it is MDDArray then write the data inside it
                    binary_file.write(cur_data)
            else:
                with open(file_name, "w") as text_file:
                    output = "  Result {} {}: {}\n".format(res_arr.nature, i + 1, cur_data)
                    # If it is scalar value then just write it
                    text_file.write(output)


if __name__ == "__main__":
    main = Main()
    # Execute the rasql query and return result to console/file
    main.execute()



