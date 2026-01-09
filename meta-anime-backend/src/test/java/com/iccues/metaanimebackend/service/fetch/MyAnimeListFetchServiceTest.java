package com.iccues.metaanimebackend.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iccues.metaanimebackend.config.PlatformConfig;
import com.iccues.metaanimebackend.config.PlatformConfigProperties;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class MyAnimeListFetchServiceTest {

    private MyAnimeListFetchService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        service = new MyAnimeListFetchService();
        objectMapper = new ObjectMapper();

        // 设置 platformConfigProperties
        PlatformConfigProperties configProperties = new PlatformConfigProperties();
        PlatformConfig malConfig = new PlatformConfig();
        malConfig.setScoreMean(7.0);
        malConfig.setScoreStd(1.0);

        Field malField = PlatformConfigProperties.class.getDeclaredField("myAnimeList");
        malField.setAccessible(true);
        malField.set(configProperties, malConfig);

        Field configField = AbstractAnimeFetchService.class.getDeclaredField("platformConfigProperties");
        configField.setAccessible(true);
        configField.set(service, configProperties);
    }

    @Test
    public void testGetPlatform() {
        assertEquals(Platform.MyAnimeList, service.getPlatform());
    }

    @Test
    public void testExtractStartDate() throws Exception {
        String jsonString = """
                {
                    "start_date": "2024-04-15"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        LocalDate result = service.extractStartDate(jsonNode);

        assertEquals(LocalDate.of(2024, 4, 15), result);
    }

    @Test
    public void testExtractStartDate_InvalidDate() throws Exception {
        String jsonString = """
                {
                    "start_date": "invalid-date"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        LocalDate result = service.extractStartDate(jsonNode);

        // 应该返回 null（异常被捕获）
        assertNull(result);
    }

    @Test
    public void testExtractStartDate_MissingDate() throws Exception {
        String jsonString = """
                {
                    "id": "12345"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        LocalDate result = service.extractStartDate(jsonNode);

        // 应该返回 null（异常被捕获）
        assertNull(result);
    }

    @Test
    public void testExtractTitles() throws Exception {
        String jsonString = """
                {
                    "alternative_titles": {
                        "en": "Attack on Titan",
                        "ja": "進撃の巨人"
                    }
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        AnimeTitles result = service.extractTitles(jsonNode);

        assertNotNull(result);
        assertEquals("Attack on Titan", result.getTitleEn());
        assertEquals("進撃の巨人", result.getTitleNative());
    }

    @Test
    public void testExtractCoverImage() throws Exception {
        String jsonString = """
                {
                    "main_picture": {
                        "large": "https://example.com/image_large.jpg",
                        "medium": "https://example.com/image_medium.jpg"
                    }
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        String result = service.extractCoverImage(jsonNode);

        assertEquals("https://example.com/image_large.jpg", result);
    }

    @Test
    public void testExtractPlatformId() throws Exception {
        String jsonString = """
                {
                    "id": "54321"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        String result = service.extractPlatformId(jsonNode);

        assertEquals("54321", result);
    }

    @Test
    public void testExtractRawScore() throws Exception {
        String jsonString = """
                {
                    "mean": 8.5
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        double result = service.extractRawScore(jsonNode);

        assertEquals(8.5, result, 0.001);
    }

    @Test
    public void testExtractRawScore_Missing() throws Exception {
        String jsonString = """
                {
                    "id": "12345"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        double result = service.extractRawScore(jsonNode);

        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testNormalizeScore() {
        // MAL: mean=7.0, std=1.0
        // z = (8.5 - 7.0) / 1.0 = 1.5
        // normalized = 50 + (1.5 * (100 / 6.0)) = 50 + 25 = 75.0
        double result = service.normalizeScore(8.5);

        assertEquals(75.0, result, 0.01);
    }

    @Test
    public void testNormalizeScore_MinValue() {
        // z = (1.0 - 7.0) / 1.0 = -6.0
        // normalized = 50 + (-6.0 * (100 / 6.0)) = 50 - 100 = -50.0
        Double result = service.normalizeScore(1.0);

        assertNotNull(result);
        assertEquals(-50.0, result, 0.01);
    }

    @Test
    public void testNormalizeScore_MaxValue() {
        // z = (10.0 - 7.0) / 1.0 = 3.0
        // normalized = 50 + (3.0 * (100 / 6.0)) = 50 + 50 = 100.0
        double result = service.normalizeScore(10.0);

        assertEquals(100.0, result, 0.01);
    }

    @Test
    public void testExtractTitles_PartialData() throws Exception {
        String jsonString = """
                {
                    "alternative_titles": {
                        "en": "Test Anime"
                    }
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        AnimeTitles result = service.extractTitles(jsonNode);

        assertNotNull(result);
        assertEquals("Test Anime", result.getTitleEn());
        assertEquals("", result.getTitleNative());
    }

    @Test
    public void testExtractTitles_MissingAlternativeTitles() throws Exception {
        String jsonString = """
                {
                    "id": "12345"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        AnimeTitles result = service.extractTitles(jsonNode);

        assertNotNull(result);
        assertEquals("", result.getTitleEn());
        assertEquals("", result.getTitleNative());
    }
}
