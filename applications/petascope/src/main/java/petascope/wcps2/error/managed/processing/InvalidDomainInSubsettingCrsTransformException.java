package petascope.wcps2.error.managed.processing;

/**
 * General error for invalid outputCrs projection
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class InvalidDomainInSubsettingCrsTransformException  extends WCPSProcessingError {

    /**
     * Constructor for the class
     *
     * @param axisName
     * @param subsettingCrs
     * @param errorMessage
     */
    public InvalidDomainInSubsettingCrsTransformException(String axisName, String subsettingCrs, String errorMessage) {
        super(ERROR_TEMPLATE.replace("$axisName", axisName)
                            .replace("$subsettingCrs", subsettingCrs)
                            .replace("$errorMessage", errorMessage));
    }

    private static final String ERROR_TEMPLATE = "Invalid domain on axis '$axisName' to transform with subsettingCrs '$subsettingCrs', '$errorMessage'.";

}