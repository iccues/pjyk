package com.yukiani.server.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RetryUtilTest {

    @Test
    public void testExecuteWithRetry_Success() {
        // 第一次就成功
        String result = RetryUtil.executeWithRetry(
                () -> "success",
                "testOperation"
        );

        assertEquals("success", result);
    }

    @Test
    public void testExecuteWithRetry_SuccessAfterRetry() {
        // 前两次失败，第三次成功
        AtomicInteger attempt = new AtomicInteger(0);

        String result = RetryUtil.executeWithRetry(
                () -> {
                    if (attempt.incrementAndGet() < 3) {
                        throw WebClientResponseException.create(
                                500,
                                "Internal Server Error",
                                null,
                                "Server error".getBytes(),
                                null
                        );
                    }
                    return "success";
                },
                3,
                10, // 使用短延迟加快测试
                "testOperation"
        );

        assertEquals("success", result);
        assertEquals(3, attempt.get());
    }

    @Test
    public void testExecuteWithRetry_4xxError_NoRetry() {
        // 4xx 错误不应该重试
        AtomicInteger attempt = new AtomicInteger(0);

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            throw WebClientResponseException.create(
                                    404,
                                    "Not Found",
                                    null,
                                    "Resource not found".getBytes(),
                                    null
                            );
                        },
                        3,
                        10,
                        "testOperation"
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(1, attempt.get(), "Should not retry for 4xx errors");
    }

    @Test
    public void testExecuteWithRetry_5xxError_RetriesExhausted() {
        // 5xx 错误应该重试，但最终失败
        AtomicInteger attempt = new AtomicInteger(0);

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            throw WebClientResponseException.create(
                                    503,
                                    "Service Unavailable",
                                    null,
                                    "Service temporarily unavailable".getBytes(),
                                    null
                            );
                        },
                        3,
                        10,
                        "testOperation"
                )
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        assertEquals(4, attempt.get(), "Should attempt 1 initial + 3 retries");
    }

    @Test
    public void testExecuteWithRetry_GenericException_Retries() {
        // 一般异常应该重试
        AtomicInteger attempt = new AtomicInteger(0);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            throw new RuntimeException("Generic error");
                        },
                        2,
                        10,
                        "testOperation"
                )
        );

        assertEquals("Generic error", exception.getMessage());
        assertEquals(3, attempt.get(), "Should attempt 1 initial + 2 retries");
    }

    @Test
    public void testExecuteWithRetry_DefaultParameters() {
        // 测试使用默认参数
        AtomicInteger attempt = new AtomicInteger(0);

        String result = RetryUtil.executeWithRetry(
                () -> {
                    if (attempt.incrementAndGet() < 2) {
                        throw new RuntimeException("Temporary error");
                    }
                    return "success";
                },
                "testOperation"
        );

        assertEquals("success", result);
        assertEquals(2, attempt.get());
    }

    @Test
    public void testExecuteWithRetry_InterruptedStopsRetrying() {
        // 等待重试时线程被中断，应立即停止重试并抛出原异常，同时保留中断标志
        AtomicInteger attempt = new AtomicInteger(0);

        try {
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> RetryUtil.executeWithRetry(
                            () -> {
                                attempt.incrementAndGet();
                                // 中断当前线程，使随后的重试等待立即被打断
                                Thread.currentThread().interrupt();
                                throw new RuntimeException("First attempt failed");
                            },
                            3,
                            1000,
                            "testOperation"
                    )
            );

            assertEquals("First attempt failed", exception.getMessage());
            assertEquals(1, attempt.get(), "Should stop retrying once interrupted");
            assertTrue(Thread.currentThread().isInterrupted(), "Interrupt flag should be preserved");
        } finally {
            // 清除中断标志，避免影响同一线程上的其他测试
            Thread.interrupted();
        }
    }

    @Test
    public void testExecuteWithRetry_429TooManyRequests_Retries() {
        // 429 属于限流，应该重试
        AtomicInteger attempt = new AtomicInteger(0);

        String result = RetryUtil.executeWithRetry(
                () -> {
                    if (attempt.incrementAndGet() < 3) {
                        throw tooManyRequests(null);
                    }
                    return "success";
                },
                3,
                10,
                "testOperation"
        );

        assertEquals("success", result);
        assertEquals(3, attempt.get());
    }

    @Test
    public void testExecuteWithRetry_408RequestTimeout_Retries() {
        // 408 属于临时超时，应该重试直到次数用尽
        AtomicInteger attempt = new AtomicInteger(0);

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            throw WebClientResponseException.create(
                                    408,
                                    "Request Timeout",
                                    null,
                                    "Request timeout".getBytes(),
                                    null
                            );
                        },
                        2,
                        10,
                        "testOperation"
                )
        );

        assertEquals(HttpStatus.REQUEST_TIMEOUT, exception.getStatusCode());
        assertEquals(3, attempt.get(), "Should attempt 1 initial + 2 retries");
    }

    @Test
    public void testExecuteWithRetry_RetryAfter_WaitsAtLeastGivenSeconds() {
        // Retry-After 比退避间隔长时，应至少等待 Retry-After 指定的时长
        AtomicInteger attempt = new AtomicInteger(0);

        long start = System.nanoTime();
        String result = RetryUtil.executeWithRetry(
                () -> {
                    if (attempt.incrementAndGet() < 2) {
                        throw tooManyRequests("1");
                    }
                    return "success";
                },
                3,
                10,
                "testOperation"
        );
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertEquals("success", result);
        assertEquals(2, attempt.get());
        // 留出系统计时精度的余量
        assertTrue(elapsedMs >= 900, "Should wait about 1s as Retry-After requires, waited " + elapsedMs + "ms");
    }

    @Test
    public void testExecuteWithRetry_RetryAfterExceedsLimit_NoRetry() {
        // Retry-After 超过上限时不等待，直接抛出交给调用方
        AtomicInteger attempt = new AtomicInteger(0);

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            throw tooManyRequests(String.valueOf(RetryUtil.MAX_RETRY_AFTER_SECONDS + 1));
                        },
                        3,
                        10,
                        "testOperation"
                )
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
        assertEquals(1, attempt.get(), "Should not retry when Retry-After exceeds the limit");
    }

    @Test
    public void testExecuteWithRetry_RetryAfterHttpDate_FallsBackToBackoff() {
        // HTTP 日期格式的 Retry-After 不解析，按退避间隔重试
        AtomicInteger attempt = new AtomicInteger(0);

        String result = RetryUtil.executeWithRetry(
                () -> {
                    if (attempt.incrementAndGet() < 2) {
                        throw tooManyRequests("Wed, 21 Oct 2015 07:28:00 GMT");
                    }
                    return "success";
                },
                3,
                10,
                "testOperation"
        );

        assertEquals("success", result);
        assertEquals(2, attempt.get());
    }

    private static WebClientResponseException tooManyRequests(String retryAfter) {
        HttpHeaders headers = new HttpHeaders();
        if (retryAfter != null) {
            headers.set(HttpHeaders.RETRY_AFTER, retryAfter);
        }
        return WebClientResponseException.create(
                429,
                "Too Many Requests",
                headers,
                "Rate limited".getBytes(),
                null
        );
    }

    @Test
    public void testExecuteWithRetry_400BadRequest() {
        // 测试 400 Bad Request 错误不重试
        AtomicInteger attempt = new AtomicInteger(0);

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            throw WebClientResponseException.create(
                                    400,
                                    "Bad Request",
                                    null,
                                    "Invalid request".getBytes(),
                                    null
                            );
                        },
                        3,
                        10,
                        "testOperation"
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(1, attempt.get(), "Should not retry for 400 errors");
    }

    @Test
    public void testExecuteWithRetry_401Unauthorized() {
        // 测试 401 Unauthorized 错误不重试
        AtomicInteger attempt = new AtomicInteger(0);

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            throw WebClientResponseException.create(
                                    401,
                                    "Unauthorized",
                                    null,
                                    "Authentication required".getBytes(),
                                    null
                            );
                        },
                        3,
                        10,
                        "testOperation"
                )
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals(1, attempt.get(), "Should not retry for 401 errors");
    }

    @Test
    public void testExecuteWithRetry_403Forbidden() {
        // 测试 403 Forbidden 错误不重试
        AtomicInteger attempt = new AtomicInteger(0);

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            throw WebClientResponseException.create(
                                    403,
                                    "Forbidden",
                                    null,
                                    "Access denied".getBytes(),
                                    null
                            );
                        },
                        3,
                        10,
                        "testOperation"
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals(1, attempt.get(), "Should not retry for 403 errors");
    }

    @Test
    public void testExecuteWithRetry_500InternalServerError() {
        // 测试 500 Internal Server Error 会重试
        AtomicInteger attempt = new AtomicInteger(0);

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            throw WebClientResponseException.create(
                                    500,
                                    "Internal Server Error",
                                    null,
                                    "Server error".getBytes(),
                                    null
                            );
                        },
                        2,
                        10,
                        "testOperation"
                )
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals(3, attempt.get(), "Should attempt 1 initial + 2 retries");
    }

    @Test
    public void testExecuteWithRetry_502BadGateway() {
        // 测试 502 Bad Gateway 会重试
        AtomicInteger attempt = new AtomicInteger(0);

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            throw WebClientResponseException.create(
                                    502,
                                    "Bad Gateway",
                                    null,
                                    "Bad gateway".getBytes(),
                                    null
                            );
                        },
                        2,
                        10,
                        "testOperation"
                )
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals(3, attempt.get(), "Should attempt 1 initial + 2 retries");
    }

    @Test
    public void testExecuteWithRetry_504GatewayTimeout() {
        // 测试 504 Gateway Timeout 会重试
        AtomicInteger attempt = new AtomicInteger(0);

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            throw WebClientResponseException.create(
                                    504,
                                    "Gateway Timeout",
                                    null,
                                    "Gateway timeout".getBytes(),
                                    null
                            );
                        },
                        2,
                        10,
                        "testOperation"
                )
        );

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, exception.getStatusCode());
        assertEquals(3, attempt.get(), "Should attempt 1 initial + 2 retries");
    }

    @Test
    public void testExecuteWithRetry_MaxRetriesZero() {
        // 测试 maxRetries=0 的边界情况（不重试）
        AtomicInteger attempt = new AtomicInteger(0);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            throw new RuntimeException("Error");
                        },
                        0, // 不重试
                        10,
                        "testOperation"
                )
        );

        assertEquals("Error", exception.getMessage());
        assertEquals(1, attempt.get(), "Should only attempt once when maxRetries=0");
    }

    @Test
    public void testExecuteWithRetry_NullReturn() {
        // 测试返回 null 的情况
        String result = RetryUtil.executeWithRetry(
                () -> null,
                "testOperation"
        );

        assertNull(result, "Should handle null return value");
    }

    @Test
    public void testExecuteWithRetry_DifferentReturnTypes() {
        // 测试不同的返回类型
        Integer intResult = RetryUtil.executeWithRetry(
                () -> 42,
                "testOperation"
        );
        assertEquals(42, intResult);

        Boolean boolResult = RetryUtil.executeWithRetry(
                () -> true,
                "testOperation"
        );
        assertTrue(boolResult);

        Double doubleResult = RetryUtil.executeWithRetry(
                () -> 3.14,
                "testOperation"
        );
        assertEquals(3.14, doubleResult);
    }
}
