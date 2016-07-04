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
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */


/**
  Test for insertion of binary data via rasj (http://rasdaman.org/ticket/1286)
*/
package tests;

import rasj.*;
import org.odmg.*;
import java.util.*;

public class InsertBinaryDataTest {
    private static final String server = "localhost";
    private static final String base = "RASBASE";
    private static final String port = "7001";
    private static final String user = "rasadmin";
    private static final String passwd = "rasadmin";
    private static final int EXIT_OK = 0;
    private static final int EXIT_FAIL = 1;

    public static void main(String[] args) {
    System.out.println("---------------------------------------");
        System.out.println("------------- Inserting binary data test");
        InsertBinaryDataTest ct = new InsertBinaryDataTest();
        int exitCode = ct.insertStripes();
        System.out.println();
        if (exitCode == EXIT_OK)
        {
          System.out.println("Test passed.");
        }
        else
        {
          System.out.println("Test failed.");
        }
        System.out.println("---------------------------------------");
        System.exit(exitCode);
    }

    public int insertStripes()
    {
      int rc = EXIT_OK;
      try {
        // from Application Examples (dev-guide-java.pdf, page 15)
        Transaction myTa = null;
        Database myDb = null;
        OQLQuery myQu = null;

        Implementation myApp = new RasImplementation("http://"+server+":"+port);
        ((RasImplementation)myApp).setUserIdentification(user, passwd);
        myDb = myApp.newDatabase();

        System.out.println("Opening database ...");
        myDb.open(base, Database.OPEN_EXCLUSIVE);

        try {
            System.out.println("Starting transaction ...");
            myTa = myApp.newTransaction();
            myTa.begin();

            // Example 3 (dev-guide-java.pdf, page 31)
            // set up query object for collection creation:
            myQu = myApp.newOQLQuery(); //AA
            myQu.create("create collection test GreySet");
            // finally, execute “create collection” statement:
            myQu.execute();

            // Example 2 (dev-guide-java.pdf, page 30)
            // create 2-D MDD with cell length 1, i.e., type “byte”:
            RasGMArray myMDD = new RasGMArray( new RasMInterval( "[1:400,1:400]"), 1 );
            // byte container for array data, matching in size:
            byte[] mydata = new byte[160000];
            // initialize array as all-black with two grey stripes:
            for(int y=0; y<400; y++)
            {
                for(int x=0; x<400; x++)
                {
                    if((x>99 && x<151) || (x>299 && x<351)) mydata[y*399+x]=100;
                    else mydata[y*399+x]=0;
                }
            }
            // now insert byte array into MDD object
            // (sets only the pointer, no copying takes place!):
            myMDD.setArray(mydata);
            // set the object type name (used for server type checking):
            myMDD.setObjectTypeName("GreyImage");
            RasStorageLayout ms = new RasStorageLayout();
            ms.setTileDomain("[1:200,1:200]");
            ms.setTileSize(40000);
            myMDD.setStorageLayout(ms);
            
            // now create the insert statement:
            myQu = myApp.newOQLQuery(); //AA
            myQu.create("insert into test values $1");
            // let the server generate a new OID for the object to be
            // inserted, and remember this OID locally:
            String myNewOID = myApp.getObjectId( myMDD );
            // bind the MDD value which substitutes formal parameter $1:
            myQu.bind(myMDD); // …and ship the complete statement to the server:
            myQu.execute();
            
            System.out.println("== Committing transaction...");
            myTa.commit();
            System.out.println("== Array inserted with OID = " + myNewOID);
            
            // drop data
            myTa = myApp.newTransaction();
            myTa.begin();
            myQu = myApp.newOQLQuery();
            myQu.create("drop collection test");
            myQu.execute();
            myTa.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            myTa.abort();
            rc = EXIT_FAIL;
        } finally {
            myDb.close();
        }
      } catch (Exception e) {
          e.printStackTrace();
          rc = EXIT_FAIL;
      }
      
      return rc;
    }

}
