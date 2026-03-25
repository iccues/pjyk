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
    protected Double extractRawScore(JsonNode jsonNode) {
        double score = jsonNode.path("mean").asDouble();
        if (score <= 0.0) {
            return null;
        }
        return score;
    }

    @Override
    protected double extractRawPopularity(JsonNode jsonNode) {
        return jsonNode.path("num_scoring_users").asDouble();
    }

    @Override
    protected List<JsonNode> fetchMappingJson(int year, Season season) {
        if (season == null) {
            List<JsonNode> list = new ArrayList<>();
            for (Season s : Season.values()) {
                list.addAll(fetchSeasonMappings(year, s));
            }
            return list;
        }
        return fetchSeasonMappings(year, season);
    }

    private List<JsonNode> fetchSeasonMappings(int year, Season season) {
        List<JsonNode> list = new ArrayList<>();
        for (int i = 0; ; i++) {
            JsonNode page = fetchPage(year, season, i);
            if (page == null) break;

            extractMediaItems(page, list);

            if (!page.path("paging").has("next")) break;
        }
        return list;
    }

    private void extractMediaItems(JsonNode page, List<JsonNode> list) {
        page.path("data").forEach(jsonNode -> {
            JsonNode node = jsonNode.path("node");
            String mediaType = node.path("media_type").asText();
            if ("music".equals(mediaType) || "pv".equals(mediaType)) {
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
