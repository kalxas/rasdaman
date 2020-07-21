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
package petascope.wms.handlers.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER;
import petascope.core.service.CrsComputerService;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.ParsedSubset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.CoordinateTranslationService;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;
import petascope.wcs2.parsers.subsets.TrimmingSubsetDimension;

/**
 * Service for parsing the subsets (e.g: time=‘2012-01-01T00:01:20Z, dim_pressure=20,...) from WMS GetMap request
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class WMSGetMapSubsetParsingService {
    
    @Autowired
    private WMSGetMapWCPSMetadataTranslatorService wmsGetMapWCPSMetadataTranslatorService;
    @Autowired
    private CoordinateTranslationService coordinateTranslationService;
    
    /**
     * From the input params time=..., elevation=..., dim_* (optional))
     * translate all these geo subsets to grid domains for all layers.
     * 
     * NOTE: if layer doesn't contain optional dimension subset, it is not an error from WMS 1.3 standard.
     * e.g: GetMap request with 2 layers, layers=layer_3D,layer_2D&time=... then the time subset only be translated on layer_3D.
     */
    public List<List<WcpsSliceSubsetDimension>> translateGridDimensionsSubsetsLayers(WcpsCoverageMetadata wcpsCoverageMetadata,
                                                                                Map<String, String> dimSubsetsMap) throws PetascopeException, SecoreException {
        // Each nested list is the translated grids for an non-XY axis (e.g: ansi -> [0, 3, 5])
        List<List<WcpsSliceSubsetDimension>> parsedNonXYAxesGridSlicings = new ArrayList<>();
        // First, parse all the dimension subsets (e.g: time=...,dim_pressure=....) as one parsed dimension subset is one of layer's overlay operator's operand.

        // First, convert all the input dimensions subsets to BigDecimal to be translated to grid subsets
        // e.g: time="2015-02-05"
        for (Axis axis : wcpsCoverageMetadata.getSortedAxesByGridOrder()) {
            if (axis.isNonXYAxis()) {
                
                List<ParsedSubset<Long>> translatedGridSubsets = new ArrayList<>();

                // Parse the requested dimension subset values from GetMap request
                List<ParsedSubset<BigDecimal>> parsedGeoSubsets = new ArrayList<>();

                // e.g: time=...&dim_pressure=...
                String dimSubset = dimSubsetsMap.get(axis.getLabel());
                if (axis.isTimeAxis()) {
                    // In case axis is time axis, it has a specific key.
                    dimSubset = dimSubsetsMap.get(KVPSymbols.KEY_WMS_TIME);
                } else if (axis.isElevationAxis()) {
                    // In case axis is elevation axis, it has a specific key.
                    dimSubset = dimSubsetsMap.get(KVPSymbols.KEY_WMS_ELEVATION);
                }

                if (dimSubset != null) {
                    // Coverage contains a non XY, time dimension axis and there is a dim_axisLabel in GetMap request.
                    parsedGeoSubsets = this.parseDimensionSubset(axis, dimSubset);
                } else {
                    // NOTE: if coverage contains a non XY, time dimension (e.g: temperature) axis but there is no dim_temperature parameter from GetMap request
                    // it will be the upper Bound grid coordinate in this axis (the latest slice of this dimension according to WMS 1.3 document).
                    Long gridUpperBound = axis.getGridBounds().getUpperLimit().longValue();
                    translatedGridSubsets.add(new ParsedSubset<>(gridUpperBound, gridUpperBound));
                }

                // Then, translate all these parsed subsets to grid domains
                for (ParsedSubset<BigDecimal> parsedGeoSubset : parsedGeoSubsets) {
                    WcpsSubsetDimension subsetDimension = new WcpsTrimSubsetDimension(axis.getLabel(), axis.getNativeCrsUri(),
                                                                                      parsedGeoSubset.getLowerLimit().toPlainString(), parsedGeoSubset.getUpperLimit().toPlainString());
                    ParsedSubset<Long> parsedGridSubset = coordinateTranslationService.geoToGridSpatialDomain(axis, subsetDimension, parsedGeoSubset);
                    translatedGridSubsets.add(parsedGridSubset);
                }
                
                List<WcpsSliceSubsetDimension> gridSlicings = this.createGridBounds(axis.getLabel(), translatedGridSubsets);
                parsedNonXYAxesGridSlicings.add(gridSlicings);
            }
        }

        List<List<WcpsSliceSubsetDimension>> results = ListUtil.cartesianProduct(parsedNonXYAxesGridSlicings);
        return results;
    }
    
    /**
     * Create a list of grid bounds which needs to regard nonXY axes properly.
     * Trimming needs to be separated to multiple slicing values (e.g: [0:3] -> 0,1,2,3
     */
    private List<WcpsSliceSubsetDimension> createGridBounds(String axisLabel, List<ParsedSubset<Long>> inputGridBounds) {
        List<WcpsSliceSubsetDimension> results = new ArrayList<>();
        
        for (ParsedSubset<Long> parsedSubset : inputGridBounds) {
            String value;
            // e.g: time, dim_pressure axis...
            if (parsedSubset.getLowerLimit().equals(parsedSubset.getUpperLimit())) {
                // slicing
                value = parsedSubset.getLowerLimit().toString();
                results.add(new WcpsSliceSubsetDimension(axisLabel, CrsUtil.GRID_CRS, value));
            } else {
                for (Long i = parsedSubset.getLowerLimit(); i <= parsedSubset.getUpperLimit(); i++) {
                    // also slicing
                    value = i.toString();
                    results.add(new WcpsSliceSubsetDimension(axisLabel, CrsUtil.GRID_CRS, value));
                }
            }
        }
        
        return results;
    }
    
    /**
     * Parse the dimension subsets (if exist) from GetMap request, e.g: time=...&dim_pressure=...
     * NOTE: WMS subset can be:
     * + Single value: e.g: dim_pressure=20
     * + Interval: e.g: dim_pressure=20/100
     * + List of values: e.g: dim_pressure=20,30,50
     * + Multiple intervals: e.g: dim_pressure=20/100,150/300
     */
    private List<ParsedSubset<BigDecimal>> parseDimensionSubset(Axis axis, String dimensionSubset) throws PetascopeException, SecoreException {
        List<ParsedSubset<BigDecimal>> parsedSubsets = new ArrayList<>();
        String[] parts = dimensionSubset.split(KVPSymbols.VALUE_WMS_SUBSET_SEPARATE_CHARACTER);
        
        for (String part : parts) {
            ParsedSubset<BigDecimal> parsedSubset;
            if (part.contains(KVPSymbols.VALUE_TIME_PATTERN_CHARACTER)) {
                // Time axis with datetime format, e.g: "2015-04-01T20:00:20Z")
                
                if (part.contains(VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER)) {
                    // e.g: "2015-05-01"/"2015-06-01"
                    String lowerBound = part.split(VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER)[0];
                    String upperBound = part.split(VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER)[1];
                    
                    TrimmingSubsetDimension subsetDimension = new TrimmingSubsetDimension(axis.getLabel(), axis.getNativeCrsUri(), lowerBound, upperBound);
                    parsedSubset = CrsComputerService.parseSubsetDimensionToNumbers(axis.getNativeCrsUri(), axis.getAxisUoM(), subsetDimension);
                } else {
                    // e.g: "2015-05-01"
                    String lowerBound = part;
                    String upperBound = part;
                    TrimmingSubsetDimension subsetDimension = new TrimmingSubsetDimension(axis.getLabel(), axis.getNativeCrsUri(), lowerBound, upperBound);
                    parsedSubset = CrsComputerService.parseSubsetDimensionToNumbers(axis.getNativeCrsUri(), axis.getAxisUoM(), subsetDimension);
                }
            } else {
                // Numeric axes
                if (part.contains(VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER)) {
                    // e.g: 20/100
                    BigDecimal lowerBound = new BigDecimal(part.split(VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER)[0]);
                    BigDecimal upperBound = new BigDecimal(part.split(VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER)[1]);
                    parsedSubset = new ParsedSubset<>(lowerBound, upperBound);
                } else {
                    // e.g: 50
                    BigDecimal lowerBound = new BigDecimal(part);
                    BigDecimal upperBound = lowerBound;
                    parsedSubset = new ParsedSubset<>(lowerBound, upperBound);
                }
            }
            
            parsedSubsets.add(parsedSubset);
        }
        
        return parsedSubsets;
    }
    
}
