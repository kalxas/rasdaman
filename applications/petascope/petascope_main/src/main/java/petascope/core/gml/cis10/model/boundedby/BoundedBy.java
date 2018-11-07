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
package petascope.core.gml.cis10.model.boundedby;

import nu.xom.Element;
import static petascope.core.XMLSymbols.LABEL_BOUNDEDBY;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 * Class to represent boundedBy element in CIS 1.0. e.g:
<gml:boundedBy>  
    <gml:Envelope srsName="http://www.opengis.net/def/crs/EPSG/0/23700" axisLabels="Y X" uomLabels="m m" srsDimension="2">
        <gml:lowerCorner>415142.866168 4896.338775410026</gml:lowerCorner>
        <gml:upperCorner>943642.866168 408646.338775</gml:upperCorner>
    </gml:Envelope>
</gml:boundedBy>
  
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class BoundedBy implements ISerializeToXMElement {
    
    private Envelope envelope;

    public BoundedBy(Envelope envelope) {
        this.envelope = envelope;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    @Override
    public Element serializeToXMLElement() {
        Element boundedByElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_BOUNDEDBY), NAMESPACE_GML);
        Element envelopeElement = this.envelope.serializeToXMLElement();
        boundedByElement.appendChild(envelopeElement);
                
        return boundedByElement;
    }
    
}
