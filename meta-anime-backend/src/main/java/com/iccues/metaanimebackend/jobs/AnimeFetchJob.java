package com.iccues.metaanimebackend.jobs;

import com.iccues.metaanimebackend.entity.Season;
import com.iccues.metaanimebackend.service.fetch.FetchService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AnimeFetchJob {

    @Resource
    FetchService fetchService;

    @Scheduled(cron = "0 0 4 * * *")
    public void fetchAnime() {
        fetchService.fetchAnime(2025, Season.SUMMER);
    }

    @PostConstruct
    public void init() {
        fetchAnime();
    }
}
