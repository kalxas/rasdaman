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

import secore.req.ResolveRequest;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static secore.util.Constants.*;

/**
 * String utilities.
 *
 * @author Dimitar Misev
 */
public class StringUtil {

  private static Logger log = LoggerFactory.getLogger(StringUtil.class);
  
  public static String SERVLET_CONTEXT = "/def";
  public static String SERVICE_URI = Config.getInstance().getServiceUrl();
  public static boolean SERVICE_URI_SET = false;
  // this URI is changed by the servlet during the first request.
  
  public static String START_DIGIT_REGEXP = "^\\d+=.+";
  
  private static final ScriptEngineManager engineManager;
  private static final ScriptEngine engine;
  
  static {
    engineManager = new ScriptEngineManager();
    engine = engineManager.getEngineByName(JAVA_SCRIPT_ENGINE);
  }
  
  /**
   * Evaluate a JavaScript expression
   * @param expr JavaScript expression
   * @return the value of the expression as string
   * @throws ScriptException 
   */
  public static String evaluate(String expr) throws ScriptException {
    log.trace("Evaluating expression: " + expr);
    Object res = engine.eval(expr);
    if (res != null) {
      return res.toString();
    }
    return null;
  }

  /** URL-decode a string, if needed */
  public static String urldecode(String encodedText) {
    if (encodedText == null) {
      return null;
    }
    String decoded = encodedText;
    if (encodedText.indexOf(WHITESPACE) == -1) {
      try {
        decoded = URLDecoder.decode(encodedText, UTF8_ENCODING);
      } catch (UnsupportedEncodingException ex) {
      }
    }
    return decoded;
  }
  
  public static boolean isUrn(String s) {
    return s != null && s.startsWith(URN_PREFIX);
  }

  public static String stripDef(String s) {
    String servletContext = SERVLET_CONTEXT + REST_SEPARATOR;
    s = wrapUri(s);
    int fragmentPos = s.indexOf(QUERY_SEPARATOR);
    if (fragmentPos == -1) {
      fragmentPos = Integer.MAX_VALUE;
    }
    if (s.contains(servletContext) && s.indexOf(servletContext) < fragmentPos) {
      s = s.substring(s.indexOf(servletContext) + servletContext.length());
    } else if (isUrn(s)) {
      s = s.substring(URN_PREFIX.length() + 1, s.length());
    } else if (s.startsWith(LOCAL_URI)) {
      s = s.substring(LOCAL_URI.length());
    }
    if (s.startsWith(REST_SEPARATOR)) {
      return s.substring(1);
    }
    return s;
  }
  
  public static String removeDuplicateDef(String s) {
    int ind = s.indexOf(SERVLET_CONTEXT);
    return s.substring(0, ind) + s.substring(ind + SERVLET_CONTEXT.length());
  }

  public static String getServiceUri(String s) {
    String servletContext = SERVLET_CONTEXT + REST_SEPARATOR;
    String ret = s;
    if (s.contains(servletContext)) {
      ret = s.substring(0, s.indexOf(servletContext) + servletContext.length());
    } else if (isUrn(s)) {
      ret = SERVICE_URI;
      if (!ret.endsWith(REST_SEPARATOR)) {
        ret += REST_SEPARATOR;
      }
      if (!ret.endsWith(servletContext)) {
        ret += servletContext;
      }
    } else if (s.startsWith(LOCAL_URI)) {
      ret = LOCAL_URI;
    } else if (!s.startsWith(HTTP_PREFIX)) {
      ret = "";
    }
    ret = wrapUri(ret);
    return ret;
  }

  /**
   * Only supports simple definition URNs. No support for compund or parameterized
   * URNs.
   * 
   * @param uri a URN to be converted into a URI
   */
  public static String convertUrnToUrl(String uri) {
    String ret = getServiceUri(uri);
    
    uri = StringUtil.stripDef(uri);
    String[] values = uri.split(URN_SEPARATOR);
    for (String value : values) {
      if (!EMPTY.equals(value)) {
        ret += value;
      } else {
        ret += ZERO;
      }
      ret += REST_SEPARATOR;
    }
    return ret;
  }
  
  /**
   * Joins strings of an array in a specified range (within the size of the array) and with a specified separator.
   * @param array     Array of Strings
   * @param start     Lower bound of the range
   * @param end       Upper bound of the range
   * @param separator The separator String
   * @return          The joined String
   */
  public static String join(String[] array, int start, int end, String separator) {
    String joined = "";
    for (int i = Math.max(start, 0);  i < Math.min(end, array.length); i++) {
      joined += (joined.isEmpty() ? "" : separator) + array[i];
    }
    return joined;
  }

  /**
   * Get the text content of an element in given xml.
   * @param xml xml
   * @param elname element name
   * @return the text content of elname
   */
  public static String getElementValue(String xml, String elname) {
    String ret = null;
    Pattern pattern = Pattern.compile("<(.+:)?" + elname + "[^>]*>([^<]+)</(.+:)?" + elname + ">.*");
    Matcher matcher = pattern.matcher(xml);
    if (matcher.find()) {
      ret = matcher.group(2);
    }
    return ret;
  }

  /**
   * Replace the value of an element in given xml with replacement.
   * @param xml xml
   * @param elname element name
   * @param replacement new element value
   * @return the text content of elname
   */
  public static String replaceElementValue(String xml, String elname, String replacement) {
    String ret = xml;
    Pattern pattern = Pattern.compile("<(.+:)?" + elname + "[^>]*>([^<]+)</(.+:)?" + elname + ">.*");
    Matcher matcher = pattern.matcher(xml);
    if (matcher.find()) {
      String match = ">" + matcher.group(2) + "<";
      replacement = replacement.replace("&", "%26");
      ret = xml.replace(match, ">" + replacement + "<");
    }
    return ret;
  }

  /**
   * Get the root element name of the given xml document.
   * @param xml an XML document
   * @return the root name, or null in case of an error
   */
  public static String getRootElementName(String xml) {
    int start = 0;
    while (start < xml.length()) {
      start = xml.indexOf("<", start);
      if (start == -1) {
        return null;
      }
      if (xml.charAt(start + 1) != '?') {
        int end = start + 1;
        while (end < xml.length() && xml.charAt(end) != ' ' && xml.charAt(end) != '>') {
          if (xml.charAt(end) == ':') {
            start = end;
          }
          ++end;
        }
        if (end == -1) {
          return null;
        }
        ++start;
        return xml.substring(start, end);
      } else {
        ++start;
      }
    }
    return null;
  }

  /**
   * Converts a collection to a string, separating the elements by c
   *
   * @param l The StringList
   * @param c The delimiter
   * @return A String of the ListElements separated by c.
   */
  public static <T> String ltos(Collection<T> l, Character c) {
    String s = EMPTY;
    for (Iterator<T> iter = l.iterator(); iter.hasNext();) {
      if (!s.equalsIgnoreCase(EMPTY)) {
        s = s + c + iter.next().toString();
      } else {
        s = s + iter.next().toString();
      }
    }
    return s;
  }

  /**
   * Converts a string to a list
   *
   * @param s The StringList
   * @return A String of the ListElements separated by c.
   */
  public static List<String> stol(String s) {
    List<String> l = new LinkedList<String>();
    if (s == null) {
      return l;
    }
    String[] sl = s.split(",");
    for (int i = 0; i < sl.length; i++) {
      l.add(sl[i]);
    }
    return l;
  }
  
  /**
   * Replace all URNs in s with URLs in REST format.
   * 
   * @param s a GML definition
   * @return the definition with the fixed links
   */
  public static String fixLinks(String s) {
    return fixLinks(s, Config.getInstance().getServiceUrl());
  }
  
  /**
   * Replace all URNs in s with URLs in REST format, given a service URI.
   * 
   * @param s a GML definition
   * @return the definition with the fixed links
   */
  public static String fixLinks(String s, String serviceUri) {
    // $1 = operation
    // $2 = authority
    // $3 = code
    // TODO fix handling of version, 0 is assumed now
    String ret = s;
    if (serviceUri != null) {
      serviceUri = wrapUri(serviceUri);
      ret = ret.replaceAll(URN_PREFIX + ":([^:]+):([^:]+):([^:]+):([^<>'\"]+)", serviceUri + "$1/$2/$3/$4");
      ret = ret.replaceAll(URN_PREFIX + ":([^:]+):([^:]+)::([^<>'\"]+)", serviceUri + "$1/$2/0/$3");
    }
    return ret;
  }
  
  /**
   * Replace all URNs in s with URLs in REST format, given a service URI.
   * Also replace { and } with {{ and }}, so that a def can be properly inserted
   * in the XML database.
   * 
   * @param s a GML definition
   * @return the definition with the fixed links and curly braces
   */
  public static String fixDef(String s, String serviceUri) {
    return fixLinks(s, serviceUri).replaceAll("\\{", "{{").replaceAll("\\}", "}}");
  }
  
  /**
   * @return the GML namespace used in the underlying database
   * @throws SecoreException 
   */
  public static String getGmlNamespace() {
//    String ret = EMPTY;
//    String prefix = "declare namespace gml = \"";
//    String suffix = "let $x := doc('gml')/gml:Dictionary\n" +
//                    "return namespace-uri($x)";
//    
//    // iterate through possible namespaces to find the one we need
//    for (String ns : GML_NAMESPACES) {
//      String query = prefix + ns + "\";\n" + suffix;
//      try {
//        ret = DbManager.getInstance().getEpsgDb().query(query);
//      } catch (Exception ex) {
//      }
//      if (!EMPTY.equals(ret)) {
//        break;
//      }
//    }
//    if (EMPTY.equals(ret)) {
//      throw new RuntimeException("Couldn't determine the GML namespace used in the database.");
//    }
//    return ret;
    return GML_NAMESPACES[1];
  }
  
  /**
   * @param s string
   * @return the original s without any colon at the end
   */
  public static String removeLastColon(String s) {
    if (s.endsWith(URN_SEPARATOR)) {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }
  
  public static boolean emptyQueryResult(String result) {
    return result == null 
        || result.equals(EMPTY_XML) 
        || result.replaceAll("[\\n\\r]", "").equals(EMPTY);
  }
  
  /**
   * @return uri with / appended if it's not already.
   */
  public static String wrapUri(String uri) {
    if (uri != null && !uri.endsWith(REST_SEPARATOR)) {
      uri += REST_SEPARATOR;
    }
    return uri;
  }
  
  /**
   * @return uri with the last '/' removed.
   */
  public static String unwrapUri(String uri) {
    while (uri != null && uri.endsWith(REST_SEPARATOR)) {
      uri = uri.substring(0, uri.length() - 1);
    }
    return uri;
  }

  /**
   * Remove the scheme/host/port from the given url, e.g.
   *
   * http://localhost:8080/def/EPSG/0/4326?test=2
   *
   * becomes
   *
   * /def/EPSG/0/4326?test=2
   *
   * @param uri input URL, can be null, absolute, relative
   * @return the path+query portions of url
   */
  public static String uriToPath(String uri) throws SecoreException {
    String ret = uri;
    if (uri != null) {
      URI tmp = null;
      try {
        tmp = new URI(uri);
      } catch (URISyntaxException ex) {
        String err = "The given identifier is not a valid URI: " + uri;
        log.error(err, ex);
        throw new SecoreException(ExceptionCode.InvalidRequest, err, ex);
      }
      ret = tmp.getPath();
      if (tmp.getQuery() != null) {
        ret += QUERY_SEPARATOR + tmp.getQuery();
      }
      if (tmp.getFragment() != null) {
        ret += FRAGMENT_SEPARATOR + tmp.getFragment();
      }
    }
    return ret;
  }
}
