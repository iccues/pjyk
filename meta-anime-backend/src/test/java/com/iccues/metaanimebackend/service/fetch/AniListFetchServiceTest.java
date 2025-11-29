package com.iccues.metaanimebackend.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AniListFetchServiceTest {

    private AniListFetchService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        service = new AniListFetchService();
        objectMapper = new ObjectMapper();
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

        double result = service.extractRawScore(jsonNode);

        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testNormalizeScore() {
        // AniList 评分已经是 0-100，不需要归一化
        double result = service.normalizeScore(85.5);

        assertEquals(85.5, result, 0.001);
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
        assertEquals("", result.getTitleEn());
        assertEquals("", result.getTitleNative());
    }
}
