package petascope.wcps2.translator;

/**
 * Translation class for trim expression in wcps.
 * <code>
 * $c[x(0:10),y(0:100)]
 * </code>
 * <p/>
 * translates to
 * <p/>
 * <code>
 * c[0:10,0:100]
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class TrimExpression extends CoverageExpression {

    public TrimExpression(IParseTreeNode coverageExpression, DimensionIntervalList dimensionIntervalList) {
        this.coverageExpression = coverageExpression;
        this.dimensionIntervalList = dimensionIntervalList;
        addChild(coverageExpression);
        addChild(dimensionIntervalList);
        setCoverage(((CoverageExpression) coverageExpression).getCoverage());
    }

    @Override
    public String toRasql() {
        return TEMPLATE.replace("$covExp", coverageExpression.toRasql()).replace("$dimensionIntervalList", dimensionIntervalList.toRasql());
    }

    /**
     * Returns the coverage expression used in  the trim operation
     *
     * @return the coverage expression
     */
    public CoverageExpression getCoverageExpression() {
        return (CoverageExpression) coverageExpression;
    }

    /**
     * Returns all the dimension intervals for the subset
     *
     * @return the dimension interval list
     */
    public DimensionIntervalList getDimensionIntervalList() {
        return dimensionIntervalList;
    }

    private final IParseTreeNode coverageExpression;
    private final DimensionIntervalList dimensionIntervalList;
    private final static String TEMPLATE = "$covExp[$dimensionIntervalList]";
}
