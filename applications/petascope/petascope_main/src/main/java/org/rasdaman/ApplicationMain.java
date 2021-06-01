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
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.gdal.gdal.gdal;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.migration.service.AbstractMigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import petascope.controller.AbstractController;
import petascope.core.GeoTransform;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsProjectionUtil;
import petascope.util.ras.TypeRegistry;
import petascope.wcs2.parsers.request.xml.XMLAbstractParser;
import static org.rasdaman.config.ConfigManager.STATIC_HTML_DIR_PATH;
import org.rasdaman.datamigration.DataMigrationService;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class initializes the petascope properties and runs the application as jar file.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author Dimitar Misev
 */
@SpringBootApplication
@EnableCaching

// NOTE: When the repository/entity/compontent package 
// is different to @SpringBootApplication (org.rasdaman for this main file),
// basePackages is required to be defined explicitly.
@EnableJpaRepositories(basePackages = {"com.rasdaman", "org.rasdaman"})
// Scan packages which contains Entities to create tables in database
@EntityScan(basePackages = {"com.rasdaman", "org.rasdaman", "petascope"}) 
@ComponentScan(basePackages = {"com.rasdaman", "org.rasdaman", "petascope"})
// NOTE: classpath is important when running as war package or it will have error: resource not found
@PropertySource({"classpath:application.properties"})
public class ApplicationMain extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationMain.class);
    
    public static final String APPLICATION_PROPERTIES_FILE = "application.properties";
    // path to gdal native files (.so) which are needed for GDAL java to invoke.
    private static final String PREFIX_INPUT_PARAMETER = "--";
    private static final String KEY_PETASCOPE_CONF_DIR = "petascope.confDir";
    private static final String KEY_MIGRATE = "migrate";
    
    // When invoked from command line (e.g: java -jar rasdaman.war), the migration
    // is set with a command-line parameter --migrate which makes this option true.
    public static boolean MIGRATE = false;
    
    // Only used when running embedded petascope to set a custom directory containing petascope.properties
    private static String OPT_PETASCOPE_CONFDIR = "";
    
    private static Properties applicationProperties = null;

    // Spring finds all the subclass of AbstractMigrationService and injects to the list
    @Resource
    List<AbstractMigrationService> migrationServices;
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    
    @Autowired
    private DataMigrationService dataMigrationService;

    /**
     * Invoked when running Petascope (rasdaman.war) only in an external servlet container. 
     * It loads properties files for both the Spring Framework and ConfigManager.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() throws Exception {
        return init();
    }
    
    @Bean
    WebMvcConfigurer configurer () {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addResourceHandlers (ResourceHandlerRegistry registry) {
                if (!STATIC_HTML_DIR_PATH.isEmpty()) {
                    // valid demo folder path as resource, e.g: file:///var/www/html/Earthlook_RASDAMAN_PROJECTS/bigdatacube/
                    registry.addResourceHandler("/**")
                            .addResourceLocations("classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/public/", 
                                                  "file://" + STATIC_HTML_DIR_PATH + "/");
                }
            }
        };
    }

    /**
     * Invoked when running Petascope (rasdaman.war) only in an external servlet container. 
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        setRegisterErrorPageFilter(false);
        return builder.sources(ApplicationMain.class);
    }

    /**
     * Load properties files and init all necessary configurations.
     */
    private static PropertySourcesPlaceholderConfigurer init() throws Exception {
        // load application.properties
        applicationProperties = loadApplicationProperties();

        // load petascope.properties
        PropertySourcesPlaceholderConfigurer ret = loadPetascopeProperties(applicationProperties);
        initConfigurations(applicationProperties);

        return ret;
    }

    private static Properties loadApplicationProperties() throws Exception {
        Properties properties = new Properties();
        properties.load(ApplicationMain.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES_FILE));
        return properties;
    }
    
    private static PropertySourcesPlaceholderConfigurer loadPetascopeProperties(Properties applicationProperties) throws Exception {
        PropertySourcesPlaceholderConfigurer ret = new PropertySourcesPlaceholderConfigurer();
        String petaPropsDir = OPT_PETASCOPE_CONFDIR;
        if (petaPropsDir.isEmpty()) {
            petaPropsDir = applicationProperties.getProperty(KEY_PETASCOPE_CONF_DIR);
        }
        File petaProps = new File(petaPropsDir, ConfigManager.PETASCOPE_PROPERTIES_FILE);
        ret.setLocation(new FileSystemResource(petaProps));
        return ret;
    }

    /**
     * Initialize all configurations for GDAL libraries, ConfigManager and OGC WCS XML Schema
     */
    private static void initConfigurations(Properties applicationProperties) throws Exception {
        if (OPT_PETASCOPE_CONFDIR.isEmpty()) {
            ConfigManager.init(applicationProperties.getProperty(KEY_PETASCOPE_CONF_DIR));
        } else {
            // properties file is configured from input parameters for embedded petascope
            ConfigManager.init(OPT_PETASCOPE_CONFDIR);
        }
        
        try {
            // Load all the type registry (set, mdd, base types) of rasdaman
            TypeRegistry.getInstance();
        } catch (Exception ex) {
            log.warn("Failed initializing type registry from rasdaman.", ex);
        }

        try {
            // Load the WCS Schema to validation if it is needed
            XMLAbstractParser.loadWcsSchema();
        } catch (Exception ex) {
            log.error("Failed loading the OGC WCS Schema for POST/SOAP request validation.", ex);
            AbstractController.startException = ex;
        }
    }
    
    private static void loadGdalLibrary() {
        
        // NOTE: to make sure that GDAL is loaded properly, do a simple CRS transformation here 
        // (if not, user has to restart Tomcat for JVM class loader does the load JNI again).
        try {
            gdal.AllRegister(); // should be done once on application startup
            // test projection
            GeoTransform sourceGT = new GeoTransform(4326, 0, 0, 1, 1, 0.5, 0.5);
            CrsProjectionUtil.getGeoTransformInTargetCRS(sourceGT, "EPSG:3857");
        } catch (Error | Exception ex) {
            String errorMessage = "Transform test failed, probably due to a problem with adding GDAL native library "
                                + "to java library path; please restart Tomcat to fix this problem. Reason: " + ex;
            AbstractController.startException = new PetascopeException(ExceptionCode.InternalComponentError, errorMessage);
        }
    }
    
    // -----------------------------------------------------------------------------------

    /**
     * Invoked when all beans are created to check it should run the migration
     * or not.
     */
    @PostConstruct
    private void postInit() throws Exception {
        
        loadGdalLibrary();
        initTrustAllSSL();

        if (MIGRATE && AbstractController.startException == null) {
            log.info("Migrating petascopedb from JDBC URL '" + ConfigManager.SOURCE_DATASOURCE_URL + 
                    "' to JDBC URL '" + ConfigManager.PETASCOPE_DATASOURCE_URL + "'...");
            /*
             * NOTE: Hibernate is already connected when migration application starts,
             * so with the embedded database, if another connection tries to connect, it will return exception.
             */
            // Then, check what kind of migration should be done
            // NOTE: There are 2 types of migration:
            // + From legacy petascopedb prior version 9.5, it checks if petascopedb contains a 
            //   legacy table name then it migrates to new petascopedb version 9.5.
            // + From petascopedb after version 9.5 to a different database, it checks if both source JDBC, 
            //   target JDBC can be connected then it migrates entities to new database.
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
                    log.error("An error occured while migrating, aborting the migration process. Reason: " + ex.getMessage());
                    // Release the lock on Migration table so later can run migration again
                    migrationService.releaseLock();
                    System.exit(ExitCode.FAILURE.getExitCode());
                }
            }
            log.info("petascopedb migrated successfully.");
            System.exit(ExitCode.SUCCESS.getExitCode());
        }
        
        // Run the 2 threads below in parallel to load coverages / layers to cache
        // and wait for them to finish before checking migration
//        CountDownLatch countDownLatch = new CountDownLatch(2);
//        this.loadCoveragesToCaches(this.coverageRepositoryService,                                    
//                                   countDownLatch);
//        this.loadLayersToCaches(this.wmsRepostioryService, countDownLatch);
//        
//        // Check if petascope should do the migration internally
//        countDownLatch.await();

        log.debug("Load coverages to caches ...");                    
        coverageRepositoryService.readAllLocalCoveragesBasicMetatata();
        coverageRepositoryService.createAllCoveragesExtents();
        
        log.debug("Load layers to caches ...");
        wmsRepostioryService.readAllLocalLayers();
        log.info("Loaded all local layer objects to cache.");
        

        this.dataMigrationService.runMigration();

    }
    
    /**
     * Run this background proces to load coverages to caches when petascope start
     */
    private void loadCoveragesToCaches(final CoverageRepositoryService coverageRepositoryService,             
            final CountDownLatch countDownLatch) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    log.debug("Load coverages to caches ...");                    
                    coverageRepositoryService.readAllLocalCoveragesBasicMetatata();
                    coverageRepositoryService.createAllCoveragesExtents();
                    
                    log.info("Loaded all local coverage objects to cache.");
                } catch (Exception ex) {
                    log.error("Cannot read local coverages to caches. Reason: " + ex.getMessage(), ex);
                }
                
                countDownLatch.countDown();
            }
        };
        new Thread(runnable).start();
    }
    
    /**
     * Run this background proces to load layers to caches when petascope start
     */
    private void loadLayersToCaches(final WMSRepostioryService wmsRepostioryService, final CountDownLatch countDownLatch) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    log.debug("Load layers to caches ...");
                    wmsRepostioryService.readAllLocalLayers();
                    log.info("Loaded all local layer objects to cache.");
                } catch (PetascopeException ex) {
                    log.error("Cannot read local layers to caches. Reason: " + ex.getMessage(), ex);
                }
                
                countDownLatch.countDown();
            }
        };
        new Thread(runnable).start();
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
     * Check if input argument is added when running embedded ptascope
     */
    private static boolean matchInputArgumentKey(String arg, String key) {
        if (arg.startsWith(PREFIX_INPUT_PARAMETER + key)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if arg is key=value format.
     */
    private static void checkValidKeyEqualsValue(String arg) throws PetascopeException {
        if (!arg.contains("=")) {
            throw new PetascopeException(ExceptionCode.InvalidParameterValue, "Input parameter must be key=value, given: "  + arg);
        }
    }

    /**
     * Main method when running embedded petascope with optional input parameters,
     * e.g: java -jar rasdaman.war --petascope.confDir=/opt/rasdaman/new_etc
     */
    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            if (matchInputArgumentKey(arg, KEY_PETASCOPE_CONF_DIR)) {
                checkValidKeyEqualsValue(arg);
                
                String value = arg.split("=")[1];
                OPT_PETASCOPE_CONFDIR = value;
            } else if (matchInputArgumentKey(arg, KEY_MIGRATE)) {
                MIGRATE = true;
            } else {
                throw new PetascopeException(ExceptionCode.NoApplicableCode, "Input parameter is not supported, given: " + arg);
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
     * Make rasfed to trust any SSL certificate. 
     * NOTE: It has problem with connecting to HTTPS with certificate trusting.
     */
    private static void initTrustAllSSL() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException ex) {
            log.warn("Cannot initialize all SSL trusting. "
                    + "Rasfed may not be able to connect to remote petascope via HTTPS protocol.", ex);
        }

    }
}
