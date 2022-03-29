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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.datamigration;
import java.util.List;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;

/**
 * Class to handle data migration version number 6
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class DataMigration6Handler extends AbstractDataMigrationHandler {
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    
    public DataMigration6Handler() {
        // NOTE: update this by one for new handler class
        this.migrationVersion = 6;
        this.handlerId = "36e950ca-af77-11ec-8597-509a4cb4e064";
    }

    @Override
    public void migrate() throws PetascopeException, SecoreException {
        
        for (String coverageId : this.coverageRepositoryService.readAllLocalCoverageIds()) {
            Coverage baseCoverage = this.coverageRepositoryService.readCoverageFullMetadataByIdFromCache(coverageId);
            if (baseCoverage.getPyramid() != null && !baseCoverage.getPyramid().isEmpty()) {
                this.coverageRepositoryService.calculateCoverageSizeInBytesWithPyramid(baseCoverage);
            
                this.coverageRepositoryService.saveCoverageSizeInBytesWithPyramid(baseCoverage);
            }
        }
        
    }
    
}