package com.yukiani.server.service;

import com.yukiani.server.entity.Anime;
import com.yukiani.server.entity.Mapping;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 维护 Anime 与 Mapping 的关联，并在关联变化后刷新聚合信息和指标。
 */
@Service
public class AnimeAggregationService {
    @Resource
    MetricService metricService;
    @Resource
    InfoService infoService;

    /**
     * 将 Mapping 转移到目标动画，并刷新新旧动画的聚合数据。
     */
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

    /**
     * 解除 Mapping 关联并刷新动画的聚合数据。
     */
    public void removeMappingWithMetrics(Anime anime, Mapping mapping) {
        anime.removeMapping(mapping);
        metricService.calculateMetric(anime);
        infoService.aggregateInfo(anime);
    }

    /**
     * 当目标动画尚无同平台 Mapping 时添加 Mapping。
     */
    public void addMappingIfAbsent(Anime anime, Mapping mapping) {
        if (anime.getMappingByPlatform(mapping.getPlatformKey().getPlatform()) == null) {
            addMappingWithMetrics(anime, mapping);
        }
    }

    /**
     * 使用新 Mapping 替换目标动画已有的同平台 Mapping。
     */
    public void upsertMapping(Anime anime, Mapping mapping) {
        Mapping existing = anime.getMappingByPlatform(mapping.getPlatformKey().getPlatform());
        if (existing != null) {
            removeMappingWithMetrics(anime, existing);
        }
        addMappingWithMetrics(anime, mapping);
    }

    /**
     * 将来源动画中尚未存在于目标动画的平台 Mapping 合并到目标动画。
     */
    public Anime mergeAnime(Anime target, Anime source) {
        List<Mapping> sourceMappings = new ArrayList<>(source.getMappings());
        for (Mapping mapping : sourceMappings) {
            addMappingIfAbsent(target, mapping);
        }
        return target;
    }
}
