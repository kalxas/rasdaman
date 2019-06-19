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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import static org.rasdaman.config.ConfigManager.STATIC_HTML_DIR_PATH;

/**
 * If one visit Petascope endpoint (e.g: http://localhost:8080/rasdaman)
 * and there is static_html_dir_path setting configured in petascope.properties,
 * it will load the index.html insie the demo web pages folder.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@RestController
public class StaticHTMLController {

    @RequestMapping("/")
    public String handle() throws IOException, PetascopeException {
        if (STATIC_HTML_DIR_PATH.isEmpty()) {
            throw new PetascopeException(ExceptionCode.NoApplicableCode, 
                                         "No valid demo web pages folder is configured in petascope.properties.");
        }
        
        File file = new File(STATIC_HTML_DIR_PATH + "/index.html");
        if (!file.exists()) {
            throw new PetascopeException(ExceptionCode.NoApplicableCode, "Missing the required 'index.html' file"
                                                                         + " as entry web page in static HTML directory '" + STATIC_HTML_DIR_PATH + "'.");
        }
        
        return FileUtils.readFileToString(file);
    }
}
