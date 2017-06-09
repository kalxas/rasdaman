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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.handlers.kvp;

import petascope.core.response.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.owsmetadata.Address;
import org.rasdaman.domain.owsmetadata.ContactInfo;
import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.rasdaman.domain.owsmetadata.Phone;
import org.rasdaman.domain.owsmetadata.ServiceContact;
import org.rasdaman.domain.owsmetadata.ServiceIdentification;
import org.rasdaman.domain.owsmetadata.ServiceProvider;
import org.rasdaman.repository.service.CoverageRepostioryService;
import org.rasdaman.repository.service.OwsMetadataRepostioryService;
import org.rasdaman.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.Templates;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.util.MIMEUtil;
import petascope.core.XMLSymbols;
import static petascope.core.XMLSymbols.NAMESPACE_OWS;
import static petascope.core.XMLSymbols.NAMESPACE_WCS;
import static petascope.core.XMLSymbols.OWS_LABEL_SERVICE_IDENTIFICATION;
import petascope.util.XMLUtil;
import static petascope.core.Templates.WCS2_GET_CAPABILITIES_OPERATIONS_METADATA_ELEMENT_URL;
import petascope.exceptions.WMSException;
import petascope.util.ListUtil;

/**
 * Handle the GetCapabilities WCS 2.0.1 request result example which is
 * validated with WCS 2.0.1 schema, see: https://pastebin.com/QUe4DKfg
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class KVPWCSGetCapabilitiesHandler extends KVPWCSAbstractHandler {

    private static Logger log = LoggerFactory.getLogger(KVPWCSGetCapabilitiesHandler.class);

    @Autowired
    private CoverageRepostioryService persistedCoverageService;
    @Autowired
    private OwsMetadataRepostioryService persistedOwsServiceMetadataService;

    // WCS ows:profiles (i.e: the supported extensions for:) 
    // EncodeFormatExtensions
    private static final String GML_IDENTIFIER = "http://www.opengis.net/spec/GMLCOV/1.0/conf/gml";
    private static final String GMLCOV_IDENTIFIER = "http://www.opengis.net/spec/GMLCOV/1.0/conf/gml-coverage";
    private static final String GEOTIFF_IDENTIFIER = "http://www.opengis.net/spec/WCS_coverage-encoding_geotiff/1.0/";
    private static final String GMLJP2_IDENTIFIER = "http://www.opengis.net/spec/GMLJP2/2.0/";
    private static final String JPEG2000_IDENTIFIER = "http://www.opengis.net/spec/WCS_coverage-encoding_jpeg2000/1.0/";
    private static final String CSV_IDENTIFIER = "https://www.ietf.org/rfc/rfc4180.txt";
    private static final String JSON_IDENTIFIER = "https://www.www.json.org/";
    private static final String JPEG_IDENTIFIER = "https://www.w3.org/Graphics/JPEG/";
    private static final String PNG_IDENTIFIER = "http://www.w3.org/TR/PNG/";
    private static final String NETCDF_IDENTIFIER = "http://www.opengis.net/spec/WCS_coverage-encoding_netcdf/1.0/";
    // Multipart result (i.e: gml + binary file in GetCoverage request)
    private static final String MULTIPART_IDENTIFIER = "http://www.opengis.net/spec/GMLCOV/1.0/conf/multipart";
    // InterpolationExtensions
    private static final String INTERPOLATION_IDENTIFIER = "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/interpolation";
    // RangeSubsettingExtension
    private static final String RANGE_SUBSETTING_IDENTIFIER = "http://www.opengis.net/spec/WCS_service-extension_range-subsetting/1.0/conf/record-subsetting";
    // ScalingExtension
    private static final String SCALING_IDENTIFIER = "http://www.opengis.net/spec/WCS_service-extension_scaling/1.0/conf/scaling";
    // ProcessCoverageExtension
    private static final String PROCESS_COVERAGE_IDENTIFIER = "http://www.opengis.net/spec/WCS_service-extension_processing/2.0/conf/processing";
    // Get KVP request (also with post if query string is long)
    private static final String KVP_IDENTIFIER = "http://www.opengis.net/spec/WCS_protocol-binding_get-kvp/1.0/conf/get-kvp";
    // Post XML request
    private static final String SOAP_IDENTIFIER = "http://www.opengis.net/spec/WCS_protocol-binding_soap/1.0";
    private static final String XML_IDENTIFIER = "http://www.opengis.net/spec/WCS_protocol-binding_post-xml/1.0";
    // WCST_Import
    private static final String WCST_IDENTIFIER = "http://www.opengis.net/spec/WCS_service-extension_transaction/2.0/conf/insert+delete";
    // CRS Projection
    private static final String CRS_IDENTIFIER = "http://www.opengis.net/spec/WCS_service-extension_crs/1.0/conf/crs";

    // Interpoltation Extension
    private static final String INTERPOLATION_NEAREST_NEIGHBOR = "http://www.opengis.net/def/interpolation/OGC/1.0/nearest-neighbor";

    // Singleton object to store all the extensions (profiles) of WCS
    private static List<String> profiles;

    private OwsServiceMetadata owsServiceMetadata;

    public static final List<String> supportedInterpolations = ListUtil.valuesToList(INTERPOLATION_NEAREST_NEIGHBOR);

    public KVPWCSGetCapabilitiesHandler() {

    }

    /**
     * Return the list of supported WCS extension (profiles)
     *
     * @return
     */
    private static List<String> getProfiles() {
        if (profiles == null) {
            profiles = new ArrayList<>();
            // Decode formats extension
            profiles.add(GML_IDENTIFIER);
            profiles.add(GMLCOV_IDENTIFIER);
            profiles.add(GEOTIFF_IDENTIFIER);
            profiles.add(GMLJP2_IDENTIFIER);
            profiles.add(JPEG2000_IDENTIFIER);
            profiles.add(CSV_IDENTIFIER);
            profiles.add(JSON_IDENTIFIER);
            profiles.add(JPEG_IDENTIFIER);
            profiles.add(PNG_IDENTIFIER);
            profiles.add(NETCDF_IDENTIFIER);
            // multipart
            profiles.add(MULTIPART_IDENTIFIER);
            // interpolation
            profiles.add(INTERPOLATION_IDENTIFIER);
            // range subsetting
            profiles.add(RANGE_SUBSETTING_IDENTIFIER);
            // scaling
            profiles.add(SCALING_IDENTIFIER);
            // processing coverage (WCPS)
            profiles.add(PROCESS_COVERAGE_IDENTIFIER);
            // request types (get, post)
            profiles.add(KVP_IDENTIFIER);
            profiles.add(SOAP_IDENTIFIER);
            profiles.add(XML_IDENTIFIER);
            // coverage transaction (WCST)
            profiles.add(WCST_IDENTIFIER);
            // crs projection
            profiles.add(CRS_IDENTIFIER);
        }

        return profiles;
    }

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {

    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        // Validate before handling the request
        this.validate(kvpParameters);

        // NOTE: GetCapabilities can contain a optional parameter: sections, just validate that the values of this parameter are standard
        // but don't need to do anything.
        String[] sections = kvpParameters.get(KVPSymbols.KEY_SECTIONS);
        if (sections != null) {
            String[] values = sections[0].split(",");
            for (String value : values) {
                if (!value.equals(KVPSymbols.VALUE_SECTIONS_ALL)
                        && !value.equals(KVPSymbols.VALUE_SECTIONS_CONTENTS)
                        && !value.equals(KVPSymbols.VALUE_SECTIONS_SERVICE_IDENTIFICATION)
                        && !value.equals(KVPSymbols.VALUE_SECTIONS_SERVICE_PROVIDER)
                        && !value.equals(KVPSymbols.VALUE_SECTIONS_OPERATIONS_METADATA)
                        && !value.equals(KVPSymbols.VALUE_SECTIONS_CONTENTS)
                        && !value.equals(KVPSymbols.VALUE_SECTIONS_LANGUAGES)) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Parameter's value received: " + sections[0] + " does not conform with protocol syntax.");

                }
            }
        }

        this.owsServiceMetadata = this.persistedOwsServiceMetadataService.read();
        // Build ows:ServiceIdentification element
        String serviceIdentication = this.buildServiceIdentification(NAMESPACE_OWS).toXML();
        // Build ows:ServiceProvider element
        String serviceProvider = this.buildServiceProvider(NAMESPACE_OWS).toXML();
        // Build ows:OperationsMetadata
        String operationsMetadata = this.buildOperationsMetadataElement().toXML();
        // Build wcs:ServiceMetadata
        String serviceMetadata = this.buildServiceMetadataElement(NAMESPACE_WCS).toXML();
        // Build wcs:Contents element
        String contents = this.buildContentsElement(NAMESPACE_WCS).toXML();

        // Load the template GetCapabilities result then replaces with the xml elements in string
        String getCapabilitiesTemplate = Templates.getTemplate(Templates.WCS2_GET_CAPABILITIES_FILE);
        getCapabilitiesTemplate = getCapabilitiesTemplate.replace(Templates.WCS2_GET_CAPABILITIES_SERVICE_IDENTIFICATION_ELEMENT, serviceIdentication);
        getCapabilitiesTemplate = getCapabilitiesTemplate.replace(Templates.WCS2_GET_CAPABILITIES_SERVICE_PROVIDER_ELEMENT, serviceProvider);
        getCapabilitiesTemplate = getCapabilitiesTemplate.replace(Templates.WCS2_GET_CAPABILITIES_OPERATIONS_METADATA_ELEMENT, operationsMetadata);
        getCapabilitiesTemplate = getCapabilitiesTemplate.replace(Templates.WCS2_GET_CAPABILITIES_SERVICE_METADATA_ELEMENT, serviceMetadata);
        getCapabilitiesTemplate = getCapabilitiesTemplate.replace(Templates.WCS2_GET_CAPABILITIES_CONTENTS_ELEMENT, contents);

        // format XML to have indentation
        getCapabilitiesTemplate = XMLUtil.formatXML(getCapabilitiesTemplate);

        // GetCapabilities only returns 1 XML string                
        return new Response(Arrays.asList(getCapabilitiesTemplate.getBytes()), MIMEUtil.MIME_GML, null);
    }

    /**
     * Build the ServiceIdentication element
     *
     * @return
     */
    private Element buildServiceIdentification(String nameSpace) {

        ServiceIdentification serviceIdentification = this.owsServiceMetadata.getServiceIdentification();
        Element serviceIdentificationElement = new Element(OWS_LABEL_SERVICE_IDENTIFICATION, nameSpace);

        // Children elements        
        Element titleElement = new Element(petascope.core.XMLSymbols.OWS_LABEL_TITLE, nameSpace);
        titleElement.appendChild(serviceIdentification.getServiceTitle());
        serviceIdentificationElement.appendChild(titleElement);

        Element abstractElement = new Element(petascope.core.XMLSymbols.OWS_LABEL_ABSTRACT, nameSpace);
        abstractElement.appendChild(serviceIdentification.getServiceAbstract());
        serviceIdentificationElement.appendChild(abstractElement);

        if (serviceIdentification.getKeywords().size() > 0) {
            Element keyWordsElement = new Element(XMLSymbols.OWS_LABEL_KEYWORDS, nameSpace);
            // keyWords element can contain multiple KeyWord elements
            for (String keyWord : serviceIdentification.getKeywords()) {
                Element keyWordElement = new Element(XMLSymbols.OWS_LABEL_KEYWORD, nameSpace);
                keyWordElement.appendChild(keyWord);

                keyWordsElement.appendChild(keyWordElement);
            }
            serviceIdentificationElement.appendChild(keyWordsElement);
        }

        Element serviceTypeElement = new Element(petascope.core.XMLSymbols.OWS_LABEL_SERVICE_TYPE, nameSpace);
        serviceTypeElement.appendChild(serviceIdentification.getServiceType());
        serviceIdentificationElement.appendChild(serviceTypeElement);

        // Only supports WCS 2.0.1 version now
        for (String version : serviceIdentification.getServiceTypeVersions()) {
            Element serviceTypeVersionElement = new Element(petascope.core.XMLSymbols.OWS_LABEL_SERVICE_TYPE_VERSION, nameSpace);
            serviceTypeVersionElement.appendChild(version);
            serviceIdentificationElement.appendChild(serviceTypeVersionElement);
        }

        if (serviceIdentification.getFees() != null) {
            Element feesElement = new Element(XMLSymbols.OWS_LABEL_FEES, nameSpace);
            feesElement.appendChild(serviceIdentification.getFees());
            serviceIdentificationElement.appendChild(feesElement);
        }

        if (serviceIdentification.getAccessConstraints().size() > 0) {
            for (String accessContraint : serviceIdentification.getAccessConstraints()) {
                Element accessConstraintsElement = new Element(XMLSymbols.OWS_LABEL_ACCESS_CONSTRAINTS, nameSpace);
                accessConstraintsElement.appendChild(accessContraint);
                serviceIdentificationElement.appendChild(accessConstraintsElement);
            }
        }

        // List of profiles (i.e: the WCS supported extensions)
        for (String extension : getProfiles()) {
            Element profileElement = new Element(XMLSymbols.OWS_LABEL_PROFILE, nameSpace);
            profileElement.appendChild(extension);

            serviceIdentificationElement.appendChild(profileElement);
        }

        return serviceIdentificationElement;
    }

    /**
     * Build the ServiceProvider element
     *
     * @return
     */
    private Element buildServiceProvider(String nameSpace) {
        ServiceProvider serviceProvider = this.owsServiceMetadata.getServiceProvider();

        // 1. parent element
        Element serviceProviderElement = new Element(XMLSymbols.OWS_LABEL_SERVICE_PROVIDER, nameSpace);

        // 1.1. children elements
        Element providerNameElement = new Element(XMLSymbols.OWS_LABEL_PROVIDER_NAME, nameSpace);
        providerNameElement.appendChild(serviceProvider.getProviderName());
        serviceProviderElement.appendChild(providerNameElement);

        Element providerSiteElement = new Element(XMLSymbols.OWS_LABEL_PROVIDER_SITE, nameSpace);
        Attribute providerSiteAttribute = new Attribute(XMLSymbols.PREFIX_XLINK + ":" + XMLSymbols.ATT_HREF,
                XMLSymbols.NAMESPACE_XLINK,
                serviceProvider.getProviderSite());
        providerSiteElement.addAttribute(providerSiteAttribute);
        serviceProviderElement.appendChild(providerSiteElement);

        ServiceContact serviceContact = serviceProvider.getServiceContact();
        Element serviceContactElement = new Element(XMLSymbols.OWS_LABEL_SERVICE_CONTACT, nameSpace);
        serviceProviderElement.appendChild(serviceContactElement);

        // 1.1.1 grand children elements (*children of ServiceContact element*)
        Element individualNameElement = new Element(XMLSymbols.OWS_LABEL_INDIVIDUAL_NAME, nameSpace);
        individualNameElement.appendChild(serviceContact.getIndividualName());
        serviceContactElement.appendChild(individualNameElement);

        Element positionNameElement = new Element(XMLSymbols.OWS_LABEL_POSITION_NAME, nameSpace);
        positionNameElement.appendChild(serviceContact.getPositionName());
        serviceContactElement.appendChild(positionNameElement);

        Element contactInfoElement = new Element(XMLSymbols.OWS_LABEL_CONTACT_INFO, nameSpace);
        ContactInfo contactInfo = serviceContact.getContactInfo();
        serviceContactElement.appendChild(contactInfoElement);

        // 1.1.1.1 *children of ContactInfo element*        
        if (contactInfo.getContactInstructions() != null) {
            Element contactInstructionsElement = new Element(XMLSymbols.OWS_LABEL_CONTACT_INSTRUCTIONS, nameSpace);
            contactInstructionsElement.appendChild(contactInfo.getContactInstructions());
            contactInfoElement.appendChild(contactInstructionsElement);
        }

        if (contactInfo.getHoursOfService() != null) {
            Element hoursOfServiceElement = new Element(XMLSymbols.OWS_LABEL_HOURS_OF_SERVICE, nameSpace);
            hoursOfServiceElement.appendChild(contactInfo.getHoursOfService());
            contactInfoElement.appendChild(hoursOfServiceElement);
        }

        if (contactInfo.getOnlineResource() != null) {
            Element onlineResourceElement = new Element(XMLSymbols.OWS_LABEL_ONLINE_RESOURCE, nameSpace);
            onlineResourceElement.appendChild(contactInfo.getOnlineResource());
            contactInfoElement.appendChild(onlineResourceElement);
        }

        Phone phone = contactInfo.getPhone();
        Element phoneElement = this.buildPhoneElement(phone, nameSpace);
        if (phoneElement.getChildCount() > 0) {
            contactInfoElement.appendChild(phoneElement);
        }

        Address address = contactInfo.getAddress();
        Element addressElement = this.buildAddressElement(address, nameSpace);
        if (addressElement.getChildCount() > 0) {
            contactInfoElement.appendChild(addressElement);
        }

        // 1.1.1 *children of ServiceContact element*
        Element roleNameElement = new Element(XMLSymbols.OWS_LABEL_ROLE, nameSpace);
        roleNameElement.appendChild(serviceContact.getRole());
        serviceContactElement.appendChild(roleNameElement);

        return serviceProviderElement;
    }

    /**
     * Build the address element of ContactInfo of ServiceContact of
     * ServiceProvider element
     *
     * @return
     */
    private Element buildAddressElement(Address address, String nameSpace) {
        Element addressElement = new Element(XMLSymbols.OWS_LABEL_ADDRESS, nameSpace);

        // Children elements
        if (address.getDeliveryPoints().size() > 0) {
            for (String deliverPoint : address.getDeliveryPoints()) {
                Element deliveryPointElement = new Element(XMLSymbols.OWS_LABEL_DELIVERY_POINT, nameSpace);
                deliveryPointElement.appendChild(deliverPoint);
                addressElement.appendChild(deliveryPointElement);
            }
        }

        if (address.getCity() != null) {
            Element cityElement = new Element(XMLSymbols.OWS_LABEL_CITY, nameSpace);
            cityElement.appendChild(address.getCity());
            addressElement.appendChild(cityElement);
        }

        if (address.getAdministrativeArea() != null) {
            Element administrativeAreaElement = new Element(XMLSymbols.OWS_LABEL_ADMINISTRATIVE_AREA, nameSpace);
            administrativeAreaElement.appendChild(address.getAdministrativeArea());
            addressElement.appendChild(administrativeAreaElement);
        }

        if (address.getPostalCode() != null) {
            Element postalCodeElement = new Element(XMLSymbols.OWS_LABEL_POSTAL_CODE, nameSpace);
            postalCodeElement.appendChild(address.getPostalCode());
            addressElement.appendChild(postalCodeElement);

        }

        if (address.getCountry() != null) {
            Element countryElement = new Element(XMLSymbols.OWS_LABEL_COUNTRY, nameSpace);
            countryElement.appendChild(address.getCountry());
            addressElement.appendChild(countryElement);
        }

        if (address.getElectronicMailAddresses().size() > 0) {
            for (String email : address.getElectronicMailAddresses()) {
                Element eMailElement = new Element(XMLSymbols.OWS_LABEL_EMAIL_ADDRESS, nameSpace);
                eMailElement.appendChild(email);
                addressElement.appendChild(eMailElement);
            }
        }

        return addressElement;
    }

    /**
     * Build the Phone element of ContactInfo of ServiceContact of
     * ServiceProvider element
     *
     * @return
     */
    private Element buildPhoneElement(Phone phone, String nameSpace) {
        Element phoneElement = new Element(XMLSymbols.OWS_LABEL_PHONE, nameSpace);

        // Children elements
        if (phone.getVoicePhones().size() > 0) {
            for (String voicePhone : phone.getVoicePhones()) {
                Element voiceElement = new Element(XMLSymbols.OWS_LABEL_VOICE, nameSpace);
                voiceElement.appendChild(voicePhone);
                phoneElement.appendChild(voiceElement);
            }
        }

        if (phone.getFacsimilePhones().size() > 0) {
            for (String facSimile : phone.getFacsimilePhones()) {
                Element facSimileElement = new Element(XMLSymbols.OWS_LABEL_FACSIMILE, nameSpace);
                facSimileElement.appendChild(facSimile);
                phoneElement.appendChild(facSimileElement);
            }
        }

        return phoneElement;
    }

    /**
     * Build the ows:OperationsMetadata element, most content is from the
     * template file
     *
     * @return
     */
    private Element buildOperationsMetadataElement() throws PetascopeException {
        try {
            String operationsMetadataTemplate = Templates.getTemplate(Templates.WCS2_GET_CAPABILITIES_OPERATIONS_METADATA_FILE);
            operationsMetadataTemplate = operationsMetadataTemplate.replace(WCS2_GET_CAPABILITIES_OPERATIONS_METADATA_ELEMENT_URL, ConfigManager.PETASCOPE_ENDPOINT_URL);

            // Parse XML in String to XML element
            Element operationsMetadataElement = XMLUtil.buildDocument(null, operationsMetadataTemplate).getRootElement();

            return operationsMetadataElement;
        } catch (IOException | ParsingException ex) {
            log.error("Cannot build ows:OperationsMetadata element", ex);
        }

        return null;
    }

    /**
     * Build the ServiceMetadata element
     *
     * @return
     */
    private Element buildServiceMetadataElement(String nameSpace) {
        Element serviceMetadataElement = new Element(XMLSymbols.WCS_LABEL_SERVICE_METADATA, nameSpace);

        // Children elements
        // List of supported encode format extensions
        for (String mimeType : MIMEUtil.getAllMimeTypes()) {
            Element formatSupportedElement = new Element(XMLSymbols.WCS_LABEL_FORMAT_SUPPORTED, nameSpace);
            formatSupportedElement.appendChild(mimeType);

            serviceMetadataElement.appendChild(formatSupportedElement);
        }

        // Current, only contains int:InterpolationMetadata element
        Element wcsExtensionElement = this.buildExtensionElement(nameSpace);
        serviceMetadataElement.appendChild(wcsExtensionElement);

        return serviceMetadataElement;
    }

    /**
     * Build wcs:Extension of wcs:ServiceMetadata element
     *
     * @return
     */
    private Element buildExtensionElement(String nameSpace) {
        Element wcsExtensionElement = new Element(XMLSymbols.WCS_LABEL_EXTENSION, nameSpace);

        // 1.1 Children elements of wcsExtension element
        Element interpolationMetadataElement = new Element(XMLSymbols.INT_LABEL_INTERPOLATION_METADATA, XMLSymbols.NAMESPACE_INTERPOLATION);
        wcsExtensionElement.appendChild(interpolationMetadataElement);

        // 1.1.1 Children elements of InterpolationMetadata element
        Element interpolationSupportedElement = new Element(XMLSymbols.INT_LABEL_INTERPOLATION_SUPPORTED, XMLSymbols.NAMESPACE_INTERPOLATION);
        interpolationSupportedElement.appendChild(INTERPOLATION_NEAREST_NEIGHBOR);
        interpolationMetadataElement.appendChild(interpolationSupportedElement);

        return wcsExtensionElement;
    }

    /**
     * Build wcs:Content element
     *
     * @param nameSpace
     * @return
     */
    private Element buildContentsElement(String nameSpace) throws PetascopeException, SecoreException {
        Element contentsElement = new Element(XMLSymbols.WCS_LABEL_CONTENTS, nameSpace);
        List<Coverage> importedCoverages = this.persistedCoverageService.readAllCoverages();

        // Children elements (list of all imported coverage)
        for (Coverage coverage : importedCoverages) {
            // 1.1 CoverageSummary element
            Element coverageSummaryElement = new Element(XMLSymbols.WCS_LABEL_COVERAGE_SUMMARY, nameSpace);
            contentsElement.appendChild(coverageSummaryElement);

            // 1.1.1 Children elements of CoverageSummary element
            Element coverageIdElement = new Element(XMLSymbols.LABEL_COVERAGE_ID, nameSpace);
            coverageIdElement.appendChild(coverage.getCoverageId());
            coverageSummaryElement.appendChild(coverageIdElement);

            Element coverageSubTypeElement = new Element(XMLSymbols.LABEL_COVERAGE_SUBTYPE, nameSpace);
            coverageSubTypeElement.appendChild(coverage.getCoverageType());
            coverageSummaryElement.appendChild(coverageSubTypeElement);

            EnvelopeByAxis envelopeByAxis = coverage.getEnvelope().getEnvelopeByAxis();
            Element boundingBox = new Element(XMLSymbols.OWS_LABEL_BOUNDING_BOX, NAMESPACE_OWS);
            // Attributes of BoundingBox element
            // attribute: compound Crs
            String crs = envelopeByAxis.getSrsName();
            Attribute crsAttribute = new Attribute(XMLSymbols.ATT_CRS, crs);
            boundingBox.addAttribute(crsAttribute);

            // attribute: dimensions
            Integer dimensions = envelopeByAxis.getSrsDimension();
            Attribute dimensionsAttribute = new Attribute(XMLSymbols.ATT_DIMENSIONS, dimensions.toString());
            boundingBox.addAttribute(dimensionsAttribute);

            coverageSummaryElement.appendChild(boundingBox);

            // 1.1.1.1 Children elements of BoundingBox element
            Element lowerCornerElement = new Element(XMLSymbols.OWS_LABEL_LOWER_CORNER, NAMESPACE_OWS);
            lowerCornerElement.appendChild(envelopeByAxis.getLowerCornerRepresentation());
            boundingBox.appendChild(lowerCornerElement);

            Element upperCornerElement = new Element(XMLSymbols.OWS_LABEL_UPPER_CORNER, NAMESPACE_OWS);
            upperCornerElement.appendChild(envelopeByAxis.getUpperCornerRepresentation());
            boundingBox.appendChild(upperCornerElement);
        }

        return contentsElement;
    }
}
