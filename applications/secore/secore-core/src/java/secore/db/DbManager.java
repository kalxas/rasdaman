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
package secore.db;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import secore.util.Config;
import secore.util.Constants;
import secore.util.IOUtil;
import secore.util.Pair;

/**
 * Holds an instance to the database.
 *
 * @author Dimitar Misev
 */
public class DbManager {
  
  private static Logger log = LoggerFactory.getLogger(DbManager.class);
  
  // should be "epsgdb" or so, but for backwards compatibility we stick to "gml"
  public static String EPSG_DB = "gml";
  public static String EPSG_DB_FILE = "GmlDictionary.xml";
  public static String USER_DB = "userdb";
  public static String USER_DB_FILE = "UserDictionary.xml";
  
  private Database db;
  private static DbManager instance;
  
  private static final Map<String, Pair<String, Boolean>> cache = 
      new HashMap<String, Pair<String, Boolean>>();
  
  private DbManager() {
    // collection name -> absolute path to initalization file
    Map<String, String> collections = new HashMap<String, String>();
    String file = null;
    try {
      file = IOUtil.findFile(Config.getInstance().getGmlDefPath() + EPSG_DB_FILE).toString();
      collections.put(EPSG_DB, file);
    } catch (IOException ex) {
      log.warn("Failed finding EPSG init file", ex);
    }
    try {
      file = IOUtil.findFile(Config.getInstance().getGmlDefPath() + USER_DB_FILE).toString();
      collections.put(USER_DB, file);
    } catch (IOException ex) {
      log.warn("Failed finding USER init file", ex);
    }
    
    db = new BaseX(collections);
  }
  
  public static DbManager getInstance() {
    if (instance == null || instance.getDb() == null) {
      instance = new DbManager();
    }
    return instance;
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
  }
  
  public static void updateCache(String key, String value, Boolean user) {
    if (value != null && !value.equals(Constants.EMPTY_XML)) {
      cache.put(key, Pair.of(value, user));
    }
  }
  
  public static Pair<String, Boolean> getCached(String key) {
    return cache.get(key);
  }
  
  public static boolean cacheContains(String key, Boolean user) {
    return cache.containsKey(key) && cache.get(key).snd.equals(user);
  }
}
