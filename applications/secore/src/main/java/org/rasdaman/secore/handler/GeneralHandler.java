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
package org.rasdaman.secore.handler;

import org.rasdaman.secore.req.ResolveResponse;
import org.rasdaman.secore.req.ResolveRequest;
import java.util.ArrayList;
import org.rasdaman.secore.util.SecoreException;
import org.rasdaman.secore.util.ExceptionCode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.secore.db.DbManager;
import org.rasdaman.secore.req.RequestParam;
import org.rasdaman.secore.Constants;
import static org.rasdaman.secore.Constants.*;
import org.rasdaman.secore.db.DbSecoreVersion;
import org.rasdaman.secore.util.Pair;
import org.rasdaman.secore.util.SecoreUtil;
import org.rasdaman.secore.util.StringUtil;

/**
 * This handler should be invoked as a fallback to the more specific handler, {@link CrsCompoundHandler}, {@link AxisHandler}, etc.
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
     * Default recursion depth of link (xlink:href) expansions in XML. A value of 0 means no link is expanded: increasing integer values identify the recursion level at which link expansion is applied. It is recommended to keep this parameter to a value greater or equal than 1 (to let Petascope fetch the required CRS metadata) and less or equal 2 to avoid performance degradation.
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
        // e.g: http://localhost:8088/def/crs/EPSG/8.9.2/3857 (0: EPSG, 1: 8.9.2, 2: 3857)
        ret = ret && request.getParams().size() == 3;
        return ret;
    }

    @Override
    // NOTE: Processing multiple CRS requests in parallel are extremely slow. Hence, it should only process 1 request
    // at once which is much faster.
    public synchronized ResolveResponse handle(ResolveRequest request) throws SecoreException {
        log.debug("Handling resolve request...");
        return resolveRequest(request);
    }

    public ResolveResponse resolveRequest(ResolveRequest request) throws SecoreException {
        // Get the parsed URL
        String url = parseRequest(request).snd;
        url = StringUtil.stripServiceURI(url);

        // Get the version number of the request
        // NOTE: the version can be (/def/crs/EPSG/8.5/4326)
        // or:   /def/crs?authority=EPSG&version=0&code=4326
        String versionNumberParam = "";
        if (request.isRest()) {
            // it is rest crs/EPSG/8.5/4326
            versionNumberParam = request.getParams().get(1).val.toString();
        } else {
            // it is KVP (authority&version&code)
            versionNumberParam = request.getParamValueByKey(Constants.VERSION);
        }

        String versionNumber = "";

        if (versionNumberParam.contains(Constants.VERSION)) {
            versionNumber = versionNumberParam.split("=")[1];
        } else {
            versionNumber = versionNumberParam;
        }

        // NOTE: check if requested ID is in userdb first (e.g: def/crs/AUTO/1.3/42001?lon=10)
        String resultDefInUserDB = SecoreUtil.existsDefInUserDB(url, versionNumber);
        this.validateCRSDefRequiredParameters(resultDefInUserDB, request.getParams());
        
        ResolveResponse ret = new ResolveResponse(resultDefInUserDB);
        
        String versionTmp = versionNumber;

        // If versionNumber does not exist in userdb, then it should be from GML dictionaries.
        if (resultDefInUserDB.equals(Constants.EMPTY_XML)) {
            
            versionTmp = DbSecoreVersion.getLatestEPSGVersionIfVersionZero(url, versionNumber);
            
            if (!DbManager.collectionExistByVersionNumber(versionTmp)) {
                throw new SecoreException(ExceptionCode.InvalidRequest, "Failed resolving request '" + request.toString() + "', check if version number is valid or crs definition exists first.");
            }
            
            ret = resolveId(parseRequest(request).snd, versionTmp, versionNumber, request.getExpandDepth(), new ArrayList<Parameter>());            
        }
        
        // check if the result is a parameterized CRS, and forward to the ParameterizedCrsHandler
        if (ParameterizedCrsHandler.isParameterizedCrsDefinition(ret.getData())) {
            ret = resolveId(parseRequest(request).snd, versionTmp, versionNumber, ZERO, new ArrayList<Parameter>());
            ParameterizedCrsHandler phandler = new ParameterizedCrsHandler();
            phandler.setDefinition(ret);
            ret = phandler.handle(request);
        }
        
        String result = DbSecoreVersion.updateEPSGResultIfVersionZero(url, versionNumber, ret.getData());
        ret.setData(result);

        return ret;
    }

    /**
     * Checks the request and returns a pair of URN/URL IDENTIFIER_LABELs to be looked up in the database.
     *
     * @param request resolving request
     * @return pair of URN/URL IDENTIFIER_LABELs to be looked up in the database.
     * @throws SecoreException in case of an invalid request
     */
    protected Pair<String, String> parseRequest(ResolveRequest request) throws SecoreException {
        List<RequestParam> params = request.getParams();

        String authorityParam = EMPTY;
        String versionParam = EMPTY;
        String codeParam = EMPTY;

        // Store the parsed version param (e.g: /def/crs?authority=EPSG&version=8.5&code=4326), version is 8.5
        String versionNumber = "";

        if (request.getOperation() != null && params.size() == 3) {
            for (int i = 0; i < params.size(); i++) {
                String key = params.get(i).key;
                String val = params.get(i).val.toString();
                if (key == null) {
                    if (authorityParam.equals(EMPTY)) {
                        authorityParam = val;
                    } else if (versionParam.equals(EMPTY)) {
                        versionParam = val;
                    } else if (codeParam.equals(EMPTY)) {
                        codeParam = val;
                    }
                } else if (key.equalsIgnoreCase(AUTHORITY_KEY)) {
                    authorityParam = val;
                } else if (key.equalsIgnoreCase(VERSION_KEY)) {
                    versionParam = val;
                } else if (key.equalsIgnoreCase(CODE_KEY)) {
                    codeParam = val;
                }
            }

            // check for empty parameters
            if (authorityParam.equals(EMPTY)) {
                log.error("No authority specified.");
                throw new SecoreException(ExceptionCode.MissingParameterValue
                        .locator(AUTHORITY_KEY), "Insufficient parameters provided");
            }
            if (versionParam.equals(EMPTY)) {
                log.error("No version specified.");
                throw new SecoreException(ExceptionCode.MissingParameterValue
                        .locator(VERSION_KEY), "Insufficient parameters provided");
            }
            if (codeParam.equals(EMPTY)) {
                log.error("No code specified.");
                throw new SecoreException(ExceptionCode.MissingParameterValue
                        .locator(CODE_KEY), "Insufficient parameters provided");
            }

            // e.g: 8.5, 8.7, 0
            versionNumber = versionParam;
            // version param is set to VERSION_NAME to change later in query parser.
            versionParam = VERSION_NUMBER;

            String url = (request.isLocal() ? "" : request.getServiceUri())
                    + Constants.WEB_APPLICATION_NAME + REST_SEPARATOR
                    + request.getOperation() + REST_SEPARATOR
                    + authorityParam + REST_SEPARATOR + versionParam + REST_SEPARATOR + codeParam;

            // e.g: (8.5, "/def/OGC/0/VERSION_NUMBER/")
            Pair<String, String> obj = new Pair<String, String>(versionNumber, url);
            return obj;

        } else {
            log.error("Can't handle the given parameters, exiting with error.");
            throw new SecoreException(ExceptionCode.MissingParameterValue, "Insufficient parameters provided");
        }
    }

    @Override
    public String getOperation() {
        return EMPTY;
    }
}
