package petascope.wcps2.util;

import org.apache.commons.lang3.math.NumberUtils;
import petascope.core.CrsDefinition;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsUtil;
import petascope.util.TimeUtil;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps2.error.managed.processing.*;
import petascope.wcps2.metadata.Coverage;
import petascope.wcps2.metadata.CoverageRegistry;
import petascope.wcps2.metadata.Interval;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    /**
     * Returns the translated interval from the original subset
     *
     * @return
     */
    public Interval<Long> getPixelIndices() {
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
            if (crsName != null && crsName.equals(CrsUtil.GRID_CRS)) {
                return returnGridPixelIndices(new Interval<Double>(lowerNumericLimit, upperNumericLimit));
            } else {
                return getNumericPixelIndices(new Interval<Double>(lowerNumericLimit, upperNumericLimit));
            }
        } catch (NumberFormatException e) {
            return getTimePixelIndices();
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
    private Interval<Long> getNumericPixelIndices(Interval<Double> numericSubset) {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);

        this.checkNumericSubsetValidity(numericSubset);

        final Interval<Long> result;
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
    private void checkNumericSubsetValidity(Interval<Double> numericSubset) {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        BigDecimal domMin = dom.getMinValue();
        BigDecimal domMax = dom.getMaxValue();
        // Check order
        if (numericSubset.getUpperLimit() < numericSubset.getLowerLimit()) {
            throw new UnorderedSubsetException(axisName, subset);
        }

        // Check intersection with extents
        if (numericSubset.getLowerLimit() > domMax.doubleValue() || numericSubset.getUpperLimit() < domMin.doubleValue()) {
            throw new OutOfBoundsSubsettingException(axisName, subset);
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
        boolean zeroIsMin = !CrsDefinition.Y_ALIASES.contains(axisName);
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
        double cellWidth = (domMax.subtract(domMin))
            .divide((BigDecimal.valueOf(pxMax + 1)).subtract(BigDecimal.valueOf(pxMin)), RoundingMode.UP)
            .doubleValue();

        // Open interval on the right: take away epsilon from upper bound:

        long returnLowerLimit, returnUpperLimit;
        if (zeroIsMin) {
            // Normal linear numerical axis
            returnLowerLimit = (long) Math.floor((numericSubset.getLowerLimit() - domMin.doubleValue()) / cellWidth) + pxMin;
            returnUpperLimit = (long) Math.floor((numericSubset.getUpperLimit() - domMin.doubleValue()) / cellWidth) + pxMin;
            // NOTE: the if a slice equals the upper bound of a coverage, out[0]=pxHi+1 but still it is a valid subset.
            if (numericSubset.getLowerLimit() == numericSubset.getUpperLimit() && numericSubset.getUpperLimit() == domMax.doubleValue()) {
                returnLowerLimit = returnLowerLimit - 1;
            }
        } else {
            // Linear negative axis (eg northing of georeferenced images)
            // First coordHi, so that left-hand index is the lower one
            returnLowerLimit = (long) Math.ceil((domMax.doubleValue() - numericSubset.getUpperLimit()) / cellWidth) + pxMin;
            returnUpperLimit = (long) Math.ceil((domMax.doubleValue() - numericSubset.getLowerLimit()) / cellWidth) + pxMin;
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
    private Interval<Long> getTimePixelIndices() {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        checkTimeSubsetValidity();
        final Interval<Long> result;
        if (dom.isIrregular()) {
            result = getTimePixelIndicesForIrregularAxis();
        } else {
            result = getTimePixelIndicesForRegularAxis();
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
    private Interval<Long> getTimePixelIndicesForRegularAxis() {
        DomainElement dom = coverage.getCoverageInfo().getDomainByName(axisName);
        String axisUoM = dom.getUom();
        String datumOrigin = dom.getAxisDef().getCrsDefinition().getDatumOrigin();
        BigDecimal domMin = dom.getMinValue();
        BigDecimal domMax = dom.getMaxValue();

        double numLo = 0;
        double numHi = 0;

        try {
            // Need to convert timestamps to TemporalCRS numeric coordinates
            numLo = TimeUtil.countOffsets(datumOrigin, subset.getLowerLimit(), axisUoM, dom.getScalarResolution().doubleValue());
            numHi = TimeUtil.countOffsets(datumOrigin, subset.getUpperLimit(), axisUoM, dom.getScalarResolution().doubleValue());
        } catch (PetascopeException e) {
            throw new InvalidCalculatedBoundsException(axisName, subset);
        }
        // Consistency check
        if (numHi < domMin.doubleValue() || numLo > domMax.doubleValue()) {
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
