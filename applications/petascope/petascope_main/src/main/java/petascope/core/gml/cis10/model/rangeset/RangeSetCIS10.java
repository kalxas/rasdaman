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

import nu.xom.Element;
import static petascope.core.XMLSymbols.LABEL_RANGE_SET;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.exceptions.PetascopeException;
import petascope.util.XMLUtil;

/**
 * Class to represent rangeSet element in CIS 1.0 for WCS GetCoverage result in GML. e.g:
 
<gml:rangeSet>
    <gml:DataBlock>
        <gml:rangeParameters/>
        <gml:tupleList cs=" " ts=",">
           19.26362 4.193265,19.26362 4.193265,19.26362 4.193265,...
        </gml:tupleList>
    </gml:DataBlock>
</gml:rangeSet> 
  
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class RangeSetCIS10 implements ISerializeToXMElement {
    
    private DataBlockCIS10 dataBlock;

    public RangeSetCIS10(DataBlockCIS10 dataBlock) {
        this.dataBlock = dataBlock;
    }

    public DataBlockCIS10 getDataBlock() {
        return dataBlock;
    }

    public void setDataBlock(DataBlockCIS10 dataBlock) {
        this.dataBlock = dataBlock;
    }
    

    @Override
    public Element serializeToXMLElement() throws PetascopeException {
        Element rangeSetElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_RANGE_SET), NAMESPACE_GML);
        
        Element dataBlockElement = this.dataBlock.serializeToXMLElement();
        rangeSetElement.appendChild(dataBlockElement);
        
        return rangeSetElement;
    }
    
}
