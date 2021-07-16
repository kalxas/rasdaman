/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.secore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.rasdaman.secore.util.ExceptionCode;
import org.rasdaman.secore.util.SecoreException;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

/**
 * Reads a configuration file, secore.conf
 *
 * @author Dimitar Misev
 */
public class ConfigManager {

    private static Logger log = LoggerFactory.getLogger(ConfigManager.class);

    // version
    public final static String VERSION_MAJOR = "0";
    public final static String VERSION_MINOR = "1";
    public final static String VERSION_MICRO = "0";
    public final static String VERSION
            = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_MICRO;
    public final static String LANGUAGE = "en";
    
    public static final String DEFAULT_SERVER_CONTEXT_PATH = "/def";
    
    /*** properties's values in secore.properties ***/
    private final static String KEY_SERVER_CONTEXT_PATH = "server.contextPath";
    private final static String KEY_DB_UPDATES_PATH = "db_updates.path";
    // This will changed the original request (before "/def") with URL prefix which is set in secore.properties
    // e.g: http://localhost:8080/def/crs/EPSG/0/4326 to http://opengist.net/def/crs/EPSG/0/4326
    private final static String KEY_SERVICE_URL = "service.url";
    private final static String KEY_CODESPACE = "codespace";
    // If java_server=external, extract secoredb to tomcat/webapps/, if java_server=embedded, extract secoredb to $RMANHOME/share/rasdaman/war
    public final static String KEY_JAVA_SERVER = "java_server";
    public static final String VALUE_JAVA_SERVER_EXTERNAL = "external";
    // Only embedded secore needs to specify the path to store secoredb folder
    private final static String KEY_EMBEDDED_SECOREDB_FOLDER_PATH = "secoredb.path";
    // NOTE: extracted gml dir will be at /tmp, but secoredb containing baseX files will be in different places (tomcat/webapps or /opt/rasdaman/data/secore)
    private static final String EXTRACTED_GML_DIR_TMP = "/tmp/rasdaman/secore";
    private static final String EMBEDDED_SECOREDB_FOLDER_PATH = "/opt/rasdaman/data/secore";
    
    // username, password to log in admin pages (*.jsp files)
    // NOTE: if no configurations in secore.properties, just login normally
    private final static String KEY_SECORE_ADMIN_USER = "secore_admin_user";
    private final static String KEY_SECORE_ADMIN_PASSWORD = "secore_admin_pass";

    // singleton
    private static ConfigManager instance;
    private static Properties props;

    public static final String SECORE_PROPERTIES_FILE = "secore.properties";
    private static final String DEFAULT_CONF_DIR = "@CONF_DIR@/etc";

    // As with embedded servlet container, it needs to extract gml.tar.gz from resource folder to a temp folder and BaseX can create secoredb    
    public static final String GML_ZIPPED_NAME = "gml.tar.gz";

    // from secore.properties used for log4j
    private static final String LOG_FILE_PATH = "log4j.appender.rollingFile.File";

    // after get the fullpath to etc/gml, then it can load gml dictionary files from this folder
    private String gmlDir;
    
    Boolean isEmbedded = null;
    public static Boolean embeddedFromPetascope = null;
    // tomcat webapp dirs, passed from petascope
    public static String webappsDir = null;

    @Autowired
    private ConfigManager(String confDir, Boolean embeddedFromPetascope, String webappsDir) {
        this.embeddedFromPetascope = embeddedFromPetascope;
        this.webappsDir = webappsDir;
        
        // (we read from @CONF_DIR@/secore.properties (i.e: $RMANHOME/etc/secore.properties)) which is copied from secore-core/etc/secore.properties
        String confFile = confDir + "/" + SECORE_PROPERTIES_FILE;
        
        if (confDir.equals(DEFAULT_CONF_DIR)) {
            log.error("SECORE's configuration properties file was not setup, please change this parameter (@CONF_DIR@) in WEB-INF/web.xml to the location of secore.properties file.");
            throw new RuntimeException("SECORE's configuration properties file was not setup, please change this parameter (@CONF_DIR@) in WEB-INF/web.xml to the location of secore.properties file.");
        }

        // read configuration
        props = new Properties();
        InputStream is = null;
        try {
            File file = new File(confFile);
            is = new FileInputStream(file);
            props.load(is);
        } catch (IOException ex) {
            log.error("Failed loading settings", ex);
            throw new RuntimeException("Failed loading settings", ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    log.warn("Failed closing stream settings input stream", ex);
                }
            }
        }
        
        // Only when embeddedFromPetascope = null, i.e. running SECORE as a standalone web application
        this.setEmbedded(embeddedFromPetascope);

        // NOTE: get the gml folder in resources folder
        File file;
        try {
            if (embeddedFromPetascope != null || this.useEmbeddedServer() == true) {
                // NOTE: it depends on the configuration of $RMANHOME/etc/secore.properties (java_server=external)
                // not by java -jar def.war, so need to change to java_server=embedded first.
                // embedded servlet container, war file is not extracted, need to extract the gml folder temporarily for BaseX to create secoredb
                InputStream inputStream = new ClassPathResource(ConfigManager.GML_ZIPPED_NAME).getInputStream();
                try {
                    File tempFile = new File(EXTRACTED_GML_DIR_TMP + "/" + ConfigManager.GML_ZIPPED_NAME);
                    // copy tar.gz from resource to /tmp folder
                    FileUtils.copyInputStreamToFile(inputStream, tempFile);
                    File extractedGmlFolder = new File(EXTRACTED_GML_DIR_TMP);
                    Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
                    // extract this tar file to a temp folder
                    archiver.extract(tempFile, extractedGmlFolder);
                    
                    gmlDir = EXTRACTED_GML_DIR_TMP + "/gml";
                    log.info("Extracted gml.zip to tmp folder '" + gmlDir + "'.");
                } catch (Exception ex) {
                    throw new RuntimeException("Cannot extract gml.tar.gz to folder '" + ConfigManager.EMBEDDED_SECOREDB_FOLDER_PATH 
                            + ", reason: " + ex.getMessage() + ". Hint: make sure user running petascope has write permission for this folder.", ex);
                }
            } else {
                /// external servlet container, war file is extracted to folder automatically, then could find the file path.
                // NOTE: for SECORE runs with external servlet container get the gml folder in resources folder of def.war, then go to $CATALINA_HOME/webapps/
                try {
                    file = new ClassPathResource("gml").getFile();
                    gmlDir = file.getCanonicalPath();
                } catch (IOException ex) {
                    // If user wants to run secore as embedded with java -jar def.war, however in secore.properties, it is set java_server=external
                    // this will throw exception as embedded could not load resource directory from file.
                    throw new RuntimeException("Cannot find gml resource folder, does secore set to " + KEY_JAVA_SERVER + "=external in secore.properties file?", ex);
                }
            }
        } catch (IOException ex) {
            log.error("Can not find gml folder in resources folder.");
            throw new ExceptionInInitializerError(ex);
        }

        // then init log4j configuration
        initLogging(confFile);
        log.info("SECORE gml dir path '" + gmlDir + "'.");
        log.info("Loaded SECORE properties from this file path '" + confFile + "'.");
    }

    /**
     * Read the log configuration from $RMANHOME/etc/secore.properties
     */
    private void initLogging(String confFile) {
        try {
            // log4j is added in secore.properties
            File file = new File(confFile);
            log.info("Logging configured from configuration file " + confFile);

            try {
                PropertyConfigurator.configure(file.toURI().toURL());
            } catch (MalformedURLException ex) {
                System.err.println("Error loading logger configuration: " + confFile);
                ex.printStackTrace();
                BasicConfigurator.configure();
            }

            String logFilePath = props.getProperty(LOG_FILE_PATH);

            // there is log file path in secore.properties
            if (logFilePath != null) {
                File f = new File(logFilePath);
                // If the log file path is configured as absolute path, we check the write permision of Tomcat username on this file.
                if (f.isAbsolute()) {
                    if (!f.canWrite()) {
                        log.warn("Cannot write to the secore log file defined in secore.properties: " + logFilePath + ".\n"
                                + "Please make sure the path specified by " + LOG_FILE_PATH + " in secore.properties is"
                                + " a location where the system user running Tomcat has write access."
                                + " Otherwise, the secore log can only be found in the Tomcat log (usually catalina.out).");
                    }
                } else {
                    // log file path is relative, we don't know where directory user want to set the log file, so user will need to see the log in catalina.out
                    log.warn(LOG_FILE_PATH + " is set to relative path: " + logFilePath + " in petascope.properties; it is recommended to set it to an absolute path."
                            + " In any case, the petascope log can be found in the Tomcat log (usually catalina.out).");
                }
            }
        } catch (Exception ex) {
            BasicConfigurator.configure();
            log.warn("No property for log4j.properties found on secore.properties file. Logging to standard output configured in code", ex);
        }
    }

    public static ConfigManager getInstance() throws ExceptionInInitializerError {
        if (instance == null) {
            log.error("Failed initializing configurations.");
            throw new ExceptionInInitializerError("Failed initializing configurations.");
        }

        return instance;
    }

    /**
     * Initialize the singleton object with configuration directory to
     * secore.properties (i.e: $RMANHOME/etc/secore.properties)
     *
     * @param confDir
     * @throws java.io.IOException
     * @throws org.rasdaman.secore.util.SecoreException
     */
    public static void initInstance(String confDir, boolean embedded, String webappsDir) throws IOException, SecoreException {
        if (instance == null) {
            instance = new ConfigManager(confDir, embedded, webappsDir);
        }
    }

    /**
     * Return a setting value from the settings file
     *
     * @param key setting key
     * @return setting value, or the empty string in case the key does not exist
     */
    private String get(String key) {
        if (props.containsKey(key)) {
            return props.getProperty(key);
        }
        log.warn("Unknown setting: " + key);

        return "";
    }

    public String getDbUpdatesPath() {
        String ret = get(KEY_DB_UPDATES_PATH);
        if (ret != null && !ret.endsWith(File.separator)) {
            ret += File.separator;
        }

        return ret;
    }
    
    /**
     * Return the servlet context path: /def (by default)
     */
    public String getServerContextPath() {
        return get(KEY_SERVER_CONTEXT_PATH);
    }

    /**
     * The prefix URL in secore.properties (secore.url) (e.g:
     * http://opengis.net/def) then all the resolved URL will use this prefix
     * URL.
     */
    public String getServiceUrl() {
        return get(KEY_SERVICE_URL);
    }

    public String getCodespace() {
        return get(KEY_CODESPACE);
    }

    public String getGMLDirectory() {
        return gmlDir;
    }

    /**
     * Check if SECORE runs by embedded or external servlet container.
     *
     */
    public boolean useEmbeddedServer() {
        return this.isEmbedded;
    }
    
    public void setEmbedded(Boolean embeddedFromPetascope) {
        if (embeddedFromPetascope == null) {
            // NOTE: only when running secore as a standalone web application then it should check java_server from secore.properties
            String value = get(KEY_JAVA_SERVER);
            if (value.equals("external")) {
                this.isEmbedded = false;
            } else {
                // java_server=embedded
                this.isEmbedded = true;
            }
        } else {
            // secore get influence from petascope
            this.isEmbedded = embeddedFromPetascope;
        }
    }
    
    /**
     * The path to secoredb folder for embedded SECORE
     * which is configged in secore.properties (secoredb.path). 
     * @return 
     * @throws org.rasdaman.secore.util.SecoreException 
     */
    public String getEmbeddedSecoreDbFolderPath() throws SecoreException {
        String secoredbFolderPath = ConfigManager.getInstance().get(KEY_EMBEDDED_SECOREDB_FOLDER_PATH);
        if (secoredbFolderPath.equals("")) {
            throw new SecoreException(ExceptionCode.InvalidParameterValue, 
                                      KEY_EMBEDDED_SECOREDB_FOLDER_PATH + " is empty in secore.properties file for embedded SECORE.");
        } else {
            File file = new File(secoredbFolderPath);
            if (!file.canRead() || !file.canWrite()) { 
                throw new SecoreException(ExceptionCode.InvalidParameterValue, 
                                          KEY_EMBEDDED_SECOREDB_FOLDER_PATH + " points to non readable/writable folder path in secore.properties file for embedded SECORE.");
            }
        }       
        
        return secoredbFolderPath;
    }
    
    public String getAdminUsername() {
        return get(KEY_SECORE_ADMIN_USER);
    }
    
    public String getAdminPassword() {
        return get(KEY_SECORE_ADMIN_PASSWORD);
    }
    
    /**
     * NOTE: only show login page for admin pages (*.jsp) if these configuration are setup in secore.properties.
     * If they don't exist, just allow access without any login form.
     */
    public boolean showLoginPage() {
        String username = this.getAdminUsername();
        String password = this.getAdminPassword();
        boolean result = !username.isEmpty() && !password.isEmpty();
        return result;
    }
}
