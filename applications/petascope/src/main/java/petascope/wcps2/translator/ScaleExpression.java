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
 * Class to translate a scale wcps expression into rasql
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ScaleExpression extends CoverageExpression {


    /**
     * Constructor for the class
     *
     * @param coverageExpression the coverage expression
     * @param dimensionIntervals the dimension intervals
     */
    public ScaleExpression(CoverageExpression coverageExpression, DimensionIntervalList dimensionIntervals) {
        this.dimensionIntervals = dimensionIntervals;
        this.coverageExpression = coverageExpression;
        setCoverage(coverageExpression.getCoverage());
        addChild(coverageExpression);
        addChild(dimensionIntervals);
    }

    /**
     * Returns the dimension intervals of the scale operation
     *
     * @return
     */
    public DimensionIntervalList getDimensionIntervals() {
        return dimensionIntervals;
    }

    @Override
    public String toRasql() {
        return TEMPLATE.replace("$coverageExpression", coverageExpression.toRasql()).replace("$dimensionIntervalList", dimensionIntervals.toRasql());
    }

    private final DimensionIntervalList dimensionIntervals;
    private final CoverageExpression coverageExpression;
    private final String TEMPLATE = "SCALE($coverageExpression, [$dimensionIntervalList])";
}
