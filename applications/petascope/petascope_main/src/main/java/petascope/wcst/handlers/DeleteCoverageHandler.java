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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcst.handlers;

import java.util.ArrayList;
import java.util.List;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.repository.service.CoverageRepostioryService;
import org.rasdaman.repository.service.WMSRepostioryService;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.ras.RasUtil;
import petascope.core.response.Response;
import petascope.wcst.exceptions.WCSTCoverageIdNotFound;
import petascope.wcst.parsers.DeleteCoverageRequest;
import petascope.wms.exception.WMSLayerNotExistException;
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
                throw new PetascopeException(ExceptionCode.InternalSqlError, e);
            }

            // check if WMS layer does exist, then remove it as well
            this.deleteFromWMS(coverage.getCoverageId());

            // collection is removed, try to remove coverage metadata
            coverageRepostioryService.delete(coverage);

        }
        return new Response();
    }

    /**
     * Check if coverageName exists in petascopedb
     *
     * @param coverageId
     * @throws WCSException
     */
    private Coverage getCoverageById(String coverageId) throws WCSException, PetascopeException {
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
     * When delete a coverage from WCS, it will need to delete the existing WMS
     * layer as well.
     *
     * @param coverageId the layerID from WMS service to delete
     */
    private void deleteFromWMS(String coverageId) {
        Layer layer = wmsRepositoryService.readLayerByNameFromDatabase(coverageId);
        if (layer != null) {
            // Layer does exist, remove it
            wmsRepositoryService.deleteLayer(layer);

            // Also remove all the cached layers's GetMap requests
            wmsGetMapCachingService.removeLayerGetMapInCache(layer.getName());
        }
    }

}
