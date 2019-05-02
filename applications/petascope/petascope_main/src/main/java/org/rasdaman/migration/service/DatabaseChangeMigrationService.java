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

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.rasdaman.MigrationBeanApplicationConfiguration;
import static org.rasdaman.MigrationBeanApplicationConfiguration.SOURCE;
import static org.rasdaman.MigrationBeanApplicationConfiguration.SOURCE_TRANSACTION_MANAGER;
import org.rasdaman.domain.cis.Coverage;
import static org.rasdaman.domain.cis.Coverage.COVERAGE_ID_PROPERTY;
import org.rasdaman.domain.migration.Migration;
import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.rasdaman.domain.wms.Layer;
import static org.rasdaman.domain.wms.Layer.LAYER_NAME_PROPERTY;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.MigrationRepositoryService;
import org.rasdaman.repository.service.OWSMetadataRepostioryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import petascope.util.DatabaseUtil;

/**
 * Class to migrate petascopedb after version 9.5 to different database
 * (same/different DMBS). NOTE: only supports DMBS with SEQUENCE.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Transactional(transactionManager = SOURCE_TRANSACTION_MANAGER)
@Conditional(MigrationBeanApplicationConfiguration.class)
public class DatabaseChangeMigrationService extends AbstractMigrationService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseChangeMigrationService.class);

    @Autowired
    private MigrationRepositoryService migrationRepositoryService;

    @Autowired
    private CoverageRepositoryService coverageRepostioryService;
    @Autowired
    private OWSMetadataRepostioryService owsMetadataRepostioryService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;

    @PersistenceContext(unitName = SOURCE)
    // Read to source database, use source transaction manager
    private EntityManager sourceEntityManager;

    private Session sourceHibernateSession;

    private Session getSourceHibernateSession() {
        if (sourceHibernateSession == null) {
            sourceHibernateSession = sourceEntityManager.unwrap(Session.class);
        }
        return sourceHibernateSession;
    }

    @Override
    public boolean canMigrate() throws Exception {
        // NOTE: Spring already checked the JDBC connections in BeanApplicationConfiguration when application starts        
        if (DatabaseUtil.sourceLegacyPetascopeDatabaseExists()) {
            return false;
        }
        return true;
    }

    @Override
    public void migrate() throws Exception {
        log.info("Migrating from petascopedb 9.5 or newer to different database.");
        // Lock the new database when migrating so no request is handled
        Migration migration = new Migration();
        migration.setLock(true);
        // Insert the first entry to migration table
        migrationRepositoryService.save(migration);

        // First, migrate the coverage's metadata
        this.saveAllCoverages();
        log.info("\n");

        // Second, migrate the OWS Service metadata (only one)
        this.saveOwsServiceMetadata();
        log.info("\n");

        // Last, migrate the WMS 1.3 layer's metadata
        this.saveWMSLayers();
        log.info("\n");

        // Legacy migration is done, release the lock
        this.releaseLock();

        log.info("petascopedb 9.5 or newer has been migrated successfully.");
    }

    @Override
    protected void saveAllCoverages() throws Exception {
        List<String> sourceCoverageIds = this.readAllCoverageIds();
        List<String> targetCoverageIds = coverageRepostioryService.readAllCoverageIds();

        log.info("Migrating coverages...");
        int totalCoverages = sourceCoverageIds.size();

        int i = 1;
        for (String coverageId : sourceCoverageIds) {
            log.info("Migrating coverage '" + coverageId + "' (" + i + "/" + totalCoverages + ")");

            // Check if coverage Id is already migrated
            if (targetCoverageIds.contains(coverageId)) {
                log.info("... already migrated, skipping.");
            } else {
                try {
                    // Coverage Id is not migrated yet, now read the whole coverage entity from source data source
                    Coverage coverage = this.readCoverageById(coverageId);
                    // And persist this coverage to target data source
                    coverageRepostioryService.save(coverage);
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
            OwsServiceMetadata owsServiceMetadata = this.readOwsServiceMetadata();
            owsMetadataRepostioryService.save(owsServiceMetadata);
            log.info("... migrated successfully.");
        } catch (Exception ex) {
            log.debug("Error when migrating OWS Service metadata", ex);
            log.info("... cannot migrate OWS Service metadata with error '" + ex.getMessage() + "', skpiing.");
        }
    }

    @Override
    protected void saveWMSLayers() throws Exception {
        List<String> sourceWmsLayerNames = this.readAllLayerNames();
        List<String> targetWmsLayerNames = wmsRepostioryService.readAllLayerNames();
        
        int totalLayers = sourceWmsLayerNames.size();

        int i = 1;
        for (String wmsLayerName : sourceWmsLayerNames) {
            log.info("Migrating WMS layer '" + wmsLayerName + "' (" + i + "/" + totalLayers + ")");

            // Check if WMS layer name is already migrated
            if (targetWmsLayerNames.contains(wmsLayerName)) {
                log.info("... already migrated, skipping.");
            } else {
                try {
                    // WMS layer is not migrated yet, now read the whole WMS layer content which is *slow*
                    Layer layer = this.readLayerByName(wmsLayerName);
                    // And persist this WMS layer to target datasource
                    wmsRepostioryService.saveLayer(layer);
                    log.info("... migrated successfully.");
                } catch (Exception ex) {
                    log.debug("Error when migrating layer", ex);
                    log.info("... cannot migrate layer with error '" + ex.getMessage() + "', skipping.");
                }
            }
            i++;
        }

        log.info("All WMS 1.3 layers migrated successfully.");
    }

    // Hibernate queries manually to source datasource
    /**
     * Read all persistent coverageIds from table coverage of source datasource.
     *
     * @return
     */
    @Transactional(transactionManager = SOURCE_TRANSACTION_MANAGER)
    // Read to source database, use source transaction manager
    private List<String> readAllCoverageIds() {
        Criteria criteria = this.getSourceHibernateSession().createCriteria(Coverage.class);
        criteria.setProjection(Projections.property(COVERAGE_ID_PROPERTY));
        List<String> coverageIds = criteria.list();

        return coverageIds;
    }

    /**
     * Read the whole coverage entity from source datasource by coverageId
     *
     * @param coverageId
     * @return
     */
    @Transactional(transactionManager = SOURCE_TRANSACTION_MANAGER)
    // Read to source database, use source transaction manager
    private Coverage readCoverageById(String coverageId) {
        Criteria criteria = this.getSourceHibernateSession().createCriteria(Coverage.class);
        criteria.add(Restrictions.eq(COVERAGE_ID_PROPERTY, coverageId));
        Coverage coverage = (Coverage) criteria.list().get(0);

        return coverage;
    }

    /**
     * Read the OWS ServiceMetadata (only 1) entity from source datasource
     */
    @Transactional(transactionManager = SOURCE_TRANSACTION_MANAGER)
    private OwsServiceMetadata readOwsServiceMetadata() {
        Criteria criteria = this.getSourceHibernateSession().createCriteria(OwsServiceMetadata.class);
        OwsServiceMetadata owsServiceMetadata;
        if (criteria.list().isEmpty()) {
            owsServiceMetadata = this.owsMetadataRepostioryService.createDefaultOWSMetadataService();
            return owsServiceMetadata;
        } else {
            owsServiceMetadata = (OwsServiceMetadata) criteria.list().get(0);
            return owsServiceMetadata;
        }
    }

    /**
     * Read all persistent WMS1.3 layer names from table coverage of source
     * datasource.
     *
     * @return
     */
    @Transactional(transactionManager = SOURCE_TRANSACTION_MANAGER)
    // Read to source database, use source transaction manager
    private List<String> readAllLayerNames() {
        Criteria criteria = this.getSourceHibernateSession().createCriteria(Layer.class);
        criteria.setProjection(Projections.property(LAYER_NAME_PROPERTY));
        List<String> layerNames = criteria.list();

        return layerNames;
    }

    /**
     * Read the whole WMS1.3 layer entity from source datasource by layerName.
     *
     * @param coverageId
     * @return
     */
    @Transactional(transactionManager = SOURCE_TRANSACTION_MANAGER)
    // Read to source database, use source transaction manager
    private Layer readLayerByName(String layerName) {
        Criteria criteria = this.getSourceHibernateSession().createCriteria(Layer.class);
        criteria.add(Restrictions.eq(LAYER_NAME_PROPERTY, layerName));
        Layer layer = (Layer) criteria.list().get(0);

        return layer;
    }
}