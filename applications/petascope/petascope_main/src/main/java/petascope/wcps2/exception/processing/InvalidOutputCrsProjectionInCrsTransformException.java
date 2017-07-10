package petascope.wcps2.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * General error for invalid outputCrs projection
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class InvalidOutputCrsProjectionInCrsTransformException  extends WCPSException {

    /**
     * Constructor for the class
     *
     * @param axisName the axis on which the point is being made
     * @param outputCrs the source Crs
     */
    public InvalidOutputCrsProjectionInCrsTransformException(String outputCrs, String axisName) {
        super(ERROR_TEMPLATE.replace("$outputCrs", outputCrs)
              .replace("$axis", axisName), ExceptionCode.WcpsError);
    }

    private static final String ERROR_TEMPLATE = "Invalid outputCRS projection '$outputCrs' for axis '$axis'.";

}