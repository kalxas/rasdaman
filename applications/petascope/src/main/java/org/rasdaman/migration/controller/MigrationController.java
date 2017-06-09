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
package org.rasdaman.migration.controller;

import java.sql.SQLException;
import java.util.List;
import org.rasdaman.Application;
import static org.rasdaman.config.ConfigManager.MIGRATION;
import org.rasdaman.migration.legacy.readdatabase.ReadLegacyCoveragesService;
import org.rasdaman.migration.legacy.LegacyCoverageMetadata;
import org.rasdaman.migration.legacy.LegacyWMSLayer;
import org.rasdaman.migration.legacy.readdatabase.ReadLegacyWMSLayerService;
import org.rasdaman.migration.service.coverage.LegacyCoverageMainService;
import org.rasdaman.migration.service.owsmetadata.LegacyOwsServiceMetadataMainService;
import org.rasdaman.migration.service.wms.LegacyWMSLayerMainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * This class is called first time when migrating from old persisting coverage
 * in petascopedb to coverage in cis database
 *
 * @author Bang Pham Huu
 */
@RestController
public class MigrationController {

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

    private static final Logger log = LoggerFactory.getLogger(MigrationController.class);

    // + Most important: coverages metadata
    // Migrate legacy coverages to database
    @RequestMapping(MIGRATION + "/save_coverages")
    public String saveAllCoverages() throws Exception {
        // validate legacy database existed first
        checkLegacyCoverageExist();
        
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

        return "All coverages in legacy database were migrated to CIS database.";
    }   

    // + OWS Service metadata for WCS, WMS (service identification, service provider,...)
    // Migrate OWS Service metadata (ServiceIdentification, ServiceProvider of WCS GetCapabilities) to database
    @RequestMapping(MIGRATION + "/save_metadata")
    public String saveOwsServiceMetadata() throws Exception {
        // validate legacy database existed first
        checkLegacyCoverageExist();
        
        log.info("Migrating legacy OWS Service metadata to database.");
        legacyOwsServiceMetadataMainService.persist();

        return "Legacy OWS Service metadata is persisted.";
    }

    // + WMS 1.3 tables to WMS 1.3 data models
    @RequestMapping(MIGRATION + "/save_wms")
    public String saveWMSLayers() throws Exception {
        // validate legacy database existed first
        checkLegacyCoverageExist();
        
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

        return "All WMS 1.3 layers in legacy database were migrated to new database.";
    }
    
    
    // For testing only
    @RequestMapping(MIGRATION + "/save_coverage/{coverage_id}")
    public String saveOneCoverage(@PathVariable String coverage_id) throws Exception {
        // validate legacy database existed first
        checkLegacyCoverageExist();
        
        log.info("--------------------------------------------------------------------");
        log.info("Migrating legacy coverage Id: " + coverage_id);
        LegacyCoverageMetadata coverageMetadata = readLegacyCoveragesService.read(coverage_id);
        legacyCoverageMainService.persist(coverageMetadata);
        log.info("Legacy coverage Id: " + coverage_id + " is migrated. Done.");
        log.info("--------------------------------------------------------------------\n");

        return "Coverage Id: " + coverage_id + " in legacy database was migrated to new database.";
    }
    
    /**
     * If legacy coverage does not exist, then LegacyDbMetaDataSource is null and this controller should not do anything else
     */
    private void checkLegacyCoverageExist() throws ClassNotFoundException, SQLException, PetascopeException {
        if (!Application.checkLegacyDatabaseExist()) {
            throw new PetascopeException(ExceptionCode.NoApplicableCode, "Legacy database does not exist, nothing to migrate to new database.");
        }
    }
}

