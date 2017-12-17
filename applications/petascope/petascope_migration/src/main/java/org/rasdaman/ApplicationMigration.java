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
import java.util.List;
import java.util.Properties;
import javax.annotation.Resource;
import static org.rasdaman.InitAllConfigurationsApplicationService.APPLICATION_PROPERTIES_FILE;
import static org.rasdaman.InitAllConfigurationsApplicationService.KEY_GDAL_JAVA_DIR;
import static org.rasdaman.InitAllConfigurationsApplicationService.KEY_PETASCOPE_CONF_DIR;
import static org.rasdaman.InitAllConfigurationsApplicationService.addLibraryPath;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.migration.service.AbstractMigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.util.DatabaseUtil;

@SpringBootApplication
@ComponentScan({"org.rasdaman", "petascope"})
@PropertySource({"classpath:application.properties"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
/**
 * Main class of migration application.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class ApplicationMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ApplicationMigration.class);

    @Resource
    // Spring finds all the subclass of AbstractMigrationService and injects to the list
    List<AbstractMigrationService> migrationServices;
    
    private static Properties properties;
    
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
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() throws FileNotFoundException, IOException, SQLException, ClassNotFoundException, PetascopeException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InterruptedException {
        String resourceName = APPLICATION_PROPERTIES_FILE; // could also be a constant
        properties = new Properties();
        InputStream resourceStream = ApplicationMigration.class.getResourceAsStream("/" + resourceName);
        properties.load(resourceStream);

        PropertySourcesPlaceholderConfigurer propertyResourcePlaceHolderConfigurer = new PropertySourcesPlaceholderConfigurer();
        File initialFile = new File(properties.getProperty(KEY_PETASCOPE_CONF_DIR) + "/" + ConfigManager.PETASCOPE_PROPERTIES_FILE);
        propertyResourcePlaceHolderConfigurer.setLocation(new FileSystemResource(initialFile));

        // Init all properties for ConfigManager
        initConfigurations(properties);
        
        if (!DatabaseUtil.petascopeDatabaseExists()) {
            // NOTE: petascopedb doesn't exist to migrate, just consider it is a success instead of failure.
            log.info("petascopedb doesn't exist, nothing to migrate.");
            System.exit(ApplicationMigration.ExitCode.SUCCESS.getExitCode());
        }
        
        return propertyResourcePlaceHolderConfigurer;
    }

    /**
     * Initialize all the configurations for GDAL libraries, ConfigManager and
     * OGC WCS XML Schema
     */
    private static void initConfigurations(Properties properties) throws SQLException, ClassNotFoundException, PetascopeException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException, InterruptedException {        
        String confDir = properties.getProperty(KEY_PETASCOPE_CONF_DIR);
        try {
            // Load properties for Spring, Hibernate from external petascope.properties
            ConfigManager.init(confDir);            
        } catch (RasdamanException ex) {
            throw new RuntimeException("Could not initialize config manager for petascope.properties", ex);
        }
    }

    /**
     * Return the exit code to user
     */
    public enum ExitCode {
        SUCCESS(0),
        FAILURE(1);

        private final int value;

        private ExitCode(int value) {
            this.value = value;
        }

        public int getExitCode() {
            return this.value;
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            SpringApplication.run(ApplicationMigration.class, args);
        } catch (Exception ex) {
            // NOTE: always return error code or migrate_petascopedb.sh script will notice it migrated successfully.
            log.error("An error occured while migrating, aborting the migration process.", ex);            
            System.exit(ExitCode.FAILURE.getExitCode());                    
        }
    }

    @Override
    public void run(String... args) throws Exception {
        
        // Load the GDAL native libraries (no need to set in IDE with VM options: -Djava.library.path="/usr/lib/java/gdal/")
        String gdalJavaDir = properties.getProperty(KEY_GDAL_JAVA_DIR);
        addLibraryPath("gdal_java", gdalJavaDir);
        
        log.info("Migrating petascopedb from JDBC URL '" + ConfigManager.LEGACY_DATASOURCE_URL + "' to JDBC URL '" + ConfigManager.PETASCOPE_DATASOURCE_URL + "'...");
        // First, check if old JDBC URL can be connected
        if (!DatabaseUtil.checkJDBCConnection(ConfigManager.LEGACY_DATASOURCE_URL,
                ConfigManager.LEGACY_DATASOURCE_USERNAME, ConfigManager.LEGACY_DATASOURCE_PASSWORD)) {
            log.error("Cannot connect to existing petascopedb database at JDBC URL '" + ConfigManager.LEGACY_DATASOURCE_URL + "', aborting the migration process.");
            System.exit(ExitCode.FAILURE.getExitCode());
        }

        /*
            NOTE: Hibernate already connected when migration application starts,
            so With the embedded database, if another connection tries to connect, it will return exception.
         */
        // Then, check what kind of migration should be done
        for (AbstractMigrationService migrationService : migrationServices) {
            if (migrationService.isMigrating()) {
                // A migration process is running, don't do anything else
                log.error("A migration process is already running.");
                System.exit(ExitCode.FAILURE.getExitCode());
            }

            // NOTE: There are 2 types of migration:
            // + From legacy petascopedb prior version 9.5, it checks if petascopedb contains a legacy table name then it migrates to new petascopedb version 9.5.
            // + From petascopedb after version 9.5 to a different database, it checks if both source JDBC, target JDBC can be connected then it migrates entities to new database.
            if (migrationService.canMigrate()) {
                try {
                    migrationService.migrate();
                } catch (Exception ex) {
                    log.error("An error occured while migrating, aborting the migration process.", ex);
                    // Release the lock on Migration table so later can run migration again
                    migrationService.releaseLock();
                    System.exit(ExitCode.FAILURE.getExitCode());
                }
                // Just do one migration
                break;
            }
        }

        log.info("petascopedb migrated successfully.");
        System.exit(ExitCode.SUCCESS.getExitCode());
    }
}
