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
import java.util.Set;
import secore.util.SecoreException;
import secore.util.ExceptionCode;
import secore.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static secore.handler.GeneralHandler.AUTHORITY_KEY;
import static secore.handler.GeneralHandler.CODE_KEY;
import static secore.handler.GeneralHandler.VERSION_KEY;
import secore.req.RequestParam;
import secore.util.Constants;
import static secore.util.Constants.*;
import secore.util.SecoreUtil;
import secore.util.StringUtil;

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
    if (request.getOperation() != null) {
      url += request.getOperation();
      String authority = EMPTY;
      String version = EMPTY;
      String code = EMPTY;
      
      for (RequestParam param : request.getParams()) {
        String key = param.key;
        String val = param.val.toString();
        
        if (key == null) {
          if (authority.equals(EMPTY)) {
            authority = val;
          } else if (version.equals(EMPTY)) {
            version = val;
          } else if (code.equals(EMPTY)) {
            code = val;
          }
        } else if (key.equalsIgnoreCase(AUTHORITY_KEY)) {
          authority = val;
        } else if (key.equalsIgnoreCase(VERSION_KEY)) {
          version = val;
        } else if (key.equalsIgnoreCase(CODE_KEY)) {
          code = val;
        }
      }
      
      if (!EMPTY.equals(authority)) {
        url += REST_SEPARATOR + authority;
      }
      if (!EMPTY.equals(version)) {
        if (EMPTY.equals(authority)) {
          log.error("Unexpected parameter version: " + version);
          throw new SecoreException(ExceptionCode.InvalidRequest
              .locator(VERSION_KEY), "Extra parameter version provided: " + version + 
              ", while missing authority parameter");
        }
        url += REST_SEPARATOR + version;
      }
      if (!EMPTY.equals(code)) {
        log.error("Unexpected parameter code: " + code);
        throw new SecoreException(ExceptionCode.InvalidRequest
            .locator(CODE_KEY), "Extra parameter code provided: " + code + 
            ", while missing version or authority");
      }
    }
//    String res = SecoreUtil.queryDef(urn, false, true, true, IDENTIFIER_LABEL);
    String res = SecoreUtil.queryDef(url, false, true, true, null);
    if (StringUtil.emptyQueryResult(res)) {
      res = "";
    } else {
      Pair<Boolean, Set<Pair<String, Boolean>>> tmp = SecoreUtil.sortElements(url, res);
      res = "";
      String serviceUri = request.getServiceUri().replaceAll("/def/?", "").replaceAll("^/", "");
      String fullUri = serviceUri + url;
      if (!fullUri.endsWith(REST_SEPARATOR)) {
        fullUri += REST_SEPARATOR;
      }
      for (Pair<String, Boolean> p : tmp.snd) {
        res += "  <" + IDENTIFIER_LABEL + ">" + fullUri + p.fst + "</" + IDENTIFIER_LABEL + ">\n";
      }
    }
    res = "<" + IDENTIFIERS_LABEL + " at='" + request.getOriginalRequest() + "' xmlns='" + Constants.CRSNTS_NAMESPACE + "'>\n"
        + res
        + "</" + IDENTIFIERS_LABEL + ">";
    
    // adapt result to XML
    ret = new ResolveResponse(res);
    return ret;
  }

  public String getOperation() {
    return EMPTY;
  }
}
