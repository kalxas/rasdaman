package petascope.wcps2.translator;

/**
 * Class to translate a boolean constant, e.g. true or false
 * <p/>
 * <code>
 * true
 * </code>
 * translates to
 * <code>
 * true
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class BooleanConstant extends IParseTreeNode {

    /**
     * Constructor for the class
     *
     * @param truthValue the boolean value in string format
     */
    public BooleanConstant(String truthValue) {
        this.truthValue = truthValue;
    }

    @Override
    public String toRasql() {
        return truthValue;
    }

    public final String truthValue;
}
