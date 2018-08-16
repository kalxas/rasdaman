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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.gml;

import petascope.core.Pair;
import petascope.core.XMLSymbols;
import petascope.util.CrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.*;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import nu.xom.Attribute;
import nu.xom.Element;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.rasdaman.domain.cis.AllowedValue;
import org.rasdaman.domain.cis.NilValue;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import static petascope.core.XMLSymbols.ATT_DEFINITION;
import static petascope.core.XMLSymbols.ATT_UOMCODE;
import static petascope.core.XMLSymbols.LABEL_ALLOWED_VALUES;
import static petascope.core.XMLSymbols.LABEL_DESCRIPTION;
import static petascope.core.XMLSymbols.LABEL_INTERVAL;
import static petascope.core.XMLSymbols.LABEL_LABEL;
import static petascope.core.XMLSymbols.LABEL_NILVALUES;
import static petascope.core.XMLSymbols.LABEL_NILVALUES_ASSOCIATION_ROLE;
import static petascope.core.XMLSymbols.LABEL_QUANTITY;
import static petascope.core.XMLSymbols.LABEL_UOM;
import static petascope.core.XMLSymbols.NAMESPACE_SWE;
import static petascope.core.XMLSymbols.PREFIX_SWE;
import petascope.wcps.metadata.model.IrregularAxis;
import petascope.core.Templates;

/**
 * Build a GML Coverage from a WcpsCoverageMetadata (i.e: represents the WCPS
 * result in GML as GetCoverage) or DescribeCoverage as well. NOTE: For
 * GetCoverge, the data of tupleList is not handled in this class but from Rasql
 * result in csv.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:a.badoi@jacobs-university.net">Andrei Badoi</a>
 */
@Service
public class GmlCoverageBuilder {

    private static final Logger log = LoggerFactory.getLogger(GmlCoverageBuilder.class);

    /* Keywords in WCS templates (alphabetic order) */
    public static final String KEY_AXISLABELS = "%axisLabels%";
    public static final String KEY_COEFFICIENTS = "%coefficients%";
    public static final String KEY_COMPONENTNAME = "%componentName%";
    public static final String KEY_COVERAGEDATA = "%coverageData%";
    public static final String KEY_COVERAGEID = "%coverageId%";
    public static final String KEY_COVERAGEFUNCTION = "%coverageFunction%";
    public static final String KEY_COVERAGESUBTYPE = "%coverageSubtype%";
    public static final String KEY_COVERAGESUBTYPEPARENT = "%coverageSubtypeParent%";
    public static final String KEY_COVERAGETYPE = "%coverageType%";
    public static final String KEY_DOMAINSET = "%domainSet%";
    public static final String KEY_FIELDNAME = "%componentName%";
    public static final String KEY_GENERAL_GRID_AXES = "%generalGridAxes%";
    public static final String KEY_GRIDAXISSPANNED = "%gridAxisSpanned%";
    public static final String KEY_GRIDDIMENSION = "%gridDimension%";
    public static final String KEY_GRIDID = "%gridId%";
    public static final String KEY_HIGH = "%high%";
    public static final String KEY_LOW = "%low%";
    public static final String KEY_LOWERCORNER = "%lowerCorner%";
    public static final String KEY_GMLCOV_METADATA = "%gmlcovMetadata%";
    public static final String KEY_MPID = "%mpId%";
    public static final String KEY_NILVALUES = "%nilValues%";
    public static final String KEY_OFFSET_VECTORS = "%offsetVectors%";
    public static final String KEY_OFFSETS = "%offsets%";
    public static final String KEY_ORIGINPOS = "%originPos%";
    public static final String KEY_POINTID = "%pointId%";
    public static final String KEY_POINTMEMBERS = "%pointMembers%";
    public static final String KEY_RANGEFIELDS = "%rangeFields%";
    public static final String KEY_RULE_DEFINITION = "%ruleDefinition%";
    public static final String KEY_SEQUENCE_RULE_ORDER = "%sequenceRuleOrder%";
    // this is used to create attributes: axisLabels, srsDimension, srsName, uomLabels in gml:Envelope
    public static final String KEY_SRSGROUP_FULL_ATTRIBUTES = "%srsGroupFullAttributes%";
    // this is used to create attribute: srsName in gml:Point, gml:offsetVector
    public static final String KEY_SRSGROUP = "%srsGroup%";
    public static final String KEY_SWE_COMPONENT = "%sweComponent%";
    public static final String KEY_UPPERCORNER = "%upperCorner%";

    public static final String PREFIX_MULTI_POINT = "multipoint_";
    public static final String SUFFIX_ORIGIN = "-origin";

    public GmlCoverageBuilder() {

    }

    /*
     * Returns true in case such coverage type refers to a gridded coverage.
     *
     * @param covType
     * @return TRUE IF covType ~ .*GridCoverage
     */
    public static boolean isGridCoverage(String covType) {
        return covType.equals(XMLSymbols.LABEL_GRID_COVERAGE)
                || covType.equals(XMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE)
                || covType.equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE);
    }

    /**
     * Returns true in case such coverage type refers to a multipoint coverage.
     *
     * @todo Not supported now
     * @param covType
     * @return TRUE IF covType ~ MultiPointCoverage
     */
    public static boolean isMultiPoint(String covType) {
        return covType.matches(XMLSymbols.LABEL_MULTIPOINT_COVERAGE);
    }

    /**
     *
     * Build a GMLCoverage in application/gml+xml format from
     * WcpsCoverageMetadata object
     *
     * @param wcpsCoverageMetadata
     * @param templateCoverage
     * @return
     * @throws PetascopeException
     * @throws SecoreException
     */
    public String build(WcpsCoverageMetadata wcpsCoverageMetadata, String templateCoverage)
            throws PetascopeException, SecoreException {

        // TODO
        // Automatize the creation of the header: namespaces, schema locations, etc. (Mind XMLSymbols).
        // Domain set
        String domainSet = "";
        if (wcpsCoverageMetadata.getGridDimension() > 0) {
            if (wcpsCoverageMetadata.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
                domainSet += Templates.getTemplate(Templates.WCS2_GRID);
            } else if (wcpsCoverageMetadata.getCoverageType().equals(XMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE)) {
                domainSet += Templates.getTemplate(Templates.WCS2_RECTIFIED_GRID);
            } else if (wcpsCoverageMetadata.getCoverageType().equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
                domainSet += Templates.getTemplate(Templates.WCS2_REFERENCEABLE_GRID_BY_VECTORS);
            } else if (wcpsCoverageMetadata.getCoverageType().equals(XMLSymbols.LABEL_MULTIPOINT_COVERAGE)) {
                // Unsupported now for multipoints
                domainSet += Templates.getTemplate(Templates.WCS2_SIMPLE_MULTIPOINT);
            } else {
                log.error("Unsupported coverage type: " + wcpsCoverageMetadata.getCoverageType());
                throw new WCSException(ExceptionCode.UnsupportedCoverageConfiguration,
                        "Unsupported coverage type: " + wcpsCoverageMetadata.getCoverageType());
            }
        } // else: "blank" domain set

        // Range type
        String rangeFields = "";
        for (RangeField range : wcpsCoverageMetadata.getRangeFields()) {
            rangeFields += Templates.getTemplate(Templates.WCS2_RANGE_FIELD,
                    Pair.of(KEY_FIELDNAME, range.getName()),
                    Pair.of(KEY_SWE_COMPONENT, getQuantity(range))
            );
        }

        // Coverage function and Envelope: required
        String coverageFunction = "";
        if (isGridCoverage(wcpsCoverageMetadata.getCoverageType())) { // coverage function is for grids
            coverageFunction += "  <" + XMLSymbols.LABEL_COVERAGE_FUNCTION + ">\n"
                    + Templates.getTemplate(Templates.WCS2_GRID_FUNCTION,
                            Pair.of(KEY_SEQUENCE_RULE_ORDER, getGridFunctionAxisOrder(wcpsCoverageMetadata))
                    ) + "\n  </" + XMLSymbols.LABEL_COVERAGE_FUNCTION + ">";
        } // else: coverageFunction yet to be investigated for non-gridded coverages. Might not be necessary for multi-*.

        // Whole document: replace keywords with values
        String ret = "";
        if (isMultiPoint(wcpsCoverageMetadata.getCoverageType())) {
            ret = Templates.getTemplate(templateCoverage,
                    // gml:Envelope with full attributes of srsGroup
                    Pair.of(KEY_SRSGROUP_FULL_ATTRIBUTES, getSrsGroupFullAttributes(wcpsCoverageMetadata)),
                    Pair.of(KEY_DOMAINSET, domainSet),
                    Pair.of(KEY_COVERAGEID, wcpsCoverageMetadata.getCoverageName()),
                    Pair.of(KEY_COVERAGETYPE, wcpsCoverageMetadata.getCoverageType()),
                    Pair.of(KEY_GMLCOV_METADATA, getGmlcovMetadata(wcpsCoverageMetadata)),
                    Pair.of(KEY_MPID, PREFIX_MULTI_POINT + wcpsCoverageMetadata.getGridId()),
                    Pair.of(KEY_SRSGROUP, getSrsGroup(wcpsCoverageMetadata)),
                    Pair.of(KEY_COVERAGEFUNCTION, coverageFunction),
                    Pair.of(KEY_RANGEFIELDS, rangeFields));
        } else {
            // gridded coverage:
            ret = Templates.getTemplate(templateCoverage,
                    Pair.of(KEY_SRSGROUP_FULL_ATTRIBUTES, getSrsGroupFullAttributes(wcpsCoverageMetadata)),
                    Pair.of(KEY_COVERAGETYPE, wcpsCoverageMetadata.getCoverageType()),
                    Pair.of(KEY_DOMAINSET, domainSet),
                    Pair.of(KEY_COVERAGEFUNCTION, coverageFunction),
                    // [!] domainSet/coverageFunction have to be replaced first: they (in turn) contains keywords to be replaced
                    // grid
                    Pair.of(KEY_AXISLABELS, StringUtils.join(getAxisLabels(wcpsCoverageMetadata), " ")),
                    Pair.of(KEY_GRIDDIMENSION, String.valueOf(wcpsCoverageMetadata.getGridDimension())),
                    Pair.of(KEY_GRIDID, wcpsCoverageMetadata.getGridId()),
                    // + rectified grid
                    Pair.of(KEY_ORIGINPOS, getOrigin(wcpsCoverageMetadata)),
                    Pair.of(KEY_POINTID, wcpsCoverageMetadata.getCoverageName() + SUFFIX_ORIGIN),
                    Pair.of(KEY_OFFSET_VECTORS, getGmlOffsetVectors(wcpsCoverageMetadata)),
                    // + referenceable grid
                    Pair.of(KEY_GENERAL_GRID_AXES, getGeneralGridAxes(wcpsCoverageMetadata)),
                    // coverage
                    Pair.of(KEY_COVERAGEID, wcpsCoverageMetadata.getCoverageName()),
                    Pair.of(KEY_COVERAGESUBTYPE, wcpsCoverageMetadata.getCoverageType()),
                    Pair.of(KEY_GMLCOV_METADATA, getGmlcovMetadata(wcpsCoverageMetadata)),
                    Pair.of(KEY_RANGEFIELDS, rangeFields),
                    Pair.of(KEY_SRSGROUP, getSrsGroup(wcpsCoverageMetadata)));
        }

        // RGBV cannot replace bounds now, see GmlFormatExtension class
        if (wcpsCoverageMetadata.getCoverageType().equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
            ret = addCoefficients(ret, wcpsCoverageMetadata);
        }

        ret = getBounds(ret, wcpsCoverageMetadata);

        return ret;
    }

    /**
     * Creates the String for gml:SRSReferenceGroup (all attributes) NOTE: only
     * used for gml:Envelope, e.g:
     * <Envelope axisLabels="Lat Long" srsDimension="2" srsName="http://localhost:8080/def/crs/EPSG/0/4326" uomLabels="degree degree"><Envelope/>
     *
     * @param wcpsCoverageMetadata WcpsCoverageMetadata
     */
    private static String getSrsGroupFullAttributes(WcpsCoverageMetadata wcpsCoverageMetadata) throws WCSException, SecoreException {
        String srsGroup;
        try {
            srsGroup = XMLSymbols.ATT_SRS_NAME + "=\"" + getSrsName(wcpsCoverageMetadata) + "\" "
                    + XMLSymbols.ATT_AXIS_LABELS + "=\"" + ListUtil.join(getAxisLabels(wcpsCoverageMetadata), " ") + "\" "
                    + XMLSymbols.ATT_UOM_LABELS + "=\"" + ListUtil.join(getUomLabels(wcpsCoverageMetadata), " ") + "\" ";
            //omit srsDimension if dimensionality == 0
            int numberOfDimensions = wcpsCoverageMetadata.getGridDimension();
            if (numberOfDimensions != 0) {
                srsGroup += XMLSymbols.ATT_SRS_DIMENSION + "=\"" + numberOfDimensions + "\"";
            }
        } catch (PetascopeException pEx) {
            log.error("Error while retrieving CRS metadata for GML: " + pEx.getMessage());
            throw new WCSException(pEx.getExceptionText(), pEx);
        } catch (SecoreException sEx) {
            log.error("Error while retrieving CRS metadata for GML: " + sEx.getMessage());
            throw sEx;
        }
        return srsGroup;
    }

    /**
     * Creates the String for gmlrgrid:origin of coverage
     *
     * @param wcpsCoverageMetadata
     * @return
     * @throws WCSException
     * @throws SecoreException
     */
    private static String getOrigin(WcpsCoverageMetadata wcpsCoverageMetadata) throws WCSException, SecoreException, PetascopeException {
        // Already ordered by CRS order
        String res = "";
        for (Axis axis : wcpsCoverageMetadata.getAxes()) {
            res += axis.getOriginRepresentation() + " ";
        }

        return res.trim();
    }

    /**
     * Creates the String for gml:SRSReferenceGroup (only 1 attribute srsName)
     * Used in: gml:Point, gml:offsetVector (NOTE: not in gml:Envelope)
     *
     * @param wcpsCoverageMetadata GetCoverageMetadata
     */
    private static String getSrsGroup(WcpsCoverageMetadata wcpsCoverageMetadata) throws WCSException, SecoreException {
        String srsGroup = XMLSymbols.ATT_SRS_NAME + "=\"" + getSrsName(wcpsCoverageMetadata) + "\"";
        return srsGroup;
    }

    /**
     * Returns the full URI of the native CRS of a coverage. Special XML
     * entities are escaped (&domain;).
     *
     * @param wcpsCoverageMetadata
     * @return
     */
    public static String getSrsName(WcpsCoverageMetadata wcpsCoverageMetadata) {
        // As coverage can be slicded axes, so the crs is not compound anymore        
        List<String> crss = new ArrayList<>();
        for (Axis axis : wcpsCoverageMetadata.getAxes()) {
            crss.add(axis.getNativeCrsUri());
        }
        String crsUri = CrsUtil.CrsUri.createCompound(crss);

        return StringUtil.escapeXmlPredefinedEntities(crsUri);
    }

    /**
     * Return the list of axes names in crs order e.g: EPSG:4326&Ansidate, the
     * order is: Lat, Long, Ansi
     *
     * @param wcpsCoverageMetadata
     * @return
     * @throws SecoreException
     * @throws PetascopeException
     */
    private static List<String> getAxisLabels(WcpsCoverageMetadata wcpsCoverageMetadata) throws SecoreException, PetascopeException {
        List<String> axisLabels = new ArrayList<>();
        for (Axis axis : wcpsCoverageMetadata.getAxes()) {
            axisLabels.add(axis.getLabel());
        }

        return axisLabels;
    }

    /**
     * Return the list of uom labels for axes in Envelope element e.g: Long
     * t(AnsiDate) will return degree d
     *
     * @param wcpsCoverageMetadata
     * @return
     */
    private static List<String> getUomLabels(WcpsCoverageMetadata wcpsCoverageMetadata) {
        List<String> uomLabels = new ArrayList<>();
        for (Axis axis : wcpsCoverageMetadata.getAxes()) {
            uomLabels.add(axis.getAxisUoM());
        }

        return uomLabels;
    }

    /**
     * Replaces the bounds of the grid
     *
     * @param gml The GML response
     * @param wcpsCoverageMetadata The metadata specific to the WCPS GetCoverage
     * request
     * @return
     * @throws petascope.exceptions.SecoreException
     * @throws petascope.exceptions.PetascopeException
     */
    public static String getBounds(String gml, WcpsCoverageMetadata wcpsCoverageMetadata) throws SecoreException, PetascopeException {
        gml = getGeoBounds(gml, wcpsCoverageMetadata);
        gml = getGridBounds(gml, wcpsCoverageMetadata);

        return gml;
    }

    /**
     * Replaces bounds for GridEnvelope e.g:
     * <GridEnvelope>
     * <low>0 0 0</low>
     * <high>62 35 3</high>
     * </GridEnvelope>
     *
     * @param gml The GML response
     * @param wcpsCoverageMetadata The metadata specific to the WCPS GetCoverage
     * request
     * @return
     */
    public static String getGridBounds(String gml, WcpsCoverageMetadata wcpsCoverageMetadata) {
        String minGridBounds = "";
        String maxGridBounds = "";
        // NOTE: OGC CITE scale (req:14, 15 tests on axes and their orders must be as same as from CRS order)
        // i.e: <GridEnvelop> </GridEnvelop> for coverage imported with CRS: 4326 will need to rewrite as Lat, Long as from CRS not by default Long, Lat (as stored in Rasdaman).
        // Don't sort by grid axes order as imported in rasdaman
        List<Axis> axisList = wcpsCoverageMetadata.getAxes();
        for (Axis axis : axisList) {
            minGridBounds += axis.getGridBounds().getLowerLimit().toPlainString() + " ";
            maxGridBounds += axis.getGridBounds().getUpperLimit().toPlainString() + " ";
        }
        gml = gml.replaceAll(KEY_LOW, minGridBounds.trim())
                .replaceAll(KEY_HIGH, maxGridBounds.trim());

        return gml;

    }

    /**
     * Replaces bounds for boundedBy Envelope
     *
     * @param gml The GML response
     * @param wcpsCoverageMetadata The metadata specific to the WCPS GetCoverage
     * request
     * @return
     * @throws petascope.exceptions.SecoreException
     * @throws petascope.exceptions.PetascopeException
     */
    public static String getGeoBounds(String gml, WcpsCoverageMetadata wcpsCoverageMetadata) throws SecoreException, PetascopeException {
        String minGeoBounds = "";
        String maxGeoBounds = "";
        // Coverage already sorted by CRS axes order (e.g: EPSG:4326&AnsiDate, then CRS order is: lat, long, ansi)
        for (Axis axis : wcpsCoverageMetadata.getAxes()) {
            minGeoBounds += axis.getLowerGeoBoundRepresentation() + " ";
            maxGeoBounds += axis.getUpperGeoBoundRepresentation() + " ";
        }

        gml = gml.replaceAll(KEY_LOWERCORNER, minGeoBounds.trim())
                .replaceAll(KEY_UPPERCORNER, maxGeoBounds.trim());

        return gml;
    }

    /**
     * Returns the configured GMLCOV metadata. This information is returned
     * along with <gmlcov:metadata> root element. NOTE: all the extra metadata
     * must be added inside <gmlcov:Extension> <covMetadata> </covMetadata>
     * </gmlcov:Extension>
     * e.g:
     * <gmlcov:metadata>
     * <gmlcov:Extension>
     * <covMetadata>
     * <Project>This is another test file</Project>
     * <Creator>This is a test creator file</Creator>
     * <Title>This is a test file</Title>
     * <slices/>
     * </covMetadata>
     * </gmlcov:Extension>
     * </gmlcov:metadata>
     *
     * @param wcpsCoverageMetadata
     */
    private static String getGmlcovMetadata(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        // GMLCOV metadata
        String gmlcovMetadata = wcpsCoverageMetadata.getMetadata();
        if (gmlcovMetadata == null || gmlcovMetadata.isEmpty()) {
            return "<" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA + "/>";
        }
        
        if (JSONUtil.isJsonValid(gmlcovMetadata)) {
            try {
                // Prettify the JSON string to be human readable
                JSONObject json = new JSONObject(gmlcovMetadata);
                gmlcovMetadata = json.toString(4).replace("\\/","/");
            } catch (JSONException ex) {
                log.warn("Cannot parse coverage extra metadata as JSON. Reason: " + ex.getMessage(), ex);
            }
        }
        
        String gmlcovFormattedMetadata = " "
                + "<" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA + ">"
                + "<" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA_EXTENSION + ">"
                + "                <" + XMLSymbols.LABEL_COVERAGE_METADATA + ">\n"
                + gmlcovMetadata // containts farther XML child elements: do not escape predefined entities (up to the user)
                + "\n                </" + XMLSymbols.LABEL_COVERAGE_METADATA + ">"
                + "</" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA_EXTENSION + ">"
                + "</" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA + ">";

        return gmlcovFormattedMetadata;
    }

    /**
     * Builds the gml:offsetVectors element for a rectified grid. The order of
     * such elements has to follow the order of grid axes (as they are stored in
     * rasdaman).
     *
     * @param wcpsCoverageMetadata
     * @return All required gml:offsetVector elements for the coverage.
     */
    private static String getGmlOffsetVectors(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException, SecoreException {
        String output = "";
        List<String> axisNames = getAxisLabels(wcpsCoverageMetadata);
        // Loop through the N dimensions
        for (int i = 0; i < axisNames.size(); i++) {
            if (i > 0) {
                output += "\n";
            }
            output += getGmlOffsetVector(wcpsCoverageMetadata, axisNames.get(i));
        }
        return output;
    }

    /**
     * Builds the gml:offsetVector element for a rectified grid along a grid
     * axis.
     *
     * @param wcpsCoverageMetadata
     * @param axisName
     * @return The single gml:offsetVector element for axis "axisName" of the
     * coverage.
     */
    private static String getGmlOffsetVector(WcpsCoverageMetadata wcpsCoverageMetadata, String axisName) throws WCSException, SecoreException {
        String output = Templates.getTemplate(Templates.WCS2_OFFSET_VECTOR,
                Pair.of(KEY_OFFSETS, getOffsetVectorComponents(wcpsCoverageMetadata, axisName)));
        return output;
    }

    /**
     * Return the offset vector (resolution for the input axis), e.g: CRS:
     * EPSG:4326&Ansidate, the CRS axes order is: Lat Long time offset vector of
     * Lat is: e.g: 10 0 0, of Long is: 0 5 0 and time is: 0 0 1
     *
     * @param metadata
     * @param axisName
     * @return
     */
    private static String getOffsetVectorComponents(WcpsCoverageMetadata metadata, String axisName) {
        int axisOrder = 0;
        BigDecimal axisResolution = null;
        for (Axis axis : metadata.getAxes()) {
            if (axis.getLabel().equals(axisName)) {
                axisResolution = axis.getResolution();
                break;
            }
            axisOrder++;
        }
        List<String> offsetVectors = new ArrayList<>();
        for (int i = 0; i < metadata.getAxes().size(); i++) {
            offsetVectors.add("0");
            if (i == axisOrder) {
                offsetVectors.set(i, BigDecimalUtil.stripDecimalZeros(axisResolution).toPlainString());
            }
        }

        return ListUtil.join(offsetVectors, " ");
    }

    /**
     * Add the coefficients in a gmlrgrid:GeneralGridAxis for irregular axes.
     *
     * @param gml The GML output already filled with data and metadata.
     * @param wcpsCoverageMetadata The metadata specific to the WCS GetCoverage
     * request
     * @return GML where {coefficients} have been replaced with real values.
     * @throws WCSException
     * @throws PetascopeException
     * @throws petascope.exceptions.SecoreException
     */
    public String addCoefficients(String gml, WcpsCoverageMetadata wcpsCoverageMetadata)
            throws WCSException, PetascopeException, SecoreException {
        for (Axis axis : wcpsCoverageMetadata.getAxes()) {
            // Only irregular axis has coefficients
            String coefficients = "";
            if (axis instanceof IrregularAxis) {
                coefficients = ((IrregularAxis) axis).getRepresentationCoefficients();
            }
            gml = gml.replaceFirst(KEY_COEFFICIENTS, coefficients);
        }
        return gml;
    }

    /**
     * Returns a gmlrgrid:generalGridAxis element, with replacements.
     *
     * @param wcpsCoverageMetadata
     */
    private static String getGeneralGridAxes(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException, SecoreException {
        String output = "";
        List<String> axisNames = getAxisLabels(wcpsCoverageMetadata);
        // Loop through the N dimensions
        for (int i = 0; i < axisNames.size(); i++) {
            if (i > 0) {
                output += "\n";
            }
            output += Templates.getTemplate(Templates.WCS2_GENERAL_GRID_AXIS,
                    Pair.of(KEY_GRIDAXISSPANNED, axisNames.get(i)),
                    Pair.of(KEY_OFFSETS,
                            getOffsetVectorComponents(wcpsCoverageMetadata, axisNames.get(i)))
            );
        }
        return output;
    }

    /**
     * If a coverage imported with this sdom [0:35,0:17,0:3] with crs axes order (EPSG:4326&AnsiDate): lat (+1), long (+2), time (+3) 
     * and grid axes order (rasdaman order) as [long (+2), lat (+1), time (+3)]
     * then the result of WCS GetCoverage in GML will return tupleList element which contains the result of: encode(c, "JSON")
     * which iterate the sdom in grid axes order: from outer time axis to lat axis then inner long axis.
     * 
     * Hence, the sequenceRule must follow the way Rasdaman iterates and it should return: +3 +1 +2 (reversed order from sdom)
     */
    private static String getGridFunctionAxisOrder(WcpsCoverageMetadata wcpsCoverageMetadata) {
        String sequenceRule = "";
        
        for (int i = wcpsCoverageMetadata.getSortedAxesByGridOrder().size() - 1; i >= 0; i--) {
            int sequenceNumber = 0;
            // Iterate by grid axes order reversed
            Axis axis = wcpsCoverageMetadata.getSortedAxesByGridOrder().get(i);
            for (int j = 0; j < wcpsCoverageMetadata.getAxes().size(); j++) {
                // Iterate by crs axes order
                if (wcpsCoverageMetadata.getAxes().get(j).getLabel().equals(axis.getLabel())) {
                    sequenceNumber = j + 1;
                    break;
                }
            }
            // e.g: +3 +1 +2 (for 3D coverage imported with grid axes order: long, lat, time)            
            sequenceRule += "+" + sequenceNumber + " ";
        }

        return sequenceRule.trim();
    }

    /**
     * Convert a WCPS RangeField object to RangeField GML representation each
     * RangeField element contains one Quantity element
     *
     * @param rangeField
     * @return
     */
    private static String getQuantity(RangeField rangeField) throws PetascopeException {
        // Example with both allowed values and NILs:
        //
        // <swe:Quantity definition="http://sweet.jpl.nasa.gov/2.0/physRadiation.owl#IonizingRadiation">
        //   <swe:label>Radiation Dose</swe:label>
        //   <swe:description>Radiation dose measured by Gamma detector</swe:description>
        //   <swe:nilValues>
        //     <swe:NilValues>
        //       <swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/BelowDetectionRange">-INF</swe:nilValue>
        //       <swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/AboveDetectionRange">INF</swe:nilValue>
        //     </swe:NilValues>
        //   </swe:nilValues>
        //   <swe:uom code="uR"/>
        //   <swe:constraint>
        //     <swe:AllowedValues>
        //       <swe:interval>-180 0</swe:interval>
        //       <swe:interval>1 180</swe:interval>
        //     </swe:AllowedValues>
        //   </swe:constraint>
        // </swe:Quantity>

        Element quantity = new Element(PREFIX_SWE + ":" + LABEL_QUANTITY, NAMESPACE_SWE);
        Element nilValues;
        Element nilValuesRole;
        Element child;
        // label
        if (null != rangeField.getName() && !rangeField.getName().isEmpty()) {
            child = new Element(PREFIX_SWE + ":" + LABEL_LABEL, NAMESPACE_SWE);
            child.appendChild(rangeField.getName());
            quantity.appendChild(child);
        }
        // description
        if (null != rangeField.getDescription() && !rangeField.getDescription().isEmpty()) {
            child = new Element(PREFIX_SWE + ":" + LABEL_DESCRIPTION, NAMESPACE_SWE);
            child.appendChild(rangeField.getDescription());
            quantity.appendChild(child);
        }
        // definition URI
        if (null != rangeField.getDefinition() && !rangeField.getDefinition().isEmpty()) {
            quantity.addAttribute(new Attribute(ATT_DEFINITION, rangeField.getDefinition()));
        }
        // NIL values
        Iterator<NilValue> nilIt = rangeField.getNodata().iterator();
        if (nilIt.hasNext()) {
            nilValuesRole = new Element(PREFIX_SWE + ":" + LABEL_NILVALUES_ASSOCIATION_ROLE, NAMESPACE_SWE);
            nilValues = new Element(PREFIX_SWE + ":" + LABEL_NILVALUES, NAMESPACE_SWE);
            while (nilIt.hasNext()) {
                NilValue nil = nilIt.next();
                if (null != nil.getValue() && !nil.getValue().isEmpty()) {
                    nilValues.appendChild(nil.toGML());
                }
            }
            nilValuesRole.appendChild(nilValues);
            quantity.appendChild(nilValuesRole);
        }

        // UoM element
        child = new Element(PREFIX_SWE + ":" + LABEL_UOM, NAMESPACE_SWE);
        child.addAttribute(new Attribute(ATT_UOMCODE, rangeField.getUomCode()));
        quantity.appendChild(child);

        // constraint element with allowed values
        List<AllowedValue> allowedValues = rangeField.getAllowedValues();
        Element constraintElement = getConstraintElement(allowedValues);
        if (constraintElement != null) {
            quantity.appendChild(constraintElement);
        }

        return quantity.toXML();
    }

    /**
     * Return the constraint element which contains <AllowedValues>
     * <swe:AllowedValues>
     * <swe:interval>-180 0</swe:interval>
     * <swe:interval>1 180</swe:interval>
     * or
     * <swe:value>180</swe:value>
     * </swe:AllowedValues>
     * </swe:constraint>
     */
    private static Element getConstraintElement(List<AllowedValue> allowedValues) {
        Element constraintElement = null;

        // Only add this element to GML when allowedValues exist in quantity of coverage's bands
        if (allowedValues.size() > 0) {
            constraintElement = new Element(PREFIX_SWE + ":" + XMLSymbols.LABEL_CONSTRAINT, NAMESPACE_SWE);

            Element allowedValuesElement = new Element(PREFIX_SWE + ":" + LABEL_ALLOWED_VALUES, NAMESPACE_SWE);
            for (AllowedValue allowedValue : allowedValues) {
                // e.g: -180 0
                if (allowedValue.getValues().trim().contains(" ")) {
                    Element intervalElement = new Element(PREFIX_SWE + ":" + LABEL_INTERVAL, NAMESPACE_SWE);
                    intervalElement.appendChild(allowedValue.getValues());
                    allowedValuesElement.appendChild(intervalElement);
                } else {
                    // e.g: 180
                    Element valueElement = new Element(PREFIX_SWE + ":" + XMLSymbols.LABEL_VALUE, NAMESPACE_SWE);
                    allowedValuesElement.appendChild(valueElement);
                }
            }

            constraintElement.appendChild(allowedValuesElement);
        }

        return constraintElement;
    }
}
