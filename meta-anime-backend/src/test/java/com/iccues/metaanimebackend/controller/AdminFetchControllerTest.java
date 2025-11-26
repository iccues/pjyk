package com.iccues.metaanimebackend.controller;

import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.service.fetch.FetchService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminFetchControllerTest {

    @Resource
    private MockMvc mockMvc;

    @MockitoBean
    private FetchService fetchService;

    @Test
    public void testFetchAnime_Success() throws Exception {
        doNothing().when(fetchService).fetchAnime(2024, Season.SPRING);

        mockMvc.perform(post("/api/admin/fetch/anime")
                        .param("year", "2024")
                        .param("season", "SPRING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("数据抓取任务已启动"));

        verify(fetchService, times(1)).fetchAnime(2024, Season.SPRING);
    }

    @Test
    public void testFetchAnime_WinterSeason() throws Exception {
        doNothing().when(fetchService).fetchAnime(2024, Season.WINTER);

        mockMvc.perform(post("/api/admin/fetch/anime")
                        .param("year", "2024")
                        .param("season", "WINTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(fetchService, times(1)).fetchAnime(2024, Season.WINTER);
    }

    @Test
    public void testFetchAnime_SummerSeason() throws Exception {
        doNothing().when(fetchService).fetchAnime(2024, Season.SUMMER);

        mockMvc.perform(post("/api/admin/fetch/anime")
                        .param("year", "2024")
                        .param("season", "SUMMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(fetchService, times(1)).fetchAnime(2024, Season.SUMMER);
    }

    @Test
    public void testFetchAnime_FallSeason() throws Exception {
        doNothing().when(fetchService).fetchAnime(2024, Season.FALL);

        mockMvc.perform(post("/api/admin/fetch/anime")
                        .param("year", "2024")
                        .param("season", "FALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(fetchService, times(1)).fetchAnime(2024, Season.FALL);
    }

    @Test
    public void testFetchMapping_Success() throws Exception {
        doNothing().when(fetchService).fetchMapping(2024, Season.SPRING);

        mockMvc.perform(post("/api/admin/fetch/mapping")
                        .param("year", "2024")
                        .param("season", "SPRING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("映射抓取任务已启动"));

        verify(fetchService, times(1)).fetchMapping(2024, Season.SPRING);
    }

    @Test
    public void testFetchMapping_DifferentYearAndSeason() throws Exception {
        doNothing().when(fetchService).fetchMapping(2023, Season.WINTER);

        mockMvc.perform(post("/api/admin/fetch/mapping")
                        .param("year", "2023")
                        .param("season", "WINTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(fetchService, times(1)).fetchMapping(2023, Season.WINTER);
    }

    @Test
    public void testLinkMappings_Success() throws Exception {
        doNothing().when(fetchService).linkMappings();

        mockMvc.perform(post("/api/admin/fetch/link"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("映射合并任务已启动"));

        verify(fetchService, times(1)).linkMappings();
    }

    @Test
    public void testCalculateScores_Success() throws Exception {
        doNothing().when(fetchService).calculateAllAverageScore();

        mockMvc.perform(post("/api/admin/fetch/calculate_scores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("评分计算任务已启动"));

        verify(fetchService, times(1)).calculateAllAverageScore();
    }

    @Test
    public void testFetchAnime_ServiceException() throws Exception {
        // Mock service 抛出异常
        doThrow(new RuntimeException("服务异常")).when(fetchService).fetchAnime(2024, Season.SPRING);

        mockMvc.perform(post("/api/admin/fetch/anime")
                        .param("year", "2024")
                        .param("season", "SPRING"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verify(fetchService, times(1)).fetchAnime(2024, Season.SPRING);
    }

    @Test
    public void testLinkMappings_ServiceException() throws Exception {
        // Mock service 抛出异常
        doThrow(new RuntimeException("链接失败")).when(fetchService).linkMappings();

        mockMvc.perform(post("/api/admin/fetch/link"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verify(fetchService, times(1)).linkMappings();
    }

    @Test
    public void testCalculateScores_ServiceException() throws Exception {
        // Mock service 抛出异常
        doThrow(new RuntimeException("计算失败")).when(fetchService).calculateAllAverageScore();

        mockMvc.perform(post("/api/admin/fetch/calculate_scores"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verify(fetchService, times(1)).calculateAllAverageScore();
    }
}
