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
package petascope.wcps.parameters.netcdf.service;

import petascope.wcps.parameters.model.netcdf.BandVariableMetadata;
import petascope.wcps.parameters.model.netcdf.DimensionVariable;
import petascope.wcps.parameters.model.netcdf.NetCDFExtraParams;
import petascope.wcps.parameters.model.netcdf.DimensionVariableMetadata;
import petascope.wcps.parameters.model.netcdf.Variable;
import petascope.wcps.parameters.model.netcdf.BandVariable;
import petascope.exceptions.PetascopeException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.wcps.encodeparameters.model.AxesMetadata;
import petascope.wcps.encodeparameters.model.BandsMetadata;
import petascope.core.gml.metadata.model.CoverageMetadata;
import petascope.core.gml.metadata.model.GridMapping;
import petascope.exceptions.ExceptionCode;
import petascope.util.BigDecimalUtil;
import petascope.util.StringUtil;
import petascope.util.TimeUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.IrregularAxis;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.RegularAxis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.AxesOrderComparator;
import ucar.units.*;

/**
 * This class will build all parameters for encoding in NetCDF
 *
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class NetCDFParametersService {
    
    // NOTE: java-doc for udunits: https://repo1.maven.org/maven2/edu/ucar/udunits/4.5.5/udunits-4.5.5-javadoc.jar
    final static UnitFormat format = UnitFormatManager.instance();
    final static DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(NetCDFParametersService.class);

    public NetCDFParametersService() {

    }

    /**
     * Build all the parameters for netCDF encoding.
     * @param metadata
     * @return
     * @throws PetascopeException 
     */
    public NetCDFExtraParams buildParameters(WcpsCoverageMetadata metadata) throws PetascopeException {
        // NOTE: this needs to write with grid axis order
        List<String> dimensions = this.buildDimensions(metadata.getSortedAxesByGridOrder());
        List<Variable> vars = this.buildVariables(metadata);
        // variables in JSON uses as a Map: { "variableName": { object }, "variableName1": { object1 }, .... }
        Map<String, Variable> variables = new LinkedHashMap<>();
        for (Variable var : vars) {
            variables.put(var.getName(), var);
        }

        NetCDFExtraParams netCDFExtraParams = new NetCDFExtraParams(dimensions, variables);
        
        return netCDFExtraParams;
    }

    /**
     * Build the dimensions parameter of netCDF encoding, e.g:
     * \"dimensions\":[\"i\",\"j\"]
     * @param axes
     * @return 
     */
    private List<String> buildDimensions(List<Axis> axes) {
        List<String> dimensions = new ArrayList<>();
        // NOTE: crs axes here must be used as grid axis order
        //sort the axes after the rasdaman order
        Collections.sort(axes, new AxesOrderComparator());
        for (Axis axis : axes) {
            dimensions.add(axis.getLabel());
        }
        return dimensions;
    }

    /**
     * Build the dimensions's variables parameters of netCDF encoding, e.g:
     * \"variables\":{\"i\":{\"type\":\"double\",\"data\":[0.5],\"name\":\"i\",\"metadata\":{\"standard_name\":\"i\",\"units\":\"GridSpacing\",\"axis\":\"X\"}}
     *               ,\"j\":{\"type\":\"double\",\"data\":[-0.5],\"name\":\"j\",\"metadata\":{\"standard_name\":\"j\",\"units\":\"GridSpacing\",\"axis\":\"Y\"}}
     * @param covName
     * @param axes
     * @return
     * @throws PetascopeException 
     */
    private List<DimensionVariable> buildDimensionVariables(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        List<Axis> axes = wcpsCoverageMetadata.getSortedAxesByGridOrder();        
        List<DimensionVariable> dimensionVariables = new ArrayList<>();
                        
        // First get all the metadata of coverage
        CoverageMetadata coverageMetadata = wcpsCoverageMetadata.getCoverageMetadata();
        
        AxesMetadata axesMetadata = coverageMetadata.getAxesMetadata();
        
        GridMapping gridMappingVariable = wcpsCoverageMetadata.getCoverageMetadata().getGridMapping();
        
        if (gridMappingVariable != null) {
            // NOTE: In case, this coverage is rotated grid mapping, then it needs to add the grid mapping variable in encode() netCDF
            // e.g. rotated_pole
            String gridMappingName = gridMappingVariable.getGlobalAttributesMap().get(GridMapping.IDENTIFIER);
            dimensionVariables.add(new DimensionVariable("int", new ArrayList<>(), gridMappingName, 
                                  new DimensionVariableMetadata(gridMappingVariable.getGlobalAttributesMap())));
        }
        
        for (Axis axis : axes) {
            Map<String, String> axesMetadataMap = null;
            
            if (axesMetadata != null) {
                // Axes's metadata exists in coverage's metadata
                for (Map.Entry<String, Map<String, String>> axisAttribute : axesMetadata.getAxesAttributesMap().entrySet()) {
                    if (axis.getLabel().equals(axisAttribute.getKey())) {
                        axesMetadataMap = axisAttribute.getValue();
                        break;
                    }
                }
            }
            DimensionVariableMetadata dimensionVariableMetadata = new DimensionVariableMetadata(axesMetadataMap);
            dimensionVariables.add(new DimensionVariable<>(RangeField.DATA_TYPE, this.buildPoisitionData(axis, coverageMetadata), axis.getLabel(), dimensionVariableMetadata));
        }
        return dimensionVariables;
    }

    
    /**
     * Build the band (range field) parameter of netCDF encoding, e.g:
     * NOTE: band's metadata is combined from swe:range element and bands element (if exist) in gmlcov:metata (i.e: coverage's metadata).
     * \"value\":{\"type\":\"unsigned char\",\"name\":\"value\",\"metadata\":{\"units\":\"10^0\", \"another_value\":\"25.565\"}}
     * @param wcpsCoverageMetadata
     * @return 
     */
    private List<BandVariable> buildBandVariables(WcpsCoverageMetadata wcpsCoverageMetadata) {
        List<BandVariable> bandVariables = new ArrayList<>();
        List<RangeField> bands = wcpsCoverageMetadata.getRangeFields();
        // NOTE: There are 2 types of bands's metadata (the mandatory one from swe:element of swe:field and the option one from coverage's metadata bands element if exists)        
        CoverageMetadata coverageMetadata = wcpsCoverageMetadata.getCoverageMetadata();
        BandsMetadata bandsMetadata = coverageMetadata.getBandsMetadata();       
       
        for (RangeField band : bands) {            
            Map<String, String> bandsMetadataMap = null;
            
            if (bandsMetadata != null) {
                // Bands's metadata exists in coverage's metadata
                for (Map.Entry<String, Map<String, String>> bandAttribute : bandsMetadata.getBandsAttributesMap().entrySet()) {
                    if (band.getName().equals(bandAttribute.getKey())) {
                        bandsMetadataMap = bandAttribute.getValue();
                        break;
                    }
                }
            }
            
            BandVariableMetadata bandVariableMetadata = new BandVariableMetadata(band.getDescription(), 
                                                                                 band.getUomCode(), band.getDefinition(), bandsMetadataMap);
            bandVariables.add(new BandVariable(band.getDataType(), band.getName(), bandVariableMetadata));
        }
        return bandVariables;
    }

    /**
     * Build all the possible variables (dimension and band)
     * @param metadata
     * @return
     * @throws PetascopeException 
     */
    private List<Variable> buildVariables(WcpsCoverageMetadata metadata) throws PetascopeException {
        List<Variable> variables = new ArrayList<>();
        // NOTE: this needs to write with grid axis order
        variables.addAll(this.buildDimensionVariables(metadata));
        variables.addAll(this.buildBandVariables(metadata));
        return variables;
    }

    /**
     * Build the position data for each dimension, e.g:
     * \"Long\":{\"type\":\"double\",\"data\":[10.0,10.5,11.0,11.5,12.0,12.5,13.0,13.5,14.0,14.5,15.0,15.5,16.0,16.5,17.0,17.5,18.0,18.5,19.0,19.5,20.0]
     * NOTE: nagative axis like Lat will write max to min values (origin is the max value)
     */
    private List<Double> buildPoisitionData(Axis axis, CoverageMetadata coverageMetadata) throws PetascopeException {

        // data=[geoLow, geoLow+res, geoLow+2*res, ...., geoHigh]
        List<Double> result = new ArrayList<>();
        if (axis.getGeoBounds() instanceof NumericTrimming) {
            BigDecimal resolution = axis.getResolution();

            BigDecimal geoDomMin = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
            BigDecimal geoDomMax = ((NumericTrimming) axis.getGeoBounds()).getUpperLimit();
            
            Pair<BigDecimal, BigDecimal> pair = BigDecimalUtil.swapIfFirstLarger(geoDomMin, geoDomMax);
            geoDomMin = pair.fst;
            geoDomMax = pair.snd;
            
            
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

                    result.add(coord.doubleValue());
                }
            } else {
                IrregularAxis irregularAxis = ((IrregularAxis)axis);
                
                BigDecimal origin = irregularAxis.getCoefficientZeroBoundNumberFromOriginalDirectPositions();
                
                for (BigDecimal coefficient : irregularAxis.getDirectPositions()) {
                    BigDecimal coord = origin.add(coefficient.multiply(resolution));
                    result.add(coord.doubleValue());
                }
            }
            
            if (axis.isTimeAxis()) {
                // Only do that, when time axis has valid "units" and "calendar" in coverage's global metadata
                result = this.adjustTimeValuesByUnitsAndCalendar(axis, result, coverageMetadata);
            }
        }

        return result;
    }
    
    /**
     * In case, time axis has metadata about "units" and "calendar", then the output values must be adjusted
     * according do the time origin specified inside the metadata.
     * 
     * doc: https://cfconventions.org/Data/cf-conventions/cf-conventions-1.9/cf-conventions.html#time-coordinate
     */
    private List<Double> adjustTimeValuesByUnitsAndCalendar(Axis timeAxis, List<Double> values, CoverageMetadata coverageMetadata) throws PetascopeException {
        List<Double> result = values;
        // e.g. "hours since 2016-12-01 00:00:00" and "proleptic_gregorian"
        Pair<String, String> unitsCalendarPair = this.getTimeUnitsCalendarFromAxisMetadata(timeAxis.getLabel(), coverageMetadata);
        
        if (unitsCalendarPair != null) {
            String units = unitsCalendarPair.fst;
            // e.g. hours / seconds
            String timeUnit = units.split(" ")[0];
            
            TimeScaleUnit timeScaleUnit = null;
            Unit unit1 = null;
            Unit unit2 = null;
            
            try {
                // e.g. "hours since 2016-12-01 00:00:00"
                timeScaleUnit = (TimeScaleUnit) format.parse(units);
                long axisTimeOriginEpochMilli = timeScaleUnit.getOrigin().toInstant().toEpochMilli();

                unit1 = format.parse("Milliseconds");
                unit2 = format.parse(timeUnit);
                
                for (int i = 0; i < values.size(); i++) {
                    Double value = values.get(i);
                    // e.g. 2015-01-01                    
                    String dateTime = StringUtil.stripQuotes(TimeUtil.valueToISODateTime(BigDecimal.ZERO, 
                                                                  new BigDecimal(String.valueOf(value)), timeAxis.getCrsDefinition()));
                    long dateTimeValueEpochMili = Instant.from(timeFormatter.parse(dateTime)).toEpochMilli();
                    
                    long diffMilli = dateTimeValueEpochMili - axisTimeOriginEpochMilli;
                    
                    // Here, the time value is adjusted based on the "units" of time axis specified in the netCDF
                    double convertedValue = unit1.getConverterTo(unit2).convert(diffMilli);
                    
                    values.set(i, convertedValue);
                }
            } catch (Exception ex) {
                log.warn("Cannot parse and adjust output time values when encoding to netCDF, based on time units: " + units 
                                             + ". Reason: " + ex.getMessage(), ex);
            }
        }
        
        
        return result;
    }

    /**
     * Check if coverage contains valid metadata for time axis to adjust, based on cf-convention.
     */
    private Pair<String, String> getTimeUnitsCalendarFromAxisMetadata(String inputAxisLabel, CoverageMetadata coverageMetadata) {
        for (Map.Entry<String, Map<String, String>> entry : coverageMetadata.getAxesMetadata().getAxesAttributesMap().entrySet()) {
            // axis -> map of (key, value)
            String axisLabel = entry.getKey();
            
            if (axisLabel.equals(inputAxisLabel)) {
                String units = null;
                String calendar = null;
                    
                // iterate metadata of an axis
                for (Map.Entry<String, String> metadataEntry : entry.getValue().entrySet()) {
                    String key = metadataEntry.getKey();
                    String value = metadataEntry.getValue();
                    
                    if (key.equals("units")) {
                        units = value;
                    } else if (key.equals("calendar")) {
                        calendar = value;
                    }
                    
                    if (units != null && calendar != null) {
                        // doc: https://cfconventions.org/Data/cf-conventions/cf-conventions-1.9/cf-conventions.html#calendar
                        // NOTE: only supports 2 calendars
                        if (calendar.equals("standard") || calendar.equals("proleptic_gregorian")) {
                            return new Pair<>(units, calendar);
                        }
                        
                        log.warn("NetCDF calendar not supported: " + calendar);
                        return null;
                    }
                }
                
                return null;
            }
        }
        
        return null;
    }
}
