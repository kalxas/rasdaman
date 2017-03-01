/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2012 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package secore.handler;

import secore.req.ResolveResponse;
import secore.req.ResolveRequest;
import java.net.URL;
import secore.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import secore.BaseTest;
import static secore.BaseTest.TEST_HOST;
import static secore.BaseTest.resetDb;
import secore.Resolver;
import secore.util.Constants;
import secore.util.SecoreException;
import secore.util.StringUtil;

/**
 *
 * @author Dimitar Misev
 */
public class CrsCompoundHandlerTest extends BaseTest {

    private static CrsCompoundHandler handler;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Config.getInstance();
        handler = new CrsCompoundHandler();
        StringUtil.SERVICE_URI = Constants.LOCAL_URI;
        resetDb();
    }

    /**
     * Test of handle method, of class CrsCombineHandler.
     */
    @Test
    public void testExpandOrFormatParameter() throws Exception {
        System.out.println("testExpandOrFormatParameter");
        String uri = "local/crs-compound?"
                     + "1=local/crs/EPSG/0/4326?expand=full&"
                     + "2=local/crs/EPSG/0/4440?format=gml";
        ResolveRequest request = new ResolveRequest(uri);

        Boolean isCorrect = false;
        try {
            ResolveResponse result = handler.handle(request);
        } catch (SecoreException e) {

            // If it throws an exception then the test is correct
            String ex = e.getMessage();

            String expResult = "Compound CRS is not allowed to add expand/format parameter(s).";
            if (ex.contains(expResult)) {
                isCorrect = true;
            }
        }

        // If it can handle correctly then res will contains the error message
        assertTrue(isCorrect);
    }

    /**
     * Test of handle method, of class CrsCombineHandler.
     */
//  @Ignore
    @Test
    public void testHandleParameterizedCrs() throws Exception {
        System.out.println("testHandleParameterizedCrs");
        String uri = "local/crs-compound?"
                     + "1=local/crs?authority=AUTO%26version=1.3%26code=42001%26lon=10&"
                     + "2=local/crs/EPSG/0/4440";
        ResolveRequest req = new ResolveRequest(uri);
        ResolveResponse res = handler.handle(req);
//    putData("AUTO+4440.exp", res.getData());
        String expResult = getData("AUTO+4440.exp");
        assertEquals(expResult, res.getData());
    }

    /**
     * Test of handle method, of class CrsCombineHandler.
     */
//  @Ignore
    @Test
    public void testHandle() throws Exception {
        System.out.println("testHandle");
        String uri = "local/crs-compound?"
                     + "1=local/crs/EPSG/0/4326&"
                     + "2=local/crs/EPSG/0/4440";
        ResolveRequest req = new ResolveRequest(uri);
        ResolveResponse res = handler.handle(req);
//    putData("4326+4440.exp", res.getData());
        String expResult = getData("4326+4440.exp");
        assertEquals(expResult, res.getData());
    }

    /**
     * Test of handle method, of class CrsCombineHandler.
     */
//  @Ignore
    @Test
    public void testHandle2() throws Exception {
        System.out.println("testHandle2");
        String uri = TEST_HOST + "/crs-compound?"
                     + "1=local/crs/EPSG/0/4326&"
                     + "2=local/crs/EPSG/0/4440";
        ResolveResponse res = Resolver.resolve(new URL(uri));
//    putData("4326+4440.exp2", res.getData());
        String expResult = getData("4326+4440.exp2");
        assertEquals(expResult, res.getData());
    }

    /**
     * Test of getOperation method, of class CrsCombineHandler.
     */
    @Test
    public void testGetOperation() {
        String expResult = "crs-compound";
        String result = handler.getOperation();
        assertEquals(expResult, result);
    }

}
