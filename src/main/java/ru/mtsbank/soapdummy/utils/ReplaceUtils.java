package ru.mtsbank.soapdummy.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math.random.RandomDataImpl;
import ru.mtsbank.soapdummy.mock.setting.DynamicResponse;
import ru.mtsbank.soapdummy.mock.setting.SpecificMockSetting;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static ru.mtsbank.soapdummy.utils.ParseUtils.extractCurrRate;
import static ru.mtsbank.soapdummy.utils.ParseUtils.extractFieldFromJson;
import static ru.mtsbank.soapdummy.utils.ParseUtils.extractFieldFromQueryParams;

@Slf4j
public class ReplaceUtils {

    public static String tivReplace(String responseFile, SpecificMockSetting mockSetting, HttpServletRequest httpRequest, String requestBody) {
        Map<String, String> extractedFields = new HashMap<>();
        // Для УниверсалБанка
        if (Boolean.TRUE.equals(mockSetting.getResponseSetting().getIsTiv()) && StringUtils.isNotBlank(requestBody)) {
            log.info("Выполняю логику ТИВа для Универсал Банка");
            log.info("Извлекаю поля из JSON запроса");
            // Извлечение нужных полей из запроса
            extractedFields = ParseUtils.extractFieldsFromJson(requestBody);
            if (extractedFields.values().stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("Поля не найдены");
            }
            extractedFields.forEach((k, v) -> log.info(String.format("%s = %s", k, v)));
            if ("RUB".equalsIgnoreCase(extractFieldFromJson(requestBody, "currency"))) {
                log.info("Определил тип запроса: CA2DA");
                // Извлечение курса из файла ответа
                double rate = extractCurrRate(responseFile);
                log.info("Определил курс: " + rate);
                //Расчёт credit_amount
                String amountStr = extractedFields.get("amount");
                try {
                    double amount = Double.parseDouble(amountStr);
                    double creditAmount = amount / rate;
                    String formattedCreditAmount = String.format(Locale.US,"%.0f", creditAmount);
                    extractedFields.put("credit_amount", formattedCreditAmount);
                    log.info("Посчитал credit_amount: " + formattedCreditAmount);
                } catch (NumberFormatException e) {
                    log.warn("Невозможно преобразовать amount в число:" + amountStr, e);
                }
            } else {
                log.info("Определил тип запроса: DA2CA");
                double rate = extractCurrRate(responseFile);
                log.info("Определил курс: " + rate);
                String amountStr = extractedFields.get("amount");
                try {
                    double creditAmount = Double.parseDouble(amountStr);
                    String formattedCreditAmount = String.format(Locale.US,"%.0f", creditAmount);
                    double amount = creditAmount * rate;
                    String formattedAmount = String.format(Locale.US,"%.0f", amount);
                    extractedFields.put("credit_amount", formattedCreditAmount);
                    extractedFields.put("amount", formattedAmount);
                    log.info("Заменил credit_amount на amount из запроса: " + formattedCreditAmount);
                    log.info("Посчитал amount: " + formattedAmount);
                } catch (NumberFormatException e) {
                    log.warn("Невозможно преобразовать amount в число:" + amountStr, e);
                }
            }
            // Для остальных
        } else if (Boolean.TRUE.equals(mockSetting.getResponseSetting().getIsTiv()) && StringUtils.isBlank(requestBody)) {
            log.info("Выполняю логику ТИВа по основному протоколу");
            log.info("Извлекаю поля из query-params запроса");
            String queryParams = httpRequest.getQueryString();
            // Извлечение нужных полей из запроса
            extractedFields = ParseUtils.extractFieldsFromQueryParams(queryParams);
            if (extractedFields.values().stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("Поля не найдены");
            }
            extractedFields.forEach((k, v) -> log.info(String.format("%s = %s", k, v)));
            // Определение типа запроса
            if ("RUB".equalsIgnoreCase(extractFieldFromQueryParams(queryParams, "CURRENCY"))) {
                log.info("Определил тип запроса: CA2DA");
                // Извлечение курса из файла ответа
                double rate = extractCurrRate(responseFile);
                log.info("Определил курс: " + rate);
                //Расчёт credit_amount
                String amountStr = extractedFields.get("amount");
                try {
                    double amount = Double.parseDouble(amountStr);
                    double creditAmount = amount * rate;
                    String formattedCreditAmount = String.format(Locale.US,"%.0f", creditAmount);
                    extractedFields.put("credit_amount", formattedCreditAmount);
                    log.info("Посчитал credit_amount: " + formattedCreditAmount);
                } catch (NumberFormatException e) {
                    log.warn("Невозможно преобразовать amount в число:" + amountStr, e);
                }
            } else {
                log.info("Определил тип запроса: DA2CA");
                double rate = extractCurrRate(responseFile);
                log.info("Определил курс: " + rate);
                String amountStr = extractedFields.get("amount");
                try {
                    double creditAmount = Double.parseDouble(amountStr);
                    String formattedCreditAmount = String.format(Locale.US,"%.0f", creditAmount);
                    double amount = creditAmount / rate;
                    String formattedAmount = String.format(Locale.US,"%.0f", amount);
                    extractedFields.put("credit_amount", formattedCreditAmount);
                    extractedFields.put("amount", formattedAmount);
                    log.info("Заменил credit_amount на amount из запроса: " + formattedCreditAmount);
                    log.info("Посчитал amount: " + formattedAmount);
                } catch (NumberFormatException e) {
                    log.warn("Невозможно преобразовать amount в число:" + amountStr, e);
                }
            }
        }
        //Замена плейсхолдеров в шаблоне ответа
        for (Map.Entry<String, String> entry : extractedFields.entrySet()) {
            log.info("Заменяю " + entry.getKey() + " на " + entry.getValue());
            responseFile = responseFile.replace("{" + entry.getKey() + "}" , entry.getValue());
        }
        log.info("Получена итоговая строка ответа:\n" + responseFile);
        return responseFile;
    }

    public static String dynamicReplace(DynamicResponse dynResp, String response) {
        return response.replace(dynResp.getPlaceHolder(), randomByType(dynResp));
    }

    /**
     * Return random value by needful type
     * @param dynamicResponse data of dynamic value
     * @return random value by needful type
     */
    private static String randomByType(DynamicResponse dynamicResponse) {
        switch (dynamicResponse.getType()) {
            case UUID:
                return UUID.randomUUID().toString().replace("-", "");
            case UUID_SEPARATED:
                return UUID.randomUUID().toString();
            case STRING:
                return RandomStringUtils.random(dynamicResponse.getFieldLength(), true, false);
            case INTEGER:
                return valueWithLength(
                        String.valueOf(new RandomDataImpl().nextInt(1, Integer.MAX_VALUE)),
                        dynamicResponse.getFieldLength()
                );
            case BIG_INTEGER:
                return valueWithLength(
                        String.valueOf(new RandomDataImpl().nextLong(1, Long.MAX_VALUE)),
                        dynamicResponse.getFieldLength()
                );
            default:
                throw new UnsupportedOperationException("Type not supported");
        }
    }

    /**
     * Intelligently cut value with length
     * @param value given value
     * @param length needed length
     * @return cut value with length
     */
    private static String valueWithLength(String value, int length) {
        if (value.length() <= length) {
            return value;
        }
        if (length == 0) {
            return value.substring(0, 1);
        }
        return value.substring(0, length);
    }
}
