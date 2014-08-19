package petascope.wcps2.error.managed.processing;

/**
 * Exception message for errors when initializing the coverage registry
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageMetadataNotInitializedException extends CoverageMetadataException {
    /**
     * Constructor for the class
     */
    public CoverageMetadataNotInitializedException(Exception originalCause) {
        super(originalCause);
    }

    public static final String ERROR_MESSAGE = "The metadata registry could not be initialized. Please check your database connection.\nFull error message: $e";
}
