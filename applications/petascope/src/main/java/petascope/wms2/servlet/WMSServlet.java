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

package petascope.wms2.servlet;

import petascope.util.request.CORSHttpServlet;
import petascope.wms2.orchestration.ServiceOrchestrator;
import petascope.wms2.service.base.Response;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for WMS. It wraps requests from the web client in a standard container, passes them to the service orchestrator
 * and writes responses back to the web client.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class WMSServlet extends CORSHttpServlet {

    /**
     * Initializes the servlet and configures the service
     *
     * @param config the servlet configuration
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        orchestrator = new ServiceOrchestrator(config.getServletContext().getInitParameter(CONFIGURATION_DIRECTORY_PARAMETER));
    }

    /**
     * Handles an HTTP GET request, passes it to the orchestrator and writes the response from it to the client
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            WMSGetRequest request = new WMSGetRequest(req);
            Response response = orchestrator.handleWMSRequest(request);
            resp.setContentType(response.getMimeType());
            resp.getOutputStream().write(response.toBytes());
        } catch (Exception e) {
            resp.getOutputStream().write(orchestrator.handleExceptions(e));
        } finally {
            resp.getOutputStream().flush();
            resp.getOutputStream().close();
        }
    }

    /**
     * This method is called when the servlet closes. We further call the close method on the orchestrator so that
     * we can release all the resources
     */
    @Override
    public void destroy() {
        super.destroy();
        orchestrator.close();
    }

    private ServiceOrchestrator orchestrator;
    private static final String CONFIGURATION_DIRECTORY_PARAMETER = petascope.ConfigManager.CONF_DIR;
}
