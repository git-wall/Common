package org.app.common.toml;

/**
 * Exception thrown when there is an error loading or parsing a TOML file.
 */
public class TomlException extends RuntimeException {

    private static final long serialVersionUID = 7551951990638928468L;

    /**
     * Creates a new TomlException with the given message.
     *
     * @param message the error message
     */
    public TomlException(String message) {
        super(message);
    }

    /**
     * Creates a new TomlException with the given message and cause.
     *
     * @param message the error message
     * @param cause   the cause of the exception
     */
    public TomlException(String message, Throwable cause) {
        super(message, cause);
    }
}