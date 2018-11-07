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
package petascope.core.gml.cis.model.rangetype;

import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_NAME;
import static petascope.core.XMLSymbols.LABEL_FIELD;
import static petascope.core.XMLSymbols.NAMESPACE_SWE;
import static petascope.core.XMLSymbols.PREFIX_SWE;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 * Class to represent Field element. e.g:
 
<swe:field name="quantity_field_with_nil">
    <swe:Quantity>
    ...
    </swe:Quantity>
</swe:field>
        
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Field implements ISerializeToXMElement {
    
    private String name;
    private Quantity quantity;

    public Field(String name, Quantity quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    @Override
    public Element serializeToXMLElement() {
        Element fieldElement = new Element(XMLUtil.createXMLLabel(PREFIX_SWE, LABEL_FIELD), NAMESPACE_SWE);
        Attribute nameAttribute = new Attribute(ATT_NAME, name);
        fieldElement.addAttribute(nameAttribute);
        
        Element quantityElement = this.quantity.serializeToXMLElement();
        fieldElement.appendChild(quantityElement);
        
        return fieldElement;
    }
    
}
