package petascope.wcps2.translator;

import petascope.wcps2.metadata.Coverage;
import petascope.wcps2.metadata.CoverageRegistry;

/**
 * Class to translate a coverage variable name
 * <code>
 * $c
 * </code>
 * translates to
 * <code>
 * c
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageExpressionVariableName extends CoverageExpression {

    /**
     * Constructor for the class
     *
     * @param coverageVariableName the variable name
     */
    public CoverageExpressionVariableName(String coverageVariableName, CoverageRegistry coverageRegistry) {
        this.coverageVariableName = coverageVariableName;
        loadCoverage(coverageRegistry);
    }

    @Override
    public String toRasql() {
        return coverageVariableName.replace("$", "");
    }

    @Override
    protected String nodeInformation() {
        return new StringBuilder("(").append(coverageVariableName).append(")").toString();
    }

    private void loadCoverage(CoverageRegistry registry) {
        setCoverage(registry.getCoverageByAlias(coverageVariableName));
    }

    private final String coverageVariableName;
}
