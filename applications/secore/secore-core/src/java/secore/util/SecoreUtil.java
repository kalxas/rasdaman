/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secore.util;

import java.util.Comparator;
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

    newd = StringUtil.fixDef(newd, StringUtil.SERVICE_URI);
    String newdId = StringUtil.getElementValue(newd, IDENTIFIER_LABEL);
    if (newdId != null) {
      id = newdId;
    }

    String error = Constants.EMPTY;

    String result = queryDef(id, true, true, false);
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
      // If user try to insert but with same identifier then update definition
      error = updateDef(newd, id, DbManager.USER_DB); // Only allow user to add in userdb
    }
    return error;
  }

  /**
   * Update definition in User or Gml dictionaries by identifier isnide DB
   * (User, Gml)
   *
   * @param mod String "new definition"
   * @param id String "identifier"
   * @param db String "DB name"
   * @return error != "" if can not update
   * @throws SecoreException
   */
  public static String updateDef(String mod, String id, String db) throws SecoreException {
    log.trace("Update definition with identifier: " + id);
    mod = StringUtil.fixDef(mod, StringUtil.SERVICE_URI);
    id = StringUtil.removeLastColon(id);
    String query = "declare namespace gml = \"" + NAMESPACE_GML + "\";" + NEW_LINE
        + "let $x := collection('" + COLLECTION_NAME + "')//gml:identifier[text() = '" + id + "']/.." + NEW_LINE
        + "return replace node $x with " + mod;

    String error = Constants.EMPTY;
    DbManager.clearCache();
    try {
      DbManager.getInstance().getDb().updateQuery(query, db);
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
   * @return the result definition
   * @throws SecoreException
   */
  public static String queryDef(String id, boolean equality, boolean user, boolean epsg) throws SecoreException {
    return queryDef(id, equality, user, epsg, null);
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
   * @param elname element name to use in the list output
   * @return the result definition
   * @throws SecoreException
   */
  public static String queryDef(String id, boolean equality, boolean user, boolean epsg, String elname) throws SecoreException {
    log.trace("Query definition with identifier: " + id);
    id = StringUtil.unwrapUri(id);

    String query = null;
    if (equality) {
      // A bug with contains is def/crs/EPSG/0/2000 will returns also (epsg-crs-20004, epsg-crs-20005,..) then it cannot update so change to ends-with
      // http://www.xqueryfunctions.com/xq/fn_ends-with.html
      query = "declare namespace gml = \"" + NAMESPACE_GML + "\";" + NEW_LINE
          + "let $d := collection('" + COLLECTION_NAME + "')//gml:identifier[ends-with(.,'" + id + "')]/.." + NEW_LINE
          + "return" + NEW_LINE
          + " if (exists($d)) then $d" + NEW_LINE
          + " else <empty/>";
    } else if (elname != null) {
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
      String userRes = DbManager.getInstance().getDb().queryUser(query.replace(USER_FLAG_KEY, USER_FLAG));
      String epsgRes = DbManager.getInstance().getDb().queryEpsg(query.replace(USER_FLAG_KEY, EPSG_FLAG));
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
      return DbManager.getInstance().getDb().queryUser(query.replace(USER_FLAG_KEY, USER_FLAG));
    } else if (epsg) {
      return DbManager.getInstance().getDb().queryEpsg(query.replace(USER_FLAG_KEY, EPSG_FLAG));
    } else {
      return DbManager.getInstance().getDb().query(query.replace(USER_FLAG_KEY, USER_FLAG));
    }
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

    String match = id + todel + "((?=(\\/))\\S*|$)";
    String query
        = "declare namespace gml = \"" + NAMESPACE_GML + "\";" + NEW_LINE
        + "for $x in collection('" + COLLECTION_NAME + "')//gml:identifier[text() = '" + id + todel + "']/.. "
        + "union doc('" + COLLECTION_NAME + "')//gml:identifier[matches(.,'" + match + "')]/.." + NEW_LINE
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
   * @param result
   * @return Set
   */
  public static Set<Pair<String, Boolean>> sortElements(String url, String result) {
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

    for (String tmpid : ids) {
      String[] pair = tmpid.split(" ");
      String id = pair[0];
      Boolean user = false;
      try {
        user = USER_FLAG.equals(pair[1].replaceAll("[\n\r]", ""));
      } catch (Exception ex) {
      }

      if (!id.isEmpty()) {
        id = StringUtil.wrapUri(id);
        String tmp = id;
        int ind = tmp.indexOf(url);
        if (ind == -1) {
          continue;
        } else {
          tmp = tmp.substring(ind + url.length());
          if (tmp.startsWith(REST_SEPARATOR)) {
            tmp = tmp.substring(1);
          }
          if (tmp.indexOf(REST_SEPARATOR) >= 0) {
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
