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
package org.rasdaman.migration.service.coverage;

import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.CoverageFunction;
import org.rasdaman.domain.cis.DomainSet;
import org.rasdaman.domain.cis.Envelope;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.RangeType;
import org.rasdaman.domain.cis.RasdamanRangeSet;
import org.rasdaman.migration.domain.legacy.LegacyCoverageMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.rasdaman.repository.service.CoverageRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.PetascopeException;

/**
 * Translation from old coverage model in petascopedb to new coverage model CIS
 * db
 *
 * @author Bang Pham huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class LegacyCoverageMainService {

    private static final Logger log = LoggerFactory.getLogger(LegacyCoverageMainService.class);

    // Translating services
    @Autowired
    private EnvelopeByAxisCreateTranslatingService envelopeByAxisTranslatingService;
    @Autowired
    private DomainSetCreateTranslatingService domainSetCreateTranslatingSerivce;
    @Autowired
    private RangeTypeCreateTranslatingService rangeTypeCreateTranslatingService;
    @Autowired
    private RasdamanRangeSetServiceCreateTranslatingService createRasdamanRangeSetService;

    // Repository services
    @Autowired
    private CoverageRepostioryService coverageRepostioryService;

    /**
     * Convert the old legacy coverage to new coverage if coverage id does not
     * exist in the database
     *
     * @param coverageMetadata
     * @return
     */
    public Coverage convertToInsert(LegacyCoverageMetadata coverageMetadata) throws Exception {
        // As old coverages's type is grid only
        Coverage coverage = new GeneralGridCoverage();

        // Build the component objects of coverage from legacy object        
        EnvelopeByAxis envelopeByAxis = envelopeByAxisTranslatingService.create(coverageMetadata);
        Envelope envelope = new Envelope(envelopeByAxis);
        DomainSet domainSet = domainSetCreateTranslatingSerivce.create(coverageMetadata);
        RangeType rangeType = rangeTypeCreateTranslatingService.create(coverageMetadata);
        RasdamanRangeSet rasdamanRangeSet = createRasdamanRangeSetService.create(coverageMetadata);

        // Return the translated new coverage object
        coverage.setCoverageId(coverageMetadata.getCoverageName());
        // To determine the coverage grid types
        coverage.setCoverageType(coverageMetadata.getCoverageType());
        // as all legacy coverages does not have coverageFunction as it is optional element, so set to the default rule (linear)
        coverage.setCoverageFunction(new CoverageFunction(CoverageFunction.DEFAULT_SEQUENCE_RULE));
        coverage.setMetadata(coverageMetadata.getExtraMetadataRepresentation());
        coverage.setEnvelope(envelope);
        coverage.setDomainSet(domainSet);
        coverage.setRangeType(rangeType);
        coverage.setRasdamanRangeSet(rasdamanRangeSet);

        // do some translation stuff here
        return coverage;
    }

    /**
     * Check if legacy coverage id is already migrated to new CIS coverage data
     * model.
     *
     * @param legacyCoverageId
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public boolean coverageIdExist(String legacyCoverageId) throws PetascopeException {
        return this.coverageRepostioryService.coverageIdExist(legacyCoverageId);
    }

    /**
     * After the translation from legacy coverage data to CIS data model, it
     * could insert new coverage to database
     *
     * @param coverageMetadata
     * @throws java.lang.Exception
     */
    public void persist(LegacyCoverageMetadata coverageMetadata) throws Exception {
        long start = System.currentTimeMillis();
        Coverage coverage = this.convertToInsert(coverageMetadata);
        long end = System.currentTimeMillis();
        log.debug("Time to convert legacy coverage metadata to new data model is: " + String.valueOf(end - start));
        
        this.coverageRepostioryService.save(coverage);        
        log.info("Coverage Id: " + coverage.getCoverageId() + " is persisted in database.");
    }
}
