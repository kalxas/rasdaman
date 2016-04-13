package petascope.exceptions.wcst;

import petascope.exceptions.ExceptionCode;

/**
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class WCSTNoReadPermissionException extends WCSTException {
    public WCSTNoReadPermissionException(String filePath) {
        super(ExceptionCode.InternalComponentError, EXCEPTION_TEXT.replace("$filePath", filePath));
    }

    public static final String EXCEPTION_TEXT = "File $filePath cannot be read. Please check the file's permissions.";
}
