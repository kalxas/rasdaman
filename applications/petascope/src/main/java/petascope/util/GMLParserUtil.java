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
package petascope.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import petascope.core.CrsDefinition;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.exceptions.WCSException;
import petascope.exceptions.wcst.WCSTInvalidNilValueException;
import petascope.exceptions.wcst.WCSTLowHighDifferentSizes;
import petascope.exceptions.wcst.WCSTMissingBoundedBy;
import petascope.exceptions.wcst.WCSTMissingDomainSet;
import petascope.exceptions.wcst.WCSTMissingEnvelope;
import petascope.exceptions.wcst.WCSTMissingGridEnvelope;
import petascope.exceptions.wcst.WCSTMissingGridOrigin;
import petascope.exceptions.wcst.WCSTMissingGridType;
import petascope.exceptions.wcst.WCSTMissingHigh;
import petascope.exceptions.wcst.WCSTMissingLimits;
import petascope.exceptions.wcst.WCSTMissingLow;
import petascope.exceptions.wcst.WCSTMissingPoint;
import petascope.exceptions.wcst.WCSTMissingPos;
import petascope.exceptions.wcst.WCSTUnsupportedCoverageTypeException;
import petascope.exceptions.wcst.WCSTWrongInervalFormat;
import petascope.exceptions.wcst.WCSTWrongNumberOfDataBlockElements;
import petascope.exceptions.wcst.WCSTWrongNumberOfFileElements;
import petascope.exceptions.wcst.WCSTWrongNumberOfFileReferenceElements;
import petascope.exceptions.wcst.WCSTWrongNumberOfFileStructureElements;
import petascope.exceptions.wcst.WCSTWrongNumberOfOffsetVectors;
import petascope.exceptions.wcst.WCSTWrongNumberOfPixels;
import petascope.exceptions.wcst.WCSTWrongNumberOfRangeSetElements;
import petascope.exceptions.wcst.WCSTWrongNumberOfTupleLists;
import petascope.swe.datamodel.AllowedValues;
import petascope.swe.datamodel.NilValue;
import petascope.swe.datamodel.Quantity;
import petascope.swe.datamodel.RealPair;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcps.server.core.RangeElement;

/**
 * Utilities for parsing parts of a coverage, from GML format.
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class GMLParserUtil {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GMLParserUtil.class);

    public static String parseCoverageType(Element root) throws WCSTUnsupportedCoverageTypeException {
        String rootName = root.getLocalName();
        if (rootName.equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
            return XMLSymbols.LABEL_GRID_COVERAGE;
        } else if (rootName.equals(XMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE)) {
            return XMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE;
        } else if (rootName.equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
            return XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE;
        }
        else {
            log.error("Unsupported coverage type: " + rootName);
            throw new WCSTUnsupportedCoverageTypeException(rootName);
        }
    }

    /**
     * Parses the domainSet from a coverage in GML format. The element is needed
     * to further distinguish between grid types.
     *
     * @param root: the root of the coverage in GML format
     * @return the domainSet element
     * @throws WCSException
     */
    public static Element parseDomainSet(Element root) throws WCSTMissingDomainSet {
        Elements domainSet = root.getChildElements(XMLSymbols.LABEL_DOMAIN_SET, XMLSymbols.NAMESPACE_GML);
        if (domainSet.size() != 1) {
            throw new WCSTMissingDomainSet();
        }
        return domainSet.get(0);
    }

    /**
     * Parses the element which determines the grid type of a coverage in GML
     * format.
     *
     * @param domainSet: the domainSet element of the coverage in GML format
     * @return the element determining the grid type
     * @throws petascope.exceptions.wcst.WCSTMissingGridType
     */
    public static Element parseGridType(Element domainSet) throws WCSTMissingGridType {
        Elements gridType = domainSet.getChildElements();
        if (gridType.size() != 1) {
            throw new WCSTMissingGridType();
        }
        return gridType.get(0);
    }

    /**
     * Parses the cellDomainElements from a rectified grid, in GML format
     *
     * @param rectifiedGrid: the rectified grid element from GML
     * @return ArrayList of cellDomainElements
     * @throws petascope.exceptions.wcst.WCSTMissingLimits
     * @throws petascope.exceptions.wcst.WCSTMissingGridEnvelope
     * @throws petascope.exceptions.wcst.WCSTMissingLow
     * @throws petascope.exceptions.wcst.WCSTMissingHigh
     * @throws petascope.exceptions.wcst.WCSTLowHighDifferentSizes
     * @throws WCSException
     */
    public static List<CellDomainElement> parseRectifiedGridCellDomain(Element rectifiedGrid)
            throws WCSTMissingLimits, WCSTMissingGridEnvelope, WCSTMissingLow, WCSTMissingHigh, WCSTLowHighDifferentSizes {
        //get the grid limits
        Elements limits = rectifiedGrid.getChildElements(XMLSymbols.LABEL_LIMITS, XMLSymbols.NAMESPACE_GML);
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
        //create the cellDominElements
        List<CellDomainElement> cellDomainElements = new ArrayList<CellDomainElement>(lowPointsList.length);
        for (int i = 0; i < lowPointsList.length; i++) {
            CellDomainElement e;
            try {
                e = new CellDomainElement(lowPointsList[i], highPointsList[i], i);
                cellDomainElements.add(e);
            } catch (WCPSException ex) {
                throw new WCSTLowHighDifferentSizes();
            }
        }
        return cellDomainElements;
    }

    /**
     * Parses the crs list from a rectified grid, in GML format
     *
     * @param root: the root element of the coverage in GML
     * @return axis -> URI
     * @throws petascope.exceptions.wcst.WCSTMissingBoundedBy
     * @throws petascope.exceptions.wcst.WCSTMissingEnvelope
     * @throws WCSException
     * @throws petascope.exceptions.SecoreException
     */
    public static List<Pair<CrsDefinition.Axis, String>> parseCrsList(Element root)
            throws WCSTMissingBoundedBy, WCSTMissingEnvelope, PetascopeException, SecoreException {
        Elements boundedBy = root.getChildElements(XMLSymbols.LABEL_BOUNDEDBY, XMLSymbols.NAMESPACE_GML);
        if (boundedBy.size() != 1) {
            throw new WCSTMissingBoundedBy();
        }
        Elements envelope = boundedBy.get(0).getChildElements(XMLSymbols.LABEL_ENVELOPE, XMLSymbols.NAMESPACE_GML);
        if (envelope.size() != 1) {
            throw new WCSTMissingEnvelope();
        }

        String srsNames = envelope.get(0).getAttributeValue(XMLSymbols.ATT_SRS_NAME);
        //the srs uri may be compund, so split it
        List<String> srsUris = CrsUtil.CrsUri.decomposeUri(srsNames);
        List<Pair<CrsDefinition.Axis, String>> crsAxes = new ArrayList<Pair<CrsDefinition.Axis, String>>();
        for (String srsUri : srsUris) {
            CrsDefinition crsDef = CrsUtil.getGmlDefinition(srsUri);
            for (CrsDefinition.Axis axis : crsDef.getAxes()) {
                crsAxes.add(Pair.of(axis, srsUri));
            }
        }

        return crsAxes;
    }

    /**
     * Parses the grid origin from a coverage in GML format.
     *
     * @param gridType
     * @return
     * @throws petascope.exceptions.wcst.WCSTMissingGridOrigin
     * @throws petascope.exceptions.wcst.WCSTMissingPoint
     * @throws petascope.exceptions.wcst.WCSTMissingPos
     * @throws petascope.exceptions.WCSException
     */
    public static String[] parseGridOrigin(Element gridType)
            throws WCSTMissingGridOrigin, WCSTMissingPoint, WCSTMissingPos, PetascopeException {
        //get the origin element
        Elements origin = gridType.getChildElements(XMLSymbols.LABEL_ORIGIN, XMLSymbols.NAMESPACE_GML);
        if (origin.size() != 1) {
            //may be in namespace gmlrgrid
            origin = gridType.getChildElements(XMLSymbols.LABEL_ORIGIN, XMLSymbols.NAMESPACE_GMLRGRID);
            //if still not exactly one
            if(origin.size() != 1) {
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
        String[] originPoints = pos.get(0).getValue().trim().split(" ");

        return originPoints;
    }

    /**
     * Parses the offset vectors from a coverage in GML format, for regular
     * grids.
     *
     * @param gridType the element which indicates the type of the grid in the
     * GML definition (e.g. RectifiedGrid)
     * @param gridOriginSize the size of the grid origin vector, for consistency
     * check
     * @return LinkedHashMap<List<BigDecimal>, BigDecimal>
     * @throws petascope.exceptions.wcst.WCSTWrongNumberOfOffsetVectors
     * @throws WCSException
     * @throws PetascopeException
     */
    public static LinkedHashMap<List<BigDecimal>, BigDecimal> parseGridAxes(Element gridType, Integer gridOriginSize)
            throws WCSTWrongNumberOfOffsetVectors, WCSException, PetascopeException {
        List<Pair<Element, BigDecimal>> offsetVectors;
        if(!gridType.getLocalName().equals(XMLSymbols.LABEL_RGBV)){
            offsetVectors = parseElements(gridType.getChildElements(XMLSymbols.LABEL_OFFSET_VECTOR, XMLSymbols.NAMESPACE_GML));
        }
        else{
            offsetVectors = parseOffsetVectorsFromRGBV(gridType);
        }
        //check that there is an offset vector for each axis
        String dimensionalityString = gridType.getAttributeValue(XMLSymbols.LABEL_DIMENSION);
        Integer dimensionality = new Integer(dimensionalityString);
        if (offsetVectors.size() != dimensionality) {
            throw new WCSTWrongNumberOfOffsetVectors();
        }
        //for each axis, add its vector
        LinkedHashMap<List<BigDecimal>, BigDecimal> result = new LinkedHashMap<List<BigDecimal>, BigDecimal>(dimensionality);
        for (int i = 0; i < dimensionality; i++) {
            //decompose the vector into elements
            String[] offsetVector = offsetVectors.get(i).fst.getValue().trim().split(" ");
            //check if the list is not empty
            if (offsetVector.length == 0 || offsetVector[0].isEmpty()) {
                throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                        "At least one offsetVector is empty");
            }
            List<BigDecimal> vector = new ArrayList<BigDecimal>(offsetVector.length);
            for (String el : offsetVector) {
                vector.add(new BigDecimal(el));
            }
            // Check if offset vector is aligned with a CRS axis
            List<Integer> orderList = Vectors.nonZeroComponentsIndices(vector.toArray(new BigDecimal[vector.size()]));
            if (Vectors.nonZeroComponentsIndices(vector.toArray(new BigDecimal[vector.size()])).size() > 1) {
                throw new PetascopeException(ExceptionCode.UnsupportedCoverageConfiguration,
                        "Offset vector " + vector + " is forbidden. "
                        + "Only aligned offset vectors are currently allowed (1 non-zero component).");
            }
            // Check consistency origin/offset-vector
            if (vector.size() != gridOriginSize) {
                throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                        "Incompatible dimensionality of grid origin (" + gridOriginSize
                        + ") and offset vector (" + vector.size() + ")");
            }

            result.put(vector, offsetVectors.get(i).snd);
        }

        return result;
    }

    private static List<Pair<Element, BigDecimal>> parseElements(Elements elements){
        List<Pair<Element, BigDecimal>> result = new ArrayList<Pair<Element, BigDecimal>>();
        for(int i = 0; i < elements.size(); i++){
            result.add(Pair.<Element, BigDecimal>of(elements.get(i), null));
        }
        return result;
    }

    private static List<Pair<Element, BigDecimal>> parseOffsetVectorsFromRGBV(Element rgbvGrid){
        List<Pair<Element, BigDecimal>> result = new ArrayList<Pair<Element, BigDecimal>>();

        //general grid axes
        Elements generalGridAxes = rgbvGrid.getChildElements(XMLSymbols.LABEL_GENERAL_GRID_AXIS, XMLSymbols.NAMESPACE_GMLRGRID);
        //axis el in each
        for(int i = 0; i < generalGridAxes.size(); i++){
            BigDecimal greatesCoefficient = null;
            Elements axis = generalGridAxes.get(i).getChildElements(XMLSymbols.LABEL_GENERAL_GRID, XMLSymbols.NAMESPACE_GMLRGRID);
            //offset vector
            if(axis.size() > 0){
                //take the first one
                Elements offsetVectors = axis.get(0).getChildElements(XMLSymbols.LABEL_OFFSET_VECTOR, XMLSymbols.NAMESPACE_GMLRGRID);
                //coefficients
                Elements coefficients = axis.get(0).getChildElements(XMLSymbols.LABEL_COEFFICIENTS, XMLSymbols.NAMESPACE_GMLRGRID);
                if(coefficients.size() > 0){
                    String coefficientValues = coefficients.get(0).getValue().trim();
                    if(!coefficientValues.isEmpty()) {
                        //split after space in case there are more than 1
                        String[] split = coefficientValues.split(" ");
                        //take the last one
                        greatesCoefficient = new BigDecimal(split[split.length - 1]);
                    }
                }
                if(offsetVectors.size() > 0){
                    result.add(Pair.of(offsetVectors.get(0), greatesCoefficient));
                }
            }
        }
        return result;
    }

    /**
     * Parses a sweQuantity element into a Quantity object
     *
     * @param quantity the Quantity element from the coverage in GML format
     * @return a Quantity object
     * @throws petascope.exceptions.wcst.WCSTWrongInervalFormat
     * @throws WCSException
     */
    public static Quantity parseSweQuantity(Element quantity) throws WCSTWrongInervalFormat, WCSException {
        String label = null;
        String description = null;
        String definitionUri = null;
        String uomCode = null;
        List<NilValue> nils = new ArrayList<NilValue>();
        AllowedValues allowedValues = null;

        //get the label
        Elements labelElements = quantity.getChildElements(XMLSymbols.LABEL_LABEL, XMLSymbols.NAMESPACE_SWE);
        if (labelElements.size() != 0) {
            label = labelElements.get(0).getValue().trim();
        }

        //get the description
        Elements descriptionElements = quantity.getChildElements(XMLSymbols.LABEL_DESCRIPTION, XMLSymbols.NAMESPACE_SWE);
        if (descriptionElements.size() != 0) {
            description = descriptionElements.get(0).getValue().trim();
        }

        //get the uom code
        Elements uomCodes = quantity.getChildElements(XMLSymbols.LABEL_UOM, XMLSymbols.NAMESPACE_SWE);
        if (uomCodes.size() != 0) {
            uomCode = uomCodes.get(0).getAttributeValue(XMLSymbols.ATT_UOMCODE);
        }

        //get the nilvalues
        Elements nilValues = quantity.getChildElements(XMLSymbols.LABEL_NILVALUES_ASSOCIATION_ROLE, XMLSymbols.NAMESPACE_SWE);
        if (nilValues.size() != 0) {
            Elements innerNilValues = nilValues.get(0).getChildElements(XMLSymbols.LABEL_NILVALUES, XMLSymbols.NAMESPACE_SWE);
            //get the actual values
            if (innerNilValues.size() != 0) {
                Elements actualNilValues = innerNilValues.get(0).getChildElements(XMLSymbols.LABEL_NILVALUE, XMLSymbols.NAMESPACE_SWE);
                if (actualNilValues.size() != 0) {
                    for (int i = 0; i < actualNilValues.size(); i++) {
                        String value = actualNilValues.get(i).getValue().trim();
                        validateNilValue(value);
                        String reason = actualNilValues.get(i).getAttributeValue(XMLSymbols.ATT_REASON);
                        nils.add(new NilValue(value, reason));
                    }
                }
            }
        }

        //get the allowed values, for now only deal with allowed intervals
        List<RealPair> allowedIntervals = new ArrayList<RealPair>();
        Elements constrains = quantity.getChildElements(XMLSymbols.LABEL_CONSTRAINT, XMLSymbols.NAMESPACE_SWE);
        for (int i = 0; i < constrains.size(); i++) {
            //get the AllowedValues element
            Elements allowedValuesEl = constrains.get(i).getChildElements(XMLSymbols.LABEL_ALLOWED_VALUES, XMLSymbols.NAMESPACE_SWE);
            for (int j = 0; j < allowedValuesEl.size(); j++) {
                //get the intervals
                Elements intervals = allowedValuesEl.get(j).getChildElements(XMLSymbols.LABEL_INTERVAL, XMLSymbols.NAMESPACE_SWE);
                //add each interval to the list
                for (int k = 0; k < intervals.size(); k++) {
                    String[] intervalValues = intervals.get(k).getValue().trim().split(" ");
                    //check if the interval has exactly 2 points: low and high
                    if (intervalValues.length != 2) {
                        throw new WCSTWrongInervalFormat();
                    }
                    BigDecimal lowLimit = new BigDecimal(intervalValues[0]);
                    BigDecimal highLimit = new BigDecimal(intervalValues[1]);
                    allowedIntervals.add(new RealPair(lowLimit, highLimit));
                }
            }
        }

        if (!allowedIntervals.isEmpty()) {
            allowedValues = new AllowedValues(allowedIntervals);
        }

        //return the quantity object
        return new Quantity(
                label,
                description,
                definitionUri,
                nils,
                uomCode,
                allowedValues
        );
    }

    /**
     * Rasdaman supports only integers or intervals formed of integers as nil values.
     */
    private static void validateNilValue(String nilValue) throws WCSTInvalidNilValueException {
       if (!"".equals(nilValue)) {
            //for intervals, split after :
            String[] parts = nilValue.split(":");
            //each part has to be an integer
            for(String i : parts){
                try {
                    Long.parseLong(i);
                } catch (NumberFormatException ex) {
                    throw new WCSTInvalidNilValueException(nilValue);
                }
            }
        }
    }

    /**
     * Parses the range elements and their quantities
     *
     * @param root the root of the coverage document in GML format
     * @return List<Pair<RangeElement, Quantity>>
     * @throws PetascopeException
     */
    public static List<Pair<RangeElement, Quantity>> parseRangeElementQuantities(Element root) throws PetascopeException {
        //get the rangeType Element
        Elements rangeTypes = root.getChildElements(XMLSymbols.LABEL_RANGE_TYPE, XMLSymbols.NAMESPACE_GMLCOV);
        if (rangeTypes.size() != 1) {
            throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration, "Wrong number of \"" + XMLSymbols.LABEL_RANGE_TYPE
                    + "\" elements encountered (exactly 1 expected).");
        }
        //get the DataRecord element
        Elements dataRecords = rangeTypes.get(0).getChildElements(XMLSymbols.LABEL_DATA_RECORD, XMLSymbols.NAMESPACE_SWE);
        if (dataRecords.size() != 1) {
            throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration, "Wrong number of \"" + XMLSymbols.LABEL_DATA_RECORD
                    + "\" elements encountered (exactly 1 expected).");
        }
        //get the fields
        Elements fields = dataRecords.get(0).getChildElements(XMLSymbols.LABEL_FIELD, XMLSymbols.NAMESPACE_SWE);
        if (fields.size() == 0) {
            throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration, "Wrong number of \"" + XMLSymbols.LABEL_FIELD
                    + "\" elements encountered (at least 1 expected).");
        }
        List<Pair<RangeElement, Quantity>> rangeElementQuantities = new ArrayList<Pair<RangeElement, Quantity>>(fields.size());
        //from each field create a range element
        for (int i = 0; i < fields.size(); i++) {
            //get the field name
            String name = fields.get(i).getAttributeValue(XMLSymbols.ATT_NAME);

            //get the quantity element
            Elements qunatities = fields.get(i).getChildElements(XMLSymbols.LABEL_QUANTITY, XMLSymbols.NAMESPACE_SWE);
            if (qunatities.size() != 1) {
                throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration, "Wrong number of \"" + XMLSymbols.LABEL_QUANTITY
                        + "\" elements encountered (exactly 1 expected).");
            }
            Quantity quantity = parseSweQuantity(qunatities.get(0));

            //get the type, char now
            String type = WcpsConstants.MSG_UNSIGNED_CHAR;

            //add the range element to the list
            rangeElementQuantities.add(new Pair<RangeElement, Quantity>(new RangeElement(name, type, quantity.getUom()), quantity));
        }

        return rangeElementQuantities;
    }

    /**
     * Parses the rangeSet element of a coverage in GML format and returns the
     * first child.
     *
     * @param root
     * @return the rangeSet element
     * @throws petascope.exceptions.wcst.WCSTWrongNumberOfRangeSetElements
     * @throws WCSException
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
     * @param rangeSet
     * @return the dataBlock element
     * @throws petascope.exceptions.wcst.WCSTWrongNumberOfDataBlockElements
     * @throws WCSException
     */
    public static Element parseDataBlock(Element rangeSet) throws WCSTWrongNumberOfDataBlockElements, WCSException {
        Elements dataBlock = rangeSet.getChildElements(XMLSymbols.LABEL_DATABLOCK, XMLSymbols.NAMESPACE_GML);
        if(dataBlock.size() != 1){
            throw new WCSTWrongNumberOfDataBlockElements();
        }
        return dataBlock.get(0);
    }

    /**
     * Returns the mime type of the inserted file
     * @param rangeSet the range set xml block
     * @return the mime type of the file to be inserted
     * @throws WCSException
     */
    public static String parseMimeType(Element rangeSet) throws WCSException {
        //get the File element
        Elements file = rangeSet.getChildElements(XMLSymbols.LABEL_FILE, XMLSymbols.NAMESPACE_GML);
        if(file.size() != 1){
            throw new WCSTWrongNumberOfFileElements();
        }
        //get the fileReference
        Elements mimetype = file.get(0).getChildElements(XMLSymbols.LABEL_FILE_STRUCTURE, XMLSymbols.NAMESPACE_GML);
        if(mimetype.size() != 1){
            throw new WCSTWrongNumberOfFileStructureElements();
        }
        return mimetype.get(0).getValue().trim();
    }

    /**
     * Returns the rangeParameters content as String if the elent exist, null otherwise.
     * @param rangeSet
     * @return
     */
    public static String parseRangeParameters(Element rangeSet){
        Elements rangeParameters = rangeSet.getChildElements(XMLSymbols.LABEL_RANGEPARAMETERS, XMLSymbols.NAMESPACE_GML);
        if(rangeParameters.size() == 1){
            return rangeParameters.get(0).getValue().trim();
        }
        else {
            //rangePrameters might be missing
            return "";
        }
    }

    /**
     * Parses the file reference form a GML coverage.
     * @param rangeSet
     * @return
     * @throws petascope.exceptions.wcst.WCSTWrongNumberOfFileElements
     * @throws petascope.exceptions.wcst.WCSTWrongNumberOfFileReferenceElements
     * @throws WCSException
     */
    public static String parseFilePath(Element rangeSet)
            throws WCSTWrongNumberOfFileElements, WCSTWrongNumberOfFileReferenceElements, WCSException {
        //get the File element
        Elements file = rangeSet.getChildElements(XMLSymbols.LABEL_FILE, XMLSymbols.NAMESPACE_GML);
        if(file.size() != 1){
            throw new WCSTWrongNumberOfFileElements();
        }
        //get the fileReference
        Elements fileName = file.get(0).getChildElements(XMLSymbols.LABEL_FILE_REFERENCE, XMLSymbols.NAMESPACE_GML);
        if(fileName.size() != 1){
            throw new WCSTWrongNumberOfFileReferenceElements();
        }

        return fileName.get(0).getValue().trim();
    }

    /**
     * Parses a GML tuple list into a rasdaman constant.
     *
     * @param dataBlock the dataBlock element
     * @param cellDomains the cellDomains of the coverage
     * @param typeSuffix the suffix to be added to each point to indicate its
     * rasdaman type (i.e. rasdaman Char 1 world be 1c, so the suffix is c)
     * @return String representation of a rasdaman constant
     * @throws petascope.exceptions.wcst.WCSTWrongNumberOfPixels
     * @throws petascope.exceptions.wcst.WCSTWrongNumberOfTupleLists
     * @throws WCSException
     */
    public static String parseGMLTupleList(Element dataBlock, List<CellDomainElement> cellDomains, String typeSuffix)
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
        //get the points
        String[] points = values.split(ts);
        String interval = "";
        String rasdamanValues = "";
        //iterate through each dimension and add points to the rasdaman constant
        //cells in the last dimension are separated by ,, all others by ;
        int totalNumberOfPoints = 1;
        int innerMostDimensionSize = 1;
        for (CellDomainElement dimension : cellDomains) {
            totalNumberOfPoints *= (dimension.getHiInt() - dimension.getLoInt() + 1);
            interval += dimension.getLo() + RASDAMAN_INTERVAL_HILO_SEP + dimension.getHi();
            //if not last, add sep
            if (dimension.getOrder() != cellDomains.size() - 1) {
                interval += RASDAMAN_INTERVAL_DIM_SEP;
            } else {
                //compute size of last dim
                innerMostDimensionSize = dimension.getHiInt() - dimension.getLoInt() + 1;
            }
        }
        if (totalNumberOfPoints != points.length) {
            throw new WCSTWrongNumberOfPixels();
        }
        //iterate through all points
        for (int i = 0; i < totalNumberOfPoints; i++) {
            rasdamanValues += parsePointValue(points[i], cs, typeSuffix);
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

        return TEMPLATE_RASDAMAN_CONSTANT.replace(TOKEN_INTERVAL, interval)
                .replace(TOKEN_VALUES, rasdamanValues);
    }

    /**
     * Helper for parsing a point value.
     *
     * @param point
     * @param separator
     * @param suffix
     * @return
     */
    private static String parsePointValue(String point, String separator, String suffix) {
        //multiband image
        if (point.contains(separator)) {
            String[] points = point.split(separator);
            String pointValues = "";
            for (String val : points) {
                pointValues += val + suffix + RASDAMAN_VALUES_CELL_SEP;
            }
            //remove the last separator
            pointValues = pointValues.substring(0, pointValues.length() - 1);
            return TEMPLATE_RASDAMAN_STRUCTURE.replace(TOKEN_STRUCTURE_CELL_VAL, pointValues);
        }
        //single band
        return point + suffix;
    }

    /**
     * Parses gml:metadata elements.
     * @param root
     * @return
     */
    public static String parseExtraMetadata(Element root){
        String ret = "";
        Elements metadata = root.getChildElements(XMLSymbols.LABEL_METADATA, XMLSymbols.NAMESPACE_GMLCOV);
        if(metadata.size() > 0 && metadata.get(0).getChildCount() > 0){
            //since the node can contain xml sometimes, json other times, we need to return
            //the actual content of the node as string.

            //the string representation of the node
            for(int i = 0; i < metadata.get(0).getChildCount(); i++){
                ret += metadata.get(0).getChild(i).toXML();
            }
        }
        return ret;
    }

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
    private static final String DEFAULT_DATATYPE = "Byte";
}
