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

package petascope.wms2.service.insertlayer;

import com.sun.istack.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.service.base.Request;

/**
 * Representation of a InsertWCSLayer request.
 *
 * +--------------------------------------------------------------------------------------------+
 * | Request Parameter       | Required/  | Description                                         |
 * |                         |  Optional  |                                                     |
 * |-------------------------+------------+-----------------------------------------------------|
 * | VERSION=version         |     O      | Request version                                     |
 * |-------------------------+------------+-----------------------------------------------------|
 * | SERVICE=WMS             |     R      | Service type                                        |
 * |-------------------------+------------+-----------------------------------------------------|
 * | REQUEST=InsertWCSLayer  |     R      | Request name                                        |
 * |-------------------------+------------+-----------------------------------------------------|
 * | WCSCOVERAGEID           |     R      | The id of the WCS coverage to be made available as  |
 * |                         |            | WMS layer                                           |
 * +--------------------------------------------------------------------------------------------+
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class InsertWCSLayerRequest  extends Request {
    /**
     * Constructor for the class
     */
    public InsertWCSLayerRequest(@NotNull Request baseRequest, @Nullable String wcsCoverageId) {
        super(baseRequest);
        this.wcsCoverageId = wcsCoverageId;
    }

    /**
     * Returns the value of the wcsCoverageId parameter.
     *
     * @return the coverage id of the targeted wcs coverage.
     */
    @NotNull
    public String getWcsCoverageId() {
        return wcsCoverageId;
    }

    /**
     * Returns the value of the request parameter for this type of request.
     * @return REQUEST_TYPE
     */
    public static String getRequestType() {
        return REQUEST_TYPE;
    }

    /**
     * Returns the key of the wcsCoverageId parameter.
     * @return REQUEST_PARAMETER_WCS_COVERAGE_ID
     */
    public static String getRequestParameterWcsCoverageId() {
        return REQUEST_PARAMETER_WCS_COVERAGE_ID;
    }

    private String wcsCoverageId;
    private static final String REQUEST_TYPE = "InsertWCSLayer";

    private static final String REQUEST_PARAMETER_WCS_COVERAGE_ID = "wcsCoverageId";
}
