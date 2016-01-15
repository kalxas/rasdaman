package org.rasdaman.rasnet.communication;

import org.junit.Assert;
import org.junit.Test;
import org.odmg.*;
import rasj.*;

import java.util.Locale;

import static org.junit.Assert.assertTrue;


// Integration test (depends on rasdaman running on http://localhost:7001)
// Requirements: collection myTestCollection should not exist in the database
public class RasRasnetImplementationTest {

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 7001;
    private static final String CONNECT_STRING = String.format("http://%s:%d", TEST_HOST, TEST_PORT);
    public static final String RASDAMAN_USER = "rasadmin";
    public static final String RASDAMAN_PASSWD = "rasadmin";

    @Test
    public void testCreateDBTwice() {

        boolean secodnCreationFailed = false;

        DBag resultBag = null;
        RasGMArray result = null;
        Transaction myTa = null;
        Database myDb = null;
        OQLQuery myQu = null;
        try {
            {
                Implementation myApp = new RasImplementation(CONNECT_STRING);
                ((RasImplementation) myApp).setUserIdentification(RASDAMAN_USER, RASDAMAN_PASSWD);
                myDb = myApp.newDatabase();

                System.out.println("Opening database ...");
                myDb.open("RASBASE", Database.OPEN_READ_WRITE);

                System.out.println("Starting transaction ...");
                myTa = myApp.newTransaction();
                myTa.begin();

                myQu = myApp.newOQLQuery();
                myQu.create("CREATE COLLECTION myTestCollection GreySet");
                myQu.execute();

                System.out.println("Committing transaction ...");
                myTa.commit();

                System.out.println("Closing database ...");
                myDb.close();
            }

            {
                try {
                    Implementation myApp = new RasImplementation(CONNECT_STRING);
                    ((RasImplementation) myApp).setUserIdentification(RASDAMAN_USER, RASDAMAN_PASSWD);
                    myDb = myApp.newDatabase();

                    System.out.println("Opening database ...");
                    myDb.open("RASBASE", Database.OPEN_READ_WRITE);

                    System.out.println("Starting transaction ...");
                    myTa = myApp.newTransaction();
                    myTa.begin();

                    myQu = myApp.newOQLQuery();
                    myQu.create("CREATE COLLECTION myTestCollection GreySet");
                    myQu.execute();

                    System.out.println("Committing transaction ...");
                    myTa.commit();

                    System.out.println("Closing database ...");
                    myDb.close();
                }
                catch (ODMGException ex)
                {
                    assertTrue(ex.getMessage().contains("Collection name exists already."));
                    secodnCreationFailed = true;
                }
            }

        } catch (org.odmg.ODMGException e) {
            System.out.println("An exception has occurred: " + e.getMessage());
            System.out.println("Try to abort the transaction ...");
            if (myTa != null) myTa.abort();

            try {
                System.out.println("Try to close the database ...");
                if (myDb != null) myDb.close();
            } catch (org.odmg.ODMGException exp) {
                System.err.println("Could not close the database: " + exp.getMessage());
            }
        }

        assertTrue(secodnCreationFailed);
    }
}
