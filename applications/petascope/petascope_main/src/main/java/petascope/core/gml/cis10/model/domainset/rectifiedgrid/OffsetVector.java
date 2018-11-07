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

import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_SRS_NAME;
import static petascope.core.XMLSymbols.LABEL_OFFSET_VECTOR;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 * Class to represent OffsetVector of RectifiedGrid coverage. e.g:
 
 <gml:offsetVector srsName="http://www.opengis.net/def/crs/EPSG/0/23700">250.0 0.0</gml:offsetVector>
  
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class OffsetVector implements ISerializeToXMElement {
    
    private String srsName;
    private String coordinates;
    
    // By default it is gml:offsetvector (RectifiedGridCoverage)
    private String prefixLabelXML = PREFIX_GML;

    public OffsetVector(String srsName, String coordinates) {
        this.srsName = srsName;
        this.coordinates = coordinates;
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }
    
    public void setPrefixLabelXML(String newPrefix) {
        this.prefixLabelXML = newPrefix;
    }

    @Override
    public Element serializeToXMLElement() {
        Element offsetVectorElement = new Element(XMLUtil.createXMLLabel(this.prefixLabelXML, LABEL_OFFSET_VECTOR), XMLUtil.getNameSpaceByPrefix(prefixLabelXML));
        
        Attribute srsNameAttribute = new Attribute(ATT_SRS_NAME, srsName);
        offsetVectorElement.addAttribute(srsNameAttribute);
        
        offsetVectorElement.appendChild(coordinates);
        
        return offsetVectorElement;
    }
    
}
