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
package org.rasdaman.secore.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.secore.ConfigManager;
import org.rasdaman.secore.util.Constants;
import org.rasdaman.secore.util.SecoreException;
import org.rasdaman.secore.util.SecoreUtil;
import org.rasdaman.secore.util.StringUtil;

/**
 * This class will handle the select/insert/update SecoreDB versions from update scripts
 * i.e: 1-insert file is the latest file in scripts folder, then SecoreDB versions is updated to 1.
 * The script folders is $RMANHOME/share/rasdaman/secore.
 * If SecoreDB verions is empty then it inserts with value 0.
 *
 * NOTE: clear cache when update, insert SecoreVersion
 *
 * @author Bang Pham Huu
 */
public class DbSecoreVersion {
    private static final Logger log = LoggerFactory.getLogger(DbSecoreVersion.class);
    private final Database baseX;

    // e.g: /home/rasdaman/install/share/rasdaman/secore (contains update files: 1-delete, 2-insert,...)
    private final String SECORE_DB = "SecoreDB";
    private final String SECORE_VERSION = "SecoreVersion";
    private final String DELETE_FILE = "delete";
    private final String INSERT_FILE = "insert";

    public DbSecoreVersion(Database baseX) {
        this.baseX = baseX;
    }

    /**
     * Handle problem with insert/update SecoreVersion from update script folders
     * @throws org.rasdaman.secore.util.SecoreException
     * @throws java.io.FileNotFoundException
     */
    public void handle() throws SecoreException, FileNotFoundException, IOException {
        String versionNumber = this.getVersion();
        log.debug("Current SecoreDB version is: " + versionNumber);
        if (versionNumber.equals(Constants.EMPTY_XML)) {
            // Insert the new domain element for SecoreVersion
            this.insertNewVersion();
            versionNumber = "0";
        }
        // Then check the update script folders and run the newer update files it they are available
        String updateFilesLatestVersion = this.getUpdateFilesLatestVersion(versionNumber);

        // There is a newer version number from update scripts folder.
        if (!updateFilesLatestVersion.equals(Constants.EMPTY)) {
            // Update the value for SecoreVersion
            this.updateVersion(updateFilesLatestVersion);
        }
    }

    /**
     * Get the latest version number from file name in update scripts folder and update BaseX database with the newer scripts if any.
     * @return
     */
    private String getUpdateFilesLatestVersion(String versionNumber) throws FileNotFoundException, SecoreException, IOException {        
        Integer latestVersionNumber = 0;
        File folder = new File(ConfigManager.getInstance().getDbUpdatesPath());
        log.debug("update version files from folder: " + folder.getCanonicalPath());
        if (folder.list() == null) {
            log.debug("No update script to update secoredb.");
        } else {
            File[] files = folder.listFiles();
            // NOTE: must sort the file names as Ubuntu can have different kind of order.
            Arrays.sort(files);
            Integer currentVersionNumber = new Integer(versionNumber);
            Integer maxNumber = 0;
            for (File updateFile : files) {
                String fileName = updateFile.getName();
                log.debug("Checking update files name: " + fileName);
                // Only handle correct update files (e.g: 1-insert, 2-delete, not: test.sh or 01-query.test)
                boolean match = fileName.matches("([0-9]+)-(" + this.INSERT_FILE + "|" + this.DELETE_FILE + ")");
                if (match) {
                    // e.g; 1-delete, 2-insert
                    Integer number = new Integer(fileName.split("-")[0]);
                    if (number > currentVersionNumber) {
                        // get the script content to update in BaseX database (userdb).
                        this.updateChangeFromFile(updateFile);
                        log.debug("Insert/Delete definition from file: " + updateFile.getCanonicalPath());
                        // get the latest version number to update in SecoreVersion value.
                        if (number > maxNumber) {
                            maxNumber = number;
                        }
                    }
                }
            }
                        
            if (maxNumber > currentVersionNumber) {
                latestVersionNumber = maxNumber;
            } else {
                latestVersionNumber = currentVersionNumber;
            }
            log.info("Latest update script version number is '" + latestVersionNumber + "'.");
        }
        
        return latestVersionNumber.toString();
    }

    /**
     * Update the userdb from changes (insert, delete) of file name.
     * e.g: 1-insert, then it will add the new GML definition to BaseX database.
     *      2-delete, then it will remove the GML definition by gml:identifier in file content.
     * @param file
     */
    private void updateChangeFromFile(File file) throws FileNotFoundException, SecoreException {
        String type = file.getName().split("-")[1];
        String content = new Scanner(file).useDelimiter("\\Z").next();
        // delete file should contain only gml:identifier (e.g: crs/EPSG/0/4326) to delete the CRS definition.
        if (type.equals(this.DELETE_FILE)) {
            SecoreUtil.deleteDef(content, "");
        } else if (type.equals(this.INSERT_FILE)) {
            // insert file should contain the validated CRS definition.
            String id = StringUtil.getElementValue(content, Constants.IDENTIFIER_LABEL);
            SecoreUtil.insertDef(content, id);
        }
    }

    /**
     * Check if SecoreDB already has the version number element.
     * e.g: <SecoreVersion>0</SecoreVersion>
     * @return
     * @throws SecoreException
     */
    private String getVersion() throws SecoreException {
        String query = "let $x := collection('" + DbManager.USER_DB + "')//" + this.SECORE_VERSION + "//text() " +
                       "return " +
                       "  if (exists($x)) then $x " +
                       "  else " + Constants.EMPTY_XML;
        return baseX.queryUser(query, DbManager.FIX_USER_VERSION_NUMBER);
    }

    /**
     * Insert the SecoreVersion element if it does not exist in BaseX database
     * @return
     * @throws SecoreException
     */
    public String insertNewVersion() throws SecoreException {
        String query = "let $x := collection('" + DbManager.USER_DB + "') " +
                       "return " +
                       "   insert node " +
                       "               <"  + this.SECORE_DB + "> " +
                       "                    <" + this.SECORE_VERSION + ">0</" + this.SECORE_VERSION + " > " +
                       "               </" + this.SECORE_DB + "> " +
                       "   into $x";
        String output = baseX.queryUser(query, DbManager.FIX_USER_VERSION_NUMBER);
        // if not clear cache the version number is kept as old number when select.
        DbManager.clearCache();
        log.debug("Inserted new version number: 0 to Secoredb.");
        return output;
    }

    /**
     * Update the SecoreVersion from the latest update scripts file name
     * e.g: 2-delete file then SecoreVersion value is updated to 2.
     * @param version
     * @return
     * @throws SecoreException
     */
    public String updateVersion(String version) throws SecoreException {
        String query = "let $x := collection('" + DbManager.USER_DB + "')//" + this.SECORE_VERSION + "//text() " +
                       "return replace node $x with " + version;
        String output = baseX.queryUser(query, DbManager.FIX_USER_VERSION_NUMBER);
        // if not clear cache the version number is kept as old number when select.
        DbManager.clearCache();
        log.debug("Updated Secoredb's version number to: " + version);
        return output;
    }
}
