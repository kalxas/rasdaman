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
package petascope.admin.update.page;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.ConfigManager;
import petascope.core.DbMetadataSource;
import petascope.core.ServiceMetadata;
import petascope.util.Pair;

/**
 *
 * @author Bang Pham Huu
 */
public class UpdateServiceProviderMetadata extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(UpdateServiceProviderMetadata.class);

    // Store the information from statc table for service identication and provider
    private DbMetadataSource dbMetadataSource = null;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Create a session object if it is already not created.
        HttpSession session = request.getSession();

        //String loginPage = "/admin_update_page/service_provider/login.jsp";
        // NOTE: this JSP files in wcs-client and only can be included when make from petascope
        // So to test JSP files, create it first in petascope project when everything is good, move it into wcs-client project
        String loginPage = "/static/wcs-client/admin/update_service_provider/login.jsp";
        String adminPage = "/static/wcs-client/admin/update_service_provider/form.jsp";
        //String loginPage = "/admin_update_page/service_provider/login.jsp";
        //String adminPage = "/admin_update_page/service_provider/form.jsp";

        String logout = request.getParameter("logout");

        RequestDispatcher view = request.getRequestDispatcher(loginPage);

        // Check logged user with some actions, first check session does exist
        if (session.getAttribute("userName") != null) {

            // 0. Check if user want to logout
            if (logout != null && logout.equals("true")) {
                // Clear session and redirect to login.jsp
                session.removeAttribute("userName");
                view.forward(request, response);
                return;
            }

            // 1. Check if user want to update identifcation
            String updateIdentification = request.getParameter("updateIdentification");
            if (updateIdentification != null) {
                // Handle update indentication
                handleUpdateIdentification(request, response, view, adminPage);
                session.setAttribute("reloadMetadata", true);
            }

            // 2. Check if user want to update provider
            String updateProvider = request.getParameter("updateProvider");
            if (updateProvider != null) {

                // Handle update provider
                handleUpdateProvider(request, response, view, adminPage);
                session.setAttribute("reloadMetadata", true);
            }

            // 3. Session does exist, no need to check anything, display form page normally (reload page)
            loadFormData(request, response, view, adminPage);
            return;

        }

        // Else, check normal login and reload page with session == null
        Boolean isSubmit = true;

        String userName = request.getParameter("userName");
        String passWord = request.getParameter("passWord");

        if (userName == null || userName.equals("")) {
            isSubmit = false;
        }

        // 1. First load page -> login.jsp
        if (session.getAttribute("userName") == null && isSubmit == false) {
            view.forward(request, response);

        } // User submit username, password first time
        else {
            // If user submit login then check valid user

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            // 2. Check if userName and passWord is valid in petascope.properties
            if (userName.equals(ConfigManager.PETASCOPE_ADMIN_USER) && passWord.equals(ConfigManager.PETASCOPE_ADMIN_PASS)) {

                // create session userName for user
                session.setAttribute("userName", userName);

                // redirect to form.jsp with all form data
                loadFormData(request, response, view, adminPage);

            } else {
                // 3. Username and Password is not valid then display error
                // return isSuccess = false to login.jsp
                request.setAttribute("isSuccess", false);
                view.forward(request, response);
            }
        } // end check submit
    }

    /**
     * This function will update Identification from submit data, then reload
     * page
     *
     * @param request
     * @param response
     * @param view
     * @param page
     */
    private void handleUpdateIdentification(HttpServletRequest request, HttpServletResponse response, RequestDispatcher view, String page) throws ServletException, IOException {
        // 1. Get all submit value to HashMap
        Map<String, String> map = new HashMap<String, String>();

        map.put("serviceTitle", request.getParameter("serviceTitle"));
        map.put("abstract", request.getParameter("abstract"));
        //map.put("serviceType", request.getParameter("serviceType")); (does not allow to change OGC WCS as it will have bad effect to other services)
        map.put("serviceTypeVersion", request.getParameter("serviceTypeVersion"));

        // 2. Update Identification with the map value
        try {
            dbMetadataSource.updateIdentication(map);

            // 3. Reload page with success message
            request.setAttribute("return", "Update *Identification* information successfully!");
        } catch (Exception e) {
            log.error("Cannot update *Identification* information.");
            log.error(e.getMessage());
            request.setAttribute("return", "Cannot update *Identification* information: " + e.getMessage());
        }
    }

    /**
     * This function will update Provider from submit data, then reload page
     *
     * @param request
     * @param response
     * @param view
     * @param page
     */
    private void handleUpdateProvider(HttpServletRequest request, HttpServletResponse response, RequestDispatcher view, String page) throws ServletException, IOException {
        // 1. Get all submit value to HashMap
        Map<String, String> map = new HashMap<String, String>();

        map.put("providerName", request.getParameter("providerName"));
        map.put("providerWebsite", request.getParameter("providerWebsite"));
        map.put("contactPerson", request.getParameter("contactPerson"));
        map.put("positionName", request.getParameter("positionName"));
        map.put("administrativeArea", request.getParameter("administrativeArea"));
        map.put("cityAddress", request.getParameter("cityAddress"));
        map.put("postalCode", request.getParameter("postalCode"));
        map.put("country", request.getParameter("country"));
        map.put("email", request.getParameter("email"));
        map.put("hoursOfService", request.getParameter("hoursOfService"));
        map.put("contactInstructions", request.getParameter("contactInstructions"));
        map.put("roleID", request.getParameter("roleID"));

        // 2. Update Identification with the map value
        try {
            dbMetadataSource.updateProvider(map);

            // 3. Reload page with success message
            request.setAttribute("return", "Update *Provider* information successfully!");
        } catch (Exception e) {
            log.error("Cannot update *Provider* information");
            log.error(e.getMessage());
            request.setAttribute("return", "Cannot update *Provider* information: " + e.getMessage());
        }
    }

    /**
     * This function will load all static data from ServiceMetadata, RoleList
     * and forward to form.jsp
     *
     * @param HttpRequest
     * @param HttpResponse
     * @param RequestDispatcher
     * @param WebPage to redirect (form.jsp)
     */
    private void loadFormData(HttpServletRequest request, HttpServletResponse response, RequestDispatcher view, String page) throws ServletException, IOException {

        try {
            // init Db Metadatasource
            // NOTE: When submit data, also need to reload MetadataSource
            dbMetadataSource = new DbMetadataSource(ConfigManager.METADATA_DRIVER,
                    ConfigManager.METADATA_URL,
                    ConfigManager.METADATA_USER,
                    ConfigManager.METADATA_PASS, false);

            // 1. Get Service Metadata (Provider, Identification)
            ServiceMetadata sMeta = dbMetadataSource.getServiceMetadata();
            // 2. Get list of role code
            List<Pair> roleList = dbMetadataSource.getListRoleCodeFromDB();

            // 3. Pass all these ojbects to from.jsp
            request.setAttribute("sMeta", sMeta);
            request.setAttribute("roleList", roleList);
            view = request.getRequestDispatcher(page);
            view.forward(request, response);

        } catch (Exception e) {
            log.error("Error initializing metadata from static tables.");
            log.error("Stack trace: {}", e);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    @SuppressWarnings("empty-statement")
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
