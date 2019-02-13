package org.rasdaman;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
/**
 * Class for the configuration
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class Config {

    // Read from test.cfg file
    public static String PETASCOPE_PORT;
    public static String SECORE_PORT;

    public static final String WEB_CONTEXT_PATH = "http://localhost:";
    public static String WS_CLIENT_CONTEXT_PATH;
    public static String SECORE_CONTEXT_PATH;

    /**
     * Wait few seconds before taking the image of web page as test result
     */
    public static final String PATH_TO_PHANTOMJS_FILE = "/tmp/phantomjs";

    public static final String PATH_TO_PROPERTIES_FILE = "/opt/rasdaman/etc";
    public static final String PATH_TO_PETASCOPE_PROPERTIES_FILE = PATH_TO_PROPERTIES_FILE + "/petascope.properties";
    public static final String PATH_TO_SECORE_PROPERTIES_FILE = PATH_TO_PROPERTIES_FILE + "/secore.properties";

    // path to oracle folder at test script folder
    public static String ORACLE_FOLDER_PATH;
    // path to output folder at test script folder
    public static String OUTPUT_FOLDER_PATH;
    // path to log file at test script folder
    public static String LOG_FILE;

    private Properties petascopeProperties = new Properties();
    private Properties secoreProperties = new Properties();
    
    /* ************************ petascope.properties **************************** */
    // KEYS in petascope.properties 
    private static final String PETASCOPE_KEY_PETASCOPE_ADMIN_USER = "petascope_admin_user";
    private static final String PETASCOPE_KEY_PETASCOPE_ADMIN_PASS = "petascope_admin_pass";
    
    // VALUES in petascope.properties 
    public static String PETASCOPE_VALUE_PETASCOPE_ADMIN_USER;
    public static String PETASCOPE_VALUE_PETASCOPE_ADMIN_PASS;
    
    /* ************************ secore.properties **************************** */
    // KEYS in secore.properties 
    private static final String SECORE_KEY_SECORE_ADMIN_USER = "secore_admin_user";
    private static final String SECORE_KEY_SECORE_ADMIN_PASS = "secore_admin_pass";
    
    // VALUES in secore.properties 
    public static String SECORE_VALUE_SECORE_ADMIN_USER;
    public static String SECORE_VALUE_SECORE_ADMIN_PASS;
    
    

    /**
     * Wait milliseconds after clicking button to get result
     */
    public static final int TIME_TO_WAIT_BEFORE_CLICK = 1500;

    /**
     * Wait milliseconds before taking the image of web page as test result
     */
    public static final int TIME_TO_WAIT_TO_CAPTURE_WEB_PAGE = 3000;
    
    /**
     * Wait milliseconds after switching to another iframe
     */
    public static final int TIME_TO_WAIT_AFTER_SWITCHING_IFRAME = 5000;

    private void loadPropertiesFile(Properties properties, String filePath) throws IOException {
        InputStream input = null;
        input = new FileInputStream(filePath);
        // load a properties file
        properties.load(input);
    }
    
    private void loadPetascopePropertiesValues() {
        PETASCOPE_VALUE_PETASCOPE_ADMIN_USER = this.petascopeProperties.getProperty(PETASCOPE_KEY_PETASCOPE_ADMIN_USER);
        PETASCOPE_VALUE_PETASCOPE_ADMIN_PASS = this.petascopeProperties.getProperty(PETASCOPE_KEY_PETASCOPE_ADMIN_PASS);
    }
    
    private void loadSecorePropertiesValues() {
        SECORE_VALUE_SECORE_ADMIN_USER = this.secoreProperties.getProperty(SECORE_KEY_SECORE_ADMIN_USER);
        SECORE_VALUE_SECORE_ADMIN_PASS = this.secoreProperties.getProperty(SECORE_KEY_SECORE_ADMIN_PASS);
    }

    public Config() throws IOException {
        // Current directory of the jar application
        String currentDirectory = Paths.get(".").toAbsolutePath().normalize().toString();
        String scriptDirectory = new File(currentDirectory).getParent();
        ORACLE_FOLDER_PATH = scriptDirectory + "/oracle/";
        OUTPUT_FOLDER_PATH = scriptDirectory + "/output/";
        LOG_FILE = scriptDirectory + "/test.log";

        WS_CLIENT_CONTEXT_PATH = WEB_CONTEXT_PATH + PETASCOPE_PORT + "/rasdaman/ows";
        SECORE_CONTEXT_PATH = WEB_CONTEXT_PATH + SECORE_PORT + "/def/";

        // Load properties for testing web applications
        this.loadPropertiesFile(petascopeProperties, PATH_TO_PETASCOPE_PROPERTIES_FILE);
        this.loadPropertiesFile(secoreProperties, PATH_TO_SECORE_PROPERTIES_FILE);
        
        // Load values from properties for testing web applications
        this.loadPetascopePropertiesValues();
        this.loadSecorePropertiesValues();
    }

    public String getProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        return value;
    }
}
