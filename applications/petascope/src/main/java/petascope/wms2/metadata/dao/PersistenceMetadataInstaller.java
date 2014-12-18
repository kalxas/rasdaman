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

import com.j256.ormlite.table.TableUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Installation class for the storage of persistent metadata objects.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class PersistenceMetadataInstaller {


    /**
     * Constructor for the class
     *
     * @param config              a configuration object for persistence
     * @param persistenceProvider the provider for the persistence objects
     */
    public PersistenceMetadataInstaller(@NotNull PersistenceConfig config, @NotNull PersistentMetadataObjectProvider persistenceProvider) {
        this.config = config;
        this.persistenceProvider = persistenceProvider;
    }

    /**
     * Installs the persistence system.
     * WARNING: This does not check if the system is installed, use with caution
     *
     * @throws SQLException
     */
    private void install() throws SQLException {
        createDatabaseTables();
    }

    /**
     * Installs the persistence system if it is not already installed. This method is safe to be called in any situation
     * although can be quite expensive.
     *
     * @throws SQLException
     */
    public void installIfNotInstalled() throws SQLException {
        if (!checkExistingInstallation()) {
            install();
        }
    }

    /**
     * Creates the database tables for each persistence metadata object
     *
     * @throws SQLException
     */
    private void createDatabaseTables() throws SQLException {
        try {
            logger.info("Installing WMS Tables: ");
            for (Class metadataClass : config.getMetadataClasses()) {
                if (!persistenceProvider.getMetadataClassByName(metadataClass).isTableExists()) {
                    TableUtils.createTableIfNotExists(config.getPersistenceConnection(), metadataClass);
                }
            }
            logger.info("WMS Installation successful");
        } catch (SQLException e) {
            logger.error("WMS Installation failed. The SQL error is appended: ", e);
            throw e;
        }
    }

    /**
     * Clears the database tables of data
     *
     * @throws SQLException
     */
    public void clearDatabaseTables() throws SQLException {
        try {
            logger.info("Installing WMS Tables: ");
            for (Class metadataClass : config.getMetadataClasses()) {
                TableUtils.createTableIfNotExists(config.getPersistenceConnection(), metadataClass);
            }
            logger.info("WMS Installation successful");
        } catch (SQLException e) {
            logger.error("WMS Installation failed. The SQL error is appended: ", e);
            throw e;
        }
    }


    /**
     * Drops the database tables
     *
     * @throws SQLException
     */
    public void dropDatabaseTables() throws SQLException {
        try {
            logger.info("Installing WMS Tables: ");
            for (Class metadataClass : config.getMetadataClasses()) {
                TableUtils.dropTable(config.getPersistenceConnection(), metadataClass, true);
            }
            logger.info("WMS Installation successful");
        } catch (SQLException e) {
            logger.error("WMS Installation failed. The SQL error is appended: ", e);
            throw e;
        }
    }

    /**
     * Checks if the installation is valid by checking if all the tables are in place.
     *
     * @return true if the installation is valid, false otherwise.
     * @throws SQLException
     */
    private boolean checkExistingInstallation() throws SQLException {
        boolean installationIsValid = true;
        for (Class metadataClass : config.getMetadataClasses()) {
            installationIsValid = installationIsValid && persistenceProvider.getMetadataClassByName(metadataClass).isTableExists();
        }
        return installationIsValid;
    }


    @NotNull
    private final PersistenceConfig config;
    @NotNull
    private final PersistentMetadataObjectProvider persistenceProvider;
    @NotNull
    private final Logger logger = LoggerFactory.getLogger(PersistenceMetadataInstaller.class);

}
