package petascope.wcps2.translator;

import petascope.wcps2.error.managed.processing.CoverageNotFoundException;
import petascope.wcps2.error.managed.processing.WCPSProcessingError;
import petascope.wcps2.metadata.CoverageRegistry;

/**
 * Translation node from wcps to rasql for the for clause.
 * Example:
 * <code>
 * for $c1 in COL1
 * </code>
 * translates to
 * <code>
 * COL1 as c1
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ForClause extends IParseTreeNode {

    /**
     * Constructor for the class
     *
     * @param coverageVariable the coverage variable name
     * @param coverageName     the name of the coverage
     * @param coverageRegistry the coverage registry
     */
    public ForClause(String coverageVariable, String coverageName, CoverageRegistry coverageRegistry) {
        this.coverageIterator = coverageVariable;
        this.coverageName = coverageName;
        this.coverageRegistry = coverageRegistry;
        coverageRegistry.addCoverageMapping(coverageName, coverageVariable);
    }


    @Override
    public String toRasql() {
        checkCorrectness();
        String translatedCoverageIterator = coverageIterator;
        //if the coverageVariable starts with $, remove it to make it valid rasql
        if (coverageIterator.startsWith(COVERAGE_VARIABLE_PREFIX)) {
            translatedCoverageIterator = coverageIterator.replace(COVERAGE_VARIABLE_PREFIX, "");
        }
        String template = TEMPLATE.replace("$iterator", translatedCoverageIterator)
            .replace("$collectionName", coverageName);
        return template;
    }

    /**
     * Checks if the coverage referenced exists and if not throws an error
     *
     * @throws WCPSProcessingError
     */
    private void checkCorrectness() {
        if (!coverageRegistry.coverageExists(coverageName)) {
            throw new CoverageNotFoundException(coverageName);
        }
    }

    @Override
    protected String nodeInformation() {
        return new StringBuilder("(").append(coverageIterator).append(",").append(coverageName).append(")").toString();
    }

    private final String coverageIterator;
    private final String coverageName;
    private final static String TEMPLATE = "$collectionName AS $iterator";
    private final static String COVERAGE_VARIABLE_PREFIX = "$";
    private final CoverageRegistry coverageRegistry;
}
