package petascope.wcps2.error.managed.processing;

/**
 * General exception class for the wcps package.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class WCPSProcessingError extends RuntimeException {
    public WCPSProcessingError() {
    }

    public WCPSProcessingError(Throwable cause) {
        super(cause);
    }

    public WCPSProcessingError(String message) {
        super(message);
    }

    public WCPSProcessingError(String message, Throwable cause) {
        super(message, cause);
    }
}
