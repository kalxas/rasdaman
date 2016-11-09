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

import java.util.Arrays;
import secore.req.ResolveResponse;
import secore.req.ResolveRequest;
import java.util.Set;
import java.util.TreeSet;
import secore.util.SecoreException;
import secore.util.ExceptionCode;
import secore.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import secore.db.DbCollection;
import secore.db.DbManager;
import static secore.handler.GeneralHandler.AUTHORITY_KEY;
import static secore.handler.GeneralHandler.CODE_KEY;
import static secore.handler.GeneralHandler.VERSION_KEY;
import secore.req.RequestParam;
import secore.util.Constants;
import static secore.util.Constants.*;
import secore.util.SecoreUtil;
import secore.util.SecoreUtil.QueryDB;
import secore.util.StringUtil;
import secore.util.XMLUtil;

/**
 * This handler deals with incomplete URLs such as
 *
 *  http://opengis.net/def/crs/EPSG/0/
 *
 * The result of such an incomplete URL is a listing of all the definitions
 * at that level. More details at
 *
 *  http://rasdaman.org/ticket/474
 *
 * @author Dimitar Misev
 */
public class IncompleteUrlHandler extends AbstractHandler {

  private static Logger log = LoggerFactory.getLogger(IncompleteUrlHandler.class);

  @Override
  public boolean canHandle(ResolveRequest request) throws SecoreException {
    return request.getOperation() == null ||
       (!OP_CRS_COMPOUND.equals(request.getOperation()) &&
        !OP_EQUAL.equals(request.getOperation()) &&
        request.getParams().size() < 3);
  }

  public ResolveResponse handle(ResolveRequest request) throws SecoreException {
    log.debug("Handling resolve request...");
    ResolveResponse ret = null;

    String url = StringUtil.SERVLET_CONTEXT + REST_SEPARATOR;
    String authorityParam = EMPTY;
    String versionNumberParam = EMPTY;
    String codeParam = EMPTY;

    if (request.getOperation() != null) {
      url += request.getOperation();


      for (RequestParam param : request.getParams()) {
        String key = param.key;
        String val = param.val.toString();

        if (key == null) {
          if (authorityParam.equals(EMPTY)) {
            authorityParam = val;
          } else if (versionNumberParam.equals(EMPTY)) {
            versionNumberParam = val;
          } else if (codeParam.equals(EMPTY)) {
            codeParam = val;
          }
        } else if (key.equalsIgnoreCase(AUTHORITY_KEY)) {
          authorityParam = val;
        } else if (key.equalsIgnoreCase(VERSION_KEY)) {
          versionNumberParam = val;
        } else if (key.equalsIgnoreCase(CODE_KEY)) {
          codeParam = val;
        }
      }

      if (!EMPTY.equals(authorityParam)) {
        url += REST_SEPARATOR + authorityParam;
      }
      if (!EMPTY.equals(versionNumberParam)) {
        if (EMPTY.equals(authorityParam)) {
          log.error("Unexpected parameter version: " + versionNumberParam);
          throw new SecoreException(ExceptionCode.InvalidRequest
              .locator(VERSION_KEY), "Extra parameter version provided: " + versionNumberParam +
              ", while missing authority parameter");
        }

        // NOTE: depend on collectionName, it will change versionNumber when query.
        url += REST_SEPARATOR + VERSION_NUMBER;
      }
      if (!EMPTY.equals(codeParam)) {
        log.error("Unexpected parameter code: " + codeParam);
        throw new SecoreException(ExceptionCode.InvalidRequest
            .locator(CODE_KEY), "Extra parameter code provided: " + codeParam +
            ", while missing version or authority");
      }
    }

    // Return list of definitions from the incomplete Url
    String res = queryIncompleteUrl(request, url, versionNumberParam);

    // adapt result to XML
    ret = new ResolveResponse(res);
    return ret;
  }

  public String getOperation() {
    return EMPTY;
  }

  /**
   * Query the URI with version number and return the list of definitions
   * Then query to list all the defintions belonged to this current level in URN. (e.g: 8.5 -> gml_85)
   * NOTE: if authority is not empty and version is empty (e.g: EPSG in /def/crs/EPSG)
   * then it needs to list all the versions of available GML dictionaries files.
   * e.g: <identifier>http://localhost:8088/def/area/EPSG/8.5</identifier>
   *      <identifier>http://localhost:8088/def/area/EPSG/8.6</identifier>
   *      ...
   * @return
   */
  private String queryIncompleteUrl(ResolveRequest request, String url, String versionNumberParam) throws SecoreException {
    String res = "";
    // e.g: /def/crs -> /def/crs/
    String urlTmp = StringUtil.wrapUri(url);

    // if URL has both authority param and version number param (e.g: /def/crs/AUTO/1.3)
    // then try with userdb first, if it does not exist, then try with GML dictionary
    if (!versionNumberParam.equals("")) {
      Boolean existsDefInUserDB = SecoreUtil.existsDefInUserDB(url, versionNumberParam);
      if (existsDefInUserDB) {
        // Only query in userdb (e.g: /def/crs/AUTO/1.3)
        res = SecoreUtil.queryDefVersion(QueryDB.USER_DB, urlTmp, versionNumberParam);
      } else {
        // Only query in gmldb (e.g: /def/crs/EPSG/0)
        res = SecoreUtil.queryDefVersion(QueryDB.EPSG_DB, urlTmp, versionNumberParam);
      }
    } else {
      // NOTE: if requested URL is the parent level of versionNumber such as /def/crs/EPSG/) then it must query all the collections (not only default gml_0)
      // as it will return multiples results: e.g: /def/crs/EPSG/0, /def/crs/EPSG/8.5, /def/crs/EPSG/8.92)     
      if (StringUtil.parentVersionNumberUri(urlTmp)) {
          for (DbCollection collection:DbManager.collections.keySet()) {
             res = SecoreUtil.queryDefVersionless(urlTmp, collection.getVersionNumber());
          }
      } else {
          // If don't have versionNumber in request then use the default: gml_0 versionNumber (e.g: /def/)          
          res = SecoreUtil.queryDefVersionless(urlTmp, DbManager.FIX_GML_VERSION_ALIAS);
      }
    }
    
    String response = "";
    
    if (!StringUtil.emptyQueryResult(res)) {      
      // parse the res as it is stored as string (e.g: /def/ will return: area crs crs datum)
      // and store these sublevel into a sorted set (remove duplicate sublevel)
      Set<String> children = new TreeSet<String>(Arrays.asList(res.split(" ")));
      // e.g: http://localhost:8080/def, then we only need the domain:port and urlTmp is the requested URI (e.g: /def/crs/)
      String requestUri = request.getServiceUri().replaceAll("/" + WEB_APPLICATION_NAME + "/?", "").replaceAll("^/", "")
                        + urlTmp.replace(VERSION_NUMBER, versionNumberParam);
      
      StringBuilder childrenURIs = new StringBuilder("");
      // create the sublevel URIs
      for (String child:children) {
          childrenURIs.append(" <" + IDENTIFIER_LABEL + ">" + requestUri + child + "</" + IDENTIFIER_LABEL + ">\n");
      }
      
      response = "<" + IDENTIFIERS_LABEL + " at='" + XMLUtil.escapeXmlPredefinedEntities(request.getOriginalRequest())
                     + "' xmlns='" + Constants.CRSNTS_NAMESPACE + "'>\n"
                     + childrenURIs
                  + "</" + IDENTIFIERS_LABEL + ">";
    }
    return response;
  }
}
