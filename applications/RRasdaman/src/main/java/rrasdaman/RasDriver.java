/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
package rrasdaman;

import org.odmg.ODMGException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RasDriver {
    private static RasDriver instance = null;

    private List<RasConnection> connections;

    public static RasDriver getInstance() {
        if (instance == null)
            instance = new RasDriver();
        return instance;
    }

    protected RasDriver() {
        connections = new LinkedList<RasConnection>();
    }

    public RasConnection createConnection(String host, Integer port, String dbName, String user,
                                          String password, RasMode mode)
            throws ODMGException {
        RasConnection conn = new RasConnection(host, port, dbName, user, password, mode, this);
        connections.add(conn);
        return conn;
    }

    public List<RasConnection> listConnections() {
        return Collections.unmodifiableList(connections);
    }

    public void unload() throws Exception {
        if (!connections.isEmpty())
            throw new RasException("There are opened connections - close them first");
    }

    public String toString() {
        return String.format("RasDriver{ connectionsNum=%d }", connections.size());
    }

    void forgetConnection(RasConnection connection) {
        connections.remove(connection);
    }
}
