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

package petascope.wcs2.parsers.wcst;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.exceptions.wcst.WCSTInvalidRequestException;
import petascope.exceptions.wcst.WCSTMalformedURL;
import petascope.exceptions.wcst.WCSTMissingCoverageParameter;
import petascope.exceptions.wcst.WCSTUnknownUseId;
import petascope.util.RequestUtil;
import petascope.wcps2.parser.wcpsParser;
import petascope.wcs2.handlers.RequestHandler;
import petascope.wcs2.parsers.*;
import petascope.wcs2.parsers.subsets.DimensionSubset;
import petascope.wcs2.parsers.subsets.SubsetParser;

/**
 * Parser for the requests handled by the Transaction Extension of OGC
 * Web Coverage Service (WCS).
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class KVPWCSTParser extends KVPParser<WCSTRequest> {

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
        //distinguish between the 3 possible request types: InsertCoverage, DeleteCoverage and UpdateCoverage
        if(params.get(REQUEST).equals(RequestHandler.INSERT_COVERAGE)){
            //validate the request against WCS-T spec requirements
            validateInsertCoverageRequest(params);
            String useId = "";
            if(params.get(USE_ID) != null){
                useId = params.get(USE_ID);
            }
            InsertCoverageRequest insertCoverageRequest = new InsertCoverageRequest(
                    params.get(COVERAGE),
                    parseCoverageRefUrl(params.get(COVERAGE_REF)),
                    useId.equals(USE_NEW_ID),
                    params.get(PIXEL_DATA_TYPE),
                    params.get(TILING));
            return insertCoverageRequest;
        }
        else if(params.get(REQUEST).equals(RequestHandler.DELETE_COVERAGE)){
            return new DeleteCoverageRequest(params.get(COVERAGE_ID));
        }
        else if(params.get(REQUEST).equals(RequestHandler.UPDATE_COVERAGE)){
            //update coverage request received
            String coverageId = params.get(COVERAGE_ID);
            String inputCoverage = params.get(INPUT_COVERAGE);
            URL inputCoverageRef = parseCoverageRefUrl(params.get(INPUT_COVERAGE_REF));
            String maskGrid = params.get(MASK_GRID);
            URL maskGridRef = parseCoverageRefUrl(params.get(MASK_GRID_REF));
            List<DimensionSubset> subsets = SubsetParser.parseSubsets(request.getRequestString());
            List<Pair<String, String>> rangeComponents = new ArrayList<Pair<String, String>>();
            String tiling = params.get(TILING);
            UpdateCoverageRequest updateCoverageRequest = new UpdateCoverageRequest(coverageId, inputCoverage, inputCoverageRef,
                     maskGrid, maskGridRef, subsets, rangeComponents, null, tiling);
            return updateCoverageRequest;
        }
        //not a request that this parser can parse, but canParse returned true
        //should never happen
        log.error("Invalid request type: " + params.get(REQUEST) + ". This parser can not parse requests of this type.");
        throw new WCSTInvalidRequestException(params.get(REQUEST));
    }

    /**
     * Parses the URL from the coverageRef parameter
     */
    private URL parseCoverageRefUrl(String coverageRef) throws WCSTMalformedURL{
        URL ret = null;
        if(coverageRef != null){
            try {
                ret = new URL(coverageRef);
            } catch (MalformedURLException ex) {
                java.util.logging.Logger.getLogger(KVPWCSTParser.class.getName()).log(Level.SEVERE, null, ex);
                throw new WCSTMalformedURL();
            }
        }
        return ret;
    }

    /**
     * Validates the request of type InsertCoverage against WCS-T spec
     */
    private void validateInsertCoverageRequest(Map<String, String> params) throws WCSTMissingCoverageParameter, WCSTUnknownUseId{
        //Req 3: at least one of coverageRef or coverage params exists
        if(!params.containsKey(COVERAGE) && !params.containsKey(COVERAGE_REF)){
            throw new WCSTMissingCoverageParameter();
        }
        //Req 6: if useId is specified, it should have one of the predefined values
        if(params.containsKey(USE_ID) && !params.get(USE_ID).equals(USE_EXISTING_ID)
                && !params.get(USE_ID).equals(USE_NEW_ID)){
            throw new WCSTUnknownUseId();
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
        Map<String, String> params = RequestUtil.parseKVPRequestParams(request.getQueryString());
        boolean canParse = request.getRequestString() != null
                && !request.getRequestString().startsWith("<")
                && !params.isEmpty()
                && (params.get(REQUEST).equals(RequestHandler.INSERT_COVERAGE)
                   || params.get(REQUEST).equals(RequestHandler.DELETE_COVERAGE)
                   || params.get(REQUEST).equals(RequestHandler.UPDATE_COVERAGE)
                );
        log.trace("KVPParser<{}> {} parse the request", getOperationName(), canParse ? "can" : "cannot");
        return canParse;
    }

    private final static String WCTS_OPERATION_NAME = "WCSTOperation";

    /**
     * Values: case sensitive!
     */
    private final static String USE_EXISTING_ID = "existing";
    private final static String USE_NEW_ID = "new";

    /**
     * Keys: case INsensitive!
     */

    private final static String USE_ID = "useid";
    private final static String COVERAGE_REF = "coverageref";
    private final static String COVERAGE = "coverage";
    private final static String COVERAGE_ID = "coverageid";
    private final static String REQUEST = "request";
    private final static String INPUT_COVERAGE = "inputcoverage";
    private final static String INPUT_COVERAGE_REF = "inputcoverageref";
    private final static String MASK_GRID = "maskgrid";
    private final static String MASK_GRID_REF = "maskgridref";
    private final static String PIXEL_DATA_TYPE = "pixeldatatype";
    private final static String TILING = "tiling";
}
