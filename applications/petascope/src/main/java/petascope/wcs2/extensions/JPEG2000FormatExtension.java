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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.extensions;

import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.Pair;
import petascope.util.ras.RasQueryResult;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;

/**
 * Return coverage as a JPEG2000 file.
 * 
 * The only coverage types supported by this specification are GridCoverage and
 * RectifiedGridCoverage with exactly 2 dimensions.
 *
 * @author <a href="mailto:m.rusu@jacobs-university.de">Mihaela Rusu</a>
 */
public class JPEG2000FormatExtension extends AbstractFormatExtension{
    @Override
    public boolean canHandle(GetCoverageRequest req) {
        return !req.isMultipart() && getMimeType().equals(req.getFormat());
        //return true;
    }

    @Override
    public Response handle(GetCoverageRequest request, DbMetadataSource meta) throws WCSException {
        GetCoverageMetadata m = new GetCoverageMetadata(request, meta);

        // First, transform possible non-native CRS subsets
        CRSExtension crsExtension = (CRSExtension) ExtensionsRegistry.getExtension(ExtensionsRegistry.CRS_IDENTIFIER);
        crsExtension.handle(request, m, meta);
        
        //Handle the range subset feature
        RangeSubsettingExtension rsubExt = (RangeSubsettingExtension) ExtensionsRegistry.getExtension(ExtensionsRegistry.RANGE_SUBSETTING_IDENTIFIER);
        rsubExt.handle(request, m);        
        
        setBounds(request, m, meta);
        if (m.getGridDimension() != 2 || !(
                m.getCoverageType().equals(GetCoverageRequest.GRID_COVERAGE) ||
                m.getCoverageType().equals(GetCoverageRequest.RECTIFIED_GRID_COVERAGE))) {
            throw new WCSException(ExceptionCode.NoApplicableCode, "The JPEG2000 format extension "
                    + "only supports GridCoverage and RectifiedGridCoverage with exactly two dimensions");
        }
        
        Pair<Object, String> p = null;
        if (m.getCoverageType().equals(GetCoverageRequest.GRID_COVERAGE)) {
            // return plain TIFF
            p = executeRasqlQuery(request, m, meta, JP2_ENCODING, null);
        } else {
            // return GeoTIFF
            p = executeRasqlQuery(request, m, meta, JP2_ENCODING, null);
        }

        RasQueryResult res = new RasQueryResult(p.fst);
        if (res.getMdds().isEmpty()) {
            return new Response(null, null, getMimeType());
        } else {
            return new Response(res.getMdds().get(0), null, getMimeType());
        }
    }

    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.JPEG2000_IDENTIFIER;
    }

    public String getMimeType() {
        return MIME_JP2;
    }
}
