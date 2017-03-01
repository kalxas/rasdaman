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

package petascope.wms2.service.getcapabilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.service.base.Request;

/**
 * Representation of a get capabilities request in WMS 1.3
 * Check the standard, section 7.2.3 for a better overview of the parameters of this request
 * +--------------------------------------------------------------------------------------------+
 * | Request Parameter       | Required/  | Description                                         |
 * |                         |  Optional  |                                                     |
 * |-------------------------+------------+-----------------------------------------------------|
 * | VERSION=version         |     O      | Request version                                     |
 * |-------------------------+------------+-----------------------------------------------------|
 * | SERVICE=WMS             |     R      | Service type                                        |
 * |-------------------------+------------+-----------------------------------------------------|
 * | REQUEST=GetCapabilities |     R      | Request name                                        |
 * |-------------------------+------------+-----------------------------------------------------|
 * | UPDATESEQUENCE=string   |     O      | Sequence number or string for cache control         |
 * +--------------------------------------------------------------------------------------------+
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class GetCapabilitiesRequest extends Request {

    /**
     * Constructor for the class
     *
     * @param request        the base service request the user made
     * @param updateSequence the update sequence the user requested
     * @param format         the format in which to return the document
     * @param baseUrl        the base url of the request
     */
    public GetCapabilitiesRequest(@NotNull Request request,
                                  @Nullable String updateSequence,
                                  @Nullable String format,
                                  @NotNull String baseUrl) {
        super(request);
        this.updateSequence = updateSequence;
        this.format = format;
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the update sequence number requested
     *
     * @return the update sequence number
     */
    @Nullable
    public String getUpdateSequence() {
        return updateSequence;
    }


    /**
     * Returns the value of the request parameter for the get capabilities request
     *
     * @return the value for the request parameter
     */
    @NotNull
    public static String getRequestType() {
        return REQUEST;
    }

    /**
     * Returns the updatesequence parameter name according to the standard
     *
     * @return the updatesequence parameter name
     */
    @NotNull
    public static String getRequestParameterUpdateSequence() {
        return REQUEST_PARAMETER_UPDATE_SEQUENCE;
    }

    /**
     * Returns the format in which the client expects the response
     *
     * @return the format
     */
    @Nullable
    public String getFormat() {
        return format;
    }

    /**
     * Returns the base url of the request
     *
     * @return the base url
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Gets the request parameter name for format
     *
     * @return the parameter name
     */
    @NotNull
    public static String getRequestParameterFormat() {
        return REQUEST_PARAMETER_FORMAT;
    }

    @Nullable
    private final String updateSequence;
    private final String format;
    private final String baseUrl;
    private final static String REQUEST = "GetCapabilities";
    private final static String REQUEST_PARAMETER_UPDATE_SEQUENCE = "updatesequence";
    private final static String REQUEST_PARAMETER_FORMAT = "format";

}
