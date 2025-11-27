package ru.mtsbank.soapdummy.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.mtsbank.soapdummy.utils.HttpHeaderConverter;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.bind.annotation.RequestMethod.TRACE;

/**
 * Controller for mock all requests
 */
@Slf4j
@RestController
@RequestMapping
@ParametersAreNonnullByDefault
public class DummyController {

    private final DummyService dummyService;

    public DummyController(DummyService dummyService) {
        this.dummyService = dummyService;
    }

    /**
     * mocking all HTTP methods request
     * @param httpRequest all info about http request
     * @param requestBodyStr String of request body
     * @return mocked (added before in settings) response
     */
    @RequestMapping(value = { "/**"},
        method = {GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE})
    @ResponseBody
    public ResponseEntity<String> dummy(HttpServletRequest httpRequest, @RequestBody(required = false) String requestBodyStr) {
        String clearRequestBodyStr = requestBodyStr == null ? "" :
                requestBodyStr.replaceAll("\\s+", "");
        log.info("Receive request with url: {} \n" +
            "Url params: {} \n" +
            "Request method: {} \n" +
            "Request headers: {} \n" +
            "With body: {}",
            httpRequest.getRequestURL().toString(),
            httpRequest.getQueryString(),
            httpRequest.getMethod(),
            HttpHeaderConverter.convert(
                httpRequest.getHeaderNames())
                .map(h -> String.format("[%s: %s]", h, httpRequest.getHeader(h)))
                .collect(Collectors.toSet()
                ),
            requestBodyStr);
        return dummyService.mockRequest(httpRequest, clearRequestBodyStr);
    }
}
