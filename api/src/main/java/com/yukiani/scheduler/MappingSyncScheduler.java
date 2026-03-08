package com.yukiani.scheduler;

import com.yukiani.service.MappingSyncService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MappingSyncScheduler {

    @Resource
    MappingSyncService mappingSyncService;

    /**
     * 每天凌晨 4 点收集需要同步的 mappings
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void scheduleDailyMappingCollection() {
        log.info("=== Daily mapping collection started ===");
        try {
            mappingSyncService.collectMappingsForSync();
            log.info("=== Daily mapping collection completed ===");
        } catch (Exception e) {
            log.error("=== Daily mapping collection failed: {} ===", e.getMessage());
        }
    }

    /**
     * 每 6 小时同步一次待处理的 mappings
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void scheduleMappingSync() {
        log.info("=== Periodic mapping sync started ===");
        try {
            mappingSyncService.processPendingMappings();
            log.info("=== Periodic mapping sync completed ===");
        } catch (Exception e) {
            log.error("=== Periodic mapping sync failed: {} ===", e.getMessage());
        }
    }

    /**
     * 应用启动时初始化：收集 mappings 并触发首次同步
     */
//    @PostConstruct
    public void init() {
        log.info("Initializing mapping sync scheduler");
        try {
            mappingSyncService.collectMappingsForSync();
            mappingSyncService.processPendingMappings();
            log.info("Mapping sync scheduler initialized");
        } catch (Exception e) {
            log.error("Failed to initialize mapping sync scheduler: {}", e.getMessage());
        }
    }
}
