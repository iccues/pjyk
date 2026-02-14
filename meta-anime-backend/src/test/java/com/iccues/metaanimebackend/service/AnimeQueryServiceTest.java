package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.entity.SortBy;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AnimeQueryService 的单元测试
 * 测试公开API的动画查询逻辑
 */
@SpringBootTest
@Transactional
public class AnimeQueryServiceTest {

    @Resource
    private AnimeRepository animeRepository;

    @Resource
    private AnimeQueryService animeQueryService;

    @AfterEach
    public void cleanup() {
        animeRepository.deleteAll();
    }

    @Test
    public void testGetAnimeList_WithoutFilters_ReturnsAllApprovedAnime() {
        // 创建测试数据
        createAnime("进击的巨人", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED, 8.5, 1000.0);
        createAnime("鬼灭之刃", LocalDate.of(2024, 7, 1), ReviewStatus.APPROVED, 9.0, 2000.0);
        createAnime("待审核动画", LocalDate.of(2024, 4, 1), ReviewStatus.PENDING, 7.0, 500.0);

        // 执行查询
        Page<Anime> result = animeQueryService.getAnimeList(null, null, 0, 10, SortBy.SCORE);

        // 验证：只返回已审核通过的动画
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(anime -> anime.getReviewStatus() == ReviewStatus.APPROVED));
    }

    @Test
    public void testGetAnimeList_WithYearFilter_ReturnsAnimeInYear() {
        // 创建测试数据
        createAnime("2024春季动画", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED, 8.5, 1000.0);
        createAnime("2024夏季动画", LocalDate.of(2024, 7, 1), ReviewStatus.APPROVED, 9.0, 2000.0);
        createAnime("2023动画", LocalDate.of(2023, 4, 1), ReviewStatus.APPROVED, 7.5, 800.0);

        // 执行查询：只查询2024年的动画
        Page<Anime> result = animeQueryService.getAnimeList(2024, null, 0, 10, SortBy.SCORE);

        // 验证
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(anime -> anime.getStartDate().getYear() == 2024));
    }

    @Test
    public void testGetAnimeList_WithYearAndSeasonFilter_ReturnsAnimeInSeason() {
        // 创建测试数据
        createAnime("2024春季动画1", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED, 8.5, 1000.0);
        createAnime("2024春季动画2", LocalDate.of(2024, 5, 15), ReviewStatus.APPROVED, 8.0, 900.0);
        createAnime("2024夏季动画", LocalDate.of(2024, 7, 1), ReviewStatus.APPROVED, 9.0, 2000.0);

        // 执行查询：只查询2024年春季的动画
        Page<Anime> result = animeQueryService.getAnimeList(2024, Season.SPRING, 0, 10, SortBy.SCORE);

        // 验证
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(anime -> {
                    int month = anime.getStartDate().getMonthValue();
                    return month >= 4 && month <= 6;
                }));
    }

    @Test
    public void testGetAnimeList_OnlyApprovedAnime_ExcludesPendingAndRejected() {
        // 创建不同审核状态的动画
        createAnime("已审核", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED, 8.5, 1000.0);
        createAnime("待审核", LocalDate.of(2024, 4, 2), ReviewStatus.PENDING, 8.0, 900.0);
        createAnime("已拒绝", LocalDate.of(2024, 4, 3), ReviewStatus.REJECTED, 7.0, 500.0);

        // 执行查询
        Page<Anime> result = animeQueryService.getAnimeList(null, null, 0, 10, SortBy.SCORE);

        // 验证：只返回APPROVED状态的动画
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(ReviewStatus.APPROVED, result.getContent().get(0).getReviewStatus());
    }

    @Test
    public void testGetAnimeList_WithPagination_ReturnsCorrectPage() {
        // 创建5个动画
        for (int i = 1; i <= 5; i++) {
            createAnime("动画" + i, LocalDate.of(2024, 4, i), ReviewStatus.APPROVED, 8.0 + i * 0.1, 1000.0);
        }

        // 第一页：每页2条
        Page<Anime> page1 = animeQueryService.getAnimeList(null, null, 0, 2, SortBy.SCORE);
        assertEquals(2, page1.getContent().size());
        assertEquals(5, page1.getTotalElements());
        assertEquals(3, page1.getTotalPages());

        // 第二页
        Page<Anime> page2 = animeQueryService.getAnimeList(null, null, 1, 2, SortBy.SCORE);
        assertEquals(2, page2.getContent().size());

        // 第三页
        Page<Anime> page3 = animeQueryService.getAnimeList(null, null, 2, 2, SortBy.SCORE);
        assertEquals(1, page3.getContent().size());
    }

    @Test
    public void testGetAnimeList_SortByScore_ReturnsDescendingOrder() {
        // 创建不同分数的动画
        createAnime("低分动画", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED, 7.0, 1000.0);
        createAnime("高分动画", LocalDate.of(2024, 4, 2), ReviewStatus.APPROVED, 9.5, 1000.0);
        createAnime("中分动画", LocalDate.of(2024, 4, 3), ReviewStatus.APPROVED, 8.0, 1000.0);

        // 执行查询：按分数排序
        Page<Anime> result = animeQueryService.getAnimeList(null, null, 0, 10, SortBy.SCORE);

        // 验证：分数降序排列
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(9.5, result.getContent().get(0).getAverageScore());
        assertEquals(8.0, result.getContent().get(1).getAverageScore());
        assertEquals(7.0, result.getContent().get(2).getAverageScore());
    }

    @Test
    public void testGetAnimeList_SortByPopularity_ReturnsDescendingOrder() {
        // 创建不同人气度的动画
        createAnime("低人气", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED, 8.0, 500.0);
        createAnime("高人气", LocalDate.of(2024, 4, 2), ReviewStatus.APPROVED, 8.0, 2000.0);
        createAnime("中人气", LocalDate.of(2024, 4, 3), ReviewStatus.APPROVED, 8.0, 1000.0);

        // 执行查询：按人气度排序
        Page<Anime> result = animeQueryService.getAnimeList(null, null, 0, 10, SortBy.POPULARITY);

        // 验证：人气度降序排列
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(2000.0, result.getContent().get(0).getPopularity());
        assertEquals(1000.0, result.getContent().get(1).getPopularity());
        assertEquals(500.0, result.getContent().get(2).getPopularity());
    }

    @Test
    public void testGetAnimeList_EmptyResult_ReturnsEmptyPage() {
        // 不创建任何数据

        // 执行查询
        Page<Anime> result = animeQueryService.getAnimeList(null, null, 0, 10, SortBy.SCORE);

        // 验证：返回空页面
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    public void testGetAnimeList_NoMatchingYear_ReturnsEmptyPage() {
        // 创建2024年的动画
        createAnime("2024动画", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED, 8.5, 1000.0);

        // 查询2023年的动画
        Page<Anime> result = animeQueryService.getAnimeList(2023, null, 0, 10, SortBy.SCORE);

        // 验证：返回空结果
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void testGetAnimeList_NoMatchingSeason_ReturnsEmptyPage() {
        // 创建春季动画
        createAnime("春季动画", LocalDate.of(2024, 4, 1), ReviewStatus.APPROVED, 8.5, 1000.0);

        // 查询夏季动画
        Page<Anime> result = animeQueryService.getAnimeList(2024, Season.SUMMER, 0, 10, SortBy.SCORE);

        // 验证：返回空结果
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // 辅助方法：创建测试用的 Anime
    private Anime createAnime(String titleNative, LocalDate startDate, ReviewStatus reviewStatus, 
                              Double score, Double popularity) {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative(titleNative);

        Anime anime = new Anime();
        anime.setTitle(titles);
        anime.setStartDate(startDate);
        anime.setReviewStatus(reviewStatus);
        anime.setAverageScore(score);
        anime.setPopularity(popularity);

        return animeRepository.save(anime);
    }
}
