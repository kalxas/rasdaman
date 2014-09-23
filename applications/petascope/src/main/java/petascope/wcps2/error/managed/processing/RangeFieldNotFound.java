package petascope.wcps2.error.managed.processing;

/**
 * Error to be thrown when the range field requested was not found
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class RangeFieldNotFound extends WCPSProcessingError {

    /**
     * Constructor for the class
     *
     * @param rangeField the range field that was not found
     */
    public RangeFieldNotFound(String rangeField) {
        this.rangeField = rangeField;

    }

    private String rangeField;

}
