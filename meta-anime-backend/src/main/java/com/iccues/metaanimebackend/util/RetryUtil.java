package com.iccues.metaanimebackend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.function.Supplier;

/**
 * 重试工具类
 * 提供通用的重试机制，用于处理临时性失败的操作
 */
@Slf4j
public class RetryUtil {

    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final long DEFAULT_RETRY_DELAY_MS = 1000;

    /**
     * 重试执行给定的操作
     *
     * @param operation     要执行的操作
     * @param maxRetries    最大重试次数
     * @param retryDelayMs  重试间隔（毫秒）
     * @param operationName 操作名称（用于日志）
     * @param <T>           返回类型
     * @return 操作结果
     */
    public static <T> T executeWithRetry(Supplier<T> operation, int maxRetries, long retryDelayMs, String operationName) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt <= maxRetries) {
            try {
                if (attempt > 0) {
                    log.warn("Retrying {} operation, attempt {}/{}", operationName, attempt, maxRetries);
                }
                return operation.get();
            } catch (WebClientResponseException e) {
                lastException = e;
                // 对于 4xx 错误（客户端错误），不重试
                if (e.getStatusCode().is4xxClientError()) {
                    log.warn("{} operation failed with client error: {} {}",
                            operationName, e.getStatusCode(), e.getMessage());
                    throw e;
                }
                // 对于 5xx 错误（服务器错误），进行重试
                if (attempt < maxRetries) {
                    log.warn("{} operation failed, will retry: {} {}",
                            operationName, e.getStatusCode(), e.getMessage());
                    sleep(retryDelayMs);
                    attempt++;
                } else {
                    log.error("{} operation failed after {} retries: {} {}",
                            operationName, maxRetries, e.getStatusCode(), e.getResponseBodyAsString());
                    throw e;
                }
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    log.warn("{} operation failed, will retry: {}", operationName, e.getMessage());
                    sleep(retryDelayMs);
                    attempt++;
                } else {
                    log.error("{} operation failed after {} retries: {}", operationName, maxRetries, e.getMessage());
                    throw e;
                }
            }
        }

        // 理论上不会到达这里，但为了类型安全
        if (lastException != null) {
            throw new RuntimeException(lastException);
        }
        throw new RuntimeException("Unexpected error in retry logic");
    }

    /**
     * 使用默认配置重试执行操作
     *
     * @param operation     要执行的操作
     * @param operationName 操作名称（用于日志）
     * @param <T>           返回类型
     * @return 操作结果
     */
    public static <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        return executeWithRetry(operation, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAY_MS, operationName);
    }

    private static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Retry sleep interrupted");
        }
    }
}
