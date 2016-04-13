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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * Translation node from wcps coverageConstant to  rasql
 * Example:
 * <code>
 * COVERAGE m
 * OVER x(0:1), y(2:4)
 * VALUES <1;2;3;4;5>
 * </code>
 * translates to
 * <code>
 * <[0:1,2:4] 1, 2; 3,4,5>
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageConstant extends CoverageBuilder {

    /**
     * Constructor for the class
     *
     * @param axisIterators the axes for this coverage
     * @param constantList  a list of constants that will form the data of the coverage
     */
    public CoverageConstant(String coverageName, ArrayList<AxisIterator> axisIterators, ArrayList<String> constantList) {
        this.coverageName = coverageName;
        this.axisIterators = axisIterators;
        this.constantList = constantList;
        constructCoverageMetadata();
    }

    @Override
    public String toRasql() {
        this.preprocessAxis();
        String intervals = StringUtils.join(this.translatedIntervalList, ",");
        ArrayList<String> constantsByDimension = new ArrayList<String>();
        Integer offset = 0;
        for (Integer i : translatedIntervalCounts) {
            //every count tells the number of cells per dimension
            ArrayList<String> currentDimensionConstants = new ArrayList<String>();
            for (Integer j = offset; j < i; j++) {
                currentDimensionConstants.add(this.constantList.get(j));
            }
            //increase the offset in the constantsList
            offset += i;
            //add the current dimension
            constantsByDimension.add(StringUtils.join(currentDimensionConstants, ","));
        }
        return TEMPLATE.replace("$intervals", intervals).replace("$constants", StringUtils.join(constantsByDimension, ";"));
    }


    /**
     * Translates the values in rasql format
     */
    private void preprocessAxis() {
        for (IParseTreeNode i : axisIterators) {
            IntervalExpression currentInterval = ((AxisSpec) i).getInterval();
            String lowerBound = currentInterval.getLowerBound();
            String upperBound = currentInterval.getUpperBound();
            this.translatedIntervalList.add(lowerBound + ":" + upperBound);
            this.translatedIntervalCounts.add(currentInterval.cellCount());
        }
    }

    private ArrayList<String> constantList;
    private ArrayList<String> translatedIntervalList = new ArrayList<String>();
    private ArrayList<Integer> translatedIntervalCounts = new ArrayList<Integer>();
    private static final String TEMPLATE = "<[$intervals] $constants>";
}
