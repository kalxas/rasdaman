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
package petascope.core.gml.cis10.model.domainset.referenceablegridbyvectors;

import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_AXIS_ORDER;
import static petascope.core.XMLSymbols.LABEL_COEFFICIENTS;
import static petascope.core.XMLSymbols.LABEL_GENERAL_GRID_AXIS;
import static petascope.core.XMLSymbols.LABEL_GENERAL_GRID_AXIS_ASSOCIATION_ROLE;
import static petascope.core.XMLSymbols.LABEL_GRID_AXES_SPANNED;
import static petascope.core.XMLSymbols.LABEL_SEQUENCE_RULE;
import static petascope.core.XMLSymbols.NAMESPACE_GMLRGRID;
import static petascope.core.XMLSymbols.PREFIX_GMLRGRID;
import static petascope.core.XMLSymbols.VALUE_SEQUENCE_RULE_AXIS_ORDER_DEFAULT;
import static petascope.core.XMLSymbols.VALUE_SEQUENCE_RULE_TYPE_DEFAULT;
import petascope.core.gml.ISerializeToXMElement;
import petascope.core.gml.cis10.model.domainset.rectifiedgrid.OffsetVector;
import petascope.util.XMLUtil;

/**
 * Class to represent GeneralGridAxis element in ReferenceableGrid coverage. e.g:
 * 
 * 
<gmlrgrid:generalGridAxis>
    <gmlrgrid:GeneralGridAxis>
        <gmlrgrid:offsetVector srsName="1=http://localhost:8080/def/crs/EPSG/0/32633&amp;2=http://localhost:8080/def/crs/OGC/0/AnsiDate">0 0 1</gmlrgrid:offsetVector>
        <gmlrgrid:coefficients>"2008-01-01T02:01:20.000Z" "2008-01-03T23:59:55.000Z" "2008-01-05T01:58:30.000Z" "2008-01-08T00:02:58.000Z"</gmlrgrid:coefficients>
        <gmlrgrid:gridAxesSpanned>ansi</gmlrgrid:gridAxesSpanned>
        <gmlrgrid:sequenceRule axisOrder="+1">Linear</gmlrgrid:sequenceRule>
    </gmlrgrid:GeneralGridAxis>
</gmlrgrid:generalGridAxis>
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class GeneralGridAxis implements ISerializeToXMElement {
    
    private OffsetVector offsetVector;
    private String coefficients;
    private String gridAxesSpanned;

    public GeneralGridAxis(OffsetVector offsetVector, String coefficients, String gridAxesSpanned) {
        this.offsetVector = offsetVector;
        this.coefficients = coefficients;
        this.gridAxesSpanned = gridAxesSpanned;
    }

    public OffsetVector getOffsetVector() {
        return offsetVector;
    }

    public void setOffsetVector(OffsetVector offsetVector) {
        this.offsetVector = offsetVector;
    }

    public String getCoefficients() {
        return coefficients;
    }

    public void setCoefficients(String coefficients) {
        this.coefficients = coefficients;
    }

    public String getGridAxesSpanned() {
        return gridAxesSpanned;
    }

    public void setGridAxesSpanned(String gridAxesSpanned) {
        this.gridAxesSpanned = gridAxesSpanned;
    }


    @Override
    public Element serializeToXMLElement() {
        
        Element generalGridAxisAssociateRoleElement = new Element(XMLUtil.createXMLLabel(PREFIX_GMLRGRID, LABEL_GENERAL_GRID_AXIS_ASSOCIATION_ROLE), NAMESPACE_GMLRGRID);
        
        Element generalGridAxisElement = new Element(XMLUtil.createXMLLabel(PREFIX_GMLRGRID, LABEL_GENERAL_GRID_AXIS), NAMESPACE_GMLRGRID);
        
        Element offsetVectorElement = this.offsetVector.serializeToXMLElement();
        Element coefficientsElement = new Element(XMLUtil.createXMLLabel(PREFIX_GMLRGRID, LABEL_COEFFICIENTS), NAMESPACE_GMLRGRID);
        coefficientsElement.appendChild(this.coefficients);
        
        Element gridAxesSpannedElement = new Element(XMLUtil.createXMLLabel(PREFIX_GMLRGRID, LABEL_GRID_AXES_SPANNED), NAMESPACE_GMLRGRID);
        gridAxesSpannedElement.appendChild(this.gridAxesSpanned);
                
        Element sequerenceRuleElement = new Element(XMLUtil.createXMLLabel(PREFIX_GMLRGRID, LABEL_SEQUENCE_RULE), NAMESPACE_GMLRGRID);
        Attribute axisOrderAttribute = new Attribute(ATT_AXIS_ORDER, VALUE_SEQUENCE_RULE_AXIS_ORDER_DEFAULT);
        sequerenceRuleElement.addAttribute(axisOrderAttribute);
        sequerenceRuleElement.appendChild(VALUE_SEQUENCE_RULE_TYPE_DEFAULT);
        
        generalGridAxisElement.appendChild(offsetVectorElement);
        generalGridAxisElement.appendChild(coefficientsElement);
        generalGridAxisElement.appendChild(gridAxesSpannedElement);
        generalGridAxisElement.appendChild(sequerenceRuleElement);
        
        generalGridAxisAssociateRoleElement.appendChild(generalGridAxisElement);
        
        return generalGridAxisAssociateRoleElement;
    }
}
