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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

package petascope.wms.exception;

import petascope.exceptions.WMSException;
import org.jetbrains.annotations.NotNull;

/**
 * Exception class for invalid XY bounding box.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class WMSInvalidBoundingBoxExcpetion extends WMSException {
    /**
     * Constructor for the class.
     */
    public WMSInvalidBoundingBoxExcpetion(String bbox) {
        super(ERROR_MESSAGE.replace("$bbox", bbox));
    }
    
    public WMSInvalidBoundingBoxExcpetion(String bbox, String reason) {
        super(ERROR_MESSAGE_WITH_REASON.replace("$bbox", bbox).replace("$reason", reason));
    }

    @NotNull
    @Override
    public String getExceptionCode() {
        return EXCEPTION_CODE;
    }

    private static final String EXCEPTION_CODE = "InvalidBoundingBox";
    private static final String ERROR = "Invalid value for 'bbox' parameter, pattern: 'xMin,yMin,xMax,yMax'";
    private static final String ERROR_MESSAGE = ERROR + ". Given '$bbox'.";
    private static final String ERROR_MESSAGE_WITH_REASON = ERROR + ". Reason: $reason.";
}

