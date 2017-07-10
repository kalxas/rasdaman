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
import petascope.exceptions.PetascopeException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.apache.commons.lang3.math.NumberUtils.isNumber;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.IrregularAxis;
import org.rasdaman.domain.cis.NilValue;
import org.rasdaman.repository.service.CoverageRepostioryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.NumericTrimming;
import petascope.wcps2.metadata.model.RangeField;
import petascope.wcps2.metadata.model.RegularAxis;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.service.AxesOrderComparator;

/**
 * This class will build parameters for encoding in NetCDF
 *
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class NetCDFParametersFactory {

    @Autowired
    private CoverageRepostioryService persistedCoverageService;

    //this key is used when the coverage metadata is not an object (xml, json) so the contents is passed as an object
    //having a single property, with this key    
    @Autowired
    private CovToCFTranslationService covToCFTranslationService;

    public NetCDFParametersFactory() {

    }

    public NetCDFExtraParams getParameters(WcpsCoverageMetadata metadata) throws PetascopeException {
        // NOTE: this needs to write with grid axis order
        List<String> dimensions = this.getDimensions(metadata.getSortedAxesByGridOrder());
        List<Variable> vars = this.getVariables(metadata);
        // variables in JSON uses as a Map: { "variableName": { object }, "variableName1": { object1 }, .... }
        Map<String, Variable> variables = new LinkedHashMap<>();
        for (Variable var : vars) {
            variables.put(var.getName(), var);
        }

        NetCDFExtraParams netCDFExtraParams = new NetCDFExtraParams(dimensions, variables);
        return netCDFExtraParams;
    }

    private List<String> getDimensions(List<Axis> axes) {
        List<String> dimensions = new ArrayList<>();
        // NOTE: crs axes here must be used as grid axis order
        //sort the axes after the rasdaman order
        Collections.sort(axes, new AxesOrderComparator());
        for (Axis axis : axes) {
            dimensions.add(axis.getLabel());
        }
        return dimensions;
    }

    private List<DimensionVariable> getDimensionVariables(String covName, List<Axis> axes) throws PetascopeException {
        List<DimensionVariable> dimensionVariables = new ArrayList<>();
        for (Axis axis : axes) {
            DimensionVariableMetadata metadata = getDimensionVariableMetadata(axis);
            dimensionVariables.add(new DimensionVariable<>(RangeField.DATA_TYPE, this.getPoisitionData(covName, axis), axis.getLabel(), metadata));
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
        List<BandVariable> bandVariables = new ArrayList<>();
        for (RangeField band : bands) {
            bandVariables.add(new BandVariable(band.getDataType(), band.getName(), new BandVariableMetadata(band.getDescription(), parseNodataValues(band.getNodata()),
                    band.getUomCode(), band.getDefinition())));
        }
        return bandVariables;
    }

    private List<Variable> getVariables(WcpsCoverageMetadata metadata) throws PetascopeException {
        List<Variable> variables = new ArrayList<>();
        // NOTE: this needs to write with grid axis order
        variables.addAll(this.getDimensionVariables(metadata.getCoverageName(), metadata.getSortedAxesByGridOrder()));
        variables.addAll(this.getBandVariables(metadata.getRangeFields()));
        return variables;
    }

    private List<Double> getPoisitionData(String covName, Axis axis) throws PetascopeException {
        Coverage coverage = this.persistedCoverageService.readCoverageByIdFromCache(covName);

        // data=[geoLow, geoLow+res, geoLow+2*res, ...., geoHigh]
        List<Double> data = new ArrayList<>();
        if (axis.getGeoBounds() instanceof NumericTrimming) {
            BigDecimal resolution = axis.getResolution();

            BigDecimal geoDomMin = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
            BigDecimal geoDomMax = ((NumericTrimming) axis.getGeoBounds()).getUpperLimit();
            BigDecimal gridDomMin = ((NumericTrimming) axis.getGridBounds()).getLowerLimit();
            BigDecimal gridDomMax = ((NumericTrimming) axis.getGridBounds()).getUpperLimit();

            int numberOfPoints = gridDomMax.subtract(gridDomMin).intValueExact() + 1;
            if (axis instanceof RegularAxis) {
                RegularAxis regularAxis = (RegularAxis) axis;
                BigDecimal coord = BigDecimal.ZERO;
                // Write the step values for each axis as an array of values in netCDF's variables
                for (int i = 0; i < numberOfPoints; i++) {
                    double offset = resolution.multiply(new BigDecimal(i)).doubleValue();
                    // positive axis (e.g: Long) so step values from min -> max (e.g: -180, -150, -120,..., 120, 150, 180)
                    if (regularAxis.getResolution().compareTo(BigDecimal.ZERO) > 0) {
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
                // Only supports GeneralGridCoverage now and this axis should be irregular axis
                GeoAxis geoAxis = ((GeneralGridCoverage) coverage).getGeoAxisByName(axis.getLabel());
                IrregularAxis irregularAxis = ((IrregularAxis) geoAxis);

                List<BigDecimal> coefficients = irregularAxis.getAllCoefficientsInInterval(coeffMin, coeffMax);

                for (BigDecimal coefficient : coefficients) {
                    BigDecimal coord = axis.getOrigin().add(coefficient.multiply(resolution));
                    data.add(coord.doubleValue());
                }
            }
        }

        return data;
    }

    private List<BigDecimal> parseNodataValues(List<NilValue> nullValues) {
        List<BigDecimal> result = new ArrayList<BigDecimal>();
        for (NilValue nullValue : nullValues) {
            String value = nullValue.getValue();
            if (isNumber(value)) {
                result.add(new BigDecimal(value));
            }
        }

        return result;
    }
}
