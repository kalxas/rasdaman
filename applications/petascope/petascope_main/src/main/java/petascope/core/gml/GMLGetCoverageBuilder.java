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
import java.util.Map;
import java.util.Set;
import nu.xom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.core.XMLSymbols.LABEL_GENERAL_GRID_COVERAGE;
import static petascope.core.XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE;
import static petascope.core.XMLSymbols.NAMESPACE_GMLCOV;
import static petascope.core.XMLSymbols.PREFIX_CIS11;
import static petascope.core.XMLSymbols.PREFIX_GMLCOV;
import petascope.core.gml.cis10.GMLCoreCIS10;
import petascope.core.gml.cis10.GMLCoreCIS10Builder;
import petascope.core.gml.cis10.GMLCIS10GetCoverage;
import petascope.core.gml.cis10.model.rangeset.DataBlockCIS10;
import petascope.core.gml.cis10.model.rangeset.RangeSetCIS10;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.core.gml.cis.AbstractGMLCISGetCoverage;
import petascope.core.gml.cis11.GMLCIS11GetCoverage;
import petascope.core.gml.cis11.GMLCoreCIS11;
import petascope.core.gml.cis11.GMLCoreCIS11Builder;
import petascope.core.gml.cis11.model.rangeset.DataBlockCIS11;
import petascope.core.gml.cis11.model.rangeset.RangeSetCIS11;
import static petascope.core.XMLSymbols.SCHEMA_LOCATION_WCS_CIS_11_GET_COVERAGE;
import static petascope.core.XMLSymbols.NAMESPACE_CIS_11;
import static petascope.core.XMLSymbols.NAMESPACE_GMLRGRID;
import static petascope.core.XMLSymbols.PREFIX_GMLRGRID;
import petascope.util.XMLUtil;
import static petascope.core.gml.GMLDescribeCoverageBuilder.isCIS11;

/**
 * Build GMLGetCoverage object as result of WCS GetCoverage request in GML.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class GMLGetCoverageBuilder {
    
    @Autowired
    private GMLCoreCIS10Builder gmlCoreCIS10Builder;
    @Autowired
    private GMLCoreCIS11Builder gmlCoreCIS11Builder;
    
    /**
     * Build RangeSet element based on the values from Rasql collection.
     */
    private RangeSetCIS10 buildRangeSetCIS10(String pixelValues) {
        DataBlockCIS10 dataBlock = new DataBlockCIS10(pixelValues);
        
        RangeSetCIS10 rangeSet = new RangeSetCIS10(dataBlock);
        return rangeSet;
    }
    
    /**
     * Build RangeSet element based on the values from Rasql collection.
     */
    private RangeSetCIS11 buildRangeSetCIS11(String pixelValues) {
        DataBlockCIS11 dataBlock = new DataBlockCIS11(pixelValues);
        
        RangeSetCIS11 rangeSet = new RangeSetCIS11(dataBlock);
        return rangeSet;
    }
    
    /**
     * Build GMLDescribeCoverage for 1 coverage.
     */
    private AbstractGMLCISGetCoverage buildGMLGetCoverage(WcpsCoverageMetadata wcpsCoverageMetadata, String pixelValues) throws PetascopeException {
        String coverageId = wcpsCoverageMetadata.getCoverageName();
        String coverageType = wcpsCoverageMetadata.getCoverageType();
        
        AbstractGMLCISGetCoverage gmlGetCoverage;
        
        if (isCIS11(coverageType)) {
            // CIS 1.1
            RangeSetCIS11 rangeSet = null; 
            if (pixelValues != null) {
                rangeSet = this.buildRangeSetCIS11(pixelValues);
            }
            GMLCoreCIS11 gmlCore = this.gmlCoreCIS11Builder.build(wcpsCoverageMetadata);        
            gmlGetCoverage = new GMLCIS11GetCoverage(coverageId, coverageType, gmlCore, rangeSet);
        } else {
            // CIS 1.0
            RangeSetCIS10 rangeSet = null;
            if (pixelValues != null) {
                rangeSet = this.buildRangeSetCIS10(pixelValues);
            }
            GMLCoreCIS10 gmlCore = this.gmlCoreCIS10Builder.build(wcpsCoverageMetadata);        
            gmlGetCoverage = new GMLCIS10GetCoverage(coverageId, coverageType, gmlCore, rangeSet);
        }
        
        return gmlGetCoverage;
    }

    /**
     * Build result for GetCoverage request in GML format
     */
    public Element buildWCSGetCoverageResult(WcpsCoverageMetadata wcpsCoverageMetadata, String pixelValues) throws PetascopeException, SecoreException {

        Map<String, String> xmlNameSpacesMap = GMLWCSRequestResultBuilder.getMandatoryXMLNameSpacesMap();
        
        Set<String> schemaLocations = new LinkedHashSet<>();
        String coverageType = wcpsCoverageMetadata.getCoverageType();
        
        if (isCIS11(coverageType)) {
            // CIS 1.1
            xmlNameSpacesMap.put(PREFIX_CIS11, NAMESPACE_CIS_11);
            schemaLocations.add(SCHEMA_LOCATION_WCS_CIS_11_GET_COVERAGE);
        } else {
            // CIS 1.0
            xmlNameSpacesMap.put(PREFIX_GMLCOV, NAMESPACE_GMLCOV);
            
            if (coverageType.equals(LABEL_REFERENCEABLE_GRID_COVERAGE)) {
                xmlNameSpacesMap.put(PREFIX_GMLRGRID, NAMESPACE_GMLRGRID);
            }
        }
        
        AbstractGMLCISGetCoverage gmlGetCoverage = this.buildGMLGetCoverage(wcpsCoverageMetadata, pixelValues);

        Element coverageTypeElement = gmlGetCoverage.serializeToXMLElement();

        XMLUtil.addXMLNameSpacesOnRootElement(xmlNameSpacesMap, coverageTypeElement);        
        XMLUtil.addXMLSchemaLocationsOnRootElement(schemaLocations, coverageTypeElement);
        
        return coverageTypeElement;
    }
    
}
