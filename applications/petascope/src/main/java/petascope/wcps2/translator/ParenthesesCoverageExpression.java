package petascope.wcps2.translator;

import petascope.wcps2.metadata.Coverage;

/**
 * Translator class for coverage expressions that are surrounded by parenthesis.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ParenthesesCoverageExpression extends CoverageExpression {

    public ParenthesesCoverageExpression(IParseTreeNode coverageExpression) {
        this.coverageExpression = coverageExpression;
        addChild(coverageExpression);

        //@TODO this should be redone in the grammar as PaenthesesCoverageExpression and ParanthesesScalarExpression to avoid this check
        if (coverageExpression instanceof CoverageExpression) {
            setCoverage(((CoverageExpression) coverageExpression).getCoverage());
        } else {
            setCoverage(Coverage.DEFAULT_COVERAGE);
        }
    }

    @Override
    public String toRasql() {
        return " ( " + coverageExpression.toRasql() + " ) ";
    }

    private final IParseTreeNode coverageExpression;
}
