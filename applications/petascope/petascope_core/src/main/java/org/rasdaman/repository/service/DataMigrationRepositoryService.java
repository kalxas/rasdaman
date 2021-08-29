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
import java.util.Iterator;
import java.util.List;
import org.rasdaman.domain.migration.DataMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.rasdaman.repository.interfaces.DataMigrationRepository;

/**
 * Repository service to insert new version to data migration version table
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class DataMigrationRepositoryService {
    
    private static final Logger log = LoggerFactory.getLogger(DataMigrationRepositoryService.class);
    
    @Autowired
    DataMigrationRepository dataMigrationRepository;
    
    /**
     * Persist the data migration version number object to database
     */
    public void save(DataMigration dataMigration) {
        dataMigrationRepository.save(dataMigration);
        
        // uuid of a handler
        String handlerId = dataMigration.getAppliedMigration();
        log.info("Applied data migration internally to version '" +  handlerId + "'.");
    }
    
    /**
     * Check if a data migration version (uuid) was applied
     */
    public boolean isApplied(String handlerId) {
        return this.dataMigrationRepository.existsByAppliedMigration(handlerId);
    }
    
    /**
     * Return the data migration version object in petascopedb
     */
    public DataMigration getDataMigration() {
        Iterator<DataMigration> iterator = dataMigrationRepository.findAll().iterator();
        // default data migration version is 0
        DataMigration result = new DataMigration();
        
        while (iterator.hasNext()) {
            // if the table has data, then get the one from the table
            result = iterator.next();
        }
        
        return result;
    }
    
    public List<DataMigration> getDataMigrations() {
        Iterator<DataMigration> iterator = dataMigrationRepository.findAll().iterator();
        List<DataMigration> results = new ArrayList<>();
        
        while (iterator.hasNext()) {
            // if the table has data, then get the one from the table
            DataMigration tmp = iterator.next();
            results.add(tmp);
        }
        
        return results;        
    }
    
}
