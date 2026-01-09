package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MetricServiceTest {

    @Resource
    MetricService metricService;

    @Resource
    AnimeRepository animeRepository;

    @Test
    public void testCalculateAverageScore_WithEqualWeights() {
        Anime anime = new Anime();

        // 三个平台，权重都是 1
        Mapping mapping1 = new Mapping();
        mapping1.setSourcePlatform(Platform.MyAnimeList);
        mapping1.setNormalizedScore(8.0);

        Mapping mapping2 = new Mapping();
        mapping2.setSourcePlatform(Platform.AniList);
        mapping2.setNormalizedScore(9.0);

        Mapping mapping3 = new Mapping();
        mapping3.setSourcePlatform(Platform.MyAnimeList);
        mapping3.setNormalizedScore(7.0);

        anime.addMapping(mapping1);
        anime.addMapping(mapping2);
        anime.addMapping(mapping3);

        metricService.calculateAverageScore(anime);

        // (8.0*1 + 9.0*1 + 7.0*1) / (1+1+1) = 24/3 = 8.0
        assertEquals(8.0, anime.getAverageScore(), 0.0001);
    }

    @Test
    public void testCalculateAverageScore_WithDifferentWeights() {
        Anime anime = new Anime();

        // Bangumi 权重 2
        Mapping bangumiMapping = new Mapping();
        bangumiMapping.setSourcePlatform(Platform.Bangumi);
        bangumiMapping.setNormalizedScore(9.0);

        // MyAnimeList 权重 1
        Mapping malMapping = new Mapping();
        malMapping.setSourcePlatform(Platform.MyAnimeList);
        malMapping.setNormalizedScore(6.0);

        anime.addMapping(bangumiMapping);
        anime.addMapping(malMapping);

        metricService.calculateAverageScore(anime);

        // (9.0*2 + 6.0*1) / (2+1) = 24/3 = 8.0
        assertEquals(8.0, anime.getAverageScore(), 0.0001);
    }

    @Test
    public void testCalculateAverageScore_WithAllPlatforms() {
        Anime anime = new Anime();

        // Bangumi 权重 2
        Mapping bangumi = new Mapping();
        bangumi.setSourcePlatform(Platform.Bangumi);
        bangumi.setNormalizedScore(8.0);

        // MyAnimeList 权重 1
        Mapping mal = new Mapping();
        mal.setSourcePlatform(Platform.MyAnimeList);
        mal.setNormalizedScore(7.0);

        // AniList 权重 1
        Mapping anilist = new Mapping();
        anilist.setSourcePlatform(Platform.AniList);
        anilist.setNormalizedScore(9.0);

        anime.addMapping(bangumi);
        anime.addMapping(mal);
        anime.addMapping(anilist);

        metricService.calculateAverageScore(anime);

        // (8.0*2 + 7.0*1 + 9.0*1) / (2+1+1) = 32/4 = 8.0
        assertEquals(8.0, anime.getAverageScore(), 0.0001);
    }

    @Test
    public void testCalculateAverageScore_WithNullScores() {
        Anime anime = new Anime();

        Mapping mapping1 = new Mapping();
        mapping1.setSourcePlatform(Platform.Bangumi);
        mapping1.setNormalizedScore(null);

        Mapping mapping2 = new Mapping();
        mapping2.setSourcePlatform(Platform.MyAnimeList);
        mapping2.setNormalizedScore(null);

        anime.addMapping(mapping1);
        anime.addMapping(mapping2);

        metricService.calculateAverageScore(anime);

        assertNull(anime.getAverageScore());
    }

    @Test
    public void testCalculateAverageScore_WithZeroScores() {
        Anime anime = new Anime();

        Mapping mapping1 = new Mapping();
        mapping1.setSourcePlatform(Platform.Bangumi);
        mapping1.setNormalizedScore(0.0);

        Mapping mapping2 = new Mapping();
        mapping2.setSourcePlatform(Platform.MyAnimeList);
        mapping2.setNormalizedScore(0.0);

        anime.addMapping(mapping1);
        anime.addMapping(mapping2);

        metricService.calculateAverageScore(anime);

        // 0 分参与计算: (0.0*2 + 0.0*1) / (2+1) = 0.0
        assertEquals(0.0, anime.getAverageScore(), 0.0001);
    }

    @Test
    public void testCalculateAverageScore_WithMixedScores() {
        Anime anime = new Anime();

        // 有效分数，权重 2
        Mapping mapping1 = new Mapping();
        mapping1.setSourcePlatform(Platform.Bangumi);
        mapping1.setNormalizedScore(8.0);

        // null 分数，应该被忽略
        Mapping mapping2 = new Mapping();
        mapping2.setSourcePlatform(Platform.MyAnimeList);
        mapping2.setNormalizedScore(null);

        // 0 分数，参与计算，权重 1
        Mapping mapping3 = new Mapping();
        mapping3.setSourcePlatform(Platform.AniList);
        mapping3.setNormalizedScore(0.0);

        // 有效分数，权重 1
        Mapping mapping4 = new Mapping();
        mapping4.setSourcePlatform(Platform.MyAnimeList);
        mapping4.setNormalizedScore(9.0);

        anime.addMapping(mapping1);
        anime.addMapping(mapping2);
        anime.addMapping(mapping3);
        anime.addMapping(mapping4);

        metricService.calculateAverageScore(anime);

        // mapping1 (8.0*2) + mapping3 (0.0*1) + mapping4 (9.0*1), null 被忽略
        // (8.0*2 + 0.0*1 + 9.0*1) / (2+1+1) = 25/4 = 6.25
        assertEquals(6.25, anime.getAverageScore(), 0.0001);
    }

    @Test
    public void testCalculateAverageScore_WithNoMappings() {
        Anime anime = new Anime();

        metricService.calculateAverageScore(anime);

        assertNull(anime.getAverageScore());
    }

    @Test
    public void testCalculateAllMetric() {
        // 创建并保存带有 mapping 的测试 anime
        Anime anime1 = new Anime();
        Mapping mapping1 = new Mapping();
        mapping1.setSourcePlatform(Platform.Bangumi);
        mapping1.setNormalizedScore(8.0);
        anime1.addMapping(mapping1);

        Anime anime2 = new Anime();
        Mapping mapping2 = new Mapping();
        mapping2.setSourcePlatform(Platform.Bangumi);
        mapping2.setNormalizedScore(9.0);
        Mapping mapping3 = new Mapping();
        mapping3.setSourcePlatform(Platform.MyAnimeList);
        mapping3.setNormalizedScore(6.0);
        anime2.addMapping(mapping2);
        anime2.addMapping(mapping3);

        anime1 = animeRepository.save(anime1);
        anime2 = animeRepository.save(anime2);

        metricService.calculateAllMetric();

        // 重新加载以获取更新后的值
        Anime reloadedAnime1 = animeRepository.findById(anime1.getAnimeId()).orElseThrow();
        Anime reloadedAnime2 = animeRepository.findById(anime2.getAnimeId()).orElseThrow();

        // 验证计算结果
        assertNotNull(reloadedAnime1.getAverageScore());
        assertEquals(8.0, reloadedAnime1.getAverageScore(), 0.0001);

        assertNotNull(reloadedAnime2.getAverageScore());
        // (9.0*2 + 6.0*1) / (2+1) = 24/3 = 8.0
        assertEquals(8.0, reloadedAnime2.getAverageScore(), 0.0001);
    }

    @Test
    public void testCalculateAverageScore_WithNegativeScores() {
        Anime anime = new Anime();

        Mapping mapping1 = new Mapping();
        mapping1.setSourcePlatform(Platform.Bangumi);
        mapping1.setNormalizedScore(-1.0);

        Mapping mapping2 = new Mapping();
        mapping2.setSourcePlatform(Platform.MyAnimeList);
        mapping2.setNormalizedScore(8.0);

        anime.addMapping(mapping1);
        anime.addMapping(mapping2);

        metricService.calculateAverageScore(anime);

        // 负分参与计算: (-1.0*2 + 8.0*1) / (2+1) = 6/3 = 2.0
        assertEquals(2.0, anime.getAverageScore(), 0.0001);
    }

    @Test
    public void testCalculateAverageScore_WithSingleScore() {
        Anime anime = new Anime();

        Mapping mapping = new Mapping();
        mapping.setSourcePlatform(Platform.Bangumi);
        mapping.setNormalizedScore(7.5);

        anime.addMapping(mapping);

        metricService.calculateAverageScore(anime);

        assertEquals(7.5, anime.getAverageScore(), 0.0001);
    }

    @Test
    public void testCalculateAverageScore_OnlyBangumiHighWeight() {
        Anime anime = new Anime();

        // 只有 Bangumi 的分数，权重为 2
        Mapping bangumi = new Mapping();
        bangumi.setSourcePlatform(Platform.Bangumi);
        bangumi.setNormalizedScore(7.5);

        anime.addMapping(bangumi);

        metricService.calculateAverageScore(anime);

        // (7.5*2) / 2 = 7.5
        assertEquals(7.5, anime.getAverageScore(), 0.0001);
    }
}
