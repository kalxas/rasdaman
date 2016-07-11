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

package petascope.wms2.service.exception.error;

import org.jetbrains.annotations.NotNull;

/**
 * Exception to be thrown when the bounding box requested is invalid
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class WMSInvalidBbox extends WMSException {
    /**
     * Constructor for the class
     *
     * @param bbox the invalid bounding box
     */
    public WMSInvalidBbox(@NotNull String bbox) {
        super(ERROR_MESSAGE.replace("$bbox", bbox));
    }
    
    /**
     * Constructor for the class with the detail error
     * @param bbox
     * @param detailError 
     */
    public WMSInvalidBbox(@NotNull String bbox, @NotNull String detailError) {
        super(ERROR_MESSAGE_DETAIL.replace("$bbox", bbox)
                                  .replace("$detailError", detailError));
    }

    @NotNull
    @Override
    public String getExceptionCode() {
        return EXCEPTION_CODE;
    }

    private final static String EXCEPTION_CODE = "InvalidBoundingBox";
    private final static String ERROR_MESSAGE = "The requested bounding box $bbox is invalid";
    private final static String ERROR_MESSAGE_DETAIL = "The requested bounding box $bbox is invalid due to: $detailError";
}
