package petascope.wcps2.translator;

/**
 * Translation node from wcps axisSpec to rasql
 * Example:
 * <code>
 * x(0:100)
 * </code>
 * translates to
 * <code>
 * x in [0:100]
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class AxisSpec extends IParseTreeNode {

    /**
     * Constructor for the class
     *
     * @param axisName the name of the variable used to iterate
     * @param interval the interval in which the iteration is done
     */
    public AxisSpec(TrimDimensionInterval interval) {
        this.axisName = interval.getAxisName();
        this.trimInterval = interval;
        this.interval = new IntervalExpression(interval.getRawTrimInterval().getLowerLimit(),
                interval.getRawTrimInterval().getUpperLimit());
        addChild(interval);
    }

    /**
     * Returns the axis name
     *
     * @return
     */
    public String getAxisName() {
        return axisName;
    }

    /**
     * Returns the interval to iterate on
     *
     * @return
     */
    public IntervalExpression getInterval() {
        return interval;
    }


    @Override
    public String toRasql() {
        String template = TEMPLATE.replace("$variable", this.axisName).replace("$interval", this.interval.toRasql());
        return template;
    }


    public TrimDimensionInterval getTrimInterval() {
        return trimInterval;
    }

    public void setAxisName(String axisName) {
        this.axisName = axisName;
        this.trimInterval.setAxisName(axisName);
    }

    protected String axisName;
    protected IntervalExpression interval;
    protected TrimDimensionInterval trimInterval;

    private final String TEMPLATE = "$variable in $interval";

}
