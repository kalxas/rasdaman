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
package secore.handler;

import secore.req.ResolveResponse;
import secore.req.ResolveRequest;
import secore.util.StringUtil;
import secore.util.SecoreException;
import secore.util.ExceptionCode;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import secore.db.DbManager;

/**
 * Execute XQuery received from a get/post request.
 * 
 * e.g: localhost:8080/def/query?query=declare namespace gml="http://www.opengis.net/gml/3.2"; 
  let $x :=collection('gml')//gml:identifier[contains(.,'/crs/EPSG/0/3857')]/text() 
  return if (exists($x)) then for $i in $x return <el>{$i}</el> else <empty/>
 *
 * @author Alireza
 */
public class QueryHandler extends AbstractHandler {

  private static Logger log = LoggerFactory.getLogger(QueryHandler.class);

  public ResolveResponse handle(ResolveRequest request) throws SecoreException {
    log.debug("Handling resolve request...");

    String originalReq = request.getOriginalRequest();
    originalReq = StringUtil.urldecode(originalReq);
    // extract the query from the url
    String query = "";    
    
    // NOTE: Only support to query on the userdb and the fixed GML dictionary version (e.g: 8.5))
    String versionNumber = DbManager.FIX_GML_VERSION_NUMBER;
    int startIdx = originalReq.indexOf("=");
    if (startIdx != -1) 
      query = originalReq.substring(startIdx + 1, originalReq.length());

    if (request.getOperation().equals(getOperation()) && query.length() != 0) {
      // Check if the query is a read query
      // NOTE: only support to use read query with ?query="", not insert/update/delete inside the Xquery
      if (!isUpdateXQ(query)) {
        // Check if the Xquery collection('') does exist.
        String collectionName = StringUtil.getCollectionNameFromXQuery(query);
        boolean collectionNameExists = DbManager.collectionExistByCollectionName(collectionName);
        if (collectionNameExists) {        
          String res =  DbManager.getInstance().getDb().queryBothDB(query, versionNumber);
          log.debug("Done, returning response.");
          return new ResolveResponse(res);
        } else {
          throw new SecoreException(ExceptionCode.CollectionNameNotFoundException, "XQuery uses non-existing collection name: " + collectionName);
        }
      } else {
        throw new SecoreException(ExceptionCode.MissingParameterValue,
          "Can't run xquery update queries on secored-dbs.");
      }

    } else {
      log.error("Can't handle the given parameters, exiting with error.");
      throw new SecoreException(ExceptionCode.MissingParameterValue,
          "Insufficient parameters provided");
    }
  }

  public String getOperation() {
    return OP_QUERY;
  }

  /**
   * Check if the query is an xquery update query or not
   * @param query user query
   * @return true if is a update query, otherwise false
   */
  private boolean isUpdateXQ(String query) {

    boolean isUpdate = false;
    // Lis of not-allowed xquery update operations
    List<String> updateKeywords = Arrays.asList("add", "delete", "rename",
        "store", "flush");

    for (String s: updateKeywords) {
      if (query.contains(s))
        isUpdate = true;
    }

    // The query can contain "replace node" operation but not only "replace"
    if (query.contains("replace") && !query.contains("replace node"))
      isUpdate = true;

    return isUpdate;
  }

}
