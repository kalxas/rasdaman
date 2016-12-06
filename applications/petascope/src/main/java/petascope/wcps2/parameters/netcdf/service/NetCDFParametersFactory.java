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
package petascope.wcps2.parameters.netcdf.service;

import petascope.wcps2.parameters.model.netcdf.BandVariableMetadata;
import petascope.wcps2.parameters.model.netcdf.DimensionVariable;
import petascope.wcps2.parameters.model.netcdf.NetCDFExtraParams;
import petascope.wcps2.parameters.model.netcdf.DimensionVariableMetadata;
import petascope.wcps2.parameters.model.netcdf.Variable;
import petascope.wcps2.parameters.model.netcdf.BandVariable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import petascope.core.DbMetadataSource;
import petascope.exceptions.PetascopeException;
import petascope.wcps2.metadata.model.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class will build parameters for encoding in NetCDF
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class NetCDFParametersFactory {

    //this key is used when the coverage metadata is not an object (xml, json) so the contents is passed as an object
    //having a single property, with this key
    private final DbMetadataSource dbMetadataSource;
    private final CovToCFTranslationService covToCFTranslationService;

    public NetCDFParametersFactory(XmlMapper xmlMapper, ObjectMapper objectMapper, DbMetadataSource dbMetadataSource,
                                   CovToCFTranslationService covToCFTranslationService) {
        
        this.dbMetadataSource = dbMetadataSource;
        this.covToCFTranslationService = covToCFTranslationService;
    }

    public NetCDFExtraParams getParameters(WcpsCoverageMetadata metadata) throws PetascopeException {
        List<String> dimensions = this.getDimensions(metadata.getAxes());
        List<Variable> vars = this.getVariables(metadata);
        // variables in JSON uses as a Map: { "variableName": { object }, "variableName1": { object1 }, .... }
        Map<String, Variable> variables = new LinkedHashMap<String, Variable>();
        for (Variable var:vars) {
            variables.put(var.getName(), var);
        }
        
        NetCDFExtraParams netCDFExtraParams = new NetCDFExtraParams(dimensions, variables);
        return netCDFExtraParams;
    }

    private List<String> getDimensions(List<Axis> axes) {
        List<String> dimensions = new ArrayList<String>();
        for (Axis axis : axes) {
            dimensions.add(axis.getLabel());
        }
        return dimensions;
    }

    private List<DimensionVariable> getDimensionVariables(String covName, List<Axis> axes) throws PetascopeException {
        List<DimensionVariable> dimensionVariables = new ArrayList<DimensionVariable>();
        for (Axis axis : axes) {
            DimensionVariableMetadata metadata = getDimensionVariableMetadata(axis);
            dimensionVariables.add(new DimensionVariable<Double>("double", this.getPoisitionData(covName, axis), axis.getLabel(), metadata));
        }
        return dimensionVariables;
    }

    private DimensionVariableMetadata getDimensionVariableMetadata(Axis axis) {
        String standardName = covToCFTranslationService.getStandardName(axis.getLabel());
        String unitOfMeasure = covToCFTranslationService.getUnitOfMeasure(axis.getLabel(), axis.getAxisUoM());
        String axisType = covToCFTranslationService.getAxisType(axis.getLabel(), axis.getAxisType());
        return new DimensionVariableMetadata(standardName, unitOfMeasure, axisType);
    }

    private List<BandVariable> getBandVariables(List<RangeField> bands) {
        List<BandVariable> bandVariables = new ArrayList<BandVariable>();
        for (RangeField band : bands) {
            bandVariables.add(new BandVariable(band.getType(), band.getName(), new BandVariableMetadata(band.getDescription(), band.getNodata(),
                                               band.getUom(), band.getDefinition())));
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

                        // NOTE: as netCDF point's coordinate is in the middle of pixel, so the coordinate will be shiftted to half positive pixel
                        // e.g: resolution is: -0.42, pixel coordinate is: -80, -79.958, then point is: -80 +(0.42/2) = -79.979
                        coord = coord.add(resolution.divide(new BigDecimal(2)).abs());
                    } else {
                        // negative axis (e.g: Lat) so step values from max -> min (e.g: 90, 80, 70,...-70, -80, -90)
                        coord = geoDomMax.subtract(new BigDecimal(Math.abs(offset)));

                        // NOTE: as netCDF point's coordinate is in the middle of pixel, so the coordinate will be shiftted to half negative pixel
                        // e.g: resolution is: -0.42, pixel coordinate is: -89, -89.042, then point is: -89 -(0.42/2) = -89.0.21
                        coord = coord.subtract(resolution.divide(new BigDecimal(2)).abs());
                    }


                    data.add(coord.doubleValue());
                }
            } else {
                //Irregular axis, need to add the coefficients into the calculation
                BigDecimal coeffMin = geoDomMin.subtract(axis.getOrigin()).divide(resolution);
                BigDecimal coeffMax = geoDomMax.subtract(axis.getOrigin()).divide(resolution);
                List<BigDecimal> coefficients = dbMetadataSource.getCoefficientsOfInterval(covName, axis.getRasdamanOrder(), coeffMin, coeffMax);
                for (BigDecimal coefficient : coefficients) {
                    BigDecimal coord = axis.getOrigin().add(coefficient.multiply(resolution));
                    data.add(coord.doubleValue());
                }
            }
        }

        return data;
    }
}
