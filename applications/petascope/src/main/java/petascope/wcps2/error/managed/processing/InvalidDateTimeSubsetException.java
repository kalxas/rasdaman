package petascope.wcps2.error.managed.processing;

import petascope.wcps2.metadata.Interval;

/**
 * Error occurring when the given time subset cannot be correctly parsed
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class InvalidDateTimeSubsetException extends InvalidSubsettingException {
    /**
     * Constructor for the class
     *
     * @param axisName the axis on which the subset is being made
     * @param subset   the offending subset
     */
    public InvalidDateTimeSubsetException(String axisName, Interval<String> subset) {
        super(axisName, subset);
    }

}
