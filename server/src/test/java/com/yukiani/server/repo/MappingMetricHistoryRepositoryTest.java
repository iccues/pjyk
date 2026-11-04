package com.yukiani.server.repo;

import com.yukiani.server.entity.MappingMetricHistory;
import com.yukiani.server.entity.Platform;
import com.yukiani.server.entity.PlatformKey;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Transactional
class MappingMetricHistoryRepositoryTest {
    @Resource MappingMetricHistoryRepository repository;
    @Resource EntityManagerFactory entityManagerFactory;
    @Resource EntityManager entityManager;

    private static final Instant START = Instant.parse("2024-01-01T00:00:00Z");
    private static final Instant END = START.plusSeconds(86400);

    @Test
    void matchesPairsAcrossPlatformsWithOneStatementPerQuery() {
        var bgm = save(Platform.Bangumi, "same", START, 7.0);
        var mal = save(Platform.MyAnimeList, "same", START, 8.0);
        var quoted = save(Platform.AniList, "id' OR 1=1 --", START, 70.0);
        save(Platform.AniList, "same", START, 99.0);
        save(Platform.Bangumi, "id' OR 1=1 --", START, 99.0);
        save(Platform.Bangumi, "same", END, 99.0);
        var keys = List.of(new PlatformKey(Platform.Bangumi, "same"),
                new PlatformKey(Platform.MyAnimeList, "same"),
                new PlatformKey(Platform.AniList, "id' OR 1=1 --"));
        var expected = List.of(bgm.getHistoryId(), mal.getHistoryId(), quoted.getHistoryId());
        entityManager.clear();
        var stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        var series = repository.querySeries(keys, START, END);
        assertEquals(1, stats.getPrepareStatementCount());
        assertEquals(3, series.size());
        assertTrue(series.stream().allMatch(h -> keys.contains(h.getPlatformKey())));
        assertTrue(series.stream().allMatch(h -> expected.contains(h.getHistoryId())));
        stats.clear();
        var latest = repository.queryLatestBefore(keys, END);
        assertEquals(1, stats.getPrepareStatementCount());
        assertEquals(3, latest.size());
        assertTrue(latest.stream().allMatch(h -> expected.contains(h.getHistoryId())));
    }

    @Test
    void handlesDuplicateKeysAndTimestampTies() {
        save(Platform.Bangumi, "tie", START.minusSeconds(1), 6.0);
        var first = save(Platform.Bangumi, "tie", START, 7.0);
        var last = save(Platform.Bangumi, "tie", START, null);
        var key = new PlatformKey(Platform.Bangumi, "tie");
        var keys = List.of(key, key);
        assertEquals(List.of(first.getHistoryId(), last.getHistoryId()),
                repository.querySeries(keys, START, END).stream().map(MappingMetricHistory::getHistoryId).toList());
        var latest = repository.queryLatestBefore(keys, END);
        assertEquals(1, latest.size());
        assertEquals(last.getHistoryId(), latest.getFirst().getHistoryId());
        assertNull(latest.getFirst().getRawScore());
    }

    @Test
    void emptyKeysReturnEmptyResults() {
        assertTrue(repository.querySeries(List.of(), START, END).isEmpty());
        assertTrue(repository.queryLatestBefore(List.of(), END).isEmpty());
    }

    private MappingMetricHistory save(Platform platform, String id, Instant time, Double score) {
        return repository.saveAndFlush(new MappingMetricHistory(new PlatformKey(platform, id), score, null, time));
    }
}
