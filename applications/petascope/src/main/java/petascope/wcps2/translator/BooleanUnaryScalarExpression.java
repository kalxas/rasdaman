package petascope.wcps2.translator;

/**
 * Translation class for boolean unary scalar expression.
 * Example
 * <code>
 *     NOT(avg_cells(c) > 10)
 * </code>
 * translates to
 * <code>
 *     not(avg_cells(c) > 10)
 * </code>
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class BooleanUnaryScalarExpression extends IParseTreeNode {
    public BooleanUnaryScalarExpression(String operand, IParseTreeNode scalarExpression) {
        this.operand = operand;
        this.scalarExpression = scalarExpression;
        addChild(scalarExpression);
    }

    @Override
    public String toRasql() {
        return TEMPLATE.replace("$operand", operand).replace("$scalarExpression", scalarExpression.toRasql());
    }

    private final String operand;
    private final IParseTreeNode scalarExpression;
    private static final String TEMPLATE = "$operand($scalarExpression)";
}
