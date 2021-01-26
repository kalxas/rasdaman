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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import static org.rasdaman.config.ConfigManager.OWS;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static petascope.core.KVPSymbols.KEY_COVERAGEID;
import static petascope.core.KVPSymbols.KEY_WMS_LAYER;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.MIMEUtil;

/**
 * Controller to handle non standard request for retrieving information about objects (coverages)
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */

@RestController
public class OWSObjectController extends AbstractController {
    
    /**
     * Check if a coverage exists, it is used for wcst_import to send an InsertCoverage or UpdateCoverage request
     */
    private static final String OBJECT_EXIST_PATH = OWS + "/objectExists";
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;

    @Override
    @RequestMapping(path = OBJECT_EXIST_PATH)
    protected void handleGet(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());
        this.requestDispatcher(httpServletRequest, kvpParameters);
    }

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
        String coverageId = this.getValueByKeyAllowNull(kvpParameters, KEY_COVERAGEID);
        String layerName = this.getValueByKeyAllowNull(kvpParameters, KEY_WMS_LAYER);
        
        if (coverageId == null && layerName == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                    "Missing one of mandatory request parameters '" + KEY_COVERAGEID + "' or '" + KEY_WMS_LAYER + "'.");
        }
        
        if (coverageId != null) {
            Boolean exist = this.coverageRepositoryService.isInCache(coverageId);
            this.writeTextResponse(exist);
        }
        
        if (layerName != null) {
            Boolean exist = this.wmsRepostioryService.isInCache(layerName);
            this.writeTextResponse(exist);
        }
    }
    
}
