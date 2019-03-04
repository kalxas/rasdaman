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
  *  Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.rasdaman.repository.service.OWSMetadataRepostioryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.exceptions.WMSException;
import petascope.util.ListUtil;
import static org.rasdaman.config.ConfigManager.OWS_ADMIN;
import org.springframework.web.bind.annotation.RestController;
import petascope.exceptions.ExceptionCode;

/**
 * Controller to handle request to Admin page to update OWS Service metadata
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@RestController
public class OWSAdminController extends AbstractController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OWSAdminController.class);

    @Autowired
    OWSMetadataRepostioryService owsMetadataRepostioryService;

    /**
     * Only process non-login request if user logged in.
     */
    private void validateSession(HttpServletRequest httpServletRequest) throws PetascopeException {
        // NOTE: for Ajax request from client (AngularJS), without "withCredentials: true" for IRequestConfig, Java Servlet will create a new session
        // and this check will always fail.
        if (httpServletRequest.getSession().getAttribute(USERNAME) == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Session does not exist, please login first.");
        }
    }
     
    @RequestMapping(OWS_ADMIN + "/Login")
    public void handleOWSLogin(HttpServletRequest httpServletRequest, HttpSession session) throws Exception {
        String postBody = this.getPOSTRequestBody(httpServletRequest);     
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        
        String username = kvpParameters.get(USERNAME)[0];
        String password = kvpParameters.get(PASSWORD)[0];
        if (!(username.equals(ConfigManager.PETASCOPE_ADMIN_USERNAME) && password.equals(ConfigManager.PETASCOPE_ADMIN_PASSWORD))) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Petascope admin username or password is not valid.");
        } else {
           session.setAttribute(USERNAME, username);
        }
        httpServletRequest.getSession().setAttribute(USERNAME, true);
    }
    
    @RequestMapping(OWS_ADMIN + "/UpdateServiceIdentification")
    public void handleOWSUpdateServiceIdentification(HttpServletRequest httpServletRequest) throws Exception {
        this.validateSession(httpServletRequest);
        
        String postBody = this.getPOSTRequestBody(httpServletRequest);     
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        OwsServiceMetadata owsServiceMetadata = owsMetadataRepostioryService.read();
        log.debug("Updating Service Identification");
        owsServiceMetadata.getServiceIdentification().setServiceTitle(kvpParameters.get("serviceTitle")[0]);
        owsServiceMetadata.getServiceIdentification().setServiceAbstract(kvpParameters.get("abstract")[0]);    
        
        // Update the new submitted values to ows service metadata object
        owsMetadataRepostioryService.save(owsServiceMetadata);            
        log.debug("OWS Service metadata is updated in database from input Service Identification.");
    }
    
    @RequestMapping(OWS_ADMIN + "/UpdateServiceProvider")
    public void handleOWSUpdateServiceProvider(HttpServletRequest httpServletRequest) throws Exception {
        this.validateSession(httpServletRequest);
        
        String postBody = this.getPOSTRequestBody(httpServletRequest);     
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        OwsServiceMetadata owsServiceMetadata = owsMetadataRepostioryService.read();
        log.debug("Updating Service Provider");
                
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
        owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setCity(kvpParameters.get("city")[0]);
        owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setAdministrativeArea(kvpParameters.get("administrativeArea")[0]);
        owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setPostalCode(kvpParameters.get("postalCode")[0]);
        owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setCountry(kvpParameters.get("country")[0]);   
        
        // Update the new submitted values to ows service metadata object
        owsMetadataRepostioryService.save(owsServiceMetadata);            
        log.debug("OWS Service metadata is updated in database from input Service Provider.");
    }

    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        // Do nothing
    }

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws IOException, PetascopeException, SecoreException, WMSException {
        // Do nothing
    }
}
