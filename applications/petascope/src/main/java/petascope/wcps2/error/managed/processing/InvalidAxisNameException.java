package petascope.wcps2.error.managed.processing;

/**
 * Error message for invalid axes
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class InvalidAxisNameException extends WCPSProcessingError {
    /**
     * Constructor for the class
     *
     * @param axisName the name of the axis that is invalid
     */
    public InvalidAxisNameException(String axisName) {
        this.axisName = axisName;
    }


    /**
     * Getter for the axis name
     *
     * @return
     */
    public String getAxisName() {
        return axisName;
    }

    private final String axisName;

    private static final String TEMPLATE = "Invalid axis name: $axisName";
}
