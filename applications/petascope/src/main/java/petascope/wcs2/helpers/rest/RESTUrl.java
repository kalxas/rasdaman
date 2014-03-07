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
package petascope.wcs2.helpers.rest;

import java.util.ArrayList;
import petascope.util.Pair;

/**
 * ServletRequest is a class containing information collected from the
 * HTTPServletRequest that needs to be passed to the request parsers
 *
 * @author <a href="alex@flanche.net">Alex Dumitru</a>
 */
public class RESTUrl {

    public RESTUrl(String urlPath) {
        this.urlPath = urlPath;
        this.urlComponents = new ArrayList<Pair<String, String>>();
        this.parseRequestComponents();
    }

    /**
     * Returns the first element from the url components that match the given
     * key.
     *
     * @param key a string to search for
     * @return the value or null if the key doesn't exist
     */
    public ArrayList<String> getByKey(String key) {
        ArrayList<String> result = new ArrayList<String>();
        for (Pair<String, String> keyVal : this.urlComponents) {
            if (keyVal.fst.equalsIgnoreCase(key)) {
                result.add(keyVal.snd);
            }
        }
        return result;
    }

    /**
     * Return the value of a url component by index
     *
     * @param index the index of the component to retrieve
     * @return
     */
    public Pair<String, String> getByIndex(int index) {
        Pair<String, String> result = null;
        if (this.urlComponents.size() > index) {
            result = this.urlComponents.get(index);
        }
        return result;
    }

    /**
     * Decides if a key exists in the url components
     *
     * @param key a string to search for
     * @param urlComponents the url components
     * @return true if exists false otherwise
     */
    public Boolean existsKey(String key) {
        Boolean result = false;
        for (Pair<String, String> keyVal : this.urlComponents) {
            if (keyVal.fst.equalsIgnoreCase(key)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Processes an url path string into an array of the processed pairs e.g.
     * example.org/wcs/subset(x[0:10) will return [{"wcs", null}, {"subset",
     * "x[0:10]"];
     *
     * @param requestString the url paths string
     */
    private void parseRequestComponents() {
        //Replace the urls with a placeholder to allow spliting on / char
        String sanitizedUrlPath = this.sanitizeSlashes(this.urlPath);
        //Split on / and create map of key value pairs
        //We have two kinds of possible values:
        //- key only: e.g. wcs in example.org/wcs/2.0/...
        //- key value pair e.g. subset in example.org/wcs/2.0/.../subset(x[0,10])
        //  where the key is subset and the value is x[0,10]        
        String[] urlParts = sanitizedUrlPath.split("/");
        for (int i = 0; i < urlParts.length; i++) {
            String key, value = null;
            int valueStartIndex = urlParts[i].indexOf("(");
            if (valueStartIndex != -1) {
                int valueEndIndex = urlParts[i].lastIndexOf(")");
                key = urlParts[i].substring(0, valueStartIndex);
                value = this.rollbackSlashes(urlParts[i].substring(valueStartIndex+1, valueEndIndex));
            } else {
                key = urlParts[i];
            }
            if (key != null && !key.isEmpty()) {
                this.urlComponents.add(new Pair<String, String>(key, value));
            }
        }
    }

    /**
     * Sanitizes all slashes that are between () as they affect the url parsing
     * process
     *
     * @param strToSanit a string that needs to be sanitized
     * @return the sanitized string
     */
    private String sanitizeSlashes(String strToSanit) {
        StringBuilder sanitizedStr = new StringBuilder();
        for (int i = 0; i < strToSanit.length(); i++) {
            if (strToSanit.charAt(i) == '(') {
                sanitizedStr.append('(');
                //try to find the first ending paranthesis
                int charsSkipped = 0;
                for (int j = i + 1; j < strToSanit.length(); j++) {
                    charsSkipped += 1;
                    if (strToSanit.charAt(j) == ')') {
                        sanitizedStr.append(')');
                        break;
                    } else if (strToSanit.charAt(j) == '/') {
                        sanitizedStr.append(RESTUrl.SLASH_PLACEHOLDER);
                    } else {
                        sanitizedStr.append(strToSanit.charAt(j));
                    }
                }
                i += charsSkipped;
            } else {
                sanitizedStr.append(strToSanit.charAt(i));
            }
        }
        return sanitizedStr.toString();
    }

    /**
     * Replaces the slash tokens with actual slashes
     *
     * @param sanitizedStr a string that was sanitized
     * @return the correct string
     */
    private String rollbackSlashes(String sanitizedStr) {
        return sanitizedStr.replace(RESTUrl.SLASH_PLACEHOLDER, "/");
    }
    private ArrayList<Pair<String, String>> urlComponents;
    private String urlPath;
    private static String SLASH_PLACEHOLDER = "_%_%RestSlash%_%_";
}
