package com.iccues.metaanimebackend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;

@Component
@Slf4j
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String remoteAddr = getClientIp(request);

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("API Request - ")
                  .append(method).append(" ")
                  .append(uri);

        if (queryString != null) {
            logMessage.append("?").append(queryString);
        }

        logMessage.append(" from ").append(remoteAddr);

        log.info(logMessage.toString());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();

            StringBuilder logMessage = new StringBuilder();
            logMessage.append("API Response - ")
                      .append(method).append(" ")
                      .append(uri)
                      .append(" - Status: ").append(status)
                      .append(" - Time: ").append(executionTime).append("ms");

            if (status >= 500) {
                log.error(logMessage.toString());
            } else if (status >= 400) {
                log.warn(logMessage.toString());
            } else {
                log.info(logMessage.toString());
            }

            if (ex != null) {
                log.error("Exception occurred during request processing", ex);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
