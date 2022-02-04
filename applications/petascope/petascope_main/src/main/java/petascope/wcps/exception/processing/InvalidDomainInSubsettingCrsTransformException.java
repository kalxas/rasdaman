package petascope.wcps.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * General error for invalid outputCrs projection
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class InvalidDomainInSubsettingCrsTransformException  extends WCPSException {

    /**
     * Constructor for the class
     */
    public InvalidDomainInSubsettingCrsTransformException(String axisName, String subsettingCrs, String errorMessage) {
        super(ExceptionCode.InvalidRequest, ERROR_TEMPLATE.replace("$axisName", axisName)
              .replace("$subsettingCrs", subsettingCrs)
              .replace("$errorMessage", errorMessage));
    }

    private static final String ERROR_TEMPLATE = "Invalid domain on axis '$axisName' to transform with subsettingCrs '$subsettingCrs', '$errorMessage'.";

}