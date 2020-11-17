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
package petascope.core.gml.cis10.model.metadata;

import nu.xom.Element;
import static petascope.core.XMLSymbols.LABEL_COVERAGE_METADATA;
import static petascope.core.XMLSymbols.LABEL_EXTENSION;
import static petascope.core.XMLSymbols.LABEL_METADATA;
import static petascope.core.XMLSymbols.NAMESPACE_GMLCOV;
import static petascope.core.XMLSymbols.NAMESPACE_RASDAMAN;
import static petascope.core.XMLSymbols.PREFIX_GMLCOV;
import static petascope.core.XMLSymbols.PREFIX_RASDAMAN;
import static petascope.core.XMLSymbols.PREFIX_XMLNS;
import petascope.core.gml.ISerializeToXMElement;
import petascope.exceptions.PetascopeException;
import petascope.util.XMLUtil;

/**
 * Class represent metadata element in CIS 1.0.
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
        Element metadataElement = new Element(XMLUtil.createXMLLabel(PREFIX_GMLCOV, LABEL_METADATA), NAMESPACE_GMLCOV); 

        if (metadata != null && !metadata.isEmpty()) {
            Element gmlExtensionElement = new Element(XMLUtil.createXMLLabel(PREFIX_GMLCOV, LABEL_EXTENSION), NAMESPACE_GMLCOV);
            metadataElement.appendChild(gmlExtensionElement);
            
            Element covMetadateElement = null;
            metadata = XMLUtil.stripXMLDeclaration(metadata);
            
            if (XMLUtil.hasXMLNameSpaceAtRootElement(metadata)) {
                // metadata has its own namspaces, e.g: INSPIRE, don't add rasdaman:covMetadata wrapper element
                covMetadateElement = XMLUtil.parseXmlFragment(metadata);
            } else {
                // metadata doesn't have its own namespaces (metadata from wcst_import), then it needs to add rasdaman:covMetadata wrapper element
                // to be valid from CIS 1.0 metadata schema validation
                String covMetadataXML = XMLUtil.createXMLString(NAMESPACE_RASDAMAN, PREFIX_RASDAMAN, LABEL_COVERAGE_METADATA, metadata);
                covMetadateElement = XMLUtil.parseXmlFragment(covMetadataXML);
            }
            
            gmlExtensionElement.appendChild(covMetadateElement);
        }
        
        return metadataElement;
    }
}
