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
package petascope.core.gml.cis11.model.domainset;

import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_AXIS_LABEL;
import static petascope.core.XMLSymbols.ATT_LOWER_BOUND;
import static petascope.core.XMLSymbols.ATT_RESOLUTION;
import static petascope.core.XMLSymbols.ATT_UOM_LABEL;
import static petascope.core.XMLSymbols.ATT_UPPER_BOUND;
import static petascope.core.XMLSymbols.LABEL_REGULAR_AXIS;
import static petascope.core.XMLSymbols.PREFIX_CIS11;
import petascope.core.gml.ISerializeToXMElement;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.XMLUtil;
import static petascope.core.XMLSymbols.NAMESPACE_CIS_11;

/**
 * Class to represent regularAxis in CIS 1.1. e.g:
 
 <cis11:regularAxis axisLabel="Lat" uomLabel="deg" lowerBound="-80" upperBound="-70" resolution="5"/>
  
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class RegularAxis implements ISerializeToXMElement {
    
    private String axisLabel;
    private String uomLabel;
    private String lowerBound;
    private String upperBound;
    private String resolution;

    public RegularAxis(String axisLabel, String uomLabel, String lowerBound, String upperBound, String resolution) {
        this.axisLabel = axisLabel;
        this.uomLabel = uomLabel;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.resolution = resolution;
    }

    public String getAxisLabel() {
        return axisLabel;
    }

    public void setAxisLabel(String axisLabel) {
        this.axisLabel = axisLabel;
    }

    public String getUomLabel() {
        return uomLabel;
    }

    public void setUomLabel(String uomLabel) {
        this.uomLabel = uomLabel;
    }

    public String getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(String lowerBound) {
        this.lowerBound = lowerBound;
    }

    public String getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(String upperBound) {
        this.upperBound = upperBound;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
    
    @Override
    public Element serializeToXMLElement() throws PetascopeException, SecoreException {
        Element regularAxisElement = new Element(XMLUtil.createXMLLabel(PREFIX_CIS11, LABEL_REGULAR_AXIS), NAMESPACE_CIS_11);
        
        Attribute axisLabelAttribute = new Attribute(ATT_AXIS_LABEL, NAMESPACE_CIS_11);
        axisLabelAttribute.setValue(this.axisLabel);
        
        Attribute uomLabelAttribute = new Attribute(ATT_UOM_LABEL, NAMESPACE_CIS_11);
        uomLabelAttribute.setValue(this.uomLabel);
        
        Attribute lowerBoundAttribute = new Attribute(ATT_LOWER_BOUND, NAMESPACE_CIS_11);
        lowerBoundAttribute.setValue(this.lowerBound);
        
        Attribute upperBoundAttribute = new Attribute(ATT_UPPER_BOUND, NAMESPACE_CIS_11);
        upperBoundAttribute.setValue(this.upperBound);
        
        Attribute resolutionAttribute = new Attribute(ATT_RESOLUTION, NAMESPACE_CIS_11);
        resolutionAttribute.setValue(this.resolution);
        
        regularAxisElement.addAttribute(axisLabelAttribute);
        regularAxisElement.addAttribute(uomLabelAttribute);
        regularAxisElement.addAttribute(lowerBoundAttribute);
        regularAxisElement.addAttribute(upperBoundAttribute);
        regularAxisElement.addAttribute(resolutionAttribute);
        
        return regularAxisElement;        
    }
}
