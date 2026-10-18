package com.yukiani.server.service;

import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.Platform;
import com.yukiani.server.entity.ReviewStatus;
import com.yukiani.server.exception.FetchFailedException;
import com.yukiani.server.repo.MappingRepository;
import com.yukiani.server.repo.SyncCandidate;
import com.yukiani.server.service.fetch.AbstractAnimeFetchService;
import com.yukiani.server.service.fetch.FetchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * MappingSyncService 测试类
 */
@ExtendWith(MockitoExtension.class)
class MappingSyncServiceTest {

    /** 固定的"今天"，保证分片计算结果稳定。 */
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 16);

    /** 本轮开始时间：今天上海时间 01:00。 */
    private static final Instant NOW = at(TODAY, 1);

    private static final Instant YESTERDAY_NOON = at(TODAY.minusDays(1), 12);

    @Mock
    private MappingRepository mappingRepository;

    @Mock
    private FetchService fetchService;

    @Mock
    private AbstractAnimeFetchService malFetchService;

    @Mock
    private AbstractAnimeFetchService bangumiFetchService;

    @InjectMocks
    private MappingSyncService mappingSyncService;

    @BeforeEach
    void setUp() {
        mappingSyncService.requestInterval = Duration.ZERO;
    }

    // ==================== 到期规则 ====================

    @Test
    void isDue_DailyTier_DueUntilSyncedToday() {
        LocalDate startDate = TODAY.minusDays(10);

        assertTrue(isDue(new SyncCandidate(1L, 1L, startDate, Platform.MyAnimeList, "1", YESTERDAY_NOON)));
        assertFalse(isDue(new SyncCandidate(1L, 1L, startDate, Platform.MyAnimeList, "1", at(TODAY, 0))));
    }

    /**
     * 30 天以上的档位按 animeId 分片：今天是排班日时到期；
     * 排班日在昨天时，以昨天上海时间零点为界判断是否已同步。
     */
    @ParameterizedTest
    @CsvSource({"60", "120", "365"})
    void isDue_FollowsAnimeIdShard(int startDaysAgo) {
        LocalDate startDate = TODAY.minusDays(startDaysAgo);
        Instant yesterdayStart = at(TODAY.minusDays(1), 0);

        assertTrue(isDue(new SyncCandidate(1L, scheduledDaysAgo(0), startDate, Platform.MyAnimeList, "1", YESTERDAY_NOON)),
                "Scheduled today and not synced today");
        assertFalse(isDue(new SyncCandidate(1L, scheduledDaysAgo(1), startDate, Platform.MyAnimeList, "1", yesterdayStart)),
                "Scheduled yesterday and synced at yesterday's midnight");
        assertTrue(isDue(new SyncCandidate(1L, scheduledDaysAgo(1), startDate, Platform.MyAnimeList, "1",
                        yesterdayStart.minusSeconds(1))),
                "Scheduled yesterday but last synced just before it");
    }

    @Test
    void isDue_MissedScheduledDay_StaysDueUntilSuccess() {
        // 30 天档，最近排班日在 3 天前
        LocalDate startDate = TODAY.minusDays(365);
        long animeId = scheduledDaysAgo(3);

        assertTrue(isDue(new SyncCandidate(1L, animeId, startDate, Platform.MyAnimeList, "1",
                at(TODAY.minusDays(4), 12))), "Not synced since the scheduled day");
        assertFalse(isDue(new SyncCandidate(1L, animeId, startDate, Platform.MyAnimeList, "1",
                at(TODAY.minusDays(2), 12))), "Caught up after the scheduled day");
    }

    /**
     * 每个档位的最后一天仍按本档同步，次日起切换到下一档。
     * animeId 选为下一档中排班日在昨天或更早、本档中排班日为今天。
     */
    @ParameterizedTest
    @CsvSource({"29, 1", "89, 2", "179, 7"})
    void isDue_TierBoundaries(int lastDayOfTier, int scheduledDaysAgoInNextTier) {
        long animeId = scheduledDaysAgo(scheduledDaysAgoInNextTier);

        assertTrue(isDue(new SyncCandidate(1L, animeId, TODAY.minusDays(lastDayOfTier),
                Platform.MyAnimeList, "1", YESTERDAY_NOON)));
        assertFalse(isDue(new SyncCandidate(1L, animeId, TODAY.minusDays(lastDayOfTier + 1),
                Platform.MyAnimeList, "1", YESTERDAY_NOON)));
    }

    // ==================== 同步轮次 ====================

    @Test
    void runSyncRound_QueriesWithShanghaiToday() {
        // 上海时间 10 月 16 日 00:30 对应 UTC 10 月 15 日 16:30，应按 10 月 16 日查询
        when(mappingRepository.findSyncCandidates(any(), any(), any())).thenReturn(List.of());

        mappingSyncService.runSyncRound(TODAY.atTime(0, 30).atZone(MappingSyncService.SYNC_ZONE).toInstant());

        verify(mappingRepository).findSyncCandidates(ReviewStatus.APPROVED, TODAY, at(TODAY, 0));
    }

    @Test
    void runSyncRound_SyncsOnlyDueMappings() {
        SyncCandidate due = candidate(1, Platform.MyAnimeList, YESTERDAY_NOON);
        // 隔天档，排班日为昨天且昨天已同步
        SyncCandidate notDue = new SyncCandidate(2L, scheduledDaysAgo(1), TODAY.minusDays(60),
                Platform.MyAnimeList, "2", YESTERDAY_NOON);
        givenCandidates(due, notDue);
        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(malFetchService);
        when(malFetchService.fetchAndSaveMapping("1")).thenReturn(saved(NOW));

        mappingSyncService.runSyncRound(NOW);

        verify(malFetchService).fetchAndSaveMapping("1");
        verify(malFetchService, never()).fetchAndSaveMapping("2");
        assertTrue(mappingSyncService.backoffs.isEmpty());
    }

    @Test
    void runSyncRound_GroupsByPlatformInUpdateTimeOrder() {
        SyncCandidate malNewer = candidate(2, Platform.MyAnimeList, YESTERDAY_NOON);
        SyncCandidate bangumi = candidate(3, Platform.Bangumi, YESTERDAY_NOON);
        SyncCandidate malOlder = candidate(1, Platform.MyAnimeList, at(TODAY.minusDays(3), 12));
        givenCandidates(malNewer, bangumi, malOlder);
        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(malFetchService);
        when(fetchService.getFetchService(Platform.Bangumi)).thenReturn(bangumiFetchService);
        when(malFetchService.fetchAndSaveMapping(anyString())).thenReturn(saved(NOW));
        when(bangumiFetchService.fetchAndSaveMapping("3")).thenReturn(saved(NOW));

        mappingSyncService.runSyncRound(NOW);

        InOrder inOrder = inOrder(malFetchService);
        inOrder.verify(malFetchService).fetchAndSaveMapping("1");
        inOrder.verify(malFetchService).fetchAndSaveMapping("2");
        verify(bangumiFetchService).fetchAndSaveMapping("3");
    }

    @Test
    void runSyncRound_FailureStartsBackoff() {
        givenCandidates(candidate(1, Platform.MyAnimeList, YESTERDAY_NOON));
        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(malFetchService);
        when(malFetchService.fetchAndSaveMapping("1")).thenThrow(new RuntimeException("API Error"));

        assertDoesNotThrow(() -> mappingSyncService.runSyncRound(NOW));

        MappingSyncService.Backoff backoff = mappingSyncService.backoffs.get(1L);
        assertEquals(Duration.ofHours(1), backoff.delay());
        assertEquals(NOW.plus(Duration.ofHours(1)), backoff.nextAttemptAt());
    }

    @Test
    void runSyncRound_NotUpdated_CountsAsFailure() {
        // 抓取未抛异常，但平台数据不完整未能保存，update_time 没有前进
        givenCandidates(candidate(1, Platform.MyAnimeList, YESTERDAY_NOON));
        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(malFetchService);
        when(malFetchService.fetchAndSaveMapping("1")).thenReturn(saved(YESTERDAY_NOON));

        mappingSyncService.runSyncRound(NOW);

        assertTrue(mappingSyncService.backoffs.containsKey(1L));
    }

    @Test
    void runSyncRound_SkipsMappingInBackoff() {
        givenCandidates(candidate(1, Platform.MyAnimeList, YESTERDAY_NOON));
        mappingSyncService.backoffs.put(1L,
                new MappingSyncService.Backoff(Duration.ofHours(1), NOW.plus(Duration.ofMinutes(30))));

        mappingSyncService.runSyncRound(NOW);

        verify(fetchService, never()).getFetchService(any());
        assertTrue(mappingSyncService.backoffs.containsKey(1L), "Still due, backoff should be kept");
    }

    @Test
    void runSyncRound_RetriesOnHourAfterFailureAndDoublesDelay() {
        // 01:00:30 失败时以 01:00 为基准，02:00 整点那一轮应当重试
        givenCandidates(candidate(1, Platform.MyAnimeList, YESTERDAY_NOON));
        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(malFetchService);
        when(malFetchService.fetchAndSaveMapping("1")).thenThrow(new RuntimeException("API Error"));

        mappingSyncService.runSyncRound(NOW.plusSeconds(30));
        mappingSyncService.runSyncRound(NOW.plus(Duration.ofHours(1)));

        verify(malFetchService, times(2)).fetchAndSaveMapping("1");
        MappingSyncService.Backoff backoff = mappingSyncService.backoffs.get(1L);
        assertEquals(Duration.ofHours(2), backoff.delay());
        assertEquals(NOW.plus(Duration.ofHours(3)), backoff.nextAttemptAt());
    }

    @Test
    void runSyncRound_BackoffCappedAtSevenDays() {
        givenCandidates(candidate(1, Platform.MyAnimeList, YESTERDAY_NOON));
        mappingSyncService.backoffs.put(1L, new MappingSyncService.Backoff(Duration.ofDays(7), NOW));
        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(malFetchService);
        when(malFetchService.fetchAndSaveMapping("1")).thenThrow(new RuntimeException("API Error"));

        mappingSyncService.runSyncRound(NOW);

        assertEquals(Duration.ofDays(7), mappingSyncService.backoffs.get(1L).delay());
    }

    @Test
    void runSyncRound_SuccessClearsBackoff() {
        givenCandidates(candidate(1, Platform.MyAnimeList, YESTERDAY_NOON));
        mappingSyncService.backoffs.put(1L, new MappingSyncService.Backoff(Duration.ofHours(4), NOW));
        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(malFetchService);
        when(malFetchService.fetchAndSaveMapping("1")).thenReturn(saved(NOW));

        mappingSyncService.runSyncRound(NOW);

        assertFalse(mappingSyncService.backoffs.containsKey(1L));
    }

    @Test
    void runSyncRound_DropsBackoffOfMappingNoLongerDue() {
        // 例如已被手动抓取成功，或所属动画被取消审核
        givenCandidates();
        mappingSyncService.backoffs.put(999L,
                new MappingSyncService.Backoff(Duration.ofHours(1), NOW.plus(Duration.ofHours(1))));

        mappingSyncService.runSyncRound(NOW);

        assertTrue(mappingSyncService.backoffs.isEmpty());
    }

    // ==================== 与平台层的衔接 ====================

    @Test
    void runSyncRound_CircuitOpenOnOnePlatform_OthersUnaffected() {
        // MAL 全部返回 503 触发熔断：已请求的条目照常进入退避，Bangumi 照常同步
        int threshold = PlatformSyncRunner.CIRCUIT_THRESHOLD;
        List<SyncCandidate> candidates = new ArrayList<>(malCandidates(threshold + 1));
        candidates.add(candidate(100, Platform.Bangumi, YESTERDAY_NOON));
        givenCandidates(candidates);
        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(malFetchService);
        when(fetchService.getFetchService(Platform.Bangumi)).thenReturn(bangumiFetchService);
        when(malFetchService.fetchAndSaveMapping(anyString())).thenThrow(fetchFailure(503));
        when(bangumiFetchService.fetchAndSaveMapping("100")).thenReturn(saved(NOW));

        mappingSyncService.runSyncRound(NOW);

        verify(malFetchService, times(threshold)).fetchAndSaveMapping(anyString());
        verify(malFetchService, never()).fetchAndSaveMapping(String.valueOf(threshold + 1));
        verify(bangumiFetchService).fetchAndSaveMapping("100");
        assertEquals(threshold, mappingSyncService.backoffs.size());
        assertFalse(mappingSyncService.backoffs.containsKey((long) threshold + 1), "Skipped mapping is not charged");
        assertFalse(mappingSyncService.backoffs.containsKey(100L));
    }

    @Test
    void runSyncRound_PersistentlyFailingMappings_DoNotBlockOthersAcrossRounds() {
        // 最旧的几条持续返回 500、触发熔断，其后一条正常：下一轮应换由正常条目先请求
        int threshold = PlatformSyncRunner.CIRCUIT_THRESHOLD;
        String healthy = String.valueOf(threshold + 1);
        givenCandidates(malCandidates(threshold + 1));
        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(malFetchService);
        when(malFetchService.fetchAndSaveMapping(anyString())).thenThrow(fetchFailure(500));
        doReturn(saved(NOW.plus(Duration.ofHours(1)))).when(malFetchService).fetchAndSaveMapping(healthy);

        mappingSyncService.runSyncRound(NOW);
        verify(malFetchService, never()).fetchAndSaveMapping(healthy);

        // 第一轮的退避此时正好到期，但有失败记录的条目排在最后
        mappingSyncService.runSyncRound(NOW.plus(Duration.ofHours(1)));

        verify(malFetchService).fetchAndSaveMapping(healthy);
        assertFalse(mappingSyncService.backoffs.containsKey((long) threshold + 1));
        assertEquals(Duration.ofHours(2), mappingSyncService.backoffs.get(1L).delay(),
                "Persistently failing mappings keep backing off");
    }

    // ==================== 辅助方法 ====================

    private static boolean isDue(SyncCandidate candidate) {
        return MappingSyncService.isDue(candidate, TODAY);
    }

    /** 上海时区指定日期的整点时刻。 */
    private static Instant at(LocalDate date, int hour) {
        return date.atTime(hour, 0).atZone(MappingSyncService.SYNC_ZONE).toInstant();
    }

    /** 返回最近排班日在 daysAgo 天前的 animeId（daysAgo 需小于所在档位的同步间隔）。 */
    private static long scheduledDaysAgo(int daysAgo) {
        return TODAY.toEpochDay() - daysAgo;
    }

    /** 开播 10 天、每天同步的候选 Mapping；platformId 与 mappingId 取相同数字，便于对照。 */
    private static SyncCandidate candidate(long mappingId, Platform platform, Instant updateTime) {
        return new SyncCandidate(mappingId, 1L, TODAY.minusDays(10), platform, String.valueOf(mappingId), updateTime);
    }

    private static Mapping saved(Instant updateTime) {
        Mapping mapping = new Mapping();
        mapping.setUpdateTime(updateTime);
        return mapping;
    }

    /** mappingId 从 1 开始的 MAL 候选，update_time 依次递增以固定处理顺序。 */
    private static List<SyncCandidate> malCandidates(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(id -> candidate(id, Platform.MyAnimeList, at(TODAY.minusDays(1), id)))
                .toList();
    }

    /** 与真实调用链一样，把平台响应异常包装在 FetchFailedException 里。 */
    private static FetchFailedException fetchFailure(int status) {
        return new FetchFailedException(Platform.MyAnimeList, "1",
                WebClientResponseException.create(status, "Status " + status, null, null, null));
    }

    private void givenCandidates(SyncCandidate... candidates) {
        givenCandidates(List.of(candidates));
    }

    private void givenCandidates(List<SyncCandidate> candidates) {
        when(mappingRepository.findSyncCandidates(ReviewStatus.APPROVED, TODAY, at(TODAY, 0)))
                .thenReturn(candidates);
    }
}
