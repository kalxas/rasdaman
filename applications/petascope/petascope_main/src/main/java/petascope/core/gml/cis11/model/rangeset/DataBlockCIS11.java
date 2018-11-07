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
package petascope.core.gml.cis11.model.rangeset;

import nu.xom.Element;
import static petascope.core.XMLSymbols.LABEL_CIS11_VALUE;
import static petascope.core.XMLSymbols.LABEL_RANGE_SET;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_CIS11;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.exceptions.PetascopeException;
import petascope.util.XMLUtil;
import static petascope.core.XMLSymbols.NAMESPACE_CIS_11;

/**
 * Class to build DataBlock element of RangeSet. e.g: 
 
<DataBlock>
    <V>01</V>
    <V>02</V>
    <V>03</V>
    <V>04</V>
    <V>05</V>
    ...
<DataBlock>

* @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class DataBlockCIS11 implements ISerializeToXMElement {
    
    private String pixelValues;

    public DataBlockCIS11(String pixelValues) {
        this.pixelValues = pixelValues;
    }

    public String getPixelValues() {
        return pixelValues;
    }

    public void setPixelValues(String pixelValues) {
        this.pixelValues = pixelValues;
    }

    @Override
    public Element serializeToXMLElement() throws PetascopeException {
        Element dataBlockElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_RANGE_SET), NAMESPACE_GML);
        
        String[] values = this.pixelValues.split(",");
        
        for (String value : values) { 
            Element valueElement = new Element(XMLUtil.createXMLLabel(PREFIX_CIS11, LABEL_CIS11_VALUE), NAMESPACE_CIS_11);
            valueElement.appendChild(value);
            
            dataBlockElement.appendChild(valueElement);
        }

        return dataBlockElement;
    }
    
    
}
