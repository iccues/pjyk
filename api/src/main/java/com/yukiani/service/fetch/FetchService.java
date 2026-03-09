package com.yukiani.service.fetch;

import com.yukiani.entity.Platform;
import com.yukiani.entity.Season;
import com.yukiani.service.TitleBasedLinkService;
import com.yukiani.service.MetricService;
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
    TitleBasedLinkService titleBasedLinkService;
    @Resource
    MetricService metricService;

    @Async
    public void fetchAnime(int year, Season season) {
        fetchMapping(year, season);
        linkMappings();
    }

    @Async
    public void fetchMapping(int year, Season season) {
        safeFetchMappings(bangumiFetchService, Platform.Bangumi, year, season);
        safeFetchMappings(aniListFetchService, Platform.AniList, year, season);
        safeFetchMappings(myAnimeListFetchService, Platform.MyAnimeList, year, season);
    }

    private void safeFetchMappings(AbstractAnimeFetchService service, Platform platform, int year, Season season) {
        try {
            service.fetchAndSaveMappings(year, season);
            log.debug("{} fetch completed for year={}, season={}", platform, year, season);
        } catch (Exception e) {
            log.error("{} fetch failed for year={}, season={}: {}", platform, year, season, e.getMessage());
        }
    }

    @Async
    public void linkMappings() {
        try {
            titleBasedLinkService.linkAllOrphanedMappings();
            log.debug("link mappings completed");
        } catch (Exception e) {
            log.error("link mappings failed: {}", e.getMessage());
        }
    }

    @Async
    public void calculateAllMetric() {
        metricService.calculateAllMetric();
    }

    public AbstractAnimeFetchService getFetchService(Platform platform) {
        return switch (platform) {
            case Bangumi -> bangumiFetchService;
            case AniList -> aniListFetchService;
            case MyAnimeList -> myAnimeListFetchService;
        };
    }
}
