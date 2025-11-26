package com.iccues.metaanimebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iccues.metaanimebackend.dto.admin.AnimeCreateRequest;
import com.iccues.metaanimebackend.dto.admin.AnimeUpdateRequest;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminAnimeControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private AnimeRepository animeRepository;

    @Resource
    private MappingRepository mappingRepository;

    @Resource
    private ObjectMapper objectMapper;

    @AfterEach
    public void cleanup() {
        mappingRepository.deleteAll();
        animeRepository.deleteAll();
    }

    @Test
    public void testGetAnimeList_WithoutFilters() throws Exception {
        // 准备测试数据
        Anime anime1 = createAnime("进击的巨人", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        Anime anime2 = createAnime("鬼灭之刃", LocalDate.of(2024, 1, 20), ReviewStatus.PENDING);
        animeRepository.save(anime1);
        animeRepository.save(anime2);

        mockMvc.perform(get("/api/admin/get_anime_list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    public void testGetAnimeList_WithReviewStatusFilter() throws Exception {
        // 准备测试数据
        Anime approvedAnime = createAnime("已审核", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        Anime pendingAnime = createAnime("待审核", LocalDate.of(2024, 1, 20), ReviewStatus.PENDING);
        animeRepository.save(approvedAnime);
        animeRepository.save(pendingAnime);

        mockMvc.perform(get("/api/admin/get_anime_list")
                        .param("reviewStatus", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title.titleNative").value("已审核"))
                .andExpect(jsonPath("$.data[0].reviewStatus").value("APPROVED"));
    }

    @Test
    public void testGetAnimeList_WithYearFilter() throws Exception {
        // 准备测试数据
        Anime anime2024 = createAnime("2024动画", LocalDate.of(2024, 4, 15), ReviewStatus.APPROVED);
        Anime anime2023 = createAnime("2023动画", LocalDate.of(2023, 4, 15), ReviewStatus.APPROVED);
        animeRepository.save(anime2024);
        animeRepository.save(anime2023);

        mockMvc.perform(get("/api/admin/get_anime_list")
                        .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title.titleNative").value("2024动画"));
    }

    @Test
    public void testGetAnimeList_WithYearAndSeasonFilter() throws Exception {
        // 准备测试数据 - 2024年春季 (4-6月)
        Anime springAnime = createAnime("春季动画", LocalDate.of(2024, 4, 15), ReviewStatus.APPROVED);
        animeRepository.save(springAnime);

        // 准备测试数据 - 2024年夏季 (7-9月)
        Anime summerAnime = createAnime("夏季动画", LocalDate.of(2024, 7, 15), ReviewStatus.APPROVED);
        animeRepository.save(summerAnime);

        mockMvc.perform(get("/api/admin/get_anime_list")
                        .param("year", "2024")
                        .param("season", "SPRING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title.titleNative").value("春季动画"));
    }

    @Test
    public void testCreateAnime_Success() throws Exception {
        // 准备请求数据
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("新动画");
        titles.setTitleRomaji("New Anime");

        AnimeCreateRequest request = new AnimeCreateRequest(
                titles,
                "https://example.com/cover.jpg",
                LocalDate.of(2024, 4, 1)
        );

        mockMvc.perform(post("/api/admin/create_anime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title.titleNative").value("新动画"))
                .andExpect(jsonPath("$.data.title.titleRomaji").value("New Anime"))
                .andExpect(jsonPath("$.data.coverImage").value("https://example.com/cover.jpg"))
                .andExpect(jsonPath("$.data.startDate").value("2024-04-01"));

        // 验证数据库中是否保存
        assertEquals(1, animeRepository.count());
    }

    @Test
    public void testUpdateAnime_Success() throws Exception {
        // 先创建一个动画
        Anime existingAnime = createAnime("原标题", LocalDate.of(2024, 1, 15), ReviewStatus.PENDING);
        existingAnime = animeRepository.save(existingAnime);

        // 准备更新请求
        AnimeTitles updatedTitles = new AnimeTitles();
        updatedTitles.setTitleNative("更新后的标题");
        updatedTitles.setTitleRomaji("Updated Title");

        AnimeUpdateRequest request = new AnimeUpdateRequest(
                existingAnime.getAnimeId(),
                updatedTitles,
                "https://example.com/new-cover.jpg",
                ReviewStatus.APPROVED,
                LocalDate.of(2024, 4, 1)
        );

        mockMvc.perform(put("/api/admin/update_anime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.animeId").value(existingAnime.getAnimeId()))
                .andExpect(jsonPath("$.data.title.titleNative").value("更新后的标题"))
                .andExpect(jsonPath("$.data.reviewStatus").value("APPROVED"))
                .andExpect(jsonPath("$.data.coverImage").value("https://example.com/new-cover.jpg"));

        // 验证数据库中的数据是否更新
        Anime updatedAnime = animeRepository.findById(existingAnime.getAnimeId()).orElseThrow();
        assertEquals("更新后的标题", updatedAnime.getTitle().getTitleNative());
        assertEquals(ReviewStatus.APPROVED, updatedAnime.getReviewStatus());
    }

    @Test
    public void testUpdateAnime_NotFound() throws Exception {
        // 准备不存在的 ID 的更新请求
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("不存在的动画");

        AnimeUpdateRequest request = new AnimeUpdateRequest(
                999L,
                titles,
                null,
                ReviewStatus.APPROVED,
                LocalDate.of(2024, 4, 1)
        );

        mockMvc.perform(put("/api/admin/update_anime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    public void testDeleteAnime_Success() throws Exception {
        // 先创建一个动画
        Anime anime = createAnime("待删除", LocalDate.of(2024, 1, 15), ReviewStatus.PENDING);
        anime = animeRepository.save(anime);

        Long animeId = anime.getAnimeId();

        mockMvc.perform(delete("/api/admin/delete_anime/" + animeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证数据库中是否删除
        assertFalse(animeRepository.existsById(animeId));
    }

    @Test
    public void testDeleteAnime_NotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/delete_anime/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    public void testDeleteAnime_WithMappings() throws Exception {
        // 先创建一个有映射关联的动画
        Anime anime = createAnime("有映射的动画", LocalDate.of(2024, 1, 15), ReviewStatus.PENDING);
        anime = animeRepository.save(anime);

        // 创建一个映射并关联到这个动画
        com.iccues.metaanimebackend.entity.Mapping mapping = new com.iccues.metaanimebackend.entity.Mapping();
        mapping.setSourcePlatform("MAL");
        mapping.setPlatformId("12345");
        mapping.setAnime(anime);
        mapping = mappingRepository.save(mapping);

        Long animeId = anime.getAnimeId();
        Long mappingId = mapping.getMappingId();

        mockMvc.perform(delete("/api/admin/delete_anime/" + animeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证动画已删除
        assertFalse(animeRepository.existsById(animeId));

        // 验证映射仍然存在，但关联已解除
        assertTrue(mappingRepository.existsById(mappingId));
        com.iccues.metaanimebackend.entity.Mapping updatedMapping = mappingRepository.findById(mappingId).orElseThrow();
        assertNull(updatedMapping.getAnime());
    }

    @Test
    public void testGetAnimeList_SortedById() throws Exception {
        // 准备测试数据，不同的创建顺序
        Anime anime3 = createAnime("动画3", LocalDate.of(2024, 1, 17), ReviewStatus.APPROVED);
        Anime anime1 = createAnime("动画1", LocalDate.of(2024, 1, 15), ReviewStatus.APPROVED);
        Anime anime2 = createAnime("动画2", LocalDate.of(2024, 1, 16), ReviewStatus.APPROVED);

        anime3 = animeRepository.save(anime3);
        anime1 = animeRepository.save(anime1);
        anime2 = animeRepository.save(anime2);

        mockMvc.perform(get("/api/admin/get_anime_list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(3)))
                // 验证按 ID 排序
                .andExpect(jsonPath("$.data[0].animeId").value(anime3.getAnimeId()))
                .andExpect(jsonPath("$.data[1].animeId").value(anime1.getAnimeId()))
                .andExpect(jsonPath("$.data[2].animeId").value(anime2.getAnimeId()));
    }

    @Test
    public void testGetAnimeList_EmptyResult() throws Exception {
        mockMvc.perform(get("/api/admin/get_anime_list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
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
