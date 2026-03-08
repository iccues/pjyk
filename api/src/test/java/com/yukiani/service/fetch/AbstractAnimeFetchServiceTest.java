package com.yukiani.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukiani.config.PlatformConfig;
import com.yukiani.config.PlatformConfigProperties;
import com.yukiani.entity.MappingInfo;
import com.yukiani.entity.AnimeTitles;
import com.yukiani.entity.Mapping;
import com.yukiani.entity.Platform;
import com.yukiani.entity.Season;
import com.yukiani.exception.FetchFailedException;
import com.yukiani.repo.MappingRepository;
import com.yukiani.service.MappingRepoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbstractAnimeFetchServiceTest {

    @Mock
    private MappingRepoService mappingRepoService;

    @Mock
    private MappingRepository mappingRepository;

    @Mock
    private PlatformConfigProperties platformConfigProperties;

    private TestAnimeFetchService testService;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        testService = new TestAnimeFetchService();
        testService.mappingRepoService = mappingRepoService;
        testService.mappingRepository = mappingRepository;
        testService.platformConfigProperties = platformConfigProperties;
        objectMapper = new ObjectMapper();

        // 配置 platformConfigProperties mock
        PlatformConfig bangumiConfig = new PlatformConfig();
        bangumiConfig.setScoreMean(6.0);
        bangumiConfig.setScoreStd(1.0);
        bangumiConfig.setPopularityMultiplier(100.0);
        lenient().when(platformConfigProperties.getConfig(Platform.Bangumi)).thenReturn(bangumiConfig);
    }

    @Test
    public void testExtractMappingInfo() throws Exception {
        // 准备 JSON 数据
        String jsonString = """
                {
                    "id": "12345",
                    "title": "Test Anime",
                    "coverImage": "https://example.com/image.jpg",
                    "startDate": "2024-01-15"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        // 调用方法
        MappingInfo result = testService.extractMappingInfo(jsonNode);

        // 验证结果
        assertNotNull(result);
        assertEquals("Test Anime", result.getTitle().getTitleNative());
        assertEquals("https://example.com/image.jpg", result.getCoverImage());
        assertEquals(LocalDate.of(2024, 1, 15), result.getStartDate());
    }

    @Test
    public void testProcessAndSaveMapping_WithScore() throws Exception {
        // 准备 JSON 数据（带评分）
        String jsonString = """
                {
                    "id": "12345",
                    "title": "Test Anime",
                    "score": 8.5
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        // 调用方法
        testService.processAndSaveMapping(jsonNode);

        // 验证：应该调用 saveOrUpdate
        verify(mappingRepoService, times(1)).saveOrUpdate(any(Mapping.class));
    }

    @Test
    public void testProcessAndSaveMapping_WithoutScore() throws Exception {
        // 准备 JSON 数据（无评分）
        String jsonString = """
                {
                    "id": "12345",
                    "title": "Test Anime",
                    "score": 0
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        // 调用方法
        testService.processAndSaveMapping(jsonNode);

        // 验证：应该调用 saveOrUpdate
        verify(mappingRepoService, times(1)).saveOrUpdate(any(Mapping.class));
    }

    @Test
    public void testFetchAndSaveMappings() throws Exception {
        // 准备 JSON 数据
        String jsonString1 = """
                {
                    "id": "1",
                    "title": "Anime 1"
                }
                """;
        String jsonString2 = """
                {
                    "id": "2",
                    "title": "Anime 2"
                }
                """;

        JsonNode jsonNode1 = objectMapper.readTree(jsonString1);
        JsonNode jsonNode2 = objectMapper.readTree(jsonString2);

        // Mock fetchMappingJson 返回两个节点
        testService.mockMediaList = List.of(jsonNode1, jsonNode2);

        // 调用方法
        testService.fetchAndSaveMappings(2024, Season.SPRING);

        // 验证：应该处理所有节点
        verify(mappingRepoService, times(2)).saveOrUpdate(any(Mapping.class));
    }

    @Test
    public void testFetchAndSaveMapping_Success() throws Exception {
        // 准备 JSON 数据
        String jsonString = """
                {
                    "id": "12345",
                    "title": "New Mapping"
                }
                """;

        // Mock fetchSingleMappingJson
        testService.mockSingleNode = objectMapper.readTree(jsonString);

        // 准备 MappingInfo
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("New Mapping");
        MappingInfo mappingInfo = new MappingInfo(titles, null, null);

        // Mock repository
        Mapping savedMapping = new Mapping(Platform.Bangumi, "12345", mappingInfo);
        when(mappingRepository.findBySourcePlatformAndPlatformId(Platform.Bangumi, "12345"))
                .thenReturn(savedMapping);

        // 调用方法
        Mapping result = testService.fetchAndSaveMapping("12345");

        // 验证
        assertNotNull(result);
        assertEquals("12345", result.getPlatformId());
        verify(mappingRepoService, times(1)).saveOrUpdate(any(Mapping.class));
    }

    @Test
    public void testFetchAndCreateMapping_FetchFailed() {
        // Mock fetchSingleMappingJson 返回 null
        testService.mockSingleNode = null;

        // 调用方法并验证异常
        assertThrows(FetchFailedException.class, () -> testService.fetchAndSaveMapping("99999"));

        // 验证：不应该调用 saveOrUpdate
        verify(mappingRepoService, never()).saveOrUpdate(any(Mapping.class));
    }

    @Test
    public void testFetchAndSaveMappings_WebClientResponseException() {
        // Mock fetchMappingJson 抛出 WebClientResponseException
        testService.shouldThrowWebClientException = true;

        // 调用方法并验证异常
        FetchFailedException exception = assertThrows(FetchFailedException.class,
                () -> testService.fetchAndSaveMappings(2024, Season.SPRING));

        // 验证异常信息
        assertTrue(exception.getMessage().contains("Bangumi"));

        // 验证：不应该调用 saveOrUpdate
        verify(mappingRepoService, never()).saveOrUpdate(any(Mapping.class));
    }

    @Test
    public void testFetchAndSaveMappings_GenericException() {
        // Mock fetchMappingJson 抛出一般异常
        testService.shouldThrowGenericException = true;

        // 调用方法并验证异常
        FetchFailedException exception = assertThrows(FetchFailedException.class,
                () -> testService.fetchAndSaveMappings(2024, Season.SPRING));

        // 验证异常信息
        assertTrue(exception.getMessage().contains("Bangumi"));

        // 验证：不应该调用 saveOrUpdate
        verify(mappingRepoService, never()).saveOrUpdate(any(Mapping.class));
    }

    @Test
    public void testFetchAndSaveMapping_WebClientResponseException() {
        // Mock fetchSingleMappingJson 抛出 WebClientResponseException
        testService.shouldThrowWebClientExceptionForSingle = true;

        // 调用方法并验证异常
        FetchFailedException exception = assertThrows(FetchFailedException.class,
                () -> testService.fetchAndSaveMapping("12345"));

        // 验证异常信息
        assertTrue(exception.getMessage().contains("Bangumi"));
        assertTrue(exception.getMessage().contains("12345"));

        // 验证：不应该调用 saveOrUpdate
        verify(mappingRepoService, never()).saveOrUpdate(any(Mapping.class));
    }

    @Test
    public void testFetchAndSaveMapping_GenericException() {
        // Mock fetchSingleMappingJson 抛出一般异常
        testService.shouldThrowGenericExceptionForSingle = true;

        // 调用方法并验证异常
        FetchFailedException exception = assertThrows(FetchFailedException.class,
                () -> testService.fetchAndSaveMapping("12345"));

        // 验证异常信息
        assertTrue(exception.getMessage().contains("Bangumi"));
        assertTrue(exception.getMessage().contains("12345"));

        // 验证：不应该调用 saveOrUpdate
        verify(mappingRepoService, never()).saveOrUpdate(any(Mapping.class));
    }

    // 测试用的具体实现类
    private static class TestAnimeFetchService extends AbstractAnimeFetchService {
        public List<JsonNode> mockMediaList;
        public JsonNode mockSingleNode;
        public boolean shouldThrowWebClientException = false;
        public boolean shouldThrowGenericException = false;
        public boolean shouldThrowWebClientExceptionForSingle = false;
        public boolean shouldThrowGenericExceptionForSingle = false;

        @Override
        protected Platform getPlatform() {
            return Platform.Bangumi;
        }

        @Override
        protected LocalDate extractStartDate(JsonNode jsonNode) {
            String dateStr = jsonNode.path("startDate").asText();
            if (dateStr == null || dateStr.isEmpty()) {
                return LocalDate.of(2024, 1, 1);
            }
            return LocalDate.parse(dateStr);
        }

        @Override
        protected AnimeTitles extractTitles(JsonNode jsonNode) {
            AnimeTitles titles = new AnimeTitles();
            titles.setTitleNative(jsonNode.path("title").asText());
            return titles;
        }

        @Override
        protected String extractCoverImage(JsonNode jsonNode) {
            return jsonNode.path("coverImage").asText();
        }

        @Override
        protected String extractPlatformId(JsonNode jsonNode) {
            return jsonNode.path("id").asText();
        }

        @Override
        protected Double extractRawScore(JsonNode jsonNode) {
            return jsonNode.path("score").asDouble(0.0);
        }

        @Override
        protected double extractRawPopularity(JsonNode jsonNode) {
            return jsonNode.path("popularity").asDouble(0.0);
        }

        @Override
        protected List<JsonNode> fetchMappingJson(int year, Season season) {
            if (shouldThrowWebClientException) {
                throw WebClientResponseException.create(
                        404,
                        "Not Found",
                        null,
                        "API returned 404".getBytes(),
                        null
                );
            }
            if (shouldThrowGenericException) {
                throw new RuntimeException("Generic error during fetch");
            }
            return mockMediaList;
        }

        @Override
        protected JsonNode fetchSingleMappingJson(String platformId) {
            if (shouldThrowWebClientExceptionForSingle) {
                throw WebClientResponseException.create(
                        500,
                        "Internal Server Error",
                        null,
                        "API error".getBytes(),
                        null
                );
            }
            if (shouldThrowGenericExceptionForSingle) {
                throw new RuntimeException("Generic error during single fetch");
            }
            return mockSingleNode;
        }
    }
}
