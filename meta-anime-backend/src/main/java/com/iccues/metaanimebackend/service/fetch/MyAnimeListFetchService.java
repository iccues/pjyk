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
public class MyAnimeListFetchService extends AbstractAnimeFetchService {

    @Override
    protected Platform getPlatform() {
        return Platform.MyAnimeList;
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
    protected double extractRawPopularity(JsonNode jsonNode) {
        return jsonNode.path("num_scoring_users").asDouble();
    }

    @Override
    protected List<JsonNode> fetchMappingJson(int year, Season season) {
        JsonNode firstPage = fetchPage(year, season, 0);
        if (firstPage == null) {
            return new ArrayList<>();
        }

        boolean hasNextPage = firstPage.path("paging").has("next");
        List<JsonNode> list = new ArrayList<>();
        addAnimeNodesToList(firstPage, list);

        for (int i = 1; hasNextPage; i++) {
            JsonNode nextPage = fetchPage(year, season, i);
            hasNextPage = nextPage.path("paging").has("next");
            addAnimeNodesToList(nextPage, list);
        }

        return list;
    }

    private void addAnimeNodesToList(JsonNode firstPage, List<JsonNode> list) {
        firstPage.path("data")
                .forEach(jsonNode -> {
                    JsonNode node = jsonNode.path("node");
                    String mediaType = node.path("media_type").asText();
                    if (mediaType.equals("music") || mediaType.equals("pv")) {
                        return;
                    }
                    list.add(node);
                });
    }

    @Resource
    WebClient myAnimeListWebClient;

    final int pageSize = 100;

    JsonNode fetchPage(int year, Season season, int page) {
        return RetryUtil.executeWithRetry(
                () -> myAnimeListWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/anime/season/{year}/{season}")
                                .queryParam("fields", "id,alternative_titles,main_picture,start_date,mean,num_scoring_users,media_type")
                                .queryParam("limit", pageSize)
                                .queryParam("offset", page * pageSize)
                                .build(year, season.toLowerName()))
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block(),
                String.format("MyAnimeList.fetchPage(year=%d, season=%s, page=%d)", year, season, page)
        );
    }

    @Override
    protected JsonNode fetchSingleMappingJson(String platformId) {
        return RetryUtil.executeWithRetry(
                () -> myAnimeListWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/anime/{anime_id}")
                                .queryParam("fields", "id,alternative_titles,main_picture,start_date,mean,num_scoring_users,media_type")
                                .build(platformId))
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block(),
                String.format("MyAnimeList.fetchSingle(platformId=%s)", platformId)
        );
    }
}
