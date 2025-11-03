package com.iccues.metaanimebackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeMapping;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AniListFetchService {

    @Resource
    AnimeMappingService animeMappingService;

    final WebClient client = WebClient.create("https://graphql.anilist.co");

    @Resource
    ObjectMapper mapper;

    @Resource
    private AnimeService animeService;

    @Resource
    AnimeRepository repo;

    Anime searchOrCreateAnime(JsonNode jsonNode) {
        JsonNode date = jsonNode.path("startDate");
        int year = date.path("year").asInt();
        int month = date.path("month").asInt();
        int day = date.path("day").asInt();
        LocalDate startDate = LocalDate.of(year, month, day);

        JsonNode titleJson = jsonNode.path("title");
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative(titleJson.path("native").asText());
        titles.setTitleRomaji(titleJson.path("romaji").asText());
        titles.setTitleEn(titleJson.path("english").asText());

        Anime anime = animeService.findAnime(startDate, titles);

        anime.setCoverImage(jsonNode.path("coverImage").path("extraLarge").asText());

        repo.save(anime);

        return anime;
    }

    AnimeMapping handleAnimeMapping(JsonNode jsonNode) {
        String platformId = jsonNode.path("id").asText();

        AnimeMapping mapping = new AnimeMapping("AniList", platformId, jsonNode);


        // score
        double rawScore = jsonNode.path("averageScore").asDouble();
        double normalizedScore = rawScore;
        mapping.setRawScore(rawScore);
        mapping.setNormalizedScore(normalizedScore);

        Anime anime = searchOrCreateAnime(jsonNode);

        mapping.setAnimeId(anime.getAnimeId());

        return mapping;
    }

    public void fetchAnime(int seasonYear, String season) {
        List<JsonNode> mediaList = fetchAnimeData(seasonYear, season);
        for (JsonNode jsonNode : mediaList) {
            AnimeMapping mapping = handleAnimeMapping(jsonNode);
            animeMappingService.saveOrUpdate(mapping);
        }

    }

    List<JsonNode> fetchAnimeData(int seasonYear, String season) {
        JsonNode firstPage = fetchPage(seasonYear, season, 1);
        boolean hasNextPage = firstPage.path("pageInfo").path("hasNextPage").asBoolean();
        List<JsonNode> resultList = new ArrayList<>();
        firstPage.path("media").forEach(resultList::add);

        for (int i = 2; hasNextPage; i++) {
            JsonNode nextPage = fetchPage(seasonYear, season, i);
            hasNextPage = nextPage.path("pageInfo").path("hasNextPage").asBoolean();
            nextPage.path("media").forEach(resultList::add);
        }

        return resultList;
    }

    JsonNode fetchPage(int seasonYear, String season, int page) {
        String query = """
                query ($seasonYear: Int, $season: MediaSeason, $page: Int) {
                  Page(page: $page) {
                    pageInfo {
                      hasNextPage
                    }
                    media(seasonYear: $seasonYear, season: $season, type: ANIME) {
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
                "seasonYear", seasonYear,
                "season", season,
                "page", page
        );

        Map<String, Object> requestBody = Map.of(
                "query", query,
                "variables", variables
        );

        var result = client.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (result == null) {
            return null;
        }

        return result.path("data").path("Page");
    }
}
