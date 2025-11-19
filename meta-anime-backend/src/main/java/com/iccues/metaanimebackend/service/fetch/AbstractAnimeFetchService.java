package com.iccues.metaanimebackend.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.iccues.metaanimebackend.dto.admin.MappingInfo;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import com.iccues.metaanimebackend.service.MappingService;
import com.iccues.metaanimebackend.service.AnimeService;
import jakarta.annotation.Resource;

import java.time.LocalDate;
import java.util.List;

public abstract class AbstractAnimeFetchService {
    @Resource
    protected MappingService mappingService;

    @Resource
    protected AnimeService animeService;

    @Resource
    protected AnimeRepository animeRepository;
    @Resource
    protected MappingRepository mappingRepository;

    protected abstract String getPlatform();

    protected abstract LocalDate extractStartDate(JsonNode jsonNode);

    protected abstract AnimeTitles extractTitles(JsonNode jsonNode);

    protected abstract String extractCoverImage(JsonNode jsonNode);

    protected abstract String extractPlatformId(JsonNode jsonNode);

    protected abstract double extractRawScore(JsonNode jsonNode);

    protected abstract double normalizeScore(double rawScore);

    public MappingInfo getMappingInfo(Mapping mapping) {
        JsonNode jsonNode = mapping.getRawJSON();
        return new MappingInfo(
                extractTitles(jsonNode),
                extractCoverImage(jsonNode),
                extractStartDate(jsonNode)
        );
    }

    Anime findOrCreateAnime(JsonNode jsonNode) {
        LocalDate startDate = extractStartDate(jsonNode);
        AnimeTitles titles = extractTitles(jsonNode);

        Anime anime = animeService.findAnime(startDate, titles);

        if (anime.getCoverImage() == null) {
            anime.setCoverImage(extractCoverImage(jsonNode));
        }

        return animeRepository.save(anime);
    }

    public void linkMappingToAnime(Mapping mapping) {
        if (mapping.getAnime() == null) {
            Anime anime = findOrCreateAnime(mapping.getRawJSON());
            anime.addMapping(mapping);
            animeRepository.save(anime);
        }
    }

    public void linkAllOrphanedMappings() {
        List<Mapping> mappings = mappingRepository.findAllBySourcePlatformAndAnimeIsNull(getPlatform());
        for (Mapping mapping : mappings) {
            linkMappingToAnime(mapping);
        }
    }

    protected abstract List<JsonNode> fetchMappingJson(int year, Season season);

    protected abstract JsonNode fetchSingleMappingJson(String platformId);

    void processAndSaveMapping(JsonNode jsonNode) {
        String platformId = extractPlatformId(jsonNode);

        Mapping mapping = new Mapping(getPlatform(), platformId, jsonNode);

        double rawScore = extractRawScore(jsonNode);
        if (rawScore > 0) {
            mapping.setRawScore(rawScore);
            double normalizedScore = normalizeScore(rawScore);
            if (normalizedScore > 0) {
                mapping.setNormalizedScore(normalizedScore);
            }
        }

        mappingService.saveOrUpdate(mapping);
    }

    public void fetchAndSaveMappings(int year, Season season) {
        List<JsonNode> mediaList = fetchMappingJson(year, season);
        for (JsonNode jsonNode : mediaList) {
            processAndSaveMapping(jsonNode);
        }
    }

    public Mapping fetchAndCreateMapping(String platformId) {
        JsonNode jsonNode = fetchSingleMappingJson(platformId);
        if (jsonNode == null) {
            throw new RuntimeException("无法从平台获取数据: " + platformId);
        }
        processAndSaveMapping(jsonNode);
        return mappingRepository.findBySourcePlatformAndPlatformId(getPlatform(), platformId);
    }
}
