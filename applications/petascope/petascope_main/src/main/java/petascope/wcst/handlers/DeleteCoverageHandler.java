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

import java.util.ArrayList;
import java.util.List;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.RasdamanDownscaledCollection;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.repository.service.CoverageRepostioryService;
import org.rasdaman.repository.service.WMSRepostioryService;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.ras.RasUtil;
import petascope.core.response.Response;
import petascope.util.ras.TypeRegistry;
import static petascope.util.ras.TypeRegistry.ARRAY_TYPE_SUFFIX;
import static petascope.util.ras.TypeRegistry.CELL_TYPE_SUFFIX;
import static petascope.util.ras.TypeRegistry.SET_TYPE_SUFFIX;
import petascope.wcst.exceptions.WCSTCoverageIdNotFound;
import petascope.wcst.parsers.DeleteCoverageRequest;
import petascope.wms.handlers.service.WMSGetMapCachingService;

/**
 * Handles the deletion of a coverage.
 *
 * @autor <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class DeleteCoverageHandler {
    
    @Autowired
    private CoverageRepostioryService coverageRepostioryService;
    @Autowired
    private WMSRepostioryService wmsRepositoryService;
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeleteCoverageHandler.class);

    public Response handle(DeleteCoverageRequest request) throws PetascopeException, SecoreException {

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
            Long oid = coverage.getRasdamanRangeSet().getOid();
            String collectionName = coverage.getRasdamanRangeSet().getCollectionName();

            // first, try to delete the rasdaman collection.
            try {
                this.deleteFromRasdaman(oid, collectionName);
            } catch (RasdamanException e) {
                log.error("Cannot delete collection: " + collectionName + ", error: ", e);
                // NOTE: If cannot delete collection for some reason (e.g: collection does not exist), it should not throw exception as it cannot delete coverage's metadata
            }
            
            // then, try to delete all associated downscaled collection
            try {
                this.deleteAssociatedScaleLevelFromRasdaman(coverage);
            } catch (Exception ex) {
                // NOTE: If cannot delete collection for some reason (e.g: collection does not exist), it should not throw exception as it cannot delete coverage's metadata
                log.error("Cannot delete associated scale levels with collection '" + collectionName +"'. Reason: " + ex.getMessage());
            }

            // check if WMS layer does exist, then remove it as well
            this.deleteFromWMS(coverage.getCoverageId());

            // collection is removed, try to remove coverage metadata
            coverageRepostioryService.delete(coverage);
            
            // Finally, delete all created set/mdd/cell types for this coverage if no other coverages is using them.
            String collectionType = coverage.getRasdamanRangeSet().getCollectionType();
            if (!coverageRepostioryService.collectionTypeExist(collectionType)) {
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
                    
                     // Then, delete these types from Rasdaman
                    if (setTypeExist) {
                        RasUtil.dropRasdamanType(collectionType);
                    }
                    if (mddTypeExist) {
                        if (!setTypeExist) { 
                            log.warn("mdd type found but corresponding set type '" + collectionType + "'  not found in rasdaman.");
                        }
                        RasUtil.dropRasdamanType(mddType);
                    }
                    if (cellTypeExist) {
                        RasUtil.dropRasdamanType(cellType);
                    }
                }
            }

        }
        
        Response response = new Response();
        response.setCoverageID(coverageIds.get(0));
        
        return new Response();
    }

    /**
     * Check if coverageName exists in petascopedb
     *
     * @param coverageId
     * @throws WCSException
     */
    private Coverage getCoverageById(String coverageId) throws WCSException, PetascopeException, SecoreException {
        Coverage coverage = coverageRepostioryService.readCoverageByIdFromDatabase(coverageId);
        if (coverage == null) {
            throw new WCSTCoverageIdNotFound(coverageId);
        }

        return coverage;
    }

    /**
     * Delete a collection from Rasdaman
     *
     * @param coverage
     * @throws RasdamanException
     */
    private void deleteFromRasdaman(Long oid, String collectionName) throws RasdamanException, PetascopeException {
        RasUtil.deleteFromRasdaman(oid, collectionName);
    }
    
    /**
     * Delete all associated scale level collections of this input coverage.
     */
    private void deleteAssociatedScaleLevelFromRasdaman(Coverage coverage) throws RasdamanException, PetascopeException {
        for (RasdamanDownscaledCollection rasdamanScaleDownCollection : coverage.getRasdamanRangeSet().getRasdamanDownscaledCollections()) {
            Long oid = rasdamanScaleDownCollection.getOid();
            String collectionName = rasdamanScaleDownCollection.getCollectionName();
            RasUtil.deleteFromRasdaman(oid, collectionName);
        }
    }

    /**
     * When delete a coverage from WCS, it will need to delete the existing WMS
     * layer as well.
     *
     * @param coverageId the layerID from WMS service to delete
     */
    private void deleteFromWMS(String coverageId) throws PetascopeException {
        Layer layer = wmsRepositoryService.readLayerByNameFromDatabase(coverageId);
        if (layer != null) {
            // Layer does exist, remove it
            wmsRepositoryService.deleteLayer(layer);

            // Also remove all the cached layers's GetMap requests
            wmsGetMapCachingService.removeLayerGetMapInCache(layer.getName());
        }
    }

}
