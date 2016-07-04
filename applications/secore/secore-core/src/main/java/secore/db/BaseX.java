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

import secore.util.SecoreException;
import secore.util.ExceptionCode;
import java.util.Map;
import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.Get;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import secore.util.Constants;
import secore.util.IOUtil;
import secore.util.Pair;
import secore.util.StringUtil;
import secore.util.XMLUtil;


/**
 * BaseX backend.
 * Run query with userdb and gml_* dictionaries file.
 *
 * @author Dimitar Misev
 */
public class BaseX implements Database {

  private static Logger log = LoggerFactory.getLogger(BaseX.class);
  private static final String XML_EXTENSION = ".xml";
  private static final String TEXT_INDEX_TYPE = "text";
  private Context context;
  // collection name -> absolute path to initalization file
  private java.util.Set<DbCollection> collections;

  
  /**
   * (versionNumber, collectionName) and XML files path
   * e.g: (8.5, gml_8.5) and ".../ect/gml/8.5/GmlDictionar.xml")
   * @param collections
   */  
  public BaseX(Map<DbCollection, String> collections) {
    this.collections = collections.keySet();

    context = new Context();

    try {
      // determine configuration directory in which to put the database
      String configuredDir = new Get(Constants.DBPATH_BASEX_PROPERTY).execute(context);
      if (configuredDir != null) {
        configuredDir = configuredDir.trim(); // remove ending new line
        configuredDir = configuredDir.replace(Constants.DEFAULT_SECORE_DB_DIR_PREFIX, "");
      }
      if (Constants.DEFAULT_SECORE_DB_DIR.equals(configuredDir)) {
        String secoreDbDir = IOUtil.getDbDir();
        if (secoreDbDir != null) {
          new Set(Constants.DBPATH_BASEX_PROPERTY, secoreDbDir).execute(context);
          log.debug("Secore database directory: " + secoreDbDir);
        } else {
          log.warn("Default secore database directory, please "
              + "consider updating DBPATH in .basex: " + configuredDir);
        }
      } else {
        log.debug("Secore database directory from .basex: '" + configuredDir + "'");
      }
    } catch (Exception ex) {
      log.warn("Failed setting secore database directory, using DBPATH value from .basex");
    }

    // Iterate all the versions which can be initialized
    for (DbCollection collection : this.collections) {
      // Get the collection name to create database name (e.g: gml_862)
      String collectionName  = collection.getCollectionName();
      try {
        new Set("CREATEFILTER", "*" + XML_EXTENSION).execute(context);
        // Create a database from collection name (e.g: gml_862)
        new Open(collectionName).execute(context);
        new CreateIndex(TEXT_INDEX_TYPE).execute(context);
        log.info("Database connection to " + collectionName + " successfully opened.");

      } catch (Exception ex) {

        log.debug("The database " + collectionName + " doesn't seem to exist");

        try {
          log.info("Initializing database " + collectionName);
          // Get the xml file path from collection.
          String xmlFile = collections.get(collection);
          // Read xml file content and set to a string
          String xml = IOUtil.fileToString(xmlFile);
          // e.g: 8.5, 0, gml_0
          String versionNumber = collection.getVersionNumber();
          
          // NOTE: only Gml version 0 has key "gml_0" as same as value "gml_0"
          if(versionNumber.contains(DbManager.EPSG_DB)) {
            // collection name: gml_0, version number: 0
            versionNumber = versionNumber.split("_")[1];
          }

          // This will change the URN to URI (e.g: urn:ogc:def:crs:EPSG::4326) to "/def/crs/EPSG/$VERSION/4326)
          xml = StringUtil.fixLinks(xml, StringUtil.SERVICE_URI, versionNumber);
          /*log.trace("Creating database with content (trimmed to first 3000 characters):\n{}",
              xml.substring(0, Math.min(xml.length(), 3000)));*/

          // Create db for each version of dictionary file.
          new CreateDB(collectionName, xml).execute(context);
          log.info("Database successfully initialized.");
          log.trace("Database information:\n" + new InfoDB().execute(context));
        } catch (Exception e) {
          log.error("Failed initializing the " + collectionName + " database", e);
        }
      } finally {
        close();
      }
    }
  }

  public void close() {
    try {
      new Close().execute(context);
    } catch (Exception ex) {
    }
  }

  /**
   * Query query with DB Name
   * @param query Xquery
   * @param collectionName the database name of the collection should be queried (e.g: gml_85 or userdb)
   * @return String
   * @throws SecoreException
   */
  private String query(String query, String collectionName) throws SecoreException {   
    try {
      long start = System.currentTimeMillis();
      new Open(collectionName).execute(context);
      String ret = null;
      // to know which database is used in cache.
      boolean cached = false;

      if (DbManager.cacheContains(query, collectionName)) {
        Pair<String, String> tmp = DbManager.getCached(query);
        ret = tmp.fst;
        cached = true;
      } else {
        ret = new XQuery(query).execute(context); // error here when update with empty value in query
      }
      long end = System.currentTimeMillis();
      if (!StringUtil.emptyQueryResult(ret)) {
        log.trace("Query successfully executed in " + (end - start) + "ms");
        log.trace("Result (trimmed to first 300 characters):\n" + ret.substring(0, Math.min(300, ret.length())));
        if (!cached) {
          DbManager.updateCache(query, ret, collectionName);
        }
      }
      return ret;
    } catch (Exception e) {
      String error = "Failed at querying the database, detail: " + e.getMessage();
      error = error + " Check collection name and version are valid first.";
      throw new SecoreException(ExceptionCode.InternalComponentError, error, e);
    } finally {
      close();
    }
  }

  /**
   * Query User or Gml Dictionaries
   * @param query XQuery
   * @param versionNumber
   * @return String
   * @throws SecoreException
   */
  public String queryBothDB(String query, String versionNumber) throws SecoreException {
    log.trace("Executing query");
    String ret = null;
    log.trace("in userdb...");
    // NOTE: userdb does not have version (only 1 file) then don't use version here.
    ret = queryUser(query, versionNumber);
    if (StringUtil.emptyQueryResult(ret)) {
      log.trace("in gmldb, version number: " + versionNumber + "...");
      ret = queryEpsg(query, versionNumber);
    }
    return ret;
  }

  /**
   * UpdateQuery to add the right DBName
   *
   * @param query String XQuery with "COLLECTION_NAME"
   * @param collectionName collection name of dictionary which want to query (e.g: userdb, gml_8.5)
   * @return String != "" if error
   * @throws SecoreException
   */
  public String updateQuery(String query, String collectionName) throws SecoreException {
    log.trace("Executing update query");
    query = StringUtil.fixLinks(query, StringUtil.SERVICE_URI);

    // in here should check if identifier is existing in GmlDictionary or UserDictionary then update query correspond with DB name
    String ret = Constants.EMPTY;
    if (collectionName.equals(DbManager.USER_DB)) {
      ret = queryUser(query, DbManager.FIX_USER_VERSION_NUMBER);
    } else {
      // it is EPSG database, then it need the versionNumber (e.g: 8.5)
      String versionNumber = DbManager.getCollectionVersionNumber(collectionName);      
      ret = queryEpsg(query, versionNumber);
    }
    return ret;
  }

  /**
   * If query EPSG database then update query with "Gml"
   *
   * @param query String XQuery need to update
   * @param versionNumber the version of EPSG database should query (8.5, 8.6)
   * @return String query
   * @throws SecoreException
   */
  public String queryEpsg(String query, String versionNumber) throws SecoreException {
    String ret = null;
    if (query == null) {
      return Constants.EMPTY;
    }
    
    String collectionName = "";
    // if versionNumber is gml_0 then use the collection name: gml_0
    String gml0 = DbManager.createGMLCollectionName(DbManager.FIX_GML_VERSION_ALIAS);
    if (versionNumber.equals(gml0)) {      
      versionNumber = DbManager.FIX_GML_VERSION_ALIAS;
      collectionName = gml0;
    } else if(versionNumber.equals("")) {
      // incomplete URL (e.g: /def/crs), version number is fixed GML (e.g: 8.5)
      versionNumber = DbManager.FIX_GML_VERSION_NUMBER;
      collectionName = DbManager.FIX_GML_COLLECTION_NAME;      
    } else {
      collectionName = DbManager.createGMLCollectionName(versionNumber);
    }       
    
    // NOTE: check if collectionName does exist.
    if (!DbManager.collectionExistByCollectionName(collectionName)) {
      throw new SecoreException(ExceptionCode.VersionNotFoundException, "Requested version: " + versionNumber + " does not exist.");
    }
    
    query = query.replaceAll(Constants.COLLECTION_NAME, collectionName);
    // NOTE: if query has used version number then need to change here to version number.
    query = query.replaceAll(Constants.VERSION_NUMBER, versionNumber);
    log.trace("Executing query against the EPSG database: " + query);

    ret = query(query, collectionName);
    return ret;
  }

  /**
   * If query User database then update query with "userdb"
   *
   * @param query String XQuery need to update
   * @return String != "" if error
   * @throws SecoreException
   */
  public String queryUser(String query, String versionNumber) throws SecoreException {
    String ret = null;
    if (query == null) {
      return Constants.EMPTY;
    }
    query = query.replaceAll(Constants.COLLECTION_NAME, DbManager.USER_DB);    
    // NOTE: if query has used version number then need to change here to the fix userdb version.
    query = query.replaceAll(Constants.VERSION_NUMBER, versionNumber);
    log.trace("Executing query against the USER database: " + query);
    ret = query(query, DbManager.USER_DB);
    return ret;
  }

  /**
   * Allow executing BaseX command in the current context.
   *
   * @param cmd command to execute
   * @return execution result
   * @throws SecoreException Just wraps around the BaseX exception
   */
  public Object executeCommand(Command cmd) throws SecoreException {
    Object ret = null;
    try {
      ret = cmd.execute(context);
    } catch (BaseXException ex) {
      throw new SecoreException(ExceptionCode.InternalComponentError,
          "Failed executing BaseX command: " + cmd.toString(), ex);
    }
    return ret;
  }
}
