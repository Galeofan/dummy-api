package ru.mtsbank.soapdummy.mock.setting;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.ConstructorProperties;

/**
 * Specific conditions for endpoint (ex. for one endpoint, but different requests)
 */
@Value
@Getter
@EqualsAndHashCode
public class SpecificMockSetting {

    /**
     * Enabled flag (if {@code enabled=false} mock will invalidate in cache )
     */
    @Nonnull
    Boolean enable;

    /**
     * Conditions for request mapping
     */
    @Nullable
    RequestMockConditions requestMockConditions;

    /**
     * Mocked response settings
     */
    @Nonnull
    ResponseSetting responseSetting;

    @ConstructorProperties({
        "enable",
        "requestMockConditions",
        "responseSetting"
    })

    public SpecificMockSetting(@Nonnull Boolean enable,
                               @Nullable RequestMockConditions requestMockConditions,
                               @Nonnull ResponseSetting responseSetting) {
        this.enable = enable;
        this.requestMockConditions = requestMockConditions;
        this.responseSetting = responseSetting;
    }
}
