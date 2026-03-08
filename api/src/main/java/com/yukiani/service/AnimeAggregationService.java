package com.yukiani.service;

import com.yukiani.entity.Anime;
import com.yukiani.entity.Mapping;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnimeAggregationService {
    @Resource
    MetricService metricService;
    @Resource
    InfoService infoService;

    public void addMappingWithMetrics(Anime anime, Mapping mapping) {
        Anime oldAnime = mapping.getAnime();
        anime.addMapping(mapping);
        metricService.calculateMetric(anime);
        infoService.aggregateInfo(anime);
        if (oldAnime != null && !oldAnime.equals(anime)) {
            metricService.calculateMetric(oldAnime);
            infoService.aggregateInfo(oldAnime);
        }
    }

    public void removeMappingWithMetrics(Anime anime, Mapping mapping) {
        anime.removeMapping(mapping);
        metricService.calculateMetric(anime);
        infoService.aggregateInfo(anime);
    }

    public void addMappingIfAbsent(Anime anime, Mapping mapping) {
        if (anime.getMappingByPlatform(mapping.getSourcePlatform()) == null) {
            addMappingWithMetrics(anime, mapping);
        }
    }

    public void upsertMapping(Anime anime, Mapping mapping) {
        Mapping existing = anime.getMappingByPlatform(mapping.getSourcePlatform());
        if (existing != null) {
            removeMappingWithMetrics(anime, existing);
        }
        addMappingWithMetrics(anime, mapping);
    }

    public Anime mergeAnime(Anime target, Anime source) {
        List<Mapping> sourceMappings = new ArrayList<>(source.getMappings());
        for (Mapping mapping : sourceMappings) {
            addMappingIfAbsent(target, mapping);
        }
        return target;
    }
}
