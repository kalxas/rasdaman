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
import java.util.ArrayList;
import secore.util.SecoreException;
import secore.util.ExceptionCode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import secore.req.RequestParam;
import static secore.util.Constants.*;
import secore.util.StringUtil;

/**
 * This handler should be invoked as a fallback to the more specific handler,
 * {@link CrsCompoundHandler}, {@link AxisHandler}, etc.
 *
 * @author Dimitar Misev
 */
public class GeneralHandler extends AbstractHandler {

  private static Logger log = LoggerFactory.getLogger(GeneralHandler.class);
  public static final String AUTHORITY_KEY = "authority";
  public static final String VERSION_KEY = "version";
  public static final String DEFAULT_VERSION = "0";
  public static final String CODE_KEY = "code";
  // flatten possibilites
  public static final String EXPAND_KEY = "expand";
  public static final String EXPAND_FULL = "full";
  public static final String EXPAND_NONE = "none";
  // Format parameter
  public static final String FORMAT_KEY = "format";
  /**
   * Default recursion depth of link (xlink:href) expansions in XML. A value of
   * 0 means no link is expanded: increasing integer values identify the
   * recursion level at which link expansion is applied. It is recommended to
   * keep this parameter to a value greater or equal than 1 (to let Petascope
   * fetch the required CRS metadata) and less or equal 2 to avoid performance
   * degradation.
   *
   * @see http://rasdaman.org/ticket/365
   */
  public static final String EXPAND_DEFAULT = "2";
  // flag indicating whether to resolve the target CRS
  public static final String RESOLVE_TARGET_KEY = "resolve-target";
  public static final String RESOLVE_TARGET_YES = "yes";
  public static final String RESOLVE_TARGET_NO = "no";

  @Override
  public boolean canHandle(ResolveRequest request) throws SecoreException {
    boolean ret = request.getOperation() != null
        && !OP_CRS_COMPOUND.equals(request.getOperation())
        && !OP_EQUAL.equals(request.getOperation());
    if (ret && request.getParams().size() < 3) {
      throw new SecoreException(ExceptionCode.MissingParameterValue, "Insufficient parameters provided");
    }
    ret = ret && request.getParams().size() == 3;
    return ret;
  }

  public ResolveResponse handle(ResolveRequest request) throws SecoreException {
    log.debug("Handling resolve request...");
    return resolveRequest(request);
  }

  public ResolveResponse resolveRequest(ResolveRequest request) throws SecoreException {
    String url = parseRequest(request);

    ResolveResponse ret = resolveId(url, request.getExpandDepth(), new ArrayList<Parameter>());

    ret = new ResolveResponse(StringUtil.replaceElementValue(
        ret.getData(), IDENTIFIER_LABEL, request.getOriginalRequest()));

    // check if the result is a parameterized CRS, and forward to the ParameterizedCrsHandler
    if (ParameterizedCrsHandler.isParameterizedCrsDefinition(ret.getData())) {
      ret = resolveId(url, ZERO, new ArrayList<Parameter>());
      ParameterizedCrsHandler phandler = new ParameterizedCrsHandler();
      phandler.setDefinition(ret);
      ret = phandler.handle(request);
    }

    return ret;
  }

  /**
   * Checks the request and returns a pair of URN/URL IDENTIFIER_LABELs to be
   * looked up in the database.
   *
   * @param request resolving request
   * @return pair of URN/URL IDENTIFIER_LABELs to be looked up in the database.
   * @throws SecoreException in case of an invalid request
   */
  protected String parseRequest(ResolveRequest request) throws SecoreException {
    List<RequestParam> params = request.getParams();

    if (request.getOperation() != null && params.size() == 3) {
      String authority = EMPTY;
      String version = EMPTY;
      String code = EMPTY;
      for (int i = 0; i < params.size(); i++) {
        String key = params.get(i).key;
        String val = params.get(i).val.toString();
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

      // check for empty parameters
      if (authority.equals(EMPTY)) {
        log.error("No authority specified.");
        throw new SecoreException(ExceptionCode.MissingParameterValue
            .locator(AUTHORITY_KEY), "Insufficient parameters provided");
      }
      if (version.equals(EMPTY)) {
        log.error("No version specified.");
        throw new SecoreException(ExceptionCode.MissingParameterValue
            .locator(VERSION_KEY), "Insufficient parameters provided");
      }
      if (code.equals(EMPTY)) {
        log.error("No code specified.");
        throw new SecoreException(ExceptionCode.MissingParameterValue
            .locator(CODE_KEY), "Insufficient parameters provided");
      }

      String url = (request.isLocal() ? "" : request.getServiceUri())
          + request.getOperation() + REST_SEPARATOR
          + authority + REST_SEPARATOR + version + REST_SEPARATOR + code;
      return url;
    } else {
      log.error("Can't handle the given parameters, exiting with error.");
      throw new SecoreException(ExceptionCode.MissingParameterValue, "Insufficient parameters provided");
    }
  }

  public String getOperation() {
    return EMPTY;
  }
}
