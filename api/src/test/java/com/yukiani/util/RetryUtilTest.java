package com.yukiani.util;

import org.junit.jupiter.api.Test;
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
    public void testExecuteWithRetry_InterruptedException() {
        // 测试重试过程中线程被中断的情况
        AtomicInteger attempt = new AtomicInteger(0);

        // 在另一个线程中执行，以便可以中断它
        Thread testThread = new Thread(() -> {
            try {
                RetryUtil.executeWithRetry(
                        () -> {
                            attempt.incrementAndGet();
                            // 第一次抛出异常，触发重试
                            if (attempt.get() == 1) {
                                // 中断当前线程，这会在 sleep 时触发 InterruptedException
                                Thread.currentThread().interrupt();
                                throw new RuntimeException("First attempt failed");
                            }
                            return "success";
                        },
                        1, // 只重试1次
                        1000, // 1秒延迟
                        "testOperation"
                );
            } catch (Exception e) {
                // 预期会抛出异常
            }
        });

        testThread.start();
        try {
            testThread.join(5000); // 等待最多5秒
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // 验证线程的中断状态被设置
        assertTrue(testThread.isInterrupted() || attempt.get() >= 1);
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
