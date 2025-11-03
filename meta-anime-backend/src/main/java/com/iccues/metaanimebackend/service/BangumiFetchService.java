package com.iccues.metaanimebackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeMapping;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BangumiFetchService {

    @Resource
    AnimeMappingService animeMappingService;

    final WebClient client = WebClient.builder()
            .baseUrl("https://api.bgm.tv/v0/subjects?type=2&sort=rank")
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB
                    .build())
            .build();

    @Resource
    ObjectMapper mapper;

    @Resource
    private AnimeService animeService;

    @Resource
    AnimeRepository repo;

    Anime searchOrCreateAnime(JsonNode jsonNode) {
        String date = jsonNode.path("date").asText();
        LocalDate startDate = LocalDate.parse(date);

        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative(jsonNode.path("name").asText());
        titles.setTitleCn(jsonNode.path("name_cn").asText());

        Anime anime = animeService.findAnime(startDate, titles);

        anime.setCoverImage(jsonNode.path("images").path("large").asText());

        repo.save(anime);

        return anime;
    }

    AnimeMapping handleAnimeMapping(JsonNode jsonNode) {
        String platformId = jsonNode.path("id").asText();

        AnimeMapping mapping = new AnimeMapping("Bangumi", platformId, jsonNode);


        // score
        double rawScore = jsonNode.path("rating").path("score").asDouble();
        double normalizedScore = (rawScore - 1) / 9 * 100;
        mapping.setRawScore(rawScore);
        if (normalizedScore > 0) {
            mapping.setNormalizedScore(normalizedScore);
        }

        Anime anime = searchOrCreateAnime(jsonNode);

        mapping.setAnimeId(anime.getAnimeId());

        return mapping;
    }

    public void fetchAnime(int year, int month) {
        List<JsonNode> mediaList = fetchAnimeData(year, month);
        for (JsonNode jsonNode : mediaList) {
            AnimeMapping mapping = handleAnimeMapping(jsonNode);
            animeMappingService.saveOrUpdate(mapping);
        }
    }

    List<JsonNode> fetchAnimeData(int year, int month) {
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

    final int pageSize = 50;

    JsonNode fetchPage(int year, int month, int page) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .queryParam("limit", pageSize)
                        .queryParam("offset", page * pageSize)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}
