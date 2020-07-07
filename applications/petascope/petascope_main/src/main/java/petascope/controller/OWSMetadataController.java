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

import org.rasdaman.AuthenticationService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.config.ConfigManager.ADMIN;
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
import org.springframework.web.bind.annotation.RestController;
import petascope.core.KVPSymbols;
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

/**
 * Controller to handle request to Admin page to update OWS Service metadata
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@RestController
public class OWSMetadataController extends AbstractController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OWSMetadataController.class);

    @Autowired
    OWSMetadataRepostioryService owsMetadataRepostioryService;

    private static final String UPDATE_SERVICE_IDENTIFICATION = "UpdateServiceIdentification";
    private static final String UPDATE_SERVICE_PROVIDER = "UpdateServiceProvider";

    @RequestMapping(ADMIN + "/" + UPDATE_SERVICE_IDENTIFICATION)
    public void handleOWSUpdateServiceIdentification(HttpServletRequest httpServletRequest) throws Exception {

        // Only Petascope admin user can update coverage's metadata
        AuthenticationService.validatePetascopeAdminUser(httpServletRequest);
        
        String postBody = this.getPOSTRequestBody(httpServletRequest);     
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        
        OwsServiceMetadata owsServiceMetadata = owsMetadataRepostioryService.read();
        
        String serviceTitleValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_SERVICE_TITLE);
        if (serviceTitleValue != null) {
            owsServiceMetadata.getServiceIdentification().setServiceTitle(serviceTitleValue);
        }
        
        String abstractValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_ABSTRACT);
        if (abstractValue != null) {
            owsServiceMetadata.getServiceIdentification().setServiceAbstract(abstractValue);  
        }
        
        // Update the new submitted values to ows service metadata object
        owsMetadataRepostioryService.save(owsServiceMetadata);
        
        log.info("OWS Service metadata is updated in database from input Service Identification.");
    }
    
    @RequestMapping(ADMIN + "/" + UPDATE_SERVICE_PROVIDER)
    public void handleOWSUpdateServiceProvider(HttpServletRequest httpServletRequest) throws Exception {

        // Only Petascope admin user can update coverage's metadata
        AuthenticationService.validatePetascopeAdminUser(httpServletRequest);
        
        String postBody = this.getPOSTRequestBody(httpServletRequest);     
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        
        OwsServiceMetadata owsServiceMetadata = owsMetadataRepostioryService.read();

        String providerNameValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_PROVIDER_NAME);
        if (providerNameValue != null) {
            owsServiceMetadata.getServiceProvider().setProviderName(providerNameValue);
        }
        String providerSiteValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_PROVIDER_SITE);
        if (providerSiteValue != null) {
            owsServiceMetadata.getServiceProvider().setProviderSite(providerSiteValue);
        }

        String individualNameValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_INDIVIDUAL_NAME);
        if (individualNameValue != null) {
            owsServiceMetadata.getServiceProvider().getServiceContact().setIndividualName(individualNameValue);
        }
        String positionNameValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_POSITION_NAME);
        if (positionNameValue != null) {
            owsServiceMetadata.getServiceProvider().getServiceContact().setPositionName(positionNameValue);
        }
        String roleValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_ROLE);
        owsServiceMetadata.getServiceProvider().getServiceContact().setRole(roleValue);

        String emailValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_EMAIL);
        if (emailValue != null) {
            List<String> emails = ListUtil.valuesToList(emailValue);
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setElectronicMailAddresses(emails);
        }

        String voicePhoneValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_VOICE_PHONE);
        if (voicePhoneValue != null) {
            List<String> voicePhones = ListUtil.valuesToList(voicePhoneValue);
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getPhone().setVoicePhones(voicePhones);
        }
        String facsimilePhoneValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_FACSIMILE_PHONE);
        if (facsimilePhoneValue != null) {
            List<String> facsimilePhones = ListUtil.valuesToList(kvpParameters.get("facsimilePhone"));
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getPhone().setFacsimilePhone(facsimilePhones);
        }

        String hourseOfServiceValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_HOURS_OF_SERVICE);
        if (hourseOfServiceValue != null) {
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().setHoursOfService(hourseOfServiceValue);
        }
        
        String contactInstructionsValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_CONTACT_INSTRUCTIONS);
        if (contactInstructionsValue != null) {
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().setContactInstructions(contactInstructionsValue);
        }
        
        String cityValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_CITY);
        if (cityValue != null) {
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setCity(cityValue);
        }
        
        String administrativeAreaValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_ADMINISTRATIVE_AREA);
        if (administrativeAreaValue != null) {
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setAdministrativeArea(administrativeAreaValue);
        }
        
        String postalCodeValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_POSTAL_CODE);
        if (postalCodeValue != null) {
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setPostalCode(postalCodeValue);
        }
        
        String countryValue = getValueByKeyAllowNull(kvpParameters, KEY_OWS_METADATA_COUNTRY);
        if (countryValue != null) {
            owsServiceMetadata.getServiceProvider().getServiceContact().getContactInfo().getAddress().setCountry(countryValue);   
        }
        
        // Update the new submitted values to ows service metadata object
        owsMetadataRepostioryService.save(owsServiceMetadata);            
        log.info("OWS Service metadata is updated in database from input Service Provider.");
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
