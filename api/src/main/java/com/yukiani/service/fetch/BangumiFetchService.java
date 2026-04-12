package com.yukiani.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.yukiani.entity.AnimeTitles;
import com.yukiani.entity.Platform;
import com.yukiani.entity.Season;
import com.yukiani.util.RetryUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BangumiFetchService extends AbstractAnimeFetchService {
    @Override
    protected Platform getPlatform() {
        return Platform.Bangumi;
    }

    @Override
    protected LocalDate extractStartDate(JsonNode jsonNode) {
        String date = jsonNode.path("date").asText();
        return LocalDate.parse(date);
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
        return jsonNode.path("images").path("common").asText();
    }

    @Override
    protected String extractPlatformId(JsonNode jsonNode) {
        return jsonNode.path("id").asText();
    }

    @Override
    protected Double extractRawScore(JsonNode jsonNode) {
        return jsonNode.path("rating").path("score").asDouble();
    }

    @Override
    protected double extractRawPopularity(JsonNode jsonNode) {
        return jsonNode.path("rating").path("total").asDouble();
    }

    @Override
    protected List<JsonNode> fetchMappingJson(int year, Season season) {
        JsonNode firstPage = fetchPage(year, season, 0);
        if (firstPage == null) {
            return new ArrayList<>();
        }

        List<JsonNode> list = new ArrayList<>();
        firstPage.path("data").forEach(list::add);


        int total = firstPage.path("total").asInt();
        int totalPage = (total + pageSize - 1) / pageSize;

        for (int i = 1; i < totalPage; i++) {
            JsonNode page = fetchPage(year, season, i);
            page.path("data").forEach(list::add);
        }

        return list;
    }

    @Resource
    WebClient bangumiWebClient;

    final int pageSize = 50;

    JsonNode fetchPage(int year, Season season, int page) {
        Map<String, Object> queryParams = season != null
                ? Map.of("year", year, "month", season.toMonth(), "limit", pageSize, "offset", page * pageSize)
                : Map.of("year", year, "limit", pageSize, "offset", page * pageSize);

        return RetryUtil.executeWithRetry(
                () -> bangumiWebClient.get()
                        .uri(uriBuilder -> {
                            queryParams.forEach(uriBuilder::queryParam);
                            return uriBuilder.build();
                        })
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block(),
                String.format("Bangumi.fetchPage(year=%d, season=%s, page=%d)", year, season, page)
        );
    }

    @Override
    protected JsonNode fetchSingleMappingJson(String platformId) {
        WebClient singleClient = WebClient.create("https://api.bgm.tv/v0/subjects");
        return RetryUtil.executeWithRetry(
                () -> singleClient.get()
                        .uri("/{id}", platformId)
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block(),
                String.format("Bangumi.fetchSingle(platformId=%s)", platformId)
        );
    }
}
