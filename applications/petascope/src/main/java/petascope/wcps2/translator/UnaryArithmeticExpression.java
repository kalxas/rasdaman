package petascope.wcps2.translator;

/**
 * Translation node from wcps unary arithmetic expression to rasql
 * Example:
 * <code>
 * abs($c1)
 * </code>
 * translates to
 * <code>
 * abs(c1)
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class UnaryArithmeticExpression extends IParseTreeNode {

    public UnaryArithmeticExpression(String operator, IParseTreeNode coverageExpr) {
        this.operator = operator;
        this.coverageExpr = coverageExpr;
        addChild(coverageExpr);
    }

    @Override
    public String toRasql() {
        String template = TEMPLATE.replace("$coverage", this.coverageExpr.toRasql());
        //real and imaginary translate to postfix operations in rasql
        //yielding .re and .im
        if (this.operator.toLowerCase().equals(POST_REAL) || this.operator.toLowerCase().equals(POST_IMAGINARY)) {
            template = template.replace("$preOperator", "").replace("$postOperator", "." + this.operator);
        } else {
            template = template.replace("$preOperator", this.operator + "(").replace("$postOperator", ")");
        }
        return template;
    }

    private String operator;
    private IParseTreeNode coverageExpr;
    private final String TEMPLATE = "$preOperator $coverage $postOperator";
    private final String POST_REAL = "re";
    private final String POST_IMAGINARY = "im";
}
