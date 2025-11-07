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
        log.info("Fetch anime start");

        bangumiFetchService.fetchAnime(year, season);
        aniListFetchService.fetchAnime(year, season);
        myAnimeListFetchService.fetchAnime(year, season);

        log.info("Fetch anime end, calculate average score start");

        scoreService.calculateAverageScore();

        log.info("Calculate average score end");
    }
}
