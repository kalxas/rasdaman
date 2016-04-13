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
 * Translation class for trim expression in wcps.
 * <code>
 * $c[x(0:10),y(0:100)]
 * </code>
 * <p/>
 * translates to
 * <p/>
 * <code>
 * c[0:10,0:100]
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class TrimExpression extends CoverageExpression {

    public TrimExpression(IParseTreeNode coverageExpression, DimensionIntervalList dimensionIntervalList) {
        this.coverageExpression = coverageExpression;
        this.dimensionIntervalList = dimensionIntervalList;
        addChild(coverageExpression);
        addChild(dimensionIntervalList);
        setCoverage(((CoverageExpression) coverageExpression).getCoverage());
    }

    @Override
    public String toRasql() {
        return TEMPLATE.replace("$covExp", coverageExpression.toRasql()).replace("$dimensionIntervalList", dimensionIntervalList.toRasql());
    }

    /**
     * Returns the coverage expression used in  the trim operation
     *
     * @return the coverage expression
     */
    public CoverageExpression getCoverageExpression() {
        return (CoverageExpression) coverageExpression;
    }

    /**
     * Returns all the dimension intervals for the subset
     *
     * @return the dimension interval list
     */
    public DimensionIntervalList getDimensionIntervalList() {
        return dimensionIntervalList;
    }

    private final IParseTreeNode coverageExpression;
    private final DimensionIntervalList dimensionIntervalList;
    private final static String TEMPLATE = "$covExp[$dimensionIntervalList]";
}
