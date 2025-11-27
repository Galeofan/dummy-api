package ru.mtsbank.soapdummy.mock.handler;

import java.util.Map;

/**
 * Handler for work with responses
 * @param <R> return type
 * @param <T> param type
 */
public interface ResponseHandler<R, T> {

    /**
     * Execute work with response
     * @param executedRule rule for executing
     * @return finally response
     */
    R execute(T executedRule);
}
