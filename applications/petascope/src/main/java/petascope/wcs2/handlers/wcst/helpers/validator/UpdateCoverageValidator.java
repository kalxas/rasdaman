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
package petascope.wcs2.handlers.wcst.helpers.validator;

import java.math.BigDecimal;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import petascope.core.CoverageMetadata;
import petascope.exceptions.wcst.*;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps.server.core.RangeElement;
import petascope.wcs2.handlers.wcst.UpdateCoverageHandler;
import petascope.wcs2.parsers.subsets.DimensionSubset;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import petascope.util.ListUtil;

/**
 * Validator for the UpdateCoverage request, following the requirements of WCS-T specs.
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class UpdateCoverageValidator {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UpdateCoverageHandler.class);

    CoverageMetadata currentCoverage;
    CoverageMetadata inputCoverage;
    List<DimensionSubset> subsets;
    List<Pair<String, String>> rangeComponents;

    /**
     * Class constructor.
     * @param currentCoverage the coverage to be updated.
     * @param inputCoverage the coverage providing the values to be used for updating.
     * @param subsets the list of subsets provided as parameter.
     * @param rangeComponents the list of range components provided as parameter.
     */
    public UpdateCoverageValidator(CoverageMetadata currentCoverage, CoverageMetadata inputCoverage,
                                   List<DimensionSubset> subsets, List<Pair<String, String>> rangeComponents) {
        this.currentCoverage = currentCoverage;
        this.inputCoverage = inputCoverage;
        this.subsets = subsets;
        this.rangeComponents = rangeComponents;
    }

    /**
     * Validates the UpdateCoverage request according to the requirements defined by the WCS-T specs.
     * @throws WCSTSubsetDimensionMismatchException
     * @throws WCSTAxisCrsMismatchException
     * @throws WCSTAxisLabelMismatchException
     * @throws WCSTDomainSetMismatchException
     * @throws WCSTAxisNumberMismatchException
     * @throws WCSTRangeFieldNumberMismatchException
     * @throws WCSTRangeFieldNameMismatchException
     * @throws petascope.exceptions.wcst.WCSTAxisOffsetVectorMismatchException
     */
    public void validate() throws WCSTSubsetDimensionMismatchException, WCSTAxisCrsMismatchException,
            WCSTAxisLabelMismatchException, WCSTDomainSetMismatchException, WCSTAxisNumberMismatchException,
            WCSTRangeFieldNumberMismatchException, WCSTRangeFieldNameMismatchException, WCSTAxisOffsetVectorMismatchException {
        validateSubsets(currentCoverage,subsets);
        validateCrses(currentCoverage, inputCoverage);
        validateDomainSet(currentCoverage, inputCoverage, subsets);
        validateOffsetVectors(currentCoverage, inputCoverage);
        validateRangeType(currentCoverage, inputCoverage, rangeComponents);
    }

    /**
     * Checks if all dimensions indicated in the subset parameters appear in the list of dimensions of the coverage
     * targeted by the update operation.
     * @param currentCoverage the coverage targeted by the update op.
     * @param subsets the list of subsets passed as parameter to the update op.
     * @throws petascope.exceptions.wcst.WCSTSubsetDimensionMismatchException
     */
    private void validateSubsets(CoverageMetadata currentCoverage, List<DimensionSubset> subsets) throws WCSTSubsetDimensionMismatchException {
        //check that all subset labels exist in the current coverage
        for (DimensionSubset subset : subsets) {
            if (currentCoverage.getDomainByName(subset.getDimension()) == null) {
                log.error("Subset dimension " + subset.getDimension() + " was not found in the list of subsets of the " +
                        "target coverage.");
                throw new WCSTSubsetDimensionMismatchException(subset.getDimension());
            }
        }
    }

    /**
     * Validates the range type in case of complete replacements.
     * @param currentCoverage the coverage targeted by the update op.
     * @param inputCoverage the input coverage for the update op.
     * @param rangeComponents the list of range components to be updated.
     * @throws petascope.exceptions.wcst.WCSTRangeFieldNumberMismatchException
     * @throws petascope.exceptions.wcst.WCSTRangeFieldNameMismatchException
     */
    private void validateRangeType(CoverageMetadata currentCoverage, CoverageMetadata inputCoverage, List<Pair<String, String>> rangeComponents) throws WCSTRangeFieldNumberMismatchException, WCSTRangeFieldNameMismatchException {
        //in case no range components are indicated, the range type must match
        if (rangeComponents == null || rangeComponents.isEmpty()) {
            //check if the coverages have the same number of bands
            if (currentCoverage.getNumberOfBands() != inputCoverage.getNumberOfBands()) {
                log.error("The number of bands (swe:field elements) of target coverage (found " + currentCoverage.getNumberOfBands() + " ) " +
                        "and input coverage (found " + inputCoverage.getNumberOfBands() + ") don't match.");
                throw new WCSTRangeFieldNumberMismatchException(currentCoverage.getNumberOfBands(), inputCoverage.getNumberOfBands());
            }
            //check if the names of the bands are the same
            Iterator<RangeElement> currentCovBands = currentCoverage.getRangeIterator();
            Iterator<RangeElement> inputCovBands = inputCoverage.getRangeIterator();
            while (currentCovBands.hasNext()){
                RangeElement currentBand = currentCovBands.next();
                RangeElement inputBand = inputCovBands.next();
                if (!currentBand.getName().equals(inputBand.getName())) {
                    log.error("Band (swe:field element) name mismatch, found " + currentBand.getName() + " in target coverage, " + inputBand.getName() + " in input coverage.");
                    throw new WCSTRangeFieldNameMismatchException(currentBand.getName(), inputBand.getName());
                }
            }

        }
    }

    /**
     * Check if both coverages have same offset vectors size (resolution) for each axis
     * (e.g: coverage 1 - pixel size: 15 meter, coverage 2 - pixel size: 300 meter) is not valid to mosaic.
     * @param currentCoverage
     * @param inputCoverage
     */
    private void validateOffsetVectors(CoverageMetadata currentCoverage, CoverageMetadata inputCoverage) throws WCSTAxisOffsetVectorMismatchException {
        Iterator<Entry<List<BigDecimal>, BigDecimal>> currentCovIterator = currentCoverage.getGridAxes().entrySet().iterator();
        Iterator<Entry<List<BigDecimal>, BigDecimal>> inputCovIterator = inputCoverage.getGridAxes().entrySet().iterator();
        
        // NOTE: in case of TimeSeries (e.g: 3D, offset vectors of existing coverage is 3 and offset vectors of input coverage is 2).
        // other case they have same offset vectors (so we use inputCovIterator to iterate).       
        while (inputCovIterator.hasNext()) {
            Entry<List<BigDecimal>, BigDecimal> currentCovEntry = currentCovIterator.next();
            Entry<List<BigDecimal>, BigDecimal> inputCovEntry = inputCovIterator.next();
            // we get the non-zero offset vector from the offset vector list (e.g: [0, 1, 0] -> 1)
            BigDecimal currentCovOffsetValue = this.getNonZeroOffsetValue(currentCovEntry.getKey());
            BigDecimal inputCovOffsetValue = this.getNonZeroOffsetValue(inputCovEntry.getKey());
            
            if (!currentCovOffsetValue.equals(inputCovOffsetValue)) {
                throw new WCSTAxisOffsetVectorMismatchException(ListUtil.printList(currentCovEntry.getKey(), " "),
                                                                ListUtil.printList(inputCovEntry.getKey(), " "));
            }
        }
    }

    /**
     * Validates the domain set in case of complete replacements.
     * @param currentCoverage the coverage targeted by the update op.
     * @param inputCoverage the input coverage.
     * @param subsets the list of subsets.
     */
    private void validateDomainSet(CoverageMetadata currentCoverage, CoverageMetadata inputCoverage, List<DimensionSubset> subsets) throws WCSTAxisNumberMismatchException, WCSTDomainSetMismatchException {
        //in case no subset is indicated, domain set must match
        if (subsets.isEmpty()) {
            List<DomainElement> currentCoverageAxes = currentCoverage.getDomainList();
            List<DomainElement> inputCoverageAxes = inputCoverage.getDomainList();
            //same number of axes
            if (currentCoverageAxes.size() != inputCoverageAxes.size()) {
                log.error("In case of complete replacement, the number of axes of the target coverage (found " + currentCoverageAxes.size() +") must match the " +
                        "number of axes of the input coverage (found " + inputCoverageAxes.size() +"). Use a subset parameter if partial replacement is intended.");
                throw new WCSTAxisNumberMismatchException(currentCoverageAxes.size(), inputCoverageAxes.size());
            }
            //axes must match in all aspects, including order
            for (int i = 0; i < currentCoverageAxes.size(); i++) {
                DomainElement currentAxis = currentCoverageAxes.get(i);
                DomainElement inputAxis = inputCoverageAxes.get(i);
                if (!currentAxis.equals(inputAxis)) {
                    log.error("In case of complete replacement, the domain set of targeted coverage and input coverage must match. Use a subset parameter if partial " +
                            "replacement is intended.");
                    throw new WCSTDomainSetMismatchException();
                }
            }
        }
    }

    /**
     * Validates the crs of the coverages. The set of axes of an input coverage must be a subset of the set of axes of
     * the coverage to be updated, regarding crses.
     * @param currentCoverage the coverage targeted by the update op.
     * @param inputCoverage the input coverage for the update op.
     * @throws WCSTAxisLabelMismatchException
     * @throws WCSTAxisCrsMismatchException
     */
    private void validateCrses(CoverageMetadata currentCoverage, CoverageMetadata inputCoverage) throws WCSTAxisLabelMismatchException, WCSTAxisCrsMismatchException {
        //iterate through the axes of the input coverage
        for (DomainElement inputCoverageAxis : inputCoverage.getDomainList()) {
            //check that a corresponding axis with the same label exists in the current coverage
            String axisName = inputCoverageAxis.getLabel();
            DomainElement currentCoverageAxis = currentCoverage.getDomainByName(axisName);
            if (currentCoverageAxis == null) {
                //axis need to have same names
                log.error("Axis label " + axisName + " from the input coverage was not found in the target coverage.");
                throw new WCSTAxisLabelMismatchException(axisName);
            }
            if (!currentCoverageAxis.getCrsDef().equals(inputCoverageAxis.getCrsDef())) {
                //axis need to have same native crs
                log.error("Native crs of axis " + axisName + " from the input coverage (found " + inputCoverageAxis.getNativeCrs() + ") " +
                        "doesn't match with one in the target coverage (found " + currentCoverageAxis.getNativeCrs() + ").");
                throw new WCSTAxisCrsMismatchException(axisName, inputCoverageAxis.getNativeCrs(), currentCoverageAxis.getNativeCrs());
            }
        }
    }
    
    /**
     * Iterate the offset vector values to get the non-zero value
     * @param offsetValues
     * @return 
     */
    private BigDecimal getNonZeroOffsetValue(List<BigDecimal> offsetValues) {
        BigDecimal output = null;
        for (BigDecimal value:offsetValues) {
            if (!value.equals(0)) {
                output = value;
                break;
            }
        }
        return output;
    }
}
