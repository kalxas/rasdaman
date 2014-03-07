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
package petascope.wcs2.handlers;

import static petascope.wcs2.extensions.FormatExtension.MIME_XML;

/**
 * Bean holding the response from executing a request operation.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class Response {

    private final byte[] data;
    private final String xml;
    private final String mimeType;
    private final int exit_code;
    private static final int DEFAULT_CODE = 200;

    // constructrs
    public Response(byte[] data) {
        this(data, null, null, DEFAULT_CODE);
    }

    public Response(byte[] data, int code) {
        this(data, null, null, code);
    }

    public Response(String xml) {
        this(null, xml, null); //FormatExtension.MIME_GML);
    }

    public Response(String xml, int code) {
        this(null, xml, MIME_XML, code);
    }

    public Response(byte[] data, String xml, String mimeType) {
        this(data, xml, mimeType, DEFAULT_CODE);
    }

    public Response(byte[] data, String xml, String mimeType, int code) {
        this.data = data;
        this.xml = xml;
        this.mimeType = mimeType;
        this.exit_code = code;
    }

    // interface
    public byte[] getData() {
        return data;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getXml() {
        return xml;
    }

    public int getExitCode() {
        return exit_code;
    }

}
