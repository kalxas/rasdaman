package org.rasdaman.rasnet.exception;

/**
 * Created by rasdaman on 2/20/15.
 */
public class DuplicateService extends Exception {
    public DuplicateService() {
    }

    public DuplicateService(String message) {
        super(message);
    }

    public DuplicateService(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateService(Throwable cause) {
        super(cause);
    }
}