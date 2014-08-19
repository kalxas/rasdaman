package petascope.wcps2.error.managed.processing;

import petascope.wcps2.metadata.Interval;

/**
 * General error for invalid subsetting
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class InvalidSubsettingException extends WCPSProcessingError {

    /**
     * Constructor for the class
     *
     * @param axisName the axis on which the subset is being made
     * @param subset   the offending subset
     */
    public InvalidSubsettingException(String axisName, Interval<String> subset) {
        super(TEMPLATE.replace("$lowerBound", subset.getLowerLimit()).replace("$upperBound", subset.getUpperLimit()).replace("$axis", axisName));
        this.axisName = axisName;
        this.subset = subset;
    }

    /**
     * Getter for axis name
     *
     * @return
     */
    public String getAxisName() {
        return axisName;
    }

    /**
     * Returns the offendingSubset
     *
     * @return
     */
    public Interval<String> getSubset() {
        return subset;
    }

    private final String axisName;
    private final Interval<String> subset;

    private static final String TEMPLATE = "Invalid subsetting coordinates: $lowerBound:$upperBound for axis $axis.";


}
