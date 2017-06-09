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
package petascope.controller.handler.service;

import java.io.IOException;
import java.util.Map;
import org.rasdaman.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.wcs2.handlers.kvp.KVPWCSDescribeCoverageHandler;
import petascope.wcs2.handlers.kvp.KVPWCSGetCapabilitiesHandler;
import petascope.core.response.Response;
import petascope.core.XMLSymbols;
import petascope.exceptions.WMSException;
import petascope.wcs2.handlers.kvp.KVPWCSGetCoverageHandler;
import petascope.wcs2.handlers.kvp.KVPWCSProcessCoverageHandler;
import petascope.wcs2.parsers.request.xml.XMLDescribeCoverageParser;
import petascope.wcs2.parsers.request.xml.XMLGetCapabilitiesParser;
import petascope.wcs2.parsers.request.xml.XMLGetCoverageParser;
import petascope.wcs2.parsers.request.xml.XMLProcessCoverageParser;

/**
 * Service class to handle WCS request in POST XML
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class XMLWCSServiceHandler extends AbstractHandler {

    Logger log = LoggerFactory.getLogger(XMLWCSServiceHandler.class);

    // Parsers
    @Autowired
    private XMLGetCapabilitiesParser getCapabilitiesXMLParser;
    @Autowired
    private XMLDescribeCoverageParser describeCoverageXMLParser;
    @Autowired
    private XMLGetCoverageParser getCoverageXMLParser;
    @Autowired
    private XMLProcessCoverageParser processCoverageXMLParser;

    // Handlers
    @Autowired
    private KVPWCSGetCapabilitiesHandler getCapabilitiesHandler;
    @Autowired
    private KVPWCSDescribeCoverageHandler describeCoverageHandler;
    @Autowired
    private KVPWCSGetCoverageHandler getCoverageHandler;
    @Autowired
    private KVPWCSProcessCoverageHandler processCoveragKVPHandler;

    public XMLWCSServiceHandler() {
        // XML WCS is a part of WCS2
        service = KVPSymbols.WCS_SERVICE;
        version = ConfigManager.WCS_VERSIONS;
        
        requestServices.add(KVPSymbols.VALUE_REQUEST_WCS_XML);
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws WCSException, IOException, PetascopeException, SecoreException, WMSException {
        // Here it will need to parse the XML document and build a WCS query with query String in KVP
        String requestBody = kvpParameters.get(KVPSymbols.KEY_REQUEST_BODY)[0];
        Response response = null;

        Map<String, String[]> parsedKvpParameters = null;
        // GetCapabilities
        if (requestBody.contains(XMLSymbols.LABEL_GET_CAPABILITIES)) {
            parsedKvpParameters = getCapabilitiesXMLParser.parse(requestBody);
            response = getCapabilitiesHandler.handle(parsedKvpParameters);
        } else if (requestBody.contains(XMLSymbols.LABEL_DESCRIBE_COVERAGE)) {
            // DescribeCoverage
            parsedKvpParameters = describeCoverageXMLParser.parse(requestBody);
            response = describeCoverageHandler.handle(parsedKvpParameters);
        } else if (requestBody.contains(XMLSymbols.LABEL_GET_COVERAGE)) {
            // GetCoverage
            parsedKvpParameters = getCoverageXMLParser.parse(requestBody);
            response = getCoverageHandler.handle(parsedKvpParameters);
        } else if (requestBody.contains(XMLSymbols.LABEL_WCPS_QUERY)) {
            // ProcessCoverages
            // NOTE: There are 3 types of WCPS (XML in xml syntax, text in abstractSyntax and text in OGC WCPS POST), but all of them contains the <query> element
            parsedKvpParameters = processCoverageXMLParser.parse(requestBody);
            response = processCoveragKVPHandler.handle(parsedKvpParameters);
        }

        return response;
    }
}
