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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Class to handle the requests from the PetascopeInterface to the WMS services. It takes a normal
 * request coming to the PetascopeInterface servlet and decides if it should be handled by the WMS service and
 * based on version it will forward it to the 1.3.0 version of the service or to the 1.1.0 version of the service
 * It also ensures proper initialization of both servlets
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class PetascopeInterfaceAdapter {

    /**
     * Constructor for the class
     */
    public PetascopeInterfaceAdapter() {
        servlet13 = new WMSServlet();
        servlet11 = new petascope.wms.WmsServlet();
    }

    /**
     * Initializes the WMS13 Servlet with the configuration of the main servlet
     *
     * @param config the servlet config received during the main servlet initialization
     * @throws javax.servlet.ServletException
     */
    public void initWMS13Servlet(ServletConfig config) throws ServletException {
        servlet13.init(config);
    }


    /**
     * Initializes the WMS11 Servlet with the configuration of the main servlet
     *
     * @param config the servlet config received during the main servlet initialization
     */
    public void initWMS11Servlet(ServletConfig config) throws ServletException {
        servlet11.init(config);
    }

    /**
     * Handles the get requests coming from the main servlet. If the request is a proper wms one, we handle it
     * and return a status to the main servlet indicating if we answered or not to the request
     *
     * @param httpRequest  the http request to be handled
     * @param httpResponse the http response to be used to return the result
     * @return true if we handled the request, false otherwise
     * @throws ServletException
     * @throws IOException
     */
    public boolean handleGetRequests(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
        WMSGetRequest getRequest = new WMSGetRequest(httpRequest);
        String serviceRequested = getRequest.getGetValueByKey(SERVICE_PARAM_NAME);
        String versionRequested = getRequest.getGetValueByKey(VERSION_PARAM_NAME);
        if (serviceRequested != null && serviceRequested.equalsIgnoreCase(SERVICE_PARAM_VALUE)) {
            //The WMS 1.3.0 states that we should respond even if the version is not presented
            //No version => latest version of the standard
            if (versionRequested == null || !versionRequested.equalsIgnoreCase(VERSION11_PARAM_VALUE)) {
                servlet13.doGet(httpRequest, httpResponse);
            } else if (versionRequested.equalsIgnoreCase(VERSION11_PARAM_VALUE)) {
                servlet11.doGet(httpRequest, httpResponse);
            }
            return true;
        }
        return false;
    }


    private final WMSServlet servlet13;
    private final petascope.wms.WmsServlet servlet11;
    private static final String SERVICE_PARAM_NAME = "service";
    private static final String VERSION_PARAM_NAME = "version";
    private static final String SERVICE_PARAM_VALUE = "wms";
    private static final String VERSION13_PARAM_VALUE = "1.3.0";
    private static final String VERSION11_PARAM_VALUE = "1.1.0";
}
