package ru.mtsbank.soapdummy.mock.setting;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import javax.annotation.Nonnull;
import java.beans.ConstructorProperties;
import java.util.Set;

/**
 * Mock data for specific endpoint
 */
@Value
@Getter
@EqualsAndHashCode
public class EndpointMockSetting {

    /**
     * Mocked service name
     */
    @Nonnull
    String serviceName;
    /**
     * Mocked service uri, without host
     */
    @Nonnull
    String endpointUrl;
    /**
     * Enabled flag (if {@code enabled=false} mock will invalidate in cache )
     */
    @Nonnull
    Boolean enable;
    /**
     * List of rules for endpoint
     */
    @Nonnull
    Set<SpecificMockSetting> specificMockSettings;

    @ConstructorProperties({
        "serviceName",
        "endpointUrl",
        "enable",
        "specificMockSettings"
    })
    public EndpointMockSetting(@Nonnull String serviceName,
                               @Nonnull String endpointUrl,
                               @Nonnull Boolean enable,
                               @Nonnull Set<SpecificMockSetting> specificMockSettings) {
        this.serviceName = serviceName;
        this.endpointUrl = endpointUrl;
        this.enable = enable;
        this.specificMockSettings = specificMockSettings;
    }
}
