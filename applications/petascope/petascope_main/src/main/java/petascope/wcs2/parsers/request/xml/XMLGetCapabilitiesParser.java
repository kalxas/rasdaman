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
package petascope.wcs2.parsers.request.xml;

import java.util.Map;
import nu.xom.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.core.XMLSymbols;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.XMLUtil;

/**
 * Parse a WCS GetCapabilities from request body in XML to map of keys, values
 * as KVP request
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class XMLGetCapabilitiesParser extends XMLAbstractParser {

    @Override
    public Map<String, String[]> parse(String requestBody) throws WCSException, PetascopeException {
        // Only used when xml_validation=true in petascope.properties
        this.validateXMLRequestBody(requestBody);

        Element rootElement = XMLUtil.parseInput(requestBody);
        kvpParameters.put(KVPSymbols.KEY_SERVICE, new String[]{KVPSymbols.WCS_SERVICE});
        kvpParameters.put(KVPSymbols.KEY_REQUEST, new String[]{KVPSymbols.VALUE_GET_CAPABILITIES});

        // parse version from WCS request body
        this.parseVersion(rootElement);

        return kvpParameters;
    }

    /**
     * Parse the AcceptVersions element to get the requested version
     *
     * @param rootElement
     */
    private void parseVersion(Element rootElement) throws WCSException {
        // NOTE: Only GetCapabilities in XML POST has the AcceptedVersions element, e.g:
//      <ows:AcceptVersions>
//          <ows:Version>2.0.1</ows:Version>
//      </ows:AcceptVersions>
        Element acceptVersionsElement = XMLUtil.firstChildRecursive(rootElement, XMLSymbols.OWS_LABEL_ACCEPT_VERSIONS);
        // e.g: 2.0,2.0.1
        String version = XMLUtil.childrenToString(acceptVersionsElement);

        if (version == null) {
            throw new WCSException(ExceptionCode.InvalidRequest, "Missing element: " + XMLSymbols.OWS_LABEL_ACCEPT_VERSIONS + " in request body.");
        } else {            
            this.validateRequestVersion(version);
        }

        kvpParameters.put(KVPSymbols.KEY_VERSION, new String[]{version});
    }
}
