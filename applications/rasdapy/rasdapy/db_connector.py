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
from rasdapy.cores import core


class DBConnector(object):
    """
    Interface to user to open a connection to rasdaman database and keep connection until user closes it.
    """
    def __init__(self, hostname, port, username, password, database="RASBASE"):
        """
        :param str hostname: e.g: localhost
        :param int port: e.g: 7001 (default rasmgr port)
        :param str username: e.g: rasguest/rasadmin
        :param str password: e.g: rasguest/rasadmin
        """
        self.hostname = hostname
        self.port = port
        self.username = username
        self.password = password
        self.database = database
        self.con = core.Connection(hostname=hostname, port=port,
                                   username=username, password=password)
        self.db = self.con.database(self.database)

    def open(self):
        """
        Open the connection to rasserver, then RASBASE
        """
        self.con.connect()
        self.db.open()

    def close(self):
        """
        Close the connection to RASBASE, then rasserver
        """
        self.db.close()
        self.con.disconnect()
