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
package petascope.core.gml.cis10.model.domainset.rectifiedgrid;

import nu.xom.Element;
import static petascope.core.XMLSymbols.LABEL_ORIGIN;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 * Class to represent Origin element of RectifiedGrid coverage. e.g:
 * 
<gml:origin>
    <gml:Point gml:id="p00_Hungary__HU_SSAND" srsName="http://www.opengis.net/def/crs/EPSG/0/23700">
        <gml:pos>415267.866168 408521.3387750001</gml:pos>
    </gml:Point>
</gml:origin>
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Origin implements ISerializeToXMElement {
    
    private Point point;
    
    // By default it is gml:offsetvector (RectifiedGridCoverage)
    private String prefixLabelXML = PREFIX_GML;

    public Origin(Point point) {
        this.point = point;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public void setPrefixLabelXML(String prefixLabelXML) {
        this.prefixLabelXML = prefixLabelXML;
    }

    @Override
    public Element serializeToXMLElement() {
        Element originElement = new Element(XMLUtil.createXMLLabel(prefixLabelXML, LABEL_ORIGIN), XMLUtil.getNameSpaceByPrefix(prefixLabelXML));
        Element pointElement = this.point.serializeToXMLElement();
        originElement.appendChild(pointElement);
        
        return originElement;
    }    
}
