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
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rasdaman.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * When Application starts, initialize all the configurations, properties which
 * are used later.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class InitAllConfigurationsApplicationService {

    private static final Logger log = LoggerFactory.getLogger(InitAllConfigurationsApplicationService.class);

    // path to gdal native files (.so) which are needed for GDAL java to invoke.
    public static final String APPLICATION_PROPERTIES_FILE = "application.properties";
    public static final String KEY_GDAL_JAVA_DIR = "gdal-java.libDir";
    public static final String KEY_PETASCOPE_CONF_DIR = "petascope.confDir";
    // NOTE: this is a hidden database of postgresql which allows to connect and create/rename other database.
    public static final String POSTGRESQL_NEUTRAL_DATABASE = "template1";

    

    /**
     * Adds the specified path to the java library path (very important to load
     * GDAL native libraries!!!)
     *
     * @param libraryName
     * @param pathToAdd the path to add
     * @throws java.lang.NoSuchFieldException
     * @throws java.lang.IllegalAccessException
     * @throws java.io.IOException
     */
    public static void addLibraryPath(String libraryName, String pathToAdd) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException, InterruptedException {
        final String tmpTargetNativeDefaultFolderPath = "/tmp/rasdaman/" + libraryName;
        Runtime rt = Runtime.getRuntime();
        if (new File(tmpTargetNativeDefaultFolderPath).exists()) {
            // Remove this temp directory for the gdal library as it already loaded in JVM
            FileUtils.deleteDirectory(new File(tmpTargetNativeDefaultFolderPath));
        }
        
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        
        // The original installation of gdal_java native in system
        File sourceNativeFolder = new File(pathToAdd);        
        // Each time application starts, copy the original native folder to a temp folder to be loaded by Classloader
        String tmpTargetNativeFolderPath =  tmpTargetNativeDefaultFolderPath + "/" + timeStamp;
        File tmpTargetNativeFolder = new File(tmpTargetNativeFolderPath);
        FileUtils.forceMkdir(tmpTargetNativeFolder);
        
        // Copy the source folder to native folder and load to class path
        FileUtils.copyDirectory(sourceNativeFolder, tmpTargetNativeFolder);
                
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        // get array of paths
        final String[] paths = (String[]) usrPathsField.get(null);

        int i = 0;
        boolean pathExist = false;
        // check if the path to add is already present
        for (String path : paths) {
            String pathFolder = StringUtils.substringBeforeLast(path, "/");
            if (pathFolder.equals(tmpTargetNativeDefaultFolderPath)) {                
                // Override the old path of rasdaman/gdal_native with the new one
                paths[i] = tmpTargetNativeFolderPath;
                usrPathsField.set(null, paths);
                pathExist = true;
                break;
            }
            i++;
        }

        if (pathExist == false) {
            //add the new path
            final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);        
            newPaths[newPaths.length - 1] = tmpTargetNativeFolderPath;
            usrPathsField.set(null, newPaths);
        }
        
        // As the war file can be run from terminal which has different user name (e.g: rasdaman not tomcat)
        // So must set it to 777 permission then the folder can be deleted from both external tomcat or embedded tomcat.
        rt.exec("chmod -R 777 " + tmpTargetNativeDefaultFolderPath);
    }
    
    /**
     * Add JDBC driver to classpath to be used at runtime from configuration in petascope.properties.
     * 
     * @param pathToJarFile 
     * @throws petascope.exceptions.PetascopeException 
     */
    public static void addJDBCDriverToClassPath(String pathToJarFile, String connectionString) throws PetascopeException {
        if (pathToJarFile.trim().isEmpty()) {
            // By default, it uses Postgresql driver and the path to JDBC driver in petascope.properties is empty
            // as Postgresql jar driver is already in dependency.
            return;
        }
        
        File jdbcJarFile = new File(pathToJarFile);
        if (!jdbcJarFile.exists()) {
            throw new PetascopeException(ExceptionCode.InvalidPropertyValue, "Path to JDBC jar driver for DMBS does not exist, given '" + pathToJarFile + "'.");
        }
        URL urls[] = new URL[1];
        try {
            urls[0] = jdbcJarFile.toURI().toURL();            
        } catch (MalformedURLException ex) {
            throw new PetascopeException(ExceptionCode.IOConnectionError, 
                    "Cannot get the URI from given JDBC jar driver file path, given '" + pathToJarFile + "', error " + ex.getMessage());
        }
        
        // Add this jar file to class path
        URLClassLoader loader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(loader);
        String className;
        if (connectionString.contains("postgresql")) {
            className = "org.postgresql.Driver";
        } else if (connectionString.contains("jdbc:h2")) {
            className = "org.h2.Driver";
        } else if (connectionString.contains("jdbc:hsqldb")) {
            className = "org.hsqldb.jdbc.JDBCDriver";
        } else {
            // Add more tested JDBC driver here when possible
            throw new PetascopeException(ExceptionCode.InternalSqlError, "DBMS is not supported for petascopedb, given JDBC URL '" + connectionString + "'.");
        }

        // NOTE: This is the trick to make DriverManager understands that this driver is registered dynamically.
        Driver driver;
        try {
            driver = (Driver) Class.forName(className, true, loader).newInstance();
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.InternalSqlError, 
                    "Cannot register JDBC Driver from file path '" + pathToJarFile + "', error message '" + ex.getMessage() + "'.", ex);
        }
        
        try {
            DriverManager.registerDriver(new DriverShim(driver));
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.InternalSqlError, 
                    "Cannot register Java SQSL Driver, error message '" + ex.getMessage() + "'.", ex);
        }
    }
}

/**
 * NOTE: It is hard to load JDBC Driver dynamically, see:
 * http://www.kfu.com/~nsayer/Java/dyn-jdbc.html
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
class DriverShim implements Driver {

    private final Driver driver;

    DriverShim(Driver d) {
        this.driver = d;
    }

    @Override
    public boolean acceptsURL(String u) throws SQLException {
        return this.driver.acceptsURL(u);
    }

    @Override
    public Connection connect(String u, Properties p) throws SQLException {
        return this.driver.connect(u, p);
    }

    @Override
    public int getMajorVersion() {
        return this.driver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return this.driver.getMinorVersion();
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
        return this.driver.getPropertyInfo(u, p);
    }

    @Override
    public boolean jdbcCompliant() {
        return this.driver.jdbcCompliant();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Unsupported feature");
    }
}
