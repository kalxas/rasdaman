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

import java.lang.reflect.Field;
import java.util.Arrays;
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
     * @param pathToAdd the path to add
     * @throws java.lang.NoSuchFieldException
     * @throws java.lang.IllegalAccessException
     */
    public static void addLibraryPath(String pathToAdd) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        // get array of paths
        final String[] paths = (String[]) usrPathsField.get(null);

        // check if the path to add is already present
        for (String path : paths) {
            if (path.equals(pathToAdd)) {
                return;
            }
        }

        //add the new path
        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length - 1] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }
}
