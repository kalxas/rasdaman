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
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
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
import petascope.controller.AbstractController;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsProjectionUtil;
import petascope.util.ras.TypeRegistry;
import petascope.wcs2.parsers.request.xml.XMLAbstractParser;

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
    public static final String KEY_GDAL_JAVA_DIR = "gdal-java.libDir";
    public static final String KEY_PETASCOPE_CONF_DIR = "petascope.confDir";
    
    private static final String TMP_GDAL_JAVA_DIR_NAME = "gdal_java";
    private static final String[] GDAL_LIB_PREFIXES = new String[] {"libgdal", "libogr", "libosr"};
    
    // When invoked from command line (e.g: java -jar rasdaman.war), the migration
    // is set with a command-line parameter --migrate which makes this option true.
    public static boolean MIGRATE = false;
    
    // Only used when running embedded petascope to set a custom directory containing petascope.properties
    private static String OPT_PETASCOPE_CONFDIR = "";

    // Spring finds all the subclass of AbstractMigrationService and injects to the list
    @Resource
    List<AbstractMigrationService> migrationServices;

    /**
     * Invoked when running Petascope (rasdaman.war) only in an external servlet container. 
     * It loads properties files for both the Spring Framework and ConfigManager.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() throws Exception {
        return init();
    }

    /**
     * Invoked when running Petascope (rasdaman.war) only in an external servlet container. 
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ApplicationMain.class);
    }

    /**
     * Load properties files and init all necessary configurations.
     */
    private static PropertySourcesPlaceholderConfigurer init() throws Exception {
        // load application.properties
        Properties applicationProperties = loadApplicationProperties();

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
        ConfigManager.init(applicationProperties.getProperty(KEY_PETASCOPE_CONF_DIR));
        loadGdalLibrary(applicationProperties);
        
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
    
    private static void loadGdalLibrary(Properties applicationProperties) {
        String systemGdalJavaDir = applicationProperties.getProperty(KEY_GDAL_JAVA_DIR);
        
        // Load the GDAL native libraries (no need to set in IDE with VM options: -Djava.library.path="/usr/lib/java/gdal/")
        try {
            addGdalLibraryPath(TMP_GDAL_JAVA_DIR_NAME, systemGdalJavaDir);
        } catch (Exception ex) {
            String errorMessage = "Cannot add GDAL native library from '" + systemGdalJavaDir + "' to java library path, "
                    + "please restart Tomcat to fix this problem. Reason: " + ex.getMessage();
            AbstractController.startException = new PetascopeException(ExceptionCode.InternalComponentError, errorMessage, ex);
            return;
        } catch (Error ex) {
            String errorMessage = "Cannot add GDAL native library from '" + systemGdalJavaDir + "' to java library path, "
                    + "please restart Tomcat to fix this problem. Reason: " + ex.getMessage();
            AbstractController.startException = new PetascopeException(ExceptionCode.InternalComponentError, errorMessage);
            return;
        }
        
        // NOTE: to make sure that GDAL is loaded properly, do a simple CRS transformation here 
        // (if not, user has to restart Tomcat for JVM class loader does the load JNI again).
        try {
            gdal.AllRegister(); // should be done once on application startup
            // test projection
            CrsProjectionUtil.transform("EPSG:3857", "EPSG:4326", new double[] {0, 0});
        } catch (Exception ex) {
            String errorMessage = "Transform test failed, probably due to a problem with adding GDAL native library from '" + 
                    systemGdalJavaDir + "' to java library path; please restart Tomcat to fix this problem. Reason: " + ex;
            AbstractController.startException = new PetascopeException(ExceptionCode.InternalComponentError, errorMessage, ex);
            return;
        }
    }
    
    // -----------------------------------------------------------------------------------
    // Load GDAL native library
    // 
    // TODO: these methods should be generalized with a library name (e.g. libgdal.so)
    //       and moved to a Util class.
    // -----------------------------------------------------------------------------------

    /**
     * Adds the specified path to the java library path (very important to load gdal-java)
     */
    private static void addGdalLibraryPath(String tmpLibPath, String systemLibPath) throws Exception {
        File parentTmpLibDir = getParentTmpLibDir(tmpLibPath);

        // Each time application starts, copy the system library to a temp folder to be loaded by Classloader
        File currentTmpLibDir = createUniqueTmpLibDir(parentTmpLibDir);
        copyFilesWithPrefix(systemLibPath, currentTmpLibDir, GDAL_LIB_PREFIXES);

        // Then load this new created tmp native library folder by Java class loader
        loadGdalLibraryByClassLoader(parentTmpLibDir.getPath(), currentTmpLibDir);              

        // As the war file can be run from terminal which has different user name (e.g: rasdaman not tomcat)
        // So must set it to 777 permission then the folder can be deleted from both external tomcat or embedded tomcat.
        setFullDirPermissions(parentTmpLibDir);
    }
    
    private static File getParentTmpLibDir(String tmpLibPath) throws PetascopeException {
        File ret = new File(ConfigManager.DEFAULT_PETASCOPE_DIR_TMP, tmpLibPath);
        if (ret.exists()) {
            // Remove this temp directory for the gdal library as it is already loaded in JVM
            try {
                FileUtils.cleanDirectory(ret);
            } catch (IOException ex) {
                throw new PetascopeException(ExceptionCode.RuntimeError,
                        "Cannot delete temp directory '" + ret + 
                        "', please remove manually and restart Tomcat. Reason: " + ex.getMessage());
            }
        }
        return ret;
    }
    
    private static File createUniqueTmpLibDir(File tmpLibDir) throws PetascopeException {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        File ret = new File(tmpLibDir, timeStamp);
        try {
            FileUtils.forceMkdir(ret);
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError,
                        "Cannot create temp directory '" + ret +  "'. Reason: " + ex.getMessage());
        }
        return ret;
    }
    
    /**
     * Given a file name prefix, it copies all matches from srcPath to dstDir.
     */
    private static void copyFilesWithPrefix(String srcPath, File dstDir, String... fileNamePrefixes) throws PetascopeException {
        File srcDir = new File(srcPath);
        
        IOFileFilter[] fileFilters = new IOFileFilter[fileNamePrefixes.length];
        for (int i = 0; i < fileNamePrefixes.length; ++i)
            fileFilters[i] = FileFilterUtils.prefixFileFilter(fileNamePrefixes[i]);
        
        try {
            FileUtils.copyDirectory(srcDir, dstDir, FileFilterUtils.or(fileFilters));
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError,
                        "Cannot copy native library folder from '" + srcDir + 
                        "' to '" + dstDir + "'. Reason: " + ex.getMessage());
        }
    }

    /**
     * Copy native library files from original source folder (e.g: /usr/lib) 
     * to a temp folder (e.g: /tmp/rasdaman_petascope/gdal_java/2018-04-08_13_31_00).
     * Then the Java class loader can load the libraries from this temp folder.
     * 
     * NOTE: it is critical that Java class loader needs different paths to temp native library folders 
     * specified by date-time stamp to load correctly.
     * Any old loaded folder by Java class loader will be removed when *petascope restarts*.
     * If Java class loader cannot load native library, the only way to solve is to *restart Tomcat*.
     * 
     * @paam tmpLibPath the tmp parent folder (e.g: /tmp/rasdaman/petascope/gdal_java) 
     * to store children folders containing native library files.
     * @param currentTmpLibDir the child folder of the tmp native library parent folder.
     */
    private static void loadGdalLibraryByClassLoader(String tmpLibPath, File currentTmpLibDir) 
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        // Based on this guide to change JNI at run time
        // http://fahdshariff.blogspot.de/2011/08/changing-java-library-path-at-runtime.html
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);
        // get array of paths
        final String[] paths = (String[]) usrPathsField.get(null);

        int i = 0;
        boolean pathExist = false;
        // check if the path to add is already present
        for (String path : paths) {
            String pathFolder = StringUtils.substringBeforeLast(path, "/");
            if (pathFolder.equals(tmpLibPath)) {                
                // Override the old path of rasdaman/gdal_native with the new one
                paths[i] = currentTmpLibDir.getPath();
                usrPathsField.set(null, paths);
                pathExist = true;
                break;
            }
            i++;
        }

        if (pathExist == false) {
            //add the new path
            final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);        
            newPaths[newPaths.length - 1] = currentTmpLibDir.getPath();
            usrPathsField.set(null, newPaths);
        }
    }
    
    /**
     * Set full directory permissions for dir.
     * TODO: move to a FileUtil class.
     */
    private static void setFullDirPermissions(File dir) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        Process process = rt.exec("chmod -R 777 " + dir);
        process.waitFor();
    }
    
    // -----------------------------------------------------------------------------------

    /**
     * Invoked when all beans are created to check it should run the migration
     * or not.
     */
    @PostConstruct
    private void handleMigrate() {

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
        // user runs Petascope in the command line to migrate petascopedb
        for (String arg : args) {
            // In case of running java -jar rasdaman.war --petascope.confDir=/opt/rasdaman/new_etc
            if (arg.startsWith(KEY_PETASCOPE_CONF_DIR)) {
                String value = arg.split("=")[1];
                OPT_PETASCOPE_CONFDIR = value;
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
}
