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
package petascope.core.gml.cis11.model.metadata;

import java.io.IOException;
import nu.xom.Element;
import nu.xom.ParsingException;
import static petascope.core.XMLSymbols.LABEL_METADATA;
import static petascope.core.XMLSymbols.PREFIX_CIS11;
import petascope.core.gml.ISerializeToXMElement;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.XMLUtil;
import static petascope.core.XMLSymbols.NAMESPACE_CIS_11;

/**
 * Class represent metadata element in CIS 1.1.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Metadata implements ISerializeToXMElement {
   
    private String metadata;

    public Metadata(String metadata) {
        this.metadata = metadata;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public Element serializeToXMLElement() throws PetascopeException {
        String metadataElementStr = XMLUtil.createXMLString(NAMESPACE_CIS_11, PREFIX_CIS11, LABEL_METADATA, metadata);
        Element metadataElement = null;
        metadataElement = XMLUtil.parseXmlFragment(metadataElementStr);
        
        return metadataElement;
    }
}
