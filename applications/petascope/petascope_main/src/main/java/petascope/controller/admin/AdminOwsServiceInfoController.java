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
package petascope.controller.admin;

import com.rasdaman.admin.service.AbstractAdminService;
import com.rasdaman.accesscontrol.service.AuthenticationService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import static org.rasdaman.config.ConfigManager.ADMIN;
import static org.rasdaman.config.ConfigManager.OWS;
import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.rasdaman.repository.service.OWSMetadataRepostioryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.ListUtil;
import org.springframework.web.bind.annotation.RestController;
import petascope.controller.AbstractController;
import petascope.controller.RequestHandlerInterface;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_ABSTRACT;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_ADMINISTRATIVE_AREA;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_CITY;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_CONTACT_INSTRUCTIONS;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_COUNTRY;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_EMAIL;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_FACSIMILE_PHONE;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_HOURS_OF_SERVICE;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_INDIVIDUAL_NAME;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_POSITION_NAME;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_POSTAL_CODE;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_PROVIDER_NAME;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_PROVIDER_SITE;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_ROLE;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_SERVICE_TITLE;
import static petascope.core.KVPSymbols.KEY_OWS_METADATA_VOICE_PHONE;
import petascope.util.SetUtil;

/**
 * Controller to handle request to Admin page to update OWS service information (ServiceIdentification / ServiceProvider)
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@RestController
public class AdminOwsServiceInfoController extends AbstractController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AdminOwsServiceInfoController.class);
    
    private static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(
            KEY_OWS_METADATA_SERVICE_TITLE, KEY_OWS_METADATA_ABSTRACT,
            KEY_OWS_METADATA_PROVIDER_NAME, KEY_OWS_METADATA_PROVIDER_SITE, KEY_OWS_METADATA_INDIVIDUAL_NAME,
            KEY_OWS_METADATA_POSITION_NAME, KEY_OWS_METADATA_ROLE, KEY_OWS_METADATA_EMAIL,
            KEY_OWS_METADATA_VOICE_PHONE, KEY_OWS_METADATA_FACSIMILE_PHONE,
            KEY_OWS_METADATA_HOURS_OF_SERVICE, KEY_OWS_METADATA_CONTACT_INSTRUCTIONS,
            KEY_OWS_METADATA_CITY, KEY_OWS_METADATA_ADMINISTRATIVE_AREA,
            KEY_OWS_METADATA_POSTAL_CODE, KEY_OWS_METADATA_COUNTRY
            );

    @Autowired
    OWSMetadataRepostioryService owsMetadataRepostioryService;

    private static final String UPDATE_SERVICE_INFO_PATH = ADMIN + "/" + OWS + "/serviceinfo";
    
    @RequestMapping(path = UPDATE_SERVICE_INFO_PATH, method = RequestMethod.GET)
    public void handleUpdateServiceInfoGet(HttpServletRequest httpServletRequest) throws Exception {
        this.handleUpdateServiceInfo(httpServletRequest, false);
    }
    
    @RequestMapping(path = UPDATE_SERVICE_INFO_PATH, method = RequestMethod.POST)
    public void handleUpdateServiceInfoPost(HttpServletRequest httpServletRequest) throws Exception {
        this.handleUpdateServiceInfo(httpServletRequest, true);
    }
    
    private boolean notEmpty(String str) {
        return str != null && !str.equals("undefined");
    }
    
    private void handleUpdateServiceInfo(HttpServletRequest httpServletRequest, boolean isPost) throws Exception {
        Map<String, String[]> kvpParameters = this.parseKvpParametersFromRequest(httpServletRequest, isPost);
        
        RequestHandlerInterface requestHandlerInterface = () -> {
            try {
                AuthenticationService.validateWriteRequestByRoleOrAllowedIP(httpServletRequest);
                this.handle(kvpParameters);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        };
        
        super.handleRequest(kvpParameters, requestHandlerInterface);
    }
    
    private void handle(Map<String, String[]> kvpParameters) throws Exception {
        
        AbstractAdminService.validateRequiredParameters(kvpParameters, VALID_PARAMETERS);
        
        // 1. ServiceIdentification
        
        OwsServiceMetadata owsServiceMetadata = owsMetadataRepostioryService.read();
        
        String serviceTitleValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_SERVICE_TITLE);
        if (notEmpty(serviceTitleValue)) {
            owsServiceMetadata.getServiceIdentification().setServiceTitle(serviceTitleValue);
        }
        
        String abstractValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_ABSTRACT);
        if (notEmpty(abstractValue)) {
            owsServiceMetadata.getServiceIdentification().setServiceAbstract(abstractValue);  
        }
        
        // 2. ServiceProvider
        
        String providerNameValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_PROVIDER_NAME);
        if (notEmpty(providerNameValue)) {
            owsServiceMetadata.getServiceProvider().setProviderName(providerNameValue);
        }
        String providerSiteValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_PROVIDER_SITE);
        if (notEmpty(providerSiteValue)) {
            owsServiceMetadata.getServiceProvider().setProviderSite(providerSiteValue);
        }

        String individualNameValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_INDIVIDUAL_NAME);
        if (notEmpty(individualNameValue)) {
            owsServiceMetadata.getServiceProvider().getServiceContact().setIndividualName(individualNameValue);
        }
        String positionNameValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_POSITION_NAME);
        if (notEmpty(positionNameValue)) {
            owsServiceMetadata.getServiceProvider().getServiceContact().setPositionName(positionNameValue);
        }
        String roleValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_ROLE);
        owsServiceMetadata.getServiceProvider().getServiceContact().setRole(roleValue);

        String emailValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_EMAIL);
        if (notEmpty(emailValue)) {
            List<String> emails = ListUtil.valuesToList(emailValue);
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setElectronicMailAddresses(emails);
        }

        String voicePhoneValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_VOICE_PHONE);
        if (notEmpty(voicePhoneValue)) {
            List<String> voicePhones = ListUtil.valuesToList(voicePhoneValue);
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getPhone().setVoicePhones(voicePhones);
        }
        String facsimilePhoneValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_FACSIMILE_PHONE);
        if (notEmpty(facsimilePhoneValue)) {
            List<String> facsimilePhones = ListUtil.valuesToList(kvpParameters.get(KEY_OWS_METADATA_FACSIMILE_PHONE));
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getPhone().setFacsimilePhone(facsimilePhones);
        }

        String hourseOfServiceValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_HOURS_OF_SERVICE);
        if (notEmpty(hourseOfServiceValue)) {
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().setHoursOfService(hourseOfServiceValue);
        }
        
        String contactInstructionsValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_CONTACT_INSTRUCTIONS);
        if (notEmpty(contactInstructionsValue)) {
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().setContactInstructions(contactInstructionsValue);
        }
        
        String cityValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_CITY);
        if (notEmpty(cityValue)) {
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setCity(cityValue);
        }
        
        String administrativeAreaValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_ADMINISTRATIVE_AREA);
        if (notEmpty(administrativeAreaValue)) {
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setAdministrativeArea(administrativeAreaValue);
        }
        
        String postalCodeValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_POSTAL_CODE);
        if (notEmpty(postalCodeValue)) {
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setPostalCode(postalCodeValue);
        }
        
        String countryValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_COUNTRY);
        if (notEmpty(countryValue)) {
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setCountry(countryValue);   
        }
        
        // Update the new submitted values to ows service metadata object
        owsMetadataRepostioryService.save(owsServiceMetadata);            
        log.info("OWS service description is updated.");
    }
    
    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
        // No need to override super's method
    }
    
    
    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws WCSException, IOException, PetascopeException, SecoreException, Exception {
        // Do nothing
    }

}
