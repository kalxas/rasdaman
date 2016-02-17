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
import petascope.wcps2.metadata.Coverage;
import petascope.wcps2.metadata.CoverageRegistry;
import petascope.wcps2.metadata.Interval;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Translates a CRS subset interval to array indices
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CrsComputer {


    /**
     * Constructor for the class
     *
     * @param axisName the name of the axis on which the subset is being made
     * @param crsName  the name of the crs that should be applied @unused
     * @param subset   the subset that has to be translated
     * @param coverage the coverage on which the subsetting is applied
     * @param registry the coverage registry metadata
     */
    public CrsComputer(String axisName, String crsName, Interval<String> subset, Coverage coverage, CoverageRegistry registry) {
        this.axisName = axisName;
        this.crsName = crsName;
        this.subset = subset;
        this.coverage = coverage;
        this.registry = registry;
    }

    public Interval<Long> getPixelIndices() {
        return getPixelIndices(false);
    }

    /**
     * Returns the translated interval from the original subset
     *
     * @return
     */
    public Interval<Long> getPixelIndices(boolean ignoreOutOfBoundsCheck) {
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
                return returnGridPixelIndices(new Interval<Double>(lowerNumericLimit, upperNumericLimit));
            } else {
                return getNumericPixelIndices(new Interval<Double>(lowerNumericLimit, upperNumericLimit), ignoreOutOfBoundsCheck);
            }
        } catch (NumberFormatException e) {
            return getTimePixelIndices(ignoreOutOfBoundsCheck);
        }
    }


    /**
     * Replaces stars in the interval with the actual value
     *
     * @param dom the domain element corresponding to the axis
     * @return a new interval without stars
     */
    private Interval<String> replaceStarsWithRealValues(DomainElement dom) {
        if (subset.getLowerLimit().equals(MISSING_DIMENSION_SYMBOL)) {
            String coordinate = dom.getMinValue().toString();
            if (!NumberUtils.isNumber(subset.getUpperLimit())) {
                coordinate = numericIndicesToTime(coordinate, dom);
            }
            subset = new Interval<String>(coordinate, subset.getUpperLimit());
        }
        if (subset.getUpperLimit().equals(MISSING_DIMENSION_SYMBOL)) {
            String coordinate = dom.getMaxValue().toString();
            if (!NumberUtils.isNumber(subset.getLowerLimit())) {
                coordinate = numericIndicesToTime(coordinate, dom);
            }
            subset = new Interval<String>(subset.getLowerLimit(), coordinate);
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
            throw new InvalidCalculatedBoundsException(axisName, subset);
        }
    }

    /**
     * Converts grid coordinates to pixel indices (nothing needs to be done except casting into the correct datatype)
     *
     * @param numericSubset the subset interval to be translated
     * @return the translated interval
     */
    private Interval<Long> returnGridPixelIndices(Interval<Double> numericSubset) {
        return new Interval<Long>((long) numericSubset.getLowerLimit().doubleValue(), (long) numericSubset.getUpperLimit().doubleValue());
    }

    /**
     * Returns the translated pixel indices for numeric subsets
     *
     * @param numericSubset the numeric subset to be translated
     * @return
     */
    private Interval<Long> getNumericPixelIndices(Interval<Double> numericSubset, boolean ignoreOutOfBoundsValidityCheck) {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);

        this.checkNumericSubsetValidity(numericSubset, ignoreOutOfBoundsValidityCheck);

        final Interval<Long> result;
        if (dom.isIrregular()) {
            result = getNumericPixelIndicesForIrregularAxes(numericSubset);

        } else {
            result = getNumericPixelIndicesForRegularAxis(numericSubset);
        }
        return result;
    }

    private void checkNumericSubsetValidity(Interval<Double> numericSubset) {
        checkNumericSubsetValidity(numericSubset, false);
    }

    /**
     * Checks if the subset is valid and can be translated
     *
     * @param numericSubset the subset to be translated
     */
    private void checkNumericSubsetValidity(Interval<Double> numericSubset, boolean ignoreOutOfBoundsValidityCheck) {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        BigDecimal domMin = dom.getMinValue();
        BigDecimal domMax = dom.getMaxValue();
        // Check order
        if (numericSubset.getUpperLimit() < numericSubset.getLowerLimit()) {
            throw new UnorderedSubsetException(axisName, subset);
        }

        if (!ignoreOutOfBoundsValidityCheck) {
            // Check intersection with extents
            if (numericSubset.getLowerLimit() > domMax.doubleValue() || numericSubset.getUpperLimit() < domMin.doubleValue()) {
                throw new OutOfBoundsSubsettingException(axisName, subset);
            }
        }
    }

    /**
     * Returns the translated subset if the coverage has an irregular axis
     *
     * @param numericSubset the subset to be translated
     * @return
     */
    private Interval<Long> getNumericPixelIndicesForIrregularAxes(Interval<Double> numericSubset) {
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
                    (new BigDecimal(numericSubset.getLowerLimit())).subtract(domMin),  // coefficients are relative to the origin, but subsets are not.
                    (new BigDecimal(numericSubset.getUpperLimit())).subtract(domMin),  //
                    pxMin, pxMax);

            // Add sdom lower bound
            return new Interval<Long>(indexes[0] + pxMin, indexes[1]);

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
    private Interval<Long> getNumericPixelIndicesForRegularAxis(Interval<Double> numericSubset) {
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
        if (crsName == CrsUtil.GRID_CRS) {
            return new Interval<Long>((long) numericSubset.getLowerLimit().doubleValue(), (long) numericSubset.getUpperLimit().doubleValue());
        }

        // This part is copied over from CRSUtil.java verbatim TODO: refactor the whole CRS computer ASAP
        // Indexed CRSs (integer "GridSpacing" UoM): no need for math proportion, coords are just int offsets.
        // This is not the same as CRS:1 which access direct grid coordinates.
        // Index CRSs have indexed coordinates, but still offset vectors can determine a different reference (eg origin is UL corner).
        if (axisUoM.equals(CrsUtil.INDEX_UOM)) {
            int count = 0;
            long indexMin = cdom.getLoInt();
            long indexMax = cdom.getHiInt();
            // S = subset value, px_s = subset grid index
            // m = min(grid index), M = max(grid index)
            // {isPositiveForwards / isNegativeForwards}
            // Formula : px_s = grid_origin + [{S/M} - {m/S}]
            long[] subsetGridIndexes = new long[2];
            for (String subset : (Arrays.asList(new String[]{numericSubset.getLowerLimit().toString(), numericSubset.getUpperLimit().toString()}))) {
                // NOTE: on subsets.lo the /next/ integer needs to be taken : trunc(stringLo) + 1 (if it is not exact integer)
                boolean roundUp = subset.equals(numericSubset.getLowerLimit().toString()) && ((double) Double.parseDouble(subset) != (long) Double.parseDouble(subset));
                long hiBound = dom.isPositiveForwards() ? (long) Double.parseDouble(subset) + (roundUp ? 1 : 0) : indexMax;
                long loBound = dom.isPositiveForwards() ? indexMin : (long) Double.parseDouble(subset) + (roundUp ? 1 : 0);
                subsetGridIndexes[count] = domMin.longValue() + (hiBound - loBound);
                count += 1;
            }
            // if offset is negative, the limits are inverted
            if(subsetGridIndexes[0] > subsetGridIndexes[1]){
                return new Interval<Long>(subsetGridIndexes[1], subsetGridIndexes[0]);
            }
            return new Interval<Long>(subsetGridIndexes[0], subsetGridIndexes[1]);
        }

        BigDecimal cellWidth = (domMax.subtract(domMin))
                .divide((BigDecimal.valueOf(pxMax + 1)).subtract(BigDecimal.valueOf(pxMin)), RoundingMode.UP);

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
            if (numericSubset.getLowerLimit() == numericSubset.getUpperLimit() && numericSubset.getUpperLimit() == domMax.doubleValue()) {
                returnLowerLimit = returnLowerLimit - 1;
            }
        } else {
            // Linear negative axis (eg northing of georeferenced images)
            // First coordHi, so that left-hand index is the lower one
            returnLowerLimit = (long) Math.ceil(BigDecimalUtil.divide(domMax.subtract(BigDecimal.valueOf(numericSubset.getUpperLimit())), cellWidth).doubleValue()) + pxMin;
            if (numericSubset.getUpperLimit() == numericSubset.getLowerLimit()) {
                returnUpperLimit = returnLowerLimit;
            } else {
                returnUpperLimit = (long) Math.floor(BigDecimalUtil.divide(domMax.subtract(BigDecimal.valueOf(numericSubset.getLowerLimit())), cellWidth).doubleValue()) - 1 + pxMin;
            }
            // NOTE: the if a slice equals the lower bound of a coverage, out[0]=pxHi+1 but still it is a valid subset.
            if (numericSubset.getLowerLimit() == numericSubset.getUpperLimit() && numericSubset.getUpperLimit() == domMin.doubleValue()) {
                returnLowerLimit -= 1;
            }
        }
        return new Interval<Long>(returnLowerLimit, returnUpperLimit);
    }


    /**
     * Checks if the subset is valid and can be translated
     */
    private void checkTimeSubsetValidity() {
        if (!TimeUtil.isValidTimestamp(subset.getLowerLimit())) {
            throw new InvalidDateTimeSubsetException(axisName, subset);
        }
        if (!TimeUtil.isValidTimestamp(subset.getUpperLimit())) {
            throw new InvalidDateTimeSubsetException(axisName, subset);
        }

        try {
            if (!TimeUtil.isOrderedTimeSubset(subset.getLowerLimit(), subset.getUpperLimit())) {
                throw new UnorderedSubsetException(axisName, subset);
            }
        } catch (PetascopeException e) {
            throw new UnorderedSubsetException(axisName, subset);
        }
    }


    /**
     * Returns the translated interval for time subsets
     *
     * @return
     */
    private Interval<Long> getTimePixelIndices(boolean ignoreOutOfBoundsCheck) {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        checkTimeSubsetValidity();
        final Interval<Long> result;
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
    private Interval<Long> getTimePixelIndicesForIrregularAxis() {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        String axisUoM = dom.getUom();
        String datumOrigin = dom.getAxisDef().getCrsDefinition().getDatumOrigin();
        double numLo = 0;
        double numHi = 0;
        try {
            numLo = TimeUtil.countOffsets(datumOrigin, subset.getLowerLimit(), axisUoM, dom.getScalarResolution().doubleValue());
            numHi = TimeUtil.countOffsets(datumOrigin, subset.getUpperLimit(), axisUoM, dom.getScalarResolution().doubleValue());
        } catch (PetascopeException e) {
            throw new InvalidCalculatedBoundsException(axisName, subset);
        }
        return getNumericPixelIndicesForIrregularAxes(new Interval<Double>(numLo, numHi));
    }

    /**
     * Returns the translated interval for coverages with a regular axis
     *
     * @return
     */
    private Interval<Long> getTimePixelIndicesForRegularAxis(boolean ignoreOutOfBoundsValidityCheck) {
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
            throw new InvalidCalculatedBoundsException(axisName, subset);
        }
        // Consistency check
        if (!ignoreOutOfBoundsValidityCheck && (numHi < domMin.doubleValue() || numLo > domMax.doubleValue())) {
            throw new OutOfBoundsSubsettingException(axisName, subset);
        }

        return getNumericPixelIndicesForRegularAxis(new Interval<Double>(numLo, numHi));
    }

    private final String axisName;
    private final String crsName;
    private Interval<String> subset;
    private final Coverage coverage;
    private final CoverageRegistry registry;
    private static final String MISSING_DIMENSION_SYMBOL = "*";
}
