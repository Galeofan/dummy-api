package ru.mtsbank.soapdummy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.mtsbank.soapdummy.config.AppConfig;

import java.util.List;

/**
 * Main class
 */
@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
@Slf4j
public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
            .bannerMode(Banner.Mode.OFF)
            .run(args);
        log.info("App started with args: {}", List.of(args));
        log.info("\n============================================================================================= " +
                "Приложение запущено" +
                " =============================================================================================");
    }
}
