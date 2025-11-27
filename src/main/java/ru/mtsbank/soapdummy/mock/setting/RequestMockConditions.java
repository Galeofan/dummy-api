package ru.mtsbank.soapdummy.mock.setting;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import javax.annotation.Nullable;
import java.beans.ConstructorProperties;
import java.util.Set;

/**
 * Conditions for request mapping
 */
@Value
@Getter
@EqualsAndHashCode
public class RequestMockConditions {

    /**
     * Mocked request body
     */
    @Nullable
    String requestBodyPath;

    /**
     * Some value or values in String format, which may be contained in request
     */
    @Nullable
    Set<String> containsValue;

    @ConstructorProperties({
        "requestBodyPath",
        "containsValue"
    })
    public RequestMockConditions(@Nullable String requestBodyPath, @Nullable Set<String> containsValue) {
        this.requestBodyPath = requestBodyPath;
        this.containsValue = containsValue;
    }
}
