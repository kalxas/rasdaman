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
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            rt.exec("rm -rf " + tmpTargetNativeDefaultFolderPath);
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
}
