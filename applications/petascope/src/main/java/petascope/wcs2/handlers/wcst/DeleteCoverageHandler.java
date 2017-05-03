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

package petascope.wcs2.handlers.wcst;

import java.math.BigInteger;
import java.sql.SQLException;

import org.slf4j.LoggerFactory;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.rasdaman.RasdamanException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.exceptions.rasdaman.RasdamanCollectionDoesNotExistException;
import petascope.util.Pair;
import petascope.util.ras.RasUtil;
import petascope.wcs2.handlers.AbstractRequestHandler;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.parsers.wcst.DeleteCoverageRequest;
import petascope.wms2.orchestration.ServiceOrchestrator;
import petascope.wms2.service.deletelayer.DeleteLayerHandler;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.servlet.WMSServlet;

/**
 * Handles the deletion of a coverage.
 * @autor <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class DeleteCoverageHandler extends AbstractRequestHandler<DeleteCoverageRequest> {

    public DeleteCoverageHandler(DbMetadataSource meta) {
        super(meta);
    }

    @Override
    public Response handle(DeleteCoverageRequest request) throws PetascopeException, SecoreException {
        String[] coverageIds = request.getCoverageId().split(COVERAGE_IDS_SEPARATOR);
        //check that all the ids exist
        for (String coverageId : coverageIds) {
            checkCoverageId(coverageId);
        }
        //delete all of them
        for (String coverageId : coverageIds) {
            try {                
                CoverageMetadata coverageMetadata = null;                
                try {
                    coverageMetadata = this.meta.read(coverageId);
                } catch (PetascopeException e) {
                    if (e.getExceptionCode().equals(ExceptionCode.CollectionDoesNotExist)) {
                        // NOTE: when collection does not exist, the coverage metadata is polluted and should be removed.                                                
                        this.deleteCoverageMetadata(coverageId);
                        return new Response(new String[] {""});
                    }
                    else {
                        throw e;
                    }
                }
                
                // first, try to delete the rasdaman collection.
                try {
                    deleteFromRasdaman(coverageMetadata);
                } catch (RasdamanException e) {
                    log.error("Cannot delete collection: " + coverageMetadata.getRasdamanCollection().snd + ", error: ", e);
                    throw new PetascopeException(ExceptionCode.InternalSqlError, e);
                } catch (Exception ex) {
                    log.error("Cannot delete collection: " + coverageMetadata.getRasdamanCollection().snd + ", error: ", ex);
                    throw new PetascopeException(ExceptionCode.InternalSqlError, ex);
                }               
                
                // collection is removed, try to remove coverage metadata
                this.deleteCoverageMetadata(coverageId);

            } catch (SQLException e) {
                log.error("Cannot delete coverage: " + coverageId + " from database, error: ", e);
                throw new PetascopeException(ExceptionCode.InternalSqlError);
            } catch (WMSException ex) {
                log.error("Cannot delete layer: " + coverageId + " by WMS service, error: ", ex);
                throw new PetascopeException(ExceptionCode.InternalWMSError);
            }
        }
        return new Response(new String[] {""});
    }
    
    /**
     * Delete coverage metadata (WCS, WMS) in petascopedb
     * @param coverageId
     * @throws PetascopeException
     * @throws SQLException
     * @throws WMSException 
     */
    private void deleteCoverageMetadata(String coverageId) throws PetascopeException, SQLException, WMSException {
        // delete coverage metadata when collection is deleted
        this.meta.delete(coverageId);

        // delete WMS layer of this coverageID (if possible)
        this.deleteFromWMS(coverageId);

        //all went well, commit
        this.meta.commitAndClose();
    }

    /**
     * Check if coverageName exists in petascopedb
     * @param coverageId
     * @throws WCSException 
     */
    private void checkCoverageId(String coverageId) throws WCSException {
        if (!meta.existsCoverageName(coverageId)) {
            throw new WCSException(ExceptionCode.NoSuchCoverage, coverageId);
        }
    }

    /**
     * Delete a collection from Rasdaman
     * @param coverage
     * @throws RasdamanException 
     */
    private void deleteFromRasdaman(CoverageMetadata coverage) throws RasdamanException, PetascopeException {
        Pair<BigInteger, String> collection = coverage.getRasdamanCollection();
        RasUtil.deleteFromRasdaman(collection.fst, collection.snd);
    }

    /**
     * When delete a coverage from WCS, it will need to delete the existing WMS layer as well.
     * @param coverageId the layerID from WMS service to delete
     */
    private void deleteFromWMS(String coverageId) throws WMSException, SQLException {
        ServiceOrchestrator serviceOrchestrator = WMSServlet.getServiceOrchestrator();
        DeleteLayerHandler handler = new DeleteLayerHandler(serviceOrchestrator.getPersistentProvider());
        handler.DeleteLayerByID(coverageId);
    }

    private final static String COVERAGE_IDS_SEPARATOR = ",";

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeleteCoverageHandler.class);
}