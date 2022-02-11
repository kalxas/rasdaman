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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.repository.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.MINUTES;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.collections4.IterableUtils;
import org.rasdaman.domain.cis.Axis;
import org.rasdaman.domain.cis.AxisExtent;
import org.rasdaman.domain.cis.BaseLocalCoverage;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.CoveragePyramid;
import org.rasdaman.domain.cis.DomainSet;
import org.rasdaman.domain.cis.Envelope;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.cis.Field;
import org.rasdaman.domain.cis.GeneralGrid;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeneralGridDomainSet;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.domain.cis.Quantity;
import org.rasdaman.domain.cis.RasdamanRangeSet;
import org.rasdaman.domain.cis.Wgs84BoundingBox;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsUtil;
import org.rasdaman.repository.interfaces.CoverageRepository;
import org.rasdaman.repository.interfaces.EnvelopeByAxisRepository;
import org.rasdaman.repository.interfaces.RasdamanRangeSetRepository;
import org.rasdaman.repository.interfaces.Wgs84BoundingBoxRepository;
import org.springframework.transaction.annotation.Transactional;
import petascope.core.AxisTypes;
import petascope.core.BoundingBox;
import petascope.core.GeoTransform;
import petascope.core.Pair;
import petascope.util.CrsProjectionUtil;
import petascope.util.ListUtil;
import static petascope.util.StringUtil.TEMP_COVERAGE_PREFIX;
import petascope.util.ThreadUtil;
import petascope.util.ras.RasUtil;
import petascope.util.ras.TypeRegistry;
import petascope.util.ras.TypeRegistry.TypeRegistryEntry;


/**
 *
 * This class is the business layer to fetch persisted coverage from database by
 * Repository layer
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
@Transactional
public class CoverageRepositoryService {

    @Autowired
    private CoverageRepository coverageRepository;
    @Autowired
    private EnvelopeByAxisRepository envelopeByAxisRepository;
    @Autowired
    private Wgs84BoundingBoxRepository wgs84BoundingBoxRepository;
    
    @Autowired
    private RasdamanRangeSetRepository rasdamanRangeSetRepository;

    // NOTE: for migration, Hibernate caches the object in first-level cache internally
    // and recheck everytime a new entity is saved, then with thousands of cached objects for nothing
    // it will slow significantly the speed of next saving coverage, then it must be clear this cache.   
    // As targetEntityManagerFactory is set with Primary in bean application of migration application, no need to specify the unitName=target for this PersistenceContext
    @PersistenceContext
    // NOTE: it needs @Transactional annotate at class level or it throw exception when saving coverage because no transaction.
    EntityManager entityManager;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CoverageRepositoryService.class);

    // add coverage to cache (if insert, update, delete coverage) then update cache also
    // NOTE: as Spring Cache with annotation @Cacheable in findAllCoverages will put only 1 key -> list of coverages so
    // it is not good as there is no chance to update or delete one of the cached coverage from this cache, then has to
    // define this map manually.
    // map of qualified coverage id (e.g: hostname:7000:covA) -> coverage
    private static final Map<String, Pair<Coverage, Boolean>> localCoveragesCacheMap = new ConcurrentSkipListMap<>();
    
    /**
     * Store the coverages's extents (only geo-referenced XY axes). First String
     * is minLong, minLat, second String is maxLong, maxLat.
     */
    public static final String COVERAGES_EXTENT_TARGET_CRS_DEFAULT = "EPSG:4326";

    public CoverageRepositoryService() {

    }

    /**
     * Return a pair of coverage object if a coverage id exists from local cache map
     */
    private Pair<Coverage, Boolean> getLocalPairCoverageByCoverageId(String inputCoverageId) throws PetascopeException {
        Pair<Coverage, Boolean> result = null;
        String coverageId = inputCoverageId;
        result = this.localCoveragesCacheMap.get(coverageId);
            
        return result;
    }
    /**
     * Add a coverage id and its coverage object to local cache map
     */
    public void putToLocalCacheMap(String inputCoverageId, Pair<Coverage, Boolean> coveragePair) {
        String coverageId = inputCoverageId;
        this.localCoveragesCacheMap.put(coverageId, coveragePair);
    }
    
    /**
     * Remove from the local cache map by coverage id
     */
    public void removeFromLocalCacheMap(String inputCoverageId) {
        String coverageId = inputCoverageId;
        this.localCoveragesCacheMap.remove(coverageId);
    }
    
    /**
     * Check if a coverage already exists from local loaded cache map
     */
    public boolean isInLocalCache(String coverageId) throws PetascopeException {
        if (localCoveragesCacheMap.isEmpty()) {
            this.readAllLocalCoveragesBasicMetatata();
        }
        
        Pair<Coverage, Boolean> coveragePair = this.getLocalPairCoverageByCoverageId(coverageId);
        return coveragePair != null && coveragePair.fst != null;
    }
    
    /**
     * Check if a coverage id exists in the cache
     */
    public boolean isInCache(String coverageId) throws PetascopeException {
        if (isInLocalCache(coverageId)) {
            return true;
        }
        
        
        return false;
    }

    /**
     * This method is used *only* when read coverage metadata to get another
     * data not from database. If using it to update an input coverage metadata,
     * it will have problem with Hibernate Cascade all.
     *
     */
    public Coverage readCoverageFullMetadataByIdFromCache(String coverageId) throws PetascopeException {
        Coverage coverage = null;
        
        if (this.isInLocalCache(coverageId)) {
            Pair<Coverage, Boolean> coveragePair = this.getLocalPairCoverageByCoverageId(coverageId);
            coverage = coveragePair.fst;
            
            if (coveragePair.snd == false) {
                coverage = this.readCoverageByIdFromDatabase(coverageId);
            }
        }
        if (coverage == null) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "Coverage '" + coverageId + "' does not exist.");
        }
        
        return coverage;
    }

    /**
     * Only read basic coverages's metadata from cache. Used when starting web
     * application as it should not query the whole coverages's metadata.
     */
    public Coverage readCoverageBasicMetadataByIdFromCache(String coverageId) throws PetascopeException {
        
        Coverage coverage = null;
        
        if (this.isInLocalCache(coverageId)) {
            coverage = this.getLocalPairCoverageByCoverageId(coverageId).fst;
        }
        
        
        if (coverage == null) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "Coverage '" + coverageId + "' does not exist.");
        }

        return coverage;
    }
    
    /**
     * Read the basic metadata for coverage object from local cache
     */
    public Coverage readCoverageBasicMetadataByIdFromLocalCache(String coverageId) throws PetascopeException {
        
        if (localCoveragesCacheMap.isEmpty()) {            
            this.readAllLocalCoveragesBasicMetatata();
        }
        
        Coverage result = null;
        Pair<Coverage, Boolean> coveragePair = this.getLocalPairCoverageByCoverageId(coverageId);
        if (coveragePair != null) {
            result = coveragePair.fst;
        }
        
        return result;
    }
    
    /**
     * Read all local coverage objects from database.
     */
    public List<Coverage> readAllLocalCoveragesFromDatabase() {
        List<Coverage> results = IterableUtils.toList(this.coverageRepository.findAll());
        return results;
    }
    
    /**
     * Read a persisted coverage from database by coverage_id (coverage_name).
     * NOTE: used only to insert/update/delete a coverage metadata from database
     * as it is not loaded from cache to avoid error with Hibernate cascade.
     */
    public Coverage readCoverageByIdFromDatabase(String coverageId) throws PetascopeException {
        // This happens when Petascope starts and user sends a WCPS query to a coverage instead of WCS GetCapabilities
        if (localCoveragesCacheMap.isEmpty()) {
            this.readAllLocalCoveragesBasicMetatata();
        }
        
        long start = System.currentTimeMillis();

        Coverage coverage = this.coverageRepository.findOneByCoverageId(coverageId);
        
        long end = System.currentTimeMillis();
        log.debug("Time to read coverage '" + coverageId + "' from database is " + String.valueOf(end - start) + " ms.");
        
        if (coverage == null) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "Coverage '" + coverageId + "' does not exist in local database.");
        } else {
            log.debug("Coverage '" + coverageId + "' is read from database.");

            this.addRasdamanDataTypesForRangeQuantities(coverage);

            if (coverage.getRasdamanRangeSet().getTiling() == null) {
                this.addRasdamanTilingConfiguration(coverage);
            }
        }
        
        // e.g: vm1:7000:covA
        this.putToLocalCacheMap(coverageId, new Pair<>(coverage, true));
        
        // NOTE: without it, after coverage's crs is replaced from $SECORE_URL$ to localhost:8080 (from petascope.properties)
        // with a DescribeCoverage request, after the replacement, 
        // it will save coverage's crs with localhost:8080 instead of the placeholder $SCORE_URL$ in database.
        entityManager.clear();
        
        // NOTE: As coverage is saved with a placeholder for SECORE prefix, so after reading coverage from database, 
        // replace placeholder with SECORE configuration endpoint from petascope.properties.
        CoverageRepositoryService.addCrsPrefix(coverage);

        return coverage;
    }

    /**
     * Return map of local coverageId -> rasdaman rangeset of this coverage
     */
    public Map<String, RasdamanRangeSet> getAllLocalRasdamanRangeSetsMap() {
        Map<String, RasdamanRangeSet> result = new LinkedHashMap<>();
        List<Object[]> list = this.coverageRepository.readAllRasdamanRangeSets();
        for (Object[] objs : list) {
            String coverageId = (String) objs[0];
            long rasdamanRangeSetId = (long) objs[1];
            String collectionName = (String) objs[2];
            String collectionType = (String) objs[3];
            String mddType = (String) objs[4];
            String tiling = (String) objs[5];
            
            RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
            rasdamanRangeSet.setId(rasdamanRangeSetId);
            rasdamanRangeSet.setCollectionType(collectionType);
            rasdamanRangeSet.setCollectionName(collectionName);
            rasdamanRangeSet.setMddType(mddType);
            rasdamanRangeSet.setTiling(tiling);
            
            result.put(coverageId, rasdamanRangeSet);
        }
        
        return result;
        
    } 
    
    /**
     * Calculate the size of a coverage in bytes from number of pixels and number of bits per band.
     */
    public long calculateCoverageSizeInBytes(Coverage coverage) throws PetascopeException {
        long result = 0;
        
        String setType = coverage.getRasdamanRangeSet().getCollectionType();
        if (setType != null) {
            TypeRegistryEntry typeEntry = null;
            try {
                typeEntry = TypeRegistry.getInstance().getTypeEntry(setType);
            } catch (PetascopeException ex) {
                log.warn("Cannot find type entry from registry for set type '" + setType + "'. Reason: " + ex.getMessage());
            }
            
            if (typeEntry != null) {            
                List<String> bandsTypes = typeEntry.getBandsTypes();
                List<Byte> bandsSizes = typeEntry.getBandsSizesInBytes(bandsTypes);
                
                GeneralGridCoverage geneneralGridCoverage = (GeneralGridCoverage)coverage;
                List<Object[]> gridBounds = new ArrayList<>();
                if (geneneralGridCoverage.getDomainSet() != null) {
                    // for full coverage object
                    List<IndexAxis> indexAxes = geneneralGridCoverage.getIndexAxes();
                    for (IndexAxis indexAxis : indexAxes) {
                        Object[] objs = {indexAxis.getLowerBound(), indexAxis.getUpperBound()};
                        gridBounds.add(objs);
                    }
                } else {
                    // for basic coverage object without domainset
                    gridBounds = coverageRepository.readGridBoundsByCoverageId(coverage.getCoverageId());
                }

                long totalPixels = 0;

                for (Object[] lowerUpperBounds : gridBounds) {
                    long lowerBound = new Long(lowerUpperBounds[0].toString());
                    long upperBound = new Long(lowerUpperBounds[1].toString());
                    if (totalPixels == 0) {
                        totalPixels = 1;
                    }
                    totalPixels *= (upperBound - lowerBound + 1);
                }

                for (Byte bandSize : bandsSizes) {
                    result += totalPixels * bandSize;
                }
            }
        } else {
            log.warn("Cannot find rasdaman set type for coverage '" + coverage.getCoverageId() + "'.");
        }
               
        return result;
    }
    
    
    /**
     * This persists rasdaman data types for range quantities as they were not saved to petascopedb when inserting coverages
     * before v9.8.
     */
    private void addRasdamanDataTypesForRangeQuantities(Coverage coverage) throws PetascopeException {
        List<Field> fields = coverage.getRangeType().getDataRecord().getFields();
        String setType = coverage.getRasdamanRangeSet().getCollectionType();
        
        Field firstField = fields.get(0);
        
        if (firstField.getQuantity().getDataType() == null) {
            // Need to update rasdaman type for range
            TypeRegistryEntry typeEntry = TypeRegistry.getInstance().getTypeRegistry().get(setType);
            List<String> bandsTypes = typeEntry.getBandsTypes();
            
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                Quantity quantity = field.getQuantity();
                
                if (bandsTypes.size() == 1) {
                    // primitive cell type
                    quantity.setDataType(bandsTypes.get(0));
                } else {
                    // struct cell type
                    quantity.setDataType(bandsTypes.get(i));
                }
             }
            
            this.save(coverage);
        }
    }
    
    /**
     * check if rasdaman's collection tile configuration of this coverage is null or not,
     * if it is null, provide the value from rasql.
     */
    private void addRasdamanTilingConfiguration(Coverage coverage) {       
        if (coverage.getRasdamanRangeSet().getTiling() == null) {
            // e.g: [0:500,0:500] ALIGNED 4194304
            String collectionName = coverage.getRasdamanRangeSet().getCollectionName();
            try {
                String tiling = RasUtil.retrieveTilingInfo(collectionName);
                coverage.getRasdamanRangeSet().setTiling(tiling);                
                // Then update coverage to database
                this.save(coverage);
            } catch (PetascopeException ex) {
                // NOTE: In case of removing coverage without updated tiling in coverage object and rasdaman collection was deleted manually, 
                // this causes exception as no collection found and it can be ignored.
                log.warn("Cannot retrieve tiling from collection '" + collectionName + "'. Reason: " + ex.getMessage(), ex);
            }
        }
    }
    

    /**
     * Read all coverage's basic metadata for WCS GetCapabilities request. Why?
     * because it is too large to read all coverage's metadata in one request
     * from database. Only when a coverage is needed to read metadata such as:
     * DescribeCoverage, GetCoverage request then it should read this coverage
     * and put to cache.
     *
     * basic metadata for GetCapabilities, e.g:
     *
     * <CoverageId xmlns="http://www.opengis.net/wcs/2.0">test_wms_4326</CoverageId>
     * <CoverageSubtype xmlns="http://www.opengis.net/wcs/2.0">RectifiedGridCoverage</CoverageSubtype>
     * <ows:BoundingBox xmlns:ows="http://www.opengis.net/ows/2.0" crs="http://localhost:8080/def/crs/EPSG/0/4326" dimensions="2">
     * <ows:LowerCorner>-44.575 111.975</ows:LowerCorner>
     * <ows:UpperCorner>-8.975 156.375</ows:UpperCorner>
     * </ows:BoundingBox>
     * </wcs:CoverageSummary>
     *
     * @return List<Pair<Coverage, Boolean>>
     */
    public List<Pair<Coverage, Boolean>> readAllLocalCoveragesBasicMetatata() throws PetascopeException {
        long start = System.currentTimeMillis();
        
        if (this.localCoveragesCacheMap.isEmpty()) {
            
            Map<String, RasdamanRangeSet> localCoverageIdRasdamanRangeSetMap = this.getAllLocalRasdamanRangeSetsMap();
            List<BaseLocalCoverage> baseCoverages = this.coverageRepository.readAllBasicCoverageMetadatas();
            
            long end = System.currentTimeMillis();
            
            log.info("Time to load all basic local coverage from database is " + (end - start) +  " ms.");
            
            // NOTE: to update SECORE_URL of Envelope = the one configured in petascope.properties without persisting this configured URL to database
            entityManager.clear();
            for (BaseLocalCoverage baseCoverage : baseCoverages) {
                String coverageId = baseCoverage.getCoverageId();
                                
                RasdamanRangeSet rasdamanRangeSet = localCoverageIdRasdamanRangeSetMap.get(coverageId);
                Coverage coverage = new GeneralGridCoverage(baseCoverage);
                coverage.setRasdamanRangeSet(rasdamanRangeSet);
                
                this.addCrsPrefix(coverage);
                
                this.localCoveragesCacheMap.put(coverageId, new Pair<>(coverage, false));
            }
            
            log.debug("Time to read all base local coverages from database is: " + String.valueOf(end - start) + " ms.");
        }
        
        List<Pair<Coverage, Boolean>> coverages = new ArrayList<>(localCoveragesCacheMap.values());

        long end = System.currentTimeMillis();
        log.debug("Time to read all local basic coverages is: " + String.valueOf(end - start) + " ms.");

        return coverages;
    }
    
    public List<Pair<Coverage, Boolean>> readAllLocalCoveragesBasicMetatataFromCache() {
        List<Pair<Coverage, Boolean>> results = new ArrayList<>();
        for (Pair<Coverage, Boolean> pair : this.localCoveragesCacheMap.values()) {
            if (pair != null) {
                results.add(pair);
            }
        }
        
        return results;
    }
    
    /**
     * Return the coverage object from the local cache
     */
    public Coverage readCoverageFromLocalCache(String coverageId) throws PetascopeException {
        if (this.localCoveragesCacheMap.isEmpty()) {
            this.readAllLocalCoveragesBasicMetatata();
        }
        
        Coverage result = null;
        Pair<Coverage, Boolean> coveragePair = this.getLocalPairCoverageByCoverageId(coverageId);
        if (coveragePair != null) {
            result = coveragePair.fst;
        }
        
        return result;
    }
    
    /**
     * Return the list of basic coverage metadata object
     */
    public List<Coverage> readAllCoveragesBasicMetadata() throws PetascopeException {
        
        if (this.localCoveragesCacheMap.isEmpty()) {
            this.readAllLocalCoveragesBasicMetatata();
        }        
        
        List<Coverage> coverages = new ArrayList<>();
        for (Map.Entry<String, Pair<Coverage, Boolean>> entry : this.localCoveragesCacheMap.entrySet()) {
            coverages.add(entry.getValue().fst);
        }
        
        return coverages;
    }


    /**
     * Create a coverage's extent by cached coverage's metadata
     * (EnvelopeByAxis). The XY axes' BoundingBox is reprojected to EPSG:4326.
     */
    public void createCoverageExtent(Coverage coverage) throws PetascopeException, SecoreException {
        Wgs84BoundingBox wgs84BoundingBox = null;
        String coverageId = coverage.getCoverageId();

        addCrsPrefix(coverage);
        BoundingBox bbox = ((GeneralGridCoverage) coverage).getEnvelope().getEnvelopeByAxis().getGeoXYBoundingBox();

        // Don't transform the XY extents to EPSG:4326 if it is not EPSG code or it is not at least 2D
        if (bbox != null) {       

            if (coverage.getDomainSet() == null) {
                // NOTE: this one is used only for older peernode petascope which doesn't have Wgs84BoundingBox object in envelope
                // It returns less precisely bounding box, but it is used for backward compatibility               
                try {
                    wgs84BoundingBox = CrsProjectionUtil.createLessPreciseWgs84BBox(coverage.getEnvelope().getEnvelopeByAxis());
                } catch(Exception ex) {
                    log.warn("Cannot create WGS84 bounding box for coverage '" + coverageId + "'. Reason: " + ex.getMessage());
                }
            }  else {            
                Pair<GeoAxis, GeoAxis> xyAxesPair = ((GeneralGridCoverage)coverage).getXYGeoAxes();
                GeoAxis geoAxisX = xyAxesPair.fst;
                IndexAxis indexAxisX = ((GeneralGridCoverage)coverage).getIndexAxisByName(geoAxisX.getAxisLabel());
                GeoAxis geoAxisY = xyAxesPair.snd;
                IndexAxis indexAxisY = ((GeneralGridCoverage)coverage).getIndexAxisByName(geoAxisY.getAxisLabel());

                if (CrsUtil.getEPSGCode(bbox.getGeoXYCrs()).equals(COVERAGES_EXTENT_TARGET_CRS_DEFAULT)) {
                    wgs84BoundingBox = new Wgs84BoundingBox(geoAxisX.getLowerBoundNumber(), geoAxisY.getLowerBoundNumber(), 
                                                            geoAxisX.getUpperBoundNumber(), geoAxisY.getUpperBoundNumber());
                } else {
                    // coverage in different CRS than EPSG:4326
                    int epsgCode = CrsUtil.getEpsgCodeAsInt(bbox.getGeoXYCrs());
                    int gridWidth = (int)(indexAxisX.getUpperBound() - indexAxisX.getLowerBound() + 1);
                    int gridHigh = (int)(indexAxisY.getUpperBound() - indexAxisY.getLowerBound() + 1);
                    GeoTransform sourceGeoTransform = new GeoTransform(epsgCode, geoAxisX.getLowerBoundNumber(), geoAxisY.getUpperBoundNumber(), 
                                                                       gridWidth, gridHigh, geoAxisX.getResolution(), geoAxisY.getResolution());

                    try {
                        BoundingBox bboxTmp = CrsProjectionUtil.transform(sourceGeoTransform, COVERAGES_EXTENT_TARGET_CRS_DEFAULT);
                        BigDecimal lonMin = bboxTmp.getXMin();
                        BigDecimal latMin = bboxTmp.getYMin();
                        BigDecimal lonMax = bboxTmp.getXMax();
                        BigDecimal latMax = bboxTmp.getYMax();

                        if (lonMin.compareTo(new BigDecimal("-180")) < 0) {
                            lonMin = new BigDecimal("-180");
                        }
                        if (latMin.compareTo(new BigDecimal("-90")) < 0) {
                            latMin = new BigDecimal("-90");
                        }                
                        if (lonMax.compareTo(new BigDecimal("180")) > 0) {
                            lonMax = new BigDecimal("180");
                        }
                        if (latMax.compareTo(new BigDecimal("90")) > 0) {
                            latMax = new BigDecimal("90");
                        }

                        // Created WGS84 bbox for coverage, then, persist to database
                        wgs84BoundingBox = new Wgs84BoundingBox(lonMin, latMin, lonMax, latMax);                    

                    } catch (Exception ex) {
                        log.warn("Cannot create extent for coverage '" + coverage.getCoverageId() + "', error from crs transform '" + ex.getMessage() + "'.");
                    }
                }
            }
        }
        
        if (wgs84BoundingBox != null) {
            if (coverage.getEnvelope().getEnvelopeByAxis().getWgs84BBox() == null) {
                // If coverage doesn't have WGS84BBox, insert a row to database value
                coverage.getEnvelope().getEnvelopeByAxis().setWgs84BBox(wgs84BoundingBox);
            } else {
                // NOTE: In case coverage has WGS84BBox, update its values to update row to database table instead of inserting a new row
                coverage.getEnvelope().getEnvelopeByAxis().getWgs84BBox().set(wgs84BoundingBox);
            }
        }
    }
    
    /**
     * Add a new coverage to database
     */
    public void add(Coverage coverage) throws PetascopeException {
        String coverageId = coverage.getCoverageId();
        if (this.isInLocalCache(coverageId)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Coverage '" + coverageId  + "' already exists in database.");
        } else {
            // add the new coverage to the database
            this.save(coverage);     
        }        
    }

    /**
     * Persit a coverage to database if it is new or existing coverage
     */
    @Transactional
    public void save(Coverage coverage) throws PetascopeException {
        String coverageId = coverage.getCoverageId();
        
        if (coverageId.startsWith(TEMP_COVERAGE_PREFIX)) {
            // don't push temp coverage to database
            return;
        }
        
        // NOTE: Don't save coverage with fixed CRS (e.g: http://localhost:8080/def/crs/epsg/0/4326)
        // it must use a string placeholder so when setting up Petascope with a different SECORE endpoint, it will replace the placeholder
        // with new SECORE endpoint or otherwise the persisted URL will throw exception as non-existing URL.
        CoverageRepositoryService.removeCrsPrefix(coverage);
        
        long coverageSize = this.calculateCoverageSizeInBytes(coverage);
        coverage.setCoverageSizeInBytes(coverageSize);
        
        long start = System.currentTimeMillis();
        // then it can save (insert/update) the coverage to database
        this.coverageRepository.save(coverage);
        long end = System.currentTimeMillis();
        log.debug("Time to persist coverage '" + coverageId + "' is " + String.valueOf(end - start) + " ms.");

        entityManager.flush();
        entityManager.clear();
        
        CoverageRepositoryService.addCrsPrefix(coverage);

        localCoveragesCacheMap.put(coverageId, new Pair(coverage, true));
        
        log.debug("Coverage '" + coverageId + "' is persisted in database.");
    }

    /**
     * Delete a persisted coverage by id
     *
     * @param coverage
     */
    @Transactional
    public void delete(Coverage coverage) {
        String coverageId = coverage.getCoverageId();
        this.coverageRepository.delete(coverage);

        // Remove the cached coverage from cache
        this.removeFromLocalCacheMap(coverageId);

        entityManager.flush();
        entityManager.clear();

        log.debug("Coverage: " + coverage.getCoverageId() + " is removed from database.");
    }

    /**
     * NOTE: Don't save coverage with fixed CRS (e.g:
     * http://localhost:8080/def/crs/epsg/0/4326) It must use a string
     * placeholder so when setting up Petascope with a different SECORE
     * endpoint, it will replace the placeholder with new SECORE endpoint or
     * otherwise the persisted URL will throw exception as non-existing URL.
     *
     * @param coverage
     */
    public static void removeCrsPrefix(Coverage coverage) {
        // + Start with EnvelopeByAxis first
        EnvelopeByAxis envelopeByAxis = coverage.getEnvelope().getEnvelopeByAxis();
        String strippedCRS = CrsUtil.CrsUri.toDbRepresentation(envelopeByAxis.getSrsName());
        envelopeByAxis.setSrsName(strippedCRS);

        for (AxisExtent axisExtent : envelopeByAxis.getAxisExtents()) {
            strippedCRS = CrsUtil.CrsUri.toDbRepresentation(axisExtent.getSrsName());
            axisExtent.setSrsName(strippedCRS);
        }

        // + Then with geoAxes of GeneralGridDomainSet as only support GeneralGridCoverage now
        DomainSet generalGridDomainSet = ((GeneralGridCoverage) coverage).getDomainSet();
        if (generalGridDomainSet != null) {
            GeneralGrid generalGrid = ((GeneralGridDomainSet) generalGridDomainSet).getGeneralGrid();
            generalGrid.setSrsName(CrsUtil.CrsUri.toDbRepresentation(generalGrid.getSrsName()));
            for (Axis geoAxis : generalGrid.getGeoAxes()) {
                strippedCRS = CrsUtil.CrsUri.toDbRepresentation(geoAxis.getSrsName());
                geoAxis.setSrsName(strippedCRS);
            }
        }
    }

    /**
     * NOTE: As coverage is saved with a placeholder for SECORE prefix, so after
     * reading coverage from database, replace placeholder with SECORE
     * configuration endpoint from petascope.properties.
     *
     */
    public static void addCrsPrefix(Coverage coverage) {
        // + Start with EnvelopeByAxis first
        EnvelopeByAxis envelopeByAxis = coverage.getEnvelope().getEnvelopeByAxis();
        String addedCRS = CrsUtil.CrsUri.fromDbRepresentation(envelopeByAxis.getSrsName());
        envelopeByAxis.setSrsName(addedCRS);

        for (AxisExtent axisExtent : envelopeByAxis.getAxisExtents()) {
            addedCRS = CrsUtil.CrsUri.fromDbRepresentation(axisExtent.getSrsName());
            axisExtent.setSrsName(addedCRS);
        }
        
        if (coverage.getDomainSet() != null) {
            // for full Coverage metadata            
            DomainSet generalGridDomainSet = ((GeneralGridCoverage) coverage).getDomainSet();
            GeneralGrid generalGrid = ((GeneralGridDomainSet) generalGridDomainSet).getGeneralGrid();
            for (Axis geoAxis : generalGrid.getGeoAxes()) {
                addedCRS = CrsUtil.CrsUri.fromDbRepresentation(geoAxis.getSrsName());
                geoAxis.setSrsName(addedCRS);
            }
        }
    }
    
    /**
     * Update a local coverage's information (id and metadata)
     * e.g. covA -> covB
     */
    public void updateCoverage(String currentCoverageId, String newCoverageId, String newMetadata) throws PetascopeException {
        if (newCoverageId != null || newMetadata != null) {
            Coverage coverage = this.readCoverageByIdFromDatabase(currentCoverageId);

            if (newCoverageId != null) {
                coverage.setCoverageId(newCoverageId);
                this.localCoveragesCacheMap.remove(currentCoverageId);            
            }
            if (newMetadata != null) {
                coverage.setMetadata(newMetadata);
            }

            this.save(coverage);

            if (newMetadata != null) {
                log.info("Updated new metadata for coverage '" + currentCoverageId + "'.");
            }        
            if (newCoverageId != null) {
                log.info("Renamed coverage id from '" + currentCoverageId + "' to '" + newCoverageId + "'.");
            }
        }
    }
    
    /**
     * Persist Wgs84BBox of an EnvelopeByAxis to database
     */
    public void saveWgs84BBox(EnvelopeByAxis envelopeByAxis) throws PetascopeException {
        Wgs84BoundingBox wgs84BBox = envelopeByAxis.getWgs84BBox();
        if (wgs84BBox != null) {
            this.wgs84BoundingBoxRepository.save(wgs84BBox);
            // NOTE: flush() to persis Wgs84BBox to database first
            // for the case with insert, when the generated id as primary key is not set. After persisting to database, then it has the id
            this.entityManager.flush();
            this.envelopeByAxisRepository.saveWgs84BBox(envelopeByAxis.getId(), envelopeByAxis.getWgs84BBox());
        }
    }
    
    /**
     * Persist coverageSizeInBytes to database of the selected coverage
     */
    @Transactional
    public void saveCoverageSizeInBytes(Coverage localCoverage) {
        long coverageAutoId = localCoverage.getId();
        long coverageSizeInBytes = localCoverage.getCoverageSizeInBytes();
        this.coverageRepository.saveCoverageSizeInBytes(coverageAutoId, coverageSizeInBytes);
    }
    
    /**
     * Persist inspireMetadataURL to database of the selected coverage
     */
    @Transactional
    public void saveCoverageInspireMetadataURL(Coverage localCoverage) {
        long coverageAutoId = localCoverage.getId();
        String inspireMetadataURL = localCoverage.getInspireMetadataURL();
        this.coverageRepository.saveCoverageInspireMetadataURL(coverageAutoId, inspireMetadataURL);
    }

    // For migration only
    /**
     * Migrate Legacy coverage's only as it will not throw exception if coverage
     * does not exist in CIS data model
     *
     * @param coverageId
     * @return
     */
    public boolean coverageIdExist(String coverageId) {
        long start = System.currentTimeMillis();
        Coverage coverage = this.coverageRepository.findOneByCoverageId(coverageId);
        long end = System.currentTimeMillis();

        log.debug("Time to find existing coverage: " + String.valueOf(end - start));

        return coverage != null;
    }

    /**
     * Just fetch all the coverage Ids in a list from local database
     */
    public List<String> readAllLocalCoverageIds() {
        List<String> coverageIds = this.coverageRepository.readAllCoverageIds();

        return coverageIds;
    }
    
    /**
     * Fetch all the collection types names from rasdaman_range_set table
     */
    public boolean collectionTypeExist(String collectionType) {
        int numberOfCollectionTypes = this.rasdamanRangeSetRepository.collectionTypeExists(collectionType);        
        return (numberOfCollectionTypes > 0);
    }
}
