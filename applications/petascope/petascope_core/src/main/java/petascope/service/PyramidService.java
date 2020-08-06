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
package petascope.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeneralGridDomainSet;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.RasdamanDownscaledCollection;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.ras.RasUtil;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;
import static petascope.util.ras.RasConstants.RASQL_INTERVAL_SEPARATION;

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

    private static org.slf4j.Logger log = LoggerFactory.getLogger(PyramidService.class);
    
    // This is the maxium number of pixels for XY axes to be selected for updating downscaled collection
    // e.g: [0:9999], [10000:19999],...
    private static final Long MAX_SELECT_GRID_WIDTH_HEIGHT_AXIS = 10000L;
    // Only select 1 pixel on non XY axes for updating downscaled collection
    private static final Long MAX_SELECT_GRID_OTHER_AXIS = 1L;

    /**
     * Create an empty rasdaman collection with pattern: collectionName_level
     * (e.g: test_mean_summer_airtemp_2)
     */
    public void insertScaleLevel(String coverageId, BigDecimal level, String username, String password) throws PetascopeException, SecoreException {

        Coverage coverage = coverageRepostioryService.readCoverageByIdFromDatabase(coverageId);

        String collectionName = coverage.getRasdamanRangeSet().getCollectionName();
        String scaledCollectionName = this.createDownscaledCollectionName(collectionName, level);
        String collectionType = coverage.getRasdamanRangeSet().getCollectionType();

        RasdamanDownscaledCollection rasdamanScaleDownCollection = new RasdamanDownscaledCollection(scaledCollectionName, level);
        if (coverage.getRasdamanRangeSet().getRasdamanDownscaledCollections().contains(rasdamanScaleDownCollection)) {
            throw new PetascopeException(ExceptionCode.CollectionExists,
                    "A downscaled collection of coverage '" + coverageId + "' for level '" + level + "' exists already.");
        }
        
        log.info("Creating new downscaled rasdaman collection '" + scaledCollectionName + "' for level '" + level + "'.");

        // Create an empty downscaled collection
        RasUtil.createRasdamanCollection(scaledCollectionName, collectionType);
        try {
            // Then insert 1 value to the newly created downscaled collection
            int numberOfDimensions = coverage.getNumberOfDimensions();
            int numberOfBands = coverage.getNumberOfBands();
            String tileSetting = coverage.getRasdamanRangeSet().getTiling();

            // Instantiate 1 first 1 point MDD in this empty downscaled collection to be ready to update data later
            RasUtil.initializeMDD(numberOfDimensions, numberOfBands, collectionType, tileSetting, scaledCollectionName, username, password);

            // Finally, add this newly created downscaled collection to coverage's list of downscaled rasdaman collections.
            coverage.getRasdamanRangeSet().getRasdamanDownscaledCollections().add(rasdamanScaleDownCollection);

            // Then save coverage to database
            this.coverageRepostioryService.save(coverage);
        } catch (PetascopeException ex) {
            log.error("Error instantiating MDD for newly "
                    + "created downscaled rasdaman collection '" + scaledCollectionName + "' for level '" + level + "'. Reason: " + ex.getExceptionText(), ex);
            // NOTE: any error when preparing the downscaled collection will need to clean the newly created collection in rasdaman.
            RasUtil.executeRasqlQuery("DROP collection " + scaledCollectionName, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);            
            throw ex;
        }
    }

    /**
     * Update data from a higher resolution collection to a lower resolution
     * collection on a specific geo XY subsets only from the geo domains of original coverage.
     * 
     * NOTE: input geoDomains will be divided to have good size enough for updating to downscaled collection.
     */
    public void updateScaleLevel(String coverageId, BigDecimal level, TreeMap<Integer, Pair<Boolean, String>> gridDomainsPairsMap, String username, String password) throws PetascopeException, SecoreException {

        Coverage coverage = coverageRepostioryService.readCoverageByIdFromDatabase(coverageId);
        String collectionName = coverage.getRasdamanRangeSet().getCollectionName();
        String targetDownscaledCollectionName = this.createDownscaledCollectionName(collectionName, level);

        RasdamanDownscaledCollection targetRasdamanDownscaledCollection = new RasdamanDownscaledCollection(targetDownscaledCollectionName, level);
        if (!coverage.getRasdamanRangeSet().getRasdamanDownscaledCollections().contains(targetRasdamanDownscaledCollection)) {
            throw new PetascopeException(ExceptionCode.CollectionDoesNotExist,
                    "A downscaled collection of coverage '" + coverageId + "' with level '" + level + "' does not exist to update.");
        }

        BigDecimal originalLevel = BigDecimal.ONE;
        BigDecimal sourceLevel = BigDecimal.ONE;
        BigDecimal targetLevel = level;
        
        // Default, use the original collection (e.g: test_mean_summer_airtemp to be source of updating test_mean_summer_airtemp_2 collection)
        String sourceDownscaledCollectionName = collectionName;
        // Get the downscaled collection with highest level but is lower than input level
        RasdamanDownscaledCollection sourceRasdamanDownscaledCollection = coverage.getRasdamanRangeSet().getRasdamanDownscaledCollectionAsSourceCollection(level);
        if (sourceRasdamanDownscaledCollection != null) {
            // There is a downscaled collection to be source collection 
            // (e.g: test_mean_summer_airtemp_3 to be source of updating test_mean_summer_airtemp_6 collection)
            sourceLevel = sourceRasdamanDownscaledCollection.getLevel();
            sourceDownscaledCollectionName = sourceRasdamanDownscaledCollection.getCollectionName();
        }
        
        // e.g: to create a new downscaled collection, level 2
        // source ratio = 1 / 1 = 1
        BigDecimal sourceDownscaledRatio = BigDecimalUtil.divide(originalLevel, sourceLevel);
        // target ration = 1 / 2 = 0.5
        BigDecimal targetDownscaledRatio = BigDecimalUtil.divide(sourceLevel, targetLevel);
        
        List<Pair<Boolean, String>> sourceAffectedDomains = new ArrayList<>();
        
        for (Pair<Boolean, String> entry : gridDomainsPairsMap.values()) {
            // Sort by rasdaman grid axes order for UPDATE query.
            String gridDomain = entry.snd;
            BigDecimal gridLowerBound, gridUpperBound;
            
            if (gridDomain.contains(RASQL_BOUND_SEPARATION)) {
                // trimming subset
                gridLowerBound = new BigDecimal(gridDomain.split(RASQL_BOUND_SEPARATION)[0]);
                gridUpperBound = new BigDecimal(gridDomain.split(RASQL_BOUND_SEPARATION)[1]);
            } else {
                // slicing subset
                gridLowerBound = new BigDecimal(gridDomain);
                gridUpperBound = new BigDecimal(gridDomain);
            }
            
            if (entry.fst) {
                // Only scale down XY axes
                Long sourceLowerBound = (gridLowerBound.multiply(sourceDownscaledRatio)).longValue();
                Long sourceUpperBound = (gridUpperBound.multiply(sourceDownscaledRatio)).longValue();
                String sourceAffectedDomain = sourceLowerBound + RASQL_BOUND_SEPARATION + sourceUpperBound;
                sourceAffectedDomains.add(new Pair<>(true, sourceAffectedDomain));
                
            } else {
                // Other types of axis (time, elevation,...)
                String affectedDomain = gridLowerBound + RASQL_BOUND_SEPARATION + gridUpperBound;
                sourceAffectedDomains.add(new Pair<>(false, affectedDomain));
            }
        }
        
        // Now, separate the (big) grid domains on source collection properly and select these suitable spatial domains to update on target downscaled collections
        this.updateScaleLevelByGridDomains(sourceDownscaledCollectionName, targetDownscaledCollectionName, sourceAffectedDomains, targetDownscaledRatio, username, password);
    }
    
    /**
     * For X or Y axis, calculate which downscaled level should be returned
     * based on the input width / height.
     */
    private BigDecimal calculateSuitableDownscaledLevelForAxis(List<RasdamanDownscaledCollection> rasdamanDownscaledCollections, 
                                                               GeoAxis geoAxis, Pair<BigDecimal, BigDecimal> geoSubset, int outputGridDomain) throws PetascopeException {
        
        int numberOfDownscaledCollections = rasdamanDownscaledCollections.size();

        BigDecimal result = BigDecimal.ONE;
        
        for (int i = 0; i < numberOfDownscaledCollections; i++) {
            BigDecimal level = rasdamanDownscaledCollections.get(i).getLevel();
            BigDecimal numberOfGridPixels = BigDecimal.ONE;
            BigDecimal axisResolutionTmp = geoAxis.getResolution().multiply(level);

            BigDecimal nextLevel = level;
            BigDecimal axisResolutionNextLevelTmp = axisResolutionTmp;
            BigDecimal numberOfGridPixelsNextLevel = BigDecimal.ONE;

            if (i < numberOfDownscaledCollections - 1) {
                nextLevel = rasdamanDownscaledCollections.get(i + 1).getLevel();
                axisResolutionNextLevelTmp = geoAxis.getResolution().multiply(nextLevel);
            }

            numberOfGridPixels = BigDecimalUtil.divide(geoSubset.snd.subtract(geoSubset.fst), axisResolutionTmp).abs();
            numberOfGridPixelsNextLevel = BigDecimalUtil.divide(geoSubset.snd.subtract(geoSubset.fst), axisResolutionNextLevelTmp).abs();

            // e.g: level 1 with grid: [0:99], level 4 with grid [0:24, 0:24], select width = 40 should use level 4
            // select width = 70 should use level 1
            BigDecimal distance = numberOfGridPixels.subtract(new BigDecimal(outputGridDomain)).abs();
            BigDecimal distanceNextLevel = numberOfGridPixelsNextLevel.subtract(new BigDecimal(outputGridDomain)).abs();

            // e.g: grid domain level 1 is 0:99, grid domain level 2 is: 0:49, select suset 0:60, then it should use 0:99 to downscale to 0:60
            if ((distance.compareTo(distanceNextLevel) < 0) || numberOfGridPixelsNextLevel.compareTo(new BigDecimal(outputGridDomain)) < 0) {
                return level;                
            } else {
                result = level;
            }
        }
        
        return result;
    }
    
    /**
     * From the list of downscaled collections, select a suitable level for input geo subset on XY axes.
     */
    public BigDecimal getDownscaledLevel(Coverage coverage, Pair<BigDecimal, BigDecimal> geoSubsetX, Pair<BigDecimal, BigDecimal> geoSubsetY, 
                                         int width, int height) 
                      throws PetascopeException {
        List<RasdamanDownscaledCollection> rasdamanDownscaledCollections = coverage.getRasdamanRangeSet().getAllPossibleRasdamanDownscaledCollections();
        // By default it is the highest downscaled level with lowest image resolution.
        int numberOfDownscaledCollections = rasdamanDownscaledCollections.size();
        BigDecimal result = rasdamanDownscaledCollections.get(numberOfDownscaledCollections - 1).getLevel();
        List<GeoAxis> geoAxes = ((GeneralGridCoverage)coverage).getGeoAxes();
        
        // Find the lowest downscaled levels for both X, Y axes which return grid domains less than width * height
        BigDecimal lowestDownscaledLevelX = result;
        BigDecimal lowestDownscaledLevelY = result;
        
        int i = 0;
        String coverageCRS = coverage.getEnvelope().getEnvelopeByAxis().getSrsName();
        
        for (GeoAxis geoAxis : geoAxes) {
            String axisType = CrsUtil.getAxisTypeByIndex(coverageCRS, i);
            
            if (CrsUtil.isXAxis(axisType)) {
                lowestDownscaledLevelX = this.calculateSuitableDownscaledLevelForAxis(rasdamanDownscaledCollections, geoAxis, geoSubsetX, width);
            } else if (CrsUtil.isYAxis(axisType)) {
                lowestDownscaledLevelY = this.calculateSuitableDownscaledLevelForAxis(rasdamanDownscaledCollections, geoAxis, geoSubsetY, height);
            }
            
            i++;
        }       
        
        // Which downscaled level be chosen as X and Y can return different levels from geoXY subsets
        if (lowestDownscaledLevelX.compareTo(lowestDownscaledLevelY) < 0) {
            result = lowestDownscaledLevelY;
        } else {
            result = lowestDownscaledLevelX;
        }
        
        return result;
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
    private void updateScaleLevelByGridDomains(String sourceDownscaledCollectionName, String targetDownscaledCollectionName, 
                                               List<Pair<Boolean, String>> sourceAffectedDomains, BigDecimal targetDownscaledRatio, String username, String password) throws PetascopeException {
        
        List<List<String>> calculatedSourceAffectedDomainsList = new ArrayList<>();
        List<List<String>> calculatedTargetAffectedDomainsList = new ArrayList<>();
        
        String sdomSourceRasdamanDownscaledCollection = RasUtil.retrieveSdomInfo(sourceDownscaledCollectionName);
        // e.g: 0:0,0:20,0:30
        String[] gridIntervals = sdomSourceRasdamanDownscaledCollection.split(RASQL_INTERVAL_SEPARATION);
        
        for (int i = 0; i < sourceAffectedDomains.size(); i++) {
            Pair<Boolean, String> sourceAffectedDomainPair = sourceAffectedDomains.get(i);
            
            boolean isXYAxis = sourceAffectedDomainPair.fst;
            String sourceAffectedDomain = sourceAffectedDomainPair.snd;
            
            List<String> calculatedSourceAffectedDomains = new ArrayList<>();
            List<String> calculatedTargetAffectedDomains = new ArrayList<>();
            
            long upperBoundGridAxis = Long.valueOf(gridIntervals[i].split(RASQL_BOUND_SEPARATION)[1]);
            
            if (isXYAxis) {
                Pair<List<String>, List<String>> separatedPair = this.separateGridDomainByValue(sourceAffectedDomain, MAX_SELECT_GRID_WIDTH_HEIGHT_AXIS, upperBoundGridAxis, targetDownscaledRatio);
                calculatedSourceAffectedDomains = separatedPair.fst;
                calculatedTargetAffectedDomains = separatedPair.snd;
                
            } else {
                // NOTE: non XY axes select with only 1 pixel (e.g: 0:0, 1:1,...)
                Pair<List<String>, List<String>> separatedPair = this.separateGridDomainByValue(sourceAffectedDomain, MAX_SELECT_GRID_OTHER_AXIS, upperBoundGridAxis, BigDecimal.ONE);
                calculatedSourceAffectedDomains = separatedPair.fst;
                calculatedTargetAffectedDomains = separatedPair.snd;
            }
            
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
    private Pair<List<String>, List<String>> separateGridDomainByValue(String gridDomain, long upperValueForSeperation, long upperBoundGridAxis, BigDecimal targetDownscaledRatio) {
        List<String> sourceSeparatedResults = new ArrayList<>();
        List<String> targetSeparatedResults = new ArrayList<>();
        
        long lowerBound = new Long(gridDomain.split(RASQL_BOUND_SEPARATION)[0]);
        long upperBound = new Long(gridDomain.split(RASQL_BOUND_SEPARATION)[1]);
        
        // e:g: domain: 0:20
        while (lowerBound <= upperBound) {
            Long temp = lowerBound + upperValueForSeperation - 1;       
            if (temp < lowerBound) {
                temp = lowerBound;
            }

            // e.g: value = 5, first is: 0:4
            String sourceResult = lowerBound + RASQL_BOUND_SEPARATION + temp;
            String targetResult = (new BigDecimal(lowerBound).multiply(targetDownscaledRatio)).longValue() 
                                + RASQL_BOUND_SEPARATION + new BigDecimal(temp).multiply(targetDownscaledRatio).longValue();
            if (temp >= upperBound) {
                // e.g: value = 5, last is 20:20
                long tempUpperBound = upperBound;
                // NOTE: cannot select out of upper grid bound of source downscaled collection for an axis
                if (upperBound > upperBoundGridAxis) {
                    tempUpperBound = upperBoundGridAxis;
                }
                sourceResult = lowerBound + RASQL_BOUND_SEPARATION + tempUpperBound;
                targetResult = (new BigDecimal(lowerBound).multiply(targetDownscaledRatio)).longValue() 
                                + RASQL_BOUND_SEPARATION + new BigDecimal(upperBound).multiply(targetDownscaledRatio).longValue();
            }
            sourceSeparatedResults.add(sourceResult);
            targetSeparatedResults.add(targetResult);
            
            lowerBound = temp + 1;
        }
        
        return new Pair<>(sourceSeparatedResults, targetSeparatedResults);
    }

    /**
     * Delete a downscaled rasdaman collection which associated with a WCS
     * coverage.
     */
    public void deleteScaleLevel(String coverageId, BigDecimal level, String username, String password) throws PetascopeException, SecoreException {

        Coverage coverage = coverageRepostioryService.readCoverageByIdFromDatabase(coverageId);
        String collectionName = coverage.getRasdamanRangeSet().getCollectionName();
        String downscaledCollectionName = this.createDownscaledCollectionName(collectionName, level);

        RasdamanDownscaledCollection rasdamanScaleDownCollection = coverage.getRasdamanRangeSet().getRasdamanDownscaledCollectionByScaleLevel(level);
        if (rasdamanScaleDownCollection == null) {
            throw new PetascopeException(ExceptionCode.CollectionDoesNotExist,
                    "A downscaled collection of coverage '" + coverageId + "' with level '" + level + "' does not exist to delete.");
        }

        log.info("Dropping downscaled rasdaman collection '" + downscaledCollectionName + "'.");
        try {
            RasUtil.deleteFromRasdaman(downscaledCollectionName, username, password);
        } catch (RasdamanException ex) {
            if (!ex.getMessage().contains("collection name does not exist")) {
                throw ex;
            }
        }

        // Finally, delete this downscaled collection from coverage's list of downscaled rasdaman collection        
        coverage.getRasdamanRangeSet().getRasdamanDownscaledCollections().remove(rasdamanScaleDownCollection);

        // Then save coverage to database
        this.coverageRepostioryService.save(coverage);
    }

    /**
     * Rasdaman downscaled collections have this pattern: coverageId_level.
     * NOTE: level can be float (e.g: 2.5) then collection name will be: test_mr_2_5.
     */
    public String createDownscaledCollectionName(String collectionName, BigDecimal level) {
        String levelStr = BigDecimalUtil.stripDecimalZeros(level).toPlainString().replace(".", "_");
        return collectionName + "_" + levelStr;
    }
}
