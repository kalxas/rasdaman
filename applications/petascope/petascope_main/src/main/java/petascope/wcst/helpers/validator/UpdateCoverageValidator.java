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
package petascope.wcst.helpers.validator;

import java.math.BigDecimal;

import org.rasdaman.domain.cis.*;
import petascope.wcst.exceptions.WCSTAxisLabelMismatchException;
import petascope.wcst.exceptions.WCSTRangeFieldNumberMismatchException;
import petascope.wcst.exceptions.WCSTRangeFieldNameMismatchException;
import petascope.wcst.exceptions.WCSTDomainSetMismatchException;
import petascope.wcst.exceptions.WCSTResolutionNotFoundException;
import petascope.wcst.exceptions.WCSTAxisNumberMismatchException;
import petascope.wcst.exceptions.WCSTAxisCrsMismatchException;
import petascope.wcst.exceptions.WCSTSubsetDimensionMismatchException;
import petascope.wcst.exceptions.WCSTCrsGridAxesMismatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import petascope.wcst.handlers.UpdateCoverageHandler;
import petascope.wcs2.parsers.subsets.AbstractSubsetDimension;

import java.util.List;
import org.rasdaman.domain.cis.Coverage;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.core.service.CrsComputerService;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;

/**
 * Validator for the UpdateCoverage request, following the requirements of WCS-T
 * specs.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class UpdateCoverageValidator {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UpdateCoverageHandler.class);

    // persisted coverage
    Coverage currentCoverage;
    // input Coverage from GML to update the current coverage
    Coverage inputCoverage;

    List<AbstractSubsetDimension> subsets;
    List<Pair<String, String>> rangeComponents;

    /**
     * Class constructor.
     *
     * @param currentCoverage the persisted coverage to be updated.
     * @param inputCoverage the input coverage providing the values from GML to
     * be used for updating.
     * @param subsets the list of subsets provided as parameter.
     * @param rangeComponents the list of range components provided as
     * parameter.
     */
    public UpdateCoverageValidator(Coverage currentCoverage, Coverage inputCoverage,
            List<AbstractSubsetDimension> subsets, List<Pair<String, String>> rangeComponents) {
        this.currentCoverage = currentCoverage;
        this.inputCoverage = inputCoverage;
        this.subsets = subsets;
        this.rangeComponents = rangeComponents;
    }

    /**
     * Validates the UpdateCoverage request according to the requirements
     * defined by the WCS-T specs.
     *
     * @throws WCSTSubsetDimensionMismatchException
     * @throws WCSTAxisCrsMismatchException
     * @throws WCSTAxisLabelMismatchException
     * @throws WCSTDomainSetMismatchException
     * @throws WCSTAxisNumberMismatchException
     * @throws WCSTRangeFieldNumberMismatchException
     * @throws WCSTRangeFieldNameMismatchException
     * @throws petascope.wcst.exceptions.WCSTResolutionNotFoundException
     * @throws petascope.wcst.exceptions.WCSTCrsGridAxesMismatch
     * @throws petascope.exceptions.SecoreException
     */
    public void validate() throws WCSTSubsetDimensionMismatchException, WCSTAxisCrsMismatchException,
            WCSTAxisLabelMismatchException, WCSTDomainSetMismatchException, WCSTAxisNumberMismatchException,
            WCSTRangeFieldNumberMismatchException, WCSTRangeFieldNameMismatchException, WCSTResolutionNotFoundException,
            WCSTCrsGridAxesMismatch, PetascopeException, SecoreException {
        validateSubsets(currentCoverage, subsets);
        validateCrses(currentCoverage, inputCoverage);
        validateOffsetVectors(currentCoverage, inputCoverage);
        validateRangeType(currentCoverage, inputCoverage, rangeComponents);
    }

    /**
     * Checks if all dimensions indicated in the subset parameters appear in the
     * list of dimensions of the coverage targeted by the update operation.
     *
     * @param currentCoverage the coverage targeted by the update op.
     * @param subsets the list of subsets passed as parameter to the update op.
     * @throws petascope.wcst.exceptions.WCSTSubsetDimensionMismatchException
     */
    private void validateSubsets(Coverage currentCoverage, List<AbstractSubsetDimension> subsets) throws WCSTSubsetDimensionMismatchException {
        //check that all subset labels exist in the current coverage
        for (AbstractSubsetDimension subset : subsets) {

            String dimensionName = subset.getDimensionName();
            AxisExtent axisExtent = currentCoverage.getEnvelope().getEnvelopeByAxis().getAxisExtentByLabel(dimensionName);
            if (axisExtent == null) {
                log.error("Subset dimension " + subset.getDimensionName() + " was not found in the list of subsets of the "
                        + "target coverage.");
                throw new WCSTSubsetDimensionMismatchException(subset.getDimensionName());
            }
        }
    }

    /**
     * Validates the range type in case of complete replacements.
     *
     * @param currentCoverage the coverage targeted by the update op.
     * @param inputCoverage the input coverage for the update op.
     * @param rangeComponents the list of range components to be updated.
     * @throws petascope.wcst.exceptions.WCSTRangeFieldNumberMismatchException
     * @throws petascope.wcst.exceptions.WCSTRangeFieldNameMismatchException
     */
    private void validateRangeType(Coverage currentCoverage, Coverage inputCoverage,
            List<Pair<String, String>> rangeComponents)
            throws WCSTRangeFieldNumberMismatchException, WCSTRangeFieldNameMismatchException {
        //in case no range components are indicated, the range type must match
        if (rangeComponents == null || rangeComponents.isEmpty()) {
            //check if the coverages have the same number of bands
            if (currentCoverage.getNumberOfBands() != inputCoverage.getNumberOfBands()) {
                log.error("The number of bands (swe:field elements) of target coverage (found " + currentCoverage.getNumberOfBands() + " ) "
                        + "and input coverage (found " + inputCoverage.getNumberOfBands() + ") don't match.");
                throw new WCSTRangeFieldNumberMismatchException(currentCoverage.getNumberOfBands(), inputCoverage.getNumberOfBands());
            }
            //check if the names of the bands are the same
            RangeType currentRangeType = currentCoverage.getRangeType();
            RangeType inputRangeType = inputCoverage.getRangeType();

            int numberOfFields = currentRangeType.getDataRecord().getFields().size();
            for (int i = 0; i < numberOfFields; i++) {
                Field currentField = currentRangeType.getDataRecord().getFields().get(i);
                Field inputField = inputRangeType.getDataRecord().getFields().get(i);
                if (!currentField.getName().equals(inputField.getName())) {
                    log.error("Band (swe:field element) name mismatch, found " + currentField.getName() + " in target coverage, "
                            + currentField.getName() + " in input coverage.");
                    throw new WCSTRangeFieldNameMismatchException(currentField.getName(), inputField.getName());
                }
            }
        }
    }

    /**
     * Check if both coverages have same offset vectors size (resolution) for
     * each axis (e.g: coverage 1 - pixel size: 15 meter, coverage 2 - pixel
     * size: 300 meter) is not valid to mosaic. NOTE: if inputCoverage is 2D
     * slice and currentCoverage is 3D then the time axis offset vector
     * (resolution) will be missed in input coverage So only check the
     * resolutions from inputCoverage are belonged to currentCoverage
     *
     * @param currentCoverage
     * @param inputCoverage
     */
    private void validateOffsetVectors(Coverage currentCoverage, Coverage inputCoverage) throws WCSTResolutionNotFoundException {
        // Only support GeneralGridCoverage now
        GeneralGridDomainSet currentDomainSet = ((GeneralGridDomainSet) currentCoverage.getDomainSet());
        GeneralGridDomainSet inputDomainSet = ((GeneralGridDomainSet) inputCoverage.getDomainSet());
        List<GeoAxis> currentAxes = currentDomainSet.getGeneralGrid().getGeoAxes();
        List<GeoAxis> inputAxes = inputDomainSet.getGeneralGrid().getGeoAxes();

        // As the number of axes in inputCoverage are not always as same as currentCoverage (e.g: 3D time series update from 2D slices)
        for (GeoAxis inputGeoAxis : inputAxes) {
            BigDecimal inputAxisResolution = inputGeoAxis.getResolution();

            // Check the resolution does exist in current coverage
            BigDecimal currentAxisResolution = null;
            for (GeoAxis currentGeoAxis : currentAxes) {
                // Input axis does exist                
                if (CrsUtil.axisLabelsMatch(currentGeoAxis.getAxisLabel(), inputGeoAxis.getAxisLabel())) {
                    currentAxisResolution = currentGeoAxis.getResolution();
                }
            }

            // Validate that the resolution of input axis is correct
            // NOTE: resolution from the files can be different and accept with a small epsilion
            // e.g: 2.1111111111111111 and 2.11111111111112 or 2.111111111111110 is the same            
            if ((currentAxisResolution.abs().subtract(inputAxisResolution.abs())).abs()
                    .compareTo(BigDecimalUtil.AXIS_RESOLUTION_EPSILION) > 0) {
                throw new WCSTResolutionNotFoundException(inputAxisResolution);
            }
        }
    }

    /**
     * Validates the crs of the coverages. The set of axes of an input coverage
     * must be a subset of the set of axes of the coverage to be updated,
     * regarding crses.
     *
     * @param currentCoverage the coverage targeted by the update op.
     * @param inputCoverage the input coverage for the update op.
     * @throws WCSTAxisLabelMismatchException
     * @throws WCSTAxisCrsMismatchException
     */
    private void validateCrses(Coverage currentCoverage, Coverage inputCoverage) throws WCSTAxisLabelMismatchException, WCSTAxisCrsMismatchException {
        //iterate through the axes of the input coverage
        List<AxisExtent> inputAxesExtent = inputCoverage.getEnvelope().getEnvelopeByAxis().getAxisExtents();
        for (AxisExtent inputAxisExtent : inputAxesExtent) {
            //check that a corresponding axis with the same label exists in the current coverage
            String inputAxisName = inputAxisExtent.getAxisLabel();
            AxisExtent currentAxisExtent = currentCoverage.getEnvelope().getEnvelopeByAxis().getAxisExtentByLabel(inputAxisName);
            if (currentAxisExtent == null) {
                //axis need to have same names
                log.error("Axis label " + inputAxisName + " from the input coverage was not found in the target coverage.");

                throw new WCSTAxisLabelMismatchException(inputAxisName);
            }

            String currentAxisSrsName = currentAxisExtent.getSrsName();
            String inputAxisSrsName = inputAxisExtent.getSrsName();

            // NOTE: use the stripped SECORE prefix URL to compare (so http://localhost:8080/def/crs/epsg/0/4326 will be the same as http://localhost:8081/def/crs/epsg/0/4326)
            if (!CrsUtil.CrsUri.toDbRepresentation(currentAxisSrsName)
                .equals(CrsUtil.CrsUri.toDbRepresentation(inputAxisSrsName))) {
                //axis need to have same native crs
                log.error("Native crs of axis " + inputAxisName
                        + " from the input coverage (found " + inputAxisSrsName + ") "
                        + "doesn't match with one in the target coverage (found "
                        + currentAxisSrsName + ").");

                throw new WCSTAxisCrsMismatchException(inputAxisName,
                        inputAxisSrsName, currentAxisSrsName);
            }
        }
    }
}
