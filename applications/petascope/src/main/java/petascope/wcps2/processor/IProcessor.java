package petascope.wcps2.processor;

import petascope.wcps2.metadata.CoverageRegistry;
import petascope.wcps2.translator.IParseTreeNode;

/**
 * Interface for processing classes that have to operate on the tree before it is translated to rasql
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public interface IProcessor {

    /**
     * Process the translation tree modifying its contents
     *
     * @param translationTree  the tree that was created after the query string was parsed
     * @param currentNode      the node that was detected as processable
     * @param coverageRegistry a coverage registry containing the necessary metadata needed to process the tree
     */
    public void process(IParseTreeNode translationTree, IParseTreeNode currentNode, CoverageRegistry coverageRegistry);

    /**
     * Decides if this processor should be applied or not on the translation tree
     *
     * @param currentNode
     * @return
     */
    public boolean canProcess(IParseTreeNode currentNode);

}
