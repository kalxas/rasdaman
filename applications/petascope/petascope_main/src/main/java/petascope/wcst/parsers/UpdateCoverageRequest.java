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

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import petascope.wcst.exceptions.WCSTMissingCoverageIdException;
import petascope.wcst.exceptions.WCSTMissingInputCoverageException;
import petascope.wcs2.parsers.subsets.AbstractSubsetDimension;

import java.net.URL;
import java.util.List;

/**
 * Update Coverage request, having 3 main components:
 * - coverageId: the id of the coverage to be updated.
 * - inputCoverage: a coverage providing the cell values for replacement.
 * - inputCoverageRef: url to the coverage providing cell values for replacement.
 * - maskGrid: binary coverage indicating which cell values to update.
 * - maskGridRef: url to binary coverage indicating which cell values to update.
 * - rangeComponent: pair of name of range component to be updated and corresponding band name form input coverage.
 * - maskPolygon: geometric enclosure indicating area of updated coverage cell values to update from input coverage.
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class UpdateCoverageRequest extends AbstractWCSTRequest {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UpdateCoverageRequest.class);

    private String coverageId;
    private String inputCoverage;
    private URL inputCoverageRef;
    private String maskGrid;
    private URL maskGridRef;
    private List<Pair<String, String>> rangeComponent;
    private String maskPolygon;
    private List<AbstractSubsetDimension> subsets;
    private String pixelDataType;

    /**
     * Class constructor.
     * @param coverageId the id of the coverage targeted by the update operation.
     * @param inputCoverage the coverage providing the cell values for replacement.
     * @param inputCoverageRef an URL pointing to the coverage providing the cell values for replacement.
     * @param maskGrid a coverage with binary data indicating which pixels are to be updated.
     * @param maskGridRef an URL pointing to a coverage with binary data indicating which pixels are to be updated.
     * @param subsets a list of subsets determining the bbox targeted in the update operation.
     * @param rangeComponent pair of name of range component to be updated and corresponding band name form input coverage.
     * @param maskPolygon geometric enclosure indicating area of updated coverage cell values to update from input coverage.
     */
    public UpdateCoverageRequest(String coverageId, String inputCoverage, URL inputCoverageRef, String maskGrid,
                                 URL maskGridRef, List<AbstractSubsetDimension> subsets, List<Pair<String, String>> rangeComponent,
                                 String maskPolygon, String pixelDataType) throws WCSTMissingCoverageIdException, WCSTMissingInputCoverageException {
        this.coverageId = coverageId;
        this.inputCoverage = inputCoverage;
        this.inputCoverageRef = inputCoverageRef;
        this.maskGrid = maskGrid;
        this.maskGridRef = maskGridRef;
        this.rangeComponent = rangeComponent;
        this.maskPolygon = maskPolygon;
        this.subsets = subsets;
        this.pixelDataType = pixelDataType;
        validateRequestParameters();
    }

    /**
     * Validates request parameters.
     * @throws WCSTMissingCoverageIdException
     * @throws WCSTMissingInputCoverageException
     */
    private void validateRequestParameters() throws WCSTMissingCoverageIdException, WCSTMissingInputCoverageException {
        //coverageId is required
        if (this.coverageId == null || this.coverageId.isEmpty()) {
            throw new WCSTMissingCoverageIdException();
        }
        //one of inputCoverage or inputCOverageRef is required
        if (this.inputCoverage == null && this.inputCoverageRef == null) {
            throw new WCSTMissingInputCoverageException();
        }
    }


    /**
     * Getters and setters.
     */

    public String getCoverageId() {
        return coverageId;
    }

    public void setCoverageId(String coverageId) {
        this.coverageId = coverageId;
    }

    public String getInputCoverage() {
        return inputCoverage;
    }

    public void setInputCoverage(String inputCoverage) {
        this.inputCoverage = inputCoverage;
    }

    public URL getInputCoverageRef() {
        return inputCoverageRef;
    }

    public void setInputCoverageRef(URL inputCoverageRef) {
        this.inputCoverageRef = inputCoverageRef;
    }

    public String getMaskGrid() {
        return maskGrid;
    }

    public void setMaskGrid(String maskGrid) {
        this.maskGrid = maskGrid;
    }

    public URL getMaskGridRef() {
        return maskGridRef;
    }

    public void setMaskGridRef(URL maskGridRef) {
        this.maskGridRef = maskGridRef;
    }

    public List<Pair<String, String>> getRangeComponent() {
        return rangeComponent;
    }

    public void setRangeComponent(List<Pair<String, String>> rangeComponent) {
        this.rangeComponent = rangeComponent;
    }

    public String getMaskPolygon() {
        return maskPolygon;
    }

    public void setMaskPolygon(String maskPolygon) {
        this.maskPolygon = maskPolygon;
    }

    public List<AbstractSubsetDimension> getSubsets() {
        return subsets;
    }

    public void setSubsets(List<AbstractSubsetDimension> subsets) {
        this.subsets = subsets;
    }

    public String getPixelDataType() {
        return pixelDataType;
    }
}
