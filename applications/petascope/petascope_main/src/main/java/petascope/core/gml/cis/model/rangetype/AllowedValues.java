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
import static petascope.core.XMLSymbols.LABEL_ALLOWED_VALUES;
import static petascope.core.XMLSymbols.LABEL_INTERVAL;
import static petascope.core.XMLSymbols.NAMESPACE_SWE;
import static petascope.core.XMLSymbols.PREFIX_SWE;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 *
 * Class to represent AllowedValues element. e.g:
 
<swe:AllowedValues>
    <swe:interval>-180 0</swe:interval>
    <swe:interval>1 180</swe:interval>
</swe:AllowedValues>
 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class AllowedValues implements ISerializeToXMElement {
    
    private List<String> intervals;

    public AllowedValues(List<String> intervals) {
        this.intervals = intervals;
    }

    public List<String> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<String> intervals) {
        this.intervals = intervals;
    }

    @Override
    public Element serializeToXMLElement() {
        Element allowedValuesElement = new Element(XMLUtil.createXMLLabel(PREFIX_SWE, LABEL_ALLOWED_VALUES), NAMESPACE_SWE);
        
        for (String interval : this.intervals) {
            Element intervalElement = new Element(XMLUtil.createXMLLabel(PREFIX_SWE, LABEL_INTERVAL), NAMESPACE_SWE);
            intervalElement.appendChild(interval);
            
            allowedValuesElement.appendChild(intervalElement);
        }
        
        return allowedValuesElement;        
    }
}
