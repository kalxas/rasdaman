package petascope.wcps2.translator;

/**
 * Translator class for complex numbers.
 *
 * <code>
 *   (2,4)
 * </code>
 *
 * translates to
 *
 * <code>
 *   complex(2,5)
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ComplexNumberConstant extends IParseTreeNode {

  public ComplexNumberConstant(String re, String im) {
    this.re = re;
    this.im = im;
  }

  @Override
  public String toRasql() {
    return TEMPLATE.replace("$re", this.re).replace("$im", this.im);
  }

  private final String re;
  private final String im;
  private final static String TEMPLATE = "complex($re, $im)";
}
