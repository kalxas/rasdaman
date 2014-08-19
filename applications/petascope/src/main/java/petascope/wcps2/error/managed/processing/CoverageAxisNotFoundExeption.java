package petascope.wcps2.error.managed.processing;

/**
 * Error exception for coverage axis lookup failure
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageAxisNotFoundExeption extends WCPSProcessingError {
    /**
     * Constructor for the class
     *
     * @param axisName the axis that was provided
     */
    public CoverageAxisNotFoundExeption(String axisName) {
        this.axisName = axisName;
    }

    /**
     * Getter for the axis name
     * @return
     */
    public String getAxisName() {
        return axisName;
    }

    private final String axisName;
    private static final String TEMPLATE = "Coverage Axis not found: $axisName";
}
