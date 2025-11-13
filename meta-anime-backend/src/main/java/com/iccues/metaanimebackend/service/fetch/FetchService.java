package com.iccues.metaanimebackend.service.fetch;

import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.service.ScoreService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FetchService {

    @Resource
    BangumiFetchService bangumiFetchService;
    @Resource
    AniListFetchService aniListFetchService;
    @Resource
    MyAnimeListFetchService myAnimeListFetchService;

    @Resource
    ScoreService scoreService;

    @Async
    public void fetchAnime(int year, Season season) {
        fetchMapping(year, season);
        mergeMappings();
        calculateAllAverageScore();
    }

    @Async
    public void fetchMapping(int year, Season season) {
        log.info("Fetch mapping start");
        bangumiFetchService.fetchAndSaveMappings(year, season);
        aniListFetchService.fetchAndSaveMappings(year, season);
        myAnimeListFetchService.fetchAndSaveMappings(year, season);
        log.info("Fetch mapping end");
    }

    @Async
    public void mergeMappings() {
        log.info("Merge mapping start");
        bangumiFetchService.linkAllOrphanedMappings();
        aniListFetchService.linkAllOrphanedMappings();
        myAnimeListFetchService.linkAllOrphanedMappings();
        log.info("Merge mapping end");
    }

    @Async
    public void calculateAllAverageScore() {
        log.info("Calculate average score start");
        scoreService.calculateAllAverageScore();
        log.info("Calculate average score end");
    }

    public AbstractAnimeFetchService getFetchServiceByName(String platformName) {
        return switch (platformName) {
            case "Bangumi" -> bangumiFetchService;
            case "AniList" -> aniListFetchService;
            case "MyAnimeList" -> myAnimeListFetchService;
            default -> null;
        };
    }
}
