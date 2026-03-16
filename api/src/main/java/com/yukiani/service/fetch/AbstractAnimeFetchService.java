package com.yukiani.service.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.yukiani.config.PlatformConfig;
import com.yukiani.config.PlatformConfigProperties;
import com.yukiani.entity.*;
import com.yukiani.exception.FetchFailedException;
import com.yukiani.repo.MappingRepository;
import com.yukiani.service.MappingRepoService;
import com.yukiani.service.MetricService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.List;

@Slf4j
public abstract class AbstractAnimeFetchService {
    @Resource
    protected MappingRepoService mappingRepoService;

    @Resource
    protected MappingRepository mappingRepository;
    @Resource
    protected PlatformConfigProperties platformConfigProperties;

    protected abstract Platform getPlatform();

    protected abstract LocalDate extractStartDate(JsonNode jsonNode);

    protected abstract AnimeTitles extractTitles(JsonNode jsonNode);

    protected abstract String extractCoverImage(JsonNode jsonNode);

    protected abstract String extractPlatformId(JsonNode jsonNode);

    protected abstract Double extractRawScore(JsonNode jsonNode);

    public Double normalizeScore(Double rawScore) {
        if (rawScore == null || rawScore < 0) {
            return null;
        }
        PlatformConfig config = platformConfigProperties.getConfig(getPlatform());
        double mean = config.getScoreMean();
        double std = config.getScoreStd();
        double z = (rawScore - mean) / std;
        return 50 + (z * (100 / 6.0));
    }

    protected abstract double extractRawPopularity(JsonNode jsonNode);

    public double normalizePopularity(double rawPopularity) {
        PlatformConfig config = platformConfigProperties.getConfig(getPlatform());
        double multiplier = config.getPopularityMultiplier();
        return rawPopularity * multiplier;
    }

    protected MappingInfo extractMappingInfo(JsonNode jsonNode) {
        return new MappingInfo(
                extractTitles(jsonNode),
                extractCoverImage(jsonNode),
                extractStartDate(jsonNode)
        );
    }

    protected abstract List<JsonNode> fetchMappingJson(int year, Season season);

    protected abstract JsonNode fetchSingleMappingJson(String platformId);

    void processAndSaveMapping(JsonNode jsonNode) {
        String platformId = extractPlatformId(jsonNode);
        MappingInfo mappingInfo = extractMappingInfo(jsonNode);

        Mapping mapping = new Mapping(getPlatform(), platformId, mappingInfo);

        Double rawScore = extractRawScore(jsonNode);
        Double normalizedScore = normalizeScore(rawScore);
        mapping.setRawScore(rawScore);
        mapping.setNormalizedScore(normalizedScore);

        double rawPopularity = extractRawPopularity(jsonNode);
        double normalizedPopularity = normalizePopularity(rawPopularity);
        mapping.setRawPopularity(rawPopularity);
        mapping.setNormalizedPopularity(normalizedPopularity);

        mappingRepoService.saveOrUpdate(mapping);
    }

    @Transactional
    public void fetchAndSaveMappings(int year, Season season) {
        try {
            List<JsonNode> mediaList = fetchMappingJson(year, season);
            for (JsonNode jsonNode : mediaList) {
                processAndSaveMapping(jsonNode);
            }
        } catch (WebClientResponseException e) {
            log.warn("Failed to fetch mappings from {} for year={}, season={}: {} {}",
                    getPlatform(), year, season, e.getStatusCode(), e.getResponseBodyAsString());
            throw new FetchFailedException(getPlatform(),
                    String.format("year=%d, season=%s", year, season));
        } catch (Exception e) {
            log.error("Unexpected error fetching mappings from {} for year={}, season={}: {}",
                    getPlatform(), year, season, e.getMessage());
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
            log.warn("Failed to fetch mapping from {} for platformId={}: {} {}",
                    getPlatform(), platformId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new FetchFailedException(getPlatform(), platformId);
        } catch (FetchFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching mapping from {} for platformId={}: {}",
                    getPlatform(), platformId, e.getMessage());
            throw new FetchFailedException(getPlatform(), platformId);
        }
    }
}
