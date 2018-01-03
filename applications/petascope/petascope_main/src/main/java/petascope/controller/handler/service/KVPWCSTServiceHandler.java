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
package petascope.controller.handler.service;

import java.io.IOException;
import java.util.Map;
import org.rasdaman.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.wcst.exceptions.WCSTCoverageParameterNotFound;
import petascope.wcst.exceptions.WCSTInvalidXML;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.wcst.handlers.DeleteCoverageHandler;
import petascope.wcst.handlers.InsertCoverageHandler;
import petascope.wcst.handlers.UpdateCoverageHandler;
import petascope.wcst.parsers.DeleteCoverageRequest;
import petascope.wcst.parsers.InsertCoverageRequest;
import petascope.wcst.parsers.KVPWCSTParser;
import petascope.wcst.parsers.UpdateCoverageRequest;

/**
 *
 * Service class for wcst_import request in KVP format (InsertCoverage,
 * UpdateCoverage, DeleteCoverage)
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class KVPWCSTServiceHandler extends AbstractHandler {
    
    private static final Logger log = LoggerFactory.getLogger(KVPWCSTServiceHandler.class);

    @Autowired private
    InsertCoverageHandler insertCoverageHandler;
    @Autowired private
    UpdateCoverageHandler updateCoverageHandler;
    @Autowired private
    DeleteCoverageHandler deleteCoverageHandler;

    @Autowired private
    KVPWCSTParser kvpWCSTParser;

    public KVPWCSTServiceHandler() {
        // WCST is a part of WCS2
        service = KVPSymbols.WCS_SERVICE;
        version = ConfigManager.WCS_VERSIONS;
        
        requestServices.add(KVPSymbols.VALUE_INSERT_COVERAGE);
        requestServices.add(KVPSymbols.VALUE_UPDATE_COVERAGE);
        requestServices.add(KVPSymbols.VALUE_DELETE_COVERAGE);
    }


    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws WCSException, IOException, PetascopeException, SecoreException {
        Response response = null;
        String queryString = this.getQueryString(kvpParameters);

        // First request is DescribeCoverage to check coverage was persisted
        if (queryString.contains(KVPSymbols.VALUE_INSERT_COVERAGE)) {
            // If coverage does not exist then insert the coverage metadata and rasdaman collection with mdd type
            long start = System.currentTimeMillis();
            response = this.handleInsertCoverageRequest(kvpParameters);
            long end = System.currentTimeMillis();
            
            log.debug("Total time to create a new coverage is " + String.valueOf(end - start) + " ms.");
        } else if (queryString.contains(KVPSymbols.VALUE_UPDATE_COVERAGE)) {
            // If coverage does exist then update the coverage metadata and rasdaman collection from file
            long start = System.currentTimeMillis();
            response = this.handleUpdateCoverageRequest(kvpParameters);
            long end = System.currentTimeMillis();
            
            log.debug("Total time to update a slice to an existing coverage is " + String.valueOf(end - start) + " ms.");
        } else if (queryString.contains(KVPSymbols.VALUE_DELETE_COVERAGE)) {
            // Delete the coverage listed in the CoverageId parameters (e.g: CoverageId=a,b,c)
            long start = System.currentTimeMillis();
            response = this.handleDeleteCoverageRequest(kvpParameters);
            long end = System.currentTimeMillis();
            
            log.debug("Total time to delete an existing coverage is " + String.valueOf(end - start) + " ms.");
        }

        return response;
    }

    /**
     * Insert coverage from wcst_import request with GML and rasdaman collection
     * type
     *
     * @Return
     */
    private Response handleInsertCoverageRequest(Map<String, String[]> kvpParameters) throws WCSException, PetascopeException, SecoreException {
        InsertCoverageRequest insertCoverageRequest = (InsertCoverageRequest) kvpWCSTParser.parse(kvpParameters);
        Response response = this.insertCoverageHandler.handle(insertCoverageRequest);

        return response;
    }

    /**
     * Update coverage from wcst_import request with GML and insert data to
     * rasdaman collection from file
     *
     * @return
     */
    private Response handleUpdateCoverageRequest(Map<String, String[]> kvpParameters)
            throws WCSException, WCSTInvalidXML, PetascopeException, WCSTCoverageParameterNotFound, SecoreException {
        UpdateCoverageRequest updateCoverageRequest = (UpdateCoverageRequest) kvpWCSTParser.parse(kvpParameters);
        Response response = this.updateCoverageHandler.handle(updateCoverageRequest);

        return response;
    }

    /**
     * Delete list of coverages from wcst_import request
     *
     * @param queryString
     * @return
     */
    private Response handleDeleteCoverageRequest(Map<String, String[]> kvpParameters) throws WCSException, PetascopeException, SecoreException {
        DeleteCoverageRequest deleteCoverageRequest = ((DeleteCoverageRequest) kvpWCSTParser.parse(kvpParameters));
        Response response = this.deleteCoverageHandler.handle(deleteCoverageRequest);

        return response;
    }
}
