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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/
package secore.gml;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import secore.db.*;

import static org.junit.Assert.*;
import org.junit.Test;
import static secore.db.DbManager.FIX_GML_COLLECTION_NAME;
import secore.handler.GeneralHandler;
import secore.req.ResolveRequest;
import secore.req.ResolveResponse;
import secore.util.Pair;
import secore.util.SecoreUtil;

/**
 *
 * @author Bang Pham Huu
 */
public class GMLValidAllDefinition {

  GeneralHandler handler; // This will handle resolve complete URI
  GMLValidator gmlValidator; // This will validate resolved GML definition

  // This function will check valid of all definition except Parameterize CRS
  public GMLValidAllDefinition() throws Exception {
    handler = new GeneralHandler();
    gmlValidator = new GMLValidator();

  }

  /**
   * This function will check all definition in userdb except ParameterizeCRS
   * output file is etc/testdata/gml_valid_all_definition_userdb.txt
   *
   * @throws Exception
   */
  @Test
  public void testUserDb() throws Exception {
    // test with userdb first
    testAllDefinitionInDb(DbManager.USER_DB);
  }

  /**
   * This function will check all definition in gml db output file is
   * etc/testdata/gml_valid_all_definition_gmldb.txt (it takes *long* time to test all the definition).
   *
   * @throws Exception
   */
  @Test
  public void testGMLDb() throws Exception {
    //testAllDefinitionInDb(FIX_GML_COLLECTION_NAME);
  }

  /**
   * This function will valid all definitions which URL is cached in DBManager
   *
   * @throws Exception
   */
  public final void testAllDefinitionInDb(String Db) throws Exception {
    String getAllGMLDbDefinition = "declare namespace gml = \"http://www.opengis.net/gml/3.2\";\n"
        + "let $x := collection('gml')//gml:identifier[contains(.,'/def')]/text()\n"
        + "return\n"
        + " if (exists($x)) then for $i in $x return <el>{$i} 0</el>\n"
        + " else <empty/>";

    String getAllUserDbDefinition = "declare namespace gml = \"http://www.opengis.net/gml/3.2\";\n"
        + "let $x := collection('userdb')//gml:identifier[contains(.,'/def')]/text()\n"
        + "return\n"
        + " if (exists($x)) then for $i in $x return <el>{$i} 1</el>\n"
        + " else <empty/>";

    // Need to do this as when run this test if DbManager has no cache then all returns null
    String versionNumber = DbManager.FIX_GML_VERSION_NUMBER;
    String initData = SecoreUtil.queryDef("/def", false, true, true, versionNumber);

    // Check test with userdb or gml db(default)
    String queryKey = getAllGMLDbDefinition;
    String outputFile = "etc/testdata/gml_valid_all_definition_gmldb.txt";
    if (Db.equals("userdb")) {
      queryKey = getAllUserDbDefinition;
      outputFile = "etc/testdata/gml_valid_all_definition_userdb.txt";
    }

    // Get all definition URL by key (XQUERY) from cached DbManager
    Pair<String, String> tmpPair = DbManager.getCached(queryKey);
    String allURL = tmpPair.fst;

    String[] urlArray = allURL.split("\n");

    // List of ParameterizeCRS is not valid
    String[] urlIgnoreList = {"crs/AUTO/1.3/42001", "crs/OGC/0/AnsiDate", "crs/OGC/0/ChronometricGeologicTime", "crs/OGC/0/Index1D",
      "crs/OGC/0/Index2D", "crs/OGC/0/Index3D", "crs/OGC/0/Index4D", "crs/OGC/0/Index5D", "crs/OGC/0/JulianDate", "crs/OGC/0/Temporal",
      "crs/OGC/0/TruncatedJulianDate", "crs/OGC/0/UnixTime"};

    // Remove duplicate String
    urlArray = new HashSet<String>(Arrays.asList(urlArray)).toArray(new String[0]);

    // Sort array of url
    Arrays.sort(urlArray);

    // Write the check result to file for easy reading
    PrintWriter writer = new PrintWriter(outputFile, "UTF-8");

    boolean isSuccess = true;

    for (int i = 0; i < urlArray.length; i++) {
      // substring from "/def" + 4 to space to get the url definition
      int tmpIndex = urlArray[i].lastIndexOf("def/") + 4;
      int spaceIndex = urlArray[i].lastIndexOf(" ");

      // Get the url (i.e: /crs/EPSG/0/4326)
      String url = urlArray[i].substring(tmpIndex, spaceIndex);

      // Check url in urlIgnoreList
      boolean isIgnore = false;
      for (String s : urlIgnoreList) {
        if (s.equals(url)) {
          isIgnore = true;
        }
      }

      // If url is in urlIngoreList (parametersizeCRS) then ignore it
      if (isIgnore) {
        continue;
      }

      System.out.print("Checking definition: " + url + "\n");

      ResolveRequest request = new ResolveRequest(url);
      ResolveResponse result = handler.handle(request);

      // Get resloved definition from SECORE
      String tmpData = result.getData();

      // Now validate it by GMLValidator
      String validResult = gmlValidator.parseAndValidateGMLFile(tmpData);

      if (validResult.equals("")) {
        System.out.println("PASS: " + url + "\n");
        writer.println("PASS: " + url);
      } else {
        System.out.println("FAIL: " + url + "\n" + validResult);
        writer.println("FAIL: " + url + "\n" + validResult);

        // Test case is false but still run to all definition and write result to file for looking easily.
        isSuccess = false;
      }
    }

    // close file
    writer.close();

    // If this is false then test fail
    assertTrue(isSuccess);

  }
}
