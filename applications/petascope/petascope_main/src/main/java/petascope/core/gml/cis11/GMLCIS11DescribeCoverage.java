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
package petascope.core.gml.cis11;

import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_ID;
import static petascope.core.XMLSymbols.LABEL_COVERAGE_DESCRIPTION;
import static petascope.core.XMLSymbols.LABEL_COVERAGE_ID;
import static petascope.core.XMLSymbols.LABEL_COVERAGE_SUBTYPE;
import static petascope.core.XMLSymbols.LABEL_NATIVE_FORMAT;
import static petascope.core.XMLSymbols.LABEL_SERVICE_PARAMETERS;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import static petascope.core.XMLSymbols.PREFIX_WCS;
import static petascope.core.XMLSymbols.VALUE_COVERAGE_NATIVE_FORMAT;
import petascope.core.gml.cis.AbstractGMLCISDescribeCoverage;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.XMLUtil;
import static petascope.core.XMLSymbols.NAMESPACE_WCS_21;

/**
 * Class to build result for WCS DescribeCoverage request.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class GMLCIS11DescribeCoverage extends AbstractGMLCISDescribeCoverage {

    private String coverageId;
    private String coverageType;
    private GMLCoreCIS11 gmlCore;

    public GMLCIS11DescribeCoverage(String coverageId, String coverageType, GMLCoreCIS11 gmlCore) {
        this.coverageId = coverageId;
        this.coverageType = coverageType;
        this.gmlCore = gmlCore;
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
    
    public GMLCoreCIS11 getGmlCore() {
        return gmlCore;
    }

    public void setGmlCore(GMLCoreCIS11 gmlCore) {
        this.gmlCore = gmlCore;
    }

    @Override
    public Element serializeToXMLElement() throws PetascopeException {
        
        // <wcs:CoverageDescription>
        Element coverageDescriptionElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_COVERAGE_DESCRIPTION), NAMESPACE_WCS_21);
        Attribute attributeId = XMLUtil.createXMLAttribute(NAMESPACE_GML, PREFIX_GML, ATT_ID, this.coverageId);
        coverageDescriptionElement.addAttribute(attributeId);
        
        // <gml:envelope>
        Element envelopeElement = this.getGmlCore().getEnvelope().serializeToXMLElement();
        coverageDescriptionElement.appendChild(envelopeElement);
        
        // <wcs:CoverageId>
        Element coverageIdElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_COVERAGE_ID), NAMESPACE_WCS_21);
        coverageIdElement.appendChild(coverageId);
        coverageDescriptionElement.appendChild(coverageIdElement);
        
        // <gml:coverageFunction>
        Element coverageFunctionElement = this.getGmlCore().getCoverageFunction().serializeToXMLElement();
        coverageDescriptionElement.appendChild(coverageFunctionElement);
        
        // <gml:domainSet>
        Element domainSetElement = this.getGmlCore().getDomainSet().serializeToXMLElement();
        coverageDescriptionElement.appendChild(domainSetElement);
        
        // <gml:rangeType>
        Element rangeTypeElement = this.getGmlCore().getRangeType().serializeToXMLElement();
        coverageDescriptionElement.appendChild(rangeTypeElement);
        
        // <gmlcov:metadata>
        Element metadataElement;
        try {
            metadataElement = this.getGmlCore().getMetadata().serializeToXMLElement();
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.XmlNotValid, "Cannot serialize coverage's metadata to XML element. Reason: " + ex.getMessage(), ex);
        }
        coverageDescriptionElement.appendChild(metadataElement);
        
        // <wcs:ServiceParameters>
        Element serviceParametersElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_SERVICE_PARAMETERS), NAMESPACE_WCS_21);
        
        Element coverageSubTypeElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_COVERAGE_SUBTYPE), NAMESPACE_WCS_21);
        coverageSubTypeElement.appendChild(coverageType);
        Element nativeFormatElement = new Element(XMLUtil.createXMLLabel(PREFIX_WCS, LABEL_NATIVE_FORMAT), NAMESPACE_WCS_21);
        nativeFormatElement.appendChild(VALUE_COVERAGE_NATIVE_FORMAT);
        
        serviceParametersElement.appendChild(coverageSubTypeElement);
        serviceParametersElement.appendChild(nativeFormatElement);
        
        coverageDescriptionElement.appendChild(serviceParametersElement);
        
        return coverageDescriptionElement;
    }
}
