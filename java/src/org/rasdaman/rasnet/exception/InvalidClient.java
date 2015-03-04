package org.rasdaman.rasnet.exception;

/**
 * Created by rasdaman on 2/19/15.
 */
public class InvalidClient extends Exception {
    public InvalidClient() {
    }

    public InvalidClient(String message) {
        super(message);
    }

    public InvalidClient(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidClient(Throwable cause) {
        super(cause);
    }
}
