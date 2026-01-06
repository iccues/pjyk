package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScoreService {

    @Resource
    AnimeRepository animeRepository;

    @Transactional
    public void calculateAllAverageScore() {
        List<Anime> list = animeRepository.findAll();

        for (Anime anime : list) {
            calculateAverageScore(anime);
            calculatePopularity(anime);
        }
    }

    @Transactional
    public void calculateAverageScore(Anime anime) {
        double totalScore = 0.0;
        int totalWeight = 0;

        for (Mapping mapping : anime.getMappings()) {
            Double normalizedScore = mapping.getNormalizedScore();
            if (normalizedScore != null && normalizedScore > 0) {
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
        return switch (platform) {
            case Bangumi -> 2;
            case AniList -> 1;
            case MyAnimeList -> 1;
        };
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
