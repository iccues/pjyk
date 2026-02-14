package com.iccues.metaanimebackend.controller;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AnimeControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private AnimeRepository animeRepository;

    @AfterEach
    public void cleanup() {
        animeRepository.deleteAll();
    }

    @Test
    public void testGetAnimeList_WithoutFilters() throws Exception {
        // 准备测试数据
        Anime anime1 = createAnime("进击的巨人", LocalDate.of(2024, 1, 15), 9.5);
        Anime anime2 = createAnime("鬼灭之刃", LocalDate.of(2024, 1, 20), 9.0);
        animeRepository.save(anime1);
        animeRepository.save(anime2);

        mockMvc.perform(get("/api/anime/get_list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.page.totalElements").value(2));
    }

    @Test
    public void testGetAnimeList_WithYearFilter() throws Exception {
        // 准备测试数据 - 2024年的动画
        Anime anime2024 = createAnime("2024动画", LocalDate.of(2024, 4, 15), 8.5);
        animeRepository.save(anime2024);

        // 准备测试数据 - 2023年的动画
        Anime anime2023 = createAnime("2023动画", LocalDate.of(2023, 4, 15), 8.0);
        animeRepository.save(anime2023);

        mockMvc.perform(get("/api/anime/get_list")
                        .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title.titleNative").value("2024动画"));
    }

    @Test
    public void testGetAnimeList_WithYearAndSeasonFilter() throws Exception {
        // 准备测试数据 - 2024年春季 (4-6月)
        Anime springAnime = createAnime("春季动画", LocalDate.of(2024, 4, 15), 9.0);
        animeRepository.save(springAnime);

        // 准备测试数据 - 2024年夏季 (7-9月)
        Anime summerAnime = createAnime("夏季动画", LocalDate.of(2024, 7, 15), 8.5);
        animeRepository.save(summerAnime);

        mockMvc.perform(get("/api/anime/get_list")
                        .param("year", "2024")
                        .param("season", "SPRING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title.titleNative").value("春季动画"));
    }

    @Test
    public void testGetAnimeList_WithPagination() throws Exception {
        // 准备多个测试数据
        for (int i = 1; i <= 5; i++) {
            Anime anime = createAnime("动画" + i, LocalDate.of(2024, 1, i), 9.0 - i * 0.1);
            animeRepository.save(anime);
        }

        // 测试第一页，每页2条
        mockMvc.perform(get("/api/anime/get_list")
                        .param("page", "0")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.page.totalElements").value(5))
                .andExpect(jsonPath("$.data.page.totalPages").value(3))
                .andExpect(jsonPath("$.data.page.number").value(0));

        // 测试第二页
        mockMvc.perform(get("/api/anime/get_list")
                        .param("page", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.page.number").value(1));
    }

    @Test
    public void testGetAnimeList_OnlyApprovedAnime() throws Exception {
        // 准备已审核的动画
        Anime approvedAnime = createAnime("已审核", LocalDate.of(2024, 1, 15), 9.0);
        approvedAnime.setReviewStatus(ReviewStatus.APPROVED);
        animeRepository.save(approvedAnime);

        // 准备未审核的动画
        Anime pendingAnime = createAnime("待审核", LocalDate.of(2024, 1, 20), 8.5);
        pendingAnime.setReviewStatus(ReviewStatus.PENDING);
        animeRepository.save(pendingAnime);

        mockMvc.perform(get("/api/anime/get_list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title.titleNative").value("已审核"));
    }

    @Test
    public void testGetAnimeList_EmptyResult() throws Exception {
        mockMvc.perform(get("/api/anime/get_list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.page.totalElements").value(0));
    }

    @Test
    public void testGetAnimeList_SortedByScore() throws Exception {
        // 准备测试数据，分数不同
        Anime anime1 = createAnime("低分", LocalDate.of(2024, 1, 15), 7.0);
        Anime anime2 = createAnime("高分", LocalDate.of(2024, 1, 16), 9.5);
        Anime anime3 = createAnime("中分", LocalDate.of(2024, 1, 17), 8.0);
        animeRepository.save(anime1);
        animeRepository.save(anime2);
        animeRepository.save(anime3);

        mockMvc.perform(get("/api/anime/get_list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                // 验证按评分降序排列 (高分 > 中分 > 低分)
                .andExpect(jsonPath("$.data.content[0].averageScore").value(9.5))
                .andExpect(jsonPath("$.data.content[1].averageScore").value(8.0))
                .andExpect(jsonPath("$.data.content[2].averageScore").value(7.0));
    }

    @Test
    public void testGetAnimeList_SortByScore() throws Exception {
        // 准备测试数据
        Anime anime1 = createAnimeWithPopularity("动画1", LocalDate.of(2024, 1, 15), 7.0, 500.0);
        Anime anime2 = createAnimeWithPopularity("动画2", LocalDate.of(2024, 1, 16), 9.0, 100.0);
        Anime anime3 = createAnimeWithPopularity("动画3", LocalDate.of(2024, 1, 17), 8.0, 300.0);
        animeRepository.save(anime1);
        animeRepository.save(anime2);
        animeRepository.save(anime3);

        // 使用 sortBy=SCORE 参数
        mockMvc.perform(get("/api/anime/get_list")
                        .param("sortBy", "SCORE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                // 验证按评分降序排列
                .andExpect(jsonPath("$.data.content[0].title.titleNative").value("动画2"))
                .andExpect(jsonPath("$.data.content[0].averageScore").value(9.0))
                .andExpect(jsonPath("$.data.content[1].title.titleNative").value("动画3"))
                .andExpect(jsonPath("$.data.content[1].averageScore").value(8.0))
                .andExpect(jsonPath("$.data.content[2].title.titleNative").value("动画1"))
                .andExpect(jsonPath("$.data.content[2].averageScore").value(7.0));
    }

    @Test
    public void testGetAnimeList_SortByPopularity() throws Exception {
        // 准备测试数据：人气度和评分不一致
        Anime anime1 = createAnimeWithPopularity("低人气高分", LocalDate.of(2024, 1, 15), 9.0, 100.0);
        Anime anime2 = createAnimeWithPopularity("高人气低分", LocalDate.of(2024, 1, 16), 7.0, 500.0);
        Anime anime3 = createAnimeWithPopularity("中人气中分", LocalDate.of(2024, 1, 17), 8.0, 300.0);
        animeRepository.save(anime1);
        animeRepository.save(anime2);
        animeRepository.save(anime3);

        // 使用 sortBy=POPULARITY 参数
        mockMvc.perform(get("/api/anime/get_list")
                        .param("sortBy", "POPULARITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                // 验证按人气度降序排列（而不是评分）
                .andExpect(jsonPath("$.data.content[0].title.titleNative").value("高人气低分"))
                .andExpect(jsonPath("$.data.content[1].title.titleNative").value("中人气中分"))
                .andExpect(jsonPath("$.data.content[2].title.titleNative").value("低人气高分"));
    }

    @Test
    public void testGetAnimeList_DefaultSortByScore() throws Exception {
        // 准备测试数据
        Anime anime1 = createAnimeWithPopularity("低分高人气", LocalDate.of(2024, 1, 15), 7.0, 500.0);
        Anime anime2 = createAnimeWithPopularity("高分低人气", LocalDate.of(2024, 1, 16), 9.0, 100.0);
        animeRepository.save(anime1);
        animeRepository.save(anime2);

        // 不传 sortBy 参数，应该默认按评分排序
        mockMvc.perform(get("/api/anime/get_list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                // 验证默认按评分降序排列
                .andExpect(jsonPath("$.data.content[0].title.titleNative").value("高分低人气"))
                .andExpect(jsonPath("$.data.content[1].title.titleNative").value("低分高人气"));
    }

    // 辅助方法：创建测试用的 Anime
    private Anime createAnime(String titleNative, LocalDate startDate, Double score) {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative(titleNative);

        Anime anime = new Anime();
        anime.setTitle(titles);
        anime.setStartDate(startDate);
        anime.setAverageScore(score);
        anime.setReviewStatus(ReviewStatus.APPROVED);
        return anime;
    }

    // 辅助方法：创建带人气度的测试 Anime
    private Anime createAnimeWithPopularity(String titleNative, LocalDate startDate, Double score, Double popularity) {
        Anime anime = createAnime(titleNative, startDate, score);
        anime.setPopularity(popularity);
        return anime;
    }
}
