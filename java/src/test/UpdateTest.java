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
 * Test updates via rasj.
 *
 * @author Dimitar Misev
 */
package tests;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import org.odmg.Database;
import org.odmg.OQLQuery;
import org.odmg.Transaction;

import rasj.RasGMArray;
import rasj.RasImplementation;
import rasj.RasMInterval;
import rasj.odmg.RasBag;

public class UpdateTest {

    public static final String RASDAMAN_URL = "http://localhost:7001";
    public static final String RASDAMAN_DATABASE = "RASBASE";
    public static final String RASDAMAN_USER = "rasadmin";
    public static final String RASDAMAN_PASS = "rasadmin";
    public static final String TESTDATA_FILE = "../systemtest/testcases_mandatory/test_select/testdata/mr_1.png";
    public static final String TESTDATA_TYPE = "GreyString";
    public static final String TESTDATA_SDOM = "[0:255,0:210]";
    public static final int TESTDATA_SIZE = 22688;
    public static final String COLL = "my_collection";

    public static void main(String[] args) {
        System.out.println("---------------------------------------");
        System.out.println("rasj update test");

        RasImplementation rasImpl = new RasImplementation(RASDAMAN_URL);
        Transaction transaction = null;
        Database rasDb = null;

        try {

            rasImpl.setUserIdentification(RASDAMAN_USER, RASDAMAN_PASS);
            rasDb = rasImpl.newDatabase();

            rasDb.open(RASDAMAN_DATABASE, Database.OPEN_READ_WRITE);

            transaction = rasImpl.newTransaction();
            transaction.begin();

            OQLQuery myQu = rasImpl.newOQLQuery();
            myQu.create("create collection " + COLL + " GreySet");
            myQu.execute();

            myQu = rasImpl.newOQLQuery();
            myQu.create("insert into " + COLL + " values inv_png($1)");
            myQu.bind(createMDDFromFile(TESTDATA_FILE));
            myQu.execute();

            myQu = rasImpl.newOQLQuery();
            myQu.create("select sdom(c) from " + COLL + " as c");
            Object result = myQu.execute();
            try {
                RasBag bag = (RasBag) result;
                Iterator it = bag.iterator();
                while (it.hasNext()) {
                    RasMInterval res = (RasMInterval) it.next();
                    if (!res.toString().equals(TESTDATA_SDOM)) {
                        System.out.println("Test failed, expected domain " + TESTDATA_SDOM + ", got " + res);
                        System.out.println("---------------------------------------");
                        error(transaction, null);
                    } else {
                        System.out.println("Test passed.\n");
                        System.out.println("---------------------------------------");
                    }
                }
            } catch (Exception ex) {
                error(transaction, ex);
            }

        } catch (Exception ex) {
            error(transaction, ex);
        } finally {
            try {
                if (!transaction.isOpen()) {
                    transaction.begin();
                }
                OQLQuery myQu = rasImpl.newOQLQuery();
                myQu.create("drop collection " + COLL);
                myQu.execute();
                transaction.commit();
                if (transaction.isOpen()) {
                    transaction.commit();
                }
                rasDb.close();
            } catch (Exception ex) {
            }
        }

    }

    public static void error(Transaction transaction, Exception ex) {
        if (ex != null) {
            ex.printStackTrace();
        }
        if (transaction.isOpen()) {
            transaction.abort();
        }
        System.exit(1);
    }

    public static RasGMArray createMDDFromFile(String fileName) throws Exception {
        RasGMArray mdd = new RasGMArray(new RasMInterval("[0:" + (TESTDATA_SIZE - 1) + "]"), 1);

        FileInputStream fr = new FileInputStream(new File(fileName));
        byte[] array = new byte[TESTDATA_SIZE];
        fr.read(array);
        mdd.setArray(array);
        mdd.setObjectTypeName(TESTDATA_TYPE);

        return mdd;
    }
}
