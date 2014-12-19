package petascope.wcps2.metadata;

import org.apache.commons.lang3.math.NumberUtils;
import petascope.wcps2.translator.TrimDimensionInterval;

/**
 * Class to represent a subset interval, e.g. [0:100]
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class Interval<CoordinateType> {

    /**
     * Constructor for the class
     *
     * @param lowerLimit the lower limit of the interval
     * @param upperLimit the upper limit of the interval
     */
    public Interval(CoordinateType lowerLimit, CoordinateType upperLimit) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    /**
     * Getter for the lower limit of the interval
     *
     * @return
     */
    public CoordinateType getLowerLimit() {
        return lowerLimit;
    }

    /**
     * Getter for the upper limit of the interval
     *
     * @return
     */
    public CoordinateType getUpperLimit() {
        return upperLimit;
    }

    /**
     * Indicates whether the interval bounds are numeric or not.
     * If one of the limits is *, the interval is still considered numeric.
     * They can be non-numeric in cases such as a[ i($x:$y)].
     *
     * @return
     */
    public boolean isCrsComputable() {
        if (isFullInterval()) {
            return false;
        }
        boolean isFirstDimensionNumeric = (NumberUtils.isNumber(lowerLimit.toString()) ||
                lowerLimit.toString().equals(TrimDimensionInterval.WHOLE_DIMENSION_SYMBOL) ||
                lowerLimit.toString().contains("-")
        );
        boolean isSecondDimensionNumeric = (NumberUtils.isNumber(upperLimit.toString()) ||
                upperLimit.toString().equals(TrimDimensionInterval.WHOLE_DIMENSION_SYMBOL) ||
                upperLimit.toString().contains("-")
        );
        return isFirstDimensionNumeric && isSecondDimensionNumeric;
    }

    /**
     * Returns true if the interval is of form *:*
     *
     * @return true if full interval false otherwise
     */
    private boolean isFullInterval() {
        return lowerLimit.toString().equals(TrimDimensionInterval.WHOLE_DIMENSION_SYMBOL) &&
                upperLimit.toString().equals(TrimDimensionInterval.WHOLE_DIMENSION_SYMBOL);
    }

    private final CoordinateType lowerLimit;
    private final CoordinateType upperLimit;

}
