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
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wmts.handlers.kvp;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import nu.xom.Attribute;
import nu.xom.Element;
import org.rasdaman.config.VersionManager;
import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.rasdaman.repository.service.OWSMetadataRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.PetascopeController;
import petascope.core.XMLSymbols;
import static petascope.core.XMLSymbols.LABEL_VERSION;
import static petascope.core.XMLSymbols.LABEL_WMTS_CAPABILITIES;
import static petascope.core.XMLSymbols.NAMESPACE_OWS_11;
import static petascope.core.XMLSymbols.NAMESPACE_WMTS;
import petascope.core.gml.GMLGetCapabilitiesBuilder;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WMSException;
import petascope.util.MIMEUtil;
import petascope.util.XMLUtil;
import petascope.wmts.handlers.service.WMTSGetCapabilitiesService;

/**
 * Handler for WMTS GetCapabilities service
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class KVPWMTSGetCapabilitiesHandler extends KVPWMTSAbstractHandler {
    
    private static Logger log = LoggerFactory.getLogger(KVPWMTSGetCapabilitiesHandler.class);
    
    @Autowired
    private GMLGetCapabilitiesBuilder gmlGetCapabilitiesBuilder;
    @Autowired
    private WMTSGetCapabilitiesService wmtsGetCapabilitiesService;
    @Autowired
    private OWSMetadataRepostioryService persistedOwsServiceMetadataService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private PetascopeController petascopeController;
    
    @Override
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
    }
        
    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException, Exception {

        OwsServiceMetadata owsServiceMetadata = this.persistedOwsServiceMetadataService.read();
        
        
        Element capabilitiesElement = new Element(LABEL_WMTS_CAPABILITIES, NAMESPACE_WMTS);
        Attribute versionAttribute = new Attribute(LABEL_VERSION, VersionManager.WMTS_VERSION_10);
        capabilitiesElement.addAttribute(versionAttribute);
        
        Element serviceIdentificationElement = this.wmtsGetCapabilitiesService.buildServiceIdentificationElement(owsServiceMetadata);
        Element serviceProviderElement = this.gmlGetCapabilitiesBuilder.buildServiceProvider(owsServiceMetadata, NAMESPACE_OWS_11);
        
        Element operationsMetadataElement = this.wmtsGetCapabilitiesService.buildOperationsMetadataElement();
        Element contentsElement = this.wmtsGetCapabilitiesService.buildContentsElement();
        
        capabilitiesElement.appendChild(serviceIdentificationElement);
        capabilitiesElement.appendChild(serviceProviderElement);
        capabilitiesElement.appendChild(operationsMetadataElement);
        capabilitiesElement.appendChild(contentsElement);
        
        Map<String, String> xmlNameSpacesMap = new LinkedHashMap<>();
        
        // Adding some specific XML namespaces of only WMTS GetCapabilities request
        xmlNameSpacesMap.put(null, NAMESPACE_WMTS);
        xmlNameSpacesMap.put(XMLSymbols.PREFIX_OWS, XMLSymbols.NAMESPACE_OWS_11);
        xmlNameSpacesMap.put(XMLSymbols.PREFIX_XLINK, XMLSymbols.NAMESPACE_XLINK);
        
        xmlNameSpacesMap.put(XMLSymbols.PREFIX_GML, XMLSymbols.NAMESPACE_GML_WMTS);
        
        Set<String> schemaLocations = new LinkedHashSet<>();
        schemaLocations.add(XMLSymbols.SCHEMA_LOCATION_WMTS);
        
        XMLUtil.addXMLNameSpacesOnRootElement(xmlNameSpacesMap, capabilitiesElement);      
        XMLUtil.addXMLSchemaLocationsOnRootElement(schemaLocations, capabilitiesElement);        
        
        String result = XMLUtil.formatXML(capabilitiesElement); 

        return new Response(Arrays.asList(result.getBytes()), MIMEUtil.MIME_GML);
    }
    
}
