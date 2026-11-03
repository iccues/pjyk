package com.yukiani.server.service;

import com.yukiani.server.entity.Anime;
import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.Platform;
import com.yukiani.server.repo.AnimeRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class MetricRecalculationServiceTest {

    @Resource
    MetricRecalculationService metricRecalculationService;

    @Resource
    AnimeRepository animeRepository;

    @Test
    void recalculatesAllAnimeMetrics() {
        Anime anime1 = new Anime();
        Mapping mapping1 = new Mapping();
        mapping1.setPlatform(Platform.Bangumi);
        mapping1.setNormalizedScore(8.0);
        anime1.addMapping(mapping1);

        Anime anime2 = new Anime();
        Mapping mapping2 = new Mapping();
        mapping2.setPlatform(Platform.Bangumi);
        mapping2.setNormalizedScore(9.0);
        Mapping mapping3 = new Mapping();
        mapping3.setPlatform(Platform.MyAnimeList);
        mapping3.setNormalizedScore(6.0);
        anime2.addMapping(mapping2);
        anime2.addMapping(mapping3);

        anime1 = animeRepository.save(anime1);
        anime2 = animeRepository.save(anime2);

        metricRecalculationService.recalculateAllAnimeMetrics();

        Anime reloadedAnime1 = animeRepository.findById(anime1.getAnimeId()).orElseThrow();
        Anime reloadedAnime2 = animeRepository.findById(anime2.getAnimeId()).orElseThrow();

        assertNotNull(reloadedAnime1.getAverageScore());
        assertEquals(8.0, reloadedAnime1.getAverageScore(), 0.0001);
        assertNotNull(reloadedAnime2.getAverageScore());
        assertEquals(8.0, reloadedAnime2.getAverageScore(), 0.0001);
    }
}
