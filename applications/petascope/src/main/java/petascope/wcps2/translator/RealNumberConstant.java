package petascope.wcps2.translator;

import petascope.wcps2.metadata.Coverage;

/**
 * Translator class for real numbers. The numbers in WCPS correspond to their definition in rasql so no translation
 * to a number format is done, the string is passed upwards.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class RealNumberConstant extends CoverageExpression {

    public RealNumberConstant(String number) {
        this.number = number;
        setCoverage(Coverage.DEFAULT_COVERAGE);
    }

    @Override
    public String toRasql() {
        return number;
    }

    private final String number;
}
