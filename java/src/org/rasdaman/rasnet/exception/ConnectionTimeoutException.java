package org.rasdaman.rasnet.exception;

/**
 * Created by rasdaman on 2/24/15.
 */
public class ConnectionTimeoutException extends RuntimeException {
    public ConnectionTimeoutException() {
    }

    public ConnectionTimeoutException(String message) {
        super(message);
    }

    public ConnectionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionTimeoutException(Throwable cause) {
        super(cause);
    }
}