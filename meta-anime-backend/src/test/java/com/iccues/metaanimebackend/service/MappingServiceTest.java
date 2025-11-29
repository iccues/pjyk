package com.iccues.metaanimebackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.AnimeTitles;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.repo.MappingRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MappingServiceTest {

    @Resource
    private MappingRepository mappingRepository;

    @Resource
    private AnimeRepository animeRepository;

    @Resource
    private MappingService mappingService;

    private ObjectMapper objectMapper;
    private JsonNode testJsonNode;

    @BeforeEach
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        testJsonNode = objectMapper.readTree("{\"title\": \"Test Anime\", \"score\": 8.5}");
    }

    @AfterEach
    public void cleanup() {
        mappingRepository.deleteAll();
        animeRepository.deleteAll();
    }

    @Test
    public void testSaveOrUpdate_NewMapping_SavesDirectly() {
        Mapping newMapping = new Mapping();
        newMapping.setSourcePlatform(Platform.MyAnimeList);
        newMapping.setPlatformId("12345");
        newMapping.setRawScore(8.5);
        newMapping.setNormalizedScore(85.0);
        newMapping.setRawJSON(testJsonNode);
        newMapping.setUpdateTime(Instant.now());

        mappingService.saveOrUpdate(newMapping);

        Mapping saved = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "12345");
        assertNotNull(saved);
        assertEquals(Platform.MyAnimeList, saved.getSourcePlatform());
        assertEquals("12345", saved.getPlatformId());
        assertEquals(8.5, saved.getRawScore());
        assertEquals(85.0, saved.getNormalizedScore());
    }

    @Test
    public void testSaveOrUpdate_ExistingMapping_UpdatesFields() {
        Mapping existingMapping = new Mapping();
        existingMapping.setSourcePlatform(Platform.MyAnimeList);
        existingMapping.setPlatformId("12345");
        existingMapping.setRawScore(8.0);
        existingMapping.setNormalizedScore(80.0);
        existingMapping.setRawJSON(objectMapper.createObjectNode().put("old", "data"));
        existingMapping.setUpdateTime(Instant.parse("2024-01-01T00:00:00Z"));
        existingMapping = mappingRepository.save(existingMapping);

        Long originalId = existingMapping.getMappingId();
        Integer originalVersion = existingMapping.getVersion();

        Mapping updateMapping = new Mapping();
        updateMapping.setSourcePlatform(Platform.MyAnimeList);
        updateMapping.setPlatformId("12345");
        updateMapping.setRawScore(8.5);
        updateMapping.setNormalizedScore(85.0);
        updateMapping.setRawJSON(testJsonNode);
        Instant newUpdateTime = Instant.now();
        updateMapping.setUpdateTime(newUpdateTime);

        mappingService.saveOrUpdate(updateMapping);

        Mapping updated = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "12345");
        assertNotNull(updated);
        assertEquals(originalId, updated.getMappingId());
        assertEquals(8.5, updated.getRawScore());
        assertEquals(85.0, updated.getNormalizedScore());
        assertEquals(testJsonNode, updated.getRawJSON());
        assertEquals(newUpdateTime, updated.getUpdateTime());
        assertEquals(originalVersion + 1, updated.getVersion());
    }

    @Test
    public void testSaveOrUpdate_ExistingMapping_PreservesAnimeRelationship() {
        AnimeTitles titles = new AnimeTitles();
        titles.setTitleNative("Test Anime");
        Anime anime = new Anime();
        anime.setTitle(titles);
        anime = animeRepository.save(anime);

        Mapping existingMapping = new Mapping();
        existingMapping.setSourcePlatform(Platform.MyAnimeList);
        existingMapping.setPlatformId("12345");
        existingMapping.setRawScore(8.0);
        existingMapping.setAnime(anime);
        existingMapping.setUpdateTime(Instant.now());
        mappingRepository.save(existingMapping);

        Mapping updateMapping = new Mapping();
        updateMapping.setSourcePlatform(Platform.MyAnimeList);
        updateMapping.setPlatformId("12345");
        updateMapping.setRawScore(8.5);
        updateMapping.setUpdateTime(Instant.now());

        mappingService.saveOrUpdate(updateMapping);

        Mapping updated = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "12345");
        assertNotNull(updated);
        assertNotNull(updated.getAnime());
        assertEquals(anime.getAnimeId(), updated.getAnime().getAnimeId());
        assertEquals(8.5, updated.getRawScore());
    }

    @Test
    public void testSaveOrUpdate_DifferentPlatforms() {
        Mapping malMapping = new Mapping();
        malMapping.setSourcePlatform(Platform.MyAnimeList);
        malMapping.setPlatformId("12345");
        malMapping.setRawScore(8.5);
        malMapping.setUpdateTime(Instant.now());

        Mapping bangumiMapping = new Mapping();
        bangumiMapping.setSourcePlatform(Platform.Bangumi);
        bangumiMapping.setPlatformId("67890");
        bangumiMapping.setRawScore(7.8);
        bangumiMapping.setUpdateTime(Instant.now());

        mappingService.saveOrUpdate(malMapping);
        mappingService.saveOrUpdate(bangumiMapping);

        Mapping savedMal = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "12345");
        Mapping savedBangumi = mappingRepository.findBySourcePlatformAndPlatformId(Platform.Bangumi, "67890");

        assertNotNull(savedMal);
        assertNotNull(savedBangumi);
        assertNotEquals(savedMal.getMappingId(), savedBangumi.getMappingId());
    }

    @Test
    public void testSaveOrUpdate_UpdateScoreToNull() {
        Mapping existingMapping = new Mapping();
        existingMapping.setSourcePlatform(Platform.MyAnimeList);
        existingMapping.setPlatformId("12345");
        existingMapping.setRawScore(8.5);
        existingMapping.setNormalizedScore(85.0);
        existingMapping.setUpdateTime(Instant.now());
        mappingRepository.save(existingMapping);

        Mapping updateMapping = new Mapping();
        updateMapping.setSourcePlatform(Platform.MyAnimeList);
        updateMapping.setPlatformId("12345");
        updateMapping.setRawScore(null);
        updateMapping.setNormalizedScore(null);
        updateMapping.setUpdateTime(Instant.now());

        mappingService.saveOrUpdate(updateMapping);

        Mapping updated = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "12345");
        assertNotNull(updated);
        assertNull(updated.getRawScore());
        assertNull(updated.getNormalizedScore());
    }

    @Test
    public void testSaveOrUpdate_UpdateJsonToNull() {
        Mapping existingMapping = new Mapping();
        existingMapping.setSourcePlatform(Platform.MyAnimeList);
        existingMapping.setPlatformId("12345");
        existingMapping.setRawJSON(testJsonNode);
        existingMapping.setUpdateTime(Instant.now());
        mappingRepository.save(existingMapping);

        Mapping updateMapping = new Mapping();
        updateMapping.setSourcePlatform(Platform.MyAnimeList);
        updateMapping.setPlatformId("12345");
        updateMapping.setRawJSON(null);
        updateMapping.setUpdateTime(Instant.now());

        mappingService.saveOrUpdate(updateMapping);

        Mapping updated = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "12345");
        assertNotNull(updated);
        assertNull(updated.getRawJSON());
    }

    @Test
    public void testSaveOrUpdate_SamePlatformDifferentIds() {
        Mapping mapping1 = new Mapping();
        mapping1.setSourcePlatform(Platform.MyAnimeList);
        mapping1.setPlatformId("12345");
        mapping1.setRawScore(8.5);
        mapping1.setUpdateTime(Instant.now());

        Mapping mapping2 = new Mapping();
        mapping2.setSourcePlatform(Platform.MyAnimeList);
        mapping2.setPlatformId("67890");
        mapping2.setRawScore(7.8);
        mapping2.setUpdateTime(Instant.now());

        mappingService.saveOrUpdate(mapping1);
        mappingService.saveOrUpdate(mapping2);

        Mapping saved1 = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "12345");
        Mapping saved2 = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "67890");

        assertNotNull(saved1);
        assertNotNull(saved2);
        assertNotEquals(saved1.getMappingId(), saved2.getMappingId());
        assertEquals(8.5, saved1.getRawScore());
        assertEquals(7.8, saved2.getRawScore());
    }

    @Test
    public void testSaveOrUpdate_MultipleUpdatesToSameMapping() {
        Mapping initialMapping = new Mapping();
        initialMapping.setSourcePlatform(Platform.MyAnimeList);
        initialMapping.setPlatformId("12345");
        initialMapping.setRawScore(8.0);
        initialMapping.setUpdateTime(Instant.now());

        mappingService.saveOrUpdate(initialMapping);

        Mapping update1 = new Mapping();
        update1.setSourcePlatform(Platform.MyAnimeList);
        update1.setPlatformId("12345");
        update1.setRawScore(8.5);
        update1.setUpdateTime(Instant.parse("2024-01-01T00:00:00Z"));

        mappingService.saveOrUpdate(update1);

        Mapping update2 = new Mapping();
        update2.setSourcePlatform(Platform.MyAnimeList);
        update2.setPlatformId("12345");
        update2.setRawScore(9.0);
        update2.setUpdateTime(Instant.parse("2024-01-02T00:00:00Z"));

        mappingService.saveOrUpdate(update2);

        Mapping finalMapping = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "12345");
        assertNotNull(finalMapping);
        assertEquals(9.0, finalMapping.getRawScore());
        assertEquals(Instant.parse("2024-01-02T00:00:00Z"), finalMapping.getUpdateTime());
        assertEquals(2, finalMapping.getVersion());
    }

    @Test
    public void testSaveOrUpdate_WithZeroScores() {
        Mapping newMapping = new Mapping();
        newMapping.setSourcePlatform(Platform.MyAnimeList);
        newMapping.setPlatformId("12345");
        newMapping.setRawScore(0.0);
        newMapping.setNormalizedScore(0.0);
        newMapping.setUpdateTime(Instant.now());

        mappingService.saveOrUpdate(newMapping);

        Mapping saved = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "12345");
        assertNotNull(saved);
        assertEquals(0.0, saved.getRawScore());
        assertEquals(0.0, saved.getNormalizedScore());
    }

    @Test
    public void testSaveOrUpdate_NaturalIdUniqueness() {
        Mapping mapping1 = new Mapping();
        mapping1.setSourcePlatform(Platform.MyAnimeList);
        mapping1.setPlatformId("12345");
        mapping1.setRawScore(8.5);
        mapping1.setUpdateTime(Instant.now());
        mappingService.saveOrUpdate(mapping1);

        Mapping mapping2 = new Mapping();
        mapping2.setSourcePlatform(Platform.MyAnimeList);
        mapping2.setPlatformId("12345");
        mapping2.setRawScore(9.0);
        mapping2.setUpdateTime(Instant.now());
        mappingService.saveOrUpdate(mapping2);

        long count = mappingRepository.count();
        assertEquals(1, count);

        Mapping saved = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "12345");
        assertEquals(9.0, saved.getRawScore());
    }

    @Test
    public void testSaveOrUpdate_OptimisticLockingVersion() {
        Mapping initialMapping = new Mapping();
        initialMapping.setSourcePlatform(Platform.MyAnimeList);
        initialMapping.setPlatformId("12345");
        initialMapping.setRawScore(8.0);
        initialMapping.setUpdateTime(Instant.now());
        mappingService.saveOrUpdate(initialMapping);

        Mapping saved = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "12345");
        assertEquals(0, saved.getVersion());

        Mapping updateMapping = new Mapping();
        updateMapping.setSourcePlatform(Platform.MyAnimeList);
        updateMapping.setPlatformId("12345");
        updateMapping.setRawScore(8.5);
        updateMapping.setUpdateTime(Instant.now());
        mappingService.saveOrUpdate(updateMapping);

        Mapping updated = mappingRepository.findBySourcePlatformAndPlatformId(Platform.MyAnimeList, "12345");
        assertEquals(1, updated.getVersion());
    }
}
