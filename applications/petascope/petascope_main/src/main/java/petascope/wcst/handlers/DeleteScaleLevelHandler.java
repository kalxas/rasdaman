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

import java.math.BigDecimal;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.service.PyramidService;
import petascope.wcst.parsers.DeleteScaleLevelRequest;
import petascope.wms.handlers.service.WMSGetMapCachingService;

/**
 * Class to handle DeleteScaleLevel request from WCST_Import to delete a
 * corresponding downscaled collection based on the input scale level.
 *
 * e.g: http://localhost:8080/rasdaman/ows?serivce=WCS&version=2.0.1&request=DeleteScaleLevel&level=2&coverageId=test_mr
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class DeleteScaleLevelHandler {

    @Autowired
    private PyramidService pyramidService;
    
    @Autowired
    private WMSGetMapCachingService wmsGetMapCachingService;
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeleteScaleLevelHandler.class);

    /**
     * Handle the DeleteScaleLevel request from client. 
     */
    public Response handle(DeleteScaleLevelRequest request) throws PetascopeException, SecoreException {
        log.debug("Handling coverage's scale level delete...");
        
        String coverageId = request.getCoverageId();
        BigDecimal level = request.getLevel();
        
        // First delete rasdaman downscaled collection
        this.pyramidService.deleteScaleLevel(coverageId, level);
        
        // Also remove GetMap requests from cache for this layer
        wmsGetMapCachingService.removeLayerGetMapInCache(coverageId);
        
        Response response = new Response();
        
        return response;
    }
}
