package com.yukiani.server.scheduler;

import com.yukiani.server.service.MappingSyncService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定期触发外部平台 Mapping 同步。
 */
@Component
@Slf4j
public class MappingSyncScheduler {

    @Resource
    MappingSyncService mappingSyncService;

    /**
     * 每小时整点执行一轮同步，只处理到期且不在退避期内的 Mapping。
     *
     * <p>在调度线程上同步执行；上一轮未结束时，Spring 会跳过错过的触发时刻，不会重叠执行。</p>
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void scheduleMappingSync() {
        log.info("=== Mapping sync round started ===");
        try {
            mappingSyncService.runSyncRound();
            log.info("=== Mapping sync round completed ===");
        } catch (Exception e) {
            log.error("=== Mapping sync round failed: {} ===", e.getMessage(), e);
        }
    }
}
