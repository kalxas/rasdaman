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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcst.handlers;

import com.rasdaman.accesscontrol.service.AuthenticationService;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.admin.pyramid.service.AdminRemovePyramidMemberService;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.RasdamanDownscaledCollection;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.repository.service.CoveragePyramidRepositoryService;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMSRepostioryService;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.PetascopeController;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.exceptions.WCSException;
import petascope.util.ras.RasUtil;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.util.ras.TypeRegistry;
import static petascope.util.ras.TypeRegistry.ARRAY_TYPE_SUFFIX;
import static petascope.util.ras.TypeRegistry.CELL_TYPE_SUFFIX;
import static petascope.util.ras.TypeRegistry.SET_TYPE_SUFFIX;
import petascope.wcst.exceptions.WCSTCoverageIdNotFound;
import petascope.wcst.parsers.DeleteCoverageRequest;
import petascope.wms.handlers.service.WMSGetMapCachingService;
import org.rasdaman.repository.service.WMTSRepositoryService;
import petascope.util.CrsUtil;
import petascope.wmts.handlers.service.WMTSGetCapabilitiesService;

/**
 * Handles the deletion of a coverage.
 *
 * @autor <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class DeleteCoverageHandler {
    
    @Autowired
    private CoverageRepositoryService coverageRepostioryService;
    @Autowired
    private WMSRepostioryService wmsRepositoryService;
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;
    @Autowired
    private CoveragePyramidRepositoryService coveragePyramidRepositoryService;
    @Autowired
    private AdminRemovePyramidMemberService removePyramidMemberService;
    @Autowired
    private WMTSRepositoryService wmtsRepositoryService;
    
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private PetascopeController petascopeController;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeleteCoverageHandler.class);

    public Response handle(DeleteCoverageRequest request) throws Exception {
        
        petascopeController.validateWriteRequestFromIP(httpServletRequest);
        
        String username = ConfigManager.RASDAMAN_ADMIN_USER;
        String password = ConfigManager.RASDAMAN_ADMIN_PASS;

        // List of coverageIds to be deleted
        List<String> coverageIds = request.getCoverageIds();
        List<Coverage> coverages = new ArrayList<>();
        // Get all the coverages by id and throw exception if one does not exist
        for (String coverageId : coverageIds) {
            Coverage coverage = this.getCoverageById(coverageId);
            coverages.add(coverage);
        }

        //delete all of them
        for (Coverage coverage : coverages) {

            String collectionName = coverage.getRasdamanRangeSet().getCollectionName();
            
            if (collectionName != null) {
                // first, try to delete the rasdaman collection.
                try {
                    this.deleteFromRasdaman(collectionName, username, password);
                } catch (RasdamanException e) {
                    log.error("Cannot delete collection: " + collectionName + ", error: ", e);
                    // NOTE: If cannot delete collection for some reason (e.g: collection does not exist), it should not throw exception as it cannot delete coverage's metadata
                }
            }
            
            // then, try to delete all associated downscaled collection
            try {
                this.deleteAssociatedScaleLevelFromRasdaman(coverage, username, password);
            } catch (Exception ex) {
                // NOTE: If cannot delete collection for some reason (e.g: collection does not exist), it should not throw exception as it cannot delete coverage's metadata
                log.error("Cannot delete associated scale levels with collection '" + collectionName +"'. Reason: " + ex.getMessage());
            }

            // check if WMS layer does exist, then remove it as well
            this.deleteFromWMS(coverage.getCoverageId());

            // collection is removed, try to remove coverage metadata
            this.removeCoverageFromPyramids(coverage);
            coverageRepostioryService.delete(coverage);
            
            // Finally, delete all created set/mdd/cell types for this coverage if no other coverages is using them.
            String collectionType = coverage.getRasdamanRangeSet().getCollectionType();
            if (!coverageRepostioryService.collectionTypeExist(collectionType)) {
                this.dropUnusedRasdamanTypes(collectionType);
            }

        }
        
        return new Response();
    }
    
    
    /**
     * Given an used rasdaman collectionType -> drop any other related types for this collectionType
     */
    public static void dropUnusedRasdamanTypes(String collectionType) throws PetascopeException {
        String suffix = collectionType.substring(collectionType.length() - 4, collectionType.length());
                
        // NOTE: Only delete types which were created from Petascope internally (with suffix _Set, _Array, _Cell)
        if (suffix.equals(SET_TYPE_SUFFIX)) {
            String prefix = collectionType.substring(0, collectionType.length() - 4);
            String mddType = prefix + ARRAY_TYPE_SUFFIX;
            String cellType = prefix + CELL_TYPE_SUFFIX;

            // And delete them from registry
            TypeRegistry typeRegistry = TypeRegistry.getInstance();
            boolean setTypeExist = typeRegistry.deleteSetTypeFromRegistry(collectionType);
            boolean mddTypeExist = typeRegistry.deleteMDDTypeFromRegistry(mddType);
            boolean cellTypeExist = typeRegistry.deleteCellTypeFromRegistry(cellType);

            final String TYPE_IS_USED_BY_ANOTHER_OBJECT_ERROR_MESSAGE = "currently in use by another stored object";

             // Then, delete these types from Rasdaman
            if (setTypeExist) {
                try {
                    RasUtil.dropRasdamanType(collectionType);
                } catch (RasdamanException ex) {
                    if (!ex.getMessage().contains(TYPE_IS_USED_BY_ANOTHER_OBJECT_ERROR_MESSAGE)) {
                        throw ex;
                    }
                }
            }
            if (mddTypeExist) {
                if (!setTypeExist) { 
                    log.warn("mdd type found but corresponding set type '" + collectionType + "'  not found in rasdaman.");
                }
                try {
                    RasUtil.dropRasdamanType(mddType);
                } catch (RasdamanException ex) {
                    if (!ex.getMessage().contains(TYPE_IS_USED_BY_ANOTHER_OBJECT_ERROR_MESSAGE)) {
                        throw ex;
                    }
                }
            }
            if (cellTypeExist) {
                try {
                    RasUtil.dropRasdamanType(cellType);
                } catch (RasdamanException ex) {
                    if (!ex.getMessage().contains(TYPE_IS_USED_BY_ANOTHER_OBJECT_ERROR_MESSAGE)) {
                        throw ex;
                    }
                }
            }
        }
    }
    
    /**
     * Remove the deleted coverage from the pyramids of all containing coverages.
     * Then, whitelist all pyramid member of this deleted coverage.
     * 
     * e.g. delete cov4 which contains cov8 and cov16 as pyramid members
     *      cov1 and cov2 contain cov4 as pyramid member
     * 
     * then, delete cov4 means:
     *  - whitelist cov8 and cov16
     *  - remove cov4 from the pyramids of cov1 and cov2
     */
    private void removeCoverageFromPyramids(Coverage deletingCoverage) throws PetascopeException {
        // e.g. cov4
        String deletingCoverageId = deletingCoverage.getCoverageId();
            
        
        // e.g cov1 and cov2 contain cov4 in their pyramids, then remove cov4 from these pyramids
        List<GeneralGridCoverage> containingCoverages = this.coveragePyramidRepositoryService.getCoveragesContainingPyramidMemberCoverageId(deletingCoverageId);
        for (GeneralGridCoverage containingCoverage : containingCoverages) {
            this.removePyramidMemberService.removePyramidMemberCoverage(containingCoverage, deletingCoverageId);
        }
    }

    /**
     * Check if coverageName exists in petascopedb
     *
     * @param coverageId
     * @throws WCSException
     */
    private Coverage getCoverageById(String coverageId) throws WCSException, PetascopeException {
        
        Coverage coverage = null;
        
        try {
            coverage = coverageRepostioryService.readCoverageByIdFromDatabase(coverageId);
        } catch (PetascopeException ex) {
            if (ex.getExceptionCode().equals(ExceptionCode.NoSuchCoverage)) {
                // In case local coverage doesn't exist, then check if this coverageId exists in the cache or not,
                // if it is (for some reasons), then remove it from the cache, so it will not appear for WCS GetCapabilities result
                if (this.coverageRepostioryService.isInLocalCache(coverageId)) {
                    this.coverageRepostioryService.removeFromLocalCacheMap(coverageId);
                }

                if (this.wmsRepositoryService.isInLocalCache(coverageId)) {
                    this.wmsRepositoryService.removeLayerFromLocalCache(coverageId);
                }

                throw new WCSTCoverageIdNotFound(coverageId);
            }
        }

        return coverage;
    }

    /**
     * Delete a collection from Rasdaman
     *
     * @param coverage
     * @throws RasdamanException
     */
    private void deleteFromRasdaman(String collectionName, String username, String password) throws RasdamanException, PetascopeException {
        RasUtil.deleteFromRasdaman(collectionName, username, password);
    }
    
    /**
     * Delete all associated scale level collections of this input coverage.
     */
    private void deleteAssociatedScaleLevelFromRasdaman(Coverage coverage, String username, String password) throws RasdamanException, PetascopeException {
        for (RasdamanDownscaledCollection rasdamanScaleDownCollection : coverage.getRasdamanRangeSet().getRasdamanDownscaledCollections()) {
            String collectionName = rasdamanScaleDownCollection.getCollectionName();
            RasUtil.deleteFromRasdaman(collectionName, username, password);
        }
    }

    /**
     * When delete a coverage from WCS, it will need to delete the existing WMS
     * layer as well.
     *
     * @param coverageId the layerID from WMS service to delete
     */
    private void deleteFromWMS(String coverageId) throws PetascopeException {
        Layer layer = null;

        try {
            layer = wmsRepositoryService.readLayerByNameFromDatabase(coverageId);
        } catch(PetascopeException ex) {
            if (!ex.getExceptionCode().equals(ExceptionCode.NoSuchLayer)) {
                throw ex;
            }
        }
        
        if (layer != null) {
            // Layer does exist, remove it
            wmsRepositoryService.deleteLayer(layer);
            
            String epsgCode = CrsUtil.getAuthorityCode(layer.getGeoXYCRS());
            this.wmtsRepositoryService.removeTileMatrixSetFromLocalCache(layer.getName(), epsgCode);
        }
        
        // Also remove all the cached layers's GetMap requests
        wmsGetMapCachingService.removeLayerGetMapInCache(coverageId);
    }

}
