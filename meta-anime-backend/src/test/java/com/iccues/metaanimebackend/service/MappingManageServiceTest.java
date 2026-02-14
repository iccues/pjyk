package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import com.iccues.metaanimebackend.exception.ResourceAlreadyExistsException;
import com.iccues.metaanimebackend.exception.ResourceNotFoundException;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import com.iccues.metaanimebackend.service.fetch.AbstractAnimeFetchService;
import com.iccues.metaanimebackend.service.fetch.FetchService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MappingManageService 的单元测试
 * 测试管理员的映射管理操作
 */
@SpringBootTest
@Transactional
public class MappingManageServiceTest {

    @Resource
    private AnimeRepository animeRepository;

    @Resource
    private MappingRepository mappingRepository;

    @Resource
    private MappingManageService mappingManageService;

    @MockitoBean
    private MetricService metricService;

    @Resource
    private AnimeAggregationService animeAggregationService;

    @MockitoBean
    private FetchService fetchService;

    @AfterEach
    public void cleanup() {
        mappingRepository.deleteAll();
        animeRepository.deleteAll();
    }

    @Test
    public void testGetUnmappedMappingList_ReturnsOnlyUnmappedMappings() {
        // 创建动画
        Anime anime = createAnime("测试动画");

        // 创建已关联的映射
        Mapping mappedMapping = createMapping(Platform.MyAnimeList, "12345");
        anime.addMapping(mappedMapping);
        animeRepository.save(anime);

        // 创建未关联的映射
        createMapping(Platform.AniList, "67890");
        createMapping(Platform.Bangumi, "11111");

        // 执行查询
        List<Mapping> result = mappingManageService.getUnmappedMappingList();

        // 验证：只返回未关联的映射
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(m -> m.getAnime() == null));
    }

    @Test
    public void testGetUnmappedMappingList_EmptyResult_ReturnsEmptyList() {
        // 创建动画和已关联的映射
        Anime anime = createAnime("测试动画");
        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        anime.addMapping(mapping);
        animeRepository.save(anime);

        // 执行查询
        List<Mapping> result = mappingManageService.getUnmappedMappingList();

        // 验证：返回空列表
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testUpdateMappingAnime_LinkMappingToAnime_Success() {
        // 创建未关联的映射
        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        Long mappingId = mapping.getMappingId();

        // 创建动画
        Anime anime = createAnime("目标动画");
        Long animeId = anime.getAnimeId();

        // 执行关联
        Mapping result = mappingManageService.updateMappingAnime(mappingId, animeId);

        // 验证：映射已关联到动画
        assertNotNull(result);
        assertEquals(mappingId, result.getMappingId());
        assertNotNull(result.getAnime());
        assertEquals(animeId, result.getAnime().getAnimeId());

        // 验证：调用了指标计算
        verify(metricService, times(1)).calculateMetric(any(Anime.class));
    }

    @Test
    public void testUpdateMappingAnime_UnlinkMapping_Success() {
        // 创建动画和关联的映射
        Anime anime = createAnime("原动画");
        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        anime.addMapping(mapping);
        animeRepository.save(anime);

        Long mappingId = mapping.getMappingId();

        // 执行解除关联（animeId = null）
        Mapping result = mappingManageService.updateMappingAnime(mappingId, null);

        // 验证：映射已解除关联
        assertNotNull(result);
        assertEquals(mappingId, result.getMappingId());
        assertNull(result.getAnime());

        // 验证：调用了指标计算（只对原动画）
        verify(metricService, times(1)).calculateMetric(any(Anime.class));
    }

    @Test
    public void testUpdateMappingAnime_ChangeMappingAnime_Success() {
        // 创建两个动画
        Anime anime1 = createAnime("原动画");
        Anime anime2 = createAnime("新动画");

        // 创建映射并关联到anime1
        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        anime1.addMapping(mapping);
        animeRepository.save(anime1);

        Long mappingId = mapping.getMappingId();
        Long anime1Id = anime1.getAnimeId();
        Long anime2Id = anime2.getAnimeId();

        // 执行：将映射从anime1改为anime2
        Mapping result = mappingManageService.updateMappingAnime(mappingId, anime2Id);

        // 验证：映射已关联到anime2
        assertNotNull(result);
        assertEquals(mappingId, result.getMappingId());
        assertEquals(anime2Id, result.getAnime().getAnimeId());

        // 验证：调用了两次指标计算（原动画和新动画）
        verify(metricService, times(2)).calculateMetric(any(Anime.class));

        // 验证：anime1已解除关联
        Anime updatedAnime1 = animeRepository.findById(anime1Id).orElse(null);
        assertNotNull(updatedAnime1);
        assertTrue(updatedAnime1.getMappings().isEmpty());
    }

    @Test
    public void testUpdateMappingAnime_MappingNotFound_ThrowsException() {
        // 使用不存在的映射ID
        Long nonExistentMappingId = 99999L;
        Long animeId = createAnime("测试动画").getAnimeId();

        // 执行并验证异常
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> mappingManageService.updateMappingAnime(nonExistentMappingId, animeId)
        );

        assertTrue(exception.getMessage().contains("Mapping"));
        assertTrue(exception.getMessage().contains(nonExistentMappingId.toString()));
    }

    @Test
    public void testUpdateMappingAnime_AnimeNotFound_ThrowsException() {
        // 创建映射
        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        Long mappingId = mapping.getMappingId();

        // 使用不存在的动画ID
        Long nonExistentAnimeId = 99999L;

        // 执行并验证异常
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> mappingManageService.updateMappingAnime(mappingId, nonExistentAnimeId)
        );

        assertTrue(exception.getMessage().contains("Anime"));
        assertTrue(exception.getMessage().contains(nonExistentAnimeId.toString()));
    }

    @Test
    public void testDeleteMapping_Success_DeletesMapping() {
        // 创建未关联的映射
        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        Long mappingId = mapping.getMappingId();

        // 执行删除
        mappingManageService.deleteMapping(mappingId);

        // 验证：映射已被删除
        assertFalse(mappingRepository.existsById(mappingId));

        // 验证：未调用指标计算（因为没有关联动画）
        verify(metricService, never()).calculateMetric(any(Anime.class));
    }

    @Test
    public void testDeleteMapping_WithAnimeAssociation_UnlinksAndDeletes() {
        // 创建动画和关联的映射
        Anime anime = createAnime("测试动画");
        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        anime.addMapping(mapping);
        animeRepository.save(anime);

        Long mappingId = mapping.getMappingId();
        Long animeId = anime.getAnimeId();

        // 执行删除
        mappingManageService.deleteMapping(mappingId);

        // 验证：映射已被删除
        assertFalse(mappingRepository.existsById(mappingId));

        // 验证：动画仍然存在，但映射已解除
        Anime updatedAnime = animeRepository.findById(animeId).orElse(null);
        assertNotNull(updatedAnime);
        assertTrue(updatedAnime.getMappings().isEmpty());

        // 验证：调用了指标计算
        verify(metricService, times(1)).calculateMetric(any(Anime.class));
    }

    @Test
    public void testDeleteMapping_NotFound_ThrowsException() {
        // 使用不存在的映射ID
        Long nonExistentMappingId = 99999L;

        // 执行并验证异常
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> mappingManageService.deleteMapping(nonExistentMappingId)
        );

        assertTrue(exception.getMessage().contains("Mapping"));
        assertTrue(exception.getMessage().contains(nonExistentMappingId.toString()));
    }

    @Test
    public void testCreateMapping_Success_CreatesAndReturnsMapping() {
        // Mock FetchService
        Mapping mockMapping = new Mapping();
        mockMapping.setSourcePlatform(Platform.MyAnimeList);
        mockMapping.setPlatformId("12345");
        mockMapping.setRawScore(8.5);

        AbstractAnimeFetchService mockFetchService = mock(AbstractAnimeFetchService.class);
        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(mockFetchService);
        when(mockFetchService.fetchAndSaveMapping("12345")).thenReturn(mockMapping);

        // 执行创建
        Mapping result = mappingManageService.createMapping(Platform.MyAnimeList, "12345");

        // 验证
        assertNotNull(result);
        assertEquals(Platform.MyAnimeList, result.getSourcePlatform());
        assertEquals("12345", result.getPlatformId());
        assertEquals(8.5, result.getRawScore());

        // 验证调用了fetchService
        verify(fetchService, times(1)).getFetchService(Platform.MyAnimeList);
        verify(mockFetchService, times(1)).fetchAndSaveMapping("12345");
    }

    @Test
    public void testCreateMapping_AlreadyExists_ThrowsException() {
        // 创建已存在的映射
        createMapping(Platform.MyAnimeList, "12345");

        // 尝试创建相同的映射
        ResourceAlreadyExistsException exception = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> mappingManageService.createMapping(Platform.MyAnimeList, "12345")
        );

        assertTrue(exception.getMessage().contains("Mapping"));
        assertTrue(exception.getMessage().contains("MyAnimeList"));
        assertTrue(exception.getMessage().contains("12345"));

        // 验证：未调用fetchService
        verify(fetchService, never()).getFetchService(any(Platform.class));
    }

    @Test
    public void testUpdateMappingAnime_RecalculatesScoreForBothAnime() {
        // 创建两个动画，每个都有映射
        Anime anime1 = createAnime("动画1");
        Anime anime2 = createAnime("动画2");

        Mapping mapping1 = createMapping(Platform.MyAnimeList, "111");
        Mapping mapping2 = createMapping(Platform.AniList, "222");

        anime1.addMapping(mapping1);
        anime2.addMapping(mapping2);
        animeRepository.save(anime1);
        animeRepository.save(anime2);

        // 将mapping2从anime2移到anime1
        mappingManageService.updateMappingAnime(mapping2.getMappingId(), anime1.getAnimeId());

        // 验证：两个动画都重新计算了指标
        verify(metricService, times(2)).calculateMetric(any(Anime.class));
    }

    @Test
    public void testDeleteMapping_WithMultipleMappingsOnAnime_OnlyDeletesOne() {
        // 创建动画
        Anime anime = createAnime("测试动画");

        // 创建多个映射
        Mapping mapping1 = createMapping(Platform.MyAnimeList, "111");
        Mapping mapping2 = createMapping(Platform.AniList, "222");
        Mapping mapping3 = createMapping(Platform.Bangumi, "333");

        anime.addMapping(mapping1);
        anime.addMapping(mapping2);
        anime.addMapping(mapping3);
        animeRepository.save(anime);

        Long mapping2Id = mapping2.getMappingId();

        // 删除其中一个映射
        mappingManageService.deleteMapping(mapping2Id);

        // 验证：只删除了mapping2
        assertFalse(mappingRepository.existsById(mapping2Id));
        assertTrue(mappingRepository.existsById(mapping1.getMappingId()));
        assertTrue(mappingRepository.existsById(mapping3.getMappingId()));

        // 验证：动画仍有其他映射
        Anime updatedAnime = animeRepository.findById(anime.getAnimeId()).orElse(null);
        assertNotNull(updatedAnime);
        assertEquals(2, updatedAnime.getMappings().size());
    }

    // 辅助方法：创建测试用的 Anime
    private Anime createAnime(String titleNative) {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative(titleNative);

        Anime anime = new Anime();
        anime.setTitle(titles);
        anime.setStartDate(LocalDate.of(2024, 4, 1));
        anime.setReviewStatus(ReviewStatus.APPROVED);

        return animeRepository.save(anime);
    }

    // 辅助方法：创建测试用的 Mapping
    private Mapping createMapping(Platform sourcePlatform, String platformId) {
        Mapping mapping = new Mapping();
        mapping.setSourcePlatform(sourcePlatform);
        mapping.setPlatformId(platformId);
        mapping.setRawScore(8.0);
        mapping.setUpdateTime(Instant.now());

        return mappingRepository.save(mapping);
    }
}
