package petascope.wcps2.translator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Translates a list of dimension intervals
 * <code>
 * [x(0:10), y(0:100)]
 * </code>
 * translates to
 * <code>
 * 0:10,0:100
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class DimensionIntervalList extends IParseTreeNode {

    /**
     * Constructor for the class
     *
     * @param intervals a list of trim intervals
     */
    public DimensionIntervalList(List<TrimDimensionInterval> intervals) {
        this.intervals = intervals;
        for(TrimDimensionInterval interval: intervals){
            addChild(interval);
        }
    }

    @Override
    public String toRasql() {
        List<String> dimensionIntervalsString = new ArrayList<String>(intervals.size());
        Collections.sort(intervals);
        for (TrimDimensionInterval interval : intervals) {
            dimensionIntervalsString.add(interval.toRasql());
        }
        return StringUtils.join(dimensionIntervalsString, ",");
    }

    /**
     * Returns a mutable list of the trim intervals
     *
     * @return the trim intervals
     */
    public List<TrimDimensionInterval> getIntervals() {
        return intervals;
    }


    @Override
    protected String nodeInformation() {
        return new StringBuilder("(").append(intervals.toString()).append(")").toString();
    }

    private final List<TrimDimensionInterval> intervals;
}
