package petascope.wcps2.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import petascope.wcps2.error.managed.processing.WCPSProcessingError;
import petascope.wcps2.error.managed.syntax.ErrorRegistry;
import petascope.wcps2.error.managed.syntax.WCPSSyntaxError;

import java.util.Collections;
import java.util.List;

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
        throw new WCPSProcessingError(error.getErrorMessage());
    }
}