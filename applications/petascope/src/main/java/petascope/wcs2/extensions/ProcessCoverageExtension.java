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
 * Implementation of the Extension interface for the Process Coverage Extension defined in
 * the  OGC Web Coverage Service (WCS)– Processing Extension, version OGC.08-059r4
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ProcessCoverageExtension implements Extension {
    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.PROCESS_COVERAGE_IDENTIFIER;
    }

    public static final String WCPS_20_VERSION_STRING = "2.0";
    public static final String WCPS_10_VERSION_STRING = "1.0";
    public static final String WCPS_EXTRA_PARAM_PREFIX = "$";
}
