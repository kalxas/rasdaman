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

import petascope.wcps2.error.managed.processing.WCPSProcessingError;

/**
 * Translation node from wcps axis iterator to rasql
 * Example:
 * <code>
 * x in x(0:100)
 * </code>
 * translates to
 * <code>
 * x in [0:100]
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class AxisIterator extends AxisSpec {
    /**
     * Constructor for the class
     *
     * @param variableName the name of the variable used to iterate
     * @param axisName     the name of the axis on which to iterate
     * @param interval     the interval on which to iterate
     */
    public AxisIterator(CoverageExpressionVariableName variableName, TrimDimensionInterval trimInterval) {
        super(trimInterval);
        this.interval = new IntervalExpression(trimInterval.getRawTrimInterval().getLowerLimit(),
                trimInterval.getRawTrimInterval().getUpperLimit());
        this.variableName = variableName;
        addChild(interval);
    }

    @Override
    public String toRasql() throws WCPSProcessingError {
        String template = TEMPLATE.replace("$variableName", this.variableName.toRasql()).replace("$interval", this.trimInterval.toRasql());
        return template;
    }


    /**
     * Returns the iterator name
     *
     * @return
     */
    public CoverageExpressionVariableName getVariableName() {
        return variableName;
    }

    private CoverageExpressionVariableName variableName;
    private final String TEMPLATE = "$variableName in [$interval]";
}
