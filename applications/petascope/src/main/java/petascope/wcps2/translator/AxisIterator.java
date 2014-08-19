package petascope.wcps2.translator;

import petascope.wcps2.error.managed.processing.WCPSProcessingError;

/**
 * Translation node from wcps axis iterator to rasql
 * Example:
 * <code>
 * x in x(0:100)
 * </code>
 * translates to
 * <code>
 * x in [0:100]
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class AxisIterator extends AxisSpec {
    /**
     * Constructor for the class
     *
     * @param variableName the name of the variable used to iterate
     * @param axisName     the name of the axis on which to iterate
     * @param interval     the interval on which to iterate
     */
    public AxisIterator(String variableName, String axisName, IntervalExpression interval) {
        super(axisName, interval);
        this.variableName = variableName;
        addChild(interval);
    }

    @Override
    public String toRasql() throws WCPSProcessingError {
        String template = TEMPLATE.replace("$variableName", this.variableName.replace("$", "")).replace("$interval", this.interval.toRasql());
        return template;
    }


    /**
     * Returns the iterator name
     *
     * @return
     */
    public String getVariableName() {
        return variableName;
    }

    private String variableName;
    private final String TEMPLATE = "$variableName in $interval";
}
