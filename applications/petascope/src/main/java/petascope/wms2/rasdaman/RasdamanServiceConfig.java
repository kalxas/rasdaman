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

package petascope.wms2.rasdaman;

/**
 * Configuration class for a rasdaman service
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class RasdamanServiceConfig {

    /**
     * Constructor for the class
     *
     * @param rasdamanUrl      the url to the rasdaman connection
     * @param databaseName     the name of the rasdaman database to open
     * @param rasdamanUser     the rasdaman user to be used by the service
     * @param rasdamanPassword the rasdaman password to be used by the service
     */
    public RasdamanServiceConfig(String rasdamanUrl, String databaseName, String rasdamanUser, String rasdamanPassword) {
        this.databaseName = databaseName;
        this.rasdamanUrl = rasdamanUrl;
        this.rasdamanUser = rasdamanUser;
        this.rasdamanPassword = rasdamanPassword;
    }

    /**
     * Gets the rasdaman url
     *
     * @return the rasdaman url
     */
    public String getRasdamanUrl() {
        return rasdamanUrl;
    }

    /**
     * Gets the rasdaman user
     *
     * @return the rasdaman user
     */
    public String getRasdamanUser() {
        return rasdamanUser;
    }

    /**
     * Gets the rasdaman password
     *
     * @return the rasdaman password
     */
    public String getRasdamanPassword() {
        return rasdamanPassword;
    }

    /**
     * Gets the name of the database to be used
     *
     * @return the rasdaman database name
     */
    public String getDatabaseName() {
        return databaseName;
    }

    private final String databaseName;
    private final String rasdamanUrl;
    private final String rasdamanUser;
    private final String rasdamanPassword;
}
