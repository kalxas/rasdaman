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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.secore.util;

import org.rasdaman.secore.ConfigManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.secore.db.DbManager;
import static org.rasdaman.secore.util.Constants.*;

/**
 * String utilities.
 *
 * @author Dimitar Misev
 */
public class StringUtil {

    private static Logger log = LoggerFactory.getLogger(StringUtil.class);

    public static String SERVLET_CONTEXT = "/def";
    public static String SERVICE_URI = ConfigManager.getInstance().getServiceUrl();
    public static boolean SERVICE_URI_SET = false;
    // this URI is changed by the servlet during the first request.

    public static String START_DIGIT_REGEXP = "^\\d+=.+";

    // substring from full uri to get the service uri (e.g: localhost:8080/def/crs/epsg/0/4326 returns localhost:8080/def/)
    public static String SERVICE_URI_REGEXP = "(http|https)://.*?/.*?/?";

    private static final ScriptEngineManager engineManager;
    private static final ScriptEngine engine;

    static {        
        engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName(JAVA_SCRIPT_ENGINE);
    }

    /**
     * Evaluate a JavaScript expression
     *
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

    /**
     * URL-decode a string, if needed
     * @param encodedText
     * @return
     */
    public static String urldecode(String encodedText) {
        if (encodedText == null) {
            return null;
        }
        String decoded = encodedText;
        if (!encodedText.contains(WHITESPACE)) {
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

    /**
     * Return the substring after the "def" from URI)
     * @param s
     * @return (e.g: crs/EPSG/0/4326)
     */
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


    /**
     * Strip the service URL and def (e.g: http://localhost:8080/.../4326)
     * @param url
     * @return /crs/EPSG/0/4326
     */
    public static String stripServiceURI(String url) {
        int index = url.indexOf(Constants.WEB_APPLICATION_NAME);
        if (index == -1) {
            // cannot find /def/ in the URI
            return url;
        } else {
            return url.substring(index + Constants.WEB_APPLICATION_NAME.length(), url.length());
        }
    }

    /**
     * Get the version number from URI (with 4 parameters)
     * //e.g: http://localhost:8080/def/crs/EPSG/0/4326 or URN (e.g: urn:ogc:def:axis:EPSG::9902)
     * @param s (0)
     * @return
     */
    public static String getVersionNumber(String s) {
        String versionNumber = null;
        String stripDef = stripDef(s);

        // e.g: crs/EPSG/0/4326
        String[] tmp = stripDef.split(REST_SEPARATOR);
        if (tmp.length > 2) {
            versionNumber = tmp[2];
        } else {
            // URN (axis:EPSG::9902)
            // NOTE: version number here is empty, which means it is version 0, it can be a number, such as: "urn:ogc:def:crs:AUTO:1.3:42001"
            tmp = stripDef.split(URN_SEPARATOR);
            versionNumber = tmp[2];
        }
        return versionNumber;
    }


    /**
     * Check if a URI is a standard URN after the service address
     * with 4 parameters: (e.g: crs/AUTO/1.3/42001 or axis:EPSG::9902).
     *
     * Use to check when insert/update gml identifier in definition correctly.
     * @param s
     * @return
     */
    public static boolean isValidIdentifierURI(String s) {
        String stripDef = stripDef(s);
        if (stripDef.split(REST_SEPARATOR).length != 4) {
            // check if input is URN (e.g: axis:EPSG::9902)
            if (stripDef.split(URN_SEPARATOR).length != 4) {
                return false;
            }
        }
        return true;
    }

    /**
     * Replace the versionNumber from source to target with a full 4 parameters URL
     * (e.g: http://localhost:8080/def/crs/EPSG/0/4326)
     * Used when change the version number of URN in userdb which is belonged to EPSG database.
     * @param s
     * @param targetVersionNumber
     * @return
     * @throws org.rasdaman.secore.util.SecoreException
     */
    public static String replaceVersionNumber(String s, String targetVersionNumber) throws SecoreException {
        String stripDef = stripDef(s);
        // e.g: crs/EPSG/0/4326
        String[] tmp = stripDef.split(REST_SEPARATOR);
        tmp[2] = targetVersionNumber;
        String ret = getServiceUri(s);

        for (int i = 0; i < tmp.length; i++) {
            ret = ret + tmp[i];
            if (i < tmp.length - 1) {
                ret = ret + REST_SEPARATOR;
            }
        }

        return ret;
    }

    /**
     * Check if a 4 full parameters CRS URI has default userdb version (e.g: 0)
     * @param s
     * @return
     */
    public static boolean hasDefaultUserDbVersion(String s) {
        String stripDef = stripDef(s);
        // e.g: crs/EPSG/0/4326
        String[] tmp = stripDef.split(REST_SEPARATOR);

        if (tmp[2].equals(DbManager.FIX_USER_VERSION_NUMBER)) {
            return true;
        }
        return false;
    }

    public static String removeDuplicateDef(String s) {
        int ind = s.indexOf(SERVLET_CONTEXT);
        return s.substring(0, ind) + s.substring(ind + SERVLET_CONTEXT.length());
    }

    /**
     * Return the substring from full uri to get the service uri 
     * (e.g: localhost:8080/def/crs/epsg/0/4326 returns localhost:8080/def/)
     * @param uri the url
     * @return 
     * @throws org.rasdaman.secore.util.SecoreException 
     */
    public static String getServiceUri(String uri) throws SecoreException {        
        String serviceUri = null;
        Pattern regex = Pattern.compile(SERVICE_URI_REGEXP);
        Matcher regexMatcher = regex.matcher(uri);
        if (regexMatcher.find()) {
            serviceUri = regexMatcher.group();
        } else {
            throw new SecoreException(ExceptionCode.InvalidRequest, "The requested service URI is not valid: " + uri);
        }        

        return serviceUri;
    }

    /**
     * Only supports simple definition URNs. No support for compound or
     * parameterized URNs.
     *
     * @param uri a URN to be converted into a URI
     * @return 
     * @throws org.rasdaman.secore.util.SecoreException
     */
    public static String convertUrnToUrl(String uri) throws SecoreException {
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
     * Utility to check if collection name does exist.
     * //e.g: userdb, gml_85
     * @param query XQuery (e.g: declare ... let $x := collection('') ...)
     * @return String
     */
    public static String getCollectionNameFromXQuery(String query) {
        String collectionName = "";
        Pattern p = Pattern.compile(Constants.COLLECTION + "\\('(.*?)'\\)");
        Matcher m = p.matcher(query);
        if (m.find()) {
            collectionName = m.group(1);
        }
        return collectionName;
    }

    /**
     * Joins strings of an array in a specified range (within the size of the
     * array) and with a specified separator.
     *
     * @param array Array of Strings
     * @param start Lower bound of the range
     * @param end Upper bound of the range
     * @param separator The separator String
     * @return The joined String
     */
    public static String join(String[] array, int start, int end, String separator) {
        String joined = "";
        for (int i = Math.max(start, 0); i < Math.min(end, array.length); i++) {
            joined += (joined.isEmpty() ? "" : separator) + array[i];
        }
        return joined;
    }

    /**
     * Get the text content of an element in given xml.
     * // e.g:  <gml:identifier codeSpace="OGP">http://www.opengis.net/def/axis/EPSG/0/1_54698797</gml:identifier>
     * // return http://www.opengis.net/def/axis/EPSG/0/1_54698797
     * @param xml xml
     * @param elname element name (e.g: identifier)
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
     *
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
     *
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
        List<String> l = new LinkedList<>();
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
     * @param s a GML definition
     * @param versionNumber
     * @return the definition with the fixed links
     */
    public static String fixLinks(String s, String versionNumber) throws SecoreException {
        return fixLinks(s, ConfigManager.getInstance().getServiceUrl(), versionNumber);
    }

    /**
     * Replace all URNs in s with URLs in REST format, given a service URI and version
     * (e.g: 8.5, 8.6 with gml dictionary or 0 with user dictionary)
     *
     * @param s a GML definition
     * @param serviceUri
     * @param versionNumber
     * @return the definition with the fixed links
     */
    public static String fixLinks(String s, String serviceUri, String versionNumber) {
        // $1 = operation
        // $2 = authority
        // $3 = code
        // TODO fix handling of version, 0 is assumed now
        String ret = s;
        if (serviceUri != null) {
            serviceUri = wrapUri(serviceUri);
            ret = ret.replaceAll(URN_PREFIX + ":([^:]+):([^:]+):([^:]+):([^<>'\"]+)", serviceUri + "$1/$2/$3/$4");
            ret = ret.replaceAll(URN_PREFIX + ":([^:]+):([^:]+)::([^<>'\"]+)", serviceUri + "$1/$2/" + versionNumber + "/$3");
        }
        return ret;
    }

    /**
     * Replace all URNs in s with URLs in REST format, given a service URI. Also
     * replace { and } with {{ and }}, so that a def can be properly inserted in
     * the XML database.
     *
     * @param s a GML definition
     * @param serviceUri
     * @param versionNumber
     * @return the definition with the fixed links and curly braces
     */
    public static String fixDef(String s, String serviceUri, String versionNumber) {
        return fixLinks(s, serviceUri, versionNumber).replaceAll("\\{", "{{").replaceAll("\\}", "}}");
    }

    /**
     * @return the GML namespace used in the underlying database
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
               || result.trim().equals(EMPTY);
    }

    /**
     * @param uri
     * @return uri with / appended if it's not already.
     */
    public static String wrapUri(String uri) {
        if (uri != null && !uri.endsWith(REST_SEPARATOR)) {
            uri += REST_SEPARATOR;
        }
        return uri;
    }

    /**
     * return uri without appended / if it is already
     * @param uri
     * @return
     */
    public static String unWrapUri(String uri) {
        if (uri.endsWith(REST_SEPARATOR)) {
            return uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    /**
     * @param uri
     * @return uri with the last '/' removed.
     */
    public static String unwrapUri(String uri) {
        while (uri != null && uri.endsWith(REST_SEPARATOR)) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    /**
     * Check if URI is the parent node of children URI which contains version number
     * e.g: /def/crs/EPSG/0 will return /def/crs/EPSG/0/4326, /def/crs/EPSG/0/3857,...
     * @param uri
     * @return
     */
    public static boolean parentVersionNumberUri(String uri) {
        String pattern = "/def/.*/.*/?";
        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(uri);
        if (m.find()) {
            return true;
        }
        return false;
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
     * @throws org.rasdaman.secore.util.SecoreException
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

    /**
     * Write stack trace error to log file instead of throwing error
     *
     * @param method (add, update, delete)
     * @param e (Exception)
     */
    public static void printStackTraceWhenEditDB(String method, Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log.debug("Error when " + method + " definition by BaseX: " + sw.toString());
    }
    
    /**
     * From the gml output of BaseX which contains stored URL (e.g: http://localhost:8080/def/crs/EPSG/0/4326).
     * Replace all these URLs with the domain configured in secore.properties for service.url
     * (e.g: http://opengis.net/def). Then, URL in newly output will be: http://opengis.net/def/crs/EPSG/0/4326
     * @param gml: output of BaseX query when resolving a request.
     * @return newly replaced gml output
     */
    public static String fixLinks(String gml) {
        String patternString = "(http(s)?://[A-Za-z0-9-_.:]*/" + Constants.WEB_APPLICATION_NAME + "/.*)";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(gml);
        
        String prefixDomain = ConfigManager.getInstance().getServiceUrl();
        
        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find()) {
            String url = prefixDomain + matcher.group(1).split(Constants.WEB_APPLICATION_NAME)[1];
            matcher.appendReplacement(stringBuffer, url);
        }
        matcher.appendTail(stringBuffer);
        
        String newGML = stringBuffer.toString();
        return newGML;        
    }
}
