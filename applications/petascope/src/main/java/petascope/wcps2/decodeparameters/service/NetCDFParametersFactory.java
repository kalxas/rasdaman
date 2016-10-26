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
package petascope.wcps2.decodeparameters.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import petascope.core.DbMetadataSource;
import petascope.exceptions.PetascopeException;
import petascope.util.AxisTypes;
import petascope.wcps2.decodeparameters.model.*;
import petascope.wcps2.metadata.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class NetCDFParametersFactory {

    //this key is used when the coverage metadata is not an object (xml, json) so the contents is passed as an object
    //having a single property, with this key
    private final static String METADATA_STRING_KEY = "metadata";
    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;
    private final DbMetadataSource dbMetadataSource;
    private final CovToCFTranslationService covToCFTranslationService;

    public NetCDFParametersFactory(XmlMapper xmlMapper, ObjectMapper objectMapper, DbMetadataSource dbMetadataSource,
                                   CovToCFTranslationService covToCFTranslationService) {
        this.xmlMapper = xmlMapper;
        this.objectMapper = objectMapper;
        this.dbMetadataSource = dbMetadataSource;
        this.covToCFTranslationService = covToCFTranslationService;
    }

    public NetCDFExtraParams getParameters(WcpsCoverageMetadata metadata) throws PetascopeException {
        List<String> dimensions = this.getDimensions(metadata.getAxes());
        List<Variable> variables = this.getVariables(metadata);
        String extraMetdata = metadata.getMetadata();
        GeoReference geoReference = getGeoreference(metadata);
        NetCDFExtraParams netCDFExtraParams = new NetCDFExtraParams(dimensions, variables, metadata.getNodata(), convertExtraMetadata(extraMetdata), geoReference);
        return netCDFExtraParams;
    }

    private GeoReference getGeoreference(WcpsCoverageMetadata metadata){
        GeoReference result = null;
        String crs = "";
        BoundingBox boundingBox = new BoundingBox();
        for(Axis axis: metadata.getAxes()){
            //for now we only add the Lat / Long axes
            if(axis.getAxisType().equals(AxisTypes.X_AXIS)){
                crs = axis.getCrsDefinition().getAuthority() + ":" + axis.getCrsDefinition().getCode();
                boundingBox.setXMin(((NumericTrimming)axis.getGeoBounds()).getLowerLimit().doubleValue());
                boundingBox.setXMax(((NumericTrimming)axis.getGeoBounds()).getUpperLimit().doubleValue());
            }
            else if(axis.getAxisType().equals(AxisTypes.Y_AXIS)){
                boundingBox.setYMin(((NumericTrimming)axis.getGeoBounds()).getLowerLimit().doubleValue());
                boundingBox.setYMax(((NumericTrimming)axis.getGeoBounds()).getUpperLimit().doubleValue());
            }
        }
        result = new GeoReference(crs, boundingBox);
        return result;
    }

    private Map<String, String> convertExtraMetadata(String extraMetadata){
        Map<String, String> convertedMetadata = null;
        //remove the slices and the gmlcov:metadata closing tag if it exists
        extraMetadata = removeMetadataSlices(extraMetadata).replace("<gmlcov:metadata />", "");
        //convert to object
        try {
            //find out the type
            if (extraMetadata.startsWith("<")) {
                //xml
                //the contents that the xmlMapper can read into a map must currently come from inside an outer tag, which is ignored
                //so we are just adding them
                convertedMetadata = xmlMapper.readValue(OUTER_TAG_START + extraMetadata + OUTER_TAG_END, Map.class);
            } else if (extraMetadata.startsWith("{")) {
                //json
                convertedMetadata = objectMapper.readValue(extraMetadata, new TypeReference<Map<String, String>>() {});
            }
        } catch (IOException e){
            //failed, just give it as string
            convertedMetadata = new HashMap<String, String>();
            convertedMetadata.put(METADATA_STRING_KEY, extraMetadata);
        }
        return convertedMetadata;
    }

    private String removeMetadataSlices(String extraMetadata){
        String result = "";
        //remove all \n and double spaces
        extraMetadata = extraMetadata.replaceAll("\\s+", " ");
        if(extraMetadata.contains("<slices>") && extraMetadata.contains("</slices>")){
            //xml
            String[] extraMetadataParts = extraMetadata.split("<slices>");
            String begin = extraMetadataParts[0];
            String[] endParts = extraMetadataParts[1].split("</slices>");
            String end = endParts[endParts.length - 1];
            result = begin + end;
        }
        else if(extraMetadata.contains("<slices />")){
            //xml, no slices
            result = extraMetadata.replace("<slices />", "");
        }
        else if(extraMetadata.contains("{ \"slices\":")){
            //json with slices as first element
            String[] extraMetadataParts = extraMetadata.split("\"slices\":");
            String begin = extraMetadataParts[0];
            String[] endParts = extraMetadataParts[1].split("],");
            String end = endParts[1];
            result = begin + end;
        }
        else if(extraMetadata.contains(", \"slices\"")){
            //json, slices not first
            String[] extraMetadataParts = extraMetadata.split(", \"slices\":");
            String begin = extraMetadataParts[0];
            String[] endParts = extraMetadataParts[1].split("]");
            String end = endParts[1];
            result = begin + end;
        }
        else {
            //default
            result = extraMetadata;
        }
        return result;
    }

    private List<String> getDimensions(List<Axis> axes) {
        List<String> dimensions = new ArrayList<String>();
        for (Axis axis:axes) {
            dimensions.add(axis.getLabel());
        }
        return dimensions;
    }

    private List<DimensionVariable> getDimensionVariables(String covName, List<Axis> axes) throws PetascopeException {
        List<DimensionVariable> dimensionVariables = new ArrayList<DimensionVariable>();
        for (Axis axis:axes) {
            DimensionVariableMetadata metadata = getDimensionVariableMetadata(axis);
            dimensionVariables.add(new DimensionVariable<Double>("double", this.getPoisitionData(covName, axis), axis.getLabel(), metadata));
        }
        return dimensionVariables;
    }

    private DimensionVariableMetadata getDimensionVariableMetadata(Axis axis){
        String standardName = covToCFTranslationService.getStandardName(axis.getLabel());
        String unitOfMeasure = covToCFTranslationService.getUnitOfMeasure(axis.getLabel(), axis.getAxisUoM());
        String axisType = covToCFTranslationService.getAxisType(axis.getLabel(), axis.getAxisType());
        return new DimensionVariableMetadata(standardName, unitOfMeasure, axisType);
    }

    private List<BandVariable> getBandVariables(List<RangeField> bands) {
        List<BandVariable> bandVariables = new ArrayList<BandVariable>();
        for (RangeField band:bands) {
            bandVariables.add(new BandVariable(band.getType(), new BandVariableMetadata(band.getDescription(), band.getNodata(),
                    band.getUom(), band.getDefinition()), band.getName()));
        }
        return bandVariables;
    }

    private List<Variable> getVariables(WcpsCoverageMetadata metadata) throws PetascopeException {
        List<Variable> variables = new ArrayList<Variable>();
        variables.addAll(this.getDimensionVariables(metadata.getCoverageName(), metadata.getAxes()));
        variables.addAll(this.getBandVariables(metadata.getRangeFields()));
        return variables;
    }

    private List<Double> getPoisitionData(String covName, Axis axis) throws PetascopeException {
        // data=[geoLow, geoLow+res, geoLow+2*res, ...., geoHigh]
        List<Double> data = new ArrayList<Double>();
        if (axis.getGeoBounds() instanceof NumericTrimming) {
            BigDecimal resolution = axis.getScalarResolution();

            BigDecimal geoDomMin = ((NumericTrimming)axis.getGeoBounds()).getLowerLimit();
            BigDecimal geoDomMax = ((NumericTrimming)axis.getGeoBounds()).getUpperLimit();
            BigDecimal gridDomMin = ((NumericTrimming)axis.getGridBounds()).getLowerLimit();
            BigDecimal gridDomMax = ((NumericTrimming)axis.getGridBounds()).getUpperLimit();

            int numberOfPoints = gridDomMax.subtract(gridDomMin).intValueExact() + 1;
            if (axis instanceof RegularAxis) {
                RegularAxis regularAxis = (RegularAxis)axis;
                BigDecimal coord = BigDecimal.ZERO;
                // Write the step values for each axis as an array of values in netCDF's variables
                for (int i = 0; i < numberOfPoints; i++) {
                    double offset = resolution.multiply(new BigDecimal(i)).doubleValue();
                    // positive axis (e.g: Long) so step values from min -> max (e.g: -180, -150, -120,..., 120, 150, 180)
                    if (regularAxis.getScalarResolution().compareTo(BigDecimal.ZERO) > 0) {
                        coord = geoDomMin.add(new BigDecimal(Math.abs(offset)));
                    } else {
                        // negative axis (e.g: Lat) so step values from max -> min (e.g: 90, 80, 70,...-70, -80, -90)
                        coord = geoDomMax.subtract(new BigDecimal(Math.abs(offset)));
                    }

                    data.add(coord.doubleValue());
                }
            }
            else {
                //Irregular axis, need to add the coefficients into the calculation
                BigDecimal coeffMin = geoDomMin.subtract(axis.getOrigin()).divide(resolution);
                BigDecimal coeffMax = geoDomMax.subtract(axis.getOrigin()).divide(resolution);
                List<BigDecimal> coefficients = dbMetadataSource.getCoefficientsOfInterval(covName, axis.getRasdamanOrder(), coeffMin, coeffMax);
                for(BigDecimal coefficient : coefficients){
                    BigDecimal coord = axis.getOrigin().add(coefficient.multiply(resolution));
                    data.add(coord.doubleValue());
                }
            }
        }

        return data;
    }

    private final static String OUTER_TAG_START = "<metadata>";
    private final static String OUTER_TAG_END = "</metadata>";
}
