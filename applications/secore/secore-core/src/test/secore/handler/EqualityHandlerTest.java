/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secore.handler;

import org.junit.Test;
import static org.junit.Assert.*;
import secore.BaseTest;
import secore.req.ResolveRequest;
import secore.req.ResolveResponse;

/**
 *
 * @author Dimitar Misev
 */
public class EqualityHandlerTest extends BaseTest {

    /**
     * Test of handle method, of class EqualityHandler.
     */
    @Test
    public void testHandle() throws Exception {
        System.out.println("handle");
        String uri = "local/equal?1=local/crs/EPSG/0/4326&"
                     + "2=local/crs/?authority=EPSG&version=0&code=4326";
        ResolveRequest request = new ResolveRequest(uri);
        EqualityHandler instance = new EqualityHandler();
        ResolveResponse result = instance.handle(request);
//    putData("equality.exp", result.getData());
        String expResult = getData("equality.exp");
        assertEquals(expResult, result.getData());
    }

    /**
     * Test of handle method, of class EqualityHandler.
     */
    @Test
    public void testHandleUnequal() throws Exception {
        System.out.println("testHandleUnequal");
        String uri = "local/equal?1=local/crs/EPSG/0/4326&"
                     + "2=local/crs/?authority=EPSG&version=0&code=4440";
        ResolveRequest request = new ResolveRequest(uri);
        EqualityHandler instance = new EqualityHandler();
        ResolveResponse result = instance.handle(request);
//    putData("equality_fail.exp", result.getData());
        String expResult = getData("equality_fail.exp");
        assertEquals(expResult, result.getData());
    }

    /**
     * Test of getOperation method, of class EqualityHandler.
     */
    @Test
    public void testGetOperation() {
        System.out.println("getOperation");
        EqualityHandler instance = new EqualityHandler();
        String expResult = "equal";
        String result = instance.getOperation();
        assertEquals(expResult, result);
    }
}