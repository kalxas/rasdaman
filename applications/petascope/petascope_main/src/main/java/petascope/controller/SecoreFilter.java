/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.controller;

import java.io.File;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.rasdaman.secore.ConfigManager;
import org.rasdaman.secore.Constants;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Configuration
@WebFilter(urlPatterns = "/def/*")
public class SecoreFilter implements Filter {
    
    // Attribute is set from servlet to jsp pages
    public static final String CLIENT_REQUEST_URI_ATTRIBUTE = "requestURI";
    public static final String CLIENT_SUCCESS_ATTRIBUTE = "success";
    // Attribute is used in servlet for dispatching only
    public static final String SERVER_REQUEST_URI_ATTRIBUTE = "serverRequestURI";
    
    // Request parameters for login, form pages 
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    
    private static final String JSP_PATTERN = ".jsp";
    private static final String JSP_FOLDER_IN_WAR_FILE = "/WEB-INF/jsp/";

    /**
     * Dispatch the request to another JSP page.
     */
    private void dispatchJSPRequest(String jspFile, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        RequestDispatcher dispatcher = request.getServletContext()
                .getRequestDispatcher(JSP_FOLDER_IN_WAR_FILE + jspFile);        
        dispatcher.forward(request, response);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        // For the external serverlet container, it will just forward request containing .jsp to jsp files
        // as the war is extracted to folder, so no problem with finding jsp files.
        if (request.getAttribute(SERVER_REQUEST_URI_ATTRIBUTE) != null) {
            chain.doFilter(request, response);
        } else if (!request.getRequestURI().endsWith(JSP_PATTERN)) {
            // Not a new/forwarded request to jsp file, don't do anything
            chain.doFilter(request, response);
        } else {
            String jspFile = Constants.LOGIN_JSP;
            // used by secore filter to continue redirect
            request.setAttribute(SERVER_REQUEST_URI_ATTRIBUTE, request.getRequestURI());
            // used by login.jsp to show the action URL in form element
            request.setAttribute(CLIENT_REQUEST_URI_ATTRIBUTE, request.getRequestURI());            
                        
            // First, check if it should show login page (depending on secore admin username/password does exist in secore.properties)
            if (ConfigManager.getInstance().showLoginPage()) {
                if (request.getRequestURI().contains(Constants.LOGOUT_JSP)) {
                    // User wants to log out, clear session and redirect to login page of index.jsp.
                    request.getSession().setAttribute(USERNAME, null);
                    String indexPageRequestURI = ConfigManager.getInstance().getServerContextPath() + "/" + Constants.INDEX_JSP;
                    request.setAttribute(SERVER_REQUEST_URI_ATTRIBUTE, indexPageRequestURI);
                    request.setAttribute(CLIENT_REQUEST_URI_ATTRIBUTE, indexPageRequestURI);
                } else if (request.getSession().getAttribute(USERNAME) == null) {
                    // Check if user already logged in
                    String username = request.getParameter(USERNAME);
                    String password = request.getParameter(PASSWORD);                    
                    if (username != null) {
                        // Login page is shown and user trying to login
                        String adminUsername = ConfigManager.getInstance().getAdminUsername();
                        String adminPassword = ConfigManager.getInstance().getAdminPassword();
                        if (username.equals(adminUsername) && password.equals(adminPassword)) {
                            // Valid login, create session then no need to relogin later
                            request.getSession().setAttribute(USERNAME, username);
                            // then redirect to the requested admin page
                            jspFile = new File(request.getRequestURI()).getName();
                        } else {
                            // invalid login return unsuccessful message to web browser
                            request.setAttribute(CLIENT_SUCCESS_ATTRIBUTE, false);
                        }
                    }
                } else {
                    // session exists, then redirect to the requested admin page (e.g: http://localhost:8080/def/crs/EPSG/0/4327/browse.jsp)
                    jspFile = new File(request.getRequestURI()).getName();
                }
            } else {
                // No admin user, password is set in secore.properties, just go to the jsp page without login form
                jspFile = new File(request.getRequestURI()).getName();
            }
            this.dispatchJSPRequest(jspFile, request, response);
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

}
