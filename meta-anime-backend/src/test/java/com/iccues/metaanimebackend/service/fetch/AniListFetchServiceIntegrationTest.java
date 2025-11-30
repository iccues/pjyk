package com.iccues.metaanimebackend.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iccues.metaanimebackend.entity.Season;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AniListFetchServiceIntegrationTest {

    @Mock
    private WebClient mockWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private AniListFetchService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        service = new AniListFetchService();
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(service, "aniListWebClient", mockWebClient);
    }

    @Test
    public void testFetchPage_GraphQLQuery() throws Exception {
        // 加载测试数据
        String jsonContent = Files.readString(
                Paths.get("src/test/resources/fetch/anilist/page1_has_next.json")
        );
        JsonNode expectedResponse = objectMapper.readTree(jsonContent);

        // Mock WebClient 链式调用 (AniList 使用 post().bodyValue().retrieve())
        when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(expectedResponse));

        // 执行测试
        JsonNode result = service.fetchPage(2024, Season.SPRING, 1);

        // 验证结果 (fetchPage 返回的是 data.Page，不是完整响应)
        assertNotNull(result);
        assertTrue(result.has("pageInfo"));
        assertTrue(result.has("media"));
        assertTrue(result.path("pageInfo").path("hasNextPage").asBoolean());

        // 验证 WebClient 被正确调用
        verify(mockWebClient).post();
        verify(requestBodyUriSpec).bodyValue(any());
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(JsonNode.class);
    }

    @Test
    public void testFetchSingleMappingJson_GraphQLQuery() throws Exception {
        // 加载测试数据
        String jsonContent = Files.readString(
                Paths.get("src/test/resources/fetch/anilist/single_anime.json")
        );
        JsonNode expectedResponse = objectMapper.readTree(jsonContent);

        // Mock WebClient 链式调用 (AniList 使用 post().bodyValue().retrieve())
        when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(expectedResponse));

        // 执行测试
        JsonNode result = service.fetchSingleMappingJson("55555");

        // 验证结果 (fetchSingleMappingJson 返回的是 data.Media，不是完整响应)
        assertNotNull(result);
        assertTrue(result.has("id"));
        assertTrue(result.has("title"));
        assertEquals("55555", result.path("id").asText());

        // 验证 WebClient 被正确调用
        verify(mockWebClient).post();
    }

    @Test
    public void testFetchMappingJson_MultiplePages() throws Exception {
        // 加载第一页数据（hasNextPage: true）
        String page1Content = Files.readString(
                Paths.get("src/test/resources/fetch/anilist/page1_has_next.json")
        );
        JsonNode page1Response = objectMapper.readTree(page1Content);

        // 加载第二页数据（hasNextPage: false）
        String page2Content = Files.readString(
                Paths.get("src/test/resources/fetch/anilist/page2_no_next.json")
        );
        JsonNode page2Response = objectMapper.readTree(page2Content);

        // Mock WebClient 链式调用 - 第一次调用返回 page1，第二次返回 page2
        when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
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
        assertEquals("98765", result.get(0).path("id").asText());
        assertEquals("98766", result.get(1).path("id").asText());
        assertEquals("98767", result.get(2).path("id").asText());

        // 验证 WebClient 被调用了两次
        verify(mockWebClient, times(2)).post();
    }

    @Test
    public void testFetchMappingJson_ResponsePathExtraction() throws Exception {
        // Mock WebClient (使用 page2_no_next 数据，因为它 hasNextPage=false)
        String page2Content = Files.readString(
                Paths.get("src/test/resources/fetch/anilist/page2_no_next.json")
        );
        JsonNode page2Response = objectMapper.readTree(page2Content);

        when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(page2Response));

        // 执行测试
        List<JsonNode> result = service.fetchMappingJson(2024, Season.SPRING);

        // 验证从 data.Page.media 正确提取了数据
        assertNotNull(result);
        assertEquals(1, result.size());

        // 验证提取的数据结构正确
        assertTrue(result.getFirst().has("id"));
        assertTrue(result.getFirst().has("title"));
        assertTrue(result.getFirst().has("startDate"));
        assertTrue(result.getFirst().has("coverImage"));
        assertTrue(result.getFirst().has("averageScore"));
    }

    @Test
    public void testFetchSingleMappingJson_ResponsePathExtraction() throws Exception {
        // 加载测试数据
        String jsonContent = Files.readString(
                Paths.get("src/test/resources/fetch/anilist/single_anime.json")
        );
        JsonNode expectedResponse = objectMapper.readTree(jsonContent);

        // Mock WebClient
        when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(expectedResponse));

        // 执行测试
        JsonNode result = service.fetchSingleMappingJson("55555");

        // 验证从 data.Media 正确提取了数据 (返回的就是 Media 节点)
        assertNotNull(result);
        assertTrue(result.has("id"));
        assertTrue(result.has("title"));
        assertEquals("55555", result.path("id").asText());
    }

    @Test
    public void testExtractStartDate_InvalidDate() throws Exception {
        // 测试无效日期处理（year=0, month=0, day=0）
        String jsonString = """
                {
                    "startDate": {
                        "year": 0,
                        "month": 0,
                        "day": 0
                    }
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        // AniList 的 extractStartDate 不捕获异常，应该抛出 DateTimeException
        assertThrows(Exception.class, () -> service.extractStartDate(jsonNode));
    }

    @Test
    public void testExtractStartDate_MissingDateFields() throws Exception {
        // 测试缺少日期字段（asInt() 返回 0）
        String jsonString = """
                {
                    "id": "12345"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        // asInt() 对缺失字段返回 0，LocalDate.of(0, 0, 0) 会抛出异常
        assertThrows(Exception.class, () -> service.extractStartDate(jsonNode));
    }

    @Test
    public void testFetchPage_NullResponse() {
        // Mock WebClient 返回 null
        when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.empty());

        // 执行测试
        JsonNode result = service.fetchPage(2024, Season.SPRING, 1);

        // 验证结果为 null
        assertNull(result);
    }
}
