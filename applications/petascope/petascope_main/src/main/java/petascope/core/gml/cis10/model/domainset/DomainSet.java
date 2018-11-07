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
package petascope.core.gml.cis10.model.domainset;

import nu.xom.Element;
import petascope.core.XMLSymbols;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.core.gml.cis10.model.domainset.grid.Grid;
import petascope.util.XMLUtil;

/**
 * Class to represent domainSet element for 3 coverage types of CIS 1.0. e.g:
 * 
 <gml:domainSet>
    <gml:RectifiedGrid gml:id="grid00__Hungary__HU_SSAND" dimension="2">
 </gml:domdainSet>
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class DomainSet implements ISerializeToXMElement {
    
    // Depend on which coverage's type to return correct domainSet element
    private Grid grid;

    public DomainSet(Grid grid) {
        this.grid = grid;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    @Override
    public Element serializeToXMLElement() {
        Element domainSetElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, XMLSymbols.LABEL_DOMAIN_SET), NAMESPACE_GML);        
        
        Element gridElement = this.grid.serializeToXMLElement();
        domainSetElement.appendChild(gridElement);
        
        return domainSetElement;
    }
}
