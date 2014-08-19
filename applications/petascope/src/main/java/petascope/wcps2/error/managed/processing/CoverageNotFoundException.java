package petascope.wcps2.error.managed.processing;

/**
 * Exception that is thrown when a referenced coverage is not found in the database
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageNotFoundException extends WCPSProcessingError {

    /**
     * Constructor for the class
     *
     * @param coverageName the coverage that was not found
     */
    public CoverageNotFoundException(String coverageName) {
        this.coverageName = coverageName;
    }

    /**
     * Getter for the coverage name
     *
     * @return
     */
    public String getCoverageName() {
        return coverageName;
    }

    private final String coverageName;
    private static final String TEMPLATE = "Coverage $coverage was not found.";
}
