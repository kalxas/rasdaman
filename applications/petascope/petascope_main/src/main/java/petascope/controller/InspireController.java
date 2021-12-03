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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.controller;


import com.rasdaman.accesscontrol.service.AuthenticationService;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import static petascope.core.KVPSymbols.KEY_INSPIRE_COVERAGE_ID;
import static petascope.core.KVPSymbols.KEY_INSPIRE_METADATA_URL;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.StringUtil;

/**
 * Controller to handle non standard request for INSPIRE coverages
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */

@RestController
public class InspireController extends AbstractController {
    
    private static org.slf4j.Logger log = LoggerFactory.getLogger(InspireController.class);
    
    private static final String UPDATE_METADATA_PATH = ConfigManager.ADMIN + "/inspire/metadata/update";
    
    public static final String PRIV_OWS_WCS_UPDATE_COV = "PRIV_OWS_WCS_UPDATE_COV";
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;

    @Override
    @RequestMapping(path = UPDATE_METADATA_PATH, method = RequestMethod.GET)
    protected void handleGet(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());
        this.requestDispatcher(httpServletRequest, kvpParameters);
    }
    
    @Override
    @RequestMapping(path = UPDATE_METADATA_PATH, method = RequestMethod.POST)
    protected void handlePost(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, String[]> kvpParameters = this.parsePostRequestToKVPMap(httpServletRequest);
        this.requestDispatcher(httpServletRequest, kvpParameters);
    }

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
        
        // If user has petascope admin credentials (e.g: logged in from WSClient) from external place,
        // then no need to check if his IP is allowed anymore.
        AuthenticationService.validateWriteRequestByRoleOrAllowedIP(httpServletRequest);
        
        String coverageId = this.getValueByKeyAllowNull(kvpParameters, KEY_INSPIRE_COVERAGE_ID);
        String metadataURL = this.getValueByKeyAllowNull(kvpParameters, KEY_INSPIRE_METADATA_URL);
        
        if (coverageId == null && metadataURL == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                    "Missing one of mandatory request parameters '" + KEY_INSPIRE_COVERAGE_ID + "' or '" + KEY_INSPIRE_METADATA_URL + "'.");
        }
        
        if (!this.coverageRepositoryService.isInLocalCache(coverageId)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                    "Coverage '" + coverageId + "' does not exist in local database to update INSPIRE metadata URL.");
        }
        
        Coverage coverage = this.coverageRepositoryService.readCoverageFromLocalCache(coverageId);
        if (!StringUtil.equalsIgnoreNull(coverage.getInspireMetadataURL(), metadataURL)) {
            coverage.setInspireMetadataURL(metadataURL);
            this.coverageRepositoryService.saveCoverageInspireMetadataURL(coverage);

            log.info("Updated INSPIRE metadata URL '" + metadataURL + "' for coverage '" + coverageId + "'.");
        }
    }
    
}
