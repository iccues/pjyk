package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ScoreServiceTest {

    @Resource
    ScoreService scoreService;

    @Resource
    AnimeRepository animeRepository;

    @Test
    public void testCalculateAverageScore_WithValidScores() {
        Anime anime = new Anime();

        Mapping mapping1 = new Mapping();
        mapping1.setNormalizedScore(8.5);

        Mapping mapping2 = new Mapping();
        mapping2.setNormalizedScore(9.0);

        Mapping mapping3 = new Mapping();
        mapping3.setNormalizedScore(7.5);

        anime.addMapping(mapping1);
        anime.addMapping(mapping2);
        anime.addMapping(mapping3);

        scoreService.calculateAverageScore(anime);

        assertEquals(8.333333333333334, anime.getAverageScore(), 0.0001);
    }

    @Test
    public void testCalculateAverageScore_WithNullScores() {
        Anime anime = new Anime();

        Mapping mapping1 = new Mapping();
        mapping1.setNormalizedScore(null);

        Mapping mapping2 = new Mapping();
        mapping2.setNormalizedScore(null);

        anime.addMapping(mapping1);
        anime.addMapping(mapping2);

        scoreService.calculateAverageScore(anime);

        assertNull(anime.getAverageScore());
    }

    @Test
    public void testCalculateAverageScore_WithZeroScores() {
        Anime anime = new Anime();

        Mapping mapping1 = new Mapping();
        mapping1.setNormalizedScore(0.0);

        Mapping mapping2 = new Mapping();
        mapping2.setNormalizedScore(0.0);

        anime.addMapping(mapping1);
        anime.addMapping(mapping2);

        scoreService.calculateAverageScore(anime);

        assertNull(anime.getAverageScore());
    }

    @Test
    public void testCalculateAverageScore_WithMixedScores() {
        Anime anime = new Anime();

        Mapping mapping1 = new Mapping();
        mapping1.setNormalizedScore(8.0);

        Mapping mapping2 = new Mapping();
        mapping2.setNormalizedScore(null);

        Mapping mapping3 = new Mapping();
        mapping3.setNormalizedScore(0.0);

        Mapping mapping4 = new Mapping();
        mapping4.setNormalizedScore(9.0);

        anime.addMapping(mapping1);
        anime.addMapping(mapping2);
        anime.addMapping(mapping3);
        anime.addMapping(mapping4);

        scoreService.calculateAverageScore(anime);

        // 只有 mapping1 (8.0) 和 mapping4 (9.0) 应该被计算
        assertEquals(8.5, anime.getAverageScore(), 0.0001);
    }

    @Test
    public void testCalculateAverageScore_WithNoMappings() {
        Anime anime = new Anime();

        scoreService.calculateAverageScore(anime);

        assertNull(anime.getAverageScore());
    }

    @Test
    public void testCalculateAllAverageScore() {
        // 创建并保存带有 mapping 的测试 anime
        Anime anime1 = new Anime();
        Mapping mapping1 = new Mapping();
        mapping1.setNormalizedScore(8.0);
        anime1.addMapping(mapping1);

        Anime anime2 = new Anime();
        Mapping mapping2 = new Mapping();
        mapping2.setNormalizedScore(9.0);
        Mapping mapping3 = new Mapping();
        mapping3.setNormalizedScore(7.0);
        anime2.addMapping(mapping2);
        anime2.addMapping(mapping3);

        anime1 = animeRepository.save(anime1);
        anime2 = animeRepository.save(anime2);

        scoreService.calculateAllAverageScore();

        // 重新加载以获取更新后的值
        Anime reloadedAnime1 = animeRepository.findById(anime1.getAnimeId()).orElseThrow();
        Anime reloadedAnime2 = animeRepository.findById(anime2.getAnimeId()).orElseThrow();

        // 验证计算结果
        assertNotNull(reloadedAnime1.getAverageScore());
        assertEquals(8.0, reloadedAnime1.getAverageScore(), 0.0001);

        assertNotNull(reloadedAnime2.getAverageScore());
        assertEquals(8.0, reloadedAnime2.getAverageScore(), 0.0001); // (9.0 + 7.0) / 2 = 8.0
    }

    @Test
    public void testCalculateAverageScore_WithNegativeScores() {
        Anime anime = new Anime();

        Mapping mapping1 = new Mapping();
        mapping1.setNormalizedScore(-1.0);

        Mapping mapping2 = new Mapping();
        mapping2.setNormalizedScore(8.0);

        anime.addMapping(mapping1);
        anime.addMapping(mapping2);

        scoreService.calculateAverageScore(anime);

        // 负分数应该被忽略
        assertEquals(8.0, anime.getAverageScore(), 0.0001);
    }

    @Test
    public void testCalculateAverageScore_WithSingleScore() {
        Anime anime = new Anime();

        Mapping mapping = new Mapping();
        mapping.setNormalizedScore(7.5);

        anime.addMapping(mapping);

        scoreService.calculateAverageScore(anime);

        assertEquals(7.5, anime.getAverageScore(), 0.0001);
    }
}
