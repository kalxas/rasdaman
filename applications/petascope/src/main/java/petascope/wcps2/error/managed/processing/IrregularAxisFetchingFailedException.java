package petascope.wcps2.error.managed.processing;

/**
 * Exception that is thrown when irregular axis cannot be fetched
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class IrregularAxisFetchingFailedException extends WCPSProcessingError {

    /**
     * Constructor for the class
     *
     * @param coverageName  the name of the coverage
     * @param axisName      the name of the axis whose fetching failed
     * @param originalCause the original exception cause
     */
    public IrregularAxisFetchingFailedException(String coverageName, String axisName, Exception originalCause) {
        this.coverageName = coverageName;
        this.axisName = axisName;
        this.originalCause = originalCause;
    }

    private final String coverageName;
    private final String axisName;
    private final Exception originalCause;

}
