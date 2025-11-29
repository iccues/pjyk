package com.iccues.metaanimebackend.service.fetch;

import com.iccues.metaanimebackend.entity.Platform;
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
        linkMappings();
        calculateAllAverageScore();
    }

    @Async
    public void fetchMapping(int year, Season season) {
        log.info("Fetch mapping start for year={}, season={}", year, season);
        safeFetchMappings(bangumiFetchService, Platform.Bangumi, year, season);
        safeFetchMappings(aniListFetchService, Platform.AniList, year, season);
        safeFetchMappings(myAnimeListFetchService, Platform.MyAnimeList, year, season);
        log.info("Fetch mapping end for year={}, season={}", year, season);
    }

    private void safeFetchMappings(AbstractAnimeFetchService service, Platform platform, int year, Season season) {
        try {
            service.fetchAndSaveMappings(year, season);
            log.info("{} fetch completed successfully for year={}, season={}", platform, year, season);
        } catch (Exception e) {
            log.error("{} fetch failed for year={}, season={}", platform, year, season, e);
        }
    }

    @Async
    public void linkMappings() {
        log.info("Merge mapping start");
        safeLinkMappings(bangumiFetchService, Platform.Bangumi);
        safeLinkMappings(aniListFetchService, Platform.AniList);
        safeLinkMappings(myAnimeListFetchService, Platform.MyAnimeList);
        log.info("Merge mapping end");
    }

    private void safeLinkMappings(AbstractAnimeFetchService service, Platform platform) {
        try {
            service.linkAllOrphanedMappings();
            log.info("{} link mappings completed successfully", platform);
        } catch (Exception e) {
            log.error("{} link mappings failed", platform, e);
        }
    }

    @Async
    public void calculateAllAverageScore() {
        log.info("Calculate average score start");
        scoreService.calculateAllAverageScore();
        log.info("Calculate average score end");
    }

    public AbstractAnimeFetchService getFetchService(Platform platform) {
        return switch (platform) {
            case Bangumi -> bangumiFetchService;
            case AniList -> aniListFetchService;
            case MyAnimeList -> myAnimeListFetchService;
        };
    }
}
