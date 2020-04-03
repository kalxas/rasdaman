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
import static petascope.core.XMLSymbols.LABEL_RANGESET_CIS11;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.exceptions.PetascopeException;
import petascope.util.XMLUtil;

/**
 * Class to represent rangeSet element in CIS 1.1 for WCS GetCoverage result in GML. e.g:
 
<gml:rangeSet>
    <gml:DataBlock>
        <cis11:V>01</cis11:V>
        <cis11:V>02</cis11:V>
        <cis11:V>03</cis11:V>
        <cis11:V>04</cis11:V>
        <cis11:V>05</cis11:V>
        ...
    <gml:DataBlock>
</gml:rangeSet>
  
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class RangeSetCIS11 implements ISerializeToXMElement {
    
    private DataBlockCIS11 dataBlock;

    public RangeSetCIS11(DataBlockCIS11 dataBlock) {
        this.dataBlock = dataBlock;
    }

    public DataBlockCIS11 getDataBlock() {
        return dataBlock;
    }

    public void setDataBlock(DataBlockCIS11 dataBlock) {
        this.dataBlock = dataBlock;
    }
    

    @Override
    public Element serializeToXMLElement() throws PetascopeException {
        Element rangeSetElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_RANGESET_CIS11), NAMESPACE_GML);
        Element dataBlockElement = this.dataBlock.serializeToXMLElement();
        rangeSetElement.appendChild(dataBlockElement);
        
        return rangeSetElement;
    }
    
}
