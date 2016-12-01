/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secore.req;

import org.junit.Test;
import static org.junit.Assert.*;
import secore.util.Constants;

/**
 * @author Dimitar Misev
 */
public class ResolveRequestTest {

    /**
     * Test of simple def identifier
     */
    @Test
    public void testLocalDef() throws Exception {
        System.out.println("testLocalDef");
        String uri = "crs/EPSG/0/4326";
        ResolveRequest instance = new ResolveRequest(uri);

        assertEquals(Constants.REST_SEPARATOR, instance.getServiceUri());
        assertEquals("crs", instance.getOperation());
        assertEquals(uri, instance.getOriginalRequest());
        assertEquals("/EPSG/0/4326", instance.paramsToString());
        assertEquals(uri, instance.toString());
    }

    /**
     * Test of simple def identifier
     */
    @Test
    public void testSimpleDefRest() throws Exception {
        System.out.println("testSimpleDefRest");
        String uri = "http://localhost:8080/def/crs/EPSG/0/4326";
        ResolveRequest instance = new ResolveRequest(uri);

        assertEquals("http://localhost:8080/def/", instance.getServiceUri());
        assertEquals("crs", instance.getOperation());
        assertEquals(uri, instance.getOriginalRequest());
        assertEquals("/EPSG/0/4326", instance.paramsToString());
        assertEquals(uri, instance.toString());
    }

    /**
     * Test of simple def identifier
     */
    @Test
    public void testSimpleDefKvp() throws Exception {
        System.out.println("testSimpleDefKvp");
        String uri = "http://localhost:8080/def/crs/?authority=EPSG&version=0&code=4326";
        ResolveRequest instance = new ResolveRequest(uri);

        assertEquals("http://localhost:8080/def/", instance.getServiceUri());
        assertEquals("crs", instance.getOperation());
        assertEquals(uri, instance.getOriginalRequest());
        assertEquals("?authority=EPSG&version=0&code=4326", instance.paramsToString());
        assertEquals("http://localhost:8080/def/crs?authority=EPSG&version=0&code=4326", instance.toString());
    }

    /**
     * Test of simple def identifier
     */
    @Test
    public void testSimpleDefRestKvp() throws Exception {
        System.out.println("testSimpleDefRestKvp");
        String uri = "http://localhost:8080/def/crs/?authority=EPSG&version=0/4326";
        ResolveRequest instance = new ResolveRequest(uri);

        assertEquals("http://localhost:8080/def/", instance.getServiceUri());
        assertEquals("crs", instance.getOperation());
        assertEquals(uri, instance.getOriginalRequest());
        assertEquals("?authority=EPSG&version=0/4326", instance.paramsToString());
        assertEquals("http://localhost:8080/def/crs?authority=EPSG&version=0/4326", instance.toString());
    }

    /**
     * Test of incomplete identifier
     */
    @Test
    public void testIncompleteUri() throws Exception {
        System.out.println("testIncompleteUri");
        String uri = "http://localhost:8080/def/crs";
        ResolveRequest instance = new ResolveRequest(uri);

        assertEquals("http://localhost:8080/def/", instance.getServiceUri());
        assertEquals("crs", instance.getOperation());
        assertEquals(uri, instance.getOriginalRequest());
        assertEquals(Constants.EMPTY, instance.paramsToString());
        assertEquals(uri, instance.toString());
    }

    /**
     * Test of incomplete identifier
     */
    @Test
    public void testIncompleteUriDef() throws Exception {
        System.out.println("testIncompleteUri");
        String uri = "http://localhost:8080/def/";
        ResolveRequest instance = new ResolveRequest(uri);

        assertEquals("http://localhost:8080/def/", instance.getServiceUri());
        assertEquals(Constants.EMPTY, instance.getOperation());
        assertEquals(uri, instance.getOriginalRequest());
        assertEquals(Constants.EMPTY, instance.paramsToString());
        assertEquals(uri, instance.toString());
    }

    /**
     * Test of simple def identifier
     */
    @Test
    public void testSimpleDefFlatten() throws Exception {
        System.out.println("testSimpleDefFlatten");
        String uri = "http://localhost:8080/def/crs/EPSG/0/4326?flatten=full";
        ResolveRequest instance = new ResolveRequest(uri);

        assertEquals("http://localhost:8080/def/", instance.getServiceUri());
        assertEquals("crs", instance.getOperation());
        assertEquals(uri, instance.getOriginalRequest());
        assertEquals("/EPSG/0/4326?flatten=full", instance.paramsToString());
        assertEquals(uri, instance.toString());
    }

    /**
     * Test of compound def identifier
     */
    @Test
    public void testCompoundCrs() throws Exception {
        System.out.println("testCompoundCrs");
        String uri = "http://localhost:8080/def/crs-compound?1=http://localhost:8080/"
                     + "def/crs/EPSG/0/4326&2=http://localhost:8080/def/crs/EPSG/0/4440";
        ResolveRequest instance = new ResolveRequest(uri);

        assertEquals("http://localhost:8080/def/", instance.getServiceUri());
        assertEquals("crs-compound", instance.getOperation());
        assertEquals(uri, instance.getOriginalRequest());
        assertEquals(uri, instance.toString());
    }

    /**
     * Test of compound def identifier
     */
    @Test
    public void testCompoundCrsSort() throws Exception {
        System.out.println("testCompoundCrsSort");
        String uri = "http://localhost:8080/def/crs-compound?2=http://localhost:8080/"
                     + "def/crs/EPSG/0/4326&1=http://localhost:8080/def/crs/EPSG/0/4440";
        ResolveRequest instance = new ResolveRequest(uri);

        assertEquals("http://localhost:8080/def/", instance.getServiceUri());
        assertEquals("crs-compound", instance.getOperation());
        assertEquals(uri, instance.getOriginalRequest());
        assertEquals("http://localhost:8080/def/crs-compound?1=http://localhost:8080/"
                     + "def/crs/EPSG/0/4440&2=http://localhost:8080/def/crs/EPSG/0/4326", instance.toString());
    }

}