package ru.mtsbank.soapdummy.mock.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.mtsbank.soapdummy.mock.setting.SpecificMockSetting;
import ru.mtsbank.soapdummy.utils.FileUtils;
import ru.mtsbank.soapdummy.utils.ReplaceUtils;

/**
 * Handler for work with dynamic fields and change value instead of placeholder in responses
 */
@Slf4j
@Service
public class DynamicFieldResponseHandler implements ResponseHandler<String, SpecificMockSetting> {
    @Override
    public String execute(SpecificMockSetting executedRule) {
        if (!executedRule.getResponseSetting().getEnableDynamicResponse()
                || CollectionUtils.isEmpty(executedRule.getResponseSetting().getDynamicResponses())) {
            return FileUtils.getFileContent(executedRule.getResponseSetting().getResponseBodyPath());
        }
        String responseContend = FileUtils.getFileContent(executedRule.getResponseSetting().getResponseBodyPath());
        for (var dynResp : executedRule.getResponseSetting().getDynamicResponses()) {
            responseContend = ReplaceUtils.dynamicReplace(dynResp, responseContend);
        }
        return responseContend;
    }
}
