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

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.config.ConfigManager.ADMIN;
import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.rasdaman.repository.service.OwsMetadataRepostioryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.exceptions.WMSException;
import petascope.util.ListUtil;

/**
 * Controller to handle request to Admin page to update OWS Service metadata
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Controller
public class AdminController extends AbstractController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AdminController.class);

    // Request parameters for login, form pages
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final String UPDATE_IDENTIFICATION = "updateIdentification";
    private static final String UPDATE_PROVIDER = "updateProvider";

    private static final String LOGIN_PAGE = "login";
    private static final String FORM_PAGE = "form";

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    OwsMetadataRepostioryService owsMetadataRepostioryService;

    /**
     * Handle the login page for logging in and update OWS Service metadata
     *
     * @param postBody
     * @param modelToView
     * @return
     * @throws java.lang.Exception
     */
    @RequestMapping(ADMIN)
    public String loginPage(@RequestBody(required = false) String postBody, Map<String, Object> modelToView) throws Exception {
        Map<String, String[]> kvpParameters;

        if (httpServletRequest.getSession().getAttribute(USERNAME) != null) {
            // Session is created, just reload the form page
            return this.handleFormRequest(null, modelToView);

        } else if (postBody != null) {
            kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
            return this.handleLoginRequest(kvpParameters, modelToView);
        }
        // This value will be passed to JSP
        modelToView.put("message", String.valueOf(modelToView.size()));

        return LOGIN_PAGE;
    }

    /**
     * Handle the form page for updating OWS Service metadata
     *
     * @param postBody
     * @param modelToView
     * @return
     * @throws java.lang.Exception
     */
    @RequestMapping(ADMIN + "/form")
    public String formPage(@RequestBody(required = false) String postBody, Map<String, Object> modelToView) throws Exception {
        Map<String, String[]> kvpParameters;

        if (postBody != null) {
            // If request without submitting data, then just return the login page
            postBody = URLDecoder.decode(postBody, "utf-8");
            kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
            // Session is created, just reload the form page with the new updated data
            return this.handleFormRequest(kvpParameters, modelToView);
        } else if (httpServletRequest.getSession().getAttribute(USERNAME) != null) {
            // User want to logout, so clear session of this user and redirect to login page
            if (httpServletRequest.getParameter("logout") != null) {
                httpServletRequest.getSession().setAttribute(USERNAME, null);

                return LOGIN_PAGE;
            } else {
                // Session is created, just reload the form page
                return this.handleFormRequest(null, modelToView);
            }
        } else {
            // Session does not exist, just return login page
            return LOGIN_PAGE;
        }
    }

    /**
     * Handle the request from/to login page
     *
     * @param kvpParameters
     * @param modelToView
     * @return
     */
    private String handleLoginRequest(Map<String, String[]> kvpParameters, Map<String, Object> modelToView) throws WCSException {
        // If submitted username, password are not as same as admin's account in petascope.properties, so return error status.
        if (!(kvpParameters.get(USERNAME)[0].equals(ConfigManager.PETASCOPE_ADMIN_USERNAME)
                && kvpParameters.get(PASSWORD)[0].equals(ConfigManager.PETASCOPE_ADMIN_PASSWORD))) {
            modelToView.put("isSuccess", "false");

            return LOGIN_PAGE;
        } else {
            modelToView.put("isSuccess", "true");
            // Create a session for this loggin then foward to form.jsp page
            httpServletRequest.getSession().setAttribute(USERNAME, kvpParameters.get(USERNAME)[0]);

            return this.handleFormRequest(null, modelToView);
        }
    }

    /**
     * Handle the request from/to the form page
     *
     * @param modelToView
     * @return
     */
    private String handleFormRequest(Map<String, String[]> kvpParameters, Map<String, Object> modelToView) throws WCSException {
        OwsServiceMetadata owsServiceMetadata = owsMetadataRepostioryService.read();

        if (kvpParameters != null) {
            // Check what kind of update request
            if (httpServletRequest.getParameter(UPDATE_IDENTIFICATION) != null) {
                log.debug("Update Identification");
                owsServiceMetadata.getServiceIdentification().setServiceTitle(kvpParameters.get("serviceTitle")[0]);
                owsServiceMetadata.getServiceIdentification().setServiceAbstract(kvpParameters.get("abstract")[0]);
                
                modelToView.put("retMessage", "Service identification is updated in database.");
            } else if (httpServletRequest.getParameter(UPDATE_PROVIDER) != null) {
                // Update Service provider
                log.debug("Update Provider");
                
                owsServiceMetadata.getServiceProvider().setProviderName(kvpParameters.get("providerName")[0]);
                owsServiceMetadata.getServiceProvider().setProviderSite(kvpParameters.get("providerSite")[0]);
                
                owsServiceMetadata.getServiceProvider().getServiceContact().setIndividualName(kvpParameters.get("individualName")[0]);
                owsServiceMetadata.getServiceProvider().getServiceContact().setPositionName(kvpParameters.get("positionName")[0]);
                owsServiceMetadata.getServiceProvider().getServiceContact().setRole(kvpParameters.get("role")[0]);
                
                List<String> emails = ListUtil.valuesToList(kvpParameters.get("email"));
                owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setElectronicMailAddresses(emails);
                
                List<String> voicePhones = ListUtil.valuesToList(kvpParameters.get("voicePhone"));
                owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getPhone().setVoicePhones(voicePhones);
                List<String> facsimilePhones = ListUtil.valuesToList(kvpParameters.get("facsimilePhone"));
                owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getPhone().setFacsimilePhone(facsimilePhones);
                
                owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().setHoursOfService(kvpParameters.get("hoursOfService")[0]);
                owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().setContactInstructions(kvpParameters.get("contactInstructions")[0]);
                owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setCity(kvpParameters.get("cityAddress")[0]);
                owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setAdministrativeArea(kvpParameters.get("administrativeArea")[0]);
                owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setPostalCode(kvpParameters.get("postalCode")[0]);
                owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setCountry(kvpParameters.get("country")[0]);
                
                modelToView.put("retMessage", "Service provider is updated in database.");
            }
            
            // Update the new submitted values to ows service metadata object
            owsMetadataRepostioryService.save(owsServiceMetadata);            
            log.debug("OWS Service metadata is updated in database.");
        }
        
        modelToView.put("owsServiceMetadata", owsServiceMetadata);

        return FORM_PAGE;
    }

    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        // Do nothing
    }

    @Override
    protected void requestDispatcher(Map<String, String[]> kvpParameters) throws IOException, PetascopeException, SecoreException, WMSException {
        // Do nothing
    }
}
