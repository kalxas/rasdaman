package org.rasdaman;

import java.io.File;
import java.nio.file.Paths;

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
    
    // path to oracle folder at test script folder
    public static String ORACLE_FOLDER_PATH;
    // path to output folder at test script folder
    public static String OUTPUT_FOLDER_PATH;
    // path to log file at test script folder
    public static String LOG_FILE;

    /**
     * Wait milliseconds after clicking button to get result
     */
    public static final int TIME_TO_WAIT_AFTER_CLICK = 1000;

    /**
     * Wait milliseconds before taking the image of web page as test result
     */
    public static final int TIME_TO_WAIT_TO_CAPTURE_WEB_PAGE = 1500;

    public Config() {
        // Current directory of the jar application
        String currentDirectory = Paths.get(".").toAbsolutePath().normalize().toString();
        String scriptDirectory = new File (currentDirectory).getParent();
        ORACLE_FOLDER_PATH = scriptDirectory + "/oracle/";
        OUTPUT_FOLDER_PATH = scriptDirectory + "/output/";
        LOG_FILE = scriptDirectory + "/test.log";
        
        WS_CLIENT_CONTEXT_PATH = WEB_CONTEXT_PATH + PETASCOPE_PORT + "/rasdaman/ows";
        SECORE_CONTEXT_PATH = WEB_CONTEXT_PATH + SECORE_PORT + "/def/";
    }
}
