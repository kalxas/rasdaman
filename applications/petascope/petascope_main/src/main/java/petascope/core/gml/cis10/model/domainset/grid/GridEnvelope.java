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
import static petascope.core.XMLSymbols.LABEL_GRID_ENVELOPE;
import static petascope.core.XMLSymbols.LABEL_HIGH;
import static petascope.core.XMLSymbols.LABEL_LOW;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 * Class to represent GridEnvelope element of GridCoverage. e.g:
 * 
<gml:GridEnvelope>
   <gml:low>0 0</gml:low>
   <gml:high>2113 1614</gml:high>
</gml:GridEnvelope>
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class GridEnvelope implements ISerializeToXMElement {
    
    private String low;
    private String high;

    public GridEnvelope(String low, String high) {
        this.low = low;
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    @Override
    public Element serializeToXMLElement() {
        Element gridEnvelopeElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_GRID_ENVELOPE), NAMESPACE_GML);
        Element lowElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_LOW), NAMESPACE_GML);
        lowElement.appendChild(low);
        
        Element highElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_HIGH), NAMESPACE_GML);
        highElement.appendChild(high);
        
        gridEnvelopeElement.appendChild(lowElement);
        gridEnvelopeElement.appendChild(highElement);
        
        return gridEnvelopeElement;
    }
}
