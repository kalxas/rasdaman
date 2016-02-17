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

import java.util.*;

/**
 * Translation node from wcps coverage list to rasql for the coverage constructor
 * Example:
 * <code>
 * COVERAGE myCoverage
 * OVER x x(0:100)
 * VALUES 200
 * </code>
 * translates to
 * <code>
 * MARRAY x in [0:100]
 * VALUES 200
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageConstructor extends CoverageBuilder {

    /**
     * Constructor for the class
     *
     * @param coverageName  the coverage name
     * @param axisIterators the iterators to be applied to the coverage
     * @param values        the values to build the coverage
     */
    public CoverageConstructor(String coverageName, ArrayList<AxisIterator> axisIterators, IParseTreeNode values) {
        this.coverageName = coverageName;
        this.axisIterators = axisIterators;
        for (AxisIterator i : axisIterators) {
            axisIteratorVariableNames.add(i.getVariableName().getCoverageVariableName());
        }
        this.values = values;
        addChild(values);
        constructCoverageMetadata();
    }

    @Override
    public String toRasql() {
        String usedVariable = "";
        List<TrimDimensionInterval> trimIntervals = new ArrayList<TrimDimensionInterval>(axisIterators.size());
        for (AxisIterator i : axisIterators) {
            if(usedVariable.isEmpty()){
                usedVariable = i.getVariableName().toRasql();
            }
            trimIntervals.add(i.getTrimInterval());
        }
        DimensionIntervalList dimensionIntervalList = new DimensionIntervalList(trimIntervals);
        String template = TEMPLATE.replace("$iter", usedVariable).
                replace("$intervals", dimensionIntervalList.toRasql()).replace("$values", values.toRasql());
        return template;
    }

    @Override
    protected String nodeInformation() {
        return "(" + coverageName + ")";
    }

    private IParseTreeNode values;
    private final String TEMPLATE = "MARRAY $iter in [$intervals] VALUES $values";
    private final String INTERVAL_SEPARATOR = ",";
    private ArrayList<String> axisIteratorVariableNames = new ArrayList<String>();

    public ArrayList<String> getAxisIteratorVariableNames() {
        return axisIteratorVariableNames;
    }

    public IParseTreeNode getValues() {
        return values;
    }
}
