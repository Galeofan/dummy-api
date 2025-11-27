package ru.mtsbank.soapdummy.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ParseUtils {

    // XML: <CURR_RATE>0.1212000000</CURR_RATE>
    private static final Pattern CURR_RATE_XML_PATTERN = Pattern.compile("<CURR_RATE>(\\d+(\\.\\d+)?)</CURR_RATE>", Pattern.CASE_INSENSITIVE);

    // JSON: "rate": 0.0064102564
    private static final Pattern RATE_JSON_PATTERN = Pattern.compile("\"rate\"\\s*:\\s*(\\d+(\\.\\d+)?)", Pattern.CASE_INSENSITIVE);

    public static Map<String, String> extractFieldsFromJson(String requestBody) {
        Map<String, String> values = new HashMap<>();
        values.put("id", extractFieldFromJson(requestBody, "id"));
        values.put("amount", extractFieldFromJson(requestBody, "amount"));
        return values;
    }

    static String extractFieldFromJson(String requestBody, String fieldName) {
        // Извлечение строковых и числовых значений по регулярке
        String patternStr = String.format("\"%s\"\\s*:\\s*(\"([^\"]*)\"|(\\d+))", fieldName);
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(requestBody);
        if (matcher.find()) {
            return matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
        }
        return null;
    }

    public static Map<String, String> extractFieldsFromQueryParams(String queryParams) {
        Map<String, String> values = new HashMap<>();
        values.put("amount", extractFieldFromQueryParams(queryParams, "AMOUNT"));
        return values;
    }

    public static String extractFieldFromQueryParams(String queryParams, String fieldName) {
        // Шаблон: ищем FIELD=значение (до следующего & или конца строки)
        String patternStr = String.format("%s=([^&]*)", Pattern.quote(fieldName));
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(queryParams);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static double extractCurrRate(String responseFile) {
        if (responseFile == null || responseFile.isBlank()) {
            throw new IllegalArgumentException("Response is empty");
        }

        responseFile = responseFile.trim();

        // Определяем формат
        if (responseFile.startsWith("{")) {
            return extractRateFromJson(responseFile);
        } else {
            return extractRateFromXml(responseFile);
        }
    }

    private static double extractRateFromXml(String xml) {
        Matcher matcher = CURR_RATE_XML_PATTERN.matcher(xml);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        throw new IllegalArgumentException("CURR_RATE не найден в XML");
    }

    private static double extractRateFromJson(String json) {
        Matcher matcher = RATE_JSON_PATTERN.matcher(json);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        throw new IllegalArgumentException("rate не найден в JSON");
    }
}
