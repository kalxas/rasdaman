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

package petascope.wms2.util;

import org.jetbrains.annotations.NotNull;

/**
 * Keeps track of configuration options for the WMS service. Please keep this file as small as possible, only
 * configuration options that should be shared across the service.
 * You should gain access to this class via the ServiceOrchestrator
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class ConfigManager {

    /**
     * Constructor for the class
     */
    public ConfigManager() {
    }

    /**
     * Returns the service name. According to the standard this is always WMS
     *
     * @return the service name
     */
    @NotNull
    public String getServiceName() {
        return SERVICE_NAME;
    }

    /**
     * Returns the version of the standard implemented.
     *
     * @return the version of the standard
     */
    @NotNull
    public String getVersion() {
        return VERSION;
    }

    /**
     * Returns the xml schema location
     *
     * @return the schema location
     */
    public String getSchemaLocation() {
        return SCHEMA_LOCATION;
    }

    /**
     * Returns the max size of the cache
     *
     * @return the max size of the cache in bytes
     */
    public long getMaxSizeOfCache() {
        return MAX_SIZE_OF_CACHE;
    }

    /**
     * Returns the version parameter name
     * @return the version parameter name
     */
    public String getVersionParam() {
        return VERSION_PARAM;
    }

    @NotNull
    private static final String SERVICE_NAME = "WMS";

    @NotNull
    private static final String VERSION = "1.3.0";

    private static final String SCHEMA_LOCATION = "http://www.opengis.net/wms " +
        "http://schemas.opengis.net/wms/1.3.0/capabilities_1_3_0.xsd ";

    private static final String VERSION_PARAM = "version";

    private static final long MAX_SIZE_OF_CACHE = 50 * 1000 * 1000; //50MB

}
