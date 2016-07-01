package petascope.wcps2.error.managed.processing;

/**
 * General error for invalid slicing point
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class InvalidSlicingException extends WCPSProcessingError {

    /**
     * Constructor for the class
     *
     * @param axisName the axis on which the point is being made
     * @param slicingCoordinate the offending slicing coordinate
     */
    public InvalidSlicingException(String axisName, String slicingCoordinate) {
        super(ERROR_TEMPLATE.replace("$slicingCoordinate", slicingCoordinate).replace("$axis", axisName));
    }

    /**
     * Constructor for the class when subclass send appropriate exception
     * message
     *
     * @param axisName the axis on which the point is being made
     * @param slicingCoordinate the offending slicing coordinate
     * @param exceptionMessage the appropriate exception message (e.g: unordered
     * interval, time error,..)
     *
     */
    public InvalidSlicingException(String axisName, String slicingCoordinate, String exceptionMessage) {
        super(exceptionMessage);
    }

    private static final String ERROR_TEMPLATE = "Invalid slicing coordinate '$slicingCoordinate' for axis '$axis'.";

}
