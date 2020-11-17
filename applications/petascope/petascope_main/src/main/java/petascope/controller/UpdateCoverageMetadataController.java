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
 * Copyright 2003 - 2019 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.controller;

import org.rasdaman.AuthenticationService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import static org.rasdaman.config.ConfigManager.ADMIN;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.StringUtil;
import petascope.util.XMLUtil;

/**
 *
 * Controller to handle request to update coverage's metadata.
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@RestController
public class UpdateCoverageMetadataController extends AbstractController {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(UpdateCoverageMetadataController.class);

    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    
    @RequestMapping(value = ADMIN + "/UpdateCoverageMetadata", method = RequestMethod.POST)
    protected void handlePost(HttpServletRequest httpServletRequest) throws Exception {
        Pair<String, String> pair = null;
        try {           
            // Only Petascope admin user can update coverage's metadata
            AuthenticationService.validatePetascopeAdminUser(httpServletRequest);
            
            pair = this.parsePostRequest(httpServletRequest);

            Coverage coverage = this.coverageRepositoryService.readCoverageByIdFromDatabase(pair.fst);
            String newMetadata = FileUtils.readFileToString(new File(pair.snd));
            newMetadata = XMLUtil.stripXMLDeclaration(newMetadata);
            
            coverage.setMetadata(newMetadata.trim());            
            this.coverageRepositoryService.save(coverage);
            
            log.info("Updated metadata for coverage '" + pair.fst + "' from posted metadata text file.");
        } finally {
            if (pair != null) {
                // Clear uploaded file after processing
                FileUtils.deleteQuietly(new File(pair.snd));
            }
        }
    }

    /**
     * Parse and validate post request to get coverageId and a stored file path
     * from uploaded file to server.
     */
    private Pair<String, String> parsePostRequest(HttpServletRequest httpServletRequest)
            throws IOException, ServletException, PetascopeException {
        String storedFilePath = null;
        String coverageId = null;

        if (httpServletRequest.getParts().isEmpty()) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Missing coverage's metadata text file from posted request.");
        } else {
            // First part is: coverageId, Second part is: a file from client
            if (httpServletRequest.getParts().size() != 2) {
                throw new PetascopeException(ExceptionCode.InvalidRequest,
                        "Request to update coverage's metadata must contain 2 parts: coverage id and medata text file. Given: " + httpServletRequest.getParts().size());
            }
            int i = 0;
            for (Part part : httpServletRequest.getParts()) {
                byte[] bytes = IOUtils.toByteArray(part.getInputStream());

                if (i == 0) {
                    coverageId = new String(bytes);
                } else {
                    // Write the uploaded file to a folder in server
                    String fileName = StringUtil.addDateTimeSuffix("uploaded_metadata_file.");

                    storedFilePath = this.storeUploadFileOnServer(fileName, bytes);
                    String mimeType = Files.probeContentType(Paths.get(storedFilePath));
                    String[] requiredMimeType = {"text", "xml", "json"};
                    
                    boolean isValid = false;
                    for (String type : requiredMimeType) {
                        if (mimeType.contains(type)) {
                            isValid = true;
                            break;
                        }
                    }
                    
                    if (!isValid) {
                        throw new PetascopeException(ExceptionCode.InvalidRequest, "Uploaded metadata file must be XML/JSON format. Given: '" + mimeType + "'.");
                    }
                }

                i++;
            }
        }

        Pair<String, String> resultPair = new Pair<>(coverageId, storedFilePath);
        return resultPair;
    }

    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws Exception {
    }

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
    }

}
