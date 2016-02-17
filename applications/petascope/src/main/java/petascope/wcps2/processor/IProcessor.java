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
