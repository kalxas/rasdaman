package petascope.wcps2.error.managed.syntax;

/**
 * A general parsing error for cases where we can't present a better solution.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class GeneralParsingError extends WCPSSyntaxError {

  @Override
  public boolean canHandle() {
    return true;
  }

  @Override
  public String getErrorMessage() {
    String error = ERROR_TEMPLATE.replace("$line", Integer.toString(line))
        .replace("$charPositionInLine", Integer.toString(charPositionInLine))
        .replace("$offendingSymbol", offendingSymbol.toString())
        .replace("$parserMessage", message);
    return error;
  }

  public static final String ERROR_TEMPLATE = "A parsing error occurred at position=$line:$charPositionInLine. Offending symbol is=$offendingSymbol. Parser message=$parserMessage.";
}
