package org.rasdaman.rasnet.exception;

public class RasnetException extends RuntimeException {

    public RasnetException() {
    }

    public RasnetException(String message) {
        super(message);
    }

    public RasnetException(String message, Throwable cause) {
        super(message, cause);
    }

    public RasnetException(Throwable cause) {
        super(cause);
    }

    public RasnetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
