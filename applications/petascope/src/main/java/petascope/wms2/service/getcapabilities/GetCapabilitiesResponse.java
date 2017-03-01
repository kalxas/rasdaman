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
import petascope.wms2.metadata.GetCapabilities;
import petascope.wms2.metadata.MetadataObjectXMLSerializer;
import petascope.wms2.service.base.Response;
import petascope.wms2.service.exception.error.WMSInvalidOperationRequestException;

import java.io.IOException;

/**
 * Class to represent a get capabilities response. it receives a GetCapabilities metadata element converts it
 * using its template and returns the result in the response specific format
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class GetCapabilitiesResponse extends Response {

    /**
     * Constructor for the class
     *
     * @param capabilities a capabilities metadata object
     */
    public GetCapabilitiesResponse(@NotNull final GetCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Returns the serialization of this response in a byte array
     *
     * @return the byte array
     * @throws WMSInvalidOperationRequestException
     */
    @Override
    public byte[] toBytes() throws WMSInvalidOperationRequestException {
        if (bytes == null) {
            MetadataObjectXMLSerializer serializer = new MetadataObjectXMLSerializer();
            try {
                String result = serializer.serialize(capabilities);
                bytes = result.getBytes(getDefaultEncoding());
            } catch (IOException e) {
                throw new WMSInvalidOperationRequestException(capabilities.getClass().getClass().getName() + ".tpl.xml");
            }
        }
        return bytes;
    }


    /**
     * Returns the mime type of the response
     *
     * @return the mime type of the response
     */
    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @NotNull
    private final GetCapabilities capabilities;
    @Nullable
    private byte[] bytes = null;
    @NotNull
    private final static String MIME_TYPE = "text/xml";

}
