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
package org.rasdaman.secore.util;

import org.rasdaman.secore.Constants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;
import org.rasdaman.secore.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * I/O utility class.
 *
 * @author Dimitar Misev
 */
public class IOUtil {

    private static Logger log = LoggerFactory.getLogger(IOUtil.class);

    // absolute path to the SECORE database dir
    private static String secoreDbDir = null;

    // dafault encoding used for reading from file
    private static final String UTF8 = "UTF-8";

    public static String getFilename(String path) {
        int ind = path.lastIndexOf(File.separator);
        if (ind != -1) {
            return path.substring(ind + 1);
        }
        return path;
    }

    /**
     * Find a file in currentDir or its parent directories
     *
     * @param fileName
     * @param currentDir
     * @param depth limit the number of parent directories to this depth, or -1 is infinite
     * @return the found file, or null
     */
    public static File findFile(String fileName, File currentDir, int depth) {
        File ret = null;
        int i = 0;

        while (currentDir != null && (depth == -1 || i++ < depth)) {
            ret = new File(currentDir.getPath() + File.separator + fileName);
            if (ret != null && ret.exists()) {
                return ret;
            }
            currentDir = currentDir.getParentFile();
        }

        return null;
    }

    /**
     * Convert URL to string, removing any URI schemes.
     * @param url
     * @return 
     * @throws java.net.URISyntaxException
     */
    public static String urlToString(URL url) throws URISyntaxException {
        String ret = url.toString();
        int ind = -1;
        while ((ind = ret.indexOf(':')) != -1 &&
                !ret.substring(0, ind).contains(File.separator)) {
            ret = ret.substring(ind + 1);
        }
        return ret;
    }

    /**
     * Find a file in currentDir or its parent directories
     *
     * @param fileName
     * @param currentDir
     * @param depth limit the number of parent directories to this depth, or -1 is infinite
     * @return the found file, or null
     */
    public static File findFile(String fileName, URL currentDir, int depth) {
        try {
            return findFile(fileName, new File(urlToString(currentDir)), depth);
        } catch (URISyntaxException ex) {
            log.warn("URI error", ex);
            return null;
        }
    }

    /**
     * Find a file in the directory of this class, or it's parents (to depth of 8)
     *
     * @param fileName the file to find
     * @return the file
     * @throws IOException in case the file was not found
     */
    public static File findFile(String fileName) throws IOException {
        File file = IOUtil.findFile(fileName, IOUtil.class.getResource(Constants.IOUTIL_CLASS), 8);
        if (file != null) {
            return file;
        } else {
            throw new IOException("Failed finding file " + fileName);
        }
    }

    /**
     * Determine SECORE database dir: $CATALINA_HOME/webapps/secoredb
     * This is where Tomcat/other servlet servers would always have access.
     *
     * @return the db dir, or null in case of an error
     * @throws org.rasdaman.secore.util.SecoreException
     */
    public static String getSecoreDbDir() throws SecoreException {
        if (secoreDbDir == null) {            
            try {
                if (ConfigManager.getInstance().useEmbeddedServer()) {
                    // embedded servlet container
                    // NOTE: secoredb is configged by user in secore.properties file (secoredb.path)
                    secoreDbDir = ConfigManager.getInstance().getEmbeddedSecoreDbFolderPath() + "/" + Constants.SECORE_DB_DIR;
                } else {
                    // external servlet container, secoredb is from webapps/secoredb
                    File webappsDir = new ClassPathResource("gml").getFile().getParentFile().getParentFile().getParentFile().getParentFile();                
                    secoreDbDir = webappsDir.getAbsolutePath() + "/" + Constants.SECORE_DB_DIR;
                }
                log.info("BaseX will create secoredb from the input gml resource folder '" + secoreDbDir + "'.");
                File secoreDbDirFile = new File(secoreDbDir);
                if (!secoreDbDirFile.exists()) {
                    if (!secoreDbDirFile.mkdir()) {
                        log.warn("Failed creating database directory: " + secoreDbDir);
                        secoreDbDir = null;
                    }
                }
            } catch (IOException ex) {
                log.warn("Couldn't determine the database directory for SECORE.", ex);
            }
        }
        return secoreDbDir;
    }

    /**
     * Read file and return contents as string.
     * @param file path to file
     * @return file contents as string
     */
    public static String fileToString(String file) {
        String ret = "";
        try {
            ret = new Scanner(new File(file), UTF8).useDelimiter("\\A").next();
        } catch (FileNotFoundException ex) {
            log.error("File " + file + " not found.", ex);
        }
        return ret;
    }
}
