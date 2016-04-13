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

package petascope.wms2.service.exception.response;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.service.exception.error.WMSException;

/**
 * Factory for constructing ExceptionResponses based on the client provided exception format
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class ExceptionResponseFactory {

    /**
     * Returns exception in the correct response format based on the given exception format
     *
     * @param exception the exception to be used in the response
     * @param format    the exception format. if null is provided the xml response will be used as default
     * @return the exception response
     */
    public static ExceptionResponse getExceptionResponse(@NotNull WMSException exception, @Nullable String format) {
        if (format != null) {
            if (format.equalsIgnoreCase(IMAGE_FORMAT)) {
                return new ImageExceptionResponse(exception);
            } else if (format.equalsIgnoreCase(BLANK_FORMAT)) {
                return new BlankExceptionResponse(exception);
            }
        }
        return new XMLExceptionResponse(exception);
    }

    private final static String IMAGE_FORMAT = "application/vnd.ogc.se_inimage";
    private final static String BLANK_FORMAT = "application/vnd.ogc.se_blank";

}
