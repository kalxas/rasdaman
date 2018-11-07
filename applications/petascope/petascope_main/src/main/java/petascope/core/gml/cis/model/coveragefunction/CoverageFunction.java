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
import static petascope.core.XMLSymbols.LABEL_COVERAGE_FUNCTION;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 * Class to represent CoverageFunction element in CIS 1.0. e.g:
 
<gml:coverageFunction>
    <gml:GridFunction>
        <gml:sequenceRule axisOrder="+1 +2">Linear</gml:sequenceRule>
            <gml:startPoint>0 0</gml:startPoint>
    </gml:GridFunction>
</gml:coverageFunction>
  
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class CoverageFunction implements ISerializeToXMElement {
    
    private GridFunction gridFunction;

    public CoverageFunction(GridFunction gridFunction) {
        this.gridFunction = gridFunction;
    }

    public GridFunction getGridFunction() {
        return gridFunction;
    }

    public void setGridFunction(GridFunction gridFunction) {
        this.gridFunction = gridFunction;
    }

    @Override
    public Element serializeToXMLElement() {
        Element coverageFunctionElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_COVERAGE_FUNCTION), NAMESPACE_GML);
        Element gridFunctionElement = this.gridFunction.serializeToXMLElement();
        coverageFunctionElement.appendChild(gridFunctionElement);
        
        return coverageFunctionElement;
    }
    
}
