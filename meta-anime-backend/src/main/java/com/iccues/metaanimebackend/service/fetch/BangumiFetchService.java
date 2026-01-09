package com.iccues.metaanimebackend.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.util.RetryUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        titles.setTitleNative(jsonNode.path("name").asText());
        titles.setTitleCn(jsonNode.path("name_cn").asText());
        return titles;
    }

    @Override
    protected String extractCoverImage(JsonNode jsonNode) {
        return jsonNode.path("images").path("large").asText();
    }

    @Override
    protected String extractPlatformId(JsonNode jsonNode) {
        return jsonNode.path("id").asText();
    }

    @Override
    protected double extractRawScore(JsonNode jsonNode) {
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
        return RetryUtil.executeWithRetry(
                () -> bangumiWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("year", year)
                                .queryParam("month", season.toMonth())
                                .queryParam("limit", pageSize)
                                .queryParam("offset", page * pageSize)
                                .build())
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
