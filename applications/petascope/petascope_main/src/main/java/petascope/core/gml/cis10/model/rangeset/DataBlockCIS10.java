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
package petascope.core.gml.cis10.model.rangeset;

import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_CS;
import static petascope.core.XMLSymbols.ATT_TS;
import static petascope.core.XMLSymbols.LABEL_DATABLOCK;
import static petascope.core.XMLSymbols.LABEL_RANGE_PARAMETERS;
import static petascope.core.XMLSymbols.LABEL_TUPLELIST;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.exceptions.PetascopeException;
import petascope.util.XMLUtil;

/**
 * Class to build DataBlock element of RangeSet. e.g: 

<gml:DataBlock>
    <gml:rangeParameters/>
    <gml:tupleList cs=" " ts=",">
       19.26362 4.193265,19.26362 4.193265,19.26362 4.193265,...
    </gml:tupleList>
</gml:DataBlock>

* @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class DataBlockCIS10 implements ISerializeToXMElement {
    
    private String tupleList;

    public DataBlockCIS10(String tupleList) {
        this.tupleList = tupleList;
    }

    public String getTupleList() {
        return tupleList;
    }

    public void setTupleList(String tupleList) {
        this.tupleList = tupleList;
    }

    @Override
    public Element serializeToXMLElement() throws PetascopeException {
        Element dataBlockElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_DATABLOCK), NAMESPACE_GML);
        
        Element rangeParametersElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_RANGE_PARAMETERS), NAMESPACE_GML);
        dataBlockElement.appendChild(rangeParametersElement);
        
        Element tupleListElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_TUPLELIST), NAMESPACE_GML);
        Attribute csAttribute = new Attribute(ATT_CS, " ");
        Attribute tsAttribute = new Attribute(ATT_TS, ",");
        tupleListElement.addAttribute(csAttribute);
        tupleListElement.addAttribute(tsAttribute);
        tupleListElement.appendChild(tupleList);
        
        dataBlockElement.appendChild(tupleListElement);
        
        return dataBlockElement;
    }
    
    
}
