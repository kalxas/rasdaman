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

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import petascope.exceptions.WCPSException;
import petascope.wcps.exception.syntax.ErrorRegistry;
import petascope.wcps.exception.syntax.WCPSSyntaxError;

import java.util.Collections;
import java.util.List;
import petascope.exceptions.ExceptionCode;
import petascope.util.XMLUtil;

/**
 * Listens for errors from the parser and maps them to known error classes.
 * If no error class is found, the parser error message is used to generate a report for the user
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ParserErrorHandler extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        List<String> stack = ((Parser) recognizer).getRuleInvocationStack();
        Collections.reverse(stack);
        ErrorRegistry registry = new ErrorRegistry();
        WCPSSyntaxError error = registry.lookupError(stack, offendingSymbol, line, charPositionInLine, msg);
        // NOTE: the error message contains special character (<, >) so must be enquoted
        String errorMessage = XMLUtil.enquoteCDATA(error.getErrorMessage());
        
        throw new WCPSException(ExceptionCode.WcpsError, errorMessage);
    }
}