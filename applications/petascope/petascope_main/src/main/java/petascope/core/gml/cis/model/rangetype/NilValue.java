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
import static petascope.core.XMLSymbols.ATT_REASON;
import static petascope.core.XMLSymbols.LABEL_NILVALUE;
import static petascope.core.XMLSymbols.NAMESPACE_SWE;
import static petascope.core.XMLSymbols.PREFIX_SWE;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 *
 * Class to represent nilValue element. e.g:
 * 
 * <swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/BelowDetectionRange">-INF</swe:nilValue>
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class NilValue implements ISerializeToXMElement {
    
    private String reason;
    private String value;

    public NilValue(String reason, String value) {
        this.reason = reason;
        this.value = value;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Element serializeToXMLElement() {
        Element nilValueElement = new Element(XMLUtil.createXMLLabel(PREFIX_SWE, LABEL_NILVALUE), NAMESPACE_SWE);
        
        if (reason != null) {
            Attribute reasonAttribute = new Attribute(ATT_REASON, reason);
            nilValueElement.addAttribute(reasonAttribute);
        }
        
        nilValueElement.appendChild(value);
        
        return nilValueElement;
    }
}
