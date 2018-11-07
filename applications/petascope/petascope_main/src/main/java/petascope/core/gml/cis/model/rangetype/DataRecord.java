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

import java.util.List;
import nu.xom.Element;
import static petascope.core.XMLSymbols.LABEL_DATA_RECORD;
import static petascope.core.XMLSymbols.NAMESPACE_SWE;
import static petascope.core.XMLSymbols.PREFIX_SWE;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 *
 * Class to represent DataRecord element in CIS 1.0. e.g:
 * 
 <swe:DataRecord>
    <swe:field name="chlor_a">
    ...
    </swe:field>
    <swe:field name="chlor_b">
    ...
    </swe:field>
</swe:DataRecord>
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class DataRecord implements ISerializeToXMElement {
    
    private List<Field> fields;

    public DataRecord(List<Field> fields) {
        this.fields = fields;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    @Override
    public Element serializeToXMLElement() {
        Element dataRecordElement = new Element(XMLUtil.createXMLLabel(PREFIX_SWE, LABEL_DATA_RECORD), NAMESPACE_SWE);
        
        for (Field field : fields) {
            Element fieldElement = field.serializeToXMLElement();
            dataRecordElement.appendChild(fieldElement);
        }
        
        return dataRecordElement;
    }
    
}
