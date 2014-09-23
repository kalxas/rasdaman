package petascope.wcps2.translator;

import petascope.wcps2.error.managed.processing.WCPSProcessingError;
import petascope.wcps2.util.CastDataTypeConverter;

/**
 * Translation node from wcps to rasql for the case expression.
 * Example:
 * <code>
 * (char) $c1
 * </code>
 * translates to
 * <code>
 * (char) c1
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CastExpression extends CoverageExpression {

    public CastExpression(String rangeType, IParseTreeNode coverageExp) {
        this.rangeType = rangeType;
        this.coverageExp = coverageExp;
        addChild(coverageExp);
        setCoverage(((CoverageExpression) coverageExp).getCoverage());
    }

    @Override
    public String toRasql() throws WCPSProcessingError {
        String template = TEMPLATE.replace("$rangeType", CastDataTypeConverter.convert(this.rangeType)).replace("$coverageExp", this.coverageExp.toRasql());
        return template;
    }

    private String rangeType;
    private IParseTreeNode coverageExp;
    private final String TEMPLATE = "($rangeType) $coverageExp";
}
