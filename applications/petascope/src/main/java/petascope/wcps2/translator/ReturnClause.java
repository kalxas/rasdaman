package petascope.wcps2.translator;

/**
 * Translation node from wcps to rasql for the return clause.
 * Example:
 * <code>
 * return $c1 + $c2
 * </code>
 * translates to
 * <code>
 * SELECT c1 + c2
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ReturnClause extends IParseTreeNode {

    public ReturnClause(IParseTreeNode processingExpr) {
        this.processingExpr = processingExpr;
        addChild(processingExpr);
    }

    @Override
    public String toRasql() {
        String template = TEMPLATE.replace("$processingExpression", this.processingExpr.toRasql());
        return template;
    }

    private IParseTreeNode processingExpr;
    private final String TEMPLATE = "SELECT $processingExpression ";

}
