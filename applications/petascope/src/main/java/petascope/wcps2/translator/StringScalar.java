package petascope.wcps2.translator;

import petascope.wcps2.metadata.Coverage;

/**
 * Translator class for string scalars
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class StringScalar extends CoverageExpression {

    /**
     * Constructor for the class
     *
     * @param scalar the string scalar
     */
    public StringScalar(String scalar) {
        this.scalar = scalar;
        this.setCoverage(Coverage.DEFAULT_COVERAGE);
    }

    @Override
    public String toRasql() {
        return this.scalar.substring(1, this.scalar.length()-1);
    }

    private final String scalar;
}
