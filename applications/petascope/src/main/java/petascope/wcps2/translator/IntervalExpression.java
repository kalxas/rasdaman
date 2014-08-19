package petascope.wcps2.translator;

/**
 * Translation node from wcps interval  rasql
 * Example:
 * <code>
 * 0:100
 * </code>
 * translates to
 * <code>
 * [0:100]
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class IntervalExpression extends IParseTreeNode {

    /**
     * Constructor for the  class
     *
     * @param low  the lower bound of the interval
     * @param high the upper bound of the interval
     */
    public IntervalExpression(String low, String high) {
        this.low = low;
        this.high = high;
    }

    @Override
    public String toRasql() {
        String template = TEMPLATE.replace("$low", this.low).replace("$high", this.high);
        return template;
    }

    /**
     * Returns the lower bound of the interval
     *
     * @return
     */
    public String getLowerBound() {
        return this.low;
    }

    /**
     * Returns the upper bound of the interval
     *
     * @return
     */
    public String getUpperBound() {
        return this.high;
    }

    /**
     * Returns the number of cells in the interval
     *
     * @return
     */
    public Integer cellCount() {
        return Integer.valueOf(this.high) - Integer.valueOf(this.low) + 1;
    }

    private String low;
    private String high;
    private String TEMPLATE = "[$low:$high]";
}
