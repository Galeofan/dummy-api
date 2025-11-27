package ru.mtsbank.soapdummy.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import java.io.IOException;
import java.util.UUID;

/**
 * Http servlet filter for http requests
 */
@Component
@Slf4j
public class HttpServletFilter extends HttpFilter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        try {
            String sessionId = UUID.randomUUID().toString();
            MDC.put("sessionId", sessionId);
            log.debug("Start session with id: {}", sessionId);
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error executing session. Error msg: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove("sessionId");
        }
    }


}
