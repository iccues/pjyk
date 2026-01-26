package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnimeAggregationServiceTest {

    @Mock
    private MetricService metricService;

    @Mock
    private InfoService infoService;

    @InjectMocks
    private AnimeAggregationService animeAggregationService;

    private Anime anime1;
    private Anime anime2;
    private Mapping mapping1;
    private Mapping mapping2;
    private Mapping mapping3;

    @BeforeEach
    void setUp() {
        anime1 = new Anime();
        anime2 = new Anime();
        
        mapping1 = new Mapping();
        mapping1.setSourcePlatform(Platform.Bangumi);
        
        mapping2 = new Mapping();
        mapping2.setSourcePlatform(Platform.MyAnimeList);
        
        mapping3 = new Mapping();
        mapping3.setSourcePlatform(Platform.AniList);
    }

    // ==================== addMappingWithMetrics 测试 ====================

    @Test
    void testAddMappingWithMetrics_NewMapping() {
        // 添加新 mapping 到空 anime
        animeAggregationService.addMappingWithMetrics(anime1, mapping1);

        // 验证 mapping 被正确添加
        assertTrue(anime1.getMappings().contains(mapping1));
        assertEquals(anime1, mapping1.getAnime());

        // 验证 metricService.calculateMetric() 和 infoService.aggregateInfo() 被调用一次
        verify(metricService, times(1)).calculateMetric(anime1);
        verify(infoService, times(1)).aggregateInfo(anime1);
    }

    @Test
    void testAddMappingWithMetrics_MappingFromAnotherAnime() {
        // 先将 mapping1 添加到 anime1
        anime1.addMapping(mapping1);

        // 将 mapping1 从 anime1 移动到 anime2
        animeAggregationService.addMappingWithMetrics(anime2, mapping1);

        // 验证 mapping1 从 anime1 移除并添加到 anime2
        assertFalse(anime1.getMappings().contains(mapping1));
        assertTrue(anime2.getMappings().contains(mapping1));
        assertEquals(anime2, mapping1.getAnime());

        // 验证两个 anime 的指标和信息都被重新计算
        verify(metricService, times(1)).calculateMetric(anime2);
        verify(metricService, times(1)).calculateMetric(anime1);
        verify(infoService, times(1)).aggregateInfo(anime2);
        verify(infoService, times(1)).aggregateInfo(anime1);
    }

    @Test
    void testAddMappingWithMetrics_MappingFromSameAnime() {
        // 先将 mapping1 添加到 anime1
        anime1.addMapping(mapping1);

        // 再次将 mapping1 添加到 anime1（相同的 anime）
        animeAggregationService.addMappingWithMetrics(anime1, mapping1);

        // 验证 mapping 仍然属于 anime1
        assertTrue(anime1.getMappings().contains(mapping1));
        assertEquals(anime1, mapping1.getAnime());

        // 验证只计算一次指标和信息（因为 oldAnime == anime）
        verify(metricService, times(1)).calculateMetric(anime1);
        verify(infoService, times(1)).aggregateInfo(anime1);
    }

    // ==================== removeMappingWithMetrics 测试 ====================

    @Test
    void testRemoveMappingWithMetrics_ExistingMapping() {
        // 先添加 mapping
        anime1.addMapping(mapping1);

        // 移除 mapping
        animeAggregationService.removeMappingWithMetrics(anime1, mapping1);

        // 验证 mapping 被移除
        assertFalse(anime1.getMappings().contains(mapping1));
        assertNull(mapping1.getAnime());

        // 验证 metricService.calculateMetric() 和 infoService.aggregateInfo() 被调用
        verify(metricService, times(1)).calculateMetric(anime1);
        verify(infoService, times(1)).aggregateInfo(anime1);
    }

    // ==================== addMappingIfAbsent 测试 ====================

    @Test
    void testAddMappingIfAbsent_WhenAbsent() {
        // anime1 没有 Bangumi 平台的 mapping
        animeAggregationService.addMappingIfAbsent(anime1, mapping1);

        // 验证 mapping 被添加
        assertTrue(anime1.getMappings().contains(mapping1));
        assertEquals(anime1, mapping1.getAnime());

        // 验证指标和信息被计算
        verify(metricService, times(1)).calculateMetric(anime1);
        verify(infoService, times(1)).aggregateInfo(anime1);
    }

    @Test
    void testAddMappingIfAbsent_WhenPresent() {
        // anime1 已有 Bangumi 平台的 mapping
        anime1.addMapping(mapping1);

        // 尝试添加另一个 Bangumi 平台的 mapping
        Mapping anotherBangumiMapping = new Mapping();
        anotherBangumiMapping.setSourcePlatform(Platform.Bangumi);

        animeAggregationService.addMappingIfAbsent(anime1, anotherBangumiMapping);

        // 验证新 mapping 不被添加
        assertFalse(anime1.getMappings().contains(anotherBangumiMapping));
        assertNull(anotherBangumiMapping.getAnime());

        // 验证只有原来的 mapping1
        assertEquals(1, anime1.getMappings().size());
        assertTrue(anime1.getMappings().contains(mapping1));

        // 验证指标不被计算（因为没有添加）
        verify(metricService, never()).calculateMetric(any());
    }

    // ==================== upsertMapping 测试 ====================

    @Test
    void testUpsertMapping_WhenAbsent() {
        // anime1 没有 Bangumi 平台的 mapping
        animeAggregationService.upsertMapping(anime1, mapping1);

        // 验证 mapping 被添加
        assertTrue(anime1.getMappings().contains(mapping1));
        assertEquals(anime1, mapping1.getAnime());

        // 验证指标和信息被计算一次
        verify(metricService, times(1)).calculateMetric(anime1);
        verify(infoService, times(1)).aggregateInfo(anime1);
    }

    @Test
    void testUpsertMapping_WhenPresent() {
        // anime1 已有 Bangumi 平台的 mapping
        Mapping oldBangumiMapping = new Mapping();
        oldBangumiMapping.setSourcePlatform(Platform.Bangumi);
        anime1.addMapping(oldBangumiMapping);

        // 用新的 Bangumi mapping 替换
        animeAggregationService.upsertMapping(anime1, mapping1);

        // 验证旧 mapping 被移除，新 mapping 被添加
        assertFalse(anime1.getMappings().contains(oldBangumiMapping));
        assertTrue(anime1.getMappings().contains(mapping1));
        assertEquals(anime1, mapping1.getAnime());
        assertNull(oldBangumiMapping.getAnime());

        // 验证指标和信息被计算两次（移除时一次，添加时一次）
        verify(metricService, times(2)).calculateMetric(anime1);
        verify(infoService, times(2)).aggregateInfo(anime1);
    }

    // ==================== mergeAnime 测试 ====================

    @Test
    void testMergeAnime_NoConflict() {
        // anime1 有 Bangumi mapping
        anime1.addMapping(mapping1);

        // anime2 有 MyAnimeList 和 AniList mapping
        anime2.addMapping(mapping2);
        anime2.addMapping(mapping3);

        // 合并 anime2 到 anime1
        Anime result = animeAggregationService.mergeAnime(anime1, anime2);

        // 验证返回的是 anime1
        assertEquals(anime1, result);

        // 验证所有 mapping 都被合并到 anime1
        assertEquals(3, anime1.getMappings().size());
        assertTrue(anime1.getMappings().contains(mapping1));
        assertTrue(anime1.getMappings().contains(mapping2));
        assertTrue(anime1.getMappings().contains(mapping3));

        // 验证 mapping 的归属
        assertEquals(anime1, mapping1.getAnime());
        assertEquals(anime1, mapping2.getAnime());
        assertEquals(anime1, mapping3.getAnime());

        // 验证指标和信息被计算（每次添加都会计算，但 addMappingWithMetrics 会为移动的 mapping 计算两次：旧 anime 和新 anime）
        // mapping2 和 mapping3 从 anime2 移动到 anime1，每个都会触发 2 次计算（anime1 一次，anime2 一次）
        verify(metricService, times(2)).calculateMetric(anime1);
        verify(metricService, times(2)).calculateMetric(anime2);
        verify(infoService, times(2)).aggregateInfo(anime1);
        verify(infoService, times(2)).aggregateInfo(anime2);
    }

    @Test
    void testMergeAnime_WithConflict() {
        // anime1 有 Bangumi mapping
        anime1.addMapping(mapping1);

        // anime2 也有 Bangumi mapping（冲突）和 MyAnimeList mapping
        Mapping conflictBangumiMapping = new Mapping();
        conflictBangumiMapping.setSourcePlatform(Platform.Bangumi);
        anime2.addMapping(conflictBangumiMapping);
        anime2.addMapping(mapping2);

        // 合并 anime2 到 anime1
        Anime result = animeAggregationService.mergeAnime(anime1, anime2);

        // 验证返回的是 anime1
        assertEquals(anime1, result);

        // 验证只保留 anime1 的 Bangumi mapping，不添加 anime2 的重复平台
        assertEquals(2, anime1.getMappings().size());
        assertTrue(anime1.getMappings().contains(mapping1));
        assertTrue(anime1.getMappings().contains(mapping2));
        assertFalse(anime1.getMappings().contains(conflictBangumiMapping));

        // 验证 mapping 的归属
        assertEquals(anime1, mapping1.getAnime());
        assertEquals(anime1, mapping2.getAnime());

        // conflictBangumiMapping 不会被添加（因为 anime1 已有 Bangumi）
        // mapping2 会被添加，触发 2 次计算（anime1 一次，anime2 一次）
        verify(metricService, times(1)).calculateMetric(anime1);
        verify(metricService, times(1)).calculateMetric(anime2);
        verify(infoService, times(1)).aggregateInfo(anime1);
        verify(infoService, times(1)).aggregateInfo(anime2);
    }

    @Test
    void testMergeAnime_EmptySource() {
        // anime1 有 Bangumi mapping
        anime1.addMapping(mapping1);

        // anime2 没有 mapping
        // 合并 anime2 到 anime1
        Anime result = animeAggregationService.mergeAnime(anime1, anime2);

        // 验证返回的是 anime1
        assertEquals(anime1, result);

        // 验证 anime1 不变
        assertEquals(1, anime1.getMappings().size());
        assertTrue(anime1.getMappings().contains(mapping1));

        // 验证指标不被计算（因为没有添加任何 mapping）
        verify(metricService, never()).calculateMetric(any());
    }
}
