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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nu.xom.Attribute;
import nu.xom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.core.XMLSymbols.ATT_SCHEMA_LOCATION;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.NAMESPACE_OWS;
import static petascope.core.XMLSymbols.NAMESPACE_SWE;
import static petascope.core.XMLSymbols.NAMESPACE_XLINK;
import static petascope.core.XMLSymbols.NAMESPACE_XSI;
import static petascope.core.XMLSymbols.PREFIX_GML;
import static petascope.core.XMLSymbols.PREFIX_OWS;
import static petascope.core.XMLSymbols.PREFIX_SWE;
import static petascope.core.XMLSymbols.PREFIX_XLINK;
import static petascope.core.XMLSymbols.PREFIX_XSI;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.XMLUtil;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Build GML result for WCS requests (DescribeCoverage, GetCoverage) in
 * application/gml+xml.
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class GMLWCSRequestResultBuilder {
    
    @Autowired
    private GMLGetCapabilitiesBuilder gmlGetCapabilitiesBuilder;
    @Autowired
    private GMLDescribeCoverageBuilder gmlDescribeCoverageBuilder;
    @Autowired
    private GMLGetCoverageBuilder gmlGetCoverageBuilder;
    
    /**
     * These XML prefixes and namespaces are always existing in root element of GML result.
     * prefix -> namespace
     */
    public static Map<String, String> getMandatoryXMLNameSpacesMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(PREFIX_GML, NAMESPACE_GML);        
        map.put(PREFIX_SWE, NAMESPACE_SWE);
        map.put(PREFIX_OWS, NAMESPACE_OWS);
        map.put(PREFIX_XLINK, NAMESPACE_XLINK);
        map.put(PREFIX_XSI, NAMESPACE_XSI);        

        return map;
    }
    
    /**
     * Build result for WCS GetCapabilities request.
     */
    public Element buildGetCapabilitiesResult(String version) throws PetascopeException, SecoreException {
        Element rootElement = this.gmlGetCapabilitiesBuilder.buildWCSGetCapabilitiesResult(version);
        
        return rootElement;
    }
    
    /**
     * Build result for WCS DescribeCoverage with result in GML (it can be multiple coverageIds)
     */
    public Element buildDescribeCoverageResult(String outputType, List<String> coverageIds) throws PetascopeException, SecoreException {
        Element rootElement = this.gmlDescribeCoverageBuilder.buildWCSDescribeCoverageResult(outputType, coverageIds);
        
        return rootElement;
    }
    
    /**
     * Build result for WCS GetCoverage with result in GML (it can be only 1 coverage)
     */
    public Element buildGetCoverageResult(WcpsCoverageMetadata wcpsCoverageMetadata, String pixelValues) throws PetascopeException, SecoreException {
        Element rootElement = this.gmlGetCoverageBuilder.buildWCSGetCoverageResult(wcpsCoverageMetadata, pixelValues);
        
        return rootElement;
    }
}
