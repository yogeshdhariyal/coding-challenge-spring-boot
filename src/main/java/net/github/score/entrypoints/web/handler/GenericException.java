package net.github.score.entrypoints.web.handler;

public class GenericException extends RuntimeException {

    public GenericException(final String message) {
        super(message);
    }

    public GenericException(String message, Throwable cause) {
        super(message, cause);
    }
}