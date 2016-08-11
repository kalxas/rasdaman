/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secore.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import secore.db.DbManager;
import static secore.util.Constants.*;

/**
 * Secore utility methods, for handling definition CRUD, etc.
 *
 * @author Dimitar Misev
 */
public class SecoreUtil {

  private static Logger log = LoggerFactory.getLogger(SecoreUtil.class);

  public static String USER_FLAG_KEY = "__USER__";
  public static String USER_FLAG = "1";
  public static String EPSG_FLAG = "0";

  /**
   * Insert new definition to User Dictionary (NOTE: always insert to User)
   *
   * @param newd String new definition to insert
   * @param id String identifier to insert
   * @return String != "" if error
   * @throws SecoreException
   */
  public static String insertDef(String newd, String id) throws SecoreException {
    log.trace("Insert definition with identifier: " + id);

    newd = StringUtil.fixDef(newd, StringUtil.SERVICE_URI, DbManager.FIX_USER_VERSION_NUMBER);
    String newdId = StringUtil.getElementValue(newd, IDENTIFIER_LABEL);
    if (newdId != null) {
      id = newdId;
    }

    String error = Constants.EMPTY;

    // version in epsg database is FIX_VERSION (
    String versionNumber = DbManager.FIX_GML_COLLECTION_NAME;        
    String result = queryDef(id, true, true, false, versionNumber);
    
    if (StringUtil.emptyQueryResult(result)) {
      String query = "declare namespace gml = \"" + NAMESPACE_GML + "\";" + NEW_LINE
          + "let $x := collection('" + COLLECTION_NAME + "')" + NEW_LINE
          + "return insert node <dictionaryEntry xmlns=\"" + NAMESPACE_GML + "\">"
          + newd
          + "</dictionaryEntry> into $x";
      DbManager.clearCache();
      try {
        DbManager.getInstance().getDb().updateQuery(query, DbManager.USER_DB); // Only allow user to add in userdb
      } catch (Exception e) {
        StringUtil.printStackTraceWhenEditDB("adding", e);
        error = e.getMessage();
      }
    } else {
      // If user try to insert but with same identifier then update the existing definition.
      error = updateDef(newd, id, DbManager.USER_DB); // Only allow user to add/edit in userdb.
    }
    return error;
  }

  /**
   * Update definition in User dictionaries by identifier inside DB
   * NOTE: no edit in any GML versions dictionaries
   * @param mod String "new definition"
   * @param id String "identifier"
   * @param collectionName the database's name of User dictionary
   * @return error != "" if can not update
   * @throws SecoreException
   */
  public static String updateDef(String mod, String id, String collectionName) throws SecoreException {
    log.trace("Update definition with identifier: " + id);
    mod = StringUtil.fixDef(mod, StringUtil.SERVICE_URI, DbManager.FIX_USER_VERSION_NUMBER);
    id = StringUtil.removeLastColon(id);
    String query = "declare namespace gml = \"" + NAMESPACE_GML + "\";" + NEW_LINE
        + "let $x := collection('" + COLLECTION_NAME + "')//gml:identifier[text() = '" + id + "']/.." + NEW_LINE
        + "return replace node $x with " + mod;

    String error = Constants.EMPTY;
    DbManager.clearCache();
    try {
      DbManager.getInstance().getDb().updateQuery(query, collectionName);
    } catch (Exception e) {
      StringUtil.printStackTraceWhenEditDB("updating", e);
      error = e.getMessage();
    }
    return error;
  }

  /**
   * Query definition according to given identifier. If both the user and epsg
   * arguments are false then whichever gives a non-empty result first (user has
   * precedence) is returned; if both are true then the results from querying
   * both are concatenated into a final result (user has precedence again).
   *
   * @param id definition id
   * @param equality true forces check for id equality, otherwise only for a
   * substring of the definition identifier
   * @param user query the user database
   * @param epsg query the epsg database
   * @param versionNumber the version of epsg database should query (e.g: gml: 8.5 or userdb: 0)
   * @return the result definition
   * @throws SecoreException
   */
  public static String queryDef(String id, boolean equality, boolean user, boolean epsg, String versionNumber) throws SecoreException {
    return queryDef(id, equality, user, epsg, versionNumber, null);
  }

  /**
   * Query definition according to given identifier. If both the user and epsg
   * arguments are false then whichever gives a non-empty result first (user has
   * precedence) is returned; if both are true then the results from querying
   * both are concatenated into a final result (user has precedence again).
   *
   * @param id definition id
   * @param equality true forces check for id equality, otherwise only for a
   * substring of the definition identifier
   * @param user query the user database
   * @param epsg query the epsg database
   * @param versionNumber the version of epsg database should query (e.g: gml: 8.5 or 0)
   * @param elname element name to use in the list output
   * @return the result definition
   * @throws SecoreException
   */
  public static String queryDef(String id, boolean equality, boolean user, boolean epsg, String versionNumber, String elname) throws SecoreException {
    log.trace("Query definition with identifier: " + id);
    id = StringUtil.unwrapUri(id);

    String query = null;
    if (equality) {
      // A bug with contains is def/crs/EPSG/0/2000 will returns also (epsg-crs-20004, epsg-crs-20005,..) then it cannot update so change to ends-with
      // http://www.xqueryfunctions.com/xq/fn_ends-with.html

      // NOTE: all area definitions have 2 gml:identifier inside them, then only have to query definition 1 time.
      // Using (collection....)[1]
      query = "declare namespace gml = \"" + NAMESPACE_GML + "\";" + NEW_LINE
          + "let $d := (collection('" + COLLECTION_NAME + "')//gml:identifier[ends-with(.,'" + id + "')]/..)[1]" + NEW_LINE
          + "return" + NEW_LINE
          + " if (exists($d)) then $d" + NEW_LINE
          + " else <empty/>";
    } // NOTE: 2 queries below which return a list of URL by text() will have duplicate value with area definitions
    // However, sortElements will remove duplicate URLs -> no need to fix problem as it is hard when using XQUERY
    else if (elname != null) {
      query = "declare namespace gml = \"" + NAMESPACE_GML + "\";" + NEW_LINE
          + "let $x := collection('" + COLLECTION_NAME + "')//gml:identifier[contains(.,'" + id + "')]/text()" + NEW_LINE
          + "return" + NEW_LINE
          + " if (exists($x)) then for $i in $x return <" + elname + ">{$i}" + "</" + elname + ">" + NEW_LINE
          + " else <empty/>";
    } else {
      query = "declare namespace gml = \"" + NAMESPACE_GML + "\";" + NEW_LINE
          + "let $x := collection('" + COLLECTION_NAME + "')//gml:identifier[contains(.,'" + id + "')]/text()" + NEW_LINE
          + "return" + NEW_LINE
          + " if (exists($x)) then for $i in $x return <el>{$i} " + USER_FLAG_KEY + "</el>" + NEW_LINE
          + " else <empty/>";
    }

    if (user && epsg) {
      // In case of listing CRS definitions from both kind of DB (userdb, gml_*) it will query from both DB.
      // NOTE: as only have 1 userdb collection then if query a definition with un-exisiting versionNumber, it will just return empty.
      // But it can have multiple epsg collections, which based on versionNumber (e.g: version: 8.5 -> gml_85), then if the versionNumber does not exist
      // it will throw an exception as the collection also does not exist (e.g: http://localhost:8080/def/crs/EPSG/9/3857)
      String userRes = DbManager.getInstance().getDb().queryUser(query.replace(USER_FLAG_KEY, USER_FLAG), versionNumber);
      // then with kind of query in both userdb and gml_*, the gml_* does not exist, means it does not have the definition in gml db
      String epsgRes = Constants.EMPTY;
      try {
        epsgRes = DbManager.getInstance().getDb().queryEpsg(query.replace(USER_FLAG_KEY, EPSG_FLAG), versionNumber);
      } catch (SecoreException e) {
          if (e.getExceptionCode().equals(ExceptionCode.VersionNotFoundException)) {
            epsgRes = Constants.EMPTY_XML;
          } else {
              // other error, then just throw the exception.
              throw e;
          }
      }
      String ret = Constants.EMPTY;
      if (!StringUtil.emptyQueryResult(userRes)) {
        ret += userRes;
      }
      if (!StringUtil.emptyQueryResult(epsgRes)) {
        if (!StringUtil.emptyQueryResult(ret)) {
          ret += "\n";
        }
        ret += epsgRes;
      }
      return ret;
    } else if (user) {
      return DbManager.getInstance().getDb().queryUser(query.replace(USER_FLAG_KEY, USER_FLAG), versionNumber);
    } else if (epsg) {
      return DbManager.getInstance().getDb().queryEpsg(query.replace(USER_FLAG_KEY, EPSG_FLAG), versionNumber);
    } else {
      return DbManager.getInstance().getDb().queryBothDB(query.replace(USER_FLAG_KEY, USER_FLAG), versionNumber);
    }
  }
  
  /**
   * Check if a crs URI (e.g: crs/EPSG/0/4326) exist in user db.
   * @param id
   * @param versionNumber
   * @return 
   * @throws secore.util.SecoreException 
   */
  public static boolean existsDefInUserDB(String id, String versionNumber) throws SecoreException {
    // NOTE: userdb version number is not consistent (e.g: it can has def/crs/OGC/0 or def/crs/AUTO/1.3), not as GML Dictionary.    
    String query = "declare namespace gml = \"" + NAMESPACE_GML + "\";" + NEW_LINE
          + "let $d := (collection('" + DbManager.USER_DB + "')//gml:identifier[contains(.,'" + id + "')]/..)[1]" + NEW_LINE
          + "return" + NEW_LINE
          + " if (exists($d)) then $d" + NEW_LINE
          + " else <empty/>";
    
    String ret = DbManager.getInstance().getDb().queryUser(query, versionNumber);
    if (ret.equals(Constants.EMPTY_XML)) {
      return false;
    }
    
    return true;    
  }

  /**
   * Delete definition (NOTE: only delete from User DB)
   *
   * @param id String identifier to delete
   * @param todel String
   * @return String
   * @throws SecoreException
   */
  public static String deleteDef(String id, String todel) throws SecoreException {
    log.trace("Delete definition with identifier: " + id);

    String fullID = id + todel;
    String childIDs = fullID + "/.*";
    String query
        = "declare namespace gml = \"" + NAMESPACE_GML + "\";" + NEW_LINE
        + "for $x in collection('" + COLLECTION_NAME + "')//gml:identifier[ends-with(.,'" + fullID + "')]/.. "
        + "union doc('" + COLLECTION_NAME + "')//gml:identifier[matches(.,'" + childIDs + "')]/.." + NEW_LINE
        + "return delete node $x";

    String error = Constants.EMPTY;
    try {
      DbManager.getInstance().getDb().updateQuery(query, DbManager.USER_DB); // only allow to delete inside "userdb"
      DbManager.clearCache();
    } catch (Exception e) {
      StringUtil.printStackTraceWhenEditDB("deleting", e);
      error = e.getMessage();
    }
    return error;
  }

  /**
   * Sort Element (URI of entries)
   *
   * @param url
   * @param versionNumber the version of dictionaries which want to query (e.g: 8.5, 0 )
   * @param result
   * @return Set
   */
  public static Set<Pair<String, Boolean>> sortElements(String url, String versionNumber, String result) {
    url = StringUtil.wrapUri(url);
    log.debug("sortElements: " + url);
    result = result.replace("</el>", "");
    String[] ids = result.split("<el>");
    Set<Pair<String, Boolean>> list = new TreeSet<Pair<String, Boolean>>(new Comparator<Pair<String, Boolean>>() {
      public int compare(Pair<String, Boolean> o1, Pair<String, Boolean> o2) {
        try {
          return Integer.parseInt(o1.fst.replaceAll("[\n\r]", ""))
              - Integer.parseInt(o2.fst.replaceAll("[\n\r]", ""));
        } catch (Exception ex) {
          return o1.fst.compareTo(o2.fst);
        }
      }
    });
    
    
    List<String> urlTmps = new ArrayList<String>();
    // Try every requested and default versions to make sure it don't ignore any result.
    String urlTmp0 = url.replace(Constants.VERSION_NUMBER, versionNumber);
    String urlTmp1 = url.replace(Constants.VERSION_NUMBER, DbManager.FIX_GML_VERSION_NUMBER);
    String urlTmp2 = url.replace(Constants.VERSION_NUMBER, DbManager.FIX_GML_VERSION_ALIAS);

    // try with requested version
    urlTmps.add(urlTmp0);
    // try with version 8.5
    urlTmps.add(urlTmp1);
    // try with version 0
    urlTmps.add(urlTmp2);

    for (String tmpid : ids) {
      String[] pair = tmpid.split(" ");
      String id = pair[0];
      Boolean user = false;
      try {
        user = USER_FLAG.equals(pair[1].replaceAll("[\n\r]", ""));
      } catch (Exception ex) {
      }

      // NOTE: if url has contain VERSION_NUMBER (e.g: /def/crs/EPSG/0), it will get the result's version to change.
      // e.g: /def/crs/EPSG/8.5/4326 (replace VERSION_NUMBER by 8.5)
      if (!id.isEmpty()) {
        id = StringUtil.wrapUri(id);
        String tmp = id;
        
        int ind = 0;
        String urlTmp = "";
        
        // Check if the result CRS has the identifier
        for (String s: urlTmps) {
          ind = tmp.indexOf(s);
          if (ind != -1 ) {
            urlTmp = s;
            break;
          }
        }
        // If the result does not contain the identifier (it can be "").
        if (ind == -1) {
            continue;
        } else {
          tmp = tmp.substring(ind + urlTmp.length());
          if (tmp.startsWith(REST_SEPARATOR)) {
            tmp = tmp.substring(1);
          }
          if (tmp.contains(REST_SEPARATOR)) {
            tmp = tmp.substring(0, tmp.indexOf(REST_SEPARATOR));
          }
          if (tmp.equals(EMPTY)) {
            continue;
          }
        }

        Pair<String, Boolean> epsgEntry = Pair.of(tmp, false);
        Pair<String, Boolean> userEntry = Pair.of(tmp, true);
        if (user) {
          if (list.contains(epsgEntry)) {
            list.remove(epsgEntry);
          }
          list.add(userEntry);
        } else {
          if (!list.contains(userEntry)) {
            list.add(epsgEntry);
          }
        }
      }
    }
    return list;
  }
}
