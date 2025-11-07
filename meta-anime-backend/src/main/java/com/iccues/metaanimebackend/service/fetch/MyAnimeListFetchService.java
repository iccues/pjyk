package com.iccues.metaanimebackend.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.Season;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MyAnimeListFetchService extends AbstractAnimeFetchService {

    @Override
    protected String getPlatform() {
        return "MyAnimeList";
    }

    @Override
    protected LocalDate extractStartDate(JsonNode jsonNode) {
        try {
            String date = jsonNode.path("start_date").asText();
            return LocalDate.parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected AnimeTitles extractTitles(JsonNode jsonNode) {
        JsonNode titleJson = jsonNode.path("alternative_titles");
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleEn(titleJson.path("en").asText());
        titles.setTitleNative(titleJson.path("ja").asText());
        return titles;
    }

    @Override
    protected String extractCoverImage(JsonNode jsonNode) {
        return jsonNode.path("main_picture").path("large").asText();
    }

    @Override
    protected String extractPlatformId(JsonNode jsonNode) {
        return jsonNode.path("id").asText();
    }

    @Override
    protected double extractRawScore(JsonNode jsonNode) {
        return jsonNode.path("mean").asDouble();
    }

    @Override
    protected double normalizeScore(double rawScore) {
        return (rawScore - 1) / 9 * 100;
    }

    @Override
    protected List<JsonNode> fetchAnimeData(int year, Season season) {
        JsonNode firstPage = fetchPage(year, season, 0);
        if (firstPage == null) {
            return new ArrayList<>();
        }

        boolean hasNextPage = firstPage.path("paging").has("next");
        List<JsonNode> list = new ArrayList<>();
        firstPage.path("data")
                .forEach(jsonNode -> list.add(jsonNode.path("node")));

        for (int i = 1; hasNextPage; i++) {
            JsonNode nextPage = fetchPage(year, season, i);
            hasNextPage = nextPage.path("paging").has("next");
            nextPage.path("data")
                    .forEach(jsonNode -> list.add(jsonNode.path("node")));
        }

        return list;
    }

    final WebClient client;

    public MyAnimeListFetchService(@Value("${mal.client-id}") String clientId) {
        this.client = WebClient.builder()
                .baseUrl("https://api.myanimelist.net/v2")
                .defaultHeader("X-MAL-CLIENT-ID", clientId)
                .build();
    }

    final int pageSize = 100;

    JsonNode fetchPage(int year, Season season, int page) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/anime/season/{year}/{season}")
                        .queryParam("fields", "id,alternative_titles,main_picture,start_date,mean")
                        .queryParam("limit", pageSize)
                        .queryParam("offset", page * pageSize)
                        .build(year, season.toLowerName()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}
