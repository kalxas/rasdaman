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
package org.rasdaman.admin.pyramid.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.CoveragePyramid;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.domain.cis.IrregularAxis;
import org.rasdaman.domain.cis.RasdamanDownscaledCollection;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.rasdaman.exceptions.RasdamanCollectionExistsException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.StringUtil;
import petascope.util.ras.RasUtil;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;
import static petascope.util.ras.RasConstants.RASQL_INTERVAL_SEPARATION;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericSlicing;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.ParsedSubset;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.service.CollectionAliasRegistry;
import petascope.wcps.metadata.service.CoordinateTranslationService;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;
import petascope.wcst.helpers.insert.RasdamanDefaultCollectionCreator;

/**
 * Utility class to create downscaled collections for input WCS Coverage on
 * geo-domain XY axes. Then, later, WMS GetMap request can select lower/higher
 * resolution collections based on zoom level.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class PyramidService {

    @Autowired
    private CoverageRepositoryService coverageRepostioryService;
    @Autowired
    private CoordinateTranslationService coordinateTranslationService;
    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;
    @Autowired
    private CollectionAliasRegistry collectionAliasRegistry;

    private static org.slf4j.Logger log = LoggerFactory.getLogger(PyramidService.class);
    
    // This is the maxium number of pixels for XY axes to be selected for updating downscaled collection
    // e.g: [0:9999], [10000:19999],...
    
    
    private static final Long MAX_SELECT_GRID_WIDTH_HEIGHT_AXIS = 10000L;
    // Only select 1 pixel on non XY axes for updating downscaled collection
    private static final Long MAX_SELECT_GRID_OTHER_AXIS = 1L;
    
    public PyramidService() {
        
    }

    /**
     * Create an empty rasdaman collection with pattern: collectionName_level
     * (e.g: test_mean_summer_airtemp_2)
     * 
     * for a pyramid member coverage (used only for CreatePyramidMember admin request)
     */
    public void initDownscaledLevelCollection(Coverage pyramidMemberCoverage,
                                              List<BigDecimal> scaleFactors, String username, String password) throws PetascopeException, SecoreException {
        String pyramidMemberCoverageId = pyramidMemberCoverage.getCoverageId();
        String downscaledLevelCollection = pyramidMemberCoverageId;
        
        String collectionType = pyramidMemberCoverage.getRasdamanRangeSet().getCollectionType();
        
        // Create an empty downscaled collection
        try {
            RasUtil.createRasdamanCollection(downscaledLevelCollection, collectionType);
        }  catch (RasdamanCollectionExistsException e) {
            // Retry to create collection with new collection name
            downscaledLevelCollection = StringUtil.addDateTimeSuffix(downscaledLevelCollection);
            RasUtil.createRasdamanCollection(downscaledLevelCollection, collectionType);
        }
        
        pyramidMemberCoverage.getRasdamanRangeSet().setCollectionName(downscaledLevelCollection);
        log.info("Created new downscaled level collection '" + downscaledLevelCollection + "' for pyramid member coverage '" + pyramidMemberCoverageId + "'.");
        
        try {
            // Then insert 1 value to the newly created downscaled collection
            int numberOfDimensions = pyramidMemberCoverage.getNumberOfDimensions();
            int numberOfBands = pyramidMemberCoverage.getNumberOfBands();
            String tileSetting = pyramidMemberCoverage.getRasdamanRangeSet().getTiling();

            // Instantiate 1 first 1 point MDD in this empty downscaled collection to be ready to update data later
            RasUtil.initializeMDD(numberOfDimensions, numberOfBands, collectionType, tileSetting, downscaledLevelCollection, username, password);
        } catch (PetascopeException ex) {
            String errorMessage = "Error initializing MDD for newly "
                    + "created downscaled rasdaman collection '" + downscaledLevelCollection + "' for pyramid member coverage '" + pyramidMemberCoverageId
                    + "'. Reason: " + ex.getExceptionText();            
            // NOTE: any error when preparing the downscaled collection will need to clean the newly created collection in rasdaman.
            this.deleteDownscaledLevelCollection(downscaledLevelCollection, username, password); 
            
            throw new PetascopeException(ExceptionCode.InternalComponentError, errorMessage, ex);
        }
    }
    

    /**
     * Update data from a higher resolution collection to a lower resolution
     * collection on a specific geo XY subsets only from the geo domains of original coverage.
     * 
     * NOTE: input geoDomains will be divided to have good size enough for updating to downscaled collection.
     */
    public void updateDownscaledLevelCollection(GeneralGridCoverage baseCoverage, 
                                                GeneralGridCoverage pyramidMemberCoverage,
                                                String targetDownscaledCollectionName,
                                                CoveragePyramid targetCoveragePyramid, 
                                                List<String> baseAffectedGridDomains,
                                                String username, String password) throws PetascopeException, SecoreException {
        
        List<BigDecimal> targetScaleFactors = targetCoveragePyramid.getScaleFactorsList();

        // Use this coverage as the source coverage for updating data to downscaled level collections
        CoveragePyramid sourceCoveragePyramid = this.getCoveragePyramidAsSourceDownscaledCollection(baseCoverage, targetScaleFactors);
        
        Coverage sourceCoverage = this.coverageRepostioryService.readCoverageFullMetadataByIdFromCache(sourceCoveragePyramid.getPyramidMemberCoverageId());
        String sourceCollectionName = sourceCoverage.getRasdamanRangeSet().getCollectionName();
        
        // Sort the scale factor by geo orders to grid order to calculate the grid domains to be select and insert into the target downscaled collection
        // e.g: source CoveragePyramid has scale factors: 1,4,4 (downscaled level 4 on XY axes)
        List<BigDecimal> sourceScaleFactorsByGridOrder = this.sortScaleFactorsByGridOrder(baseCoverage, sourceCoveragePyramid.getScaleFactorsList());
        // e.g: target CoveragePyramid has scale factos: 1,8,8 (downscaled level 8 on XY axes)
        List<BigDecimal> targetScaleFactorsByGridOrder = this.sortScaleFactorsByGridOrder(baseCoverage, targetScaleFactors);
        
        GeoAxis geoAxisX = ((GeneralGridCoverage)sourceCoverage).getXYGeoAxes().fst;
        GeoAxis geoAxisY = ((GeneralGridCoverage)sourceCoverage).getXYGeoAxes().snd;
        
        int gridOrderAxisX = ((GeneralGridCoverage)sourceCoverage).getIndexAxisByName(geoAxisX.getAxisLabel()).getAxisOrder();
        int gridOrderAxisY = ((GeneralGridCoverage)sourceCoverage).getIndexAxisByName(geoAxisY.getAxisLabel()).getAxisOrder();
        
        // Now, separate the (big) grid domains on source collection properly and select these suitable spatial domains to update on target downscaled collections
        this.updateScaleLevelByGridDomains(baseCoverage,
                                           pyramidMemberCoverage,
                                           sourceCollectionName, targetDownscaledCollectionName, baseAffectedGridDomains, 
                                           sourceScaleFactorsByGridOrder,
                                           targetScaleFactorsByGridOrder, username, password, gridOrderAxisX, gridOrderAxisY);
    }
    
    /**
     * When updating a base coverage with WCS-T UpdateCoverage request, then this selected pyramid member
     * coverage of this base coverage should be updated as well
     */
    public static void updateGridAndGeoDomainsOnDownscaledLevelCoverage(GeneralGridCoverage baseCoverage,
                                                           GeneralGridCoverage pyramidMemberCoverage,
                                                           List<BigDecimal> inputScaleFactors) throws PetascopeException {
        
        // as inputScaleFactors is geo-CRS order (e.g. Lat,Long) not grid oder (e.g. Long,Lat) 
        List<BigDecimal> scaleFactorsByGridOrder = sortScaleFactorsByGridOrder(baseCoverage, inputScaleFactors);
        
        for (int i = 0; i < baseCoverage.getIndexAxes().size(); i++) {

            // Update grid bounds
            IndexAxis baseIndexAxis = baseCoverage.getIndexAxisByOrder(i);
            IndexAxis pyramidMemberIndexAxis = pyramidMemberCoverage.getIndexAxisByOrder(i);
            String aixsLabel = pyramidMemberIndexAxis.getAxisLabel();
            
            GeoAxis baseGeoAxis = baseCoverage.getGeoAxisByName(aixsLabel);
            GeoAxis pyramidMemberGeoAxis = pyramidMemberCoverage.getGeoAxisByName(aixsLabel);           
            
            BigDecimal scaleFactor = scaleFactorsByGridOrder.get(i);
            
            if (!baseGeoAxis.isIrregular()) {
                // e.g: 907 as base grid upper bound
                long baseIndexAxisLowerBound = baseIndexAxis.getLowerBound();
                long baseIndexAxisUpperBound = baseIndexAxis.getUpperBound();
                
                // e.g: 7 as pyramid member grid lower bound with scale ratio = 122.0
                long gridIndexAxisLowerBoundNew = BigDecimalUtil.divideToLong(new BigDecimal(baseIndexAxisLowerBound), scaleFactor);
                long gridIndexAxisUpperBoundNew = BigDecimalUtil.divideToLong(new BigDecimal(baseIndexAxisUpperBound), scaleFactor);
                
                pyramidMemberIndexAxis.setLowerBound(gridIndexAxisLowerBoundNew);                
                pyramidMemberIndexAxis.setUpperBound(gridIndexAxisUpperBoundNew);

            } else {
                // NOTE: for irregular axis, set as base coverage
                pyramidMemberIndexAxis.setLowerBound(baseIndexAxis.getLowerBound());
                pyramidMemberIndexAxis.setUpperBound(baseIndexAxis.getUpperBound());
            }
            
            // Update geo bounds in case the base's geo domains are extended for pyramid member coverage
            pyramidMemberGeoAxis.setLowerBound(baseGeoAxis.getLowerBound());
            pyramidMemberGeoAxis.setUpperBound(baseGeoAxis.getUpperBound());
            
            if (!baseGeoAxis.isIrregular()) {
                // for regular axis
                BigDecimal pyramidGeoDistance = pyramidMemberGeoAxis.getUpperBoundNumber().subtract(pyramidMemberGeoAxis.getLowerBoundNumber());
                long pyramidGridDistance = pyramidMemberIndexAxis.getUpperBound() - pyramidMemberIndexAxis.getLowerBound() + 1;

                BigDecimal newPyramidResolution = BigDecimalUtil.divide(pyramidGeoDistance, new BigDecimal(pyramidGridDistance));

                BigDecimal oldPyramidResolution = pyramidMemberGeoAxis.getResolution();
                // for axis with negative resolution, e.g: Lat
                if (oldPyramidResolution.compareTo(BigDecimal.ZERO) < 0) {
                    newPyramidResolution = newPyramidResolution.multiply(BigDecimal.valueOf(-1));
                }

                pyramidMemberGeoAxis.setResolution(newPyramidResolution);
            } else {
                // NOTE: for irregular axes, coefficients are the same between base and pyramid member coverages
                org.rasdaman.domain.cis.IrregularAxis baseIrregularAxis = ((org.rasdaman.domain.cis.IrregularAxis)baseGeoAxis);
                org.rasdaman.domain.cis.IrregularAxis pyramidMemberIrregularAxis = ((org.rasdaman.domain.cis.IrregularAxis)pyramidMemberGeoAxis);
                
                pyramidMemberIrregularAxis.setDirectPositions(baseIrregularAxis.getDirectPositionsAsNumbers());
            }
            
        }
        
    }
    
    public void deleteDownscaledLevelCollection(String downscaledCollectionName, String username, String password) throws PetascopeException {
        RasUtil.deleteFromRasdaman(downscaledCollectionName, username, password);
    }
    
    /**
     * Update downscaled data from source collection to target collection by calculating how much data it should query and update.
     * e.g: source grid domains [0:2, 0:200, 0:150] with axes: time, long, lat
     *      target grid domains [0:2, 0:100, 0:75] with axes: time, long, lat
     * 
     * then, there will be 3 query to select from source collection and update downscaled data to target collection.
     * - select c[0, 0:200, 0:150] -> d[0, 0:100, 0:75]
     * - select c[1, 0:200, 0:150] -> d[1, 0:100, 0:75]
     * - select c[2, 0:200, 0:150] -> d[2, 0:100, 0:75]
     */
    private void updateScaleLevelByGridDomains(GeneralGridCoverage baseCoverage,
                                               GeneralGridCoverage pyramidMemberCoverage,
                                               String sourceDownscaledCollectionName, String targetDownscaledCollectionName, 
                                               List<String> baseAffectedGridDomains, 
                                               List<BigDecimal> sourceScaleFactors,
                                               List<BigDecimal> targetScaleFactors,
                                               String username, String password,
                                               int gridOrderAxisX, int gridOrderAxisY) throws PetascopeException {
        
        List<List<String>> calculatedSourceAffectedDomainsList = new ArrayList<>();
        List<List<String>> calculatedTargetAffectedDomainsList = new ArrayList<>();
        
        String sdomSourceRasdamanDownscaledCollection = null;
        if (sourceDownscaledCollectionName != null) {
            sdomSourceRasdamanDownscaledCollection = RasUtil.retrieveSdomInfo(sourceDownscaledCollectionName);
        } else {
            sdomSourceRasdamanDownscaledCollection = ListUtil.join(baseAffectedGridDomains, ",");
        }
        
        
        // e.g: 0:0,0:20,0:30
        String[] gridIntervals = sdomSourceRasdamanDownscaledCollection.split(RASQL_INTERVAL_SEPARATION);
        
        for (int i = 0; i < baseAffectedGridDomains.size(); i++) {
            // e.g: 4
            BigDecimal sourceScaleFactor = sourceScaleFactors.get(i);
            // e.g: 8
            BigDecimal targetScaleFactor = targetScaleFactors.get(i);
            BigDecimal targetDownscaledRatio = BigDecimalUtil.divide(sourceScaleFactor, targetScaleFactor);
            
            // e.g: 0:100 with scaleFactor = 2 -> 0:50
            String baseAffectedGridDomain = baseAffectedGridDomains.get(i);
            String[] tmps = baseAffectedGridDomain.split(RASQL_BOUND_SEPARATION);
            long sourceGridLowerBound = BigDecimalUtil.divide(new BigDecimal(tmps[0]), sourceScaleFactor).longValue();
            long sourceGridUpperBound = sourceGridLowerBound;
            
            if (tmps.length == 2) {
                sourceGridUpperBound = BigDecimalUtil.divide(new BigDecimal(tmps[1]), sourceScaleFactor).longValue();
            }
            
            String sourceAffectedGridDomain = sourceGridLowerBound + RASQL_BOUND_SEPARATION + sourceGridUpperBound;
            
            long upperBoundGridAxis = Long.valueOf(gridIntervals[i].split(RASQL_BOUND_SEPARATION)[1]);
            Pair<List<String>, List<String>> separatedPair;
            
            IndexAxis pyramidIndexAxis = pyramidMemberCoverage.getIndexAxisByOrder(i);
            
            if (i == gridOrderAxisX || i == gridOrderAxisY) {
                // X or Y axes, seperated by size 10000 x 10000
                separatedPair = this.separateGridDomainByValue(sourceAffectedGridDomain, MAX_SELECT_GRID_WIDTH_HEIGHT_AXIS,
                                                                                            upperBoundGridAxis, targetDownscaledRatio, pyramidIndexAxis);
            } else {
                // non XY axes, seperated by size 1
                separatedPair = this.separateGridDomainByValue(sourceAffectedGridDomain, MAX_SELECT_GRID_OTHER_AXIS,
                                                                                            upperBoundGridAxis, targetDownscaledRatio, pyramidIndexAxis);
            }
            List<String> calculatedSourceAffectedDomains = separatedPair.fst;
            List<String> calculatedTargetAffectedDomains = separatedPair.snd;
            
            calculatedSourceAffectedDomainsList.add(calculatedSourceAffectedDomains);
            calculatedTargetAffectedDomainsList.add(calculatedTargetAffectedDomains);
        }
        
        // e.g: [0:0, 0:100, 0:200], [1:1, 0:100, 0:200]
        List<List<String>> sourceAffectedDomainsList = ListUtil.cartesianProduct(calculatedSourceAffectedDomainsList);
        // e.g: [0:0, 0:50, 0:100], [1:1, 0:50, 0:100]
        List<List<String>> targetAffectedDomainsList = ListUtil.cartesianProduct(calculatedTargetAffectedDomainsList);
        
        for (int i = 0; i < sourceAffectedDomainsList.size(); i++) {
            String sourceAffectedDomain = sourceAffectedDomainsList.get(i).toString();
            String targetAffectedDomain = targetAffectedDomainsList.get(i).toString();

            RasUtil.updateDownscaledCollectionFromSourceCollection(sourceAffectedDomain, targetAffectedDomain, sourceDownscaledCollectionName,
                                                                   targetDownscaledCollectionName, username, password);
        }
    }
    
    /**
     * Separate a grid domain equally by a value. e.g: 0:20 and value is 5, 
     * result will be: 0:4,5:9,10:14,15:19,20:20
     */
    private Pair<List<String>, List<String>> separateGridDomainByValue(String gridDomain, long upperValueForSeparation, long upperBoundGridAxis,
                                                                       BigDecimal targetDownscaledRatio,
                                                                       IndexAxis pyramidIndexAxis) {
        List<String> sourceSeparatedResults = new ArrayList<>();
        List<String> targetSeparatedResults = new ArrayList<>();
        
        long lowerBound = new Long(gridDomain.split(RASQL_BOUND_SEPARATION)[0]);
        long upperBound = new Long(gridDomain.split(RASQL_BOUND_SEPARATION)[1]);
        
        // e:g: domain: 0:20
        while (lowerBound <= upperBound) {
            Long temp = lowerBound + upperValueForSeparation - 1;       
            if (temp < lowerBound) {
                temp = lowerBound;
            }

            // e.g: value = 5, first is: 0:4
            String sourceResult = lowerBound + RASQL_BOUND_SEPARATION + temp;
            long gridLowerBoundTargetResult = BigDecimalUtil.multipleToLong(new BigDecimal(lowerBound), targetDownscaledRatio);
            long gridUpperBoundTargetResult = BigDecimalUtil.multipleToLong(new BigDecimal(temp), targetDownscaledRatio);
            
            String targetResult = gridLowerBoundTargetResult + RASQL_BOUND_SEPARATION + gridUpperBoundTargetResult;
            if (temp >= upperBound) {
                // e.g: value = 5, last is 20:20
                long tempUpperBound = upperBound;
                // NOTE: cannot select out of upper grid bound of source downscaled collection for an axis
                if (upperBound > upperBoundGridAxis) {
                    tempUpperBound = upperBoundGridAxis;
                }
                sourceResult = lowerBound + RASQL_BOUND_SEPARATION + tempUpperBound;
                
                long minGridDownscaledLowerBound = pyramidIndexAxis.getLowerBound();
                long maxGridDownscaledUpperBound = pyramidIndexAxis.getUpperBound();
                
                long downscaledLowerBound = BigDecimalUtil.multipleToLong(new BigDecimal(lowerBound), targetDownscaledRatio);
                // lowerBound must be in the calculated bound of PyramidMemberCoverage
                if (downscaledLowerBound < minGridDownscaledLowerBound) {
                    downscaledLowerBound = minGridDownscaledLowerBound;
                }
                
                long downscaledUpperBound = BigDecimalUtil.multipleToLong(new BigDecimal(tempUpperBound), targetDownscaledRatio);
                // upperBound must be in the calculated bound of PyramidMemberCoverage
                if (downscaledUpperBound > maxGridDownscaledUpperBound) {
                    downscaledUpperBound = maxGridDownscaledUpperBound;
                }
                
                targetResult = downscaledLowerBound + RASQL_BOUND_SEPARATION + downscaledUpperBound;
            }
            sourceSeparatedResults.add(sourceResult);
            targetSeparatedResults.add(targetResult);
            
            lowerBound = temp + 1;
        }
        
        return new Pair<>(sourceSeparatedResults, targetSeparatedResults);
    }

    /**
     * Rasdaman downscaled collections have this pattern: coverageId_level.
     * NOTE: level can be float (e.g: 2.5) then collection name will be: test_mr_2_5.
     */
    public String createDownscaledCollectionName(String collectionName, List<BigDecimal> scaleFactors) {
        List<String> factors = new ArrayList<>();
        for (BigDecimal value : scaleFactors) {
            // e.g: 8.5 -> 8_5
            String levelStr = BigDecimalUtil.replacePointByUnderscore(value);
            factors.add(levelStr);
        }
        
        // e.g: cov1_2_2 (with 2 and 2 are scale factors for 2 Y axes)
        return ListUtil.createDownscaledCoverageName(collectionName, factors);
    }
    
    /**
     * Select the suitable coverage pyramid from the pyramid set of a base coverage
     * which can be used as the source collection to populate scaled data to a higher downscaled level collection
     */
    public CoveragePyramid getCoveragePyramidAsSourceDownscaledCollection(Coverage baseCoverage, List<BigDecimal> inputScaleFactors) {
        CoveragePyramid result = this.createBaseCoveragePyramid(baseCoverage);
        
        if (baseCoverage.getPyramid() != null) {
            for (CoveragePyramid currentCoveragePyramid : baseCoverage.getPyramid()) {
                if (currentCoveragePyramid.isSynced()) {

                    List<BigDecimal> sourceScaleFactors = currentCoveragePyramid.getScaleFactorsList();
                    boolean selected = true;

                    // e.g: pyramid has levels, 1, 4, 8, and one wants to import level 6, then the selected source level will be 4
                    // to be used for creating downscaled level queries to insert data to level 6
                    for (int i = 0; i < sourceScaleFactors.size(); i++) {
                        BigDecimal firstValue = result.getScaleFactorsList().get(i);
                        BigDecimal secondValue = sourceScaleFactors.get(i);
                        BigDecimal thirdValue = inputScaleFactors.get(i);

                        if (!(secondValue.compareTo(firstValue) > 0 && secondValue.compareTo(thirdValue) < 0)) {
                            selected = false;
                            break;
                        }
                    }

                    if (selected) {
                        result = currentCoveragePyramid;
                    }
                }
            }
        }
        
        return result;        
    }
    
    /**
     * Create a CoveragePyramid object with default scale levels = 1
     */
    public CoveragePyramid createBaseCoveragePyramid(Coverage baseCoverage) {
        int numberOfAxes = baseCoverage.getNumberOfDimensions();
        List<String> scaleFactors = ListUtil.createListOfSizeAndValues(numberOfAxes, BigDecimal.ONE.toPlainString());
        CoveragePyramid result = new CoveragePyramid(baseCoverage.getCoverageId(), scaleFactors, true);
        
        return result;
    }
    
    /**
     * Sort a list of scale factors by geo CRS order to a list of scale factors by grid oder
     * e.g: time,lat,long in geoCRS order -> time,long,lat in grid order
     */
    public static List<BigDecimal> sortScaleFactorsByGridOrder(GeneralGridCoverage baseCoverage, List<BigDecimal> scaleFactors) {
        TreeMap<Integer, BigDecimal> map = new TreeMap<>();
        
        int i = 0;
        for (GeoAxis geoAxis : baseCoverage.getGeoAxes()) {
            IndexAxis indexAxis = baseCoverage.getIndexAxisByName(geoAxis.getAxisLabel());
            int gridAxisOrder = indexAxis.getAxisOrder();
            
            BigDecimal scaleFactor = scaleFactors.get(i);
            map.put(gridAxisOrder, scaleFactor);
            i++;
        }        
        
        List<BigDecimal> results = new ArrayList<>(map.values());
        return results;
    }
    
        /**
     * Sort a list of scale factors by geo CRS order to a list of scale factors by grid oder
     * e.g: time,lat,long in geoCRS order -> time,long,lat in grid order
     */
    public List<Pair<String, BigDecimal>> sortScaleFactorsByGridOderWithAxisLabel(GeneralGridCoverage baseCoverage, List<BigDecimal> scaleFactors) {
        TreeMap<Integer, Pair<String, BigDecimal>> map = new TreeMap<>();
        
        int i = 0;
        for (GeoAxis geoAxis : baseCoverage.getGeoAxes()) {
            IndexAxis indexAxis = baseCoverage.getIndexAxisByName(geoAxis.getAxisLabel());
            int gridAxisOrder = indexAxis.getAxisOrder();
            
            BigDecimal scaleFactor = scaleFactors.get(i);
            String axisLabel = geoAxis.getAxisLabel();
            map.put(gridAxisOrder, new Pair<>(axisLabel, scaleFactor));
            i++;
        }        
        
        List<Pair<String, BigDecimal>> results = new ArrayList<>(map.values());
        return results;
    }
    
    /**
     * Check if this pyramid member also contains the subsets for non-XY axes (e.g. time).
     */
    private boolean isGoodPyramidMemberForNonXYSubsets(List<WcpsSubsetDimension> nonXYSubsetDimensions, CoveragePyramid coveragePyramid) throws PetascopeException {
        
        if (nonXYSubsetDimensions != null && !nonXYSubsetDimensions.isEmpty()) {
            // e.g. pyramid_member_2
            String coverageId = coveragePyramid.getPyramidMemberCoverageId();
            GeneralGridCoverage pyramidMemberCoverage = (GeneralGridCoverage)this.coverageRepostioryService.readCoverageFullMetadataByIdFromCache(coverageId);
            
            for (WcpsSubsetDimension subsetDimension : nonXYSubsetDimensions) {
                String axisLabel = subsetDimension.getAxisName();
                GeoAxis geoAxis = pyramidMemberCoverage.getGeoAxisByName(axisLabel);
                
                BigDecimal geoLowerBound = null;
                BigDecimal geoUpperBound = null;
                
                if (subsetDimension instanceof WcpsSliceSubsetDimension) {
                    WcpsSliceSubsetDimension sliceSubset = (WcpsSliceSubsetDimension)subsetDimension;
                    geoLowerBound = new BigDecimal((sliceSubset.getBound()));
                } else {
                    WcpsTrimSubsetDimension trimSubset = (WcpsTrimSubsetDimension)subsetDimension;
                    geoLowerBound = new BigDecimal((trimSubset.getLowerBound()));
                    geoUpperBound = new BigDecimal((trimSubset.getUpperBound()));
                }
                
                if (!BigDecimalUtil.isValidValue(geoAxis.getLowerBoundNumber(), geoAxis.getUpperBoundNumber(), geoLowerBound)) {
                    return false;
                }
                if (!BigDecimalUtil.isValidValue(geoAxis.getLowerBoundNumber(), geoAxis.getUpperBoundNumber(), geoUpperBound)) {
                    return false;
                }
                
                if (geoAxis instanceof IrregularAxis) {
                    IrregularAxis irregularAxis = (IrregularAxis)geoAxis;
                    BigDecimal coefficientZeroGeoBound = irregularAxis.getCoefficientZeroBoundNumber();
                    
                    // Check if subset on irregular axis of this pyramid member touches the existing coefficients
                    // NOTE: a pyramid member can contain less coefficients than the base coverage
                    BigDecimal geoLowerBoundCoefficient = geoLowerBound.subtract(coefficientZeroGeoBound);
                    BigDecimal geoUpperBoundCoefficient = geoLowerBoundCoefficient;
                    if (geoUpperBound != null) {
                        geoUpperBoundCoefficient = geoUpperBound.subtract(coefficientZeroGeoBound);
                    }

                    Pair<Long, Long> pair = irregularAxis.getGridIndices(geoLowerBoundCoefficient, geoUpperBoundCoefficient);
                    if (pair.fst == null || pair.snd == null) {
                        return false;
                    }
                    
                }
            }
            
        }
        
        return true;
    }
    
    
    /**
    *  Given geo subsets on X and Y axes, and their scale target grid domains,
    * return the suitable coverage pyramid member.
    */
    public CoveragePyramid getSuitableCoveragePyramidForScaling(GeneralGridCoverage baseCoverage, 
                                             Pair<BigDecimal, BigDecimal> geoSubsetX, Pair<BigDecimal, BigDecimal> geoSubsetY,
                                             Axis subsettedAxisX, Axis subsettedAxisY,
                                             int width, int height,
                                             List<WcpsSubsetDimension> nonXYSubsetDimensions) throws PetascopeException {
        Pair<GeoAxis, GeoAxis> baseXYGeoAxes = baseCoverage.getXYGeoAxes();
        GeoAxis baseXGeoAxis = baseXYGeoAxes.fst;
        GeoAxis baseYGeoAxis = baseXYGeoAxes.snd;
        
        IndexAxis baseXIndexAxis = baseCoverage.getIndexAxisByName(baseXGeoAxis.getAxisLabel());
        IndexAxis baseYIndexAxis = baseCoverage.getIndexAxisByName(baseYGeoAxis.getAxisLabel());
        
        ParsedSubset<BigDecimal> baseXParsedSubset = new ParsedSubset<>(geoSubsetX.fst, geoSubsetX.snd);
        ParsedSubset<Long> baseXGridBounds = this.coordinateTranslationService.geoToGridForRegularAxis(baseXParsedSubset, baseXGeoAxis.getLowerBoundNumber(),
                                                                                                       baseXGeoAxis.getUpperBoundNumber(), baseXGeoAxis.getResolution(), 
                                                                                                       new BigDecimal(baseXIndexAxis.getLowerBound()));
        long numberOfBaseXGridPixels = baseXGridBounds.getUpperLimit() - baseXGridBounds.getLowerLimit() + 1;
        
        ParsedSubset<BigDecimal> baseYParsedSubset = new ParsedSubset<>(geoSubsetY.fst, geoSubsetY.snd);
        ParsedSubset<Long> baseYGridBounds = this.coordinateTranslationService.geoToGridForRegularAxis(baseYParsedSubset, baseYGeoAxis.getLowerBoundNumber(),
                                                                                                       baseYGeoAxis.getUpperBoundNumber(), baseYGeoAxis.getResolution(), 
                                                                                                       new BigDecimal(baseYIndexAxis.getLowerBound()));
        long numberOfBaseYGridPixels = baseYGridBounds.getUpperLimit() - baseYGridBounds.getLowerLimit() + 1;
        
        CoveragePyramid result = this.createBaseCoveragePyramid(baseCoverage);
        
        List<CoveragePyramid> coveragePyramids = new ArrayList<>();
        coveragePyramids.add(result);
        coveragePyramids.addAll(baseCoverage.getPyramid());
        
        for (CoveragePyramid coveragePyramid : coveragePyramids) {
            GeneralGridCoverage pyramidMemberCoverage = (GeneralGridCoverage)this.coverageRepostioryService.readCoverageFullMetadataByIdFromCache(coveragePyramid.getPyramidMemberCoverageId());
            Pair<GeoAxis, GeoAxis> pyramidMemberXYGeoAxes = pyramidMemberCoverage.getXYGeoAxes();
            
            GeoAxis pyramidMemberGeoAxisX = pyramidMemberXYGeoAxes.fst;
            GeoAxis pyramidMemberGeoAxisY = pyramidMemberXYGeoAxes.snd;
            IndexAxis pyramidMemberIndexAxisX = pyramidMemberCoverage.getIndexAxisByName(pyramidMemberGeoAxisX.getAxisLabel());
            IndexAxis pyramidMemberIndexAxisY = pyramidMemberCoverage.getIndexAxisByName(pyramidMemberGeoAxisY.getAxisLabel());
            
            if (subsettedAxisX != null && subsettedAxisY != null
                && subsettedAxisX.isTransatedGridToGeoBounds() && subsettedAxisY.isTransatedGridToGeoBounds()) {
                Long gridLowerBoundX = subsettedAxisX.getGridBounds().getLowerLimit().longValue();
                Long gridUpperBoundX = subsettedAxisX.getGridBounds().getUpperLimit().longValue();
                
                Long gridLowerBoundY = subsettedAxisY.getGridBounds().getLowerLimit().longValue();
                Long gridUpperBoundY = subsettedAxisY.getGridBounds().getUpperLimit().longValue();
                
                if (pyramidMemberIndexAxisX.getLowerBound() > gridLowerBoundX
                    || pyramidMemberIndexAxisX.getUpperBound() < gridUpperBoundX
                    || pyramidMemberIndexAxisY.getLowerBound() > gridLowerBoundY
                    || pyramidMemberIndexAxisY.getUpperBound() < gridUpperBoundY) {
                    // NOTE: in this case, pyramid member doesn't have adequate grid domains on XY axes as the ones from subsetted X,Y axes
                    // so this pyramid member cannot be chosen
                    return result;
                }
            }
            
            ParsedSubset<BigDecimal> pyramidMemberXParsedSubset = new ParsedSubset<>(geoSubsetX.fst, geoSubsetX.snd);
            ParsedSubset<Long> pyramidMemberXGridBounds = this.coordinateTranslationService.geoToGridForRegularAxis(pyramidMemberXParsedSubset, pyramidMemberGeoAxisX.getLowerBoundNumber(),
                                                                                                       pyramidMemberGeoAxisX.getUpperBoundNumber(), pyramidMemberGeoAxisX.getResolution(), 
                                                                                                       new BigDecimal(pyramidMemberIndexAxisX.getLowerBound()));
            long numberOfPyramidMemberXGridPixels = pyramidMemberXGridBounds.getUpperLimit() - pyramidMemberXGridBounds.getLowerLimit() + 1;
        
            ParsedSubset<BigDecimal> pyramidMemberYParsedSubset = new ParsedSubset<>(geoSubsetY.fst, geoSubsetY.snd);
            ParsedSubset<Long> pyramidMemberYGridBounds = this.coordinateTranslationService.geoToGridForRegularAxis(pyramidMemberYParsedSubset, pyramidMemberGeoAxisY.getLowerBoundNumber(),
                                                                                                       pyramidMemberGeoAxisY.getUpperBoundNumber(), pyramidMemberGeoAxisY.getResolution(), 
                                                                                                       new BigDecimal(pyramidMemberIndexAxisY.getLowerBound()));
            long numberOfPyramidMemberYGridPixels = pyramidMemberYGridBounds.getUpperLimit() - pyramidMemberYGridBounds.getLowerLimit() + 1;            
                
            // NOTE: if a pyramid member also contains subsetting coefficients for irregular axis (e.g. time), then it can be considered as a candidate
            if (this.isGoodPyramidMemberForNonXYSubsets(nonXYSubsetDimensions, coveragePyramid)) {
                if ((numberOfPyramidMemberXGridPixels <= numberOfBaseXGridPixels && numberOfPyramidMemberXGridPixels > width)
                    && (numberOfPyramidMemberYGridPixels <= numberOfBaseYGridPixels && numberOfPyramidMemberYGridPixels > height)) {
                    result = coveragePyramid;
                } else {
                    return result;
                }
            }
            
        }
        
        return result;
    }
    
    /**
     * Given a coverage, return a list of pyramid member coverages of this coverage recursively
     * e.g. covA has pyramid member covB
     *      covB has pyramid member covC
     *      covC has pyramid member covD
     * 
     * then it returns [covB, covC, covD]
     */
    public void getListOfNestedPyramidMemberCoverages(GeneralGridCoverage inputCoverage, List<GeneralGridCoverage> results) throws PetascopeException {
        
        for (CoveragePyramid coveragePyramid : inputCoverage.getPyramid()) {
            GeneralGridCoverage coverage = (GeneralGridCoverage)this.coverageRepostioryService.readCoverageFullMetadataByIdFromCache(coveragePyramid.getPyramidMemberCoverageId());
            this.getListOfNestedPyramidMemberCoverages(coverage, results);
            results.add(coverage);            
        }
        
    }
    
    /**
     * Given a base covA and a list of pyramid members coverages: covB, covC and covD
     * then calculate the scale factors of covB, covC and covD based on covA, e.g. [4,4], [2,2] and [8,8]
     * then return the sorted list by the products of scale factors from each axis
     * 4, { covC, [2,2] }
     * 16, { covB, [4,4] }
     * 64, { covD, [8,8] }
     */
    public TreeMap<BigDecimal, Pair<GeneralGridCoverage, List<String>>> getSortedPyramidMemberCoveragesByScaleFactors(GeneralGridCoverage baseCoverage, 
                                                                                                     List<GeneralGridCoverage> pyramidMemberCoverages) {
        
        TreeMap<BigDecimal, Pair<GeneralGridCoverage, List<String>>> results = new TreeMap<>();
        
        for (GeneralGridCoverage pyramidMemberCoverage : pyramidMemberCoverages) {
            
            BigDecimal productOfScaleFactors = BigDecimal.ONE;
            List<String> scaleFactors = new ArrayList<>();
            
            for (int i = 0; i < baseCoverage.getGeoAxes().size(); i++) {
                GeoAxis baseGeoAxis = baseCoverage.getGeoAxes().get(i);
                GeoAxis pyramidMemberGeoAxis = pyramidMemberCoverage.getGeoAxes().get(i);                
                
                // e.g: 10 / 5 = 2
                BigDecimal scaleFactor = BigDecimalUtil.stripDecimalZeros(BigDecimalUtil.divide(pyramidMemberGeoAxis.getResolution().abs(), 
                                                                                                baseGeoAxis.getResolution().abs()));
                // e.g: 2 * 1 = 2
                productOfScaleFactors = productOfScaleFactors.multiply(scaleFactor);
                
                scaleFactors.add(scaleFactor.toPlainString());
            }
            
            results.put(productOfScaleFactors, new Pair<>(pyramidMemberCoverage, scaleFactors));
        }
        
        return results;
    }
    
    /**
     * Given a list of CoveragePyramid objects, sort them by the products of scale factors
     * smaller (2,2) -> bigger (4,4)
     */
    public List<CoveragePyramid> sortByScaleFactors(List<CoveragePyramid> coveragePyramids) {
        TreeMap<BigDecimal, CoveragePyramid> treeMap = new TreeMap<>();
        
        for (CoveragePyramid coveragePyramid : coveragePyramids) {
            BigDecimal productOfScaleFactors = BigDecimalUtil.calculateProduct(coveragePyramid.getScaleFactorsList());
            treeMap.put(productOfScaleFactors, coveragePyramid);
        }
        
        return new ArrayList<>(treeMap.values());
    }
    
}
