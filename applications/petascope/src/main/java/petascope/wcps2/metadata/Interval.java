package petascope.wcps2.metadata;

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

    private final CoordinateType lowerLimit;
    private final CoordinateType upperLimit;

}
