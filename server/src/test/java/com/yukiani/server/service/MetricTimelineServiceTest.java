package com.yukiani.server.service;

import com.yukiani.server.entity.MappingMetricHistory;
import com.yukiani.server.entity.Platform;
import com.yukiani.server.repo.MappingMetricHistoryRepository;
import com.yukiani.server.service.MetricTimelineService.AnimePoint;
import com.yukiani.server.service.MetricTimelineService.MappingPoint;
import com.yukiani.server.entity.PlatformKey;
import com.yukiani.server.util.MetricDateUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class MetricTimelineServiceTest {

    // 默认夹具仅在 D1（Bangumi）和 D3（MyAnimeList）有观测。
    private static final LocalDate D0 = LocalDate.parse("2026-09-01");
    private static final LocalDate D1 = LocalDate.parse("2026-09-02");
    private static final LocalDate D2 = LocalDate.parse("2026-09-03");
    private static final LocalDate D3 = LocalDate.parse("2026-09-04");
    private static final LocalDate D4 = LocalDate.parse("2026-09-05");

    private static final PlatformKey BANGUMI = new PlatformKey(Platform.Bangumi, "bgm-1");
    private static final PlatformKey MAL = new PlatformKey(Platform.MyAnimeList, "mal-1");
    private static final PlatformKey ANILIST = new PlatformKey(Platform.AniList, "ani-1");
    private static final List<PlatformKey> KEYS = List.of(BANGUMI, MAL);

    // 测试配置下仅 Bangumi 参与计算的综合评分。
    private static final double BANGUMI_ONLY_SCORE = 65.1515151515;
    // 两个平台的评分权重为 Bangumi : MyAnimeList = 2 : 1。
    private static final double BOTH_SCORE = 65.6565656566;

    @Resource
    MetricTimelineService metricTimelineService;

    @Resource
    MappingMetricHistoryRepository historyRepository;

    @BeforeEach
    public void setUp() {
        record(BANGUMI, noonOf(D1), 7.0, 800.0);
        record(MAL, noonOf(D3), 8.0, 25000.0);
    }

    @Test
    public void testAnimeMetricAt_BeforeAnyObservation_ReturnsNull() {
        assertNull(metricTimelineService.animeMetricAt(KEYS, D0));
    }

    @Test
    public void testAnimeMetricAt_IncludesSameDayObservation() {
        AnimePoint point = metricTimelineService.animeMetricAt(KEYS, D1);

        assertEquals(D1, point.date());
        assertEquals(BANGUMI_ONLY_SCORE, point.averageScore(), 0.0001);
    }

    @Test
    public void testAnimeMetricAt_OnlyOnePlatformObserved_ExcludesTheOther() {
        AnimePoint point = metricTimelineService.animeMetricAt(KEYS, D2);

        // MyAnimeList 尚无观测，不参与加权。
        assertEquals(D1, point.date());
        assertEquals(BANGUMI_ONLY_SCORE, point.averageScore(), 0.0001);
        assertEquals(20000.0, point.popularity(), 0.0001);
    }

    @Test
    public void testAnimeMetricAt_CarriesForwardStaleObservation() {
        AnimePoint point = metricTimelineService.animeMetricAt(KEYS, D4);

        // D4 仍沿用 Bangumi 的 D1 观测。
        assertEquals(BOTH_SCORE, point.averageScore(), 0.0001);
        assertEquals(D3, point.date());
        assertEquals(30000.0, point.popularity(), 0.0001);
    }

    @Test
    public void testAnimeHistory_CarriesInObservationBeforeRange() {
        List<AnimePoint> points = metricTimelineService.animeHistory(KEYS, D2, D4);

        // 起点 D2 不补点，D3 沿用区间前的 Bangumi 观测。
        assertEquals(1, points.size());
        assertEquals(D3, points.get(0).date());
        assertEquals(BOTH_SCORE, points.get(0).averageScore(), 0.0001);
    }

    @Test
    public void testAnimeHistory_NoObservationInRange_ReturnsEmpty() {
        // 区间前有观测，但区间内没有观测时不生成采样点。
        assertTrue(metricTimelineService.animeHistory(KEYS, D2, D2).isEmpty());
    }

    @Test
    public void testAnimeHistory_ContainsOnlyObservationDates() {
        List<AnimePoint> points = metricTimelineService.animeHistory(KEYS, D0, D4);

        assertEquals(2, points.size());
        assertEquals(D1, points.get(0).date());
        assertEquals(D3, points.get(1).date());
    }

    @Test
    public void testAnimeHistory_GridIsUnionOfObservationDates() {
        record(BANGUMI, noonOf(D3), 7.5, 900.0);

        List<AnimePoint> points = metricTimelineService.animeHistory(KEYS, D0, D4);

        // 两个平台同日观测只生成一个综合指标点。
        assertEquals(2, points.size());
        assertEquals(D1, points.get(0).date());
        assertEquals(D3, points.get(1).date());
    }

    @Test
    public void testAnimeHistory_EmptyKeys_ReturnsEmpty() {
        assertTrue(metricTimelineService.animeHistory(List.of(), D0, D4).isEmpty());
    }

    @Test
    public void testMappingHistory_ReturnsObservationsWithoutPadding() {
        List<MappingPoint> points = metricTimelineService.mappingHistory(BANGUMI, D0, D4);

        assertEquals(1, points.size());
        MappingPoint point = points.get(0);
        assertEquals(D1, point.date());
        assertEquals(7.0, point.rawScore(), 0.0001);
        assertEquals(BANGUMI_ONLY_SCORE, point.normalizedScore(), 0.0001);
        assertEquals(800.0, point.rawPopularity(), 0.0001);
        assertEquals(10000.0, point.normalizedPopularity(), 0.0001);
    }

    @Test
    public void testMappingHistory_ExcludesObservationsOutsideRange() {
        assertTrue(metricTimelineService.mappingHistory(MAL, D0, D2).isEmpty());
    }

    @Test
    public void testMappingHistory_SameDayObservations_KeepsLast() {
        record(BANGUMI, noonOf(D1).plusSeconds(3600), 8.0, 800.0);

        List<MappingPoint> points = metricTimelineService.mappingHistory(BANGUMI, D0, D4);

        assertEquals(1, points.size());
        assertEquals(D1, points.get(0).date());
        assertEquals(8.0, points.get(0).rawScore(), 0.0001);
    }

    @Test
    public void testMappingHistory_DateBoundaryFollowsSiteTimeZone() {
        // UTC 的 09-02 16:30 对应北京时间的 09-03 00:30（D2）。
        record(ANILIST, Instant.parse("2026-09-02T16:30:00Z"), 70.0, 16000.0);

        List<MappingPoint> points = metricTimelineService.mappingHistory(ANILIST, D0, D4);

        assertEquals(1, points.size());
        assertEquals(D2, points.get(0).date());
    }

    @Test
    public void testMappingMetricAt_BeforeAnyObservation_ReturnsNull() {
        assertNull(metricTimelineService.mappingMetricAt(MAL, D2));
    }

    @Test
    public void testMappingMetricAt_IncludesSameDayObservation() {
        MappingPoint point = metricTimelineService.mappingMetricAt(MAL, D3);

        assertEquals(D3, point.date());
        assertEquals(8.0, point.rawScore(), 0.0001);
    }

    @Test
    public void testMappingMetricAt_ReturnsStaleObservationWithItsOwnDate() {
        MappingPoint point = metricTimelineService.mappingMetricAt(BANGUMI, D4);

        assertEquals(D1, point.date());
        assertEquals(7.0, point.rawScore(), 0.0001);
        assertEquals(BANGUMI_ONLY_SCORE, point.normalizedScore(), 0.0001);
    }

    @Test
    public void testMappingMetricAt_MatchesLastPointOfHistory() {
        record(BANGUMI, noonOf(D3), 7.5, 900.0);
        record(BANGUMI, noonOf(D3).plusSeconds(3600), 7.8, 950.0);

        for (LocalDate day : List.of(D1, D2, D3, D4)) {
            List<MappingPoint> history = metricTimelineService.mappingHistory(BANGUMI, D0, day);
            MappingPoint point = metricTimelineService.mappingMetricAt(BANGUMI, day);

            // 区间查询和时点查询采用不同实现，应选中同一条观测。
            assertEquals(history.get(history.size() - 1), point, "day=" + day);
        }
    }

    @Test
    public void testSameTimestampUsesLatestIdAndDoesNotRestoreNullMetrics() {
        Instant at = noonOf(D3);
        record(BANGUMI, at, 8.0, 800.0);
        record(BANGUMI, at, null, null);

        MappingPoint point = metricTimelineService.mappingMetricAt(BANGUMI, D3);
        assertNull(point.rawScore());
        assertNull(point.normalizedScore());
        assertNull(point.rawPopularity());
        assertNull(point.normalizedPopularity());
        assertEquals(point, metricTimelineService.mappingHistory(BANGUMI, D3, D3).getFirst());
        // 沿用区间前的空观测，不回退到更早的有效指标。
        record(MAL, noonOf(D4), null, null);
        AnimePoint aggregate = metricTimelineService.animeHistory(KEYS, D4, D4).getFirst();
        assertNull(aggregate.averageScore());
        assertEquals(0.0, aggregate.popularity());
    }

    @Test
    public void testMissingPopularityAggregatesToZero() {
        record(ANILIST, noonOf(D1), null, null);
        AnimePoint missing = metricTimelineService.animeMetricAt(List.of(ANILIST), D1);
        assertNull(missing.averageScore());
        assertEquals(0.0, missing.popularity());
        record(ANILIST, noonOf(D2), null, 0.0);
        AnimePoint zero = metricTimelineService.animeMetricAt(List.of(ANILIST), D2);
        assertNull(zero.averageScore());
        assertEquals(0.0, zero.popularity());
    }

    /** 使用站点时区的正午，避开日期边界。 */
    private Instant noonOf(LocalDate date) {
        return date.atTime(12, 0).atZone(MetricDateUtil.ZONE).toInstant();
    }

    private void record(PlatformKey key, Instant at, Double rawScore, Double rawPopularity) {
        historyRepository.save(new MappingMetricHistory(
                key, rawScore, rawPopularity, at));
        historyRepository.flush();
    }
}
