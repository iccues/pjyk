package com.yukiani.server.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.yukiani.server.entity.AnimeTitles;
import com.yukiani.server.entity.LocalDateRange;
import com.yukiani.server.entity.Platform;
import com.yukiani.server.entity.Season;
import com.yukiani.server.service.SeasonService;
import com.yukiani.server.util.RetryUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通过 Bangumi API 抓取动画 Mapping 和指标。
 */
@Service
public class BangumiFetchService extends AbstractAnimeFetchService {
    @Override
    protected Platform getPlatform() {
        return Platform.Bangumi;
    }

    @Override
    protected LocalDate extractStartDate(JsonNode jsonNode) {
        String dateStr = jsonNode.path("date").asText(null);

        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    protected AnimeTitles extractTitles(JsonNode jsonNode) {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative(jsonNode.path("name").asText(null));
        titles.setTitleCn(jsonNode.path("name_cn").asText(null));
        return titles;
    }

    @Override
    protected String extractCoverImage(JsonNode jsonNode) {
        return jsonNode.path("images").path("common").asText(null);
    }

    @Override
    protected String extractPlatformId(JsonNode jsonNode) {
        return jsonNode.path("id").asText(null);
    }

    @Override
    protected Double extractRawScore(JsonNode jsonNode) {
        double score = jsonNode.path("rating").path("score").asDouble();
        if (score <= 0.0) {
            return null;
        }
        return score;
    }

    @Override
    protected double extractRawPopularity(JsonNode jsonNode) {
        return jsonNode.path("rating").path("total").asDouble();
    }

    @Override
    protected List<JsonNode> fetchMappingJson(int year, Season season) {
        LocalDateRange range = seasonService.getStartDateRange(year, season);
        List<JsonNode> list = new ArrayList<>();
        // 与榜单查询范围一致，日期递增同时处理冬季和全年抓取的跨年月份。
        for (LocalDate month = range.start(); month.isBefore(range.end()); month = month.plusMonths(1)) {
            list.addAll(fetchMonthMappings(month.getYear(), month.getMonthValue()));
        }
        return list;
    }

    private List<JsonNode> fetchMonthMappings(int year, int month) {
        JsonNode firstPage = fetchPage(year, month, 0);
        if (firstPage == null) {
            return new ArrayList<>();
        }

        List<JsonNode> list = new ArrayList<>();
        firstPage.path("data").forEach(list::add);

        int total = firstPage.path("total").asInt();
        int totalPage = (total + pageSize - 1) / pageSize;

        for (int i = 1; i < totalPage; i++) {
            JsonNode page = fetchPage(year, month, i);
            page.path("data").forEach(list::add);
        }

        return list;
    }

    @Resource
    WebClient bangumiWebClient;

    @Resource
    SeasonService seasonService;

    /** Bangumi 单次请求的最大记录数。 */
    final int pageSize = 50;

    JsonNode fetchPage(int year, int month, int page) {
        Map<String, Object> queryParams = Map.of(
                "year", year, "month", month, "limit", pageSize, "offset", page * pageSize);

        return RetryUtil.executeWithRetry(
                () -> bangumiWebClient.get()
                        .uri(uriBuilder -> {
                            uriBuilder.path("/subjects").queryParam("type", 2);
                            queryParams.forEach(uriBuilder::queryParam);
                            return uriBuilder.build();
                        })
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block(),
                String.format("Bangumi.fetchPage(year=%d, month=%s, page=%d)", year, month, page)
        );
    }

    @Override
    protected JsonNode fetchSingleMappingJson(String platformId) {
        return RetryUtil.executeWithRetry(
                () -> bangumiWebClient.get()
                        .uri("/subjects/{id}", platformId)
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block(),
                String.format("Bangumi.fetchSingle(platformId=%s)", platformId)
        );
    }
}
