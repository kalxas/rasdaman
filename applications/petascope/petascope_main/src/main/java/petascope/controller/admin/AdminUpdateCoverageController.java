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

import com.rasdaman.accesscontrol.service.AuthenticationService;
import com.rasdaman.admin.coverage.service.AdminUpdateCoverageService;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import static org.rasdaman.config.ConfigManager.ADMIN;
import static org.rasdaman.config.ConfigManager.COVERAGE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import petascope.controller.AbstractController;
import petascope.controller.RequestHandlerInterface;
import static petascope.core.KVPSymbols.KEY_METADATA;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * Class to handle update coverage (id and metadata) object.
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@RestController
public class AdminUpdateCoverageController extends AbstractController {

    private static final String COVERAGE_UPDATE_PATH = ADMIN + "/" + COVERAGE + "/update";

    @Autowired
    private AdminUpdateCoverageService adminUpdateCoverageService;

    @Override
    @RequestMapping(path = COVERAGE_UPDATE_PATH, method = RequestMethod.GET)
    protected void handleGet(HttpServletRequest httpServletRequest) throws Exception {
        this.handle(httpServletRequest);
    }

    @Override
    @RequestMapping(path = COVERAGE_UPDATE_PATH, method = RequestMethod.POST)
    protected void handlePost(HttpServletRequest httpServletRequest) throws Exception {
        this.handle(httpServletRequest);
    }
    
    private void handle(HttpServletRequest httpServletRequest) throws Exception {
        final Map<String, String[]> kvpParameters = this.parseKvpParametersFromRequest(httpServletRequest);
        
        RequestHandlerInterface requestHandlerInterface = () -> {
            try {
                this.validateWriteRequestFromIP(httpServletRequest);

                this.adminUpdateCoverageService.handle(httpServletRequest, kvpParameters);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        };
        
        super.handleRequest(kvpParameters, requestHandlerInterface);
    }

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
        
    }

    /**
     * Parse and validate post request to get coverageId and a stored file path
     * from uploaded file to server.
     */
    private Map<String, String[]> parsePostRequest(HttpServletRequest httpServletRequest)
            throws IOException, ServletException, PetascopeException, Exception {
        
        Map<String, String[]> kvpParameters = new LinkedHashMap<>();
        
        if (httpServletRequest instanceof StandardMultipartHttpServletRequest) {
            // Multipart request POST (metadata exists in a local file)
            
            // e.g. curl --user petauser:petapasswd -F "file=@/home/rasdaman/metadata.xml" "http://localhost:8080/rasdaman/admin/UpdateCoverageMetadata" -F "coverageId=test"

            StandardMultipartHttpServletRequest request = (StandardMultipartHttpServletRequest)httpServletRequest;
            
            for (Map.Entry<String, List<MultipartFile>> entry : request.getMultiFileMap().entrySet()) {
                MultipartFile firstMultipartFile = entry.getValue().get(0);
                String mimeType = firstMultipartFile.getContentType().toLowerCase();
                
                if (!(mimeType.contains("text")
                    || mimeType.contains("json")
                    || mimeType.contains("xml"))) {
                    throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                                "Uploaded metadata file must be text/XML/JSON format. Given: '" + mimeType + "'.");
                }
                
                String metadata = new String(firstMultipartFile.getBytes());
                kvpParameters.put(KEY_METADATA, new String[] { metadata });
                
                break;
            }
            
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                String key = entry.getKey().toLowerCase();
                String[] values = entry.getValue();
                kvpParameters.put(key, values);
            }
            
        } else {
            // normal POST request (metadata exists in a POST parameter)
            
            // curl --user petauser:petapasswd -d "metadata=<a>This is a metadata</a>" "http://localhost:8080/rasdaman/admin/UpdateCoverageMetadata" -d "coverageId=test"            
            String postBody = this.getPOSTRequestBody(httpServletRequest);
            kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        }
        
        return kvpParameters;
    }
}
