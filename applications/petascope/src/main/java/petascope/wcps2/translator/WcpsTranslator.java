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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import petascope.wcps2.error.managed.processing.WCPSProcessingError;
import petascope.wcps2.metadata.CoverageRegistry;
import petascope.wcps2.parser.ParserErrorHandler;
import petascope.wcps2.parser.wcpsEvaluator;
import petascope.wcps2.parser.wcpsLexer;
import petascope.wcps2.parser.wcpsParser;
import petascope.wcps2.processor.ProcessorRegistry;

/**
 * Class to translate a wcps query into a rasql query using the antlr generated parser and
 * the translation classes in this package
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class WcpsTranslator {

    /**
     * Constructor for the class
     *
     * @param wcpsQuery the query to be translated
     */
    public WcpsTranslator(String wcpsQuery) {
        this.wcpsQuery = wcpsQuery;
    }

    /**
     * Translates the wcps query into a rasql query
     *
     * @return the translated query
     */
    public String translate() throws WCPSProcessingError {
        return translateTreeToRasql(preprocessTree(getTranslationTree(wcpsQuery)));
    }

    /**
     * Converts the wcps query into a translation tree that can be used to generate a
     * rasql query
     *
     * @param wcpsQuery the query to be translated
     * @return a translation tree
     */
    private IParseTreeNode getTranslationTree(String wcpsQuery) {
        ANTLRInputStream input = new ANTLRInputStream(wcpsQuery);
        wcpsLexer lexer = new wcpsLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        wcpsParser parser = new wcpsParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(new ParserErrorHandler());
        ParseTree parseTree = parser.wcpsQuery();
        wcpsEvaluator evaluator = new wcpsEvaluator(coverageRegistry);
        IParseTreeNode translationTree = evaluator.visit(parseTree);
        return translationTree;
    }

    /**
     * Translates a translation tree to rasql query by calling the toRasql method.
     *
     * @param translationTree
     * @return
     */
    private String translateTreeToRasql(IParseTreeNode translationTree) throws WCPSProcessingError {
        String rasqlQuery = translationTree.toRasql();
        return rasqlQuery;
    }

    /**
     * Preprocesses the tree before it is translated to rasql
     *
     * @param translationTree
     * @return
     */
    private IParseTreeNode preprocessTree(IParseTreeNode translationTree) {
        final ProcessorRegistry processorRegistry = new ProcessorRegistry(translationTree, coverageRegistry);
        processorRegistry.runProcessors();
        return translationTree;
    }

    private final String wcpsQuery;
    private final CoverageRegistry coverageRegistry = new CoverageRegistry();
}
