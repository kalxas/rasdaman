package org.rasdaman.rasnet.exception;

/**
 * Created by rasdaman on 2/20/15.
 */
public class AlreadyRunningException extends Exception {
    public AlreadyRunningException() {
    }

    public AlreadyRunningException(String message) {
        super(message);
    }

    public AlreadyRunningException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyRunningException(Throwable cause) {
        super(cause);
    }
}
