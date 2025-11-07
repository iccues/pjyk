package com.iccues.metaanimebackend.jobs;

import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.service.*;
import com.iccues.metaanimebackend.service.fetch.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AnimeFetchJob {

    @Resource
    BangumiFetchService bangumiFetchService;
    @Resource
    AniListFetchService aniListFetchService;
    @Resource
    MyAnimeListFetchService myAnimeListFetchService;

    @Resource
    ScoreService scoreService;

    @Scheduled(cron = "0 0 4 * * *")
    public void fetchAnime() {
        bangumiFetchService.fetchAnime(2025, Season.FALL);
        aniListFetchService.fetchAnime(2025, Season.FALL);
        myAnimeListFetchService.fetchAnime(2025, Season.FALL);

        scoreService.calculateAverageScore();
    }

    @PostConstruct
    public void init() {
        fetchAnime();
    }
}
