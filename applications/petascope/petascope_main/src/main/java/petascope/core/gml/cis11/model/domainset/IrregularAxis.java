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

import java.util.List;
import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_AXIS_LABEL;
import static petascope.core.XMLSymbols.ATT_LOWER_BOUND;
import static petascope.core.XMLSymbols.LABEL_INDEX_AXIS;
import static petascope.core.XMLSymbols.LABEL_IRREGULAR_AXIS_COEFFICIENT;
import static petascope.core.XMLSymbols.PREFIX_CIS11;
import petascope.core.gml.ISerializeToXMElement;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.XMLUtil;
import static petascope.core.XMLSymbols.NAMESPACE_CIS_11;

/**
 *
 * Class to represent irregularAxis in CIS 1.1. e.g:
     
<cis11:irregularAxis axisLabel="h" uomLabel="m">
    <C>10</C>
    <C>50</C>
    <C>100</C>
</cis11:irregularAxis>

 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class IrregularAxis implements ISerializeToXMElement {
    
    private String axisLabel;
    private String uomLabel;
    private List<String> coefficients;

    public IrregularAxis(String axisLabel, String uomLabel, List<String> coefficients) {
        this.axisLabel = axisLabel;
        this.uomLabel = uomLabel;
        this.coefficients = coefficients;
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

    public List<String> getCoefficients() {
        return coefficients;
    }

    public void setCoefficients(List<String> coefficients) {
        this.coefficients = coefficients;
    }
    
    @Override
    public Element serializeToXMLElement() throws PetascopeException, SecoreException {
        Element irregularAxisElement = new Element(XMLUtil.createXMLLabel(PREFIX_CIS11, LABEL_INDEX_AXIS), NAMESPACE_CIS_11);
        
        Attribute axisLabelAttribute = new Attribute(ATT_AXIS_LABEL, NAMESPACE_CIS_11);
        axisLabelAttribute.setValue(this.axisLabel);
        
        Attribute uomLabelAttribute = new Attribute(ATT_LOWER_BOUND, NAMESPACE_CIS_11);
        uomLabelAttribute.setValue(this.uomLabel);
        
        irregularAxisElement.addAttribute(axisLabelAttribute);
        irregularAxisElement.addAttribute(uomLabelAttribute);
        
        for (String coefficient : this.coefficients) {
            Element coefficientElement = new Element(XMLUtil.createXMLLabel(PREFIX_CIS11, LABEL_IRREGULAR_AXIS_COEFFICIENT), NAMESPACE_CIS_11);
            coefficientElement.appendChild(coefficient);
            
            irregularAxisElement.appendChild(coefficientElement);
        }
        
        return irregularAxisElement;
    }
}
