package ru.mtsbank.soapdummy.mock;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.mtsbank.soapdummy.mock.exception.MockNotFoundException;
import ru.mtsbank.soapdummy.mock.handler.ResponseHandler;
import ru.mtsbank.soapdummy.mock.setting.DummyCacheStorage;
import ru.mtsbank.soapdummy.mock.setting.EndpointMockSetting;
import ru.mtsbank.soapdummy.mock.setting.RequestMockConditions;
import ru.mtsbank.soapdummy.mock.setting.ResponseSetting;
import ru.mtsbank.soapdummy.mock.setting.SpecificMockSetting;
import ru.mtsbank.soapdummy.utils.FileUtils;
import ru.mtsbank.soapdummy.utils.ReplaceUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service for work around the mocked requests
 */
@Slf4j
@AllArgsConstructor
@Service
public class DummyService {

    private final DummyCacheStorage cacheStorage;

    private final ResponseHandler<String, SpecificMockSetting> responseHandler;


    /**
     * Mocking request.
     * Request will mock, with added specific settings, which will be loaded from cache.
     * See {@link EndpointMockSetting} for more understanding mock rules
     *
     * @param httpRequest all info about http request
     * @param requestBody String of request body
     * @return mocked (added bofore in settings) response
     */
    @Nonnull
    public ResponseEntity<String> mockRequest(HttpServletRequest httpRequest, String requestBody) {
        log.info("Start mock request");
        EndpointMockSetting mockSetting = cacheStorage.getServiceSetting(
                        httpRequest.getRequestURI()
                )
                .orElseThrow(() -> new MockNotFoundException(
                        "Mock not found for endpoint: " + httpRequest.getRequestURL().toString()
                ));
        log.info("Found mock setting rule: {}", mockSetting);
        return runMockRules(httpRequest, requestBody, mockSetting);
    }

    @Nonnull
    private ResponseEntity<String> runMockRules(HttpServletRequest httpRequest,
                                                String requestBody,
                                                EndpointMockSetting mockSetting) {
        for (SpecificMockSetting setting : mockSetting.getSpecificMockSettings()) {
            log.info("Trying to map mock settings: {}", setting);

            if (!setting.getEnable()) {
                log.info("Setting is disabled. Skipping...");
                continue;
            }

            RequestMockConditions conditions = setting.getRequestMockConditions();

            if (conditions == null) {
                log.info("Mapping by general conditions for endpoint");
                return executeRules(setting, httpRequest, requestBody);
            }

            if (matchesByRequestBody(requestBody, conditions.getRequestBodyPath())) {
                log.info("Matched by request body condition");
                return executeRules(setting, httpRequest, requestBody);
            }

            if (matchesByContainsValue(httpRequest, requestBody, conditions.getContainsValue())) {
                return executeRules(setting, httpRequest, requestBody);
            }
        }
        log.info("Setting did not match request");
        throw new MockNotFoundException(
                "No enabled or matching mocks found for endpoint: " + httpRequest.getRequestURL()
        );
    }

    private boolean matchesByRequestBody(String requestBody, String expectedBodyPath) {
        if (StringUtils.isBlank(expectedBodyPath) || StringUtils.isBlank(requestBody)) {
            return false;
        }

        String expectedBody = FileUtils.getFileContent(expectedBodyPath);
        return requestBody.equals(expectedBody);
    }

    private boolean matchesByContainsValue(HttpServletRequest httpRequest, String
            requestBody, Set <String> containsValueSet){
        if (containsValueSet == null || containsValueSet.isEmpty()) {
            return false;
        }
        String query = httpRequest.getQueryString();

        //Проверка совпадения по строковому литералу
        boolean containsText = containsValueSet.stream()
                .filter(s -> (StringUtils.isNotBlank(query) && query.contains(s)) ||
                        (StringUtils.isNotBlank(requestBody) && requestBody.contains(s)))
                .peek(s -> log.info("Matched by text: " + s))
                .count() > 0;

        if (containsText) {
            return true;
        }

        //Проверка совпадения по регулярке
        boolean matchesRegex = containsValueSet.stream()
                .filter(s -> (StringUtils.isNotBlank(query) && Pattern.compile(s).matcher(query).find()) ||
                        (StringUtils.isNotBlank(requestBody) && Pattern.compile(s).matcher(requestBody).find()))
                .peek(s -> log.info("Matched by regex: " + s))
                .count() > 0;

        if (matchesRegex) {
            return true;
        }

        return false;
    }

    @Nonnull
    private ResponseEntity<String> executeRules(SpecificMockSetting setting,
                                                HttpServletRequest httpRequest,
                                                String requestBody) {
        log.info("Execute setting rules");
        runExecutionSleepRule(setting);
        ResponseEntity<String> responseEntity = buildResponse(setting, httpRequest, requestBody);
        log.info("Finish mock with returned response: {}", responseEntity);
        return responseEntity;
    }

    /**
     * Run execution sleep rules, which added in settings file
     *
     * @param mockSetting mock setting loaded from file
     */
    private void runExecutionSleepRule (SpecificMockSetting mockSetting) {
        log.info("Run thread sleep execution rule if need");
        if (!mockSetting.getResponseSetting().getSleepRule().getIsEnabled()) {
            log.info("Thread sleep is disabled");
            return;
        }
        log.info("Thread sleep execution rule enabled");
        try {
            long sleepMs = mockSetting.getResponseSetting().getSleepRule().getSleepMs();
            log.info("Thread will be sleep on: {} ms", sleepMs);
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            log.error("Error while execution rule of sleep", e);
            Thread.currentThread().interrupt();
        }
        log.info("Finish thread sleep execution rule");
    }

    /**
     * Build response
     *
     * @param mockSetting setting for mock
     * @return api response
     */
    @Nonnull
    private ResponseEntity<String> buildResponse(SpecificMockSetting mockSetting, HttpServletRequest
            httpRequest, String requestBody) {
        log.info("Build response");
        if (StringUtils.isNotBlank(mockSetting.getResponseSetting().getResponseBodyPath())) {
            return buildResponseWithBody(mockSetting, httpRequest, requestBody);
        } else {
            return ResponseEntity
                    .status(mockSetting.getResponseSetting().getStatusCode())
                    .build();
        }
    }

    /**
     * Build response with contend body
     *
     * @param mockSetting setting for mock
     * @return api response with body
     */
    private ResponseEntity<String> buildResponseWithBody(SpecificMockSetting mockSetting, HttpServletRequest
            httpRequest, String requestBody) {
        // Обработка динамических плейсхолдеров
        if (mockSetting.getResponseSetting().getEnableDynamicResponse()) {
            return responseBuilder(mockSetting.getResponseSetting())
                    .body(responseHandler.execute(mockSetting));
            // Обработка ТИВа
        } else if (Boolean.TRUE.equals(mockSetting.getResponseSetting().getIsTiv())) {
            return responseBuilder(mockSetting.getResponseSetting())
                    //TODO переделать на обработчик ResponseHandler
                    .body(ReplaceUtils.tivReplace(FileUtils.getFileContent(mockSetting.getResponseSetting().getResponseBodyPath()), mockSetting, httpRequest, requestBody));
            // Дефолтная обработка
        } else {
            return responseBuilder(mockSetting.getResponseSetting())
                    .body(FileUtils.getFileContent(mockSetting.getResponseSetting().getResponseBodyPath()));
        }
    }

    private ResponseEntity.BodyBuilder responseBuilder(ResponseSetting responseSetting) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(responseSetting.getStatusCode());
        MediaType contentType = getContentType(responseSetting);
        if (contentType != null) {
            builder.contentType(contentType);
        }
        return builder;
    }

    private MediaType getContentType(ResponseSetting responseSetting) {
        if (StringUtils.isNotBlank(responseSetting.getContentType())) {
            return MediaType.parseMediaType(responseSetting.getContentType());
        }

        String responseBodyPath = responseSetting.getResponseBodyPath();
        if (responseBodyPath != null && responseBodyPath.endsWith(".json")) {
            return new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
        }
        if (responseBodyPath != null && responseBodyPath.endsWith(".xml")) {
            return new MediaType(MediaType.TEXT_XML, StandardCharsets.UTF_8);
        }
        return null;
    }
}
