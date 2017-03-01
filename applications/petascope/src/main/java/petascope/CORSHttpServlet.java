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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

package petascope;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet allowing CORS requests.
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class CORSHttpServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", CORS_ACCESS_CONTROL_ALLOW_ORIGIN);
    }

    /**
     * Implement the CORS requirements to allow browser clients to request
     * resources from different origin domains. i.e. http://example.org can make
     * requests to http://example.com
     *
     * @param req the http request
     * @param resp the http response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", CORS_ACCESS_CONTROL_ALLOW_ORIGIN);
        resp.setHeader("Access-Control-Allow-Methods", CORS_ACCESS_CONTROL_ALLOW_METHODS);
        resp.setHeader("Access-Control-Allow-Headers", CORS_ACCESS_CONTROL_ALLOW_HEADERS);
        resp.setHeader("Access-Control-Max-Age", CORS_ACCESS_CONTROL_MAX_AGE);
        resp.setHeader("Content-Length", "0");
        resp.setStatus(200);
    }

    private static final String CORS_ACCESS_CONTROL_ALLOW_ORIGIN = "*";
    private static final String CORS_ACCESS_CONTROL_ALLOW_METHODS = "POST, GET, OPTIONS";
    private static final String CORS_ACCESS_CONTROL_ALLOW_HEADERS = "Content-Type";
    private static final String CORS_ACCESS_CONTROL_MAX_AGE = "1728000";

}
