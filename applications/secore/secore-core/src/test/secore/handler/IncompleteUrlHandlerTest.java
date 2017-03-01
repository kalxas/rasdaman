/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secore.handler;

import secore.req.ResolveResponse;
import secore.req.ResolveRequest;
import org.junit.BeforeClass;
import org.junit.Test;
import secore.BaseTest;
import static org.junit.Assert.*;
import secore.db.BaseX;
import secore.db.DbManager;
import secore.util.Config;
import secore.util.SecoreException;

/**
 *
 * @author dimitar
 */
public class IncompleteUrlHandlerTest extends BaseTest {

    private static IncompleteUrlHandler handler;
    private static BaseX db;

    @BeforeClass
    public static void setUpClass() throws SecoreException {
        Config.getInstance();
        handler = new IncompleteUrlHandler();
        DbManager.getInstance().getDb();
    }

    /**
     * Test of handle method, of class IncompleteUrlHandler.
     */
    @Test
    public void testHandle() throws Exception {
        System.out.println("testHandle");

        String uri = "/def/crs";
        ResolveRequest request = new ResolveRequest(uri);

        IncompleteUrlHandler instance = new IncompleteUrlHandler();
        ResolveResponse result = instance.handle(request);
//    putData("incomplete_url.exp", result.getData());

        String expResult = getData("incomplete_url.exp");

        assertEquals(expResult, result.getData());
    }

    /**
     * Test of handle method, of class IncompleteUrlHandler.
     */
    @Test
    public void testHandle2() throws Exception {
        System.out.println("testHandle2");

        String uri = "/def/crs?authority=EPSG";
        ResolveRequest request = new ResolveRequest(uri);

        IncompleteUrlHandler instance = new IncompleteUrlHandler();
        ResolveResponse result = instance.handle(request);
//    putData("incomplete_url2.exp", result.getData());

        String expResult = getData("incomplete_url2.exp");

        assertEquals(expResult, result.getData());
    }

    /**
     * Test of handle method, of class IncompleteUrlHandler.
     */
    @Test
    public void testHandle3() throws Exception {
        System.out.println("testHandle3");

        String uri = "/def/crs/EPSG/0";
        ResolveRequest request = new ResolveRequest(uri);

        IncompleteUrlHandler instance = new IncompleteUrlHandler();
        ResolveResponse result = instance.handle(request);
//    putData("incomplete_url3.exp", result.getData());

        String expResult = getData("incomplete_url3.exp");

        assertEquals(expResult, result.getData());
    }

    /**
     * Test of handle method, of class IncompleteUrlHandler.
     */
    @Test
    public void testHandle4() throws Exception {
        System.out.println("testHandle4");

        String uri = "/def";
        ResolveRequest request = new ResolveRequest(uri);

        IncompleteUrlHandler instance = new IncompleteUrlHandler();
        ResolveResponse result = instance.handle(request);
//    putData("incomplete_url4.exp", result.getData());

        String expResult = getData("incomplete_url4.exp");

        assertEquals(expResult, result.getData());
    }
}