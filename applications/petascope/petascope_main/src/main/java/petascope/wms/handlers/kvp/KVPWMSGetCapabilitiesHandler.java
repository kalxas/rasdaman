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
package petascope.wms.handlers.kvp;

import java.util.ArrayList;
import petascope.core.response.Response;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import nu.xom.Attribute;
import nu.xom.Element;
import org.apache.commons.lang3.StringUtils;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.config.VersionManager;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.Wgs84BoundingBox;
import org.rasdaman.domain.owsmetadata.Address;
import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.rasdaman.domain.owsmetadata.Phone;
import org.rasdaman.domain.owsmetadata.ServiceIdentification;
import org.rasdaman.domain.owsmetadata.ServiceProvider;
import org.rasdaman.domain.wms.Dimension;
import org.rasdaman.domain.wms.EXGeographicBoundingBox;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.domain.wms.LayerAttribute;
import org.rasdaman.domain.wms.LegendURL;
import org.rasdaman.domain.wms.Style;
import org.rasdaman.domain.wms.Style.ColorTableType;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.OWSMetadataRepostioryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.PetascopeController;
import petascope.core.KVPSymbols;
import petascope.core.Templates;
import petascope.core.XMLSymbols;
import static petascope.core.XMLSymbols.ATT_HREF;
import static petascope.core.XMLSymbols.ATT_TYPE;
import static petascope.core.XMLSymbols.LABEL_ADDITIONAL_PARAMETER_NAME;
import static petascope.core.XMLSymbols.LABEL_ADDITIONAL_PARAMETER_VALUE;
import static petascope.core.XMLSymbols.LABEL_VERSION;
import static petascope.core.XMLSymbols.NAMESPACE_WMS;
import static petascope.core.XMLSymbols.NAMESPACE_XLINK;
import static petascope.core.XMLSymbols.NAMESPACE_XSI;
import static petascope.core.XMLSymbols.PREFIX_XLINK;
import static petascope.core.XMLSymbols.PREFIX_XSI;
import static petascope.core.XMLSymbols.SCHEMA_LOCATION_WMS;
import petascope.core.gml.GMLGetCapabilitiesBuilder;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.MIMEUtil;
import petascope.exceptions.WMSException;
import petascope.util.CrsProjectionUtil;
import petascope.util.ListUtil;
import petascope.util.XMLUtil;
import petascope.wms.exception.WMSLayerNotExistException;
import petascope.util.StringUtil;

/**
 * Handle the GetCapabilities WMS 1.3 request. A model result, see:
 * http://schemas.opengis.net/wms/1.3.0/capabilities_1_3_0.xml
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWMSGetCapabilitiesHandler extends KVPWMSAbstractHandler {

    private static Logger log = LoggerFactory.getLogger(KVPWMSGetCapabilitiesHandler.class);
    
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private OWSMetadataRepostioryService persistedOwsServiceMetadataService;
    @Autowired
    private GMLGetCapabilitiesBuilder wcsGMLGetCapabilitiesBuild;
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private PetascopeController petascopeController;    

    private OwsServiceMetadata owsServiceMetadata;

    public KVPWMSGetCapabilitiesHandler() {

    }

    // List of output format for WMS request (only 2D)
    public static final List<String> supportedFormats = ListUtil.valuesToList(MIMEUtil.MIME_JPEG, MIMEUtil.MIME_PNG, MIMEUtil.MIME_TIFF);

    // Xml output. (The default format)
    public static final String EXCEPTION_XML = "XML";
    // INIMAGE: Generates an image
    public static final String EXCEPTION_INIMAGE = "INIMAGE";
    // BLANK: Generates a blank image
    public static final String EXCEPTION_BLANK = "BLANK";
    public static final List<String> supportedExceptions = ListUtil.valuesToList(EXCEPTION_XML, EXCEPTION_INIMAGE, EXCEPTION_BLANK);

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws WMSException {

    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException, Exception {
        // Validate before handling the request
        this.validate(kvpParameters);

        owsServiceMetadata = this.persistedOwsServiceMetadataService.read();
        
        Element wmsGetCapabilitiesElement = this.buildWMSGetCapabilitiesElement();
        
        // format XML to have indentation
        String result = XMLUtil.formatXML(wmsGetCapabilitiesElement);
        // Replace all the string enquotes from getRepresentation() of inner elments
        result = XMLUtil.replaceEnquotes(result);

        // GetCapabilities only returns 1 XML string                
        return new Response(Arrays.asList(result.getBytes()), MIMEUtil.MIME_GML);
    }
    
    private Element buildWMSGetCapabilitiesElement() throws PetascopeException {
        
        Set<String> schemaLocations = new LinkedHashSet<>();
        schemaLocations.add(SCHEMA_LOCATION_WMS);
        
        Element wmsCapabilitiesElement = new Element(XMLSymbols.LABEL_WMS_WMS_CAPABILITIES);
        Attribute versionAttribute = new Attribute(LABEL_VERSION, VersionManager.WMS_VERSION_13);
        wmsCapabilitiesElement.addAttribute(versionAttribute);
        
        Attribute updateSequenceAttribute = new Attribute(XMLSymbols.ATT_WMS_UPDATE_SEQUENCE, "3");
        wmsCapabilitiesElement.addAttribute(updateSequenceAttribute);
        
        
        Element serviceElement = this.buildServiceElement();
        Element capabilityElement = this.buildCapabilityElement();
        
        wmsCapabilitiesElement.appendChild(serviceElement);
        wmsCapabilitiesElement.appendChild(capabilityElement);
        
        // Adding some specific XML namespaces of only WMS GetCapabilities request
        Map<String, String> xmlNameSpacesMap = new LinkedHashMap<>();
        xmlNameSpacesMap.put(PREFIX_XLINK, NAMESPACE_XLINK);
        xmlNameSpacesMap.put(PREFIX_XSI, NAMESPACE_XSI);
        
        XMLUtil.addXMLNameSpacesOnRootElement(xmlNameSpacesMap, wmsCapabilitiesElement);      
        XMLUtil.addXMLSchemaLocationsOnRootElement(schemaLocations, wmsCapabilitiesElement);
        
        return wmsCapabilitiesElement; 
    }    

    /**
     * Build the Service Element of WMS_Capabilities element
     *
     */
    private Element buildServiceElement() {

        ServiceIdentification serviceIdentification = this.owsServiceMetadata.getServiceIdentification();
        ServiceProvider serviceProvider = this.owsServiceMetadata.getServiceProvider();
        // Service
        Element serviceElement = new Element(XMLSymbols.LABEL_WMS_SERVICE);

        // Name
        Element nameElement = new Element(XMLSymbols.LABEL_WMS_NAME);
        nameElement.appendChild(KVPSymbols.WMS_SERVICE);
        serviceElement.appendChild(nameElement);

        // Title
        Element titleElement = new Element(XMLSymbols.LABEL_WMS_TITLE);
        titleElement.appendChild(serviceIdentification.getServiceTitle());
        serviceElement.appendChild(titleElement);

        // Abstract
        Element abstractElement = new Element(XMLSymbols.LABEL_WMS_ABSTRACT);
        abstractElement.appendChild(serviceIdentification.getServiceAbstract());
        serviceElement.appendChild(abstractElement);

        // KeywordList
        if (!serviceIdentification.getKeywords().isEmpty()) {
            Element keyWordsElement = this.buildKeywordListElement(serviceIdentification.getKeywords());
            serviceElement.appendChild(keyWordsElement);
        }

        // OnlineResource
        Element onlineResourceElement = new Element(XMLSymbols.LABEL_WMS_ONLINE_RESOURCE);
        Attribute providerSiteAttribute = new Attribute(XMLSymbols.PREFIX_XLINK + ":" + XMLSymbols.ATT_HREF,
                                                        XMLSymbols.NAMESPACE_XLINK,
                                                        serviceProvider.getProviderSite());
        onlineResourceElement.addAttribute(providerSiteAttribute);
        serviceElement.appendChild(onlineResourceElement);

        // ContactInformation
        Element contactInformationElement = this.buildContactInformation(serviceProvider);
        serviceElement.appendChild(contactInformationElement);

        // Fees
        if (serviceIdentification.getFees() != null) {
            Element feesElement = new Element(XMLSymbols.LABEL_WMS_FEES);
            feesElement.appendChild(serviceIdentification.getFees());
            serviceElement.appendChild(feesElement);
        }

        // AccessConstraints
        if (serviceIdentification.getAccessConstraints().size() > 0) {
            Element accessContraintsElement = new Element(XMLSymbols.LABEL_WMS_ACCESS_CONSTRAINTS);
            accessContraintsElement.appendChild(serviceIdentification.getAccessConstraints().get(0));
        }

        return serviceElement;
    }

    /**
     * Build the ContactInformation element of Service element
     *
     * @param serviceProvider
     * @return
     */
    private Element buildContactInformation(ServiceProvider serviceProvider) {
        // ContactInformation        
        Element contactInformationElement = new Element(XMLSymbols.LABEL_WMS_CONTACT_INFORMATION);

        // ContactPersonPrimary
        Element contactPersonPrimaryElement = new Element(XMLSymbols.LABEL_WMS_CONTACT_PERSON_PRIMARY);
        contactInformationElement.appendChild(contactPersonPrimaryElement);

        // ContactPerson
        Element contactPersonElement = new Element(XMLSymbols.LABEL_WMS_CONTACT_PERSON);
        contactPersonElement.appendChild(serviceProvider.getServiceContact().getIndividualName());
        contactPersonPrimaryElement.appendChild(contactPersonElement);
        // ContactOrganization
        Element contactOrganizationElement = new Element(XMLSymbols.LABEL_WMS_CONTACT_ORGANIZATION);
        contactOrganizationElement.appendChild(serviceProvider.getProviderName());
        contactPersonPrimaryElement.appendChild(contactOrganizationElement);

        // ContactPosition
        Element contactPositionElement = new Element(XMLSymbols.LABEL_WMS_CONTACT_POSITION);
        contactPositionElement.appendChild(serviceProvider.getServiceContact().getPositionName());
        contactInformationElement.appendChild(contactPositionElement);

        // ContactAddress
        Address address = serviceProvider.getServiceContact().getContactInfo().getAddress();
        Element contactAddressElement = new Element(XMLSymbols.LABEL_WMS_CONTACT_ADDRESS);
        contactInformationElement.appendChild(contactAddressElement);

        // AddressType
        Element addressTypeElement = new Element(XMLSymbols.LABEL_WMS_ADDRESS_TYPE);
        addressTypeElement.appendChild(XMLSymbols.VALUE_WMS_ADDRES_TYPE);
        contactAddressElement.appendChild(addressTypeElement);

        // Address
        Element addressElement = new Element(XMLSymbols.LABEL_WMS_ADDRESS);
        addressElement.appendChild(address.getDeliveryPoints().get(0));
        contactAddressElement.appendChild(addressElement);

        // City
        Element cityElement = new Element(XMLSymbols.LABEL_WMS_CITY);
        cityElement.appendChild(address.getCity());
        contactAddressElement.appendChild(cityElement);

        // StateOrProvince
        Element stateOrProvinceElement = new Element(XMLSymbols.LABEL_WMS_STATE_OR_PROVINCE);
        stateOrProvinceElement.appendChild(address.getCity());
        contactAddressElement.appendChild(stateOrProvinceElement);

        // PostCode
        Element postCodeElement = new Element(XMLSymbols.LABEL_WMS_POST_CODE);
        postCodeElement.appendChild(address.getPostalCode());
        contactAddressElement.appendChild(postCodeElement);

        // Country
        Element countryElement = new Element(XMLSymbols.LABEL_WMS_COUNTRY);
        countryElement.appendChild(address.getCountry());
        contactAddressElement.appendChild(countryElement);

        Phone phone = serviceProvider.getServiceContact().getContactInfo().getPhone();
        // ContactVoiceTelephone
        Element contactVoiceTelephoneElement = new Element(XMLSymbols.LABEL_WMS_CONTACT_VOICE_TELEPHONE);
        String voicePhone = phone.getVoicePhones().get(0);
        contactVoiceTelephoneElement.appendChild(voicePhone);
        contactInformationElement.appendChild(contactVoiceTelephoneElement);

        // ContactFacsimileTelephone (optional)
        if (!phone.getFacsimilePhones().isEmpty()) {
            Element contactFacsimileTelephoneElement = new Element(XMLSymbols.LABEL_WMS_CONTACT_FACSIMILE_TELEPHONE);
            String facsimilePhone = phone.getFacsimilePhones().get(0);
            contactFacsimileTelephoneElement.appendChild(facsimilePhone);
            contactInformationElement.appendChild(contactFacsimileTelephoneElement);
        }

        // ContactElectronicMailAddress
        Element contactEmailElement = new Element(XMLSymbols.LABEL_WMS_CONTACT_EMAIL);
        String email = address.getElectronicMailAddresses().get(0);
        contactEmailElement.appendChild(email);
        contactInformationElement.appendChild(contactEmailElement);

        return contactInformationElement;
    }
    
    private Element buildCapabilityElement() throws PetascopeException {
        Element capabilityElement = new Element(XMLSymbols.LABEL_WMS_CAPABILITY);
        
        Element requestElement = new Element(XMLSymbols.LABEL_WMS_REQUEST);
        capabilityElement.appendChild(requestElement);
        
        Element getCapabilitiesElement = new Element(XMLSymbols.LABEL_WMS_GET_CAPABILITIES);
        requestElement.appendChild(getCapabilitiesElement);
        
        Element formatElement = new Element(XMLSymbols.LABEL_WMS_FORMAT);
        formatElement.appendChild("text/xml");
        getCapabilitiesElement.appendChild(formatElement);
        
        // --- dcpType1 ----
        
        Element dcpTypeElement1 = new Element(XMLSymbols.LABEL_WMS_DCPTYPE);
        getCapabilitiesElement.appendChild(dcpTypeElement1);
        
        Element httpElement1 = new Element(XMLSymbols.LABEL_WMS_HTTP);
        dcpTypeElement1.appendChild(httpElement1);
        
        Element getElement1 = new Element(XMLSymbols.LABEL_WMS_GET);                
        Element postElement1 = new Element(XMLSymbols.LABEL_WMS_POST);
        httpElement1.appendChild(getElement1);
        httpElement1.appendChild(postElement1);
        
        Element onlineResourceElement1 = new Element(XMLSymbols.LABEL_WMS_ONLINE_RESOURCE);        
        Attribute xlinkHrefAttribute1 = XMLUtil.createXMLAttribute(NAMESPACE_XLINK, PREFIX_XLINK, ATT_HREF, ConfigManager.PETASCOPE_ENDPOINT_URL);
        Attribute xlinkTypeAttribute1 = XMLUtil.createXMLAttribute(NAMESPACE_XLINK, PREFIX_XLINK, ATT_TYPE, ConfigManager.PETASCOPE_ENDPOINT_URL);
        onlineResourceElement1.addAttribute(xlinkHrefAttribute1);
        onlineResourceElement1.addAttribute(xlinkTypeAttribute1);
        getElement1.appendChild(onlineResourceElement1);
        
        Element onlineResourceElement2 = new Element(XMLSymbols.LABEL_WMS_ONLINE_RESOURCE);        
        Attribute xlinkHrefAttribute2 = XMLUtil.createXMLAttribute(NAMESPACE_XLINK, PREFIX_XLINK, ATT_HREF, ConfigManager.PETASCOPE_ENDPOINT_URL);
        Attribute xlinkTypeAttribute2 = XMLUtil.createXMLAttribute(NAMESPACE_XLINK, PREFIX_XLINK, ATT_TYPE, ConfigManager.PETASCOPE_ENDPOINT_URL);
        onlineResourceElement2.addAttribute(xlinkHrefAttribute2);
        onlineResourceElement2.addAttribute(xlinkTypeAttribute2);
        postElement1.appendChild(onlineResourceElement2);
        
         
        Element getMapElement = new Element(XMLSymbols.LABEL_WMS_GET_MAP);
        requestElement.appendChild(getMapElement);
        
        List<String> formats = Arrays.asList("image/jpeg", "image/png", "image/tiff");
        for (String format : formats) {
            Element formatElementTmp = new Element(XMLSymbols.LABEL_WMS_FORMAT);
            formatElementTmp.appendChild(format);
            
            getMapElement.appendChild(formatElementTmp);
        }
        
        // --- dcpType2 ----
        
        Element dcpTypeElement2 = new Element(XMLSymbols.LABEL_WMS_DCPTYPE);
        getMapElement.appendChild(dcpTypeElement2);
        
        Element httpElement2 = new Element(XMLSymbols.LABEL_WMS_HTTP);
        dcpTypeElement2.appendChild(httpElement2);
        
        Element getElement2 = new Element(XMLSymbols.LABEL_WMS_GET);                
        Element postElement2 = new Element(XMLSymbols.LABEL_WMS_POST);
        httpElement2.appendChild(getElement2);
        httpElement2.appendChild(postElement2);
        
        Element onlineResourceElement3 = new Element(XMLSymbols.LABEL_WMS_ONLINE_RESOURCE);        
        Attribute xlinkHrefAttribute3 = XMLUtil.createXMLAttribute(NAMESPACE_XLINK, PREFIX_XLINK, ATT_HREF, ConfigManager.PETASCOPE_ENDPOINT_URL);
        Attribute xlinkTypeAttribute3 = XMLUtil.createXMLAttribute(NAMESPACE_XLINK, PREFIX_XLINK, ATT_TYPE, ConfigManager.PETASCOPE_ENDPOINT_URL);
        onlineResourceElement3.addAttribute(xlinkTypeAttribute3);
        onlineResourceElement3.addAttribute(xlinkHrefAttribute3);
        getElement2.appendChild(onlineResourceElement3);
        
        Element onlineResourceElement4 = new Element(XMLSymbols.LABEL_WMS_ONLINE_RESOURCE);        
        Attribute xlinkHrefAttribute4 = XMLUtil.createXMLAttribute(NAMESPACE_XLINK, PREFIX_XLINK, ATT_HREF, ConfigManager.PETASCOPE_ENDPOINT_URL);
        Attribute xlinkTypeAttribute4 = XMLUtil.createXMLAttribute(NAMESPACE_XLINK, PREFIX_XLINK, ATT_TYPE, ConfigManager.PETASCOPE_ENDPOINT_URL);
        onlineResourceElement4.addAttribute(xlinkHrefAttribute4);
        onlineResourceElement4.addAttribute(xlinkTypeAttribute4);
        postElement2.appendChild(onlineResourceElement4);
        
        Element exceptionElement = new Element(XMLSymbols.LABEL_WMS_EXCEPTION);
        formats = Arrays.asList("XML", "INIMAGE", "BLANK");
        for (String format : formats) {
            exceptionElement.appendChild(format);
        }
        
        Element layerElement = new Element(XMLSymbols.LABEL_WMS_LAYER);
        capabilityElement.appendChild(layerElement);
        
        Element titleElement = new Element(XMLSymbols.LABEL_WMS_TITLE);
        titleElement.appendChild("asdaman Web Map Service");
        Element abstractElement = new Element(XMLSymbols.LABEL_WMS_ABSTRACT);
        abstractElement.appendChild("A compliant implementation of WMS 1.3.0 for raster data");
        
        layerElement.appendChild(titleElement);
        layerElement.appendChild(abstractElement);
        
        this.buildLayerElements(layerElement);
        
        return capabilityElement;
        
    }
    

    /**
     * Build the list of layers element which is child element of an outer Layer
     * element
     *
     * @return
     */
    private void buildLayerElements(Element layerElement) throws WMSLayerNotExistException, PetascopeException {
        // All WMS layers
        List<Layer> layers = this.wmsRepostioryService.readAllLayersFromCaches();
        
        // to build layerElements
        for (Layer layer : layers) {
            Element layerElementTmp = this.buildLayerElement(layer);
            
            if (layerElementTmp != null) {
                layerElement.appendChild(layerElementTmp);
            }
        }
    }

    /**
     * Build a layer element
     */
    public Element buildLayerElement(Layer layer) throws PetascopeException {
        Element layerElement = new Element(XMLSymbols.LABEL_WMS_LAYER);

        // All the attributes for one layer element
        LayerAttribute layerAttribute = layer.getLayerAttribute();
        Attribute queryableAttribute = new Attribute(LayerAttribute.QUERYABLE, String.valueOf(layerAttribute.getQueryable()));
        Attribute cascadedAttribute = new Attribute(LayerAttribute.CASCADED, String.valueOf(layerAttribute.getCascaded()));
        Attribute opaqueAttribute = new Attribute(LayerAttribute.OPAQUE, String.valueOf(layerAttribute.getOpaque()));
        Attribute noSubsetsAttribute = new Attribute(LayerAttribute.NO_SUBSETS, String.valueOf(layerAttribute.getNoSubsets()));
        Attribute fixedWidthAttribute = new Attribute(LayerAttribute.FIXED_WIDTH, String.valueOf(layerAttribute.getFixedWidth()));
        Attribute fixedHeightAttribute = new Attribute(LayerAttribute.FIXED_HEIGHT, String.valueOf(layerAttribute.getFixedHeight()));
        layerElement.addAttribute(queryableAttribute);
        layerElement.addAttribute(cascadedAttribute);
        layerElement.addAttribute(opaqueAttribute);
        layerElement.addAttribute(noSubsetsAttribute);
        layerElement.addAttribute(fixedWidthAttribute);
        layerElement.addAttribute(fixedHeightAttribute);

        // Name
        Element nameElement = new Element(XMLSymbols.LABEL_WMS_NAME);

        String layerName = layer.getName(); 
        nameElement.appendChild(layerName);
        layerElement.appendChild(nameElement);

        // Title        
        Element titleElement = new Element(XMLSymbols.LABEL_WMS_TITLE);
        titleElement.appendChild(layer.getTitle());
        layerElement.appendChild(titleElement);

        // Abstract
        Element abstractElement = new Element(XMLSymbols.LABEL_WMS_ABSTRACT);
        String layerAbstract = layer.getLayerAbstract();        
        abstractElement.appendChild(layerAbstract);
        layerElement.appendChild(abstractElement);
        
        Coverage coverage = this.coverageRepositoryService.readCoverageFromLocalCache(layer.getName());
        try {
            coverage = this.coverageRepositoryService.readCoverageBasicMetadataByIdFromCache(layer.getName());
            if (coverage != null) {
                Element customizedMetadataElement = this.wcsGMLGetCapabilitiesBuild.createCustomizedCoverageMetadataElement(coverage);
                if (customizedMetadataElement != null) {
                    layerElement.appendChild(customizedMetadataElement);
                }
            }
        } catch (PetascopeException ex) {
            if (ex.getExceptionCode().equals(ExceptionCode.NoSuchCoverage)) {
                // The associated coverage with the layer was changed, log an warning instead of throwing exception
                log.warn("Coverage associated with the layer: " + layer.getName() + " doees not exist.");
                return null;
            }
        }

        // KeywordList
        if (layer.getKeywordList().size() > 0) {
            Element keywordListElement = this.buildKeywordListElement(layer.getKeywordList());
            layerElement.appendChild(keywordListElement);
        }

        // CRSs (Current only contain one native CRS of coverage for geo XY axes)
        Element crsElement = new Element(XMLSymbols.LABEL_WMS_CRS);
        crsElement.appendChild(layer.getCrss().get(0));
        layerElement.appendChild(crsElement);

        // EX_GeographicBoundingBox 
        Wgs84BoundingBox wgs84BBox = coverage.getEnvelope().getEnvelopeByAxis().getWgs84BBox();
        
        EXGeographicBoundingBox exGeographicBoundingBox = new EXGeographicBoundingBox(wgs84BBox);
        layer.setExGeographicBoundingBox(exGeographicBoundingBox);
        layerElement.appendChild(exGeographicBoundingBox.getElement());

        // BoundingBox (Current only contain one bounding box for geo XY axes)
        // NOTE: WMS 1.3 use the order from CRS (e.g: CRS:4326, order is Lat, Long, CRS:3857, order is E, N)
        // @TODO: If layer has multiple bboxes (!)
        layerElement.appendChild(layer.getBoundingBoxes().get(0).getElement());

        // Dimension (Current not support yet)
        if (layer.getDimensions().size() > 0) {
            for (Dimension dimension : layer.getDimensions()) {
                layerElement.appendChild(dimension.getElement());
            }
        }

        // Each layer contains zero or multiple styles (by default, style is as same as layer).
        if (!layer.getStyles().isEmpty()) {
            this.buildStyleElements(layerElement, layer, layer.getStyles());
        }

        return layerElement;
    }

    /**
     * Build a list of Style elements for a layer
     *
     */
    private void buildStyleElements(Element layerElement, Layer layer, List<Style> styles) throws PetascopeException {
        for (Style style : styles) {
            try {
                Element styleElement = this.getStyleElement(layer, style);
                // NOTE: styleElement can contain SLD which is a big nested XML, hence, it must keep string format to avoid problem with XML parser
                layerElement.appendChild(styleElement.toXML());
            } catch (PetascopeException ex) {
                throw new PetascopeException(ex.getExceptionCode(), 
                                            "Cannot create style element '" + style.getName() + "' of layer '" + layer.getName() + "'"
                                            + ". Reason: " + ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Return the Style XML element
     *
     */
    public Element getStyleElement(Layer layer, Style style) throws PetascopeException {
        Element styleElement = new Element(XMLSymbols.LABEL_WMS_STYLE);

        // Name
        Element nameElement = new Element(XMLSymbols.LABEL_WMS_NAME);
        nameElement.appendChild(style.getName());
        styleElement.appendChild(nameElement);

        // Title
        Element titleElement = new Element(XMLSymbols.LABEL_WMS_TITLE);
        titleElement.appendChild(style.getTitle());
        styleElement.appendChild(titleElement);

        // Abstract
        Element abstractElement = this.buildStyleAbstractElement(layer, style);
        styleElement.appendChild(abstractElement);

        if (style.getLegendURL() != null) {
            styleElement.appendChild(this.buildLegendURLElement(style));
        }

        return styleElement;
    }
    
    /**
     *
     * Build XML element for a style's abstract.
     */
    private Element buildStyleAbstractElement(Layer layer, Style style) throws PetascopeException {
        Element abstractElement = new Element(XMLSymbols.LABEL_WMS_ABSTRACT);
        
        // User's abstract for the style
        String styleAbstractStr = style.getStyleAbstract();
        
        // Rasdaman's abstract for the style (non-standard)
        String rasqlQueryFragment = style.getRasqlQueryFragment();
        String wcpsQueryFragment = style.getWcpsQueryFragment();
        Byte colorTableTypeCode = style.getColorTableType();
        String colorTableType = null;
        String colorTableDefinition = null;
        
        if (colorTableTypeCode != null) {
            colorTableType = ColorTableType.getType(colorTableTypeCode);
            colorTableDefinition = style.getColorTableDefinition();
        }
        
        Element rasdamanElement = new Element(XMLSymbols.LABEL_RASDAMAN);
        
        if (layer.isDefaultStyle(style)) {
            Element defaultStyleElement = new Element(XMLSymbols.LABEL_WMS_DEFAULT_STYLE);
            defaultStyleElement.appendChild(Boolean.TRUE.toString());
            rasdamanElement.appendChild(defaultStyleElement);
        }
        
        if (!StringUtils.isEmpty(rasqlQueryFragment)) {
            Element rasqlQueryFragmentElement = new Element(XMLSymbols.LABEL_WMS_RASQL_QUERY_FRAGMENT);
            rasqlQueryFragmentElement.appendChild(rasqlQueryFragment);
            rasdamanElement.appendChild(rasqlQueryFragmentElement);
        }
        
        if (!StringUtils.isEmpty(wcpsQueryFragment)) {
            Element wcpsQueryFragmentElement = new Element(XMLSymbols.LABEL_WMS_WCPS_QUERY_FRAGMENT);
            wcpsQueryFragmentElement.appendChild(wcpsQueryFragment);
            rasdamanElement.appendChild(wcpsQueryFragmentElement);
        }
        
        Element colorTableElement = new Element(XMLSymbols.LABEL_WMS_COLOR_TABLE);
        
        if (!StringUtils.isEmpty(colorTableType)) {
            Element colorTableTypeElement = new Element(XMLSymbols.LABEL_WMS_COLOR_TABLE_TYPE);
            colorTableTypeElement.appendChild(colorTableType);
            colorTableElement.appendChild(colorTableTypeElement);
        
            Element colorTableDefinitionElement = new Element(XMLSymbols.LABEL_WMS_COLOR_TABLE_DEFINITION);
            
            if (colorTableType.toLowerCase().equals(ColorTableType.SLD.toString().toLowerCase())) {
                // NOTE: SLD is another nested XML element
                colorTableDefinitionElement.appendChild(XMLUtil.parseXmlFragmentWithoutReplacingAmpersand(colorTableDefinition));
            } else {
                colorTableDefinitionElement.appendChild(colorTableDefinition);
            }
            
            colorTableElement.appendChild(colorTableDefinitionElement);
        }
        
        if (colorTableElement.getChildCount() > 0) {
            rasdamanElement.appendChild(colorTableElement);
        }
        
        String content = styleAbstractStr + "\n" + XMLUtil.enquoteCDATA(rasdamanElement.toXML());
        abstractElement.appendChild(content);
        
        return abstractElement;
    }
    

    /**
     * Build a KeywordList element from list of keywords
     */
    public Element buildKeywordListElement(List<String> keywords) {
        Element keywordsElement = new Element(XMLSymbols.LABEL_KEYWORDS);
        // keyWords element can contain multiple KeyWord elements
        for (String keyWord : keywords) {
            Element keyWordElement = new Element(XMLSymbols.LABEL_KEYWORD);
            keyWordElement.appendChild(keyWord);

            keywordsElement.appendChild(keyWordElement);
        }

        return keywordsElement;
    }
    
     /**
     * If a style has legendURL then it shows in WMS GetCapabilities
     */
    private Element buildLegendURLElement(Style style) {
        LegendURL legendURL = style.getLegendURL();
        
        Element legendURLElement = new Element(XMLSymbols.LABEL_WMS_LEGEND_URL);
        
        // Format (required)
        Element formatElement = new Element(XMLSymbols.LABEL_WMS_FORMAT);
        formatElement.appendChild(legendURL.getFormat());
        legendURLElement.appendChild(formatElement);
        
        // OnlineResource (required)
        Element onlineResourceElement = new Element(XMLSymbols.LABEL_WMS_ONLINE_RESOURCE);
        
        // e.g. https://localhost:8080/rasdaman/ows?service=WMS&request=GetLegendGraphic&format=image%2Fpng&width=20&height=20&layer=ne%3Ane
        String getLegendGraphicURL = StringUtil.escapeAmpersands(legendURL.getOnlineResourceURL());
        // xlink:href="URL"
        Attribute hrefAttribute = new Attribute(XMLSymbols.PREFIX_XLINK + ":" + XMLSymbols.ATT_HREF,
                                                XMLSymbols.NAMESPACE_XLINK, getLegendGraphicURL);
        onlineResourceElement.addAttribute(hrefAttribute);
        legendURLElement.appendChild(onlineResourceElement);
        
        return legendURLElement;
    }

}
