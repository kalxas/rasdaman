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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.gml.cis10;

import java.math.BigDecimal;
import java.util.*;

import nu.xom.Element;
import nu.xom.Elements;
import org.apache.commons.lang3.StringUtils;
import org.rasdaman.domain.cis.AllowedValue;
import org.rasdaman.domain.cis.Field;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.domain.cis.IrregularAxis;
import org.rasdaman.domain.cis.NilValue;
import org.rasdaman.domain.cis.Quantity;
import org.rasdaman.domain.cis.RegularAxis;
import org.rasdaman.domain.cis.Uom;
import org.slf4j.LoggerFactory;
import petascope.core.CrsDefinition;
import static petascope.core.CrsDefinition.LONGITUDE_AXIS_LABEL_EPGS_VERSION_0;
import static petascope.core.CrsDefinition.LONGITUDE_AXIS_LABEL_EPGS_VERSION_85;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.wcst.exceptions.WCSTInvalidNilValueException;
import petascope.wcst.exceptions.WCSTLowHighDifferentSizes;
import petascope.wcst.exceptions.WCSTMissingBoundedBy;
import petascope.wcst.exceptions.WCSTMissingDomainSet;
import petascope.wcst.exceptions.WCSTMissingEnvelope;
import petascope.wcst.exceptions.WCSTMissingGridEnvelope;
import petascope.wcst.exceptions.WCSTMissingGridOrigin;
import petascope.wcst.exceptions.WCSTMissingGridType;
import petascope.wcst.exceptions.WCSTMissingHigh;
import petascope.wcst.exceptions.WCSTMissingLimits;
import petascope.wcst.exceptions.WCSTMissingLow;
import petascope.wcst.exceptions.WCSTMissingLowerCorner;
import petascope.wcst.exceptions.WCSTMissingPoint;
import petascope.wcst.exceptions.WCSTMissingPos;
import petascope.wcst.exceptions.WCSTMissingUpperCorner;
import petascope.wcst.exceptions.WCSTUnsupportedCoverageTypeException;
import petascope.wcst.exceptions.WCSTWrongInervalFormat;
import petascope.wcst.exceptions.WCSTWrongNumberOfDataBlockElements;
import petascope.wcst.exceptions.WCSTWrongNumberOfFileElements;
import petascope.wcst.exceptions.WCSTWrongNumberOfFileReferenceElements;
import petascope.wcst.exceptions.WCSTWrongNumberOfFileStructureElements;
import petascope.wcst.exceptions.WCSTWrongNumberOfPixels;
import petascope.wcst.exceptions.WCSTWrongNumberOfRangeSetElements;
import petascope.wcst.exceptions.WCSTWrongNumberOfTupleLists;
import petascope.util.CrsUtil;
import petascope.core.Pair;
import petascope.util.StringUtil;
import petascope.core.XMLSymbols;
import petascope.util.ListUtil;
import petascope.util.ras.TypeResolverUtil;
import static petascope.util.ras.TypeResolverUtil.R_Abb_Float;

/**
 * Utilities for parsing parts of a coverage, from GML format.
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class GMLParserService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GMLParserService.class);

    /**
     * Return the coverage type from wcst_import
     *
     * @param root
     * @return
     * @throws WCSTUnsupportedCoverageTypeException
     */
    public static String parseCoverageType(Element root) throws WCSTUnsupportedCoverageTypeException {
        String rootName = root.getLocalName();
        if (rootName.equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
            return XMLSymbols.LABEL_GRID_COVERAGE;
        } else if (rootName.equals(XMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE)) {
            return XMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE;
        } else if (rootName.equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
            return XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE;
        } else {
            log.error("Unsupported coverage type: " + rootName);
            throw new WCSTUnsupportedCoverageTypeException(rootName);
        }
    }

    /**
     * Parse the boundedBy element (only one) from a GMLCov.
     *
     * @param rootElement
     * @return
     * @throws petascope.wcst.exceptions.WCSTMissingBoundedBy
     */
    public static Element parseBoundedBy(Element rootElement) throws WCSTMissingBoundedBy {
        Elements boundedBy = rootElement.getChildElements(XMLSymbols.LABEL_BOUNDEDBY, XMLSymbols.NAMESPACE_GML);
        if (boundedBy.size() != 1) {
            throw new WCSTMissingBoundedBy();
        }

        return boundedBy.get(0);

    }

    /**
     *
     * Parse the child Envelope element of boundedBy element
     *
     * <Envelope srsName="http://localhost:8080/def/crs/EPSG/0/4326" axisLabels="Lat Long" uomLabels=" " srsDimension="2">
     * <lowerCorner>-44.525 111.975</lowerCorner>
     * <upperCorner>-8.975 156.275</upperCorner>
     * </Envelope>
     *
     * @param boundedByElement
     * @return
     * @throws petascope.wcst.exceptions.WCSTMissingEnvelope
     */
    public static Element parseEnvelope(Element boundedByElement) throws WCSTMissingEnvelope {
        Elements envelope = boundedByElement.getChildElements(XMLSymbols.LABEL_ENVELOPE, XMLSymbols.NAMESPACE_GML);

        if (envelope.size() != 1) {
            throw new WCSTMissingEnvelope();
        }

        return envelope.get(0);
    }

    /**
     * Parse the domainSet from a coverage in GML format. The element is needed
     * to further distinguish between grid types.
     *
     * @param rootElement: the root of the coverage in GML format
     * @return the domainSet element
     * @throws WCSTMissingDomainSet
     */
    public static Element parseDomainSet(Element rootElement) throws WCSTMissingDomainSet {
        Elements domainSet = rootElement.getChildElements(XMLSymbols.LABEL_DOMAIN_SET, XMLSymbols.NAMESPACE_GML);
        if (domainSet.size() != 1) {
            throw new WCSTMissingDomainSet();
        }
        return domainSet.get(0);
    }

    /**
     * Parses the element which determines the grid type of a coverage in GML
     * format.
     *
     * @param domainSetElement
     * @return the element determining the grid type
     * @throws petascope.wcst.exceptions.WCSTMissingGridType
     */
    public static Element parseGridType(Element domainSetElement) throws WCSTMissingGridType {
        Elements gridType = domainSetElement.getChildElements();
        if (gridType.size() != 1) {
            throw new WCSTMissingGridType();
        }
        return gridType.get(0);
    }

    /**
     * Parses the list IndexAxes (cellDomainElements) from a rectified grid, in
     * GML format
     *
     * @param rectifiedGridElement: the rectified grid element from GML
     * @return ArrayList of cellDomainElements
     * @throws petascope.wcst.exceptions.WCSTMissingLimits
     * @throws petascope.wcst.exceptions.WCSTMissingGridEnvelope
     * @throws petascope.wcst.exceptions.WCSTMissingLow
     * @throws petascope.wcst.exceptions.WCSTMissingHigh
     * @throws petascope.wcst.exceptions.WCSTLowHighDifferentSizes
     */
    public static List<IndexAxis> parseIndexAxes(Element rectifiedGridElement)
            throws WCSTMissingLimits, WCSTMissingGridEnvelope, WCSTMissingLow,
            WCSTMissingHigh, WCSTLowHighDifferentSizes {
        //get the grid limits
        Elements limits = rectifiedGridElement.getChildElements(XMLSymbols.LABEL_LIMITS, XMLSymbols.NAMESPACE_GML);
        if (limits.size() != 1) {
            throw new WCSTMissingLimits();
        }
        //get the grid envelope
        Elements gridEnvelope = limits.get(0).getChildElements(XMLSymbols.LABEL_GRID_ENVELOPE, XMLSymbols.NAMESPACE_GML);
        if (gridEnvelope.size() != 1) {
            throw new WCSTMissingGridEnvelope();
        }
        //get the lower bounds
        Elements lowPoints = gridEnvelope.get(0).getChildElements(XMLSymbols.LABEL_LOW, XMLSymbols.NAMESPACE_GML);
        if (lowPoints.size() != 1) {
            throw new WCSTMissingLow();
        }
        //get upper bounds
        Elements highPoints = gridEnvelope.get(0).getChildElements(XMLSymbols.LABEL_HIGH, XMLSymbols.NAMESPACE_GML);
        if (highPoints.size() != 1) {
            throw new WCSTMissingHigh();
        }
        String[] lowPointsList = lowPoints.get(0).getValue().trim().split(" ");
        String[] highPointsList = highPoints.get(0).getValue().trim().split(" ");
        if (lowPointsList.length != highPointsList.length) {
            throw new WCSTLowHighDifferentSizes();
        }

        // get the grid axis names which are ordered correctly for rasdaman from wcst_import (<axisLabels>Long Lat</axisLabels>)
        // (i.e: the order is set by gridOrder in ingredient file)
        String axisLabelsTmp = rectifiedGridElement.getChildElements(XMLSymbols.ATT_AXIS_LABELS, XMLSymbols.NAMESPACE_GML).get(0).toXML();
        // extract Long Lat
        String[] axisLabels = axisLabelsTmp.substring(axisLabelsTmp.indexOf(">") + 1, axisLabelsTmp.lastIndexOf("<")).split(" ");

        // create the indexAxes (cellDomainElement)
        List<IndexAxis> indexAxes = new ArrayList<>();

        // This is the order of grid axes (not CRS axes)
        for (int i = 0; i < lowPointsList.length; i++) {
            IndexAxis indexAxis = new IndexAxis();
            indexAxis.setAxisLabel(axisLabels[i]);
            indexAxis.setLowerBound(new Long(lowPointsList[i]));
            indexAxis.setUpperBound(new Long(highPointsList[i]));
            indexAxis.setAxisOrder(i);

            indexAxes.add(indexAxis);
        }

        return indexAxes;
    }

    /**
     * Parses the geo origin from a coverage in GML format e.g:  <pos>29.95 24.95
     * "1980-12-01T12:00:00+00:00"</pos>
     *
     * @param gridTypeElement
     * @return
     * @throws petascope.wcst.exceptions.WCSTMissingGridOrigin
     * @throws petascope.wcst.exceptions.WCSTMissingPoint
     * @throws petascope.wcst.exceptions.WCSTMissingPos
     * @throws petascope.exceptions.WCSException
     */
    public static String parseCoverageOrigin(Element gridTypeElement)
            throws WCSTMissingGridOrigin, WCSTMissingPoint, WCSTMissingPos, PetascopeException {
        //get the origin element
        Elements origin = gridTypeElement.getChildElements(XMLSymbols.LABEL_ORIGIN, XMLSymbols.NAMESPACE_GML);
        if (origin.size() != 1) {
            //may be in namespace gmlrgrid
            origin = gridTypeElement.getChildElements(XMLSymbols.LABEL_ORIGIN, XMLSymbols.NAMESPACE_GMLRGRID);
            //if still not exactly one
            if (origin.size() != 1) {
                throw new WCSTMissingGridOrigin();
            }
        }
        //get the point element
        Elements point = origin.get(0).getChildElements(XMLSymbols.LABEL_POINT, XMLSymbols.NAMESPACE_GML);
        if (point.size() != 1) {
            throw new WCSTMissingPoint();
        }
        //get the pos element
        Elements pos = point.get(0).getChildElements(XMLSymbols.LABEL_POS, XMLSymbols.NAMESPACE_GML);
        if (pos.size() != 1) {
            throw new WCSTMissingPos();
        }

        //transform the points into a list of BigDecimals
        String originPoints = pos.get(0).getValue().trim();

        return originPoints;
    }

    /**
     * Parses the compoundCRS from GML coverage and return list of uom for geo
     * axis order
     *
     * @param axisLabel
     * @param envelopeElement
     * @return URI -> uom of the input axis
     * @throws petascope.wcst.exceptions.WCSTMissingBoundedBy
     * @throws petascope.wcst.exceptions.WCSTMissingEnvelope
     * @throws WCSException
     * @throws petascope.exceptions.SecoreException
     */
    public static Pair<String, String> parseCrsUom(int crsAxisIndex, String axisLabel, Element envelopeElement)
            throws WCSTMissingBoundedBy, WCSTMissingEnvelope, PetascopeException, SecoreException {

        String srsNames = envelopeElement.getAttributeValue(XMLSymbols.ATT_SRS_NAME);
        //the srs uri may be compund, so split it
        List<String> srsUris = CrsUtil.CrsUri.decomposeUri(srsNames);
        
        int i = 0;
        
        for (String srsUri : srsUris) {
            CrsDefinition crsDef = CrsUtil.getCrsDefinition(srsUri);
            for (CrsDefinition.Axis crsDefinitionAxis : crsDef.getAxes()) {
                // A CRS can contains multiple axes (e.g: EPSG:4326 has Lat, Long axes)
                if (crsAxisIndex == i) {
                    // e.g: EPSG:4326 -> metre
                    Pair<String, String> crsUom = new Pair<>(srsUri, crsDefinitionAxis.getUoM());

                    return crsUom;
                }
                i++;
            }
        }

        throw new PetascopeException(ExceptionCode.InvalidAxisLabel, "Could not find coverage axis label '" + axisLabel + "' in coverage's CRS definition.");
    }

    /**
     *
     * Return the Map of geoAxesLabel and parsed geo domains (lowerBound,
     * upperBound) from <boundedBy> element
     *
     * @param rootElement
     * @return
     * @throws petascope.wcst.exceptions.WCSTMissingBoundedBy
     * @throws petascope.wcst.exceptions.WCSTMissingEnvelope
     * @throws petascope.wcst.exceptions.WCSTMissingLowerCorner
     * @throws petascope.wcst.exceptions.WCSTMissingUpperCorner
     */
    public static Map<String, Pair<String, String>> parseLowerUpperBounds(Element rootElement) throws WCSTMissingBoundedBy, WCSTMissingEnvelope, WCSTMissingLowerCorner, WCSTMissingUpperCorner {
        // e.g: geo order is  axisLabels="Lat Long t"
        // bounds: 25.00 -40.50 "1949-12-31T12:00:00+00:00"
        // then Lat -> 25, Long -> -40.50, t -> "1949-12-31T12:00:00+00:00"
        Map<String, Pair<String, String>> lowerUpperBoundsMap = new LinkedHashMap<>();

        Element boundedByElement = parseBoundedBy(rootElement);
        Element envelopeElement = parseEnvelope(boundedByElement);

        //get the lower bounds
        Elements lowerCornerElement = envelopeElement.getChildElements(XMLSymbols.LABEL_LOWER_CORNER, XMLSymbols.NAMESPACE_GML);
        if (lowerCornerElement.size() != 1) {
            throw new WCSTMissingLowerCorner();
        }
        //get upper bounds
        Elements upperCornerElement = envelopeElement.getChildElements(XMLSymbols.LABEL_UPPER_CORNER, XMLSymbols.NAMESPACE_GML);
        if (upperCornerElement.size() != 1) {
            throw new WCSTMissingUpperCorner();
        }

        // Get the geo axes order (CRS order)
        String[] axisLabels = envelopeElement.getAttributeValue(XMLSymbols.ATT_AXIS_LABELS).split(" ");

        // NOTE: the values are geoAxis order (i.e: the axes from compoundCrs)
        // e.g: <lowerCorner>25.00 -40.50 "1949-12-31T12:00:00+00:00"</lowerCorner>
        String[] lowerPoints = lowerCornerElement.get(0).getValue().trim().split(" ");
        // e.g: <upperCorner>75.50 75.50 "1950-01-06T12:00:00+00:00"</upperCorner>
        String[] upperPoints = upperCornerElement.get(0).getValue().trim().split(" ");

        for (int i = 0; i < lowerPoints.length; i++) {
            String axisLabel = axisLabels[i];
            String lowerBound = lowerPoints[i];
            String upperBound = upperPoints[i];

            Pair<String, String> lowerUpperBound = new Pair<>(lowerBound, upperBound);
            lowerUpperBoundsMap.put(axisLabel, lowerUpperBound);
        }

        return lowerUpperBoundsMap;
    }

    /**
     *
     * Return the list of geo axes (regular axis, irregular axis) for all 3
     * GeneralGridCoverage types GridCoverage, RecitfiedGridCoverage and
     * ReferenceableGridCoverage
     *
     * @param rootElement
     * @return
     * @throws petascope.wcst.exceptions.WCSTMissingDomainSet
     * @throws petascope.wcst.exceptions.WCSTMissingBoundedBy
     * @throws petascope.wcst.exceptions.WCSTMissingEnvelope
     * @throws petascope.wcst.exceptions.WCSTMissingGridType
     * @throws petascope.wcst.exceptions.WCSTMissingLowerCorner
     * @throws petascope.wcst.exceptions.WCSTMissingUpperCorner
     * @throws petascope.wcst.exceptions.WCSTMissingLimits
     * @throws petascope.wcst.exceptions.WCSTMissingGridEnvelope
     * @throws petascope.wcst.exceptions.WCSTMissingLow
     * @throws petascope.wcst.exceptions.WCSTMissingHigh
     * @throws petascope.wcst.exceptions.WCSTLowHighDifferentSizes
     * @throws petascope.exceptions.SecoreException
     */
    public static List<GeoAxis> parseGeoAxes(Element rootElement) throws WCSTMissingDomainSet, WCSTMissingBoundedBy,
            WCSTMissingEnvelope, WCSTMissingGridType, WCSTMissingLowerCorner, WCSTMissingUpperCorner, WCSTMissingLimits,
            WCSTMissingGridEnvelope, WCSTMissingLow, WCSTMissingHigh, WCSTLowHighDifferentSizes, PetascopeException, SecoreException {
        List<GeoAxis> geoAxes = new ArrayList<>();

        Element boundedByElement = parseBoundedBy(rootElement);
        Element envelopElement = parseEnvelope(boundedByElement);

        Element domainSetElement = parseDomainSet(rootElement);
        Element gridTypeElement = parseGridType(domainSetElement);

        // List of geo domains (lowerBound, upperBound) for CRS axes order
        Map<String, Pair<String, String>> lowerUpperBoundsMap = parseLowerUpperBounds(rootElement);
        // NOTE: the order of grid axes is different with geo axes (CRS axes)
        List<IndexAxis> indexAxes = parseIndexAxes(gridTypeElement);
        // List of axis resolutions (regular axis) and irregular axis by default is 1
        List<BigDecimal> offsetVectors = parseOffsetVectors(gridTypeElement);
        // Map of grid axis Order -> list of coefficients (directPositions) for irregular axis
        Map<Integer, List<BigDecimal>> coefficientMap = parseAxesCoefficients(gridTypeElement);

        // Iterate the map of geoAxes and their lower, upper bounds
        int i = 0;
        
        for (Map.Entry<String, Pair<String, String>> entry : lowerUpperBoundsMap.entrySet()) {
            String axisLabel = entry.getKey();
            int axisOrder = -1;
            Pair<String, String> lowerUpperBound = entry.getValue();

            // As the CRS order could be different with grid order, we want to map correctly resolution and coefficients to geo axis
            for (IndexAxis indexAxis : indexAxes) {
                // IndexAxis here contains the geo axis label
                if (indexAxis.getAxisLabel().equals(axisLabel)) {
                    axisOrder = indexAxis.getAxisOrder();
                    break;
                }
            }

            BigDecimal offsetVector = offsetVectors.get(axisOrder);
            // Each axis should contain a CRS URI and its uom
            Pair<String, String> crsUom = parseCrsUom(i, axisLabel, envelopElement);
            List<BigDecimal> coefficients = coefficientMap.get(axisOrder);
            if (coefficients == null) {
                // No coefficients in GML string -> Regular Axis
                RegularAxis regularAxis = new RegularAxis();
                regularAxis.setAxisLabel(axisLabel);
                regularAxis.setLowerBound(lowerUpperBound.fst);
                regularAxis.setUpperBound(lowerUpperBound.snd);
                regularAxis.setResolution(offsetVector);
                regularAxis.setSrsName(crsUom.fst);
                regularAxis.setUomLabel(crsUom.snd);

                geoAxes.add(regularAxis);
            } else {
                // Has coeffcients in GML string -> Irregular axis
                IrregularAxis irregularAxis = new IrregularAxis();
                irregularAxis.setAxisLabel(axisLabel);
                irregularAxis.setLowerBound(lowerUpperBound.fst);
                irregularAxis.setUpperBound(lowerUpperBound.snd);
                irregularAxis.setResolution(offsetVector);
                irregularAxis.setSrsName(crsUom.fst);
                irregularAxis.setUomLabel(crsUom.snd);
                // and the cofficients for axis
                irregularAxis.setDirectPositions(coefficients);

                geoAxes.add(irregularAxis);
            }
            
            i++;
        }

        return geoAxes;
    }

    /**
     * Parses the coefficient list of each axis of a coverage. They are stored
     * in a map of form grid axisOrder => coefficientList. In case the axis
     * doesn't have any coefficients, the value of the map for the axis is an
     * empty list.
     *
     * @param gridTypeElement: the xml element representing the root of the
     * grid.
     * @return map of axisOrder => list of coefficients.
     */
    public static Map<Integer, List<BigDecimal>> parseAxesCoefficients(Element gridTypeElement) {
        Map<Integer, List<BigDecimal>> result = new LinkedHashMap<>();

        //in case coefficients exist, update them
        if (gridTypeElement.getLocalName().equals(XMLSymbols.LABEL_RGBV)) {
            //general grid axes
            Elements generalGridAxes = gridTypeElement.getChildElements(XMLSymbols.LABEL_GENERAL_GRID_AXIS_ASSOCIATION_ROLE, XMLSymbols.NAMESPACE_GMLRGRID);
            //axis el in each
            for (int i = 0; i < generalGridAxes.size(); i++) {
                // Each axis contains 1 offsetVector
                Elements axis = generalGridAxes.get(i).getChildElements(XMLSymbols.LABEL_GENERAL_GRID_AXIS, XMLSymbols.NAMESPACE_GMLRGRID);
                // offset vector
                // if axis is irregular it will contains coefficients
                Elements coefficientsElement = axis.get(0).getChildElements(XMLSymbols.LABEL_COEFFICIENTS, XMLSymbols.NAMESPACE_GMLRGRID);
                if (coefficientsElement.size() > 0) {
                    String coefficientValues = coefficientsElement.get(0).getValue().trim();
                    if (!coefficientValues.isEmpty()) {
                        //split after space in case there are more than 1
                        String[] split = coefficientValues.split(" ");
                        List<BigDecimal> coefficients = new ArrayList<>();
                        for (String coeffieicent : split) {
                            coefficients.add(new BigDecimal(coeffieicent));
                        }

                        // grid axisOrder -> coefficientList
                        result.put(i, coefficients);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Return list of offset vectors (resolutions) from GridCoverage,
     * RectifiedGridCoverage or ReferenceableGridCoverage
     */
    public static List<BigDecimal> parseOffsetVectors(Element gridTypeElement) {
        List<BigDecimal> offsetVectors = new ArrayList<>();

        Elements offsetVectorElements = null;
        Elements generalGridAxes = null;

        if (!gridTypeElement.getLocalName().equals(XMLSymbols.LABEL_RGBV)) {
            offsetVectorElements = gridTypeElement.getChildElements(XMLSymbols.LABEL_OFFSET_VECTOR, XMLSymbols.NAMESPACE_GML);
        } else {
            //general grid axes
            generalGridAxes = gridTypeElement.getChildElements(XMLSymbols.LABEL_GENERAL_GRID_AXIS_ASSOCIATION_ROLE, XMLSymbols.NAMESPACE_GMLRGRID);
        }
        
        int numberOfAxes = 0;
        // RectifiedGridCoverage
        if (offsetVectorElements == null) {
            numberOfAxes = generalGridAxes.size();
        } else {
            // GridCoverage, RectifiedGridCoverage
            numberOfAxes = offsetVectorElements.size();
        }

        for (int i = 0; i < numberOfAxes; i++) {
            String[] values = null;
            // Referenceable Grid Coverage has a GML structure with
            // <generalGridAxis>
            //  <GeneralGridAxis><offsetVector/><GeneralGridAxis>
            //  <GeneralGridAxis><offsetVector/><GeneralGridAxis>
            //</generalGridAxis>
            if (generalGridAxes != null) {
                Elements axis = generalGridAxes.get(i).getChildElements(XMLSymbols.LABEL_GENERAL_GRID_AXIS, XMLSymbols.NAMESPACE_GMLRGRID);
                Element offsetVectorElement = axis.get(0).getChildElements(XMLSymbols.LABEL_OFFSET_VECTOR, XMLSymbols.NAMESPACE_GMLRGRID).get(0);
                //e.g: 0 0 1 (1 is resolution of irregular axis by default)
                values = offsetVectorElement.getValue().trim().split(" ");
            } else {
                // e.g: 0 -0.5 0 (-0.5 is resolution of negative axis, e.g: Lat)
                values = offsetVectorElements.get(i).getValue().trim().split(" ");
            }
            for (String value : values) {
                // e.g: 1 is offset vector of TimeAxis
                if (!value.equals(DEFAULT_NO_OFFSET_VECTOR)) {
                    offsetVectors.add(new BigDecimal(value));
                    break;
                }
            }
        }

        return offsetVectors;
    }

    /**
     *
     * Parses the RangeType element and all the belonging fields containing
     * quantities from GML document
     *
     * <RangeType>
     * <DataRecord>
     * <field>
     * <Quantity/>
     * </field>
     * <DataRecord>
     * ...
     * </RangeType>
     */
    public static List<Field> parseFields(Element rootElement) throws PetascopeException {
        //get the rangeType Element
        Elements rangeTypeElements = rootElement.getChildElements(XMLSymbols.LABEL_RANGE_TYPE, XMLSymbols.NAMESPACE_GMLCOV);
        if (rangeTypeElements.size() != 1) {
            throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration, "Wrong number of \"" + XMLSymbols.LABEL_RANGE_TYPE
                    + "\" elements encountered (exactly 1 expected).");
        }
        //get the DataRecord element
        Elements dataRecordElements = rangeTypeElements.get(0).getChildElements(XMLSymbols.LABEL_DATA_RECORD, XMLSymbols.NAMESPACE_SWE);
        if (dataRecordElements.size() != 1) {
            throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration, "Wrong number of \"" + XMLSymbols.LABEL_DATA_RECORD
                    + "\" elements encountered (exactly 1 expected).");
        }
        //get the fields
        Elements fieldElements = dataRecordElements.get(0).getChildElements(XMLSymbols.LABEL_FIELD, XMLSymbols.NAMESPACE_SWE);
        if (fieldElements.size() == 0) {
            throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration, "Wrong number of \"" + XMLSymbols.LABEL_FIELD
                    + "\" elements encountered (at least 1 expected).");
        }

        // Each field contains a quantity
        List<Field> fields = new ArrayList<>();
        for (int i = 0; i < fieldElements.size(); i++) {

            //get the quantity element
            Elements quantitiesElement = fieldElements.get(i).getChildElements(XMLSymbols.LABEL_QUANTITY, XMLSymbols.NAMESPACE_SWE);
            if (quantitiesElement.size() != 1) {
                throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration, "Wrong number of \"" + XMLSymbols.LABEL_QUANTITY
                        + "\" elements encountered (exactly 1 expected).");
            }

            //get the field name
            String name = fieldElements.get(i).getAttributeValue(XMLSymbols.ATT_NAME);
            Field field = new Field();
            field.setName(name);

            Quantity quantity = parseSweQuantity(quantitiesElement.get(0));
            field.setQuantity(quantity);

            fields.add(field);
        }

        return fields;
    }

    /**
     * Parses a sweQuantity element into a Quantity object
     *
     * @param quantityElement the Quantity element from the coverage in GML
     * format
     * @return a Quantity object
     */
    public static Quantity parseSweQuantity(Element quantityElement) throws WCSTWrongInervalFormat, WCSException {
        String description = null;
        String definitionUri = null;
        String uomCode = null;

        //get the description
        Elements descriptionElements = quantityElement.getChildElements(XMLSymbols.LABEL_DESCRIPTION, XMLSymbols.NAMESPACE_SWE);
        if (descriptionElements.size() != 0) {
            description = descriptionElements.get(0).getValue().trim();
        }

        //get the uom code
        Elements uomCodes = quantityElement.getChildElements(XMLSymbols.LABEL_UOM, XMLSymbols.NAMESPACE_SWE);
        if (uomCodes.size() != 0) {
            uomCode = uomCodes.get(0).getAttributeValue(XMLSymbols.ATT_UOMCODE);
        }

        // get the nilvalues
        List<NilValue> nils = new ArrayList<>();
        Elements nilValues = quantityElement.getChildElements(XMLSymbols.LABEL_NILVALUES_ASSOCIATION_ROLE, XMLSymbols.NAMESPACE_SWE);
        if (nilValues.size() != 0) {
            Elements innerNilValues = nilValues.get(0).getChildElements(XMLSymbols.LABEL_NILVALUES, XMLSymbols.NAMESPACE_SWE);
            //get the actual values
            if (innerNilValues.size() != 0) {
                Elements actualNilValues = innerNilValues.get(0).getChildElements(XMLSymbols.LABEL_NILVALUE, XMLSymbols.NAMESPACE_SWE);
                if (actualNilValues.size() != 0) {
                    for (int i = 0; i < actualNilValues.size(); i++) {
                        String value = actualNilValues.get(i).getValue().trim();
                        // Don't add the nilValue is "" to the list of nilValues
                        if (!value.equals("")) {                            
                            validateNilValue(value);
                            String reason = actualNilValues.get(i).getAttributeValue(XMLSymbols.ATT_REASON);
                            nils.add(new NilValue(value, reason));
                        }
                    }
                }
            }
        }

        // get the allowedValues (not supported yet in wcst_import)
        // @TODO: add this feature first in wcst_import and to petascope
        List<AllowedValue> allowedValues = new ArrayList<>();

        Quantity quantity = new Quantity();
        quantity.setDescription(description);
        quantity.setDefinition(definitionUri);
        quantity.setAllowedValues(allowedValues);
        quantity.setUom(new Uom(uomCode));
        quantity.setNilValues(nils);

        return quantity;
    }

    /**
     * Rasdaman supports only integers or intervals formed of integers as nil
     * values values.
     */
    private static void validateNilValue(String nilValue) throws WCSTInvalidNilValueException {
        if (!"".equals(nilValue)) {
            //for intervals, split after :
            String[] parts = nilValue.split(":");
            //each part has to be an integer
            for (String part : parts) {
                if (!containsNaN(part)) {
                    try {
                        new Float(part);
                    } catch (NumberFormatException ex) {
                        throw new WCSTInvalidNilValueException(nilValue);
                    }
                    }
            }
        }
    }

    /**
     * Parses the rangeSet element of a coverage in GML format and returns the
     * first child.
     */
    public static Element parseRangeSet(Element root) throws WCSTWrongNumberOfRangeSetElements {
        Elements rangeSet = root.getChildElements(XMLSymbols.LABEL_RANGESET, XMLSymbols.NAMESPACE_GML);
        if (rangeSet.size() != 1) {
            throw new WCSTWrongNumberOfRangeSetElements();
        }

        return rangeSet.get(0);
    }

    /**
     * Parses the dataBlock element of a coverage in GML format and returns the
     * first child.
     */
    public static Element parseDataBlock(Element rangeSet) throws WCSTWrongNumberOfDataBlockElements, WCSException {
        Elements dataBlock = rangeSet.getChildElements(XMLSymbols.LABEL_DATABLOCK, XMLSymbols.NAMESPACE_GML);
        if (dataBlock.size() != 1) {
            throw new WCSTWrongNumberOfDataBlockElements();
        }
        return dataBlock.get(0);
    }

    /**
     * Returns the mime type of the inserted file
     *
     * @param rangeSet the range set xml block
     * @return the mime type of the file to be inserted
     */
    public static String parseMimeType(Element rangeSet) throws WCSException {
        //get the File element
        Elements file = rangeSet.getChildElements(XMLSymbols.LABEL_FILE, XMLSymbols.NAMESPACE_GML);
        if (file.size() != 1) {
            throw new WCSTWrongNumberOfFileElements();
        }
        //get the fileReference
        Elements mimetype = file.get(0).getChildElements(XMLSymbols.LABEL_FILE_STRUCTURE, XMLSymbols.NAMESPACE_GML);
        if (mimetype.size() != 1) {
            throw new WCSTWrongNumberOfFileStructureElements();
        }
        return mimetype.get(0).getValue().trim();
    }

    /**
     * Returns the rangeParameters content as String if the elent exist, null
     * otherwise.
     */
    public static String parseRangeParameters(Element rangeSet) {
        Elements rangeParameters = rangeSet.getChildElements(XMLSymbols.LABEL_RANGE_PARAMETERS, XMLSymbols.NAMESPACE_GML);
        if (rangeParameters.size() == 1) {
            return rangeParameters.get(0).getValue().trim();
        } else {
            //rangePrameters might be missing
            return "";
        }
    }

    /**
     * Parses the file reference form a GML coverage.
     */
    public static String parseFilePath(Element rangeSet)
            throws WCSTWrongNumberOfFileElements, WCSTWrongNumberOfFileReferenceElements, WCSException {
        //get the File element
        Elements file = rangeSet.getChildElements(XMLSymbols.LABEL_FILE, XMLSymbols.NAMESPACE_GML);
        if (file.size() != 1) {
            throw new WCSTWrongNumberOfFileElements();
        }
        //get the fileReference
        Elements fileName = file.get(0).getChildElements(XMLSymbols.LABEL_FILE_REFERENCE, XMLSymbols.NAMESPACE_GML);
        if (fileName.size() != 1) {
            throw new WCSTWrongNumberOfFileReferenceElements();
        }

        return fileName.get(0).getValue().trim();
    }

    /**
     * Parses a GML tuple list into a rasdaman constant.
     *
     * @param dataBlock the dataBlock element
     * @param indexAxes
     * @param typeSuffixes the suffix to be added to each point to indicate its
     * rasdaman type (i.e. rasdaman Char 1 world be 1c, so the suffix is c)
     * @return String representation of a rasdaman constant
     */
    public static String parseGMLTupleList(Element dataBlock, List<IndexAxis> indexAxes, List<String> typeSuffixes)
            throws WCSTWrongNumberOfPixels, WCSTWrongNumberOfTupleLists, WCSException {
        //get the tuple list
        Elements tupleLists = dataBlock.getChildElements(XMLSymbols.LABEL_TUPLELIST, XMLSymbols.NAMESPACE_GML);
        if (tupleLists.size() != 1) {
            throw new WCSTWrongNumberOfTupleLists();
        }
        //get the cell separators
        String ts = DEFAULT_TS;
        String cs = DEFAULT_CS;
        if (tupleLists.get(0).getAttribute(XMLSymbols.ATT_TS) != null) {
            ts = tupleLists.get(0).getAttributeValue(XMLSymbols.ATT_TS);
        }
        if (tupleLists.get(0).getAttribute(XMLSymbols.ATT_CS) != null) {
            cs = tupleLists.get(0).getAttributeValue(XMLSymbols.ATT_CS);
        }
        //get the values
        String values = StringUtil.trim(tupleLists.get(0).getValue().trim());
        //get the points (inside <gml:tupleList> element of incoming GML)
        String[] points = values.split(ts);
        String interval = "";
        String rasdamanValues = "";
        //iterate through each dimension and add points to the rasdaman constant
        //cells in the last dimension are separated by ,, all others by ;
        long totalNumberOfPoints = 1;
        long innerMostDimensionSize = 1;
        for (IndexAxis indexAxis : indexAxes) {
            totalNumberOfPoints *= (indexAxis.getUpperBound() - indexAxis.getLowerBound() + 1);
            interval += indexAxis.getLowerBound() + RASDAMAN_INTERVAL_HILO_SEP + indexAxis.getUpperBound();
            //if not last, add sep
            if (indexAxis.getAxisOrder() != indexAxes.size() - 1) {
                interval += RASDAMAN_INTERVAL_DIM_SEP;
            } else {
                //compute size of last dim
                innerMostDimensionSize = indexAxis.getUpperBound() - indexAxis.getLowerBound() + 1;
            }
        }
        if (totalNumberOfPoints != points.length) {
            throw new WCSTWrongNumberOfPixels();
        }
        //iterate through all points
        for (int i = 0; i < totalNumberOfPoints; i++) {
            rasdamanValues += parsePointValue(points[i], cs, typeSuffixes);
            if (i != totalNumberOfPoints - 1) {
                //add separator
                if ((i + 1) % innerMostDimensionSize == 0) {
                    //add dimension separator
                    rasdamanValues += RASDAMAN_VALUES_DIM_SEP;
                } else {
                    //add cell separator
                    rasdamanValues += RASDAMAN_VALUES_CELL_SEP;
                }
            }
        }
        
        String result = TEMPLATE_RASDAMAN_CONSTANT.replace(TOKEN_INTERVAL, interval)
                .replace(TOKEN_VALUES, rasdamanValues);

        return result;
    }
    
    /**
     * Add data type suffix  (e.g: "0l" with "l" is suffix) if input value is not "NaN".
     */
    private static String addSuffix(String val, String suffix) {
        if (containsNaNf(val) ||
            (containsNaN(val) && !suffix.equals(R_Abb_Float)))
            return val;
        else
            return val + suffix;
    }

    /**
     * Helper for parsing a point value.
     * e.g: -999,-999,0 with data types (float,float,short) 
     * return -999f,-999f,0s
     */
    private static String parsePointValue(String point, String separator, List<String> typeSuffixes) {
        //multiband image
        if (point.contains(separator)) {
            String[] pointValues = point.split(separator);
            List<String> addedSuffixValues = new ArrayList<>();
            int i = 0;
            for (String val : pointValues) {
                String typeSuffix = typeSuffixes.get(i);
                // e.g: {0c,0c,0c}
                String addedValue = addSuffix(val, typeSuffix);
                addedSuffixValues.add(addedValue);
                
                i++;
            }
            
            String result = TEMPLATE_RASDAMAN_STRUCTURE.replace(TOKEN_STRUCTURE_CELL_VAL, ListUtil.join(addedSuffixValues, ","));
            return result;
        }
        // single band (e.g: 0c)
        point = addSuffix(point, typeSuffixes.get(0));
        return point;
    }
    
    /**
     * Check if input string contains NaN as null value
     */
    private static boolean containsNaN(String point) {
        return StringUtils.containsIgnoreCase(point, NAN_NULL_VALUE);
    }

    /**
     * Check if input string contains NaNf as null value
     */
    private static boolean containsNaNf(String point) {
        return StringUtils.containsIgnoreCase(point, NANF_NULL_VALUE);
    }

    /**
     * Parses gml:metadata elements.
     */
    public static String parseExtraMetadata(Element root) {
        String ret = "";
        Elements metadata = root.getChildElements(XMLSymbols.LABEL_METADATA, XMLSymbols.NAMESPACE_GMLCOV);
        if (metadata.size() > 0 && metadata.get(0).getChildCount() > 0) {
            //since the node can contain xml sometimes, json other times, we need to return
            //the actual content of the node as string.

            //the string representation of the node
            for (int i = 0; i < metadata.get(0).getChildCount(); i++) {
                ret += metadata.get(0).getChild(i).toXML();
            }
        }
        return ret;
    }

    /**
     * Parses the compound crs from list of geo axes CRSs
     */
    public static String parseSrsName(Element rootElement) throws WCSTMissingBoundedBy, WCSTMissingEnvelope {
        Element boundedByElement = GMLParserService.parseBoundedBy(rootElement);
        Element envelopeElement = GMLParserService.parseEnvelope(boundedByElement);
        String srsName = envelopeElement.getAttributeValue(XMLSymbols.ATT_SRS_NAME);

        return srsName;
    }

   
    private static final String DEFAULT_NO_OFFSET_VECTOR = "0";
    private static final String NAN_NULL_VALUE = "NaN";
    private static final String NANF_NULL_VALUE = "NaNf";
    private static final String DEFAULT_TS = " ";
    private static final String DEFAULT_CS = ",";
    private static final String TOKEN_INTERVAL = "%tokenInterval%";
    private static final String TOKEN_VALUES = "%tokenValues%";
    private static final String TEMPLATE_RASDAMAN_CONSTANT = "<[" + TOKEN_INTERVAL + "] " + TOKEN_VALUES + ">";
    private static final String RASDAMAN_INTERVAL_HILO_SEP = ":";
    private static final String RASDAMAN_INTERVAL_DIM_SEP = ",";
    private static final String RASDAMAN_VALUES_CELL_SEP = ",";
    private static final String RASDAMAN_VALUES_DIM_SEP = ";";
    private static final String TOKEN_STRUCTURE_CELL_VAL = "%structureCellValue%";
    private static final String TEMPLATE_RASDAMAN_STRUCTURE = "{" + TOKEN_STRUCTURE_CELL_VAL + "}";

}
