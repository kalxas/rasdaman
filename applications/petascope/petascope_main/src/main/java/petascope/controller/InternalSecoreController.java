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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import petascope.util.CrsUtil;

/**
 * Controller to access to internal SECORE of petascope
 * default endpoint is http://localhost:8080/rasdaman/def
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Controller
public class InternalSecoreController {
    
    private static final Logger log = LoggerFactory.getLogger(InternalSecoreController.class);
    
    @RequestMapping("/def/**")
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {String uri = req.getRequestURL().toString();        
        CrsUtil.handleSecoreController(req, resp);
    }
    
}
