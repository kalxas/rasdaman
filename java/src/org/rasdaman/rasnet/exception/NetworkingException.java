package org.rasdaman.rasnet.exception;

/**
 * Created by rasdaman on 2/23/15.
 */
public class NetworkingException extends Exception {
    public NetworkingException() {
    }

    public NetworkingException(String message) {
        super(message);
    }

    public NetworkingException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkingException(Throwable cause) {
        super(cause);
    }
}
