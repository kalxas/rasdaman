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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.rasdaman.secore.util.Constants;
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
    private final static String DB_UPDATES_PATH = "db_updates.path";
    // This will changed the original request (before "/def") with URL prefix which is set in secore.properties
    // e.g: http://localhost:8080/def/crs/EPSG/0/4326 to http://opengist.net/def/crs/EPSG/0/4326
    private final static String SERVICE_URL = "service.url";
    private final static String CODESPACE_KEY = "codespace";
    // If java_server=external, extract secoredb to tomcat/webapps/, if java_server=embedded, extract secoredb to $RMANHOME/share/rasdaman/war
    private final static String JAVA_SERVER = "java_server";
    // Only embedded secore needs to specify the path to store secoredb folder
    private final static String EMBEDDED_SECOREDB_FOLDER_PATH = "secoredb.path";

    // singleton
    private static ConfigManager instance;
    private static Properties props;

    public static final String SECORE_PROPERTIES_FILE = "secore.properties";
    private static final String DEFAULT_CONF_DIR = "@CONF_DIR@/etc";

    // As with embedded servlet container, it needs to extract gml.tar.gz from resource folder to a temp folder and BaseX can create secoredb    
    public static final String GML_ZIPPED_NAME = "gml.tar.gz";
    public static final String SECORE_EMBEDDED_TMP_FOLDER = "/tmp/rasdaman/secore";
    public static final String GML_EMBEDDED_TMP_FOLDER = SECORE_EMBEDDED_TMP_FOLDER + "/gml";

    // from secore.properties used for log4j
    private static final String LOG_FILE_PATH = "log4j.appender.rollingFile.File";

    // after get the fullpath to etc/gml, then it can load gml dictionary files from this folder
    private String gmlDir;

    @Autowired
    private ConfigManager(String confDir) {
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

        // NOTE: get the gml folder in resources folder
        File file;
        try {
            if (this.useEmbeddedServer()) {
                // NOTE: it depends on the configuration of $RMANHOME/etc/secore.properties (java_server=external)
                // not by java -jar def.war, so need to change to java_server=embedded first.
                // embedded servlet container, war file is not extracted, need to extract the gml folder temporarily for BaseX to create secoredb
                InputStream inputStream = new ClassPathResource(ConfigManager.GML_ZIPPED_NAME).getInputStream();
                File tempFile = new File(ConfigManager.SECORE_EMBEDDED_TMP_FOLDER + "/" + ConfigManager.GML_ZIPPED_NAME);
                // copy tar.gz from resource to /tmp folder
                FileUtils.copyInputStreamToFile(inputStream, tempFile);
                File extractedGmlFolder = new File(ConfigManager.SECORE_EMBEDDED_TMP_FOLDER);
                Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
                // extract this tar file to a temp folder
                archiver.extract(tempFile, extractedGmlFolder);
                gmlDir = ConfigManager.GML_EMBEDDED_TMP_FOLDER;
            } else {
                /// external servlet container, war file is extracted to folder automatically, then could find the file path.
                // NOTE: for SECORE runs with external servlet container get the gml folder in resources folder of def.war, then go to $CATALINA_HOME/webapps/
                try {
                    file = new ClassPathResource("gml").getFile();
                    gmlDir = file.getCanonicalPath();
                } catch (IOException ex) {
                    // If user wants to run secore as embedded with java -jar def.war, however in secore.properties, it is set java_server=external
                    // this will throw exception as embedded could not load resource directory from file.
                    throw new RuntimeException("Cannot find gml resource folder, does secore set to " + JAVA_SERVER + "=external in secore.properties file?", ex);
                }
            }
        } catch (IOException ex) {
            log.error("Can not find gml folder in resources folder.");
            throw new ExceptionInInitializerError(ex);
        }

        // then init log4j configuration
        initLogging(confFile);
        
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
    public static void initInstance(String confDir) throws IOException, SecoreException {
        if (instance == null) {
            instance = new ConfigManager(confDir);
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
        String ret = get(DB_UPDATES_PATH);
        if (ret != null && !ret.endsWith(File.separator)) {
            ret += File.separator;
        }

        return ret;
    }

    /**
     * The prefix URL in secore.properties (secore.url) (e.g:
     * http://opengis.net/def) then all the resolved URL will use this prefix
     * URL.
     *
     * @return
     */
    public String getServiceUrl() {
        return get(SERVICE_URL);
    }

    public String getCodespace() {
        return get(CODESPACE_KEY);
    }

    public String getGMLDirectory() {
        return gmlDir;
    }

    /**
     * Check if SECORE runs by embedded or external servlet container.
     *
     * @return
     */
    public boolean useEmbeddedServer() {
        String value = get(JAVA_SERVER);
        if (value.equals("external")) {
            return false;
        }
        return true;
    }
    
    /**
     * The path to secoredb folder for embedded SECORE
     * which is configged in secore.properties (secoredb.path). 
     * @return 
     * @throws org.rasdaman.secore.util.SecoreException 
     */
    public String getEmbeddedSecoreDbFolderPath() throws SecoreException {
        String secoredbFolderPath = ConfigManager.getInstance().get(EMBEDDED_SECOREDB_FOLDER_PATH);
        if (secoredbFolderPath.equals("")) {
            throw new SecoreException(ExceptionCode.InvalidParameterValue, 
                                      EMBEDDED_SECOREDB_FOLDER_PATH + " is empty in secore.properties file for embedded SECORE.");
        } else {
            File file = new File(secoredbFolderPath);
            if (!file.canRead() || !file.canWrite()) { 
                throw new SecoreException(ExceptionCode.InvalidParameterValue, 
                                          EMBEDDED_SECOREDB_FOLDER_PATH + " points to non readable/writable folder path in secore.properties file for embedded SECORE.");
            }
        }       
        
        return secoredbFolderPath;
    }
}
