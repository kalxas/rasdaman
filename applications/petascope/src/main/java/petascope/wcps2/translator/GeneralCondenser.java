package petascope.wcps2.translator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

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
    public GeneralCondenser(String operation, ArrayList<IParseTreeNode> axisIterators, IParseTreeNode whereClause, IParseTreeNode values) {
        this.operation = operation;
        for (IParseTreeNode i : axisIterators) {
            this.axisIterators.add(i.toRasql());
        }
        this.values = values;
        this.whereClause = whereClause;
        if (whereClause != null) addChild(whereClause);
        addChild(values);
    }

    @Override
    public String toRasql() {
        String intervals = StringUtils.join(this.axisIterators, INTERVAL_SEPARATOR);
        String template = TEMPLATE.replace("$intervals", intervals).replace("$values", values.toRasql()).replace("$operation", this.operation);
        if (this.whereClause != null) {
            template = template.replace("$whereClause", this.whereClause.toRasql());
        } else {
            template = template.replace("WHERE $whereClause", "");
        }
        return template;
    }

    private String operation;
    private ArrayList<String> axisIterators = new ArrayList<String>();
    private IParseTreeNode values;
    private IParseTreeNode whereClause;
    private final String TEMPLATE = "CONDENSE $operation OVER $intervals WHERE $whereClause USING $values";
    private final String INTERVAL_SEPARATOR = ",";
}
