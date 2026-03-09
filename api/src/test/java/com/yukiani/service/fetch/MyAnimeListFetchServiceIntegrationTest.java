package com.yukiani.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukiani.entity.Season;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MyAnimeListFetchServiceIntegrationTest {

    @Mock
    private WebClient mockWebClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private MyAnimeListFetchService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        service = new MyAnimeListFetchService();
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(service, "myAnimeListWebClient", mockWebClient);
    }

    @Test
    public void testFetchPage_Success() throws Exception {
        // 加载测试数据
        String jsonContent = Files.readString(
                Paths.get("src/test/resources/fetch/myanimelist/seasonal_page1.json")
        );
        JsonNode expectedResponse = objectMapper.readTree(jsonContent);

        // Mock WebClient 链式调用
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(expectedResponse));

        // 执行测试
        JsonNode result = service.fetchPage(2024, Season.SPRING, 0);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.has("data"));
        assertTrue(result.has("paging"));
        assertEquals(2, result.path("data").size());

        // 验证 WebClient 被正确调用
        verify(mockWebClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(JsonNode.class);
    }

    @Test
    public void testFetchSingleMappingJson_Success() throws Exception {
        // 加载测试数据
        String jsonContent = Files.readString(
                Paths.get("src/test/resources/fetch/myanimelist/single_anime.json")
        );
        JsonNode expectedResponse = objectMapper.readTree(jsonContent);

        // Mock WebClient 链式调用
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(expectedResponse));

        // 执行测试
        JsonNode result = service.fetchSingleMappingJson("54321");

        // 验证结果
        assertNotNull(result);
        assertEquals("54321", result.path("id").asText());

        // 验证 WebClient 被正确调用
        verify(mockWebClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
    }

    @Test
    public void testFetchMappingJson_MultiplePages() throws Exception {
        // 加载第一页数据（有 next）
        String page1Content = Files.readString(
                Paths.get("src/test/resources/fetch/myanimelist/seasonal_page1.json")
        );
        JsonNode page1Response = objectMapper.readTree(page1Content);

        // 加载第二页数据（无 next）
        String page2Content = Files.readString(
                Paths.get("src/test/resources/fetch/myanimelist/seasonal_page2_last.json")
        );
        JsonNode page2Response = objectMapper.readTree(page2Content);

        // Mock WebClient 链式调用 - 第一次调用返回 page1，第二次返回 page2
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(page1Response))
                .thenReturn(Mono.just(page2Response));

        // 执行测试
        List<JsonNode> result = service.fetchMappingJson(2024, Season.SPRING);

        // 验证结果 - 应该有 3 个动漫（page1 有 2 个，page2 有 1 个）
        assertNotNull(result);
        assertEquals(3, result.size());

        // 验证 ID
        assertEquals("12345", result.get(0).path("id").asText());
        assertEquals("12346", result.get(1).path("id").asText());
        assertEquals("12347", result.get(2).path("id").asText());

        // 验证 WebClient 被调用了两次
        verify(mockWebClient, times(2)).get();
    }

    @Test
    public void testFetchMappingJson_EmptyResult() throws Exception {
        // 创建空结果响应
        String emptyJson = "{\"data\": [], \"paging\": {}}";
        JsonNode emptyResponse = objectMapper.readTree(emptyJson);

        // Mock WebClient
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(emptyResponse));

        // 执行测试
        List<JsonNode> result = service.fetchMappingJson(2024, Season.SPRING);

        // 验证结果 - 应该返回空列表
        assertNotNull(result);
        assertEquals(0, result.size());

        // 验证 WebClient 只被调用了一次
        verify(mockWebClient, times(1)).get();
    }

    @Test
    public void testAddAnimeNodesToList_FilterMusic() throws Exception {
        // 加载包含 music 和 pv 的测试数据
        String jsonContent = Files.readString(
                Paths.get("src/test/resources/fetch/myanimelist/with_music_pv.json")
        );
        JsonNode responseWithMusicPV = objectMapper.readTree(jsonContent);

        // Mock WebClient
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(responseWithMusicPV));

        // 执行测试
        List<JsonNode> result = service.fetchMappingJson(2024, Season.SPRING);

        // 验证结果 - 只应该有 2 个（tv 和 movie），music 和 pv 被过滤
        assertNotNull(result);
        assertEquals(2, result.size());

        // 验证保留的是 tv 和 movie
        assertEquals("11111", result.get(0).path("id").asText());
        assertEquals("tv", result.get(0).path("media_type").asText());
        assertEquals("44444", result.get(1).path("id").asText());
        assertEquals("movie", result.get(1).path("media_type").asText());
    }

    @Test
    public void testAddAnimeNodesToList_FilterPV() throws Exception {
        // 这个测试已经包含在 testAddAnimeNodesToList_FilterMusic 中
        // 因为测试数据同时包含 music 和 pv
        String jsonContent = Files.readString(
                Paths.get("src/test/resources/fetch/myanimelist/with_music_pv.json")
        );
        JsonNode responseWithMusicPV = objectMapper.readTree(jsonContent);

        // 验证原始数据包含 pv
        JsonNode pvNode = responseWithMusicPV.path("data").get(2).path("node");
        assertEquals("pv", pvNode.path("media_type").asText());

        // Mock WebClient
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(responseWithMusicPV));

        // 执行测试
        List<JsonNode> result = service.fetchMappingJson(2024, Season.SPRING);

        // 验证 pv 被过滤掉
        result.forEach(node -> {
            String mediaType = node.path("media_type").asText();
            assertNotEquals("pv", mediaType);
            assertNotEquals("music", mediaType);
        });
    }

    @Test
    public void testAddAnimeNodesToList_AcceptValidTypes() throws Exception {
        // 加载正常的季度数据（包含 tv, movie, ova）
        String page1Content = Files.readString(
                Paths.get("src/test/resources/fetch/myanimelist/seasonal_page1.json")
        );
        JsonNode page1Response = objectMapper.readTree(page1Content);

        String page2Content = Files.readString(
                Paths.get("src/test/resources/fetch/myanimelist/seasonal_page2_last.json")
        );
        JsonNode page2Response = objectMapper.readTree(page2Content);

        // Mock WebClient
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(page1Response))
                .thenReturn(Mono.just(page2Response));

        // 执行测试
        List<JsonNode> result = service.fetchMappingJson(2024, Season.SPRING);

        // 验证所有有效类型都被接受
        assertEquals(3, result.size());
        assertEquals("tv", result.get(0).path("media_type").asText());
        assertEquals("movie", result.get(1).path("media_type").asText());
        assertEquals("ova", result.get(2).path("media_type").asText());
    }

    @Test
    public void testFetchPage_NullResponse() {
        // Mock WebClient 返回 null
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.empty());

        // 执行测试
        JsonNode result = service.fetchPage(2024, Season.SPRING, 0);

        // 验证结果为 null
        assertNull(result);
    }
}
