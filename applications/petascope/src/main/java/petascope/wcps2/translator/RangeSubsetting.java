package petascope.wcps2.translator;

/**
 * Translation node from wcps to rasql for range subsetting.
 * Example:
 * <code>
 * $c1.red
 * </code>
 * translates to
 * <code>
 * c1.red
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class RangeSubsetting extends CoverageExpression {

    public RangeSubsetting(String rangeType, CoverageExpression coverageExp) {
        this.rangeType = rangeType;
        this.coverageExp = coverageExp;
        addChild(coverageExp);
        setCoverage(coverageExp.getCoverage());
    }

    @Override
    public String toRasql() {
        String template = TEMPLATE.replace("$coverageExp", this.coverageExp.toRasql()).replace("$rangeType", this.rangeType);
        return template;
    }

    private String rangeType;
    private IParseTreeNode coverageExp;
    private final String TEMPLATE = "$coverageExp.$rangeType";
}
