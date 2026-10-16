package com.yukiani.server.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.function.Supplier;

/**
 * 为外部平台请求提供同步重试机制。
 *
 * <p>429、408、5xx 和其他临时异常按指数退避重试，其余 4xx 直接抛出。
 * 响应带有 {@code Retry-After} 时至少等待其指定的时长，超过上限则不再重试。</p>
 */
@Slf4j
public class RetryUtil {

    /** 默认最大重试次数，不包含首次执行。 */
    public static final int DEFAULT_MAX_RETRIES = 3;

    /** 默认首次重试间隔，单位为毫秒；之后每次重试翻倍。 */
    public static final long DEFAULT_RETRY_DELAY_MS = 1000;

    /** 可接受的 {@code Retry-After} 上限，单位为秒；超过时不再等待，交由调用方处理。 */
    public static final long MAX_RETRY_AFTER_SECONDS = 60;

    /**
     * 使用指定次数和间隔重试操作。
     *
     * @param maxRetries    最大重试次数，不含首次执行
     * @param retryDelayMs  首次重试间隔（毫秒），之后每次翻倍
     */
    public static <T> T executeWithRetry(Supplier<T> operation, int maxRetries, long retryDelayMs, String operationName) {
        for (int attempt = 0; ; attempt++) {
            try {
                return operation.get();
            } catch (RuntimeException e) {
                // 除限流和请求超时外，4xx 通常无法通过重试恢复，立即交由调用方处理。
                if (e instanceof WebClientResponseException r && !isRetryable(r.getStatusCode())) {
                    log.warn("{} operation failed with client error: {}", operationName, e.getMessage());
                    throw e;
                }
                if (attempt >= maxRetries) {
                    log.error("{} operation failed after {} retries: {}", operationName, maxRetries, e.getMessage());
                    throw e;
                }

                long delayMs = retryDelayMs << attempt;
                Long retryAfterSeconds = parseRetryAfterSeconds(e);
                if (retryAfterSeconds != null) {
                    // 平台要求等待过久时不阻塞当前线程，由调用方决定何时再试。
                    if (retryAfterSeconds > MAX_RETRY_AFTER_SECONDS) {
                        log.warn("{} operation asked to retry after {}s, exceeding the {}s limit",
                                operationName, retryAfterSeconds, MAX_RETRY_AFTER_SECONDS);
                        throw e;
                    }
                    delayMs = Math.max(delayMs, retryAfterSeconds * 1000);
                }

                log.warn("{} operation failed, retry {}/{} in {}ms: {}",
                        operationName, attempt + 1, maxRetries, delayMs, e.getMessage());
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException interrupted) {
                    // 保留中断标志并停止重试，让调用方尽快结束。
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }

    /**
     * 使用默认次数和间隔重试操作。
     */
    public static <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        return executeWithRetry(operation, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAY_MS, operationName);
    }

    /** 429 和 408 属于临时状态，其余 4xx 视为请求本身有误。 */
    private static boolean isRetryable(HttpStatusCode status) {
        return !status.is4xxClientError()
                || status.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)
                || status.isSameCodeAs(HttpStatus.REQUEST_TIMEOUT);
    }

    /**
     * 解析响应中以秒为单位的 {@code Retry-After}。
     *
     * @return 等待秒数；不是响应异常、未提供该响应头或使用 HTTP 日期格式时返回 {@code null}
     */
    private static Long parseRetryAfterSeconds(RuntimeException e) {
        if (!(e instanceof WebClientResponseException r)) {
            return null;
        }
        String value = r.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
