package com.yukiani.service;

import com.yukiani.entity.Anime;
import com.yukiani.entity.Mapping;
import com.yukiani.entity.Platform;
import com.yukiani.entity.ReviewStatus;
import com.yukiani.repo.AnimeRepository;
import com.yukiani.service.fetch.AbstractAnimeFetchService;
import com.yukiani.service.fetch.FetchService;
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
    MetricService metricService;

    List<Mapping> pendingMappings = Collections.synchronizedList(new ArrayList<>());

    /**
     * 收集需要同步的 mappings（最近 90 天内开播的已审核动漫）
     * 每天执行一次，会先报告并清空前一天失败的 mappings
     */
    @Transactional(readOnly = true)
    public void collectMappingsForSync() {
        // 报告前一天未成功同步的 mappings
        if (!pendingMappings.isEmpty()) {
            log.error("Found {} mapping(s) that failed to sync yesterday", pendingMappings.size());
            // 只在 DEBUG 级别输出详细列表，避免日志过多
            if (log.isDebugEnabled()) {
                pendingMappings.forEach(mapping ->
                    log.debug("Failed mapping: {}:{}", mapping.getSourcePlatform(), mapping.getPlatformId())
                );
            }
        }

        // 清空前一天的队列，开始新的一天
        pendingMappings.clear();

        List<Anime> animeList = animeRepository.findAllByReviewStatus(ReviewStatus.APPROVED);

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
        int total = pendingMappings.size();
        long startTime = System.currentTimeMillis();
        
        log.info("Processing {} pending mapping(s)", total);

        List.copyOf(pendingMappings)
                .parallelStream()
                .forEach(this::syncMapping);

        int failed = pendingMappings.size();
        int success = total - failed;
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("Mapping sync completed - Total: {}, Success: {}, Failed: {}, Duration: {}ms", 
            total, success, failed, duration);
    }

    private boolean shouldSyncAnime(Anime anime) {
        if (anime.getStartDate() == null) {
            log.warn("Anime {} has no start date, skipping sync", anime.getAnimeId());
            return false;
        }

        long today = LocalDate.now().toEpochDay();
        long startDate = anime.getStartDate().toEpochDay();
        long daysSinceStart = today - startDate;

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
            log.debug("Successfully synced mapping {}:{}", platform, platformId);
        } catch (Exception e) {
            log.warn("Failed to sync mapping {}:{} - {}", platform, platformId, e.getMessage());
        }
    }
}
