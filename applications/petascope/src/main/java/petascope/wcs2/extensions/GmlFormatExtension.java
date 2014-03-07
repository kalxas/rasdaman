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

import java.util.List;
import java.util.ListIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import static petascope.core.DbMetadataSource.TABLE_MULTIPOINT;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.WcsUtil;
import petascope.util.XMLSymbols;
import petascope.util.ras.RasQueryResult;
import petascope.wcps.server.core.CellDomainElement;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionSlice;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionSubset;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionTrim;
import petascope.wcs2.templates.Templates;

/**
 * Return coverage as pure GML.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class GmlFormatExtension extends AbstractFormatExtension {

    private static final Logger log = LoggerFactory.getLogger(GmlFormatExtension.class);
    public static final String DATATYPE_URN_PREFIX = "urn:ogc:def:dataType:OGC:1.1:"; // FIXME: now URNs are deprecated

    @Override
    public boolean canHandle(GetCoverageRequest req) {
        return req.getFormat() == null || (!req.isMultipart() && getMimeType().equals(req.getFormat()));
        //return false;
    }

    @Override
    public Response handle(GetCoverageRequest request, DbMetadataSource meta)
            throws PetascopeException, WCSException, SecoreException {
        GetCoverageMetadata m = new GetCoverageMetadata(request, meta);

        //Handle the range subset feature
        RangeSubsettingExtension rsubExt = (RangeSubsettingExtension) ExtensionsRegistry.getExtension(ExtensionsRegistry.RANGE_SUBSETTING_IDENTIFIER);
        rsubExt.handle(request, m);

        if (WcsUtil.isMultiPoint(m.getCoverageType())) {
            Response r = handleMultiPoint(request, request.getCoverageId(), meta, m);
            String xml = r.getXml();
            return new Response(r.getData(), xml, r.getMimeType());

        } else if (WcsUtil.isGrid(m.getCoverageType())) {

            // Use the GridCoverage template, which works with any subtype of AbstractGridCoverage via the {domainSetaddition}
            try {
                // GetCoverage metadata was initialized with native coverage metadata, but subsets may have changed it:
                updateGetCoverageMetadata(request, m, meta);
            } catch (PetascopeException pEx) {
                throw pEx;
            }

            String gml = WcsUtil.getGML(m, Templates.COVERAGE, meta);
            gml = addCoverageData(gml, request, meta, m.getMetadata());

            // RGBV coverages
            if (m.getCoverageType().equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
                gml = WcsUtil.addCoefficients(gml, m, meta);
                // Grid and Coverage bounds need to be updated, now we know the coefficients
                updateGetCoverageMetadata(request, m, meta);
                gml = WcsUtil.getBounds(gml, m);
            }


            return new Response(null, gml, FormatExtension.MIME_XML);
            // TODO : use XOM serializer (current problem: license header is trimmed to one line and namespaces need to be added)
            //Builder xmlBuilder = new Builder();
            //try {
            //    Document gmlDoc = xmlBuilder.build(new StringReader(gml));
            //    return new Response(null, serialize(gmlDoc), FormatExtension.MIME_XML);
            //} catch (IOException ex) {
            //    throw new WCSException(ExceptionCode.IOConnectionError,
            //        "Error serializing constructed document", ex);
            //} catch (ParsingException ex) {
            //    throw new WCSException(ExceptionCode.InternalComponentError,
            //        "Error creating the GML response document.", ex);
            //}
        } else {
            throw new WCSException(ExceptionCode.UnsupportedCoverageConfiguration,
                    "The coverage type '" + m.getCoverageType() + "' is not supported.");
        }
    }

    /**
     * Inserts rangeSet values for grid-coverages.
     * @param gml
     * @param request
     * @param meta
     * @param m
     * @return
     * @throws WCSException
     * @throws PetascopeException
     */
    protected String addCoverageData(String gml, GetCoverageRequest request, DbMetadataSource meta, CoverageMetadata m)
            throws WCSException, PetascopeException {
        RasQueryResult res = new RasQueryResult(executeRasqlQuery(request, m, meta, CSV_ENCODING, null).fst);
        if (!res.getMdds().isEmpty()) {
            String data = new String(res.getMdds().get(0));
            data = WcsUtil.csv2tupleList(data);
            gml = gml.replace("{" + Templates.KEY_COVERAGEDATA + "}", data);
        }
        return gml;
    }

    /**
     * Handles a request for MultiPoint Coverages and returns a response XML
     * @param req
     * @param coverageName
     * @return
     * @throws WCSException
     */
    private Response handleMultiPoint(GetCoverageRequest req, String coverageName, DbMetadataSource meta, GetCoverageMetadata m)
            throws WCSException, SecoreException {
        CoverageMetadata cov = m.getMetadata();
        String ret = WcsUtil.getGML(m, Templates.COVERAGE, meta);
        String pointMembers = "";
        String rangeMembers = "";
        String low = "", high = "";
        StringBuilder sb = new StringBuilder();

        try {

            List<CellDomainElement> cellDomainList = cov.getCellDomainList();

            /* check for subsetting */
            List<DimensionSubset> subsets = req.getSubsets();
            if (!subsets.isEmpty()) {

                /* subsetting ON: get coverage metadata */
                ListIterator<DimensionSubset> listIterator = subsets.
                        listIterator();
                while (listIterator.hasNext()) {

                    DimensionSubset subsetElement = listIterator.next();
                    String dimension = subsetElement.getDimension();
                    int dimIndex = cov.getDomainIndexByName(dimension);
                    CellDomainElement cellDomain = cellDomainList.get(dimIndex);

                    if (subsetElement instanceof DimensionTrim) {
                        DimensionTrim trim = (DimensionTrim) subsetElement;
                        cellDomain.setHi(trim.getTrimHigh());
                        cellDomain.setLo(trim.getTrimLow());
                        cellDomain.setSubsetElement(subsetElement);
                    }

                    if (subsetElement instanceof DimensionSlice) {
                        DimensionSlice slice = (DimensionSlice) subsetElement;

                        String[] boundary = slice.getSlicePoint().split(":");
                        cellDomain.setLo(boundary[0]);
                        if ( boundary.length == 2 ) {
                            cellDomain.setHi(boundary[1]);
                        } else if (boundary.length == 1) {
                            cellDomain.setHi(boundary[0]);
                        }
                        cellDomain.setSubsetElement(subsetElement);
                    }
                }
            }

            // Add domainSet and rangeSet of the points
            String[] members = meta.multipointDomainRangeData(TABLE_MULTIPOINT, meta.coverageID(coverageName), coverageName, cellDomainList);
            pointMembers = members[0];
            rangeMembers = members[1];
            String[] split1 = ret.split("\\{" + Templates.KEY_POINTMEMBERS + "\\}");
            String[] split2 = split1[1].split("\\{" + Templates.KEY_COVERAGEDATA + "\\}");
            sb.append(split1[0]).append(pointMembers).append(split2[0]).append(rangeMembers).append(split2[1]);

        } catch (PetascopeException ex) {
            log.error("Error", ex);
        }
        return new Response(sb.toString());
    }

    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.ENCODING_IDENTIFIER;
    }

    public String getMimeType() {
        return MIME_GML;
    }
}
