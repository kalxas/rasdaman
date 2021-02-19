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
package petascope.core.gml.cis10;

import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_ID;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.NAMESPACE_GMLCOV;
import static petascope.core.XMLSymbols.PREFIX_GML;
import static petascope.core.XMLSymbols.PREFIX_GMLCOV;
import petascope.core.gml.cis.AbstractGMLCISGetCoverage;
import petascope.core.gml.cis10.model.rangeset.RangeSetCIS10;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.XMLUtil;

/**
 * Class to build result for WCS GetCoverage request in GML. 
 * e.g: http://schemas.opengis.net/cis/1.1/gml/examples-1.0/exampleRectifiedGridCoverage-2.xml
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class GMLCIS10GetCoverage extends AbstractGMLCISGetCoverage {

    private String coverageId;
    private String coverageType;
    private GMLCoreCIS10 gmlCore;
    private RangeSetCIS10 rangeSet;

    public GMLCIS10GetCoverage(String coverageId, String coverageType, GMLCoreCIS10 gmlCore, RangeSetCIS10 rangeSet) {
        this.coverageId = coverageId;
        this.coverageType = coverageType;
        this.gmlCore = gmlCore;
        this.rangeSet = rangeSet;
    }

    public String getCoverageId() {
        return coverageId;
    }

    public void setCoverageId(String coverageId) {
        this.coverageId = coverageId;
    }

    public String getCoverageType() {
        return coverageType;
    }

    public void setCoverageType(String coverageType) {
        this.coverageType = coverageType;
    }

    public GMLCoreCIS10 getGmlCore() {
        return gmlCore;
    }

    public void setGmlCore(GMLCoreCIS10 gmlCore) {
        this.gmlCore = gmlCore;
    }

    public RangeSetCIS10 getRangeSet() {
        return rangeSet;
    }

    public void setRangeSet(RangeSetCIS10 rangeSet) {
        this.rangeSet = rangeSet;
    }

    @Override
    public Element serializeToXMLElement() throws PetascopeException {
        
        // <gmlcov:COVERAGE_TYPE>
        Element coverageTypeElement = new Element(XMLUtil.createXMLLabel(PREFIX_GMLCOV, this.coverageType), NAMESPACE_GMLCOV);
        Attribute attributeId = XMLUtil.createXMLAttribute(NAMESPACE_GML, PREFIX_GML, ATT_ID, this.coverageId);
        coverageTypeElement.addAttribute(attributeId);
        
        // <gml:boundedBy>
        Element boundedByElement = this.getGmlCore().getBoundedBy().serializeToXMLElement();
        coverageTypeElement.appendChild(boundedByElement);
        
        // <gml:domainSet>
        Element domainSetElement = this.getGmlCore().getDomainSet().serializeToXMLElement();
        coverageTypeElement.appendChild(domainSetElement);
        
        // <gml:rangeSet>
        if (this.rangeSet != null) {
            Element rangeSetElement = this.getRangeSet().serializeToXMLElement();
            coverageTypeElement.appendChild(rangeSetElement);
        }
        
        // <gml:coverageFunction>
        Element coverageFunctionElement = this.getGmlCore().getCoverageFunction().serializeToXMLElement();
        coverageTypeElement.appendChild(coverageFunctionElement);
        
        // <gml:rangeType>
        Element rangeTypeElement = this.getGmlCore().getRangeType().serializeToXMLElement();
        coverageTypeElement.appendChild(rangeTypeElement);
        
        // <gmlcov:metadata>
        Element metadataElement;
        try {
            metadataElement = this.getGmlCore().getMetadata().serializeToXMLElement();
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.XmlNotValid, "Cannot serialize coverage's metadata to XML element. Reason: " + ex.getMessage(), ex);
        }
        coverageTypeElement.appendChild(metadataElement);
        
        
        return coverageTypeElement;
    }
}
