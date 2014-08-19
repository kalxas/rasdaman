package petascope.wcps2.error.managed.syntax;

/**
 * Syntax error class for missing coverage prefix
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class MissingCoveragePrefixError extends WCPSSyntaxError {

  @Override
  public boolean canHandle() {
    boolean containsCVName = this.message.contains("expecting COVERAGE_VARIABLE_NAME");
    boolean offendingSymbolStartsWithPrefix = this.offendingSymbol.toString().contains("$");
    if (containsCVName && !offendingSymbolStartsWithPrefix) {
      return true;
    }
    return false;
  }

  @Override
  public String getErrorMessage() {
    String error = ERROR_TEMPLATE.replace("$line", Integer.toString(line))
        .replace("$charPositionInLine", Integer.toString(charPositionInLine))
        .replace("$offendingSymbol", offendingSymbol.toString())
        .replace("$parserMessage", message);
    return error;
  }

  public static final String ERROR_TEMPLATE = "A parsing error occurred at position=$line:$charPositionInLine. \n" +
      "Offending symbol is=$offendingSymbol. Parser message=$parserMessage.\nSuggestions: Please check that the coverage " +
      "iterator starts with the correct prefix '$'.";
}
