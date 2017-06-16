/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.migration.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.annotation.PostConstruct;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.config.ConfigManager.LEGACY_DATASOURCE_DRIVER;
import org.rasdaman.domain.migration.Migration;
import org.rasdaman.migration.domain.legacy.LegacyCoverageMetadata;
import org.rasdaman.migration.domain.legacy.LegacyWMSLayer;
import org.rasdaman.migration.legacy.readdatabase.ReadLegacyCoveragesService;
import org.rasdaman.migration.legacy.readdatabase.ReadLegacyWMSLayerService;
import org.rasdaman.migration.service.coverage.LegacyCoverageMainService;
import org.rasdaman.migration.service.owsmetadata.LegacyOwsServiceMetadataMainService;
import org.rasdaman.migration.service.wms.LegacyWMSLayerMainService;
import org.rasdaman.repository.service.MigrationRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A Service is called internally when new Petascope starts first time and
 * migrate database from legacy petascopedb to new cisdb.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class LegacyMigrationService extends AbstractMigrationService {

    @Autowired
    private LegacyCoverageMainService legacyCoverageMainService;
    @Autowired
    private LegacyOwsServiceMetadataMainService legacyOwsServiceMetadataMainService;
    @Autowired
    private LegacyWMSLayerMainService legacyWMSLayerMainService;

    @Autowired
    private ReadLegacyCoveragesService readLegacyCoveragesService;
    @Autowired
    private ReadLegacyWMSLayerService readLegacyWMSLayerService;
    @Autowired
    private MigrationRepositoryService migrationRepositoryService;

    private static final Logger log = LoggerFactory.getLogger(LegacyMigrationService.class);

    @Override
    public boolean canMigrate() throws Exception {
        // validate legacy database existed first
        boolean a = checkLegacyDatabaseExist();
        // migration table is empty and legacy migration is the first entry
        boolean b = this.getMigrations().isEmpty();

        return a && b;
    }

    /**
     * This method is called after the bean for this service class is finished
     * (i.e: other autowired dependent services are not null). Then it will
     * check if it needs to migrate from legacy database to new database.
     *
     * @throws java.lang.Exception
     */
    @PostConstruct
    @Override
    protected void migrate() throws Exception {
        if (canMigrate()) {
            log.info("Legacy database: petascopedb existed, need to migrate to new cisdb database.");
            // Lock the new database when migrating so no request is handled
            Migration migration = new Migration();
            migration.setLock(true);
            // Insert the first entry to migration table
            migrationRepositoryService.save(migration);

            // First, migrate the legacy coverage's metadata to new CIS data model
            this.saveAllCoverages();
            log.info("\n");
            
            // Second, migrate the OWS Service metadata (only one)
            this.saveOwsServiceMetadata();
            log.info("\n");
            
            // Last, migrate the WMS 1.3 layer's metadata to new database
            this.saveWMSLayers();
            log.info("\n");

            // Legacy migration is done, release the lock then all requests can be handled
            migration.setLock(false);
            migrationRepositoryService.save(migration);

        }
    }

    /**
     * In version 9.5, petascopedb will not exist as update_petascopedb.sh
     * script is removed. So don't create the bean for reading legacy database
     * source as it will throw exception when initializing server.
     *
     * @return
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public static boolean checkLegacyDatabaseExist() throws ClassNotFoundException, SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName(LEGACY_DATASOURCE_DRIVER);
            connection = DriverManager.getConnection(ConfigManager.LEGACY_DATASOURCE_URL, ConfigManager.LEGACY_DATASOURCE_USERNAME, ConfigManager.LEGACY_DATASOURCE_PASSWORD);
        } catch (SQLException ex) {
            log.info("Legacy petascopedb does not exist, no need to migrate data to new database.");
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                throw ex;
            }
        }

        return true;
    }

    /**
     * Migrate legacy coverages from petascopedb to new database cisdb
     *
     * @throws Exception
     */
    private void saveAllCoverages() throws Exception {

        List<String> legacyCoverageIds = readLegacyCoveragesService.readAllCoverageIds();
        int totalCoverages = legacyCoverageIds.size();

        int i = 1;
        for (String legacyCoverageId : legacyCoverageIds) {
            log.info("--------------------------------------------------------------------");
            log.info("Migrating legacy coverage: " + i + "/" + totalCoverages + " with Id: " + legacyCoverageId);

            // Check if legacy coverage Id is already migrated
            if (legacyCoverageMainService.coverageIdExist(legacyCoverageId)) {
                log.info("Legacy coverage Id: " + legacyCoverageId + " already migrated. Done.");
            } else {
                // Legacy coverage Id is not migrated yet, now read the whole legacy coverage content which is *slow*
                LegacyCoverageMetadata legacyCoverageMetadata = readLegacyCoveragesService.read(legacyCoverageId);
                // And persist this legacy coverage metadata by converting it to new CIS data model and saving to database
                legacyCoverageMainService.persist(legacyCoverageMetadata);
                log.info("Legacy coverage Id: " + legacyCoverageId + " is migrated. Done.");
            }
            log.info("--------------------------------------------------------------------\n");
            i++;
        }

        log.info("All coverages in legacy database were migrated to CIS database.");
    }

    /**
     * OWS Service metadata for WCS, WMS (service identification, service
     * provider,...) Migrate OWS Service metadata (ServiceIdentification,
     * ServiceProvider of WCS GetCapabilities) to database
     *
     * @throws Exception
     */
    private void saveOwsServiceMetadata() throws Exception {

        log.info("Migrating legacy OWS Service metadata to database.");
        legacyOwsServiceMetadataMainService.persist();

        log.info("Legacy OWS Service metadata is persisted.");
    }

    /**
     * WMS 1.3 tables to WMS 1.3 data models
     *
     * @throws Exception
     */
    private void saveWMSLayers() throws Exception {

        List<String> legacyWmsLayers = readLegacyWMSLayerService.readlAllLayerNames();
        int totalLayers = legacyWmsLayers.size();

        int i = 1;
        for (String legacyWmsLayerName : legacyWmsLayers) {
            log.info("--------------------------------------------------------------------");
            log.info("Migrating legacy WMS layer: " + i + "/" + totalLayers + " with name: " + legacyWmsLayerName);

            // Check if legacy WMS layer name is already migrated
            if (legacyWMSLayerMainService.layerNameExist(legacyWmsLayerName)) {
                log.info("Legacy WMS layer name: " + legacyWmsLayerName + " already migrated. Done.");
            } else {
                // Legacy WMS layer is not migrated yet, now read the whole legacy WMS layer content which is *slow*
                LegacyWMSLayer legacyWMSLayer = readLegacyWMSLayerService.read(legacyWmsLayerName);
                // And persist this legacy WMS layer's metadata by converting it to new data model and saving to database
                legacyWMSLayerMainService.persist(legacyWMSLayer);
                log.info("Legacy WMS layer name: " + legacyWmsLayerName + " is migrated. Done.");
            }
            log.info("--------------------------------------------------------------------\n");
            i++;
        }

        log.info("All WMS 1.3 layers in legacy database were migrated to new database.");
    }

}
