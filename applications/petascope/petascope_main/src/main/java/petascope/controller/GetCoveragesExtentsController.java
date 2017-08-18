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
package petascope.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import static org.rasdaman.config.ConfigManager.GET_COVERAGE_EXTENTS;
import static org.rasdaman.config.ConfigManager.OWS;
import org.rasdaman.repository.service.CoverageRepostioryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import petascope.core.BoundingBox;
import petascope.core.CoverageExtent;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.exceptions.WMSException;
import petascope.util.JSONUtil;
import petascope.util.MIMEUtil;

/**
 * Controller for a GetCoveragesExtents request which returns list of
 * coverages's extents in EPSG:4326 for WCS-Client to display on Openlayers map.
 *
 * <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@RestController
// @Controller will have an error to find a jsp file: GetCoveragesExtentsController.jsp, so only use @RestController
public class GetCoveragesExtentsController extends AbstractController {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(GetCoveragesExtentsController.class);
    
    @Autowired
    CoverageRepostioryService coverageRepostioryService;

    @RequestMapping(value = OWS + "/" + GET_COVERAGE_EXTENTS, method = RequestMethod.POST)
    protected void handlePost(HttpServletRequest httpServletRequest) throws IOException, PetascopeException, WCSException, SecoreException, Exception {
        this.handleGet(httpServletRequest);
    }

    @RequestMapping(value = OWS + "/" + GET_COVERAGE_EXTENTS, method = RequestMethod.GET)
    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());
        this.requestDispatcher(kvpParameters);
    }

    @Override
    protected void requestDispatcher(Map<String, String[]> kvpParameters) throws IOException, PetascopeException, WCSException, SecoreException, WMSException {
        log.debug("Received request: " + this.getRequestRepresentation(kvpParameters));
        Response response = this.handle(kvpParameters);
        this.writeResponseResult(response);
    }

    /**
     * Handle the GetCoveragesExtents request
     *
     * @return
     * @throws WCSException
     */
    private Response handle(Map<String, String[]> kvpParameters) throws WCSException, PetascopeException, SecoreException {
        // Default load all coverages's extents
        String coverageId = null;
        if (kvpParameters.get("coverageId") != null) {
            // If the request sepecfies the coverageId parameter (DescribeCoverage, GetCoverage) then return this coverage's extent only.
            coverageId = kvpParameters.get("coverageId")[0];
        }
        
        String coveragesExtentsJSON = null;
        try {
            if (coverageId == null) {
                // Return all coverages's extents
                List<CoverageExtent> coveragesExtents = new ArrayList<>();
                        
                // This GetCoverageExtents request is sent from wcs_client after WCS GetCapabilities request, then all the coverages's metadata should be ready
                // then create the XY axes's extent in EPSG:4326 for WCS_Client to display in WebWorldWind
                coverageRepostioryService.createAllCoveragesExtents();
                
                for (Map.Entry<String, BoundingBox> entry : CoverageRepostioryService.coveragesExtentsCacheMap.entrySet()) {
                    coveragesExtents.add(new CoverageExtent(entry.getKey(), entry.getValue()));
                }

                // Sort the list of coverages's extents by their areas descending to allow Openlayers select the smaller coverages in top of bigger coverages when hovering on extents.
                Collections.sort(coveragesExtents);
                coveragesExtentsJSON = JSONUtil.serializeObjectToJSONString(coveragesExtents);
            } else {
                // Return just specified coverage's extent
                CoverageExtent coverageExtent = new CoverageExtent(coverageId, CoverageRepostioryService.coveragesExtentsCacheMap.get(coverageId));
                coveragesExtentsJSON = JSONUtil.serializeObjectToJSONString(coverageExtent);
            }
        } catch (JsonProcessingException ex) {
            throw new WCSException(ExceptionCode.InternalComponentError, "Cannot serialize coverages's extents to JSON String", ex);
        }

        return new Response(Arrays.asList(coveragesExtentsJSON.getBytes()), MIMEUtil.MIME_JSON, null);
    }
}
