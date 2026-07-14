package ru.mtsbank.soapdummy.mock.setting;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import org.springframework.util.CollectionUtils;
import ru.mtsbank.soapdummy.mock.exception.ReadMockContendException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.ConstructorProperties;
import java.util.List;

/**
 * Mocked response data
 */
@Value
@Getter
@EqualsAndHashCode
public class ResponseSetting {

    /**
     * Mocked sleep behavior
     */
    @Nonnull
    RuntimeSleepRule sleepRule;

    /**
     * Mocked return status code
     */
    @Nonnull
    Integer statusCode;

    /**
     * Mocked response body path
     */
    @Nullable
    String responseBodyPath;

    /**
     * Flag for enabling dynamic response
     */
    @Nonnull
    Boolean enableDynamicResponse;

    /**
     * Field responses for setting dynamic value by rules
     */
    @Nullable
    List<DynamicResponse> dynamicResponses;

    /**
     * Flag to check need calculate sum and throw-over phoneNumber for SBP TIV
     */
    @Nullable
    Boolean isTiv;

    /**
     * Mocked response Content-Type header value.
     * Examples: application/json;charset=UTF-8, text/xml;charset=UTF-8, application/xml;charset=UTF-8
     */
    @Nullable
    String contentType;

    @ConstructorProperties({
            "sleepRule",
            "statusCode",
            "responseBodyPath",
            "enableDynamicResponse",
            "dynamicResponses",
            "isTiv",
            "contentType"
    })
    public ResponseSetting(@Nonnull RuntimeSleepRule sleepRule,
                           @Nonnull Integer statusCode,
                           @Nullable String responseBodyPath,
                           @Nullable Boolean enableDynamicResponse,
                           @Nullable List<DynamicResponse> dynamicResponses,
                           @Nullable Boolean isTiv,
                           @Nullable String contentType) {
        this.sleepRule = sleepRule;
        this.statusCode = statusCode;
        this.responseBodyPath = responseBodyPath;
        this.enableDynamicResponse = enableDynamicResponse != null ? enableDynamicResponse : false;
        this.isTiv = isTiv;
        this.contentType = contentType;
        if (this.enableDynamicResponse && CollectionUtils.isEmpty(dynamicResponses)) {
            throw new ReadMockContendException("When Dynamic Response enabled, dynamicResponses must be set");
        }
        this.dynamicResponses = dynamicResponses;
    }
}
