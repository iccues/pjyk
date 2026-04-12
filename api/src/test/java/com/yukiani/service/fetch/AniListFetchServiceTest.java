package com.yukiani.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukiani.config.PlatformConfig;
import com.yukiani.config.PlatformConfigProperties;
import com.yukiani.entity.AnimeTitles;
import com.yukiani.entity.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AniListFetchServiceTest {

    private AniListFetchService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        service = new AniListFetchService();
        objectMapper = new ObjectMapper();

        // 设置 platformConfigProperties
        PlatformConfigProperties configProperties = new PlatformConfigProperties();
        PlatformConfig aniListConfig = new PlatformConfig();
        aniListConfig.setScoreMean(70.0);
        aniListConfig.setScoreStd(10.0);

        Field aniListField = PlatformConfigProperties.class.getDeclaredField("aniList");
        aniListField.setAccessible(true);
        aniListField.set(configProperties, aniListConfig);

        Field configField = AbstractAnimeFetchService.class.getDeclaredField("platformConfigProperties");
        configField.setAccessible(true);
        configField.set(service, configProperties);
    }

    @Test
    public void testGetPlatform() {
        assertEquals(Platform.AniList, service.getPlatform());
    }

    @Test
    public void testExtractStartDate() throws Exception {
        String jsonString = """
                {
                    "startDate": {
                        "year": 2024,
                        "month": 4,
                        "day": 15
                    }
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        LocalDate result = service.extractStartDate(jsonNode);

        assertEquals(LocalDate.of(2024, 4, 15), result);
    }

    @Test
    public void testExtractTitles() throws Exception {
        String jsonString = """
                {
                    "title": {
                        "romaji": "Shingeki no Kyojin",
                        "english": "Attack on Titan",
                        "native": "進撃の巨人"
                    }
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        AnimeTitles result = service.extractTitles(jsonNode);

        assertNotNull(result);
        assertEquals("Shingeki no Kyojin", result.getTitleRomaji());
        assertEquals("Attack on Titan", result.getTitleEn());
        assertEquals("進撃の巨人", result.getTitleNative());
    }

    @Test
    public void testExtractCoverImage() throws Exception {
        String jsonString = """
                {
                    "coverImage": {
                        "extraLarge": "https://example.com/image.jpg",
                        "large": "https://example.com/image_large.jpg"
                    }
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        String result = service.extractCoverImage(jsonNode);

        assertEquals("https://example.com/image.jpg", result);
    }

    @Test
    public void testExtractPlatformId() throws Exception {
        String jsonString = """
                {
                    "id": "12345"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        String result = service.extractPlatformId(jsonNode);

        assertEquals("12345", result);
    }

    @Test
    public void testExtractRawScore() throws Exception {
        String jsonString = """
                {
                    "averageScore": 85.5
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        double result = service.extractRawScore(jsonNode);

        assertEquals(85.5, result, 0.001);
    }

    @Test
    public void testExtractRawScore_Missing() throws Exception {
        String jsonString = """
                {
                    "id": "12345"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        Double result = service.extractRawScore(jsonNode);

        assertNull(result);
    }

    @Test
    public void testNormalizeScore() {
        // AniList: mean=70.0, std=10.0
        // z = (85.0 - 70.0) / 10.0 = 1.5
        // normalized = 50 + (1.5 * (100 / 6.0)) = 50 + 25 = 75.0
        double result = service.normalizeScore(85.0);

        assertEquals(75.0, result, 0.01);
    }

    @Test
    public void testExtractTitles_PartialData() throws Exception {
        String jsonString = """
                {
                    "title": {
                        "romaji": "Test Anime"
                    }
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        AnimeTitles result = service.extractTitles(jsonNode);

        assertNotNull(result);
        assertEquals("Test Anime", result.getTitleRomaji());
        assertNull(result.getTitleEn());
        assertNull(result.getTitleNative());
    }
}
