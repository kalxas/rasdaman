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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.handlers;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import petascope.core.DbMetadataSource;
import static petascope.util.XMLSymbols.*;
import petascope.wcs2.parsers.Request;

/**
 * An abstract implementation of {@link WcsOperation}, which provides some
 * convenience methods to concrete implementations.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 * @param <T>
 */
public abstract class AbstractRequestHandler<T extends Request> implements RequestHandler<T> {
    
    protected final DbMetadataSource meta;

    public AbstractRequestHandler(DbMetadataSource meta) {
        this.meta = meta;
    }

    /**
     * Initializes an XML document with some required namespaces.
     *
     * @param name
     * @param namespace
     * @return document with root element with name in namespace
     */
    protected Document constructDocument(String name, String namespace) {
        Element root = new Element(name, namespace);

        root.setNamespacePrefix(PREFIX_WCS);
        root.setNamespaceURI(NAMESPACE_WCS);

        // xmlns:
        root.addNamespaceDeclaration(PREFIX_CRS, NAMESPACE_CRS);
        root.addNamespaceDeclaration(PREFIX_OWS, NAMESPACE_OWS);
        root.addNamespaceDeclaration(PREFIX_GML, NAMESPACE_GML);
        root.addNamespaceDeclaration(PREFIX_XSI, NAMESPACE_XSI);
        root.addNamespaceDeclaration(PREFIX_XLINK, NAMESPACE_XLINK);
        root.addNamespaceDeclaration(PREFIX_WCS, NAMESPACE_WCS);

        // schemaLocation
        root.addAttribute(
                new Attribute(PREFIX_XSI + ":" + ATT_SCHEMA_LOCATION, NAMESPACE_XSI,
                          NAMESPACE_WCS  + " " + SCHEMA_LOCATION_WCS)
                );

        return new Document(root);
    }
}
