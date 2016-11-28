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
package petascope;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;
import petascope.util.StringUtil;

public class CustomRequestWrapper extends HttpServletRequestWrapper {

    private HttpServletRequest wrapped;

    private Map<String, String[]> parameterMap;
    private String queryString;

    public CustomRequestWrapper(HttpServletRequest wrapped) throws UnsupportedEncodingException {
        super(wrapped);
        this.wrapped = wrapped;
    }

    /**
     * Wrap the HttpServletRequest in KVP and parse the queryString and parameter map correctly (e.g: convert "+" to "%2B")
     * @throws UnsupportedEncodingException
     */
    public void buildGetKvpParametersMap() throws UnsupportedEncodingException, IOException {
        this.queryString = wrapped.getQueryString();
        if (queryString == null) {
            // NOTE: It can be a GET KVP request but parameters are inside request body, so have to build parameter maps from query string
            // e.g: curl -s -X GET 'http://localhost:8080/rasdaman/ows' --data-urlencode 'SERVICE=WCS&VERSION=2.0.1&REQUEST=ProcessCoverages&query=...'
            this.buildPostKvpParametersMap();
        } else {
            // Try to parse query string to parameter maps with GET
            parseQueryString();
        }
    }

    /**
     * When sending a POST request, the parameter map only has 1 entry (service -> ....) and queryString is null, then need to parse it correctly.
     * e.g: curl -X POST 'http://localhost:8080/rasdaman/ows/wcs'
     * --data-urlencode 'SERVICE=WCS&VERSION=2.0.1&REQUEST=ProcessCoverages&query=...'
     *      */
    public void buildPostKvpParametersMap() throws IOException {
        // Read query string from the post request body
        this.queryString = this.decodeURL(IOUtils.toString(this.wrapped.getReader()));
        // NOTE: if send a XML/SOAP request not KVP request then don't parse anything.
        if (!this.queryString.trim().startsWith("<")) {
            // then parse query string to parameter maps
            this.parseQueryString();
        }
    }

    /**
     * Add new parameter and value to query string
     * @param name
     * @param value
     */
    public void addParameter(String name, String value) {
        if (parameterMap == null) {
            parameterMap = new HashMap<String, String[]>();
            parameterMap.putAll(wrapped.getParameterMap());
        }
        String[] values = parameterMap.get(name);
        if (values == null) {
            values = new String[0];
        }
        List<String> list = new ArrayList<String>(values.length + 1);
        list.addAll(Arrays.asList(values));
        list.add(value);
        parameterMap.put(name, list.toArray(new String[0]));
    }

    @Override
    public String getParameter(String name) {
        if (parameterMap == null) {
            return wrapped.getParameter(name);
        }

        String[] strings = parameterMap.get(name);
        if (strings != null) {
            return strings[0];
        }
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (parameterMap == null) {
            return wrapped.getParameterMap();
        }

        return Collections.unmodifiableMap(parameterMap);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (parameterMap == null) {
            return wrapped.getParameterNames();
        }

        return Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        if (parameterMap == null) {
            return wrapped.getParameterValues(name);
        }
        return parameterMap.get(name);
    }

    @Override
    public String getQueryString() {
        // We will need to override the super's method as the super() is encoded() queryString and it cannot parse in handlers.
        return this.queryString;
    }

    /**
     * Parse query string to parameters map
     * NOTE: replace "+" with "%2B"
     */
    private void parseQueryString() throws UnsupportedEncodingException {
        // decode URL
        this.queryString = this.decodeURL(this.queryString);
        String[] params = this.queryString.split("&");
        params = StringUtil.clean(params);

        // Parse all the parameters and values to a hash map
        Map<String, List<String>> paramsMapTmp = new LinkedHashMap<String, List<String>>();
        parameterMap = new HashMap<String, String[]>();
        for (String param : params) {
            String[] paramTmp = param.split("=");
            // NOTE: some param like &Style= does not have value then it is empty
            String name = param.split("=")[0];
            String value = "";
            if (paramTmp.length > 1) {
                value = param.split("=")[1];
            }

            Object listValues = paramsMapTmp.get(name);
            if (listValues != null) {
                // add another values for the existence parameter (e.g:query="1"&query="2"...)
                if (listValues instanceof List) {
                    ((List<String>)listValues).add(value);
                }
            } else {
                // new key-value in paramsMap (e.g:query="1")
                List<String> tmp = new LinkedList<String>();
                tmp.add(value);
                paramsMapTmp.put(name, tmp);
            }
        }

        // Add the parsed parameters to parameters map of HTTPRequest object
        for (Map.Entry<String, List<String>> entry : paramsMapTmp.entrySet()) {
            String paramName = entry.getKey();
            int size = entry.getValue().size();
            String[] paramValues = entry.getValue().toArray(new String[size]);
            parameterMap.put(paramName, paramValues);
        }
    }

    /**
     * Decode from KVP request
     * @param queryString
     * @return
     * @throws java.io.UnsupportedEncodingException
     */
    private String decodeURL(String queryString) throws UnsupportedEncodingException {
        // if query is not encoded then must convert the "+" to encoded character
        queryString = queryString.replace("+", "%2B");
        // then decode the posted query string
        queryString = URLDecoder.decode(queryString, "UTF8");
        queryString = queryString.replace("\n", "").replace("\r", "");
        return queryString;
    }
}