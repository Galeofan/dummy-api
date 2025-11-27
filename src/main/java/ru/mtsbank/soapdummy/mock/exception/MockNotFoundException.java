package ru.mtsbank.soapdummy.mock.exception;

/**
 * Exception, when mock didn't find in cache
 */
public class MockNotFoundException extends RuntimeException {
    public MockNotFoundException(String message) {
        super(message);
    }
}
