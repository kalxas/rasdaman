/**
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

package rasj.test;

import java.util.ArrayList;
import java.util.List;
import org.odmg.DBag;
import org.odmg.Database;
import org.odmg.ODMGException;
import org.odmg.OQLQuery;
import org.odmg.Transaction;
import rasj.RasGMArray;
import rasj.RasImplementation;


/**
 * Simple implementation which stores the mdds/scalars from the result as lists.
 *
 * @author Dimitar Misev
 */
class RasQueryResult {

    protected final Object result;
    /**
     * Prevent multiple inspections
     */
    private boolean inspected;
    private final List<String> scalars;
    private final List<byte[]> mdds;

    public RasQueryResult(Object result) {
        this.result = result;
        inspected = false;
        scalars = new ArrayList<String>();
        mdds = new ArrayList<byte[]>();
        this.inspect();
    }

    public void mdd(RasGMArray res) {
        mdds.add(res.getArray());
    }

    public void scalar(Object res) {
        String scalarResult = res.toString();
        // NOTE: keep the result consistent as from rasql.
        switch (scalarResult) {
            case "-Infinity":
                scalarResult = "-inf";
                break;
            case "Infinity":
                scalarResult = "inf";
                break;
            case "true":
                scalarResult = "t";
                break;
            case "false":
                scalarResult = "f";
                break;
            default:
                break;
        }
        scalars.add(scalarResult);
    }

    public List<byte[]> getMdds() {
        return mdds;
    }

    public List<String> getScalars() {
        return scalars;
    }

    public Object getResult() {
        return result;
    }

    private void inspect() {
        if (inspected) {
            return;
        }
        if (result instanceof DBag) {
            DBag bag = (DBag) result;
            for (Object e : bag) {
                if (e instanceof RasGMArray) {
                    mdd((RasGMArray) e);
                } else {
                    scalar(e);
                }
            }
        } else if (result instanceof Integer) {
            scalar(result);
        }
        inspected = true;
    }

    @Override
    public String toString() {

        String out = "";
        StringBuilder sb = new StringBuilder(out);

        if (!getMdds().isEmpty()) {
            for (byte[] mdd : getMdds()) {
                sb.append(new String(mdd)); // don't use mdd.toString()
            }
        } else if (!getScalars().isEmpty()) {
            for (String scalar : getScalars()) {
                sb.append(scalar);
            }
        }
        return sb.toString();
    }
}


/**
 * Test transaction handling.
 */
public class TestTransaction {

    static String server = "localhost";
    static String base = "RASBASE";
    static String coll = "test_updating";
    static String port = "7001";
    static String user = "rasadmin";
    static String passwd = "rasadmin";
    static String collName = "test_updating";
    static int updateCount = 0;
    
    static final boolean RW = true;
    static final boolean RO = false;
    
    private static void closeDB(Database db) {
        if (db != null) {
            try {
                db.close();
            } catch (Exception ex) {
                System.err.println("Failed closing rasdaman db connection: " + ex.getMessage());
            }
        }
    }
    
    private static void abortTR(Transaction tr) {
        if (tr != null) {
            try {
                tr.abort();
            } catch (Exception ex) {
                System.err.println("Failed closing rasdaman transaction: " + ex.getMessage());
            }
        }
    }

    public static Object executeQuery(String query, boolean rw) {
        final long start = System.currentTimeMillis();
        
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Executing rasql query: " + query);
        
        
        System.out.println("\nCreating implementation...");
        RasImplementation impl = new RasImplementation("http://" + server + ":" + port);
        impl.setUserIdentification(user, passwd);
        
        System.out.println("Instantiating database object...");
        Object ret = null;
        Database db = impl.newDatabase();
        try {
            // open db
            System.out.println("Opening database...");
            db.open(base, 
                    rw ? Database.OPEN_READ_WRITE : Database.OPEN_READ_ONLY);
        } catch (Exception ex) {
            System.err.println("Failed opening " + (rw ? "rw" : "ro") + " database connection to rasdaman: " + ex.getMessage());
            throw new RuntimeException(ex);
        }

        Transaction tr = null;
        try {
            // open transaction
            tr = impl.newTransaction();
            System.out.println("Opening transaction...");
            tr.begin();
        } catch (Exception ex) {
            System.err.println("Failed opening " + (rw ? "rw" : "ro") + " transaction to rasdaman: " + ex.getMessage());
            closeDB(db);
            throw new RuntimeException(ex);
        }
        
        OQLQuery q = null;
        try {
            q = impl.newOQLQuery();
            q.create(query);
        } catch (Exception ex) {
            // not really supposed to ever throw an exception
            System.err.println("Failed creating query object: " + ex.getMessage());
            abortTR(tr);
            closeDB(db);
            throw new RuntimeException(ex);
        }
        
        try {
            System.out.println("Executing query...");
            ret = q.execute();
            tr.commit();
        } catch (ODMGException ex) {
            System.err.println("Failed executing query: " + ex.getMessage());
            abortTR(tr);
            throw new RuntimeException(ex);
        } catch (Throwable ex) {
            System.err.println("Critical failed executing query: " + ex.getMessage());
            abortTR(tr);
            if (ex instanceof OutOfMemoryError) {
                System.err.println("Requested more data than the server can handle at once. "
                        + "Try increasing the maximum memory allowed for Tomcat (-Xmx JVM option).");
            } else {
                System.err.println("Failed executing query: " + ex.getMessage());
            }
            throw new RuntimeException(ex);
        } finally {
            closeDB(db);
        }

        final long end = System.currentTimeMillis();
        final long totalTime = end - start;
        System.err.println("Rasql query executed in " + String.valueOf(totalTime) + " ms.");

        return ret;
    }
    
    

    public static void main(String[] args) {

        for (int i = args.length - 1; i >= 0; i--) {
            if (args[i].equals("-server")) {
                server = args[i + 1];
            }
            if (args[i].equals("-database")) {
                base = args[i + 1];
            }
            if (args[i].equals("-collection")) {
                coll = args[i + 1];
            }
            if (args[i].equals("-port")) {
                port = args[i + 1];
            }
            if (args[i].equals("-user")) {
                user = args[i + 1];
            }
            if (args[i].equals("-passwd")) {
                passwd = args[i + 1];
            }
        }
        
        boolean ret = true; // success

        Object res = null;
        
        {
            System.out.println("Testing simple select query...");
            res = executeQuery("select version()", RO);
            ret = ret && (res != null);
            ret = ret && new RasQueryResult(res).toString().startsWith("rasdaman ");
        }
        {
            System.out.println("Testing simple update query...");
            try {
                res = executeQuery("create collection test_transaction GreySet", RW);
            } catch (Exception ex) {
                if (!ex.getMessage().contains("Collection name exists already")) {
                    ret = false;
                }
            }
            ret = ret && (res != null);
        }
        {
            System.out.println("Testing invalid select query...");
            try {
                res = executeQuery("select versions()", RO);
                ret = false;
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
        {
            System.out.println("Testing long queries saturating servers...");
            final int maxQueries = 4;
            for (int i = 0; i < maxQueries; i++) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            executeQuery("select avg_cells(marray i in [0:10000,0:1000] values 1c)", RO);
                        } catch (Exception ex) {
                            System.out.println("Thread caught exception: " + ex.getMessage());
                        }
                    }
                }).start();
            }
        }
        
        System.out.println("Test result: " + (ret ? "PASSED" : "FAILED"));
        System.exit(ret ? 0 : 1);
    }
}
