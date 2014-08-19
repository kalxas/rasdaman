package petascope.wcps2.translator;

/**
 * Translation node from wcps to rasql for the where clause.
 * Example:
 * <code>
 * WHERE c.red > 10
 * </code>
 * translates to
 * <code>
 * WHERE c.red > 10
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class WhereClause extends IParseTreeNode {

    public WhereClause(IParseTreeNode booleanExpression) {
        this.booleanExpression = booleanExpression;
        addChild(booleanExpression);
    }

    @Override
    public String toRasql() {
        return TEMPLATE.replace("$booleanExpression", booleanExpression.toRasql());
    }

    private final IParseTreeNode booleanExpression;
    private static final String TEMPLATE = " WHERE $booleanExpression ";
}
