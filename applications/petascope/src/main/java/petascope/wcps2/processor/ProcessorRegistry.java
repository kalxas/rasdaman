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

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for the processors that can be applied to a translation tree
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ProcessorRegistry {

    /**
     * Constructor for the class
     *
     * @param translationTree  the translation tree
     * @param coverageRegistry the coverage registry containing the needed metadata for processing
     */
    public ProcessorRegistry(IParseTreeNode translationTree, CoverageRegistry coverageRegistry) {
        this.translationTree = translationTree;
        this.coverageRegistry = coverageRegistry;
        registerProcessors();
    }

    /**
     * Runs all registered processors on the translation tree
     */
    public void runProcessors() {
        List<IParseTreeNode> nodes = translationTree.getDescendants();
        for (IParseTreeNode node : nodes) {
            for (IProcessor processor : processors) {
                if (processor.canProcess(node)) {
                    processor.process(translationTree, node, coverageRegistry);
                }
            }
        }
    }

    /**
     * Registers all processors that need to be called when translating
     */
    private void registerProcessors() {
        processors.add(new CrsSubsetComputer());
        processors.add(new PetascopeAxesToRasdamanAxesTranslator());
        processors.add(new IntervalVariablesTranslator());
    }

    private final List<IProcessor> processors = new ArrayList<IProcessor>();
    private final IParseTreeNode translationTree;
    private final CoverageRegistry coverageRegistry;
}
