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

package petascope.wms2.service.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.servlet.WMSGetRequest;

/**
 * Common interface for all WMS requests. All requests have to extend this class to be able to be
 * processed by the service
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class Request {


    /**
     * Constructor for the class
     *
     * @param service the service name that the user requested
     * @param version the version the user requested
     * @param request the request the user made
     */
    protected Request(@Nullable String service, @Nullable String version, @Nullable String request, @NotNull WMSGetRequest rawRequest) {
        this.service = service;
        this.version = version;
        this.request = request;
        this.rawRequest = rawRequest;
    }

    /**
     * Creates a request from another request
     *
     * @param request the request to be used as a base for this service
     */
    protected Request(@NotNull Request request) {
        this.service = request.getService();
        this.version = request.getVersion();
        this.request = request.getRequest();
        this.rawRequest = request.getRawRequest();
    }

    /**
     * Returns the service requested
     *
     * @return the service
     */
    @Nullable
    public String getService() {
        return service;
    }

    /**
     * Returns the version of the service requested by the client
     *
     * @return the version
     */
    @Nullable
    public String getVersion() {
        return version;
    }

    /**
     * Returns the request type that was requested by the client
     *
     * @return the update sequence number
     */
    @Nullable
    public String getRequest() {
        return request;
    }

    /**
     * Returns the raw request
     *
     * @return the raw request from which this was parsed
     */
    @NotNull
    public WMSGetRequest getRawRequest() {
        return rawRequest;
    }

    /**
     * Gets the request parameter name for version
     *
     * @return the parameter name
     */
    public static String getRequestParameterVersion() {
        return REQUEST_PARAMETER_VERSION;
    }

    /**
     * Gets the request parameter name for service
     *
     * @return the parameter name
     */
    public static String getRequestParameterService() {
        return REQUEST_PARAMETER_SERVICE;
    }

    /**
     * Gets the request parameter name for request
     *
     * @return the parameter name
     */
    public static String getRequestParameterRequest() {
        return REQUEST_PARAMETER_REQUEST;
    }

    /**
     * Gets the parameter name for the exception format
     *
     * @return the exception format
     */
    public static String getRequestParameterExceptionFormat() {
        return REQUEST_PARAMETER_EXCEPTION_FORMAT;
    }

    @Nullable
    private final String service;
    @Nullable
    private final String version;
    @Nullable
    private final String request;
    @NotNull
    private final WMSGetRequest rawRequest;

    private final static String REQUEST_PARAMETER_VERSION = "version";
    private final static String REQUEST_PARAMETER_SERVICE = "service";
    private final static String REQUEST_PARAMETER_REQUEST = "request";
    private final static String REQUEST_PARAMETER_EXCEPTION_FORMAT = "exceptions";

}
