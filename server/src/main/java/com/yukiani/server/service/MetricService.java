package com.yukiani.server.service;

import com.yukiani.server.config.PlatformConfigProperties;
import com.yukiani.server.entity.Anime;
import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.Platform;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 根据各平台归一化指标及权重计算动画的综合评分和热度。
 */
@Service
public class MetricService {

    @Resource
    PlatformConfigProperties platformConfigProperties;

    @Transactional
    public void calculateMetric(Anime anime) {
        calculateAverageScore(anime);
        calculatePopularity(anime);
    }

    /**
     * 按平台权重计算归一化评分的加权平均值。
     */
    @Transactional
    public void calculateAverageScore(Anime anime) {
        double totalScore = 0.0;
        double totalWeight = 0;

        for (Mapping mapping : anime.getMappings()) {
            Double normalizedScore = mapping.getNormalizedScore();
            if (normalizedScore != null) {
                double weight = getScoreWeight(mapping.getPlatform());
                totalScore += normalizedScore * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight > 0) {
            anime.setAverageScore(totalScore / totalWeight);
        } else  {
            anime.setAverageScore(null);
        }
    }

    private double getScoreWeight(Platform platform) {
        return platformConfigProperties.getConfig(platform).getScoreWeight();
    }


    /**
     * 按平台权重累加有效的归一化热度。
     */
    @Transactional
    public void calculatePopularity(Anime anime) {
        double totalPopularity = 0.0;
        for (Mapping mapping : anime.getMappings()) {
            Double normalizedPopularity = mapping.getNormalizedPopularity();
            if (normalizedPopularity != null && normalizedPopularity > 0) {
                double weight = getPopularityWeight(mapping.getPlatform());
                totalPopularity += normalizedPopularity * weight;
            }
        }
        anime.setPopularity(totalPopularity);
    }

    private double getPopularityWeight(Platform platform) {
        return platformConfigProperties.getConfig(platform).getPopularityWeight();
    }
}
