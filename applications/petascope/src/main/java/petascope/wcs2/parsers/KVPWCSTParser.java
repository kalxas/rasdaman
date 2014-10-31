/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

package petascope.wcs2.parsers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.RequestUtil;
import petascope.util.StringUtil;
import petascope.wcs2.handlers.RequestHandler;

/**
 * Parser for the requests handled by the Transaction Extension of OGC
 * Web Coverage Service (WCS).
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class KVPWCSTParser extends KVPParser<WCSTRequest>{

    private static final Logger log = LoggerFactory.getLogger(KVPParser.class);

    /**
     * Constructor which distinguishes between the possible request types.
     * @param request
     * @return
     * @throws WCSException
     */
    public WCSTRequest parse(HTTPRequest request) throws WCSException {
        //split query string into parameters
        Map<String, String> params = RequestUtil.parseKVPRequestParams(request.getQueryString());
        //distiguish between the 3 possible requet types: InsertCoverage, DeleteCoverage and UpdateCoverage
        if(params.get(REQUEST).equals(INSERT_COVERAGE)){
            //validate the request against WCS-T spec requirements
            validateInsertCoverageRequest(params);
            String useId = "";
            if(params.get(USE_ID) != null){
                useId = params.get(USE_ID);
            }
            return new InsertCoverageRequest(
                    params.get(COVERAGE),
                    parseCoverageRefUrl(params.get(COVERAGE_REF)),
                    useId.equals(USE_NEW_ID));
        }
        else if(params.get(REQUEST).equals(DELETE_COVERAGE)){
            return new DeleteCoverageRequest(params.get(COVERAGE_ID));
        }
        else{
            //update coverage request received
            //not supported yet
        }


        return new InsertCoverageRequest(null, null, null);
    }

    /**
     * Parses the URL from the coverageRef parameter
     */
    private URL parseCoverageRefUrl(String coverageRef) throws WCSException{
        URL ret = null;
        if(coverageRef != null){
            try {
                ret = new URL(coverageRef);
            } catch (MalformedURLException ex) {
                java.util.logging.Logger.getLogger(KVPWCSTParser.class.getName()).log(Level.SEVERE, null, ex);
                throw new WCSException(ExceptionCode.WCSTMalformedURL);
            }
        }
        return ret;
    }

    /**
     * Validates the request of type InsertCoverage against WCS-T spec
     */
    private void validateInsertCoverageRequest(Map<String, String> params) throws WCSException{
        //Req 3: at least one of coverageRef or coverage params exists
        if(!params.containsKey(COVERAGE) && !params.containsKey(COVERAGE_REF)){
            throw new WCSException(ExceptionCode.WCSTMissingCoverageParameter);
        }
        //Req 6: if useId is specified, it should have one of the predefined values
        if(params.containsKey(USE_ID) && !params.get(USE_ID).equals(USE_EXISTING_ID)
                && !params.get(USE_ID).equals(USE_NEW_ID)){
            throw new WCSException((ExceptionCode.WCSTUnknownUseId));
        }
    }

    /**
     * This parser handles operations of 3 types: INSERT_COVERAGE, DELETE_COVERAGE
     * and UPDATE_COVERAGE. The WCST operation itself is abstract and only acts as
     * a wrapper to these. Thus the operation name is not used when deciding which
     * parser to use.
     * @return
     */
    public String getOperationName() {
        return WCTS_OPERATION_NAME;
    }

    /**
     * This parser can parse operations of 3 types: INSERT_COVERAGE, DELETE_COVERAGE
     * and UPDATE_COVERAGE. The WCST operation itself is abstract and only acts as
     * a wrapper to these. For this reason the canParse method is overridden and
     * a direct check against the operation name is not done when choosing the parser.
     * @param request
     * @return
     */
    @Override
    public boolean canParse(HTTPRequest request) {
        boolean canParse = request.getRequestString() != null
                && !request.getRequestString().startsWith("<")
                && (request.getRequestString().contains(RequestHandler.INSERT_COVERAGE)
                   || request.getRequestString().contains(RequestHandler.DELETE_COVERAGE)
                   || request.getRequestString().contains(RequestHandler.UPDATE_COVERAGE)
                );
        log.trace("KVPParser<{}> {} parse the request", getOperationName(), canParse ? "can" : "cannot");
        return canParse;
    }

    private final static String WCTS_OPERATION_NAME = "WCSTOperation";
    private final static String USE_ID = "useId";
    private final static String COVERAGE_REF = "coverageRef";
    private final static String COVERAGE = "coverage";
    private final static String COVERAGE_ID = "coverageId";
    private final static String INSERT_COVERAGE = "InsertCoverage";
    private final static String DELETE_COVERAGE = "DeleteCoverage";
    private final static String UPDATE_COVERAGE = "UpdateCoverage";
    private final static String USE_EXISTING_ID = "existing";
    private final static String USE_NEW_ID = "new";
    private final static String REQUEST = "request";
}
