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

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;
import org.slf4j.LoggerFactory;

/**
 * XML utilities.
 *
 * @author Dimitar Misev
 */
public class XMLUtil {

  private static org.slf4j.Logger log = LoggerFactory.getLogger(XMLUtil.class);

  public static IXMLElement parse(String xml) throws SecoreException {
    IXMLParser parser = null;
    try {
      parser = XMLParserFactory.createDefaultXMLParser();
    } catch (Exception ex) {
      log.error("Failed creating XML parser.");
      throw new SecoreException(ExceptionCode.InternalComponentError);
    }
    IXMLReader reader = StdXMLReader.stringReader(xml);
    parser.setReader(reader);
    try {
      return (IXMLElement) parser.parse();
    } catch (XMLException ex) {
      log.error("XML parsing failed.", ex);
      throw new SecoreException(ExceptionCode.XmlNotValid);
    }
  }

  /**
   * Replaces all <tt>'&'</tt> characters with <tt>'&amp;'</tt>
   * @param aString
   * @return
   */
  private static String escapeAmpersands(String aString){
    return aString.replace("&", "&" + PREDEFINED_ENTITY_AMPERSAND + ";");
  }

  /**
   * Replaces all <tt>'\''</tt> characters with <tt>'&apos;'</tt>
   * @param aString
   * @return
   */
  private static String escapeApostrophes(String aString){
    return aString.replace("'", "&" + PREDEFINED_ENTITY_APOSTROPHE + ";");
  }

  /**
   * Replaces all <tt>'<'</tt> characters with <tt>'&lt;'</tt>
   * @param aString
   * @return
   */
  private static String escapeLessThanSigns(String aString){
    return aString.replace("<", "&" + PREDEFINED_ENTITY_LESSTHAN_SIGN + ";");
  }

  /**
   * Replaces all <tt>'>'</tt> characters with <tt>'&gt;'</tt>
   * @param aString
   * @return
   */
  private static String escapeGreaterThanSigns(String aString){
    return aString.replace(">", "&" + PREDEFINED_ENTITY_GREATERTHAN_SIGN + ";");
  }

  /**
   * Replaces all <tt>'\"'</tt> characters with <tt>'&quot;'</tt>
   * @param aString
   * @return
   */
  private static String escapeQuotes(String aString){
    return aString.replace("\"", "&" + PREDEFINED_ENTITY_QUOTES + ";");
  }

  /**
   * Fix a string for valid insertion in XML document (escape reserved entities).
   * @see http://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references
   * @param aString
   * @return XML-escaped input string.
   */
  public static String escapeXmlPredefinedEntities(String aString){
    String escapedString;

    escapedString = escapeAmpersands(aString);
    escapedString = escapeApostrophes(escapedString);
    escapedString = escapeLessThanSigns(escapedString);
    escapedString = escapeGreaterThanSigns(escapedString);
    escapedString = escapeQuotes(escapedString);

    return escapedString;
  }

    // Predefined entities' names
    private static final String PREDEFINED_ENTITY_AMPERSAND        = "amp";
    private static final String PREDEFINED_ENTITY_APOSTROPHE       = "apos";
    private static final String PREDEFINED_ENTITY_LESSTHAN_SIGN    = "lt";
    private static final String PREDEFINED_ENTITY_GREATERTHAN_SIGN = "gt";
    private static final String PREDEFINED_ENTITY_QUOTES           = "quot";
}
