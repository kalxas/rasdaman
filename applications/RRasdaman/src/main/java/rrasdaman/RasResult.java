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

import org.odmg.DBag;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RasResult {
    private String statement;
    DBag bag;
    private final RasConnection connection;
    boolean active;
    private int emitted;
    private Iterator iterator;

    RasResult(String statement, Object result, RasConnection conn) throws RasException {
        this.statement = statement;
        this.bag = (result == null) ? null : (DBag) result;
        this.connection = conn;
        this.active = true;
        this.emitted = 0;
        iterator = (result == null) ? null : bag.iterator();
    }

    public List<Object> fetch(int rowsCount) throws RasException {
        // pass rowsCount = -1 for fetching all records

        List<Object> result = new LinkedList<Object>();
        if (active && bag != null) {
            while (iterator != null && iterator.hasNext() && rowsCount != 0) {
                Object current = iterator.next();
                result.add(current);
                if (rowsCount > 0) {
                    --rowsCount;
                }
                ++emitted;
            }
        }
        return result;
    }

    public int getRowCount() {
        return emitted;
    }

    public String getStatement() {
        return statement;
    }

    public boolean hasCompleted() {
        return !iterator.hasNext();
    }

    public void clearResult() {
        if (!active) {
            return;
        }
        statement = null;
        bag = null;
        active = false;
        connection.removeResult(this);
    }

    public String toString() {
        return String.format("RasResult{ total %d elements, %d emitted }",
                             bag != null ? bag.size() : 0, emitted);
    }
}
