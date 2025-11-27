package ru.mtsbank.soapdummy.mock.exception;

/**
 * Exception, when content from any mocks cannot be read
 */
public class ReadMockContendException extends RuntimeException {

    public ReadMockContendException(String message) {
        super(message);
    }
    public ReadMockContendException(String message, Throwable cause) {
        super(message, cause);
    }
}
