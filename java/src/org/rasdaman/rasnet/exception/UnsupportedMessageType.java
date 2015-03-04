package org.rasdaman.rasnet.exception;

/**
 * Created by rasdaman on 2/19/15.
 */
public class UnsupportedMessageType extends Exception {
    public UnsupportedMessageType() {
    }

    public UnsupportedMessageType(String message) {
        super(message);
    }

    public UnsupportedMessageType(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedMessageType(Throwable cause) {
        super(cause);
    }
}
