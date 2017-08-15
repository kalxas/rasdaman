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
import static org.rasdaman.InitAllConfigurationsApplicationService.KEY_GDAL_JAVA_DIR;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.InitAllConfigurationsApplicationService.KEY_PETASCOPE_CONF_DIR;
import static org.rasdaman.InitAllConfigurationsApplicationService.addLibraryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.util.DatabaseUtil;

@SpringBootApplication
/**
 * NOTE: this class is only used to initialize configurations for ApplicationMigration class.
 * Not sure why with @autowired directly in this class, bean object is null but not in the ApplicationMigration class.
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class InitializeApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InitializeApplication.class);

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
    public PropertySourcesPlaceholderConfigurer placeholderConfigurer() throws FileNotFoundException, IOException, SQLException, ClassNotFoundException, PetascopeException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        String resourceName = APPLICATION_PROPERTIES_FILE; // could also be a constant
        Properties properties = new Properties();
        InputStream resourceStream = this.getClass().getResourceAsStream("/" + resourceName);
        properties.load(resourceStream);

        PropertySourcesPlaceholderConfigurer propertyResourcePlaceHolderConfigurer = new PropertySourcesPlaceholderConfigurer();
        File initialFile = new File(properties.getProperty(KEY_PETASCOPE_CONF_DIR) + "/" + ConfigManager.PETASCOPE_PROPERTIES_FILE);
        propertyResourcePlaceHolderConfigurer.setLocation(new FileSystemResource(initialFile));

        // Init all properties for ConfigManager
        initConfigurations(properties);
        
        // NOTE: Cannot migrate with same JDBC URLs (e.g: jdbc:postgresql://localhost:5432/petascopedb)
        // except with the migration of legacy petascope'db prior version 9.5
        if (!DatabaseUtil.legacyPetascopeDatabaseExists() && ConfigManager.LEGACY_DATASOURCE_URL.equalsIgnoreCase(ConfigManager.PETASCOPE_DATASOURCE_URL)) {
            log.error("Cannot migrate petascope's database with same JDBC URL: " + ConfigManager.LEGACY_DATASOURCE_URL);
            System.exit(ApplicationMigration.ExitCode.FAILURE.getExitCode());
        }
        
        return propertyResourcePlaceHolderConfigurer;
    }

    /**
     * Initialize all the configurations for GDAL libraries, ConfigManager and
     * OGC WCS XML Schema
     */
    private void initConfigurations(Properties properties) throws SQLException, ClassNotFoundException, PetascopeException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
        String GDAL_JAVA_DIR = properties.getProperty(KEY_GDAL_JAVA_DIR);
        String CONF_DIR = properties.getProperty(KEY_PETASCOPE_CONF_DIR);
        try {
            // Load the GDAL native libraries (no need to set in IDE with VM options: -Djava.library.path="/usr/lib/java/gdal/")            
            addLibraryPath("gdal_java", GDAL_JAVA_DIR);
            // Load properties for Spring, Hibernate from external petascope.properties
            ConfigManager.init(CONF_DIR);
            // Create the new database if not exist
            DatabaseUtil.createDatabaseIfNotExist(null);
        } catch (RasdamanException ex) {
            throw new RuntimeException("Could not initialize config manager for petascope.properties", ex);
        }
    }

 
    /**
     * Only used to initialize configurations for ApplicationMigration class.
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
    }
}
