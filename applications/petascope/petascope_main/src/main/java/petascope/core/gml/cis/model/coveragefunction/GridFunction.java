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
package petascope.core.gml.cis.model.coveragefunction;

import nu.xom.Element;
import static petascope.core.XMLSymbols.LABEL_GRID_FUNCTION;
import static petascope.core.XMLSymbols.LABEL_START_POINT;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 * Class to represent GridFunction element in CIS 1.0. e.g:
 
 <gml:GridFunction>
    <gml:sequenceRule axisOrder="+1 +2">Linear</gml:sequenceRule>
    <gml:startPoint>0 0</gml:startPoint>
 </gml:GridFunction>
  
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class GridFunction implements ISerializeToXMElement {
    
    private SequenceRule sequenceRule;   
    private String startPoint;

    public GridFunction(SequenceRule sequenceRule, String startPoint) {
        this.sequenceRule = sequenceRule;
        this.startPoint = startPoint;
    }

    public SequenceRule getSequenceRule() {
        return sequenceRule;
    }

    public void setSequenceRule(SequenceRule sequenceRule) {
        this.sequenceRule = sequenceRule;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    @Override
    public Element serializeToXMLElement() {
        Element gridFunctionElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_GRID_FUNCTION), NAMESPACE_GML);
        
        Element sequenceRuleElement = this.sequenceRule.serializeToXMLElement();
        Element startPointElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_START_POINT), NAMESPACE_GML);
        startPointElement.appendChild(startPoint);
        
        gridFunctionElement.appendChild(sequenceRuleElement);
        gridFunctionElement.appendChild(startPointElement);
        
        return gridFunctionElement;
    }
    
}
