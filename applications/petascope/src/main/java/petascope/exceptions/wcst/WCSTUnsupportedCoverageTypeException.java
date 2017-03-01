package petascope.exceptions.wcst;

import petascope.exceptions.ExceptionCode;

/**
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class WCSTUnsupportedCoverageTypeException extends WCSTException {
    public WCSTUnsupportedCoverageTypeException(String coverageType) {
        super(ExceptionCode.InvalidCoverageType, EXCEPTION_TEXT.replace("$coverageType", coverageType));
    }

    public static final String EXCEPTION_TEXT = "Unsupported coverage type: $coverageType";
}
