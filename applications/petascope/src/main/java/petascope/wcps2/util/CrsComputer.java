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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.util;

import org.apache.commons.lang3.math.NumberUtils;
import petascope.exceptions.PetascopeException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.TimeUtil;
import petascope.util.XMLSymbols;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps2.error.managed.processing.*;
import petascope.wcps2.metadata.legacy.Coverage;
import petascope.wcps2.metadata.legacy.CoverageRegistry;
import petascope.wcps2.metadata.model.ParsedSubset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import petascope.exceptions.WCSException;

/**
 * Translates a CRS subset interval to array indices
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CrsComputer {


    /**
     * Constructor for the class in case trimming
     *
     * @param axisName the name of the axis on which the subset is being made
     * @param crsName  the name of the crs that should be applied @unused
     * @param subset   the subset that has to be translated
     * @param coverage the coverage on which the subsetting is applied
     * @param registry the coverage registry metadata
     */
    public CrsComputer(String axisName, String crsName, ParsedSubset<String> subset, Coverage coverage, CoverageRegistry registry) {
        this.axisName = axisName;
        this.crsName = crsName;
        this.subset = subset;
        this.coverage = coverage;
        this.registry = registry;
        this.subset.setSubsetTrim(true);
    }

    /**
     * Constructor for the class in case slicing
     *
     * @param axisName the name of the axis on which the subset is being made
     * @param crsName the name of the crs that should be applied @unused
     * @param slicingCoordinate the slicing coordinate that has to be translated
     * @param coverage the coverage on which the subsetting is applied
     * @param registry the coverage registry metadata
     */
    public CrsComputer(String axisName, String crsName, String slicingCoordinate, Coverage coverage, CoverageRegistry registry) {
        this.axisName = axisName;
        this.crsName = crsName;
        // NOTE: in here, update slicing case to trimming case to use the same functions in this class
        this.subset = new ParsedSubset<String>(slicingCoordinate, slicingCoordinate);
        this.coverage = coverage;
        this.registry = registry;
        this.subset.setSubsetTrim(false);
    }

    public ParsedSubset<Long> getPixelIndices() throws WCSException {
        return getPixelIndices(false);
    }

    /**
     * Returns the translated interval from the original subset
     * @param ignoreOutOfBoundsCheck
     * @return
     */
    public ParsedSubset<Long> getPixelIndices(boolean ignoreOutOfBoundsCheck) throws WCSException {
        double lowerNumericLimit, upperNumericLimit;
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        CellDomainElement cdom = coverage.getCoverageInfo().getCellDomainByName(axisName);
        if (null == cdom || null == dom) {
            throw new CoverageAxisNotFoundExeption(axisName);
        }

        try {
            subset = replaceStarsWithRealValues(dom);
            lowerNumericLimit = Double.parseDouble(subset.getLowerLimit());
            upperNumericLimit = Double.parseDouble(subset.getUpperLimit());
            if (coverage.getCoverageMetadata().getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
                return returnGridPixelIndices(new ParsedSubset<Double>(lowerNumericLimit, upperNumericLimit));
            } else {
                return getNumericPixelIndices(new ParsedSubset<Double>(lowerNumericLimit, upperNumericLimit), ignoreOutOfBoundsCheck);
            }
        } catch (NumberFormatException e) {
            // NOTE: in case time axis is String, e.g "1950-01-01" then it will use this method
            return getTimePixelIndices(ignoreOutOfBoundsCheck);
        }
    }


    /**
     * Replaces stars in the interval with the actual value
     *
     * @param dom the domain element corresponding to the axis
     * @return a new interval without stars
     */
    private ParsedSubset<String> replaceStarsWithRealValues(DomainElement dom) {
        if (subset.getLowerLimit().equals(MISSING_DIMENSION_SYMBOL)) {
            String coordinate = dom.getMinValue().toString();
            if (!NumberUtils.isNumber(subset.getUpperLimit())) {
                coordinate = numericIndicesToTime(coordinate, dom);
            }
            subset = new ParsedSubset<String>(coordinate, subset.getUpperLimit());
        }
        if (subset.getUpperLimit().equals(MISSING_DIMENSION_SYMBOL)) {
            String coordinate = dom.getMaxValue().toString();
            if (!NumberUtils.isNumber(subset.getLowerLimit())) {
                coordinate = numericIndicesToTime(coordinate, dom);
            }
            subset = new ParsedSubset<String>(subset.getLowerLimit(), coordinate);
        }
        return subset;
    }

    /**
     * Converts numeric indices to time coordinates
     *
     * @param coordinate the numeric coordinate
     * @param dom        the domain element of the axis
     * @return a string representing the translated time
     */
    private String numericIndicesToTime(String coordinate, DomainElement dom) {
        String datumOrigin = dom.getAxisDef().getCrsDefinition().getDatumOrigin();
        try {
            return TimeUtil.coordinate2timestamp(Double.parseDouble(coordinate), datumOrigin, dom.getAxisDef().getUoM());
        } catch (PetascopeException e) {
            throw new InvalidCalculatedBoundsSubsettingException(axisName, subset);
        }
    }

    /**
     * Converts grid coordinates to pixel indices (nothing needs to be done except casting into the correct datatype)
     *
     * @param numericSubset the subset interval to be translated
     * @return the translated interval
     */
    private ParsedSubset<Long> returnGridPixelIndices(ParsedSubset<Double> numericSubset) {
        return new ParsedSubset<Long>((long) numericSubset.getLowerLimit().doubleValue(), (long) numericSubset.getUpperLimit().doubleValue());
    }

    /**
     * Returns the translated pixel indices for numeric subsets
     *
     * @param numericSubset the numeric subset to be translated
     * @return
     */
    private ParsedSubset<Long> getNumericPixelIndices(ParsedSubset<Double> numericSubset, boolean ignoreOutOfBoundsValidityCheck) {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);

        this.checkNumericSubsetValidity(numericSubset, ignoreOutOfBoundsValidityCheck);

        final ParsedSubset<Long> result;
        if (dom.isIrregular()) {
            result = getNumericPixelIndicesForIrregularAxes(numericSubset);

        } else {
            result = getNumericPixelIndicesForRegularAxis(numericSubset);
        }
        return result;
    }

    /**
     * Checks if the subset is valid and can be translated
     *
     * @param numericSubset the subset to be translated
     */
    private void checkNumericSubsetValidity(ParsedSubset<Double> numericSubset, boolean ignoreOutOfBoundsValidityCheck) {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        BigDecimal domMin = dom.getMinValue();
        BigDecimal domMax = dom.getMaxValue();
        // Check order
        if (numericSubset.getUpperLimit() < numericSubset.getLowerLimit()) {
            throw new UnorderedSubsetException(axisName, subset);
        }

        if (!ignoreOutOfBoundsValidityCheck) {
            // ticket:1212 - no accept intersection, only query inside domain is accepted.
            // NOTE: no check if subset is used in scale, extend expression: scale(c, {...}), etend(c, {...})
            if (!subset.isSubsetScaleExtend()) {
                if (numericSubset.getLowerLimit() < domMin.doubleValue() || numericSubset.getUpperLimit() > domMax.doubleValue()) {
                    throw new OutOfBoundsSubsettingException(axisName, subset, domMin.doubleValue(), domMax.doubleValue());
                }
            }
        }
    }

    /**
     * Returns the translated subset if the coverage has an irregular axis
     *
     * @param numericSubset the subset to be translated
     * @return
     */
    private ParsedSubset<Long> getNumericPixelIndicesForIrregularAxes(ParsedSubset<Double> numericSubset) {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        CellDomainElement cdom = coverage.getCoverageInfo().getCellDomainByName(axisName);
        long pxMin = cdom.getLoInt();
        long pxMax = cdom.getHiInt();
        BigDecimal domMin = dom.getMinValue();
        // Need to query the database (IRRSERIES table) to get the extents
        try {
            // Retrieve correspondent cell indexes (unique method for numerical/timestamp values)
            // TODO: I need to extract all the values, not just the extremes

            long[] indexes = registry.getMetadataSource().getIndexesFromIrregularRectilinearAxis(
                                 coverage.getCoverageInfo().getCoverageName(),
                                 coverage.getCoverageInfo().getDomainIndexByName(axisName), // i-order of axis
                                 (new BigDecimal(numericSubset.getLowerLimit().toString())).subtract(domMin),  // coefficients are relative to the origin, but subsets are not.
                                 (new BigDecimal(numericSubset.getUpperLimit().toString())).subtract(domMin),  //
                                 pxMin, pxMax);

            // Add sdom lower bound
            return new ParsedSubset<Long>(indexes[0] + pxMin, indexes[1]);

        } catch (Exception e) {
            throw new IrregularAxisFetchingFailedException(coverage.getCoverageName(), axisName, e);
        }
    }

    /**
     * Returns the translated subset for regular axis
     *
     * @param numericSubset the numeric subset to be translated
     * @return
     */
    private ParsedSubset<Long> getNumericPixelIndicesForRegularAxis(ParsedSubset<Double> numericSubset) {
        boolean zeroIsMin = coverage.getCoverageMetadata().getDomainDirectionalResolution(axisName).doubleValue() > 0;
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        CellDomainElement cdom = coverage.getCoverageInfo().getCellDomainByName(axisName);
        String axisUoM = dom.getUom();
        // Get Domain extremes (real sdom)
        BigDecimal domMin = dom.getMinValue();
        BigDecimal domMax = dom.getMaxValue();

        long pxMin = cdom.getLoInt();
        long pxMax = cdom.getHiInt();

        // Indexed CRSs do not require conversion
        if (crsName.contains(CrsUtil.GRID_CRS)) {
            return new ParsedSubset<Long>((long) numericSubset.getLowerLimit().doubleValue(), (long) numericSubset.getUpperLimit().doubleValue());
        }

        //use real cell width when more that 1 pixel exists on this dimension

        BigDecimal cellWidth = null;
        if (domMax.compareTo(domMin) == 0) {
            cellWidth = coverage.getCoverageMetadata().getDomainDirectionalResolution(axisName);
        } else {
            cellWidth = (domMax.subtract(domMin))
                        .divide((BigDecimal.valueOf(pxMax + 1)).subtract(BigDecimal.valueOf(pxMin)), RoundingMode.UP);
        }

        // Open interval on the right: take away epsilon from upper bound:

        long returnLowerLimit, returnUpperLimit;
        if (zeroIsMin) {
            // Normal linear numerical axis
            returnLowerLimit = (long) Math.floor(BigDecimalUtil.divide(BigDecimal.valueOf(numericSubset.getLowerLimit()).subtract(domMin), cellWidth).doubleValue()) + pxMin;
            if (numericSubset.getUpperLimit().equals(numericSubset.getLowerLimit())) {
                returnUpperLimit = returnLowerLimit;
            } else {
                returnUpperLimit = (long) Math.ceil(BigDecimalUtil.divide(BigDecimal.valueOf(numericSubset.getUpperLimit()).subtract(domMin), cellWidth).doubleValue()) - 1 + pxMin;
            }
            // NOTE: the if a slice equals the upper bound of a coverage, out[0]=pxHi+1 but still it is a valid subset.

            if ((domMax.compareTo(domMin) != 0) && numericSubset.getLowerLimit().equals(numericSubset.getUpperLimit()) && numericSubset.getUpperLimit().equals(domMax.doubleValue())) {
                returnLowerLimit = returnLowerLimit - 1;
            }
            
            // This happens when max_subset = min_subset + 0.01 for example (at least for WMS)
            if (returnLowerLimit > returnUpperLimit) {
                returnUpperLimit = returnLowerLimit;
            }
        } else {
            // Linear negative axis (eg northing of georeferenced images)
            // First coordHi, so that left-hand index is the lower one
            returnLowerLimit = (long) Math.ceil(BigDecimalUtil.divide(domMax.subtract(BigDecimal.valueOf(numericSubset.getUpperLimit())), cellWidth).doubleValue()) + pxMin;
            if (numericSubset.getUpperLimit().equals(numericSubset.getLowerLimit())) {
                returnUpperLimit = returnLowerLimit;
            } else {
                returnUpperLimit = (long) Math.floor(BigDecimalUtil.divide(domMax.subtract(BigDecimal.valueOf(numericSubset.getLowerLimit())), cellWidth).doubleValue()) - 1 + pxMin;
            }
            // NOTE: the if a slice equals the lower bound of a coverage, out[0]=pxHi+1 but still it is a valid subset.
            if ((domMax.compareTo(domMin) != 0) && numericSubset.getLowerLimit().equals(numericSubset.getUpperLimit()) && numericSubset.getUpperLimit() == domMin.doubleValue()) {
                returnLowerLimit -= 1;
            }
            
            // This happens when max_subset = min_subset + 0.01 for example (at least for WMS)
            if (returnLowerLimit > returnUpperLimit) {
                returnUpperLimit = returnLowerLimit;
            }
        }
        return new ParsedSubset<Long>(returnLowerLimit, returnUpperLimit);
    }


    /**
     * Checks if the subset is valid and can be translated
     */
    private void checkTimeSubsetValidity() throws WCSException {
        if (!TimeUtil.isValidTimestamp(subset.getLowerLimit())) {
            throw new InvalidDateTimeSubsetException(axisName, subset);
        }
        if (!TimeUtil.isValidTimestamp(subset.getUpperLimit())) {
            throw new InvalidDateTimeSubsetException(axisName, subset);
        }

        if (!TimeUtil.isOrderedTimeSubset(subset.getLowerLimit(), subset.getUpperLimit())) {
            throw new UnorderedSubsetException(axisName, subset);
        }
    }


    /**
     * Returns the translated interval for time subsets
     *
     * @return
     */
    private ParsedSubset<Long> getTimePixelIndices(boolean ignoreOutOfBoundsCheck) throws WCSException {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        checkTimeSubsetValidity();
        final ParsedSubset<Long> result;
        if (dom.isIrregular()) {
            result = getTimePixelIndicesForIrregularAxis();
        } else {
            result = getTimePixelIndicesForRegularAxis(ignoreOutOfBoundsCheck);
        }
        return result;
    }

    /**
     * Returns the translated interval for coverages with a irregular axis
     *
     * @return
     */
    private ParsedSubset<Long> getTimePixelIndicesForIrregularAxis() {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        String axisUoM = dom.getUom();
        String datumOrigin = dom.getAxisDef().getCrsDefinition().getDatumOrigin();
        double numLo = 0;
        double numHi = 0;
        try {
            numLo = TimeUtil.countOffsets(datumOrigin, subset.getLowerLimit(), axisUoM, dom.getScalarResolution().doubleValue());
            numHi = TimeUtil.countOffsets(datumOrigin, subset.getUpperLimit(), axisUoM, dom.getScalarResolution().doubleValue());
        } catch (PetascopeException e) {
            throw new InvalidCalculatedBoundsSubsettingException(axisName, subset);
        }
        return getNumericPixelIndicesForIrregularAxes(new ParsedSubset<Double>(numLo, numHi));
    }

    /**
     * Returns the translated interval for coverages with a regular axis
     *
     * @return
     */
    private ParsedSubset<Long> getTimePixelIndicesForRegularAxis(boolean ignoreOutOfBoundsValidityCheck) {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        String axisUoM = dom.getUom();
        String datumOrigin = dom.getAxisDef().getCrsDefinition().getDatumOrigin();
        BigDecimal domMin = dom.getMinValue();
        BigDecimal domMax = dom.getMaxValue();

        double numLo = 0;
        double numHi = 0;

        try {
            // Need to convert timestamps to TemporalCRS numeric coordinates
            numLo = TimeUtil.countOffsets(datumOrigin, subset.getLowerLimit(), axisUoM, 1D); // do not normalize by vector here:
            numHi = TimeUtil.countOffsets(datumOrigin, subset.getUpperLimit(), axisUoM, 1D); // absolute time coordinates needed.

        } catch (PetascopeException e) {
            throw new InvalidCalculatedBoundsSubsettingException(axisName, subset);
        }
        // Consistency check
        // ticket:1212 - no accept intersection, only query inside domain is accepted.
        if (!ignoreOutOfBoundsValidityCheck && (numLo < domMin.doubleValue() || numHi > domMax.doubleValue())) {
            String timeUpperBound;
            String timeLowerBound;
            try {
                // Get the lowerbound for time axis
                timeLowerBound = TimeUtil.coordinate2timestamp(domMin.doubleValue(), datumOrigin, dom.getAxisDef().getUoM());
                // Get the upperbound for time axis
                timeUpperBound = TimeUtil.coordinate2timestamp(domMax.doubleValue(), datumOrigin, dom.getAxisDef().getUoM());
            } catch (PetascopeException ex) {
                throw new InvalidCalculatedBoundsSubsettingException(axisName, subset);
            }
            // Return the lowerbound:upperbound in string for time axis
            throw new OutOfBoundsSubsettingException(axisName, subset, timeLowerBound, timeUpperBound);
        }

        return getNumericPixelIndicesForRegularAxis(new ParsedSubset<Double>(numLo, numHi));
    }

    private final String axisName;
    private final String crsName;
    private ParsedSubset<String> subset;
    private final Coverage coverage;
    private final CoverageRegistry registry;
    private static final String MISSING_DIMENSION_SYMBOL = "*";
}
