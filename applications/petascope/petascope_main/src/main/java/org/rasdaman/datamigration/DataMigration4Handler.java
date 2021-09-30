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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.cis.RasdamanRangeSet;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.ThreadUtil;

/**
 * Class to handle data migration version number 4
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class DataMigration4Handler extends AbstractDataMigrationHandler {
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    
    public DataMigration4Handler() {
        // NOTE: update this by one for new handler class
        this.migrationVersion = 4;
        this.handlerId = "6472e830-1d05-11ec-be02-509a4cb4e064";
    }

    @Override
    public void migrate() throws PetascopeException, SecoreException {
        
        List<Pair<Coverage, Boolean>> pairsList = this.coverageRepositoryService.readAllLocalCoveragesBasicMetatata();
         // local coverageId -> rasdaman rangeset
        Map<String, RasdamanRangeSet> localRangeSetMap = this.coverageRepositoryService.getAllLocalRasdamanRangeSetsMap();
        
        final CoverageRepositoryService coverageRepositoryService = this.coverageRepositoryService;
        
        List<Callable<Object>> todoList = new ArrayList<>();
        
        for (Pair<Coverage, Boolean> pair : pairsList) {
            final Coverage localCoverage = pair.fst;
            final String coverageId = localCoverage.getCoverageId();
            
            EnvelopeByAxis envelopeByAxis = localCoverage.getEnvelope().getEnvelopeByAxis();
            if (envelopeByAxis.getWgs84BBox() == null) {
                // If coverage doesn't have EPSG:4326 bbox, then it tries to create
                this.coverageRepositoryService.createCoverageExtent(localCoverage);

                // Then persist the coverage extent to database
                this.coverageRepositoryService.saveWgs84BBox(envelopeByAxis);
            }
            
            RasdamanRangeSet rasdamanRangeSet = localRangeSetMap.get(coverageId);
            localCoverage.setRasdamanRangeSet(rasdamanRangeSet);
            
            long coverageSizeInBytes = this.coverageRepositoryService.calculateCoverageSizeInBytes(localCoverage);
            localCoverage.setCoverageSizeInBytes(coverageSizeInBytes);
            
             Runnable task = new Runnable() {
                @Override
                public void run() {
                    coverageRepositoryService.saveCoverageSizeInBytes(localCoverage);
                }
            };
            
            todoList.add(Executors.callable(task));
        }
        
        // Run multiple threads to save separated coverages' sizeInBytes to database
        ThreadUtil.executeMultipleTasksInParallel(todoList);
    }
    
}