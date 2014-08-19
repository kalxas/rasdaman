package petascope.wcps2.translator;

/**
 * Translator class for the extend operation in wcps
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ExtendExpression extends CoverageExpression {


    /**
     * Constructor for the class
     *
     * @param coverage              the coverage to be extended
     * @param dimensionIntervalList a list of intervals to extend the coverage onto
     */
    public ExtendExpression(CoverageExpression coverage, DimensionIntervalList dimensionIntervalList) {
        this.coverage = coverage;
        this.dimensionIntervalList = dimensionIntervalList;
        setCoverage(coverage.getCoverage());
    }

    @Override
    public String toRasql() {
        return TEMPLATE.replace("$coverage", coverage.toRasql()).replace("$intervalList", dimensionIntervalList.toRasql());
    }

    /**
     * Getter for the coverage expression
     *
     * @return
     */
    public CoverageExpression getCoverageExpression() {
        return coverage;
    }

    /**
     * Getter for the dimension interval list
     *
     * @return
     */
    public DimensionIntervalList getDimensionIntervalList() {
        return dimensionIntervalList;
    }

    private final CoverageExpression coverage;
    private final DimensionIntervalList dimensionIntervalList;
    private static String TEMPLATE = "extend($coverage, [$intervalList])";
}
