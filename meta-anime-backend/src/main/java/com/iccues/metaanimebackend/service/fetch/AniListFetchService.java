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
import java.util.Map;

@Service
public class AniListFetchService extends AbstractAnimeFetchService {
    @Override
    protected Platform getPlatform() {
        return Platform.AniList;
    }

    @Override
    protected LocalDate extractStartDate(JsonNode jsonNode) {
        JsonNode date = jsonNode.path("startDate");
        int year = date.path("year").asInt();
        int month = date.path("month").asInt();
        int day = date.path("day").asInt();
        return LocalDate.of(year, month, day);
    }

    @Override
    protected AnimeTitles extractTitles(JsonNode jsonNode) {
        JsonNode titleJson = jsonNode.path("title");
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative(titleJson.path("native").asText());
        titles.setTitleRomaji(titleJson.path("romaji").asText());
        titles.setTitleEn(titleJson.path("english").asText());
        return titles;
    }

    @Override
    protected String extractCoverImage(JsonNode jsonNode) {
        return jsonNode.path("coverImage").path("extraLarge").asText();
    }

    @Override
    protected String extractPlatformId(JsonNode jsonNode) {
        return jsonNode.path("id").asText();
    }

    @Override
    protected double extractRawScore(JsonNode jsonNode) {
        return jsonNode.path("averageScore").asDouble();
    }

    @Override
    protected double normalizeScore(double rawScore) {
        return rawScore;
    }

    @Override
    protected double extractRawPopularity(JsonNode jsonNode) {
        return jsonNode.path("popularity").asDouble();
    }

    @Override
    protected double normalizePopularity(double rawPopularity) {
        return rawPopularity;
    }

    @Override
    protected List<JsonNode> fetchMappingJson(int year, Season season) {
        JsonNode firstPage = fetchPage(year, season, 1);
        if (firstPage == null) {
            return new ArrayList<>();
        }

        boolean hasNextPage = firstPage.path("pageInfo").path("hasNextPage").asBoolean();
        List<JsonNode> list = new ArrayList<>();
        firstPage.path("media").forEach(list::add);

        for (int i = 2; hasNextPage; i++) {
            JsonNode nextPage = fetchPage(year, season, i);
            hasNextPage = nextPage.path("pageInfo").path("hasNextPage").asBoolean();
            nextPage.path("media").forEach(list::add);
        }

        return list;
    }

    @Resource
    WebClient aniListWebClient;

    JsonNode fetchPage(int year, Season season, int page) {
        String query = """
                query ($seasonYear: Int, $season: MediaSeason, $page: Int) {
                  Page(page: $page) {
                    pageInfo {
                      hasNextPage
                    }
                    media(seasonYear: $seasonYear, season: $season, type: ANIME, isAdult: false) {
                      id
                      title {
                        romaji
                        english
                        native
                      }
                      coverImage {
                        extraLarge
                        large
                        medium
                        color
                      }
                      averageScore
                      popularity
                      startDate {
                        year
                        month
                        day
                      }
                      idMal
                      seasonYear
                      season
                    }
                  }
                }""";

        Map<String, Object> variables = Map.of(
                "seasonYear", year,
                "season", season.name(),
                "page", page
        );

        Map<String, Object> requestBody = Map.of(
                "query", query,
                "variables", variables
        );

        var result = RetryUtil.executeWithRetry(
                () -> aniListWebClient.post()
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block(),
                String.format("AniList.fetchPage(year=%d, season=%s, page=%d)", year, season, page)
        );

        if (result == null) {
            return null;
        }

        return result.path("data").path("Page");
    }

    @Override
    protected JsonNode fetchSingleMappingJson(String platformId) {
        String query = """
                query ($id: Int) {
                  Media(id: $id, type: ANIME) {
                    id
                    title {
                      romaji
                      english
                      native
                    }
                    coverImage {
                      extraLarge
                      large
                      medium
                      color
                    }
                    averageScore
                    popularity
                    startDate {
                      year
                      month
                      day
                    }
                    idMal
                    seasonYear
                    season
                  }
                }""";

        Map<String, Object> variables = Map.of("id", Integer.parseInt(platformId));

        Map<String, Object> requestBody = Map.of(
                "query", query,
                "variables", variables
        );

        var result = RetryUtil.executeWithRetry(
                () -> aniListWebClient.post()
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block(),
                String.format("AniList.fetchSingle(platformId=%s)", platformId)
        );

        if (result == null) {
            return null;
        }

        return result.path("data").path("Media");
    }
}
