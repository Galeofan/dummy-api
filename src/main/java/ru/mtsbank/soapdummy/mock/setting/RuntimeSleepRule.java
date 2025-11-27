package ru.mtsbank.soapdummy.mock.setting;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import javax.annotation.Nonnull;
import java.beans.ConstructorProperties;

/**
 * Sleep rule behavior for mocked resource
 * You can set thread wait for specific resource
 */
@Value
@Getter
@EqualsAndHashCode
public class RuntimeSleepRule {

    /**
     * Enabled flag (if {@code isEnabled=false} - sleep is disabled)
     */
    @Nonnull
    Boolean isEnabled;
    /**
     * Sleep timeout in millis
     */
    @Nonnull
    Long sleepMs;

    @ConstructorProperties({
        "isEnabled",
        "sleepMs"
    })
    public RuntimeSleepRule(@Nonnull Boolean isEnabled, @Nonnull Long sleepMs) {
        this.isEnabled = isEnabled;
        this.sleepMs = sleepMs;
    }
}
