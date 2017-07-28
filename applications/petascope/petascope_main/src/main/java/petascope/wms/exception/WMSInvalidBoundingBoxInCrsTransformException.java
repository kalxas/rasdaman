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

package petascope.wms.exception;

import petascope.exceptions.WMSException;
import org.jetbrains.annotations.NotNull;

/**
 * Exception class for invalid XY bounding box to transform from sourceCrs to targetCrs.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class WMSInvalidBoundingBoxInCrsTransformException extends WMSException {
    /**
     * Constructor for the class.
     *
     * @param bbox
     * @param sourceCRS
     * @param targetCRS
     * @param errorMessage
     */
    public WMSInvalidBoundingBoxInCrsTransformException(String bbox, String sourceCRS, String targetCRS, String errorMessage) {
        super(ERROR_MESSAGE.replace(BBOX_TOKEN, bbox)
                           .replace(SOURCE_CRS_TOKEN, sourceCRS)
                           .replace(TARGET_CRS_TOKEN, targetCRS)
                           .replace(ERROR_MESSAGE_TOKEN, errorMessage));
    }

    @NotNull
    @Override
    public String getExceptionCode() {
        return EXCEPTION_CODE;
    }

    private static final String BBOX_TOKEN = "$bbox$";
    private static final String SOURCE_CRS_TOKEN = "$sourceCRS$";
    private static final String TARGET_CRS_TOKEN = "$targetCRS$";
    private static final String ERROR_MESSAGE_TOKEN = "$errorMessage";
    private static final String EXCEPTION_CODE = "InvalidBoundingBoxToTransform";
    private static final String ERROR_MESSAGE = "The supplied XY bounding box '" 
                                               + BBOX_TOKEN + "' is invalid to transform from source CRS '" + SOURCE_CRS_TOKEN 
                                               + "', to target CRS '" + TARGET_CRS_TOKEN + "' with error '" + ERROR_MESSAGE_TOKEN + "'." ;
}

