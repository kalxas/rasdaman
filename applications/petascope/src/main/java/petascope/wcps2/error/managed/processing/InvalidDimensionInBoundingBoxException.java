package petascope.wcps2.error.managed.processing;

/**
 * General error for invalid bounding box
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class InvalidDimensionInBoundingBoxException  extends WCPSProcessingError {

    /**
     * Constructor for the class
     *
     * @param dimension number of dimension which is not equal to 2
     */
    public InvalidDimensionInBoundingBoxException(String dimension) {
        super(ERROR_TEMPLATE.replace("$dimension", dimension));
    }

    private static final String ERROR_TEMPLATE = "Only support encoding with bounding box in 2D, received '$dimensionD'.";

}