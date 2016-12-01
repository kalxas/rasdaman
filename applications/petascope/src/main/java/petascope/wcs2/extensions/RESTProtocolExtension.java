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
package petascope.wcs2.extensions;

import petascope.HTTPRequest;

/**
 * A superclass for REST protocol binding extensions, which provides some
 * convenience methods to concrete implementations.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
public class RESTProtocolExtension extends AbstractProtocolExtension {

    public boolean canHandle(HTTPRequest request) {
        return request.getUrlPath().contains(RESTProtocolExtension.REST_PROTOCOL_WCS_IDENTIFIER);
    }

    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.REST_IDENTIFIER;
    }

    /**
     * @return False: this extension has is no parent extension with identifier.
     */
    public Boolean hasParent() {
        return false;
    }

    /**
     * @return The identifier of the parent extension.
     */
    public String getParentExtensionIdentifier() {
        return "";
    }

    public static String mapRestResourcesToCoverageOperation(String restResource) {
        if (restResource.contains("capabilities")) {
            return "GetCapabilities";
        } else if (restResource.contains("coverage") && restResource.contains("description")) {
            return "DescribeCoverage";
        } else if (restResource.contains("coverage")) {
            return "GetCoverage";
        }
        return "";
    }
    public static final String REST_PROTOCOL_WCS_IDENTIFIER = "wcs";
    public static final String REST_PROTOCOL_WCPS_IDENTIFIER = "wcps";
}
