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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.RasdamanException;
import petascope.util.IOUtil;
import petascope.util.StringUtil;
import petascope.util.XMLUtil;
import petascope.util.ras.RasUtil;
import petascope.wps.server.WpsServer;

/**
 * Configuration Manager class: a single entry point for all server settings.
 * Implements the singleton design pattern.
 *
 * Note (AB): Although this class implements the singleton pattern, it offers
 * public static members that come pre-initialized, allowing use of values that
 * are not provided by the configuration file without getting an instance. These
 * should be made private and only the get method on the unique instance allowed
 *
 * @author Andrei Aiordachioaie
 * @author Dimitar Misev
 */
public class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);

    /* Major version number. This is the first release (1). */
    public final static String MAJOR = "2";
    /*
     * Minor version number.
     * v2 adds the reference implementation of WCS 2.0.
     * v3 adds WGS84 handling in WCPS requests
     * v4 adds a WPS implementation.
     * v5 adds integration to n52 WPS framework
     */
    public final static String MINOR = "0";
    /* Bug-fix count. We have a hack: every WCPS response is written to disk. */
    public final static String BUGFIX = "0";

    public final static String PETASCOPE_VERSION = MAJOR + "." + MINOR + "." + BUGFIX;
    public final static String PETASCOPE_LANGUAGE = "en";
    /* This URL gets initialized automatically when the first request is received.
     * Its value is used in the Capabilities response */
    public static String PETASCOPE_SERVLET_URL;

    /*
     * settings.properties
     */

    // petascope metadata (stored in postgres)
    public static String METADATA_DRIVER = "org.postgresql.Driver";
    public static String METADATA_URL = "jdbc:postgresql://localhost:5432/petascopedb";
    public static String METADATA_USER = "petauser";
    public static String METADATA_PASS = "petapasswd";

    // rasdaman connection settings
    public static String RASDAMAN_SERVER = "'localhost";
    public static String RASDAMAN_PORT = "7001";
    public static String RASDAMAN_URL = "http://" + RASDAMAN_SERVER + ":" + RASDAMAN_PORT;
    public static String RASDAMAN_DATABASE = "RASBASE";
    public static String RASDAMAN_USER = "rasguest";
    public static String RASDAMAN_PASS = "rasguest";
    public static String RASDAMAN_ADMIN_USER = "rasadmin";
    public static String RASDAMAN_ADMIN_PASS = "rasadmin";
    public static String RASDAMAN_VERSION = "v9.0.0beta1";

    // XML validation schema control setting
    public static final String XML_VALIDATION_F = "false";
    public static final String XML_VALIDATION_T = "true";
    public static String XML_VALIDATION = XML_VALIDATION_F;

    //Retry settings when opening a connection to rasdaman server. Ernesto Rodriguez <ernesto4160@gmail.com>
    //Time in seconds between each re-connect attempt
    public static String RASDAMAN_RETRY_TIMEOUT="5";
    //Maximum number of re-connect attempts
    public static String RASDAMAN_RETRY_ATTEMPTS="3";

    // OGC services info
    public static String WCST_LANGUAGE  = "en";
    public static String WCST_VERSION = "1.1.4";
    public static String WCPS_LANGUAGE = "en";
    public static String WCPS_VERSION = "1.0.0";
    public static String WPS_LANGUAGE = "en";
    public static String WPS_VERSION = "1.0.0";
    public static String WCS_DEFAULT_LANGUAGE = "en";
    public static String WCS_DEFAULT_VERSION = "2.0.1";
    public static String WCS_LANGUAGES = "en";
    public static String WCS_VERSIONS = "1.1.2," + WCS_DEFAULT_VERSION;
    public static String WMS_LANGUAGES = "en";
    public static String WMS_VERSIONS = "1.0.0,1.1.0";  // (!) Keep consistent with WmsRequest.java
    public static String RASDAMAN_LANGUAGE = "en";

    // OWS Metadata enable/disable
    public static final String ENABLE_OWS_METADATA_F = "false";
    public static final String ENABLE_OWS_METADATA_T = "true";
    public static String ENABLE_OWS_METADATA = ENABLE_OWS_METADATA_T;

    // depends on ccip_version in the petascope settings, ccip_version=true
    // will make this flag true.
    public static boolean CCIP_HACK = false;

    // SECORE connection settings
    public static List<String> SECORE_URLS = Arrays.asList(new String[]{"http://localhost:8080/def"});
    public static List<String> SECORE_VERSIONS = Arrays.asList(new String[]{"0.1.0"});
    // SECORE keyword used in PS_CRS table to be replaces with the first configured resolver
    public static final String SECORE_URL_KEYWORD = "%SECORE_URL%";
    // [!] Must match with what manually inserted in petascopedb (mind also global_const.sql URLs)

    /* WPS variables*/
    public static URI WPS_GET_CAPABILITIES_URI;
    public static URI WPS_DESCRIBE_PROCESS_URI;

    /* WCS-T Settings. Overridden by user-preferences in <code>settings.properties</code> */
    public static String WCST_DEFAULT_DATATYPE = "unsigned char";

    /* CRS RESOLVERS' timeouts (milliseconds) */
    public static final int CRSRESOLVER_CONN_TIMEOUT = 2000;
    public static final int CRSRESOLVER_READ_TIMEOUT = 10000;

    /* Singleton instance */
    private static ConfigManager instance;
    private static Properties props;

    // confdir parameter name
    public static final String CONF_DIR = "confDir";
    public static final String CONF_DIR_DEFAULT = "@confdir@";
    public static final String SETTINGS_FILE = "petascope.properties";
    public static final String LOG_PROPERTIES_FILE = "log4j.properties";

    // keys
    public static final String KEY_ENABLE_OWS_METADATA = "enable_ows_metadata";
    public static final String KEY_RASDAMAN_DATABASE = "rasdaman_database";
    public static final String KEY_RASDAMAN_URL = "rasdaman_url";
    public static final String KEY_RASDAMAN_USER = "rasdaman_user";
    public static final String KEY_RASDAMAN_PASS = "rasdaman_pass";
    public static final String KEY_RASDAMAN_ADMIN_USER = "rasdaman_admin_user";
    public static final String KEY_RASDAMAN_ADMIN_PASS = "rasdaman_admin_pass";
    public static final String KEY_RASDAMAN_VERSION = "rasdaman_version";
    public static final String KEY_METADATA_DRIVER = "metadata_driver";
    public static final String KEY_METADATA_URL = "metadata_url";
    public static final String KEY_METADATA_USER = "metadata_user";
    public static final String KEY_METADATA_PASS = "metadata_pass";
    public static final String KEY_RASDAMAN_RETRY_TIMEOUT = "rasdaman_retry_timeout";
    public static final String KEY_RASDAMAN_RETRY_ATTEMPTS = "rasdaman_retry_attempts";
    public static final String KEY_CCIP_VERSION = "ccip_version";
    public static final String KEY_WCST_DEFAULT_DATATYPE = "default_datatype";
    public static final String KEY_SECORE_URLS = "secore_urls";
    public static final String KEY_SECORE_VERSIONS = "secore_versions";
    public static final String KEY_XML_VALIDATION = "xml_validation";

    public static final String TEMPLATES_PATH = "../templates/";
    public static final String GETCAPABILITIES_XML = "GetCapabilities.xml";
    public static final String DESCRIBEPROCESS_XML = "DescribeProcess.xml";

    /**
     * Private constructor. Use <i>getInstance()</i>.
     *
     * @param confDir Path to the settings directory
     * @throws RasdamanException
     */
    private ConfigManager(String confDir) throws RasdamanException {

        if (confDir == null) {
            StringBuilder msg = new StringBuilder();
            msg.append("Your web.xml file is missing the configuration dir parameter.\n");
            msg.append("Please add the following in your $CATALINA_HOME/webapps/petascope/WEB-INF/web.xml:\n");
            msg.append("<context-param>\n");
            msg.append("    <description>Directory containing the configuration files</description>\n");
            msg.append("    <param-name>confDir</param-name>\n");
            msg.append("    <param-value>/path/to/petascope/configuration/files</param-value>\n");
            msg.append("</context-param>\n");

            System.err.println(msg.toString());
            throw new IllegalArgumentException(msg.toString());
        }

        if (confDir.equals(CONF_DIR_DEFAULT)) {
            String msg = "Please set a valid path in your $CATALINA_HOME/webapps/petascope/WEB-INF/web.xml for the confDir parameter.";
            System.err.println(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!(new File(confDir)).isDirectory()) {
            String msg = "Configuration directory not found, please update the confDir in your $CATALINA_HOME/webapps/petascope/WEB-INF/web.xml";
            System.err.println(msg);
            throw new IllegalArgumentException(msg);
        }

        confDir = IOUtil.wrapDir(confDir);

        // moved configuration files from the war file to a directory specified in web.xml -- DM 2012-jul-09
        System.out.println("Configuration dir: " + confDir);

        // load logging configuration
        try {
            PropertyConfigurator.configure(confDir + LOG_PROPERTIES_FILE);
        } catch (Exception ex) {
            System.err.println("Error loading logger configuration " + confDir + LOG_PROPERTIES_FILE);
            ex.printStackTrace();
            BasicConfigurator.configure();
        }

        // init XML parser
        XMLUtil.init();

        // load petascope configuration
        props = new Properties();
        try {
            InputStream is = new FileInputStream(new File(confDir + SETTINGS_FILE));
            if (is != null) {
                props.load(is);
            }
            initSettings();
        } catch (IOException e) {
            log.error("Failed loading the settings file " + confDir + SETTINGS_FILE, e);
            throw new RuntimeException("Failed loading the settings file " + confDir + SETTINGS_FILE, e);
        }
    }

    /**
     * Returns the instance of the ConfigManager. If no such instance exists,
     * it creates one with the specified settings file.
     *
     * @param confDir Path to the settings file
     * @return instance of the ConfigManager class
     * @throws RasdamanException
     */
    public static ConfigManager getInstance(String confDir) throws RasdamanException {
        if (instance == null) {
            instance = new ConfigManager(confDir);
        }
        return instance;
    }

    /**
     * Return a setting value from the settings file
     *
     * @param key Key of the setting
     * @return String value, or the empty string in case the key does not exist
     */
    private String get(String key) {
        String result = "";
        if (props.containsKey(key)) {
            result = props.getProperty(key);
        }
        return result;
    }

    /**
     * Overwrite defaults settings with user-defined values in petascope.properties
     * @throws RasdamanException
     */
    private void initSettings() throws RasdamanException {

        ENABLE_OWS_METADATA     = get(KEY_ENABLE_OWS_METADATA);
        RASDAMAN_DATABASE       = get(KEY_RASDAMAN_DATABASE);
        RASDAMAN_URL            = get(KEY_RASDAMAN_URL);
        RASDAMAN_USER           = get(KEY_RASDAMAN_USER);
        RASDAMAN_PASS           = get(KEY_RASDAMAN_PASS);
        RASDAMAN_ADMIN_USER     = get(KEY_RASDAMAN_ADMIN_USER);
        RASDAMAN_ADMIN_PASS     = get(KEY_RASDAMAN_ADMIN_PASS);
        METADATA_DRIVER         = get(KEY_METADATA_DRIVER);
        METADATA_URL            = get(KEY_METADATA_URL);
        METADATA_USER           = get(KEY_METADATA_USER);
        METADATA_PASS           = get(KEY_METADATA_PASS);
        RASDAMAN_RETRY_TIMEOUT  = get(KEY_RASDAMAN_RETRY_TIMEOUT);
        RASDAMAN_RETRY_ATTEMPTS = get(KEY_RASDAMAN_RETRY_ATTEMPTS);
        XML_VALIDATION          = get(KEY_XML_VALIDATION);

        CCIP_HACK = Boolean.parseBoolean(get(KEY_CCIP_VERSION));

        // Get rasdaman version from RasQL (see #546)
        RASDAMAN_VERSION = RasUtil.getRasdamanVersion();

        // SECORE
        SECORE_URLS     = StringUtil.csv2list(get(KEY_SECORE_URLS));
        SECORE_VERSIONS = StringUtil.csv2list(get(KEY_SECORE_VERSIONS));
        // check that a version is assigned to every URI, set the last version to the orphan URIs otherwise
        // NOTE: throwing an exception for a missing version is too harsh.
        if (SECORE_VERSIONS.size() < SECORE_URLS.size()) {
            String lastVersion = SECORE_VERSIONS.get(SECORE_VERSIONS.size()-1);
            SECORE_VERSIONS.addAll(StringUtil.repeat(lastVersion, SECORE_URLS.size()-SECORE_VERSIONS.size()));
        }

        //WPS 1.0.0 describeprocess and getcapabilities documents
        try {
            WPS_GET_CAPABILITIES_URI = WpsServer.class.getResource(TEMPLATES_PATH + GETCAPABILITIES_XML).toURI();
            WPS_DESCRIBE_PROCESS_URI = WpsServer.class.getResource(TEMPLATES_PATH + DESCRIBEPROCESS_XML).toURI();
        } catch (Exception e) {
            log.warn("Could not find WPS GetCapabilities and DescribeProcess Documents");
        }

        /* User preferences override default values for WCS-T */
        String tmp = get(KEY_WCST_DEFAULT_DATATYPE);
        if (tmp.length() > 0) {
            WCST_DEFAULT_DATATYPE = tmp;
        }

        log.info("------------------------------------");
        log.info("       *** PETASCOPE ***      ");
        log.info("Petascope Version: " + PETASCOPE_VERSION);
        log.info("Metadata Driver  : " + METADATA_DRIVER);
        log.info("Metadata URL     : " + METADATA_URL);
        log.info("Metadata Username: " + METADATA_USER);
        log.info("");
        log.info("       *** RASDAMAN ***       ");
        log.info("Rasdaman URL        : " + RASDAMAN_URL);
        log.info("Rasdaman DB         : " + RASDAMAN_DATABASE);
        log.info("Rasdaman user       : " + RASDAMAN_USER);
        log.info("Rasdaman version    : " + RASDAMAN_VERSION);
        log.info("Rasdaman admin user : " + RASDAMAN_ADMIN_USER);
        log.info("");
        log.info("       *** SECORE ***       ");
        log.info("SECORE URL       : " + SECORE_URLS);
        log.info("SECORE version   : " + SECORE_VERSIONS);
        log.info("");
        log.info("       *** WCS-T ***       ");
        log.info("WCS-T Language: " + WCST_LANGUAGE);
        log.info("WCS-T Version : " + WCST_VERSION);
        log.info("WCS-T Default Datatype: " + WCST_DEFAULT_DATATYPE);
        log.info("");
        log.info("       *** WCPS ***       ");
        log.info("WCPS Language : " + WCPS_LANGUAGE);
        log.info("WCPS Version  : " + WCPS_VERSION);
        log.info("");
        log.info("       *** WCS ***       ");
        log.info("WCS Languages : " + WCS_LANGUAGES);
        log.info("WCS Versions  : " + WCS_VERSIONS);
        log.info("");
        log.info("       *** WPS ***       ");
        log.info("WPS Language  : " + WPS_LANGUAGE);
        log.info("WPS Version   : " + WPS_VERSION);
        log.info("WPS GetCapabilities template: " + WPS_GET_CAPABILITIES_URI);
        log.info("WPS DescribeProcess template: " + WPS_DESCRIBE_PROCESS_URI);
        log.info("");
        log.info("       *** WMS ***       ");
        log.info("WMS Languages : " + WMS_LANGUAGES);
        log.info("WMS Versions  : " + WMS_VERSIONS);
        log.info("");
        log.info("------------------------------------");
    }
}
