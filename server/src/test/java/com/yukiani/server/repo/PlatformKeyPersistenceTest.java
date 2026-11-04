package com.yukiani.server.repo;

import com.yukiani.server.entity.Anime;
import com.yukiani.server.entity.Mapping;
import com.yukiani.server.entity.Platform;
import com.yukiani.server.entity.PlatformKey;
import com.yukiani.server.entity.ReviewStatus;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PlatformKeyPersistenceTest {

    @Resource
    private MappingRepository mappingRepository;

    @Resource
    private AnimeRepository animeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void rejectsDuplicatePlatformKeysInTheDatabase() {
        mappingRepository.saveAndFlush(new Mapping(new PlatformKey(Platform.Bangumi, "12345"), null));
        entityManager.clear();

        assertThrows(DataIntegrityViolationException.class,
                () -> mappingRepository.saveAndFlush(new Mapping(new PlatformKey(Platform.Bangumi, "12345"), null)));
    }

    @Test
    void projectsPlatformKeyIntoSyncCandidates() {
        Instant lastUpdated = Instant.parse("2026-10-01T00:00:00Z");
        PlatformKey key = new PlatformKey(Platform.AniList, "12345");
        Anime anime = new Anime();
        anime.setReviewStatus(ReviewStatus.APPROVED);
        Mapping mapping = new Mapping(key, null);
        mapping.setUpdateTime(lastUpdated);
        anime.addMapping(mapping);
        anime = animeRepository.saveAndFlush(anime);
        Long mappingId = anime.getMappings().getFirst().getMappingId();
        entityManager.clear();

        SyncCandidate candidate = mappingRepository.findSyncCandidates(ReviewStatus.APPROVED, lastUpdated.plusSeconds(1))
                .stream().filter(value -> value.mappingId().equals(mappingId)).findFirst().orElseThrow();

        assertEquals(key, candidate.platformKey());
        assertEquals(lastUpdated, candidate.updateTime());
    }
}
