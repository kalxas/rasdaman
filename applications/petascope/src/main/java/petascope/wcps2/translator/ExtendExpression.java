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
 * Translator class for the extend operation in wcps
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ExtendExpression extends CoverageExpression {


    /**
     * Constructor for the class
     *
     * @param coverage              the coverage to be extended
     * @param dimensionIntervalList a list of intervals to extend the coverage onto
     */
    public ExtendExpression(CoverageExpression coverage, DimensionIntervalList dimensionIntervalList) {
        this.coverage = coverage;
        this.dimensionIntervalList = dimensionIntervalList;
        setCoverage(coverage.getCoverage());
    }

    @Override
    public String toRasql() {
        return TEMPLATE.replace("$coverage", coverage.toRasql()).replace("$intervalList", dimensionIntervalList.toRasql());
    }

    /**
     * Getter for the coverage expression
     *
     * @return
     */
    public CoverageExpression getCoverageExpression() {
        return coverage;
    }

    /**
     * Getter for the dimension interval list
     *
     * @return
     */
    public DimensionIntervalList getDimensionIntervalList() {
        return dimensionIntervalList;
    }

    private final CoverageExpression coverage;
    private final DimensionIntervalList dimensionIntervalList;
    private static String TEMPLATE = "extend($coverage, [$intervalList])";
}
