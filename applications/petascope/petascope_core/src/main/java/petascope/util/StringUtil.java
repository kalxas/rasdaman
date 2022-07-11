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
package petascope.util;

import java.io.File;
import java.io.IOException;
import petascope.core.XMLSymbols;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.ogr;
import static org.rasdaman.config.ConfigManager.OWS;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * String utilities.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class StringUtil {

    public static final String MIME_URLENCODED = "application/x-www-form-urlencoded";
    public static final String ENCODING_UTF8 = "UTF-8";
    
    public static final String DOLLAR_SIGN = "$";
    public static final String AND_SIGN = "&";
    public static final String EQUAL_SIGN = "=";
    public static final String POSITIVE_INTEGER_PATTERN = "^[1-9]\\d*$";
    // e.g: $1, $2,..
    public static final Pattern POSITIONAL_PARAMETER_PATTERN = Pattern.compile("\\$[1-9]\\d*");
    public static final String FALSE_VALUE = "false";
    public static final String TRUE_VALUE = "true";
    
    public static final String POST_STRING_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String POST_TEXT_PLAIN_CONTENT_TYPE = "text/plain";

    public static final String POST_XML_CONTENT_TYPE = "text/xml";
    public static final String POST_XML_SOAP_CONTENT_TYPE = "application/soap+xml";
  
    public static final Pattern squareBracketsPattern = Pattern.compile("\\[(.*?)\\]");
    
    /**
     * For coverages created temporarily, used for WCPS decode() from uploaded files
     */
    public static final String TEMP_COVERAGE_PREFIX = "WCPS_TEMP_COV";


    private static String COMMA = ",";

    /**
     * Remove leading/trailing spaces and any newlines.
     *
     * @param data String to be trimmed
     * @return the trimmed result
     */
    public static String trim(String data) {
        if (data == null) {
            return null;
        }

        String ret = data.trim();
        ret = ret.replaceAll("\n", "");
        return ret;
    }

    /**
     * Wrap a long string in multiple lines
     *
     * @param s the string
     * @param lineLength the preferred line length
     * @return the wrapped string
     */
    public static String wrap(String s, int lineLength) {
        return wrap(s, ' ', lineLength, 10, 10);
    }

    /**
     * Wrap a long string in multiple lines
     *
     * @param s the string
     * @param c the preferred char on which to split the string (usually ' ')
     * @param lineLength the preferred line length
     * @param leps tolerance to the left of <code>lineLength</code>
     * @param reps tolerance to the right of <code>lineLength</code>
     * @return the wrapped string
     */
    public static String wrap(String s, char c, int lineLength, int leps, int reps) {
        if (s.length() < lineLength) {
            return s;
        }
        int eps = Math.max(leps, reps);
        for (int i = 0; i < eps; i++) {
            int l = lineLength - i;
            int r = lineLength + i;
            if (i < leps && l >= 0 && l < s.length() && s.charAt(l) == c) {
                return s.substring(0, l) + "\n" + wrap(s.substring(l), c, lineLength, leps, reps);
            }
            if (i < reps && r >= 0 && r < s.length() && s.charAt(r) == c) {
                return s.substring(0, r) + "\n" + wrap(s.substring(l), c, lineLength, leps, reps);
            }
        }
        return s;
    }

    public static String quote(String s) {
        return "\"" + s + "\"";
    }

    public static String unquote(String s) {
        if (s == null || s.length() < 2) {
            return s;
        }
        if ((s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"')
                || (s.charAt(0) == '\'' && s.charAt(s.length() - 1) == '\'')) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    public static String combine(String sep, String... s) {
        String ret = "";
        if (s.length > 0) {
            ret = s[0];
        }
        for (int i = 1; i < s.length; i++) {
            ret += sep + s[i];
        }
        return ret;
    }

    public static String join(String... s) {
        return combine("\n", s);
    }

    /**
     * Converts a collection to a string, separating the elements by ","
     *
     * @param l The StringList
     * @param c The delimiter
     * @return A String of the ListElements separated by c.
     */
    public static <T> String ltos(Collection<T> l, Character c) {
        String s = "";
        for (Iterator<T> iter = l.iterator(); iter.hasNext();) {
            if (!s.equalsIgnoreCase("")) {
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

    public static <T> T[] array(T... elements) {
        return elements;
    }

    public static String d2s(Double d) {
        Double tmp = (double) d.intValue();
        if (Math.abs(d - tmp) < 0.0000001) {
            return d.intValue() + "";
        } else {
            return d.toString();
        }
    }

    /**
     * URL-decode a string, if needed
     */
    public static String urldecode(String encodedText, String contentType) {
        if (encodedText == null) {
            return null;
        }
        String decoded = encodedText;
        // fix ticket 466
        // if (contentType == null || (contentType.equals("application/x-www-form-urlencoded") && encodedText.indexOf(" ") == -1)) {
        if (contentType == null
                || (contentType.toLowerCase().startsWith(MIME_URLENCODED) && encodedText.indexOf(" ") == -1)) {
            try {
                encodedText = encodedText.replaceAll("\\+", "%2B"); // ticket:456
                decoded = URLDecoder.decode(encodedText, ENCODING_UTF8);
            } catch (UnsupportedEncodingException ex) {
            }
        }
        return decoded.trim();
    }

    public static String urlencode(String text) {
        try {
            return URLEncoder.encode(text, ENCODING_UTF8);
        } catch (UnsupportedEncodingException ex) {
            return text;
        }
    }

    /**
     * Create a readable random string from the input string, which combine the
     * current dateTime and a random number
     *
     * @param label
     * @return
     */
    public static String addDateTimeSuffix(String label) {
        String dateTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        Random random = new Random();
        String randomNumber = String.valueOf(random.nextInt(10000) + 1);
        return label + "_" + dateTime + "_" + randomNumber;
    }

    /**
     * Check if a string which has the pattern of random string (contains
     * dd_mm_yy_hh_mm_ss_randomNumber) example: label_2016_09_09_13_56_33_5132
     *
     * @param prefixLabel (e.g: label)
     * @param randomString (e.g: label_2016_09_09_13_56_33_5132)
     * @return
     */
    public static boolean isRandomString(String prefixLabel, String randomString) {
        String regex = prefixLabel + "_[0-9]+_[0-9]+_[0-9]+_[0-9]+_[0-9]+_[0-9]+_[0-9]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(randomString);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    /**
     * <b>Taken from HttpUtils.java and modified</b>
     *
     * Parses a query string passed from the client to the server and builds a
     * <code>HashTable</code> object with key-value pairs. The query string
     * should be in the form of a string packaged by the GET or POST method,
     * that is, it should have key-value pairs in the form <i>key=value</i>,
     * with each pair separated from the next by a &amp; character.
     *
     * <p>
     * A key can appear more than once in the query string with different
     * values. However, the key appears only once in the hashtable, with its
     * value being an array of strings containing the multiple values sent by
     * the query string.
     *
     * <p>
     * The keys and values in the hashtable are stored in their decoded form, so
     * any + characters are converted to spaces, and characters sent in
     * hexadecimal notation (like <i>%xx</i>) are converted to ASCII characters.
     *
     * @param s a string containing the query to be parsed
     *
     * @return a <code>HashTable</code> object built from the parsed key-value
     * pairs
     *
     * @exception IllegalArgumentException if the query string is invalid
     *
     */
    public static Map<String, List<String>> parseQuery(String s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        Map<String, List<String>> ret = new LinkedHashMap<String, List<String>>();
        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(s, "&");
        while (st.hasMoreTokens()) {
            String pair = (String) st.nextToken();
            int pos = pair.indexOf('=');
            if (pos == -1) {
                // XXX
                // should give more detail about the illegal argument
                throw new IllegalArgumentException();
            }
            String key = parseName(pair.substring(0, pos), sb).toLowerCase();
            List<String> val = stol(parseName(pair.substring(pos + 1, pair.
                    length()), sb));
            if (ret.containsKey(key)) {
                ret.get(key).addAll(val);
            } else {
                ret.put(key, val);
            }
        }
        return ret;
    }

    /*
     * Parse a name in the query string.
     */
    private static String parseName(String s, StringBuffer sb) {
        sb.setLength(0);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '%':
                    try {
                        sb.append((char) Integer.parseInt(s.substring(i + 1, i + 3),
                                16));
                        i += 2;
                    } catch (NumberFormatException e) {
                        // XXX
                        // need to be more specific about illegal arg
                        throw new IllegalArgumentException();
                    } catch (StringIndexOutOfBoundsException e) {
                        String rest = s.substring(i);
                        sb.append(rest);
                        if (rest.length() == 2) {
                            i++;
                        }
                    }

                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Replaces all <tt>'&'</tt> characters with <tt>'&amp;'</tt>
     *
     * @param aString
     */
    public static String escapeAmpersands(String aString) {
        return aString.replace("&", "&" + XMLSymbols.PREDEFINED_ENTITY_AMPERSAND + ";");
    }
    
    public static String escapeQuotes(String str) {
        return str.replace("\"", "\\\"");
    }
    
    /**
     * Replaces all <tt>'\''</tt> characters with <tt>'&apos;'</tt>
     *
     * @param aString
     */
    private static String escapeApostrophes(String aString) {
        return aString.replace("'", "&" + XMLSymbols.PREDEFINED_ENTITY_APOSTROPHE + ";");
    }

    /**
     * Replaces all <tt>'<'</tt> characters with <tt>'&lt;'</tt>
     *
     * @param aString
     */
    private static String escapeLessThanSigns(String aString) {
        return aString.replace("<", "&" + XMLSymbols.PREDEFINED_ENTITY_LESSTHAN_SIGN + ";");
    }

    /**
     * Replaces all <tt>'>'</tt> characters with <tt>'&gt;'</tt>
     *
     * @param aString
     */
    private static String escapeGreaterThanSigns(String aString) {
        return aString.replace(">", "&" + XMLSymbols.PREDEFINED_ENTITY_GREATERTHAN_SIGN + ";");
    }

    /**
     * Fix a string for valid insertion in XML document (escape reserved
     * entities).
     * http://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references
     *
     * @param aString
     */
    public static String escapeXmlPredefinedEntities(String aString) {
        String escapedString;

        escapedString = escapeAmpersands(aString);
        escapedString = escapeApostrophes(escapedString);
        escapedString = escapeLessThanSigns(escapedString);
        escapedString = escapeGreaterThanSigns(escapedString);
        escapedString = escapeQuotes(escapedString);

        return escapedString;
    }
    
    /**
     * e.g: \" -> \\\"
     */
    public static String escapeQuotesJSON(String str) {
        return str.replace("\"", "\\\"");
    }

    /**
     * Takes two numeric strings and returns their range.
     *
     * @param lo
     * @param hi
     * @return hi-lo
     */
    public static String getRange(String lo, String hi) {
        BigDecimal bLo = new BigDecimal(lo);
        BigDecimal bHi = new BigDecimal(hi);
        return bHi.subtract(bLo).toString();
    }

    /**
     * Takes two (discrete) numeric strings and returns their range.
     *
     * @param lo
     * @param hi
     * @return hi-lo
     */
    public static String getCount(String lo, String hi) {
        BigInteger bLo = new BigInteger(lo);
        BigInteger bHi = new BigInteger(hi);
        return bHi.subtract(bLo).add(BigInteger.ONE).toString();
    }

    /**
     * Returns a list of String literals from a comma-separated value String.
     *
     * @param csvString
     */
    public static List<String> csv2list(String csvString) {
        List<String> outList = new ArrayList<String>();

        for (String element : csvString.split(COMMA)) {
            if (!element.isEmpty()) {
                outList.add(element);
            }
        }
        return outList;
    }

    /**
     * Repeats a String literal N times.
     *
     * @param value The single input literal to be repeated
     * @param times How many repetitions
     */
    public static List<String> repeat(String value, int times) {
        List<String> outList = new ArrayList<String>(times);

        for (int i = 0; i < times; i++) {
            outList.add(value);
        }

        return outList;
    }

    /**
    * Strip the " at first and last position of input string e.g: "abcdefef" ->  abcdefef
     *
     * @param input
     * @return
     */
    public static String stripFirstAndLastQuotes(String input) {
        String output = input.replaceAll("^\"|\"$", "");
        return output;
    }
    
    /**
     * Strip any " in a string
     */
    public static String stripQuotes(String input) {
        String output = input.replace("\"", "");
        return output;
    }
    
    /**
     * Strip any , in aa string
     */
    public static String stripCommas(String input) {
        String output = input.replace(",", "");
        return output;
    }

    /**
     * Clean all the empty, null elements in string array
     *
     * @param v
     * @return
     */
    public static String[] clean(final String[] v) {
        List<String> list = new ArrayList<String>(Arrays.asList(v));
        list.removeAll(Collections.singleton(null));
        list.removeAll(Collections.singleton(""));
        return list.toArray(new String[list.size()]);
    }

    /**
     * Build a query string from all KVP keys and values
     *
     * @param kvpParameters
     * @return
     */
    public static String buildQueryString(Map<String, String[]> kvpParameters) {
        List<String> keyValues = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : kvpParameters.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            for (String value : values) {
                keyValues.add(key + "=" + value);
            }
        }

        return ListUtil.join(keyValues, "&");
    }
    
    /**
     * As replace() does not work with "$", all the "$" of string, e.g: $c will need to be stripped
     * @param input
     * @return 
     */
    public static String stripDollarSign(String input) {
        return input.replaceAll("\\$", "");
    }
    
    /**
    * Replace any non digit and characters to "_"
    */
    public static String replaceSpecialCharacters(String input) {
        String result = input.replaceAll("[^\\dA-Za-z ]", "_");
        return result;
    }
    
    /**
     * Replace '(' and ')' from input string
     */
    public static String stripParentheses(String input) {
        return input.replace("(", "").replace(")", "");
    }
    
    /**
     * Replace '[' and ']' from input string
     */
    public static String stripBrackets(String input) {
        return input.replace("[", "").replace("]", "");
    }
    
    /**
     * Given a file path, return a temp coverage id with random suffix from datetime
     */
    public static String createTempCoverageId(String filePath) {
        String coverageId = TEMP_COVERAGE_PREFIX + "_" + StringUtil.addDateTimeSuffix(FilenameUtils.getBaseName(filePath));
        return coverageId;
    }
    
    /**
     * Given an input datetime, if it doesn't contain quotes, then add them
     * e.g. 2015-01-01 -> "2015-01-01"
     */
    public static String enquoteIfNotEnquotedAlready(String inputDateTime) {
        String result = inputDateTime;
        
        if (!inputDateTime.contains("\"")) {
            result = "\"" + inputDateTime + "\"";
        }
        
        return result;
    }
    
    /**
     * e.g. 2015-01-02 -> '2015-01-02'
     */
    public static String enquoteSingleIfNotEnquotedAlready(String inputDateTime) {
        String result = inputDateTime;
        
        if (!inputDateTime.contains("'")) {
            result = "'" + inputDateTime + "'";
        }
        
        return result;
    }
    
    public static String readFileToString(String filePath) throws IOException {
        File file = new File(filePath);
        return FileUtils.readFileToString(file);
    }
    
    /**
     * Given WKT ( e.g. POLYGON((...)) or MULTIPOLYGON(((...))) ),
     * return the valid WKT result with the coordinates of the first and the last vertices are the same.
     * 
     * e.g. POLYGON((1 1, 2 2, 3 3)) -> POLYGON((1 1, 2 2, 3 3, 1 1))
     */
    public static String closeRingsWKT(String wkt) throws PetascopeException {
        Geometry geom = ogr.CreateGeometryFromWkt(wkt);
        if (geom != null) {
            geom.CloseRings();
        
            String result = geom.ExportToWkt();
            return result;
        }
        
        throw new PetascopeException(ExceptionCode.InvalidRequest, "WKT is not valid. Given: " + wkt);
    }
    
    /**
     * a = null -> "null" 
     * a = "aa" -> "aa"
     */
    public static String getNullLiteralOrValue(String input) {
        if (input == null) {
            return "null";
        }
        
        return input;
    } 
    
    /**
     * e.g. given https://mundi.rasdaman.com/rasdaman/ows or https://mundi.rasdaman.com/rasdaman/ows/
     * return https://mundi.rasdaman.com/rasdaman
     */
    public static String getPetascopeContextURLWithoutOWS(String petascopeEndPoint) {
        String endpoint = petascopeEndPoint;

        if (petascopeEndPoint.endsWith("/" + OWS) || petascopeEndPoint.endsWith("/" + OWS + "/")) {
            endpoint = "";
            String[] tmps = petascopeEndPoint.split("/");
            
            for (int i = 0; i < tmps.length - 1; i++) {
                endpoint += tmps[i];
                if (i < tmps.length - 2) {
                    endpoint += "/";
                }
            }
        }
        
        return endpoint;
    }
    
    /**
     * Check if 2 strings are equivalent, if both are null or empty or one is null and one is empty, then they are equivalent
     */
    public static boolean equalsIgnoreNull(String input1, String input2) {
        if (input1 == null) {
            input1 = "";
        }
        if (input2 == null) {
            input2 = "";
        }
        
        return input1.equals(input2);
    }
    
    public static String decodeUTF8(String input) throws PetascopeException {
        try {
            return URLDecoder.decode(input, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError, "Cannot decode string: " + input + " to UTF-8. Reason: " + ex.getMessage());
        }
    }

    /**
     * Extract substring inside square brackets, e.g. "{[1,2,], [4,5], [6,7]}"
     * returns a list of "1,2", "4,5", "6,7"
     */    
    public static List<String> extractStringsBetweenSquareBrackets(String str) {
        List<String> results = new ArrayList<>();
        // extract values inside [ ]        
        Matcher m = squareBracketsPattern.matcher(str);

        while (m.find()) {
            String v = m.group(1);
            results.add(v);
        }
        
        return results;
    }
    
    /**
     * e.g. 123.567000000000000000000 -> 123.567
     *      123.000 -> 123
     */
    public static String stripZerosAfterDecimal(String str) {
        return str.replaceAll("^([-?\\d,]+)$|^([-?\\d,]+)\\.0*$|^([-?\\d,]+\\.[0-9]*?)0*$", "$1$2$3");
    }
    
    /**
     * Return an iterator label, starting with char: i
     * size = 1 -> char j
     * size = 2 -> char k
     */
    public static String getIteratorLabel(int incrementSize) {
        return Character.toString((char) ('i' + incrementSize));
    }
    
    public static Boolean stringToBoolean(String value) {
        if (value == null) {
            return null;
        } else {
            return Boolean.valueOf(value);
        }
    }
    
}
