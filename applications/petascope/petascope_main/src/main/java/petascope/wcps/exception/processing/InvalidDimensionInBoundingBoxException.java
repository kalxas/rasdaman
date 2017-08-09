package petascope.wcps.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * General error for invalid bounding box
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class InvalidDimensionInBoundingBoxException  extends WCPSException {

    /**
     * Constructor for the class
     *
     * @param dimension number of dimension which is not equal to 2
     */
    public InvalidDimensionInBoundingBoxException(String dimension) {
        super(ExceptionCode.WcpsError, ERROR_TEMPLATE.replace("$dimension", dimension));
    }

    private static final String ERROR_TEMPLATE = "Only support encoding with bounding box in 2D, received '$dimensionD'.";

}