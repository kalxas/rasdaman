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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * I/O utility class.
 *
 * @author Dimitar Misev
 */
public class IOUtil {

  private static Logger log = LoggerFactory.getLogger(IOUtil.class);
  
  // absolute path to the SECORE database dir
  private static String secoreDbDir = null;

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
   * Find a file in the directory of this class, or it's parents (to depth of 5)
   * 
   * @param fileName the file to find
   * @return the file
   * @throws IOException in case the file was not found
   */
  public static File findFile(String fileName) throws IOException {
    File f = IOUtil.findFile(fileName, IOUtil.class.getResource(Constants.IOUTIL_CLASS), 7);
    if (f != null) {
      return f;
    } else {
      throw new IOException("Failed opening file " + fileName);
    }
  }
  
  /**
   * Determine SECORE database dir: $CATALINA_HOME/webapps/.secoredb
   * This is where Tomcat/other servlet servers would always have access.
   * 
   * @return the db dir, or null in case of an error
   */
  public static String getDbDir() {
    if (secoreDbDir == null) {
      File indexFile;
      try {
        // get path to $CATALINA_HOME/webapps/def/index.jsp
        indexFile = IOUtil.findFile(Constants.INDEX_FILE);
        // get path to $CATALINA_HOME/webapps/
        File webappsDir = indexFile.getParentFile().getParentFile();
        // return $CATALINA_HOME/webapps/.secoredb
        secoreDbDir = webappsDir.getAbsolutePath() +
            File.separator + Constants.SECORE_DB_DIR;
      } catch (Exception ex) {
        log.warn("Couldn't determine the database directory for SECORE.", ex);
      }
    }
    return secoreDbDir;
  }
}
