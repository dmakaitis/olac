package org.olac.reservation.exception;

public class OlacException extends RuntimeException {

    public OlacException(String message) {
        super(message);
    }

    public OlacException(String message, Throwable cause) {
        super(message, cause);
    }

}
