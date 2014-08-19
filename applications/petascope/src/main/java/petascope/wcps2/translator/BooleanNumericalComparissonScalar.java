package petascope.wcps2.translator;

/**
 * Translation class for Boolean numerical comparisons.
 * <code>
 * avg($c) > 1
 * </code>
 * <p/>
 * translates to
 * <p/>
 * <code>
 * avg_cells(c) > 1
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class BooleanNumericalComparissonScalar extends IParseTreeNode {


    /**
     * Constructor for the class
     *
     * @param leftComparisonParameter  the left operand
     * @param rightComparisonParameter the right operand
     * @param operator                 the comparisson operator
     */
    public BooleanNumericalComparissonScalar(IParseTreeNode leftComparisonParameter, IParseTreeNode rightComparisonParameter, String operator) {
        this.leftComparisonParameter = leftComparisonParameter;
        this.rightComparisonParameter = rightComparisonParameter;
        this.operator = operator;
        addChild(leftComparisonParameter);
        addChild(rightComparisonParameter);
    }

    @Override
    public String toRasql() {
        return TEMPLATE.replace("$leftOperand", leftComparisonParameter.toRasql())
            .replace("$operator", operator)
            .replace("$rightOperand", rightComparisonParameter.toRasql());
    }

    private final IParseTreeNode leftComparisonParameter;
    private final IParseTreeNode rightComparisonParameter;
    private final String operator;
    private static final String TEMPLATE = " $leftOperand $operator $rightOperand ";
}
