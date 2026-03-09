package com.yukiani.service;

import com.yukiani.entity.Anime;
import com.yukiani.entity.Mapping;
import com.yukiani.entity.Platform;
import com.yukiani.entity.ReviewStatus;
import com.yukiani.repo.AnimeRepository;
import com.yukiani.service.fetch.AbstractAnimeFetchService;
import com.yukiani.service.fetch.FetchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * MappingSyncService 测试类
 */
@ExtendWith(MockitoExtension.class)
class MappingSyncServiceTest {

    @Mock
    private AnimeRepository animeRepository;

    @Mock
    private FetchService fetchService;

    @Mock
    private MetricService metricService;

    @Mock
    private AbstractAnimeFetchService malFetchService;

    @InjectMocks
    private MappingSyncService mappingSyncService;

    @BeforeEach
    void setUp() {
        // 重置 pendingMappings 列表
        List<Mapping> pendingMappings = Collections.synchronizedList(new ArrayList<>());
        ReflectionTestUtils.setField(mappingSyncService, "pendingMappings", pendingMappings);
    }

    /**
     * 测试收集映射 - 成功场景（使用0-30天的动漫确保稳定性）
     */
    @Test
    void testCollectMappingsForSync_Success() {
        // 准备测试数据 - 都使用 0-30 天内的动漫，确保一定会被同步
        Anime anime1 = createAnimeWithMapping(1L, LocalDate.now().minusDays(10), Platform.MyAnimeList, "12345");
        Anime anime2 = createAnimeWithMapping(2L, LocalDate.now().minusDays(20), Platform.MyAnimeList, "67890");

        // 执行测试并获取结果
        List<Mapping> pendingMappings = collectAndGetPendingMappings(List.of(anime1, anime2));

        // 验证结果
        assertNotNull(pendingMappings);
        assertEquals(2, pendingMappings.size());
        assertTrue(pendingMappings.contains(anime1.getMappings().getFirst()));
        assertTrue(pendingMappings.contains(anime2.getMappings().getFirst()));

        verify(animeRepository, times(1)).findAllByReviewStatus(ReviewStatus.APPROVED);
    }

    /**
     * 测试收集映射 - 渐进式同步策略
     * 规则：
     * - 未来开播：不同步
     * - 0-30天：每天同步
     * - 30-90天：隔天同步（today % 2 == animeId % 2）
     * - 90-180天：每周同步（today % 7 == animeId % 7）
     * - 180天以上：每月同步（today % 30 == animeId % 30）
     */
    @Test
    void testCollectMappingsForSync_FilterByDate() {
        // 准备测试数据
        Anime newAnime = createAnimeWithMapping(1L, LocalDate.now().minusDays(15), Platform.MyAnimeList, "12345");
        Anime mediumAnime = createAnimeWithMapping(createMatchingAnimeId(2), LocalDate.now().minusDays(60), Platform.MyAnimeList, "67890");
        Anime mediumAnimeSkip = createAnimeWithMapping(createNonMatchingAnimeId(2), LocalDate.now().minusDays(60), Platform.MyAnimeList, "11111");
        Anime futureAnime = createAnimeWithMapping(10L, LocalDate.now().plusDays(10), Platform.MyAnimeList, "22222");

        // 执行测试并获取结果
        List<Mapping> pendingMappings = collectAndGetPendingMappings(
                List.of(newAnime, mediumAnime, mediumAnimeSkip, futureAnime));

        // 验证结果
        assertNotNull(pendingMappings);
        assertTrue(pendingMappings.contains(newAnime.getMappings().getFirst()),
                "New anime (0-30 days) should be synced");
        assertTrue(pendingMappings.contains(mediumAnime.getMappings().getFirst()),
                "Medium anime matching mod should be synced");
        assertFalse(pendingMappings.contains(mediumAnimeSkip.getMappings().getFirst()),
                "Medium anime not matching mod should be skipped");
        assertFalse(pendingMappings.contains(futureAnime.getMappings().getFirst()),
                "Future anime should not be synced");
    }

    /**
     * 测试收集映射 - 跳过没有开播日期的动漫
     */
    @Test
    void testCollectMappingsForSync_SkipNoStartDate() {
        // 准备测试数据
        Anime animeWithDate = createAnimeWithMapping(1L, LocalDate.now().minusDays(15), Platform.MyAnimeList, "12345");
        Anime animeWithoutDate = createAnimeWithMapping(2L, null, Platform.MyAnimeList, "67890");

        // 执行测试并获取结果
        List<Mapping> pendingMappings = collectAndGetPendingMappings(List.of(animeWithDate, animeWithoutDate));

        // 验证结果 - 只收集有开播日期的动漫
        assertNotNull(pendingMappings);
        assertEquals(1, pendingMappings.size());
        assertEquals(animeWithDate.getMappings().getFirst(), pendingMappings.getFirst());
    }

    /**
     * 测试收集映射 - 清空前一天的失败记录
     */
    @Test
    void testCollectMappingsForSync_ClearPreviousPending() {
        // 模拟前一天有失败的 mappings
        List<Mapping> pendingMappings = getPendingMappings();
        Anime oldAnime = createAnimeWithMapping(1L, LocalDate.now().minusDays(15), Platform.MyAnimeList, "99999");
        pendingMappings.add(oldAnime.getMappings().getFirst());

        // 准备新一天的数据并执行测试
        Anime newAnime = createAnimeWithMapping(2L, LocalDate.now().minusDays(15), Platform.MyAnimeList, "12345");
        collectAndGetPendingMappings(List.of(newAnime));

        // 验证结果 - 旧的记录被清空，只包含新的
        assertEquals(1, pendingMappings.size());
        assertEquals(newAnime.getMappings().getFirst(), pendingMappings.getFirst());
        assertFalse(pendingMappings.contains(oldAnime.getMappings().getFirst()));
    }

    /**
     * 测试收集映射 - 处理多个 mappings
     */
    @Test
    void testCollectMappingsForSync_MultipleMapping() {
        // 准备测试数据 - 一个动漫有多个 mappings
        Anime anime = createAnimeWithMappings(1L, LocalDate.now().minusDays(15),
                Platform.MyAnimeList, "12345", Platform.Bangumi, "67890");

        // 执行测试并获取结果
        List<Mapping> pendingMappings = collectAndGetPendingMappings(List.of(anime));

        // 验证结果 - 所有 mappings 都被收集
        assertNotNull(pendingMappings);
        assertEquals(2, pendingMappings.size());
        assertTrue(pendingMappings.containsAll(anime.getMappings()));
    }

    /**
     * 测试渐进式同步策略 - 90-180天范围
     */
    @Test
    void testCollectMappingsForSync_OldAnime90To180Days() {
        // 准备测试数据
        Anime oldAnimeMatch = createAnimeWithMapping(createMatchingAnimeId(7),
                LocalDate.now().minusDays(120), Platform.MyAnimeList, "12345");
        Anime oldAnimeSkip = createAnimeWithMapping(createNonMatchingAnimeId(7),
                LocalDate.now().minusDays(120), Platform.MyAnimeList, "67890");

        // 执行测试并获取结果
        List<Mapping> pendingMappings = collectAndGetPendingMappings(List.of(oldAnimeMatch, oldAnimeSkip));

        // 验证结果 - 只有匹配的会被同步（每周同步一次）
        assertNotNull(pendingMappings);
        assertTrue(pendingMappings.contains(oldAnimeMatch.getMappings().getFirst()),
                "Old anime (90-180 days) matching mod 7 should be synced");
        assertFalse(pendingMappings.contains(oldAnimeSkip.getMappings().getFirst()),
                "Old anime (90-180 days) not matching mod 7 should be skipped");
    }

    /**
     * 测试渐进式同步策略 - 180天以上
     */
    @Test
    void testCollectMappingsForSync_VeryOldAnimeOver180Days() {
        // 准备测试数据
        Anime veryOldAnimeMatch = createAnimeWithMapping(createMatchingAnimeId(30),
                LocalDate.now().minusDays(365), Platform.MyAnimeList, "12345");
        Anime veryOldAnimeSkip = createAnimeWithMapping(createNonMatchingAnimeId(30),
                LocalDate.now().minusDays(365), Platform.MyAnimeList, "67890");

        // 执行测试并获取结果
        List<Mapping> pendingMappings = collectAndGetPendingMappings(List.of(veryOldAnimeMatch, veryOldAnimeSkip));

        // 验证结果 - 只有匹配的会被同步（每月同步一次）
        assertNotNull(pendingMappings);
        assertTrue(pendingMappings.contains(veryOldAnimeMatch.getMappings().getFirst()),
                "Very old anime (>180 days) matching mod 30 should be synced");
        assertFalse(pendingMappings.contains(veryOldAnimeSkip.getMappings().getFirst()),
                "Very old anime (>180 days) not matching mod 30 should be skipped");
    }

    /**
     * 测试同步单个映射 - 成功场景
     */
    @Test
    void testSyncMapping_Success() {
        // 准备测试数据
        Anime anime = createAnimeWithMapping(1L, LocalDate.now().minusDays(30), Platform.MyAnimeList, "12345");
        Mapping mapping = anime.getMappings().getFirst();
        List<Mapping> pendingMappings = getPendingMappings();
        pendingMappings.add(mapping);

        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(malFetchService);
        when(malFetchService.fetchAndSaveMapping("12345")).thenReturn(mapping);

        // 执行测试
        mappingSyncService.syncMapping(mapping);

        // 验证结果
        verify(fetchService, times(1)).getFetchService(Platform.MyAnimeList);
        verify(malFetchService, times(1)).fetchAndSaveMapping("12345");
        assertFalse(pendingMappings.contains(mapping)); // 成功后应该从队列中移除
    }

    /**
     * 测试同步单个映射 - 找不到对应的 FetchService
     */
    @Test
    void testSyncMapping_NoFetchService() {
        // 准备测试数据
        Anime anime = createAnimeWithMapping(1L, LocalDate.now().minusDays(30), Platform.Bangumi, "12345");
        Mapping mapping = anime.getMappings().getFirst();
        List<Mapping> pendingMappings = getPendingMappings();
        pendingMappings.add(mapping);

        when(fetchService.getFetchService(Platform.Bangumi)).thenReturn(null);

        // 执行测试
        mappingSyncService.syncMapping(mapping);

        // 验证结果
        verify(fetchService, times(1)).getFetchService(Platform.Bangumi);
        verify(malFetchService, never()).fetchAndSaveMapping(anyString());
        assertTrue(pendingMappings.contains(mapping)); // 未找到服务，保留在队列中
    }

    /**
     * 测试同步单个映射 - 抛出异常
     */
    @Test
    void testSyncMapping_ThrowsException() {
        // 准备测试数据
        Anime anime = createAnimeWithMapping(1L, LocalDate.now().minusDays(30), Platform.MyAnimeList, "12345");
        Mapping mapping = anime.getMappings().getFirst();
        List<Mapping> pendingMappings = getPendingMappings();
        pendingMappings.add(mapping);

        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(malFetchService);
        when(malFetchService.fetchAndSaveMapping("12345")).thenThrow(new RuntimeException("API Error"));

        // 执行测试 - 不应该抛出异常
        assertDoesNotThrow(() -> mappingSyncService.syncMapping(mapping));

        // 验证结果
        verify(fetchService, times(1)).getFetchService(Platform.MyAnimeList);
        verify(malFetchService, times(1)).fetchAndSaveMapping("12345");
        assertTrue(pendingMappings.contains(mapping)); // 异常时保留在队列中
    }

    /**
     * 测试处理待同步映射 - 成功场景
     */
    @Test
    void testProcessPendingMappings_Success() {
        // 准备测试数据
        Anime anime1 = createAnimeWithMapping(1L, LocalDate.now().minusDays(30), Platform.MyAnimeList, "12345");
        Anime anime2 = createAnimeWithMapping(2L, LocalDate.now().minusDays(60), Platform.MyAnimeList, "67890");

        List<Mapping> pendingMappings = getPendingMappings();
        pendingMappings.add(anime1.getMappings().getFirst());
        pendingMappings.add(anime2.getMappings().getFirst());

        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(malFetchService);
        when(malFetchService.fetchAndSaveMapping(anyString()))
                .thenReturn(anime1.getMappings().getFirst(), anime2.getMappings().getFirst());

        // 执行测试
        mappingSyncService.processPendingMappings();

        // 验证结果
        verify(fetchService, times(2)).getFetchService(Platform.MyAnimeList);
        verify(malFetchService, times(1)).fetchAndSaveMapping("12345");
        verify(malFetchService, times(1)).fetchAndSaveMapping("67890");
        assertEquals(0, pendingMappings.size()); // 全部处理完成
    }

    /**
     * 测试处理待同步映射 - 空队列
     */
    @Test
    void testProcessPendingMappings_EmptyQueue() {
        // 准备测试数据 - 空队列
        List<Mapping> pendingMappings = getPendingMappings();
        assertTrue(pendingMappings.isEmpty());

        // 执行测试
        mappingSyncService.processPendingMappings();

        // 验证结果
        verify(fetchService, never()).getFetchService(any());
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取 pendingMappings 列表（消除重复的 ReflectionTestUtils 调用）
     */
    @SuppressWarnings("unchecked")
    private List<Mapping> getPendingMappings() {
        return (List<Mapping>) ReflectionTestUtils.getField(mappingSyncService, "pendingMappings");
    }

    /**
     * 创建测试用 Anime 对象
     */
    private Anime createAnime(Long id, LocalDate startDate, ReviewStatus status) {
        Anime anime = new Anime();
        anime.setAnimeId(id);
        anime.setStartDate(startDate);
        anime.setReviewStatus(status);
        anime.setMappings(new ArrayList<>());
        return anime;
    }

    /**
     * 创建测试用 Mapping 对象
     */
    private Mapping createMapping(Platform platform, String platformId, Anime anime) {
        Mapping mapping = new Mapping();
        mapping.setSourcePlatform(platform);
        mapping.setPlatformId(platformId);
        mapping.setAnime(anime);
        return mapping;
    }

    /**
     * 创建带 Mapping 的 Anime（简化创建流程）
     */
    private Anime createAnimeWithMapping(Long id, LocalDate startDate, Platform platform, String platformId) {
        Anime anime = createAnime(id, startDate, ReviewStatus.APPROVED);
        Mapping mapping = createMapping(platform, platformId, anime);
        anime.setMappings(List.of(mapping));
        return anime;
    }

    /**
     * 创建带多个 Mappings 的 Anime
     */
    private Anime createAnimeWithMappings(Long id, LocalDate startDate, Object... platformAndIds) {
        Anime anime = createAnime(id, startDate, ReviewStatus.APPROVED);
        List<Mapping> mappings = new ArrayList<>();
        for (int i = 0; i < platformAndIds.length; i += 2) {
            Platform platform = (Platform) platformAndIds[i];
            String platformId = (String) platformAndIds[i + 1];
            mappings.add(createMapping(platform, platformId, anime));
        }
        anime.setMappings(mappings);
        return anime;
    }

    /**
     * 创建匹配今天取模规则的动漫ID（用于测试渐进式同步）
     */
    private long createMatchingAnimeId(int modulo) {
        long today = LocalDate.now().toEpochDay();
        return today % modulo;
    }

    /**
     * 创建不匹配今天取模规则的动漫ID
     */
    private long createNonMatchingAnimeId(int modulo) {
        long today = LocalDate.now().toEpochDay();
        return (today + 1) % modulo;
    }

    /**
     * 执行收集并返回 pendingMappings（简化测试流程）
     */
    private List<Mapping> collectAndGetPendingMappings(List<Anime> animeList) {
        when(animeRepository.findAllByReviewStatus(ReviewStatus.APPROVED))
                .thenReturn(animeList);
        mappingSyncService.collectMappingsForSync();
        return getPendingMappings();
    }
}
