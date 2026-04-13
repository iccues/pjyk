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
public class AniListFetchService extends AbstractAnimeFetchService {
    @Override
    protected Platform getPlatform() {
        return Platform.AniList;
    }

    @Override
    protected LocalDate extractStartDate(JsonNode jsonNode) {
        JsonNode date = jsonNode.path("startDate");
        if (date.isMissingNode() || date.isNull()) {
            return null;
        }

        int year = date.path("year").asInt();
        int month = date.path("month").asInt();
        int day = date.path("day").asInt();

        if (year <= 0 || month <= 0 || day <= 0) {
            return null;
        }

        try {
            return LocalDate.of(year, month, day);
        } catch (java.time.DateTimeException e) {
            return null;
        }
    }

    @Override
    protected AnimeTitles extractTitles(JsonNode jsonNode) {
        JsonNode titleJson = jsonNode.path("title");
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative(titleJson.path("native").asText(null));
        titles.setTitleRomaji(titleJson.path("romaji").asText(null));
        titles.setTitleEn(titleJson.path("english").asText(null));
        return titles;
    }

    @Override
    protected String extractCoverImage(JsonNode jsonNode) {
        return jsonNode.path("coverImage").path("extraLarge").asText(null);
    }

    @Override
    protected String extractPlatformId(JsonNode jsonNode) {
        return jsonNode.path("id").asText(null);
    }

    @Override
    protected Double extractRawScore(JsonNode jsonNode) {
        double score = jsonNode.path("averageScore").asDouble();
        if (score <= 0.0) {
            return null;
        }
        return score;
    }

    @Override
    protected double extractRawPopularity(JsonNode jsonNode) {
        return jsonNode.path("popularity").asDouble();
    }

    @Override
    protected List<JsonNode> fetchMappingJson(int year, Season season) {
        List<JsonNode> list = new ArrayList<>();
        for (int i = 2; ; i++) {
            JsonNode page = fetchPage(year, season, i);
            if (page == null) break;

            page.path("media").forEach(list::add);

            if(!page.path("pageInfo").path("hasNextPage").asBoolean()) break;
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

        Map<String, Object> variables = season != null
                ? Map.of("seasonYear", year, "page", page, "season", season.name())
                : Map.of("seasonYear", year, "page", page);

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
