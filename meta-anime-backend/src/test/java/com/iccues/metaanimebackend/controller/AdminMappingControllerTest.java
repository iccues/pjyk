package com.iccues.metaanimebackend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iccues.metaanimebackend.dto.admin.CreateMappingRequest;
import com.iccues.metaanimebackend.dto.admin.UpdateMappingAnimeRequest;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.MappingInfo;
import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import com.iccues.metaanimebackend.service.fetch.AbstractAnimeFetchService;
import com.iccues.metaanimebackend.service.fetch.FetchService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AdminMappingControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private AnimeRepository animeRepository;

    @Resource
    private MappingRepository mappingRepository;

    @Resource
    private ObjectMapper objectMapper;

    @MockitoBean
    private FetchService fetchService;

    @AfterEach
    public void cleanup() {
        mappingRepository.deleteAll();
        animeRepository.deleteAll();
    }

    @Test
    public void testGetUnmappedMappingList() throws Exception {
        // 准备测试数据 - 未关联的 mapping
        Mapping unmappedMapping1 = createMapping(Platform.MyAnimeList, "12345");
        Mapping unmappedMapping2 = createMapping(Platform.Bangumi, "67890");
        mappingRepository.save(unmappedMapping1);
        mappingRepository.save(unmappedMapping2);

        // 准备测试数据 - 已关联的 mapping
        Anime anime = createAnime("测试动画");
        anime = animeRepository.save(anime);
        Mapping mappedMapping = createMapping(Platform.AniList, "11111");
        mappedMapping.setAnime(anime);
        mappingRepository.save(mappedMapping);

        mockMvc.perform(get("/api/admin/get_unmapped_mapping_list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].sourcePlatform", containsInAnyOrder("MyAnimeList", "Bangumi")));
    }

    @Test
    public void testGetUnmappedMappingList_EmptyResult() throws Exception {
        mockMvc.perform(get("/api/admin/get_unmapped_mapping_list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    public void testUpdateMappingAnime_LinkMappingToAnime() throws Exception {
        // 准备未关联的 mapping
        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        mapping = mappingRepository.save(mapping);

        // 准备动画
        Anime anime = createAnime("目标动画");
        anime = animeRepository.save(anime);

        UpdateMappingAnimeRequest request = new UpdateMappingAnimeRequest(
                mapping.getMappingId(),
                anime.getAnimeId()
        );

        mockMvc.perform(put("/api/admin/update_mapping_anime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mappingId").value(mapping.getMappingId()))
                .andExpect(jsonPath("$.data.animeId").value(anime.getAnimeId()));

        // 验证数据库中的关联
        Mapping updatedMapping = mappingRepository.findById(mapping.getMappingId()).orElseThrow();
        assertNotNull(updatedMapping.getAnime());
        assertEquals(anime.getAnimeId(), updatedMapping.getAnime().getAnimeId());
    }

    @Test
    public void testUpdateMappingAnime_UnlinkMapping() throws Exception {
        // 准备已关联的 mapping
        Anime anime = createAnime("原动画");
        anime = animeRepository.save(anime);

        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        mapping.setAnime(anime);
        mapping = mappingRepository.save(mapping);

        UpdateMappingAnimeRequest request = new UpdateMappingAnimeRequest(
                mapping.getMappingId(),
                null  // null 表示解除关联
        );

        mockMvc.perform(put("/api/admin/update_mapping_anime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mappingId").value(mapping.getMappingId()));

        // 验证数据库中的关联已解除
        Mapping updatedMapping = mappingRepository.findById(mapping.getMappingId()).orElseThrow();
        assertNull(updatedMapping.getAnime());
    }

    @Test
    public void testUpdateMappingAnime_ChangeMappingAnime() throws Exception {
        // 准备原动画和目标动画
        Anime oldAnime = createAnime("原动画");
        oldAnime = animeRepository.save(oldAnime);

        Anime newAnime = createAnime("新动画");
        newAnime = animeRepository.save(newAnime);

        // 准备已关联到原动画的 mapping
        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        mapping.setAnime(oldAnime);
        mapping = mappingRepository.save(mapping);

        UpdateMappingAnimeRequest request = new UpdateMappingAnimeRequest(
                mapping.getMappingId(),
                newAnime.getAnimeId()
        );

        mockMvc.perform(put("/api/admin/update_mapping_anime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.animeId").value(newAnime.getAnimeId()));

        // 验证数据库中的关联已更改
        Mapping updatedMapping = mappingRepository.findById(mapping.getMappingId()).orElseThrow();
        assertEquals(newAnime.getAnimeId(), updatedMapping.getAnime().getAnimeId());
    }

    @Test
    public void testUpdateMappingAnime_MappingNotFound() throws Exception {
        UpdateMappingAnimeRequest request = new UpdateMappingAnimeRequest(999L, null);

        mockMvc.perform(put("/api/admin/update_mapping_anime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    public void testUpdateMappingAnime_AnimeNotFound() throws Exception {
        // 准备 mapping
        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        mapping = mappingRepository.save(mapping);

        UpdateMappingAnimeRequest request = new UpdateMappingAnimeRequest(
                mapping.getMappingId(),
                999L  // 不存在的动画 ID
        );

        mockMvc.perform(put("/api/admin/update_mapping_anime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    public void testDeleteMapping_Success() throws Exception {
        // 准备 mapping
        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        mapping = mappingRepository.save(mapping);

        Long mappingId = mapping.getMappingId();

        mockMvc.perform(delete("/api/admin/delete_mapping/" + mappingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证数据库中已删除
        assertFalse(mappingRepository.existsById(mappingId));
    }

    @Test
    public void testDeleteMapping_WithAnimeAssociation() throws Exception {
        // 准备关联了动画的 mapping
        Anime anime = createAnime("关联动画");
        anime = animeRepository.save(anime);

        Mapping mapping = createMapping(Platform.MyAnimeList, "12345");
        mapping.setAnime(anime);
        mapping = mappingRepository.save(mapping);

        Long mappingId = mapping.getMappingId();

        mockMvc.perform(delete("/api/admin/delete_mapping/" + mappingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证数据库中已删除
        assertFalse(mappingRepository.existsById(mappingId));

        // 验证动画仍然存在
        assertTrue(animeRepository.existsById(anime.getAnimeId()));
    }

    @Test
    public void testDeleteMapping_NotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/delete_mapping/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    public void testCreateMapping_Success() throws Exception {
        // Mock FetchService
        AbstractAnimeFetchService mockFetchServiceImpl = mock(AbstractAnimeFetchService.class);
        Mapping mockedMapping = createMapping(Platform.MyAnimeList, "12345");
        mockedMapping.setRawScore(8.5);

        when(fetchService.getFetchService(Platform.MyAnimeList)).thenReturn(mockFetchServiceImpl);
        when(mockFetchServiceImpl.fetchAndSaveMapping("12345")).thenReturn(mockedMapping);

        CreateMappingRequest request = new CreateMappingRequest(Platform.MyAnimeList, "12345");

        mockMvc.perform(post("/api/admin/create_mapping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sourcePlatform").value("MyAnimeList"))
                .andExpect(jsonPath("$.data.platformId").value("12345"));

        verify(fetchService, atLeast(1)).getFetchService(Platform.MyAnimeList);
        verify(mockFetchServiceImpl, times(1)).fetchAndSaveMapping("12345");
    }

    @Test
    public void testCreateMapping_AlreadyExists() throws Exception {
        // 先创建一个 mapping
        Mapping existingMapping = createMapping(Platform.MyAnimeList, "12345");
        mappingRepository.save(existingMapping);

        CreateMappingRequest request = new CreateMappingRequest(Platform.MyAnimeList, "12345");

        mockMvc.perform(post("/api/admin/create_mapping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ALREADY_EXISTS"));
    }

    // 辅助方法：创建测试用的 Anime
    private Anime createAnime(String titleNative) {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative(titleNative);

        Anime anime = new Anime();
        anime.setTitle(titles);
        anime.setStartDate(LocalDate.of(2024, 1, 15));
        anime.setReviewStatus(ReviewStatus.APPROVED);
        return anime;
    }

    // 辅助方法：创建测试用的 Mapping
    private Mapping createMapping(Platform sourcePlatform, String platformId) {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("Test Anime");
        MappingInfo mappingInfo = new MappingInfo(titles, null, null);
        return new Mapping(sourcePlatform, platformId, mappingInfo);
    }
}
