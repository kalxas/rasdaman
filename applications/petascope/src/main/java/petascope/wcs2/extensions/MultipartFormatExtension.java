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
package petascope.wcs2.extensions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.parsers.GetCoverageRequest;

/**
 * Common class for multipart image extensions.
 *
 * @author <a href="mailto:m.rusu@jacobs-university.de">Mihaela Rusu</a>
 */
public class MultipartFormatExtension extends  GmlFormatExtension {

    public static final String[] SET_VALUES = new String[] { MIME_TIFF, MIME_JP2 };
    public static final Set<String> SUPPORTED_FORMATS = new HashSet<String>(Arrays.asList(SET_VALUES));

    @Override
    public boolean canHandle(GetCoverageRequest req) {
        return req.isMultipart() && SUPPORTED_FORMATS.contains(req.getFormat());
    }

    @Override
    public Response handle(GetCoverageRequest req, DbMetadataSource meta)
            throws PetascopeException, WCSException, SecoreException {
        // get gml response, but without the {coverageData} replaced
        Response gml = super.handle(req, meta);
        // get the GeoTIFF file
        Response image = ExtensionsRegistry.getFormatExtension(false, req.getFormat()).handle(req, meta);
        // return multipart response
        String xml = gml.getXml().replace("{coverageData}",
                   "<File>"
                + "<fileName>" + "file" + "</fileName>"
                + "<fileStructure>Record Interleaved</fileStructure>"
                + "<mimeType>" + req.getFormat() + "</mimeType>"
                + "</File>");
        return new Response(image.getData(), xml, req.getFormat());
    }

    @Override
    protected String addCoverageData(String gml, GetCoverageRequest request, DbMetadataSource meta, CoverageMetadata covmeta)
            throws WCSException {
        return gml;
    }

}
