/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secore.handler;

import java.net.URL;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import secore.BaseTest;
import static secore.BaseTest.TEST_HOST;
import static secore.BaseTest.resetDb;
import secore.Resolver;
import secore.req.ResolveRequest;
import secore.req.ResolveResponse;
import secore.util.Config;
import secore.util.Constants;
import secore.util.StringUtil;

/**
 *
 * @author alireza
 */
public class QueryHandlerTest extends BaseTest {

    private static QueryHandler handler;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Config.getInstance();
        handler = new QueryHandler();
        StringUtil.SERVICE_URI = Constants.LOCAL_URI;
        resetDb();
    }

    /**
     * Test of handle method, of class QueryHandler.
     */
//  @Ignore
    @Test
    public void testHandle() throws Exception {
        System.out.println("testHandle");
        String uri = TEST_HOST + "/query?"
                     + "query%3Ddeclare+namespace+gml+%3D+%22http%3A%2F%2Fwww.opengis.net%2Fgml%2F3.2%22%3B+let+%24d+"
                     + "%3A%3D+doc%28%27gml%27%29+return+%24d%2F%2Fgml%3Aidentifier%5Bcontains%28text%28%29%2C+%27%2Fcrs%2FEPSG%2F0%2F4326%27%29%5D";

        ResolveResponse res = Resolver.resolve(new URL(uri));
        String expResult = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                           "<gml:identifier xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:epsg=\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\" "
                           + "xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\" "
                           + "codeSpace=\"OGP\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\">"
                           + "local/crs/EPSG/0/4326</gml:identifier>";
        System.out.println("*********:" + res.getData());
        assertEquals(expResult, res.getData());

    }

    @Test
    public void testHandle1() throws Exception {
        System.out.println("testHandle");
        String uri = TEST_HOST + "/query?"
                     + "query=declare namespace gml = \"http://www.opengis.net/gml/3.2\"; let $d := doc('gml')"
                     + " return $d//gml:identifier[contains(text(), '/crs/EPSG/0/4326')]";
        ResolveRequest req = new ResolveRequest(uri);
        ResolveResponse res = handler.handle(req);
        String expResult = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                           "<gml:identifier xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:epsg=\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\" "
                           + "xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\" "
                           + "codeSpace=\"OGP\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\">"
                           + "local/crs/EPSG/0/4326</gml:identifier>";
        System.out.println("*********:" + res.getData());
        assertEquals(expResult, res.getData());

    }


}
