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
import petascope.controller.AbstractController;
import petascope.exceptions.WCSException;
import petascope.wcst.exceptions.WCSTInvalidRequestException;
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
import static petascope.core.KVPSymbols.USE_EXISTING_ID;
import static petascope.core.KVPSymbols.USE_NEW_ID;
import petascope.exceptions.ExceptionCode;
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
            String value = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_USE_ID);
            if (value != null) {
                useId = value;
            }
            
            String gmlCoverage = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_COVERAGE);
            String coverageRef = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_COVERAGE_REF);
            String pixelDataType = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_PIXEL_DATA_TYPE);
            String tiling = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_TILING);
            InsertCoverageRequest insertCoverageRequest = new InsertCoverageRequest(
                    gmlCoverage,
                    parseCoverageRefUrl(coverageRef),
                    useId.equals(USE_NEW_ID),
                    pixelDataType,
                    tiling);

            return insertCoverageRequest;
        } else if (requestType.equals(KVPSymbols.VALUE_DELETE_COVERAGE)) {
            String coverageId = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_COVERAGEID);

            return new DeleteCoverageRequest(coverageId);
        } else if (requestType.equals(KVPSymbols.VALUE_UPDATE_COVERAGE)) {
            //update coverage request received
            String coverageId = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_COVERAGEID);
            String inputCoverage = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_INPUT_COVERAGE);
            String inputCoverageRef = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_INPUT_COVERAGE_REF);
            URL inputCoverageRefURL = parseCoverageRefUrl(inputCoverageRef);

            String maskGrid = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_MASK_GRID);
            String maskGridRef = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_MASK_GRID_REF);
            URL maskGridRefURL = parseCoverageRefUrl(maskGridRef);

            List<AbstractSubsetDimension> subsets = SubsetDimensionParserService.parseSubsets(kvpParameters.get(KVPSymbols.KEY_SUBSET));
            List<Pair<String, String>> rangeComponents = new ArrayList<>();
            String tiling = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_TILING);
            
            String uploadedFilePath = AbstractController.getValueByKeyAllowNull(kvpParameters, KVPSymbols.KEY_INTERNAL_UPLOADED_FILE_PATH);
            
            UpdateCoverageRequest updateCoverageRequest = new UpdateCoverageRequest(coverageId, inputCoverage, inputCoverageRefURL,
                    maskGrid, maskGridRefURL, subsets, rangeComponents, null, tiling, uploadedFilePath);

            return updateCoverageRequest;
        }

        //not a request that this parser can parse, but canParse returned true
        //should never happen
        throw new WCSTInvalidRequestException(kvpParameters.get(KEY_REQUEST)[0]);
    }

    /**
     * Parses the URL from the coverageRef parameter
     */
    private URL parseCoverageRefUrl(String coverageRef) throws PetascopeException {
        URL ret = null;
        if (coverageRef != null) {
            try {
                ret = new URL(coverageRef);
            } catch (MalformedURLException ex) {
                throw new PetascopeException(ExceptionCode.InvalidRequest,
                                            "URL " + coverageRef + "' for " + KVPSymbols.KEY_COVERAGE_REF + " is malformed. Reason: " + ex.getMessage());
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

}
