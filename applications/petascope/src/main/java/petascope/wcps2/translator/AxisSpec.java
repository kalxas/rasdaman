/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
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
