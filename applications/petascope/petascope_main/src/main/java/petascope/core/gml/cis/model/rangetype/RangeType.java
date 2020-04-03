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

import nu.xom.Element;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 * Class to represent rangeType element in CIS 1.0, CIS 1.1. e.g:
 * 
 <gmlcov:rangeType>
    <swe:DataRecord>
    ...
    </swe:DataRecord>
</gmlcov:rangeType>
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class RangeType implements ISerializeToXMElement {
    
    private DataRecord dataRecord;
    private String xmlPrefix;
    private String rangeTypeElementName;
    private String xmlNamespace;

    public RangeType(String xmlPrefix, String rangeTypeElementName, String xmlNamespace, DataRecord dataRecord) {
        this.dataRecord = dataRecord;
        this.xmlPrefix = xmlPrefix;
        this.rangeTypeElementName = rangeTypeElementName;
        this.xmlNamespace = xmlNamespace;
    }

    public DataRecord getDataRecord() {
        return dataRecord;
    }

    public void setDataRecord(DataRecord dataRecord) {
        this.dataRecord = dataRecord;
    }

    @Override
    public Element serializeToXMLElement() {
        
        // for CIS 1.0, prefix is gmlcov:rangeType, for CIS 1.1, prefix is cis11:rangeType
        Element rangeTypeElement = new Element(XMLUtil.createXMLLabel(this.xmlPrefix, this.rangeTypeElementName), this.xmlNamespace);
        Element dataRecordElement = this.dataRecord.serializeToXMLElement();
        rangeTypeElement.appendChild(dataRecordElement);
        
        return rangeTypeElement;
    }
}
