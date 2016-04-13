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

package petascope.wms2.servlet;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the basic information needed by the WMS parsers to recognize the request type and parse
 * it accordingly. We do not expose the HttpServletRequest directly to the parsers to avoid hard-coupling
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class WMSGetRequest {

    /**
     * Constructor for the class
     *
     * @param request the raw http servlet request
     */
    public WMSGetRequest(HttpServletRequest request) {
        this.request = request;
        canonicalMap = getCanonicalParametersMap();
    }

    /**
     * Returns the GET value for a certain key (case insensitive comparison)
     *
     * @param key the key to return the value for
     * @return either the value or null if no value was found
     */
    @Nullable
    public String getGetValueByKey(@NotNull String key) {
        key = key.toLowerCase();
        return canonicalMap.get(key);
    }

    /**
     * Returns true if the GET key is present, false otherwise
     *
     * @param key the key to check for
     * @return true if the key exists, false otherwise
     */
    public boolean hasGetValue(@NotNull String key) {
        key = key.toLowerCase();
        return canonicalMap.containsKey(key);
    }

    /**
     * Sets the get value of a get param
     *
     * @param key   the key for the get param
     * @param value the value of the get param
     */
    public void setGetValue(@NotNull String key, @NotNull String value) {
        canonicalMap.put(key, value);
    }

    /**
     * Returns all the get values of the request. Avoid using this method, the preferred way of access is getting it
     * by key directly
     *
     * @return all the GET values as a map GETKey -> GETValue
     */
    @NotNull
    public Map<String, String> getAllGetValues() {
        return canonicalMap;
    }

    /**
     * Returns the base url of the request. This means everything until the ? character.
     * E.g. http://example.org/ows?key=val => http://example.org/ows
     *
     * @return the base url of the request
     */
    public String getBaseUrl() {
        return request.getRequestURL().toString();
    }

    /**
     * Returns the parameters map in a canonical form with all keys in lower case.
     *
     * @return the canonical parameter map
     */
    @NotNull
    private Map<String, String> getCanonicalParametersMap() {
        Map<String, String> ret = new HashMap<String, String>(request.getParameterMap().size());
        @SuppressWarnings("unchecked")
        Map<String, String[]> kvpMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : kvpMap.entrySet()) {
            String value = null;
            if (entry.getValue() != null) {
                value = StringUtils.join(entry.getValue(), ",").trim();
            }
            ret.put(entry.getKey().toLowerCase(), value);
        }
        return ret;
    }

    @Override
    public String toString() {
        return getCanonicalParametersMap().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WMSGetRequest that = (WMSGetRequest) o;
        return canonicalMap.equals(that.canonicalMap);

    }

    @Override
    public int hashCode() {
        return canonicalMap.hashCode();
    }

    /**
     * Returns the full url from the servlet request
     *
     * @param request the servlet request
     * @return the full url
     */
    public static String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    @NotNull
    private final Map<String, String> canonicalMap;
    private final HttpServletRequest request;

}
