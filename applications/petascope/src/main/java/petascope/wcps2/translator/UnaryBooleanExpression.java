package petascope.wcps2.translator;

/**
 * Translation node from wcps to rasql for unary boolean expressions.
 * Example:
 * <code>
 * not($c1)
 * </code>
 * translates to
 * <code>
 * not(c1)
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class UnaryBooleanExpression extends IParseTreeNode {

  public UnaryBooleanExpression(IParseTreeNode coverageExp){
    this.coverageExp = coverageExp;
    this.scalarExp  = null;
      addChild(coverageExp);
  }

  public UnaryBooleanExpression(IParseTreeNode coverageExp, IParseTreeNode scalarExp){
    this.coverageExp = coverageExp;
    this.scalarExp = scalarExp;
  }

  @Override
  public String toRasql() {
    String template;
    //if scalarExp exists, we deal with a bit operation
    if(this.scalarExp != null){
      template = TEMPLATE_BIT.replace("$coverageExp", this.coverageExp.toRasql()).replace("$scalarExp", this.scalarExp.toRasql());
    }
    else{
      //not expression
      template = TEMPLATE_NOT.replace("$coverageExp", this.coverageExp.toRasql());
    }
    return template;
  }

  private IParseTreeNode coverageExp;
  private IParseTreeNode scalarExp;
  private final String TEMPLATE_NOT = "NOT($coverageExp)";
  private final String TEMPLATE_BIT = "BIT($coverageExp, $scalarExp)";
}
