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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.io.FileUtils;
import static org.rasdaman.InitAllConfigurationsApplicationService.APPLICATION_PROPERTIES_FILE;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.InitAllConfigurationsApplicationService.KEY_GDAL_JAVA_DIR;
import static org.rasdaman.InitAllConfigurationsApplicationService.KEY_PETASCOPE_CONF_DIR;
import static org.rasdaman.InitAllConfigurationsApplicationService.addLibraryPath;
import org.rasdaman.migration.service.AbstractMigrationService;
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
import petascope.controller.AbstractController;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import petascope.util.CrsProjectionUtil;
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
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class ApplicationMain extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationMain.class);
    // This one is to determine when application is started inside a external servlet container (e.g: tomcat/webapps)
    // or it is invoked from command line (e.g: java -jar rasdaman.war), default is external.
    public static boolean MIGRATE = false;
    
    // NOTE: Only used when running embedded petascope, to set a path to a new directory 
    // which contains a customized petascope.properties (e.g: /opt/rasdaman/new_etc)
    private static String petascopeConfigDir = "";

    @Resource
    // Spring finds all the subclass of AbstractMigrationService and injects to the list
    List<AbstractMigrationService> migrationServices;

    /**
     * Load properties files and init all necessary configurations.
     *
     * @throws Exception
     */
    private static PropertySourcesPlaceholderConfigurer init() throws Exception {
        String resourceName = APPLICATION_PROPERTIES_FILE; // could also be a constant
        Properties properties = new Properties();
        InputStream resourceStream = ApplicationMain.class.getClassLoader().getResourceAsStream(resourceName);
        properties.load(resourceStream);

        PropertySourcesPlaceholderConfigurer propertyResourcePlaceHolderConfigurer = new PropertySourcesPlaceholderConfigurer();
        File initialFile = null;
        if (petascopeConfigDir.isEmpty()) {
             initialFile = new File(properties.getProperty(KEY_PETASCOPE_CONF_DIR) + "/" + ConfigManager.PETASCOPE_PROPERTIES_FILE);
        } else {
            initialFile = new File(petascopeConfigDir + "/" + ConfigManager.PETASCOPE_PROPERTIES_FILE);
        }
        
        propertyResourcePlaceHolderConfigurer.setLocation(new FileSystemResource(initialFile));
        initConfigurations(properties);

        return propertyResourcePlaceHolderConfigurer;
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

    /**
     * NOTE: This one is used when running Petascope (rasdaman.war) only inside
     * an external servlet application (not embedded one). And it is used to
     * invoke loading properties files both for Spring Framework and
     * ConfigManager.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() throws Exception {
        PropertySourcesPlaceholderConfigurer propertyResourcePlaceHolderConfigurer = init();
        return propertyResourcePlaceHolderConfigurer;
    }

    @Override
    /**
     * NOTE: This one will be invoked only when running in external servlet
     * application (i.e: not embedded).
     */
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ApplicationMain.class);
    }

    public static void main(String[] args) throws Exception {
        // user runs Petascope in the command line to migrate petascopedb
        for (String arg : args) {
            // In case of running java -jar rasdaman.war --petascope.confDir=/opt/rasdaman/new_etc
            if (arg.startsWith(KEY_PETASCOPE_CONF_DIR)) {
                String value = arg.split("=")[1];
                petascopeConfigDir = value;
            } else if (arg.startsWith("--migrate")) {
                MIGRATE = true;
            }
        }
        
        init();
        
        try {
            SpringApplication.run(ApplicationMain.class, args);
        } catch (Exception ex) {
            // NOTE: This class is private in Spring framework, cannot be imported here to compare by instanceof so must use class name to compare.
            if (!ex.getClass().getCanonicalName().equals("org.springframework.boot.devtools.restart.SilentExitExceptionHandler.SilentExitException")) {
                // There is a NULL error from Spring dev tools restarts Tomcat internally when a java file is saved (Compile on save) and this can be ignored in any cases.
                // NOTE: This error only happens when starting Petascope main application with mvn spring-boot:run for development.
                log.error("Error starting petascope with embedded Tomcat. Reason: " + ex.getMessage(), ex);
                System.exit(ExitCode.FAILURE.getExitCode());
            }
        }
    }

    /**
     * Initialize all the configurations for GDAL libraries, ConfigManager and
     * OGC WCS XML Schema
     */
    private static void initConfigurations(Properties properties) throws Exception {
        String GDAL_JAVA_DIR = properties.getProperty(KEY_GDAL_JAVA_DIR);
        String CONF_DIR = properties.getProperty(KEY_PETASCOPE_CONF_DIR);
        final String TMP_DIR = "gdal_java";
        try {
            // Load the GDAL native libraries (no need to set in IDE with VM options: -Djava.library.path="/usr/lib/java/gdal/")        
            addLibraryPath(TMP_DIR, GDAL_JAVA_DIR);
            // NOTE: to make sure that GDAL is loaded properly, do a simple CRS transformation here 
            // (if not, user has to restart Tomcat for JVM class loader does the load JNI again).
            CrsProjectionUtil.transform("EPSG:3857", "EPSG:4326", new double[] {0, 0});            
        } catch (Error ex) {
            String errorMessage = "Cannot add GDAL java native library from '" + GDAL_JAVA_DIR + "' to java library path, "
                    + "please restart Tomcat containing Petascope to fix this problem. Reason: " + ex.getMessage();
            log.error(errorMessage, ex);
            AbstractController.startException = new PetascopeException(ExceptionCode.InternalComponentError, errorMessage);
        } finally {
            final String tmpNativeParentFolderPath = ConfigManager.DEFAULT_PETASCOPE_DIR_TMP + "/" + TMP_DIR;
            File tmpNativeParentFolder = new File(tmpNativeParentFolderPath);
            // Clean anything inside /tmp/rasdaman_petascope/gdal-java/ as it either loaded to memory or failed to start
            if (tmpNativeParentFolder.exists()) {
                // Clean content of this temp directory for the gdal library as it is already loaded in JVM
                try {
                    FileUtils.cleanDirectory(tmpNativeParentFolder);
                } catch (IOException ex) {
                    log.warn("Cannot clear content of temp directory '" + tmpNativeParentFolder.getCanonicalPath() + "',"
                            + " please remove it manually. Reason:" + ex.getMessage(), ex);
                }
            }
        }

        // Load properties for Spring, Hibernate from external petascope.properties
        ConfigManager.init(CONF_DIR);
        try {
            // Load all the type registry (set, mdd, base types) of rasdaman
            TypeRegistry.getInstance();
        } catch (Exception ex) {
            log.warn("Failed initializing type registry from rasdaman.", ex);
        }

        try {
            // Load the WCS Schema to validation if it is needed
            XMLAbstractParser.loadWcsSchema();
        } catch (WCSException ex) {
            log.error("Cannot load the OGC WCS Schema to validate POST/SOAP requests.", ex);
            AbstractController.startException = ex;
        }
    }

    /**
     * Invoked when all beans are created to check it should run the migration
     * or not.
     */
    @PostConstruct
    private void handleMigrate() {

        if (MIGRATE && AbstractController.startException == null) {
            log.info("Migrating petascopedb from JDBC URL '" + ConfigManager.SOURCE_DATASOURCE_URL + "' to JDBC URL '" + ConfigManager.PETASCOPE_DATASOURCE_URL + "'...");
            /*
            NOTE: Hibernate already connected when migration application starts,
            so With the embedded database, if another connection tries to connect, it will return exception.
             */
            // Then, check what kind of migration should be done
            // NOTE: There are 2 types of migration:
            // + From legacy petascopedb prior version 9.5, it checks if petascopedb contains a legacy table name then it migrates to new petascopedb version 9.5.
            // + From petascopedb after version 9.5 to a different database, it checks if both source JDBC, target JDBC can be connected then it migrates entities to new database.
            for (AbstractMigrationService migrationService : migrationServices) {
                if (migrationService.isMigrating()) {
                    // A migration process is running, don't do anything else
                    log.error("A migration process is already running.");
                    System.exit(ExitCode.FAILURE.getExitCode());
                }
                
                try {
                    if (migrationService.canMigrate()) {
                        migrationService.migrate();
                        // Just do one migration
                        break;
                    }
                } catch (Exception ex) {
                    log.error("An error occured while migrating, aborting the migration process.\n Reason: " + ex.getMessage());
                    // Release the lock on Migration table so later can run migration again
                    migrationService.releaseLock();
                    System.exit(ExitCode.FAILURE.getExitCode());
                }
            }
            log.info("petascopedb migrated successfully.");
            System.exit(ExitCode.SUCCESS.getExitCode());
        }
    }
}
