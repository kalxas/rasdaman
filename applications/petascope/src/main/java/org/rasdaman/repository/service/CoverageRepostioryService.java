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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import org.rasdaman.domain.cis.Axis;
import org.rasdaman.domain.cis.AxisExtent;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.DomainSet;
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
    private CoverageRepository abstractCoverageRepository;
    
    // NOTE: for migration, Hibernate caches the object in first-level cache internally
    // and recheck everytime a new entity is saved, then with thousands of cached objects for nothing
    // it will slow significantly the speed of next saving coverage, then it must be clear this cache.    
    @Autowired
    EntityManager entityManager;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CoverageRepostioryService.class);

    // add coverage to cache (if insert, update, delete coverage) then update cache also
    // NOTE: as Spring Cache with annotation @Cacheable in findAllCoverages will put only 1 key -> list of coverages so
    // it is not good as there is no chance to update or delete one of the cached coverage from this cache, then has to
    // define this map manually.
    public static final Map<String, Coverage> coveragesCacheMap = new ConcurrentHashMap<>();

    public CoverageRepostioryService() {

    }

    @PostConstruct
    /**
     * This method is called after the bean for this service class is finished
     * (i.e: other autowired dependent services are not null). Then it can load
     * all coverages to cache.
     */
    private void initCoveragesCache() {
        this.readAllCoverages();
        log.debug("Initialized all the coverages's metadata to cache.");
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
    public Coverage readCoverageByIdFromCache(String coverageId) throws PetascopeException {
        Coverage coverage = coveragesCacheMap.get(coverageId);
        // Check if coverage is already cached
        if (coverage == null) {
            // If coverage is not cached, then read it from database
            coverage = this.readCoverageByIdFromDatabase(coverageId);
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
        long start = System.currentTimeMillis();

        Coverage coverage = this.abstractCoverageRepository.findOneByCoverageId(coverageId);
        long end = System.currentTimeMillis();
        log.debug("Time to read a coverage from database is: " + String.valueOf(end - start));

        if (coverage == null) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "Coverage: " + coverageId + " does not exist in persistent database.");
        }

        // NOTE: As coverage is saved with a placeholder for SECORE prefix, so after reading coverage from database, 
        // replace placeholder with SECORE configuration endpoint from petascope.properties.
        this.addCrsPrefix(coverage);
        log.debug("Coverage: " + coverageId + " is read from database.");

        // put to cache        
        coveragesCacheMap.put(coverageId, coverage);

        return coverage;
    }

    /**
     *
     * Read all persistent coverages which were translated from legacy coverage
     * or imported as new objects. Used when starting Petascope to load
     * coverage's metadata to cache and WCS GetCapabilities will fetch from
     * cache.
     *
     * @return List<Coverage>
     */
    public List<Coverage> readAllCoverages() {
        long start = System.currentTimeMillis();

        List<Coverage> coverages = new ArrayList<>();
        // Only read from database when starting petascope and cache all the coverages
        if (coveragesCacheMap.isEmpty()) {
            for (Coverage coverage : abstractCoverageRepository.findAll()) {
                // NOTE: As coverage is saved with a placeholder for SECORE prefix, so after reading coverage from database, 
                // replace placeholder with SECORE configuration endpoint from petascope.properties.
                CoverageRepostioryService.addCrsPrefix(coverage);
                coverages.add(coverage);

                // Then cache read coverage
                coveragesCacheMap.put(coverage.getCoverageId(), coverage);
            }
            // Read all coverage from persistent database
            log.debug("Read all persistent coverages from database.");
        } else {
            coverages = new ArrayList<>(coveragesCacheMap.values());
            log.debug("Read all persistent coverages from cache.");
        }

        long end = System.currentTimeMillis();
        log.debug("Time to read all coverages is: " + String.valueOf(end - start));

        return coverages;
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
        this.abstractCoverageRepository.save(coverage);
        long end = System.currentTimeMillis();
        log.debug("Time to persist a coverage is: " + String.valueOf(end - start));

        // NOTE: add to cache the coverage with a specific SECORE prefix not with string placeholder
        CoverageRepostioryService.addCrsPrefix(coverage);

        // add to coverage cache if it does not exist or update the existing one
        coveragesCacheMap.put(coverageId, coverage);
        
        entityManager.flush();
        entityManager.clear();

        log.info("Coverage: " + coverage.getCoverageId() + " is persisted in database.");
        
        return coverage;
    }

    /**
     * Delete a persisted coverage by id
     *
     * @param coverage
     */
    public void delete(Coverage coverage) {
        String coverageId = coverage.getCoverageId();
        this.abstractCoverageRepository.delete(coverage);

        // Remove the cached coverage from cache
        coveragesCacheMap.remove(coverageId);
        
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
        Coverage coverage = this.abstractCoverageRepository.findOneByCoverageId(coverageId);
        long end = System.currentTimeMillis();

        log.debug("Time to find existing coverage: " + String.valueOf(end - start));

        return coverage != null;
    }

}
