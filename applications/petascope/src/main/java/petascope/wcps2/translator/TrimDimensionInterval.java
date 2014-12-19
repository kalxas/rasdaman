package petascope.wcps2.translator;

import petascope.wcps2.metadata.Interval;

/**
 * Class to translate trimming operations to rasql
 * <code>
 * x,http://crs.com/def/ESPG4236(4.56:2.32)
 * </code>
 * translates to
 * <code>
 * 4.56:2.32
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class TrimDimensionInterval extends IParseTreeNode implements Comparable {

    /**
     * Constructor for the class
     *
     * @param axisName      the name of the axis on which the trim operation is made
     * @param crs           the crs of the subset
     * @param rawLowerBound the lower bound of the trim interval
     * @param rawUpperBound the upper bound of the interval
     */
    public TrimDimensionInterval(String axisName, String crs, CoverageExpression rawLowerBound, CoverageExpression rawUpperBound) {
        this.rawLowerBound = rawLowerBound;
        this.rawUpperBound = rawUpperBound;
        this.axisName = axisName;
        this.crs = crs;
        this.rawTrimInterval = new Interval<String>(rawLowerBound.toRasql(), rawUpperBound.toRasql());
        this.trimInterval = new Interval<Long>(Long.MAX_VALUE, Long.MAX_VALUE);
        addChild(rawLowerBound);
        addChild(rawUpperBound);
    }

    /**
     * Returns the raw trim interval
     *
     * @return
     */
    public Interval<String> getRawTrimInterval() {
        return rawTrimInterval;
    }

    /**
     * Sets the numeric trim interval
     *
     * @param trimInterval
     */
    public void setTrimInterval(Interval<Long> trimInterval) {
        this.trimInterval = trimInterval;
    }

    /**
     * Returns the axis on which the trim interval is being done
     *
     * @return
     */
    public String getAxisName() {
        return axisName;
    }

    public void setAxisName(String axisName) {this.axisName = axisName;}
    /**
     * Sets the corresponding array subset position for this coverage axis
     *
     * @return the position in the array subset of this axis
     */
    public int getAxisPosition() {
        return axisPosition;
    }

    /**
     * Sets the corresponding array subset position for this coverage axis
     *
     * @param axisPosition the position in the array subset of this axis
     */
    public void setAxisPosition(int axisPosition) {
        this.axisPosition = axisPosition;
    }

    /**
     * Returns the crs of the trim interval
     * @return
     */
    public String getCrs() {
        return crs;
    }

    /**
     * Compares two trim dimension intervals based on their axis order
     */
    @Override
    public int compareTo(Object o) {
        TrimDimensionInterval other = (TrimDimensionInterval) o;

        if (getAxisPosition() > other.getAxisPosition()) {
            return 1;
        } else if (getAxisPosition() == other.getAxisPosition()) {
            return 0;
        }
        return -1;
    }

    @Override
    public String toRasql() {
        /*if (trimInterval.getLowerLimit() == Long.MAX_VALUE || trimInterval.getUpperLimit() == Long.MAX_VALUE) {
            throw new InvalidCalculatedBoundsException(axisName, new Interval<String>(rawTrimInterval.getLowerLimit(), rawTrimInterval.getUpperLimit()));
        }*/
        String lowerLimit;
        String upperLimit;
        if(rawTrimInterval.isCrsComputable()) {
            //numeric interval
            lowerLimit = rawTrimInterval.getLowerLimit().equals(WHOLE_DIMENSION_SYMBOL) ? WHOLE_DIMENSION_SYMBOL : String.valueOf(trimInterval.getLowerLimit());
            upperLimit = rawTrimInterval.getUpperLimit().equals(WHOLE_DIMENSION_SYMBOL) ? WHOLE_DIMENSION_SYMBOL : String.valueOf(trimInterval.getUpperLimit());
        }
        else{
            //non-numeric interval, just pass the limits as the evaluation result of the coverage expressions they represent
            lowerLimit = rawLowerBound.toRasql();
            upperLimit = rawUpperBound.toRasql();
        }

        return fillTemplate(lowerLimit, upperLimit);
    }

    @Override
    protected String nodeInformation() {
        return new StringBuilder("(").append(rawTrimInterval.getLowerLimit()).append(":").append(rawTrimInterval.getUpperLimit()).append(")").toString();
    }

    /**
     * Fills the right template and returns the result. Differentiates between points and intervals.
     * @param lowerBound
     * @param upperBound
     * @return
     */
    private static String fillTemplate(String lowerBound, String upperBound){
        if(!lowerBound.equals(upperBound) || lowerBound.equals(WHOLE_DIMENSION_SYMBOL)){
            return TEMPLATE.replace("$lowerBound", lowerBound)
                    .replace("$upperBound", upperBound);
        }
        else{
            return TEMPLATE_POINT.replace("$lowerBound", lowerBound);
        }
    }


    public static final String WHOLE_DIMENSION_SYMBOL = "*";

    private CoverageExpression rawLowerBound;
    private CoverageExpression rawUpperBound;
    private int axisPosition = Integer.MAX_VALUE;
    private String axisName;
    private final String crs;
    private final Interval<String> rawTrimInterval;
    private Interval<Long> trimInterval;
    private final static String TEMPLATE = "$lowerBound:$upperBound";
    private final static String TEMPLATE_POINT = "$lowerBound";
}
