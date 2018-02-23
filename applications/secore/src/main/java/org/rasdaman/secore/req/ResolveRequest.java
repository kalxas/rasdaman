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
package org.rasdaman.secore.req;

import org.rasdaman.secore.util.SecoreException;
import org.rasdaman.secore.util.ExceptionCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.rasdaman.secore.Constants.*;
import static org.rasdaman.secore.handler.GeneralHandler.*;
import org.rasdaman.secore.ConfigManager;
import org.rasdaman.secore.Constants;
import org.rasdaman.secore.util.StringUtil;

/**
 * Abstracts away requests (identifiers) that secore can resolve. This implements {@link ParamValue} as it can appear as a parameter value in a compound CRS URI.
 *
 * @author Dimitar Misev
 */
public class ResolveRequest implements ParamValue {

    private static Logger log = LoggerFactory.getLogger(ResolveRequest.class);

    /**
     * Request parameters
     */
    private final List<RequestParam> params;

    /**
     * The original request, e.g. in http://opengis.net/def/crs/EPSG/0/4326 the original request is obviously http://opengis.net/def/crs/EPSG/0/4326
     */
    private final String originalRequest;
    /**
     * SECORE endpoint, e.g. in http://opengis.net/def/crs/EPSG/0/4326 the service URI is http://opengis.net/def/ Note that the service URI always ends with a '/' and is followed by the operation.
     */
    private final String serviceUri;

    /**
     * Request operation, e.g. in http://opengis.net/def/crs/EPSG/0/4326 the operation is "crs"
     */
    private final String operation;

    /**
     * Indicate level of xlink expansion.
     */
    private RequestParam expand = null;

    /**
     * Indicate if request is local.
     */
    private boolean local;

    /**
     * Create a new request to resolve out of a URI. If uri is a URN, it will be converted to URI using the default service URL specified in secore.conf The URL parameters are decoded only after they have been separated.
     *
     * @param uri the original request to be parsed
     * @throws SecoreException
     */
    public ResolveRequest(String uri) throws SecoreException {
        log.trace("Parsing URI: " + uri);

        params = new ArrayList<>();

        // normalize uri to a url
        if (StringUtil.isUrn(uri)) {
            uri = StringUtil.convertUrnToUrl(uri);
        }

        // now it is http://opengis.net/def/crs/EPSG/0/4326
        this.originalRequest = uri;

        // http://opengis.net/def/
        this.serviceUri = StringUtil.getServiceUri(uri);
        setLocal(serviceUri);

        // crs/EPSG/0/4326
        uri = StringUtil.stripDef(uri);

        // determine request operation; at this point uri can one of these forms:
        // 1. crs/EPSG/0/4326
        // 2. crs?authority=EPSG&..
        // 3. crs
        // 4. '' (empty string)
        int ind1 = uri.indexOf(REST_SEPARATOR);
        int ind2 = uri.indexOf(QUERY_SEPARATOR);
        int ind = -1;
        if (ind1 != -1 && (ind2 == -1 || ind1 < ind2)) {
            ind = ind1;
        } else if (ind2 != -1 && (ind1 == -1 || ind2 < ind1)) {
            ind = ind2;
        } else if (ind1 == -1 && ind2 == -1 && uri.length() > 0) {
            ind = uri.length();
        } else {
            ind = 0;
        }
        this.operation = uri.substring(0, ind);
        if (ind > 0 && ind != uri.length()) {
            uri = uri.substring(ind + 1);
        } else {
            uri = EMPTY;
        }

        // remove first separator
        if (uri.startsWith(QUERY_SEPARATOR) || uri.startsWith(PAIR_SEPARATOR)) {
            uri = uri.substring(1);
        }

        //
        // parse params
        //
        if (uri.matches(StringUtil.START_DIGIT_REGEXP)) {

            /**
             * crs-compound/crs-equal, URI is now of the form
             *
             * 1=http://opengis.net/def/crs/EPSG/0/4326&2=http://opengis.net/...
             */
            while (uri.matches(StringUtil.START_DIGIT_REGEXP)) {
                // first find final position
                int pos = uri.indexOf(PAIR_SEPARATOR);
                boolean added = false;
                while (pos != -1) {
                    String curr = uri.substring(pos + 1);
                    if (curr.matches(StringUtil.START_DIGIT_REGEXP)) {
                        String kvp = uri.substring(0, pos);
                        params.add(new RequestParam(StringUtil.urldecode(kvp)));
                        uri = uri.substring(pos + 1);
                        added = true;
                        pos = -1;
                    } else {
                        pos = uri.indexOf(PAIR_SEPARATOR, pos + 1);
                    }
                }
                if (!added) {
                    params.add(new RequestParam(StringUtil.urldecode(uri)));
                    uri = EMPTY;
                }
            }

            Collections.sort(params);

        } else {

            // all other cases
            uri = StringUtil.urldecode(uri);
            String[] pairs = uri.split("[/\\?&]");
            for (String pair : pairs) {
                if (!pair.equals(EMPTY)) {
                    String key = null;
                    String val = pair;
                    if (pair.contains(KEY_VALUE_SEPARATOR)) {
                        String[] tmp = pair.split(KEY_VALUE_SEPARATOR);
                        key = tmp[0];
                        if (tmp.length > 1) {
                            val = tmp[1];
                        }
                    }
                    addParam(key, val);
                }
            }
        }
    }

    /**
     * Create a new request to resolve.
     *
     * @param operation the operation to perform, e.g. crs-compound, crs, axis, ...
     * @param service the service's URL, e.g. http://www.opengis.net/def
     * @param fullUri
     * @throws SecoreException
     */
    public ResolveRequest(String operation, String service, String fullUri) throws SecoreException {
        params = new ArrayList<>();
        this.operation = operation;
        this.serviceUri = StringUtil.wrapUri(service);
        this.originalRequest = fullUri;
        setLocal(serviceUri);
    }

    /**
     * Determine if request is local based on the service URI.
     */
    private void setLocal(String serviceUri) {
        this.local = serviceUri == null || serviceUri.equals(EMPTY)
                || serviceUri.startsWith(LOCAL_URI) || serviceUri.equals(REST_SEPARATOR);
    }

    /**
     * Add a new parameter to the request.
     *
     * @param key parameter key, can be null
     * @param value the value, must not be null
     * @throws SecoreException
     */
    public void addParam(String key, String value) throws SecoreException {
        if (value == null) {
            throw new SecoreException(ExceptionCode.InvalidParameterValue.locator(key),
                    "Null value encountered");
        }
        if (key != null && key.equalsIgnoreCase(EXPAND_KEY)) {
            int expand;
            if (value.equalsIgnoreCase(EXPAND_NONE)) {
                expand = 0;
            } else if (value.equalsIgnoreCase(EXPAND_FULL)) {
                expand = Integer.MAX_VALUE;
            } else {
                try {
                    expand = Integer.parseInt(value);
                    if (expand < 0) {
                        throw new SecoreException(ExceptionCode.InvalidParameterValue.locator(EXPAND_KEY),
                                "Expand value must be a number >= 0: " + value);
                    }
                } catch (NumberFormatException | SecoreException ex) {
                    throw new SecoreException(ExceptionCode.InvalidRequest,
                            "Invalid expand level specified, expected a number >= 0, full or none: " + value);
                }
            }
            this.expand = new RequestParam(key, expand + "");
        } else {
            params.add(new RequestParam(key, value));
        }
    }

    /**
     * @return the parameters this request holds
     */
    public List<RequestParam> getParams() {
        return params;
    }

    /**
     * Get the param value from param key e.g: crs?authority=EPSG&code=4327&version=0 key is: authority and value is: EPSG
     *
     * @param key
     * @return
     */
    public String getParamValueByKey(String key) {
        // NOTE: in case of rest request 
        // e.g: def/crs/EPSG/8.5/4327, param key is NULL, param value is (e.g: epsg)

        String value = "";
        for (RequestParam param : this.params) {
            if (param.key.equals(key)) {
                value = param.val.toString();
            }
        }
        return value;
    }

    /**
     * @return the parameters as a string
     */
    public String paramsToString() {
        StringBuilder ret = new StringBuilder(EMPTY);
        boolean first = true;
        for (RequestParam param : params) {
            if (first) {
                ret.append(param.getFragmentSeparator());
                if (param.isKvp()) {
                    first = false;
                }
            } else {
                ret.append(param.getParamSeparator());
            }
            ret.append(param.toString());
        }
        if (expand != null) {
            ret.append(expand.getParamSeparator()).append(expand.toString());
        }
        return ret.toString();
    }

    /**
     * @return the operation of this request
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @return the service URL
     */
    public String getServiceUri() {
        return serviceUri;
    }

    /**
     * @return true if service URI is empty or equal to '/'
     */
    public boolean isLocalUri() {
        return serviceUri == null || serviceUri.equals(EMPTY) || serviceUri.equals(REST_SEPARATOR);
    }

    /**
     * @return the full URL for this request
     */
    public String getOriginalRequest() {
        return originalRequest;
    }

    /**
     * Using the prefix URL from service.url in secore.properties e.g: request: http://localhost:8080/def -> http://opengis.net/def or compoundCRS: http://localhost:8080/def/crs-compound? 1=http://localhost:8080/def/crs/EPSG/0/32633 &2=http://localhost:8080/def/crs/OGC/0/AnsiDate
     *
     * @return
     */
    public String getReplacedURLPrefixRequest() throws SecoreException {
        // split by "def", so the first path is the original prefix of request (http://localhost:8080/)
        String[] tmp = originalRequest.split(Constants.WEB_APPLICATION_NAME);
        String originalPrefix = tmp[0];
        // replace all the originalPrefix occurences by the configuration prefix in secore.properties
        String replacedURL = this.originalRequest.replace(originalPrefix + Constants.WEB_APPLICATION_NAME, ConfigManager.getInstance().getServiceUrl());
        return replacedURL;
    }

    /**
     * @return the depth to which links in the returned XML document should be resolved
     */
    public String getExpandDepth() {
        if (expand == null) {
            return EXPAND_DEFAULT;
        } else {
            return expand.val.toString();
        }
    }

    /**
     * @return true if this request is local, directed to the local database, or false if it isn't.
     */
    public boolean isLocal() {
        return local;
    }

    /**
     * Check if request is rest (/def/crs/EPSG/0/4326) or KVP (/def/crs?authority=EPSG&version=0&code=4326)
     *
     * @return
     */
    public boolean isRest() {
        for (RequestParam param : this.params) {
            if (param.key == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder(isLocal() ? "" : serviceUri);
        ret.append(operation).append(paramsToString());
        return ret.toString();
    }

    public String toDebugString() {
        return "ResolveRequest {\n"
                + "\tparams=" + params
                + "\n\toperation=" + operation
                + "\n\tserviceUri=" + serviceUri
                + "\n\toriginalUri=" + originalRequest
                + "\n\texpand=" + expand
                + "\n}";
    }
}
