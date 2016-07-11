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
import petascope.wms2.service.exception.error.WMSException;

import java.nio.charset.Charset;

/**
 * Generic interface for responses of a WMS service. All classes that wish to be used as a response
 * to a request in WMS should implement this interface.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public abstract class Response {

    /**
     * Returns a byte representation of the response. This will be used as a common interface for
     * services that wish to display the response, in the case of WMS the servlet.
     * <p/>
     * If this response is intended to be cached, this method should not do any work in subsequent calls,
     * but just return the intended result.
     *
     * @return a byte array representing the response
     * @throws petascope.wms2.service.exception.error.WMSException
     */
    public abstract byte[] toBytes() throws WMSException;

    /**
     * Returns the mime type of the response. Subclasses are encouraged to override this method
     * and provide a better mime type for the response
     *
     * @return the mime type of the response
     */
    public String getMimeType() {
        return DEFAULT_MIME_TYPE;
    }

    /**
     * The default encoding to be used across the application
     */
    @NotNull
    protected static Charset getDefaultEncoding() {
        return DEFAULT_ENCODING;
    }

    @NotNull
    private static final Charset DEFAULT_ENCODING = Charset.defaultCharset();
    @NotNull
    private static final String DEFAULT_MIME_TYPE = "text/plain";

}
