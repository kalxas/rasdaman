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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.gml;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nu.xom.Attribute;
import nu.xom.Element;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.owsmetadata.Address;
import org.rasdaman.domain.owsmetadata.ContactInfo;
import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.rasdaman.domain.owsmetadata.Phone;
import org.rasdaman.domain.owsmetadata.ServiceContact;
import org.rasdaman.domain.owsmetadata.ServiceIdentification;
import org.rasdaman.domain.owsmetadata.ServiceProvider;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.OWSMetadataRepostioryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.rasdaman.config.VersionManager;
import org.rasdaman.domain.cis.RasdamanDownscaledCollection;
import petascope.core.BoundingBox;
import static petascope.core.KVPSymbols.VALUE_GENERAL_GRID_COVERAGE;
import static petascope.core.KVPSymbols.WCS_SERVICE;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.ListUtil;
import petascope.util.MIMEUtil;
import petascope.util.XMLUtil;
import static petascope.core.XMLSymbols.ATT_CRS;
import static petascope.core.XMLSymbols.ATT_DIMENSIONS;
import static petascope.core.XMLSymbols.ATT_HREF;
import static petascope.core.XMLSymbols.ATT_NAME;
import static petascope.core.XMLSymbols.ATT_VALUE_POST_ENDCODING;
import static petascope.core.XMLSymbols.LABEL_ABSTRACT;
import static petascope.core.XMLSymbols.LABEL_ACCESS_CONSTRAINTS;
import static petascope.core.XMLSymbols.LABEL_ADDRESS;
import static petascope.core.XMLSymbols.LABEL_ADMINISTRATIVE_AREA;
import static petascope.core.XMLSymbols.LABEL_ALLOWED_VALUES;
import static petascope.core.XMLSymbols.LABEL_BOUNDING_BOX;
import static petascope.core.XMLSymbols.LABEL_CAPABILITIES;
import static petascope.core.XMLSymbols.LABEL_CITY;
import static petascope.core.XMLSymbols.LABEL_CONSTRAINT_ASSOCIATE_ROLE;
import static petascope.core.XMLSymbols.LABEL_CONTACT_INFO;
import static petascope.core.XMLSymbols.LABEL_CONTACT_INSTRUCTIONS;
import static petascope.core.XMLSymbols.LABEL_CONTENTS;
import static petascope.core.XMLSymbols.LABEL_COUNTRY;
import static petascope.core.XMLSymbols.LABEL_COVERAGE_ID;
import static petascope.core.XMLSymbols.LABEL_COVERAGE_SUBTYPE;
import static petascope.core.XMLSymbols.LABEL_COVERAGE_SUMMARY;
import static petascope.core.XMLSymbols.LABEL_CUSTOMIZED_DOWNSCALED_COLLECTION_LEVELS;
import static petascope.core.XMLSymbols.LABEL_CUSTOMIZED_METADATA;
import static petascope.core.XMLSymbols.LABEL_CUSTOMIZED_METADATA_COVERAGE_SIZE_IN_BYTES;
import static petascope.core.XMLSymbols.LABEL_DCP;
import static petascope.core.XMLSymbols.LABEL_DELIVERY_POINT;
import static petascope.core.XMLSymbols.LABEL_DESCRIBE_COVERAGE;
import static petascope.core.XMLSymbols.LABEL_EMAIL_ADDRESS;
import static petascope.core.XMLSymbols.LABEL_EXTENSION;
import static petascope.core.XMLSymbols.LABEL_FACSIMILE;
import static petascope.core.XMLSymbols.LABEL_FEES;
import static petascope.core.XMLSymbols.LABEL_FORMAT_SUPPORTED;
import static petascope.core.XMLSymbols.LABEL_GET;
import static petascope.core.XMLSymbols.LABEL_GET_CAPABILITIES;
import static petascope.core.XMLSymbols.LABEL_GET_COVERAGE;
import static petascope.core.XMLSymbols.LABEL_HOURS_OF_SERVICE;
import static petascope.core.XMLSymbols.LABEL_HTTP;
import static petascope.core.XMLSymbols.LABEL_INDIVIDUAL_NAME;
import static petascope.core.XMLSymbols.LABEL_INTERPOLATION_METADATA;
import static petascope.core.XMLSymbols.LABEL_INTERPOLATION_SUPPORTED;
import static petascope.core.XMLSymbols.LABEL_KEYWORD;
import static petascope.core.XMLSymbols.LABEL_KEYWORDS;
import static petascope.core.XMLSymbols.LABEL_LOWER_CORNER_ASSOCIATE_ROLE;
import static petascope.core.XMLSymbols.LABEL_ONLINE_RESOURCE;
import static petascope.core.XMLSymbols.LABEL_OPERATION;
import static petascope.core.XMLSymbols.LABEL_OPERATIONS_METADATA;
import static petascope.core.XMLSymbols.LABEL_PHONE;
import static petascope.core.XMLSymbols.LABEL_POSITION_NAME;
import static petascope.core.XMLSymbols.LABEL_POST;
import static petascope.core.XMLSymbols.LABEL_POSTAL_CODE;
import static petascope.core.XMLSymbols.LABEL_PROFILE;
import static petascope.core.XMLSymbols.LABEL_PROVIDER_NAME;
import static petascope.core.XMLSymbols.LABEL_PROVIDER_SITE;
import static petascope.core.XMLSymbols.LABEL_ROLE;
import static petascope.core.XMLSymbols.LABEL_SERVICE_CONTACT;
import static petascope.core.XMLSymbols.LABEL_SERVICE_IDENTIFICATION;
import static petascope.core.XMLSymbols.LABEL_SERVICE_METADATA;
import static petascope.core.XMLSymbols.LABEL_SERVICE_PROVIDER;
import static petascope.core.XMLSymbols.LABEL_SERVICE_TYPE;
import static petascope.core.XMLSymbols.LABEL_SERVICE_TYPE_VERSION;
import static petascope.core.XMLSymbols.LABEL_TITLE;
import static petascope.core.XMLSymbols.LABEL_UPPER_CORNER_ASSOCIATE_ROLE;
import static petascope.core.XMLSymbols.LABEL_VALUE;
import static petascope.core.XMLSymbols.LABEL_VERSION;
import static petascope.core.XMLSymbols.LABEL_VOICE;
import static petascope.core.XMLSymbols.LABEL_WGS84_BOUNDING_BOX;
import static petascope.core.XMLSymbols.NAMESPACE_INTERPOLATION;
import static petascope.core.XMLSymbols.NAMESPACE_OWS;
import static petascope.core.XMLSymbols.NAMESPACE_RASDAMAN;
import static petascope.core.XMLSymbols.NAMESPACE_WCS_CRS;
import static petascope.core.XMLSymbols.NAMESPACE_XLINK;
import static petascope.core.XMLSymbols.PREFIX_INT;
import static petascope.core.XMLSymbols.PREFIX_OWS;
import static petascope.core.XMLSymbols.PREFIX_WCS;
import static petascope.core.XMLSymbols.PREFIX_WCS_CRS;
import static petascope.core.XMLSymbols.PREFIX_XLINK;
import static petascope.core.XMLSymbols.VALUE_CONSTRAINT_POST_ENCODING_SOAP;
import static petascope.core.XMLSymbols.VALUE_CONSTRAINT_POST_ENCODING_XML;
import static petascope.core.XMLSymbols.NAMESPACE_WCS_20;
import static petascope.core.XMLSymbols.NAMESPACE_WCS_21;
import static petascope.core.XMLSymbols.PREFIX_RASDAMAN;
import static petascope.core.XMLSymbols.SCHEMA_LOCATION_WCS_20_GET_CAPABILITIES;
import static petascope.core.XMLSymbols.SCHEMA_LOCATION_WCS_21_GET_CAPABILITIES;
import petascope.util.BigDecimalUtil;

/**
 * Class to represent result of WCS GetCapabilities request.
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class GMLGetCapabilitiesBuilder {

    @Autowired
    private CoverageRepositoryService persistedCoverageService;
    @Autowired
    private OWSMetadataRepostioryService persistedOwsServiceMetadataService;

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
    private static final String INTERPOLATION_NEAR = "http://www.opengis.net/def/interpolation/OGC/1.0/near";
    private static final String INTERPOLATION_BILINEAR = "http://www.opengis.net/def/interpolation/OGC/1.0/bilinear";
    private static final String INTERPOLATION_CUBIC = "http://www.opengis.net/def/interpolation/OGC/1.0/cubic";
    private static final String INTERPOLATION_CUBICSPLINE = "http://www.opengis.net/def/interpolation/OGC/1.0/cubicspline";
    private static final String INTERPOLATION_LANCZOS = "http://www.opengis.net/def/interpolation/OGC/1.0/lanczos";
    private static final String INTERPOLATION_AVERAGE = "http://www.opengis.net/def/interpolation/OGC/1.0/average";
    private static final String INTERPOLATION_MODE = "http://www.opengis.net/def/interpolation/OGC/1.0/mode";
    private static final String INTERPOLATION_MAX = "http://www.opengis.net/def/interpolation/OGC/1.0/max";
    private static final String INTERPOLATION_MIN = "http://www.opengis.net/def/interpolation/OGC/1.0/min";
    private static final String INTERPOLATION_MED = "http://www.opengis.net/def/interpolation/OGC/1.0/med";
    private static final String INTERPOLATION_Q1 = "http://www.opengis.net/def/interpolation/OGC/1.0/q1";
    private static final String INTERPOLATION_Q3 = "http://www.opengis.net/def/interpolation/OGC/1.0/q3";

    // Singleton object to store all the extensions (profiles) of WCS
    private static List<String> profiles;

    public static final List<String> SUPPORTED_INTERPOLATIONS = ListUtil.valuesToList(INTERPOLATION_NEAR, INTERPOLATION_BILINEAR,
                                                                        INTERPOLATION_CUBIC, INTERPOLATION_CUBICSPLINE, INTERPOLATION_LANCZOS,
                                                                        INTERPOLATION_AVERAGE, INTERPOLATION_MODE, INTERPOLATION_MAX,
                                                                        INTERPOLATION_MIN, INTERPOLATION_MED, INTERPOLATION_Q1, INTERPOLATION_Q3
                                                                        );

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

    /**
     * Build the ServiceIdentication element
     */
    private Element buildServiceIdentification(OwsServiceMetadata owsServiceMetadata) {

        ServiceIdentification serviceIdentification = owsServiceMetadata.getServiceIdentification();
        Element serviceIdentificationElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_SERVICE_IDENTIFICATION), NAMESPACE_OWS);

        // Children elements        
        Element titleElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_TITLE), NAMESPACE_OWS);
        titleElement.appendChild(serviceIdentification.getServiceTitle());
        serviceIdentificationElement.appendChild(titleElement);

        Element abstractElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_ABSTRACT), NAMESPACE_OWS);
        abstractElement.appendChild(serviceIdentification.getServiceAbstract());
        serviceIdentificationElement.appendChild(abstractElement);

        if (serviceIdentification.getKeywords().size() > 0) {
            Element keyWordsElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_KEYWORDS), NAMESPACE_OWS);
            // keyWords element can contain multiple KeyWord elements
            for (String keyWord : serviceIdentification.getKeywords()) {
                Element keyWordElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_KEYWORD), NAMESPACE_OWS);
                keyWordElement.appendChild(keyWord);

                keyWordsElement.appendChild(keyWordElement);
            }
            serviceIdentificationElement.appendChild(keyWordsElement);
        }

        Element serviceTypeElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_SERVICE_TYPE), NAMESPACE_OWS);
        serviceTypeElement.appendChild(serviceIdentification.getServiceType());
        serviceIdentificationElement.appendChild(serviceTypeElement);

        // List of WCS supported versions
        for (String version : VersionManager.getAllSupportedVersions(WCS_SERVICE)) {
            Element serviceTypeVersionElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_SERVICE_TYPE_VERSION), NAMESPACE_OWS);
            serviceTypeVersionElement.appendChild(version);
            serviceIdentificationElement.appendChild(serviceTypeVersionElement);
        }

        if (serviceIdentification.getFees() != null) {
            Element feesElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_FEES), NAMESPACE_OWS);
            feesElement.appendChild(serviceIdentification.getFees());
            serviceIdentificationElement.appendChild(feesElement);
        }

        if (serviceIdentification.getAccessConstraints().size() > 0) {
            for (String accessContraint : serviceIdentification.getAccessConstraints()) {
                Element accessConstraintsElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_ACCESS_CONSTRAINTS), NAMESPACE_OWS);
                accessConstraintsElement.appendChild(accessContraint);
                serviceIdentificationElement.appendChild(accessConstraintsElement);
            }
        }

        // List of profiles (i.e: the WCS supported extensions)
        for (String extension : getProfiles()) {
            Element profileElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_PROFILE), NAMESPACE_OWS);
            profileElement.appendChild(extension);

            serviceIdentificationElement.appendChild(profileElement);
        }

        return serviceIdentificationElement;
    }

    /**
     * Build the ServiceProvider element
     */
    private Element buildServiceProvider(OwsServiceMetadata owsServiceMetadata) {
        ServiceProvider serviceProvider = owsServiceMetadata.getServiceProvider();

        // 1. parent element
        Element serviceProviderElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_SERVICE_PROVIDER), NAMESPACE_OWS);

        // 1.1. children elements
        Element providerNameElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_PROVIDER_NAME), NAMESPACE_OWS);
        providerNameElement.appendChild(serviceProvider.getProviderName());
        serviceProviderElement.appendChild(providerNameElement);

        Element providerSiteElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_PROVIDER_SITE), NAMESPACE_OWS);
        Attribute providerSiteAttribute = new Attribute(XMLUtil.createXMLLabel(PREFIX_XLINK, ATT_HREF), NAMESPACE_XLINK,  serviceProvider.getProviderSite());
        providerSiteElement.addAttribute(providerSiteAttribute);
        serviceProviderElement.appendChild(providerSiteElement);

        ServiceContact serviceContact = serviceProvider.getServiceContact();
        Element serviceContactElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_SERVICE_CONTACT), NAMESPACE_OWS);
        serviceProviderElement.appendChild(serviceContactElement);

        // 1.1.1 grand children elements (*children of ServiceContact element*)
        Element individualNameElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_INDIVIDUAL_NAME), NAMESPACE_OWS);
        individualNameElement.appendChild(serviceContact.getIndividualName());
        serviceContactElement.appendChild(individualNameElement);

        Element positionNameElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_POSITION_NAME), NAMESPACE_OWS);
        positionNameElement.appendChild(serviceContact.getPositionName());
        serviceContactElement.appendChild(positionNameElement);

        Element contactInfoElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_CONTACT_INFO), NAMESPACE_OWS);
        ContactInfo contactInfo = serviceContact.getContactInfo();
        serviceContactElement.appendChild(contactInfoElement);

        // 1.1.1.1 *children of ContactInfo element*        
        if (contactInfo.getContactInstructions() != null) {
            Element contactInstructionsElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_CONTACT_INSTRUCTIONS), NAMESPACE_OWS);
            contactInstructionsElement.appendChild(contactInfo.getContactInstructions());
            contactInfoElement.appendChild(contactInstructionsElement);
        }

        if (contactInfo.getHoursOfService() != null) {
            Element hoursOfServiceElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_HOURS_OF_SERVICE), NAMESPACE_OWS);
            hoursOfServiceElement.appendChild(contactInfo.getHoursOfService());
            contactInfoElement.appendChild(hoursOfServiceElement);
        }

        if (contactInfo.getOnlineResource() != null) {
            Element onlineResourceElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_ONLINE_RESOURCE), NAMESPACE_OWS);
            onlineResourceElement.appendChild(contactInfo.getOnlineResource());
            contactInfoElement.appendChild(onlineResourceElement);
        }

        Phone phone = contactInfo.getPhone();
        Element phoneElement = this.buildPhoneElement(phone);
        if (phoneElement.getChildCount() > 0) {
            contactInfoElement.appendChild(phoneElement);
        }

        Address address = contactInfo.getAddress();
        Element addressElement = this.buildAddressElement(address);
        if (addressElement.getChildCount() > 0) {
            contactInfoElement.appendChild(addressElement);
        }

        // 1.1.1 *children of ServiceContact element*
        Element roleNameElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_ROLE), NAMESPACE_OWS);
        roleNameElement.appendChild(serviceContact.getRole());
        serviceContactElement.appendChild(roleNameElement);

        return serviceProviderElement;
    }

    /**
     * Build the address element of ContactInfo of ServiceContact of
     * ServiceProvider element
     */
    private Element buildAddressElement(Address address) {
        Element addressElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_ADDRESS), NAMESPACE_OWS);

        // Children elements
        if (address.getDeliveryPoints().size() > 0) {
            for (String deliverPoint : address.getDeliveryPoints()) {
                Element deliveryPointElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_DELIVERY_POINT), NAMESPACE_OWS);
                deliveryPointElement.appendChild(deliverPoint);
                addressElement.appendChild(deliveryPointElement);
            }
        }

        if (address.getCity() != null) {
            Element cityElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_CITY), NAMESPACE_OWS);
            cityElement.appendChild(address.getCity());
            addressElement.appendChild(cityElement);
        }

        if (address.getAdministrativeArea() != null) {
            Element administrativeAreaElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_ADMINISTRATIVE_AREA), NAMESPACE_OWS);
            administrativeAreaElement.appendChild(address.getAdministrativeArea());
            addressElement.appendChild(administrativeAreaElement);
        }

        if (address.getPostalCode() != null) {
            Element postalCodeElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_POSTAL_CODE), NAMESPACE_OWS);
            postalCodeElement.appendChild(address.getPostalCode());
            addressElement.appendChild(postalCodeElement);

        }

        if (address.getCountry() != null) {
            Element countryElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_COUNTRY), NAMESPACE_OWS);
            countryElement.appendChild(address.getCountry());
            addressElement.appendChild(countryElement);
        }

        if (address.getElectronicMailAddresses().size() > 0) {
            for (String email : address.getElectronicMailAddresses()) {
                Element eMailElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_EMAIL_ADDRESS), NAMESPACE_OWS);
                eMailElement.appendChild(email);
                addressElement.appendChild(eMailElement);
            }
        }

        return addressElement;
    }

    /**
     * Build the Phone element of ContactInfo of ServiceContact of
     * ServiceProvider element
     */
    private Element buildPhoneElement(Phone phone) {
        Element phoneElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_PHONE), NAMESPACE_OWS);

        // Children elements
        if (phone.getVoicePhones().size() > 0) {
            for (String voicePhone : phone.getVoicePhones()) {
                Element voiceElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_VOICE), NAMESPACE_OWS);
                voiceElement.appendChild(voicePhone);
                phoneElement.appendChild(voiceElement);
            }
        }

        if (phone.getFacsimilePhones().size() > 0) {
            for (String facSimile : phone.getFacsimilePhones()) {
                Element facSimileElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_FACSIMILE), NAMESPACE_OWS);
                facSimileElement.appendChild(facSimile);
                phoneElement.appendChild(facSimileElement);
            }
        }

        return phoneElement;
    }
    
    /**
     * Build Constraint element for OperationsMetadata element. e.g:
     
    <ows:Constraint name="PostEncoding">
        <ows:AllowedValues>
            <ows:Value>XML</ows:Value>
            <ows:Value>SOAP</ows:Value>
        </ows:AllowedValues>
    </ows:Constraint>
      
     */
    private Element buildConstraintElement() {
        
        Element constraintElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_CONSTRAINT_ASSOCIATE_ROLE), NAMESPACE_OWS);
        Attribute nameAttribute = new Attribute(ATT_NAME, ATT_VALUE_POST_ENDCODING);
        constraintElement.addAttribute(nameAttribute);
        
        Element allowedValuesElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_ALLOWED_VALUES), NAMESPACE_OWS);
        
        Element xmlValueElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_VALUE), NAMESPACE_OWS);
        xmlValueElement.appendChild(VALUE_CONSTRAINT_POST_ENCODING_XML);
        
        Element soapValueElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_VALUE), NAMESPACE_OWS);
        soapValueElement.appendChild(VALUE_CONSTRAINT_POST_ENCODING_SOAP);
        
        allowedValuesElement.appendChild(xmlValueElement);
        allowedValuesElement.appendChild(soapValueElement);
        
        constraintElement.appendChild(allowedValuesElement);
        
        return constraintElement;
    }
    
    /**
     * Build ows:Operation element for OperationsMetadata element. e.g:

    <ows:Operation name="GetCoverage">
        <ows:DCP>
            <ows:HTTP>
                <ows:Get xlink:href="http://dgiwg.ign.fr/wcs?" xlink:type="simple"/>
                <ows:Post xlink:href="http://dgiwg.ign.fr/wcs?" xlink:type="simple"/>                    
            </ows:HTTP>
        </ows:DCP>
    </ows:Operation>
     
     */
    private Element buildOperationElement(String operationName) {
        
        Element operationElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_OPERATION), NAMESPACE_OWS);
        
        Attribute nameAttribute = new Attribute(ATT_NAME, NAMESPACE_OWS);
        nameAttribute.setValue(operationName);
        operationElement.addAttribute(nameAttribute);
        
        
        Element dcpElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_DCP), NAMESPACE_OWS);
        Element httpElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_HTTP), NAMESPACE_OWS);
        
        // GET request
        Element getElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_GET), NAMESPACE_OWS);
        Attribute getHrefAttribute = XMLUtil.createXMLAttribute(NAMESPACE_XLINK, PREFIX_XLINK, ATT_HREF, ConfigManager.PETASCOPE_ENDPOINT_URL);
        getElement.addAttribute(getHrefAttribute);
        
        httpElement.appendChild(getElement);
        
        // POST request
        Element postElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_POST), NAMESPACE_OWS);
        Attribute postHrefAttribute = XMLUtil.createXMLAttribute(NAMESPACE_XLINK, PREFIX_XLINK, ATT_HREF, ConfigManager.PETASCOPE_ENDPOINT_URL);
        postElement.addAttribute(postHrefAttribute);
        
        httpElement.appendChild(postElement);
        
        dcpElement.appendChild(httpElement);
        
        operationElement.appendChild(dcpElement);
        
        return operationElement;
    }

    /**
     * Build the ows:OperationsMetadata element
     */
    private Element buildOperationsMetadataElement() throws PetascopeException {
        
        Element operationsMetadataElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_OPERATIONS_METADATA), NAMESPACE_OWS);
        
        // Operation name="GetCapabilities"
        Element operationGetCapabilitiesElement = this.buildOperationElement(LABEL_GET_CAPABILITIES);
        
        // Operation name="DescribeCoverage"
        Element operationDescribeCoverageElement = this.buildOperationElement(LABEL_DESCRIBE_COVERAGE);
        
        // Operation name="GetCoverage"
        Element operationGetCoverageElement = this.buildOperationElement(LABEL_GET_COVERAGE);
        
        Element constraintElement = this.buildConstraintElement();
                
        operationsMetadataElement.appendChild(operationGetCapabilitiesElement);
        operationsMetadataElement.appendChild(operationDescribeCoverageElement);
        operationsMetadataElement.appendChild(operationGetCoverageElement);
        operationsMetadataElement.appendChild(constraintElement);
        
        return operationsMetadataElement;
    }

    /**
     * Build the ServiceMetadata element
     */
    private Element buildServiceMetadataElement(String version) {
        
        String wcsNameSpace = this.getWCSNameSpace(version);        
        Element serviceMetadataElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_SERVICE_METADATA), wcsNameSpace);

        // Children elements
        // List of supported encode format extensions
        for (String mimeType : MIMEUtil.getAllMimeTypes()) {
            Element formatSupportedElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_FORMAT_SUPPORTED), wcsNameSpace);
            formatSupportedElement.appendChild(mimeType);

            serviceMetadataElement.appendChild(formatSupportedElement);
        }

        // Current, only contains int:InterpolationMetadata element
        Element wcsExtensionElement = this.buildExtensionElement(version);
        serviceMetadataElement.appendChild(wcsExtensionElement);

        return serviceMetadataElement;
    }

    /**
     * Build wcs:Extension of wcs:ServiceMetadata element
     */
    private Element buildExtensionElement(String version) {
        
        String wcsNameSpace = this.getWCSNameSpace(version);
        Element wcsExtensionElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_EXTENSION), wcsNameSpace);

        // 1.1 Children elements of wcsExtension element
        Element interpolationMetadataElement = new Element(XMLUtil.createXMLLabel(PREFIX_INT, LABEL_INTERPOLATION_METADATA), NAMESPACE_INTERPOLATION);
        wcsExtensionElement.appendChild(interpolationMetadataElement);

        // 1.1.1 Children elements of InterpolationMetadata element
        for (String interpolationType : SUPPORTED_INTERPOLATIONS) {
            Element interpolationSupportedElement = new Element(XMLUtil.createXMLLabel(PREFIX_INT, LABEL_INTERPOLATION_SUPPORTED), NAMESPACE_INTERPOLATION);
            interpolationSupportedElement.appendChild(interpolationType);
            interpolationMetadataElement.appendChild(interpolationSupportedElement);
        }

        return wcsExtensionElement;
    }

    /**
     * Build wcs:Content element
     */
    private Element buildContentsElement(String version) throws PetascopeException, SecoreException {
        Element contentsElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_CONTENTS), this.getWCSNameSpace(version));
        List<Pair<Coverage, Boolean>> importedCoveragePairs = this.persistedCoverageService.readAllCoveragesBasicMetatata();
        this.persistedCoverageService.createAllCoveragesExtents();

        // Children elements (list of all imported coverage)
        for (Pair<Coverage, Boolean> coveragePair : importedCoveragePairs) {
            Coverage coverage = coveragePair.fst;
            
            // NOTE: According to WCS 2.1.0 standard, WCS 2.0.1 does not list CIS 1.1 coverages to the result.
            if (version.equals(VersionManager.WCS_VERSION_20) && coverage.getCoverageType().equals(VALUE_GENERAL_GRID_COVERAGE)) {
                continue;
            }
            // 1.1 CoverageSummary element
            Element coverageSummaryElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_COVERAGE_SUMMARY), this.getWCSNameSpace(version));
            contentsElement.appendChild(coverageSummaryElement);

            // 1.1.1 Children elements of CoverageSummary element
            Element coverageIdElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_COVERAGE_ID), this.getWCSNameSpace(version));
            coverageIdElement.appendChild(coveragePair.fst.getCoverageId());
            coverageSummaryElement.appendChild(coverageIdElement);

            Element coverageSubTypeElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_COVERAGE_SUBTYPE), this.getWCSNameSpace(version));
            coverageSubTypeElement.appendChild(coveragePair.fst.getCoverageType());
            coverageSummaryElement.appendChild(coverageSubTypeElement);
            
            // Optional element for coverage which can reproject to WGS84 CRS
            Element wgs84BoundingBoxElement = this.createWGS84BoundingBoxElement(coverage.getCoverageId());
            if (wgs84BoundingBoxElement != null) {
                coverageSummaryElement.appendChild(wgs84BoundingBoxElement);
            }
            
            EnvelopeByAxis envelopeByAxis = coveragePair.fst.getEnvelope().getEnvelopeByAxis();
            Element boundingBox = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_BOUNDING_BOX), NAMESPACE_OWS);
            // Attributes of BoundingBox element
            // attribute: compound Crs
            String crs = envelopeByAxis.getSrsName();
            Attribute crsAttribute = new Attribute(ATT_CRS, crs);
            boundingBox.addAttribute(crsAttribute);

            // attribute: dimensions
            Integer dimensions = envelopeByAxis.getSrsDimension();
            Attribute dimensionsAttribute = new Attribute(ATT_DIMENSIONS, dimensions.toString());
            boundingBox.addAttribute(dimensionsAttribute);

            coverageSummaryElement.appendChild(boundingBox);
            
            if (!ConfigManager.OGC_CITE_OUTPUT_OPTIMIZATION) {
                // NOTE: Rasdaman customized metatadata is not valid from GML 3.2.1 schema validating in WCS GetCapabilities
                Element customizedMetadataElement = this.createCustomizedCoverageMetadataElement(coveragePair.fst);
                if (customizedMetadataElement != null) {
                    coverageSummaryElement.appendChild(customizedMetadataElement);
                }
            }

            // 1.1.1.1 Children elements of BoundingBox element
            Element lowerCornerElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_LOWER_CORNER_ASSOCIATE_ROLE), NAMESPACE_OWS);
            lowerCornerElement.appendChild(envelopeByAxis.getLowerCornerRepresentation());
            boundingBox.appendChild(lowerCornerElement);

            Element upperCornerElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_UPPER_CORNER_ASSOCIATE_ROLE), NAMESPACE_OWS);
            upperCornerElement.appendChild(envelopeByAxis.getUpperCornerRepresentation());
            boundingBox.appendChild(upperCornerElement);
        }

        return contentsElement;
    }

    /**
     * This metadata element is not the real coverage's metadata but some additional information
     * which is customized for client to get an overview of a coverage.
     * 
     * e.g: 
     * 
     * <ows:Metadata>
     *   <rasdaman:location>
     *      <rasdaman:hostname>code-de</rasdaman:hostname>
     *      <rasdaman:endpoint>http://code-de.bigdatacube.org:8080/rasdaman/ows</rasdaman:endpoint>
     *   <rasdaman:location>
     * </ows:Metadata>
     */
    public Element createCustomizedCoverageMetadataElement(Coverage coverage) {
	Element metadataElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_CUSTOMIZED_METADATA), NAMESPACE_OWS);

        // Coverage size in bytes
        Element coverageSizeInBytesElement = new Element(XMLUtil.createXMLLabel(PREFIX_RASDAMAN, LABEL_CUSTOMIZED_METADATA_COVERAGE_SIZE_IN_BYTES), NAMESPACE_RASDAMAN);
        Long sizeInBytes = coverage.getCoverageSizeInBytes();
        if (coverage.getCoverageSizeInBytes() > 0) {
            coverageSizeInBytesElement.appendChild(sizeInBytes.toString());
            metadataElement.appendChild(coverageSizeInBytesElement);
        }

        // Downscaled collection levels of a coverage if exist
        List<String> downscaledCollectionLevels = new ArrayList<>();
        for (RasdamanDownscaledCollection rasdamanDownscaledCollection : coverage.getRasdamanRangeSet().getRasdamanDownscaledCollections()) {
            downscaledCollectionLevels.add(rasdamanDownscaledCollection.getLevel().toPlainString());
        }
        
        if (downscaledCollectionLevels.size() > 0) {
            Element downscaledCollectionLevelsElement = new Element(XMLUtil.createXMLLabel(PREFIX_RASDAMAN, LABEL_CUSTOMIZED_DOWNSCALED_COLLECTION_LEVELS),
                                                                    NAMESPACE_RASDAMAN);
            String levels = ListUtil.join(downscaledCollectionLevels, ",");
            downscaledCollectionLevelsElement.appendChild(levels);
            metadataElement.appendChild(downscaledCollectionLevelsElement);
        }

        // No customized metadata is added for coverage, not show it to client
        if (metadataElement.getChildElements().size() == 0) {
            metadataElement = null;            
        }
        
        return metadataElement;
    }
    
    /**
     * Create an optional WGS84 bounding box for coverages which have X and Y georeferenced-axes
     * which can project to EPSG:4326 CRS (Long - Lat order)
     */
    private Element createWGS84BoundingBoxElement(String coverageId) {
        BoundingBox wgs84BoundingBox = this.persistedCoverageService.coveragesExtentsCacheMap.get(coverageId);
        Element wgs84BoundingBoxElement = null;
        
        if (wgs84BoundingBox != null) {
            // Only coverage with possible projection is displayed
            wgs84BoundingBoxElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_WGS84_BOUNDING_BOX), NAMESPACE_OWS);
            
            Element lowerCornerElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_LOWER_CORNER_ASSOCIATE_ROLE), NAMESPACE_OWS);
            String lowerCorner = BigDecimalUtil.stripDecimalZeros(wgs84BoundingBox.getXMin()) + " " + BigDecimalUtil.stripDecimalZeros(wgs84BoundingBox.getYMin());
            lowerCornerElement.appendChild(lowerCorner);
            wgs84BoundingBoxElement.appendChild(lowerCornerElement);

            Element upperCornerElement = new Element(XMLUtil.createXMLLabel(PREFIX_OWS, LABEL_UPPER_CORNER_ASSOCIATE_ROLE), NAMESPACE_OWS);
            String upperCorner = BigDecimalUtil.stripDecimalZeros(wgs84BoundingBox.getXMax()) + " " + BigDecimalUtil.stripDecimalZeros(wgs84BoundingBox.getYMax());
            upperCornerElement.appendChild(upperCorner);
            wgs84BoundingBoxElement.appendChild(upperCornerElement);
        }
        
        return wgs84BoundingBoxElement;
    }

    /**
     * Return WCS XML namespace by WCS version.
     */
    private String getWCSNameSpace(String version) {
        if (version.equals(VersionManager.WCS_VERSION_20)) {
            return NAMESPACE_WCS_20;
        } else {
            return NAMESPACE_WCS_21;
        }
    }
    
    public Element serializeToXMLElement(String version) throws PetascopeException, SecoreException {
        
        OwsServiceMetadata owsServiceMetadata = this.persistedOwsServiceMetadataService.read();
        
        Map<String, String> xmlNameSpacesMap = GMLWCSRequestResultBuilder.getMandatoryXMLNameSpacesMap();
        Set<String> schemaLocations = new LinkedHashSet<>();
        schemaLocations.add(SCHEMA_LOCATION_WCS_20_GET_CAPABILITIES);
        
        Element capabilitiesElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_CAPABILITIES), this.getWCSNameSpace(version));
        Attribute versionAttribute = new Attribute(LABEL_VERSION, version);
        capabilitiesElement.addAttribute(versionAttribute);
        
        Element serviceIdentificationElement = this.buildServiceIdentification(owsServiceMetadata);
        Element serviceProviderElement = this.buildServiceProvider(owsServiceMetadata);
        Element operationsMetadataElement = this.buildOperationsMetadataElement();
        Element serviceMetadataElement = this.buildServiceMetadataElement(version);
        Element contentsElement = this.buildContentsElement(version);
        
        capabilitiesElement.appendChild(serviceIdentificationElement);
        capabilitiesElement.appendChild(serviceProviderElement);
        capabilitiesElement.appendChild(operationsMetadataElement);
        capabilitiesElement.appendChild(serviceMetadataElement);
        capabilitiesElement.appendChild(contentsElement);
        
        // Adding some specific XML namespaces of only WCS GetCapabilities request
        xmlNameSpacesMap.put(PREFIX_INT, NAMESPACE_INTERPOLATION);
        xmlNameSpacesMap.put(PREFIX_WCS_CRS, NAMESPACE_WCS_CRS);
        
        XMLUtil.addXMLNameSpacesOnRootElement(xmlNameSpacesMap, capabilitiesElement);      
        XMLUtil.addXMLSchemaLocationsOnRootElement(schemaLocations, capabilitiesElement);
        
        return capabilitiesElement;        
    }
    
    /**
     * Build result for WCS GetCapabilities request by a specific version (e.g: 2.0.1).
     */
    public Element buildWCSGetCapabilitiesResult(String version) throws PetascopeException, SecoreException {
        
        Element capabilitiesElement = this.serializeToXMLElement(version);
        
        if (version.equals(VersionManager.WCS_VERSION_21)) {
            Set<String> schemaLocations = new LinkedHashSet<>();
            schemaLocations.add(SCHEMA_LOCATION_WCS_21_GET_CAPABILITIES);
            XMLUtil.addXMLSchemaLocationsOnRootElement(schemaLocations, capabilitiesElement);
        }
        
        return capabilitiesElement;
    }
}
