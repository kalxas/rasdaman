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
package secore.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Dimitar Misev
 */
public class StringUtilTest {

  /**
   * Test of urldecode method, of class StringUtil.
   */
  @Test
  public void testUrldecode() {
    String encodedText = "hello%20world";
    String expResult = "hello world";
    String result = StringUtil.urldecode(encodedText);
    assertEquals(expResult, result);
  }

  /**
   * Test of getElementValue method, of class StringUtil.
   */
  @Test
  public void testGetElementValue() {
    String xml = "<hello:world>HELLO WORLD</hello:world>";
    String elname = "world";
    String expResult = "HELLO WORLD";
    String result = StringUtil.getElementValue(xml, elname);
    assertEquals(expResult, result);
  }
  
  @Test
  public void testFixLinks() {
    String arg = "<gml:identifier codeSpace=\"OGP\">urn:ogc:def:cs:OGC:0.1:Cartesian2D</gml:identifier>";
    String result = StringUtil.fixLinks(arg);
    System.out.println(result);
    String expResult = "<gml:identifier codeSpace=\"OGP\">http://www.opengis.net/def/cs/OGC/0.1/Cartesian2D</gml:identifier>";
    assertEquals(expResult, result);
  }
}
