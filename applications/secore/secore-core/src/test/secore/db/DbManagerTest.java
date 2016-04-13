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
package secore.db;

import org.junit.Test;
import static org.junit.Assert.*;
import secore.util.StringUtil;

/**
 *
 * @author Dimitar Misev
 */
public class DbManagerTest {
  
  /**
   * Test of getInstance method, of class DbManager.
   */
  @Test
  public void testGetInstance() {
    assertNotNull(DbManager.getInstance().getDb());
  }

  @Test
  public void testQuery() throws Exception {
    String query =
            "declare namespace gml = \"" + StringUtil.getGmlNamespace() + "\";\n"
          + "let $d := doc('gml')\n"
          + "return $d//gml:identifier[contains(text(), '/crs/EPSG/0/4326')]";
    String result = DbManager.getInstance().getDb().query(query);
    assertTrue(result.matches("<gml:identifier [^>]+>.+/crs/EPSG/0/4326</gml:identifier>"));
  }

  @Test
  public void testDocumentNames() throws Exception {
    String query =
           "for $doc in collection() return base-uri($doc)";
    String result = DbManager.getInstance().getDb().query(query);
    assertEquals("userdb/userdb.xml", result);
  }
}
