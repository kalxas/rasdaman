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

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.GdalParameters;
import petascope.util.Pair;
import petascope.util.WcsUtil;
import petascope.util.XMLSymbols;
import petascope.util.ras.RasQueryResult;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;

/**
 * Return coverage as a Netcdf file.
 *
 * The only coverage types supported by this specification are GridCoverage and
 * RectifiedGridCoverage with exactly 2 dimensions.
 *
 * @author <a href="mailto:m.rusu@jacobs-university.de">Mihaela Rusu</a>
 */
public class NetcdfFormatExtension extends AbstractFormatExtension {

    /* Member */
    GdalParameters gdalParams;
    private static final Logger log = LoggerFactory.getLogger(NetcdfFormatExtension.class);

    /* Interface */
    public GdalParameters getCrsProperties() {
        return gdalParams;
    }

    /* Methods */
    @Override
    public boolean canHandle(GetCoverageRequest req) {
        return !req.isMultiPart() && getMimeType().equals(req.getFormat());
        //return true;
    }

    @Override
    public Response handle(GetCoverageRequest request, DbMetadataSource meta)
            throws PetascopeException, WCSException, SecoreException {
        GetCoverageMetadata m = new GetCoverageMetadata(request, meta);

        // First, transform possible non-native CRS subsets
        CRSExtension crsExtension = (CRSExtension) ExtensionsRegistry.getExtension(ExtensionsRegistry.CRS_IDENTIFIER);
        crsExtension.handle(request, m);
        
        //Handle the range subset feature
        RangeSubsettingExtension rsubExt = (RangeSubsettingExtension) ExtensionsRegistry.getExtension(ExtensionsRegistry.RANGE_SUBSETTING_IDENTIFIER);
        rsubExt.handle(request, m);

        try {
            updateGetCoverageMetadata(request, m, meta);
        } catch (PetascopeException pEx) {
            throw pEx;
        }

       if (!(WcsUtil.isGrid(m.getCoverageType()))) {
       throw new WCSException(ExceptionCode.NoApplicableCode, "The Netcdf format extension "
                    + "only supports GridCoverage and RectifiedGridCoverage");
        }

        Pair<Object, String> p = null;
        if (m.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
            // return plain Netcdf
            gdalParams = new GdalParameters();
            p = executeRasqlQuery(request, m, meta, NETCDF_ENCODING, null);
        } else {
            // RectifiedGrid: geometry is associated with a CRS -> return Netcdf with geo-metadata
            // Need to use the GetCoverage metadata which has updated bounds [see super.setBounds()]
            String[] domLo = m.getGisDomLow().split(" ");
            String[] domHi = m.getGisDomHigh().split(" ");

            gdalParams = new GdalParameters(domLo[0], domHi[0], domLo[1], domHi[1], m.getCrs());
            p = executeRasqlQuery(request, m, meta, NETCDF_ENCODING, gdalParams.toString());
        }

        RasQueryResult res = new RasQueryResult(p.fst);
        if (res.getMdds().isEmpty()) {
            return new Response(null, null, getMimeType());
        } else {
            List<byte[]> data = new ArrayList<byte[]>();
            data.add(res.getMdds().get(0));
            return new Response(data, null, getMimeType());
        }
    }

    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.NETCDF_IDENTIFIER;
    }

    /**
     * @return False: this extension has is no parent extension with identifier.
     */
    public Boolean hasParent() {
        return false;
    }

    /**
     * @return The identifier of the parent extension.
     */
    public String getParentExtensionIdentifier() {
        return "";
    }

    public String getMimeType() {
        return MIME_NETCDF;
    }
}