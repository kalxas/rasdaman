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
 * Copyright 2003 - 2012 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package secore.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a configuration file, secore.conf
 *
 * @author Dimitar Misev
 */
public class Config {

    private static Logger log = LoggerFactory.getLogger(Config.class);

    // version
    public final static String VERSION_MAJOR = "0";
    public final static String VERSION_MINOR = "1";
    public final static String VERSION_MICRO = "0";
    public final static String VERSION
            = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_MICRO;
    public final static String LANGUAGE = "en";

    // configuration keys
    private final static String USERNAME_KEY = "username";
    private final static String PASSWORD_KEY = "password";
    private final static String GML_DEF_KEY = "gml.def.path";
    private final static String DB_UPDATES_PATH = "db_updates.path";
    // This will changed the original request (before "/def") with URL prefix which is set in secore.properties
    // e.g: http://localhost:8080/def/crs/EPSG/0/4326 to http://opengist.net/def/crs/EPSG/0/4326
    private final static String SERVICE_URL = "service.url";
    private final static String CODESPACE_KEY = "codespace";

    // singleton
    private static Config instance;
    private static Properties props;

    private static final String PROPERTIES_FILE = "secore.properties";
    private static final String DEFAULT_CONF_DIR = "@CONF_DIR@/etc";
    // we will search the full path for the etc/gml folder inside war file
    private static final String ETC_DIRECTORY = "etc";

    // from secore.properties used for log4j
    private static final String LOG_FILE_PATH = "log4j.appender.rollingFile.File";

    // after get the fullpath to etc/gml, then it can load gml dictionary files from this folder
    private String gmlDir;

    private Config(String confDir) {
        // (we read from @CONF_DIR@/secore.properties (i.e: $RMANHOME/etc/secore.properties)) which is copied from secore-core/etc/secore.properties
        String confFile = confDir + "/" + PROPERTIES_FILE;

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

        // find the gml directory which contains EPSG, userdb
        initGMLDirectory();

        // then init log4j configuration
        initLogging(confFile);
    }

    /**
     * Find GML directory in war file
     */
    private void initGMLDirectory() {
        try {
            // need to findout the fullpath to etc/gml folder
            File file = IOUtil.findFile(ETC_DIRECTORY);
            String filePath = file.getAbsolutePath();
            log.debug("secore.properties file path: " + filePath);
            // get the file path to etc/gml directory
            gmlDir = filePath.substring(0, filePath.lastIndexOf("/") + 1) + this.getGmlDefPath();
            log.debug("gml directory: " + gmlDir);
        } catch (IOException ex) {
            log.error("Cannot find the GML directory in SECORE to initialize database.");
            throw new RuntimeException("Cannot find the GML directory in SECORE to initialize database.");
        }
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
            } catch (Exception ex) {
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
            BasicConfigurator.configure(new ConsoleAppender(
                    new PatternLayout("[%d{HH:mm:ss}]%6p %c{1}@%L: %m%n")));
            log.warn("No property for log4j.properties found on secore.properties file. Logging to standard output configured in code");
        }
    }

    public static Config getInstance() {
        if (instance == null) {
            log.error("Failed initializing configurations.");
            throw new RuntimeException("Failed initializing configurations.");
        }
        return instance;
    }

    /**
     * Initialize the singleton object with configuration directory to secore.properties (i.e: $RMANHOME/etc/secore.properties)
     *
     * @param confDir
     */
    public static void initInstance(String confDir) {
        if (instance == null) {
            instance = new Config(confDir);
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

    public String getPassword() {
        return get(USERNAME_KEY);
    }

    public String getUsername() {
        return get(PASSWORD_KEY);
    }

    // e.g: etc/gml (need to load all EPSG database folders here), default is 8.5
    public String getGmlDefPath() {
        String ret = get(GML_DEF_KEY);
        if (ret != null && !ret.endsWith(File.separator)) {
            ret += File.separator;
        }
        return ret;
    }

    public String getDbUpdatesPath() {
        String ret = get(DB_UPDATES_PATH);
        if (ret != null && !ret.endsWith(File.separator)) {
            ret += File.separator;
        }
        return ret;
    }

    /**
     * The prefix URL in secore.properties (secore.url) (e.g: http://opengis.net/def) then all the resolved URL will use this prefix URL.
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
}
