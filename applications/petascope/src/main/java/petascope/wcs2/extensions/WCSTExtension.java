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

/**
 * Implementation of the Extension interface for the Transaction Extension of
 * OGC Web Coverage Service (WCS)
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */

public class WCSTExtension implements Extension {

    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.WCST_IDENTIFIER;
    }

    /**
     * @return False: this extension has no parent extension with identifier.
     */
    public Boolean hasParent() {
        return false;
    }

    /**
     * @return "": this extension has is no parent extension with identifier.
     */
    public String getParentExtensionIdentifier() {
        return "";
    }

}
