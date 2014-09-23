package petascope.wcps2.translator;

/**
 * Class to translate a scale wcps expression into rasql
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ScaleExpression extends CoverageExpression {


    /**
     * Constructor for the class
     *
     * @param coverageExpression the coverage expression
     * @param dimensionIntervals the dimension intervals
     */
    public ScaleExpression(CoverageExpression coverageExpression, DimensionIntervalList dimensionIntervals) {
        this.dimensionIntervals = dimensionIntervals;
        this.coverageExpression = coverageExpression;
        setCoverage(coverageExpression.getCoverage());
        addChild(coverageExpression);
        addChild(dimensionIntervals);
    }

    /**
     * Returns the dimension intervals of the scale operation
     *
     * @return
     */
    public DimensionIntervalList getDimensionIntervals() {
        return dimensionIntervals;
    }

    @Override
    public String toRasql() {
        return TEMPLATE.replace("$coverageExpression", coverageExpression.toRasql()).replace("$dimensionIntervalList", dimensionIntervals.toRasql());
    }

    private final DimensionIntervalList dimensionIntervals;
    private final CoverageExpression coverageExpression;
    private final String TEMPLATE = "SCALE($coverageExpression, [$dimensionIntervalList])";
}
