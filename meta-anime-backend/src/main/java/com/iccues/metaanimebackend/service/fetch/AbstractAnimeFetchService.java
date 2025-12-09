package com.iccues.metaanimebackend.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.iccues.metaanimebackend.entity.MappingInfo;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.exception.FetchFailedException;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import com.iccues.metaanimebackend.service.MappingService;
import com.iccues.metaanimebackend.service.AnimeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.List;

@Slf4j
public abstract class AbstractAnimeFetchService {
    @Resource
    protected MappingService mappingService;

    @Resource
    protected AnimeService animeService;

    @Resource
    protected AnimeRepository animeRepository;
    @Resource
    protected MappingRepository mappingRepository;

    protected abstract Platform getPlatform();

    protected abstract LocalDate extractStartDate(JsonNode jsonNode);

    protected abstract AnimeTitles extractTitles(JsonNode jsonNode);

    protected abstract String extractCoverImage(JsonNode jsonNode);

    protected abstract String extractPlatformId(JsonNode jsonNode);

    protected abstract double extractRawScore(JsonNode jsonNode);

    protected abstract double normalizeScore(double rawScore);

    protected MappingInfo extractMappingInfo(JsonNode jsonNode) {
        return new MappingInfo(
                extractTitles(jsonNode),
                extractCoverImage(jsonNode),
                extractStartDate(jsonNode)
        );
    }

    @Transactional
    Anime findOrCreateAnime(MappingInfo mappingInfo) {
        LocalDate startDate = mappingInfo.getStartDate();
        AnimeTitles titles = mappingInfo.getTitle();

        Anime anime = animeService.findAnime(startDate, titles);

        if (anime.getCoverImage() == null) {
            anime.setCoverImage(mappingInfo.getCoverImage());
        }

        return animeRepository.save(anime);
    }

    @Transactional
    public void linkMappingToAnime(Mapping mapping) {
        if (mapping.getAnime() == null) {
            Anime anime = findOrCreateAnime(mapping.getMappingInfo());
            anime.addMapping(mapping);
            animeRepository.save(anime);
        }
    }

    @Transactional
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
        MappingInfo mappingInfo = extractMappingInfo(jsonNode);

        Mapping mapping = new Mapping(getPlatform(), platformId, mappingInfo);

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

    @Transactional
    public void fetchAndSaveMappings(int year, Season season) {
        try {
            List<JsonNode> mediaList = fetchMappingJson(year, season);
            for (JsonNode jsonNode : mediaList) {
                processAndSaveMapping(jsonNode);
            }
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch mappings from {} for year={}, season={}: {} {}",
                    getPlatform(), year, season, e.getStatusCode(), e.getResponseBodyAsString());
            throw new FetchFailedException(getPlatform(),
                    String.format("year=%d, season=%s", year, season));
        } catch (Exception e) {
            log.error("Unexpected error fetching mappings from {} for year={}, season={}",
                    getPlatform(), year, season, e);
            throw new FetchFailedException(getPlatform(),
                    String.format("year=%d, season=%s", year, season));
        }
    }

    @Transactional
    public Mapping fetchAndSaveMapping(String platformId) {
        try {
            JsonNode jsonNode = fetchSingleMappingJson(platformId);
            if (jsonNode == null) {
                log.warn("{} API returned null for platformId={}", getPlatform(), platformId);
                throw new FetchFailedException(getPlatform(), platformId);
            }
            processAndSaveMapping(jsonNode);
            return mappingRepository.findBySourcePlatformAndPlatformId(getPlatform(), platformId);
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch mapping from {} for platformId={}: {} {}",
                    getPlatform(), platformId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new FetchFailedException(getPlatform(), platformId);
        } catch (FetchFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching mapping from {} for platformId={}",
                    getPlatform(), platformId, e);
            throw new FetchFailedException(getPlatform(), platformId);
        }
    }
}
