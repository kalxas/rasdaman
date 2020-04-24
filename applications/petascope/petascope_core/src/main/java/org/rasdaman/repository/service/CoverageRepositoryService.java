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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.rasdaman.domain.cis.Axis;
import org.rasdaman.domain.cis.AxisExtent;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.DomainSet;
import org.rasdaman.domain.cis.Envelope;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.cis.Field;
import org.rasdaman.domain.cis.GeneralGrid;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeneralGridDomainSet;
import org.rasdaman.domain.cis.Quantity;
import org.rasdaman.domain.cis.RasdamanRangeSet;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsUtil;
import org.rasdaman.repository.interfaces.CoverageRepository;
import org.rasdaman.repository.interfaces.RasdamanRangeSetRepository;
import org.springframework.transaction.annotation.Transactional;
import petascope.core.AxisTypes;
import petascope.core.BoundingBox;
import petascope.core.Pair;
import petascope.util.CrsProjectionUtil;
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
    public static final Map<String, Pair<Coverage, Boolean>> coveragesCacheMap = new ConcurrentSkipListMap<>();

    /**
     * Store the coverages's extents (only geo-referenced XY axes). First String
     * is minLong, minLat, second String is maxLong, maxLat.
     */
    public static final Map<String, BoundingBox> coveragesExtentsCacheMap = new ConcurrentHashMap<>();
    public static final String COVERAGES_EXTENT_TARGET_CRS_DEFAULT = "EPSG:4326";
    
    // Any geo coverages which cannot project its geo bounding box to EPSG:4326 
    // will be ignored to not show warn log in petascope.log multiple times
    public static final Set<String> problemCoveragesExtentsCache = new HashSet<>();

    public CoverageRepositoryService() {

    }
    
    /**
     * Check if a coverage already exists from local loaded cache map
     */
    public boolean isInLocalCache(String coverageId) throws PetascopeException {
        if (coveragesCacheMap.isEmpty()) {
            this.readAllLocalCoveragesBasicMetatata();
        }
        
        if (!coveragesCacheMap.containsKey(coverageId)) {
            return false;
        }
        
        return true;
    }

    /**
     * This method is used *only* when read coverage metadata to get another
     * data not from database. If using it to update an input coverage metadata,
     * it will have problem with Hibernate Cascade all.
     *
     * @param coverageId
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public Coverage readCoverageFullMetadataByIdFromCache(String coverageId) throws PetascopeException {
        Pair<Coverage, Boolean> coveragePair = coveragesCacheMap.get(coverageId);
        Coverage coverage = null;
        // Check if coverage is already cached
        if (coveragePair == null) {
            // If coverage is not cached then read it from database
            coverage = this.readCoverageByIdFromDatabase(coverageId);
            coveragesCacheMap.put(coverageId, new Pair<>(coverage, true));
        } else if (coveragePair.snd == false) {
            // If coverage just contains basic metadata, then read it from database
            coverage = this.readCoverageByIdFromDatabase(coverageId);
            coveragesCacheMap.put(coverageId, new Pair<>(coverage, true));
        } else {
            // Coverage already exists in the cache.
            coverage = coveragePair.fst;
        }
        
        return coverage;
    }

    /**
     * Only read basic coverages's metadata from cache. Used when starting web
     * application as it should not query the whole coverages's metadata.
     */
    public Coverage readCoverageBasicMetadataByIdFromCache(String coverageId) throws PetascopeException {
        
        Coverage coverage = null;
        
        if (coveragesCacheMap.isEmpty()) {            
            this.readAllLocalCoveragesBasicMetatata();
        }
        
        if (coveragesCacheMap.get(coverageId) != null) {
            coverage = coveragesCacheMap.get(coverageId).fst;
        } else {
            // NOTE: if the cache map doesn't contain the coverage for some reason
            // then try again (this happened for WMS GetCapabitilies requests from wsclient when pestascope starts and throws coverage not found)
            coverage = this.readCoverageFullMetadataByIdFromCache(coverageId);
        }
        
        if (coverage == null) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "Coverage: " + coverageId + " does not exist.");
        }
        
        return coverage;
    }

    /**
     * Read a persisted coverage from database by coverage_id (coverage_name).
     * NOTE: used only to insert/update/delete a coverage metadata from database
     * as it is not loaded from cache to avoid error with Hibernate cascade.
     *
     * @param coverageId
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public Coverage readCoverageByIdFromDatabase(String coverageId) throws PetascopeException {
        
        // This happens when Petascope starts and user sends a WCPS query to a coverage instead of WCS GetCapabilities
        if (coveragesCacheMap.isEmpty()) {
            this.readAllLocalCoveragesBasicMetatata();
        }
        
        long start = System.currentTimeMillis();

        Coverage coverage = this.coverageRepository.findOneByCoverageId(coverageId);
        
        long end = System.currentTimeMillis();
        log.debug("Time to read coverage '" + coverageId + "' from database is " + String.valueOf(end - start) + " ms.");

        if (coverage == null) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "Coverage '" + coverageId + "' does not exist in persistent database.");
        }
        
        log.debug("Coverage '" + coverageId + "' is read from database.");
        
        long coverageSize = this.calculateCoverageSizeInBytes(coverage);
        coverage.setCoverageSizeInBytes(coverageSize);
        this.addRasdamanDataTypesForRangeQuantities(coverage);
        this.addRasdamanTilingConfiguration(coverage);
        
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

                List<Object[]> gridBounds = coverageRepository.readGridBoundsByCoverageId(coverage.getCoverageId());

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
     * Read a basic coverage metadata from local database.
     */
    private void readCoverageBasicMetadataFromDatabase(String coverageId, String coverageType) throws PetascopeException {
        // Each coverage has only 1 envelope and each envelope has only 1 envelopeByAxis
        EnvelopeByAxis envelopeByAxis = coverageRepository.readEnvelopeByAxisByCoverageId(coverageId);
        // NOTE: this coverage object will update CRS with current configured SECORE URL (e.g: http://localhost:8080/def) in petascope.propeties
        // don't let Hibernate see this object is updated and it will update this change to petascopedb as it should keep the pattern $SECORE_URL$/crs in petascopedb.
        entityManager.clear();

        // NOTE: replace the abstract SECORE url in database first ($SECORE$/crs -> localhost:8080/def/crs)
        envelopeByAxis.setSrsName(CrsUtil.CrsUri.fromDbRepresentation(envelopeByAxis.getSrsName()));
        // also with AxisExtents of EnvelopeByAxis
        for (AxisExtent axisExtent : envelopeByAxis.getAxisExtents()) {
            axisExtent.setSrsName(CrsUtil.CrsUri.fromDbRepresentation(axisExtent.getSrsName()));
        }

        Coverage coverage = new GeneralGridCoverage();
        coverage.setCoverageId(coverageId);
        coverage.setCoverageType(coverageType);
        Envelope envelope = new Envelope();
        envelope.setEnvelopeByAxis(envelopeByAxis);
        ((GeneralGridCoverage) coverage).setEnvelope(envelope);
        
        RasdamanRangeSet rasdamanRangeSet = this.coverageRepository.readRasdamanRangeSet(coverageId);
        coverage.setRasdamanRangeSet(rasdamanRangeSet);

        long coverageSize = this.calculateCoverageSizeInBytes(coverage);
        coverage.setCoverageSizeInBytes(coverageSize);
        
        // Then cache the read coverage's basic metadata
        coveragesCacheMap.put(coverage.getCoverageId(), new Pair<>(coverage, false));
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

        List<Pair<Coverage, Boolean>> coverages = new ArrayList<>();
        List<Object[]> coverageIdsAndTypes = coverageRepository.readAllCoverageIdsAndTypes();
        for (Object[] coverageIdAndType : coverageIdsAndTypes) {
            String coverageId = coverageIdAndType[0].toString();
            String coverageType = coverageIdAndType[1].toString();
            
            if (!coveragesCacheMap.containsKey(coverageId)) {
                try {
                    this.readCoverageBasicMetadataFromDatabase(coverageId, coverageType);

                } catch (Exception ex) {
                    throw new PetascopeException(ExceptionCode.InternalSqlError,
                                                 "Cannot read coverage basic metadata '" + coverageId + "' from database. Reason: " + ex.getMessage(), ex);
                }
            }
        }

        // Read all coverage from persistent database
        log.debug("Read all persistent coverages from database.");
        coverages = new ArrayList<>(coveragesCacheMap.values());

        long end = System.currentTimeMillis();
        log.debug("Time to read all coverages is: " + String.valueOf(end - start) + " ms.");

        return coverages;
    }

    /**
     * From the cached coverages's basic metadata (EnvelopeByAxis with
     * AxisExtents), create the coverages's extents by reprojecting from native
     * XY axes's CRS to EPSG:4326 and cache all the results (minLongLat,
     * maxLongLat). NOTE: Only add coverage with native CRS EPSG code as GDAL
     * cannot reproject unknown CRS.
     *
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.SecoreException
     */
    public void createAllCoveragesExtents() throws PetascopeException, SecoreException {
        long start = System.currentTimeMillis();
        for (String coverageId : coveragesCacheMap.keySet()) {
            if (!coveragesExtentsCacheMap.containsKey(coverageId)) {
                try {
                    this.createCoverageExtent(coverageId);
                } catch (Exception ex) {
                    log.warn("Cannot create geo extents in EPSG:4326 for coverage '" + coverageId + "'. Reason: " + ex.getMessage(), ex);
                }
            }
        }

        long end = System.currentTimeMillis();
        log.debug("Time to compute all coverage extents is " + String.valueOf(end - start) + " ms.");
    }

    /**
     * Create a coverage's extent by cached coverage's metadata
     * (EnvelopeByAxis). The XY axes' BoundingBox is reprojected to EPSG:4326.
     */
    public void createCoverageExtent(String coverageId) throws PetascopeException, SecoreException {
        if (this.problemCoveragesExtentsCache.contains(coverageId)) {
            // this coverage has problem to create coverage extent, ignore it
            return;
        }
        
        // Only need a coverage's basic metadata, it is slow to query the whole coverage's metadata
        Coverage coverage = this.readCoverageBasicMetadataByIdFromCache(coverageId);
        List<AxisExtent> axisExtents = ((GeneralGridCoverage) coverage).getEnvelope().getEnvelopeByAxis().getAxisExtents();
        boolean foundX = false, foundY = false;
        BigDecimal xMin = null, yMin = null, xMax = null, yMax = null;
        String xyAxesCRS = null;
        String coverageCRS = coverage.getEnvelope().getEnvelopeByAxis().getSrsName();
        
        int i = 0;
        for (AxisExtent axisExtent : axisExtents) {
            String axisExtentCrs = axisExtent.getSrsName();
            // NOTE: the basic coverage metadata can have the abstract SECORE URL, so must replace it first
            axisExtentCrs = CrsUtil.CrsUri.fromDbRepresentation(axisExtentCrs);
            
            if (axisExtentCrs.contains(CrsUtil.EPSG_AUTH)) {
                // x, y
                String axisType = CrsUtil.getAxisTypeByIndex(coverageCRS, i);
                if (axisType.equals(AxisTypes.X_AXIS)) {
                    foundX = true;
                    xMin = new BigDecimal(axisExtent.getLowerBound());
                    xMax = new BigDecimal(axisExtent.getUpperBound());
                    xyAxesCRS = axisExtentCrs;
                } else if (axisType.equals(AxisTypes.Y_AXIS)) {
                    foundY = true;
                    yMin = new BigDecimal(axisExtent.getLowerBound());
                    yMax = new BigDecimal(axisExtent.getUpperBound());
                }
                if (foundX && foundY) {
                    break;
                }
            }
            
            i++;
        }

        // Don't transform the XY extents to EPSG:4326 if it is not EPSG code or it is not at least 2D
        BoundingBox boundingBox = null;
        if (foundX && foundY && CrsUtil.isValidTransform(xyAxesCRS)) {
            if (xyAxesCRS.contains(CrsUtil.WGS84_EPSG_CODE)) {
                // coverage already in EPSG:4326
                boundingBox = new BoundingBox(xMin, yMin, xMax, yMax);                
            } else {            
                // coverage in different CRS than EPSG:4326
                double[] xyMinArray = {xMin.doubleValue(), yMin.doubleValue()};
                double[] xyMaxArray = {xMax.doubleValue(), yMax.doubleValue()};
                List<BigDecimal> minLongLatList = null, maxLongLatList = null;

                try {
                    // NOTE: cannot reproject every kind of EPSG:codes to EPSG:4326, but should not stop application for this.
                    minLongLatList = CrsProjectionUtil.transform(xyAxesCRS, COVERAGES_EXTENT_TARGET_CRS_DEFAULT, xyMinArray);
                    maxLongLatList = CrsProjectionUtil.transform(xyAxesCRS, COVERAGES_EXTENT_TARGET_CRS_DEFAULT, xyMaxArray);

                    // NOTE: EPSG:4326 cannot display outside of coordinates for lat(-90, 90), lon(-180, 180) and it should only contain 5 precisions.                
                    BigDecimal lonMin = minLongLatList.get(0).setScale(5, BigDecimal.ROUND_HALF_UP);
                    BigDecimal latMin = minLongLatList.get(1).setScale(5, BigDecimal.ROUND_HALF_UP);
                    BigDecimal lonMax = maxLongLatList.get(0).setScale(5, BigDecimal.ROUND_HALF_UP);
                    BigDecimal latMax = maxLongLatList.get(1).setScale(5, BigDecimal.ROUND_HALF_UP);

                    if (lonMin.compareTo(new BigDecimal("-180")) < 0) {
                        if (lonMin.compareTo(new BigDecimal("-180.5")) > 0) {
                            lonMin = new BigDecimal("-180");
                        } else {
                            // It is too far from the threshold and basically it is wrong bounding box from input coverage.
                            return;
                        }
                    }
                    if (latMin.compareTo(new BigDecimal("-90")) < 0) {
                        if (lonMin.compareTo(new BigDecimal("-90.5")) > 0) {
                            latMin = new BigDecimal("-90");
                        } else {
                            // It is too far from the threshold and basically it is wrong bounding box from input coverage.
                            return;
                        }                    
                    }                
                    if (lonMax.compareTo(new BigDecimal("180")) > 0) {
                        if (lonMax.compareTo(new BigDecimal("180.5")) < 0) {
                            lonMax = new BigDecimal("180");
                        } else {
                            // It is too far from the threshold and basically it is wrong bounding box from input coverage.
                            return;
                        }
                    }
                    if (latMax.compareTo(new BigDecimal("90")) > 0) {
                        if (latMax.compareTo(new BigDecimal("90.5")) < 0) {
                            latMax = new BigDecimal("90");
                        } else {
                            // It is too far from the threshold and basically it is wrong bounding box from input coverage.
                            return;
                        }
                    }
                    
                    boundingBox = new BoundingBox(lonMin, latMin, lonMax, latMax);
                } catch (PetascopeException ex) {
                    log.warn("Cannot create extent for coverage '" + coverageId + "', error from crs transform '" + ex.getExceptionText() + "'.");
                    this.problemCoveragesExtentsCache.add(coverageId);
                }
            }
            
            if (boundingBox != null) {
                // Transformed is done, put it to cache
                coveragesExtentsCacheMap.put(coverageId, boundingBox);
            }
        }
    }

    /**
     * Add a new coverage to database
     */
    public void add(Coverage coverage) throws PetascopeException {
        String coverageId = coverage.getCoverageId();
        if (isInLocalCache(coverageId)) {
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
        // NOTE: Don't save coverage with fixed CRS (e.g: http://localhost:8080/def/crs/epsg/0/4326)
        // it must use a string placeholder so when setting up Petascope with a different SECORE endpoint, it will replace the placeholder
        // with new SECORE endpoint or otherwise the persisted URL will throw exception as non-existing URL.
        CoverageRepositoryService.removeCrsPrefix(coverage);

        long start = System.currentTimeMillis();
        // then it can save (insert/update) the coverage to database
        this.coverageRepository.save(coverage);
        long end = System.currentTimeMillis();
        log.debug("Time to persist coverage '" + coverageId + "' is " + String.valueOf(end - start) + " ms.");

        entityManager.flush();
        entityManager.clear();
        
        CoverageRepositoryService.addCrsPrefix(coverage);
        
        long coverageSize = this.calculateCoverageSizeInBytes(coverage);
        coverage.setCoverageSizeInBytes(coverageSize);

        coveragesCacheMap.put(coverageId, new Pair(coverage, true));
        problemCoveragesExtentsCache.remove(coverageId);
        
        try {
            // Also insert/update the coverage's extent in cache as the bounding box of XY axes can be extended by WCST_Import.
            this.createCoverageExtent(coverageId);
        } catch (SecoreException ex) {
            log.warn("Cannot create coverage's extent for coverage '" + coverageId + "'. Reason: " + ex.getExceptionText());
        }

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
        coveragesCacheMap.remove(coverageId);
        // Remove the coverageExtent from cache
        coveragesExtentsCacheMap.remove(coverageId);

        entityManager.flush();
        entityManager.clear();
        
        problemCoveragesExtentsCache.remove(coverageId);

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
        GeneralGrid generalGrid = ((GeneralGridDomainSet) generalGridDomainSet).getGeneralGrid();
        generalGrid.setSrsName(CrsUtil.CrsUri.toDbRepresentation(generalGrid.getSrsName()));
        for (Axis geoAxis : generalGrid.getGeoAxes()) {
            strippedCRS = CrsUtil.CrsUri.toDbRepresentation(geoAxis.getSrsName());
            geoAxis.setSrsName(strippedCRS);
        }
    }

    /**
     * NOTE: As coverage is saved with a placeholder for SECORE prefix, so after
     * reading coverage from database, replace placeholder with SECORE
     * configuration endpoint from petascope.properties.
     *
     * @param coverage
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

        // + Then with geoAxes of GeneralGridDomainSet as only support GeneralGridCoverage now
        DomainSet generalGridDomainSet = ((GeneralGridCoverage) coverage).getDomainSet();
        GeneralGrid generalGrid = ((GeneralGridDomainSet) generalGridDomainSet).getGeneralGrid();
        for (Axis geoAxis : generalGrid.getGeoAxes()) {
            addedCRS = CrsUtil.CrsUri.fromDbRepresentation(geoAxis.getSrsName());
            geoAxis.setSrsName(addedCRS);
        }
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
