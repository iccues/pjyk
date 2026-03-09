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

public class BangumiFetchServiceTest {

    private BangumiFetchService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        service = new BangumiFetchService();
        objectMapper = new ObjectMapper();

        // 设置 platformConfigProperties
        PlatformConfigProperties configProperties = new PlatformConfigProperties();
        PlatformConfig bangumiConfig = new PlatformConfig();
        bangumiConfig.setScoreMean(6.0);
        bangumiConfig.setScoreStd(1.0);

        Field bangumiField = PlatformConfigProperties.class.getDeclaredField("bangumi");
        bangumiField.setAccessible(true);
        bangumiField.set(configProperties, bangumiConfig);

        Field configField = AbstractAnimeFetchService.class.getDeclaredField("platformConfigProperties");
        configField.setAccessible(true);
        configField.set(service, configProperties);
    }

    @Test
    public void testGetPlatform() {
        assertEquals(Platform.Bangumi, service.getPlatform());
    }

    @Test
    public void testExtractStartDate() throws Exception {
        String jsonString = """
                {
                    "date": "2024-04-15"
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
                    "name": "進撃の巨人",
                    "name_cn": "进击的巨人"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        AnimeTitles result = service.extractTitles(jsonNode);

        assertNotNull(result);
        assertEquals("進撃の巨人", result.getTitleNative());
        assertEquals("进击的巨人", result.getTitleCn());
    }

    @Test
    public void testExtractCoverImage() throws Exception {
        String jsonString = """
                {
                    "images": {
                        "large": "https://example.com/image_large.jpg",
                        "medium": "https://example.com/image_medium.jpg",
                        "common": "https://example.com/image_common.jpg"
                    }
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        String result = service.extractCoverImage(jsonNode);

        assertEquals("https://example.com/image_common.jpg", result);
    }

    @Test
    public void testExtractPlatformId() throws Exception {
        String jsonString = """
                {
                    "id": "67890"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        String result = service.extractPlatformId(jsonNode);

        assertEquals("67890", result);
    }

    @Test
    public void testExtractRawScore() throws Exception {
        String jsonString = """
                {
                    "rating": {
                        "score": 8.5
                    }
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
        // Bangumi: mean=6.0, std=1.0
        // z = (8.5 - 6.0) / 1.0 = 2.5
        // normalized = 50 + (2.5 * (100 / 6.0)) = 50 + 41.67 = 91.67
        double result = service.normalizeScore(8.5);

        assertEquals(91.67, result, 0.01);
    }

    @Test
    public void testNormalizeScore_MinValue() {
        // z = (1.0 - 6.0) / 1.0 = -5.0
        // normalized = 50 + (-5.0 * (100 / 6.0)) = 50 - 83.33 = -33.33
        Double result = service.normalizeScore(1.0);

        assertNotNull(result);
        assertEquals(-33.33, result, 0.01);
    }

    @Test
    public void testNormalizeScore_MaxValue() {
        // z = (10.0 - 6.0) / 1.0 = 4.0
        // normalized = 50 + (4.0 * (100 / 6.0)) = 50 + 66.67 = 116.67
        double result = service.normalizeScore(10.0);

        assertEquals(116.67, result, 0.01);
    }

    @Test
    public void testExtractTitles_MissingCnName() throws Exception {
        String jsonString = """
                {
                    "name": "Test Anime"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        AnimeTitles result = service.extractTitles(jsonNode);

        assertNotNull(result);
        assertEquals("Test Anime", result.getTitleNative());
        assertEquals("", result.getTitleCn());
    }
}
