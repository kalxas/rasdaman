package petascope.wcps2.translator;

/**
 * Class that represents a binary scalar expression
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class BinaryScalarExpression extends IParseTreeNode {

    /**
     * Constructor for the class
     *
     * @param firstParameter  the first operand
     * @param operator        the operator of the operation
     * @param secondParameter the second operand
     */
    public BinaryScalarExpression(String firstParameter, String operator, String secondParameter) {
        this.firstParameter = firstParameter;
        this.secondParameter = secondParameter;
        this.operator = operator;
    }

    @Override
    public String toRasql() {
        return firstParameter + operator + secondParameter;
    }

    public final String firstParameter;
    public final String secondParameter;
    public final String operator;
}
