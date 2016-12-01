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

import org.odmg.*;
import rasj.*;

import java.util.*;

public class RasConnection {
    private String host;
    private int port;
    private String dbName;
    private String user;
    private RasMode mode;

    private RasDriver driver;
    private boolean connected;
    private boolean opened;
    private final RasImplementation impl;
    private final Database connection;
    private final Transaction transaction;

    private List<RasResult> results;


    RasConnection(String host, int port, String dbName, String user, String password, RasMode mode,
                  RasDriver driver) throws ODMGException {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.user = user;
        this.mode = mode;
        this.driver = driver;

        impl = new RasImplementation("http://" + host + ":" + port);
        impl.setUserIdentification(user, password);
        connection = impl.newDatabase();
        connection.open(dbName,
                        mode == RasMode.READ_ONLY ? Database.OPEN_READ_ONLY : Database.OPEN_READ_WRITE);

        transaction = impl.newTransaction();
        connected = true;
        opened = false;
        results = new LinkedList<RasResult>();
    }

    public RasResult executeQuery(String query) throws RasException, ODMGException {
        return executeQuery(query, null);
    }

    public RasResult executeQuery(String query, List<Object> params)
    throws RasException, ODMGException {
        checkState();

        if (!transaction.isOpen()) {
            transaction.begin();
        }

        OQLQuery oqlquery = impl.newOQLQuery();
        oqlquery.create(query);
        if (params != null) {
            for (Object obj : params) {
                oqlquery.bind(obj);
            }
        }
        Object execResult = null;
        try {
            opened = true;
            execResult = oqlquery.execute();
        } catch (TransactionNotInProgressException e) {
            // Sad, but true: sometimes result of transaction.isOpen is not correct
            transaction.begin();
            execResult = oqlquery.execute();
        }
        RasResult result = new RasResult(query, execResult, this);
        results.add(result);
        return result;
    }

    public List<String> listCollections() throws ODMGException, RasException {
        checkState();
        String query = "select r from RAS_COLLECTIONNAMES as r";
        RasResult rr = executeQuery(query);

        List<String> colls = new ArrayList<String>();
        if (rr.bag != null) {
            Iterator iter = rr.bag.iterator();
            while (iter.hasNext()) {
                RasGMArray result = (RasGMArray)iter.next();
                byte[] bytes = result.getArray();
                String name = new String(bytes, 0, bytes.length - 1);
                colls.add(name);
            }
        }
        Collections.sort(colls);
        rr.clearResult();
        return colls;
    }

    public boolean existsCollection(String name) throws RasException, ODMGException {
        List<String> colls = listCollections();
        return colls.contains(name);
    }

    public boolean removeCollection(String name) throws RasException {
        checkState();
        String query = "drop collection " + name;
        try {
            executeQuery(query);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getTypeStructure(String typeName) {
        if (!transaction.isOpen()) {
            transaction.begin();
        }
        String struct;
        try {
            opened = true;
            struct = impl.getTypeStructure(typeName, RasImplementation.TYPE_TYPE_MDD);
        } catch (TransactionNotInProgressException e) {
            // Sad, but true: sometimes result of transaction.isOpen is not correct
            transaction.begin();
            struct = impl.getTypeStructure(typeName, RasImplementation.TYPE_TYPE_MDD);
        }
        if (struct.length() == 0) {
            struct = impl.getTypeStructure(typeName, RasImplementation.TYPE_TYPE_SET);
        }
        return struct;
    }

    public List<RasResult> listResults() {
        return results;
    }

    void removeResult(RasResult result) {
        results.remove(result);
    }

    public void disconnect() throws RasException {
        if (!connected) {
            return;
        }
        try {
            commit();
            connection.close();
        } catch (ODMGException e) {
            e.printStackTrace();
        } finally {
            connected = false;
            driver.forgetConnection(this);
        }
    }

    public void commit() {
        clearState(true);
    }

    public void rollback() {
        clearState(false);
    }

    public String toString() {
        String mode = (this.mode == RasMode.READ_ONLY) ? "READ_ONLY" : "READ_WRITE";
        String alive = connected ? "true" : "false";
        return String.format("RasConnection{ host=%s, port=%d, db=%s, user=%s, mode=%s, alive=%s }",
                             host, port, dbName, user, mode, alive);
    }

    void clearState(boolean success) {
        results.clear();
        if (opened) {
            if (success) {
                transaction.commit();
            } else {
                transaction.abort();
            }
            opened = false;
        }
    }

    private void checkState() throws RasException {
        if (!connected) {
            throw new RasException("Expired rasdaman connection");
        }
    }
}
