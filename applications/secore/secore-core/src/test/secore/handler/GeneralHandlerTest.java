/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secore.handler;

import secore.req.ResolveResponse;
import secore.req.ResolveRequest;
import org.basex.core.cmd.DropDB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import secore.util.Config;
import static org.junit.Assert.*;
import org.junit.Ignore;
import secore.BaseTest;
import secore.db.BaseX;
import secore.db.DbManager;
import secore.util.Constants;
import secore.util.ExceptionCode;
import secore.util.SecoreException;
import secore.util.StringUtil;

/**
 *
 * @author Dimitar Misev
 */
public class GeneralHandlerTest extends BaseTest {
  
  private static GeneralHandler handler;
  private static BaseX db;
  
  public GeneralHandlerTest() {
  }
  
  @BeforeClass
  public static void setUpClass() throws SecoreException {
    Config.getInstance();
    handler = new GeneralHandler();
    StringUtil.SERVICE_URI = Constants.LOCAL_URI;
    db = resetDb();
  }
  
  @AfterClass
  public static void tearDownClass() {
  }

  /**
   * Test of unresolved definition from "gml" which is included in "userdb"
   * (xlink:href)
   */
  @Test
  public void testUnresolvedFromGmlDbInUserDb() throws Exception {
    System.out.println("testUnresolvedFromGmlDbInUserDb");

    String testDefiniton = "<gml:GeodeticCRS xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:epsg=\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\" xmlns=\"http://www.opengis.net/gml/3.2\" gml:id=\"epsg-crs-4326\">\n"
        + "  <gml:metaDataProperty>\n"
        + "    <epsg:CRSMetaData>\n"
        + "      <epsg:projectionConversion xlink:href=\"http://www.opengis.net/def/coordinateOperation/EPSG/0/15593\"/>\n"
        + "      <epsg:sourceGeographicCRS xlink:href=\"http://www.opengis.net/def/crs/EPSG/0/4979\"/>\n"
        + "    </epsg:CRSMetaData>\n"
        + "  </gml:metaDataProperty>\n"
        + "  <gml:identifier codeSpace=\"OGP\">http://www.opengis.net/def/crs/EPSG/0/test1</gml:identifier>\n"
        + "  <gml:name>WGS 84</gml:name>\n"
        + "  <gml:domainOfValidity xlink:href=\"http://www.opengis.net/def/area/EPSG/0/1262\"/>\n"
        + "  <gml:ellipsoidalCS xlink:href=\"http://www.opengis.net/def/cs/EPSG/0/6422\"/>\n"
        + "  <gml:geodeticDatum xlink:href=\"http://www.opengis.net/def/datum/EPSG/0/6326\"/>\n"
        + "</gml:GeodeticCRS>";

    String query = "declare namespace gml = \"" + Constants.NAMESPACE_GML + "\";" + Constants.NEW_LINE
        + "let $x := collection('" + Constants.COLLECTION_NAME + "')" + Constants.NEW_LINE
        + "return insert node <dictionaryEntry xmlns=\"" + Constants.NAMESPACE_GML + "\">"
        + testDefiniton
        + "</dictionaryEntry> into $x";
    DbManager.getInstance().getDb().updateQuery(query, DbManager.USER_DB);

    String uri = "local/crs/EPSG/0/test1";
    ResolveRequest request = new ResolveRequest(uri);
    ResolveResponse result = handler.handle(request);
    String data = result.getData();
    System.out.println(data);

    // Remove definition after inserting for test
    String url = "crs/EPSG/0/test1";
    query = "declare namespace gml = \"" + Constants.NAMESPACE_GML + "\";" + Constants.NEW_LINE
        + "for $x in collection('" + Constants.COLLECTION_NAME + "')//gml:identifier[contains(text(), '" + url + "')]/.." + Constants.NEW_LINE
        + "return delete node $x";
    DbManager.getInstance().getDb().updateQuery(query, DbManager.USER_DB);
    DbManager.clearCache();

    // Now check result from "resolved data" (if contains "empty" then return false)
    assertFalse(data.contains("<empty/>"));

  }

  /**
   * Test of handle method, of class CrsCombineHandler.
   */
//  @Ignore
  @Test
  public void testMissingParameters() throws Exception {
    System.out.println("testMissingParameters");
    
    String uri = "local/crs?authority=EPSG&version=0";
    ResolveRequest request = new ResolveRequest(uri);
    
    try {
      ResolveResponse res = handler.handle(request);
      fail();
    } catch (SecoreException ex) {
      if (!ex.getExceptionCode().equals(ExceptionCode.MissingParameterValue)) {
        fail("expected a missing parameter exception");
      }
    }
  }

  /**
   * Test of handle method, of class GeneralHandler.
   */
//  @Ignore
  @Test
  public void testInsert() throws Exception {
    System.out.println("insert");
    
    String query = "declare namespace gml = \"" + Constants.NAMESPACE_GML + "\";" + Constants.NEW_LINE
          + "let $x := collection('" + Constants.COLLECTION_NAME + "')" + Constants.NEW_LINE
          + "return insert node <dictionaryEntry xmlns=\"" + Constants.NAMESPACE_GML + "\">"
          + getData("4326_newdef.xml")
          + "</dictionaryEntry> into $x";
    DbManager.getInstance().getDb().updateQuery(query, DbManager.USER_DB);
    
    String uri = "local/crs/EPSG/0/43260";
    ResolveRequest request = new ResolveRequest(uri);
    ResolveResponse result = handler.handle(request);
//    putData("43260.exp", result.getData());
    String expResult = getData("43260.exp");
    assertEquals(expResult, result.getData());
  }

  /**
   * Test of handle method, of class GeneralHandler.
   */
//  @Ignore
  @Test
  public void testDelete() throws Exception {
    System.out.println("delete");
    
    String url = "crs/EPSG/0/43260";
    String query = "declare namespace gml = \"" + Constants.NAMESPACE_GML + "\";" + Constants.NEW_LINE
          + "for $x in collection('" + Constants.COLLECTION_NAME + "')//gml:identifier[contains(text(), '"+ url +"')]/.." + Constants.NEW_LINE
          + "return delete node $x";
    DbManager.getInstance().getDb().updateQuery(query, DbManager.USER_DB);
    DbManager.clearCache();
    
    String uri = "local/crs/EPSG/0/43260";
    ResolveRequest request = new ResolveRequest(uri);
    try {
      ResolveResponse result = handler.handle(request);
      System.out.println(result.getData());
      fail("delete didn't result in empty result exception");
    } catch (SecoreException ex) {
      if (!ex.getExceptionCode().equals(ExceptionCode.NoSuchDefinition)) {
        fail("delete didn't result in empty result exception");
      }
    }
  }

  /**
   * Test of handle method, of class GeneralHandler.
   */
//  @Ignore
  @Test
  public void testHandle() throws Exception {
    System.out.println("handle");
    String uri = "local/crs/EPSG/0/4326";
    ResolveRequest request = new ResolveRequest(uri);
    ResolveResponse result = handler.handle(request);
    putData("43260.exp2", result.getData());
    String expResult = getData("43260.exp2");
    assertEquals(expResult, result.getData());
  }

  /**
   * Test of handle method, of class GeneralHandler.
   */
//  @Ignore
  @Test
  public void testUrnTranslation() throws Exception {
    System.out.println("testUrnTranslation");
    
    String query = "declare namespace gml = \"" + Constants.NAMESPACE_GML + "\";" + Constants.NEW_LINE
          + "let $x := collection('" + Constants.COLLECTION_NAME + "')" + Constants.NEW_LINE
          + "return insert node <dictionaryEntry xmlns=\"" + Constants.NAMESPACE_GML + "\">"
          + getData("ImageCRS_newdef.xml")
          + "</dictionaryEntry> into $x";
    DbManager.getInstance().getDb().updateQuery(query, DbManager.USER_DB);
    
    String uri = "local/crs/OGC/0.1/Image2D";
    ResolveRequest request = new ResolveRequest(uri);
    ResolveResponse result = handler.handle(request);
//    putData("ImageCRS.exp", result.getData());
    String expResult = getData("ImageCRS.exp");
    assertEquals(expResult, result.getData());
    
    uri = "local/crs/EPSG/0/4326";
    request = new ResolveRequest(uri);
    result = handler.handle(request);
    System.out.println(result.getData());
  }
}
