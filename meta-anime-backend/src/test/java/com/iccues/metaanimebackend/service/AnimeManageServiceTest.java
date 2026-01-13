package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.exception.ResourceNotFoundException;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AnimeManageService 的单元测试
 * 测试管理员的动画 CRUD 操作
 */
@SpringBootTest
@Transactional
public class AnimeManageServiceTest {

    @Resource
    private AnimeRepository animeRepository;

    @Resource
    private MappingRepository mappingRepository;

    @Resource
    private AnimeManageService animeManageService;

    @AfterEach
    public void cleanup() {
        mappingRepository.deleteAll();
        animeRepository.deleteAll();
    }

    @Test
    public void testGetAnimeList_WithoutFilters_ReturnsAllAnime() {
        // 创建不同审核状态的动画
        createAnime("已审核", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED);
        createAnime("待审核", LocalDate.of(2024, 4, 2), ReviewStatus.PENDING);
        createAnime("已拒绝", LocalDate.of(2024, 4, 3), ReviewStatus.REJECTED);

        // 执行查询：不过滤审核状态
        List<Anime> result = animeManageService.getAnimeList(null, null, null);

        // 验证：返回所有动画
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void testGetAnimeList_WithReviewStatusFilter_ReturnsFilteredAnime() {
        // 创建不同审核状态的动画
        createAnime("已审核1", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED);
        createAnime("已审核2", LocalDate.of(2024, 4, 2), ReviewStatus.APPROVED);
        createAnime("待审核", LocalDate.of(2024, 4, 3), ReviewStatus.PENDING);

        // 执行查询：只查询APPROVED状态
        List<Anime> result = animeManageService.getAnimeList(ReviewStatus.APPROVED, null, null);

        // 验证
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream()
                .allMatch(anime -> anime.getReviewStatus() == ReviewStatus.APPROVED));
    }

    @Test
    public void testGetAnimeList_WithYearFilter_ReturnsAnimeInYear() {
        // 创建不同年份的动画
        createAnime("2024动画", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED);
        createAnime("2023动画", LocalDate.of(2023, 4, 1), ReviewStatus.APPROVED);

        // 执行查询：只查询2024年
        List<Anime> result = animeManageService.getAnimeList(null, 2024, null);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2024, result.get(0).getStartDate().getYear());
    }

    @Test
    public void testGetAnimeList_WithYearAndSeasonFilter_ReturnsAnimeInSeason() {
        // 创建不同季度的动画
        createAnime("春季动画", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED);
        createAnime("夏季动画", LocalDate.of(2024, 7, 1), ReviewStatus.APPROVED);

        // 执行查询：只查询2024年春季
        List<Anime> result = animeManageService.getAnimeList(null, 2024, Season.SPRING);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getStartDate().getMonthValue() >= 4 
                && result.get(0).getStartDate().getMonthValue() <= 6);
    }

    @Test
    public void testGetAnimeList_SortedById_ReturnsAscendingOrder() {
        // 创建多个动画
        Anime anime1 = createAnime("动画1", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED);
        Anime anime2 = createAnime("动画2", LocalDate.of(2024, 4, 2), ReviewStatus.APPROVED);
        Anime anime3 = createAnime("动画3", LocalDate.of(2024, 4, 3), ReviewStatus.APPROVED);

        // 执行查询
        List<Anime> result = animeManageService.getAnimeList(null, null, null);

        // 验证：按ID升序排列
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.get(0).getAnimeId() < result.get(1).getAnimeId());
        assertTrue(result.get(1).getAnimeId() < result.get(2).getAnimeId());
    }

    @Test
    public void testGetAnimeList_EmptyResult_ReturnsEmptyList() {
        // 不创建任何数据

        // 执行查询
        List<Anime> result = animeManageService.getAnimeList(null, null, null);

        // 验证：返回空列表
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCreateAnime_Success_ReturnsSavedAnime() {
        // 准备数据
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("新动画");

        Anime anime = new Anime();
        anime.setTitle(titles);
        anime.setStartDate(LocalDate.of(2024, 4, 1));
        anime.setReviewStatus(ReviewStatus.PENDING);

        // 执行创建
        Anime result = animeManageService.createAnime(anime);

        // 验证
        assertNotNull(result);
        assertNotNull(result.getAnimeId());
        assertEquals("新动画", result.getTitle().getTitleNative());
        assertEquals(ReviewStatus.PENDING, result.getReviewStatus());

        // 验证数据库中存在
        Anime saved = animeRepository.findById(result.getAnimeId()).orElse(null);
        assertNotNull(saved);
        assertEquals("新动画", saved.getTitle().getTitleNative());
    }

    @Test
    public void testUpdateAnime_Success_UpdatesAnime() {
        // 创建初始动画
        Anime anime = createAnime("原始标题", LocalDate.of(2024, 4, 1), ReviewStatus.PENDING);
        Long animeId = anime.getAnimeId();

        // 执行更新
        Anime updated = animeManageService.updateAnime(animeId, a -> {
            a.getTitle().setTitleNative("更新后的标题");
            a.setReviewStatus(ReviewStatus.APPROVED);
            a.setAverageScore(8.5);
        });

        // 验证
        assertNotNull(updated);
        assertEquals(animeId, updated.getAnimeId());
        assertEquals("更新后的标题", updated.getTitle().getTitleNative());
        assertEquals(ReviewStatus.APPROVED, updated.getReviewStatus());
        assertEquals(8.5, updated.getAverageScore());

        // 验证数据库中已更新
        Anime saved = animeRepository.findById(animeId).orElse(null);
        assertNotNull(saved);
        assertEquals("更新后的标题", saved.getTitle().getTitleNative());
        assertEquals(ReviewStatus.APPROVED, saved.getReviewStatus());
    }

    @Test
    public void testUpdateAnime_NotFound_ThrowsException() {
        // 使用不存在的ID
        Long nonExistentId = 99999L;

        // 执行更新并验证异常
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> animeManageService.updateAnime(nonExistentId, a -> a.setAverageScore(8.0))
        );

        assertTrue(exception.getMessage().contains("Anime"));
        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
    }

    @Test
    public void testDeleteAnime_Success_DeletesAnime() {
        // 创建动画
        Anime anime = createAnime("待删除动画", LocalDate.of(2024, 4, 1), ReviewStatus.PENDING);
        Long animeId = anime.getAnimeId();

        // 执行删除
        animeManageService.deleteAnime(animeId);

        // 验证：动画已被删除
        assertFalse(animeRepository.existsById(animeId));
    }

    @Test
    public void testDeleteAnime_WithMappings_UnlinksAndDeletesAnime() {
        // 创建动画
        Anime anime = createAnime("有映射的动画", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED);
        Long animeId = anime.getAnimeId();

        // 创建关联的映射
        Mapping mapping1 = createMapping(Platform.MyAnimeList, "12345");
        Mapping mapping2 = createMapping(Platform.AniList, "67890");
        anime.addMapping(mapping1);
        anime.addMapping(mapping2);
        animeRepository.save(anime);

        Long mapping1Id = mapping1.getMappingId();
        Long mapping2Id = mapping2.getMappingId();

        // 执行删除
        animeManageService.deleteAnime(animeId);

        // 验证：动画已被删除
        assertFalse(animeRepository.existsById(animeId));

        // 验证：映射仍然存在，但已解除关联
        Mapping savedMapping1 = mappingRepository.findById(mapping1Id).orElse(null);
        Mapping savedMapping2 = mappingRepository.findById(mapping2Id).orElse(null);
        assertNotNull(savedMapping1);
        assertNotNull(savedMapping2);
        assertNull(savedMapping1.getAnime());
        assertNull(savedMapping2.getAnime());
    }

    @Test
    public void testDeleteAnime_NotFound_ThrowsException() {
        // 使用不存在的ID
        Long nonExistentId = 99999L;

        // 执行删除并验证异常
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> animeManageService.deleteAnime(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("Anime"));
        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
    }

    @Test
    public void testDeleteAnime_WithMultipleMappings_UnlinksAllMappings() {
        // 创建动画
        Anime anime = createAnime("多映射动画", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED);

        // 创建多个映射
        Mapping mapping1 = createMapping(Platform.MyAnimeList, "111");
        Mapping mapping2 = createMapping(Platform.AniList, "222");
        Mapping mapping3 = createMapping(Platform.Bangumi, "333");
        
        anime.addMapping(mapping1);
        anime.addMapping(mapping2);
        anime.addMapping(mapping3);
        animeRepository.save(anime);

        Long animeId = anime.getAnimeId();
        int initialMappingCount = mappingRepository.findAll().size();

        // 执行删除
        animeManageService.deleteAnime(animeId);

        // 验证：动画已删除
        assertFalse(animeRepository.existsById(animeId));

        // 验证：所有映射仍存在但已解除关联
        assertEquals(initialMappingCount, mappingRepository.findAll().size());
        List<Mapping> unmappedMappings = mappingRepository.findAllByAnimeIsNull();
        assertEquals(initialMappingCount, unmappedMappings.size());
    }

    // 辅助方法：创建测试用的 Anime
    private Anime createAnime(String titleNative, LocalDate startDate, ReviewStatus reviewStatus) {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative(titleNative);

        Anime anime = new Anime();
        anime.setTitle(titles);
        anime.setStartDate(startDate);
        anime.setReviewStatus(reviewStatus);

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
