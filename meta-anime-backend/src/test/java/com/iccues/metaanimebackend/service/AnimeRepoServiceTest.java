package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.MappingInfo;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AnimeRepoServiceTest {

    @Resource
    private AnimeRepository animeRepository;

    @Resource
    private AnimeRepoService animeRepoService;

    @AfterEach
    public void cleanup() {
        animeRepository.deleteAll();
    }

    @Test
    public void testFindAnimeAroundDate_WithValidDate() {
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("進撃の巨人");
        titles.setTitleRomaji("Shingeki no Kyojin");

        Anime anime = new Anime();
        anime.setStartDate(testDate);
        anime.setTitle(titles);
        animeRepository.save(anime);

        List<Anime> result = animeRepoService.findAnimeAroundDate(testDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("進撃の巨人", result.getFirst().getTitle().getTitleNative());
    }

    @Test
    public void testFindAnimeAroundDate_WithNullDate() {
        List<Anime> result = animeRepoService.findAnimeAroundDate(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAnimeAroundDate_WithNoResults() {
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        List<Anime> result = animeRepoService.findAnimeAroundDate(testDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAnime_MatchingExistingAnime() {
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        AnimeTitles existingTitles = new AnimeTitles();
        existingTitles.setTitleNative("進撃の巨人");
        existingTitles.setTitleRomaji("Shingeki no Kyojin");
        existingTitles.setTitleEn("Attack on Titan");

        Anime existingAnime = new Anime();
        existingAnime.setStartDate(testDate);
        existingAnime.setTitle(existingTitles);
        existingAnime = animeRepository.save(existingAnime);

        AnimeTitles newTitles = new AnimeTitles();
        newTitles.setTitleNative("進撃の巨人");
        newTitles.setTitleCn("进击的巨人");

        Anime result = animeRepoService.findAnime(testDate, newTitles);

        assertNotNull(result);
        assertEquals(existingAnime.getAnimeId(), result.getAnimeId());
        assertEquals("进击的巨人", result.getTitle().getTitleCn());
        assertEquals("Shingeki no Kyojin", result.getTitle().getTitleRomaji());
        assertEquals("Attack on Titan", result.getTitle().getTitleEn());
    }

    @Test
    public void testCreateAnime_WithAllFields() {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("進撃の巨人");
        titles.setTitleRomaji("Shingeki no Kyojin");
        titles.setTitleEn("Attack on Titan");

        MappingInfo mappingInfo = new MappingInfo(
                titles,
                "https://example.com/cover.jpg",
                LocalDate.of(2024, 1, 15)
        );

        Anime result = animeRepoService.createAnime(mappingInfo);

        assertNotNull(result);
        assertNull(result.getAnimeId()); // 未保存到数据库
        assertEquals(LocalDate.of(2024, 1, 15), result.getStartDate());
        assertEquals("進撃の巨人", result.getTitle().getTitleNative());
        assertEquals("Shingeki no Kyojin", result.getTitle().getTitleRomaji());
        assertEquals("Attack on Titan", result.getTitle().getTitleEn());
        assertEquals("https://example.com/cover.jpg", result.getCoverImage());
    }

    @Test
    public void testCreateAnime_WithMinimalFields() {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("One Piece");

        MappingInfo mappingInfo = new MappingInfo(titles, null, null);

        Anime result = animeRepoService.createAnime(mappingInfo);

        assertNotNull(result);
        assertNull(result.getAnimeId());
        assertNull(result.getStartDate());
        assertNull(result.getCoverImage());
        assertEquals("One Piece", result.getTitle().getTitleNative());
    }

    @Test
    public void testFindAnime_MultipleAnimesInDateRange_MatchesCorrectOne() {
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        AnimeTitles titles1 = new AnimeTitles();
        titles1.setTitleNative("進撃の巨人");
        Anime anime1 = new Anime();
        anime1.setStartDate(testDate);
        anime1.setTitle(titles1);
        animeRepository.save(anime1);

        AnimeTitles titles2 = new AnimeTitles();
        titles2.setTitleNative("鬼滅の刃");
        Anime anime2 = new Anime();
        anime2.setStartDate(testDate);
        anime2.setTitle(titles2);
        anime2 = animeRepository.save(anime2);

        AnimeTitles searchTitles = new AnimeTitles();
        searchTitles.setTitleNative("鬼滅の刃");

        Anime result = animeRepoService.findAnime(testDate, searchTitles);

        assertNotNull(result);
        assertEquals(anime2.getAnimeId(), result.getAnimeId());
    }

    @Test
    public void testFindAnime_TitleMergePreservesExistingData() {
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        AnimeTitles existingTitles = new AnimeTitles();
        existingTitles.setTitleNative("進撃の巨人");
        existingTitles.setTitleRomaji("Shingeki no Kyojin");
        existingTitles.setTitleEn("Attack on Titan");

        Anime existingAnime = new Anime();
        existingAnime.setStartDate(testDate);
        existingAnime.setTitle(existingTitles);
        animeRepository.save(existingAnime);

        AnimeTitles newTitles = new AnimeTitles();
        newTitles.setTitleNative("進撃の巨人");
        newTitles.setTitleCn("进击的巨人");

        Anime result = animeRepoService.findAnime(testDate, newTitles);

        assertNotNull(result);
        assertEquals("進撃の巨人", result.getTitle().getTitleNative());
        assertEquals("Shingeki no Kyojin", result.getTitle().getTitleRomaji());
        assertEquals("Attack on Titan", result.getTitle().getTitleEn());
        assertEquals("进击的巨人", result.getTitle().getTitleCn());
    }

    @Test
    public void testFindAnimeAroundDate_DateRangeCoverage() {
        LocalDate centerDate = LocalDate.of(2024, 1, 15);

        AnimeTitles titles1 = new AnimeTitles();
        titles1.setTitleNative("前一天");
        Anime anime1 = new Anime();
        anime1.setStartDate(centerDate.minusDays(1));
        anime1.setTitle(titles1);
        animeRepository.save(anime1);

        AnimeTitles titles2 = new AnimeTitles();
        titles2.setTitleNative("同一天");
        Anime anime2 = new Anime();
        anime2.setStartDate(centerDate);
        anime2.setTitle(titles2);
        animeRepository.save(anime2);

        AnimeTitles titles3 = new AnimeTitles();
        titles3.setTitleNative("后一天");
        Anime anime3 = new Anime();
        anime3.setStartDate(centerDate.plusDays(1));
        anime3.setTitle(titles3);
        animeRepository.save(anime3);

        AnimeTitles titles4 = new AnimeTitles();
        titles4.setTitleNative("两天前");
        Anime anime4 = new Anime();
        anime4.setStartDate(centerDate.minusDays(2));
        anime4.setTitle(titles4);
        animeRepository.save(anime4);

        List<Anime> result = animeRepoService.findAnimeAroundDate(centerDate);

        assertEquals(3, result.size());
    }

    @Test
    public void testFindAnime_SimilarTitles_Matches() {
        LocalDate testDate = LocalDate.of(2024, 1, 15);

        AnimeTitles existingTitles = new AnimeTitles();
        existingTitles.setTitleNative("Attack on Titan Season 1");

        Anime existingAnime = new Anime();
        existingAnime.setStartDate(testDate);
        existingAnime.setTitle(existingTitles);
        existingAnime = animeRepository.save(existingAnime);

        AnimeTitles newTitles = new AnimeTitles();
        newTitles.setTitleNative("Attack on Titan Season 2");

        Anime result = animeRepoService.findAnime(testDate, newTitles);

        assertNotNull(result);
        assertEquals(existingAnime.getAnimeId(), result.getAnimeId());
    }
}
