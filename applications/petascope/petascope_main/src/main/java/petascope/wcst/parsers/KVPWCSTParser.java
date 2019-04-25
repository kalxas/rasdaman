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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcst.parsers;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import petascope.exceptions.WCSException;
import petascope.wcst.exceptions.WCSTInvalidRequestException;
import petascope.wcst.exceptions.WCSTMalformedURL;
import petascope.wcst.exceptions.WCSTMissingCoverageParameter;
import petascope.wcst.exceptions.WCSTUnknownUseId;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.KEY_COVERAGE;
import static petascope.core.KVPSymbols.KEY_COVERAGEID;
import static petascope.core.KVPSymbols.KEY_COVERAGE_REF;
import static petascope.core.KVPSymbols.KEY_INPUT_COVERAGE;
import static petascope.core.KVPSymbols.KEY_INPUT_COVERAGE_REF;
import static petascope.core.KVPSymbols.KEY_LEVEL;
import static petascope.core.KVPSymbols.KEY_MASK_GRID;
import static petascope.core.KVPSymbols.KEY_MASK_GRID_REF;
import static petascope.core.KVPSymbols.KEY_PIXEL_DATA_TYPE;
import static petascope.core.KVPSymbols.KEY_REQUEST;
import static petascope.core.KVPSymbols.KEY_TILING;
import static petascope.core.KVPSymbols.KEY_USE_ID;
import petascope.exceptions.PetascopeException;
import petascope.wcs2.parsers.subsets.AbstractSubsetDimension;
import petascope.wcs2.parsers.subsets.SubsetDimensionParserService;
import petascope.wcst.exceptions.WCSTMissingMandatoryParameter;
import petascope.wcst.exceptions.WCSTScaleLevelNotValid;

/**
 * Parser for the requests handled by the Transaction Extension of OGC Web
 * Coverage Service (WCS).
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class KVPWCSTParser {

    private static final Logger log = LoggerFactory.getLogger(KVPWCSTParser.class);

    /**
     * Constructor which distinguishes between the possible request types.
     *
     * @param kvpParameters
     * @return
     * @throws WCSException
     */
    public AbstractWCSTRequest parse(Map<String, String[]> kvpParameters) throws WCSException, PetascopeException {
        //distinguish between the 3 possible request types: InsertCoverage, DeleteCoverage and UpdateCoverage
        String requestType = kvpParameters.get(KEY_REQUEST)[0];

        if (requestType.equals(KVPSymbols.VALUE_INSERT_COVERAGE)) {
            //validate the request against WCS-T spec requirements
            validateInsertCoverageRequest(kvpParameters);
            String useId = "";
            if (kvpParameters.get(KEY_USE_ID) != null) {
                useId = kvpParameters.get(KEY_USE_ID)[0];
            }
            String gmlCoverage = kvpParameters.get(KEY_COVERAGE) == null ? null : kvpParameters.get(KEY_COVERAGE)[0];
            String coverageRef = kvpParameters.get(KEY_COVERAGE_REF) == null ? null : kvpParameters.get(KEY_COVERAGE_REF)[0];
            String pixelDataType = kvpParameters.get(KEY_PIXEL_DATA_TYPE) == null ? null : kvpParameters.get(KEY_PIXEL_DATA_TYPE)[0];
            String tiling = kvpParameters.get(KEY_TILING) == null ? null : kvpParameters.get(KEY_TILING)[0];
            InsertCoverageRequest insertCoverageRequest = new InsertCoverageRequest(
                    gmlCoverage,
                    parseCoverageRefUrl(coverageRef),
                    useId.equals(USE_NEW_ID),
                    pixelDataType,
                    tiling);

            return insertCoverageRequest;
        } else if (requestType.equals(KVPSymbols.VALUE_DELETE_COVERAGE)) {
            String coverageId = kvpParameters.get(KEY_COVERAGEID) == null ? null : kvpParameters.get(KEY_COVERAGEID)[0];

            return new DeleteCoverageRequest(coverageId);
        } else if (requestType.equals(KVPSymbols.VALUE_UPDATE_COVERAGE)) {
            //update coverage request received
            String coverageId = kvpParameters.get(KEY_COVERAGEID) == null ? null : kvpParameters.get(KEY_COVERAGEID)[0];
            String inputCoverage = kvpParameters.get(KEY_INPUT_COVERAGE) == null ? null : kvpParameters.get(KEY_INPUT_COVERAGE)[0];
            String inputCoverageRef = kvpParameters.get(KEY_INPUT_COVERAGE_REF) == null ? null : kvpParameters.get(KEY_INPUT_COVERAGE_REF)[0];
            URL inputCoverageRefURL = parseCoverageRefUrl(inputCoverageRef);

            String maskGrid = kvpParameters.get(KEY_MASK_GRID) == null ? null : kvpParameters.get(KEY_MASK_GRID)[0];
            String maskGridRef = kvpParameters.get(KEY_MASK_GRID_REF) == null ? null : kvpParameters.get(KEY_MASK_GRID_REF)[0];
            URL maskGridRefURL = parseCoverageRefUrl(maskGridRef);

            List<AbstractSubsetDimension> subsets = SubsetDimensionParserService.parseSubsets(kvpParameters.get(KVPSymbols.KEY_SUBSET));
            List<Pair<String, String>> rangeComponents = new ArrayList<>();
            String tiling = kvpParameters.get(KEY_TILING) == null ? null : kvpParameters.get(KEY_TILING)[0];

            UpdateCoverageRequest updateCoverageRequest = new UpdateCoverageRequest(coverageId, inputCoverage, inputCoverageRefURL,
                    maskGrid, maskGridRefURL, subsets, rangeComponents, null, tiling);

            return updateCoverageRequest;
        } else if (requestType.equals(KVPSymbols.VALUE_INSERT_SCALE_LEVEL)) {
            // InsertScaleLevel request received (e.g: coverageId=test_mr&level=2)
            String coverageId = kvpParameters.get(KEY_COVERAGEID) == null ? null : kvpParameters.get(KEY_COVERAGEID)[0];
            BigDecimal level = this.getScaleLevel(kvpParameters);
            
            if (coverageId == null) {
                throw new WCSTMissingMandatoryParameter(KEY_COVERAGEID);
            }
            
            InsertScaleLevelRequest insertScaleLevelRequest = new InsertScaleLevelRequest(coverageId, level);
            
            return insertScaleLevelRequest;
        } else if (requestType.equals(KVPSymbols.VALUE_DELETE_SCALE_LEVEL)) {
            // DeleteScaleLevel request received (e.g: coverageId=test_mr&level=2)
            String coverageId = kvpParameters.get(KEY_COVERAGEID) == null ? null : kvpParameters.get(KEY_COVERAGEID)[0];
            BigDecimal level = this.getScaleLevel(kvpParameters);
            
            if (coverageId == null) {
                throw new WCSTMissingMandatoryParameter(KEY_COVERAGEID);
            }
            
            DeleteScaleLevelRequest deleteScaleLevelRequest = new DeleteScaleLevelRequest(coverageId, level);
            
            return deleteScaleLevelRequest;
        }
        //not a request that this parser can parse, but canParse returned true
        //should never happen
        log.error("Invalid request type: " + kvpParameters.get(KEY_REQUEST)[0] + ". This parser can not parse requests of this type.");
        throw new WCSTInvalidRequestException(kvpParameters.get(KEY_REQUEST)[0]);
    }
    
    /**
     * Check if level parameter for InsertScaleLevel request should be integer and greater than 1.
     */
    private BigDecimal getScaleLevel(Map<String, String[]> kvpParameters) throws WCSTScaleLevelNotValid, WCSTMissingMandatoryParameter {
        BigDecimal level = BigDecimal.ONE;
        try {
            level = kvpParameters.get(KEY_LEVEL) == null ? null : new BigDecimal(kvpParameters.get(KEY_LEVEL)[0]);
            if (level == null) {
                throw new WCSTMissingMandatoryParameter(KEY_LEVEL);
            } else if (level.compareTo(BigDecimal.ONE) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            throw new WCSTScaleLevelNotValid(kvpParameters.get(KEY_LEVEL)[0]);
        }
        
        return level;
    }

    /**
     * Parses the URL from the coverageRef parameter
     */
    private URL parseCoverageRefUrl(String coverageRef) throws WCSTMalformedURL {
        URL ret = null;
        if (coverageRef != null) {
            try {
                ret = new URL(coverageRef);
            } catch (MalformedURLException ex) {
                throw new WCSTMalformedURL();
            }
        }
        return ret;
    }

    /**
     * Validates the request of type InsertCoverage against WCS-T spec
     */
    private void validateInsertCoverageRequest(Map<String, String[]> kvpParameters) throws WCSTMissingCoverageParameter, WCSTUnknownUseId {
        //Req 3: at least one of coverageRef or coverage params exists
        if (!kvpParameters.containsKey(KEY_COVERAGE) && !kvpParameters.containsKey(KEY_COVERAGE_REF)) {
            throw new WCSTMissingCoverageParameter();
        }
        //Req 6: if useId is specified, it should have one of the predefined values
        if (kvpParameters.containsKey(KEY_USE_ID) && !kvpParameters.get(KEY_USE_ID)[0].equals(USE_EXISTING_ID)
                && !kvpParameters.get(KEY_USE_ID)[0].equals(USE_NEW_ID)) {
            throw new WCSTUnknownUseId();
        }
    }

    /**
     * This parser handles operations of 3 types: INSERT_COVERAGE,
     * DELETE_COVERAGE and UPDATE_COVERAGE. The WCST operation itself is
     * abstract and only acts as a wrapper to these. Thus the operation name is
     * not used when deciding which parser to use.
     *
     * @return
     */
    public String getOperationName() {
        return WCTS_OPERATION_NAME;
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

}
