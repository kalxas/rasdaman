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
package petascope.wms2.metadata.dao;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.metadata.*;

import java.sql.SQLException;

/**
 * This class keeps track of the persistence configuration options.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class PersistenceConfig {

    /**
     * Constructor for the class
     *
     * @param databaseConnectionUrl      the connection url to the database to be used for persistence
     * @param databaseConnectionUser     the connection user to the database
     * @param databaseConnectionPassword the connection password to the database
     */
    public PersistenceConfig(@NotNull String databaseConnectionUrl, @NotNull String databaseConnectionUser, @NotNull String databaseConnectionPassword) {
        this.databaseConnectionUrl = databaseConnectionUrl;
        this.databaseConnectionUser = databaseConnectionUser;
        this.databaseConnectionPassword = databaseConnectionPassword;
    }

    /**
     * Returns the connection to the persistence layer
     *
     * @return the connection to the persistence layer
     * @throws java.sql.SQLException
     */
    @NotNull
    public ConnectionSource getPersistenceConnection() throws SQLException {
        if (persistenceConnection == null) {
            persistenceConnection = new JdbcConnectionSource(getDatabaseConnectionUrl(), getDatabaseConnectionUser(), getDatabaseConnectionPassword());
        }
        return persistenceConnection;
    }

    /**
     * Returns the url to connect to the metadata database
     *
     * @return a jdbc connection url
     */
    @NotNull
    String getDatabaseConnectionUrl() {
        return databaseConnectionUrl;
    }

    /**
     * Returns the username for the database connection
     *
     * @return the username for the database connection
     */
    @NotNull
    String getDatabaseConnectionUser() {
        return databaseConnectionUser;
    }

    /**
     * Returns the password for the database connection
     *
     * @return the password for the database connection
     */
    @NotNull
    String getDatabaseConnectionPassword() {
        return databaseConnectionPassword;
    }

    /**
     * Returns all the metadata classes that need to be persisted
     *
     * @return the persistent metadata class names
     */
    public Class[] getMetadataClasses() {
        return metadataClasses;
    }

    @NotNull
    private final String databaseConnectionUrl;

    @NotNull
    private final String databaseConnectionUser;

    @NotNull
    private final String databaseConnectionPassword;

    @Nullable
    private ConnectionSource persistenceConnection = null;

    /**
     * All metadata classes. If a new metadata class is added it should be listed here for it to be initialized
     */
    private final Class[] metadataClasses = new Class[]{Attribution.class, AuthorityURL.class,
            BoundingBox.class, ContactInformation.class, Crs.class, DataURL.class, EXGeographicBoundingBox.class, ExceptionFormat.class,
            GetCapabilitiesFormat.class, GetMapFormat.class, Layer.class, LegendURL.class, MetadataURL.class, Service.class,
            ServiceKeyword.class, Style.class, RasdamanLayer.class, Dimension.class
    };
}
