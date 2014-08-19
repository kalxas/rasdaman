package petascope.wcps2.translator;

import org.apache.commons.lang3.StringUtils;
import petascope.wcps2.error.managed.processing.WCPSProcessingError;

import java.util.ArrayList;

/**
 * Translation node from wcps coverage list to rasql for the FOR clause.
 * Example:
 * <code>
 * for $c1 in COL1
 * for $c2 in COL2
 * for $c3 in COL3
 * </code>
 * translates to
 * <code>
 * FROM COL1 as c1, COL2 as c2, COL3 as c3
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ForClauseList extends IParseTreeNode {

    /**
     * Constructor for the class
     *
     * @param forClauses a list of the for clauses
     * @throws WCPSProcessingError
     */
    public ForClauseList(ArrayList<IParseTreeNode> forClauses) throws WCPSProcessingError {
        for (IParseTreeNode i : forClauses) {
            this.forClauses.add(i.toRasql());
            addChild(i);
        }
    }

    @Override
    public String toRasql() {
        String template = TEMPLATE.replace("$forClausesList", StringUtils.join(forClauses, FROM_CLAUSE_SEPARATOR));
        return template;
    }


    /**
     * Returns a mutable list of the for clauses
     *
     * @return the list of the for clauses
     */
    public ArrayList<String> getForClauses() {
        return forClauses;
    }

    private final ArrayList<String> forClauses = new ArrayList<String>();
    private static final String TEMPLATE = "FROM $forClausesList";
    private static final String FROM_CLAUSE_SEPARATOR = ",";
}
