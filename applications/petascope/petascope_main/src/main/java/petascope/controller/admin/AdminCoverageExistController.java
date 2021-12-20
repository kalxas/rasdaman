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
package petascope.controller.admin;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import static org.rasdaman.config.ConfigManager.ADMIN;
import static org.rasdaman.config.ConfigManager.COVERAGE;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import petascope.controller.AbstractController;
import static petascope.core.KVPSymbols.KEY_COVERAGEID;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * Controller to handle non standard request for checking if a coverage exists
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */

@RestController
public class AdminCoverageExistController extends AbstractController {
    
    private static final String COVERAGE_EXISTS_PATH = ADMIN + "/" + COVERAGE + "/exist";
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;

    @Override
    @RequestMapping(path = COVERAGE_EXISTS_PATH,  method = RequestMethod.GET)
    protected void handleGet(HttpServletRequest httpServletRequest) throws Exception {
        this.handle(httpServletRequest, false);
    }
    
    @Override
    @RequestMapping(path = COVERAGE_EXISTS_PATH,  method = RequestMethod.POST)
    protected void handlePost(HttpServletRequest httpServletRequest) throws Exception {
        this.handle(httpServletRequest, true);
    }
    
    private void handle(HttpServletRequest httpServletRequest, boolean isPost) throws Exception {
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());
        
        if (isPost) {
            String postBody = this.getPOSTRequestBody(httpServletRequest);
            kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        }
        
        String coverageId = this.getValueByKeyAllowNull(kvpParameters, KEY_COVERAGEID);
        Boolean exist = false;
        
        if (coverageId == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                    "Missing one of mandatory request parameters '" + KEY_COVERAGEID + "'.");
        } else {
            
            exist = this.coverageRepositoryService.isInLocalCache(coverageId);
            
            this.writeTextResponse(exist);
        }
    }

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
    }
    
}
