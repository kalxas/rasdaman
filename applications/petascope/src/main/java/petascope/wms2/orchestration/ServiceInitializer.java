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

package petascope.wms2.orchestration;

import com.sun.istack.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.wms2.metadata.DefaultMetadataConstructor;
import petascope.wms2.metadata.dao.PersistenceConfig;
import petascope.wms2.metadata.dao.PersistenceMetadataInstaller;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;
import petascope.wms2.rasdaman.RasdamanService;
import petascope.wms2.rasdaman.RasdamanServiceConfig;
import petascope.wms2.service.base.RequestCacheEngine;
import petascope.wms2.util.ConfigManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Class to initialize all the instances needed by the service orchestrator. This will initialize the following:
 * - Persistence layer
 * - Config Manager
 * - Default Values for metadata
 * - Logging framework
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
class ServiceInitializer {

    /**
     * Constructor for the class
     *
     * @param pathToConfigurationDirectory the path to the configuration directory of petascope
     */
    public ServiceInitializer(@Nullable String pathToConfigurationDirectory) {
        if (pathToConfigurationDirectory == null) {
            throw new IllegalArgumentException("No configuration directory path could be detected." +
                "Please adjust the value of <param-name>confDir</param-name> in your web.xml file (usually at $CATALINA_HOME/webapps/petascope/WEB-INF/web.xml).");
        }
        this.pathToDefaultValuesFile = pathToConfigurationDirectory + "/" + WMS_SERVICE_DEFAULT_VALUES_FILE;
        this.pathToConfigurationFile = pathToConfigurationDirectory + "/" + SETTINGS_FILE;
        this.pathToLoggingFile = pathToConfigurationDirectory + "/" + LOGGING_CONF_FILE;
        checkConfigurationPath();
    }

    /**
     * Returns the initialized config manager
     *
     * @return the config manager
     */
    @NotNull
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Returns the initialized persistent metadata object provider
     *
     * @return the persistent metadata object provider
     */
    @NotNull
    public PersistentMetadataObjectProvider getPersistentMetadataObjectProvider() {
        return persistentMetadataObjectProvider;
    }

    /**
     * Returns the initialized cache engine
     *
     * @return the cache engine
     */
    @NotNull
    public RequestCacheEngine getCacheEngine() {
        return cacheEngine;
    }

    /**
     * Returns the initialized rasdaman service
     *
     * @return the rasdaman service
     */
    @NotNull
    public RasdamanService getRasdamanService() {
        return rasdamanService;
    }

    /**
     * This ensures that the system is correctly initialized and functions correctly
     */
    public void initializeSystem() {
        try {
            initializeConfigManager();
            initializePersistenceConfiguration();
            initializePersistenceLayer(persistenceConfig);
            initializeDefaultValues(persistentMetadataObjectProvider);
            initializeLogging();
            initializeCacheEngine(configManager);
            initializeRasdamanService();
        } catch (Exception e) {
            logger.error("The system could not be initialized! This was caused by the following error: ", e);
        }
    }


    /**
     * Checks the configuration path to contain the needed files
     */
    private void checkConfigurationPath() {
        File defaultFile = new File(pathToDefaultValuesFile);
        File configFile = new File(pathToConfigurationFile);
        File loggingFile = new File(pathToLoggingFile);
        if (!defaultFile.exists() || !defaultFile.canRead()) {
            throw new IllegalArgumentException("The file containing the default service provider information was not found at " + defaultFile.getAbsolutePath() +
                ". Please make sure that the file exists and is readable by the server user. If the file path listed above is wrong " +
                "please adjust the value of <param-name>confDir</param-name> in your web.xml file (usually at $CATALINA_HOME/webapps/petascope/WEB-INF/web.xml).");
        }
        if (!configFile.exists() || !configFile.canRead()) {
            throw new IllegalArgumentException("The file containing the configuration options for petascope was not found at " + configFile.getAbsolutePath() +
                ". Please make sure that the file exists and is readable by the server user. If the file path listed above is wrong " +
                "please adjust the value of <param-name>confDir</param-name> in your web.xml file (usually at $CATALINA_HOME/webapps/petascope/WEB-INF/web.xml).");
        }
        if (!loggingFile.exists() || !loggingFile.canRead()) {
            throw new IllegalArgumentException("The file containing the logging options was not found at " + loggingFile.getAbsolutePath() +
                ". Please make sure that the file exists and is readable by the server user. If the file path listed above is wrong " +
                "please adjust the value of <param-name>confDir</param-name> in your web.xml file (usually at $CATALINA_HOME/webapps/petascope/WEB-INF/web.xml).");
        }
    }

    /**
     * Initializes the cache engine
     *
     * @param configManager the config manager
     */
    private void initializeCacheEngine(ConfigManager configManager) {
        cacheEngine = new RequestCacheEngine(configManager.getMaxSizeOfCache());
    }

    /**
     * Initializes the persistence provider service
     *
     * @param persistenceConfig the configuration for the persistence layer
     * @throws java.sql.SQLException
     */
    private void initializePersistenceLayer(@NotNull PersistenceConfig persistenceConfig) throws SQLException {
        persistentMetadataObjectProvider = new PersistentMetadataObjectProvider(persistenceConfig.getPersistenceConnection());
        PersistenceMetadataInstaller installer = new PersistenceMetadataInstaller(persistenceConfig, persistentMetadataObjectProvider);
        installer.installIfNotInstalled();
    }

    /**
     * Initializes the default values for the metadata objects by reading from the petascope service file
     *
     * @param persistenceProvider the persistence provider for the metadata objects
     * @throws java.io.IOException
     * @throws SQLException
     */
    private void initializeDefaultValues(@NotNull PersistentMetadataObjectProvider persistenceProvider) throws IOException, SQLException {
        Properties properties = filePathToPropertiesObject(pathToDefaultValuesFile);
        DefaultMetadataConstructor metadataConstructor = new DefaultMetadataConstructor(properties, persistenceProvider);
        metadataConstructor.construct();
    }

    /**
     * Initializes the configuration manager by reading from petascope properties file the database settings
     *
     * @throws IOException
     */
    private void initializePersistenceConfiguration() throws IOException, SQLException {
        Properties properties = filePathToPropertiesObject(pathToConfigurationFile);
        persistenceConfig = new PersistenceConfig(
            properties.getProperty(KEY_METADATA_URL, "NONE").trim(),
            properties.getProperty(KEY_METADATA_USER, "NONE").trim(),
            properties.getProperty(KEY_METADATA_PASS, "NONE").trim()
        );
        persistentMetadataObjectProvider = new PersistentMetadataObjectProvider(persistenceConfig.getPersistenceConnection());
    }

    /**
     * Initializes the rasdaman service from the petascope properties configuration file
     *
     * @throws IOException
     */
    private void initializeRasdamanService() throws IOException {
        Properties properties = filePathToPropertiesObject(pathToConfigurationFile);
        RasdamanServiceConfig config = new RasdamanServiceConfig(
            properties.getProperty(KEY_RASDAMAN_URL),
            properties.getProperty(KEY_RASDAMAN_DATABASE),
            properties.getProperty(KEY_RASDAMAN_USER),
            properties.getProperty(KEY_RASDAMAN_PASSWORD)
        );
        rasdamanService = new RasdamanService(config);
    }

    /**
     * Initializes the logging framework
     */
    private void initializeLogging() {
        PropertyConfigurator.configure(pathToLoggingFile);
    }

    /**
     * Initializes the config manager
     */
    private void initializeConfigManager() {
        configManager = new ConfigManager();
    }

    /**
     * Given a path to a properties file, returns the properties object
     *
     * @param filePath the path to the file
     * @return the properties object
     * @throws IOException
     */
    private static Properties filePathToPropertiesObject(String filePath) throws IOException {
        InputStream stream = null;
        try {
            File propertiesFile = new File(filePath);
            stream = new FileInputStream(propertiesFile);
            Properties properties = new Properties();
            properties.load(stream);
            return properties;
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @NotNull
    private final String pathToDefaultValuesFile;
    @NotNull
    private final String pathToConfigurationFile;
    @NotNull
    private final String pathToLoggingFile;
    @NotNull
    private ConfigManager configManager;
    @NotNull
    private PersistenceConfig persistenceConfig;
    @NotNull
    private PersistentMetadataObjectProvider persistentMetadataObjectProvider;
    @NotNull
    private RequestCacheEngine cacheEngine;
    @NotNull
    private RasdamanService rasdamanService;
    @NotNull
    private final Logger logger = LoggerFactory.getLogger(ServiceInitializer.class);

    /**
     * Wrap around the petascope.ConfigManager constants so that we only couple to them in one place
     */
    @NotNull
    private final static String KEY_METADATA_URL = petascope.ConfigManager.KEY_METADATA_URL;
    @NotNull
    private final static String KEY_METADATA_USER = petascope.ConfigManager.KEY_METADATA_USER;
    @NotNull
    private final static String KEY_METADATA_PASS = petascope.ConfigManager.KEY_METADATA_PASS;
    @NotNull
    private final static String KEY_RASDAMAN_URL = petascope.ConfigManager.KEY_RASDAMAN_URL;
    @NotNull
    private final static String KEY_RASDAMAN_USER = petascope.ConfigManager.KEY_RASDAMAN_USER;
    @NotNull
    private final static String KEY_RASDAMAN_PASSWORD = petascope.ConfigManager.KEY_RASDAMAN_PASS;
    @NotNull
    private final static String KEY_RASDAMAN_DATABASE = petascope.ConfigManager.KEY_RASDAMAN_DATABASE;
    @NotNull
    private final static String SETTINGS_FILE = petascope.ConfigManager.SETTINGS_FILE;
    @NotNull
    private final static String LOGGING_CONF_FILE = petascope.ConfigManager.LOG_PROPERTIES_FILE;
    @NotNull
    private final static String WMS_SERVICE_DEFAULT_VALUES_FILE = "wms_service.properties";
}
