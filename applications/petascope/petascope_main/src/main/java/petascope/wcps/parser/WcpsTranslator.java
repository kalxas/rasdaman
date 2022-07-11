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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.parser;

import petascope.wcps.exception.syntax.ParserErrorHandler;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.wcps.handler.Handler;
import petascope.wcps.result.VisitorResult;

/**
 * Class to translate a wcps query into a rasql query using the antlr generated
 * parser and the translation classes in this package
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class WcpsTranslator {

    @Autowired
    private WcpsEvaluator wcpsEvaluator;

    public WcpsTranslator() {

    }

    /**
     * Translates a wcps query into a single rasql query or value. This method
     * should be called AFTER query rewriting (for multipart).
     *
     * @param wcpsQuery WCPS query to be parsed to rasql query
     * @return the translated query
     * @throws petascope.exceptions.PetascopeException
     */
    public VisitorResult translate(String wcpsQuery) throws WCPSException, PetascopeException {
        //create a evaluator object.
        VisitorResult result = getTranslationTree(wcpsQuery);
        return result;
    }

    /**
     * Converts the wcps query into a translation tree that can be used to
     * generate a rasql query
     *
     * @param wcpsQuery the query to be translated
     * @return a translation tree
     */
    private VisitorResult getTranslationTree(String wcpsQuery) throws PetascopeException {
        ANTLRInputStream input = new ANTLRInputStream(wcpsQuery);
        wcpsLexer lexer = new wcpsLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        wcpsParser parser = new wcpsParser(tokenStream);
        parser.removeErrorListeners();
        // Add a listener to throw WCPSProcessingError when parsing is error
        parser.addErrorListener(new ParserErrorHandler());
        VisitorResult translationTree = null;
        ParseTree parseTree = null;


        // If query cannot be parsed, it is SyntaxError Exception (needed for OGC CITE test)
        try {
            parseTree = parser.wcpsQuery();
        } catch (WCPSException ex) {
            throw new PetascopeException(ExceptionCode.SyntaxError, ex.getMessage(), ex);
        }

        // If query can be parsed, then it can have error in handlers.
        try {
            // When the tree is parsed, it will traverse to each node to evaluate
            // And throw WCPSProcessingError or other kind of Exceptions if possible
            Handler rootHandler = this.wcpsEvaluator.visit(parseTree);
            translationTree = rootHandler.handle();

        } catch (WCPSException ex) {
            throw new PetascopeException(ex.getExceptionCode(), ex.getMessage(), ex);
        }
        return translationTree;
    }
}
