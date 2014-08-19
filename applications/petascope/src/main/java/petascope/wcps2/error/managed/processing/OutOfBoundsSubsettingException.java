package petascope.wcps2.error.managed.processing;

import petascope.wcps2.metadata.Interval;

/**
 * Error for invalid out of bounds subset parameters
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class OutOfBoundsSubsettingException extends InvalidSubsettingException {

    /**
     * Constructor for the class
     *
     * @param axisName the axis on which the subset is being made
     * @param subset   the offending subset
     */
    public OutOfBoundsSubsettingException(String axisName, Interval<String> subset) {
        super(axisName, subset);
    }
}
