// -- rasdaman enterprise begin

package org.rasdaman.datamigration;
import com.rasdaman.admin.layer.service.AdminCreateOrUpdateLayerService;
import java.util.ArrayList;
import java.util.List;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.Wgs84BoundingBox;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsProjectionUtil;

/**
 * Class to handle data migration version number 8.
 * It needs to update any local WMS layers from their associated coverages
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class DataMigration8Handler extends AbstractDataMigrationHandler {
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private AdminCreateOrUpdateLayerService createOrUpdateLayerService;
    
    private static final Logger log = LoggerFactory.getLogger(DataMigration8Handler.class);
    
    public DataMigration8Handler() {
        // NOTE: update this by one for new handler class
        this.migrationVersion = 8;
        this.handlerId = "8ca57fde-4fa4-11ed-a476-509a4cb4e064";
    }

    @Override
    public void migrate() throws PetascopeException, Exception {
        
        this.wmsRepostioryService.readAllLocalLayers();
        
        for (String coverageId : this.coverageRepositoryService.readAllLocalCoverageIds()) {
            if (this.wmsRepostioryService.isInLocalCache(coverageId)) {                
                this.coverageRepositoryService.readCoverageFullMetadataByIdFromCache(coverageId);
                // If this coverage has an associated WMS layer -> update
                this.createOrUpdateLayerService.save(coverageId, null);
            }
        }
        
    }
    
}

// -- rasdaman enterprise end