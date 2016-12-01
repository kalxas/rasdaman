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
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import secore.BaseTest;
import secore.db.BaseX;
import secore.db.DbManager;
import secore.util.Config;
import secore.util.Constants;
import secore.util.ExceptionCode;
import secore.util.SecoreException;
import secore.util.StringUtil;

/**
 *
 * @author Dimitar Misev
 */
public class ParameterizedCrsHandlerTest extends BaseTest {

    private static ParameterizedCrsHandler handler = null;

    private static BaseX db = null;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Config.getInstance();
        DbManager.getInstance().getDb();
        handler = new ParameterizedCrsHandler();

        StringUtil.SERVICE_URI = Constants.LOCAL_URI;
        db = resetDb();
    }

    /**
     * Test of handle method, of class ParameterizedCrsHandler.
     */
//  @Ignore
    @Test
    public void testXQuery() throws Exception {
        System.out.println("xquery");
        String query = getData("parameterized.xquery");
        String versionNumber = DbManager.FIX_GML_COLLECTION_NAME;
        String result = DbManager.getInstance().getDb().queryBothDB(query, versionNumber);
        putData("parameterized.exp2", result);
        String expResult = getData("parameterized.exp");
        assertEquals(expResult, result);
    }

    /**
     * Test of handle method, of class ParameterizedCrsHandler.
     */
//  @Ignore
    @Test
    public void testHandle() throws Exception {
        System.out.println("handle");
        ResolveRequest req = new ResolveRequest("local/crs/AUTO/1.3/42001?lon=-100");
        ResolveResponse res = handler.handle(req);
        putData("42001.exp", res.getData());
        String expResult = getData("42001.exp");
        assertEquals(expResult, res.getData());
    }

    /**
     * Test of handle method, of class ParameterizedCrsHandler.
     */
//  @Ignore
    @Test
    public void testWrongParameter() throws Exception {
        System.out.println("handle");
        ResolveRequest req = new ResolveRequest("/def/crs/EPSG/0/4326?lon=-100");
        try {
            ResolveResponse res = handler.handle(req);
            fail();
        } catch (SecoreException ex) {
            if (!ex.getExceptionCode().equals(ExceptionCode.NoSuchDefinition)) {
                fail("expected a missing parameter exception, got " + ex.getMessage());
            }
        }
    }

    /**
     * http://rasdaman.org/ticket/386
     */
//  @Ignore
    @Test
    public void testUrnInTarget() throws Exception {
        System.out.println("testUrnInTarget");

        System.out.println("insert testdata");

        String query = "declare namespace gml = \"" + Constants.NAMESPACE_GML + "\";" + Constants.NEW_LINE
                       + "let $x := collection('" + Constants.COLLECTION_NAME + "')" + Constants.NEW_LINE
                       + "return insert node <dictionaryEntry xmlns=\"" + Constants.NAMESPACE_GML + "\">"
                       + StringUtil.fixLinks(getData("AUTO_urn_newdef.xml"), Constants.LOCAL_URI)
                       + "</dictionaryEntry> into $x";
        DbManager.getInstance().getDb().updateQuery(query, DbManager.USER_DB);

        ResolveRequest req = new ResolveRequest("local/crs/AUTO/1.3/42005?lon=-100");
        ResolveResponse res = handler.handle(req);
//    putData("AUTO_urn.exp", res.getData());
        String expResult = getData("AUTO_urn.exp");
        assertEquals(expResult, res.getData());
    }
}
