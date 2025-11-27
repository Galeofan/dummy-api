package ru.mtsbank.soapdummy.mock.setting;

import lombok.Getter;
import lombok.Value;

import javax.annotation.Nonnull;
import java.beans.ConstructorProperties;
import java.util.List;

/**
 * Mock settings data
 */
@Value
@Getter
public class MockSettings {

    /**
     * List of mocked settings for resources
     */
    @Nonnull
    List<EndpointMockSetting> endpointMocks;

    @ConstructorProperties({
        "endpointMocks"
    })
    public MockSettings(@Nonnull List<EndpointMockSetting> mockSettings) {
        this.endpointMocks = mockSettings;
    }
}
