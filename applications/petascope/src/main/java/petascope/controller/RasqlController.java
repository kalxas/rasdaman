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

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.config.ConfigManager.RASQL;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import petascope.controller.handler.service.KVPRasqlServiceHandler;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.exceptions.WMSException;
import petascope.util.StringUtil;

/**
 * Controller for Rasql query as RasqlServlet before
 *
 * <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Controller
public class RasqlController extends AbstractController {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(RasqlController.class);

    @Autowired
    KVPRasqlServiceHandler kvpRasqlServiceHandler;

    @RequestMapping(value = RASQL, method = RequestMethod.POST)
    protected void handlePost(HttpServletRequest httpServletRequest, @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        String requestBody = this.getPOSTRequestBody(httpServletRequest);
        Map<String, String[]> kvpParameters = this.buildRequestKvpParametersMap(requestBody);

        if (file != null) {
            // It is a upload file request
            byte[] bytes = file.getBytes();            
            // Check if temp folder exist first
            File folderPath = new File(ConfigManager.RASQL_SERVLET_UPLOAD_DIR);
            if (!folderPath.exists()) {
                folderPath.mkdir();
            }
            String fileName = StringUtil.createRandomString(file.getOriginalFilename());
            String filePath = ConfigManager.RASQL_SERVLET_UPLOAD_DIR + "/" + fileName;
            Path path = Paths.get(filePath);
            Files.write(path, bytes);
            log.debug("Uploaded request file to local temp folder: " + filePath);
            kvpParameters.put(KVPSymbols.KEY_UPLOADED_FILE_PATH, new String[]{filePath});
        }

        this.requestDispatcher(kvpParameters);
    }

    @RequestMapping(value = RASQL, method = RequestMethod.GET)
    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        Map<String, String[]> kvpParameters = this.buildRequestKvpParametersMap(httpServletRequest.getQueryString());
        this.requestDispatcher(kvpParameters);
    }

    @Override
    protected void requestDispatcher(Map<String, String[]> kvpParameters) throws IOException, PetascopeException, WCSException, SecoreException, WMSException {
        log.debug("Received request: " + this.getRequestRepresentation(kvpParameters));
        Response response = kvpRasqlServiceHandler.handle(kvpParameters);
        this.writeResponseResult(response);
    }
}
