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
import java.sql.SQLException;
import java.util.Properties;
import static org.rasdaman.InitAllConfigurationsApplicationService.APPLICATION_PROPERTIES_FILE;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.InitAllConfigurationsApplicationService.KEY_GDAL_JAVA_DIR;
import static org.rasdaman.InitAllConfigurationsApplicationService.KEY_PETASCOPE_CONF_DIR;
import static org.rasdaman.InitAllConfigurationsApplicationService.addLibraryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import petascope.controller.handler.service.AbstractHandler;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.util.DatabaseUtil;
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
public class ApplicationMain extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationMain.class);

    @Autowired
    AbstractHandler abstractHandler;

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
    public PropertySourcesPlaceholderConfigurer placeholderConfigurer() throws FileNotFoundException, IOException, SQLException, ClassNotFoundException, PetascopeException, InterruptedException {
        String resourceName = APPLICATION_PROPERTIES_FILE; // could also be a constant
        Properties properties = new Properties();
        InputStream resourceStream = this.getClass().getResourceAsStream("/" + resourceName);
        properties.load(resourceStream);

        PropertySourcesPlaceholderConfigurer propertyResourcePlaceHolderConfigurer = new PropertySourcesPlaceholderConfigurer();
        File initialFile = new File(properties.getProperty(KEY_PETASCOPE_CONF_DIR) + "/" + ConfigManager.PETASCOPE_PROPERTIES_FILE);
        propertyResourcePlaceHolderConfigurer.setLocation(new FileSystemResource(initialFile));

        // Init all properties for ConfigManager
        initConfigurations(properties);

        // NOTE: If legacy petascopedb exists, don't start web application or Liquibase will populate tables to same database
        if (DatabaseUtil.legacyPetascopeDatabaseExists()) {
            throw new PetascopeException(ExceptionCode.InternalSqlError, "petascopedb 9.4 or older already exists, "
                    + "please run the migrate_petascopedb.sh script to migrate to the new petascope schema first.");
        }

        return propertyResourcePlaceHolderConfigurer;
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // This one will be invoked only when running in web application container (i.e: not as jar file)
        return builder.sources(ApplicationMain.class);
    }

    public static void main(String[] args) throws Exception {        
        SpringApplication.run(ApplicationMain.class, args);
    }

    /**
     * Initialize all the configurations for GDAL libraries, ConfigManager and
     * OGC WCS XML Schema
     */
    private void initConfigurations(Properties properties) throws SQLException, ClassNotFoundException, PetascopeException, IOException, InterruptedException {                
        String GDAL_JAVA_DIR = properties.getProperty(KEY_GDAL_JAVA_DIR);
        String CONF_DIR = properties.getProperty(KEY_PETASCOPE_CONF_DIR);
        try {
            // Load the GDAL native libraries (no need to set in IDE with VM options: -Djava.library.path="/usr/lib/java/gdal/")        
            addLibraryPath("gdal_java", GDAL_JAVA_DIR);
            // Load properties for Spring, Hibernate from external petascope.properties
            ConfigManager.init(CONF_DIR);
            // Load all the type registry (set, mdd, base types) of rasdaman
            TypeRegistry.getInstance();
            // Load the WCS Schema to validation if it is needed
            XMLAbstractParser.loadWcsSchema();

            // Create the new database if not exist
            DatabaseUtil.createDatabaseIfNotExist(null);
        } catch (RasdamanException ex) {
            throw new RuntimeException("Could not initialize config manager for petascope.properties", ex);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException("Could not add GDAL java native library from '" + GDAL_JAVA_DIR + "' to java library path.", ex);
        } catch (WCSException ex) {
            throw new RuntimeException("Could not load the OGC WCS Schema to validate POST/SOAP requests.", ex);
        }
    }
}
