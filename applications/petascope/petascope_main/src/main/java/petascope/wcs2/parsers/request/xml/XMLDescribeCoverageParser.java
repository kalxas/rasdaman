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
package petascope.wcs2.parsers.request.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nu.xom.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.KEY_COVERAGEID;
import petascope.util.ListUtil;
import petascope.core.XMLSymbols;
import petascope.exceptions.PetascopeException;
import petascope.util.XMLUtil;

/**
 * Parse a WCS DescribeCoverage from request body in XML to map of keys, values
 * as KVP request
 *
 @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class XMLDescribeCoverageParser extends XMLAbstractParser {   

    @Override
    public Map<String, String[]> parse(String requestBody) throws WCSException, PetascopeException {        
        // Only used when xml_validation=true in petascope.properties
        this.validateXMLRequestBody(requestBody);

        Element rootElement = XMLUtil.parseInput(requestBody);
        // e.g: <wcs:DescribeCoverage ... version="2.0.1">...</wcs:DescribeCoverage>        
        String version = rootElement.getAttributeValue(XMLSymbols.ATT_VERSION);
        this.validateRequestVersion(version);
        
        kvpParameters.put(KVPSymbols.KEY_SERVICE, new String[]{KVPSymbols.WCS_SERVICE});
        kvpParameters.put(KVPSymbols.KEY_VERSION, new String[]{version});
        kvpParameters.put(KVPSymbols.KEY_REQUEST, new String[]{KVPSymbols.VALUE_DESCRIBE_COVERAGE});
        
        // parse coverageId(s) from WCS request body
        this.parseCoverageId(rootElement);

        return kvpParameters;
    }

    /**
     * Parse the coverageId(s) from WCS request body
     * @param rootElement
     * @throws WCSException 
     */
    private void parseCoverageId(Element rootElement) throws WCSException {

        // Describe Coverage can contain multiple coverageIds in XML request, e.g:
//    <wcs:CoverageId>BGS_EMODNET_CentralMed-MCol</wcs:CoverageId>
//    <wcs:CoverageId>BGS_EMODNET_CentralMed-MCol</wcs:CoverageId>
//    <wcs:CoverageId>BGS_EMODNET_BayBiscayIberianCoast-MCol</wcs:CoverageId>        
        // The result is:
//        <wcs:CoverageDescriptions>
//          <wcs:CoverageDescription gml:id="test_mr">...</wcs:CoverageDescription>
//          <wcs:CoverageDescription gml:id="test_rgb">...</wcs:CoverageDescription>
//        </wcs:CoverageDescriptions>
        List<Element> coverageIdElements = XMLUtil.getChildElements(rootElement, XMLSymbols.LABEL_COVERAGE_ID);
        if (coverageIdElements.isEmpty()) {
            throw new WCSException(ExceptionCode.InvalidRequest, "A DescribeCoverage request must specify at least one " + KEY_COVERAGEID + ".");
        }

        List<String> coverageIds = new ArrayList<>();
        for (Element coverageIdElement : coverageIdElements) {
            // e.g: <wcs:CoverageId>test_mr</wcs:CoverageId> return test_mr
            coverageIds.add(XMLUtil.getText(coverageIdElement));
        }

        String coverageId = ListUtil.join(coverageIds, ",");
        kvpParameters.put(KVPSymbols.KEY_COVERAGEID, new String[]{coverageId});
    }
}
