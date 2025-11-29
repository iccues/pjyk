package com.iccues.metaanimebackend.service;

import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.entity.Mapping;
import com.iccues.metaanimebackend.entity.Platform;
import com.iccues.metaanimebackend.entity.ReviewStatus;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import com.iccues.metaanimebackend.service.fetch.AbstractAnimeFetchService;
import com.iccues.metaanimebackend.service.fetch.FetchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class MappingSyncService {

    @Resource
    AnimeRepository animeRepository;

    @Resource
    FetchService fetchService;

    @Resource
    ScoreService scoreService;

    List<Mapping> pendingMappings = Collections.synchronizedList(new ArrayList<>());

    /**
     * 收集需要同步的 mappings（最近 90 天内开播的已审核动漫）
     * 每天执行一次，会先报告并清空前一天失败的 mappings
     */
    @Transactional(readOnly = true)
    public void collectMappingsForSync() {
        log.info("Starting to collect mappings for sync");

        // 报告前一天未成功同步的 mappings
        if (!pendingMappings.isEmpty()) {
            log.warn("Found {} mapping(s) that failed to sync yesterday:", pendingMappings.size());
            for (Mapping mapping : pendingMappings) {
                log.warn("  - {}:{}", mapping.getSourcePlatform(), mapping.getPlatformId());
            }
        }

        // 清空前一天的队列，开始新的一天
        pendingMappings.clear();

        List<Anime> animeList = animeRepository.findAllByReviewStatus(ReviewStatus.APPROVED);
        log.info("Found {} approved anime(s)", animeList.size());

        // 在事务内收集所有需要同步的 mappings，避免懒加载异常
        pendingMappings.addAll(animeList.stream()
                .filter(this::shouldSyncAnime)
                .flatMap(anime -> anime.getMappings().stream())
                .toList());

        log.info("Collected {} new mapping(s) for today", pendingMappings.size());
    }

    /**
     * 处理待同步的 mappings（异步并行执行）
     */
    @Async
    public void processPendingMappings() {
        log.info("Starting to process {} pending mapping(s)", pendingMappings.size());

        List.copyOf(pendingMappings)
                .parallelStream()
                .forEach(this::syncMapping);

        log.info("Mapping sync completed - Failed/Remaining: {}", pendingMappings.size());

        scoreService.calculateAllAverageScore();
        log.info("Average score calculation completed");
    }

    private boolean shouldSyncAnime(Anime anime) {
        if (anime.getStartDate() == null) {
            log.warn("Anime {} has no start date, skipping sync", anime.getAnimeId());
            return false;
        }

        long today = LocalDate.now().toEpochDay();
        long startDate = anime.getStartDate().toEpochDay();
        long daysSinceStart = today - startDate;

//        // 同步最近 90 天内开播的动漫（修正了日期计算逻辑）
//        return daysSinceStart >= 0 && daysSinceStart < 90;
        if (daysSinceStart <= 0) {
            return false;
        }
        if (daysSinceStart < 30) {
            return true;
        }
        if (daysSinceStart < 90) {
            return today % 2 == anime.getAnimeId() % 2;
        }
        if (daysSinceStart < 180) {
            return today % 7 == anime.getAnimeId() % 7;
        }
        return today % 30 == anime.getAnimeId() % 30;
    }

    /**
     * 同步单个 mapping 的数据
     * 成功时从待处理队列中移除，失败时保留在队列中等待下次重试
     */
    @Transactional
    public void syncMapping(Mapping mapping) {
        Platform platform = mapping.getSourcePlatform();
        String platformId = mapping.getPlatformId();

        try {
            AbstractAnimeFetchService service = fetchService.getFetchService(platform);
            log.debug("Syncing mapping {}:{}", platform, platformId);
            service.fetchAndSaveMapping(platformId);
            pendingMappings.remove(mapping);
            log.info("Successfully synced mapping {}:{}", platform, platformId);
        } catch (Exception e) {
            log.error("Failed to sync mapping {}:{} - {}", platform, platformId, e.getMessage(), e);
        }
    }
}
