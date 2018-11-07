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
package petascope.core.gml.cis10.model.domainset.grid;

import nu.xom.Element;
import static petascope.core.XMLSymbols.LABEL_LIMITS;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 * Class to represent Limits element of Grid coverage. e.g:
 * 
<gml:limits>
    <gml:GridEnvelope>
        <gml:low>0 0</gml:low>
        <gml:high>2113 1614</gml:high>
    </gml:GridEnvelope>
</gml:limits>
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Limits implements ISerializeToXMElement {
    
    private GridEnvelope gridEnvelope;

    public Limits(GridEnvelope gridEnvelope) {
        this.gridEnvelope = gridEnvelope;
    }

    public GridEnvelope getGridEnvelope() {
        return gridEnvelope;
    }

    public void setGridEnvelope(GridEnvelope gridEnvelope) {
        this.gridEnvelope = gridEnvelope;
    }

    @Override
    public Element serializeToXMLElement() {
        Element limitsElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_LIMITS), NAMESPACE_GML);
        Element gridEnvelopeElement = this.gridEnvelope.serializeToXMLElement();
        limitsElement.appendChild(gridEnvelopeElement);
        
        return limitsElement;
    }
}
