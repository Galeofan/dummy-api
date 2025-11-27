package ru.mtsbank.soapdummy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Config class for application
 */
@Configuration
@EnableScheduling
@PropertySource(value = "file:${app_properties_filepath}", ignoreResourceNotFound = false)
@ConfigurationProperties
public class AppConfig {
}

