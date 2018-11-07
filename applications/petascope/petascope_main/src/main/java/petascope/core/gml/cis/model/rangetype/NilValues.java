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
import static petascope.core.XMLSymbols.LABEL_NILVALUES;
import static petascope.core.XMLSymbols.LABEL_NILVALUES_ASSOCIATION_ROLE;
import static petascope.core.XMLSymbols.NAMESPACE_SWE;
import static petascope.core.XMLSymbols.PREFIX_SWE;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 * Class to present NilValues element.e.g:
 * 
<swe:nilValues>
    <swe:NilValues>
        <swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/BelowDetectionRange">-INF</swe:nilValue>
        <swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/AboveDetectionRange">INF</swe:nilValue>
    </swe:NilValues>
</swe:nilValues>
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class NilValues implements ISerializeToXMElement {
    
    private List<NilValue> nilValues;

    public NilValues(List<NilValue> nilValues) {
        this.nilValues = nilValues;
    }

    public List<NilValue> getNilValues() {
        return nilValues;
    }

    public void setNilValues(List<NilValue> nilValues) {
        this.nilValues = nilValues;
    }

    @Override
    public Element serializeToXMLElement() {
        Element nilValuesAssociaionRoleElement = new Element(XMLUtil.createXMLLabel(PREFIX_SWE, LABEL_NILVALUES_ASSOCIATION_ROLE), NAMESPACE_SWE);
        Element nilValuesElement = new Element(XMLUtil.createXMLLabel(PREFIX_SWE, LABEL_NILVALUES), NAMESPACE_SWE);
        nilValuesAssociaionRoleElement.appendChild(nilValuesElement);
        
        for (NilValue nilValue : nilValues) {
            Element nilValueElement = nilValue.serializeToXMLElement();
            nilValuesElement.appendChild(nilValueElement);
        }
        
        return nilValuesAssociaionRoleElement;
    }
}
