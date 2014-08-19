package petascope.wcps2.error.managed.processing;

/**
 * Exception for errors regarding coverage metadata
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageMetadataException extends WCPSProcessingError {
    /**
     * Constructor for the class
     *
     * @param originalCause the exception that caused the error
     */
    public CoverageMetadataException(Exception originalCause) {
        super(TEMPLATE.replace("$metadataError", originalCause.getMessage()));
        this.originalCause = originalCause;

    }

    /**
     * Getter for the original cause error
     *
     * @return
     */
    public Exception getOriginalCause() {
        return originalCause;
    }

    private final Exception originalCause;
    private static final String TEMPLATE = "Error in processing coverage metadata: $metadataError";
}
