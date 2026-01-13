package com.iccues.metaanimebackend.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iccues.metaanimebackend.config.PlatformConfig;
import com.iccues.metaanimebackend.config.PlatformConfigProperties;
import com.iccues.metaanimebackend.entity.MappingInfo;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.exception.FetchFailedException;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import com.iccues.metaanimebackend.service.AnimeRepoService;
import com.iccues.metaanimebackend.service.MappingRepoService;
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
    private AnimeRepoService animeRepoService;

    @Mock
    private AnimeRepository animeRepository;

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
        testService.animeRepoService = animeRepoService;
        testService.animeRepository = animeRepository;
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
    public void testFindOrCreateAnime_NewAnime() throws Exception {
        // 准备 MappingInfo
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("New Anime");
        MappingInfo mappingInfo = new MappingInfo(titles, "https://example.com/new.jpg", LocalDate.of(2024, 4, 1));

        // Mock animeRepoService.findAnime 返回 null（找不到现有动画）
        when(animeRepoService.findAnime(any(LocalDate.class), any(AnimeTitles.class)))
                .thenReturn(null);

        // Mock animeRepoService.createAnime 返回新创建的动画
        Anime newAnime = new Anime();
        newAnime.setTitle(titles);
        newAnime.setStartDate(LocalDate.of(2024, 4, 1));
        newAnime.setCoverImage("https://example.com/new.jpg");
        when(animeRepoService.createAnime(any(MappingInfo.class))).thenReturn(newAnime);
        when(animeRepository.save(any(Anime.class))).thenReturn(newAnime);

        // 调用方法
        Anime result = testService.findOrCreateAnime(mappingInfo);

        // 验证结果
        assertNotNull(result);
        assertEquals("https://example.com/new.jpg", result.getCoverImage());
        verify(animeRepoService, times(1)).createAnime(mappingInfo);
        verify(animeRepository, times(1)).save(newAnime);
    }

    @Test
    public void testFindOrCreateAnime_ExistingAnimeWithCover() throws Exception {
        // 准备 MappingInfo
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("Existing Anime");
        MappingInfo mappingInfo = new MappingInfo(titles, "https://example.com/new.jpg", LocalDate.of(2024, 4, 1));

        // Mock animeRepoService.findAnime 返回一个已有封面的动画
        Anime existingAnime = new Anime();
        existingAnime.setTitle(titles);
        existingAnime.setStartDate(LocalDate.of(2024, 4, 1));
        existingAnime.setCoverImage("https://example.com/old.jpg");

        when(animeRepoService.findAnime(any(LocalDate.class), any(AnimeTitles.class)))
                .thenReturn(existingAnime);

        // 调用方法
        Anime result = testService.findOrCreateAnime(mappingInfo);

        // 验证结果：应该直接返回现有的 Anime，不调用 save
        assertNotNull(result);
        assertEquals("https://example.com/old.jpg", result.getCoverImage());
        verify(animeRepository, never()).save(any(Anime.class));
        verify(animeRepoService, never()).createAnime(any(MappingInfo.class));
    }

    @Test
    public void testLinkMappingToAnime_UnlinkedMapping() throws Exception {
        // 准备 MappingInfo
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("Test Anime");
        MappingInfo mappingInfo = new MappingInfo(titles, "https://example.com/image.jpg", LocalDate.of(2024, 1, 15));

        // 准备未关联的 Mapping
        Mapping mapping = new Mapping(Platform.Bangumi, "12345", mappingInfo);

        // Mock anime - findAnime 返回现有的 Anime
        Anime anime = new Anime();
        anime.setTitle(titles);

        when(animeRepoService.findAnime(any(LocalDate.class), any(AnimeTitles.class)))
                .thenReturn(anime);
        when(animeRepository.save(any(Anime.class))).thenReturn(anime);

        // 调用方法
        testService.linkMappingToAnime(mapping);

        // 验证：findOrCreateAnime 找到现有 Anime 不调用 save，linkMappingToAnime 调用一次，共1次
        verify(animeRepository, times(1)).save(anime);
    }

    @Test
    public void testLinkAllOrphanedMappings() throws Exception {
        // 准备 MappingInfo
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("Orphaned Anime");
        MappingInfo mappingInfo = new MappingInfo(titles, "https://example.com/image.jpg", LocalDate.of(2024, 1, 15));

        Mapping mapping1 = new Mapping(Platform.Bangumi, "1", mappingInfo);
        Mapping mapping2 = new Mapping(Platform.Bangumi, "2", mappingInfo);

        List<Mapping> orphanedMappings = List.of(mapping1, mapping2);

        // Mock repository
        when(mappingRepository.findAllBySourcePlatformAndAnimeIsNull(Platform.Bangumi))
                .thenReturn(orphanedMappings);

        // Mock anime service - findAnime 返回现有的 Anime
        Anime anime = new Anime();
        anime.setTitle(titles);

        when(animeRepoService.findAnime(any(LocalDate.class), any(AnimeTitles.class)))
                .thenReturn(anime);
        when(animeRepository.save(any(Anime.class))).thenReturn(anime);

        // 调用方法
        testService.linkAllOrphanedMappings();

        // 验证：每个 mapping 调用 linkMappingToAnime，findOrCreateAnime 找到现有 Anime 不调用 save，linkMappingToAnime 调用一次，共2次
        verify(animeRepository, times(2)).save(any(Anime.class));
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
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        // Mock fetchSingleMappingJson
        testService.mockSingleNode = jsonNode;

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
        protected double extractRawScore(JsonNode jsonNode) {
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
