package com.yukiani.server.service;

import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.Platform;
import com.yukiani.server.exception.FetchFailedException;
import com.yukiani.server.repo.SyncCandidate;
import com.yukiani.server.service.fetch.AbstractAnimeFetchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * PlatformSyncRunner 测试类
 */
@ExtendWith(MockitoExtension.class)
class PlatformSyncRunnerTest {

    private static final int THRESHOLD = PlatformSyncRunner.CIRCUIT_THRESHOLD;

    /** 候选 Mapping 上次成功同步的时间。 */
    private static final Instant LAST_SYNCED = Instant.parse("2026-10-15T04:00:00Z");

    private static final Instant NOW = Instant.parse("2026-10-16T01:00:00Z");

    @Mock
    private AbstractAnimeFetchService fetchService;

    private PlatformSyncRunner runner;

    @BeforeEach
    void setUp() {
        runner = new PlatformSyncRunner(Platform.MyAnimeList, fetchService, Duration.ZERO);
    }

    // ==================== 同步结果 ====================

    @Test
    void run_SeparatesSuccessFromItemFailures() {
        when(fetchService.fetchAndSaveMapping("1")).thenReturn(saved(NOW));
        when(fetchService.fetchAndSaveMapping("2")).thenThrow(fetchFailure(404));

        PlatformSyncRunner.Result result = runner.run(candidates(2));

        assertEquals(List.of(1L), result.succeeded());
        assertEquals(List.of(2L), result.failed());
    }

    // ==================== 熔断 ====================

    @Test
    void run_CircuitOpensAfterConsecutivePlatformFailures() {
        when(fetchService.fetchAndSaveMapping(anyString())).thenThrow(fetchFailure(503));

        PlatformSyncRunner.Result result = runner.run(candidates(THRESHOLD + 1));

        verify(fetchService, times(THRESHOLD)).fetchAndSaveMapping(anyString());
        verify(fetchService, never()).fetchAndSaveMapping(String.valueOf(THRESHOLD + 1));
        assertEquals(List.of(), result.succeeded());
        // 熔断只停止本轮请求，触发熔断的失败照常交给轮次层计入退避
        assertEquals(ids(1, THRESHOLD), result.failed());
    }

    @Test
    void run_ResponseResetsConsecutiveCount() {
        // 阈值减一次 503、一次成功、再阈值减一次 503：始终没有连续达到阈值，全部照常请求
        int count = 2 * THRESHOLD - 1;
        when(fetchService.fetchAndSaveMapping(anyString())).thenThrow(fetchFailure(503));
        doReturn(saved(NOW)).when(fetchService).fetchAndSaveMapping(String.valueOf(THRESHOLD));

        PlatformSyncRunner.Result result = runner.run(candidates(count));

        verify(fetchService, times(count)).fetchAndSaveMapping(anyString());
        assertEquals(List.of((long) THRESHOLD), result.succeeded());
        assertEquals(count - 1, result.failed().size());
    }

    @Test
    void run_ItemFailures_DoNotOpenCircuit() {
        // 连续 404 是条目问题：全部照常请求，各自算作条目失败
        int count = THRESHOLD + 1;
        when(fetchService.fetchAndSaveMapping(anyString())).thenThrow(fetchFailure(404));

        PlatformSyncRunner.Result result = runner.run(candidates(count));

        verify(fetchService, times(count)).fetchAndSaveMapping(anyString());
        assertEquals(ids(1, count), result.failed());
    }

    // ==================== 失败归因 ====================

    @ParameterizedTest
    @CsvSource({
            "500, true",
            "502, true",
            "503, true",
            "429, true",
            "408, true",
            "404, false",
            "400, false",
            "403, false"
    })
    void isPlatformFailure_ByStatus(int status, boolean expected) {
        assertEquals(expected, PlatformSyncRunner.isPlatformFailure(responseException(status)));
    }

    @Test
    void isPlatformFailure_RequestExceptionWithoutResponse() {
        // 网络错误和超时都会包装成 WebClientRequestException
        WebClientRequestException e = new WebClientRequestException(
                new IOException("Connection reset"), HttpMethod.GET, URI.create("https://example.com"), new HttpHeaders());

        assertTrue(PlatformSyncRunner.isPlatformFailure(e));
    }

    @Test
    void isPlatformFailure_LooksThroughCauseChain() {
        assertTrue(PlatformSyncRunner.isPlatformFailure(fetchFailure(503)));
        assertFalse(PlatformSyncRunner.isPlatformFailure(fetchFailure(404)));
    }

    @Test
    void isPlatformFailure_OtherFailuresAreItemLevel() {
        assertFalse(PlatformSyncRunner.isPlatformFailure(new FetchFailedException(Platform.MyAnimeList, "1")));
        assertFalse(PlatformSyncRunner.isPlatformFailure(new RuntimeException("Parse error")));
    }

    // ==================== 辅助方法 ====================

    /** mappingId 与 platformId 均从 1 开始编号的候选 Mapping。 */
    private static List<SyncCandidate> candidates(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(id -> new SyncCandidate((long) id, 1L, LocalDate.of(2026, 10, 1),
                        Platform.MyAnimeList, String.valueOf(id), LAST_SYNCED))
                .toList();
    }

    private static List<Long> ids(long from, long to) {
        return LongStream.rangeClosed(from, to).boxed().toList();
    }

    private static Mapping saved(Instant updateTime) {
        Mapping mapping = new Mapping();
        mapping.setUpdateTime(updateTime);
        return mapping;
    }

    private static WebClientResponseException responseException(int status) {
        return WebClientResponseException.create(status, "Status " + status, null, null, null);
    }

    /** 与真实调用链一样，把平台响应异常包装在 FetchFailedException 里。 */
    private static FetchFailedException fetchFailure(int status) {
        return new FetchFailedException(Platform.MyAnimeList, "1", responseException(status));
    }
}
