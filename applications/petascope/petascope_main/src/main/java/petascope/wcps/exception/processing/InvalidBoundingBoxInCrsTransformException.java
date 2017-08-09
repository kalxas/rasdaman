package petascope.wcps.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * General error for invalid outputCrs projection
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class InvalidBoundingBoxInCrsTransformException  extends WCPSException {

    /**
     * Constructor for the class
     *
     * @param boundingBox the bounding box which is used to transform
     * @param outputCrs
     * @param errorMessage
     */
    public InvalidBoundingBoxInCrsTransformException(String boundingBox, String outputCrs, String errorMessage) {
        super(ExceptionCode.WcpsError, ERROR_TEMPLATE.replace("$boundingBox", boundingBox)
              .replace("$outputCrs", outputCrs)
              .replace("$errorMessage", errorMessage));
    }

    private static final String ERROR_TEMPLATE = "Invalid bounding box '$boundingBox' in CRS transform with outputCrs '$outputCrs', $errorMessage.";

}