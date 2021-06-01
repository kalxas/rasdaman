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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.repository.service;

import java.util.ArrayList;
import java.util.List;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.repository.interfaces.CoveragePyramidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;

/**
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class CoveragePyramidRepositoryService {
    
    @Autowired
    CoveragePyramidRepository coveragePyramidRepository;
    
    @Autowired
    CoverageRepositoryService coverageRepositoryService;
    
    /**
     * Given a pyramid member coverage id, return the list of local coverage objects (persisted in database)
     * from the table coverage_pyramid which contains input pyramidMemberCoverageId in its pyramid 
     */
    public List<GeneralGridCoverage> getCoveragesContainingPyramidMemberCoverageId(String pyramidMeberCoverageId) throws PetascopeException {
        List<String> localCoverageIds = this.coveragePyramidRepository.readCoverageIdsContainingPyramidMemberCoverageId(pyramidMeberCoverageId);
        
        List<GeneralGridCoverage> results = new ArrayList<>();
        
        for (String localCoverageId : localCoverageIds) {
            GeneralGridCoverage generalGridCoverage = (GeneralGridCoverage)this.coverageRepositoryService.readCoverageByIdFromDatabase(localCoverageId);
            results.add(generalGridCoverage);
        }
        
        return results;
    }
}
