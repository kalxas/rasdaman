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


import java.util.ArrayList;
import java.util.List;

/**
 * Translation node from wcps coverage list to rasql for the general condenser
 * Example:
 * <code>
 * CONDENSE +
 * OVER x x(0:100)
 * WHERE true
 * USING 2
 * </code>
 * translates to
 * <code>
 * CONDENSE +
 * OVER x in [0:100]
 * WHERE true
 * USING 2
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class GeneralCondenser extends IParseTreeNode {

    /**
     * Constructor for the class
     *
     * @param operation     the operation that is being applied
     * @param axisIterators the axes on which the operation will be applied
     * @param whereClause   a where clause that selects the pixels
     * @param values        the values that have to be condensed
     */
    public GeneralCondenser(String operation, ArrayList<AxisIterator> axisIterators, IParseTreeNode whereClause, IParseTreeNode values) {
        this.operation = operation;
        this.axisIterators = axisIterators;
        for (AxisIterator i : axisIterators) {
            //add as child
            addChild(i);
            //keep track of the variable names
            this.axisIteratorVariableNames.add(i.getVariableName().getCoverageVariableName());
        }
        this.values = values;
        this.whereClause = whereClause;
        if (whereClause != null) addChild(whereClause);
        addChild(values);
    }

    @Override
    public String toRasql() {
        List<TrimDimensionInterval> trimIntervals = new ArrayList<TrimDimensionInterval>(axisIterators.size());
        String usedVariable = "";
        for (AxisIterator i : axisIterators) {
            if(usedVariable.isEmpty()){
                usedVariable = i.getVariableName().toRasql();
            }
            trimIntervals.add(i.getTrimInterval());
        }
        dimensionIntervalList = new DimensionIntervalList(trimIntervals);
        String template = TEMPLATE.replace("$iter", usedVariable).replace("$intervals", dimensionIntervalList.toRasql()).
                replace("$values", values.toRasql()).replace("$operation", this.operation);
        if (this.whereClause != null) {
            template = template.replace("$whereClause", this.whereClause.toRasql());
        } else {
            template = template.replace("WHERE $whereClause", "");
        }
        return template;
    }

    public ArrayList<String> getAxisIteratorVariableNames() {
        return axisIteratorVariableNames;
    }

    public IParseTreeNode getValues() {
        return values;
    }

    public ArrayList<AxisIterator> getAxisIterators() {
        return axisIterators;
    }

    private ArrayList<AxisIterator> axisIterators;
    private String operation;
    private DimensionIntervalList dimensionIntervalList;
    private ArrayList<String> axisIteratorVariableNames = new ArrayList<String>();
    private IParseTreeNode values;
    private IParseTreeNode whereClause;
    private final static String TEMPLATE = "CONDENSE $operation OVER $iter in [$intervals] WHERE $whereClause USING $values";
    private final static String INTERVAL_SEPARATOR = ",";
}
