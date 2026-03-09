package com.yukiani.service;

import com.yukiani.config.PlatformConfigProperties;
import com.yukiani.entity.Anime;
import com.yukiani.entity.Mapping;
import com.yukiani.entity.Platform;
import com.yukiani.repo.AnimeRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MetricService {

    @Resource
    AnimeRepository animeRepository;

    @Resource
    PlatformConfigProperties platformConfigProperties;

    @Transactional
    public void calculateAllMetric() {
        List<Anime> list = animeRepository.findAll();

        for (Anime anime : list) {
            calculateMetric(anime);
        }
    }

    @Transactional
    public void calculateMetric(Anime anime) {
        calculateAverageScore(anime);
        calculatePopularity(anime);
    }

    @Transactional
    public void calculateAverageScore(Anime anime) {
        double totalScore = 0.0;
        int totalWeight = 0;

        for (Mapping mapping : anime.getMappings()) {
            Double normalizedScore = mapping.getNormalizedScore();
            if (normalizedScore != null) {
                int weight = getWeight(mapping.getSourcePlatform());
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

    private int getWeight(Platform platform) {
        return platformConfigProperties.getConfig(platform).getScoreWeight();
    }


    @Transactional
    public void calculatePopularity(Anime anime) {
        double totalPopularity = 0.0;
        for (Mapping mapping : anime.getMappings()) {
            Double normalizedPopularity = mapping.getNormalizedPopularity();
            if (normalizedPopularity != null && normalizedPopularity > 0) {
                totalPopularity += normalizedPopularity;
            }
        }
        anime.setPopularity(totalPopularity);
    }
}
