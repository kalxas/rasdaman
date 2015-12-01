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
 *
 * @author Dimitar Misev
 */
public class BaseX implements Database {

  private static Logger log = LoggerFactory.getLogger(BaseX.class);
  private static final String XML_EXTENSION = ".xml";
  private static final String TEXT_INDEX_TYPE = "text";
  private Context context;
  // collection name -> absolute path to initalization file
  private java.util.Set<String> collections;

  public BaseX(Map<String, String> collections) {
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

    for (String coll : this.collections) {
      try {
        new Set("CREATEFILTER", "*" + XML_EXTENSION).execute(context);
        new Open(coll).execute(context);
        new CreateIndex(TEXT_INDEX_TYPE).execute(context);
        log.info("Database connection to " + coll + " successfully opened.");
      } catch (Exception ex) {
        log.debug("The database " + coll + " doesn't seem to exist");
        try {
          log.info("Initializing database " + coll);
          String xml = IOUtil.fileToString(collections.get(coll));

          xml = StringUtil.fixLinks(xml, StringUtil.SERVICE_URI);
          log.trace("Creating database with content (trimmed to first 3000 characters):\n{}",
              xml.substring(0, Math.min(xml.length(), 3000)));
          new CreateDB(coll, xml).execute(context);
          log.info("Database successfully initialized.");
          log.trace("Database information:\n" + new InfoDB().execute(context));
        } catch (Exception e) {
          log.error("Failed initializing the " + coll + " database", e);
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
   * @param databaseName DatabaseName
   * @return String
   * @throws SecoreException
   */
  private String query(String query, String databaseName) throws SecoreException {
    try {
      long start = System.currentTimeMillis();
      new Open(databaseName).execute(context);
      String ret = null;
      boolean user = DbManager.USER_DB.equals(databaseName);
      boolean cached = false;

      if (DbManager.cacheContains(query, user)) {
        Pair<String, Boolean> tmp = DbManager.getCached(query);
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
          DbManager.updateCache(query, ret, user);
        }
      }
      return ret;
    } catch (Exception e) {
      throw new SecoreException(ExceptionCode.InternalComponentError, "Failed at querying the database", e);
    } finally {
      close();
    }
  }

  /**
   * Query User or Gml Dictionaries
   * @param query XQuery
   * @return String
   * @throws SecoreException
   */
  public String query(String query) throws SecoreException {
    log.trace("Executing query");
    String ret = null;
    ret = queryUser(query);
    if (StringUtil.emptyQueryResult(ret)) {
      ret = queryEpsg(query);
    }
    return ret;
  }

  /**
   * UpdateQuery to add the right DBName
   *
   * @param query String XQuery with "COLLECTION_NAME"
   * @param db String DBName
   * @return String != "" if error
   * @throws SecoreException
   */
  public String updateQuery(String query, String db) throws SecoreException {
    log.trace("Executing update query");
    query = StringUtil.fixLinks(query, StringUtil.SERVICE_URI);

    // in here should check if identifier is existing in GmlDictionary or UserDictionary then update query correspond with DB name
    String ret = Constants.EMPTY;
    if (db.equals(DbManager.USER_DB)) {
      ret = queryUser(query);
    } else if (db.equals(DbManager.EPSG_DB)) {
      ret = queryEpsg(query);
    }
    return ret;
  }

  /**
   * If query EPSG database then update query with "Gml"
   *
   * @param query String XQuery need to update
   * @return String query
   * @throws SecoreException
   */
  public String queryEpsg(String query) throws SecoreException {
    String ret = null;
    if (query == null) {
      return Constants.EMPTY;
    }
    query = query.replaceAll(Constants.COLLECTION_NAME, DbManager.EPSG_DB);
    log.trace("Executing query against the EPSG database: " + query);
    ret = query(query, DbManager.EPSG_DB);
    return ret;
  }

  /**
   * If query User database then update query with "userdb"
   *
   * @param query String XQuery need to update
   * @return String != "" if error
   * @throws SecoreException
   */
  public String queryUser(String query) throws SecoreException {
    String ret = null;
    if (query == null) {
      return Constants.EMPTY;
    }
    query = query.replaceAll(Constants.COLLECTION_NAME, DbManager.USER_DB);
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
