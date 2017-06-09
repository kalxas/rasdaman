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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.config.ConfigManager.LEGACY_DATASOURCE_DRIVER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.util.ras.TypeRegistry;
import petascope.wcs2.parsers.request.xml.XMLAbstractParser;

@SpringBootApplication
@EnableCaching
@ComponentScan({"org.rasdaman", "petascope"})
// NOTE: classpath is important when running as war package or it will have error resource not found
@PropertySource({"classpath:application.properties"})
/**
 * This class initialize the Petascope properties then run the application as
 * jar file
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class Application extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    // path to gdal native files (.so) which are needed for GDAL java to invoke.
    private static final String APPLICATION_PROPERTIES_FILE = "application.properties";
    private static final String KEY_GDAL_JAVA_DIR = "gdal-java.libDir";
    private static final String KEY_PETASCOPE_CONF_DIR = "petascope.confDir";

    /**
     * NOTE: This one is use to load Petascope properties from external file
     * when Spring Environment is not wired (i.e: null). Then all the
     * configurations for application from petascope.properties is loaded.
     *
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     * @PropertySource in class level will not work as it requires a constant
     * path to petascope.properties.
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Bean
    public PropertySourcesPlaceholderConfigurer placeholderConfigurer() throws FileNotFoundException, IOException, SQLException, ClassNotFoundException, PetascopeException {
        String resourceName = APPLICATION_PROPERTIES_FILE; // could also be a constant
        Properties properties = new Properties();
        InputStream resourceStream = this.getClass().getResourceAsStream("/" + resourceName);
        properties.load(resourceStream);

        PropertySourcesPlaceholderConfigurer propertyResourcePlaceHolderConfigurer = new PropertySourcesPlaceholderConfigurer();
        File initialFile = new File(properties.getProperty(KEY_PETASCOPE_CONF_DIR) + "/" + ConfigManager.PETASCOPE_PROPERTIES_FILE);
        propertyResourcePlaceHolderConfigurer.setLocation(new FileSystemResource(initialFile));
        // also initialize all the ConfigManager properties
        initConfigurations(properties);

        return propertyResourcePlaceHolderConfigurer;
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // This one will be invoked only when running in web application container (i.e: not as jar file)
        return builder.sources(Application.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Initialize all the configurations for GDAL libraries, ConfigManager and
     * OGC WCS XML Schema
     */
    private void initConfigurations(Properties properties) throws SQLException, ClassNotFoundException, PetascopeException {
        String GDAL_JAVA_DIR = properties.getProperty(KEY_GDAL_JAVA_DIR);
        String CONF_DIR = properties.getProperty(KEY_PETASCOPE_CONF_DIR);
        try {
            // Load the GDAL native libraries (no need to set in IDE with VM options: -Djava.library.path="/usr/lib/java/gdal/")        
            addLibraryPath(GDAL_JAVA_DIR);
            // Load properties for Spring, Hibernate from external petascope.properties
            ConfigManager.init(CONF_DIR);
            // Load all the type registry (set, mdd, base types) of rasdaman
            TypeRegistry.getInstance();
            // Load the WCS Schema to validation if it is needed
            XMLAbstractParser.loadWcsSchema();

            // Create the database if not exist
            this.createDatabaseIfNotExist();
        } catch (RasdamanException ex) {
            throw new RuntimeException("Could not initialize config manager for petascope.properties", ex);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException("Could not add GDAL java native library from: " + GDAL_JAVA_DIR + " to java library path.", ex);
        } catch (WCSException ex) {
            throw new RuntimeException("Could not load the OGC WCS Schema to validate POST/SOAP request.", ex);
        }
    }

    /**
     * Adds the specified path to the java library path (very important to load
     * GDAL native libraries!!!)
     *
     * @param pathToAdd the path to add
     * @throws java.lang.NoSuchFieldException
     * @throws java.lang.IllegalAccessException
     */
    private static void addLibraryPath(String pathToAdd) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        // get array of paths
        final String[] paths = (String[]) usrPathsField.get(null);

        // check if the path to add is already present
        for (String path : paths) {
            if (path.equals(pathToAdd)) {
                return;
            }
        }

        //add the new path
        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length - 1] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }
    
    /**
     * In version 9.5, petascopedb will not exist as update_petascopedb.sh script is removed.
     * So don't create the bean for reading legacy database source as it will throw exception when initializing server.
     * @return 
     * @throws java.lang.ClassNotFoundException 
     * @throws java.sql.SQLException 
     */
    public static boolean checkLegacyDatabaseExist() throws ClassNotFoundException, SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName(LEGACY_DATASOURCE_DRIVER);
            connection = DriverManager.getConnection(ConfigManager.LEGACY_DATASOURCE_URL, ConfigManager.LEGACY_DATASOURCE_USERNAME, ConfigManager.LEGACY_DATASOURCE_PASSWORD);            
        } catch (SQLException ex) {
            log.info("Legacy petascopedb does not exist, no need to migrate data to new database.");
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                throw ex;
            }
        }
        
        log.info("Legacy petascopedb existed, need to migrate data to new database.");
        return true;
    }

    /**
     * Postgresql does not allow Hibernate to create database as other
     * relational databases so instead of throwing exception, it should create
     * it internally.
     *
     * NOTE: every relational database has different format for JDBC connection
     * URL, so only supports for Postgresql to create a database if not exist
     * now.
     *
     * Postgresql_URL: jdbc:postgresql://HOST/DATABASE_NAME
     */
    private void createDatabaseIfNotExist() throws SQLException, ClassNotFoundException {
        // No need to do anything if it is not Postgresql
        if (!ConfigManager.PETASCOPE_DATASOURCE_DRIVER.contains("postgresql")) {
            return;
        }
        int lastIndex = ConfigManager.PETASCOPE_DATASOURCE_URL.lastIndexOf("/");

        // template1 is a default database for postgresql to create empty database, it is not listed in pgAdmin III and it will not be removed as a temp database
        String defaultURL = ConfigManager.PETASCOPE_DATASOURCE_URL.substring(0, lastIndex) + "/" + "template1";
        String databaseName = ConfigManager.PETASCOPE_DATASOURCE_URL.substring(lastIndex + 1, ConfigManager.PETASCOPE_DATASOURCE_URL.length());

        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName(LEGACY_DATASOURCE_DRIVER);
            connection = DriverManager.getConnection(defaultURL, ConfigManager.LEGACY_DATASOURCE_USERNAME, ConfigManager.LEGACY_DATASOURCE_PASSWORD);
            // Then try to query from the database
            statement = connection.createStatement();
            String selectQuery = "select count(*) from pg_catalog.pg_database where datname = '" + databaseName + "'";
            String value = "0";
            try (ResultSet resultSet = statement.executeQuery(selectQuery)) {
                while (resultSet.next()) {
                    value = resultSet.getString("count");
                }
            }

            // If result returns zero, so database does not exist, create it
            if (value.equals("0")) {
                String sqlQuery = "CREATE DATABASE " + databaseName + "";
                statement = connection.createStatement();
                statement.executeUpdate(sqlQuery);
                log.info("Postgresql database: " + databaseName + " has successfully been created.");
            }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                throw ex;
            }
        }
    }
}
