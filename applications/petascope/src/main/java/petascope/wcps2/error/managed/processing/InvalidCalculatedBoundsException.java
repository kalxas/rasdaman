package petascope.wcps2.error.managed.processing;

import petascope.wcps2.metadata.Interval;

/**
 * Error to be thrown if bounds that are calculated are not correct.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class InvalidCalculatedBoundsException extends InvalidSubsettingException {

    /**
     * Constructor for the class
     *
     * @param axisName the axis on which the subset is being made
     * @param subset   the offending subset
     */
    public InvalidCalculatedBoundsException(String axisName, Interval<String> subset) {
        super(axisName, subset);
    }

    public static String TEMPLATE = "The bounds were not correctly calculated$message";
}
