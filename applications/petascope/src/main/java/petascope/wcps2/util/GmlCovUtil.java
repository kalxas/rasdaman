package petascope.wcps2.util;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.swe.datamodel.NilValue;
import petascope.swe.datamodel.RealPair;
import petascope.util.*;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.RangeField;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.service.CoverageRegistry;
import petascope.wcs2.templates.Templates;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static petascope.util.XMLSymbols.*;

/**
 * Utilities for GMLCOV.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:a.badoi@jacobs-university.net">Andrei Badoi</a>
 */
public class GmlCovUtil {
    private static final Logger log = LoggerFactory.getLogger(GmlCovUtil.class);

    private CoverageRegistry coverageRegistry;

    public GmlCovUtil(CoverageRegistry coverageRegistry) {
        this.coverageRegistry = coverageRegistry;
    }

    /**
     * Convert csv format from rasdaman into a tupleList format, for including
     * in a gml:DataBlock
     *
     * @param csv coverage in csv format
     * @return tupleList representation
     */
    public static String csv2tupleList(String csv) {
        return rasCsvToTupleList(csv);
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

    public String getGML(WcpsCoverageMetadata m, String template)
            throws PetascopeException, SecoreException {

        // TODO
        // Automatize the creation of the header: namespaces, schema locations, etc. (Mind XMLSymbols).

        // Domain set
        String domainSet = "";
        if (m.getGridDimension() > 0) {
            if (m.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
                domainSet += Templates.getTemplate(Templates.GRID);
            } else if (m.getCoverageType().equals(XMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE)) {
                domainSet += Templates.getTemplate(Templates.RECTIFIED_GRID);
            } else if (m.getCoverageType().equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
                domainSet += Templates.getTemplate(Templates.REFERENCEABLE_GRID_BY_VECTORS);
            } else if (m.getCoverageType().equals(XMLSymbols.LABEL_MULTIPOINT_COVERAGE)) {
                domainSet += Templates.getTemplate(Templates.SIMPLE_MULTIPOINT);
            } else {
                log.error("Unsupported coverage type: " + m.getCoverageType());
                throw new WCSException(ExceptionCode.UnsupportedCoverageConfiguration,
                        "Unsupported coverage type: " + m.getCoverageType());
            }
        } // else: "blank" domain set

        // Range type
        String rangeFields = "";
        for (RangeField range: m.getRangeFields()) {
            rangeFields += Templates.getTemplate(Templates.RANGE_FIELD,
                    Pair.of("\\{" + Templates.KEY_FIELDNAME     + "\\}", range.getName()),
                    Pair.of("\\{" + Templates.KEY_SWE_COMPONENT + "\\}", toGML(range))
            );
        }

        // Coverage function and Envelope: required
        String coverageFunction = "";
        if (WcsUtil.isGrid(m.getCoverageType())) { // coverage function is for grids
            coverageFunction += "  <" + XMLSymbols.LABEL_COVERAGE_FUNCTION + ">\n" +
                    Templates.getTemplate(Templates.GRID_FUNCTION,
                            Pair.of("\\{" + Templates.KEY_SEQUENCE_RULE_ORDER + "\\}", getOuterInnerAxisRuleOrder(m))
                    ) + "\n  </" + XMLSymbols.LABEL_COVERAGE_FUNCTION + ">";
        } // else: coverageFunction yet to be investigated for non-gridded coverages. Might not be necessary for multi-*.


        // Whole document: replace keywords with values
        String ret = "";
        if (WcsUtil.isMultiPoint(m.getCoverageType())){
            ret = Templates.getTemplate(template,
                    // gml:Envelope with full attributes of srsGroup
                    Pair.of("\\{" + Templates.KEY_SRSGROUP_FULL_ATTRIBUTES   + "\\}", getSrsGroupFullAttributes(m)),
                    Pair.of("\\{" + Templates.KEY_DOMAINSET             + "\\}", domainSet),
                    Pair.of("\\{" + Templates.KEY_COVERAGEID            + "\\}", m.getCoverageName()),
                    Pair.of("\\{" + Templates.KEY_COVERAGETYPE          + "\\}", m.getCoverageType()),
                    Pair.of("\\{" + Templates.KEY_GMLCOV_METADATA       + "\\}", getGmlcovMetadata(m)),
                    // multipoint
                    Pair.of("\\{" + Templates.KEY_MPID                  + "\\}", Templates.PREFIX_MP + m.getGridId()),
                    Pair.of("\\{" + Templates.KEY_SRSGROUP              + "\\}", getSrsGroup(m)),
                    Pair.of("\\{" + Templates.KEY_COVERAGEFUNCTION      + "\\}", coverageFunction),
                    Pair.of("\\{" + Templates.KEY_RANGEFIELDS           + "\\}", rangeFields));
        } else {
            // gridded coverage:
            ret = Templates.getTemplate(template,
                    Pair.of("\\{" + Templates.KEY_SRSGROUP_FULL_ATTRIBUTES   + "\\}", getSrsGroupFullAttributes(m)),
                    Pair.of("\\{" + Templates.KEY_DOMAINSET             + "\\}", domainSet),
                    Pair.of("\\{" + Templates.KEY_COVERAGEFUNCTION      + "\\}", coverageFunction),
                    // [!] domainSet/coverageFunction have to be replaced first: they (in turn) contains keywords to be replaced
                    // grid
                    Pair.of("\\{" + Templates.KEY_AXISLABELS            + "\\}", StringUtils.join(getAxisLabels(m), " ")),
                    Pair.of("\\{" + Templates.KEY_GRIDDIMENSION         + "\\}", String.valueOf(m.getGridDimension())),
                    Pair.of("\\{" + Templates.KEY_GRIDID                + "\\}", m.getGridId()),
                    // + rectified grid
                    Pair.of("\\{" + Templates.KEY_ORIGINPOS             + "\\}", getOrigin(m)),
                    Pair.of("\\{" + Templates.KEY_POINTID               + "\\}", m.getCoverageName() + Templates.SUFFIX_ORIGIN),
                    Pair.of("\\{" + Templates.KEY_OFFSET_VECTORS        + "\\}", getGmlOffsetVectors(m)),
                    // + referenceable grid
                    Pair.of("\\{" + Templates.KEY_GENERAL_GRID_AXES     + "\\}", getGeneralGridAxes(m)),
                    // coverage
                    Pair.of("\\{" + Templates.KEY_COVERAGEID            + "\\}", m.getCoverageName()),
                    Pair.of("\\{" + Templates.KEY_COVERAGETYPE          + "\\}", m.getCoverageType()),
                    Pair.of("\\{" + Templates.KEY_COVERAGESUBTYPE       + "\\}", m.getCoverageType()),
                    Pair.of("\\{" + Templates.KEY_GMLCOV_METADATA       + "\\}", getGmlcovMetadata(m)),
                    Pair.of("\\{" + Templates.KEY_RANGEFIELDS           + "\\}", rangeFields),
                    Pair.of("\\{" + Templates.KEY_SRSGROUP              + "\\}", getSrsGroup(m)));
        }

        // RGBV cannot replace bounds now, see GmlFormatExtension class
        if (m.getCoverageType().equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
            ret = addCoefficients(ret, m);
        }

        ret = getBounds(ret, m);

        return ret;
    }

    /**
     * Creates the String for gml:SRSReferenceGroup (all attributes)
     * NOTE: only used gml:Envelope, e.g:
     * <Envelope axisLabels="Lat Long" srsDimension="2" srsName="http://localhost:8080/def/crs/EPSG/0/4326" uomLabels="degree degree"><Envelope/>
     * @param m WcpsCoverageMetadata
     */
    private static String getSrsGroupFullAttributes(WcpsCoverageMetadata m) throws WCSException, SecoreException {
        String srsGroup;
        List<String> ccrsUri = CrsUtil.CrsUri.decomposeUri(m.getCrsUri());
        try {
            srsGroup =   XMLSymbols.ATT_SRS_NAME + "=\"" + getSrsName(m) + "\" " +
                    XMLSymbols.ATT_AXIS_LABELS + "=\"" + ListUtil.printList(CrsUtil.getAxesLabels(ccrsUri), " ") + "\" " +
                    XMLSymbols.ATT_UOM_LABELS + "=\"" + ListUtil.printList(CrsUtil.getAxesUoMs(ccrsUri), " ") + "\" ";
            //omit srsDimension if dimensionality == 0
            if (CrsUtil.getTotalDimensionality(ccrsUri) != 0) {
                srsGroup += XMLSymbols.ATT_SRS_DIMENSION + "=\"" + CrsUtil.getTotalDimensionality(ccrsUri) + "\"";
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

    private static String getOrigin(WcpsCoverageMetadata m) throws WCSException, SecoreException {
        List<String> ccrsUri = CrsUtil.CrsUri.decomposeUri(m.getCrsUri());
        String res = "";
        try {
            List<String> orderedAxesLabels = CrsUtil.getAxesLabels(ccrsUri);
            for(String label : orderedAxesLabels){
                res += m.getAxisByName(label).getOrigin().toPlainString() + " ";
            }
        } catch (PetascopeException pEx){
            log.error("Error while retrieving CRS metadata for GML: " + pEx.getMessage());
            throw new WCSException(pEx.getExceptionText(), pEx);
        } catch (SecoreException sEx) {
            log.error("Error while retrieving CRS metadata for GML: " + sEx.getMessage());
            throw sEx;
        }
        return res;
    }

    /**
     * Creates the String for gml:SRSReferenceGroup (only 1 attribute srsName)
     * Used in: gml:Point, gml:offsetVector (NOTE: not in gml:Envelope)
     * @param m GetCoverageMetadata
     */
    private static String getSrsGroup(WcpsCoverageMetadata m) throws WCSException, SecoreException {
        String srsGroup = XMLSymbols.ATT_SRS_NAME + "=\"" + getSrsName(m) + "\"";
        return srsGroup;
    }

    /**
     * Returns the full URI of the native CRS of a coverage.
     * Special XML entities are escaped (&entity;).
     *
     * @param m
     */
    public static String getSrsName(WcpsCoverageMetadata m) {
        if (m.getCrsUri() != null) {
            // Need to encode the '&' that are in CCRS
            return StringUtil.escapeXmlPredefinedEntities(m.getCrsUri());
        } else {
            return CrsUtil.GRID_CRS;
        }
    }

    /**
     * Replaces the bounds of the grid
     *
     * @param gml The GML response
     * @param m   The metadata specific to the WCPS GetCoverage request
     */
    public static String getBounds(String gml, WcpsCoverageMetadata m) throws SecoreException, PetascopeException {
        gml = getGridBounds(gml, m);
        gml = getGeoBounds(gml,m);
        return gml;
    }

    /**
     * Replaces bounds for GridEnvelope
     *
     * @param gml The GML response
     * @param m   The metadata specific to the WCPS GetCoverage request
     */
    public static String getGridBounds(String gml, WcpsCoverageMetadata m){
        String minGridBounds = "";
        String maxGridBounds = "";
        List<Axis> axisList = m.getAxes();
        Collections.sort(axisList, new Comparator<Axis>() {
            @Override
            public int compare(Axis axis1, Axis axis2) {
                if(axis1.getRasdamanOrder() < axis2.getRasdamanOrder()){
                    return -1;
                } else if (axis1.getRasdamanOrder() > axis2.getRasdamanOrder()){
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        for(Axis axis : axisList){
            minGridBounds += axis.getGridBounds().getLowerLimit().toString() + " ";
            maxGridBounds += axis.getGridBounds().getUpperLimit().toString() + " ";
        }
        gml = gml.replaceAll("\\{" + Templates.KEY_LOW        + "\\}", minGridBounds)
                .replaceAll("\\{" + Templates.KEY_HIGH        + "\\}", maxGridBounds);

        return gml;

    }

    /**
     * Replaces bounds for Envelope
     *
     * @param gml The GML response
     * @param m   The metadata specific to the WCPS GetCoverage request
     */
    public static String getGeoBounds(String gml, WcpsCoverageMetadata m) throws SecoreException, PetascopeException {
        String minGeoBounds = "";
        String maxGeoBounds = "";

        List<String> crsAxisLabels = getAxisLabels(m);

        for(String label : crsAxisLabels){
            //find the corresponding axis
            Axis axis = m.getAxisByName(label);
            minGeoBounds += axis.getGeoBounds().getLowerLimit().toString() + " ";
            maxGeoBounds += axis.getGeoBounds().getUpperLimit().toString() + " ";
        }

        gml = gml
                .replaceAll("\\{" + Templates.KEY_LOWERCORNER + "\\}", minGeoBounds)
                .replaceAll("\\{" + Templates.KEY_UPPERCORNER + "\\}", maxGeoBounds);

        return gml;
    }

    //TODO:returns the list of axis labels in the crs order
    private static List<String> getAxisLabels(WcpsCoverageMetadata m) throws SecoreException, PetascopeException {
        String crs = m.getCrsUri();
        return CrsUtil.getAxesLabels(crs);

    }

    /**
     * Returns the configured GMLCOV metadata.
     * This information is returned along with <gmlcov:metadata> root element.
     * The content is extracted from petascopedb::ps_extrametadata.
     * NOTE: all the extra metadata must be added inside <gmlcov:Extension> </gmlcov:Extension>
     * e.g:
     *  <gmlcov:metadata>
     <gmlcov:Extension>
     <Project>This is another test file</Project>
     <Creator>This is a test creator file</Creator>
     <Title>This is a test file</Title>
     <slices/>
     </gmlcov:Extension>
     </gmlcov:metadata>
     * @param m
     */
    private static String getGmlcovMetadata(WcpsCoverageMetadata m) {
        // GMLCOV metadata
        String gmlcovMetadata = m.getMetadata();
        if (gmlcovMetadata == null || gmlcovMetadata.isEmpty()) {
            return "<" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA + "/>";
        }
        String gmlcovFormattedMetadata =  "  " +
                "<" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA + ">"
                + "<" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA_EXTENSION + ">"
                + m.getMetadata() // containts farther XML child elements: do not escape predefined entities (up to the user)
                + "</" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA_EXTENSION + ">"
                + "</" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_GMLCOVMETADATA + ">";

        return gmlcovFormattedMetadata;
    }

    /**
     * Builds the gml:offsetVectors element for a rectified grid.
     * The order of such elements has to follow the order of grid axes (as they are stored in rasdaman).
     *
     * @param m
     * @return All required gml:offsetVector elements for the coverage.
     */
    private static String getGmlOffsetVectors(WcpsCoverageMetadata m) throws PetascopeException, SecoreException {
        String output = "";
        String[] axisNames = new String[getAxisLabels(m).size()];
        axisNames = getAxisLabels(m).toArray(axisNames);
        // Loop through the N dimensions
        for (int i = 0; i < axisNames.length; i++) {
            if (i > 0) {
                output += "\n";
            }
            output += getGmlOffsetVector(m, axisNames[i]);
        }
        return output;
    }

    /**
     * Builds the gml:offsetVector element for a rectified grid along a grid axis.
     *
     * @param m
     * @param axisName
     * @return The single gml:offsetVector element for axis "axisName" of the coverage.
     */
    private static String getGmlOffsetVector(WcpsCoverageMetadata m, String axisName) throws WCSException, SecoreException {
        String output = Templates.getTemplate(Templates.OFFSET_VECTOR,
                Pair.of("\\{" + Templates.KEY_OFFSETS + "\\}", getVectorComponents(m, axisName)));
        return output;
    }

    /**
     * Gets the components of the offset vector of a certain axis of a grid.
     * The order of components here has to follow the axis order in the CRS definition.
     *
     * @param m
     * @param axisName
     * @return The tuple of CRS coordinates for a specified offset vector.
     */
    private static String getVectorComponents(WcpsCoverageMetadata m, String axisName) throws WCSException, SecoreException {
        String output = "";
        List<String> ccrsUri = CrsUtil.CrsUri.decomposeUri(m.getCrsUri());

        if (!ccrsUri.isEmpty()) {
            try {
                if (CrsUtil.getAxesLabels(ccrsUri).contains(axisName)) { // guard for 0D coverages
                    // Example, axisName is third axis in the 3D CRS definition:
                    // offsetVector() := resolution * {0,0,1} = {0,0,resolution}
                    BigDecimal originalVector = m.getAxisByName(axisName).getResolution();
                    BigDecimal[] vectorComponents = (BigDecimal[]) Vectors.scalarMultiplication(
                            originalVector, // axis resolution (possibly scaled via WCS extension)
                            Vectors.unitVector( // {0,0,__,1,__,0,0}
                                    CrsUtil.getTotalDimensionality(ccrsUri),
                                    CrsUtil.getCrsAxisOrder(ccrsUri, axisName)
                            ));
                    output = ListUtil.printList(Arrays.asList(vectorComponents), " ");
                }
            } catch (PetascopeException pEx) {
                log.error("Error while retrieving CRS metadata for GML: " + pEx.getMessage());
                throw new WCSException(pEx.getExceptionText(), pEx);
            } catch (SecoreException sEx) {
                log.error("Error while retrieving CRS metadata for GML: " + sEx.getMessage());
                throw sEx;
            }
        }
        return output;
    }




    /**
     * Add the coefficients in a gmlrgrid:GeneralGridAxis.
     * They are not known at the time of initializing the GML output, but only after processing
     * the coverage data (see petascope.wcps.server.core.crs and DbMetadataSource.getCoefficientsOfInterval()).
     *
     * @param gml    The GML output already filled with data and metadata.
     * @param m      The metadata specific to the WCS GetCoverage request
     * @return GML where {coefficients} have been replaced with real values.
     * @throws WCSException
     * @throws PetascopeException
     * @throws petascope.exceptions.SecoreException
     */
    public String addCoefficients(String gml, WcpsCoverageMetadata m)
            throws WCSException, PetascopeException, SecoreException {
        // Loop through the N dimensions (rely on order)
        for (Axis axis: m.getAxes()) {
            gml = gml.replaceFirst("\\{" + Templates.KEY_COEFFICIENTS + "\\}", coverageRegistry.getCoefficients(m, axis.getLabel()));
        }
        return gml;
    }

    /**
     * Returnes a gmlrgrid:generalGridZxis element, with replacements.
     *
     * @param m
     */
    private static String getGeneralGridAxes(WcpsCoverageMetadata m) throws PetascopeException, SecoreException {
        String output = "";
        String[] axisNames = new String[getAxisLabels(m).size()];
        axisNames = getAxisLabels(m).toArray(axisNames);
        // Loop through the N dimensions
        for (int i = 0; i < axisNames.length; i++) {
            if (i > 0) {
                output += "\n";
            }
            output += Templates.getTemplate(Templates.GENERAL_GRID_AXIS,
                    Pair.of("\\{" + Templates.KEY_GRIDAXISSPANNED + "\\}", axisNames[i]),
                    Pair.of("\\{" + Templates.KEY_OFFSETS + "\\}", getVectorComponents(m, axisNames[i]))
                    // coefficients are visible /after/ WCPS processing
            );
        }
        return output;
    }

    /**
     * Return the outer-inner axis order rule for a grid function.
     * Specifically, this method gives the proper formatting for a gml:sequenceRule/@axisOrder attribute:
     * Eg. "+3 +2 +1" for a 3D grid.
     * Spanning from lower to upper coordinates ('+') is assumed for every dimension.
     *
     * @param m
     * @return The whitespace-separated list of inner-outer axis order.
     */
    private static String getOuterInnerAxisRuleOrder(WcpsCoverageMetadata m) {
        StringBuilder outRule = new StringBuilder();
        for (int i = m.getGridDimension(); i > 0; i--) {
            outRule.append('+').append(i).append(' ');
        }
        return outRule.toString().trim();
    }

    public static String toGML(RangeField rangeField) {
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
        // UoM
        child = new Element(PREFIX_SWE + ":" + LABEL_UOM, NAMESPACE_SWE);
        child.addAttribute(new Attribute(ATT_UOMCODE, rangeField.getUom()));
        quantity.appendChild(child);
        // constraint
        Iterator<RealPair> constraintIt = rangeField.getAllowedValues().getIntervalIterator();
        Element constraintEl;
        if (constraintIt.hasNext()) {
            constraintEl = new Element(PREFIX_SWE + ":" + LABEL_CONSTRAINT, NAMESPACE_SWE);
            constraintEl.appendChild(rangeField.getAllowedValues().toGML());
            quantity.appendChild(constraintEl);
        }

        Document indentedQuantity = new Document(quantity);
        try {
            Serializer serializer = new Serializer(System.out);
            serializer.setIndent(2);
            serializer.write(indentedQuantity);
        } catch (IOException ex) {

        }
        return indentedQuantity.getRootElement().toXML();
    }

}
