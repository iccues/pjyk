package com.yukiani.server.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukiani.server.config.PlatformConfig;
import com.yukiani.server.config.PlatformConfigProperties;
import com.yukiani.server.entity.MappingInfo;
import com.yukiani.server.entity.AnimeTitles;
import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.Platform;
import com.yukiani.server.entity.Season;
import com.yukiani.server.exception.FetchFailedException;
import com.yukiani.server.service.MappingRepoService;
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
    private PlatformConfigProperties platformConfigProperties;

    private TestAnimeFetchService testService;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        testService = new TestAnimeFetchService();
        testService.mappingRepoService = mappingRepoService;
        testService.platformConfigProperties = platformConfigProperties;
        objectMapper = new ObjectMapper();

        // 配置 platformConfigProperties mock
        PlatformConfig bangumiConfig = new PlatformConfig();
        bangumiConfig.setScoreMean(6.0);
        bangumiConfig.setScoreStd(1.0);
        bangumiConfig.setScorePriorStrength(1000.0);
        bangumiConfig.setPopularityMedian(800.0);
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
    public void testBuildMapping_WithScore() throws Exception {
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
        Mapping mapping = testService.buildMapping(jsonNode);

        // 验证：只构造不保存
        assertEquals(Platform.Bangumi, mapping.getSourcePlatform());
        assertEquals("12345", mapping.getPlatformId());
        assertEquals(8.5, mapping.getRawScore(), 0.0001);
        assertNotNull(mapping.getNormalizedScore());
        verifyNoInteractions(mappingRepoService);
    }

    @Test
    public void testBuildMapping_AdjustsScoreBeforeNormalization() throws Exception {
        String jsonString = """
                {
                    "id": "12345",
                    "title": "Test Anime",
                    "score": 9.0,
                    "popularity": 80
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        Mapping mapping = testService.buildMapping(jsonNode);

        assertEquals(9.0, mapping.getRawScore(), 0.0001);
        assertEquals(80.0, mapping.getRawPopularity(), 0.0001);
        assertEquals(1000.0, mapping.getNormalizedPopularity(), 0.0001);
        assertEquals(75.0, mapping.getNormalizedScore(), 0.0001);
    }

    @Test
    public void testRecalculateMetrics_UsesRawValues() {
        Mapping mapping = new Mapping();
        mapping.setSourcePlatform(Platform.Bangumi);
        mapping.setRawPopularity(80.0);
        mapping.setRawScore(9.0);

        testService.recalculateMetrics(mapping);

        assertEquals(1000.0, mapping.getNormalizedPopularity(), 0.0001);
        assertEquals(75.0, mapping.getNormalizedScore(), 0.0001);
    }

    @Test
    public void testRecalculateMetrics_RejectsAnotherPlatform() {
        Mapping mapping = new Mapping();
        mapping.setSourcePlatform(Platform.AniList);

        assertThrows(IllegalArgumentException.class, () -> testService.recalculateMetrics(mapping));
    }

    @Test
    public void testBuildMapping_WithoutScore() throws Exception {
        // 准备 JSON 数据（无评分）
        String jsonString = """
                {
                    "id": "12345",
                    "title": "Test Anime",
                    "score": 0
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        // 调用方法并验证：无有效评分时归一化评分为空
        assertNull(testService.buildMapping(jsonNode).getNormalizedScore());
    }

    @Test
    public void testBuildMapping_KeepsMissingFieldsAsNull() throws Exception {
        // 构造时不做校验，缺失的 platformId 和开播日期留给调用方判断
        Mapping mapping = testService.buildMapping(objectMapper.readTree("""
                { "title": "Empty Media" }
                """));

        assertNull(mapping.getPlatformId());
        assertNull(mapping.getMappingInfo().getStartDate());
    }

    @Test
    public void testFetchAndSaveMappings() throws Exception {
        // 准备 JSON 数据
        String jsonString1 = """
                {
                    "id": "1",
                    "title": "Anime 1",
                    "startDate": "2024-04-01"
                }
                """;
        String jsonString2 = """
                {
                    "id": "2",
                    "title": "Anime 2",
                    "startDate": "2024-04-08"
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
    public void testFetchAndSaveMappings_SkipsIncompleteEntries() throws Exception {
        // 缺少开播日期或 platformId 的条目跳过，其余照常保存
        testService.mockMediaList = List.of(
                objectMapper.readTree("""
                        { "id": "1", "title": "Anime 1", "startDate": "2024-04-01" }
                        """),
                objectMapper.readTree("""
                        { "id": "2", "title": "Anime 2" }
                        """),
                objectMapper.readTree("""
                        { "title": "Anime 3", "startDate": "2024-04-08" }
                        """));

        testService.fetchAndSaveMappings(2024, Season.SPRING);

        var mappingCaptor = org.mockito.ArgumentCaptor.forClass(Mapping.class);
        verify(mappingRepoService).saveOrUpdate(mappingCaptor.capture());
        assertEquals("1", mappingCaptor.getValue().getPlatformId());
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

        // Mock 保存结果
        Mapping savedMapping = new Mapping(Platform.Bangumi, "12345", mappingInfo);
        when(mappingRepoService.saveOrUpdate(any(Mapping.class))).thenReturn(savedMapping);

        // 调用方法
        Mapping result = testService.fetchAndSaveMapping("12345");

        // 验证：直接返回保存后的 Mapping
        assertSame(savedMapping, result);
        verify(mappingRepoService, times(1)).saveOrUpdate(any(Mapping.class));
    }

    @Test
    public void testFetchAndSaveMapping_WithoutStartDate_StillSaved() throws Exception {
        // 单条抓取时开播日期缺失照常保存，评分和热度仍会更新
        testService.mockSingleNode = objectMapper.readTree("""
                { "id": "12345", "title": "No Date", "score": 8.0 }
                """);

        testService.fetchAndSaveMapping("12345");

        var mappingCaptor = org.mockito.ArgumentCaptor.forClass(Mapping.class);
        verify(mappingRepoService).saveOrUpdate(mappingCaptor.capture());
        assertNull(mappingCaptor.getValue().getMappingInfo().getStartDate());
        assertEquals(8.0, mappingCaptor.getValue().getRawScore(), 0.0001);
    }

    @Test
    public void testFetchAndSaveMapping_MissingPlatformId_Throws() throws Exception {
        // 平台返回的数据缺少 platformId 时不能静默返回旧数据
        testService.mockSingleNode = objectMapper.readTree("""
                { "title": "Empty Media" }
                """);

        assertThrows(FetchFailedException.class, () -> testService.fetchAndSaveMapping("12345"));

        verify(mappingRepoService, never()).saveOrUpdate(any(Mapping.class));
    }

    @Test
    public void testFetchAndSaveMapping_DifferentPlatformId_Throws() throws Exception {
        // 平台返回了其他条目，保存它并不能更新请求的 Mapping
        testService.mockSingleNode = objectMapper.readTree("""
                { "id": "99999", "title": "Another Anime", "startDate": "2024-04-01" }
                """);

        assertThrows(FetchFailedException.class, () -> testService.fetchAndSaveMapping("12345"));

        verify(mappingRepoService, never()).saveOrUpdate(any(Mapping.class));
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
        assertInstanceOf(WebClientResponseException.class, exception.getCause(), "Original exception should be kept as cause");

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
        assertEquals("Generic error during fetch", exception.getCause().getMessage());

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
        assertInstanceOf(WebClientResponseException.class, exception.getCause(), "Original exception should be kept as cause");

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
        assertEquals("Generic error during single fetch", exception.getCause().getMessage());

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
            // 与各平台实现一致：缺少开播日期时返回 null
            String dateStr = jsonNode.path("startDate").asText(null);
            if (dateStr == null || dateStr.isEmpty()) {
                return null;
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
            return jsonNode.path("id").asText(null);
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
