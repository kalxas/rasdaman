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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nu.xom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.core.KVPSymbols.VALUE_GENERAL_GRID_COVERAGE;
import petascope.core.gml.cis10.GMLCoreCIS10;
import petascope.core.gml.cis10.GMLCoreCIS10Builder;
import petascope.core.gml.cis10.GMLCIS10DescribeCoverage;
import static petascope.core.XMLSymbols.LABEL_COVERAGE_DESCRIPTIONS;
import static petascope.core.XMLSymbols.LABEL_GENERAL_GRID_COVERAGE;
import static petascope.core.XMLSymbols.LABEL_GRID_COVERAGE;
import static petascope.core.XMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE;
import static petascope.core.XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE;
import static petascope.core.XMLSymbols.NAMESPACE_GMLCOV;
import static petascope.core.XMLSymbols.NAMESPACE_GMLRGRID;
import static petascope.core.XMLSymbols.PREFIX_GMLRGRID;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.XMLUtil;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
import static petascope.core.XMLSymbols.PREFIX_CIS11;
import static petascope.core.XMLSymbols.PREFIX_GMLCOV;
import static petascope.core.XMLSymbols.PREFIX_WCS;
import petascope.core.gml.cis.AbstractGMLCISDescribeCoverage;
import petascope.core.gml.cis11.GMLCIS11DescribeCoverage;
import petascope.core.gml.cis11.GMLCoreCIS11;
import petascope.core.gml.cis11.GMLCoreCIS11Builder;
import static petascope.core.XMLSymbols.SCHEMA_LOCATION_WCS_CIS_10_COVERAGE_DESCRIBE_COVERAGE;
import static petascope.core.XMLSymbols.SCHEMA_LOCATION_WCS_CIS_10_REFERENCEABLE_COVERAGE_DESCRIBE_COVERAGE;
import static petascope.core.XMLSymbols.SCHEMA_LOCATION_WCS_CIS_11_DESCRIBE_COVERAGE;
import static petascope.core.XMLSymbols.NAMESPACE_WCS_20;
import static petascope.core.XMLSymbols.NAMESPACE_WCS_21;
import static petascope.core.XMLSymbols.NAMESPACE_CIS_11;

/**
 * Build GMLDescribeCoverage object as result of WCS DescribeCoverage request.
 * NOTE: it can handle multiple coverageIds (CIS1.0, CIS1.1) together.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class GMLDescribeCoverageBuilder {
    
    @Autowired
    private GMLCoreCIS10Builder gmlCoreCIS10Builder;
    @Autowired
    private GMLCoreCIS11Builder gmlCoreCIS11Builder;
    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;
    
    /**
     * Build GMLDescribeCoverage for 1 CIS 1.0 / CIS 1.1 coverage
     */
    private AbstractGMLCISDescribeCoverage buildGMLCISDescribeCoverage(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        String coverageId = wcpsCoverageMetadata.getCoverageName();
        String coverageType = wcpsCoverageMetadata.getCoverageType();
        
        AbstractGMLCISDescribeCoverage gmlCISDescribeCoverage; 
        
        if (isCIS11(coverageType)) {
            // CIS 1.1
            GMLCoreCIS11 gmlCore = this.gmlCoreCIS11Builder.build(wcpsCoverageMetadata);
            gmlCISDescribeCoverage = new GMLCIS11DescribeCoverage(coverageId, coverageType, gmlCore);            
        } else {
            // CIS 1.0
            GMLCoreCIS10 gmlCore = this.gmlCoreCIS10Builder.build(wcpsCoverageMetadata);
            gmlCISDescribeCoverage = new GMLCIS10DescribeCoverage(coverageId, coverageType, gmlCore);
        }
        
        return gmlCISDescribeCoverage;
    }
    
    public static boolean isCIS11(String coverageType) {
        return coverageType.equals(LABEL_GENERAL_GRID_COVERAGE);
    }
    
    /**
     * From the list of input coverageIds, build the result for DescribeCoverage
     * request.
     */
    public Element buildWCSDescribeCoverageResult(String outputType, List<String> coverageIds) throws PetascopeException, SecoreException {

        Element coverageDescriptionsElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_COVERAGE_DESCRIPTIONS), NAMESPACE_WCS_20);
        Map<String, String> xmlNameSpacesMap = GMLWCSRequestResultBuilder.getMandatoryXMLNameSpacesMap();
        Set<String> schemaLocations = new LinkedHashSet<>();
        
        boolean hasCIS11Coverage = false;

        for (String coverageId : coverageIds) {
            WcpsCoverageMetadata wcpsCoverageMetadata = this.wcpsCoverageMetadataTranslator.translate(coverageId);
            
            // Transform all coverages CIS 1.0 to CIS 1.1 output
            if (outputType != null && outputType.equals(VALUE_GENERAL_GRID_COVERAGE)) {
                wcpsCoverageMetadata.setCoverageType(VALUE_GENERAL_GRID_COVERAGE);
            }
            
            AbstractGMLCISDescribeCoverage gmlDescribeCoverage = this.buildGMLCISDescribeCoverage(wcpsCoverageMetadata);

            Element coverageDescriptionElement = gmlDescribeCoverage.serializeToXMLElement();
            coverageDescriptionsElement.appendChild(coverageDescriptionElement);
            
            String coverageType = wcpsCoverageMetadata.getCoverageType();
            
            // CIS 1.0
            if (coverageType.equals(LABEL_GRID_COVERAGE) || coverageType.equals(LABEL_RECTIFIED_GRID_COVERAGE)) {
                schemaLocations.add(SCHEMA_LOCATION_WCS_CIS_10_COVERAGE_DESCRIBE_COVERAGE);                
            } else if (coverageType.equals(LABEL_REFERENCEABLE_GRID_COVERAGE)) {
                schemaLocations.add(SCHEMA_LOCATION_WCS_CIS_10_REFERENCEABLE_COVERAGE_DESCRIBE_COVERAGE);
                xmlNameSpacesMap.put(PREFIX_GMLRGRID, NAMESPACE_GMLRGRID);
            }            
            // CIS 1.1
            else if (coverageType.equals(LABEL_GENERAL_GRID_COVERAGE)) {                
                hasCIS11Coverage = true;
            }
        }
        
        // NOTE: if DescribeCoverage with mixed CIS coverage types, it needs to use the namespace from the latest one (CIS 1.1).
        if (hasCIS11Coverage) {
            // CIS 1.1
            schemaLocations.add(SCHEMA_LOCATION_WCS_CIS_11_DESCRIBE_COVERAGE);
            xmlNameSpacesMap.put(PREFIX_CIS11, NAMESPACE_CIS_11);
            
            coverageDescriptionsElement.setNamespaceURI(NAMESPACE_WCS_21);
        } else {
            // CIS 1.0
            xmlNameSpacesMap.put(PREFIX_GMLCOV, NAMESPACE_GMLCOV);
        }
        
        XMLUtil.addXMLNameSpacesOnRootElement(xmlNameSpacesMap, coverageDescriptionsElement);
        XMLUtil.addXMLSchemaLocationsOnRootElement(schemaLocations, coverageDescriptionsElement);

        return coverageDescriptionsElement;
    }
    
}
