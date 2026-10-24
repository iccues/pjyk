package com.yukiani.server.service.fetch;

import com.yukiani.server.entity.Season;
import com.yukiani.server.service.SeasonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BangumiFetchServiceIntegrationTest {

    @Mock
    private WebClient mockWebClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private BangumiFetchService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        service = new BangumiFetchService();
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(service, "bangumiWebClient", mockWebClient);
        ReflectionTestUtils.setField(service, "seasonService", new SeasonService());
    }

    @Test
    public void testFetchPage_Success() throws Exception {
        // 加载测试数据
        String jsonContent = Files.readString(
                Paths.get("src/test/resources/fetch/bangumi/calendar_total_120.json")
        );
        JsonNode expectedResponse = objectMapper.readTree(jsonContent);

        // Mock WebClient 链式调用
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(expectedResponse));

        // 执行测试
        JsonNode result = service.fetchPage(2024, 4, 0);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.has("total"));
        assertTrue(result.has("data"));
        assertEquals(120, result.path("total").asInt());
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
                Paths.get("src/test/resources/fetch/bangumi/single_subject.json")
        );
        JsonNode expectedResponse = objectMapper.readTree(jsonContent);

        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/subjects/{id}", "88888")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(expectedResponse));

        JsonNode result = service.fetchSingleMappingJson("88888");

        assertNotNull(result);
        assertEquals("88888", result.path("id").asString());
        verify(mockWebClient).get();
        verify(requestHeadersUriSpec).uri("/subjects/{id}", "88888");
    }

    @Test
    public void testFetchMappingJson_TotalPageCalculation() throws Exception {
        // 加载第一页数据 (total=120, pageSize=50, 应该有 3 页)
        String page1Content = Files.readString(
                Paths.get("src/test/resources/fetch/bangumi/calendar_total_120.json")
        );
        JsonNode page1Response = objectMapper.readTree(page1Content);

        // 创建第2页和第3页的响应（模拟空数据，只需要验证调用次数）
        String emptyPageJson = "{\"total\": 120, \"data\": []}";
        JsonNode emptyPageResponse = objectMapper.readTree(emptyPageJson);

        // Mock WebClient 链式调用
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(page1Response))  // 第1页
                .thenReturn(Mono.just(emptyPageResponse))  // 第2页
                .thenReturn(Mono.just(emptyPageResponse)); // 第3页

        // 执行测试
        List<JsonNode> result = service.fetchMappingJson(2024, Season.SPRING);

        // 验证结果 - total=120, pageSize=50, 计算: (120+50-1)/50 = 3 页
        assertNotNull(result);
        assertEquals(2, result.size()); // 只有第一页有 2 条数据

        // 每个月有 3 页，季度共请求 9 次。
        verify(mockWebClient, times(9)).get();
    }

    @Test
    public void testFetchMappingJson_SinglePage() throws Exception {
        // 加载单页数据 (total=30, pageSize=50, 应该只有 1 页)
        String jsonContent = Files.readString(
                Paths.get("src/test/resources/fetch/bangumi/calendar_single_page.json")
        );
        JsonNode singlePageResponse = objectMapper.readTree(jsonContent);

        // Mock WebClient
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(singlePageResponse));

        // 执行测试
        List<JsonNode> result = service.fetchMappingJson(2024, Season.SPRING);

        // 验证结果 - total=30 < pageSize(50), 计算: (30+50-1)/50 = 1 页
        assertNotNull(result);
        assertEquals(3, result.size());

        // 季度的三个月各请求一次。
        verify(mockWebClient, times(3)).get();
    }

    @Test
    public void testFetchMappingJson_FetchesEveryMonthInSeason() throws Exception {
        JsonNode emptyResponse = objectMapper.readTree("{\"total\": 0, \"data\": []}");
        BangumiFetchService spyService = spy(service);
        doReturn(emptyResponse).when(spyService).fetchPage(anyInt(), anyInt(), anyInt());

        List<JsonNode> result = spyService.fetchMappingJson(2024, Season.SPRING);

        assertTrue(result.isEmpty());
        verify(spyService).fetchPage(2024, 3, 0);
        verify(spyService).fetchPage(2024, 4, 0);
        verify(spyService).fetchPage(2024, 5, 0);
        verify(spyService, times(3)).fetchPage(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void testFetchMappingJson_WinterCrossesYearAndPaginatesEachMonth() throws Exception {
        BangumiFetchService spyService = spy(service);
        doReturn(objectMapper.readTree("{\"total\": 51, \"data\": [{\"id\": 1}]}"))
                .when(spyService).fetchPage(2023, 12, 0);
        doReturn(objectMapper.readTree("{\"total\": 51, \"data\": [{\"id\": 2}]}"))
                .when(spyService).fetchPage(2023, 12, 1);
        doReturn(objectMapper.readTree("{\"total\": 0, \"data\": []}"))
                .when(spyService).fetchPage(2024, 1, 0);
        doReturn(objectMapper.readTree("{\"total\": 1, \"data\": [{\"id\": 3}]}"))
                .when(spyService).fetchPage(2024, 2, 0);

        List<JsonNode> result = spyService.fetchMappingJson(2024, Season.WINTER);

        assertEquals(List.of(1, 2, 3), result.stream().map(node -> node.path("id").asInt()).toList());
        verify(spyService).fetchPage(2023, 12, 0);
        verify(spyService).fetchPage(2023, 12, 1);
        verify(spyService).fetchPage(2024, 1, 0);
        verify(spyService).fetchPage(2024, 2, 0);
        verify(spyService, times(4)).fetchPage(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void testFetchMappingJson_WholeYearRunsFromPreviousDecemberThroughNovember() throws Exception {
        JsonNode emptyResponse = objectMapper.readTree("{\"total\": 0, \"data\": []}");
        BangumiFetchService spyService = spy(service);
        doReturn(emptyResponse).when(spyService).fetchPage(anyInt(), anyInt(), anyInt());

        assertTrue(spyService.fetchMappingJson(2024, null).isEmpty());

        verify(spyService).fetchPage(2023, 12, 0);
        for (int month = 1; month <= 11; month++) {
            verify(spyService).fetchPage(2024, month, 0);
        }
        verify(spyService, times(12)).fetchPage(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void testFetchMappingJson_ExactMultiplePages() throws Exception {
        // 测试恰好整数页的情况 (total=100, pageSize=50, 应该是 2 页)
        String exactJson = "{\"total\": 100, \"data\": [{\"id\": 1}, {\"id\": 2}]}";
        JsonNode exactResponse = objectMapper.readTree(exactJson);

        // Mock WebClient
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(exactResponse))
                .thenReturn(Mono.just(exactResponse));

        // 执行测试
        List<JsonNode> result = service.fetchMappingJson(2024, Season.SPRING);

        // 验证结果 - 计算: (100+50-1)/50 = 2 页
        assertNotNull(result);
        assertEquals(12, result.size()); // 3个月 * 2页 * 2条数据 = 12条

        // 三个月各请求 2 页。
        verify(mockWebClient, times(6)).get();
    }

    @Test
    public void testExtractStartDate_InvalidDate() throws Exception {
        // 测试无效日期处理
        String jsonString = """
                {
                    "date": "invalid-date"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        // 应该抛出异常或返回 null（取决于实现）
        assertNull(service.extractStartDate(jsonNode));
    }

    @Test
    public void testExtractStartDate_MissingDate() throws Exception {
        // 测试缺少日期字段
        String jsonString = """
                {
                    "id": "12345"
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        // 应该抛出异常（date 字段缺失）
        assertNull(service.extractStartDate(jsonNode));
    }

    @Test
    public void testFetchPage_EmptyResult() throws Exception {
        // 创建空结果响应
        String emptyJson = "{\"total\": 0, \"data\": []}";
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

        // 季度的三个月各请求一次。
        verify(mockWebClient, times(3)).get();
    }

    @Test
    public void testFetchPage_NullResponse() {
        // Mock WebClient 返回 null
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.empty());

        // 执行测试
        JsonNode result = service.fetchPage(2024, 4, 0);

        // 验证结果为 null
        assertNull(result);
    }
}
