package com.yukiani.server.service;

import com.yukiani.server.entity.Anime;
import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.Platform;
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

    @Test
    public void testCalculateAverageScore_WithEqualWeights() {
        Anime anime = new Anime();

        // 三个平台，权重都是 1
        Mapping mapping1 = new Mapping();
        mapping1.setPlatform(Platform.MyAnimeList);
        mapping1.setNormalizedScore(8.0);

        Mapping mapping2 = new Mapping();
        mapping2.setPlatform(Platform.AniList);
        mapping2.setNormalizedScore(9.0);

        Mapping mapping3 = new Mapping();
        mapping3.setPlatform(Platform.MyAnimeList);
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
        bangumiMapping.setPlatform(Platform.Bangumi);
        bangumiMapping.setNormalizedScore(9.0);

        // MyAnimeList 权重 1
        Mapping malMapping = new Mapping();
        malMapping.setPlatform(Platform.MyAnimeList);
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
        bangumi.setPlatform(Platform.Bangumi);
        bangumi.setNormalizedScore(8.0);

        // MyAnimeList 权重 1
        Mapping mal = new Mapping();
        mal.setPlatform(Platform.MyAnimeList);
        mal.setNormalizedScore(7.0);

        // AniList 权重 1
        Mapping anilist = new Mapping();
        anilist.setPlatform(Platform.AniList);
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
        mapping1.setPlatform(Platform.Bangumi);
        mapping1.setNormalizedScore(null);

        Mapping mapping2 = new Mapping();
        mapping2.setPlatform(Platform.MyAnimeList);
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
        mapping1.setPlatform(Platform.Bangumi);
        mapping1.setNormalizedScore(0.0);

        Mapping mapping2 = new Mapping();
        mapping2.setPlatform(Platform.MyAnimeList);
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
        mapping1.setPlatform(Platform.Bangumi);
        mapping1.setNormalizedScore(8.0);

        // null 分数，应该被忽略
        Mapping mapping2 = new Mapping();
        mapping2.setPlatform(Platform.MyAnimeList);
        mapping2.setNormalizedScore(null);

        // 0 分数，参与计算，权重 1
        Mapping mapping3 = new Mapping();
        mapping3.setPlatform(Platform.AniList);
        mapping3.setNormalizedScore(0.0);

        // 有效分数，权重 1
        Mapping mapping4 = new Mapping();
        mapping4.setPlatform(Platform.MyAnimeList);
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
    public void testCalculateAverageScore_WithNegativeScores() {
        Anime anime = new Anime();

        Mapping mapping1 = new Mapping();
        mapping1.setPlatform(Platform.Bangumi);
        mapping1.setNormalizedScore(-1.0);

        Mapping mapping2 = new Mapping();
        mapping2.setPlatform(Platform.MyAnimeList);
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
        mapping.setPlatform(Platform.Bangumi);
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
        bangumi.setPlatform(Platform.Bangumi);
        bangumi.setNormalizedScore(7.5);

        anime.addMapping(bangumi);

        metricService.calculateAverageScore(anime);

        // (7.5*2) / 2 = 7.5
        assertEquals(7.5, anime.getAverageScore(), 0.0001);
    }

    @Test
    public void testCalculatePopularity_WithPlatformWeights() {
        Anime anime = new Anime();

        Mapping bangumi = new Mapping();
        bangumi.setPlatform(Platform.Bangumi);
        bangumi.setNormalizedPopularity(1000.0);

        Mapping anilist = new Mapping();
        anilist.setPlatform(Platform.AniList);
        anilist.setNormalizedPopularity(1000.0);

        Mapping mal = new Mapping();
        mal.setPlatform(Platform.MyAnimeList);
        mal.setNormalizedPopularity(1000.0);

        anime.addMapping(bangumi);
        anime.addMapping(anilist);
        anime.addMapping(mal);

        metricService.calculatePopularity(anime);

        // Bangumi: 1000×2，AniList 和 MyAnimeList: 1000×1
        assertEquals(4000.0, anime.getPopularity(), 0.0001);
    }
}
