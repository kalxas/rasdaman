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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.secore.ConfigManager;
import org.rasdaman.secore.Constants;
import org.rasdaman.secore.util.ExceptionCode;
import org.rasdaman.secore.util.Pair;
import org.rasdaman.secore.util.SecoreException;

/**
 * Holds an instance to the database.
 *
 * @author Dimitar Misev
 */
public class DbManager {

    private static final Logger log = LoggerFactory.getLogger(DbManager.class);

    // should be "epsgdb" or so, but for backwards compatibility we stick to "gml"
    public static final String EPSG_DB = "gml";
    public static final String EPSG_DB_FILE = "GmlDictionary.xml";
    public static final String USER_DB = "userdb";
    public static final String USER_DB_FILE = "UserDictionary.xml";


    // keep backwards support (e.g: http://localhost:8080/def/crs/OGC/0/Index2D/)
    // so if request does not have the specific version, it should load the GMLDictionary from this version
    public static final String FIX_GML_VERSION_NUMBER = "8.5";
    public static final String FIX_GML_VERSION_ALIAS = "0";

    // it can be changed (e.g: crs/OGC/0/Index2D -> crs/OGC/1.3/Index2D)
    public static final String FIX_USER_VERSION_NUMBER = "0";

    // BaseX cannot use "." in the collection name
    // collection name -> absolute path to initalization file
    // NOTE: gml database can have many versions (e.g: 8.5, 8.6,...) then use gml_version as key (e.g: gml_8.5)
    public static final String FIX_GML_COLLECTION_NAME = createGMLCollectionName(FIX_GML_VERSION_NUMBER);    
    
    private Database db;
    private static DbManager instance;

    /**
     * To notify Servlet when should clear the cache (i.e: when BaseX update/delete/insert definitions) then need to clear cache from BaseX query and also on Servlet.
     */
    private static boolean needToClearCache = false;

    // Cache query, (return and boolean) - version to know this query is from EPSG dictionary or User dictionary.
    private static final Map<String, Pair<String, String>> cache =
        new HashMap<String, Pair<String, String>>();

    /**
     * NOTE: collection name in XQuery cannot have ".".
     * pair (versionNumber, collectionNumber)  (e.g: 8.5, gml_85)
     * and full path to XML dictionary files. (e.g: .../ect/gml/$VERSION/GmlDictionary.xml)
     */
    public static final Map<DbCollection, String> collections = new HashMap<DbCollection, String>();
    private List<String> supportedGMLCollectionVersions = new ArrayList<>();
    
    private DbManager() throws SecoreException {
        
        boolean hasFixVersion = false;
        boolean hasUserDBFile = false;

        // path to fixed version of Gml file (e.g: .../etc/gml/8.5/GmlDictionary.xml)
        String fixedGmlFilePath = "";

        // 1. Load all the EPSG folders with version name in the configuration of secore.conf (normally it is in etc/gml).
        File folder = new File(ConfigManager.getInstance().getGMLDirectory());
        // Get the folders and 1 UserDictionary.xml in the etc/gml/
        List<File> files = new ArrayList<>(Arrays.asList(folder.listFiles()));
        
        // Default version 0 is what GML collection supports
        supportedGMLCollectionVersions.add(FIX_GML_VERSION_ALIAS);

        // Iterate all the EPSG versions in the config directory
        for (File f : files) {
            if (f.getName().equals(USER_DB_FILE)) {
                // it is a UserDB file then return an object (e.g: (0, userdb)).
                DbCollection obj = new DbCollection(FIX_USER_VERSION_NUMBER, USER_DB);
                collections.put(obj, f.getAbsolutePath());
                hasUserDBFile = true;
            } else {
                String versionNumber = f.getName();
                supportedGMLCollectionVersions.add(versionNumber);
                // it is folder, get deeper to get the GMLDictionary file
                // e.g: gml_85, gml_892
                String collectionName = createGMLCollectionName(versionNumber);
                String gmlFilePath = f.getAbsolutePath() + "/" + EPSG_DB_FILE;
                File gmlFile = new File(gmlFilePath);
                if (!f.exists()) {
                    log.error("Failed finding EPSG init file");
                    throw new SecoreException(ExceptionCode.GMLDictionaryFileNotFoundException, "Cannot load GML Dictionary file: "
                                              + gmlFile.getAbsolutePath() + " for this version: " + collectionName);
                } else {
                    // put versions (number and collectionName) and file paths in the collection
                    DbCollection obj = new DbCollection(versionNumber, collectionName);
                    collections.put(obj, gmlFilePath);

                    if (collectionName.equals(FIX_GML_COLLECTION_NAME)) {
                        hasFixVersion = true;
                        fixedGmlFilePath = gmlFilePath;
                    }
                }
            }
        } 
        
        // Sort the versions of EPSG GML from oldest to newest
        java.util.Collections.sort(supportedGMLCollectionVersions);

        // Check if it has FixVersion file
        if (!hasFixVersion) {
            log.error("Failed finding stable GML dictionary file, version: " + FIX_GML_COLLECTION_NAME);
            throw new SecoreException(ExceptionCode.GMLDictionaryFileNotFoundException, "Cannot load stable GML Dictionary file, version: " + FIX_GML_COLLECTION_NAME);
        }

        // Check if it has UserDirectory file
        if (!hasUserDBFile) {
            log.error("Failed finding User init file");
            throw new SecoreException(ExceptionCode.UserDictionaryFileNotFoundException, "Cannot load GML User Dictionary file.");
        }

        // NOTE: to support backwards, we use the default GML dictionary as a collection 0.
        // DbKeyValue for GML dictionary version 0: (gml_0, gml_0), User dictionary version 0: (0, userdb)
        String collectionName = createGMLCollectionName(FIX_GML_VERSION_ALIAS);
        DbCollection obj = new DbCollection(collectionName, collectionName);
        collections.put(obj, fixedGmlFilePath);

        // initialise the database from dictionary files
        db = new BaseX(collections);
    }

    /**
     * Utility to create a collection name from version for GMLDictionary
     * @param versionNumber GML EPSG version (e.g: 8.5)
     * @return  string
     */
    public static String createGMLCollectionName(String versionNumber) {
        // first replace "." in version (e.g: 8.5 -> 85)
        String tmp = versionNumber.replace(".", "");
        // then add the prefix (gml_85)
        String collectionName = EPSG_DB + "_" + tmp;
        return collectionName;

    }

    /**
     * Utility to get a collection version number (e.g: 8.5) by collection name (gml_8.5)
     * @param collectionName
     * @return
     */
    public static String getCollectionVersionNumber(String collectionName) {
        String versionNumber = "";
        for (DbCollection coll : collections.keySet()) {
            if (coll.getCollectionName().equals(collectionName)) {
                versionNumber = coll.getVersionNumber();
            }
        }
        return versionNumber;
    }

    /**
     * Static factory method to create the BaseX database for first load SECORE or read from caches if not.
     * @return
     * @throws SecoreException
     * @throws java.io.IOException
     */
    public static DbManager getInstance() throws SecoreException {
        if (instance == null || instance.getDb() == null) {
            instance = new DbManager();
        }
        return instance;
    }

    /**
     * Check if collectionName does exist on the versions list.
     * // e.g: userdb, gml_85
     * @param collectionName
     * @return
     */
    public static boolean collectionExistByCollectionName(String collectionName) {
        // pair(8.5, "gml_8.5")
        for (DbCollection coll : collections.keySet()) {
            if (coll.getCollectionName().equals(collectionName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if versionNumber does exist on the versions list
     * @param versionNumber (e.g: 8.5, 0)
     * @return
     */
    public static boolean collectionExistByVersionNumber(String versionNumber) {
        for (DbCollection coll : collections.keySet()) {
            if (coll.getVersionNumber().equals(versionNumber)) {
                return true;
            }
        }

        return false;
    }
    
    public List<String> getSupportedGMLCollectionVersions() {
        return this.supportedGMLCollectionVersions;
    }
    
    
    /**
     * Get the latest supported EPSG GML version in SECORE (e.g: 9.4.2)
     */
    public String getLatestGMLCollectionVersion() {
        int index = this.supportedGMLCollectionVersions.size() - 1;
        return this.supportedGMLCollectionVersions.get(index);
    }

    public Database getDb() {
        return db;
    }

    public void setDb(Database db) {
        this.db = db;
    }

    /*
     * Cache maintenance
     */
    public static void clearCache() {
        cache.clear();
        // servlet need to clear cache as well
        needToClearCache = true;
    }

    /**
     *  Servlet already clear its cache
     */
    public static void clearedCache() {
        needToClearCache = false;
    }

    public static boolean getNeedToClearCache() {
        return needToClearCache;
    }

    public static void updateCache(String key, String value, String version) {
        if (value != null && !value.equals(Constants.EMPTY_XML)) {
            cache.put(key, Pair.of(value, version));
        }
    }

    public static Pair<String, String> getCached(String key) {
        return cache.get(key);
    }

    public static boolean cacheContains(String key, String version) {
        return cache.containsKey(key) && cache.get(key).snd.equals(version);
    }
}
