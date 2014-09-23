package petascope.wcps2.translator;

import petascope.wcps.metadata.CoverageInfo;
import petascope.wcps2.error.managed.processing.IncompatibleCoverageExpressionException;
import petascope.wcps2.error.managed.processing.WCPSProcessingError;
import petascope.wcps2.metadata.Coverage;

/**
 * Translation node from wcps coverage list to rasql for the LogicalCOverageExpressions
 * Example:
 * <code>
 * $c1 OR $c2
 * </code>
 * translates to
 * <code>
 * c1 OR c2
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class BinaryCoverageExpression extends CoverageExpression {

    /**
     * Constructor for the class
     *
     * @param firstCoverageExpr  the first coverage expression
     * @param operator           the operator of the expression
     * @param secondCoverageExpr the second coverage expression
     */
    public BinaryCoverageExpression(IParseTreeNode firstCoverageExpr, String operator, IParseTreeNode secondCoverageExpr) {
        this.firstCoverageExpr = (CoverageExpression) firstCoverageExpr;
        this.secondCoverageExpr = (CoverageExpression) secondCoverageExpr;
        this.operator = operator;
        checkConsistency();
        addChild(firstCoverageExpr);
        addChild(secondCoverageExpr);
        createResultingCoverage();
    }

    @Override
    public String toRasql() throws WCPSProcessingError {
        return this.firstCoverageExpr.toRasql() + " " + operator + " " + secondCoverageExpr.toRasql();
    }

    /**
     * Checks the consistency of the operation by verifying if the coverages are compatible
     */
    private void checkConsistency() {
        if (!firstCoverageExpr.getCoverage().isCompatibleWith(secondCoverageExpr.getCoverage())) {
            throw new IncompatibleCoverageExpressionException(firstCoverageExpr.getCoverage(), secondCoverageExpr.getCoverage());
        }
    }

    /**
     * Creates the resulting coverage from the two operands
     */
    private void createResultingCoverage() {
        CoverageInfo covMeta = new CoverageInfo(firstCoverageExpr.getCoverage().getCoverageInfo());
        String coverageName = firstCoverageExpr.getCoverage().getCoverageName() + operator + secondCoverageExpr.getCoverage().getCoverageName();
        setCoverage(new Coverage(coverageName, covMeta, firstCoverageExpr.getCoverage().getCoverageMetadata()));
    }

    private CoverageExpression firstCoverageExpr;
    private CoverageExpression secondCoverageExpr;
    private String operator;
}
