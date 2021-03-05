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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.rasdaman.domain.migration.DataMigration;
import org.rasdaman.repository.service.DataMigrationRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class to handle data migration version
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class DataMigrationService {

    private static final Logger log = LoggerFactory.getLogger(DataMigrationService.class);

    @Autowired
    DataMigrationRepositoryService dataMigrationRepositoryService;
    
    @Resource
    // Spring finds all the subclass of AbstractHandler and injects to the list
    List<AbstractDataMigrationHandler> handlers;
    
    // e.g: version 1 -> uuid1
    //      version 2 -> uuid2
    private static final Map<Integer, AbstractDataMigrationHandler> versionHandlersMap = new TreeMap<>();

    public DataMigrationService() {
    }
    
    @PostConstruct
    // Invoked after object is initialized
    private void initMap() {
        for (AbstractDataMigrationHandler handler : this.handlers) {
            // e.g: version 1 -> uuid1
            this.versionHandlersMap.put(handler.getMigrationVersion(), handler);
        }
    }

    /**
     * petascopdb has outdated version number (e.g: version 2), while petascope
     * has version number 6, then migration version handlers 3, 4, 5, 6 will be
     * invoked
     */
    public void runMigration() {
        for (Entry<Integer, AbstractDataMigrationHandler> entry : this.versionHandlersMap.entrySet()) {
            AbstractDataMigrationHandler handler = entry.getValue();
            String handlerId = handler.getHandlerId();
            
            if (!this.dataMigrationRepositoryService.isApplied(handlerId)) {
                handler.migrate();
                this.dataMigrationRepositoryService.save(new DataMigration(handlerId));
            }
        }
    }
}
