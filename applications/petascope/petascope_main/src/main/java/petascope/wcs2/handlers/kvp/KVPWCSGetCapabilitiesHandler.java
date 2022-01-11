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
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import nu.xom.Element;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.config.VersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.KEY_ACCEPTLANGUAGES;
import static petascope.core.KVPSymbols.KEY_ACCEPTVERSIONS;
import static petascope.core.KVPSymbols.KEY_REQUEST;
import static petascope.core.KVPSymbols.KEY_SECTIONS;
import static petascope.core.KVPSymbols.KEY_SERVICE;
import static petascope.core.KVPSymbols.KEY_VERSION;
import petascope.util.MIMEUtil;
import petascope.util.XMLUtil;
import petascope.core.gml.GMLWCSRequestResultBuilder;
import petascope.exceptions.WMSException;
import petascope.util.SetUtil;

/**
 * Handle the GetCapabilities WCS 2.0.1 request result example which is
 * validated with WCS 2.0.1 schema, see: https://pastebin.com/QUe4DKfg
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWCSGetCapabilitiesHandler extends KVPWCSAbstractHandler {

    private static Logger log = LoggerFactory.getLogger(KVPWCSGetCapabilitiesHandler.class);
    protected static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_SERVICE, KEY_VERSION, KEY_REQUEST, KEY_ACCEPTVERSIONS, KEY_ACCEPTLANGUAGES, KEY_SECTIONS);
    
    @Autowired
    private GMLWCSRequestResultBuilder gmlWCSRequestResultBuilder;
    @Autowired
    private HttpServletRequest httpServletRequest;

    public KVPWCSGetCapabilitiesHandler() {

    }

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        this.validateParameters(kvpParameters, VALID_PARAMETERS);
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
        
        String gml = "";

        // The first supported version will return GetCapabilities result (e.g: acceptVersions=2.0.1,2.1) then it returns 2.0.1 GetCapabilities
        String[] versions = kvpParameters.get(KEY_VERSION);        
        for (String version : versions) {
            if (VersionManager.getAllSupportedVersions(KVPSymbols.WCS_SERVICE).contains(version)) {
                Element capabilitiesElement = this.gmlWCSRequestResultBuilder.buildGetCapabilitiesResult(version);

                // format XML to have indentation
                gml = XMLUtil.formatXML(capabilitiesElement.toXML());
                
                break;
            }
        }

        // GetCapabilities only returns 1 XML string                
        return new Response(Arrays.asList(gml.getBytes()), MIMEUtil.MIME_GML);
    }
}
