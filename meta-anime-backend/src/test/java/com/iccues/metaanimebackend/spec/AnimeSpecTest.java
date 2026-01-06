package com.iccues.metaanimebackend.spec;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.LocalDateRange;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AnimeSpecTest {

    @Resource
    private AnimeRepository animeRepository;

    @AfterEach
    public void cleanup() {
        animeRepository.deleteAll();
    }

    @Test
    public void testReviewStatusEquals_Approved() {
        // 准备测试数据
        Anime approvedAnime = createAnime("已审核动画", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        Anime pendingAnime = createAnime("待审核动画", LocalDate.of(2024, 1, 16), ReviewStatus.PENDING);
        animeRepository.save(approvedAnime);
        animeRepository.save(pendingAnime);

        // 查询 APPROVED 状态的动画
        Specification<Anime> spec = AnimeSpec.reviewStatusEquals(ReviewStatus.APPROVED);
        List<Anime> result = animeRepository.findAll(spec);

        // 验证结果
        assertEquals(1, result.size());
        assertEquals("已审核动画", result.getFirst().getTitle().getTitleNative());
        assertEquals(ReviewStatus.APPROVED, result.getFirst().getReviewStatus());
    }

    @Test
    public void testReviewStatusEquals_Pending() {
        // 准备测试数据
        Anime approvedAnime = createAnime("已审核动画", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        Anime pendingAnime = createAnime("待审核动画", LocalDate.of(2024, 1, 16), ReviewStatus.PENDING);
        animeRepository.save(approvedAnime);
        animeRepository.save(pendingAnime);

        // 查询 PENDING 状态的动画
        Specification<Anime> spec = AnimeSpec.reviewStatusEquals(ReviewStatus.PENDING);
        List<Anime> result = animeRepository.findAll(spec);

        // 验证结果
        assertEquals(1, result.size());
        assertEquals("待审核动画", result.getFirst().getTitle().getTitleNative());
        assertEquals(ReviewStatus.PENDING, result.getFirst().getReviewStatus());
    }

    @Test
    public void testReviewStatusEquals_Null() {
        // 准备测试数据
        Anime approvedAnime = createAnime("已审核动画", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        Anime pendingAnime = createAnime("待审核动画", LocalDate.of(2024, 1, 16), ReviewStatus.PENDING);
        animeRepository.save(approvedAnime);
        animeRepository.save(pendingAnime);

        // null 应该不应用过滤
        Specification<Anime> spec = AnimeSpec.reviewStatusEquals(null);
        List<Anime> result = animeRepository.findAll(spec);

        // 验证结果：应该返回所有动画
        assertEquals(2, result.size());
    }

    @Test
    public void testStartDateBetween_ValidRange() {
        // 准备测试数据
        Anime anime1 = createAnime("1月动画", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        Anime anime2 = createAnime("2月动画", LocalDate.of(2024, 2, 15), ReviewStatus.APPROVED);
        Anime anime3 = createAnime("3月动画", LocalDate.of(2024, 3, 15), ReviewStatus.APPROVED);
        animeRepository.save(anime1);
        animeRepository.save(anime2);
        animeRepository.save(anime3);

        // 查询 1月1日 到 3月1日 的动画（不包括 3月1日）
        LocalDateRange range = new LocalDateRange(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 1)
        );
        Specification<Anime> spec = AnimeSpec.startDateBetween(range);
        List<Anime> result = animeRepository.findAll(spec);

        // 验证结果：应该只包含1月和2月的动画
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(a -> a.getTitle().getTitleNative().equals("1月动画")));
        assertTrue(result.stream().anyMatch(a -> a.getTitle().getTitleNative().equals("2月动画")));
        assertFalse(result.stream().anyMatch(a -> a.getTitle().getTitleNative().equals("3月动画")));
    }

    @Test
    public void testStartDateBetween_EdgeCase() {
        // 准备测试数据：边界日期
        Anime animeOnStart = createAnime("开始日期", LocalDate.of(2024, 1, 1), ReviewStatus.APPROVED);
        Anime animeOnEnd = createAnime("结束日期", LocalDate.of(2024, 2, 1), ReviewStatus.APPROVED);
        animeRepository.save(animeOnStart);
        animeRepository.save(animeOnEnd);

        // 查询 1月1日 到 2月1日
        LocalDateRange range = new LocalDateRange(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1)
        );
        Specification<Anime> spec = AnimeSpec.startDateBetween(range);
        List<Anime> result = animeRepository.findAll(spec);

        // 验证结果：应该包含开始日期，不包含结束日期
        assertEquals(1, result.size());
        assertEquals("开始日期", result.getFirst().getTitle().getTitleNative());
    }

    @Test
    public void testStartDateBetween_Null() {
        // 准备测试数据
        Anime anime1 = createAnime("动画1", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        Anime anime2 = createAnime("动画2", LocalDate.of(2024, 2, 15), ReviewStatus.APPROVED);
        animeRepository.save(anime1);
        animeRepository.save(anime2);

        // null 应该不应用过滤
        Specification<Anime> spec = AnimeSpec.startDateBetween(null);
        List<Anime> result = animeRepository.findAll(spec);

        // 验证结果：应该返回所有动画
        assertEquals(2, result.size());
    }

    @Test
    public void testOrderByScoreNullLast() {
        // 准备测试数据：不同的评分，包括 null
        Anime anime1 = createAnime("低分", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        anime1.setAverageScore(70.0);

        Anime anime2 = createAnime("高分", LocalDate.of(2024, 1, 16), ReviewStatus.APPROVED);
        anime2.setAverageScore(90.0);

        Anime anime3 = createAnime("中分", LocalDate.of(2024, 1, 17), ReviewStatus.APPROVED);
        anime3.setAverageScore(80.0);

        Anime anime4 = createAnime("无评分", LocalDate.of(2024, 1, 18), ReviewStatus.APPROVED);
        // averageScore 为 null

        animeRepository.save(anime1);
        animeRepository.save(anime2);
        animeRepository.save(anime3);
        animeRepository.save(anime4);

        // 查询并排序
        Specification<Anime> spec = AnimeSpec.orderByScoreNullLast();
        List<Anime> result = animeRepository.findAll(spec);

        // 验证结果：应该按评分降序排列，null 在最后
        assertEquals(4, result.size());
        assertEquals("高分", result.get(0).getTitle().getTitleNative());
        assertEquals(90.0, result.get(0).getAverageScore());

        assertEquals("中分", result.get(1).getTitle().getTitleNative());
        assertEquals(80.0, result.get(1).getAverageScore());

        assertEquals("低分", result.get(2).getTitle().getTitleNative());
        assertEquals(70.0, result.get(2).getAverageScore());

        assertEquals("无评分", result.get(3).getTitle().getTitleNative());
        assertNull(result.get(3).getAverageScore());
    }

    @Test
    public void testOrderById() {
        // 准备测试数据：以乱序保存
        Anime anime3 = createAnime("动画3", LocalDate.of(2024, 1, 17), ReviewStatus.APPROVED);
        Anime anime1 = createAnime("动画1", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        Anime anime2 = createAnime("动画2", LocalDate.of(2024, 1, 16), ReviewStatus.APPROVED);

        anime3 = animeRepository.save(anime3);
        anime1 = animeRepository.save(anime1);
        anime2 = animeRepository.save(anime2);

        // 查询并按 ID 排序
        Specification<Anime> spec = AnimeSpec.orderById();
        List<Anime> result = animeRepository.findAll(spec);

        // 验证结果：应该按 ID 升序排列
        assertEquals(3, result.size());
        assertEquals(anime3.getAnimeId(), result.get(0).getAnimeId());
        assertEquals(anime1.getAnimeId(), result.get(1).getAnimeId());
        assertEquals(anime2.getAnimeId(), result.get(2).getAnimeId());
    }

    @Test
    public void testCombinedSpecs() {
        // 准备测试数据
        Anime anime1 = createAnime("2024春季-已审核", LocalDate.of(2024, 4, 15), ReviewStatus.APPROVED);
        anime1.setAverageScore(85.0);

        Anime anime2 = createAnime("2024春季-待审核", LocalDate.of(2024, 5, 15), ReviewStatus.PENDING);
        anime2.setAverageScore(90.0);

        Anime anime3 = createAnime("2024夏季-已审核", LocalDate.of(2024, 7, 15), ReviewStatus.APPROVED);
        anime3.setAverageScore(80.0);

        animeRepository.save(anime1);
        animeRepository.save(anime2);
        animeRepository.save(anime3);

        // 组合多个条件：春季 + 已审核 + 按评分排序
        LocalDateRange springRange = new LocalDateRange(
                LocalDate.of(2024, 4, 1),
                LocalDate.of(2024, 7, 1)
        );

        Specification<Anime> spec = Specification.allOf(
                AnimeSpec.startDateBetween(springRange),
                AnimeSpec.reviewStatusEquals(ReviewStatus.APPROVED),
                AnimeSpec.orderByScoreNullLast()
        );

        List<Anime> result = animeRepository.findAll(spec);

        // 验证结果：只应该返回春季且已审核的动画
        assertEquals(1, result.size());
        assertEquals("2024春季-已审核", result.getFirst().getTitle().getTitleNative());
    }

    @Test
    public void testOrderByPopularityNullLast() {
        // 准备测试数据：不同的人气度，包括 null
        Anime anime1 = createAnime("低人气", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        anime1.setPopularity(100.0);

        Anime anime2 = createAnime("高人气", LocalDate.of(2024, 1, 16), ReviewStatus.APPROVED);
        anime2.setPopularity(500.0);

        Anime anime3 = createAnime("中人气", LocalDate.of(2024, 1, 17), ReviewStatus.APPROVED);
        anime3.setPopularity(300.0);

        Anime anime4 = createAnime("无人气数据", LocalDate.of(2024, 1, 18), ReviewStatus.APPROVED);
        // popularity 为 null

        animeRepository.save(anime1);
        animeRepository.save(anime2);
        animeRepository.save(anime3);
        animeRepository.save(anime4);

        // 查询并排序
        Specification<Anime> spec = AnimeSpec.orderByPopularityNullLast();
        List<Anime> result = animeRepository.findAll(spec);

        // 验证结果：应该按人气度降序排列，null 在最后
        assertEquals(4, result.size());
        assertEquals("高人气", result.get(0).getTitle().getTitleNative());
        assertEquals(500.0, result.get(0).getPopularity());

        assertEquals("中人气", result.get(1).getTitle().getTitleNative());
        assertEquals(300.0, result.get(1).getPopularity());

        assertEquals("低人气", result.get(2).getTitle().getTitleNative());
        assertEquals(100.0, result.get(2).getPopularity());

        assertEquals("无人气数据", result.get(3).getTitle().getTitleNative());
        assertNull(result.get(3).getPopularity());
    }

    @Test
    public void testOrderBy_WithScoreSortBy() {
        // 准备测试数据
        Anime anime1 = createAnime("低分", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        anime1.setAverageScore(70.0);

        Anime anime2 = createAnime("高分", LocalDate.of(2024, 1, 16), ReviewStatus.APPROVED);
        anime2.setAverageScore(90.0);

        animeRepository.save(anime1);
        animeRepository.save(anime2);

        // 使用 SCORE 排序
        Specification<Anime> spec = AnimeSpec.orderBy(com.iccues.metaanimebackend.entity.SortBy.SCORE);
        List<Anime> result = animeRepository.findAll(spec);

        // 验证结果：应该按评分降序排列
        assertEquals(2, result.size());
        assertEquals("高分", result.get(0).getTitle().getTitleNative());
        assertEquals("低分", result.get(1).getTitle().getTitleNative());
    }

    @Test
    public void testOrderBy_WithPopularitySortBy() {
        // 准备测试数据
        Anime anime1 = createAnime("低人气", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        anime1.setPopularity(100.0);

        Anime anime2 = createAnime("高人气", LocalDate.of(2024, 1, 16), ReviewStatus.APPROVED);
        anime2.setPopularity(500.0);

        animeRepository.save(anime1);
        animeRepository.save(anime2);

        // 使用 POPULARITY 排序
        Specification<Anime> spec = AnimeSpec.orderBy(com.iccues.metaanimebackend.entity.SortBy.POPULARITY);
        List<Anime> result = animeRepository.findAll(spec);

        // 验证结果：应该按人气度降序排列
        assertEquals(2, result.size());
        assertEquals("高人气", result.get(0).getTitle().getTitleNative());
        assertEquals("低人气", result.get(1).getTitle().getTitleNative());
    }

    // 辅助方法：创建测试用的 Anime
    private Anime createAnime(String titleNative, LocalDate startDate, ReviewStatus reviewStatus) {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative(titleNative);

        Anime anime = new Anime();
        anime.setTitle(titles);
        anime.setStartDate(startDate);
        anime.setReviewStatus(reviewStatus);
        return anime;
    }
}
