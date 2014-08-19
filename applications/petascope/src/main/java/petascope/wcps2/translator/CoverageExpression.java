package petascope.wcps2.translator;

import org.jetbrains.annotations.NotNull;
import petascope.wcps2.metadata.Coverage;

/**
 * Class to represent a node in the tree that is a coverage expression
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public abstract class CoverageExpression extends IParseTreeNode {

    /**
     * Returns the coverage resulting from this operation
     *
     * @return
     */
    @NotNull
    public Coverage getCoverage() {
        return coverage;
    }

    /**
     * Sets the coverage resulting from this operation
     *
     * @param coverage
     * @return
     */
    public void setCoverage(@NotNull Coverage coverage) {
        this.coverage = coverage;
    }

    private Coverage coverage = null;
}
