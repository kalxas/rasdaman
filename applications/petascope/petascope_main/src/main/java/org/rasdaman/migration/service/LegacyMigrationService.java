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
  *  Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.migration.service;

import java.sql.SQLException;
import java.util.List;
import org.rasdaman.BeanApplicationConfiguration;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.migration.Migration;
import org.rasdaman.migration.domain.legacy.LegacyCoverageMetadata;
import org.rasdaman.migration.domain.legacy.LegacyDbMetadataSource;
import org.rasdaman.migration.domain.legacy.LegacyWMSLayer;
import org.rasdaman.migration.legacy.readdatabase.ReadLegacyCoveragesService;
import org.rasdaman.migration.legacy.readdatabase.ReadLegacyWMSLayerService;
import org.rasdaman.migration.service.coverage.LegacyCoverageMainService;
import org.rasdaman.migration.service.owsmetadata.LegacyOwsServiceMetadataMainService;
import org.rasdaman.migration.service.wms.LegacyWMSLayerMainService;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.MigrationRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsUtil;
import petascope.util.DatabaseUtil;

/**
 * A Service is called internally when new Petascope starts first time and
 * migrate database from legacy petascopedb to new petascopedb. NOTE: legacy
 * petascopedb is renamed to petascopedb_94_backup afterwards.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
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
    
    @Autowired
    private CoverageRepositoryService coverageRepostioryService;

    @Autowired
    // Use to close the connection to legacy petascopedb when migration is done
    private LegacyDbMetadataSource legacyDbMetadataSource;

    /**
     * When legacy petascopedb already exists, need to create a temp database in
     * Postgresql for Liquibase to create the schema first.
     */
    
    public static final String LEGACY_PETASCOPEDB_MIGRATION_TEMP_POSTFIX = "_migration_temp";
    public static final String LEGACY_PETASCOPEDB_MIGRATION_TEMP = ConfigManager.PETASCOPE_DB + LEGACY_PETASCOPEDB_MIGRATION_TEMP_POSTFIX;
    private static final Logger log = LoggerFactory.getLogger(LegacyMigrationService.class);

    public LegacyMigrationService() {

    }

    @Override
    public boolean canMigrate() throws Exception {
        // If old petascope's database is prior version 9.5
        if (DatabaseUtil.sourceLegacyPetascopeDatabaseExists()) {
            return true;
        }

        return false;
    }

    /**
     * This method is called after the bean for this service class is finished
     * (i.e: other autowired dependent services are not null). Then it will
     * check if it needs to migrate from legacy database to new database.
     *
     * @throws java.lang.Exception
     */
    @Override
    public void migrate() throws Exception {
        log.info("Migrating from petascopedb 9.4 or older to new version.");
        // Lock the new database when migrating so no request is handled
        Migration migration = new Migration();
        migration.setLock(true);
        // Insert the first entry to migration table
        migrationRepositoryService.save(migration);
        
        // NOTE: Check secore is running first, if it is not running, migration cannot be done
        try {
            // try to request to localhost:8080/def/crs/EPSG/0/4326
            CrsUtil.getCrsDefinition(CrsUtil.EPSG_ALL_CRS + "/4326");
        } catch (Exception ex) {            
            String errorMessage = "Test request '" + CrsUtil.EPSG_ALL_CRS + "/4326' failed; "
                                + "please make sure def.war (SECORE) is deployed in Tomcat and then retry the migration.";
            log.debug(errorMessage, ex);
            throw new PetascopeException(ExceptionCode.InternalComponentError, errorMessage);
        }

        // First, migrate the legacy coverage's metadata to new CIS data model
        this.saveAllCoverages();
        log.info("\n");

        // Second, migrate the OWS Service metadata (only one)
        this.saveOwsServiceMetadata();
        log.info("\n");

        // Last, migrate the WMS 1.3 layer's metadata to new database
        this.saveWMSLayers();
        log.info("\n");

        // Legacy migration is done, release the lock
        this.releaseLock();

        // No need to connect to legacy petascopedb anymore
        legacyDbMetadataSource.closeConnection();

        // rename the legacy database to DATABASE_NAME_94_backup (e.g: petascopedb_94_backup)
        String newOldPetascopeDBName = this.renameLegacyPetascopeDatabaseToBackUp();
        log.info("The original petascopedb has been backed up as database '" + newOldPetascopeDBName + "'.");

        // rename the temp database is used to migrate to official name
        this.renamePetascopeDatabaseMigrationTemp();
        log.info("Moving temporary migration database '" + LEGACY_PETASCOPEDB_MIGRATION_TEMP + "' to final database '" + ConfigManager.PETASCOPE_DB + "'.");

        log.info("petascopedb 9.4 or older has been migrated successfully.");
    }

    @Override
    protected void saveAllCoverages() throws Exception {
        List<String> legacyCoverageIds = readLegacyCoveragesService.readAllCoverageIds();
        log.info("Migrating coverages...");
        int totalCoverages = legacyCoverageIds.size();

        int i = 1;
        for (String legacyCoverageId : legacyCoverageIds) {            
            log.info("Migrating coverage '" + legacyCoverageId + "' (" + i + "/" + totalCoverages + ")");

            // Check if legacy coverage Id is already migrated
            if (legacyCoverageMainService.coverageIdExist(legacyCoverageId)) {
                log.info("... already migrated, skipping.");
            } else {
                try {
                    // Legacy coverage Id is not migrated yet, now read the whole legacy coverage content which is *slow*
                    LegacyCoverageMetadata legacyCoverageMetadata = readLegacyCoveragesService.read(legacyCoverageId);
                    // And persist this legacy coverage metadata by converting it to new CIS data model and saving to database
                    legacyCoverageMainService.persist(legacyCoverageMetadata);
                    log.info("... migrated successfully.");
                } catch (Exception ex) {
                    log.debug("Error when migrating coverage", ex);
                    log.info("... cannot migrate coverage with error '" + ex.getMessage() + "', skipping.");
                }
            }            
            i++;
        }

        log.info("All coverages migrated successfully.");
    }

    @Override
    protected void saveOwsServiceMetadata() throws Exception {
        log.info("Migrating OWS Service metadata...");
        try {
            legacyOwsServiceMetadataMainService.persist();
            log.info("... migrated successfully.");
        } catch (Exception ex) {
            log.debug("Error when migrating OWS Service metadata", ex);
            log.info("... cannot migrate OWS Service metadata with error '" + ex.getMessage() + "', skpiing.");
        }

    }

    @Override
    protected void saveWMSLayers() throws Exception {
        List<String> legacyWmsLayers = readLegacyWMSLayerService.readAllLayerNames();
        int totalLayers = legacyWmsLayers.size();

        int i = 1;
        for (String legacyWmsLayerName : legacyWmsLayers) {            
            log.info("Migrating WMS layer '" + legacyWmsLayerName + "' (" + i + "/" + totalLayers + ")");

            // Check if legacy WMS layer name is already migrated
            if (legacyWMSLayerMainService.layerNameExist(legacyWmsLayerName)) {
                log.info("... already migrated, skipping.");
            } else {
                // Legacy WMS layer is not migrated yet, now read the whole legacy WMS layer content which is *slow*
                LegacyWMSLayer legacyWMSLayer = readLegacyWMSLayerService.read(legacyWmsLayerName);                
                if (legacyWMSLayer == null) {
                    log.info("Associated coverage for this layer does not exist in database to migrate, skipping.");
                } else {
                    try {
                        // NOTE: WMS does not support 3D+ coverage, so don't migrate the legacy WMS layer which was imported before accidentally.                    
                        Coverage coverage = this.coverageRepostioryService.readCoverageFullMetadataByIdFromCache(legacyWmsLayerName);
                        List<GeoAxis> geoAxes = ((GeneralGridCoverage) coverage).getGeoAxes();
                        if (geoAxes.size() > 2) {            
                            log.info("WMS does not support 3D+ layer to migrate, skipping.");
                        } else {
                            // And persist this legacy WMS layer's metadata by converting it to new data model and saving to database
                            legacyWMSLayerMainService.persist(legacyWMSLayer);
                            log.info("... migrated successfully.");
                        }
                    } catch (Exception ex) {
                        log.debug("Error when migrating layer", ex);
                        log.info("... cannot migrate layer with error '" + ex.getMessage() + "', skipping.");
                    }
                }
            }            
            i++;
        }

        log.info("All WMS 1.3 layers migrated successfully.");
    }

    /**
     * Rename the petascopedb prior version 9.5 to petascope_94_backup and
     * return the new renamed name.
     * http://rasdaman.org/wiki/Petascope_9.5#Howtomigrate
     */
    private String renameLegacyPetascopeDatabaseToBackUp() throws SQLException, ClassNotFoundException, PetascopeException {
        // Normally it is petascopedb, but it could be changed before.
        String oldLegacyDatabaseName = ConfigManager.SOURCE_DATASOURCE_URL.substring(
                ConfigManager.SOURCE_DATASOURCE_URL.lastIndexOf("/") + 1, ConfigManager.SOURCE_DATASOURCE_URL.length());
        String newLegacyDatabaseName = oldLegacyDatabaseName + "_94_backup";

        // Rename legacy database to a backup database
        DatabaseUtil.renamePostgresqLegacyDatabase(oldLegacyDatabaseName, newLegacyDatabaseName);

        return newLegacyDatabaseName;
    }

    /**
     * After the migration is done, rename the temp migrated database to the
     * official name: petascopedb
     */
    private void renamePetascopeDatabaseMigrationTemp() throws PetascopeException, SQLException, ClassNotFoundException, InterruptedException {
        // rename it while closing all the connections to this database of Hibernate
        DatabaseUtil.renamePostgresqLegacyDatabase(LEGACY_PETASCOPEDB_MIGRATION_TEMP, ConfigManager.PETASCOPE_DB);
    }
}
