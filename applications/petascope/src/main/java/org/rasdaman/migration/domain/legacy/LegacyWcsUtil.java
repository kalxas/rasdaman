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
package org.rasdaman.migration.domain.legacy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nu.xom.Element;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.rasdaman.migration.domain.legacy.LegacyXMLSymbols.*;
import petascope.core.CrsDefinition;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.CrsUtil;

/**
 * WCS utility methods.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class LegacyWcsUtil {

    private static final Logger log = LoggerFactory.getLogger(LegacyWcsUtil.class);

    /* Constants */
    public static final String KEY_CHAR = "char";
    public static final String KEY_UCHAR = "unsigned char";
    public static final String KEY_SHORT = "short";
    public static final String KEY_USHORT = "unsigned short";
    public static final String KEY_INT = "int";
    public static final String KEY_UINT = "unsigned int";
    public static final String KEY_LONG = "long";
    public static final String KEY_ULONG = "unsigned long";
    public static final String KEY_FLOAT = "float";
    public static final String KEY_DOUBLE = "double";



    public static final String CHAR_MIN = "-128";
    public static final String CHAR_MAX = "127";
    public static final String UCHAR_MIN = "0";
    public static final String UCHAR_MAX = "255";
    public static final String SHORT_MIN = "-32768";
    public static final String SHORT_MAX = "32767";
    public static final String USHORT_MIN = "0";
    public static final String USHORT_MAX = "65535";
    public static final String INT_MIN = "-2147483648";
    public static final String INT_MAX = "2147483647";
    public static final String UINT_MIN = "0";
    public static final String UINT_MAX = "4294967295";
    public static final String LONG_MIN = "-9223372036854775808";
    public static final String LONG_MAX = "9223372036854775807";
    public static final String ULONG_MIN = "0";
    public static final String ULONG_MAX = "18446744073709551615";
    public static final String FLOAT_MIN = "+/-3.4e-38";
    public static final String FLOAT_MAX = "+/-3.4e+38";
    public static final String DOUBLE_MIN = "+/-1.7e-308";
    public static final String DOUBLE_MAX = "+/-1.7e+308";

    // If subsetting with error greater than this value (i.e: max of geo bound is: -8.975 and subset is: -8.97499999999999999 is still valid).
    public static final BigDecimal SUBSETTING_ALLOWED_ERROR = new BigDecimal(0.00001);

    /**
     * Utility method to read coverage's metadata.
     *
     * @param meta
     * @param coverageId
     * @throws SecoreException
     * @throws WCSException
     */
    public static LegacyCoverageMetadata getMetadata(LegacyDbMetadataSource meta, String coverageId)
    throws Exception {
        try {
            return meta.read(coverageId);
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Transforms a csv output returned by rasdaman server into a csv format
     * accepted by the gml:tupleList according to section 19.3.8 of the
     * OGC GML standard version 3.2.1
     *
     * @param csv - a csv input like {b1 b2 ... bn, b1 b2 ... bn, ...}, {...}
     *            where each {...} represents a dimension and each sequence b1 ... bn n bands
     * @return csv string of form b1 b2 .. bn, b1 b2 ... bn, ...
     */
    protected static String rasCsvToTupleList(String csv) {
        return csv.replace("{", "").replace("}", "").replace("\"", "");
    }

    /**
     * Convert csv format from rasdaman into a tupleList format, for including
     * in a gml:DataBlock
     *
     * @param csv coverage in csv format
     * @return tupleList representation
     */
    public static String csv2tupleList(String csv) {
        return rasCsvToTupleList(csv); // FIXME
    }

//    /**
//     * Convert spatial domain of the form [band1][band2]..., where band1 is of
//     * the form [low:high, low:high,...]
//     *
//     * @param sdom spatial domain as retreived from rasdaman with sdom(coverage)
//     * @return (low, high) bound
//     */
//    public static Pair<String, String> sdom2bounds(String sdom) 

//    public static String exceptionReportToXml(ExceptionReport report) {
//        String output = null;
//        try {
//            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.
//                                                 newInstance(report.getClass().getPackage().getName());
//            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
//            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
//            marshaller.setProperty("jaxb.formatted.output", true);
//            marshaller.setProperty("jaxb.schemaLocation",
//                                   "http://www.opengis.net/ows http://schemas.opengis.net/ows/2.0/owsExceptionReport.xsd");
//            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new PetascopeXmlNamespaceMapper());
//            StringWriter strWriter = new StringWriter();
//            marshaller.marshal(report, strWriter);
//            output = strWriter.toString();
//            String sub = output.substring(output.indexOf("<ows:Exception "), output.
//                                          indexOf("</ows:ExceptionReport>"));
//            log.debug(output);
//            log.debug(sub);
//            try {
//                output = getTemplate(EXCEPTION_REPORT, Pair.
//                                               of("\\{exception\\}", sub));
//            } catch (Exception ex) {
//                log.warn("Error handling exception report template");
//            }
//            log.debug("Done marshalling Error Report.");
//            log.debug(output);
//        } catch (JAXBException e2) {
//            log.error("Stack trace: {}", e2);
//            log.error("Error stack trace: " + e2);
//        }
//        return output;
//    }

//    public static String exceptionToXml(Exception e) {
//        return exceptionReportToXml(e.getReport());
//    }
//
//    public static String getGML(GetCoverageMetadata m, String template, DbMetadataSource meta)
//    throws WCSException, SecoreException {
//
//        // TODO
//        // Automatize the creation of the header: namespaces, schema locations, etc. (Mind XMLSymbols).
//
//        // Domain set
//        String domainSet = "";
//        if (m.getGridDimension() > 0) {
//            if (m.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
//                domainSet += getTemplate(GRID);
//            } else if (m.getCoverageType().equals(XMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE)) {
//                domainSet += getTemplate(RECTIFIED_GRID);
//            } else if (m.getCoverageType().equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
//                domainSet += getTemplate(REFERENCEABLE_GRID_BY_VECTORS);
//            } else if (m.getCoverageType().equals(XMLSymbols.LABEL_MULTIPOINT_COVERAGE)) {
//                domainSet += getTemplate(SIMPLE_MULTIPOINT);
//            } else {
//                log.error("Unsupported coverage type: " + m.getCoverageType());
//                throw new WCSException(ExceptionCode.UnsupportedCoverageConfiguration,
//                                       "Unsupported coverage type: " + m.getCoverageType());
//            }
//        } // else: "blank" domain set
//
//        // Range type
//        String rangeFields = "";
//        for (RangeField range : m.getRangeFields()) {
//            rangeFields += getTemplate(RANGE_FIELD,
//                                                 Pair.of("\\{" + KEY_FIELDNAME     + "\\}", range.getFieldName()),
//                                                 Pair.of("\\{" + KEY_SWE_COMPONENT + "\\}", range.getSWEComponent().toGML())
//                                                );
//        }
//
//        // Coverage function and Envelope: required
//        String coverageFunction = "";
//        if (WcsUtil.isGrid(m.getCoverageType())) { // coverage function is for grids
//            coverageFunction += "  <" + XMLSymbols.LABEL_COVERAGE_FUNCTION + ">\n" +
//                                getTemplate(GRID_FUNCTION,
//                                        Pair.of("\\{" + KEY_SEQUENCE_RULE_ORDER + "\\}", getOuterInnerAxisRuleOrder(m))
//                                                     ) + "\n  </" + XMLSymbols.LABEL_COVERAGE_FUNCTION + ">";
//        } // else: coverageFunction yet to be investigated for non-gridded coverages. Might not be necessary for multi-*.
//
//
//        // Whole document: replace keywords with values
//        String ret = "";
//        if (WcsUtil.isMultiPoint(m.getCoverageType())) {
//            ret = getTemplate(template,
//                    // gml:Envelope with full attributes of srsGroup
//                    Pair.of("\\{" + KEY_SRSGROUP_FULL_ATTRIBUTES   + "\\}", getSrsGroupFullAttributes(m)),
//                    Pair.of("\\{" + KEY_DOMAINSET             + "\\}", domainSet),
//                    Pair.of("\\{" + KEY_COVERAGEID            + "\\}", m.getCoverageId()),
//                    Pair.of("\\{" + KEY_COVERAGETYPE          + "\\}", m.getCoverageType()),
//                    Pair.of("\\{" + KEY_GMLCOV_METADATA       + "\\}", getGmlcovMetadata(m)),
//                    // multipoint
//                    Pair.of("\\{" + KEY_MPID                  + "\\}", PREFIX_MP + m.getGridId()),
//                    Pair.of("\\{" + KEY_SRSGROUP              + "\\}", getSrsGroup(m)),
//                    Pair.of("\\{" + KEY_COVERAGEFUNCTION      + "\\}", coverageFunction),
//                    Pair.of("\\{" + KEY_RANGEFIELDS           + "\\}", rangeFields));
//        } else {
//            // gridded coverage:
//            ret = getTemplate(template,
//                    // gml:Envelope with full attributes of srsGroup
//                    Pair.of("\\{" + KEY_SRSGROUP_FULL_ATTRIBUTES   + "\\}", getSrsGroupFullAttributes(m)),
//                    Pair.of("\\{" + KEY_DOMAINSET             + "\\}", domainSet),
//                    Pair.of("\\{" + KEY_COVERAGEFUNCTION      + "\\}", coverageFunction),
//                    // [!] domainSet/coverageFunction have to be replaced first: they (in turn) contains keywords to be replaced
//                    // grid
//                    Pair.of("\\{" + KEY_AXISLABELS            + "\\}", m.getGridAxisLabels()),
//                    Pair.of("\\{" + KEY_GRIDDIMENSION         + "\\}", String.valueOf(m.getGridDimension())),
//                    Pair.of("\\{" + KEY_GRIDID                + "\\}", m.getGridId()),
//                    // + rectified grid
//                    Pair.of("\\{" + KEY_ORIGINPOS             + "\\}", m.getGridOrigin()),
//                    Pair.of("\\{" + KEY_POINTID               + "\\}", m.getCoverageId() + SUFFIX_ORIGIN),
//                    Pair.of("\\{" + KEY_OFFSET_VECTORS        + "\\}", getGmlOffsetVectors(m)),
//                    // + referenceable grid
//                    Pair.of("\\{" + KEY_GENERAL_GRID_AXES     + "\\}", getGeneralGridAxes(m)),
//                    // coverage
//                    Pair.of("\\{" + KEY_COVERAGEID            + "\\}", m.getCoverageId()),
//                    Pair.of("\\{" + KEY_COVERAGETYPE          + "\\}", m.getCoverageType()),
//                    Pair.of("\\{" + KEY_COVERAGESUBTYPE       + "\\}", m.getCoverageType()),
//                    Pair.of("\\{" + KEY_COVERAGESUBTYPEPARENT + "\\}", addSubTypeParents(meta.getParentCoverageType(m.getCoverageType()), meta).toXML()),
//                    Pair.of("\\{" + KEY_GMLCOV_METADATA       + "\\}", getGmlcovMetadata(m)),
//                    Pair.of("\\{" + KEY_RANGEFIELDS           + "\\}", rangeFields),
//                    Pair.of("\\{" + KEY_SRSGROUP              + "\\}", getSrsGroup(m)));
//        }
//
//        // RGBV cannot replace bounds now, see GmlFormatExtension class
//        if (!m.getCoverageType().equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
//            ret = getBounds(ret, m);
//        }
//
//        // Check if boundedBy element need to add the srsName (for outputCRS or subsettingCRS)
//        // NOTE: <boundedBy srsName="http://www.opengis.net/def/crs/EPSG/0/4326"> is not valid WCS Schema 2.0.1
//        // but it is valid standard so just add this feature, meanwhile OGC CITE can update the WCS Schema.
//        ret = setBoundedByOutputCRS(ret, m);
//
//        return ret;
//    }
//
//    /**
//     * Creates the String for gml:SRSReferenceGroup (all attributes)
//     * NOTE: only used gml:Envelope, e.g:
//     * <Envelope axisLabels="Lat Long" srsDimension="2" srsName="http://localhost:8080/def/crs/EPSG/0/4326" uomLabels="degree degree"><Envelope/>
//     * @param m GetCoverageMetadata
//     */
//    private static String getSrsGroupFullAttributes(GetCoverageMetadata m) throws WCSException, SecoreException {
//        String srsGroup;
//        List<String> ccrsUri = CrsUtil.CrsUri.decomposeUri(m.getCrs());
//        try {
//            srsGroup =   XMLSymbols.ATT_SRS_NAME + "=\"" + getSrsName(m) + "\" " +
//                         XMLSymbols.ATT_AXIS_LABELS + "=\"" + ListUtil.printList(CrsUtil.getAxesLabels(ccrsUri), " ") + "\" " +
//                         XMLSymbols.ATT_UOM_LABELS + "=\"" + ListUtil.printList(CrsUtil.getAxesUoMs(ccrsUri), " ") + "\" ";
//            //omit srsDimension if dimensionality == 0
//            if (CrsUtil.getTotalDimensionality(ccrsUri) != 0) {
//                srsGroup += XMLSymbols.ATT_SRS_DIMENSION + "=\"" + CrsUtil.getTotalDimensionality(ccrsUri) + "\"";
//            }
//        } catch (PetascopeException pEx) {
//            log.error("Error while retrieving CRS metadata for GML: " + pEx.getMessage());
//            throw new WCSException(pEx.getExceptionText(), pEx);
//        } catch (SecoreException sEx) {
//            log.error("Error while retrieving CRS metadata for GML: " + sEx.getMessage());
//            throw sEx;
//        }
//        return srsGroup;
//    }
//
//    /**
//     * Creates the String for gml:SRSReferenceGroup (only 1 attribute srsName)
//     * Used in: gml:Point, gml:offsetVector (NOTE: not in gml:Envelope)
//     * @param m GetCoverageMetadata
//     */
//    private static String getSrsGroup(GetCoverageMetadata m) throws WCSException, SecoreException {
//        String srsGroup = XMLSymbols.ATT_SRS_NAME + "=\"" + getSrsName(m) + "\"";
//        return srsGroup;
//    }
//
//
//    /**
//     * Returns the full URI of the native CRS of a coverage.
//     * Special XML entities are escaped (&domain;).
//     *
//     * @param m
//     */
//    public static String getSrsName(GetCoverageMetadata m) {
//        if (m.getCrs() != null) {
//            // Need to encode the '&' that are in CCRS
//            return StringUtil.escapeXmlPredefinedEntities(m.getCrs());
//        } else {
//            return CrsUtil.GRID_CRS;
//        }
//    }
//
//    /**
//     * Replaces the bounds of the grid
//     *
//     * @param gml The GML response
//     * @param m   The metadata specific to the WCS GetCoverage request
//     */
//    public static String getBounds(String gml, GetCoverageMetadata m) {
//        gml = gml.replaceAll("\\{" + KEY_LOW        + "\\}", m.getLow())
//              .replaceAll("\\{" + KEY_HIGH        + "\\}", m.getHigh())
//              .replaceAll("\\{" + KEY_LOWERCORNER + "\\}", m.getDomLow())
//              .replaceAll("\\{" + KEY_UPPERCORNER + "\\}", m.getDomHigh());
//        return gml;
//    }
//
//    /**
//     * If outputCrs is not null or if subsettingCrs is not null then in <boundedBy> must add attribute srsName
//     * <boundedBy srsName="http://localhost:8080/def/crs/EPSG/0/4326">
//     * </boundedBy>
//     *
//     * @return
//     */
//    public static String setBoundedByOutputCRS(String gml, GetCoverageMetadata m) {
//        String srsName = null;
//        if (m.getOutputCrs() != null) {
//            srsName = m.getOutputCrs();
//        } else if (m.getSubsettingCrs() != null) {
//            srsName = m.getSubsettingCrs();
//        }
//
//        // wcs request with SubsettingCRS or OutputCRS as parameters
//        if (srsName != null) {
//            String boundedBy = "<" + LABEL_BOUNDEDBY + " " + ATT_SRS_NAME + "=\"" + srsName + "\">";
//            gml = gml.replace("<" + LABEL_BOUNDEDBY + ">" , boundedBy);
//        }
//        return gml;
//    }
//
//    /**
//     * Returns the configured GMLCOV metadata.
//     * This information is returned along with <gmlcov:metadata> root element.
//     * The content is extracted from petascopedb::ps_extrametadata.
//     * NOTE: all the extra metadata must be added inside <gmlcov:Extension> </gmlcov:Extension>
//     * e.g:
//     *  <gmlcov:metadata>
//            <gmlcov:Extension>
//                <Project>This is another test file</Project>
//                <Creator>This is a test creator file</Creator>
//                <Title>This is a test file</Title>
//                <slices/>
//            </gmlcov:Extension>
//        </gmlcov:metadata>
//     * @param m
//     */
//    private static String getGmlcovMetadata(GetCoverageMetadata m) {
//        // GMLCOV metadata
//        List<String> gmlcovMetadata = m.getMetadata().getExtraMetadata(XMLSymbols.PREFIX_GMLCOV);
//        if (gmlcovMetadata.isEmpty()) {
//            return "<" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA + "/>";
//        }
//        String gmlcovFormattedMetadata = "";
//        for (String metadataValue : gmlcovMetadata) {
//            gmlcovFormattedMetadata += "  " +
//                                       "<" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA + ">"
//                                       + "<" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA_EXTENSION + ">"
//                                       + metadataValue // containts farther XML child elements: do not escape predefined entities (up to the user)
//                                       + "</" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA_EXTENSION + ">"
//                                       + "</" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA + ">";
//        }
//        return gmlcovFormattedMetadata;
//    }
//
//    /**
//     * Builds the gml:offsetVectors element for a rectified grid.
//     * The order of such elements has to follow the order of grid axes (as they are stored in rasdaman).
//     *
//     * @param m
//     * @return All required gml:offsetVector elements for the coverage.
//     */
//    private static String getGmlOffsetVectors(GetCoverageMetadata m) throws WCSException, SecoreException {
//        String output = "";
//        String[] axisNames = m.getGridAxisLabels().split(" ");
//        // Loop through the N dimensions
//        for (int i = 0; i < axisNames.length; i++) {
//            if (i > 0) {
//                output += "\n";
//            }
//            output += getGmlOffsetVector(m, axisNames[i]);
//        }
//        return output;
//    }
//
//    /**
//     * Builds the gml:offsetVector element for a rectified grid along a grid axis.
//     *
//     * @param m
//     * @param axisName
//     * @return The single gml:offsetVector element for axis "axisName" of the coverage.
//     */
//    private static String getGmlOffsetVector(GetCoverageMetadata m, String axisName) throws WCSException, SecoreException {
//        String output = getTemplate(OFFSET_VECTOR,
//                                              Pair.of("\\{" + KEY_OFFSETS + "\\}", getVectorComponents(m, axisName)));
//        return output;
//    }

    /**
     * Gets the components of the offset vector of a certain axis of a grid.
     * The order of components here has to follow the axis order in the CRS definition.
     *
     * @param m
     * @param axisName
     * @return The tuple of CRS coordinates for a specified offset vector.
     */
    private static String getVectorComponents(LegacyGetCoverageMetadata m, String axisName) throws Exception {
        String output = "";
        List<String> ccrsUri = CrsUtil.CrsUri.decomposeUri(m.getCrs());

        if (!ccrsUri.isEmpty()) {
            try {
                if (CrsUtil.getAxesLabels(ccrsUri).contains(axisName)) { // guard for 0D coverages
                    // Example, axisName is third axis in the 3D CRS definition:
                    // offsetVector() := resolution * {0,0,1} = {0,0,resolution}
                    BigDecimal originalVector = m.getMetadata().getDomainByName(axisName).getDirectionalResolution();
                    BigDecimal scaledVector = originalVector.multiply(m.getScalingFactor(axisName));
                    BigDecimal[] vectorComponents = (BigDecimal[]) LegacyVectors.scalarMultiplication(
                                                        scaledVector, // axis resolution (possibly scaled via WCS extension)
                                                        LegacyVectors.unitVector( // {0,0,__,1,__,0,0}
                                                            CrsUtil.getTotalDimensionality(ccrsUri),
                                                            CrsUtil.getCrsAxisOrder(ccrsUri, axisName)
                                                        ));
                    output = LegacyListUtil.printList(Arrays.asList(vectorComponents), " ");
                }
            } catch (Exception pEx) {
                log.error("Error while retrieving CRS metadata for GML: " + pEx.getMessage());
                throw pEx;
            }
        }
        return output;
    }

    /**
     * Converts the time coefficients to ISO datetime stamp yyyy-MM-dd'T'HH:mm:ssZ (e.g: 2008-01-01T00:00:00Z)
     *
     * @param coeffs   time coefficients from time axis (NOTE: added with the SubsetLow of start date)
     * @param crsDefinition contains information of Time CRS
     * @return string list of time coefficients (days, seconds) to ISO datetime stamp
     * @throws petascope.exceptions.PetascopeException
     */
    public static List<String> toISODate(List<BigDecimal> coeffs, CrsDefinition crsDefinition) throws Exception {
        List<String> isoDates = new ArrayList<String>();

        // Get the UOM in milliseconds (e.g: ansidate uom: d is 86 400 000 millis, unixtime uom: seconds is 1000 millis)
        Long milliSeconds = LegacyTimeUtil.getMillis(crsDefinition);
        DateTime dateTime = new DateTime(crsDefinition.getDatumOrigin());

        for (BigDecimal coeff: coeffs) {
            // formular: Origin + (Time Coefficients * UOM in milliSeconds)
            long duration = coeff.multiply(new BigDecimal(milliSeconds)).setScale(0, RoundingMode.HALF_UP).longValue();            
            DateTime dt = dateTime.plus(duration);

            // Then convert the added date to ISO 8601 datetime (Z means UTC)
            // and we add the qoute to make XML parser easier as it is time value
            isoDates.add("\"" + dt.toString(DateTimeFormat.forPattern(LegacyTimeUtil.ISO_8061_FORMAT).withZoneUTC()) + "\"");
        }
        return isoDates;
    }

    /**
     * Extracts the coefficients of an axis.
     * Empty string is returned on regular ones.
     *
     * @param m
     * @param axisName
     * @param dbMeta
     * @return The whitespace-separated list of vector coefficients of an axis (empty string if not defined)
     * @throws WCSException
     * @throws petascope.exceptions.SecoreException
     */
    public static String getCoefficients(LegacyGetCoverageMetadata m, String axisName, LegacyDbMetadataSource dbMeta) throws Exception {
        // init
        String coefficients = "";

        if (!axisName.isEmpty()) {
            LegacyCoverageMetadata meta = m.getMetadata();
            LegacyDomainElement domEl = meta.getDomainByName(axisName);

            // "optional" coefficients
            if (domEl.isIrregular()) {
                List<BigDecimal> coeffs = domEl.getCoefficients();
                // When an irregular axis is not trimmed, the coefficients are not set in the domain element.
                // Coefficients are indeed lazily loaded in memory so that possibly only the
                // ones which have to be included in the response are actually fetched.
                // When no subsets on an irregular domain element are requested then the domain element
                // itself is not considered so there isno way to retrieve them
                // We need to get the coefficients now (laziest loading):
                if (coeffs.isEmpty()) {
                    try {
                        domEl.setCoefficients(
                            dbMeta.getAllCoefficients(
                                m.getMetadata().getCoverageName(),
                                m.getMetadata().getDomainIndexByName(domEl.getLabel()) // i-order of axis
                            ));
                        coeffs = domEl.getCoefficients();
                    } catch (Exception ex) {
                        log.error("Error while fetching the coefficients of " + domEl.getLabel());
                        throw ex;
                    }
                }

                // Adjust the coefficients to the origin of the requested grid (originally they are relative to the native origin)
                List<String> subsetLabels = CrsUtil.getAxesLabels(CrsUtil.CrsUri.decomposeUri(m.getCrs()));

                String timeCrs = null;
                // Check given axis is a time axis, then the coeffecients will need to be calculated into timestamp instead of numbers
                if (domEl.getLabel().equals(axisName) && domEl.timeAxis()) {
                    timeCrs = domEl.getNativeCrs();
                }
                                
                if (timeCrs == null) {
                    // if axis is not time axis then just get the raw coefficients
                    coefficients = LegacyListUtil.printList(coeffs, " ");
                } else {
                    // in case of time axis, subset low is a start number from the origin of CRS definition
                    // e.g: AnsiDate origin: 1600-12-31T00:00:00Z, start date (irr_cube_2) is: 2018-01-01T00:00:00Z, then subsetlow is: 148654 days.
                    // the coefficients for the time axis (irr_cube_2) is 0 (2008-01-01T00:00:00Z), 2 (2008-01-03T00:00:00Z), 4 (2008-01-05T00:00:00Z), 7 (2008-01-08T00:00:00Z)
                    coeffs = LegacyVectors.add(coeffs, domEl.getMinValue());
                    CrsDefinition crsDefinition = CrsUtil.getCrsDefinition(timeCrs);
                    // if axis is time axis then calculate the coefficients with the origin and uom to timestamp
                    coefficients = LegacyListUtil.printList(toISODate(coeffs, crsDefinition), " ");
                }
            }
        }
        return coefficients;
    }

//    /**
//     * Add the coefficients in a gmlrgrid:GeneralGridAxis.
//     * They are not known at the time of initializing the GML output, but only after processing
//     * the coverage data (see petascope.wcps.server.core.crs and DbMetadataSource.getCoefficientsOfInterval()).
//     *
//     * @param gml    The GML output already filled with data and metadata.
//     * @param m      The metadata specific to the WCS GetCoverage request
//     * @param dbMeta
//     * @return GML where {coefficients} have been replaced with real values.
//     * @throws WCSException
//     * @throws PetascopeException
//     * @throws petascope.exceptions.SecoreException
//     */
//    public static String addCoefficients(String gml, GetCoverageMetadata m, DbMetadataSource dbMeta)
//    throws WCSException, PetascopeException, SecoreException {
//        String[] axisNames = m.getGridAxisLabels().split(" ");
//        // Loop through the N dimensions (rely on order)
//        for (int i = 0; i < axisNames.length; i++) {
//            gml = gml.replaceFirst("\\{" + KEY_COEFFICIENTS + "\\}", WcsUtil.getCoefficients(m, axisNames[i], dbMeta));
//        }
//        return gml;
//    }
//
//    /**
//     * Returnes a gmlrgrid:generalGridZxis element, with replacements.
//     *
//     * @param m
//     */
//    private static String getGeneralGridAxes(GetCoverageMetadata m) throws WCSException, SecoreException {
//        String output = "";
//        String[] axisNames = m.getGridAxisLabels().split(" ");
//        // Loop through the N dimensions
//        for (int i = 0; i < axisNames.length; i++) {
//            if (i > 0) {
//                output += "\n";
//            }
//            output += getTemplate(GENERAL_GRID_AXIS,
//                                            Pair.of("\\{" + KEY_GRIDAXISSPANNED + "\\}", axisNames[i]),
//                                            Pair.of("\\{" + KEY_OFFSETS + "\\}", getVectorComponents(m, axisNames[i]))
//                                            // coefficients are visible /after/ WCPS processing
//                                           );
//        }
//        return output;
//    }
//
//    /**
//     * Return the outer-inner axis order rule for a grid function.
//     * Specifically, this method gives the proper formatting for a gml:sequenceRule/@axisOrder attribute:
//     * Eg. "+3 +2 +1" for a 3D grid.
//     * Spanning from lower to upper coordinates ('+') is assumed for every dimension.
//     *
//     * @param m
//     * @return The whitespace-separated list of inner-outer axis order.
//     */
//    private static String getOuterInnerAxisRuleOrder(GetCoverageMetadata m) {
//        StringBuilder outRule = new StringBuilder();
//        for (int i = m.getGridDimension(); i > 0; i--) {
//            outRule.append('+').append(i).append(' ');
//        }
//        return outRule.toString().trim();
//    }

    /**
     * Returns the XML genealogy of a specified GMLCOV type by recursive calls.
     *
     * @param covType The GMLCOV child type
     * @param meta    The link to the db info
     * @return The XML sequence of parent types (wcs:CoverageSubtypeParent)
     */
    public static Element addSubTypeParents(String covType, LegacyDbMetadataSource meta) {

        Element hierarchy = null;

        if (!covType.isEmpty()) {
            hierarchy = new Element(LABEL_COVERAGE_SUBTYPE_PARENT, NAMESPACE_WCS);

            // Add the type
            Element c = new Element(LABEL_COVERAGE_SUBTYPE, NAMESPACE_WCS);
            c.appendChild(covType);
            hierarchy.appendChild(c);
            // Add the parent (if exists)
            String parentCovType = meta.getParentCoverageType(covType);
            if (!parentCovType.isEmpty()) {
                hierarchy.appendChild(addSubTypeParents(parentCovType, meta));
            }
        }
        return hierarchy;
    }

    /**
     * @return the minimum interval from a and b
     */
    public static String min(String a, String b) {
        String[] as = a.split(":");
        String[] bs = b.split(":");
        if (as.length < bs.length) {
            return a;
        } else if (as.length > bs.length) {
            return b;
        }
        Integer al = toInt(as, 0);
        Integer bl = toInt(bs, 0);
        if (as.length == 1) {
            if (al < bl) {
                return a;
            } else {
                return b;
            }
        }
        Integer ah = toInt(as, 1);
        Integer bh = toInt(bs, 1);

        Integer rl = al;
        if (rl > bl) {
            rl = bl;
        }
        Integer rh = ah;
        if (rh > bh) {
            rh = bh;
        }

        return toStr(rl) + ":" + toStr(rh);
    }

    private static String toStr(Integer i) {
        if (i == Integer.MAX_VALUE) {
            return "*";
        } else {
            return i.toString();
        }
    }

    private static Integer toInt(String[] s, int i) {
        if (s[i].equals("*")) {
            return Integer.MAX_VALUE;
        } else {
            return Integer.parseInt(s[i]);
        }
    }

    /**
     * Get the signed shift that separates the grid origin from the border (from origin to corner), along one dimension.
     *
     * E.g. 3x2 grid, with origin LL corner:
     *
     *       x--------x-------x
     *       |        |       |
     *   o-------o    |       |
     *   |   |   |    |       |
     *   |   +---|----x-------x
     *   |       |
     *   o-------o
     *   <---
     *   shift
     *
     *  + = grid origin, at centre of sample space
     *  x = grid points (at centre of their sample spaces/pixels, though not depicted)
     *  o = corners of the sample space
     *
     * @param offsetVector
     * @param isIrregular
     * @param crsCode
     * @param coverageType
     * @return
     */
    public static BigDecimal getSampleSpaceShift(BigDecimal offsetVector, boolean isIrregular, String crsCode, String coverageType) {
        BigDecimal shift;
        if (isIrregular || CrsUtil.isGridCrs(crsCode) || coverageType.equals(LABEL_GRID_COVERAGE)) {
            shift = BigDecimal.ZERO;
        } else {
            shift = LegacyBigDecimalUtil.divide(offsetVector, BigDecimal.valueOf(-2));
        }
        return shift;
    }

    /**
     * Returns the next greater/lower valid value along a grid dimension.
     * Output envelopes (BBOX) need to be aligned with the sample spaces of the grid points.
     *
     * @param coordinateValue
     * @param domEl
     * @param isUpperBound
     * @return The next greater/lower value that coincides with the envelope of a point's sample space.
     */
    public static BigDecimal fitToSampleSpace(BigDecimal coordinateValue, LegacyDomainElement domEl, boolean isUpperBound, String coverageType) {

        // local variables
        BigDecimal fittedCoordinateValue = coordinateValue;
        BigDecimal distanceFromOrigin;
        BigInteger cellsFromLowerBound;

        if (!domEl.hasSampleSpace()) {
            // 0D sample space: need to fit to nearest point
            if (domEl.isIrregular() && !domEl.getCoefficients().isEmpty()) {
                // method can be called also before fetching the coefficients down at WCPS level;
                // in that case the the given coordinateValue already fits with the bbox: no need to seek the next point
                List<BigDecimal> coefficients = domEl.getCoefficients();
                // The loaded coefficients are the ones associated with points inside the input subsets
                BigDecimal fitCoefficient = (isUpperBound) ?
                                            coefficients.get(coefficients.size() - 1) : // isUpperBound : get the last point included in the response
                                            coefficients.get(0);                     // isLowerBound : get the first point included in the response
                // coordinate = Origin + (coefficient * offset_vector)
                fittedCoordinateValue = domEl.getMinValue().add(domEl.getDirectionalResolution().multiply(fitCoefficient));
            } else if (CrsUtil.isGridCrs(domEl.getCrsDef().getCode()) || coverageType.equals(LABEL_GRID_COVERAGE)) {
                // only integral coordinates are legal here
                // round up on subset.lo bounds and if coordinate is not integral
                boolean roundUp = !isUpperBound && (coordinateValue.subtract(BigDecimal.valueOf(coordinateValue.longValue())).compareTo(BigDecimal.ZERO) != 0);
                fittedCoordinateValue = BigDecimal.valueOf(coordinateValue.longValue()); // decimals discarded
                if (roundUp) {
                    fittedCoordinateValue = fittedCoordinateValue.add(BigDecimal.ONE);
                }
            }
        } else {
            // 1D sample space (along this dimension): need to fit to sample space borders
            // Count the number of full sample-spaces that fit in the bbox:
            distanceFromOrigin = coordinateValue.subtract(domEl.getMinValue());
            cellsFromLowerBound = LegacyBigDecimalUtil.divide(distanceFromOrigin, domEl.getScalarResolution()).toBigInteger();
            if (isUpperBound) {
                // User is asking the next fitted value *greater* than the given one (for upper corners)
                cellsFromLowerBound = cellsFromLowerBound.add(BigInteger.ONE);
            } // else : User is asking the next fitted value *lower* than the given one (for lower corners)
            // Z_aliged = || Z_min +   (C)*Z_res   if lower
            //            || Z_min + (C+1)*Z_res   if greater
            fittedCoordinateValue = domEl.getMinValue().add(domEl.getScalarResolution().multiply(new BigDecimal(cellsFromLowerBound)));
        }

        // Do not go beyond the native BBOX:
        if (fittedCoordinateValue.compareTo(domEl.getMaxValue()) > 0) {
            fittedCoordinateValue = domEl.getMaxValue();
        } else if (fittedCoordinateValue.compareTo(domEl.getMinValue()) < 0) {
            fittedCoordinateValue = domEl.getMinValue();
        }

        return LegacyBigDecimalUtil.stripDecimalZeros(fittedCoordinateValue);
    }

    // Overload for String input
    public static String fitToSampleSpace(String coordinateValue, LegacyDomainElement domEl, boolean greaterValue, String coverageType) {
        return fitToSampleSpace(new BigDecimal(coordinateValue), domEl, greaterValue, coverageType).toPlainString();
    }

    /**
     * Returns true in case such coverage type refers to a gridded coverage.
     *
     * @param covType
     * @return TRUE IF covType ~ .*GridCoverage
     */
    public static boolean isGrid(String covType) {
        return covType.equals(LegacyXMLSymbols.LABEL_GRID_COVERAGE) ||
               covType.equals(LegacyXMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE) ||
               covType.equals(LegacyXMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE);
    }

    /**
     * Returns true in case such coverage type refers to a multipoint coverage.
     *
     * @param covType
     * @return TRUE IF covType ~ MultiPointCoverage
     */
    public static boolean isMultiPoint(String covType) {
        return covType.matches(LegacyXMLSymbols.LABEL_MULTIPOINT_COVERAGE);
    }

    /**
     * Returns true in case the coverage of the specified type and with the given axes is aligned with the CRS axes.
     * Currently this shall always be true for grid coverages.
     *
     * @param covType
     * @param axes
     * @return TRUE IF all grid axes are aligned with a CRS axis.
     */
    public static boolean isAlignedGrid(String covType, List<LegacyDomainElement> axes) {
        return isGrid(covType);
    }

    /**
     * Returns true in case the coverage's geometry is a rectified grid.
     * This is not true when grid points are irregularly spaced along 1+ grid axes.
     *
     * @param covType
     * @param axes
     * @return TRUE IF all grid axes define regular spacing of grid points.
     */
    public static boolean isRectifiedGrid(String covType, List<LegacyDomainElement> axes) {
        boolean isRectified = true;

        if (isGrid(covType)) {
            if (!axes.isEmpty()) { // might be if all axes are sliced
                for (LegacyDomainElement domEl : axes) {
                    if (null != domEl && domEl.isIrregular()) {
                        // irregular axis: referenceable grid
                        isRectified = false;
                    }
                }
            }
        } else { // not a grid (eg a point cloud)
            isRectified = false;
        }

        return isRectified;
    }
    
    /**
     * Depend on domainElement to return the point from referenced-axis, e.g: if point of Lat axis is 90 then just returns 90.
     * If point of AnsiDate axis then instead of return value as coefficient, it needs to convert to ISO DateTime.
     * @param point
     * @param domainElement
     * @return 
     * @throws petascope.exceptions.PetascopeException 
     * @throws petascope.exceptions.SecoreException 
     */
    public static String getReferencedPointValue(BigDecimal point, LegacyDomainElement domainElement) throws Exception {
        if (domainElement.timeAxis()) {
            CrsDefinition crsDefinition = CrsUtil.getCrsDefinition(domainElement.getNativeCrs());
            List<BigDecimal> coeffcient = new ArrayList<BigDecimal>();
            coeffcient.add(point);
            
            // This list should have only 1 value
            String convertedPoint = LegacyListUtil.printList(toISODate(coeffcient, crsDefinition), " ");
            return convertedPoint;
        }
        // Not time axis, just return point
        return LegacyBigDecimalUtil.stripDecimalZeros(point).toPlainString();
    }
}
