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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.rasdaman.domain.cis.Axis;
import org.rasdaman.domain.cis.AxisExtent;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.DomainSet;
import org.rasdaman.domain.cis.Envelope;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.cis.GeneralGrid;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeneralGridDomainSet;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsUtil;
import org.rasdaman.repository.interfaces.CoverageRepository;
import petascope.core.AxisTypes;
import petascope.core.BoundingBox;
import petascope.core.Pair;
import petascope.util.CrsProjectionUtil;

/**
 *
 * This class is the business layer to fetch persisted coverage from database by
 * Repository layer
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
@Transactional
public class CoverageRepostioryService {

    @Autowired
    private CoverageRepository coverageRepository;

    // NOTE: for migration, Hibernate caches the object in first-level cache internally
    // and recheck everytime a new entity is saved, then with thousands of cached objects for nothing
    // it will slow significantly the speed of next saving coverage, then it must be clear this cache.   
    // As targetEntityManagerFactory is set with Primary in bean application of migration application, no need to specify the unitName=target for this PersistenceContext
    @PersistenceContext
    EntityManager entityManager;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CoverageRepostioryService.class);

    // add coverage to cache (if insert, update, delete coverage) then update cache also
    // NOTE: as Spring Cache with annotation @Cacheable in findAllCoverages will put only 1 key -> list of coverages so
    // it is not good as there is no chance to update or delete one of the cached coverage from this cache, then has to
    // define this map manually.
    public static final Map<String, Pair<Coverage, Boolean>> coveragesCacheMap = new ConcurrentHashMap<>();

    /**
     * Store the coverages's extents (only geo-referenced XY axes). First String
     * is minLong, minLat, second String is maxLong, maxLat.
     */
    public static final Map<String, BoundingBox> coveragesExtentsCacheMap = new ConcurrentHashMap<>();
    public static final String COVERAGES_EXTENT_TARGET_CRS_DEFAULT = "EPSG:4326";

    public CoverageRepostioryService() {

    }

    /**
     * This method is used *only* when read coverage metadata to get another
     * data not from database. If using it to update an input coverage metadata,
     * it will have problem with Hibernate Cascade all.
     *
     * @param coverageId
     * @return
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.SecoreException
     */
    public Coverage readCoverageFullMetadataByIdFromCache(String coverageId) throws PetascopeException {
        Pair<Coverage, Boolean> coveragePair = coveragesCacheMap.get(coverageId);
        Coverage coverage = null;
        // Check if coverage is already cached
        if (coveragePair == null) {
            // If coverage is not cached then read it from database
            coverage = this.readCoverageByIdFromDatabase(coverageId);
        } else if (coveragePair.snd == false) {
            // If coverage just contains basic metadata, then read it from database
            coverage = this.readCoverageByIdFromDatabase(coverageId);
        } else {
            // Coverage already exists in the cache.
            coverage = coveragePair.fst;
        }
        
        log.debug("Coverage id '" + coverageId + "' is read from cache.");

        return coverage;
    }

    /**
     * Only read basic coverages's metadata from cache. Used when starting web
     * application as it should not query the whole coverages's metadata.
     *
     * @param coverageId
     * @return
     */
    public Coverage readCoverageBasicMetadataByIdFromCache(String coverageId) {
        Coverage coverage = coveragesCacheMap.get(coverageId).fst;

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
     * @throws petascope.exceptions.SecoreException
     */
    public Coverage readCoverageByIdFromDatabase(String coverageId) throws PetascopeException {
        
        // This happens when Petascope starts and user sends a WCPS query to a coverage instead of WCS GetCapabilities
        if (coveragesCacheMap.isEmpty()) {
            this.readAllCoveragesBasicMetatata();
        }
        
        long start = System.currentTimeMillis();

        Coverage coverage = this.coverageRepository.findOneByCoverageId(coverageId);
        long end = System.currentTimeMillis();
        log.debug("Time to read a coverage from database is: " + String.valueOf(end - start));

        if (coverage == null) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "Coverage: " + coverageId + " does not exist in persistent database.");
        }

        log.debug("Coverage: " + coverageId + " is read from database.");
        
        // NOTE: without it, after coverage's crs is replaced from $SECORE_URL$ to localhost:8080 (from petascope.properties)
        // with a DescribeCoverage request, after the replacement, 
        // it will save coverage's crs with localhost:8080 instead of the placeholder $SCORE_URL$ in database.
        entityManager.clear();

        // NOTE: As coverage is saved with a placeholder for SECORE prefix, so after reading coverage from database, 
        // replace placeholder with SECORE configuration endpoint from petascope.properties.
        CoverageRepostioryService.addCrsPrefix(coverage);

        // put to cache        
        coveragesCacheMap.put(coverageId, new Pair<>(coverage, true));

        return coverage;
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
    public List<Pair<Coverage, Boolean>> readAllCoveragesBasicMetatata() throws PetascopeException {        
        long start = System.currentTimeMillis();

        List<Pair<Coverage, Boolean>> coverages = new ArrayList<>();
        // Only read from database when starting petascope and cache all the coverages
        if (coveragesCacheMap.isEmpty()) {
            List<Object[]> coverageIdsAndTypes = coverageRepository.readAllCoverageIdsAndTypes();
            for (Object[] coverageIdsAndType : coverageIdsAndTypes) {
                String coverageId = coverageIdsAndType[0].toString();
                String coverageType = coverageIdsAndType[1].toString();

                // Each coverage has only 1 envelope and each envelope has only 1 envelopeByAxis
                EnvelopeByAxis envelopeByAxis = coverageRepository.readEnvelopeByAxisByCoverageId(coverageId);
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

                // Then cache the read coverage's basic metadata
                coveragesCacheMap.put(coverage.getCoverageId(), new Pair<>(coverage, false));
            }

            // Read all coverage from persistent database
            log.debug("Read all persistent coverages from database.");
            coverages = new ArrayList<>(coveragesCacheMap.values());
        } else {
            coverages = new ArrayList<>(coveragesCacheMap.values());
            log.debug("Read all persistent coverages from cache.");
        }

        long end = System.currentTimeMillis();
        log.debug("Time to read all coverages is: " + String.valueOf(end - start));

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
            this.createCoverageExtent(coverageId);
        }

        long end = System.currentTimeMillis();
        log.debug("Time to compute all Coverages's Extends is: " + String.valueOf(end - start));
    }

    /**
     * Create a coverage's extent by cached coverage's metadata
     * (EnvelopeByAxis). The XY axes' BoundingBox is reprojected to EPSG:4326.
     */
    private void createCoverageExtent(String coverageId) throws PetascopeException, SecoreException {
        // Tranformed XY extents to EPSG:4326 (Long, Lat for OpenLayers)
        BoundingBox boundingBox = new BoundingBox();

        // Only need a coverage's basic metadata, it is slow to query the whole coverage's metadata
        Coverage coverage = this.readCoverageBasicMetadataByIdFromCache(coverageId);
        List<AxisExtent> axisExtents = ((GeneralGridCoverage) coverage).getEnvelope().getEnvelopeByAxis().getAxisExtents();
        boolean foundX = false, foundY = false;
        String xMin = null, yMin = null, xMax = null, yMax = null;
        String xyAxesCRS = null;
        for (AxisExtent axisExtent : axisExtents) {
            String axisExtentCrs = axisExtent.getSrsName();
            // NOTE: the basic coverage metadata can have the abstract SECORE URL, so must replace it first
            axisExtentCrs = CrsUtil.CrsUri.fromDbRepresentation(axisExtentCrs);
            // x, y, t,...
            String axisType = CrsUtil.getAxisType(axisExtentCrs, axisExtent.getAxisLabel());
            if (axisType.equals(AxisTypes.X_AXIS)) {
                foundX = true;
                xMin = axisExtent.getLowerBound();
                xMax = axisExtent.getUpperBound();
                xyAxesCRS = axisExtentCrs;
            } else if (axisType.equals(AxisTypes.Y_AXIS)) {
                foundY = true;
                yMin = axisExtent.getLowerBound();
                yMax = axisExtent.getUpperBound();
            }
            if (foundX && foundY) {
                break;
            }
        }

        log.debug("Coverage Id '" + coverageId + "' to transform extents.");

        // Don't transform the XY extents to EPSG:4326 if it is not EPSG code or it is not at least 2D
        if (foundX && foundY && CrsUtil.isValidTransform(xyAxesCRS)) {
            double[] xyMinArray = {Double.valueOf(xMin), Double.valueOf(yMin)};
            double[] xyMaxArray = {Double.valueOf(xMax), Double.valueOf(yMax)};
            List<BigDecimal> minLongLatList = null, maxLongLatList = null;

            try {
                // NOTE: cannot reproject every kind of EPSG:codes to EPSG:4326, but should not stop application for this.
                minLongLatList = CrsProjectionUtil.transform(xyAxesCRS, COVERAGES_EXTENT_TARGET_CRS_DEFAULT, xyMinArray);
                maxLongLatList = CrsProjectionUtil.transform(xyAxesCRS, COVERAGES_EXTENT_TARGET_CRS_DEFAULT, xyMaxArray);
                
                // NOTE: EPSG:4326 cannot display outside of coordinates for lat(-90, 90), lon(-180, 180) and it should only contain 5 precisions.                
                BigDecimal minLon = minLongLatList.get(0).setScale(5, BigDecimal.ROUND_HALF_UP);
                BigDecimal minLat = minLongLatList.get(1).setScale(5, BigDecimal.ROUND_HALF_UP);
                BigDecimal maxLon = maxLongLatList.get(0).setScale(5, BigDecimal.ROUND_HALF_UP);
                BigDecimal maxLat = maxLongLatList.get(1).setScale(5, BigDecimal.ROUND_HALF_UP);
                
                if (minLon.compareTo(new BigDecimal("-180")) < 0) {
                    if (minLon.compareTo(new BigDecimal("-180.5")) > 0) {
                        minLon = new BigDecimal("-180");
                    } else {
                        // It is too far from the threshold and basically it is wrong bounding box from input coverage.
                        return;
                    }
                }
                if (minLat.compareTo(new BigDecimal("-90")) < 0) {
                    if (minLon.compareTo(new BigDecimal("-90.5")) > 0) {
                        minLat = new BigDecimal("-90");
                    } else {
                        // It is too far from the threshold and basically it is wrong bounding box from input coverage.
                        return;
                    }                    
                }                
                if (maxLon.compareTo(new BigDecimal("180")) > 0) {
                    if (maxLon.compareTo(new BigDecimal("180.5")) < 0) {
                        maxLon = new BigDecimal("180");
                    } else {
                        // It is too far from the threshold and basically it is wrong bounding box from input coverage.
                        return;
                    }
                }
                if (maxLat.compareTo(new BigDecimal("90")) > 0) {
                    if (maxLat.compareTo(new BigDecimal("90.5")) < 0) {
                        maxLat = new BigDecimal("90");
                    } else {
                        // It is too far from the threshold and basically it is wrong bounding box from input coverage.
                        return;
                    }
                }                
                
                boundingBox.setXmin(minLon);
                boundingBox.setYmin(minLat);
                boundingBox.setXmax(maxLon);
                boundingBox.setYmax(maxLat);
                
                // Transformed is done, put it to cache
                coveragesExtentsCacheMap.put(coverageId, boundingBox);
            } catch (PetascopeException ex) {
                log.warn("Cannot create extent for coverage '" + coverageId + "', error from GDAL crs transform '" + ex.getExceptionText() + "'.");
            }
        }
    }

    /**
     * Update or Insert a new Coverage to database
     *
     * @param coverage
     * @return
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.SecoreException
     */
    public Coverage save(Coverage coverage) throws PetascopeException, SecoreException {
        String coverageId = coverage.getCoverageId();
        // NOTE: Don't save coverage with fixed CRS (e.g: http://localhost:8080/def/crs/epsg/0/4326)
        // it must use a string placeholder so when setting up Petascope with a different SECORE endpoint, it will replace the placeholder
        // with new SECORE endpoint or otherwise the persisted URL will throw exception as non-existing URL.
        CoverageRepostioryService.removeCrsPrefix(coverage);

        long start = System.currentTimeMillis();
        // then it can save (insert/update) the coverage to database
        this.coverageRepository.save(coverage);
        long end = System.currentTimeMillis();
        log.debug("Time to persist a coverage " + coverageId + " is: " + String.valueOf(end - start));

        entityManager.flush();
        entityManager.clear();

        // Then add the coverage with abstractCRS SECORE to cache
        // Don't addCrsPrefix now as Hibernate not yet persists the coverage's metadata, so, it will only save the fixed CRSs instead of the abstract CRSs to database.
        // When reading coverage from cache method, it will add back the CRS.
        coveragesCacheMap.put(coverageId, new Pair(coverage, false));
        // Also insert/update the coverage's extent in cache as the bounding box of XY axes can be extended by WCST_Import.
        this.createCoverageExtent(coverageId);

        log.debug("Coverage '" + coverageId + "' is persisted in database.");

        return coverage;
    }

    /**
     * Delete a persisted coverage by id
     *
     * @param coverage
     */
    public void delete(Coverage coverage) {
        String coverageId = coverage.getCoverageId();
        this.coverageRepository.delete(coverage);

        // Remove the cached coverage from cache
        coveragesCacheMap.remove(coverageId);
        // Remove the coverageExtent from cache
        coveragesExtentsCacheMap.remove(coverageId);

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
     * Just fetch all the coverage Ids in a list.
     *
     * @return
     */
    public List<String> readAllCoverageIds() {
        List<String> coverageIds = this.coverageRepository.readAllCoverageIds();

        return coverageIds;
    }
}
